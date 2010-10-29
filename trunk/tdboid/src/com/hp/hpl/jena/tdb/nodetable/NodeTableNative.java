/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.nodetable;

import static com.hp.hpl.jena.tdb.lib.NodeLib.setHash ;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.Pair ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.Record ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.lib.NodeLib ;
import com.hp.hpl.jena.tdb.store.Hash ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** A concrete NodeTable based on native storage (string file and a index) */ 
public class NodeTableNative implements NodeTable
{
    // Assumes an StringFile and an Indexer, which may be an Index but allows
    // this to be overriden for a direct use of BDB.

    protected ObjectFile objects ;
    protected Index nodeHashToId ;        // hash -> int
    
    // Delayed construction - must call init explicitly.
    protected NodeTableNative() {}
    
    // Combined into one constructor.
    public NodeTableNative(Index nodeToId, ObjectFile objectFile)
    {
        this() ;
        init(nodeToId, objectFile) ;
    }
    
    protected void init(Index nodeToId, ObjectFile objectFile)
    {
        this.nodeHashToId = nodeToId ;
        this.objects = objectFile;
    }

    // ---- Public interface for Node <==> NodeId

    /** Get the Node for this NodeId, or null if none */
    //@Override
    public Node getNodeForNodeId(NodeId id)
    {
        return _retrieveNodeByNodeId(id) ;
    }

    /** Find the NodeId for a node, or return NodeId.NodeDoesNotExist */ 
    //@Override
    public NodeId getNodeIdForNode(Node node)  { return _idForNode(node, false) ; }

    /** Find the NodeId for a node, allocating a new NodeId if the Node does not yet have a NodeId */ 
    //@Override
    public NodeId getAllocateNodeId(Node node)  { return _idForNode(node, true) ; }

    // ---- The worker functions
    
    private Node _retrieveNodeByNodeId(NodeId id)
    {
        if ( NodeId.doesNotExist(id) )
            return null ;
        if ( NodeId.isAny(id) )
            return null ;
        
        Node n = readNodeByNodeId(id) ;
        return n ;
//        
//        // Inline?
//        Node n = NodeId.extract(id) ;
//        if ( n != null )
//            return n ; 
//        
//        synchronized (this)
//        {
//            n = cacheLookup(id) ;   // Includes known to not exist
//            if ( n != null )
//                return n ; 
//
//            n = readNodeByNodeId(id) ;
//            cacheUpdate(n, id) ;
//            return n ;
//            
//        }
    }

    // ----------------
    
    // Node to NodeId worker
    // Find a node, possibly placing it in the node file as well
    private NodeId _idForNode(Node node, boolean allocate)
    {
        if ( node == Node.ANY )
            return NodeId.NodeIdAny ;
        
        // Inline?
        NodeId nodeId = NodeId.inline(node) ;
        if ( nodeId != null )
            return nodeId ;

        nodeId = accessIndex(node, allocate) ;
        return nodeId ;
    }
    
    // Access the node->NodeId index.
    // Synchronized further out.
    protected NodeId accessIndex(Node node, boolean create)
    {
        Hash hash = new Hash(nodeHashToId.getRecordFactory().keyLength()) ;
        setHash(hash, node) ;
        byte k[] = hash.getBytes() ;        
        // Key only.
        Record r = nodeHashToId.getRecordFactory().create(k) ;

        // Key and value, or null
        Record r2 = nodeHashToId.find(r) ;
        if ( r2 != null )
        {
            // Found.  Get the NodeId.
            NodeId id = NodeId.create(r2.getValue(), 0) ;
            return id ;
        }

        // Not found.
        if ( ! create )
            return NodeId.NodeDoesNotExist ;

        // Write the node, which allocates an id for it.
        NodeId id = writeNodeToTable(node) ;

        // Update the r record with the new id.
        // r.valkue := id bytes ; 
        id.toBytes(r.getValue(), 0) ;

        // Put in index - may appear because of concurrency
        if ( ! nodeHashToId.add(r) )
            throw new TDBException("NodeTableBase::nodeToId - record mysteriously appeared") ;

        return id ;
    }
    
    // -------- NodeId<->Node
    // Assumes NodeId inlining and caching has been handled.
    // Assumes synchronized (the caches will be updated consistently)
    
    // Only places for accessing the StringFile.
    
    protected final NodeId writeNodeToTable(Node node)
    {
        long x = NodeLib.encodeStore(node, getObjects()) ;
        return NodeId.create(x);
    }
    

    protected final Node readNodeByNodeId(NodeId id)
    {
        return NodeLib.fetchDecode(id.getId(), getObjects()) ;
    }
    // -------- NodeId<->Node

    //@Override
    public synchronized void close()
    {
        // Close once.  This may be shared (e.g. triples table and quads table). 
        if ( nodeHashToId != null )
        {
            nodeHashToId.close() ;
            nodeHashToId = null ;
        }
        if ( getObjects() != null )
        {
            getObjects().close() ;
            objects = null ;
        }
    }

    public Iterator<Pair<NodeId, Node>> all()
    {
        // Could be quicker by hoping down the objects files.
        Iterator<Record> iter = nodeHashToId.iterator() ; ;

        Transform<Record, Pair<NodeId, Node>> transform = new Transform<Record, Pair<NodeId, Node>>() {
            public Pair<NodeId, Node> convert(Record item)
            {
                NodeId id = NodeId.create(item.getValue(), 0) ;
                Node n = NodeLib.fetchDecode(id.getId(), getObjects()) ;
                return new Pair<NodeId, Node>(id, n) ;
            }};
        return Iter.map(iter, transform) ;
    }

    //@Override
    public void sync() { sync(true) ; } 
    
    //@Override
    public synchronized void sync(boolean force)
    {
        if ( nodeHashToId != null )
            nodeHashToId.sync(force) ;
        if ( getObjects() != null )
            getObjects().sync(force) ;
    }

    public ObjectFile getObjects()
    {
        return objects;
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