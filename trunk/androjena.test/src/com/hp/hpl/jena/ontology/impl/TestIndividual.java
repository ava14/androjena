/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            23-May-2003
 * Filename           $RCSfile: TestIndividual.java,v $
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
import java.util.Iterator;

import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;


/**
 * <p>
 * Unit tests for ontology individuals
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: TestIndividual.java,v 1.2 2009/10/06 13:04:42 ian_dickinson Exp $
 */
public class TestIndividual
    extends OntTestBase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    static public TestSuite suite() {
        return new TestIndividual( "TestIndividual" );
    }

    public TestIndividual( String name ) {
        super( name );
    }


    // External signature methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "Individual.sameAs", true, false, true, false ) {
                /** Note: 6/Nov/2003 - updated to use sameAs not sameIndividualAs, following changes to OWL spec */
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    Individual x = m.createIndividual( A );
                    Individual y = m.createIndividual( A );
                    Individual z = m.createIndividual( A );

                    x.addSameAs( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "x should be the same as y", y, x.getSameAs() );
                    assertTrue( "x should be the same as y", x.isSameAs( y ) );

                    x.addSameAs( z );
                    assertEquals( "Cardinality should be 2", 2, x.getCardinality( prof.SAME_AS() ) );
                    iteratorTest( x.listSameAs(), new Object[] {z,y} );

                    x.setSameAs( z );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "x should be same indiv. as z", z, x.getSameAs() );

                    x.removeSameAs( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_AS() ) );
                    x.removeSameAs( z );
                    assertEquals( "Cardinality should be 0", 0, x.getCardinality( prof.SAME_AS() ) );
                }
            },

            new OntTestCase( "Individual.hasOntClass", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( A );

                    assertTrue( x.hasOntClass( A ) );
                    assertFalse( x.hasOntClass( B ) );
                }
            },

            new OntTestCase( "Individual.hasOntClass direct", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    assertTrue( x.hasOntClass( A, false ) );
                    assertTrue( x.hasOntClass( B, false ) );

                    assertTrue( x.hasOntClass( A, false ) );
                    assertTrue( x.hasOntClass( B, true ) );

                }
            },

            new OntTestCase( "Individual.hasOntClass string", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );

                    Individual x = m.createIndividual( A );

                    assertTrue( x.hasOntClass( NS + "A" ) );
                }
            },

            new OntTestCase( "Individual.getOntClass", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    Individual x = m.createIndividual( A );

                    assertEquals( A, x.getOntClass() );
                }
            },

            new OntTestCase( "Individual.getOntClass direct", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    // should never get A since it's not a direct class
                    assertEquals( B, x.getOntClass( true ) );
                }
            },

            new OntTestCase( "Individual.listOntClasses", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );

                    // now check the return types
                    for (Iterator<OntClass> i = x.listOntClasses( false ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.listOntClasses direct", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    iteratorTest( x.listOntClasses( true ), new Object[] {B} );

                    // now check the return types
                    for (Iterator<OntClass> i = x.listOntClasses( true ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.addOntClass", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A} );

                    // add a class
                    x.addOntClass( B );

                    // test again
                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );
                    for (Iterator<OntClass> i = x.listOntClasses( false ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.setOntClass", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A} );

                    // replace the class
                    x.setOntClass( B );

                    // test again
                    iteratorTest( x.listOntClasses( false ), new Object[] {B} );
                    for (Iterator<OntClass> i = x.listOntClasses( false ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.removeOntClass", true, true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );

                    Individual x = m.createIndividual( A );
                    x.addOntClass( B );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );

                    x.removeOntClass( A );
                    iteratorTest( x.listOntClasses( false ), new Object[] {B} );

                    x.removeOntClass( A );
                    iteratorTest( x.listOntClasses( false ), new Object[] {B} );

                    x.removeOntClass( B );
                    iteratorTest( x.listOntClasses( false ), new Object[] {} );
                }
            },
        };
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


