/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.tokens;

import static com.hp.hpl.jena.riot.RiotChars.*;


import java.io.IOException ;
import java.util.NoSuchElementException;

import org.openjena.atlas.io.IO ;
import org.openjena.atlas.io.PeekReader ;

import com.hp.hpl.jena.riot.ParseException;


/** Tokenizer for all sorts of things RDF-ish */

public final class TokenizerText implements Tokenizer
{
    // TODO Various allow/deny options
    
    // RDF mode:
    //   Prefixes - yes and no.
    //   IRIs
    //   BNodes
    
    // Space for CURIEs, stricter Turtle QNames, sane Turtle (i.e. leading digits in local part).
    public static final int CTRL_CHAR = CH_STAR ;
    
    public static boolean Checking = false ;

    private Token token = null ; 
    private final StringBuilder stringBuilder = new StringBuilder(200) ;
    private final PeekReader reader ;
    private boolean finished = false ;
    private TokenChecker checker = null ; // new CheckerBase()  ;
    
    /*package*/ TokenizerText(PeekReader reader)
    {
        this.reader = reader ;
    }
    
    //@Override
    public final boolean hasNext()
    {
        if ( finished )
            return false ;
        if ( token != null )
            return true ;
        skip() ;
        if (reader.eof())
        {
            //close() ;
            finished = true ;
            return false ;
        }
        token = parseToken() ;
        if ( token == null )
        {
            //close() ;
            finished = true ;
            return false ;
        }
        return true ;
    }    
    
    
    //@Override
    public final Token next()
    {
        if ( ! hasNext() )
            throw new NoSuchElementException() ;
        Token t = token ;
        token = null ;
        return t ;
    }
    
    //@Override
    public void remove()
    { throw new UnsupportedOperationException() ; }

    public TokenChecker getChecker() { return checker ; }
    public void setChecker(TokenChecker checker) { this.checker = checker ; }

    //@Override
    public void close()
    { 
        try { reader.close() ; }
        catch (IOException ex) { IO.exception(ex) ; }
    }


    
    // ---- Machinary
    
    private void skip()
    {
        int ch = EOF ;
        for ( ;; )
        {
            if ( reader.eof() )
                return ;
    
            ch = reader.peekChar() ;
            if ( ch == CH_HASH )
            {
                reader.readChar() ;
                // Comment.  Skip to NL
                for ( ;; )
                {
                    ch = reader.peekChar() ;
                    if ( ch == EOF || isNewlineChar(ch) )
                        break ;
                    reader.readChar() ;
                }
            }
            
            // Including excess newline chars from comment.
            if ( ! isWhitespace(ch) )
                break ;
            reader.readChar() ;
        }
    }

