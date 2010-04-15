
/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * See end of file.
 */
package com.hp.hpl.jena.rdf.arp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestSuite;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.ARPEventHandler;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.NTriple;
import com.hp.hpl.jena.rdf.arp.impl.ARPResource;
import com.hp.hpl.jena.rdf.arp.impl.ARPSaxErrorHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.shared.wg.TestInputStreamFactory;

/**
 * A version of the test suite which uses the
 * ARP internal N-triple writer, and not the
 * Jena N-triple writer.
 * @author Jeremy Carroll
 *
 * 
 */
class NTripleTestSuite extends WGTestSuite {
	NTripleTestSuite(TestInputStreamFactory fact, String name, boolean b) {
		super(fact, name, b);
	}

	static TestSuite suite(IRI testDir, String d, String nm) {
		return new NTripleTestSuite(
			new TestInputStreamFactory(testDir, d),
			nm,
			true);
	}

	static TestSuite suite(IRI testDir, IRI d, String nm) {
		return new NTripleTestSuite(
			new TestInputStreamFactory(testDir, d),
			nm,
			true);
	}

	static class SimulatedException extends RuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = -4804213791508445759L;
	}
	static class TestHandler
		extends ARPSaxErrorHandler
		implements ARPEventHandler, org.xml.sax.ErrorHandler {
		TestHandler(RDFErrorHandler eh) {
			this(eh, 0);
		}
		TestHandler(RDFErrorHandler eh, int cnt) {
			super(eh);
			countDown = cnt;
			xCountDown = cnt;
		}
		final int xCountDown;
		Set<AResource> anon = new HashSet<AResource>();
		Set<AResource> oldAnon = new HashSet<AResource>();
		int state = 1; // 1 begin, 2 in RDF, 3 after RDF, 4 at end-of-file.
		int countDown;
		public void statement(AResource subj, AResource pred, AResource obj) {
			Assert.assertEquals(state, 2);
			seeing(subj);
			seeing(obj);
			if (--countDown == 0)
				throw new SimulatedException();
		}

		/**
		 * @param subj
		 */
		private void seeing(AResource subj) {
			if (subj.isAnonymous())
				anon.add(subj);
			Assert.assertFalse("bnode reuse?", oldAnon.contains(subj));
		}
		/**
		* @param subj
		*/
		private void seen(AResource subj) {
			if (!anon.contains(subj)) {
                if (ARPResource.DEBUG) {
                    ((RuntimeException)subj.getUserData()).printStackTrace();
                }
                Assert.assertFalse(
                        "end-scope called twice for a bnode: "
                            + subj.getAnonymousID(),
                        oldAnon.contains(subj));
				Assert.assertTrue(
					"end-scope for a bnode that had not been used "
						+ subj.getAnonymousID(),
					anon.contains(subj));
            }
			anon.remove(subj);
			oldAnon.add(subj);
		}

		public void statement(AResource subj, AResource pred, ALiteral lit) {
			Assert.assertEquals("no start RDF seen", state, 2);
			seeing(subj);
			if (--countDown == 0)
				throw new SimulatedException();
		}

		public void endBNodeScope(AResource bnode) {
			Assert.assertTrue(bnode.isAnonymous());
			switch (state) {
				case 1 :
					Assert.fail("Missing startRDF"); return ;
				case 2 :
					Assert.assertFalse(bnode.hasNodeID());
					seen(bnode);
					break;
				case 3 :
				case 4 :
					Assert.assertTrue(bnode.hasNodeID());
					seen(bnode);
					state = 4;
					break;
				default :
					Assert.fail("impossible - test logic error");
			}

		}

		public void startRDF() {
			switch (state) {
				case 2 :
				case 4 :
					Assert.fail("Bad state for startRDF " + state);
			}
			state = 2;
		}

		public void endRDF() {
			Assert.assertEquals(state, 2);
			state = 3;
		}

		public void startPrefixMapping(String prefix, String uri) {

		}

		public void endPrefixMapping(String prefix) {

		}

		/**
		 * 
		 */
		public void atEndOfFile() {
			if (!anon.isEmpty()) {
				Iterator<AResource> it = anon.iterator();
				while (it.hasNext()) {
                    AResource a =
                    it.next();
					System.err.print(a.getAnonymousID() + ", ");
                    if (ARPResource.DEBUG) {
                        RuntimeException rte = (RuntimeException)a.getUserData();
//                        throw rte;
                        rte.printStackTrace();
                    }
                }
			}
			Assert.assertTrue("("+xCountDown+") some bnode still in scope ", //hasErrors||
			anon.isEmpty());
			switch (state) {
				case 1 :
					Assert.fail("end-of-file before anything"); return ;
				case 2 :
					Assert.fail("did not see endRDF"); return ;
				case 3 :
				case 4 :
					break;
				default :
					Assert.fail("impossible logic error in test");
			}
		}
		boolean hasErrors = false;

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
		 */
		@Override
        public void error(SAXParseException exception) throws SAXException {
			hasErrors = true;
			super.error(exception);

		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		@Override
        public void fatalError(SAXParseException exception)
			throws SAXException {
			hasErrors = true;
			super.fatalError(exception);

		}
		/**
		 * 
		 */
		public int getCount() {
			return -countDown;
		}
		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.rdf.arp.ExtendedHandler#discardNodesWithNodeID()
		 */
		public boolean discardNodesWithNodeID() {
			return false;
		}

	}

	@Override
    Model loadRDF(InFactoryX in, RDFErrorHandler eh, String base)
		throws IOException {
		return loadRDFx(in, eh, base, true, 0);
	}
	static Model loadRDFx(
		InFactoryX in,
		RDFErrorHandler eh,
		String base,
		boolean wantModel,
		int cnt)
		throws IOException {
		InputStream oldIn = System.in;
		InputStream ntIn = null;
		File ntriples = null;

		PrintStream out;
		TestHandler th;
		if (wantModel) {
			ntriples = File.createTempFile("arp", ".nt");
			out = new PrintStream(new FileOutputStream(ntriples));
			th = new TestHandler(eh);
		} else {
			out = new PrintStream(new OutputStream() {

				@Override
                public void write(int b) throws IOException {
				}
			});
			th = new TestHandler(eh, cnt);
		}
		PrintStream oldOut = System.out;
		try {
			System.setIn(in.open());
			System.setOut(out);
			try {
				NTriple.mainEh(new String[] { "-b", base, "-s" }, th, th);
			} catch (SimulatedException e) {
				if (wantModel)
					throw e;
			}
			out.close();
			th.atEndOfFile();

			if (cnt == 0) {
				// retry with sudden death
				for (int i = th.getCount(); i >= 1; i--)
					loadRDFx(in, TestScope.suppress, base, false, i);
			}
			if (wantModel) {
				ntIn = new FileInputStream(ntriples);
				return loadNT(ntIn, base);
			} 
		    return null;
		} finally {
			System.in.close();
			System.setIn(oldIn);
			System.setOut(oldOut);
			if (ntIn != null)
				ntIn.close();
			if (ntriples != null)
				ntriples.delete();
		}
	}

}
/*
 *  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
