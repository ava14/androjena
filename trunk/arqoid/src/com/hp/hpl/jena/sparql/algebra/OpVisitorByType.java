/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.sparql.algebra.op.*;

/** A vistor helper that maps all vists to a few general ones */ 
public abstract class OpVisitorByType implements OpVisitor
{
    protected abstract void visitN(OpN op) ;

    protected abstract void visit2(Op2 op) ;
    
    protected abstract void visit1(Op1 op) ;
    
    protected abstract void visit0(Op0 op) ;    
    
    protected abstract void visitExt(OpExt op) ;    

    public void visit(OpBGP opBGP)
    { visit0(opBGP) ; }
    
    public void visit(OpQuadPattern quadPattern)
    { visit0(quadPattern) ; }

    public void visit(OpTriple opTriple)
    { visit0(opTriple) ; }
    
    public void visit(OpPath opPath)
    { visit0(opPath) ; }
    
    public void visit(OpProcedure opProcedure)
    { visit1(opProcedure) ; }

    public void visit(OpPropFunc opPropFunc)
    { visit1(opPropFunc) ; }

    public void visit(OpJoin opJoin)
    { visit2(opJoin) ; }

    public void visit(OpSequence opSequence)
    { visitN(opSequence) ; }
    
    public void visit(OpDisjunction opDisjunction)
    { visitN(opDisjunction) ; }
    
    public void visit(OpLeftJoin opLeftJoin)
    { visit2(opLeftJoin) ; }

    public void visit(OpDiff opDiff)
    { visit2(opDiff) ; }

    public void visit(OpMinus opMinus)
    { visit2(opMinus) ; }

    public void visit(OpUnion opUnion)
    { visit2(opUnion) ; }
    
    public void visit(OpConditional opCond)
    { visit2(opCond) ; }

    public void visit(OpFilter opFilter)
    { visit1(opFilter) ; }

    public void visit(OpGraph opGraph)
    { visit1(opGraph) ; }

    public void visit(OpService opService)
    { visit1(opService) ; }

    public void visit(OpDatasetNames dsNames)
    { visit0(dsNames) ; }

    public void visit(OpTable opUnit)
    { visit0(opUnit) ; }

    public void visit(OpExt opExt)
    { visitExt(opExt) ; }

    public void visit(OpNull opNull)
    { visit0(opNull) ; }

    public void visit(OpLabel opLabel)
    { visit1(opLabel) ; }

    public void visit(OpAssign opAssign)
    { visit1(opAssign) ; }

    public void visit(OpList opList)
    { visit1(opList) ; }

    public void visit(OpOrder opOrder)
    { visit1(opOrder) ; }

    public void visit(OpProject opProject)
    { visit1(opProject) ; }

    public void visit(OpReduced opReduced)
    { visit1(opReduced) ; }

    public void visit(OpDistinct opDistinct)
    { visit1(opDistinct) ; }

    public void visit(OpSlice opSlice)
    { visit1(opSlice) ; }

    public void visit(OpGroupAgg opGroupAgg)
    { visit1(opGroupAgg) ; }
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