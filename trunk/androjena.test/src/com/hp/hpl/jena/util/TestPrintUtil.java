/******************************************************************
 * File:        TestPrintUtil.java
 * Created by:  Dave Reynolds
 * Created on:  16-Aug-2006
 * 
 * (c) Copyright 2006, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestPrintUtil.java,v 1.1 2009/06/29 18:42:05 andy_seaborne Exp $
 *****************************************************************/

package com.hp.hpl.jena.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.PrintUtil;

public class TestPrintUtil extends TestCase
{    
    
    public TestPrintUtil(String name) {
        super( name );
    }
     
     public static TestSuite suite() {
         return new TestSuite( TestPrintUtil.class );
     }   

     // Minimal test of formating a URI with prefixes
     public void testPrefixUse() {
         String NS = "http://jena.hpl.hp.com/example#";
         String name = "r1";
         String uri = NS + name;
         String shortform = "p:" + name;
         Resource r = ResourceFactory.createResource(uri);
         assertEquals(uri, PrintUtil.print(r));
         
         PrintUtil.registerPrefix("p", NS);
         assertEquals(shortform, PrintUtil.print(r));
         
         PrintUtil.removePrefix("p");
         assertEquals(uri, PrintUtil.print(r));
         
         Map<String, String> map = new HashMap<String, String>();
         map.put("p", NS);
         PrintUtil.registerPrefixMap(map);
         assertEquals(shortform, PrintUtil.print(r));

         PrintUtil.removePrefixMap( map );
         assertEquals(uri, PrintUtil.print(r));
     }
}


/*
    (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
