/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.algebra.opt.TransformSimplify ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.PathBlock ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.E_Aggregator ;
import com.hp.hpl.jena.sparql.expr.E_Exists ;
import com.hp.hpl.jena.sparql.expr.E_LogicalNot ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;
import com.hp.hpl.jena.sparql.path.PathCompiler ;
import com.hp.hpl.jena.sparql.path.PathLib ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.syntax.* ;
import com.hp.hpl.jena.sparql.util.ALog ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class AlgebraGenerator 
{
    // Fixed filter position means leave exactly where it is syntactically (illegal SPARQL)
    // Helpful only to write exactly what you mean and test the full query compiler.
    private boolean fixedFilterPosition = false ;
    private Context context ;
    private PathCompiler pathCompiler = new PathCompiler() ;
    
    // simplifyInAlgebraGeneration=true is the alternative reading of
    // the DAWG Algebra translation algorithm. 

    // If we simplify during algebra generation, it changes the SPARQL for OPTIONAL {{ FILTER }}
    // The  {{}} results in (join unit (filter ...)) the filter is not moved
    // into the LeftJoin.  
    
    static public boolean applySimplification = true ;              // Allows raw algebra to be generated (testing) 
    private boolean simplifyTooEarlyInAlgebraGeneration = false ;   // False is the correct setting. 

    public AlgebraGenerator(Context context)
    { 
        if ( context == null )
            context = ARQ.getContext().copy() ;
        this.context = context ;
    }
    
    public AlgebraGenerator() { this(null) ; } 
    
    //-- Public operations.  Do not call recursively (call compileElement).
    // These operations apply the simplification step which is done, once, at the end.
    
    public Op compile(Query query)
    {
        Op pattern = compile(query.getQueryPattern()) ;     // Not compileElement - may need to apply simplification.
        Op op = compileModifiers(query, pattern) ;
        return op ;
    }
    
    protected static Transform simplify = new TransformSimplify() ;
    // Compile any structural element
    public Op compile(Element elt)
    {
        Op op = compileElement(elt) ;
        Op op2 = op ;
        if ( ! simplifyTooEarlyInAlgebraGeneration && applySimplification && simplify != null )
            op2 = simplify(op) ;
        return op2;
    }
    
    private static Op simplify(Op op)
    {
        return Transformer.transform(simplify, op) ;
    }

    // This is the operation to call for recursive application of step 4.
    protected Op compileElement(Element elt)
    {
        if ( elt instanceof ElementUnion )
            return compileElementUnion((ElementUnion)elt) ;
      
        if ( elt instanceof ElementGroup )
            return compileElementGroup((ElementGroup)elt) ;
      
        if ( elt instanceof ElementNamedGraph )
            return compileElementGraph((ElementNamedGraph)elt) ; 
      
        if ( elt instanceof ElementService )
            return compileElementService((ElementService)elt) ; 
        
        if ( elt instanceof ElementFetch )
            return compileElementFetch((ElementFetch)elt) ; 

        // This is only here for queries built programmatically
        // (triple patterns not in a group) 
        if ( elt instanceof ElementTriplesBlock )
            return compileBasicPattern(((ElementTriplesBlock)elt).getPattern()) ;
        
        // Ditto.
        if ( elt instanceof ElementPathBlock )
            return compilePathBlock(((ElementPathBlock)elt).getPattern()) ;

        if ( elt instanceof ElementSubQuery )
            return compileElementSubquery((ElementSubQuery)elt) ; 
        
        if ( elt == null )
            return OpNull.create() ;

        broken("compile(Element)/Not a structural element: "+Utils.className(elt)) ;
        return null ;
        
    }
    
    protected Op compileElementUnion(ElementUnion el)
    { 
        if ( el.getElements().size() == 1 )
        {
            Element subElt = el.getElements().get(0) ;
            return compileElement(subElt) ;
        }
        
        Op current = null ;
        
        for ( Element subElt: el.getElements() )
        {
            Op op = compileElement(subElt) ;
            current = union(current, op) ;
        }
        return current ;
    }
    
    // Produce the algebra for a single group.
    // http://www.w3.org/TR/rdf-sparql-query/#convertGraphPattern
    //
    // We do some of the steps recursively as we go along. 
    // The only step that must be done after the others to get
    // the right results is simplification.
    //
    // Step 0: (URI resolving and triple pattern syntax forms) was done during parsing
    // Step 1: (BGPs) Done in this code
    // Step 2: (Groups and unions) Was done during parsing to get ElementUnion.
    // Step 3: (GRAPH) Done in this code.
    // Step 4: (Filter extraction and OPTIONAL) Done in this code
    // Simplification: Done later 
    // If simplicifation is done now, it changes OPTIONAL { { ?x :p ?w . FILTER(?w>23) } } because it removes the
    //   (join Z (filter...)) that in turn stops the filter getting moved into the LeftJoin.  
    //   It need a depth of 2 or more {{ }} for this to happen. 

    protected Op compileElementGroup(ElementGroup groupElt)
    {
        Op current = OpTable.unit() ;
        ExprList exprList = new ExprList() ;
        
        // First: get all filters, merge adjacent BGPs. This includes BGP-FILTER-BGP
        // This is done in finalizeSyntax after which the new ElementGroup is in
        // the right order w.r.t. BGPs and filters. 
        // 
        // This is a delay from parsing time is so a printed query
        // keeps filters where the query writer put them.
        
        List<Element> groupElts = finalizeSyntax(groupElt) ;

        // Second: compile the consolidated group elements.
        for (Iterator<Element> iter = groupElts.listIterator() ; iter.hasNext() ; )
        {
            Element elt = iter.next() ;
            current = compileOneInGroup(elt, current) ;
        }
            
        // Third: Filters collected from the group. 
        if ( ! exprList.isEmpty() )
            current = OpFilter.filter(exprList, current) ;
        
        return current ;
    }

    /* Extract filters, merge adjacent BGPs.
     * Return a list of elements: update the exprList
     */
    
    private List<Element> finalizeSyntax(ElementGroup groupElt)
    {
        if ( fixedFilterPosition )
            // Illegal SPARQL
            return groupElt.getElements() ;
        
        
        List<Element> groupElts = new ArrayList<Element>() ;
        BasicPattern prev = null ;
        List<ElementFilter> filters = null ;
        PathBlock prev2 = null ;
        
        for (Element elt : groupElt.getElements() )
        {
            if ( elt instanceof ElementFilter )
            {
                ElementFilter f = (ElementFilter)elt ;
                if ( filters == null )
                    filters = new ArrayList<ElementFilter>() ;
                filters.add(f) ;
                // Collect filters but do not place them yet.
                continue ;
            }
            
            if ( elt instanceof ElementTriplesBlock )
            {
                if ( prev2 != null )
                    throw new ARQInternalErrorException("Mixed ElementTriplesBlock and ElementPathBlock (case 1)") ;
                
                ElementTriplesBlock etb = (ElementTriplesBlock)elt ;

                if ( prev != null )
                {
                    // Previous was an ElementTriplesBlock.
                    // Merge because they were adjacent in a group
                    // in syntax, so it must have been BGP, Filter, BGP.
                    // Or someone constructed a non-serializable query. 
                    prev.addAll(etb.getPattern()) ;
                    continue ;
                }
                // New BGP.
                // Copy - so that any later mergings do not change the original query. 

                ElementTriplesBlock etb2 = new ElementTriplesBlock() ;
                etb2.getPattern().addAll(etb.getPattern()) ;
                prev = etb2.getPattern() ;
                groupElts.add(etb2) ;
                continue ;
            }
            
            // TIDY UP - grr this is duplication.
            // Can't mix ElementTriplesBlock and ElementPathBlock (which subsumes ElementTriplesBlock)
            if ( elt instanceof ElementPathBlock )
            {
                if ( prev != null )
                    throw new ARQInternalErrorException("Mixed ElementTriplesBlock and ElementPathBlock (case 2)") ;
                
                ElementPathBlock epb = (ElementPathBlock)elt ;
                if ( prev2 != null )
                {
                    prev2.addAll(epb.getPattern()) ;
                    continue ;
                }
                
                ElementPathBlock epb2 = new ElementPathBlock() ;
                epb2.getPattern().addAll(epb.getPattern()) ;
                prev2 = epb2.getPattern() ;
                groupElts.add(epb2) ;
                continue ;
            }
            
            // Anything else.  End of BGP - put in any accumulated filters 
            endBGP(groupElts, filters) ;

            // Clear any BGP-related accumulators.
            filters = null ;
            prev = null ;
            prev2 = null ;
            
            // Add this element (not BGP/Filter related).
            groupElts.add(elt) ;
        }
        //End of BGP - put in any accumulated filters 
        endBGP(groupElts, filters) ;
        return groupElts ;
    }

    private void endBGP(List<Element> groupElts, List<ElementFilter> filters)
    {
        if ( filters != null )
            groupElts.addAll(filters) ;
    }
    
    private Op compileOneInGroup(Element elt, Op current)
    {
        // Replace triple patterns by OpBGP (i.e. SPARQL translation step 1)
        if ( elt instanceof ElementTriplesBlock )
        {
            ElementTriplesBlock etb = (ElementTriplesBlock)elt ;
            Op op = compileBasicPattern(etb.getPattern()) ;
            return join(current, op) ;
        }
        
        if ( elt instanceof ElementPathBlock )
        {
            ElementPathBlock epb = (ElementPathBlock)elt ;
            Op op = compilePathBlock(epb.getPattern()) ;
            
            // Not a join
            
            return join(current, op) ;
        }
        
        // Filters were collected together by finalizeSyntax.
        // So they are in the right place.
        if ( elt instanceof ElementFilter )
        {
            ElementFilter f = (ElementFilter)elt ;
            return OpFilter.filter(f.getExpr(), current) ;
        }
    
        // Optional: recurse
        if ( elt instanceof ElementOptional )
        {
            ElementOptional eltOpt = (ElementOptional)elt ;
            return compileElementOptional(eltOpt, current) ;
        }
        
        if ( elt instanceof ElementSubQuery )
        {
            ElementSubQuery elQuery = (ElementSubQuery)elt ;
            Op op = compileElementSubquery(elQuery) ;
            return join(current, op) ;
        }
        
        if ( elt instanceof ElementAssign )
        {
            ElementAssign assign = (ElementAssign)elt ;
            Op subOp = OpAssign.assign(current, assign.getVar(), assign.getExpr()) ;
            return subOp ;
        }
        
        if ( elt instanceof ElementExists )
        {
            ElementExists elt2 = (ElementExists)elt ;
            Op op = compileElementExists(current, elt2) ;
            return op ;
        }
        
        if ( elt instanceof ElementNotExists )
        {
            ElementNotExists elt2 = (ElementNotExists)elt ;
            Op op = compileElementNotExists(current, elt2) ;
            return op ;
        }
        
        if ( elt instanceof ElementMinus )
        {
            ElementMinus elt2 = (ElementMinus)elt ;
            Op op = compileElementMinus(current, elt2) ;
            return op ;
        }
        
        // All other elements: compile the element and then join on to the current group expression.
        if ( elt instanceof ElementGroup || 
             elt instanceof ElementNamedGraph ||
             elt instanceof ElementService ||
             elt instanceof ElementFetch ||
             elt instanceof ElementUnion )
        {
            Op op = compileElement(elt) ;
            return join(current, op) ;
        }
        
        broken("compile/Element not recognized: "+Utils.className(elt)) ;
        return null ;
    }

    private Op compileElementNotExists(Op current, ElementNotExists elt2)
    {
        Op op = compile(elt2.getElement()) ;    // "compile", not "compileElement" -- do simpliifcation  
        Expr expr = new E_Exists(elt2, op) ;
        expr = new E_LogicalNot(expr) ;
        return OpFilter.filter(expr, current) ;
    }

    private Op compileElementExists(Op current, ElementExists elt2)
    {
        Op op = compile(elt2.getElement()) ;    // "compile", not "compileElement" -- do simpliifcation 
        Expr expr = new E_Exists(elt2, op) ;
        return OpFilter.filter(expr, current) ;
    }

    private Op compileElementMinus(Op current, ElementMinus elt2)
    {
        Op op = compile(elt2.getMinusElement()) ;
        Op opMinus = OpMinus.create(current, op) ;
        return opMinus ;
    }

    protected Op compileElementOptional(ElementOptional eltOpt, Op current)
    {
        Element subElt = eltOpt.getOptionalElement() ;
        Op op = compileElement(subElt) ;
        
        ExprList exprs = null ;
        if ( op instanceof OpFilter )
        {
            OpFilter f = (OpFilter)op ;
            //f = OpFilter.tidy(f) ;  // Collapse filter(filter(..))
            Op sub = f.getSubOp() ;
            if ( sub instanceof OpFilter )
                broken("compile/Optional/nested filters - unfinished") ; 
            exprs = f.getExprs() ;
            op = sub ;
        }
        current = OpLeftJoin.create(current, op, exprs) ;
        return current ;
    }
    
    protected Op compileBasicPattern(BasicPattern pattern)
    {
        return new OpBGP(pattern) ;
    }
    
    protected Op compilePathBlock(PathBlock pathBlock)
    {
        // Empty path block : the parser does not generate this case.
        if ( pathBlock.size() == 0 )
            return OpTable.unit() ;

        // Always turns the most basic paths to triples.
        return PathLib.pathToTriples(pathBlock) ;
    }

    protected Op compileElementGraph(ElementNamedGraph eltGraph)
    {
        Node graphNode = eltGraph.getGraphNameNode() ;
        Op sub = compileElement(eltGraph.getElement()) ;
        return new OpGraph(graphNode, sub) ;
    }

    protected Op compileElementService(ElementService eltService)
    {
        Node serviceNode = eltService.getServiceNode() ;
        Op sub = compileElement(eltService.getElement()) ;
        return new OpService(serviceNode, sub, eltService) ;
    }
    
    private Op compileElementFetch(ElementFetch elt)
    {
        Node serviceNode = elt.getFetchNode() ;
        
        // Probe to see if enabled.
        ExtBuilder builder = OpExtRegistry.builder("fetch") ;
        if ( builder == null )
        {
            ALog.warn(this, "Attempt to use OpFetch - need to enable first with a call to OpFetch.enable()") ; 
            return OpLabel.create("fetch/"+serviceNode, OpTable.unit()) ;
        }
        Item item = Item.createNode(elt.getFetchNode()) ;
        ItemList args = new ItemList() ;
        args.add(item) ;
        return builder.make(args) ;
    }

    protected Op compileElementSubquery(ElementSubQuery eltSubQuery)
    {
        Op sub = this.compile(eltSubQuery.getQuery()) ;
        return sub ;
    }
    
    /** Compile query modifiers */
    public Op compileModifiers(Query query, Op pattern)
    {
        // Preparation: sort SELECT clause into assignments and projects.
        VarExprList projectVars = query.getProject() ;
        
        VarExprList exprs = new VarExprList() ;
        List<Var> vars = new ArrayList<Var>() ;
        
        Op op = pattern ;
        
        // ---- ToList
        if ( context.isTrue(ARQ.generateToList) )
            // Listify it.
            op = new OpList(op) ;
        
        // ---- GROUP BY
        
        if ( query.hasGroupBy() || query.getAggregators().size() > 0 )
        {
            // When there is no GroupBy but there are some aggregates, it's a group of no variables.
            op = new OpGroupAgg(op, query.getGroupBy(), query.getAggregators()) ;
            // Modified exprs.
        }
        
        //---- Assignments from SELECT and other places (TBD) (so available to ORDER and HAVING)
        // Now do assignments from expressions 
        // Must be after "group by" has intriduces it's variables.
        if ( ! projectVars.isEmpty() && ! query.isQueryResultStar())
        {
            // Don't project for QueryResultStar so initial bindings show
            // through in SELECT *
            if ( projectVars.size() == 0 && query.isSelectType() )
                ALog.warn(this,"No project variables") ;

            // Separate assignments and variable projection.
            for ( Var v : query.getProject().getVars() )
            {
                Expr e = query.getProject().getExpr(v) ;
                if ( e != null )
                {
                    // If an aggregator, then the project expression
                    // is the variable of the aggregator, not the aggregation function. 
                    if ( e instanceof E_Aggregator )
                    {
                        // Force the expression to be the variable.
                        ExprVar actualVar = new ExprVar(((E_Aggregator)e).asVar()) ;
                        e = actualVar ;
                    }
                    exprs.add(v, e) ;
                }
                // Include in project
                vars.add(v) ;
            }
        }

        
        // ---- Assignments from SELECT and other places (TBD) (so available to ORDER and HAVING)
        if ( ! exprs.isEmpty() )
            // Potential rewrites based of assign introducing aliases.
            op = OpAssign.assign(op, exprs) ;

        // ---- HAVING
        if ( query.hasHaving() )
        {
            for (Expr expr : query.getHavingExprs())
                op = OpFilter.filter(expr , op) ;    
        }
        
        // ---- ORDER BY
        if ( query.getOrderBy() != null )
            op = new OpOrder(op, query.getOrderBy()) ;
        
        // ---- PROJECT
        
        // No projection => initial variables are exposed.
        // Needed for CONSTRUCT and initial bindings + SELECT *
        
        if ( vars.size() > 0 )
            op = new OpProject(op, vars) ;
        
        // ---- DISTINCT
        if ( query.isDistinct() )
            op = new OpDistinct(op) ;
        
        // ---- REDUCED
        if ( query.isReduced() )
            op = new OpReduced(op) ;
        
        // ---- LIMIT/OFFSET
        if ( query.hasLimit() || query.hasOffset() )
            op = new OpSlice(op, query.getOffset() /*start*/, query.getLimit()/*length*/) ;
        
        return op ;
    }

    // -------- 
    
    protected Op join(Op current, Op newOp)
    { 
//        if ( current instanceof OpBGP && newOp instanceof OpBGP )
//        {
//            OpBGP opBGP = (OpBGP)current ;
//            opBGP.getPattern().addAll( ((OpBGP)newOp).getPattern() ) ;
//            return current ;
//        }
        
        if ( simplifyTooEarlyInAlgebraGeneration && applySimplification )
        {
            if ( OpJoin.isJoinIdentify(current) )
                return newOp ;
            if ( OpJoin.isJoinIdentify(newOp) )
                return current ;
        }
        
        return OpJoin.create(current, newOp) ;
    }

    protected Op sequence(Op current, Op newOp)
    {
        return OpSequence.create(current, newOp) ;
    }
    
    protected Op union(Op current, Op newOp)
    {
        return OpUnion.create(current, newOp) ;
    }
    
    private void broken(String msg)
    {
        //System.err.println("AlgebraGenerator: "+msg) ;
        throw new ARQInternalErrorException(msg) ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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