/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.page.PageBase;

/** Engine that wraps from blocks to typed (in-memory) representations. */

public class BlockConverter<T extends PageBase>
{
    public interface Converter<T>
    {
        /* Synchronization: The fromByteBuffer() operation must either be called
         * from a situation where it is safe against multiple readers
         * or the implementation coverter must provide multiple-reader safty.
         * 
         * toByteBuffer and createFromByteBuffer are writer-operations, hence
         * single writer policy applies. 
         */ 
        public T fromByteBuffer(ByteBuffer byteBuffer) ;
        public ByteBuffer toByteBuffer(T t) ;
        public T createFromByteBuffer(ByteBuffer bb, BlockType bType) ;
    }
    
    private BlockMgr blockMgr ;
    private Converter<T> pageFactory ;

    protected BlockConverter(Converter<T> pageFactory, BlockMgr blockMgr)
    { 
        this.pageFactory = pageFactory ;
        this.blockMgr = blockMgr ;
    }
   
    // Sometimes, the subclass must pass null to the constructor then call this. 
    protected void setConverter(Converter<T> pageFactory) { this.pageFactory = pageFactory ; }
    
    public BlockMgr getBlockMgr() { return blockMgr ; } 
    
    /** Allocate an uninitialized slot.  Fill with a .put later */ 
    public int allocateId()           { return blockMgr.allocateId() ; }
    
    /** Allocate a new thing */
    public T create(int id, BlockType bType)
    {
        ByteBuffer bb = blockMgr.allocateBuffer(id) ;
        T newThing = pageFactory.createFromByteBuffer(bb, bType) ;
        newThing.setId(id) ;
        return newThing ;
    }
    
    /** Fetch a block and make a T : must be called single-reader */
    public T get(int id)
    {
        synchronized (blockMgr)
        {
            ByteBuffer bb = blockMgr.get(id) ;
            T newThing = pageFactory.fromByteBuffer(bb) ;
            newThing.setId(id) ;
            return newThing ;
        }
    }

    public void put(int id, T page)
    {
        ByteBuffer bb = pageFactory.toByteBuffer(page) ;
        blockMgr.put(id, bb) ;
    }
    
    public void put(T page)
    {
        put(page.getId(), page) ;
    }

    public void release(int id)     { blockMgr.freeBlock(id) ; }
    
    public boolean valid(int id)    { return blockMgr.valid(id) ; }
    
    public void dump()
    { 
        for ( int idx = 0 ; valid(idx) ; idx++ )
        {
            T page = get(idx) ;
            System.out.println(page) ;
        }
    }
    
    /** Signal the start of an update operation */
    public void startUpdate()       { blockMgr.startUpdate() ; }
    
    /** Signal the completion of an update operation */
    public void finishUpdate()      { blockMgr.finishUpdate() ; }

    /** Signal the start of an update operation */
    public void startRead()         { blockMgr.startRead() ; }
    
    /** Signal the completeion of an update operation */
    public void finishRead()        { blockMgr.finishRead() ; }
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