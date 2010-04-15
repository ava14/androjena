/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            21-Jun-2003
 * Filename           $RCSfile: TestOntModel.java,v $
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
import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DataRange;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.reasoner.rulesys.test.TestBugs;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;



/**
 * <p>
 * Unit tests on OntModel capabilities.  Many of OntModel's methods are tested by the other
 * abstractions' unit tests.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: TestOntModel.java,v 1.2 2009/10/06 13:04:42 ian_dickinson Exp $
 */
public class TestOntModel
    extends ModelTestBase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    public static final String BASE = "http://www.hp.com/test";
    public static final String NS = BASE + "#";

    public static final String DOC = "<rdf:RDF" +
                                     "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
                                     "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" +
                                     "   xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#D\">" +
                                     "    <rdfs:subClassOf>" +
                                     "      <owl:Class rdf:about=\"http://www.hp.com/test#B\"/>" +
                                     "    </rdfs:subClassOf>" +
                                     "  </owl:Class>" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#B\">" +
                                     "    <rdfs:subClassOf rdf:resource=\"http://www.hp.com/test#A\"" +
                                     "       rdf:type=\"http://www.w3.org/2002/07/owl#Class\"/>" +
                                     "  </owl:Class>" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#C\">" +
                                     "    <rdfs:subClassOf rdf:resource=\"http://www.hp.com/test#B\"/>" +
                                     "  </owl:Class>" +
                                     "  <owl:ObjectProperty rdf:about=\"http://www.hp.com/test#p\">" +
                                     "    <rdfs:domain rdf:resource=\"http://www.hp.com/test#A\"/>" +
                                     "    <rdfs:range rdf:resource=\"http://www.hp.com/test#B\"/>" +
                                     "    <rdfs:range rdf:resource=\"http://www.hp.com/test#C\"/>" +
                                     "  </owl:ObjectProperty>" +
                                     "</rdf:RDF>";

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntModel( String name ) {
        super( name );
    }

    // External signature methods
    //////////////////////////////////

    @Override
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }


    /** Test writing the base model to an output stream */
    public void testWriteOutputStream() {
        OntModel m = ModelFactory.createOntologyModel();

        // set up the model
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );

        A.addSubClass( B );
        B.addSubClass( C );
        B.addSubClass( D );

        ObjectProperty p = m.createObjectProperty( NS + "p" );

        p.addDomain( A );
        p.addRange( B );
        p.addRange( C );

        // write to a stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.write( out );

        String s = out.toString();
        ByteArrayInputStream in = new ByteArrayInputStream( s.getBytes() );

        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( in, BASE );

        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new ByteArrayInputStream( DOC.getBytes() ), BASE );

        // should be the same
        assertTrue( "InputStream write/read cycle failed (1)", mIn1.isIsomorphicWith( m.getBaseModel() ) );
        assertTrue( "InputStream write/read cycle failed (2)", mIn2.isIsomorphicWith( m.getBaseModel() ) );
    }

    public void testGetBaseModelPrefixes()
        {
        OntModel om = ModelFactory.createOntologyModel();
        om.setNsPrefix( "bill", "http://bill.and.ben/flowerpot#" );
        om.setNsPrefix( "grue", "ftp://grue.and.bleen/2000#" );
        assertEquals( om.getNsPrefixMap(), om.getBaseModel().getNsPrefixMap() );
        }

    public void testWritesPrefixes()
        {
        OntModel om = ModelFactory.createOntologyModel();
        om.setNsPrefix( "spoo", "http://spoo.spoo.com/spoo#" );
        om.add( statement( om, "ping http://spoo.spoo.com/spoo#pang pilly" ) );
        om.add( statement( om, "gg " + OWL.getURI() + "hh ii" ) );
        StringWriter sw = new StringWriter();
        om.write( sw );
        String s = sw.getBuffer().toString();
        assertTrue( s.indexOf( "xmlns:spoo=\"http://spoo.spoo.com/spoo#\"" ) > 0 );
        assertTrue( s.indexOf( "xmlns:owl=\"" + OWL.getURI() + "\"" ) > 0 );
        }

    /** Test writing the base model to an output stream */
    public void testWriteWriter() {
        OntModel m = ModelFactory.createOntologyModel();

        // set up the model
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );

        A.addSubClass( B );
        B.addSubClass( C );
        B.addSubClass( D );

        ObjectProperty p = m.createObjectProperty( NS + "p" );

        p.addDomain( A );
        p.addRange( B );
        p.addRange( C );

        // write to a stream
        StringWriter out = new StringWriter();
        m.write( out );

        String s = out.toString();

        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( new StringReader( s ), BASE );

        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new StringReader( DOC ), BASE );

        // should be the same
        assertTrue( "Writer write/read cycle failed (1)", mIn1.isIsomorphicWith( m.getBaseModel() ) );
        assertTrue( "Writer write/read cycle failed (2)", mIn2.isIsomorphicWith( m.getBaseModel() ) );
    }

    public void testGetOntology() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createOntology( NS + "s" );
        assertEquals( "Result of get s", s, m.getOntology( NS + "s" ) );
        assertNull( "result of get q", m.getOntology( NS+"q") );
        assertNull( "result of get r", m.getOntology( NS+"r"));
    }


    public void testGetIndividual() {
        OntModel m = ModelFactory.createOntologyModel();
        OntClass c = m.createClass( NS +"c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createIndividual( NS + "s", c );
        assertEquals( "Result of get s", s, m.getIndividual( NS + "s" ) );
        assertNull( "result of get q", m.getIndividual( NS+"q") );
    }

    /** User requested: allow null arguments when creating individuals */
    public void testCreateIndividual() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource i0 = m.createIndividual( OWL.Thing );
        Resource i1 = m.createIndividual( null );
        Resource i2 = m.createIndividual( NS + "i2", OWL.Thing );
        Resource i3 = m.createIndividual( NS + "i3", null );
        Resource i4 = m.createIndividual( null, OWL.Thing );
        Resource i5 = m.createIndividual( null, null );

        assertNotNull( i0 );
        assertNotNull( i1 );
        assertNotNull( i2 );
        assertNotNull( i3 );
        assertNotNull( i4 );
        assertNotNull( i5 );
    }

    public void testGetOntProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createOntProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getOntProperty( NS + "s" ) );
        assertNull( "result of get q", m.getOntProperty( NS+"q") );
        assertNull( "result of get r", m.getOntProperty( NS+"r"));
    }


    public void testGetObjectProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createObjectProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getObjectProperty( NS + "s" ) );
        assertNull( "result of get q", m.getObjectProperty( NS+"q") );
        assertNull( "result of get r", m.getObjectProperty( NS+"r"));
    }


    public void testGetTransitiveProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createTransitiveProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getTransitiveProperty( NS + "s" ) );
        assertNull( "result of get q", m.getTransitiveProperty( NS+"q") );
        assertNull( "result of get r", m.getTransitiveProperty( NS+"r"));
    }


    public void testGetSymmetricProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createSymmetricProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getSymmetricProperty( NS + "s" ) );
        assertNull( "result of get q", m.getSymmetricProperty( NS+"q") );
        assertNull( "result of get r", m.getSymmetricProperty( NS+"r"));
    }


    public void testGetInverseFunctionalProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createInverseFunctionalProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getInverseFunctionalProperty( NS + "s" ) );
        assertNull( "result of get q", m.getInverseFunctionalProperty( NS+"q") );
        assertNull( "result of get r", m.getInverseFunctionalProperty( NS+"r"));
    }


    public void testGetDatatypeProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createDatatypeProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getDatatypeProperty( NS + "s" ) );
        assertNull( "result of get q", m.getDatatypeProperty( NS+"q") );
        assertNull( "result of get r", m.getDatatypeProperty( NS+"r"));
    }


    public void testGetAnnotationProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createAnnotationProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getAnnotationProperty( NS + "s" ) );
        assertNull( "result of get q", m.getAnnotationProperty( NS+"q") );
        assertNull( "result of get r", m.getAnnotationProperty( NS+"r"));
    }

    public void testGetOntResource() {
        OntModel m = ModelFactory.createOntologyModel();
        OntResource r0 = m.getOntResource( NS + "a" );
        assertNull( r0 );
        OntResource r1 = m.createOntResource( NS + "aaa" );
        assertInstanceOf( OntResource.class, r1 );
        Resource r2a = m.getResource( NS + "a" );
        Resource r2b = m.getResource( NS + "b" );
        Property p = m.getProperty( NS + "p" );
        m.add( r2a, p, r2b );
        r0 = m.getOntResource( NS + "a" );
        assertInstanceOf( OntResource.class, r0 );
        OntResource r3 = m.getOntResource( r2b );
        assertInstanceOf( OntResource.class, r3 );
    }

    public void testGetOntClass() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        Resource r0 = m.getResource( NS + "r0" );
        m.add( r, RDF.type, r0 );
        Resource s = m.createClass( NS + "s" );
        assertEquals( "Result of get s", s, m.getOntClass( NS + "s" ) );
        assertNull( "result of get q", m.getOntClass( NS+"q") );
        assertNull( "result of get r", m.getOntClass( NS+"r"));
    }


    public void testGetComplementClass() {
        OntModel m = ModelFactory.createOntologyModel();
        OntClass c = m.createClass( NS +"c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createComplementClass( NS + "s", c );
        assertEquals( "Result of get s", s, m.getComplementClass( NS + "s" ) );
        assertNull( "result of get q", m.getComplementClass( NS+"q") );
        assertNull( "result of get r", m.getComplementClass( NS+"r"));
    }


    public void testGetEnumeratedClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createEnumeratedClass( NS + "s", l );
        assertEquals( "Result of get s", s, m.getEnumeratedClass( NS + "s" ) );
        assertNull( "result of get q", m.getEnumeratedClass( NS+"q") );
        assertNull( "result of get r", m.getEnumeratedClass( NS+"r"));
    }


    public void testGetUnionClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createUnionClass( NS + "s", l );
        assertEquals( "Result of get s", s, m.getUnionClass( NS + "s" ) );
        assertNull( "result of get q", m.getUnionClass( NS+"q") );
        assertNull( "result of get r", m.getUnionClass( NS+"r"));
    }


    public void testGetIntersectionClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createIntersectionClass( NS + "s", l );
        assertEquals( "Result of get s", s, m.getIntersectionClass( NS + "s" ) );
        assertNull( "result of get q", m.getIntersectionClass( NS+"q") );
        assertNull( "result of get r", m.getIntersectionClass( NS+"r"));
    }


    public void testGetRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createRestriction( NS + "s", p );
        assertEquals( "Result of get s", s, m.getRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getRestriction( NS+"q") );
        assertNull( "result of get r", m.getRestriction( NS+"r"));
    }


    public void testGetHasValueRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createHasValueRestriction( NS + "s", p, c );
        assertEquals( "Result of get s", s, m.getHasValueRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getHasValueRestriction( NS+"q") );
        assertNull( "result of get r", m.getHasValueRestriction( NS+"r"));
    }


    public void testGetSomeValuesFromRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createSomeValuesFromRestriction( NS + "s", p, c );
        assertEquals( "Result of get s", s, m.getSomeValuesFromRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getSomeValuesFromRestriction( NS+"q") );
        assertNull( "result of get r", m.getSomeValuesFromRestriction( NS+"r"));
    }


    public void testGetAllValuesFromRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createAllValuesFromRestriction( NS + "s", p, c );
        assertEquals( "Result of get s", s, m.getAllValuesFromRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getAllValuesFromRestriction( NS+"q") );
        assertNull( "result of get r", m.getAllValuesFromRestriction( NS+"r"));
    }


    public void testGetCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( "Result of get s", s, m.getCardinalityRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getCardinalityRestriction( NS+"q") );
        assertNull( "result of get r", m.getCardinalityRestriction( NS+"r"));
    }


    public void testGetMinCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createMinCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( "Result of get s", s, m.getMinCardinalityRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getMinCardinalityRestriction( NS+"q") );
        assertNull( "result of get r", m.getMinCardinalityRestriction( NS+"r"));
    }


    public void testGetMaxCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createMaxCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( "Result of get s", s, m.getMaxCardinalityRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getMaxCardinalityRestriction( NS+"q") );
        assertNull( "result of get r", m.getMaxCardinalityRestriction( NS+"r"));
    }

    public void testGetSubgraphs() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URL
        m.read( TestHelper.getResourceURL("testing/ontology/testImport6/a.owl") );
        assertEquals( "Marker count not correct", 4, TestOntDocumentManager.countMarkers( m ) );

        List<Graph> subs = m.getSubGraphs();

        assertEquals( "n subgraphs should be ", 3, subs.size() );

        boolean isGraph = true;
        for (Iterator<Graph> i = subs.iterator(); i.hasNext(); ) {
            Object x = i.next();
            if (!(x instanceof Graph)) {
                isGraph = false;
            }
        }
        assertTrue( "All sub-graphs should be graphs", isGraph );

    }


    public void testListImportURIs() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URL
        String a = TestHelper.getResourceURL("testing/ontology/testImport6/a.owl");
