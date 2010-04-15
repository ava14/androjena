/*
 *  (c)     Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: MoreTests.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdf.arp;

import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author jjc
 *  
 */
public class MoreTests extends TestCase implements RDFErrorHandler,
		ARPErrorNumbers {
	static private Logger logger = LoggerFactory.getLogger(MoreTests.class);

	static public Test suite() {
		TestSuite suite = new TestSuite("ARP Plus");

		suite.addTest(TestErrorMsg.suite());

        suite.addTest(TestPropEltErrorMsg.suite());
		suite.addTest(TestScope.suite());
		suite.addTest(ExceptionTests.suite());
        
        suite.addTest(new MoreDOM2RDFTest("testDOMwithARP"));
	
        // Better add tests automatcally:
        suite.addTestSuite(MoreTests.class) ;
//		suite.addTest(new MoreTests("testIcu"));
//		suite.addTest(new MoreTests("testLatin1"));
//		suite.addTest(new MoreTests("testIcu2"));
//		suite.addTest(new MoreTests("testEncodingMismatch1"));
//		suite.addTest(new MoreTests("testEncodingMismatch2"));
//		suite.addTest(new MoreTests("testEncodingMismatch3"));
//		suite.addTest(new MoreTests("testNullBaseParamOK"));
//		suite.addTest(new MoreTests("testNullBaseParamError"));
//		suite.addTest(new MoreTests("testEmptyBaseParamOK"));
//		suite.addTest(new MoreTests("testEmptyBaseParamError"));
//      suite.addTest(new MoreTests("testBadBaseParamOK"));
//      suite.addTest(new MoreTests("testBadBaseParamError"));
//      suite.addTest(new MoreTests("testRelativeBaseParamOK"));
//      suite.addTest(new MoreTests("testRelativeBaseParamError"));
//      suite.addTest(new MoreTests("testBaseTruncation"));
//		suite.addTest(new MoreTests("testWineDefaultNS"));
//		suite.addTest(new MoreTests("testInterrupt"));
//      suite.addTest(new MoreTests("testDanBriXMLBase"));
//		suite.addTest(new MoreTests("testToString"));
		
//for (int i=0; i< 20; i++ ) {
		//suite.addTest(new MoreTests("testTokenGarbage1"));
		//suite.addTest(new MoreTests("testTokenGarbage2"));
//		suite.addTest(new MoreTests("testTokenGarbage1"));
//		suite.addTest(new MoreTests("testTokenGarbage2"));
//		suite.addTest(new MoreTests("testTokenGarbage1"));
//		suite.addTest(new MoreTests("testTokenGarbage2"));
//}
		return suite;
	}

	public MoreTests(String s) {
		super(s);
	}

	protected Model createMemModel() {
		return ModelFactory.createDefaultModel();
	}

	@Override
    public void setUp() {
		// ensure the ont doc manager is in a consistent state
		OntDocumentManager.getInstance().reset(true);
	}

	public void testWineDefaultNS() throws IOException {
		testWineNS(createMemModel());
		testWineNS(ModelFactory.createOntologyModel());
	}

	private void testWineNS(Model m) throws FileNotFoundException, IOException {
		//ANDROID: changed to use classloader
		InputStream in = TestHelper.openResource("testing/arp/xmlns/wine.rdf");
//		InputStream in = new FileInputStream("testing/arp/xmlns/wine.rdf");
		m.read(in, "");
		in.close();
		assertEquals("http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine#", m
				.getNsPrefixURI(""));
	}

	public void testLatin1() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader and reader
		Reader r = new InputStreamReader(TestHelper.openResource(
		"testing/arp/i18n/latin1.rdf"));
//		InputStream r = new FileInputStream(
//				"testing/arp/i18n/latin1.rdf");
		
		rdr.setErrorHandler(this);
		expected = new int[] { WARN_NONCANONICAL_IANA_NAME };
		rdr.read(m, r, "http://example.org/");
		checkExpected();
	}
	public void testARPMacRoman() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader and reader
		Reader r = new InputStreamReader(TestHelper.openResource(
		"testing/arp/i18n/macroman.rdf"));
