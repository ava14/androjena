/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb;

import org.openjena.atlas.logging.Log ;
import tdb.cmdline.CmdTDB;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollector;
import com.hp.hpl.jena.tdb.store.GraphTDB;


public class tdbstats extends CmdTDB
{
    // tdbconfig?
    static public void main(String... argv)
    { 
        Log.setLog4j() ;
        new tdbstats(argv).mainRun() ;
    }

    protected tdbstats(String[] argv)
    {
        super(argv) ;
    }
    
    @Override
    protected String getSummary()
    {
        return null ;
    }

    @Override
    protected void exec()
    {
        GraphTDB graph = getGraph() ;
        Item item = StatsCollector.gatherTDB(graph) ;
        System.out.println(item) ;
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