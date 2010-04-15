package com.hp.hpl.jena.n3.turtle ;

/* CVS $Id: TurtleTestVocab.java,v 1.1 2009/06/29 18:42:05 andy_seaborne Exp $ */
 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from TurtleTestVocab.ttl 
 * @author Auto-generated by schemagen on 22 Dec 2005 15:02 
 */
public class TurtleTestVocab {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://jena.hpl.hp.com/2005/12/test-turtle#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Declare a IRI for the test input</p> */
    public static final Property inputIRI = m_model.createProperty( "http://jena.hpl.hp.com/2005/12/test-turtle#inputIRI" );
    
    /** <p>Output of a test</p> */
    public static final Property output = m_model.createProperty( "http://jena.hpl.hp.com/2005/12/test-turtle#output" );
    
    /** <p>Input to a test</p> */
    public static final Property input = m_model.createProperty( "http://jena.hpl.hp.com/2005/12/test-turtle#input" );
    
    public static final Resource TestInOut = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#TestInOut" );
    
    public static final Resource TestBadSyntax = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#TestBadSyntax" );
    
    public static final Resource TestSyntax = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#TestSyntax" );
    
    public static final Resource Test = m_model.createResource( "http://jena.hpl.hp.com/2005/12/test-turtle#Test" );
    
}
