/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.syntax;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** A SPARQL BasicGraphPattern
 * 
 * @author Andy Seaborne
 */

public class ElementPathBlock extends Element implements TripleCollector
{
    private PathBlock pattern = new PathBlock() ; 

    public ElementPathBlock()
    {  }

    public boolean isEmpty() { return pattern.isEmpty() ; }
    
    public void addTriple(TriplePath tp)
    { pattern.add(tp) ; }
    
    public int mark() { return pattern.size() ; }
    
    public void addTriple(Triple t)
    { addTriplePath(new TriplePath(t)) ; }

    public void addTriple(int index, Triple t)
    { addTriplePath(index, new TriplePath(t)) ; }

    public void addTriplePath(TriplePath tPath)
    { pattern.add(tPath) ; }

    public void addTriplePath(int index, TriplePath tPath)
    { pattern.add(index, tPath) ; }
    
    public PathBlock getPattern() { return pattern ; }
    public Iterator<TriplePath> patternElts() { return pattern.iterator(); }
    
    @Override
    public int hashCode()
    { 
        int calcHashCode = Element.HashBasicGraphPattern ;
        calcHashCode ^=  pattern.hashCode() ; 
        return calcHashCode ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( el2 instanceof ElementPathBlock) )
            return false ;
        ElementPathBlock eg2 = (ElementPathBlock)el2 ;
        return this.pattern.equiv(eg2.pattern, isoMap) ; 
    }

    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }
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