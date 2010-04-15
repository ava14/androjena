/*
   (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestScope.java,v 1.1 2009/07/04 16:41:34 andy_seaborne Exp $
*/
package com.hp.hpl.jena.rdf.arp;
import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.RDFErrorHandler;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class TestScope extends TestCase {
	public static Test suite() {
		TestSuite s = new TestSuite(TestScope.class);
		s.setName("ARP Scoping");
		return s;
	}
  public	TestScope(String nm){
		super(nm);
	}
	/*
	public void test01() throws Exception {

		check("testing/wg/rdfms-syntax-incomplete/test004.rdf");
	//	check("testing/arp/scope/test01.rdf");
	}
	public void test02() throws Exception {
		check("testing/arp/scope/test02.rdf");
	}
	public void test03() throws Exception {
		check("testing/arp/scope/test03.rdf");
	}
	

	public void test04() throws Exception {
		check("testing/arp/scope/test04.rdf");
	}
	
	public void test05() throws Exception {
		check("testing/arp/scope/test05.rdf");
	}
*/
    public void test06() throws Exception {
        check("testing/arp/scope/test06.rdf");
    }
	static RDFErrorHandler suppress = new RDFErrorHandler(){

		public void warning(Exception e) {
		}

		public void error(Exception e) {
		}

		public void fatalError(Exception e) {
		}
		
	};
	private void check(final String fn) throws IOException {
		
		NTripleTestSuite.loadRDFx(new InFactoryX(){

			public InputStream open() throws IOException {
				//ANDROID: changed to use classloader
				return TestHelper.openResource(fn);
//				return new FileInputStream(fn);
			}
		},suppress,"http://example.org/a",false,0);
	//	in.close();
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