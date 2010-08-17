/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.n3.JenaURIException;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.NodeConst;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Exists;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.path.P_Link;
import com.hp.hpl.jena.sparql.path.Path;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TripleCollector;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.sparql.util.LabelToNodeMap;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
import com.hp.hpl.jena.vocabulary.RDF;

/** Base class for RDF related parsers */ 
public class ParserBase
{
    // NodeConst
    protected final Node XSD_TRUE       = NodeConst.nodeTrue ;
    protected final Node XSD_FALSE      = NodeConst.nodeFalse ; 
    
    protected final Node nRDFtype       = NodeConst.nodeRDFType ;
    
    protected final Node nRDFnil        = NodeConst.nodeNil ;
    protected final Node nRDFfirst      = NodeConst.nodeFirst ;
    protected final Node nRDFrest       = NodeConst.nodeRest ;
    
    protected final Node nRDFsubject    = RDF.Nodes.subject ;
    protected final Node nRDFpredicate  = RDF.Nodes.predicate ;
    protected final Node nRDFobject     = RDF.Nodes.object ;
    
    // ----
    protected boolean inConstructTemplate = false ;
    
    // label => bNode for construct templates patterns
    final LabelToNodeMap bNodeLabels = LabelToNodeMap.createBNodeMap() ;
    
    // label => bNode (as variable) for graph patterns
    final LabelToNodeMap anonVarLabels = LabelToNodeMap.createVarMap() ;
    
    // This is the map used allocate blank node labels during parsing.
    // 1/ It is different between CONSTRUCT and the query pattern
    // 2/ Each BasicGraphPattern is a scope for blank node labels so each
    //    BGP causes the map to be cleared at the start of the BGP
    
    LabelToNodeMap activeLabelMap = anonVarLabels ;
    Set<String> oldLabels = new HashSet<String>() ; 
    
    //LabelToNodeMap listLabelMap = new LabelToNodeMap(true, new VarAlloc("L")) ;
    // ----
    
    public ParserBase() {}
    
    protected Prologue prologue ;
    public void setPrologue(Prologue prologue) { this.prologue = prologue ; }
    public Prologue getPrologue() { return prologue ; }
    
    protected void setInConstructTemplate(boolean b)
    {
        inConstructTemplate = b ;
        if ( inConstructTemplate )
            activeLabelMap = bNodeLabels ;
        else 
            activeLabelMap = anonVarLabels ;
    }
    
    protected Element compressGroupOfOneGroup(ElementGroup elg)
    {
        // remove group of one group.
        if ( elg.getElements().size() == 1 )
        {
            Element e1 = elg.getElements().get(0) ;
            if ( e1 instanceof ElementGroup )
                return e1 ;
        }
        return elg ;
    }
    
    protected Node createLiteralInteger(String lexicalForm)
    {
        return Node.createLiteral(lexicalForm, null, XSDDatatype.XSDinteger) ;
    }
    
    protected Node createLiteralDouble(String lexicalForm)
    {
        return Node.createLiteral(lexicalForm, null, XSDDatatype.XSDdouble) ;
    }
    
    protected Node createLiteralDecimal(String lexicalForm)
    {
        return Node.createLiteral(lexicalForm, null, XSDDatatype.XSDdecimal) ;
    }

    protected Node stripSign(Node node)
    {
        if ( ! node.isLiteral() ) return node ;
        String lex = node.getLiteralLexicalForm() ;
        String lang = node.getLiteralLanguage() ;
        RDFDatatype dt = node.getLiteralDatatype() ;
        
        if ( ! lex.startsWith("-") && ! lex.startsWith("+") )
            throw new ARQInternalErrorException("Literal does not start with a sign: "+lex) ;
        
        lex = lex.substring(1) ;
        return Node.createLiteral(lex, lang, dt) ;
    }
    
