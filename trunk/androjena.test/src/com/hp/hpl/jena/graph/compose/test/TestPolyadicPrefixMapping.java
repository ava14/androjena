/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPolyadicPrefixMapping.java,v 1.2 2009/06/29 18:42:06 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.shared.AbstractTestPrefixMapping;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestPolyadicPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestPolyadicPrefixMapping( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestPolyadicPrefixMapping.class ); }   
    
    Graph gBase;
    Graph g1, g2;
    
    /**
        Will be a polyadic graph with base gBase and subs g1, g2.
    */
    Polyadic poly;
    
    protected static final String alpha = "something:alpha#";
    protected static final String beta = "something:beta#";
    
    @Override
    public void setUp()
        {
        gBase = Factory.createDefaultGraph();
        g1 = Factory.createDefaultGraph();
        g2 = Factory.createDefaultGraph();
        poly = new MultiUnion( new Graph[] {gBase, g1, g2} );
        poly.setBaseGraph( gBase );
        }
    
    @Override
    protected PrefixMapping getMapping()
        {
        Graph gBase = Factory.createDefaultGraph();
        Graph g1 = Factory.createDefaultGraph();
        Graph g2 = Factory.createDefaultGraph();
        Polyadic poly = new MultiUnion( new Graph[] {gBase, g1, g2} );
        return new PolyadicPrefixMappingImpl( poly ); 
        }        
    
    /*
        tests for polyadic prefix mappings
        (a) base mapping is the mutable one
        (b) base mapping over-rides all others
        (c) non-overridden mappings in other maps are visible
    */
    
    public void testOnlyBaseMutated()
        {
        poly.getPrefixMapping().setNsPrefix( "a", alpha );
        assertEquals( null, g1.getPrefixMapping().getNsPrefixURI( "a" ) );
        assertEquals( null, g2.getPrefixMapping().getNsPrefixURI( "a" ) );
        assertEquals( alpha, gBase.getPrefixMapping().getNsPrefixURI( "a" ) );
        }
    
    public void testUpdatesVisible()
        {
        g1.getPrefixMapping().setNsPrefix( "a", alpha );
        g2.getPrefixMapping().setNsPrefix( "b", beta );
        assertEquals( alpha, poly.getPrefixMapping().getNsPrefixURI( "a" ) );
        assertEquals( beta, poly.getPrefixMapping().getNsPrefixURI( "b" ) );
        }
    
    public void testUpdatesOverridden()
        {
        g1.getPrefixMapping().setNsPrefix( "x", alpha );
        poly.getPrefixMapping().setNsPrefix( "x", beta );
        assertEquals( beta, poly.getPrefixMapping().getNsPrefixURI( "x" ) );
        }
    
    public void testQNameComponents()
        {
        g1.getPrefixMapping().setNsPrefix( "x", alpha );
        g2.getPrefixMapping().setNsPrefix( "y", beta );
        assertEquals( "x:hoop", poly.getPrefixMapping().qnameFor( alpha + "hoop" ) );
        assertEquals( "y:lens", poly.getPrefixMapping().qnameFor( beta + "lens" ) );
        }
    
    /**
        Test that the default namespace of a sub-graph doesn't appear as a
        default namespace of the polyadic graph.
     */
    public void testSubgraphsDontPolluteDefaultPrefix() 
        {
        String imported = "http://imported#", local = "http://local#";
        g1.getPrefixMapping().setNsPrefix( "", imported );
        poly.getPrefixMapping().setNsPrefix( "", local );
        assertEquals( null, poly.getPrefixMapping().getNsURIPrefix( imported ) );
        }
    
    public void testPolyDoesntSeeImportedDefaultPrefix()
        {
        String imported = "http://imported#";
        g1.getPrefixMapping().setNsPrefix( "", imported );
        assertEquals( null, poly.getPrefixMapping().getNsPrefixURI( "" ) );
        }
    
    public void testPolyMapOverridesFromTheLeft()
        {
        g1.getPrefixMapping().setNsPrefix( "a", "eh:/U1" );
        g2.getPrefixMapping().setNsPrefix( "a", "eh:/U2" );
        String a = poly.getPrefixMapping().getNsPrefixMap().get( "a" );
        assertEquals( "eh:/U1", a );
        }
    
    public void testPolyMapHandlesBase()
        {
        g1.getPrefixMapping().setNsPrefix( "", "eh:/U1" );
        g2.getPrefixMapping().setNsPrefix( "", "eh:/U2" );
        String a = poly.getPrefixMapping().getNsPrefixMap().get( "" );
        assertEquals( poly.getPrefixMapping().getNsPrefixURI( "" ), a );
        }
    }

/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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