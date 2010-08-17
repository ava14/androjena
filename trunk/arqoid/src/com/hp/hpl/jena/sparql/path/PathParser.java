/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.io.Reader;
import java.io.StringReader;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.lang.arq.ARQParser;
import com.hp.hpl.jena.sparql.util.ALog;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryParseException;

public class PathParser
{
    public static Path parse(String str, PrefixMapping pmap)
    { return parse(str, new Prologue(pmap)) ; }
    
    public static Path parse(String str, Prologue prologue)
    {
        Query query = new Query(prologue) ;
        Reader in = new StringReader(str) ;
        ARQParser parser = new ARQParser(in) ;

        try {
            query.setStrict(true) ;
            parser.setQuery(query) ;
            return parser.PathUnit() ;
        } catch (com.hp.hpl.jena.sparql.lang.arq.ParseException ex)
        {
            throw new QueryParseException(ex.getMessage(),
                                          ex.currentToken.beginLine,
                                          ex.currentToken.beginColumn
                                          ) ; }
        catch (com.hp.hpl.jena.sparql.lang.arq.TokenMgrError tErr)
        {
            // Last valid token : not the same as token error message - but this should not happen
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new QueryParseException(tErr.getMessage(), line, col) ;
        }
        catch (QueryException ex) { throw ex ; }
        catch (JenaException ex)  { throw new QueryException(ex.getMessage(), ex) ; }
        catch (Error err)
        {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1) ;
        }
        catch (Throwable th)
        {
            ALog.warn(PathParser.class, "Unexpected throwable: ",th) ;
            throw new QueryException(th.getMessage(), th) ;
        }
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