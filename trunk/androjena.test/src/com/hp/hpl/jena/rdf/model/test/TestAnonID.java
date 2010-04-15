/******************************************************************
 * File:        TestAnonID.java
 * Created by:  Dave Reynolds
 * Created on:  18-Mar-2004
 * 
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
 * [See end of file]
 * $Id: TestAnonID.java,v 1.1 2009/06/29 08:55:33 castagna Exp $
 *****************************************************************/
package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.test.JenaTestBase;

import junit.framework.TestSuite;

/**
 * Test for anonID generation. (Originally test for the debugging hack
 * that switches off anonID generation.)
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009/06/29 08:55:33 $
 */
public class TestAnonID extends JenaTestBase {
    
    /**
     * Boilerplate for junit
     */ 
    public TestAnonID( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestAnonID.class ); 
    }  

    /**
     * Check that anonIDs are distinct whichever state the flag is in.
     */
    public void testAnonID() {
        boolean prior = JenaParameters.disableBNodeUIDGeneration;
        try
            {
            JenaParameters.disableBNodeUIDGeneration = false;
            doTestAnonID();
            JenaParameters.disableBNodeUIDGeneration = true;
            doTestAnonID();
            }
        finally
            { JenaParameters.disableBNodeUIDGeneration = prior; }
    }

    /**
         Check that anonIDs are distinct whichever state the flag is in.
    */
    public void doTestAnonID() {
        AnonId id1 = AnonId.create();
        AnonId id2 = AnonId.create();
        AnonId id3 = AnonId.create();
        AnonId id4 = AnonId.create();
        
        assertDiffer( id1, id2 );
        assertDiffer( id1, id3 );
        assertDiffer( id1, id4 );
        assertDiffer( id2, id3 );
        assertDiffer( id2, id4 );
    }
    
    /**
        Test that creation of an AnonId from an AnonId string preserves that
        string and is equal to the original AnonId.
    */
    public void testAnonIdPreserved()
        {
        AnonId anon = AnonId.create();
        String id = anon.toString();
        assertEquals( anon, AnonId.create( id ) );
        assertEquals( id, AnonId.create( id ).toString() );
        }

}


/*
    (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
