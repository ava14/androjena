/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: QueryTest.java,v 1.1 2009/06/29 08:55:50 castagna Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.*;

import junit.framework.*;

/**
    Test query over plain memory graphs.
*/

public class QueryTest extends TestCase
    {
	public QueryTest( String name )
		{ super( name ); }
		
    public static TestSuite suite()
    	{ 
        TestSuite result = new TestSuite();
        result.addTest( new TestSuite( TestQueryGraphMem.class ) );
        result.addTest( new TestSuite( TestQuerySmallGraphMem.class ) );
        result.addTestSuite( TestQuery.class );
        return result;
        } 
    
    public static class TestQueryGraphMem extends AbstractTestQuery
        {
        public TestQueryGraphMem( String name ) { super( name ); }
        
        @Override public Graph getGraph() { return Factory.createGraphMem(); }       
        }
    
    public static class TestQuerySmallGraphMem extends AbstractTestQuery
        {
        public TestQuerySmallGraphMem( String name ) { super( name ); }
        
        @Override public Graph getGraph() { return new SmallGraphMem(); }       
        }
     
    }

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
