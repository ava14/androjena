/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelTestBase.java,v 1.1 2009/06/29 08:55:33 castagna Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.CollectionFactory;

import java.util.*;

/**
    provides useful functionality for testing models, eg building small models
    from strings, testing equality, etc.
    
 	@author kers
*/

public class ModelTestBase extends GraphTestBase
    {
    public ModelTestBase(String name)
        { super(name); }
     
    protected static Model aModel = extendedModel();
    
    protected static final Model empty = ModelFactory.createDefaultModel();
    
    protected static Model extendedModel()
        {
        Model result = ModelFactory.createDefaultModel();
        result.setNsPrefixes( PrefixMapping.Extended );
        return result;
        }
    
    protected static String nice( RDFNode n )
        { return nice( n.asNode() ); }
    
     /**
        create a Statement in a given Model with (S, P, O) extracted by parsing a string.
        
        @param m the model the statement is attached to
        @param an "S P O" string. 
        @return m.createStatement(S, P, O)
     */   
    public static Statement statement( Model m, String fact )
         {
         StringTokenizer st = new StringTokenizer( fact );
         Resource sub = resource( m, st.nextToken() );
         Property pred = property( m, st.nextToken() );
         RDFNode obj = rdfNode( m, st.nextToken() );
         return m.createStatement( sub, pred, obj );    
         }    
    
    public static Statement statement( String fact )
        { return statement( aModel, fact ); }
         
     public static RDFNode rdfNode( Model m, String s )
        { return m.asRDFNode( NodeCreateUtils.create( m, s ) ); }

     public static <T extends RDFNode> T rdfNode( Model m, String s, Class<T> c )
         { return rdfNode( m, s ).as(  c  );  }
     
     protected static Resource resource()
         { return ResourceFactory.createResource(); }
         
    public static Resource resource( String s )
        { return resource( aModel, s ); }
    
    public static Resource resource( Model m, String s )
        { return (Resource) rdfNode( m, s ); }
        
    public static Property property( String s )
        { return property( aModel, s ); }
    
    public static Property property( Model m, String s )
        { return rdfNode( m, s ).as( Property.class ); }
        
    public static Literal literal( Model m, String s )
        { return rdfNode( m, s ).as( Literal.class ); }
         
     /**
        Create an array of Statements parsed from a semi-separated string.
        
        @param m a model to serve as a statement factory
        @param facts a sequence of semicolon-separated "S P O" facts
        @return a Statement[] of the (S P O) statements from the string
     */
     public static Statement [] statements( Model m, String facts )
        {
        ArrayList<Statement> sl = new ArrayList<Statement>();
        StringTokenizer st = new StringTokenizer( facts, ";" );
        while (st.hasMoreTokens()) sl.add( statement( m, st.nextToken() ) );  
        return sl.toArray( new Statement[sl.size()] );
        }
        
    /**
        Create an array of Resources from a whitespace-separated string
        
        @param m a model to serve as a resource factory
        @param items a whitespace-separated sequence to feed to resource
        @return a RDFNode[] of the parsed resources
    */
    public static Resource [] resources( Model m, String items )
        {
        ArrayList<Resource> rl = new ArrayList<Resource>();
        StringTokenizer st = new StringTokenizer( items );
        while (st.hasMoreTokens()) rl.add( resource( m, st.nextToken() ) );  
        return rl.toArray( new Resource[rl.size()] );
        }    
    
    /**
        Answer the set of resources given by the space-separated 
        <code>items</code> string. Each resource specification is interpreted
        as per <code>resource</code>.
    */
    public static Set<Resource> resourceSet( String items )
        {
        Set<Resource> result = new HashSet<Resource>();
        StringTokenizer st = new StringTokenizer( items );
        while (st.hasMoreTokens()) result.add( resource( st.nextToken() ) );  
        return result;
        }
        
    /**
        add to a model all the statements expressed by a string.
        
        @param m the model to be updated
        @param facts a sequence of semicolon-separated "S P O" facts
        @return the updated model
    */
    public static Model modelAdd( Model m, String facts )
        {
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) m.add( statement( m, semis.nextToken() ) );   
        return m;
        }
    
    /**
        makes a model initialised with statements parsed from a string.
        
        @param facts a string in semicolon-separated "S P O" format
        @return a model containing those facts
    */
    public static Model modelWithStatements( String facts )
        { return modelWithStatements( ReificationStyle.Standard, facts ); }

    /**
        makes a model with a given reiifcation style, initialised with statements parsed 
        from a string.
        
        @param style the required reification style
        @param facts a string in semicolon-separated "S P O" format
        @return a model containing those facts
    */        
    public static Model modelWithStatements( ReificationStyle style, String facts )
        { return modelAdd( createModel( style ), facts ); }
        
    /**
        make a model with a given reification style, give it Extended prefixes
    */
    public static Model createModel( ReificationStyle style )
        {
    	Model result = ModelFactory.createDefaultModel( style );
        result.setNsPrefixes( PrefixMapping.Extended );
        return result;
        }
    
    /**
        Answer a default model; it exists merely to abbreviate the rather long explicit
        invocation.
        
     	@return a new default [aka memory-based] model
    */ 
    public static Model createMemModel()
        { return ModelFactory.createDefaultModel(); }
        
     /**
        test that two models are isomorphic and fail if they are not.
        
        @param title a String appearing at the beginning of the failure message
        @param wanted the model value that is expected
        @param got the model value to check
        @exception if the models are not isomorphic
     */    
    public static void assertIsoModels( String title, Model wanted, Model got )
        {
        if (wanted.isIsomorphicWith( got ) == false)
            {
            Map<Node, Object> map = CollectionFactory.createHashedMap();
            fail( title + ": expected " + nice( wanted.getGraph(), map ) + "\n but had " + nice( got.getGraph(), map ) );
            }
        }        

    /**
        Fail if the two models are not isomorphic. See assertIsoModels(String,Model,Model).
    */
    public static  void assertIsoModels( Model wanted, Model got )
        { assertIsoModels( "models must be isomorphic", wanted, got ); }
        
    }


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/