//		InputStream r = new FileInputStream(
//				"testing/arp/i18n/macroman.rdf");
		
		rdr.setErrorHandler(this);
		expected = new int[] { WARN_UNSUPPORTED_ENCODING, WARN_NON_IANA_ENCODING };
		expected[Charset.isSupported("MacRoman")?0:1]=0;
//		 Only one of the warnings is expected, which depends on Java version
		
		rdr.read(m, r, "http://example.org/");
		checkExpected();
	}
	public void testARPMacArabic() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader and use a reader
		Reader r = new InputStreamReader(TestHelper.openResource(
		"testing/arp/i18n/arabic-macarabic.rdf"));
//		InputStream r = new FileInputStream(
//				"testing/arp/i18n/arabic-macarabic.rdf");
		
		rdr.setErrorHandler(this);
		expected = new int[] { WARN_UNSUPPORTED_ENCODING, WARN_NON_IANA_ENCODING };
		expected[Charset.isSupported("MacArabic")?0:1]=0;
//		 Only one of the warnings is expected, which depends on Java version
		rdr.read(m, r, "http://example.org/");
		checkExpected();
	}
	

	public void testEncodingMismatch1() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
		InputStreamReader r = new InputStreamReader(TestHelper.openResource("testing/wg/rdfms-syntax-incomplete/test001.rdf"));
