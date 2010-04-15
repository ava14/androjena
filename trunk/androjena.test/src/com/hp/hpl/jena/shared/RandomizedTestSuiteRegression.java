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
 * $Id: RandomizedTestSuiteRegression.java,v 1.1 2009/06/29 18:42:05 andy_seaborne Exp $
 */

package com.hp.hpl.jena.shared;

import com.hp.hpl.jena.mem.test.TestSuiteRegression;

import junit.framework.*;

/**
 *
 * @author  bwm
 * @version $Name: Jena-2_6_2 $ $Revision: 1.1 $ $Date: 2009/06/29 18:42:05 $
 */
public class RandomizedTestSuiteRegression extends Object {

    public static TestSuite suite() {
    	TestSuite s = new TestSuite();
    	s.setName("Random order models");
        return suite(s);
    }

    public static TestSuite suite(TestSuite suite) {
    	for (int i=0;i<TestSuiteRegression.testNames.length;i++)
            suite.addTest(new RandomizedTestCaseBasic(TestSuiteRegression.testNames[i]));
     	
        return suite;
    }
}
