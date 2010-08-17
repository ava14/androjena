/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.resultset.XMLInput ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileManager ;


public class QueryEngineHTTP implements QueryExecution
{
    public static final String QUERY_MIME_TYPE = "application/sparql-query" ;
    String queryString ;
    String service ;
    Context context = null ;
    
    //Params
    Params params = null ;
    
    // Protocol
    List<String> defaultGraphURIs = new ArrayList<String>() ;
    List<String> namedGraphURIs  = new ArrayList<String>() ;
    private String user = null ;
    private char[] password = null ;
    
    public QueryEngineHTTP(String serviceURI, Query query)
    { 
        this(serviceURI, query.toString()) ;
    }
    
    public QueryEngineHTTP(String serviceURI, String queryString)
    { 
        this.queryString = queryString ;
        service = serviceURI ;
        // Copy the global context to freeze it.
        context = new Context(ARQ.getContext()) ;
    }

//    public void setParams(Params params)
//    { this.params = params ; }
    
    // Meaning-less
    public void setFileManager(FileManager fm)
    { throw new UnsupportedOperationException("FileManagers do not apply to remote query execution") ; }  

    public void setInitialBinding(QuerySolution binding)
    { throw new UnsupportedOperationException("Initial bindings not supported for remote queries") ; }
    
    public void setInitialBindings(ResultSet table)
    { throw new UnsupportedOperationException("Initial bindings not supported for remote queries") ; }
    
    /**  @param defaultGraphURIs The defaultGraphURIs to set. */
    public void setDefaultGraphURIs(List<String> defaultGraphURIs)
    {
        this.defaultGraphURIs = defaultGraphURIs ;
    }

    /**  @param namedGraphURIs The namedGraphURIs to set. */
    public void setNamedGraphURIs(List<String> namedGraphURIs)
    {
        this.namedGraphURIs = namedGraphURIs ;
    }

    public void addParam(String field, String value)
    {
        if ( params == null )
            params = new Params() ;
        params.addParam(field, value) ;
    }
    
    /** @param defaultGraph The defaultGraph to add. */
    public void addDefaultGraph(String defaultGraph)
    {
        if ( defaultGraphURIs == null )
            defaultGraphURIs = new ArrayList<String>() ;
        defaultGraphURIs.add(defaultGraph) ;
    }

    /** @param name The URI to add. */
    public void addNamedGraph(String name)
    {
        if ( namedGraphURIs == null )
            namedGraphURIs = new ArrayList<String>() ;
        namedGraphURIs.add(name) ;
    }
    
    /** Set user and password for basic authentication.
     *  After the request is made (one of the exec calls), the application
     *  can overwrite the password array to remove details of the secret.
     * @param user
     * @param password
     */
    public void setBasicAuthentication(String user, char[] password)
    {
        this.user = user ;
        this.password = password ;
    }
    
    public ResultSet execSelect()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        // TODO Allow other content types.
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        ResultSet rs = ResultSetFactory.fromXML(in) ;
        return rs ;
    }

    public Model execConstruct()             { return execConstruct(GraphFactory.makeJenaDefaultModel()) ; }
    
    public Model execConstruct(Model model)  { return execModel(model) ; }

    public Model execDescribe()              { return execDescribe(GraphFactory.makeJenaDefaultModel()) ; }
    
    public Model execDescribe(Model model)   { return execModel(model) ; }

    private Model execModel(Model model)
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeRDFXML) ;
        InputStream in = httpQuery.exec() ;
        model.read(in, null) ;
        return model ;
    }
    
    public boolean execAsk()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        return XMLInput.booleanFromXML(in) ;
    }

    public Context getContext() { return context ; }
    
    private HttpQuery makeHttpQuery()
    {
        HttpQuery httpQuery = new HttpQuery(service) ;
        httpQuery.addParam(HttpParams.pQuery, queryString );
        
        for ( Iterator<String> iter = defaultGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String dft = iter.next() ;
            httpQuery.addParam(HttpParams.pDefaultGraph, dft) ;
        }
        for ( Iterator<String> iter = namedGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String name = iter.next() ;
            httpQuery.addParam(HttpParams.pNamedGraph, name) ;
        }
        
        if ( params != null )
            httpQuery.merge(params) ;
        
        httpQuery.setBasicAuthentication(user, password) ;
        return httpQuery ;
    }
    
    public void abort() { }

    public void close() { }

//    public boolean isActive() { return false ; }
    
    @Override
    public String toString()
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        return "GET "+httpQuery.toString() ;
    }

    public Dataset getDataset()
    {
        return null ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