//		FileReader r = new FileReader(
//				"testing/wg/rdfms-syntax-incomplete/test001.rdf");
		if (r.getEncoding().startsWith("UTF")) {
			System.err
					.println("WARNING: Encoding mismatch tests not executed on platform with default UTF encoding.");
			return;
		}
		rdr.setErrorHandler(this);
		expected = new int[] { WARN_ENCODING_MISMATCH };
		rdr.read(m, r, "http://example.org/");
		//System.err.println(m.size() + " triples read.");
		checkExpected();

	}

	public void testIcu() throws IOException {
//	  "\u0b87\u0ba8\u0bcd\u0ba4\u0bbf\u0baf\u0bbe"
//	    Normalizer.  isNormalized(
//	            "\u0bcd\u0ba4\u0bbf\u0baf\u0bbe"
//	            ,Normalizer.NFC,0);
	    
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
		InputStream r = TestHelper.openResource(
		"testing/arp/i18n/icubug.rdf");
//		FileInputStream r = new FileInputStream(
//				"testing/arp/i18n/icubug.rdf");
	    rdr.setErrorHandler(this);
		expected = new int[] { WARN_STRING_COMPOSING_CHAR  };
		rdr.read(m, r, "http://example.org/");
		r.close();
		checkExpected();
	
	}
	public void testIcu2() throws IOException {
//		  "\u0b87\u0ba8\u0bcd\u0ba4\u0bbf\u0baf\u0bbe"
//		    Normalizer.  isNormalized(
//		            "\u0bcd\u0ba4\u0bbf\u0baf\u0bbe"
//		            ,Normalizer.NFC,0);
		    
			Model m = createMemModel();
			RDFReader rdr = m.getReader();
			//ANDROID: changed to use classloader
			InputStream r = TestHelper.openResource(
			"testing/arp/i18n/icubugtwo.rdf");
//			FileInputStream r = new FileInputStream(
//					"testing/arp/i18n/icubugtwo.rdf");
			rdr.setErrorHandler(this);
			expected = new int[] { WARN_STRING_NOT_NORMAL_FORM_C };
			rdr.read(m, r, "http://example.org/");
			r.close();
			checkExpected();
		
		}
	static class ToStringStatementHandler implements StatementHandler {
		String obj;

		String subj;

		public void statement(AResource sub, AResource pred, ALiteral lit) {
			// System.out.println("(" + sub + ", " + pred + ", " + lit + ")");
			subj = sub.toString();
		}

		public void statement(AResource sub, AResource pred, AResource ob) {
			//  System.out.println("(" + sub + ", " + pred + ", " + ob + ")");
			obj = ob.toString();
		}

	}

	public void testToString() throws IOException, SAXException {

		String testcase = "<rdf:RDF xmlns:music=\"http://www.kanzaki.com/ns/music#\" "
				+ "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"> "
				+ "<rdf:Description> "
				+ "  <music:performs rdf:nodeID=\"p1\"/> "
				+ "</rdf:Description> "
				+ "<rdf:Description rdf:nodeID=\"p1\"> "
				+ "  <music:opus>op.26</music:opus> "
				+ "</rdf:Description> "
				+ "</rdf:RDF>";

		ARP parser = new ARP();
		ToStringStatementHandler tssh = new ToStringStatementHandler();
		parser.getHandlers().setStatementHandler(tssh);
		parser.load(new StringReader(testcase), "http://www.example.com");
		assertEquals(tssh.subj, tssh.obj);
	}

	public void testEncodingMismatch2() throws IOException {
		//ANDROID: changed to use classloader
		InputStreamReader r = new InputStreamReader(TestHelper.openResource("testing/wg/rdf-charmod-literals/test001.rdf"));
//		FileReader r = new FileReader(
//				"testing/wg/rdf-charmod-literals/test001.rdf");

		subTestEncodingMismatch2(r);
	}


	public void testEncodingMismatch3() throws IOException {
		//ANDROID: changed to use classloader
		InputStream fin = TestHelper.openResource(
				"testing/wg/rdf-charmod-literals/test001.rdf"
				);
//		FileInputStream fin = new FileInputStream(
//				"testing/wg/rdf-charmod-literals/test001.rdf"
//				);
		InputStreamReader r = null;
		try {
		   r = new InputStreamReader(
				fin,
				"MS950"
				);
		}
		catch (java.io.UnsupportedEncodingException e) {
			System.err
			.println("WARNING: Encoding mismatch3 test not executed on platform without MS950 encoding.");

			return;
		}
		
		
		subTestEncodingMismatch2(r);
	}

	private void subTestEncodingMismatch2(InputStreamReader r) {
		if (r.getEncoding().startsWith("UTF")) {
			// see above for warning message.
			return;
		}
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		
		rdr.setErrorHandler(this);
		expected = new int[] { WARN_ENCODING_MISMATCH, ERR_ENCODING_MISMATCH };
		rdr.read(m, r, "http://example.org/");

		checkExpected();
	}

	public void testNullBaseParamOK() throws IOException {
		Model m = createMemModel();
		Model m1 = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
		InputStream fin = TestHelper.openResource(
		"testing/wg/rdfms-identity-anon-resources/test001.rdf");
//		FileInputStream fin = new FileInputStream(
//				"testing/wg/rdfms-identity-anon-resources/test001.rdf");

		rdr.setErrorHandler(this);
		expected = new int[] {};
		rdr.read(m, fin, "http://example.org/");
		fin.close();
		//ANDROID: changed to use classloader
		fin = TestHelper.openResource(
				"testing/wg/rdfms-identity-anon-resources/test001.rdf");
		rdr.read(m1, fin, null);
		fin.close();
		assertTrue("Base URI should have no effect.", m.isIsomorphicWith(m1));
		checkExpected();
	}
    public void testDanBriXMLBase() throws IOException {
        Model m = createMemModel();
        Model m1 = createMemModel();
      //ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/arp/xmlbase/danbri.rdf");
//        FileInputStream fin = new FileInputStream(
//        "testing/arp/xmlbase/danbri.rdf");

        m.read(fin,"http://wrong.example.org/");
        fin.close();
      //ANDROID: changed to use classloader
        fin = TestHelper.openResource(
        "testing/arp/xmlbase/danbri.nt");
//        fin = new FileInputStream(
//        "testing/arp/xmlbase/danbri.nt");

        m1.read(fin,"http://wrong.example.org/","N-TRIPLE");
        fin.close();
        assertTrue("Dan Bri nested XML Base.", m.isIsomorphicWith(m1));
    }

	public void testNullBaseParamError() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
