/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.out;

import java.io.IOException ;
import java.io.Writer ;
import java.net.MalformedURLException ;

import org.openjena.atlas.io.OutputUtils ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.iri.IRIFactory ;
import com.hp.hpl.jena.iri.IRIRelativize ;
import com.hp.hpl.jena.riot.PrefixMap ;
import com.hp.hpl.jena.riot.Prologue ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.lib.NodeFmtLib ;

/** Utilites for formatter output (N-Triples and Turtle formats) */
public class OutputLangUtils
{
    // Make an object so it can have per-instance flags
    // ASCII vs UTF-8
    // Abbreviate numbers or not.
    // Avoids creating intermediate strings.
    
    private static boolean asciiOnly = true ;

    static public void output(Writer out, Quad quad, Prologue prologue)
    {
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = quad.getGraph() ;
        output(out, s, p, o, g, prologue) ;
    }
    
    static public void output(Writer out, Node s, Node p, Node o, Node g, Prologue prologue)
    {
        output(out, s, prologue) ;
        print(out," ") ;
        output(out, p, prologue) ;
        print(out," ") ;
        output(out, o, prologue) ;
        if ( g != null )
        {
            print(out," ") ;
            output(out, g, prologue) ;
        }
        print(out," .") ;
        println(out) ;
    }
    
    static public void output(Writer out, Triple triple, Node graphNode, Prologue prologue)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        output(out, s, p, o, graphNode, prologue) ;
    }
    
    static public void output(Writer out, Triple triple, Prologue prologue)
    {
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        output(out, s, p, o, null, prologue) ;
    }
    
    static public void output(Writer out, Node node, Prologue prologue)
    {
        // NodeVisitor would be nice but don't want to create an object per static call. 
        
        if ( node.isURI() ) 
        { 
            printIRI(out, node.getURI(), prologue) ;
            return ; 
        }
        if ( node.isBlank() )
        {
            print(out,"_:") ;
            // N-triples is quite restrictive about the labels. [A-Za-z][A-Za-z0-9]*
            // Our format is _:B<encoded>
            String label = node.getBlankNodeLabel() ;
            label = NodeFmtLib.safeBNodeLabel(label) ;
            print(out,label) ;
            return ;
        }
        
        if ( node.isLiteral() )
        {
            // TODO Do Turtle number abbreviates, controlled by a flag.
            // So there are flags ==> make an object
            print(out,'"') ;
            outputEsc(out, node.getLiteralLexicalForm()) ;
            print(out,'"') ;

            if ( node.getLiteralLanguage() != null && node.getLiteralLanguage().length()>0)
            {
                print(out,'@') ;
                print(out,node.getLiteralLanguage()) ;
            }

            if ( node.getLiteralDatatypeURI() != null )
            {
                print(out,"^^") ;
                printIRI(out,node.getLiteralDatatypeURI(), prologue) ;
            }
            return ; 
        }
        if ( node.isVariable() )
        {
            print(out,'?') ;
            print(out, node.getName()) ;
            return ; 
        }
        System.err.println("Illegal node: "+node) ;
    }
    
    private static void printIRI(Writer out, String iriStr, Prologue prologue)
    {
        if ( prologue != null )
        {
            PrefixMap pmap = prologue.getPrefixMap() ;
            if (  pmap != null )
            {
                String pname = prefixFor(iriStr, pmap) ;
                if ( pname != null )
                {
                    print(out,pname) ;
                    return ;
                }
            }
            String base = prologue.getBaseURI() ; 
            if ( base != null )
            {
                String x = abbrevByBase(iriStr, base) ;
                if ( x != null )
                    iriStr = x ;
                // And drop through.
            }
        }
        
        print(out,"<") ;
        // IRIs can have non-ASCII characters.
        if ( asciiOnly )
            outputEsc(out, iriStr) ;
        else
            print(out,iriStr) ;
        print(out,">") ;
    }

    private static String prefixFor(String uri, PrefixMap mapping)
    {
        if ( mapping == null ) return null ;

        String pname = mapping.abbreviate(uri) ;
        if ( pname != null ) // Assume only validperfixes in the map else ... && checkValidPrefixName(pname) )
            return pname ;
        return null ;
    }

    static private int relFlags = IRIRelativize.SAMEDOCUMENT | IRIRelativize.CHILD ;
    static public String abbrevByBase(String uri, String base)
    {
        if ( base == null )
            return null ;
        IRI baseIRI = IRIFactory.jenaImplementation().construct(base) ;
        IRI rel = baseIRI.relativize(uri, relFlags) ;
        String r = null ;
        try { r = rel.toASCIIString() ; }
        catch (MalformedURLException  ex) { r = rel.toString() ; }
        return r ;
    }
    
  

    private static void print(Writer out, String s)
    {
        try { out.append(s) ; } catch (IOException ex) {}
    }

    private static void print(Writer out, char ch)
    {
        try { out.append(ch) ; } catch (IOException ex) {}
    }

    private static void println(Writer out)
    {
        try { out.append("\n") ; } catch (IOException ex) {}
    }

    
    static boolean applyUnicodeEscapes = true ;
    
//    static private void writeString(String s, PrintWriter writer) {
//
//        for (int i = 0; i < s.length(); i++) {
//            char c = s.charAt(i);
//            if (c == '\\' || c == '"') {
//                writer.print('\\');
//                writer.print(c);
//            } else if (c == '\n') {
//                writer.print("\\n");
//            } else if (c == '\r') {
//                writer.print("\\r");
//            } else if (c == '\t') {
//                writer.print("\\t");
//            } else if (c >= 32 && c < 127) {
//                writer.print(c);
//            } else {
//                String hexstr = Integer.toHexString(c).toUpperCase();
//                int pad = 4 - hexstr.length();
//                writer.print("\\u");
//                for (; pad > 0; pad--)
//                    writer.print("0");
//                writer.print(hexstr);
//            }
//        }
//    }
    
    
    static public void outputEsc(Writer out, String s)
    {
        int len = s.length() ;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            
            // Escape escapes and quotes
            if (c == '\\' || c == '"' ) 
            {
                print(out,'\\') ;
                print(out,c) ;
            }
            else if (c == '\n') print(out,"\\n");
            else if (c == '\t') print(out,"\\t");
            else if (c == '\r') print(out,"\\r");
            else if (c == '\f') print(out,"\\f");
            else if ( c >= 32 && c < 127 )
                print(out,c);
            else if ( !asciiOnly )
                print(out,c);
            else
            {
                // Outside the charset range.
                // Does not cover beyond 16 bits codepoints directly
                // (i.e. \U escapes) but Java keeps these as surrogate
                // pairs and will print as characters
                print(out, "\\u") ;
                OutputUtils.printHex(out, c, 4) ;
            }
        }
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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