    protected Node createLiteral(String lexicalForm, String langTag, String datatypeURI)
    {
        Node n = null ;
        // Can't have type and lang tag.
        if ( datatypeURI != null)
        {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI) ;
            n = Node.createLiteral(lexicalForm, null, dType) ;
        }
        else
            n = Node.createLiteral(lexicalForm, langTag, null) ;
        return n ;
    }
    
    protected long integerValue(String s)
    {
        if ( s.startsWith("+") )
            s = s.substring(1) ;
        if ( s.startsWith("0x") )
        {
            // Hex
            s = s.substring(2) ;
            return Long.parseLong(s, 16) ;
        }
        return Long.parseLong(s) ;
    }
    
    protected double doubleValue(String s)
    {
        if ( s.startsWith("+") )
            s = s.substring(1) ;
        double valDouble = Double.parseDouble(s) ;
        return valDouble ; 
    }
    
    /** Remove first and last characters (e.g. ' or "") from a string */
    protected static String stripQuotes(String s)
    {
        return s.substring(1,s.length()-1)  ;
    }
    
    /** Remove first 3 and last 3 characters (e.g. ''' or """) from a string */ 
    protected static String stripQuotes3(String s)
    {
        return s.substring(3,s.length()-3)  ;
    }

    /** remove the first n charcacters from the string*/ 
    public static String stripChars(String s, int n)
    {
        return s.substring(n, s.length())  ;
    }
        
    protected Var createVariable(String s, int line, int column)
    {
        s = s.substring(1) ; // Drop the marker
        
        // This is done by the parser input stream nowadays.
        //s = unescapeCodePoint(s, line, column) ;
        // Check \ u did not put in any illegals. 
        return Var.alloc(s) ;
    }

    // ---- IRIs and Nodes
    
    final static String bNodeLabelStart = "_:" ;
    
    boolean skolomizedBNodes = ARQ.isTrue(ARQ.constantBNodeLabels) ;
    
    protected String resolveQuotedIRI(String iriStr ,int line, int column)
    {
        iriStr = stripQuotes(iriStr) ;
        return resolveIRI(iriStr, line, column) ;
    }

    
    protected String resolveIRI(String iriStr ,int line, int column)
    {
        if ( isBNodeIRI(iriStr) )
            return iriStr ;
        
        if ( getPrologue() != null )
        {
            if ( getPrologue().getResolver() != null )
                try {
                    iriStr = getPrologue().getResolver().resolve(iriStr) ;
                } catch (JenaURIException ex)
                { throwParseException(ex.getMessage(), line, column) ; }
        }
        return iriStr ;
    }
    
    protected String resolvePName(String qname, int line, int column)
    {
        String s = getPrologue().expandPrefixedName(qname) ;
        if ( s == null )
            throwParseException("Unresolved prefixed name: "+qname, line, column) ;
        return s ;
    }
    
    protected Node createNode(String iri)
    {
        // Is it a bNode label? i.e. <_:xyz>
        if ( isBNodeIRI(iri) )
        {
            String s = iri.substring(bNodeLabelStart.length()) ;
            Node n = Node.createAnon(new AnonId(s)) ;
            return n ;
        }
        return Node.createURI(iri) ;
    }
    
    protected boolean isBNodeIRI(String iri)
    {
        return skolomizedBNodes && iri.startsWith(bNodeLabelStart) ;
    }
    
    
    // -------- Basic Graph Patterns and Blank Node label scopes
    
    // A BasicGraphPattern is any sequence of TripleBlocks, separated by filters,
    // but not by other graph patterns 
    
    // SPARQL does not have a straight syntactic unit for BGPs - but prefix does. 
    
    protected void startBasicGraphPattern()
    { activeLabelMap.clear() ; }

    protected void endBasicGraphPattern()
    { oldLabels.addAll(activeLabelMap.getLabels()) ; }
    
    protected void startTriplesBlock()
    { }
    
    protected void endTriplesBlock()
    { } 

    // On entry to a new group, the current BGP is ended.
    protected void startGroup(ElementGroup elg)
    { 
        endBasicGraphPattern() ;
        startBasicGraphPattern() ;
    }
    
    protected void endGroup(ElementGroup elg)
    { 
        endBasicGraphPattern() ;
    }
    
    // --------
    
    // BNode from a list
