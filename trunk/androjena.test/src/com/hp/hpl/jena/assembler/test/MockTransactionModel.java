/*
 (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 All rights reserved - see end of file.
 $Id: MockTransactionModel.java,v 1.1 2009/06/29 08:55:53 castagna Exp $
 */

package com.hp.hpl.jena.assembler.test;

import java.util.List;

import junit.framework.Assert;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.ModelAssembler;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

/**
    A model assembler that creates a model with controllable supporting of
    transactions and aborting on adding statements; the model logs transactions
    and adding-of-models. For testing only.
 	@author kers
*/
final class MockTransactionModel extends ModelAssembler
    {
    private final List<String> history;
    private final Model expected;
    private final boolean supportsTransactions;
    private final boolean abortsOnAdd;

    protected MockTransactionModel
        ( List<String> history, Model expected, boolean supportsTransactions, boolean abortsOnAdd )
        {
        super();
        this.history = history;
        this.expected = expected;
        this.supportsTransactions = supportsTransactions;
        this.abortsOnAdd = abortsOnAdd;
        }

    @Override
    protected Model openEmptyModel( Assembler a, Resource root, Mode irrelevant )
        {
        return new ModelCom( Factory.createDefaultGraph() ) 
            {
            @Override
            public Model begin()
                {
                history.add( "begin" );
                Assert.assertTrue( isEmpty() );
                return this;
                }

            @Override
            public Model add( Model other )
                {
                history.add( "add" );
                if (abortsOnAdd) throw new RuntimeException( "model aborts on add of " + other );
                super.add( other );
                return this;
                }

            @Override
            public Model abort()
                {
                history.add( "abort" );
                return this;
                }

            @Override
            public Model commit()
                {
                ModelTestBase.assertIsoModels( expected, this );
                history.add( "commit" );
                return this;
                }

            @Override
            public boolean supportsTransactions()
                {
                history.add( "supports[" + supportsTransactions + "]" );
                return supportsTransactions;
                }
            };
        }
    }

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */