/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestContains.java,v 1.1 2009/06/29 08:55:33 castagna Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import junit.framework.*;

/**
 	@author kers
*/
public class TestContains extends ModelTestBase
    {
    public TestContains( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestContains.class ); }          
        
    public void testContains( boolean yes, String facts, String resource )
        {
        Model m = modelWithStatements( facts );
        RDFNode r = rdfNode( m, resource );
        if (modelWithStatements( facts ).containsResource( r ) != yes)
            fail( "[" + facts + "] should" + (yes ? "" : " not") + " contain " + resource );
        }
        
    public void testContains()
        {
        testContains( false, "", "x" );
        testContains( false, "a R b", "x" );
        testContains( false, "a R b; c P d", "x" );
    /* */
        testContains( false, "a R b", "z" );
    /* */
        testContains( true, "x R y", "x" );
        testContains( true, "a P b", "P" );
        testContains( true, "i  Q  j", "j" );
        testContains( true, "x R y; a P b; i Q j", "y" );
    /* */
        testContains( true, "x R y; a P b; i Q j", "y" );
        testContains( true, "x R y; a P b; i Q j", "R" );
        testContains( true, "x R y; a P b; i Q j", "a" );
        }
    
    private Resource res( String uri )
        { return ResourceFactory.createResource( "eh:/" + uri ); }
    
    private Property prop( String uri )
        { return ResourceFactory.createProperty( "eh:/" + uri ); }
        
    public void testContainsWithNull()
        {
        testCWN( false, "", null, null, null );
        testCWN( true, "x R y", null, null, null );
        testCWN( false, "x R y", null, null, res( "z" ) );
        testCWN( true, "x RR y", res( "x" ), prop( "RR" ), null );
        testCWN( true, "a BB c", null, prop( "BB" ), res( "c" ) );
        testCWN( false, "a BB c", null, prop( "ZZ" ), res( "c" ) );
        }
    
    public void testCWN( boolean yes, String facts, Resource S, Property P, RDFNode O )
        { assertEquals( yes, modelWithStatements( facts ).contains( S, P, O ) ); }
    
    public void testModelComContainsSPcallsContainsSPO()
        {
        Graph g = Factory.createDefaultGraph();
        final boolean [] wasCalled = {false};
        Model m = new ModelCom( g )
            {
            @Override
            public boolean contains( Resource s, Property p, RDFNode o )
                {
                wasCalled[0] = true;
                return super.contains( s, p, o );
                }
            };
        assertFalse( m.contains( resource( "r" ), property( "p" ) ) );
        assertTrue( "contains(S,P) should call contains(S,P,O)", wasCalled[0] );
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