//    protected Node createListNode()
//    { return listLabelMap.allocNode() ; }
    
    protected Node createListNode() { return createBNode() ; }

    // Unlabelled bNode.
    protected Node createBNode() { return activeLabelMap.allocNode() ; }
    
    // Labelled bNode.
    protected Node createBNode(String label, int line, int column)
    { 
        if ( oldLabels.contains(label) )
            throwParseException("Blank node reused across basic graph patterns: "+label,
                                line, column) ;
        
        //label = unescapeCodePoint(label, line, column) ;
        return activeLabelMap.asNode(label) ;
    }
    
    protected Expr createExprExists(Element element)
    {
        return new E_Exists(element) ;
    }
    
    protected Expr createExprNotExists(Element element)
    {
        // Could negate here.
        return new E_NotExists(element) ;
    }
    
    protected String fixupPrefix(String prefix, int line, int column)
    {
        // \ u processing!
        if ( prefix.endsWith(":") )
            prefix = prefix.substring(0, prefix.length()-1) ;
        return prefix ; 
    }
    
    protected void insert(TripleCollector acc, Node s, Node p, Node o)
    {
        acc.addTriple(new Triple(s, p, o)) ;
    }
    
    protected void insert(TripleCollector acc, int index, Node s, Node p, Node o)
    {
        acc.addTriple(index, new Triple(s, p, o)) ;
    }
    
    // insert-with-path is used by both SPARQL and ARQ (extended SPARQL). 
    
    protected void insert(TripleCollector acc, Node s, Node p, Path path, Node o)
    {
        if ( p == null )
            acc.addTriplePath(new TriplePath(s, path, o)) ;
        else
            acc.addTriple(new Triple(s, p, o)) ;
    }
    
    protected void insert(TripleCollector acc, int index, Node s, Node p, Path path, Node o)
    {
        if ( p == null )
            acc.addTriplePath(index, new TriplePath(s, path, o)) ;
        else
            acc.addTriple(index, new Triple(s, p, o)) ;
    }

    static Path fixPredicate(Node p)
    {
        if ( p.isURI() )
            return new P_Link(p) ;
        return null ;
    }
    
    protected Expr asExpr(Node n)
    {
        return ExprUtils.nodeToExpr(n) ;
    }

    protected Expr asExprNoSign(Node n)
    {
        String lex = n.getLiteralLexicalForm() ;
        String lang = n.getLiteralLanguage() ;
        String dtURI = n.getLiteralDatatypeURI() ;
        n = createLiteral(lex, lang, dtURI) ;
        
        return ExprUtils.nodeToExpr(n) ;
    }

    // Utilities to remove escapes
    
    // Testing interface
    
    // SPARQL/Update 
    protected Graph convertTemplateToTriples(Template template, int line, int col)
    {
        List<Triple> acc = new ArrayList<Triple>() ;
        TriplesDataCollector collector = new TriplesDataCollector(acc, line, col) ;
        template.visit(collector) ;
        Graph g = GraphFactory.createPlainGraph() ;
        g.getBulkUpdateHandler().add(acc) ;
        return g ;
    }
    
    public static String unescapeStr(String s)
    { return unescape(s, '\\', false, 1, 1) ; }

