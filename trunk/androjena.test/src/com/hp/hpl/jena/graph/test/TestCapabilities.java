/*
  (c) Copyright 2002, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestCapabilities.java,v 1.1 2009/06/29 08:55:40 castagna Exp $
*/

package com.hp.hpl.jena.graph.test;

import com.hp.hpl.jena.graph.*;
import junit.framework.*;

/**
    Test graph capabilities.
 	@author kers
*/
public class TestCapabilities extends GraphTestBase
    {
    protected final class AllFalse implements Capabilities
        {
        public boolean sizeAccurate()
            { return false; }

        public boolean addAllowed()
            { return false; }

        public boolean addAllowed( boolean everyTriple )
            { return false; }

        public boolean deleteAllowed()
            { return false; }

        public boolean deleteAllowed( boolean everyTriple )
            { return false; }

        public boolean iteratorRemoveAllowed()
            { return false; }

        public boolean canBeEmpty()
            { return false; }

        public boolean findContractSafe()
            { return false; }

        public boolean handlesLiteralTyping()
            { return false; }
        }

    public TestCapabilities( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestCapabilities.class ); }   

    /**
        pending on use-cases.
    */
    public void testTheyreThere()
        {
        Graph g = Factory.createDefaultGraph();
        g.getCapabilities();
        }
    
    public void testCanConstruct()
        {
        Capabilities c = new AllFalse();
        }
    
    public void testCanAccess()
        {
        Capabilities c = new AllFalse();
        boolean b = false;
        b = c.addAllowed();
        b = c.addAllowed( true );
        b = c.canBeEmpty();
        b = c.deleteAllowed();
        b = c.deleteAllowed( false );
        b = c.sizeAccurate();
        b = c.iteratorRemoveAllowed();
        b = c.findContractSafe();
        b = c.handlesLiteralTyping();
        }
    }


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/