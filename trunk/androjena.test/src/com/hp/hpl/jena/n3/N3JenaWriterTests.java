/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3 ;

import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;
import junit.framework.TestSuite;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriterTests.java,v 1.1 2009/06/29 18:42:06 andy_seaborne Exp $
 */
public class N3JenaWriterTests extends N3ExternalTestsCom
{
	/* JUnit swingUI needed this */
    static public TestSuite suite() {
        return new N3JenaWriterTests() ;
    }
	
    static final String uriBase = "http://host/base/" ;
	
	public N3JenaWriterTests()
	{
		this("n3-writer-tests") ;
	}
	
	public N3JenaWriterTests(String filename)
	{
		super("N3 Jena Writer tests", filename) ;
	}

	
	@Override
    protected void makeTest(String inputFile, String resultsFile)
	{
		String testName = inputFile ;

		if ( basedir != null )
			inputFile = basedir+"/"+inputFile ;

		if ( basedir != null && resultsFile != null && !resultsFile.equals("") )
			resultsFile = basedir + "/" + resultsFile ;
			
        // Run on each of the writers
		addTest(new Test(testName, inputFile, resultsFile,
                         N3JenaWriter.n3WriterPrettyPrinter)) ;
        addTest(new Test(testName, inputFile, resultsFile,
                         N3JenaWriter.n3WriterPlain)) ;
        addTest(new Test(testName, inputFile, resultsFile,
                         N3JenaWriter.n3WriterTriples)) ;
	}


	static class Test extends TestCase
	{
        String writerName = null ;
		String testName = null ;
		String basename = null ;
		String inputFile = null ;
		String resultsFile = null ;	
		Reader data = null ;
		
		
		Test(String _testName, String _inputFile, String _resultsFile, String wName)
		{
			super("N3 Jena Writer test: "+_testName+"-"+wName) ;
			testName = _testName ;
			inputFile = _inputFile ;
			resultsFile = _resultsFile ;
            writerName = wName ;
		}
		
		@Override
        protected void runTest() throws Throwable
		{
			try {
				//ANDROID: changed to use classloader
				InputStream is = TestHelper.openResource(inputFile);
				if(is == null) throw new IOException();
				data = makeReader(is) ;
//				data = makeReader(new FileInputStream(inputFile)) ;
			} catch (IOException ioEx)
			{
				fail("File does not exist: "+inputFile) ;
				return ;
			}

			// Test: write model to a string, read it again and see if same/isomorphic
			
			Model model_1 = ModelFactory.createDefaultModel() ;
			model_1.read(data, uriBase, "N3") ;
            
			StringWriter w = new StringWriter() ;
            model_1.write(w, writerName, uriBase) ;
            // Check we really are writing different things!
            //model_1.write(System.out, writerName, uriBase) ;
			w.close() ;
			
			StringReader r = new StringReader(w.toString()) ;
			Model model_2 = ModelFactory.createDefaultModel() ;
			model_2.read(r, uriBase, "N3") ;
			
            if ( ! model_1.isIsomorphicWith(model_2) )
			{
				System.out.println("#### ---- "+testName+" ------------------------------") ;
                System.out.println("#### Model 1 ---- "+testName+" ------------------------------") ;
                TestHelper.dumpModel(model_1,"N3");
//                model_1.write(System.out, "N3") ;
                System.out.println("#### Model 2 --- "+testName+" ------------------------------") ;
                TestHelper.dumpModel(model_2,"N3");
//                model_2.write(System.out, "N3") ;
                assertTrue("Models don't match: "+testName, false) ;
			}
		}
	}
}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
