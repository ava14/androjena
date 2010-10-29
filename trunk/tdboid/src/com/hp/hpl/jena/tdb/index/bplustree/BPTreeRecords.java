/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import static com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams.CheckingNode;
import static java.lang.String.format;
import static org.openjena.atlas.lib.Lib.decodeIndex ;

import java.nio.ByteBuffer;


import org.openjena.atlas.io.IndentedWriter ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.tdb.base.StorageException;
import com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage;

import com.hp.hpl.jena.tdb.base.buffer.RecordBuffer;
import com.hp.hpl.jena.tdb.base.record.Record;

/** B+Tree wrapper over a block of records in a RecordBufferPage.
 * This class adds no peristent state to a RecordBufferPage */
final class BPTreeRecords extends BPTreePage
{
    // Could require all Page operations to the RecordBufferPage
    // Page is then an interface and BPTreeNode has the state 
    private static Logger log = LoggerFactory.getLogger(BPTreeRecords.class) ;
    private final RecordBufferPage    rBuffPage ;
    private final RecordBuffer        rBuff ;        // Used heavily.
    
    public BPTreeRecords(BPlusTree bpTree, RecordBufferPage rbp)
    {
        super(bpTree, bpTree.getRecordsMgr().getBlockMgr()) ;
        rBuffPage = rbp ;
        rBuff = rBuffPage.getRecordBuffer() ;
    }
    
    RecordBufferPage getRecordBufferPage()
    { return rBuffPage ; }
    
    RecordBuffer getRecordBuffer()
    { return rBuff ; }

    int getLink()
    { return rBuffPage.getLink() ; }

    @Override
    boolean isFull()
    {
        return ( rBuff.size() >= rBuff.maxSize() ) ;
    }

    @Override
    boolean isMinSize()
    {
        // 50% packing minimum.
        // If of max length 5 (i.e. odd), min size is 2.  Integer division works.  
        return ( rBuff.size() <= rBuff.maxSize()/2 ) ;
   }

    @Override
    Record internalSearch(Record rec)
    {
        int i = rBuff.find(rec) ;
        if ( i < 0 )
            return null ;
        return rBuff.get(i) ;
    }

    @Override
    BPTreeRecords findPage(Record record)
    {
        if ( rBuff.size() == 0 )
            return this ;
        
        // Not true if above the last record.
        if ( this.getLink() != RecordBufferPage.NO_ID && Record.keyGT(record, maxRecord()) ) 
            error("Record [%s] not in this page: %s", record , this) ;
        return this ;
    }
    
    @Override
    BPTreeRecords findFirstPage() { return this ; }

    @Override final
    void put()   { bpTree.getRecordsMgr().put(this) ; } 
    
    @Override final
    void release()   { bpTree.getRecordsMgr().release(getId()) ; } 
    
    @Override
    Record internalInsert(Record record)
    {
        int i = rBuff.find(record) ;
        Record r2 = null ;
        if ( i < 0 )
        {
            i = decodeIndex(i) ;
            if ( rBuff.size() >= rBuff.maxSize())  
                throw new StorageException("RecordBlock.put overflow") ; 
            rBuff.add(i, record) ;
        }
        else
        {
            r2 = rBuff.get(i) ;
            if ( Record.compareByKeyValue(record, r2) != 0 )
                // Replace : return old
                rBuff.set(i, record) ;
        }
        put() ;
        return r2 ;
    }

    @Override
    Record internalDelete(Record record)
    {
        int i = rBuff.find(record) ;
        if ( i < 0 )
            return null ;
        Record r2 = rBuff.get(i) ;
        rBuff.remove(i) ;
        put() ;
        return r2 ;       
    }
    
    @Override final
    Record getSplitKey()
    {
        int splitIdx = rBuff.size()/2-1 ;
        Record r = rBuff.get(splitIdx) ;
        return r ;
    }

    /** Split: place old high half in 'other'. Return the new (upper) BPTreeRecords(BPTreePage).
     * Split is the high end of the low page.
     */
    @Override final
    BPTreePage split() 
    {
        // LinkIn
        // Create a new BPTreeRecords.
        
        BPTreeRecords other = create(rBuffPage.getLink()) ;
        rBuffPage.setLink(other.getId()) ;
        
        int splitIdx = rBuff.size()/2-1 ;
        Record r = rBuff.get(splitIdx) ;                // Only need key for checking later.
        
        int moveLen =  rBuff.size()-(splitIdx+1) ;      // Number to move.
        // Copy high end to new.  
        rBuff.copy(splitIdx+1, other.getRecordBufferPage().getRecordBuffer(), 0, moveLen) ;
        rBuff.clear(splitIdx+1, moveLen) ;
        rBuff.setSize(splitIdx+1) ;
        
        if ( CheckingNode )
        {
            if ( ! Record.keyEQ(r, maxRecord()) )
            {
                System.err.println(rBuff) ;
                System.err.println(other.rBuff) ;
                error("BPTreeRecords.split: Not returning expected record") ;
            }
        }
        return other ;
    }

