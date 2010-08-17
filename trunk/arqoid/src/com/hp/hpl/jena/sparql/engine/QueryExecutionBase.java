/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.n3.IRIResolver ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandler ;
import com.hp.hpl.jena.sparql.core.describe.DescribeHandlerRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.syntax.ElementGroup ;
import com.hp.hpl.jena.sparql.syntax.Template ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;
import com.hp.hpl.jena.sparql.util.ModelUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.util.FileManager ;

/** All the SPARQL query result forms made from a graph-level execution object */ 

public class QueryExecutionBase implements QueryExecution
{
    // Pull over the "build dataset code"
    // Initial bindings.
    // Split : QueryExecutionGraph already has the dataset.

    private Query              query ;
    private Dataset            dataset ;
    private QueryEngineFactory qeFactory ;
    private QueryIterator      queryIterator = null ;
    private Plan               plan = null ;
    private Context            context ;
    private FileManager        fileManager = FileManager.get() ;
    private QuerySolution      initialBinding = null ;      

    public QueryExecutionBase(Query query, 
                              Dataset dataset,
                              Context context,
                              QueryEngineFactory qeFactory)
    {
        this.query = query ;
        this.dataset = dataset ;
        this.context = context ;
        this.qeFactory = qeFactory ;
    }
    
    public void abort()
    {
        if ( queryIterator != null )
            queryIterator.abort() ;
    }

    public void close()
    {
        if ( queryIterator != null )
            queryIterator.close() ;
        if ( plan != null )
            plan.close() ;
    }

    public ResultSet execSelect()
    {
        if ( ! query.isSelectType() )
            throw new QueryExecException("Attempt to have ResultSet from a "+labelForQuery(query)+" query") ; 
        return execResultSet() ;
    }


    // Construct
    public Model execConstruct()
    { return execConstruct(GraphFactory.makeJenaDefaultModel()) ; }

    public Model execConstruct(Model model)
    {
        if ( ! query.isConstructType() )
            throw new QueryExecException("Attempt to get a CONSTRUCT model from a "+labelForQuery(query)+" query") ;
        // This causes there to be no PROJECT around the pattern.
        // That in turn, exposes the initial bindings.  
        query.setQueryResultStar(true) ;

        startQueryIterator() ;
        
        // Prefixes for result
        insertPrefixesInto(model) ;
        Template template = query.getConstructTemplate() ;

        // Build each template substitution as triples.
        for ( ; queryIterator.hasNext() ; )
        {
            Set<Triple> set = new HashSet<Triple>() ;
            Map<Node, Node> bNodeMap = new HashMap<Node, Node>() ;
            Binding binding = queryIterator.nextBinding() ;
            template.subst(set, bNodeMap, binding) ; 

            // Convert and merge into Model.
            for ( Iterator<Triple> iter = set.iterator() ; iter.hasNext() ; )
            {
                Triple t = iter.next() ;
                Statement stmt = ModelUtils.tripleToStatement(model, t) ;
                if ( stmt != null )
                    model.add(stmt) ;
            }
        }
        this.close() ;
        return model ;
    }

    public Model execDescribe()
    { return execDescribe(GraphFactory.makeJenaDefaultModel()) ; }


    public Model execDescribe(Model model)
    {
        if ( ! query.isDescribeType() )
            throw new QueryExecException("Attempt to get a DESCRIBE result from a "+labelForQuery(query)+" query") ; 
        //Was: query.setQueryResultStar(true) ; but why?
        query.setResultVars() ;
        // If there was no WhereClause, use an empty pattern (one solution, no columns). 
        if ( query.getQueryPattern() == null )
            query.setQueryPattern(new ElementGroup()) ;
        
        Set<RDFNode> set = new HashSet<RDFNode>() ;

        //May return null (no query pattern) 
        ResultSet qRes = execResultSet() ;

        // Prefixes for result (after initialization)
        insertPrefixesInto(model) ;
        if ( qRes != null )
        {
            for ( ; qRes.hasNext() ; )
            {
                QuerySolution rb = qRes.nextSolution() ;
                for ( String varName : query.getResultVars() )
                {
                    RDFNode n = rb.get(varName) ;
                    set.add(n) ;
                }
            }
            this.close() ;
        }

        if ( query.getResultURIs() != null )
        {
            // Any URIs in the DESCRIBE
            for (Node n : query.getResultURIs())
            {
                // Need to make dataset available to describe handlers.
                RDFNode rNode = ModelUtils.convertGraphNodeToRDFNode(n, dataset.getDefaultModel()) ;
                set.add(rNode) ;
            }
        }

        // Create new handlers for this process.
        List<DescribeHandler> dhList = DescribeHandlerRegistry.get().newHandlerList() ;

        getContext().put(ARQConstants.sysCurrentDataset, getDataset()) ;
        // Notify start of describe phase
        for (DescribeHandler dh : dhList)
            dh.start(model, getContext()) ;

        // Do describe for each resource found.
        for (Iterator<RDFNode> iter = set.iterator() ; iter.hasNext() ;)
        {
            RDFNode n = iter.next() ;

            if ( n instanceof Resource )
            {
                for (DescribeHandler dh : dhList)
                    dh.describe((Resource)n) ;
            }
            else
                // Can't describe literals
                continue ;
        }

        for (DescribeHandler dh : dhList)
            dh.finish() ;

        this.close() ;
        return model ; 
    }

