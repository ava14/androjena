/*
 	(c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestEntityOutput.java,v 1.2 2009/10/02 12:59:11 andy_seaborne Exp $
*/

package com.hp.hpl.jena.xmloutput;

import it.polimi.dei.dbgroup.pedigree.androjena.test.TestHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;


import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.xmloutput.impl.BaseXMLWriter;

/**
    Tests for entities being created corresponding to prefixes.
    @author kers
*/
public class TestEntityOutput extends ModelTestBase
    {
    public TestEntityOutput( String name )
        { super( name ); }
    
    public void testSettingWriterEntityProperty()
        {
        FakeBaseWriter w = new FakeBaseWriter();
        assertEquals( false, w.getShowDoctypeDeclaration() );
        assertEquals( "false", w.setProperty( "showDoctypeDeclaration", "true" ) );
        assertEquals( true, w.getShowDoctypeDeclaration() );
        assertEquals( "true", w.setProperty( "showDoctypeDeclaration", "false" ) );
        assertEquals( false, w.getShowDoctypeDeclaration() );
    //
        assertEquals( "false", w.setProperty( "showDoctypeDeclaration", Boolean.TRUE ) );
        assertEquals( true, w.getShowDoctypeDeclaration() );
        assertEquals( "true", w.setProperty( "showDoctypeDeclaration", Boolean.FALSE ) );
        assertEquals( false, w.getShowDoctypeDeclaration() );
        }    
    
    public void testKnownEntityNames()
        {
        BaseXMLWriter w = new FakeBaseWriter();
        assertEquals( true, w.isPredefinedEntityName( "lt" ) );
        assertEquals( true, w.isPredefinedEntityName( "gt" ) );
        assertEquals( true, w.isPredefinedEntityName( "amp" ) );
        assertEquals( true, w.isPredefinedEntityName( "apos" ) );
        assertEquals( true, w.isPredefinedEntityName( "quot" ) );
    //
        assertEquals( false, w.isPredefinedEntityName( "alt" ) );
        assertEquals( false, w.isPredefinedEntityName( "amper" ) );
        assertEquals( false, w.isPredefinedEntityName( "tapost" ) );
        assertEquals( false, w.isPredefinedEntityName( "gte" ) );
    //
        assertEquals( false, w.isPredefinedEntityName( "rdf" ) );
        assertEquals( false, w.isPredefinedEntityName( "smerp" ) );
        assertEquals( false, w.isPredefinedEntityName( "nl" ) );
        assertEquals( false, w.isPredefinedEntityName( "acute" ) );
        }

    public void testRDFNamespaceMissing()
        {
        Model m = createMemModel();
        modelAdd( m, "x R fake:uri#bogus" );
        m.setNsPrefix( "spoo", "fake:uri#" );
        m.setNsPrefix( "eh", "eh:/" );
        String s = checkedModelToString( m );
        assertMatches( "<!DOCTYPE rdf:RDF \\[", s );
        assertMatches( "<!ENTITY spoo 'fake:uri#'>", s );
        assertMatches( "rdf:resource=\"&spoo;bogus\"", s );
        }
    public void testUsesEntityForPrefix()
        {
        Model m = modelWithStatements( "x R fake:uri#bogus" );
        m.setNsPrefix( "spoo", "fake:uri#" );
        m.setNsPrefix( "eh", "eh:/" );
        String s = checkedModelToString( m );
        assertMatches( "<!DOCTYPE rdf:RDF \\[", s );
        assertMatches( "<!ENTITY spoo 'fake:uri#'>", s );
        assertMatches( "rdf:resource=\"&spoo;bogus\"", s );
        }

    public void testCatchesBadEntities()
        {
        testCatchesBadEntity( "amp" );
        testCatchesBadEntity( "lt" );
        testCatchesBadEntity( "gt" );
        testCatchesBadEntity( "apos" );
        testCatchesBadEntity( "quot" );
        }
    
    /* Old code produced:
<!DOCTYPE rdf:RDF [
  <!ENTITY dd 'http://www.example.org/a"b#'>
  <!ENTITY ampersand 'http://www.example.org/a?a&b#'>
  <!ENTITY espace 'http://www.example.org/a%20space#'>
  <!ENTITY zz 'http://www.example.org/a'b#'>
  <!ENTITY rdf 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'>]>
     * 
     */
    /**
     * See
     * http://www.w3.org/TR/xml/#NT-EntityValue
     * " & and % ' are all legal URI chars, but illegal
     * in entity defn.
     * @throws IOException 
     */
    public void testDifficultChars() throws IOException 
    {
    	Model m = createMemModel();
    	//ANDROID:changed to use classloader URL
    	m.read(TestHelper.getResourceURL("testing/abbreviated/entities.rdf"));
//    	m.read("file:testing/abbreviated/entities.rdf");
    	StringWriter w = new StringWriter();
    	RDFWriter wr = m.getWriter();
    	wr.setProperty("showDoctypeDeclaration", "true");
    	wr.write(m, w, "http://example.org/");
    	w.close();
//    	System.err.println(w.toString());
    	Reader r = new StringReader(w.toString());
    	Model m2 = createMemModel();
    	m2.read(r,"http://example.org/");
    	assertIsoModels("showDoctypeDeclaration problem", m, m2);
    }
    
    public void testCRinLiterals() throws IOException 
    {
        Model m = createMemModel();
        Resource r = m.createResource("http://example/r") ;
        Property p = m.createProperty("http://example/p") ;
        m.add(r, p, "abc\r\nxyz") ;
        StringWriter w = new StringWriter();
        m.write(w) ;
        Model m2 = createMemModel();
        m2.read(new StringReader(w.toString()), null) ;
        assertTrue(m.isIsomorphicWith(m2)) ;
    }

    private void testCatchesBadEntity( String bad )
        {
        Model m = modelWithStatements( "ampsersand spelt '&'; x R goo:spoo/noo" );
        m.setNsPrefix( "rdf", RDF.getURI() );
        m.setNsPrefix( bad, "goo:spoo" );
        m.setNsPrefix( "eh", "eh:/" );
        String s = checkedModelToString( m );
        //assertTrue( s.toString().contains( "<!DOCTYPE rdf:RDF [" ) ); // java5-ism
        assertTrue( s.toString().indexOf( "<!DOCTYPE rdf:RDF [" ) >= 0 );
        assertMismatches( "<!ENTITY " + bad + " ", s );
        assertMismatches( "rdf:resource=\"&" + bad + ";noo\"", s );
        }

    private void checkModelFromXML( Model shouldBe, String s )
        {
        Model m = createMemModel();
        m.read( new StringReader( s ), null, "RDF/XML" );
        assertIsoModels( "model should be read back correctly", shouldBe, m );
        }

    private String checkedModelToString( Model m )
        {
        String result = modelToString( m );
        checkModelFromXML( m, result );
        return result;
        }

    private String modelToString( Model m )
        {
        StringWriter s = new StringWriter();
        RDFWriter w = m.getWriter( "RDF/XML-ABBREV" );
        w.setProperty( "showDoctypeDeclaration", Boolean.TRUE );
        w.write( m, s, null );
        return s.toString();
        }
    
    private void assertMatches( String pattern, String x )
        {
        if (!x.matches( "(?s).*(" + pattern + ").*" ) )
                fail( "pattern {" + pattern + "} does not match string {" + x + "}" );
        }
    
    private void assertMismatches( String pattern, String x )
        {
        if (x.matches( "(?s).*(" + pattern + ").*" ) )
                fail( "pattern {" + pattern + "} should not match string {" + x + "}" );
        }
    
    private final static class FakeBaseWriter extends BaseXMLWriter
        {
        @Override
        protected void unblockAll() {}

        @Override
        protected void blockRule( Resource r ) {}

        @Override
        protected void writeBody( Model mdl, PrintWriter pw, String baseUri, boolean inclXMLBase ) {}

        protected boolean getShowDoctypeDeclaration() { return showDoctypeDeclaration.booleanValue(); }
        }
    }

/*
 *  (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
