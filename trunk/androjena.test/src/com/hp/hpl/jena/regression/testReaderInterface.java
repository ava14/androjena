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
 * $Id: testReaderInterface.java,v 1.1 2009/06/29 08:55:39 castagna Exp $
 */

package com.hp.hpl.jena.regression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.impl.NTripleReader;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NoReaderForLangException;

/**
 *
 * @author  bwm
 * @version $Revision: 1.1 $
 */
public class testReaderInterface extends Object {


    protected static void doTest(Model m1) {
        (new testReaderInterface()).test(m1);
    }

    protected static Logger logger = LoggerFactory.getLogger( testReaderInterface.class );
    
    void test(Model m1) {

        String  test = "testReaderInterface";
        String  filebase = "testing/regression/" + test + "/";
    //    System.out.println("Beginning " + test);
        int n = 0;
        try {
            n++; RDFReader reader = m1.getReader();

            /*
            if (! (reader instanceof com.hp.hpl.jena.rdf.arp.JenaReader ))
                 error(test, n);

            n++; reader = m1.getReader("RDF/XML");
            if (! (reader instanceof com.hp.hpl.jena.rdf.arp.JenaReader ))
                 error(test, n);
            */

            n++; reader = m1.getReader("N-TRIPLE");
                 if (! (reader instanceof NTripleReader)) error(test, n);

            n++; try {
                    m1.setReaderClassName("foobar", "");
                    reader = m1.getReader("foobar");
                    error(test, n);
                 } catch (NoReaderForLangException jx) {
                     // that's what we expect
                 }

            n++; m1.setReaderClassName("foobar",
                                       com.hp.hpl.jena.rdf.arp.JenaReader.class.getName());
                 reader = m1.getReader("foobar");
                 if (! (reader instanceof com.hp.hpl.jena.rdf.arp.JenaReader)) error(test, n);

                 try {

                n++; m1.read("http://www.w3.org/2000/10/rdf-tests/rdfcore/"
                          +  "rdf-containers-syntax-vs-schema/test001.rdf");

                n++; m1.read("http://www.w3.org/2000/10/rdf-tests/rdfcore/"
                          +  "rdf-containers-syntax-vs-schema/test001.nt",
                             "N-TRIPLE");
                } catch (JenaException jx)
                    {
                    if (jx.getCause() instanceof NoRouteToHostException
                        || jx.getCause() instanceof UnknownHostException
                        || jx.getCause() instanceof ConnectException
                        || jx.getCause() instanceof IOException
                        )
                        {logger.warn("Cannot access public internet - part of test not executed" );
                        }
                    else
                        throw jx;
                    }

            //ANDROID: use a reader to prevent errors for unrecognized tokens from SAX parser
                n++; m1.read(new BufferedReader(new InputStreamReader(ResourceReader.getInputStream(filebase + "1.rdf"))), "http://example.org/");

                n++; m1.read(
                		new BufferedReader(new InputStreamReader(ResourceReader.getInputStream(filebase + "2.nt"))),  "", "N-TRIPLE");
//            n++; m1.read(ResourceReader.getInputStream(filebase + "1.rdf"), "http://example.org/");
//
//            n++; m1.read(
//                    ResourceReader.getInputStream(filebase + "2.nt"),  "", "N-TRIPLE");


        } catch (Exception e) {
            inError = true;
            logger.error( " test " + test + "[" + n + "]", e);
        }
      //  System.out.println("End of " + test);
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
