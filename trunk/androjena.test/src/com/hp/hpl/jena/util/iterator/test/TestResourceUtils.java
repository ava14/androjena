/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            06-Jun-2003
 * Filename           $RCSfile: TestResourceUtils.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2009/10/06 13:04:44 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.iterator.test;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.util.*;

import junit.framework.*;

import java.util.*;


/**
 * <p>
 * Unit tests on resource utilities
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: TestResourceUtils.java,v 1.2 2009/10/06 13:04:44 ian_dickinson Exp $
 */
public class TestResourceUtils
    extends TestCase
{
    // Constants
    //////////////////////////////////

    public static final String NS = "http://jena.hp.com/test#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestResourceUtils( String name ) {
        super( name );
    }

    // External signature methods
    //////////////////////////////////

    public void testMaximalLowerElements() {
        Model m = ModelFactory.createDefaultModel();

        Resource a = m.createResource( NS + "a" );
        Resource b = m.createResource( NS + "b" );
        Resource c = m.createResource( NS + "c" );
        Resource d = m.createResource( NS + "d" );

        b.addProperty( RDFS.subClassOf, a );
        c.addProperty( RDFS.subClassOf, a );
        d.addProperty( RDFS.subClassOf, c );
        d.addProperty( RDFS.subClassOf, a );

        List<Resource> abcd = Arrays.asList( new Resource[] {a,b,c,d} );
        List<Resource> bcd = Arrays.asList( new Resource[] {b,c,d} );
        List<Resource> cd = Arrays.asList( new Resource[] {c,d} );

        assertEquals( "Wrong number of remaining resources", 1, ResourceUtils.maximalLowerElements( abcd, RDFS.subClassOf, true ).size() );
        assertEquals( "Result should be a", a, ResourceUtils.maximalLowerElements( abcd, RDFS.subClassOf, true ).iterator().next() );
        assertEquals( "Wrong number of remaining resources", 2, ResourceUtils.maximalLowerElements( bcd, RDFS.subClassOf, true ).size() );
        assertEquals( "Wrong number of remaining resources", 1, ResourceUtils.maximalLowerElements( cd, RDFS.subClassOf, true ).size() );
        assertEquals( "Result should be a", c, ResourceUtils.maximalLowerElements( cd, RDFS.subClassOf, true ).iterator().next() );
    }

    public void testRenameResource() {
        Model m = ModelFactory.createDefaultModel();

        Resource a = m.createResource( NS + "a" );
        Resource b = m.createResource( NS + "b" );
        Resource c = m.createResource( NS + "c" );
        Resource d = m.createResource( NS + "d" );

        Property p = m.createProperty( NS, "p" );
        Property q = m.createProperty( NS, "q" );

        a.addProperty( p, b );
        a.addProperty( q, c );
        d.addProperty( p, a );
        d.addProperty( p, b );

        // now rename a to e
        Resource e = ResourceUtils.renameResource( a, NS + "e" );

        assertTrue( "should be no properties of a", !a.listProperties().hasNext() );
        assertEquals( "uri of a", NS + "a", a.getURI() );
        assertEquals( "uri of e", NS + "e", e.getURI() );

        assertTrue( "d should not have p a", !d.hasProperty( p, a ));
        assertTrue( "d should have p e", d.hasProperty( p, e ));

        assertTrue( "e should have p b", e.hasProperty( p, b ) );
        assertTrue( "e should have q c", e.hasProperty( q, c ) );

        assertTrue( "d p b should be unchanged", d.hasProperty( p, b ) );

        // now rename e to anon
        Resource anon = ResourceUtils.renameResource( e, null );

        assertTrue( "should be no properties of e", !e.listProperties().hasNext() );
        assertEquals( "uri of e", NS + "e", e.getURI() );
        assertTrue( "anon", anon.isAnon() );

        assertTrue( "d should not have p e", !d.hasProperty( p, e ));
        assertTrue( "d should have p anon", d.hasProperty( p, anon ));

        assertTrue( "anon should have p b", anon.hasProperty( p, b ) );
        assertTrue( "anon should have q c", anon.hasProperty( q, c ) );

        assertTrue( "d p b should be unchanged", d.hasProperty( p, b ) );

        // reflexive case
        Resource f = m.createResource( NS + "f" );
        f.addProperty( p, f );

        Resource f1 = ResourceUtils.renameResource( f, NS +"f1" );
        assertFalse( "Should be no f statements",  m.listStatements( f, null, (RDFNode) null).hasNext() );
        assertTrue( "f1 has p f1", f1.hasProperty( p, f1 ) );
    }

    public void testReachableGraphClosure() {
        Model m0 = ModelFactory.createDefaultModel();
        Resource a = m0.createResource( "a" );
        Resource b = m0.createResource( "b" );
        Resource c = m0.createResource( "c" );
        Resource d = m0.createResource( "d" );
        Property p = m0.createProperty( "p" );

        m0.add( a, p, b );
        m0.add( a, p, c );
        m0.add( b, p, b );  // unit loop
        m0.add( b, p, a );  // loop
        m0.add( d, p, a );  // not reachable from a

        Model m1 = ModelFactory.createDefaultModel();
        m1.add( a, p, b );
        m1.add( a, p, c );
        m1.add( b, p, b );
        m1.add( b, p, a );

        assertTrue( "m1 should be isomorphic with the reachable sub-graph from a", m1.isIsomorphicWith( ResourceUtils.reachableClosure(a)));
    }

    public void testRemoveEquiv() {
        Model m = ModelFactory.createDefaultModel();

        Resource a = m.createResource( NS + "a" );
        Resource b = m.createResource( NS + "b" );
        Resource c = m.createResource( NS + "c" );
        Resource d = m.createResource( NS + "d" );
        Resource e = m.createResource( NS + "e" );

        b.addProperty( RDFS.subClassOf, a );
        a.addProperty( RDFS.subClassOf, b );  // a,b are equivalent
        d.addProperty( RDFS.subClassOf, e );
        e.addProperty( RDFS.subClassOf, d );  // d,e are equivalent

        // reflexive relations - would be inferred by inf engine
        a.addProperty( RDFS.subClassOf, a );
        b.addProperty( RDFS.subClassOf, b );
        c.addProperty( RDFS.subClassOf, c );
        d.addProperty( RDFS.subClassOf, d );
        e.addProperty( RDFS.subClassOf, e );

        List<Resource> abcde = Arrays.asList( new Resource[] {a,b,c,d,e} );
        List<Resource> ab = Arrays.asList( new Resource[] {a,b} );
        List<Resource> cde = Arrays.asList( new Resource[] {c,d,e} );
        List<Resource> abde = Arrays.asList( new Resource[] {a,b,d,e} );
        List<Resource> de = Arrays.asList( new Resource[] {d,e} );

        List<Resource> in = new ArrayList<Resource>();
        in.addAll( abcde );
        List<Resource> out = null;
        assertTrue( in.equals( abcde ) );
        assertFalse( in.equals( cde ));
        assertNull( out );

        out = ResourceUtils.removeEquiv( in, RDFS.subClassOf, a );

        assertFalse( in.equals( abcde ) );
        assertTrue( in.equals( cde ));
        assertNotNull( out );
        assertEquals( out, ab );

        out = ResourceUtils.removeEquiv( in, RDFS.subClassOf, e );

        assertFalse( in.equals( abcde ) );
        assertTrue( in.equals( Collections.singletonList( c ) ));
        assertNotNull( out );
        assertEquals( out, de );
    }

    public void testPartition() {
        Model m = ModelFactory.createDefaultModel();

        Resource a = m.createResource( NS + "a" );
        Resource b = m.createResource( NS + "b" );
        Resource c = m.createResource( NS + "c" );
        Resource d = m.createResource( NS + "d" );
        Resource e = m.createResource( NS + "e" );

        b.addProperty( RDFS.subClassOf, a );
        a.addProperty( RDFS.subClassOf, b );  // a,b are equivalent
        d.addProperty( RDFS.subClassOf, e );
        e.addProperty( RDFS.subClassOf, d );  // d,e are equivalent

        // reflexive relations - would be inferred by inf engine
        a.addProperty( RDFS.subClassOf, a );
        b.addProperty( RDFS.subClassOf, b );
        c.addProperty( RDFS.subClassOf, c );
        d.addProperty( RDFS.subClassOf, d );
        e.addProperty( RDFS.subClassOf, e );

        List<Resource> abcde = Arrays.asList( new Resource[] {a,b,c,d,e} );
        List<Resource> ab = Arrays.asList( new Resource[] {b,a} );
        List<Resource> cc = Arrays.asList( new Resource[] {c} );
        List<Resource> de = Arrays.asList( new Resource[] {e,d} );

        List<List<Resource>> partition = ResourceUtils.partition( abcde, RDFS.subClassOf );
        assertEquals( "Should be 3 partitions", 3, partition.size() );
        assertEquals( "First parition should be (a,b)", ab, partition.get(0) );
        assertEquals( "First parition should be (c)", cc, partition.get(1) );
        assertEquals( "First parition should be (d,e)", de, partition.get(2) );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

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