//        String b = TestHelper.getResourceURL("testing/ontology/testImport6/b.owl");
//        String cc = TestHelper.getResourceURL("testing/ontology/testImport6/c.owl");
//        String d = TestHelper.getResourceURL("testing/ontology/testImport6/d.owl");
        m.read( a );
//        Collection<String> c = m.listImportedOntologyURIs();
//
//        assertEquals( "Should be two non-closed import URI's", 2, c.size() );
//        assertTrue( "b should be imported ", c.contains( b ));
//        assertFalse( "c should not be imported ", c.contains( cc ));
//        assertTrue( "d should be imported ", c.contains( d ));
//
//        c = m.listImportedOntologyURIs( true );
//
//        assertEquals( "Should be two non-closed import URI's", 3, c.size() );
//        assertTrue( "b should be imported ", c.contains( b ));
//        assertTrue( "c should be imported ", c.contains( cc ));
//        assertTrue( "d should be imported ", c.contains( d ));
//        m.read( "file:testing/ontology/testImport6/a.owl" );
        Collection<String> c = m.listImportedOntologyURIs();

        assertEquals( "Should be two non-closed import URI's", 2, c.size() );
        assertTrue( "b should be imported ", c.contains( "file:testing/ontology/testImport6/b.owl" ));
        assertFalse( "c should not be imported ", c.contains( "file:testing/ontology/testImport6/c.owl" ));
        assertTrue( "d should be imported ", c.contains( "file:testing/ontology/testImport6/d.owl" ));

        c = m.listImportedOntologyURIs( true );

        assertEquals( "Should be two non-closed import URI's", 3, c.size() );
        assertTrue( "b should be imported ", c.contains( "file:testing/ontology/testImport6/b.owl" ));
        assertTrue( "c should be imported ", c.contains( "file:testing/ontology/testImport6/c.owl" ));
        assertTrue( "d should be imported ", c.contains( "file:testing/ontology/testImport6/d.owl" ));
    }

    /* Test removed: superseded by testListSubmodelsN (below)
    //    public void testListImportedModels() {
     *
     */

    /** Some tests for listing properties. See also {@link TestListSyntaxCategories} */

    public void testListOntProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertFalse( iteratorContains( m.listOntProperties(), op ) );
        assertFalse( iteratorContains( m.listOntProperties(), dp ) );
        assertFalse( iteratorContains( m.listOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listOntProperties(), rdfp ) );
    }

    public void testListOntProperties1() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        assertTrue( iteratorContains( m.listOntProperties(), op ) );
        assertTrue( iteratorContains( m.listOntProperties(), dp ) );

        // note that owl:AnnotationProperty is an rdf:Property in OWL Full
        assertTrue( iteratorContains( m.listOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listOntProperties(), rdfp ) );
    }

    public void testListOntProperties2() {
        OntModelSpec owlDLReasoner = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        owlDLReasoner.setReasoner( OntModelSpec.OWL_MEM_MICRO_RULE_INF.getReasoner() );
        OntModel m = ModelFactory.createOntologyModel( owlDLReasoner );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        assertTrue( iteratorContains( m.listOntProperties(), op ) );
        assertTrue( iteratorContains( m.listOntProperties(), dp ) );

        // note that owl:AnnotationProperty not an rdf:Property in OWL DL
        assertFalse( iteratorContains( m.listOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listOntProperties(), rdfp ) );
    }


    public void testListAllOntProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertTrue( iteratorContains( m.listAllOntProperties(), op ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), dp ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), rdfp ) );
    }

    public void testListObjectProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertTrue( iteratorContains( m.listObjectProperties(), op ) );
        assertFalse( iteratorContains( m.listObjectProperties(), dp ) );
        assertFalse( iteratorContains( m.listObjectProperties(), ap ) );
        assertFalse( iteratorContains( m.listObjectProperties(), ontp ) );
        assertFalse( iteratorContains( m.listObjectProperties(), rdfp ) );
    }

    public void testListDatatypeProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertFalse( iteratorContains( m.listDatatypeProperties(), op ) );
        assertTrue( iteratorContains( m.listDatatypeProperties(), dp ) );
        assertFalse( iteratorContains( m.listDatatypeProperties(), ap ) );
        assertFalse( iteratorContains( m.listDatatypeProperties(), ontp ) );
        assertFalse( iteratorContains( m.listDatatypeProperties(), rdfp ) );
    }

    public void testListAnnotationProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertFalse( iteratorContains( m.listAnnotationProperties(), op ) );
        assertFalse( iteratorContains( m.listAnnotationProperties(), dp ) );
        assertTrue( iteratorContains( m.listAnnotationProperties(), ap ) );
        assertFalse( iteratorContains( m.listAnnotationProperties(), ontp ) );
        assertFalse( iteratorContains( m.listAnnotationProperties(), rdfp ) );
    }

    public void testListSubModels0() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URL
        m.read( TestHelper.getResourceURL("testing/ontology/testImport6/a.owl" ));
