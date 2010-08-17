/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.shared.LockMRSW ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Implementation of a DatasetGraph where all graphs "exist".
 * New graphs are created (via the policy of a GraphMaker) when a getGraph call is 
 * made to a graph that has not been allocated.
 */
public class DatasetGraphOpen implements DatasetGraph
{
    public interface GraphMaker { public Graph create() ; }
    
    private Context context = new Context() ;
    private Map<Node, Graph> graphs = new HashMap<Node, Graph>() ;
    
    private Graph defaultGraph ;
    private GraphMaker graphMaker ;

    public DatasetGraphOpen(GraphMaker graphMaker)
    {
        this.graphMaker = graphMaker ;
        defaultGraph = graphMaker.create() ;
    }

    public boolean containsGraph(Node graphNode)
    {
        return true ;
    }

    public Graph getDefaultGraph()
    {
        return defaultGraph ;
    }

    public Graph getGraph(Node graphNode)
    {
        Graph g = graphs.get(graphNode) ;
        if ( g == null )
        {
            g = graphMaker.create() ;
            graphs.put(graphNode, g) ;
        }
        
        return g ;
    }

    private Lock lock = new LockMRSW() ;
    public Lock getLock()
    {
        return lock ;
    }

    public Iterator<Node> listGraphNodes()
    {
        return graphs.keySet().iterator() ;
    }

    public Context getContext()
    {
        return context ;
    }

    public int size()
    {
        return graphs.size() ;
    }

    public void close()
    {}
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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