//		FileInputStream fin = new FileInputStream(
//				"testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
		rdr.setErrorHandler(this);
		expected = new int[] { ERR_RESOLVING_URI_AGAINST_NULL_BASE, WARN_RELATIVE_URI};
		rdr.read(m, fin, null);
		fin.close();
		checkExpected();
	}

	public void testEmptyBaseParamOK() throws IOException {
		Model m = createMemModel();
		Model m1 = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/wg/rdfms-identity-anon-resources/test001.rdf");
//		FileInputStream fin = new FileInputStream(
//				"testing/wg/rdfms-identity-anon-resources/test001.rdf");

		rdr.setErrorHandler(this);
		expected = new int[] {};
		rdr.read(m, fin, "http://example.org/");
		fin.close();
		//ANDROID: changed to use classloader
        fin = TestHelper.openResource(
        "testing/wg/rdfms-identity-anon-resources/test001.rdf");
//		fin = new FileInputStream(
//				"testing/wg/rdfms-identity-anon-resources/test001.rdf");
		rdr.read(m1, fin, "");
		fin.close();
		assertTrue("Empty base URI should have no effect.[" + m1.toString()
				+ "]", m.isIsomorphicWith(m1));
		checkExpected();
	}

	public void testEmptyBaseParamError() throws IOException {
		Model m = createMemModel();
		RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
//		FileInputStream fin = new FileInputStream(
//				"testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
		rdr.setErrorHandler(this);
		expected = new int[] { WARN_RESOLVING_URI_AGAINST_EMPTY_BASE};
		rdr.read(m, fin, "");
		fin.close();
		Model m1 = createMemModel();
		m1.createResource("#foo").addProperty(RDF.value, "abc");
		assertTrue("Empty base URI should produce relative URI.["
				+ m.toString() + "]", m.isIsomorphicWith(m1));
		checkExpected();
	}

    public void testBadBaseParamError() throws IOException {
        Model m = createMemModel();
        RDFReader rdr = m.getReader();
        //ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
//        FileInputStream fin = new FileInputStream(
//                "testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
        rdr.setErrorHandler(this);
        expected = new int[] { WARN_MALFORMED_URI, 
                WARN_MALFORMED_URI, 
//        WARN_RELATIVE_URI, ERR_RESOLVING_AGAINST_MALFORMED_BASE};
               ERR_RESOLVING_AGAINST_MALFORMED_BASE};
        rdr.read(m, fin, "http://jjc^3.org/demo.mp3");
        fin.close();
        Model m1 = createMemModel();
        assertTrue("Bad base URI should produce no URIs in model.["
                + m.toString() + "]", m.isIsomorphicWith(m1));
        checkExpected();
    }
    
    public void testBadBaseParamOK() throws IOException {
        Model m = createMemModel();
        Model m1 = createMemModel();
        RDFReader rdr = m.getReader();
      //ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/wg/rdfms-identity-anon-resources/test001.rdf");
//        FileInputStream fin = new FileInputStream(
//                "testing/wg/rdfms-identity-anon-resources/test001.rdf");

        rdr.setErrorHandler(this);
        expected = new int[] { WARN_MALFORMED_URI };
        rdr.read(m, fin, "http://jjc^3.org/demo.mp3");
        fin.close();
      //ANDROID: changed to use classloader
        fin = TestHelper.openResource(
                "testing/wg/rdfms-identity-anon-resources/test001.rdf");
        rdr.read(m1, fin, "");
        fin.close();
        assertTrue("Bad base URI should have no effect on model.[" + m1.toString()
                + "]", m.isIsomorphicWith(m1));
        checkExpected();
    }
    public void testRelativeBaseParamError() throws IOException {
        Model m = createMemModel();
        RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
//        FileInputStream fin = new FileInputStream(
//                "testing/wg/rdfms-difference-between-ID-and-about/test1.rdf");
        rdr.setErrorHandler(this);
        expected = new int[] { WARN_RELATIVE_URI, WARN_RELATIVE_URI,  ERR_RESOLVING_AGAINST_RELATIVE_BASE, };
        rdr.setProperty("ERR_RESOLVING_AGAINST_RELATIVE_BASE","EM_WARNING");
        rdr.read(m, fin, "foo/");
        fin.close();
        Model m1 = createMemModel();
        m1.createResource("foo/#foo").addProperty(RDF.value, "abc");
        assertTrue("Relative base URI should produce relative URIs in model (when error suppressed).["
                + m.toString() + "]", m.isIsomorphicWith(m1));
        checkExpected();
    }
    
    public void testRelativeBaseParamOK() throws IOException {
        Model m = createMemModel();
        Model m1 = createMemModel();
        RDFReader rdr = m.getReader();
		//ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
        "testing/wg/rdfms-identity-anon-resources/test001.rdf");
