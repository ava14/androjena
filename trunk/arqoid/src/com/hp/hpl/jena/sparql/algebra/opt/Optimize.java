/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.opt;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.OpWalker ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformWrapper ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.algebra.OpWalker.WalkerVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transformer.ApplyTransformVisitor ;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel ;
import com.hp.hpl.jena.sparql.algebra.op.OpService ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;

public class Optimize implements Rewrite
{
    static private Logger log = LoggerFactory.getLogger(Optimize.class) ;

    // A small (one slot) registry to allow plugging in an alternative optimizer
    public interface Factory { Rewrite create(Context context) ; }
    
    
    // ----    
    public static Factory noOptimizationFactory = new Factory()
    {
        public Rewrite create(Context context)
        {
            return new Rewrite() {

                public Op rewrite(Op op)
                {
                    return op ;
                }} ;
        }} ;
        
    public static Factory stdOptimizationFactory = new Factory()
    {
        public Rewrite create(Context context)
        {
            return new Optimize(context) ;
        }
    } ;
    
    // Set this to a different factory implementation to have a different general optimizer.  
    private static Factory factory = stdOptimizationFactory ;
    
    // ----        
        
    public static Op optimize(Op op, ExecutionContext execCxt)
    {
        return optimize(op, execCxt.getContext()) ;
    }

    // The execution-independent optimizations
    public static Op optimize(Op op, Context context)
    {
        Rewrite opt = decideOptimizer(context) ;
        return opt.rewrite(op) ;
    }

    /** Set the global optimizer factory to one that does nothing */
    public static void noOptimizer()
    {
        setFactory(noOptimizationFactory) ;
    }

    static private Rewrite decideOptimizer(Context context)
    {
        Factory f = (Factory)context.get(ARQConstants.sysOptimizer) ;
        if ( f == null )
            f = factory ;
        if ( f == null )
            f = stdOptimizationFactory ;    // Only if default 'factory' gets lost.
        return f.create(context) ;
    }

    
    /** Globably set the fcaory for making optimizers */ 
    public static void setFactory(Factory aFactory)
    { factory = aFactory ; }

    /** Get the global factory for making optimizers */ 
    public static Factory getFactory(Factory aFactory)
    { return factory ; }
    
    // ---- The object proper for the standard optimizations
    
    private final Context context ;
    private Optimize(ExecutionContext execCxt)
    {
        this(execCxt.getContext()) ;
    }
    
    private Optimize(Context context)
    {
        this.context = context ;
    }

    /** Alternative name for compatibility only */
    public static final Symbol filterPlacement2 = ARQConstants.allocSymbol("filterPlacement") ;
    
    @SuppressWarnings("deprecation")
    public Op rewrite(Op op)
    {
        if ( context.isDefined(filterPlacement2) ) 
        {
            if ( context.isUndef(ARQ.filterPlacement) )
                context.set(ARQ.filterPlacement, context.get(filterPlacement2)) ;
        }
        
        if ( false )
        {
            // Simplify is always applied by the AlgebraGenerator
            op = apply("Simplify", new TransformSimplify(), op) ;
            op = apply("Delabel", new TransformRemoveLabels(), op) ;
        }

        // Prepare expressions.
        OpWalker.walk(op, new OpVisitorExprPrepare(context)) ;
        
        // Need to allow subsystems to play with this list.
        
        if ( context.isTrueOrUndef(ARQ.propertyFunctions) )
            op = apply("Property Functions", new TransformPropertyFunction(context), op) ;

        if ( context.isTrueOrUndef(ARQ.optFilterConjunction) )
            op = apply("Break up filter conjunctions", new TransformFilterConjunction(), op) ;

        if ( context.isTrueOrUndef(ARQ.optFilterExpandOneOf) )
            op = apply("Break up IN and NOT IN", new TransformExpandOneOf(), op) ;
        
        // TODO Improve filter placement to go through assigns that have no effect.
        // Do this before filter placement and other sequence generating transformations.
        // or improve to place in a sequence.
        
        if ( context.isTrueOrUndef(ARQ.optFilterEquality) )
            op = apply("Filter Equality", new TransformFilterEquality(), op) ;
        
        if ( context.isTrueOrUndef(ARQ.optFilterDisjunction) )
            op = apply("Filter Disjunction", new TransformFilterDisjunction(), op) ;
        
        op = apply("Join strategy", new TransformJoinStrategy(context), op) ;
        
        if ( context.isTrueOrUndef(ARQ.optFilterPlacement) )
            // This can be done too early (breaks up BGPs).
            op = apply("Filter Placement", new TransformFilterPlacement(), op) ;
        
        op = apply("Path flattening", new TransformPathFlattern(), op) ;
        // Mark
        if ( false )
            op = OpLabel.create("Transformed", op) ;
        return op ;
    }

    public static Op apply(String label, Transform transform, Op op)
    {
        Op op2 ;
        // Skip SERVICE
        // This avoids needing to ensure every optimizer transform knows not to touch OpService. 
        if ( true )
        {
            // Simplest way but still walks the OpService subtree (and throws away the transformation).
            transform = new TransformSkipService(transform) ;
            op2 = Transformer.transform(transform, op) ;
        }
        else
        {
            // Don't transform OpService and don'e walk the sub-op 
            ApplyTransformVisitorServiceAsLeaf v = new ApplyTransformVisitorServiceAsLeaf(transform) ;
            WalkerVisitorSkipService walker = new WalkerVisitorSkipService(v) ;
            OpWalker.walk(walker, op, v) ;
            op2 = v.result() ;
        }

        
        final boolean debug = false ;
        
        if ( debug )
        {
            log.info("Transform: "+label) ;
            if ( op == op2 ) 
            {
                log.info("No change (==)") ;
                return op2 ;
            }

            if ( op.equals(op2) ) 
            {
                log.info("No change (equals)") ;
                return op2 ;
            }
            log.info("\n"+op.toString()) ;
            log.info("\n"+op2.toString()) ;
        }
        return op2 ;
    }
    

    // --------------------------------
    // Modified classes to avoid transforming SERVICE/OpService.
    
    /** Treat OpService as a leaf of the tree */
    static class ApplyTransformVisitorServiceAsLeaf  extends  ApplyTransformVisitor
    {
        public ApplyTransformVisitorServiceAsLeaf(Transform transform)
        {
            super(transform) ;
        }
        
        @Override
        public void visit(OpService op)
        {
            // Treat as a leaf that does not change.
            push(op) ;
        }
    }
    
    /** Don't walk down an OpService sub-operation */
    static class WalkerVisitorSkipService extends WalkerVisitor
    {
        public WalkerVisitorSkipService(OpVisitor visitor)
        {
            super(visitor) ;
        }
        
        @Override
        public void visit(OpService op)
        { 
            before(op) ;
            // visit1 code from WalkerVisitor
//            if ( op.getSubOp() != null ) op.getSubOp().visit(this) ;
            
            // Just visit the OpService node itself.
            // The transformer needs to push teh code as a result (see ApplyTransformVisitorSkipService)
            if ( visitor != null ) op.visit(visitor) ;
            
            after(op) ;
        }
        
    }
    
    // --------------------------------
    // Plan B: ignore transformation of OpService and return the original.
    // Still walks the sub-op of OpService 
    static class TransformSkipService extends TransformWrapper
    {
        public TransformSkipService(Transform transform)
        {
            super(transform) ;
        }
        
        @Override
        public Op transform(OpService opService, Op subOp)
        { return opService ; } 

        
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * 
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