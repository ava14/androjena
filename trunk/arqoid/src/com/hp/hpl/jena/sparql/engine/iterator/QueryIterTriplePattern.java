/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

public class QueryIterTriplePattern extends QueryIterRepeatApply
{
    Triple pattern ;
    
    public QueryIterTriplePattern( QueryIterator input,
                                   Triple pattern , 
                                   ExecutionContext cxt)
    {
        super(input, cxt) ;
        this.pattern = pattern ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        return new TripleMapper(binding, pattern, getExecContext()) ;
    }
    
    static int countMapper = 0 ; 
    static class TripleMapper extends QueryIter
    {
        private Node s ;
        private Node p ;
        private Node o ;
        private Binding binding ;
        private ClosableIterator<Triple> graphIter ;
        //private Iterator graphIter ;

        // This one-slot lookahead is common - but is it worth extracting?
        private Binding slot = null ;

        TripleMapper(Binding binding, Triple pattern, ExecutionContext cxt)
        {
            super(cxt) ;
            this.s = substitute(pattern.getSubject(), binding) ;
            this.p = substitute(pattern.getPredicate(), binding) ;
            this.o = substitute(pattern.getObject(), binding) ;
            this.binding = binding ;
            Node s2 = tripleNode(s) ;
            Node p2 = tripleNode(p) ;
            Node o2 = tripleNode(o) ;
            Graph graph = cxt.getActiveGraph() ;
            
            ExtendedIterator<Triple> iter = graph.find(s2, p2, o2) ;
            
            if ( false )
            {
                // Materialize the results now. Debugging only.
                List<Triple> x = iter.toList() ;
                this.graphIter = WrappedIterator.create(x.iterator()) ;
                iter.close();
            }
            else
                // Stream.
                this.graphIter = iter ;
        }

        private Node tripleNode(Node node)
        {
            if ( node.isVariable() )
                return Node.ANY ;
            return node ;
        }

        private Node substitute(Node node, Binding binding)
        {
            if ( Var.isVar(node) )
            {
                Node x = binding.get(Var.alloc(node)) ;
                if ( x != null )
                    return x ;
            }
            return node ;
        }

        // ---- Iterator machinary
        
        private Binding mapper(Triple r)
        {
            Binding results = new BindingMap(binding) ;

            if ( ! insert(s, r.getSubject(), results) )
                return null ; 
            if ( ! insert(p, r.getPredicate(), results) )
                return null ;
            if ( ! insert(o, r.getObject(), results) )
                return null ;
            return results ;
        }

        private static boolean insert(Node inputNode, Node outputNode, Binding results)
        {
            if ( ! Var.isVar(inputNode) )
                return true ;
            
            Var v = Var.alloc(inputNode) ;
            Node x = results.get(v) ;
            if ( x != null )
                return outputNode.equals(x) ;
            
            results.add(v, outputNode) ;
            return true ;
        }

        // TODO Test and swap to this code.
        // Avoid allocating a Binding.
        private Binding _mapper(Triple r)
        {
            Binding results = new BindingMap(binding) ;

            int z = 0 ; // Number of bindings that have occurred. 
            
            {
                int x = _insert(s, r.getSubject(), results) ;
                if ( x == -1 ) return null ;
                z += x ;
            }
            
            {
                int x = _insert(p, r.getPredicate(), results) ;
                if ( x == -1 ) return null ;
                z += x ;
            }
            
            {
                int x = _insert(o, r.getObject(), results) ;
                if ( x == -1 ) return null ;
                z += x ;
            }

            if ( z == 0 )
                // No binding occurred.
                return binding ;
            return results ;
        }

        // return -1 - no match ; 0 - OK, no binding change ; 1 - OK, binding happened
        private static int _insert(Node inputNode, Node outputNode, Binding results)
        {
            if ( ! Var.isVar(inputNode) )
                return 0 ;
            
            Var v = Var.alloc(inputNode) ;
            Node x = results.get(v) ;
            if ( x != null )
                return outputNode.equals(x) ? 0 : -1 ;
            
            results.add(v, outputNode) ;
            return 1 ;
        }        
        
        
        @Override
        protected boolean hasNextBinding()
        {
            if ( slot != null ) return true ;

            while(graphIter.hasNext() && slot == null )
            {
                Triple t = graphIter.next() ;
                slot = mapper(t) ;
            }
            return slot != null ;
        }

        @Override
        protected Binding moveToNextBinding()
        {
            hasNextBinding() ;
            Binding r = slot ;
            slot = null ;
            return r ;
        }

        @Override
        protected void closeIterator()
        {
            if ( graphIter != null )
                NiceIterator.close(graphIter) ;
            graphIter = null ;
        }
    }
}


/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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