//        FileInputStream fin = new FileInputStream(
//                "testing/wg/rdfms-identity-anon-resources/test001.rdf");

        rdr.setErrorHandler(this);
        expected = new int[] { WARN_RELATIVE_URI };
        rdr.read(m, fin, "foo/");
        fin.close();
        fin = TestHelper.openResource(
                "testing/wg/rdfms-identity-anon-resources/test001.rdf");
        rdr.read(m1, fin, "");
        fin.close();
        assertTrue("Bad base URI should have no effect on model.[" + m1.toString()
                + "]", m.isIsomorphicWith(m1));
        checkExpected();
    }

    public void testBaseTruncation() throws IOException {
        Model m = createMemModel();
        Model m1 = createMemModel();
        RDFReader rdr = m.getReader();
      //ANDROID: changed to use classloader
        InputStream fin = TestHelper.openResource(
                "testing/wg/rdfms-identity-anon-resources/test001.rdf");

        rdr.setErrorHandler(this);
        expected = new int[] { WARN_MALFORMED_URI, WARN_RELATIVE_URI };
        rdr.read(m, fin, "ht#tp://jjc3.org/demo.mp3#frag");
        fin.close();
      //ANDROID: changed to use classloader
        fin = TestHelper.openResource(
                "testing/wg/rdfms-identity-anon-resources/test001.rdf");
        rdr.read(m1, fin, "");
        fin.close();
        assertTrue("Bad base URI should have no effect.[" + m1.toString()
                + "]", m.isIsomorphicWith(m1));
        checkExpected();
    }
	public void testInterrupt() throws SAXException, IOException {
		ARP a = new ARP();
		InputStream in;
//		long start = System.currentTimeMillis();
		//ANDROID: changed to use classloader
		in = TestHelper.openResource("testing/wg/miscellaneous/consistent001.rdf");
//		in = new FileInputStream("testing/wg/miscellaneous/consistent001.rdf");
		a.getHandlers().setStatementHandler(new StatementHandler() {
			int countDown = 10;

			public void statement(AResource subj, AResource pred, AResource obj) {
				if (countDown-- == 0)
					Thread.currentThread().interrupt();

			}

			public void statement(AResource subj, AResource pred, ALiteral lit) {

			}
		});
        a.getHandlers().setErrorHandler(new ErrorHandler(){
            public void error(SAXParseException exception) throws SAXException {
                throw new RuntimeException("Unexpected error", exception);
            }
            public void fatalError(SAXParseException exception) throws SAXException {
              throw exception;  
            }
            public void warning(SAXParseException exception) throws SAXException {
                throw new RuntimeException("Unexpected warning", exception);
            }});
		try {
			a.load(in);
			fail("Thread was not interrupted.");
		} catch (InterruptedIOException e) {
		} catch (SAXParseException e) {
		} finally {
			in.close();
		}
		// System.err.println("Finished "+Thread.interrupted());

	}

	static String RDF_TEXT = "<?xml version=\"1.0\" ?>\n" +
    "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
    " <rdf:Description>\n" +
    "  <rdf:value rdf:resource=\"http://example/some random text\"/>\n" +
    "  <rdf:value rdf:resource=\"relative random text\"/>\n" +
    " </rdf:Description>\n" +
    "</rdf:RDF>\n";
	
	public void testIRIRules_1()
	{
        Model model = ModelFactory.createDefaultModel() ;
        model.read(new StringReader(RDF_TEXT), "http://example/") ;
	}
	
	public void testIRIRules_2()
	{
        Model model = ModelFactory.createDefaultModel() ;
	    IRIFactory f = ARPOptions.getIRIFactoryGlobal() ;
	    try {
	        ARPOptions.setIRIFactoryGlobal(IRIFactory.iriImplementation()) ;
	        RDFReader r =  model.getReader("RDF/XML") ;
            expected = new int[] { WARN_MALFORMED_URI , WARN_MALFORMED_URI };
	        r.setErrorHandler(this);
	        r.read(model, new StringReader(RDF_TEXT), "http://example/") ;
	    } finally { ARPOptions.setIRIFactoryGlobal(f) ; }
        checkExpected() ;
	}	        

    public void testIRIRules_2a()
    {
        Model model = ModelFactory.createDefaultModel() ;
        RDFReader r =  model.getReader("RDF/XML") ;
        r.setErrorHandler(this);
        expected = new int[] { };
        model.read(new StringReader(RDF_TEXT), "http://example/") ;
        checkExpected() ;
    }           
	
    public void testIRIRules_3()
    {
        Model model = ModelFactory.createDefaultModel() ;
        RDFReader r =  model.getReader("RDF/XML") ;
        r.setErrorHandler(this);
        expected = new int[] { WARN_MALFORMED_URI , WARN_MALFORMED_URI };
        r.setProperty("iri-rules", "strict") ;
        r.read(model, new StringReader(RDF_TEXT), "http://example/") ;
        checkExpected() ;
    }           

    public void testIRIRules_4()
    {
        Model model = ModelFactory.createDefaultModel() ;
        RDFReader r =  model.getReader("RDF/XML") ;
        r.setProperty("iri-rules", "strict") ;
        r.setProperty( "WARN_MALFORMED_URI", ARPErrorNumbers.EM_ERROR) ;
        r.setErrorHandler(this);
        expected = new int[] { WARN_MALFORMED_URI , WARN_MALFORMED_URI };   // Errors actually continue.
        r.read(model, new StringReader(RDF_TEXT), "http://example/") ;
        checkExpected() ;
    }
	
	private void checkExpected() {
		for (int i = 0; i < expected.length; i++)
			if (expected[i] != 0) {
				fail("Expected error: " + ParseException.errorCodeName(expected[i])
						+ " but it did not occur.");
			}
	}

	public void warning(Exception e) {
		error(0, e);
	}

	public void error(Exception e) {
		error(1, e);
	}

	public void fatalError(Exception e) {
		error(2, e);
	}

	private void error(int level, Exception e) {
		//System.err.println(e.getMessage());
		if (e instanceof ParseException) {
			int eCode = ((ParseException) e).getErrorNumber();
			onError(level, eCode);
		} else {
			fail("Not expecting an Exception: " + e.getMessage());
		}
	}

	private int expected[];

	private void println(String m) {
		logger.error(m);
	}

	void onError(int level, int num) {
		for (int i = 0; i < expected.length; i++)
			if (expected[i] == num) {
				expected[i] = 0;
				return;
			}
		String msg = "Parser reports unexpected "
				+ WGTestSuite.errorLevelName[level] + ": "
				+ ParseException.errorCodeName(num);
		println(msg);
		fail(msg);
	}

//	private void tokenGarbage(String file) {
//		try {
//			Token.COUNT = true;
//			Token.COUNTTEST = true;
//			Token.reinitHighTide();
//			NTriple.main(new String[] { "-t", file });
//			//System.err.println("["+Token.highTide+"]");
//			assertTrue("Too many tokens used: "+ Token.highTide,
//					Token.highTide<2000);
//		} finally {
//			Token.COUNT = false;
//			Token.COUNTTEST = false;
//		}
//	}
//
//	public void testTokenGarbage1() {
//		tokenGarbage("testing/ontology/owl/Wine/wine.owl");
//	}
//
//	public void testTokenGarbage2() {
//
//		tokenGarbage("testing/arp/gc/someWordNet.rdf");
//	}
}

/*
 * (c) Copyright 2000, 2001, 2002, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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