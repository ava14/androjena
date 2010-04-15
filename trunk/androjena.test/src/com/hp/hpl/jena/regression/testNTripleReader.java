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
 * $Id: testNTripleReader.java,v 1.1 2009/06/29 08:55:39 castagna Exp $
 */

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  bwm
 * @version $Revision: 1.1 $
 */
public class testNTripleReader extends Object {
   
    
    protected static void doTest(Model m1) {
        (new testNTripleReader()).test(m1);
    }
    
    protected static Logger logger = LoggerFactory.getLogger( testNTripleReader.class );
    
    void test(Model m1) {

        String  test = "testNTripleReader";
        String  filebase = "testing/regression/" + test + "/";
    //    System.out.println("Beginning " + test);
        int n = 0;
        try {
                 empty(m1);
            n++; m1.read(ResourceReader.getInputStream(filebase + "1.nt"), "", "N-TRIPLE");
                 if (m1.size() != 5) error(test, n);
                 StmtIterator iter = 
                     m1.listStatements( null, null, "foo\"\\\n\r\tbar" );
            n++; if (! iter.hasNext()) error(test, n);
        } catch (Exception e) {
            inError = true;
            logger.error( " test " + test + "[" + n + "]", e);
        }
  //      System.out.println("End of " + test);        
    }
    
    protected void empty(Model m) {
        StmtIterator iter = m.listStatements();
        while (iter.hasNext()) {
            iter.nextStatement();
            iter.remove();
        }
    }
    private boolean inError = false;
       
    protected void error(String test, int n) {
        System.out.println(test + ": failed test " + Integer.toString(n));
        inError = true;
    }
    
    public boolean getErrors() {
        return inError;
    }
     
}
