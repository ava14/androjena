/*
    (c) Copyright 2001, 2002, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.
    [See end of file]
    $Id: testWriterAndReader.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $
*/

package com.hp.hpl.jena.xmloutput;

import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.RDFSyntax;

/**
 * This will test any Writer and Reader pair.
 * It writes out a random model, and reads it back in.
 * The test fails if the models are not 'the same'.
 * Quite what 'the same' means is debatable.
 * @author  jjc
 
 * @version  Release='$Name: Jena-2_6_2 $' Revision='$Revision: 1.1 $' Date='$Date: 2009/07/04 16:41:34 $'
 */
public class testWriterAndReader 
    extends ModelTestBase implements RDFErrorHandler {
	static private boolean showProgress = false;
	//static private boolean errorDetail = false;
	static private int firstTest = 4;
	static private int lastTest = 9;
	static private int repetitionsJ = 6;
    
  protected static Logger logger = LoggerFactory.getLogger( testWriterAndReader.class );
    
	final String lang;
   
	final int fileNumber;

    final int options;
    
    String test;

    testWriterAndReader( String name, String lang, int fileNumber ) 
        { this( name, lang, fileNumber, 0 ); }
    
	testWriterAndReader(String name, String lang, int fileNumber, int options) {
		super( name );
		this.lang = lang;
		this.fileNumber = fileNumber;
		this.options = options;
	}
    
	@Override
    public String toString() {
		return getName()
			+ " "
			+ lang
			+ " t"
			+ fileNumber
			+ "000.rdf"
			+ (options != 0 ? ("[" + options + "]") : "");
	}
    
	static Test suiteN_TRIPLE()
        { return baseSuite( "N-TRIPLE" ); }
    
    static TestSuite suiteXML()
        { 
        TestSuite baseTests = baseSuite( "RDF/XML" );
        baseTests.addTestSuite( TestXMLFeatures_XML.class );
        baseTests.addTest( addXMLtests( "RDF/XML", false ) );
        return baseTests; 
        }
    
    static Test suiteXML_ABBREV()
        { 
        TestSuite suite = baseSuite( "RDF/XML-ABBREV" );
        suite.addTestSuite( TestXMLFeatures_XML_ABBREV.class );
        suite.addTestSuite( TestXMLAbbrev.class );
        suite.addTest( addXMLtests( "RDF/XML-ABBREV", false ) );
        return suite; 
        }
    
    public static TestSuite repeatedAbbrevSuite()
        { 
        TestSuite suite = baseSuite( "RDF/XML-ABBREV" );
        suite.addTestSuite( TestXMLFeatures_XML_ABBREV.class );
        suite.addTestSuite( TestXMLAbbrev.class );
        suite.addTest( addXMLtests( "RDF/XML-ABBREV", true ) );
        return suite; 
        }

    static TestSuite baseSuite( String lang ) 
        {
        TestSuite langsuite = new TestSuite();
        langsuite.setName( lang );
        langsuite.addTest( new testWriterInterface( "testWriting", lang ) );
        langsuite.addTest( new testWriterInterface( "testLineSeparator", lang ) );
        return langsuite;
        }
    
    public static class TestXMLFeatures_XML extends TestXMLFeatures
        {
        public TestXMLFeatures_XML( String name )
            { super( name, "RDF/XML" ); }
        }
    
    public static class TestXMLFeatures_XML_ABBREV extends TestXMLFeatures
        {
        public TestXMLFeatures_XML_ABBREV( String name )
            { super( name, "RDF/XML-ABBREV" ); }
        }
    
	static private boolean nBits( int i, int [] ok ) 
        {
		int bitCount = 0;
		while (i > 0) 
            {
			if ((i & 1) == 1) bitCount += 1;
			i >>= 1;
            }
		for (int j = 0; j < ok.length; j += 1)
			if (bitCount == ok[j]) return true;
		return false;
        }
    
    private static TestSuite addXMLtests( String lang, boolean lots )
        {
        TestSuite suite = new TestSuite();
        int optionLimit = (lang.equals( "RDF/XML-ABBREV" ) ? 1 << blockRules.length : 2);
        for (int fileNumber = firstTest; fileNumber <= lastTest; fileNumber++) 
            {
        	suite.addTest(new testWriterAndReader("testRandom", lang, fileNumber ) );
        	suite.addTest( new testWriterAndReader( "testLongId", lang, fileNumber ) );
            for (int optionMask = 1; optionMask < optionLimit; optionMask += 1) 
                {
        		if (lots || nBits( optionMask, new int[] { 1, /* 2,3,4,5, */ 6,7 } ))
        			suite.addTest( createTestOptions( lang, fileNumber, optionMask ) );
                }
            }
        return suite;
        }

    private static testWriterAndReader createTestOptions( String lang, int fileNumber, int optionMask )
        {
        return new testWriterAndReader( "testOptions " + fileNumber + " " + optionMask, lang, fileNumber, optionMask ) 
            {
            @Override
            public void runTest() throws IOException { testOptions(); }
            };
        }

	public void testRandom() throws IOException 
        {
		doTest( new String[] {}, new Object[] {} );
        }
    
	public void testLongId() throws IOException 
        {
		doTest( new String[] {"longId"}, new Object[] {Boolean.TRUE} );
        }
    
	static Resource [] blockRules =
		{
		RDFSyntax.parseTypeLiteralPropertyElt,
		RDFSyntax.parseTypeCollectionPropertyElt,
		RDFSyntax.propertyAttr,
		RDFSyntax.sectionReification,
		RDFSyntax.sectionListExpand,
		RDFSyntax.parseTypeResourcePropertyElt,
		DAML_OIL.collection 
        };
    
	public void testOptions() throws IOException 
        {
		Vector<Resource> v = new Vector<Resource>();
		for (int i = 0; i < blockRules.length; i += 1) 
            {
			if ((options & (1 << i)) != 0) v.add( blockRules[i] );
            }
		Resource blocked[] = new Resource[v.size()];
		v.copyInto( blocked );
		doTest( new String[] { "blockRules" }, new Resource[][] { blocked } );
        }
    
	public void doTest( String[] propNames, Object[] propVals ) throws IOException 
        {
		test( lang, 35, 1, propNames, propVals );
        }

	static final String baseUris[] =
		{
		"http://foo.com/Hello",
		"http://foo.com/Hello",
		"http://daml.umbc.edu/ontologies/calendar-ont",
		"http://www.daml.org/2001/03/daml+oil-ex" 
        };
            
    ByteArrayOutputStream tmpOut;
    
	/**
	 * @param rwLang Use Writer for this lang
	 * @param seed  A seed for the random number generator
	 * @param variationMax Number of random variations
	 * @param wopName  Property names to set on Writer
	 * @param wopVal   Property values to set on Writer
	 */
	public void test(
		String rwLang,
		int seed,
		int variationMax,
		String[] wopName,
		Object[] wopVal)
		throws IOException {

		Model m1 = createMemModel();
		test = "testWriterAndReader lang=" + rwLang + " seed=" + seed;
		String filebase = "testing/regression/testWriterAndReader/";
		if (showProgress)
			System.out.println("Beginning " + test);
		Random random = new Random(seed);

        RDFReader rdfRdr = m1.getReader( rwLang );
		RDFWriter rdfWtr = m1.getWriter( rwLang );

		setWriterOptionsAndHandlers( wopName, wopVal, rdfRdr, rdfWtr );
		for (int variationIndex = 0; variationIndex < variationMax; variationIndex++) 
			testVariation( filebase, random, rdfRdr, rdfWtr );
		if (showProgress)
			System.out.println("End of " + test);
	}
    
    /**
     	@param wopName
     	@param wopVal
     	@param rdfRdr
     	@param rdfWtr
    */
    private void setWriterOptionsAndHandlers( String[] wopName, Object[] wopVal, RDFReader rdfRdr, RDFWriter rdfWtr )
        {
        rdfRdr.setErrorHandler( this );
        rdfWtr.setErrorHandler( this );
		if (wopName != null)
			for (int i = 0; i < wopName.length; i++)
				rdfWtr.setProperty( wopName[i], wopVal[i] );
        }
    
    /**
     	@param filebase
     	@param random
     	@param rdfRdr
     	@param rdfWtr
     	@throws FileNotFoundException
     	@throws IOException
    */
    private void testVariation( String filebase, Random random, RDFReader rdfRdr, RDFWriter rdfWtr ) 
        throws FileNotFoundException, IOException
        {
        Model m1 = createMemModel();
        Model m2;
        String fileName = "t" + (fileNumber * 1000) + ".rdf";
        String baseUriRead;
        if (fileNumber < baseUris.length)
        	baseUriRead = baseUris[fileNumber];
        else
        	baseUriRead = "http://foo.com/Hello";
        //ANDROID: changed to use classloader
        InputStream rdr = TestHelper.openResource( filebase + fileName );
//        InputStream rdr = new FileInputStream( filebase + fileName );
        m1.read(rdr, baseUriRead);
        rdr.close();
        for (int j = 0; j < repetitionsJ; j++) {

            String baseUriWrite =
        		j % 2 == 0 ? baseUriRead : "http://bar.com/irrelevant";
        	int cn = (int) m1.size();
        	if ((j % 2) == 0 && j > 0)
        		prune(m1, random, 1 + cn / 10);
        	if ((j % 2) == 0 && j > 0)
        		expand(m1, random, 1 + cn / 10);
            
            tmpOut = new ByteArrayOutputStream() ;
            rdfWtr.write(m1, tmpOut, baseUriWrite);
            tmpOut.flush() ;
            tmpOut.close() ;
        	m2 = createMemModel();
        	//empty(m2);
            
            InputStream in = new ByteArrayInputStream( tmpOut.toByteArray() ) ;
            //ANDROID: use a reader to prevent unrecognized tokens error in sax parser
            rdfRdr.read(m2, new BufferedReader(new InputStreamReader(in)), baseUriWrite);
//        	rdfRdr.read(m2, in, baseUriWrite);
        	in.close();
        	Model s1 = m1;
        	Model s2 = m2;
        	/*
        	System.err.println("m1:");
        	m1.write(System.err,"N-TRIPLE");
        	System.err.println("m2:");
        	
        	m2.write(System.err,"N-TRIPLE");
        	System.err.println("=");
        	*/
//				assertTrue(
//                        "Comparison of file written out, and file read in.",
//                        s1.isIsomorphicWith(s2));
            assertIsoModels( "Comparison of file written out, and file read in.", s1, s2 );
            // Free resources explicitily.
            tmpOut.reset() ;
            tmpOut = null ;
        }
        if (showProgress) {
        	System.out.print("+");
        	System.out.flush();
        }
        }
    
  static boolean linuxFileDeleteErrorFlag = false;
  
	/**Deletes count edges from m chosen by random.
	 * @param count The number of statements to delete.
	 * @param m A model with more than count statements.
	 */
	private void prune(Model m, Random random, int count)  {
		//    System.out.println("Pruning from " + (int)m.size() + " by " + cnt );
		Statement toRemove[] = new Statement[count];
		int sz = (int) m.size();
		StmtIterator ss = m.listStatements();
		try {
			for (int i = 0; i < count; i++)
				toRemove[i] = ss.nextStatement();
			while (ss.hasNext()) {
				int ix = random.nextInt(sz);
				if (ix < count)
					toRemove[ix] = ss.nextStatement();
			}
		} finally {
			ss.close();
		}
		for (int i = 0; i < count; i++)
			m.remove( toRemove[i] );
		//    System.out.println("Reduced to " + (int)m.size()  );
	}
    
	/**
	 *  Adds count edges to m chosen by random.
	 *
	 * @param count The number of statements to add.
	 * @param m A model with more than cnt statements.
	 */
	private void expand(Model m, Random random, int count)  {
		// System.out.println("Expanding from " + (int)m.size() + " by " + cnt );
		Resource subject[] = new Resource[count];
		Property predicate[] = new Property[count];
		RDFNode object[] = new RDFNode[count];
		int sz = (int) m.size();
		StmtIterator ss = m.listStatements();
		try {
			for (int i = 0; i < count; i++) {
				Statement s = ss.nextStatement();
				subject[i] = s.getSubject();
				predicate[i] = s.getPredicate();
				object[i] = s.getObject();
			}
			while (ss.hasNext()) {
				Statement s = ss.nextStatement();
				Resource subj = s.getSubject();
				RDFNode obj = s.getObject();
				int ix = random.nextInt(sz);
				if (ix < count)
					subject[ix] = subj;
				ix = random.nextInt(sz);
				if (ix < count)
					object[ix] = subj;
				ix = random.nextInt(sz);
				if (ix < count)
					predicate[ix] = s.getPredicate();
				ix = random.nextInt(sz);
				if (ix < count)
					object[ix] = obj;
				if (obj instanceof Resource) {
					ix = random.nextInt(sz);
					if (ix < count)
						subject[ix] = (Resource) obj;
				}
			}
		} finally {
			ss.close();
		}
		for (int i = 0; i < count; i++)
			m.add(subject[i], predicate[i], object[i]);
		//   System.out.println("Expanded to " + (int)m.size()  );
	}

	/** report a warning
	 * @param e an exception representing the error
	 */
	public void warning(Exception e) {
//		logger.warn( toString() + " " + e.getMessage(), e );
        System.out.println(new String(tmpOut.toString()));
        
		throw new JenaException( e );
	}
    
	public void error(Exception e) {
		fail(e.getMessage());
	}

	public void fatalError(Exception e) {
		error(e);
		throw new JenaException(e);
	}

}

/*
 *  (c)   Copyright 2001,2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *    All rights reserved.
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
 *
 * $Id: testWriterAndReader.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $
 */