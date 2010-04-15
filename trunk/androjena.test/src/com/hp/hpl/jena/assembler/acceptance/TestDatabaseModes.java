/*
 (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 All rights reserved - see end of file.
 $Id: TestDatabaseModes.java,v 1.1 2009/06/29 08:55:57 castagna Exp $
 */

package com.hp.hpl.jena.assembler.acceptance;

import junit.framework.Assert;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.db.test.TestConnection;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.shared.NotFoundException;

public class TestDatabaseModes extends AssemblerTestBase {
	public TestDatabaseModes(String name) {
		super(name);
	}

	// ANDROID: in android, the junit.extensions is not available, so TestSetup
	// can't be used.
	// I override setUp and tearDown methods directly on TestDatabaseModes
	// instead.

	@Override
	public void setUp() throws Exception {
		super.setUp();
		IDBConnection conn = TestConnection.makeAndCleanTestConnection();
		ModelRDB.createModel(conn, "square");
		ModelRDB.createModel(conn, "circle");
		ModelRDB.createModel(conn, "triangle");
		ModelRDB.createModel(conn, "hex");
		conn.close();
		IDBConnection x = ModelFactory.createSimpleRDBConnection();
		assertEquals(true, x.containsModel("square"));
		assertEquals(false, x.containsModel("line"));
		x.close();
	}

	public void testRDBModelOpenedWhenExists() {
		openWith("square", false, true);
		openWith("circle", true, true);
	}

	public void testRDBModelCreatedWhenMissing() {
		openWith("line", true, true);
		openWith("edge", true, false);
	}

	public void testRDBModelFailsIfExists() {
		try {
			openWith("triangle", true, false);
			Assert.fail("should trap existing model");
		} catch (AlreadyExistsException e) {
			Assert.assertEquals("triangle", e.getMessage());
		}
		try {
			openWith("hex", false, false);
			Assert.fail("should trap existing model");
		} catch (AlreadyExistsException e) {
			Assert.assertEquals("hex", e.getMessage());
		}
	}

	public void testRDBModelFailsIfMissing() {
		try {
			openWith("parabola", false, true);
			Assert.fail("should trap missing model");
		} catch (NotFoundException e) {
			Assert.assertEquals("parabola", e.getMessage());
		}
		try {
			openWith("curve", false, false);
			Assert.fail("should trap missing model");
		} catch (NotFoundException e) {
			Assert.assertEquals("curve", e.getMessage());
		}
	}

	private void openWith(String name, boolean mayCreate, boolean mayReuse) {
		Assembler.general.openModel(getRoot(name),
				new Mode(mayCreate, mayReuse)).close();
	}

	private Resource getRoot(String name) {
		return resourceInModel(getDescription(name));
	}

	private String getDescription(String modelName) {
		return ("x rdf:type ja:RDBModel; x ja:modelName 'spoo'; x ja:connection C"
				+ "; C ja:dbURLProperty 'jena.db.url'"
				+ "; C ja:dbUserProperty 'jena.db.user'"
				+ "; C ja:dbPasswordProperty 'jena.db.password'"
				+ "; C ja:dbTypeProperty 'jena.db.type'"
				+ "; C ja:dbClassProperty 'jena.db.driver'").replaceAll("spoo",
				modelName);
	}
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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