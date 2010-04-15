/*
 	(c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: TestConcurrentModificationException.java,v 1.1 2009/06/29 08:55:59 castagna Exp $
*/

package com.hp.hpl.jena.mem.faster.test;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public abstract class TestConcurrentModificationException extends ModelTestBase
    {
    public TestConcurrentModificationException( String name )
        { super( name ); }

    public abstract TripleBunch getBunch();
    
    public static TestSuite suite()
        { 
        TestSuite result = new TestSuite();
        result.addTestSuite( TestArrayBunchCME.class ); 
        result.addTestSuite( TestSetBunchCME.class ); 
        result.addTestSuite( TestHashedBunchCME.class ); 
        return result;
        }

    public static class TestArrayBunchCME extends TestConcurrentModificationException
        {
        public TestArrayBunchCME(String name)
            { super( name ); }

        @Override public TripleBunch getBunch()
            { return new ArrayBunch(); }
        }
    
    public static class TestSetBunchCME extends TestConcurrentModificationException
        {
        public TestSetBunchCME(String name)
            { super( name ); }

        @Override public TripleBunch getBunch()
            { return new SetBunch( new ArrayBunch() ); }
        }
    
    public static class TestHashedBunchCME extends TestConcurrentModificationException
        {
        public TestHashedBunchCME(String name)
            { super( name ); }

        @Override
        public TripleBunch getBunch()
            { return new HashedTripleBunch( new ArrayBunch() ); }
        }

    public void testAddThenNextThrowsCME()
        { 
        TripleBunch b = getBunch();
        b.add( NodeCreateUtils.createTriple( "a P b" ) );
        b.add( NodeCreateUtils.createTriple( "c Q d" ) );
        ExtendedIterator<Triple> it = b.iterator();
        it.next();
        b.add( NodeCreateUtils.createTriple( "change its state" ) );
        try { it.next(); fail( "should have thrown ConcurrentModificationException" ); }
        catch (ConcurrentModificationException e) { pass(); } 
        }

    public void testDeleteThenNextThrowsCME()
        { 
        TripleBunch b = getBunch();
        b.add( NodeCreateUtils.createTriple( "a P b" ) );
        b.add( NodeCreateUtils.createTriple( "c Q d" ) );
        ExtendedIterator<Triple> it = b.iterator();
        it.next();
        b.remove( NodeCreateUtils.createTriple( "a P b" ) );
        try { it.next(); fail( "should have thrown ConcurrentModificationException" ); }
        catch (ConcurrentModificationException e) { pass(); } 
        }

    private static final MatchOrBind mob = new MatchOrBind() 
        {
        @Override
        public boolean matches( Triple t )
            {
            return true;
            }
        
        @Override
        public MatchOrBind reset( Domain d )
            {
            return null;
            }
        };

    public void testAddDuringAppThrowsCME()
        {
        final TripleBunch b = getBunch();
        b.add( NodeCreateUtils.createTriple( "a P b" ) );
        b.add( NodeCreateUtils.createTriple( "c Q d" ) );
        StageElement se = new StageElement() 
            {
            @Override
            public void run( Domain current )
                { b.add( NodeCreateUtils.createTriple(  "S P O"  ) ); }
            };
        try { b.app(  new Domain( 0 ), se, mob ); fail(" should throw CME" ); }
        catch (ConcurrentModificationException e) { pass(); }
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