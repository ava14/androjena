/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 * $Id: TestCaseBugs.java,v 1.1 2009/06/29 08:55:51 castagna Exp $
 */

package com.hp.hpl.jena.mem.test;

import com.hp.hpl.jena.rdf.model.*;

/**
    @author  bwm
    @version $Name: Jena-2_6_2 $ $Revision: 1.1 $ $Date: 2009/06/29 08:55:51 $
*/
public class TestCaseBugs 
            extends TestCaseBasic {
                
    Model model = null;

    public TestCaseBugs(String name) {
        super(name);
    }
    
    @Override public void setUp() {
        model = ModelFactory.createDefaultModel();
    }
    
    public void bug36() {
    // addLiteral deprecated, test suppressed
//            Resource r    = model.createResource();
//            Object   oc   = RDFS.Class;
//            Object   op   = RDF.Property;
//            
//            Statement s = model.createLiteralStatement(r, RDF.type, oc);
//            assertInstanceOf(Resource.class, s.getObject() );
//            
//            //s.changeObject(op);
//            s = model.createLiteralStatement(r, RDF.type, op);
//            
//            assertInstanceOf(Resource.class, s.getObject() );
//            
//            model.addLiteral(r, RDF.type, oc);
//            RDFNode n = model.listStatements()
//                             .nextStatement()
//                             .getObject();
//            assertInstanceOf(Resource.class, n );
//            
//            assertTrue(model.listResourcesWithProperty(RDF.type, oc).hasNext());
//            
//            assertTrue(model.containsLiteral(r, RDF.type, oc));  
         }
}