//        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( "Marker count not correct", 4, TestOntDocumentManager.countMarkers( m ) );

        List<OntModel> importModels = new ArrayList<OntModel>();
        for (Iterator<OntModel> j = m.listSubModels(); j.hasNext(); ) {
            importModels.add( j.next() );
        }

        assertEquals( "n import models should be ", 3, importModels.size() );

        boolean isOntModel = true;
        int nImports = 0;

        for (Iterator<OntModel> i = importModels.iterator(); i.hasNext(); ) {
            Object x = i.next();
            if (!(x instanceof OntModel)) {
                isOntModel = false;
            }
            else {
                // count the number of imports of each sub-model
                nImports += ((OntModel) x).countSubModels();
            }
        }

        assertTrue( "All import models should be OntModels", isOntModel );

        // listSubModels' default behaviour is *not* to include imports of sub-models
        assertEquals( "Wrong number of sub-model imports", 0, nImports );
    }

    public void testListSubModels1() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URL
        m.read( TestHelper.getResourceURL("testing/ontology/testImport6/a.owl" ));
//        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( "Marker count not correct", 4, TestOntDocumentManager.countMarkers( m ) );

        List<OntModel> importModels = new ArrayList<OntModel>();
        for (Iterator<OntModel> j = m.listSubModels( true ); j.hasNext(); ) {
            importModels.add( j.next() );
        }

        assertEquals( "n import models should be ", 3, importModels.size() );

        boolean isOntModel = true;
        int nImports = 0;

        for (Iterator<OntModel> i = importModels.iterator(); i.hasNext(); ) {
            Object x = i.next();
            if (!(x instanceof OntModel)) {
                isOntModel = false;
            }
            else {
                // count the number of imports of each sub-model
                nImports += ((OntModel) x).countSubModels();
            }
        }

        assertTrue( "All import models should be OntModels", isOntModel );
        assertEquals( "Wrong number of sub-model imports", 2, nImports );
    }

    public void testGetImportedModel() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URL
        m.read( TestHelper.getResourceURL("testing/ontology/testImport6/a.owl") );
