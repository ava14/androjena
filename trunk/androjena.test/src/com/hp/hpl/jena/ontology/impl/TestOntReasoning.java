/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            05-Jun-2003
 * Filename           $RCSfile: TestOntReasoning.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2009/10/06 13:04:42 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * <p>
 * Unit tests on ont models with reasoning
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: TestOntReasoning.java,v 1.2 2009/10/06 13:04:42 ian_dickinson Exp $
 */
public class TestOntReasoning
    extends TestCase
{
    // Constants
    //////////////////////////////////
    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntReasoning( String name ) {
        super( name );
    }

    // External signature methods
    //////////////////////////////////

    @Override
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }


    public void testSubClassDirectTransInf1a() {
        OntModel m = ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );

        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );

        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );

        iteratorTest( A.listSubClasses(), new Object[] {B, C, D} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C} );
    }

    public void testSubClassDirectTransInf1b() {
        OntModel m = ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );

        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );

        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );
        A.addSubClass( D );     // directly asserts a link that could be inferred

        iteratorTest( A.listSubClasses(), new Object[] {B, C, D} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C} );
    }

    public void testSubClassDirectTransInf2a() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasonerFactory( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );

        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );

        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );

        iteratorTest( A.listSubClasses(), new Object[] {B, C} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C} );
    }

    public void testSubClassDirectTransInf2b() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasonerFactory( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );

        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );

        A.addSubClass( B );
        A.addSubClass( C );
        C.addSubClass( D );
        A.addSubClass( D );     // directly asserts a link that could be inferred

        iteratorTest( A.listSubClasses(), new Object[] {B, C, D} );
        iteratorTest( A.listSubClasses( true ), new Object[] {B, C} );
    }

    public void testSubPropertyDirectTransInf1a() {
        OntModel m = ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );

        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );

        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );

        iteratorTest( p.listSubProperties(), new Object[] {p,q,r,s} );
        iteratorTest( p.listSubProperties( true ), new Object[] {q,r} );
    }

    public void testSubPropertyDirectTransInf1b() {
        OntModel m = ModelFactory.createOntologyModel( ProfileRegistry.OWL_LITE_LANG );

        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );

        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );
        p.addSubProperty( s );     // directly asserts a link that could be inferred

        iteratorTest( p.listSubProperties(), new Object[] {p,q,r,s} );
        iteratorTest( p.listSubProperties( true ), new Object[] {q,r} );
    }

    public void testSubPropertyDirectTransInf2a() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasonerFactory( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );

        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );

        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );

        iteratorTest( p.listSubProperties(), new Object[] {q,r} );
        iteratorTest( p.listSubProperties( true ), new Object[] {q,r} );
    }

    public void testSubPropertyDirectTransInf2b() {
        // test the code path for generating direct sc with no reasoner
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_LITE_MEM );
        spec.setReasonerFactory( null );
        OntModel m = ModelFactory.createOntologyModel( spec, null );

        OntProperty p = m.createObjectProperty( NS + "p" );
        OntProperty q = m.createObjectProperty( NS + "q" );
        OntProperty r = m.createObjectProperty( NS + "r" );
        OntProperty s = m.createObjectProperty( NS + "s" );

        p.addSubProperty( q );
        p.addSubProperty( r );
        r.addSubProperty( s );
        p.addSubProperty( s );     // directly asserts a link that could be inferred

        iteratorTest( p.listSubProperties(), new Object[] {q,r,s} );
        iteratorTest( p.listSubProperties( true ), new Object[] {q,r} );
    }

    public void testListDefinedProperties() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RULE_INF, null );

        // a simple class hierarchy  organism -> vertebrate -> mammal -> dog
        OntClass organism = m.createClass( NS + "Organism" );
        OntClass vertebrate = m.createClass( NS + "Vertebrate" );
        OntClass mammal = m.createClass( NS + "Mammal" );
        OntClass dog = m.createClass( NS + "Dog" );

        organism.addSubClass( vertebrate );
        vertebrate.addSubClass( mammal );
        mammal.addSubClass( dog );

        // hair as a covering
        OntClass covering = m.createClass( NS + "Covering" );
        Individual hair = m.createIndividual( NS+"hair", covering );

        // various properties
        DatatypeProperty limbsCount = m.createDatatypeProperty( NS + "limbsCount" );
        DatatypeProperty hasCovering = m.createDatatypeProperty( NS + "hasCovering" );
        DatatypeProperty numYoung = m.createDatatypeProperty( NS + "numYoung" );

        // vertebrates have limbs, mammals have live young
        limbsCount.addDomain( vertebrate );
        numYoung.addDomain( mammal );

        // mammals have-covering = hair
        Restriction r = m.createRestriction( hasCovering );
        r.convertToHasValueRestriction( hair );
        mammal.addSuperClass( r );

        iteratorTest( organism.listDeclaredProperties(), new Object[] {hasCovering} );
        iteratorTest( vertebrate.listDeclaredProperties(), new Object[] {limbsCount, hasCovering} );
        iteratorTest( mammal.listDeclaredProperties(), new Object[] {limbsCount, hasCovering, numYoung} );
        iteratorTest( dog.listDeclaredProperties(), new Object[] {limbsCount, hasCovering, numYoung} );
        iteratorTest( r.listDeclaredProperties(), new Object[] {hasCovering} );

        iteratorTest( organism.listDeclaredProperties(true), new Object[] {hasCovering} );
        iteratorTest( vertebrate.listDeclaredProperties(true), new Object[] {limbsCount} );
        iteratorTest( mammal.listDeclaredProperties(true), new Object[] {numYoung} );
        iteratorTest( dog.listDeclaredProperties(true), new Object[] {} );
        iteratorTest( r.listDeclaredProperties(true), new Object[] {hasCovering} );

        iteratorTest( organism.listDeclaredProperties(false), new Object[] {hasCovering} );
        iteratorTest( vertebrate.listDeclaredProperties(false), new Object[] {hasCovering,limbsCount} );
        iteratorTest( mammal.listDeclaredProperties(false), new Object[] {hasCovering,numYoung,limbsCount} );
        iteratorTest( dog.listDeclaredProperties(false), new Object[] {hasCovering,numYoung,limbsCount} );
        iteratorTest( r.listDeclaredProperties(false), new Object[] {hasCovering} );
    }

    // Internal implementation methods
    //////////////////////////////////

    /** Test that an iterator delivers the expected values */
    protected void iteratorTest( Iterator<?> i, Object[] expected ) {
        Logger logger = LoggerFactory.getLogger( getClass() );
        List<Object> expList = new ArrayList<Object>();
        for (int j = 0; j < expected.length; j++) {
            expList.add( expected[j] );
        }

        while (i.hasNext()) {
            Object next = i.next();

            // debugging
            if (!expList.contains( next )) {
                logger.debug( getName() + " - Unexpected iterator result: " + next );
            }

            assertTrue( "Value " + next + " was not expected as a result from this iterator ", expList.contains( next ) );
            assertTrue( "Value " + next + " was not removed from the list ", expList.remove( next ) );
        }

        if (!(expList.size() == 0)) {
            logger.debug( getName() + " Expected iterator results not found" );
            for (Iterator<?> j = expList.iterator(); j.hasNext(); ) {
                logger.debug( getName() + " - missing: " + j.next() );
            }
        }
        assertEquals( "There were expected elements from the iterator that were not found", 0, expList.size() );
    }


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

