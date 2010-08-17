/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr.aggregate;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.sse.writers.WriterExpr;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class AggSum implements AggregateFactory
{
    // ---- SUM(?var)
    
    // ---- AggregatorFactory
    private Expr expr ;

    public AggSum(Expr var) { this.expr = var ; } 

    public Aggregator create()
    {
        // One per each time there is an aggregation.
        return new AggSumWorker() ;
    }
    
    // XQuery/XPath Functions&Operators suggests zero
    // SQL suggests null.
    private static final NodeValue noValuesToSum = NodeValue.nvZERO ; // null 
    
    // ---- Aggregator
    class AggSumWorker extends AggregatorBase
    {
        public AggSumWorker()
        {
            super() ;
        }

        @Override
        public String toString() { return "sum("+ExprUtils.fmtSPARQL(expr)+")" ; }
        public String toPrefixString() { return "(sum "+WriterExpr.asString(expr)+")" ; }

        @Override
        protected Accumulator createAccumulator()
        { 
            return new AccSumVar() ;
        }
        
        private final Expr getExpr() { return expr ; }
        
        public boolean equalsAsExpr(Aggregator other)
        {
            if ( ! ( other instanceof AggSumWorker ) )
                return false ;
            AggSumWorker agg = (AggSumWorker)other ;
            return agg.getExpr().equals(getExpr()) ;
        } 

        
        /* null is SQL-like.  NodeValue.nodeIntZERO is F&O like */ 
        @Override
        public Node getValueEmpty()     { return NodeValue.toNode(noValuesToSum) ; } 
    }

    // ---- Accumulator
    class AccSumVar implements Accumulator
    {
        // Non-empty case but still can be nothing because the expression may be undefined.
        private NodeValue total = noValuesToSum ;
        
        public AccSumVar() {}

        public void accumulate(Binding binding, FunctionEnv functionEnv)
        { 
            try {
                NodeValue nv = expr.eval(binding, functionEnv) ;
                if ( nv.isNumber() )
                {
                    if ( total == noValuesToSum )
                        total = nv ;
                    else
                        total = XSDFuncOp.add(nv, total) ;
                }
            } catch (ExprEvalException ex)
            {}
        }
        public NodeValue getValue()
        { return total ; }
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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