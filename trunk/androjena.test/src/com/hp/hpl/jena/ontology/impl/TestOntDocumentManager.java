/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            4 Mar 2003
 * Filename           $RCSfile: TestOntDocumentManager.java,v $
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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.util.Log;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.ProfileRegistry;
import com.hp.hpl.jena.ontology.OntDocumentManager.ReadFailureHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.OntDocManagerVocab;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * <p>
 * Unit tests for document manager
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: TestOntDocumentManager.java,v 1.2 2009/10/06 13:04:42 ian_dickinson Exp $
 */
public class TestOntDocumentManager
    extends TestCase
{
    // Constants
    //////////////////////////////////

    private static Boolean F = Boolean.FALSE;
    private static Boolean T = Boolean.TRUE;

    // Static variables
    //////////////////////////////////

    /** Logger for this class */
    private static Logger log = LoggerFactory.getLogger( TestOntDocumentManager.class );

    public static final Integer cnt( int x ) {return new Integer(x);}

    /* Data for various combinations of test import conditions */
    public static Object[][] s_testData = new Object[][] {
        // directory to look in             marker count        imports     path (null = default)
        {  "testing/ontology/testImport1",  cnt(1),             T,          null },
        {  "testing/ontology/testImport2",  cnt(2),             T,          null },
        {  "testing/ontology/testImport2",  cnt(1),             F,          null },
        {  "testing/ontology/testImport3",  cnt(3),             T,          null },
        {  "testing/ontology/testImport4",  cnt(2),             T,          null },
        {  "testing/ontology/testImport5",  cnt(2),             T,          "file:testing/ontology/testImport5/ont-policy.rdf" }
    };


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    public TestOntDocumentManager( String s ) {
        super( s );
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite( "TestOntDocumentManager" );

        // add the fixed test cases
        suite.addTestSuite( TestOntDocumentManager.class );

        // add the data-driven test cases
        for (int i = 0;  i < s_testData.length;  i++) {
            suite.addTest( new DocManagerImportTest( (String) s_testData[i][0],
                                                     ((Integer) s_testData[i][1]).intValue(),
                                                     ((Boolean) s_testData[i][2]).booleanValue(),
                                                     (String) s_testData[i][3]) );
        }
        return suite;
    }


    // External signature methods
    //////////////////////////////////

    @Override
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );

        // forget any cached models in the model spec
        // TODO remove this once we rationalise modelmakers in the OntModel code
        Set<String> modelNames = new HashSet<String>();
        ModelMaker memMaker = OntModelSpec.OWL_MEM.getImportModelMaker();
        for (Iterator<String> i = memMaker.listModels(); i.hasNext(); ) {
            modelNames.add( i.next() );
        }

        for (Iterator<String> i = modelNames.iterator(); i.hasNext(); ) {
            String mn = i.next();
            memMaker.removeModel( mn );
        }
    }

    public void testConstruct0() {
        OntDocumentManager m = new OntDocumentManager();
        assertNotNull( m );
        assertEquals( m.getMetadataSearchPath(), OntDocumentManager.DEFAULT_METADATA_PATH );
    }

    public void testConstruct1() {
        OntDocumentManager mgr = new OntDocumentManager( "" );
        assertTrue( "Should be no specification loaded", !mgr.listDocuments().hasNext() );
    }

    public void testConstruct2() {
        // make sure we don't fail on null
        OntDocumentManager mgr = new OntDocumentManager( (String) null );
        assertTrue( "Should be no specification loaded", !mgr.listDocuments().hasNext() );
    }

    public void testConstruct3() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addProperty( RDF.type, OntDocManagerVocab.OntologySpec );
        r.addProperty( OntDocManagerVocab.publicURI, m.createResource("http://example.com/foo") );
        r.addProperty( OntDocManagerVocab.altURL, m.createResource("file:local.rdf") );

        OntDocumentManager mgr = new OntDocumentManager( m );
        assertEquals( "cache URL not correct", "file:local.rdf", mgr.doAltURLMapping( "http://example.com/foo" ));
    }

    public void testInitialisation() {
        OntDocumentManager mgr = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );

        assertTrue( "Should be at least one specification loaded", mgr.listDocuments().hasNext() );
        assertNotNull( "cache URL for owl should not be null", mgr.doAltURLMapping( "http://www.w3.org/2002/07/owl" ));
        assertEquals( "cache URL for owl not correct", "file:vocabularies/owl.owl", mgr.doAltURLMapping( "http://www.w3.org/2002/07/owl" ));
    }

    public void testGetInstance() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        assertNotNull( odm );

        OntDocumentManager odm2 = OntDocumentManager.getInstance();
        assertSame( odm, odm2 );
    }

    public void testSetMetadataSearchPath() {
        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        assertEquals( "file:etc/ont-policy-test.rdf", odm.getMetadataSearchPath() );
        assertTrue( odm.listDocuments().hasNext() );
        assertEquals( "file:etc/ont-policy-test.rdf", odm.getLoadedPolicyURL() );

        odm.setMetadataSearchPath( "file:notexist.rdf", false );
        assertTrue( odm.listDocuments().hasNext() );
        assertNull( odm.getLoadedPolicyURL() );

        odm.setMetadataSearchPath( "file:notexist.rdf", true );
        assertFalse( odm.listDocuments().hasNext() );
        assertNull( odm.getLoadedPolicyURL() );

        odm.setMetadataSearchPath( "file:etc/ont-policy-test.rdf", false );
        assertTrue( odm.listDocuments().hasNext() );
        assertEquals( "file:etc/ont-policy-test.rdf", odm.getLoadedPolicyURL() );
    }

    public void testConfigure0() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addProperty( RDF.type, OntDocManagerVocab.OntologySpec );
        r.addProperty( OntDocManagerVocab.publicURI, m.createResource("http://example.com/foo") );
        r.addProperty( OntDocManagerVocab.altURL, m.createResource("file:local.rdf") );

        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        TestUtil.assertIteratorLength( odm.listDocuments(), 3 );

        odm.configure( m, false );
        TestUtil.assertIteratorLength( odm.listDocuments(), 4 );
    }

    public void testConfigure1() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addProperty( RDF.type, OntDocManagerVocab.OntologySpec );
        r.addProperty( OntDocManagerVocab.publicURI, m.createResource("http://example.com/foo") );
        r.addProperty( OntDocManagerVocab.altURL, m.createResource("file:local.rdf") );

        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        TestUtil.assertIteratorLength( odm.listDocuments(), 3 );

        odm.configure( m );
        TestUtil.assertIteratorLength( odm.listDocuments(), 1 );
    }

    public void testConfigure2() {
        // create a simple policy
        Model m = ModelFactory.createDefaultModel();
        Resource policy = m.createResource();
        m.add( policy, RDF.type, OntDocManagerVocab.DocumentManagerPolicy );
        m.addLiteral( policy, OntDocManagerVocab.cacheModels, false );

        OntDocumentManager mgr = new OntDocumentManager( (String) null );
        assertTrue( mgr.getCacheModels() );
        mgr.configure( m );
        assertFalse( "Docmgr configure() should have updated cache models flag", mgr.getCacheModels() );
    }


    public void testReset() {
        OntDocumentManager mgr = new OntDocumentManager( (String) null );

        assertTrue( mgr.getProcessImports() );
        mgr.setProcessImports( false );
        assertFalse( mgr.getProcessImports() );
        mgr.reset();
        assertTrue( mgr.getProcessImports() );

        mgr.setMetadataSearchPath( "file:foo.xml", true );
        assertEquals( "file:foo.xml", mgr.getMetadataSearchPath() );
        mgr.reset();
        assertEquals( "file:foo.xml", mgr.getMetadataSearchPath() );

        assertTrue( mgr.getCacheModels() );
        mgr.setCacheModels(false );
        assertFalse( mgr.getCacheModels() );
        mgr.reset();
        assertTrue( mgr.getCacheModels() );
    }

    public void testDoAltMapping() {
        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        assertEquals( "file:vocabularies/owl.owl", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ));
        assertEquals( "http://example.com/nocache", odm.doAltURLMapping( "http://example.com/nocache" ));
    }

    public void testAddModel0() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        Model m = ModelFactory.createDefaultModel();
        String uri = "http://example.com/test#m";
        assertNull( odm.getModel( uri ));
        odm.addModel( uri, m );
        assertSame( m, odm.getModel(uri));
    }

    public void testAddModel1() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        Model m0 = ModelFactory.createDefaultModel();
        Model m1 = ModelFactory.createDefaultModel();
        String uri = "http://example.com/test#m";
        assertNull( odm.getModel( uri ));
        odm.addModel( uri, m0 );

        // add duplicate with no replace
        odm.addModel( uri, m1 );
        assertSame( m0, odm.getModel(uri));

        // add duplicate with replace
        odm.addModel( uri, m1, true );
        assertSame( m1, odm.getModel(uri));
    }

    public void testClearCache() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        Model m = ModelFactory.createDefaultModel();
        String uri = "http://example.com/test#m";
        assertNull( odm.getModel( uri ));
        odm.addModel( uri, m );
        odm.clearCache();
        assertSame( null, odm.getModel(uri));
    }

    public void testForget() {
        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        assertEquals( "file:vocabularies/owl.owl", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ) );
        OntModel m = ModelFactory.createOntologyModel();
        odm.addModel( "http://www.w3.org/2002/07/owl#", m );
        assertNotNull( odm.getModel( "http://www.w3.org/2002/07/owl#" ));

        odm.forget( "http://www.w3.org/2002/07/owl#" );
        odm.forget( "http://www.w3.org/2002/07/owl" );

        assertEquals( "http://www.w3.org/2002/07/owl", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ) );
        assertNull( odm.getModel( "http://www.w3.org/2002/07/owl#" ));
    }

    public void testGetOntology() {
        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        OntModel m = odm.getOntology( "http://www.w3.org/2002/07/owl", OntModelSpec.OWL_MEM );
        assertNotNull( m );
        assertSame( odm, m.getDocumentManager() );
        OntModel m1 = odm.getOntology( "http://www.w3.org/2002/07/owl", OntModelSpec.OWL_MEM );
        assertSame( m, m1 );
    }

    public void testProcessImports() {
        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        assertTrue( odm.getProcessImports() );
        odm.setProcessImports( false );
        assertFalse( odm.getProcessImports() );
    }

    public void testCacheModels() {
        OntDocumentManager odm = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        assertTrue( odm.getCacheModels() );
        odm.setCacheModels( false );
        assertFalse( odm.getCacheModels() );
    }

    public void testManualAssociation() {
        OntDocumentManager odm = new OntDocumentManager( (String) null );

        odm.addAltEntry( "http://www.w3.org/2002/07/owl", "file:foo.bar" );
        assertEquals( "Failed to retrieve cache location", "file:foo.bar", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ) );
    }


    public void testIgnoreImport() throws IOException {
        OntDocumentManager odm = new OntDocumentManager();
        //ANDROID: copy files to filesystem and add mappings for imports
        File aTemp = TestHelper.copyFile("testing/ontology/testImport3/a.owl");
        File bTemp = TestHelper.copyFile("testing/ontology/testImport3/b.owl");
        File cTemp = TestHelper.copyFile("testing/ontology/testImport3/c.owl");
        
        odm.addAltEntry("file:testing/ontology/testImport3/b.owl", "file:" + bTemp.getAbsolutePath());
        odm.addAltEntry("file:testing/ontology/testImport3/c.owl", "file:" + cTemp.getAbsolutePath());
        odm.addAltEntry("file:testing/ontology/testImport3/a.owl", "file:" + aTemp.getAbsolutePath());
        TestUtil.assertIteratorLength( odm.listIgnoredImports(), 0 );

        //ANDROID: changed to use classloader URI
//        String ignoredImport = TestHelper.getResourceURL("testing/ontology/testImport3/c.owl");
//        odm.addIgnoreImport( ignoredImport );
        odm.addIgnoreImport( "file:testing/ontology/testImport3/c.owl" );
        TestUtil.assertIteratorLength( odm.listIgnoredImports(), 1 );
//        assertTrue( odm.ignoringImport( ignoredImport));
//        assertFalse( odm.ignoringImport( TestHelper.getResourceURL("testing/ontology/foo.owl")));
        assertTrue( odm.ignoringImport( "file:testing/ontology/testImport3/c.owl"));
        assertFalse( odm.ignoringImport( "file:testing/ontology/foo.owl"));

        OntModelSpec spec = new OntModelSpec( null, odm, null, ProfileRegistry.OWL_LANG );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        assertNotNull( "Ontology model should not be null", m );
        
//        m.read(TestHelper.getResourceURL("testing/ontology/testImport3/a.owl") );
        m.read( "file:testing/ontology/testImport3/a.owl" );
        aTemp.delete();
        bTemp.delete();
        cTemp.delete();
        assertEquals( "Marker count not correct", 2, countMarkers( m ));

//        odm.removeIgnoreImport(ignoredImport );
        odm.removeIgnoreImport( "file:testing/ontology/testImport3/c.owl" );
        TestUtil.assertIteratorLength( odm.listIgnoredImports(), 0 );
        assertFalse( odm.ignoringImport( "file:testing/ontology/testImport3/c.owl"));
    }

    /** Simple case: a imports b, b imports c, remove c */
    public void testUnloadImport1() {
    	
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URI
        m.read( TestHelper.getResourceURL("testing/ontology/testImport3/a.owl" ));
      //ANDROID: list imported models for debug
        for(String importedUri : m.listImportedOntologyURIs()) {
        	Log.d("TestOntDocumentManager", "imported URI: " + importedUri);
        }
//        m.read( "file:testing/ontology/testImport3/a.owl" );
        assertEquals( "Marker count not correct", 3, countMarkers( m ) );
//        assertTrue( "c should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport3/c.owl" )) );
        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
//        m.getDocumentManager().unloadImport( m, TestHelper.getResourceURL("testing/ontology/testImport3/c.owl" ));
        m.getDocumentManager().unloadImport( m, "file:testing/ontology/testImport3/c.owl" );
        assertEquals( "Marker count not correct", 2, countMarkers( m ) );
//        assertFalse( "c should not be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport3/c.owl" )) );
        assertFalse( "c should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
    }

    /** case 2: a imports b, b imports c, remove b */
    public void testUnloadImport2() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URI
        m.read(TestHelper.getResourceURL("testing/ontology/testImport3/a.owl" ));
//        m.read( "file:testing/ontology/testImport3/a.owl" );
      //ANDROID: list imported models for debug
        for(String importedUri : m.listImportedOntologyURIs()) {
        	Log.d("TestOntDocumentManager", "imported URI: " + importedUri);
        }
        assertEquals( "Marker count not correct", 3, countMarkers( m ) );
//        assertTrue( "c should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport3/c.owl" )) );
//        assertTrue( "b should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport3/b.owl" )) );
//        m.getDocumentManager().unloadImport( m, TestHelper.getResourceURL("testing/ontology/testImport3/b.owl" ));
        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertTrue( "b should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );
        m.getDocumentManager().unloadImport( m, "file:testing/ontology/testImport3/b.owl" );
        assertEquals( "Marker count not correct", 1, countMarkers( m ) );
//        assertFalse( "c should not be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport3/c.owl" )) );
//        assertFalse( "b should not be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport3/b.owl" )) );
        assertFalse( "c should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertFalse( "b should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );
    }

    /** case 3: a imports b, b imports c, a imports d, d imports c, remove b */
    public void testUnloadImport3() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URI
        m.read( TestHelper.getResourceURL("testing/ontology/testImport6/a.owl" ));
//        m.read( "file:testing/ontology/testImport6/a.owl" );
      //ANDROID: list imported models for debug
        for(String importedUri : m.listImportedOntologyURIs()) {
        	Log.d("TestOntDocumentManager", "imported URI: " + importedUri);
        }
        assertEquals( "Marker count not correct", 4, countMarkers( m ) );

//        assertTrue( "c should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport6/c.owl" )) );
//        assertTrue( "b should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport6/b.owl" )) );
//        assertTrue( "d should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport6/d.owl" )) );
//        m.getDocumentManager().unloadImport( m, TestHelper.getResourceURL("testing/ontology/testImport6/b.owl" ));
//        assertEquals( "Marker count not correct", 3, countMarkers( m ) );
//        assertTrue( "c should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport6/c.owl" )) );
//        assertTrue( "d should be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport6/d.owl" )) );
//        assertFalse( "b should not be imported", m.hasLoadedImport( TestHelper.getResourceURL("testing/ontology/testImport6/b.owl" )) );
        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport6/c.owl" ) );
        assertTrue( "b should be imported", m.hasLoadedImport( "file:testing/ontology/testImport6/b.owl" ) );
        assertTrue( "d should be imported", m.hasLoadedImport( "file:testing/ontology/testImport6/d.owl" ) );
        m.getDocumentManager().unloadImport( m, "file:testing/ontology/testImport6/b.owl" );
        assertEquals( "Marker count not correct", 3, countMarkers( m ) );
        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport6/c.owl" ) );
        assertTrue( "d should be imported", m.hasLoadedImport( "file:testing/ontology/testImport6/d.owl" ) );
        assertFalse( "b should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport6/b.owl" ) );
    }

    public void testDynamicImports1() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );
        m.add( a, m.getProfile().IMPORTS(), b );

        // not dymamically imported by default
        assertEquals( "Marker count not correct", 0, countMarkers( m ) );

        assertFalse( "c should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertFalse( "b should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );
    }


    public void testDynamicImports2() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );

        m.setDynamicImports( true );

        m.add( a, m.getProfile().IMPORTS(), b );

        // dymamically imported
        assertEquals( "Marker count not correct", 2, countMarkers( m ) );

        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertTrue( "b should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );
    }


    public void testDynamicImports3() {
        OntModel m = ModelFactory.createOntologyModel();
        //ANDROID: changed to use classloader URI
        m.read( TestHelper.getResourceURL("testing/ontology/testImport3/a.owl" ));
//        m.read( "file:testing/ontology/testImport3/a.owl" );
        assertEquals( "Marker count not correct", 3, countMarkers( m ) );

        assertTrue( "c should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertTrue( "b should be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );

        m.setDynamicImports( true );

        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );
        m.remove( m.createStatement( a, m.getProfile().IMPORTS(), b ) );

        assertEquals( "Marker count not correct", 1, countMarkers( m ) );
        assertFalse( "c should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ) );
        assertFalse( "b should not be imported", m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ) );
    }

    public void testSearchPath() {
        OntDocumentManager o1 = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        assertEquals( "Did not return correct loaded search path", "file:etc/ont-policy-test.rdf", o1.getLoadedPolicyURL() );

        OntDocumentManager o2 = new OntDocumentManager( "file:etc/ont-policy-test.notexist.rdf;file:etc/ont-policy-test.rdf" );
        assertEquals( "Did not return correct loaded search path", "file:etc/ont-policy-test.rdf", o2.getLoadedPolicyURL() );

        OntDocumentManager o3 = new OntDocumentManager( (String) null );
        assertNull( "Most recent policy should be null", o3.getLoadedPolicyURL() );

        o3.setMetadataSearchPath( "file:etc/ont-policy-test.rdf", true );
        assertEquals( "Did not return correct loaded search path", "file:etc/ont-policy-test.rdf", o2.getLoadedPolicyURL() );

        o3.setMetadataSearchPath( "file:etc/ont-policy-test.notexist.rdf", true );
        assertNull( "Most recent policy should be null", o3.getLoadedPolicyURL() );
    }

    public void testReadFailHandler0() {
        //ANDROID: changed to use classloader URI
        OntDocumentManager o1 = new OntDocumentManager( TestHelper.getResourceURL("etc/ont-policy-test.rdf") );
        assertNull( o1.getReadFailureHandler() );

        OntDocumentManager.ReadFailureHandler rfh = new OntDocumentManager.ReadFailureHandler() {
            public void handleFailedRead( String url, Model model, Exception e ) {/**/}};

        o1.setReadFailureHandler( rfh );
        assertSame( rfh, o1.getReadFailureHandler() );
    }

    public void testReadFailHandler1() {
        OntDocumentManager o1 = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );

        TestFailHandler rfh = new TestFailHandler();
        o1.setReadFailureHandler( rfh );

        // trigger the odm to read a non-existant source
        String source = "@prefix owl: <http://www.w3.org/2002/07/owl#> . <> a owl:Ontology ; owl:imports <http://example.com/not/exist>. ";
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
        spec.setDocumentManager(  o1 );
        OntModel m = ModelFactory.createOntologyModel( spec );
        m.read( new StringReader( source ), "http://example.com/foo#", "N3" );

        assertTrue( rfh.m_seen );
    }

    public void testReadHook0() {
        TestReadHook rh = new TestReadHook( false );
        OntDocumentManager o1 = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        o1.setReadHook( rh );
        o1.reset();

        String source = "@prefix owl: <http://www.w3.org/2002/07/owl#> . <> a owl:Ontology ; owl:imports <file:testing/ontology/testImport3/a.owl>. ";

        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
        spec.setDocumentManager(  o1 );
        OntModel m = ModelFactory.createOntologyModel( spec );
        m.read( new StringReader( source ), "http://example.com/foo#", "N3" );

        assertEquals( "Wrong number of calls to before load hook", 3, rh.m_before );
        assertEquals( "Wrong number of calls to before load hook", 3, rh.m_after );
    }


    public void testReadHook1() {
        TestReadHook rh = new TestReadHook( true );
        OntDocumentManager o1 = new OntDocumentManager( "file:etc/ont-policy-test.rdf" );
        o1.setReadHook( rh );
        o1.reset();

        String source = "@prefix owl: <http://www.w3.org/2002/07/owl#> . <> a owl:Ontology ; owl:imports <file:testing/ontology/testImport3/a.owl>. ";

        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
        spec.setDocumentManager(  o1 );
        OntModel m = ModelFactory.createOntologyModel( spec );
        m.read( new StringReader( source ), "http://example.com/foo#", "N3" );

        assertEquals( "Wrong number of calls to before load hook", 1, rh.m_before );
        assertEquals( "Wrong number of calls to after load hook", 1, rh.m_after );
    }


    /* count the number of marker statements in the combined model */
    public static int countMarkers( Model m ) {
        int count = 0;

        Resource marker = m.getResource( "http://jena.hpl.hp.com/2003/03/testont#Marker" );
        for (StmtIterator i = m.listStatements( null, RDF.type, marker ); i.hasNext();  ) {
            count++;
            i.next();
        }

        return count;
    }

    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * Document manager imports test case. Each test case starts with a root model (always a.owl in some
     * directory), and loads the model. Depending on the model contents, and the settings of the doc
     * manager, other models will be loaded. Each model is set to contain a fixed number of marker
     * statements of the form:
     * <code><pre>
     *   <Marker rdf:ID="a0" />
     * </pre></code>
     * the test for having correctly loaded the models is to count the markers and compare to the predicted
     * total.
     */
    static class DocManagerImportTest
        extends TestCase
    {
        String m_dir;
        int m_count;
        String m_path;
        boolean m_processImports;

        /* constuctor */
        DocManagerImportTest( String dir, int count, boolean processImports, String path ) {
            super( dir );
            m_dir = dir;
            m_count = count;
            m_path = path;
            m_processImports = processImports;
        }

        // external contract methods

        @Override
        public void runTest() {
            OntDocumentManager dm = new OntDocumentManager();

            // adjust the doc manager properties according to the test setup
            dm.setProcessImports( m_processImports );
            if (m_path != null) {
                dm.setMetadataSearchPath( m_path, true );
            }

            // now load the model - we always start from a.owl in the given directory
            OntModelSpec spec = new OntModelSpec( null, dm, null, ProfileRegistry.OWL_LANG );
            OntModel m = ModelFactory.createOntologyModel( spec, null );
            assertNotNull( "Ontology model should not be null", m );

            //ANDROID: changed to use classloader URI
            m.read(TestHelper.getResourceURL( m_dir + "/a.owl" ));
            assertEquals( "Marker count not correct", m_count, countMarkers( m ));
        }
    }

    static class TestFailHandler
        implements ReadFailureHandler
    {
        public boolean m_seen = false;
        public void handleFailedRead( String url, Model model, Exception e ) {
            m_seen = true;
            log.debug( "Seeing failed read of " + url, e );
        }

    }

    static class TestReadHook
        implements OntDocumentManager.ReadHook
    {
        private int m_before = 0;
        private int m_after = 0;
        private boolean m_renaming = false;

        TestReadHook( boolean renaming ) {
            m_renaming = renaming;
        }

        public void afterRead( Model model, String source, OntDocumentManager odm ) {
            m_after++;
        }

        public String beforeRead( Model model, String source, OntDocumentManager odm ) {
            if (m_renaming) {
                // local rewrite of the source file, which could be used e.g. to
                // get the source from a .jar file
                m_before++;
                return "file:testing/ontology/testImport3/c.owl";
            }
            else {
                m_before++;
                return source;
            }
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