    private Token parseToken()
    {
        token = new Token(getLine(), getColumn()) ;
        
        int ch = reader.peekChar() ;

        // ---- IRI
        if ( ch == CH_LT )
        {
            reader.readChar() ;
            token.setImage(allBetween(CH_LT, CH_GT, false, false)) ;
            token.setType(TokenType.IRI) ;
            if ( Checking ) checkURI(token.getImage()) ;
            return token ;
        }

        // ---- Literal
        if ( ch == CH_QUOTE1 || ch == CH_QUOTE2 )
        {
            reader.readChar() ;
            int ch2 = reader.peekChar() ;
            if (ch2 == ch )
            {
                reader.readChar() ; // Read potential second quote.
                int ch3 = reader.peekChar() ;
                if ( ch3 == ch )
                {
                    reader.readChar() ;
                    token.setImage(readLongString(ch, false)) ;
                    TokenType tt = (ch == CH_QUOTE1) ? TokenType.LONG_STRING1 : TokenType.LONG_STRING2 ;
                    token.setType(tt) ;
                }
                else
                {
                    // Two quotes then a non-quote.
                    // Must be '' or ""
                    // No need to pushback characters as we know the lexical form is the empty string.
                    //if ( ch2 != EOF ) reader.pushbackChar(ch2) ;
                    //if ( ch1 != EOF ) reader.pushbackChar(ch1) ;    // Must be '' or ""
                    token.setImage("") ;
                    token.setType( (ch == CH_QUOTE1) ? TokenType.STRING1 : TokenType.STRING2 ) ;
                }
            }
            else
            {
                // Single quote character.
                token.setImage(allBetween(ch, ch, true, false)) ;
                // Single quoted string.
                token.setType( (ch == CH_QUOTE1) ? TokenType.STRING1 : TokenType.STRING2 ) ;
            }
            
            // Literal.  Is it @ or ^^
            if ( reader.peekChar() == CH_AT )
            {
                reader.readChar() ;
                token.setImage2(langTag()) ;
                token.setType(TokenType.LITERAL_LANG) ;
                if ( Checking ) checkLiteralLang(token.getImage(), token.getImage2()) ;
            }
            else if ( reader.peekChar() == '^' )
            {
                expect("^^") ;
                
                // Recursive call!
                // Check no whitespace.
                int nextCh = reader.peekChar() ;
                if ( isWhitespace(nextCh) )
                    exception("No whitespace after ^^ in literal with datatype") ;
                if ( nextCh != '<' && ! isAlpha(nextCh) )
                    exception("Datatype URI required after ^^ - URI or prefixed name expected") ;

                // Stash current token.
                Token mainToken = token ;
                Token subToken = parseToken() ;
                token = mainToken ;
                token.setSubToken(subToken) ;
                token.setType(TokenType.LITERAL_DT) ;
                if ( Checking ) checkLiteralDT(token.getImage(), subToken) ;
            }
            else
            {
                // Was a simple string.
                if ( Checking ) checkString(token.getImage()) ;
            }
            return token ;
        }

        if ( ch == CH_UNDERSCORE )        // Blank node :label must be at least one char
        {
            expect("_:") ;
            token.setImage(readBlankNodeLabel()) ;
            token.setType(TokenType.BNODE) ;
            if ( Checking ) checkBlankNode(token.getImage()) ;
            return token ;
        }

        // Control
        if ( ch == CTRL_CHAR )
        {
            reader.readChar() ;
            token.setType(TokenType.CNTRL) ;
            ch = reader.readChar() ;
            if ( ch == EOF )
                exception("EOF found after "+CTRL_CHAR) ;
            token.cntrlCode = (char)ch ;
            if ( Checking ) checkControl(token.cntrlCode) ;
            return token ;
        }

        if ( ch == CH_AT )
        {
            reader.readChar() ;
            token.setType(TokenType.DIRECTIVE) ;
            token.setImage(readWord(false)) ; 
            if ( Checking ) checkDirective(token.cntrlCode) ;
            return token ;
        }
        
        if ( ch == CH_QMARK )
        {
            reader.readChar() ;
            token.setType(TokenType.VAR) ;
            // Character set?
            token.setImage(readWord(true)) ; 
            if ( Checking ) checkVariable(token.getImage()) ;
            return token ;
        }
        
        // Number?
        switch(ch)
        { 
            // DOT can start a decimal.  Check for digit.
            case CH_DOT:
                reader.readChar() ;
                ch = reader.peekChar() ;
                if ( range(ch, '0', '9') )
                {
                    // Not a DOT after all.
                    reader.pushbackChar(CH_DOT) ;
                    readNumber() ;
                    return token ;
                }
                token.setType(TokenType.DOT) ;
                return token ;
            
            case CH_SEMICOLON:  reader.readChar() ; token.setType(TokenType.SEMICOLON) ; token.setImage(";") ; return token ;
            case CH_COMMA:      reader.readChar() ; token.setType(TokenType.COMMA) ;     token.setImage(",") ; return token ;
            case CH_LBRACE:     reader.readChar() ; token.setType(TokenType.LBRACE) ;    token.setImage("{") ; return token ;
            case CH_RBRACE:     reader.readChar() ; token.setType(TokenType.RBRACE) ;    token.setImage("}") ; return token ;
            case CH_LPAREN:     reader.readChar() ; token.setType(TokenType.LPAREN) ;    token.setImage("(") ; return token ;
            case CH_RPAREN:     reader.readChar() ; token.setType(TokenType.RPAREN) ;    token.setImage(")") ; return token ;
            case CH_LBRACKET:   reader.readChar() ; token.setType(TokenType.LBRACKET) ;  token.setImage("[") ; return token ;
            case CH_RBRACKET:   reader.readChar() ; token.setType(TokenType.RBRACKET) ;  token.setImage("]") ; return token ;
            case CH_EQUALS:     reader.readChar() ; token.setType(TokenType.EQUALS) ;    token.setImage("=") ; return token ;

            // Specials (if processing off) -- FIX ME
            //case CH_COLON:      reader.readChar() ; token.setType(TokenType.COLON) ; return token ;
            case CH_UNDERSCORE: reader.readChar() ; token.setType(TokenType.UNDERSCORE) ; token.setImage("_") ; return token ;
            case CH_LT:         reader.readChar() ; token.setType(TokenType.LT) ; token.setImage("<") ; return token ;
            case CH_GT:         reader.readChar() ; token.setType(TokenType.GT) ; token.setImage(">") ; return token ;
            // TODO
            // GE, LE
            // Single character symbols for * / + -

//            case CH_PLUS:
//            case CH_MINUS:
//            case CH_STAR:
//            case CH_SLASH:
//            case CH_RSLASH:
                
        }
        
        
        if ( ch == CH_PLUS || ch == CH_MINUS || range(ch, '0', '9'))
        {
            readNumber() ;
            if ( Checking ) checkNumber(token.getImage(), token.getImage2() ) ;
            return token ;
        }

        // Plain words and prefixes.
        //   Can't start with a number due to numeric test above.
        //   Can't start with a '_' due to blank node test above.
        // If we see a :, the first time it means a prefixed name else it's a token break.

        readPrefixedNameOrKeyWord(token) ;
        
        if ( Checking ) checkKeyword(token.getImage()) ;
        return token ;
    }

