/*
  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestPerlyParser.java,v 1.1 2009/06/29 08:56:03 castagna Exp $
*/
package com.hp.hpl.jena.graph.query.regexptrees.test;

import java.util.Arrays;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.query.regexptrees.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;

/**
     TestPerlyParser - tests for the parser of Perl REs into RegexpTrees.
     @author kers
*/
public class TestPerlyParser extends GraphTestBase
    {
    public TestPerlyParser( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestPerlyParser.class ); }
    
    protected static class FlagException extends RuntimeException
        {}
    
    public void testAlternateGenerator()
        {
        RegexpTreeGenerator g = new SimpleGenerator()
            {
            @Override
            public RegexpTree getAnySingle() { throw new FlagException(); }
            };
        PerlPatternParser p = new PerlPatternParser( ".", g );
        try { p.parseAtom(); fail( "should be using supplied generator" ); }
        catch (FlagException e) { pass(); }
        }
    
    public void testLit()
        {
        assertEquals( Text.create( "a" ), Text.create( "a" ) );
        assertDiffer( Text.create( "a" ), Text.create( "b" ) );
        assertEquals( Text.create( "aga" ).hashCode(), Text.create( "aga" ).hashCode() );
        }
    
    public void testInitialParserState()
        {
        assertEquals( 0, new PerlPatternParser( "hello" ).getPointer() );
        assertEquals( "hello", new PerlPatternParser( "hello" ).getString() );
        }
    
    public void testLetterAtoms()
        {
        for (char ch = 0; ch < 256; ch += 1)
            if (Character.isLetter( ch ))
                {
                PerlPatternParser p = new PerlPatternParser( "" + ch );
                assertEquals( Text.create( ch ), p.parseAtom() );
                assertEquals( 1, p.getPointer() );
                }
        }
    
    public void testEmptyExpression()
        { assertEquals( new Nothing(), element( "" ) ); }
    
    public void testDotAtom()
        { testSimpleSpecialAtom( RegexpTree.ANY, "." ); }
    
    public void testHatAtom()
        { testSimpleSpecialAtom( RegexpTree.SOL, "^" );  }
    
    public void testDollarAtom()
        { testSimpleSpecialAtom( RegexpTree.EOL, "$" ); }
    
    public void testTerminatorsReturnNull()
        {
        assertEquals( new Nothing(), element( "|" ) );
        }
    
    public void testSimpleBackslashEscapes()
        {
        for (char ch = 0; ch < 256; ch += 1)
            {
            if ("bBAZnrtfdDwWSsxc0123456789".indexOf( ch ) < 0)
                assertEquals( Text.create( ch ), new PerlPatternParser( "\\" + ch ).parseAtom() ); 
            }    
        }
    
    public void testSpecialBackslashEscapes()
        {
        String specials = "bBAZ";
        for (int i = 0; i < specials.length(); i += 1)
            try {new PerlPatternParser( "\\" + specials.charAt(i) ).parseAtom(); fail( "backslash escape " + specials.charAt(i) ); }
            catch (PerlPatternParser.SyntaxException e)
                { pass(); }    
        }
    
    public void testWordEscapes()
        {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        String wordChars = "0123456789" + letters + "_" + letters.toUpperCase();
        assertEquals( new AnyOf( wordChars ), element( "\\w" ) );
        assertEquals( new NoneOf( wordChars ), element( "\\W" ) );
        }
    
    public void testDigitEscapes()
        {
        assertEquals( new AnyOf( "0123456789" ), element( "\\d" ) );
        assertEquals( new NoneOf( "0123456789" ), element( "\\D" ) );
        }
    
    public void testWhitespaceEscapes()
        {
        assertEquals( Text.create( "\n" ), element( "\\n" ) );
        assertEquals( Text.create( "\t" ), element( "\\t" ) );
        assertEquals( Text.create( "\f" ), element( "\\f" ) );
        assertEquals( Text.create( "\r" ), element( "\\r" ) );
        assertEquals( new AnyOf( " \r\n\t\f"), element( "\\s" ) );
        assertEquals( new NoneOf( " \r\n\t\f" ), element( "\\S" ) );
        }
    
    public void testHexEscapes()
        {
        assertParse( Text.create( "\u00ac" ), "\\xac" );
        assertParse( Text.create( "\u00ff" ), "\\xff" );
        assertParse( Text.create( "\u0012" ), "\\x12" );
        assertParse( Text.create( "\u00af" ), "\\xAF" );
        }
    
    public void testControlEscapes()
        {
        assertParse( Text.create( "\u0001" ), "\\cA" );
        assertParse( Text.create( "\u001a" ), "\\cZ" );
        }
    
    public void testNoQuantifier()
        {
        RegexpTree d = RegexpTree.ANY;
        assertSame( d, quantifier( "", d ) );
        assertSame( d, quantifier( "x", d ) );
        assertSame( d, quantifier( "[", d ) );
        assertSame( d, quantifier( "(", d ) );
        assertSame( d, quantifier( ".", d ) );
        assertSame( d, quantifier( "\\", d ) );
        }
    
    public void testStarQuantifier()
        {
        RegexpTree d = RegexpTree.EOL;
        assertEquals( new ZeroOrMore( d ), quantifier( "*", d ) );
        }
    
    public void testPlusQuantifier()
        {
        RegexpTree d = RegexpTree.SOL;
        assertEquals( new OneOrMore( d ), quantifier( "+", d ) );
        }

    public void testQueryQuantifier()
        {
        RegexpTree d = RegexpTree.ANY;
        assertEquals( new Optional( d ), quantifier( "?", d ) );
        }
    
    public void testUnboundQuantifiers()
        { testUnboundQuantifier( "*" );
        testUnboundQuantifier( "+" );
        testUnboundQuantifier( "?" );
        testUnboundQuantifier( "{" ); }

    /**
    	check that the quantifier string <code>q</code>throws a syntax error if it's
        not preceeded by an atom.
    */
    private void testUnboundQuantifier( String q )
        { PerlPatternParser p = new PerlPatternParser( q ); 
        try { p.parseElement(); fail( "must trap unbound quantifier " + q ); }
        catch (PerlPatternParser.SyntaxException e) { pass(); } }

    public void testUnitSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "x" );
        assertEquals( Text.create( "x" ), p.parseSeq() );
        }
    
    public void testSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "^.$" );
        assertEquals( seq3( new StartOfLine(), new AnySingle(), new EndOfLine() ), p.parseSeq() );
        }
    
    public void testBracketConstruction()
        { assertParse( new Paren( Text.create( "x" ) ), "(x)" ); }
    
    public void testBracketClosure()
        {
        PerlPatternParser p = new PerlPatternParser( "()y" );
        assertEquals( seq2( new Paren( new Nothing() ), Text.create( "y" ) ), p.parseAlts() );
        }
    
    public void testDetectsMissingClosingBracket()
        {
        PerlPatternParser p = new PerlPatternParser( "(x" );
        try { p.parseAlts(); fail( "should detect missing close bracket" ); }
        catch (PerlPatternParser.SyntaxException e) { pass(); }
        }
    
    public void testAlt()
        {
        PerlPatternParser L = new PerlPatternParser( "abc" );
        PerlPatternParser R = new PerlPatternParser( "def" );
        PerlPatternParser p = new PerlPatternParser( "abc|def" );
        assertEquals( alt( L.parseSeq(), R.parseSeq() ), p.parseAlts() );
        }

    public void testSimpleClass()
        { assertParse( new AnyOf( "x1B" ), "[x1B]" ); }
    
    public void testSimpleClassNegated()
        { assertParse( new NoneOf( "b0#" ), "[^b0#]" ); }
    
    public void testClassRangeAlphabet()
        { assertParse( new AnyOf( "ABCDEFGHIJKLMNOPQRSTUVWXYZ" ), "[A-Z]" ); }

    public void testClassRangeSomeLetters()
        { assertParse( new AnyOf( "abcdef" ), "[a-f]" ); }
    
    public void testClassRangeDigits()
        { assertParse( new AnyOf( "abc0123456789rst" ), "[a-c0-9r-t]" ); }

    public void testClassHats()
        { assertParse( new AnyOf( "ab^cd" ), "[ab^cd]" ); }
    
    public void testClassRange()
        { assertParse( new AnyOf( "-R" ), "[-R]" ); }
    
    public void testClassBackslash()
        { assertParse( new AnyOf( "]" ), "[\\]]" ); }
    
    public void testBackReference()
        { assertParse( seq2( new Paren( Text.create( "x" ) ), new BackReference( 1 ) ), "(x)\\1" ); }

    public void testOctalNonBackReference()
        { assertParse( seq2( new Paren( Text.create( "x" ) ), Text.create( "\10" ) ), "(x)\\10" ); }
    
    protected RegexpTree seq2( RegexpTree a, RegexpTree b )
        {
        return Sequence.create( Arrays.asList( new RegexpTree[] {a, b} ) );
        }
        
    protected RegexpTree seq3( RegexpTree a, RegexpTree b, RegexpTree c )
        {
        return Sequence.create( Arrays.asList( new RegexpTree[] {a, b, c} ) );
        }
    
    protected RegexpTree alt( RegexpTree L, RegexpTree R )
        { return Alternatives.create( Arrays.asList( new RegexpTree[] {L, R} ) ); }
    
    public void testPerlParse()
        {
        assertInstanceOf( Alternatives.class, PerlPatternParser.parse( "this is|a pattern" ) );
        assertInstanceOf( Alternatives.class, PerlPatternParser.parse( "this is|a pattern", new SimpleGenerator() ) );
        }
    
    public void testOldSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "hello" );
        assertEquals( Text.create( "h" ), p.parseAtom() );
        assertEquals( 1, p.getPointer() );
        assertEquals( Text.create( "e" ), p.parseAtom() );
        assertEquals( 2, p.getPointer() );
        assertEquals( Text.create( "l" ), p.parseAtom() );
        assertEquals( 3, p.getPointer() );
        assertEquals( Text.create( "l" ), p.parseAtom() );
        assertEquals( 4, p.getPointer() );
        assertEquals( Text.create( "o" ), p.parseAtom() );
        assertEquals( 5, p.getPointer() );
        assertEquals( new Nothing(), p.parseAtom() );
        }
    
    public void assertParse( RegexpTree wanted, String toParse )
        { assertEquals( wanted, new PerlPatternParser( toParse ).parseAlts() ); }
    
    public void testSimpleSpecialAtom( Object wanted, String toParse )
        {
        PerlPatternParser p = new PerlPatternParser( toParse );
        assertEquals( wanted, p.parseAtom() );
        assertEquals( 1, p.getPointer() );
        }
    
    protected RegexpTree quantifier( String toParse, RegexpTree x )
        { return new PerlPatternParser( toParse ).parseQuantifier( x ); }
    
    protected RegexpTree element( String toParse )
        { return new PerlPatternParser( toParse ).parseElement(); }
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