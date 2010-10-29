/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.graph;

import com.hp.hpl.jena.graph.impl.TransactionHandlerBase;

import com.hp.hpl.jena.tdb.store.GraphTDB;

/** TDB does not support ACID transactions - it uses the SPARQL/Update events.  
 *  It (weakly) flushes if commit is called although it denies supporting transactions
 */

public class TransactionHandlerTDB extends TransactionHandlerBase //implements TransactionHandler 
{
    private final GraphTDB graph ;

    public TransactionHandlerTDB(GraphTDB graph)
    {
        this.graph = graph ;
    }
    
    //@Override
    public void abort()
    {
        throw new UnsupportedOperationException("TDB: 'abort' of a transaction not supported") ;
        //log.warn("'Abort' of a transaction not supported - ignored") ;
    }

    //@Override
    public void begin()
    {}

    //@Override
    public void commit()
    {
        graph.sync(true) ;
    }

    //@Override
    public boolean transactionsSupported()
    {
        return false ;
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