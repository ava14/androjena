/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: MetaTestGraph.java,v 1.1 2009/06/29 08:55:40 castagna Exp $
*/

package com.hp.hpl.jena.graph.test;

import java.lang.reflect.*;

import junit.framework.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.shared.*;

/**
	MetaTestGraph

	@author kers
*/
public class MetaTestGraph extends AbstractTestGraph 
    {
    protected final Class<? extends Graph> graphClass;
    protected final ReificationStyle style;
    
	public MetaTestGraph( Class<? extends Graph> graphClass, String name, ReificationStyle style ) 
        {
		super( name );
        this.graphClass = graphClass;
        this.style = style;
        }
        
    public MetaTestGraph( String name )
        { super( name ); graphClass = null; style = null; }
     
    public static TestSuite suite()
        { return suite( MetaTestGraph.class, GraphMem.class, ReificationStyle.Minimal ); }
            
    /**
        Construct a suite of tests from the test class <code>testClass</code>
        by instantiating it three times, once each for the three reification styles,
        and applying it to the graph <code>graphClass</code>.
    */
    public static TestSuite suite( Class<? extends Test> testClass, Class<? extends Graph> graphClass )
        {
        TestSuite result = new TestSuite();
        result.addTest( suite( testClass, graphClass, ReificationStyle.Minimal ) ); 
        result.addTest( suite( testClass, graphClass, ReificationStyle.Standard ) ); 
        result.addTest( suite( testClass, graphClass, ReificationStyle.Convenient ) ); 
        return result;    
        }
        
    public static TestSuite suite( Class<? extends Test> testClass, Class<? extends Graph> graphClass, ReificationStyle style )
        {
        TestSuite result = new TestSuite();
        for (Class<?> c = testClass; Test.class.isAssignableFrom( c ); c = c.getSuperclass())
            {
            Method [] methods = c.getDeclaredMethods();
            addTestMethods( result, testClass, methods, graphClass, style );  
            }
        return result;    
        }
        
    public static void addTestMethods
        ( TestSuite result, Class<? extends Test> testClass, Method [] methods, Class<? extends Graph> graphClass, ReificationStyle style  )
        {
        for (int i = 0; i < methods.length; i += 1)
            if (isPublicTestMethod( methods[i] )) 
                result.addTest( makeTest( testClass, graphClass, methods[i].getName(), style ) );  
        }
        
    public static TestCase makeTest( Class<? extends Test> testClass, Class<? extends Graph> graphClass, String name, ReificationStyle style )
        {
        Constructor<?> cons = getConstructor( testClass, new Class[] {Class.class, String.class, ReificationStyle.class} );
        if (cons == null) throw new JenaException( "cannot find MetaTestGraph constructor" );
        try { return (TestCase) cons.newInstance( new Object [] {graphClass, name, style} ); }
        catch (Exception e) { throw new JenaException( e ); }
        }

	@Override public Graph getGraph() 
        { return getGraph( this, graphClass, style ); }
        
    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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