    private void readPrefixedNameOrKeyWord(Token token2)
    {
        long posn = reader.getPosition() ;
        token2.setImage(readWord(false)) ;
        token2.setType(TokenType.KEYWORD) ;
        int ch = reader.peekChar() ;
        if ( ch == CH_COLON )
        {
            reader.readChar() ;
            token2.setType(TokenType.PREFIXED_NAME) ;
            token2.setImage2(readWord(true)) ;
            if ( Checking ) checkPrefixedName(token2.getImage(), token2.getImage2()) ;
        }

        // If we made no progress, nothing found, not even a keyword -- it's an error.
        if ( posn == reader.getPosition() )  
            exception(String.format("Unknown char: %c(%d)",ch,ch)) ;

        if ( Checking ) checkKeyword(token2.getImage()) ;
        
    }
    
    private String readLongString(int quoteChar, boolean endNL)
    {
        stringBuilder.setLength(0) ;
        for ( ;; )
        {
            int ch = reader.readChar() ;
            if ( ch == EOF )
            {
                if ( endNL ) return stringBuilder.toString() ; 
                exception("Broken long string") ;
            }
            
            if ( ch == quoteChar )
            {
                if ( threeQuotes(quoteChar) )
                    return stringBuilder.toString() ;
            }
            
            if ( ch == '\\' )
                ch = readLiteralEscape() ;
            insertCodepoint(stringBuilder, ch) ;
        }
    }

    // Need "readCharOrEscape"
    
    // Assume have read the first quote char.
    // On return:
    //   If false, have moved over no more characters (due to pushbacks) 
    //   If true, at end of 3 quotes
    private boolean threeQuotes(int ch)
    {
        //reader.readChar() ;         // Read first quote.
        int ch2 = reader.peekChar() ;
        if (ch2 != ch )
        {
            //reader.pushbackChar(ch2) ;
            return false ;
        }
        
        reader.readChar() ;         // Read second quote.
        int ch3 = reader.peekChar() ;
        if ( ch3 != ch )
        {
            //reader.pushbackChar(ch3) ;
            reader.pushbackChar(ch2) ;
            return false ;
        }
            
        // Three quotes.
        reader.readChar() ;         // Read third quote.
        return true ;
    }
    
    private String readWord(boolean leadingDigitAllowed)
    { return readWordSub(leadingDigitAllowed, false) ; }
    
