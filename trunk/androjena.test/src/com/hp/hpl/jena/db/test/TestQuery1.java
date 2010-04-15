/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestQuery1.java,v 1.1 2009/06/29 08:55:54 castagna Exp $
*/

package com.hp.hpl.jena.db.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

import junit.framework.*;

/**
    Apply the abstract query tests to an RDB graph.
 	@author kers
*/
public class TestQuery1 extends AbstractTestQuery1
    {
    public TestQuery1( String name )
        { super( name ); }

	public static TestSuite suite()
        { return new TestSuite( TestQuery1.class ); }     
       
    private IDBConnection theConnection;
    private int count = 0;
    
    private List<GraphRDB> graphs;
    
    @Override
    public void setUp() throws Exception
        {
        theConnection = TestConnection.makeTestConnection();
        graphs = new ArrayList<GraphRDB>();
        super.setUp();
        }
        
    @Override
    public void tearDown() throws Exception
        {
        removeGraphs();
        theConnection.close(); 
        super.tearDown(); 
        }
        
    private void removeGraphs()
        { for (int i = 0; i < graphs.size(); i += 1) graphs.get(i).remove(); }

	@Override
    public Graph getGraph ( ) {
		return getGraph( ReificationStyle.Minimal );
	}
        
    @Override
    public Graph getGraph ( ReificationStyle style )
        { 
        String name = "jena-test-rdb-TestQuery1-" + count ++;
        if (theConnection.containsModel( name )) makeGraph( name, false, style ).remove();
        GraphRDB result = makeGraph( name, true, style );
        graphs.add( result );    
        return result;
        }
        
    protected GraphRDB makeGraph( String name, boolean fresh, ReificationStyle style )
        { return new GraphRDB
            (
            theConnection,
            name, 
            theConnection.getDefaultModelProperties().getGraph(),
            GraphRDB.styleRDB( style ), 
            fresh
            ); }

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