    public boolean execAsk()
    {
        if ( ! query.isAskType() )
            throw new QueryExecException("Attempt to have boolean from a "+labelForQuery(query)+" query") ; 

        startQueryIterator() ;
        boolean r = queryIterator.hasNext() ;
        this.close() ;
        return r ; 
    }

    protected void execInit() {}

    private ResultSet asResultSet(QueryIterator qIter)
    {
        Model model = null ;
        if ( dataset != null )
            model = dataset.getDefaultModel() ;
        else
            model = ModelFactory.createDefaultModel() ;
        
        ResultSetStream rStream = new ResultSetStream(query.getResultVars(), model, qIter) ;
        return rStream ;
    }
    
    private void startQueryIterator()
    {
        execInit() ;
        if ( queryIterator != null )
            ALog.warn(this, "Query iterator has already been started") ;
        queryIterator = getPlan().iterator() ;
    }
    
    private ResultSet execResultSet()
    {
        startQueryIterator() ;
        return asResultSet(queryIterator) ; 
    }

    public Plan getPlan() 
    {
        if ( plan == null )
        {
            DatasetGraph dsg = prepareDataset(dataset, query, fileManager) ;
            Binding inputBinding = null ;
            if ( initialBinding != null )
            {
                inputBinding = new BindingMap() ;
                BindingUtils.addToBinding(inputBinding, initialBinding) ;
            }
            if ( inputBinding == null )
                inputBinding = BindingRoot.create() ;

            plan = qeFactory.create(query, dsg, inputBinding, getContext()) ;
        }            
        return plan ;
    }
    
    private void insertPrefixesInto(Model model)
    {
        try {
            if ( dataset != null )
            {
                // Load the models prefixes first
                PrefixMapping m = dataset.getDefaultModel() ;
                model.setNsPrefixes(m) ;
            }
            // Then add the queries (just the declared mappings)
            // so the query declarations override the data sources. 
            model.setNsPrefixes(query.getPrefixMapping()) ;

        } catch (Exception ex)
        {
            ALog.warn(this, "Exception in insertPrefixes: "+ex.getMessage(), ex) ;
        }
    }

    static private String labelForQuery(Query q)
    {
        if ( q.isSelectType() )     return "SELECT" ; 
        if ( q.isConstructType() )  return "CONSTRUCT" ; 
        if ( q.isDescribeType() )   return "DESCRIBE" ; 
        if ( q.isAskType() )        return "ASK" ;
        return "<<unknown>>" ;
    }

    public Context getContext() { return context ; }
    
    public Dataset getDataset() { return dataset ; }
    
    // Call after setFM called.
    private static DatasetGraph prepareDataset(Dataset dataset, Query query, FileManager fileManager)
    {
        if ( dataset != null )
            return dataset.asDatasetGraph() ;
        
        if ( ! query.hasDatasetDescription() ) 
            //Query.ALog.warn(this, "No data for query (no URL, no model)");
            throw new QueryExecException("No dataset description for query");
        
        String baseURI = query.getBaseURI() ;
        if ( baseURI == null )
            baseURI = IRIResolver.chooseBaseURI() ;
        
        DatasetGraph dsg =
            DatasetUtils.createDatasetGraph(query.getGraphURIs(),
                                            query.getNamedGraphURIs(),
                                            fileManager, baseURI ) ;
        return dsg ;
    }

    
    public void setFileManager(FileManager fm) { fileManager = fm ; }
    
    public void setInitialBinding(QuerySolution startSolution)
    { 
        initialBinding = startSolution ;
    }

    
    //protected QuerySolution getInputBindings() { return initialBinding ; }

    public void setInitialBindings(ResultSet table)
    { throw new UnsupportedOperationException("setInitialBindings(ResultSet)") ; }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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