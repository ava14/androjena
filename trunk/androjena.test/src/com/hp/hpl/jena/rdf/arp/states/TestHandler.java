/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;


import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import com.hp.hpl.jena.rdf.arp.impl.ANode;
import com.hp.hpl.jena.rdf.arp.impl.Location;
import com.hp.hpl.jena.rdf.arp.impl.Taint;
import com.hp.hpl.jena.rdf.arp.impl.XMLHandler;

import com.hp.hpl.jena.iri.IRI;

class TestHandler extends XMLHandler {
    public void wrong(String msg) {
        wrong = true;
        if (failOnWarning)
            Assert.fail("unexpected warning: "+msg);
    }

    @Override
    public void warning(Taint taintMe,int i, String s) {
        if (i<100)
            return;
        wrong = true;
        if (failOnWarning)
            Assert.fail("unexpected warning: "+s);
    }
    @Override
    public void endLocalScope(ANode v) {
        scope ++;
    }
    @Override
    public void triple(ANode s, ANode p, ANode o) {
        triples++;
    }
    boolean wrong;
    int triples;
    int scope;
    boolean failOnWarning;
    public void clear(boolean failOnWarning_) {
        wrong = false;
        triples = 0;
        scope = 0;
        this.failOnWarning = failOnWarning_;
        idsUsed = new HashMap<IRI, Map<String,Location>>();
        idsUsedCount = 0;
    }

    public String info() {
        return wrong?"?":
            (
            (triples==0?"":("T"+triples))
            +
            (scope==0?"":(" E"+scope)) );
    }

    public void check(EventRecord r) {
        r.initCounts();
        Assert.assertEquals("triple count",r.triples,triples);
        Assert.assertEquals("end bnode scope count",r.scope,scope);
        
    }
}

/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 