    private String readWordSub(boolean leadingDigitAllowed, boolean leadingSignAllowed)
    {
        stringBuilder.setLength(0) ;
        int idx = 0 ;
        if ( ! leadingDigitAllowed )
        {
            int ch = reader.peekChar() ;
            if ( Character.isDigit(ch) )
                return "" ;
        }
        if ( ! leadingSignAllowed )
        {
            int ch = reader.peekChar() ;
            if ( ch == '-' || ch == '+' )
                return "" ;
        }
        
        for ( ;; idx++ )
        {
            int ch = reader.peekChar() ;
            
            if ( Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '-' )
            {
                reader.readChar() ;
                stringBuilder.append((char)ch) ;
                continue ;
            }
            else
                break ;
            
        }
        // BAD : assumes pushbackChar is infinite.
        // Check is ends in "."
        while ( idx > 0 && stringBuilder.charAt(idx-1) == CH_DOT )
        {
            // Push back the dot.
            reader.pushbackChar(CH_DOT) ;
            stringBuilder.setLength(idx-1) ;
            idx -- ;
        }
        return stringBuilder.toString() ;
    }

    // Make better!
    /*
    [16]    integer         ::=     ('-' | '+') ? [0-9]+
    [17]    double          ::=     ('-' | '+') ? ( [0-9]+ '.' [0-9]* exponent | '.' ([0-9])+ exponent | ([0-9])+ exponent )
                                    0.e0, .0e0, 0e0
    [18]    decimal         ::=     ('-' | '+')? ( [0-9]+ '.' [0-9]* | '.' ([0-9])+ | ([0-9])+ )
                                    0.0 .0
    [19]    exponent        ::=     [eE] ('-' | '+')? [0-9]+
    []      hex             ::=     0x0123456789ABCDEFG
    
    */
    private void readNumber()
    {
        // One entry, definitely a number.
        // Beware of '.' as a (non) decimal.
        /*
        maybeSign()
        digits()
        if dot ==> decimal, digits
        if e   ==> double, maybeSign, digits
        else
            check not "." for decimal.
        */
        boolean isDouble = false ;
        boolean isDecimal = false ;
        stringBuilder.setLength(0) ;
        
        int x = 0 ; // Digits before a dot.
        int ch = reader.peekChar() ;
        if ( ch == '0' )
        {
            x++ ;
            reader.readChar() ;
            stringBuilder.append((char)ch) ;
            ch = reader.peekChar() ;
            if ( ch == 'x' || ch == 'X' )
            {
                reader.readChar() ;
                stringBuilder.append((char)ch) ;
                readHex(reader, stringBuilder) ;
                token.setImage(stringBuilder.toString()) ;
                token.setType(TokenType.HEX) ;
                return ;
            }
        }
        else if ( ch == '-' || ch == '+' )
        {
            readPossibleSign(stringBuilder) ;
        }
        
        
        x += readDigits(stringBuilder) ;
//        if ( x == 0 )
//        {
//            
//        }
        ch = reader.peekChar() ;
        if ( ch == CH_DOT )
        {
            reader.readChar() ;
            stringBuilder.append(CH_DOT) ;
            isDecimal = true ;  // Includes things that will be doubles.
            readDigits(stringBuilder) ;
        }
        
        if ( x == 0 && ! isDecimal )
            // Possible a tokenizer error - should not have entered readNumber in the first place.
            exception("Unrecognized as number") ;
        
        if ( exponent(stringBuilder) )
        {
            isDouble = true ;
            isDecimal = false ;
            
        }
        
        token.setImage(stringBuilder.toString()) ;
        if ( isDouble )
            token.setType(TokenType.DOUBLE) ;
        else if ( isDecimal )
            token.setType(TokenType.DECIMAL) ;
        else
            token.setType(TokenType.INTEGER) ;
    }

    
    private static void readHex(PeekReader reader, StringBuilder sb)
    {
        // Just after the 0x, which are in sb
        int x = 0 ;
        for(;;)
        {
            int ch = reader.peekChar() ;

            if ( ! range(ch, '0', '9') && ! range(ch, 'a', 'f') && ! range(ch, 'A', 'F') )
                break ;
            reader.readChar() ;
            sb.append((char)ch) ;
            x++ ;
        }
        if ( x == 0 )
            exception(reader, "No hex characters after "+sb.toString()) ;
    }

    private boolean exponent(StringBuilder sb)
    {
        int ch = reader.peekChar() ;
        if ( ch != 'e' && ch != 'E' )
            return false ;
        reader.readChar() ;
        sb.append((char)ch) ;
        readPossibleSign(sb) ;
        int x = readDigits(sb) ;
        if ( x == 0 )
            exception("Malformed double: "+sb) ;
        return true ;
    }

    private void readPossibleSign(StringBuilder sb)
    {
        int ch = reader.peekChar() ;
        if ( ch == '-' || ch == '+' )
        {
            reader.readChar() ;
            sb.append((char)ch) ;
        }
    }