    private BPTreeRecords create(int linkId)
    {
        int id = bpTree.getRecordsMgr().allocateId() ;
        BPTreeRecords newPage = bpTree.getRecordsMgr().create(id) ;
        newPage.getRecordBufferPage().setLink(linkId) ;
        newPage.getRecordBufferPage().setId(id) ;
        return newPage ;
    }

    @Override
    Record shiftRight(BPTreePage other, Record splitKey)
    {
        // Error checking by RecordBuffer
        BPTreeRecords page = cast(other) ;
        rBuff.shiftRight(page.rBuff) ;
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getHigh() ;
    }
    
    @Override
    Record shiftLeft(BPTreePage other, Record splitKey)
    {
        // Error checking by RecordBuffer
        BPTreeRecords page = cast(other) ;
        rBuff.shiftLeft(page.rBuff) ;
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getHigh() ;
    }

    @Override
    BPTreePage merge(BPTreePage right, Record splitKey)
    {
        // Split key ignored - it's for the B+Tree case of pushing down a key
        // Records blocks have all the key/values in them anyway.
        return merge(this, cast(right)) ;
    }

    private static BPTreeRecords merge(BPTreeRecords left, BPTreeRecords right)
    {
        // Copy right to top of left.
        // The other way round needs a shift as well.
        right.rBuff.copyToTop(left.rBuff) ;
        // Same as: right.rBuff.copy(0, left.rBuff, left.rBuff.size(), right.rBuff.size()) ;
        right.rBuff.clear() ;
        
        //The right page is released by the caller.  left is still in use.
        // So the test code can poke around in the right block after merge. 
        //left.bpTree.getRecordsMgr().release(left.getId()) ;
        
        // Fix up the link chain.
        left.rBuffPage.setLink(right.rBuffPage.getLink()) ;
        return left ;
    }
    
    private static BPTreeRecords cast(BPTreePage page)
    {
        try { return (BPTreeRecords)page  ; }
        catch (ClassCastException ex) { error("Wrong type: "+page) ; return null ; }
    }
    
    @Override final
    Record minRecord()
    {
        return getLowRecord() ;
    }

    @Override final
    Record maxRecord()
    {
        return getHighRecord() ;
    }

    private static void error(String msg, Object... args)
    {
        msg = format(msg, args) ;
        System.out.println(msg) ;
        System.out.flush();
        throw new BPTreeException(msg) ;
    }

    @Override
    final Record getLowRecord()
    {
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getLow() ;
    }

    @Override
    final Record getHighRecord()
    {
        if ( rBuff.size() == 0 )
            return null ;
        return rBuff.getHigh() ;
    }

    //@Override
    public final int getMaxSize()             { return rBuff.maxSize() ; }
    
    //@Override
    public final int getCount()             { return rBuff.size() ; }
 
    //@Override
    public final void setCount(int count)   { rBuff.setSize(count) ; }
    
    @Override
    public String toString()
    { return String.format("BPTreeRecords[id=%d, link=%d]: %s", getId(), getLink(), rBuff.toString()); }
    
    @Override
    public final void checkNode()
    {
        if ( ! CheckingNode ) return ;
        if ( rBuff.size() < 0 || rBuff.size() > rBuff.maxSize() )
            error("Misized: %s", this) ;

        for ( int i = 1 ; i < getCount() ; i++ )
        {
            Record r1 = rBuff.get(i-1) ;
            Record r2 = rBuff.get(i) ;
            if ( Record.keyGT(r1, r2) )
                error("Not sorted: %s", this) ;
        }
    }
    
    @Override
    public final void checkNodeDeep()
    { checkNode() ; }

    //@Override
    public ByteBuffer getBackingByteBuffer()   { return rBuffPage.getBackingByteBuffer() ; }

    //@Override
    public int getId()                  { return rBuffPage.getId() ; } 

    //@Override
    public void setId(int id)           { rBuffPage.setId(id) ; }

    public void output(IndentedWriter out)
    {
        out.print(toString()) ;
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