/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Environment;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * com.hp.hpl.jena.brql.util.test.TestFileManager
 * 
 * @author Andy Seaborne
 * @version $Id: TestFileManager.java,v 1.1 2009/06/29 18:42:05 andy_seaborne
 *          Exp $
 */

public class TestFileManager extends TestCase {
	static Logger log = LoggerFactory.getLogger(TestFileManager.class);
	static final String testingDir = "testing/FileManager";
	static final String filename = "fmgr-test-file";
	static final String filenameNonExistent = "fmgr-test-file-1421";
	static final String fileModel = "foo.n3";
	static final String zipname = testingDir + "/fmgr-test.zip";

	public TestFileManager(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestFileManager.class);
	}

	public void testFileManagerFileLocator() {
		FileManager fileManager = new FileManager();
		// ANDROID: we have to create the file in sdcard
		try {
			InputStream is = TestHelper.openResource(
					testingDir + "/" + filename);
			if (is == null)
				throw new Exception(testingDir + "/" + filename + " not found");
			File root = Environment.getExternalStorageDirectory();
			File file = new File(root, filename);
			if (file.exists())
				file.delete();
			file.createNewFile();
			file.deleteOnExit();
			FileOutputStream os = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = is.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, len);
			}
			is.close();
			os.flush();
			os.close();
			fileManager.addLocatorFile();
			InputStream in = fileManager.open(file.getAbsolutePath());
			 assertNotNull(in);
			 closeInputStream(in);
		} catch (Exception ex) {
			fail("Cannot read or create " + testingDir + "/" + filename);
		}
		
		// fileManager.addLocatorFile();
		// InputStream in = fileManager.open(testingDir + "/" + filename);
		// assertNotNull(in);
		// closeInputStream(in);
	}

	public void testFileManagerFileLocatorWithDir() {
		FileManager fileManager = new FileManager();
		// ANDROID: we have to create the file in sdcard
		try {
			InputStream is = TestHelper.openResource(
					testingDir + "/" + filename);
			if (is == null)
				throw new Exception(testingDir + "/" + filename + " not found");
			File root = Environment.getExternalStorageDirectory();
			String dirname = "tempdir";
			File dir = new File(root, dirname);
			dir.mkdir();
			dir.deleteOnExit();
			File file = new File(dir, filename);
			if (file.exists())
				file.delete();
			file.createNewFile();
			file.deleteOnExit();
			FileOutputStream os = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = is.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, len);
			}
			is.close();
			os.flush();
			os.close();
			 fileManager.addLocatorFile(dir.getAbsolutePath());
			 InputStream in = fileManager.open(filename);
			 assertNotNull(in);
			 closeInputStream(in);
		} catch (Exception ex) {
			fail("Cannot read or create " + testingDir + "/" + filename);
		}
		// fileManager.addLocatorFile(testingDir);
		// InputStream in = fileManager.open(filename);
		// assertNotNull(in);
		// closeInputStream(in);
	}

	public void testFileManagerNoFile() {
		FileManager fileManager = new FileManager();
		fileManager.addLocatorFile();
		try {
			// Tests either way round - exception or a null return.
			InputStream in = fileManager.open(filenameNonExistent);
			closeInputStream(in);
			assertNull("Found non-existant file: " + filenameNonExistent, in);
		} catch (NotFoundException ex) {
		}
	}

	public void testFileManagerLocatorClassLoader() {
		FileManager fileManager = new FileManager();
		fileManager.addLocatorClassLoader(fileManager.getClass()
				.getClassLoader());
		// ANDROID: changed to an asset because in Android there are no class
		// files.
		InputStream in = fileManager.open("testing/abbreviated/collection.rdf");
		// InputStream in = fileManager.open("java/lang/String.class") ;
		assertNotNull(in);
		closeInputStream(in);
	}

	public void testFileManagerLocatorClassLoaderNotFound() {
		FileManager fileManager = new FileManager();
		fileManager.addLocatorClassLoader(fileManager.getClass()
				.getClassLoader());
		try {
			InputStream in = fileManager.open("not/java/lang/String.class");
			closeInputStream(in);
			assertNull("Found non-existant class", in);
		} catch (NotFoundException ex) {
		}
	}

	public void testFileManagerLocatorZip() {
		FileManager fileManager = new FileManager();
		// ANDROID we have to copy the zip as a class resource to sdcard to be
		// able to open it
		try {
			InputStream is = TestHelper.openResource(
					zipname);
			if (is == null)
				throw new Exception(zipname + " not found");
			File root = Environment.getExternalStorageDirectory();
			File zipfile = new File(root, "jenatest.zip");
			if (zipfile.exists())
				zipfile.delete();
			zipfile.createNewFile();
			zipfile.deleteOnExit();
			FileOutputStream os = new FileOutputStream(zipfile);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = is.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, len);
			}
			is.close();
			os.flush();
			os.close();
			fileManager.addLocatorZip(zipfile.getAbsolutePath());
		} catch (Exception ex) {
			fail("Failed to create a filemanager and add a zip locator");
		}
		InputStream in = fileManager.open(filename);
		assertNotNull(in);
		closeInputStream(in);
		// try {
		// fileManager.addLocatorZip(zipname) ;
		// } catch (Exception ex) {
		// fail("Failed to create a filemanager and add a zip locator");
		// }
		// InputStream in = fileManager.open(filename);
		// assertNotNull(in);
		// closeInputStream(in);
	}

	public void testFileManagerLocatorZipNonFound() {
		FileManager fileManager = new FileManager();
		// ANDROID we have to copy the zip as a class resource to sdcard to be
		// able to open it
		try {
			InputStream is = TestHelper.openResource(
					zipname);
			if (is == null)
				throw new Exception(zipname + " not found");
			File root = Environment.getExternalStorageDirectory();
			File zipfile = new File(root, "jenatest.zip");
			if (zipfile.exists())
				zipfile.delete();
			zipfile.createNewFile();
			zipfile.deleteOnExit();
			FileOutputStream os = new FileOutputStream(zipfile);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = is.read(buffer, 0, buffer.length)) != -1) {
				os.write(buffer, 0, len);
			}
			is.close();
			os.flush();
			os.close();
			fileManager.addLocatorZip(zipfile.getAbsolutePath());
		} catch (Exception ex) {
			fail("Failed to create a filemanager and add a zip locator");
		}
		// try {
		// fileManager.addLocatorZip(zipname) ;
		// } catch (Exception ex) {
		// fail("Failed to create a filemanager and add a zip locator");
		// }
		try {
			InputStream in = fileManager.open(filenameNonExistent);
			closeInputStream(in);
			assertNull("Found non-existant zip file member", in);
		} catch (NotFoundException ex) {
		}
	}

	public void testFileManagerClone() {
		FileManager fileManager1 = new FileManager();
		FileManager fileManager2 = new FileManager(fileManager1);

		// Should not affect fileManager2
		// ANDROID: changed to use classloader
		fileManager1.addLocatorClassLoader(getClass().getClassLoader());
		// fileManager1.addLocatorFile();
		{
			InputStream in = fileManager1.open(testingDir + "/" + filename);
			assertNotNull(in);
			closeInputStream(in);
		}
		// Should not work.
		try {
			InputStream in = fileManager2.open(testingDir + "/" + filename);
			closeInputStream(in);
			assertNull("Found file via wrong FileManager", in);
		} catch (NotFoundException ex) {
		}
	}

	public void testLocationMappingURLtoFileOpen() {
		//ANDROID: does not work. to make it work, we should copy testing/FileManager/location-mapping-test.n3
		// and location-mapping-test-file to the sdcard, and make the mappings point to the correct paths
		// inside the sdcard.
		LocationMapper locMap = new LocationMapper(TestLocationMapper.mapping);
		FileManager fileManager = new FileManager(locMap);
		fileManager.addLocatorFile();
		InputStream in = fileManager.open("http://example.org/file");
		assertNotNull(in);
		closeInputStream(in);
	}

	public void testLocationMappingURLtoFileOpenNotFound() {
		//ANDROID: does not work. to make it work, we should copy testing/FileManager/location-mapping-test.n3
		// and location-mapping-test-file to the sdcard, and make the mappings point to the correct paths
		// inside the sdcard. It doesn't fail but the reason is the wrong one.
		LocationMapper locMap = new LocationMapper(TestLocationMapper.mapping);
		FileManager fileManager = new FileManager(locMap);
		fileManager.addLocatorClassLoader(fileManager.getClass()
				.getClassLoader());
		try {
			InputStream in = fileManager.open("http://example.org/file");
			closeInputStream(in);
			assertNull("Found nont-existant URL", null);
		} catch (NotFoundException ex) {
		}
	}

	public void testCache1() {
		FileManager fileManager = new FileManager();
		// ANDROID: changed to use classloader
		fileManager.addLocatorClassLoader(getClass().getClassLoader());
		Model m1 = fileManager.loadModel(testingDir + "/" + fileModel);
		Model m2 = fileManager.loadModel(testingDir + "/" + fileModel);
		// fileManager.addLocatorFile(testingDir) ;
		// Model m1 = fileManager.loadModel(fileModel) ;
		// Model m2 = fileManager.loadModel(fileModel) ;
		assertNotSame(m1, m2);
	}

	public void testCache2() {
		FileManager fileManager = FileManager.get();
		// ANDROID: changed to use classloader
		fileManager.addLocatorClassLoader(getClass().getClassLoader());
		fileManager.setModelCaching(true);
		Model m1 = fileManager.loadModel(testingDir + "/" + fileModel);
		Model m2 = fileManager.loadModel(testingDir + "/" + fileModel);
		// fileManager.addLocatorFile(testingDir) ;
		// fileManager.setModelCaching(true) ;
		// Model m1 = fileManager.loadModel(fileModel) ;
		// Model m2 = fileManager.loadModel(fileModel) ;
		assertSame(m1, m2);
	}

	public void testCache3() {
		FileManager fileManager = FileManager.get();
		// ANDROID: changed to use classloader
		fileManager.addLocatorClassLoader(getClass().getClassLoader());
		fileManager.setModelCaching(true);
		Model m1 = fileManager.loadModel(testingDir + "/" + fileModel);
		Model m2 = fileManager.loadModel(testingDir + "/" + fileModel);
		assertSame(m1, m2);

		fileManager.removeCacheModel(testingDir + "/" + fileModel);
		Model m3 = fileManager.loadModel(testingDir + "/" + fileModel);
		assertNotSame(m1, m3);

		fileManager.resetCache();
		Model m4 = fileManager.loadModel(testingDir + "/" + fileModel);
		Model m5 = fileManager.loadModel(testingDir + "/" + fileModel);
		// fileManager.addLocatorFile(testingDir) ;
		// fileManager.setModelCaching(true) ;
		// Model m1 = fileManager.loadModel(fileModel) ;
		// Model m2 = fileManager.loadModel(fileModel) ;
		// assertSame(m1, m2) ;
		//        
		// fileManager.removeCacheModel(fileModel) ;
		// Model m3 = fileManager.loadModel(fileModel) ;
		// assertNotSame(m1, m3) ;
		//        
		// fileManager.resetCache() ;
		// Model m4 = fileManager.loadModel(fileModel) ;
		// Model m5 = fileManager.loadModel(fileModel) ;

		assertSame(m4, m5);
		assertNotSame(m1, m4);
		assertNotSame(m3, m4);
	}

	// public void testFileManagerLocatorURL()
	// {
	// FileManager fileManager = new FileManager() ;
	// fileManager.addLocatorURL() ;
	// InputStream in = fileManager.open("http:///www.bbc.co.uk/") ;
	// //assertNotNull(in) ;
	// // Proxies matter.
	// if ( in == null )
	// log.warn("Failed to contact http:///www.bbc.co.uk/: maybe due to proxy issues")
	// ;
	//        
	// try { if ( in != null ) in.close() ; }
	// catch (Exception ex) {}
	// }

	// -------- Helpers

	private void closeInputStream(InputStream in) {
		try {
			if (in != null)
				in.close();
		} catch (Exception ex) {
		}
	}
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development
 * Company, LP All rights reserved.
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