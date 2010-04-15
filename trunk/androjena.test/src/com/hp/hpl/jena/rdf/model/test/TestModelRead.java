/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelRead.java,v 1.1 2009/06/29 08:55:33 castagna Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.ConfigException;
import com.hp.hpl.jena.shared.JenaException;

/**
     TestModelRead - test that the new model.read operation(s) exist.
     @author kers
 */
public class TestModelRead extends ModelTestBase
    {
    protected static Logger logger = LoggerFactory.getLogger( TestModelRead.class );

    public TestModelRead( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelRead.class ); }
    
    public void testReturnsSelf()
        {
        Model m = ModelFactory.createDefaultModel();
        //ANDROID: changed to use classloader URL
        assertSame( m, m.read( TestHelper.getResourceURL("testing/modelReading/empty.n3"), "base", "N3" ) );
//        assertSame( m, m.read( "file:testing/modelReading/empty.n3", "base", "N3" ) );
        assertTrue( m.isEmpty() );
        }
    
    public void testGRDDLConfigMessage() {
    	Model m = ModelFactory.createDefaultModel();
    	try {
    		m.read("http://www.w3.org/","GRDDL");
    		// ok.
    	}
    	catch (ConfigException e) {
    		// expected.
    	}
    }
    public void testLoadsSimpleModel()
        {
        Model expected = ModelFactory.createDefaultModel();
        Model m = ModelFactory.createDefaultModel();
        //ANDROID: changed to use classloader URL
        expected.read( TestHelper.getResourceURL("testing/modelReading/simple.n3"), "N3" );
        assertSame( m, m.read( TestHelper.getResourceURL("testing/modelReading/simple.n3"), "base", "N3" ) );
//        expected.read( "file:testing/modelReading/simple.n3", "N3" );
//        assertSame( m, m.read( "file:testing/modelReading/simple.n3", "base", "N3" ) );
        assertIsoModels( expected, m );
        }    
    
    /*
         Suppressed, since the other Model::read(String url) operations apparently
         don't retry failing URLs as filenames. But the code text remains, so that
         when-and-if, we have a basis.
     */
//    public void testLoadsSimpleModelWithoutProtocol()
//        {
//        Model expected = ModelFactory.createDefaultModel();
//        Model m = ModelFactory.createDefaultModel();
//        expected.read( "testing/modelReading/simple.n3", "RDF/XML" );
//        assertSame( m, m.read( "testing/modelReading/simple.n3", "base", "N3" ) );
//        assertIsoModels( expected, m );
//        }    
    
    public void testSimpleLoadImplictBase()
        {
        Model mBasedImplicit = ModelFactory.createDefaultModel();
        //ANDROID: changed to use classloader URL
        String fn = TestHelper.getResourceURL("testing/modelReading/based.n3");
        Model wanted = 
            ModelFactory.createDefaultModel()
            .add( resource( fn ), property( "ja:predicate" ), resource( "ja:object" ) );
        mBasedImplicit.read(fn , "N3" );
//        String fn = IRIResolver.resolveFileURL("file:testing/modelReading/based.n3" );
//        Model wanted = 
//            ModelFactory.createDefaultModel()
//            .add( resource( fn ), property( "ja:predicate" ), resource( "ja:object" ) );
//        mBasedImplicit.read( fn, "N3" );
        assertIsoModels( wanted, mBasedImplicit );
        }
    
    public void testSimpleLoadExplicitBase()
        {
        Model mBasedExplicit = ModelFactory.createDefaultModel();
        //ANDROID: changed to use classloader URL
        mBasedExplicit.read( TestHelper.getResourceURL("testing/modelReading/based.n3"), "http://example/", "N3" );
//        mBasedExplicit.read( "file:testing/modelReading/based.n3", "http://example/", "N3" );
        assertIsoModels( modelWithStatements( "http://example/ ja:predicate ja:object" ), mBasedExplicit );
        }
    
    public void testDefaultLangXML()
        {
        Model m = ModelFactory.createDefaultModel();
        //ANDROID: changed to use classloader URL
        m.read( TestHelper.getResourceURL("testing/modelReading/plain.rdf"), null, null );
//        m.read( "file:testing/modelReading/plain.rdf", null, null );
        }
    
    public void testContentNegotiation() {
		Model m = ModelFactory.createDefaultModel();
//		Model m2 = ModelFactory.createDefaultModel();

		try {
			m.read("http://jena.sourceforge.net/test/mime/test1");
		    assertEquals(m.size(),1);
//		    m2.read("http://xmlns.com/foaf/0.1/");
		} catch (JenaException jx) {
			if (jx.getCause() instanceof NoRouteToHostException
					|| jx.getCause() instanceof UnknownHostException
					|| jx.getCause() instanceof ConnectException
					|| jx.getCause() instanceof IOException) {
				logger
						.warn("Cannot access public internet - content negotiation test not executed");
			} else
				throw jx;
		}
	}
    
    }


/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All
 * rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */