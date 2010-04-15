/*
    (c) Copyright 2001, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.
    [See end of file]
    $Id: TestPackage.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $
*/
package com.hp.hpl.jena.xmloutput;

// Imports
///////////////
import java.io.StringWriter;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.BadURIException;

/**
 * JUnit regression tests for output
 *
 * @author Jeremy Carroll
 * @version CVS info: $Id: TestPackage.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $,
 */
public class TestPackage extends TestCase{

    /**
     * Answer a suite of all the tests defined here
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest( TestMacEncodings.suite() );
        // add all the tests defined in this class to the suite
        /* */
        suite.addTestSuite( PrettyWriterTest.class );
        suite.addTest(new testWriterInterface("testInterface", null)); 
        /* */
        suite.addTest(new testWriterInterface("testNoWriter", null)); 
        /* */
        suite.addTest(new testWriterInterface("testAnotherWriter", null));
        /* */
        if (false) suite.addTest( BigAbbrevTestPackage.suite() ); // TODO may be obsolete. Ask Jeremy.
        suite.addTest( testWriterAndReader.suiteXML() );
        suite.addTest( testWriterAndReader.suiteXML_ABBREV() );
        suite.addTest( testWriterAndReader.suiteN_TRIPLE() );
        suite.addTestSuite( TestURIExceptions.class );
        suite.addTestSuite( TestEntityOutput.class );
        suite.addTestSuite( TestLiteralEncoding.class );
        suite.addTestSuite( TestWriterFeatures.class ) ;
        return suite;
    }
    
    /**
         Added as a place to put the test(s) which ensure that thrown URI exceptions
         carry the bad URI with them. I (Chris) would embed them in the other tests,
         but I can't work out how to do so ...
        @author kers
    */
    public static class TestURIExceptions extends TestCase
        {
        public TestURIExceptions( String name )
            { super( name ); }
        
        public void testBadURIExceptionContainsBadURIInMessage()
            {
            String badURI = "http:";            
            Model m = ModelFactory.createDefaultModel();
            m.add( m.createResource( badURI ), m.createProperty( "eg:B C" ), m.createResource( "eg:C D" ) );
            try { m.write( new StringWriter() ); fail( "should detect bad URI " + badURI ); } 
            catch (BadURIException e) { assertTrue( "message must contain failing URI", e.getMessage().indexOf( badURI ) > 0 ); }
            }
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
 * $Id: TestPackage.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $
 */
