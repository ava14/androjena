/*
  (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPrefixMapping.java,v 1.1 2009/06/29 18:42:05 andy_seaborne Exp $
*/

package com.hp.hpl.jena.shared;

import junit.framework.TestSuite;

import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.RSS;
import com.hp.hpl.jena.vocabulary.VCARD;

/**
    Tests PrefixMappingImpl by subclassing AbstractTestPrefixMapping, qv.
    @author kers
*/

public class TestPrefixMapping extends AbstractTestPrefixMapping
    {
    public TestPrefixMapping( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestPrefixMapping.class ); }

    @Override
    protected PrefixMapping getMapping()
        { return new PrefixMappingImpl(); }

    public void testStandard()
        { testStandard( PrefixMapping.Standard ); }

    public void testExtended()
        { testExtended( PrefixMapping.Extended ); }

    public void testStandard( PrefixMapping st )
        {
        assertEquals( RDF.getURI(), st.getNsPrefixURI( "rdf" ) );
        assertEquals( RDFS.getURI(), st.getNsPrefixURI( "rdfs" ) );
        assertEquals( DC_11.getURI(), st.getNsPrefixURI( "dc" ) );
        assertEquals( OWL.getURI(), st.getNsPrefixURI( "owl" ) );
        }

    public void testExtended( PrefixMapping st )
        {
        testStandard( st );
        assertEquals( DAMLVocabulary.NAMESPACE_DAML_2001_03_URI, st.getNsPrefixURI( "daml" ) );
        assertEquals( RSS.getURI(), st.getNsPrefixURI( "rss" ) );
        assertEquals( VCARD.getURI(), st.getNsPrefixURI( "vcard" ) );
        assertEquals( JA.getURI(), st.getNsPrefixURI( "ja" ) );
        assertEquals( "http://www.example.org/", st.getNsPrefixURI( "eg" ) );
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