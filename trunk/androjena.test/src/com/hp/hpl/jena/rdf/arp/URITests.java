/*
 *  (c)     Copyright 2000-2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: URITests.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdf.arp;
import com.hp.hpl.jena.iri.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import java.net.*;
/**
 * @author jjc
 *
 */
public class URITests
	extends TestCase {
    // TODO: not for 2.3 relative/absolute tests
	static public Test suite() {
		TestSuite suite = new TestSuite("URIs");
		suite.addTest(new URITests("testNoDomain"));
		suite.addTest(new URITests("testLong"));
        suite.addTest(new URITests("testBadScheme"));
        suite.addTest(new URITests("testJustScheme"));
        
        // TODO: not for 2.3. are these tests right?
//        suite.addTest(new URITests("testBadHost"));
//        suite.addTest(new URITests("testBadPort"));
//      suite.addTest(new URITests("testBadUserHost"));
        suite.addTest(new URITests("testHostPortNoSlashWithFragment"));
        suite.addTest(new URITests("testHostNoSlashWithFragment"));

        suite.addTest(new URITests("testBadAuthority"));
        suite.addTest(new URITests("testTwoHashes"));
        suite.addTest(new URITests("testTwoHashes2"));
		return suite;
	}

	URITests(String s) {
		super(s);
	}

    static IRIFactory factory = IRIFactory.jenaImplementation();
//    static {
//        factory.useSpecificationRDF(false);
//    }
	public void testURI(String uri, boolean ok) {
		    IRI ref =
            factory.create(uri);
            if (ok && ref.hasViolation(false)) {
                Violation v = ref.violations(false).next();
                fail("<" + uri + "> is expected to be a URI, but: "+v.getLongMessage());
            }
			assertEquals("<" + uri + "> is"+(ok?" ":" not ")+"a URI", ok, !ref.hasViolation(false));
            
	}
	public void testNoDomain()  {
       testURI("app://calendar/event",true);
	}
	public void testLong()  {
	   testURI("http://46229EFFE16A9BD60B9F1BE88B2DB047ADDED785/demo.mp3",true);
	}
    
    public void testBadScheme()  {
           testURI("ht^tp://www.w3.org/demo.mp3",false);
        }

    public void testFragmentLooksLikeScheme()  {
           testURI("ht#tp://www.w3.org/demo.mp3",true);
        }
     
    public void testHostNoSlashWithFragment()  {
        testURI("http://www.w#3.org/demo.mp3",true);
     }
    public void testHostPortNoSlashWithFragment()  {
        testURI("http://www.w3.org:1#4/demo.mp3",true);
     }
	
    public void testBadHost()  {
        testURI("http://www.w+3.org/demo.mp3",false);
     }

    // TODO: not for 2.3. Is this test correct?
    public void testJustScheme()  {
        testURI("http:",false);
     }
    public void testBadPort()  {
        testURI("http://www.w3.org:1+4/demo.mp3",false);
     }
    public void testBadUserHost()  {
        testURI("http://jjc@www.w@3.org/demo.mp3",false);
     }
    public void testBadAuthority()  {
        testURI("http://jjc^3.org/demo.mp3",false);
     }
    public void testTwoHashes() {
        testURI("ht#tp://jjc3.org/demo.mp3#frag",false);
            
    }
    public void testTwoHashes2() {
        testURI("http://jjc#3.org/demo.mp3#frag",false);
            
    }
}

/*
    (c) Copyright 2000-2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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