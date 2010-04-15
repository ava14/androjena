/*
  (c) Copyright 2002, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestDyadic.java,v 1.1 2009/06/29 08:55:42 castagna Exp $
*/

package com.hp.hpl.jena.graph.compose.test;

import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.*;
import com.hp.hpl.jena.graph.test.*;


import java.util.*;
import junit.framework.*;

/**
	@author kers
*/
public class TestDyadic extends GraphTestBase
	{
	public TestDyadic( String name )
		{ super( name ); }
		
	public static TestSuite suite()
    	{ return new TestSuite( TestDyadic.class ); }
    	
	static private ExtendedIterator<String> things( final String x ) 
		{
		return new NiceIterator<String>()
			{
			private StringTokenizer tokens = new StringTokenizer( x );
			@Override public boolean hasNext() { return tokens.hasMoreTokens(); }
			@Override public String next() { return tokens.nextToken(); }
			};
		}
		
	public void testDyadic() 
		{
		ExtendedIterator<String> it1 = things( "now is the time" );
		ExtendedIterator<String> it2 = things( "now is the time" );
		ExtendedIterator<String> mt1 = things( "" );
		ExtendedIterator<String> mt2 = things( "" );
		assertEquals( "mt1.hasNext()", false, mt1.hasNext() );
		assertEquals( "mt2.hasNext()", false, mt2.hasNext() );
		assertEquals( "andThen(mt1,mt2).hasNext()", false, mt1.andThen( mt2 ).hasNext() ); 		
		assertEquals( "butNot(it1,it2).hasNext()", false, CompositionBase.butNot( it1, it2 ).hasNext() );
		assertEquals( "x y z @butNot z", true, CompositionBase.butNot( things( "x y z" ), things( "z" ) ).hasNext() );
		assertEquals( "x y z @butNot a", true, CompositionBase.butNot( things( "x y z" ), things( "z" ) ).hasNext() );
		}
    
    public void testDyadicOperands()
        {
        Graph g = Factory.createGraphMem(), h = Factory.createGraphMem();
        Dyadic d = new Dyadic( g, h )
            {
            @Override public ExtendedIterator<Triple> graphBaseFind( TripleMatch m ) { return null; }
            };
        assertSame( g, d.getL() );
        assertSame( h, d.getR() );
        }
	}

/*
    (c) Copyright 2002, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
