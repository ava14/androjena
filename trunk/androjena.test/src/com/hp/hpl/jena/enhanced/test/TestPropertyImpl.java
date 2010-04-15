/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestPropertyImpl.java,v 1.1 2009/06/29 08:55:55 castagna Exp $
*/

package com.hp.hpl.jena.enhanced.test;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @see TestObjectImpl
 * @author  jjc
 */
public class TestPropertyImpl  extends TestCommonImpl implements TestProperty {

    public static final Implementation factory = new Implementation() {
        @Override public EnhNode wrap(Node n,EnhGraph eg) {
            return new TestPropertyImpl(n,eg);
        }    
    
        @Override public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
    };
    
    /** Creates a new instance of TestAllImpl */
    private TestPropertyImpl(Node n,EnhGraph eg) {
        super( n, eg );
    }
    
    @Override public <X extends RDFNode> boolean supports( Class<X> t )
        { return t.isInstance( this ) && isProperty(); }
        
    public boolean isProperty() {
        return findPredicate() != null;
    }
        
    public TestObject anObject() {
        if (!isProperty())
            throw new IllegalStateException("Node is not the property of a triple.");
        return enhGraph.getNodeAs(findPredicate().getObject(),TestObject.class);
    }
    
}

/*
	(c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

