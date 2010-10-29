/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;


import java.util.Iterator ;

import org.openjena.atlas.iterator.NullIterator ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Tuple ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Closeable ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.lib.Sync ;
import com.hp.hpl.jena.tdb.lib.TupleLib ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;


/** Quad table - a collection of TupleIndexes for 4-tuples
 *  together with a node table.
 */

public class QuadTable implements Sync, Closeable
{
    final NodeTupleTable table ;
    
    public QuadTable(TupleIndex[] indexes, NodeTable nodeTable)
    {
        table = new NodeTupleTable(4, indexes, nodeTable);
    }

    /** Add a quad - return true if it was added, false if it already existed */
    public boolean add( Quad quad ) 
    { 
        return table.addRow(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    /** Add a quad (as graph node and triple) - return true if it was added, false if it already existed */
    public boolean add( Node gn, Triple triple ) 
    { 
        return table.addRow(gn, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    /** Delete a quad - return true if it was deleted, false if it didn't exist */
    public boolean delete( Quad quad ) 
    { 
        return table.deleteRow(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
    }

    /** Delete a quad (as graph node and triple) - return true if it was deleted, false if it didn't exist */
    public boolean delete( Node gn, Triple triple ) 
    { 
        return table.deleteRow(gn, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }

    /** Find matching quads */
    public Iterator<Quad> find(Node g, Node s, Node p, Node o)
    {
        Iterator<Tuple<NodeId>> iter = table.findAsNodeIds(g, s, p, o) ;
        if ( iter == null )
            return new NullIterator<Quad>() ;
        Iterator<Quad> iter2 = TupleLib.convertToQuads(table.getNodeTable(), iter) ;
        return iter2 ;
    }
    
    private static Transform<Tuple<Node>, Quad> action = new Transform<Tuple<Node>, Quad>(){
        //@Override
        public Quad convert(Tuple<Node> item)
        {
            return new Quad(item.get(0), item.get(1), item.get(2), item.get(3)) ;
        }} ; 
    
    
    public NodeTupleTable getNodeTupleTable() { return table ; }

    //@Override
    public void sync() { sync(true) ; }

    //@Override
    public void sync(boolean force)
    { table.sync(force) ; }

    public boolean isEmpty()        { return table.isEmpty() ; }
    
    //@Override
    public void close()
    { table.close() ; }
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