    private int readDigits(StringBuilder buffer)
    {
        int count = 0 ;
        for(;;)
        {
            int ch = reader.peekChar() ;
            if ( ! range(ch, '0', '9' ) )
                break ;
            reader.readChar() ;
            buffer.append((char)ch) ;
            count ++ ;
        }
        return count ;
    }
    
    private String langTag()
    {
        stringBuilder.setLength(0) ;
        a2z(stringBuilder) ;
        if ( stringBuilder.length() == 0 )
            exception("Bad language tag") ;
        for ( ;; )
        {
            int ch = reader.peekChar() ;
            if ( ch == '-' )
            {
                reader.readChar() ;
                stringBuilder.append('-') ;
                int x = stringBuilder.length();
                a2zN(stringBuilder) ;
                if ( stringBuilder.length() == x )
                    exception("Bad language tag") ;
            }
            else
                break ;
        }
        return stringBuilder.toString();
    }
    
    // ASCII-only e.g. in lang tags.
    private void a2z(StringBuilder sb2)
    {
        for ( ;; )
        {
            int ch = reader.peekChar() ;
            if ( isA2Z(ch) )
            {
                reader.readChar() ;
                stringBuilder.append((char)ch) ;
            }
            else
                return ;
        }
    }
    
    private void a2zN(StringBuilder sb2)
    {
        for ( ;; )
        {
            int ch = reader.peekChar() ;
            if ( isA2ZN(ch) )
            {
                reader.readChar() ;
                stringBuilder.append((char)ch) ;
            }
            else
                return ;
        }
    }

    // Blank node label: letters, numbers and '-'
    private String readBlankNodeLabel()
    {
        stringBuilder.setLength(0) ;
        boolean seen = false ;
        for(;;)
        {
            int ch = reader.peekChar() ;
            if ( ch == EOF )
                break ;
            if ( ! isAlphaNumeric(ch) && ch != '-' )
                break ;
            reader.readChar() ;
            stringBuilder.append((char)ch) ;
            seen = true ;
        }
        if ( ! seen )
            exception("Blank node label missing") ;
        return stringBuilder.toString() ; 
    }

    
    // Get characters between two markers.
    // strEscapes may be processed
    // endNL end of line as an ending is OK
    private String allBetween(int startCh, int endCh,
                              boolean strEscapes, boolean endNL)
    {
        long y = getLine() ;
        long x = getColumn() ;
        stringBuilder.setLength(0) ;

        // Assumes first char read already.
//        int ch0 = reader.readChar() ;
//        if ( ch0 != startCh )
//            exception("Broken parser", y, x) ;

        
        for(;;)
        {
            int ch = reader.readChar() ;
            if ( ch == EOF )
            {
                if ( endNL ) return stringBuilder.toString() ; 
                exception("Broken token: "+stringBuilder.toString(), y, x) ;
            }

            if ( ch == '\n' )
                exception("Broken token (newline): "+stringBuilder.toString(), y, x) ;
            
            if ( ch == endCh )
            {
                //sb.append(((char)ch)) ;
                return stringBuilder.toString() ;
            }
            
            if ( ch == '\\' )
            {
                if ( strEscapes )
                    ch = readLiteralEscape() ;
                else
                    ch = readUnicodeEscape() ;
                // Drop through.
            }
            insertCodepoint(stringBuilder, ch) ;
        }
    }
    
    private void insertCodepoint(StringBuilder buffer, int ch)
    {
        if ( Character.charCount(ch) == 1 )
            buffer.append((char)ch) ;
        else
        {
            // Convert to UTF-16.  Note that the rest of any system this is used
            // in must also respect codepoints and surrogate pairs. 
            if ( ! Character.isDefined(ch) && ! Character.isSupplementaryCodePoint(ch) )
                exception(String.format("Illegal codepoint: 0x%04X", ch)) ;
            char[] chars = Character.toChars(ch) ;
            buffer.append(chars) ;
        }
    }

    public long getColumn()
    {
        return reader.getColNum() ;
    }

    public long getLine()
    {
        return reader.getLineNum() ;
    }

    // ---- Routines to check tokens
    
    private void checkBlankNode(String blankNodeLabel)
    { 
        if ( checker != null ) checker.checkBlankNode(blankNodeLabel) ;
    }

