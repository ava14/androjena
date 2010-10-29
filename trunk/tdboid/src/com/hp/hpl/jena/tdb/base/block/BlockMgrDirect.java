/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import static java.lang.String.format;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Block manager that is NOT memory mapped. */
public class BlockMgrDirect extends BlockMgrFile
{
    // Consider: having one file per "segment", not one large file.
    private static Logger log = LoggerFactory.getLogger(BlockMgrDirect.class) ;
    
    BlockMgrDirect(String filename, int blockSize)
    {
        super(filename, blockSize) ;
    }
    
    @Override
    public ByteBuffer allocateBuffer(int id)
    {
//    if ( getLog().isDebugEnabled() ) 
//        getLog().debug(format("allocateBuffer(%d)", id)) ;
    
        return ByteBuffer.allocate(blockSize) ;
    }

    //@Override
    public ByteBuffer get(int id)
    {
        check(id) ;
        checkIfClosed() ;
        
        if ( log.isDebugEnabled() ) 
            log.debug(format("get(%d)", id)) ;
        return getByteBuffer(id) ;
    }

    private ByteBuffer getByteBuffer(int id)
    {
        try {
            ByteBuffer dst = allocateBuffer(id) ;
            int len = channel.read(dst, filePosition(id)) ;
            if ( len != blockSize )
                throw new BlockException(format("get: short read (%d, not %d)", len, blockSize)) ;   
            return dst ;
        } catch (IOException ex)
        { throw new BlockException("BlockMgrDirect.get", ex) ; }
    }
    
    //@Override
    public void put(int id, ByteBuffer block)
    {
        if ( log.isDebugEnabled() ) 
            log.debug(format("put(%d)", id)) ;
        check(id, block) ;
        checkIfClosed() ;
        block.position(0) ;
        block.limit(block.capacity()) ;
        try {
            int len = channel.write(block, filePosition(id)) ;
            if ( len != blockSize )
                throw new BlockException(format("put: short write (%d, not %d)", len, blockSize)) ;   
        } catch (IOException ex)
        { throw new BlockException("BlockMgrDirect.put", ex) ; }
        putNotification(id, block) ;
    }
    
    
    private final long filePosition(int id)
    {
        return ((long)id)*((long)blockSize) ;
    }
    
    //@Override
    public void freeBlock(int id)
    { 
        check(id) ;
        checkIfClosed() ;
        if ( log.isDebugEnabled() ) 
            log.debug(format("release(%d)", id)) ;
    }
    
//    @Override
//    public void finishUpdate()
//    {}
//
//    @Override
//    public void startUpdate()
//    {}
//
//    @Override
//    public void startRead()
//    {}
//
//    @Override
//    public void finishRead()
//    {}

    @Override
    protected Logger getLog()
    {
        return log ;
    }

    static long count = 0 ;
    //@Override
    public void sync(boolean force)
    {
        count++ ;
        if ( getLog().isDebugEnabled() )
            getLog().debug("Sync/BlockMgrDirect "+label+" -- "+count) ;
        if ( force )
            force() ;
    }

    @Override
    protected void _close()
    {}
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