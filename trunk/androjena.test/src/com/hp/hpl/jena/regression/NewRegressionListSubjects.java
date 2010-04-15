/*
 	(c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: NewRegressionListSubjects.java,v 1.1 2009/06/29 08:55:39 castagna Exp $
*/

package com.hp.hpl.jena.regression;

import java.util.*;

import junit.framework.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
//import com.hp.hpl.jena.regression.Regression.*;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;

public class NewRegressionListSubjects extends ModelTestBase
    {
    public NewRegressionListSubjects( String name )
        { super( name ); }

    public static Test suite()
        { return new TestSuite( NewRegressionListSubjects.class ); }
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    protected Model m;
    
    @Override public void setUp()
        { 
        m = getModel();
        fillModel();
        }
    
    @Override public void tearDown()
        { m = null; }
    
    static final String subjectPrefix = "http://aldabaran/test8/s";
    
    static final String predicatePrefix = "http://aldabaran/test8/";
    
    Resource [] subjects;
    Property [] predicates;  
    RDFNode []  objects;
//    Literal []  tvLitObjs;
    Resource [] tvResObjs;
    
    boolean [] tvBooleans = { false, true };
    long []    tvLongs    = { 123, 321 };
    char []    tvChars    = { '@', ';' };
    float []   tvFloats   = { 456.789f, 789.456f };
    double []  tvDoubles  = { 123.456, 456.123 };
    String []  tvStrings  = { "test8 testing string 1", "test8 testing string 2" };
    String []  langs     = { "en", "fr" };
    
    protected Set<Resource> subjectsTo( String prefix, int limit )
        {
        Set<Resource> result = new HashSet<Resource>();
        for (int i = 0; i < limit; i += 1) result.add( resource( prefix + i ) );
        return result;
        }
    
// the methods are deprecated, the tests eliminated
//    public void testListResourcesOnObject()
//        {
//        Object d = new Date();
//        Model m = modelWithStatements( "" );
//        m.addLiteral( resource( "S" ), property( "P" ), d );
//        m.addLiteral( resource( "X" ), property( "P" ), new Object() );
//        List answers = m.listResourcesWithProperty( property( "P" ), d ).toList();
//        assertEquals( listOfOne( resource( "S" ) ), answers );
//        }
    
    public void test8()  
        {
        assertEquiv( subjectsTo( subjectPrefix, 5 ), m.listResourcesWithProperty( predicates[4] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0] ) );
        
        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], tvBooleans[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvBooleans[1] ) );
       
        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], tvChars[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvChars[1] ) );
        
        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], tvLongs[0] ) );
    
        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvLongs[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], tvFloats[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvFloats[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], tvDoubles[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvDoubles[1] ) );
        
        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], (byte) tvLongs[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], (byte) tvLongs[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], (short) tvLongs[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], (short) tvLongs[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], (int) tvLongs[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], (int) tvLongs[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicates[0], tvStrings[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicates[0], tvStrings[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listSubjectsWithProperty( predicates[0], tvStrings[0], langs[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicates[0], tvStrings[1], langs[0] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicates[0], tvStrings[0], langs[1] ) );

        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listSubjectsWithProperty( predicates[0], tvStrings[1], langs[1] ) );

//        assertEquiv( subjectsTo( subjectPrefix, 2 ), m.listResourcesWithProperty( predicates[0], tvLitObjs[0] ) );
//
//        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvLitObjs[1] ) );
//
//        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvResObjs[0] ) );
//
//        assertEquiv( subjectsTo( subjectPrefix, 0 ), m.listResourcesWithProperty( predicates[0], tvResObjs[1] ) );

        // assertEquiv( new HashSet( Arrays.asList( objects ) ), m.listObjectsOfProperty( predicates[1] ) );
        }

    protected void assertEquiv( Set<? extends Resource> set, Iterator<? extends Resource> iterator )
        {
        List<? extends Resource> L = iteratorToList( iterator );
        assertEquals( set.size(), L.size() );
        assertEquals( set, new HashSet<Resource>( L ) );
        }

    public void testGetRequiredProperty()
        {
        Statement s = m.getRequiredProperty( subjects[1], predicates[1] );
        try { m.getRequiredProperty( subjects[1], RDF.value ); 
            fail( "should not find absent property" ); } 
        catch (PropertyNotFoundException e) 
            { pass(); }
        }

    protected void fillModel(  )
        {
        final int num = 5;
//        tvLitObjs = new Literal[] 
//            { m.createTypedLiteral( new LitTestObjF() ),
//            m.createTypedLiteral( new LitTestObjF() ) };
        
//        tvResObjs  = new Resource[] 
//            { m.createResource( new ResTestObjF() ),
//            m.createResource( new ResTestObjF() ) };
        
        objects = new RDFNode[]
            {
            m.createTypedLiteral( tvBooleans[1] ),
            m.createTypedLiteral( tvLongs[1] ),
            m.createTypedLiteral( tvChars[1] ),
            m.createTypedLiteral( tvFloats[1] ),
            m.createTypedLiteral( tvDoubles[1] ),
            m.createLiteral( tvStrings[1] ),
            m.createLiteral( tvStrings[1], langs[1] )
//            tvLitObjs[1],
//            tvResObjs[1]                  
            };

        subjects = new Resource[num];
        predicates = new Property[num];
        
        for (int i = 0; i<num; i++) 
            {
            subjects[i] = m.createResource( subjectPrefix + i );
            predicates[i] = m.createProperty( predicatePrefix + i, "p");
            }
        
        for (int i = 0; i < num; i += 1) 
            m.addLiteral(subjects[i], predicates[4], false );
        
        for (int i = 0; i < 2 ; i += 1) 
            {
            for (int j = 0; j < 2; j += 1) 
                {
                m.add(subjects[i], predicates[j], m.createTypedLiteral( tvBooleans[j] ) );
                m.addLiteral(subjects[i], predicates[j], tvLongs[j] );
                m.addLiteral(subjects[i], predicates[j], tvChars[j] );
                m.add(subjects[i], predicates[j], m.createTypedLiteral( tvFloats[j] ) );
                m.add(subjects[i], predicates[j], m.createTypedLiteral( tvDoubles[j] ) );
                m.add(subjects[i], predicates[j], tvStrings[j] );
                m.add(subjects[i], predicates[j], tvStrings[j], langs[j] );
//                m.add(subjects[i], predicates[j], tvLitObjs[j] );
//                m.add(subjects[i], predicates[j], tvResObjs[j] );
                }
            }
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