    private void checkLiteralLang(String lexicalForm, String langTag)
    {
       if ( checker != null ) checker.checkLiteralLang(lexicalForm, langTag) ;
    }

    private void checkLiteralDT(String lexicalForm, Token datatype)
    {
       if ( checker != null ) checker.checkLiteralDT(lexicalForm, datatype) ;
    }

    private void checkString(String string)
    {
       if ( checker != null ) checker.checkString(string) ;
    }

    private void checkURI(String uriStr)
    {
       if ( checker != null ) checker.checkURI(uriStr) ;
    }

    private void checkNumber(String image, String datatype)
    {
       if ( checker != null ) checker.checkNumber(image, datatype) ;
    }

    private void checkVariable(String tokenImage)
    {
       if ( checker != null ) checker.checkVariable(tokenImage) ;
    }

    private void checkDirective(int cntrlCode)
    {
       if ( checker != null ) checker.checkDirective(cntrlCode) ;
    }

    private void checkKeyword(String tokenImage)
    {
       if ( checker != null ) checker.checkKeyword(tokenImage) ;
    }

    private void checkPrefixedName(String tokenImage, String tokenImage2)
    {
       if ( checker != null ) checker.checkPrefixedName(tokenImage, tokenImage2) ;
    }

    private void checkControl(int code)
    {
       if ( checker != null ) checker.checkControl(code) ;
    }

    // ---- Escape sequences
    
    private final
    int readLiteralEscape()
    {
        int c = reader.readChar();
        if ( c==EOF )
            exception("Escape sequence not completed") ;

        switch (c)
        {
            case 'n':   return NL ; 
            case 'r':   return CR ;
            case 't':   return '\t' ;
            case '"':   return '"' ;
            case '\'':  return '\'' ;
            case '\\':  return '\\' ;
            case 'u':   return readUnicode4Escape();
            case 'U':   return readUnicode8Escape();
            default:
                exception(String.format("illegal escape sequence value: %c (0x%02X)", c, c));
                return 0 ;
        }
    }
    
    
    private final
    int readUnicodeEscape()
    {
        int ch = reader.readChar() ;
        if ( ch == EOF )
            exception("Broken escape sequence") ;

        switch (ch)
        {
            case 'u': return readUnicode4Escape(); 
            case 'U': return readUnicode8Escape(); 
            default:
                exception(String.format("illegal escape sequence value: %c (0x%02X)", ch, ch));
        }
        return 0 ;
    }
    
    private final
    int readUnicode4Escape() { return readUnicodeEscape(4) ; }
    
    private final
    int readUnicode8Escape()
    {
        int ch8 = readUnicodeEscape(8) ;
        if ( ch8 > Character.MAX_CODE_POINT )
            exception(String.format("illegal code point in \\U sequence value: 0x%08X", ch8));
        return ch8 ;
    }
    
    private final
    int readUnicodeEscape(int N)
    {
        int x = 0 ;
        for ( int i = 0 ; i < N ; i++ )
        {
            int d = readHexChar() ;
            if ( d < 0 )
                return -1 ;
            x = (x<<4)+d ;
        }
        return x ; 
    }
    
    private final
    int readHexChar()
    {
        int ch = reader.readChar() ;
        if ( ch == EOF )
            exception("Not a hexadecimal character (end of file)") ;

        int x =  valHexChar(ch) ;
        if ( x != -1 )
            return x ; 
        exception("Not a hexadecimal character: "+(char)ch) ;
        return -1 ; 
    }
    
    private boolean expect(String str) {
        for (int i = 0; i < str.length(); i++) {
            char want = str.charAt(i);
            if (reader.eof())
            {
                exception("End of input during expected string: "+str) ;
                return false ;
            }
            int inChar = reader.readChar();
            if (inChar != want) {
                //System.err.println("N-triple reader error");
                exception("expected \"" + str + "\"");
                return false;
            }
        }
        return true;
    }

    private void exception(String message)
    {
        exception(message, reader.getLineNum(), reader.getColNum()) ;
    }
    
    private static void exception(PeekReader reader, String message)
    {
        exception(message, reader.getLineNum(), reader.getColNum()) ;
    }

    private static void exception(String message, long line, long col)
    {
        throw new ParseException(message, line, col) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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