//        m.read( "file:testing/ontology/testImport6/a.owl" );
//
        OntModel m0 = m.getImportedModel( "file:testing/ontology/testImport6/b.owl" );
        OntModel m1 = m.getImportedModel( "file:testing/ontology/testImport6/c.owl" );
        OntModel m2 = m.getImportedModel( "file:testing/ontology/testImport6/d.owl" );
        OntModel m3 = m.getImportedModel( "file:testing/ontology/testImport6/b.owl" )
                       .getImportedModel( "file:testing/ontology/testImport6/c.owl" );
        OntModel m4 = m.getImportedModel( "file:testing/ontology/testImport6/a.owl" );
//      OntModel m0 = m.getImportedModel( TestHelper.getResourceURL("testing/ontology/testImport6/b.owl") );
//      OntModel m1 = m.getImportedModel( TestHelper.getResourceURL("testing/ontology/testImport6/c.owl") );
//      OntModel m2 = m.getImportedModel( TestHelper.getResourceURL("testing/ontology/testImport6/d.owl") );
//      OntModel m3 = m.getImportedModel( TestHelper.getResourceURL("testing/ontology/testImport6/b.owl") )
//                     .getImportedModel( TestHelper.getResourceURL("testing/ontology/testImport6/c.owl") );
//      OntModel m4 = m.etImportedModel( TestHelper.getResourceURL("testing/ontology/testImport6/a.owl") );

        assertNotNull( "Import model b should not be null", m0 );
        assertNotNull( "Import model c should not be null", m1 );
        assertNotNull( "Import model d should not be null", m2 );
        assertNotNull( "Import model b-c should not be null", m3 );
        assertNull( "Import model a should be null", m4 );
    }

    /**
     * Test that the supports checks that are defined in the OWL full profile are not
     * missing in the DL and Lite profiles, unless by design.
     * Not strictly a model test, but it has to go somewhere */
    public void testProfiles() {
        List<Class<?>> notInDL = Arrays.asList( new Class<?>[] {} );
        List<Class<?>> notInLite = Arrays.asList( new Class<?>[] {DataRange.class, HasValueRestriction.class} );

        Map<?,?> fullProfileMap = new OWLProfileExt().getSupportsMap();
        Map<?,?> dlProfileMap = new OWLDLProfileExt().getSupportsMap();
        Map<?,?> liteProfileMap = new OWLLiteProfileExt().getSupportsMap();

        for (Iterator<?> i = fullProfileMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<?,?> kv = (Map.Entry<?,?>) i.next();
            Class<?> c = (Class<?>) kv.getKey();
            assertTrue( "Key in OWL DL profile: " + c.getName(), dlProfileMap.containsKey( c ) || notInDL.contains( c ));
            assertTrue( "Key in OWL lite profile: " + c.getName(), liteProfileMap.containsKey( c ) || notInLite.contains( c ));
        }
    }


    /**
        Added by kers to ensure that bulk update works; should really be a test
        of the ontology Graph using AbstractTestGraph, but that fails because there
        are too many things that don't pass those tests.
    <p>
        <b>Yet</b>.
    */
    public void testBulkAddWorks()
        {
        OntModel om1= ModelFactory.createOntologyModel();
        OntModel om2 = ModelFactory.createOntologyModel();
        om1.add( om2 );
        }

    public void testRead() {
        String base0 = "http://example.com/test0";
        String ns0 = base0 + "#";
        String base1 = "http://example.com/test1";
        String ns1 = base1 + "#";

        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        m.getDocumentManager().reset();
      //ANDROID: changed to use classloader URL
        String file = TestHelper.getResourceURL("testing/ontology/relativenames.rdf");
        m.getDocumentManager().addAltEntry( base0, file );
        m.read( base0, "RDF/XML" );
        assertNotNull( "Should be a class ns0:A", m.getOntClass( ns0 + "A" ) );
        assertNull( "Should not be a class ns1:A", m.getOntClass( ns1 + "A" ) );

        m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        m.getDocumentManager().reset();
        m.getDocumentManager().addAltEntry( base0, file );
        m.read( base0, base1, "RDF/XML" );
        assertNull( "Should not be a class ns0:A", m.getOntClass( ns0 + "A" ) );
        assertNotNull( "Should be a class ns1:A", m.getOntClass( ns1 + "A" ) );
//        m.getDocumentManager().addAltEntry( base0, "file:testing/ontology/relativenames.rdf" );
//        m.read( base0, "RDF/XML" );
//        assertNotNull( "Should be a class ns0:A", m.getOntClass( ns0 + "A" ) );
//        assertNull( "Should not be a class ns1:A", m.getOntClass( ns1 + "A" ) );
//
//        m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
//        m.getDocumentManager().reset();
//        m.getDocumentManager().addAltEntry( base0, "file:testing/ontology/relativenames.rdf" );
//        m.read( base0, base1, "RDF/XML" );
//        assertNull( "Should not be a class ns0:A", m.getOntClass( ns0 + "A" ) );
//        assertNotNull( "Should be a class ns1:A", m.getOntClass( ns1 + "A" ) );
    }

    public void testListDataRange() {
        String base = "http://jena.hpl.hp.com/test#";
        String doc =
                 "<?xml version='1.0'?>"
                + "<!DOCTYPE owl ["
                + "      <!ENTITY rdf  'http://www.w3.org/1999/02/22-rdf-syntax-ns#' >"
                + "      <!ENTITY rdfs 'http://www.w3.org/2000/01/rdf-schema#' >"
                + "      <!ENTITY xsd  'http://www.w3.org/2001/XMLSchema#' >"
                + "      <!ENTITY owl  'http://www.w3.org/2002/07/owl#' >"
                + "      <!ENTITY dc   'http://purl.org/dc/elements/1.1/' >"
                + "      <!ENTITY base  'http://jena.hpl.hp.com/test' >"
                + "    ]>"
                + "<rdf:RDF"
                + "   xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "   xmlns:owl='http://www.w3.org/2002/07/owl#'>"
                + "  <owl:DataRange>"
                + "    <owl:oneOf>"
                + "      <rdf:List>"
                + "        <rdf:first rdf:datatype='&xsd;integer'>0</rdf:first>"
                + "        <rdf:rest rdf:resource='&rdf;nil' />"
                + "      </rdf:List>"
                + "    </owl:oneOf>"
                + "  </owl:DataRange>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.read(new StringReader(doc), base);

        Iterator<DataRange> i = m.listDataRanges();
        assertTrue( "Should be at least one DataRange", i.hasNext() );
        Object dr = i.next();
        assertInstanceOf( DataRange.class, dr );
        assertFalse( "Should no more DataRange", i.hasNext() );
    }



    public void testListHierarchyRoots0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        assertFalse( m.listHierarchyRootClasses().hasNext() );
        m = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM );
        assertFalse( m.listHierarchyRootClasses().hasNext() );
    }

    public void testListHierarchyRoots1() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a owl:Class. "
                ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        TestUtil.assertIteratorValues( this, m.listHierarchyRootClasses(),
                                       new Object[] {a} );
    }


    public void testListHierarchyRoots2() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a owl:Class. "
                ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        TestUtil.assertIteratorValues( this, m.listHierarchyRootClasses(),
                                       new Object[] {a} );
    }


    public void testListHierarchyRoots3() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a owl:Class. "
                + ":B a owl:Class ; rdfs:subClassOf :A . "
                ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MINI_RULE_INF, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        TestUtil.assertIteratorValues( this, m.listHierarchyRootClasses(),
                                       new Object[] {a} );
    }

    public void testListHierarchyRoots4() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a rdfs:Class. "
                + ":C a rdfs:Class. "
                + ":B a rdfs:Class ; rdfs:subClassOf :A . "
                ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        OntClass c = m.getOntClass(NS+"C");
        TestUtil.assertIteratorValues( this, m.listHierarchyRootClasses(),
                                       new Object[] {a,c} );
    }

    /* Auto-loading of imports is off by default */
    public void testLoadImports0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );
        m.add( a, m.getProfile().IMPORTS(), b );

        // not dymamically imported by default
        assertEquals( "Marker count not correct", 0, TestOntDocumentManager.countMarkers( m ) );

        assertFalse( "c should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertFalse( "b should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );

        m.loadImports();

        assertEquals( "Marker count not correct", 2, TestOntDocumentManager.countMarkers( m ) );

        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertTrue( "b should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );
    }


    /* Auto-loading of imports = on */
    public void testLoadImports1() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );

        m.setDynamicImports( true );
        m.add( a, m.getProfile().IMPORTS(), b );

        assertEquals( "Marker count not correct", 2, TestOntDocumentManager.countMarkers( m ) );

        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertTrue( "b should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );

        // this should have no effect
        m.loadImports();

        assertEquals( "Marker count not correct", 2, TestOntDocumentManager.countMarkers( m ) );

        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertTrue( "b should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );
    }

    /** Test getting conclusions after loading imports */
    public void testAddImports0() {
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        base.createClass( NS + "A" );
        base.createClass( NS + "B" );

        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, base );

        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );

        // nothing is known about a and b yet
        assertFalse( a.hasSubClass( b ) );

        // add some ontology data
        OntModel imp = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        imp.add( b, RDFS.subClassOf, a );

        m.addSubModel( imp, true );
        assertTrue( a.hasSubClass( b ) );
    }

    public void testAddImports1() {
        String ns = "http://jena.hpl.hp.com/2003/03/testont";
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        OntDocumentManager odm = OntDocumentManager.getInstance();
        odm.addAltEntry( ns + "#a", "file:testing/ontology/testImport7/a.owl" );


        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, base );

        Ontology oo = base.createOntology( ns );
        oo.addImport( base.createResource( ns + "#a") );

        // nothing is known about a and b yet
        Resource a = m.getResource( ns + "#A" );
        Resource c = m.getResource( ns + "#C" );
        assertFalse( m.contains( c, RDFS.subClassOf, a ) );

        // when we load the imports, the odm must kick the reasoner with a rebind()
        m.getDocumentManager().loadImports( m );
        assertTrue( m.contains( c, RDFS.subClassOf, a ) );
    }

    /** Getting the deductions model of an OntModel
     * see also {@link TestBugs#testOntModelGetDeductions()}
     * <p>ijd: Feb 6th, 2008 - this test has been disabled for
     * the time being, since it is not correct as written. However,
     * I'm not removing or changing it just yet, since it is showing up
     * an infelicity in the rule engine that Dave will investigate
     * at some future date.</p>
     * */
    public void xxtestGetDeductionsModel0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        OntClass c = m.createClass( NS + "C" );

        b.addSubClass( c );

        // we see the entailments only in the deductions model
        Model dm = m.getDeductionsModel();
        assertTrue( dm.contains( OWL.Nothing, RDFS.subClassOf, a ) );
        assertTrue( dm.contains( OWL.Nothing, RDFS.subClassOf, c ) );

        a.addSubClass( b );

        assertTrue( a.hasSubClass( c ));

        dm = m.getDeductionsModel();

        assertFalse( dm.contains( OWL.Nothing, RDFS.subClassOf, a ) );
        assertTrue( dm.contains( OWL.Nothing, RDFS.subClassOf, c ) );
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * Answer true iff an iterator contains a given value.
     */
    private boolean iteratorContains( Iterator<?> i, Object x ) {
        boolean found = false;
        while (i.hasNext()) {
            found = i.next().equals( x ) || found;
        }
        return found;
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected class OWLProfileExt extends OWLProfile
    {
        public Map<?,?> getSupportsMap() {
            return getCheckTable();
        }
    }

    protected class OWLDLProfileExt extends OWLDLProfile
    {
        public Map<?,?> getSupportsMap() {
            return getCheckTable();
        }
    }

    protected class OWLLiteProfileExt extends OWLLiteProfile
    {
        public Map<?,?> getSupportsMap() {
            return getCheckTable();
        }
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

