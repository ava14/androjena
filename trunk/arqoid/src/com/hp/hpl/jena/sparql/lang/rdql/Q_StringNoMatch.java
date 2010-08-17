/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.rdql;

import com.hp.hpl.jena.graph.query.IndexValues ;
import com.hp.hpl.jena.graph.query.Expression ; 
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

import java.util.regex.*;

public class Q_StringNoMatch extends ExprNodeRDQL implements ExprRDQL, ExprBoolean
{
    ExprRDQL left ;
    ExprRDQL right ;
    Q_PatternLiteral regex = null ;
    
    // Cache the compiled regular expression.
    
    private String printName = "strNoMatch" ;
    private String opSymbol = "!~" ;
    Pattern pattern = null ;
    
    Q_StringNoMatch(int id)
    { super(id); }
    
    Q_StringNoMatch(RDQLParser p, int id)
    { super(p, id); }
    
    
    public RDQL_NodeValue evalRDQL(Query q, IndexValues env)
    {
        // There is a decision here : do we allow anything to be
        // tested as string or do restrict ourselves to things
        // that started as strings.  Example: A URI is not string
        // so should be it be possible to have:
        //      ?x ne <uri>
        // Decision here is to allow string tests on anything.
        
        RDQL_NodeValue x = left.evalRDQL(q, env) ;
        //NodeValue y = right.eval(q, env) ;    // Must be a pattern literal
        
        // Allow anything to be forced to be a string.
        
        String xx = x.valueString() ;
        // Had better be the pattern string!
        //String yy = y.toString() ;
        
        // Actually do it!
        boolean b = pattern.matcher(xx).find() ;
        NodeValueSettable result = new WorkingVar() ;
        result.setBoolean(!b) ;
        return result ;
    }
    
    // -----------
    // graph.query.Expression

    @Override
    public boolean isApply()         { return true ; }
    @Override
    public String getFun()           { return super.constructURI(this.getClass().getName()) ; }
    @Override
    public int argCount()            { return 2; }
    @Override
    public Expression getArg(int i)  
    {
        if ( i == 0 && left instanceof Expression )
            return (Expression)left ;
        if ( i == 1 && right instanceof Expression )
            return (Expression)right ;
        return null;
    }
    @Override
    public void jjtClose()
    {
        int n = jjtGetNumChildren() ;
        if ( n != 2 )
            throw new QueryException("Q_StringNoMatch: Wrong number of children: "+n) ;
        
        left = (ExprRDQL)jjtGetChild(0) ;
        right = (ExprRDQL)jjtGetChild(1) ;    // Must be a pattern literal
        if ( ! ( right instanceof Q_PatternLiteral ) )
            throw new RDQLEvalFailureException("Q_StringNoMatch: Pattern error") ;
        
        regex = (Q_PatternLiteral)right ;
        
        try
        {
            pattern = Pattern.compile(regex.patternString, regex.mask) ;
        } catch (PatternSyntaxException pEx)
        {
            throw new RDQLEvalFailureException("Q_StringMatch: Pattern exception: "+pEx) ;
        }
    }
    
    public String asInfixString()
    {
        return RDQLQueryPrintUtils.asInfixString2(left, right, printName, opSymbol) ;
    }
    
    public String asPrefixString()
    {
        return RDQLQueryPrintUtils.asPrefixString(left, right, printName, opSymbol) ;
    }
    
    @Override
    public void format(IndentedWriter w)
    {
        RDQLQueryPrintUtils.format(w, left, right, printName, opSymbol) ;
    }
    
    @Override
    public String toString()
    {
        return asInfixString() ;
    }
}
/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
