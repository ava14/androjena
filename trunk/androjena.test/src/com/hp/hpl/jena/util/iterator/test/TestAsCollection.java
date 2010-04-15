/*
 	(c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestAsCollection.java,v 1.1 2009/06/29 08:55:59 castagna Exp $
*/

package com.hp.hpl.jena.util.iterator.test;

import java.util.*;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class TestAsCollection extends ModelTestBase
    {
    public TestAsCollection( String name )
        { super( name ); }

    public void testAsList()
        {
        testReturnsList( "" );
        testReturnsList( "understanding" );
        testReturnsList( "understanding is" );
        testReturnsList( "understanding is a three-edged sword" );
        }
    
    public void testAsSet()
        {
        testReturnsSet( "" );
        testReturnsSet( "x" );
        testReturnsSet( "x x" );
        testReturnsSet( "x y x" );
        testReturnsSet( "a b c d e f a c f x" );
        testReturnsSet( "the avalanch has already started" );
        }

    private Set<String> testReturnsSet( String elements )
        {
        Set<String> result = setOfStrings( elements );
        assertEquals( result, WrappedIterator.create( result.iterator() ).toSet() );
        return result;
        }

    private void testReturnsList( String elements )
        {
        List<String> L = listOfStrings( elements );
        assertEquals( L, WrappedIterator.create( L.iterator() ).toList() );
        }
    }


/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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