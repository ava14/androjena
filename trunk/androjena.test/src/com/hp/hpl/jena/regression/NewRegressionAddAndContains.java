/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionAddAndContains.java,v 1.1 2009/06/29 08:55:39 castagna Exp $
*/

package com.hp.hpl.jena.regression;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.regression.Regression.LitTestObj;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionAddAndContains extends NewRegressionBase
    {
    public NewRegressionAddAndContains( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( NewRegressionAddAndContains.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    protected Resource S;
    protected Property P;
    
    @Override
    public void setUp()
        { 
        m = getModel();
        S = m.createResource( "http://nowhere.man/subject" ); 
        P = m.createProperty( "http://nowhere.man/predicate" ); 
        }
    
    @Override
    public void tearDown()
        { m = null; S = null; P = null; }
    
    public void testEmpty() 
        {
        assertFalse( m.containsLiteral( S, P, tvBoolean ) );
        assertFalse( m.contains( S, P, m.createResource() ) );
        assertFalse( m.containsLiteral( S, P, tvByte ) );
        assertFalse( m.containsLiteral( S, P, tvShort ) );
        assertFalse( m.containsLiteral( S, P, tvInt ) );
        assertFalse( m.containsLiteral( S, P, tvLong ) );
        assertFalse( m.containsLiteral( S, P, tvChar ) );
        assertFalse( m.containsLiteral( S, P, tvFloat ) );
        assertFalse( m.containsLiteral( S, P, tvDouble ) );
        assertFalse( m.containsLiteral( S, P, new LitTestObj( 12345 ) ) );
        assertFalse( m.contains( S, P, "test string" ) );
        assertFalse( m.contains( S, P, "test string", "en" ) );
        }
    
    public void testAddContainsResource()
        {
        Resource r = m.createResource();
        m.add( S, P, r );
        assertTrue( m.contains( S, P, r ) );
        }
    
    public void testAddContainsBoolean()
        {
        m.addLiteral( S, P, tvBoolean );
        assertTrue( m.containsLiteral( S, P, tvBoolean ) );
        }
    
    public void testAddContainsByte()
        {
        m.addLiteral( S, P, tvByte );
        assertTrue( m.containsLiteral( S, P, tvByte ) );
        }
    
    public void testAddContainsShort()
        {
        m.addLiteral( S, P, tvShort );
        assertTrue( m.containsLiteral( S, P, tvShort ) );
        }    
    
    public void testAddContainsInt()
        {
        m.addLiteral( S, P, tvInt );
        assertTrue( m.containsLiteral( S, P, tvInt ) );
        }
    
    public void testAddContainsLong()
        {
        m.addLiteral( S, P, tvLong );
        assertTrue( m.containsLiteral( S, P, tvLong ) );
        }
    
    public void testAddContainsChar()
        {
        m.addLiteral( S, P, tvChar );
        assertTrue( m.containsLiteral( S, P, tvChar ) );
        }
    
    public void testAddContainsFloat()
        {
        m.addLiteral( S, P, tvFloat );
        assertTrue( m.containsLiteral( S, P, tvFloat ) );
        }
    
    public void testAddContainsDouble()
        {
        m.addLiteral( S, P, tvDouble );
        assertTrue( m.containsLiteral( S, P, tvDouble ) );
        }

//    public void testAddContainsObject()
//        {
//        LitTestObj O = new LitTestObj( 12345 );
//        m.addLiteral( S, P, O );
//        assertTrue( m.containsLiteral( S, P, O ) );
//        }
    
    public void testAddContainsPlainString()
        {
        m.add( S, P, "test string" );
        assertTrue( m.contains( S, P, "test string" ) );
        assertFalse( m.contains( S, P, "test string", "en" ) );
        }
    
    public void testAddContainsLanguagedString()
        {
        m.add( S, P, "test string", "en" );
        assertFalse( m.contains( S, P, "test string" ) );
        assertTrue( m.contains( S, P, "test string", "en" ) );
        }
    
    public void testAddContainLiteralByStatement()
        {
        Literal L = m.createTypedLiteral( 210 );
        Statement s = m.createStatement( S, RDF.value, L );
        assertTrue( m.add( s ).contains( s ) );
        assertTrue( m.contains( S, RDF.value ) );
        }
    
    public void testAddDuplicateLeavesSizeSame()
        {
        Statement s = m.createStatement( S, RDF.value, "something" );
        m.add( s );
        long size = m.size();
        m.add( s );
        assertEquals( size, m.size() );
        }

    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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