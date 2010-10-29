/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import org.openjena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadPattern ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;

/** Execution logging for query processing on a per query basis.
 * This class provides an overlay on top of the system logging to provide
 * control of log message down to a per query basis. The associated logging channel
 * must also be enabled.  
 * 
 * An execution can detail the query, the algebra and every point at which the dataset is touched.
 *  
 *  Caution: logging can be a significant cost for small queries
 *  because of disk or console output overhead.
 *  
 *  @see TDB#logExec
 *  @see TDB#setExecutionLogging
 */

public class Explain
{
    // Logging: TRACE < DEBUG < INFO < WARN < ERROR < FATAL
    /* Design:
     * Logger level: always INFO?
     * Per query: SYSTEM > EXEC (Query) > DETAIL (Algebra) > DEBUG (every BGP)
     * 
     * Control:
     *   tdb:logExec = true (all), or enum
     * 
Document:
  Include setting different loggers etc for log4j.
     */

    // Need per-query identifier.
    
    // These are the per-execution levels.
    
    /** Information level for query execution. */
    public static enum InfoLevel
    {
        /** Log each query */
        INFO { @Override public int level() { return 1 ; } } ,
        /** Log each query and it's algebra form after optimization */
        FINE { @Override public int level() { return 2 ; } } ,
        /** Log query, algebra and every database access (can be expensive) */
        ALL { @Override public int level() { return 3 ; } } ,
        /** No query execution logging. */
        NONE { @Override public int level() { return -1 ; } }
        ;
        
        abstract public int level() ;
    }

    // CHANGE ME.
    static public final Logger logExec = TDB.logExec ;
    static public final Logger logInfo = TDB.logInfo ;
    
    // MOVE ME to ARQConstants
    public static final Symbol symLogExec = TDB.symLogExec ; //ARQConstants.allocSymbol("logExec") ;
    
    // ---- Query
    
    public static void explain(Query query, Context context)
    {
        explain("Query", query, context) ;
    }
    
    public static void explain(String message, Query query, Context context)
    {
        if ( explaining(InfoLevel.INFO, logExec, context) )
        {
            // One line.
            // Careful - currently ARQ version needed
            IndentedLineBuffer iBuff = new IndentedLineBuffer() ;
            //iBuff.getIndentedWriter().setFlatMode(true) ;
            query.serialize(iBuff) ;
            String x = iBuff.asString() ;
            
            _explain(logExec, message, x, true) ;
        }
    }
    
    // ---- Algebra
    
    public static void explain(Op op, Context context)
    {
        explain("Algebra", op, context) ;
    }
    
    public static void explain(String message, Op op, Context context)
    {
        if ( explaining(InfoLevel.FINE, logExec, context) )
            _explain(logExec, message, op.toString(), true) ;
    }
    
    // ---- BGP and quads
    
    public static void explain(BasicPattern bgp, Context context)
    {
        explain("BGP", bgp, context) ; 
    }
    
    public static void explain(String message, BasicPattern bgp, Context context)
    {
        if ( explaining(InfoLevel.ALL, logExec,context) )
            _explain(logExec, message, bgp.toString(), false) ;
    }
    
    public static void explain(String message, QuadPattern quads, Context context)
    {
        if ( explaining(InfoLevel.ALL, logExec,context) )
        {
            String str = formatQuads(quads) ;
            _explain(logExec, message, str, false) ;
        }
    }

    // TEMP : quad list that looks right.
    // Renove when QuadPatterns roll through from ARQ.
    
    private static String formatQuads(QuadPattern quads)
    {
        IndentedLineBuffer out = new IndentedLineBuffer() ;

        SerializationContext sCxt = SSE.sCxt((SSE.defaultPrefixMapWrite)) ;

        boolean first = true ;
        for ( Quad qp : quads )
        {
            if ( !first )
                out.print(" ") ;
            else
                first = false ;
            // Adds (triple ...)
            // SSE.write(buff.getIndentedWriter(), t) ;
            out.print("(") ;
            WriterNode.output(out, qp.getGraph(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getSubject(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getPredicate(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getObject(), sCxt) ;
            out.print(")") ;
        }
        out.flush();
        return out.toString() ;
    }    
    // ----
    
    private static void _explain(Logger logger, String reason, String explanation, boolean newlineAlways)
    {
        while ( explanation.endsWith("\n") || explanation.endsWith("\r") )
            explanation = StrUtils.chop(explanation) ;
        if ( newlineAlways || explanation.contains("\n") )
            explanation = reason+"\n"+explanation ;
        else
            explanation = reason+" :: "+explanation ;
        _explain(logger, explanation) ;
        //System.out.println(explanation) ;
    }
    
    private static void _explain(Logger logger, String explanation)
    {
        logger.info(explanation) ;
    }

    // General information
    public static void explain(Context context, String message)
    {
        if ( explaining(InfoLevel.INFO, logInfo, context) )
            _explain(logInfo, message) ;
    }

    public static void explain(Context context, String format, Object... args)
    {
        if ( explaining(InfoLevel.INFO, logInfo, context) )
        {
            // Caveat: String.format is not cheap.
            String str = String.format(format, args) ;
            _explain(logInfo, str) ;
        }
    }

    public static boolean explaining(InfoLevel level, Context context)
    {
        return explaining(level, logInfo, context) ;
    }
    

    
    public static boolean explaining(InfoLevel level, Logger logger, Context context)
    {
        if ( ! _explaining(level, context) ) return false ;
        return logger.isInfoEnabled() ;
    }
    
    private static boolean _explaining(InfoLevel level, Context context)
    {
        if ( level == InfoLevel.NONE ) return false ;
        
        Object x = context.get(symLogExec, null) ;
        
        if ( x == null )
            return false ;
        
        // Enum level.
        if ( level.level() == InfoLevel.NONE.level() ) return false ;

        if ( x instanceof InfoLevel )
        {
            InfoLevel z = (InfoLevel)x ;
            if ( z == InfoLevel.NONE ) return false ;
            return ( z.level() >= level.level() ) ;
        }
        
        if ( x instanceof String )
        {
            String s = (String)x ;
            
            if ( s.equalsIgnoreCase("info") )
                return level.equals(InfoLevel.INFO) ;
            if ( s.equalsIgnoreCase("fine") ) 
                return level.equals(InfoLevel.FINE) || level.equals(InfoLevel.INFO) ;
            if ( s.equalsIgnoreCase("all") )
                // All levels.
                return true ;
            // Backwards compatibility.
            if ( s.equalsIgnoreCase("true") ) 
                return true ;
            if ( s.equalsIgnoreCase("none") ) 
                return false ;

        }
        
        return Boolean.TRUE.equals(x) ;
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