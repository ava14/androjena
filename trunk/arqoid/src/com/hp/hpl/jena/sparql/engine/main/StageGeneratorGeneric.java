/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main;

import static com.hp.hpl.jena.sparql.engine.optimizer.reorder.PatternElements.TERM;

import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriples;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterBlockTriplesQH;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.*;
//ANDROID: removed management dependence
//import com.hp.hpl.jena.sparql.mgt.Explain;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.sparql.util.Utils;

/** Generic - always works - StageGenerator */
public class StageGeneratorGeneric implements StageGenerator
{
    public final static Symbol altMatcher = ARQConstants.allocSymbol("altmatcher") ;
    
    public StageGeneratorGeneric() {}
    
    public QueryIterator execute(BasicPattern pattern, 
                                 QueryIterator input,
                                 ExecutionContext execCxt)
    {
        if ( input == null )
            ALog.fatal(this, "Null input to "+Utils.classShortName(this.getClass())) ;

        Graph graph = execCxt.getActiveGraph() ; 

        // Choose reorder transformation and execution strategy.
        
        final ReorderTransformation reorder ;
        final StageGenerator executor ;
        
        if ( graph instanceof GraphMemFaster )
        {
            reorder = reorderBasicStats(graph) ;
            executor = executeInline ; 
        }
        else if ( graph instanceof GraphRDB )
        {
            reorder = null ;
            executor = executeQueryHandler ;
        }
        else if ( graph instanceof GraphMem )            // Old Graph-in-memory
        {
            reorder = reorderBasicStats(graph) ;
            executor = executeInline ; 
        }
        else
        {
            // When in doubt ... use the general pass-through to graph query handler matcher.
            // Includes union graphs, InfGraphs and other composite or unusual kinds.
            reorder = null ;
            executor = executeInline ;
        }

        return execute(pattern, reorder, executor, input, execCxt) ;
    }

    protected QueryIterator execute(BasicPattern pattern,
                                    ReorderTransformation reorder,
                                    StageGenerator execution, 
                                    QueryIterator input,
                                    ExecutionContext execCxt)
    {
    	//ANDROID: removed management dependence
//        Explain.explain(pattern, execCxt.getContext()) ;
        
        if ( reorder != null )
        {
            pattern = reorder.reorder(pattern) ;
          //ANDROID: removed management dependence
//            Explain.explain("Reorder", pattern, execCxt.getContext()) ;
        }

        return execution.execute(pattern, input, execCxt) ; 
    }
    
    
    // ---- Execution policies
    private static StageGenerator executeQueryHandler = new StageGenerator() {
        //@Override
        public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
        {
            return QueryIterBlockTriplesQH.create(input, pattern, execCxt) ;
        }} ;

    private static StageGenerator executeInline = new StageGenerator() {
        //@Override
        public QueryIterator execute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
        {
                return QueryIterBlockTriples.create(input, pattern, execCxt) ;
        }} ;
        
    // ---- Reorder policies
        
    // Fixed - Variable counting only. 
    private static ReorderTransformation reorderFixed() { return ReorderLib.fixed() ; } 

    // Uses Jena's statistics handler.
    private static ReorderTransformation reorderBasicStats(Graph graph)
    {
        GraphStatisticsHandler stats = graph.getStatisticsHandler() ;
        if ( stats == null )
            return reorderFixed() ;
        return new ReorderStatsHandler(graph, graph.getStatisticsHandler()) ;
    }

    /* Execution - allow the inline matcher to be turned off */ 
    private static QueryIterator baseExecute(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        if ( execCxt.getContext().isTrueOrUndef(altMatcher) )
            return executeInline(pattern, input, execCxt) ;
        else
            return executeQueryHandler(pattern, input, execCxt) ;
    }
    
    /** Use the inline BGP matcher */ 
    public static QueryIterator executeInline(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        return QueryIterBlockTriples.create(input, pattern, execCxt) ;
    }

    /** Use the graph's query handler. */ 
    private static QueryIterator executeQueryHandler(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt)
    {
        return QueryIterBlockTriplesQH.create(input, pattern, execCxt) ;
    }
    
    
    
    /** Reorder a basic graph pattern using a graph statistic handler */
    private static class ReorderStatsHandler extends ReorderTransformationBase
    {
        static ReorderFixed fixed = (ReorderFixed)reorderFixed() ;        // We need our own copy to call into.
        
        // Guesses at the selectivity of fixed, but unknown, values.
        // Choose these for large graphs because bad guesses don't harm small graphs.  
        
        final long TERM_S ;         // Used for S ? ? if no stats
        final long TERM_TYPE ;
        final long TERM_P ;         // Used for ? P ? if no stats
        final long TERM_O ;         // Used for ? ? O if no stats
        final long N ;
        
        private GraphStatisticsHandler stats ;

        ReorderStatsHandler(Graph graph, GraphStatisticsHandler stats)
        {
            this.stats = stats ;
            N = graph.size() ;
            // Note: when these are too badly wrong, the app can supply a statistics file. 
            TERM_S = 10 ;       // Wild guess: "An average subject has 10 properties".
            TERM_P = N/10 ;     // Wild guess: "An average vocabulary has 10 properties"
            TERM_O = 20 ;       // Wild guess: "An average object is in 20 triples".
            TERM_TYPE = N/10 ;  // Wild guess: "An average class has 1/10 of the resources."
        }
        
        @Override
        protected double weight(PatternTriple pt)
        {
            double x = fixed.weight(pt) ;
            // If there are two fixed terms, use the fixed weighting, all of which are quite small.
            // This chooses a less optimal triple but the worse choice is still a very selective choice.
            // One case is IFPs: the multi term choice for PO is not 1. 
            if ( x < ReorderFixed.MultiTermMax )
            {
                // Rescale it from the fixed numbers of  ReorderFixed
                //x = ReorderFixed.MultiTermSampleSize * x / N ;
            }
            else
                x = weight1(pt) ;
            
            //System.out.printf("** %s: --> %s\n", pt, x) ;
            return x ;
        }
        
        
        private double weight1(PatternTriple pt)
        {
            // One or zero fixed terms.
            
            long S = -1 ;
            long P = -1 ;
            long O = -1 ;
            
            // Include guesses for SP, OP, typeClass
            
            if ( pt.subject.isNode() )
                S = stats.getStatistic(pt.subject.getNode(), Node.ANY, Node.ANY) ;
            else if ( TERM.equals(pt.subject) )
                S = TERM_S ;
            
            // rdf:type.
            if ( pt.predicate.isNode() )
                P = stats.getStatistic(Node.ANY, pt.predicate.getNode(), Node.ANY) ;
            else if ( TERM.equals(pt.predicate) )
                P = TERM_P ;
            
            if ( pt.object.isNode() )
                O = stats.getStatistic(Node.ANY, Node.ANY, pt.object.getNode()) ;
            else if ( TERM.equals(pt.object) )
                O = TERM_O ;

            if ( S == 0 || P == 0 || O == 0 )
                // Can't match.
                return 0 ;
            
            // Find min positive
            double x = -1 ;
            if ( S > 0 ) x = S ;
            if ( P > 0 && P < x ) x = P ;
            if ( O > 0 && O < x ) x = O ;
            //System.out.printf("** [%d, %d, %d]\n", S, P ,O) ;

            return x ;
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