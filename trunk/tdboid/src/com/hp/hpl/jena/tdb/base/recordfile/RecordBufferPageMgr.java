/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.recordfile;

import static com.hp.hpl.jena.tdb.base.recordfile.RecordBufferPage.* ;
import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.block.BlockConverter;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockType;
import com.hp.hpl.jena.tdb.base.record.RecordException;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;

/** Manager for a block that is all records.  
 *  This must be compatible with B+Tree records nodes and with hash buckets. 
 * @author Andy Seaborne
 * @version $Id$
 */

final
public class RecordBufferPageMgr extends BlockConverter<RecordBufferPage>
{
    public RecordBufferPageMgr(RecordFactory factory, BlockMgr blockMgr)
    {
        super(null, blockMgr) ;
        Block2RecordBufferPage conv = new Block2RecordBufferPage(factory, this) ;
        super.setConverter(conv) ;
    }

    public RecordBufferPage create(int x)
    {
        RecordBufferPage rbp = super.create(x, BlockType.RECORD_BLOCK) ;
        rbp.setId(x) ;
        return rbp ;
    }
    
    @Override
    public RecordBufferPage get(int id)
    {
        synchronized(this) 
        {
            // Must call in single reader for the context.
            RecordBufferPage rbp = super.get(id) ;
            rbp.setPageMgr(this) ;
            // Link and Count are in the block and got done by the converter.
            return rbp ;
        }
    }
    
    private static class Block2RecordBufferPage implements Converter<RecordBufferPage>
    {
        private RecordFactory factory ;
        private RecordBufferPageMgr pageMgr ;

        Block2RecordBufferPage(RecordFactory factory, RecordBufferPageMgr pageMgr)
        {
            this.factory = factory ;
            this.pageMgr = pageMgr ;
        }
        
        //@Override
        public RecordBufferPage createFromByteBuffer(ByteBuffer bb, BlockType blkType)
        {
            if ( blkType != BlockType.RECORD_BLOCK )
                throw new RecordException("Not RECORD_BLOCK: "+blkType) ;
            // Initially empty
            RecordBufferPage rb = new RecordBufferPage(NO_ID, NO_ID, bb, factory, pageMgr, 0) ;
            return rb ;
        }

        //@Override
        public RecordBufferPage fromByteBuffer(ByteBuffer byteBuffer)
        {
            synchronized (byteBuffer)
            {
                int count = byteBuffer.getInt(COUNT) ;
                int linkId = byteBuffer.getInt(LINK) ;
                RecordBufferPage rb = new RecordBufferPage(NO_ID, linkId, byteBuffer, factory, pageMgr, count) ;
                return rb ;
            }
        }

        //@Override
        public ByteBuffer toByteBuffer(RecordBufferPage rbp)
        {
            int count = rbp.getRecordBuffer().size() ;
            rbp.setCount(count) ;
            rbp.getBackingByteBuffer().putInt(COUNT, rbp.getCount()) ;
            rbp.getBackingByteBuffer().putInt(LINK, rbp.getLink()) ;
            return rbp.getBackingByteBuffer() ;
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