//    public static String unescapeCodePoint(String s)
//    { return unescape(s, '\\', true, 1, 1) ; }
//
//    protected String unescapeCodePoint(String s, int line, int column)
//    { return unescape(s, '\\', true, line, column) ; }

    
    public static String unescapeStr(String s, int line, int column)
    { return unescape(s, '\\', false, line, column) ; }
    
    // Worker function
    public static String unescape(String s, char escape, boolean pointCodeOnly, int line, int column)
    {
        int i = s.indexOf(escape) ;
        
        if ( i == -1 )
            return s ;
        
        // Dump the initial part straight into the string buffer
        StringBuffer sb = new StringBuffer(s.substring(0,i)) ;
        
        for ( ; i < s.length() ; i++ )
        {
            char ch = s.charAt(i) ;
            // Keep line and column numbers.
            switch (ch)
            {
                case '\n': 
                case '\r':
                    line++ ;
                    column = 1 ;
                    break ;
                default:
                    column++ ;
                    break ;
            }

            if ( ch != escape )
            {
                sb.append(ch) ;
                continue ;
            }
                
            // Escape
            if ( i >= s.length()-1 )
                throwParseException("Illegal escape at end of string", line, column) ;
            char ch2 = s.charAt(i+1) ;
            column = column+1 ;
            i = i + 1 ;
            
            // \\u and \\U
            if ( ch2 == 'u' )
            {
                // i points to the \ so i+6 is next character
                if ( i+4 >= s.length() )
                    throwParseException("\\u escape too short", line, column) ;
                int x = hex(s, i+1, 4, line, column) ;
                sb.append((char)x) ;
                // Jump 1 2 3 4 -- already skipped \ and u
                i = i+4 ;
                column = column+4 ;
                continue ;
            }
            if ( ch2 == 'U' )
            {
                // i points to the \ so i+6 is next character
                if ( i+8 >= s.length() )
                    throwParseException("\\U escape too short", line, column) ;
                int x = hex(s, i+1, 8, line, column) ;
                // Convert to UTF-16 codepoint pair.
                sb.append((char)x) ;
                // Jump 1 2 3 4 5 6 7 8 -- already skipped \ and u
                i = i+8 ;
                column = column+8 ;
                continue ;
            }
            
            // Are we doing just point code escapes?
            // If so, \X-anything else is legal as a literal "\" and "X" 
            
            if ( pointCodeOnly )
            {
                sb.append('\\') ;
                sb.append(ch2) ;
                i = i + 1 ;
                continue ;
            }
            
            // Not just codepoints.  Must be a legal escape.
            char ch3 = 0 ;
            switch (ch2)
            {
                case 'n': ch3 = '\n' ;  break ; 
                case 't': ch3 = '\t' ;  break ;
                case 'r': ch3 = '\r' ;  break ;
                case 'b': ch3 = '\b' ;  break ;
                case 'f': ch3 = '\f' ;  break ;
                case '\'': ch3 = '\'' ; break ;
                case '\"': ch3 = '\"' ; break ;
                case '\\': ch3 = '\\' ; break ;
                default:
                    throwParseException("Unknown escape: \\"+ch2, line, column) ;
            }
            sb.append(ch3) ;
        }
        return sb.toString() ;
    }

    // Line and column that started the escape
    public static int hex(String s, int i, int len, int line, int column)
    {
//        if ( i+len >= s.length() )
//        {
//            
//        }
        int x = 0 ;
        for ( int j = i ; j < i+len ; j++ )
        {
           char ch = s.charAt(j) ;
           column++ ;
           int k = 0  ;
           switch (ch)
           {
               case '0': k = 0 ; break ; 
               case '1': k = 1 ; break ;
               case '2': k = 2 ; break ;
               case '3': k = 3 ; break ;
               case '4': k = 4 ; break ;
               case '5': k = 5 ; break ;
               case '6': k = 6 ; break ;
               case '7': k = 7 ; break ;
               case '8': k = 8 ; break ;
               case '9': k = 9 ; break ;
               case 'A': case 'a': k = 10 ; break ;
               case 'B': case 'b': k = 11 ; break ;
               case 'C': case 'c': k = 12 ; break ;
               case 'D': case 'd': k = 13 ; break ;
               case 'E': case 'e': k = 14 ; break ;
               case 'F': case 'f': k = 15 ; break ;
               default:
                   throwParseException("Illegal hex escape: "+ch, line, column) ;
           }
           x = (x<<4)+k ;
        }
        return x ;
    }
    
    public static void throwParseException(String msg, int line, int column)
    {
        throw new QueryParseException("Line " + line + ", column " + column + ": " + msg,
                                      line, column) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
