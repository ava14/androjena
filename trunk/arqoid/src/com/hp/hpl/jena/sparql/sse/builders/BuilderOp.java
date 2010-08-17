/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.*;
import com.hp.hpl.jena.sparql.expr.E_Aggregator;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.sse.Tags;


public class BuilderOp
{
    // It's easier to have a object than have all statics because the
    // order of statics matters (the dispatch table gets initialized to
    // tag/null because the buildXYZ are null at that point).
    // which forces the code structure unnaturally.
    
    private static BuilderOp builderOp = new BuilderOp() ;
    
    public static Op build(Item item)
    {
        if (item.isNode() )
            BuilderLib.broken(item, "Attempt to build op structure from a plain node") ;

        if (item.isSymbol() )
            BuilderLib.broken(item, "Attempt to build op structure from a bare symbol") ;
        
        if (!item.isTagged())
            BuilderLib.broken(item, "Attempt to build op structure from a non-tagged item") ;

        return builderOp.build(item.getList()) ;
    }

    protected Map<String, Build> dispatch = new HashMap<String, Build>() ;

    public BuilderOp()
    {
        addBuild(Tags.tagBGP,           buildBGP) ;
        addBuild(Tags.tagQuadPattern,   buildQuadPattern) ;
        addBuild(Tags.tagTriple,        buildTriple) ;
        addBuild(Tags.tagTriplePath,    buildTriplePath) ;
        addBuild(Tags.tagFilter,        buildFilter) ;
        addBuild(Tags.tagGraph,         buildGraph) ;
        addBuild(Tags.tagService,       buildService) ;
        addBuild(Tags.tagProc,          buildProcedure) ;
        addBuild(Tags.tagPropFunc,      buildPropertyFunction) ;
        addBuild(Tags.tagJoin,          buildJoin) ;
        addBuild(Tags.tagSequence,      buildSequence) ;
        addBuild(Tags.tagDisjunction,   buildDisjunction) ;
        addBuild(Tags.tagLeftJoin,      buildLeftJoin) ;
        addBuild(Tags.tagDiff,          buildDiff) ;
        addBuild(Tags.tagMinus,         buildMinus) ;
        addBuild(Tags.tagUnion,         buildUnion) ;
        addBuild(Tags.tagConditional,   buildConditional) ;

        addBuild(Tags.tagToList,        buildToList) ;
        addBuild(Tags.tagGroupBy,       buildGroupBy) ;
        addBuild(Tags.tagOrderBy,       buildOrderBy) ;
        addBuild(Tags.tagProject,       buildProject) ;
        addBuild(Tags.tagDistinct,      buildDistinct) ;
        addBuild(Tags.tagReduced,       buildReduced) ;
        addBuild(Tags.tagAssign,        buildAssign) ;
        addBuild(Tags.symAssign,        buildAssign) ;
        addBuild(Tags.tagSlice,         buildSlice) ;

        addBuild(Tags.tagTable,         buildTable) ;
        addBuild(Tags.tagNull,          buildNull) ;
        addBuild(Tags.tagLabel,         buildLabel) ;
    }

    
    public static void add(String tag, Build builder)
    {
        builderOp.addBuild(tag, builder) ;
    }

    public static void remove(String tag)
    {
        builderOp.removeBuild(tag) ;
    }
    
    public static boolean contains(String tag)
    {
        return builderOp.containsBuild(tag) ;
    }
    
    // The main recursive build operation.
    private Op build(ItemList list)
    {
        Item head = list.get(0) ;
        String tag = head.getSymbol() ;

        Build bob = findBuild(tag) ;
        if ( bob != null )
            return bob.make(list) ;
        else
            BuilderLib.broken(head, "Unrecognized algebra operation: "+tag) ;
        return null ;
    }

    public static BasicPattern buildBGP(Item item)
    {
        if ( ! item.isTagged(Tags.tagBGP) )
            BuilderLib.broken(item, "Not a basic graph pattern") ;
        if ( ! item.isList() )
            BuilderLib.broken(item, "Not a list for a basic graph pattern") ;
        ItemList list = item.getList() ;
        return buildBGP(list) ;
        
    }
    
    private static BasicPattern buildBGP(ItemList list)
    {
        // Skips the tag.
        BasicPattern triples = new BasicPattern() ;
        for ( int i = 1 ; i < list.size() ; i++ )
        {
            Item item = list.get(i) ;
            if ( ! item.isList() )
                BuilderLib.broken(item, "Not a triple structure") ;
            Triple t = BuilderGraph.buildTriple(item.getList()) ;
            triples.add(t) ; 
        }
        return triples ;
    }
    
    public static Expr buildExpr(Item item)
    {
        return BuilderExpr.buildExpr(item) ;
    }

    // Build a list of expressions.
    public static List<Expr> buildExpr(ItemList list)
    {
        List<Expr> x = new ArrayList<Expr>() ;
        for ( int i = 0 ; i < list.size() ; i++ )
        {
            Item itemExpr = list.get(i) ;
            Expr expr = buildExpr(itemExpr) ;
            x.add(expr) ;
        }
        return x ;
    }


    protected Op build(ItemList list, int idx)
    {
        return build(list.get(idx).getList()) ;
    }

    // <<<< ---- Coordinate these 
    // Lowercase on insertion?
    protected void addBuild(String tag, Build builder)
    {
        dispatch.put(tag, builder) ;
    }
    
    protected void removeBuild(String tag)
    {
        dispatch.remove(tag) ;
    }
    
    protected boolean containsBuild(String tag)
    {
        return findBuild(tag) != null ;
        
    }

    protected Build findBuild(String str)
    {
        for ( Iterator<String> iter = dispatch.keySet().iterator() ; iter.hasNext() ; )
        {
            String key = iter.next() ; 
            if ( str.equalsIgnoreCase(key) )
                return dispatch.get(key) ;
        }
        return null ;
    }

    // >>>> ----
    
    static public interface Build { Op make(ItemList list) ; }

    // Not static.  The initialization through the singleton would not work
    // (static initialization order - these operations would need to go
    // before the singelton. 
    // Or assign null and create object on first call but that breaks add/remove
    final protected Build buildTable = new Build()
    {
        public Op make(ItemList list)
        {
            Item t = Item.createList(list) ;
            Table table = BuilderTable.build(t) ; 
            return OpTable.create(table) ;
        }
    } ;

    final protected Build buildBGP = new Build()
    {
        public Op make(ItemList list)
        {
            BasicPattern triples = buildBGP(list) ;
            return new OpBGP(triples) ;
        }
    } ;

    final protected Build buildQuadPattern = new Build()
    {
        public Op make(ItemList list)
        {
            Node g = null ;
            BasicPattern bp = new BasicPattern() ;
            for ( int i = 1 ; i < list.size() ; i++ )
            {
                Item item = list.get(i) ;
                if ( ! item.isList() )
                    BuilderLib.broken(item, "Not a quad structure") ;
                Quad q = BuilderGraph.buildQuad(item.getList()) ;
                if ( g == null )
                    g = q.getGraph() ;
                else
                {
                    if ( ! g.equals(q.getGraph()) )
                        BuilderLib.broken(item, "Quad has different graph node in quadapttern: "+q) ;
                }
                bp.add(q.asTriple()) ;
                
            }
            
            OpQuadPattern op = new OpQuadPattern(g, bp) ;
            return op ;
        }
    } ;

    final protected Build buildTriple = new Build(){
        public Op make(ItemList list)
        {
            Triple t = BuilderGraph.buildTriple(list) ;
            return new OpTriple(t) ;
        }} ;
    
    
    final protected Build buildTriplePath = new Build(){
        public Op make(ItemList list)
        {
            TriplePath tp = BuilderPath.buildTriplePath(list) ;
            return new OpPath(tp) ;
        }} ;
    
    final protected Build buildFilter = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "Malformed filter") ;
            Item itemExpr = list.get(1) ;
            Item itemOp = list.get(2) ;

            Op op = build(itemOp.getList()) ;
            ExprList exprList = BuilderExpr.buildExprOrExprList(itemExpr) ;
            return OpFilter.filter(exprList, op) ;
        }
    } ;

    final protected Build buildJoin = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "Join") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpJoin.create(left, right) ;
            return op ;
        }
    } ;

    // Add all the operations from the list to the OpN
    final private void addOps(OpN op, ItemList list)
    {
        for ( int i = 1 ; i < list.size() ; i++ )
        {
            Op sub = build(list, i) ;
            op.add(sub) ;
        }
    }

    final protected Build buildSequence = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(2, list, "Sequence") ;
            OpSequence op = OpSequence.create() ;
            addOps(op, list) ;
            return op ;
        }
    } ;
    
    final protected Build buildDisjunction = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLengthAtLeast(2, list, "Disjunction") ;
            OpDisjunction op = OpDisjunction.create() ;
            addOps(op, list) ;
            return op ;
        }
    } ;

    final protected Build buildLeftJoin = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list, "leftjoin: wanted 2 or 3 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Expr expr = null ;
            if ( list.size() == 4 ) 
                expr = buildExpr(list.get(3)) ;
            Op op = OpLeftJoin.create(left, right, expr) ;
            return op ;
        }
    } ;

    final protected Build buildDiff = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list, "diff: wanted 2 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpDiff.create(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildMinus = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list, "minus: wanted 2 arguments") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = OpMinus.create(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildUnion = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "union") ;
            Op left = build(list, 1) ;
            Op right  = build(list, 2) ;
            Op op = new OpUnion(left, right) ;
            return op ;
        }
    } ;
    
    final protected Build buildConditional = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, 3, list, "condition") ;
            Op left = build(list, 1) ;
            // No second argument means unit.
            Op right = OpTable.unit() ;
            if ( list.size() != 2 )
                right  = build(list, 2) ;
            Op op = new OpConditional(left, right) ;
            return op ;
        }
    } ;

    final protected Build buildGraph = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "graph") ;
            Node graph = BuilderNode.buildNode(list.get(1)) ;
            Op sub  = build(list, 2) ;
            return new OpGraph(graph, sub) ;
        }
    } ;

    final protected Build buildService = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "service") ;
            Node service = BuilderNode.buildNode(list.get(1)) ;
            if ( ! service.isURI() && ! service.isVariable() )
                BuilderLib.broken(list, "Service must provide a URI or variable") ;
            Op sub  = build(list, 2) ;
            return new OpService(service, sub) ;
        }
    } ;
    
    final protected Build buildProcedure = new Build()
    {
        public Op make(ItemList list)
        {
            // (proc <foo> (args) form)
            BuilderLib.checkLength(4, list, "proc") ;
            Node procId = BuilderNode.buildNode(list.get(1)) ;
            if ( ! procId.isURI() )
                BuilderLib.broken(list, "Procedure name must be a URI") ;
            ExprList args = BuilderExpr.buildExprOrExprList(list.get(2)) ;
            Op sub  = build(list, 3) ;
            return new OpProcedure(procId, args, sub) ;
        }

    } ;

    final protected Build buildPropertyFunction = new Build()
    {
        public Op make(ItemList list)
        {
            // (proc <foo> (subject args) (object args) form)
            BuilderLib.checkLength(5, list, "propfunc") ;
            Node property = BuilderNode.buildNode(list.get(1)) ;
            
            if ( ! property.isURI() )
                BuilderLib.broken(list, "Property function name must be a URI") ;

            PropFuncArg subjArg = readPropFuncArg(list.get(2)) ;
            PropFuncArg objArg = readPropFuncArg(list.get(3)) ;
            Op sub  = build(list, 4) ;
            return new OpPropFunc(property, subjArg, objArg, sub) ;
        }
    } ;
    
    static final private PropFuncArg readPropFuncArg(Item item)
    {
        if ( item.isNode() )
            return new PropFuncArg(BuilderNode.buildNode(item)) ;
        if ( item.isList() )
            return new PropFuncArg(BuilderNode.buildNodeList(item)) ;
        BuilderLib.broken(item, "Expected a property function argument (node or list of nodes") ;
        return null ;
    }

    final protected Build buildToList = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "tolist") ;
            Op sub = build(list, 1) ;
            Op op = new OpList(sub) ;
            return op ;
        }
    } ;


    final protected Build buildGroupBy = new Build()
    {
        // See buildProject
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, 4, list,  "Group") ;
            // GroupBy
            VarExprList vars = BuilderExpr.buildNamedExprList(list.get(1).getList()) ;
            List<E_Aggregator> aggregators = new ArrayList<E_Aggregator>() ;
            
            if ( list.size() == 4 )
            {
                // Aggregations : assume that the exprs are legal.
                VarExprList y = BuilderExpr.buildNamedExprList(list.get(2).getList()) ;

                // Aggregations need to know the name of the variable they are associated with
                // (so it can be set by the aggregation calculation)
                // Bind aggregation to variable
                for (  Entry<Var, Expr> entry : y.getExprs().entrySet() )
                {
                    if ( ! ( entry.getValue() instanceof E_Aggregator ) )
                        BuilderLib.broken(list, "Not a aggregate expression: "+entry.getValue()) ;
                    E_Aggregator eAgg = (E_Aggregator)entry.getValue() ;
                    eAgg.setVar(entry.getKey()) ;
                    aggregators.add(eAgg) ;    
                }
            }
            
            Op sub = build(list, list.size()-1) ;
            Op op = new OpGroupAgg(sub,vars, aggregators) ;
            return op ;
        }
    } ;


    final protected Build buildOrderBy = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list,  "Order") ;
            ItemList conditions = list.get(1).getList() ;
            
            // Maybe tagged (asc, desc or a raw expression)
            List<SortCondition> x = new ArrayList<SortCondition>() ;
            
            for ( int i = 0 ; i < conditions.size() ; i++ )
            {
                //int direction = Query.ORDER_DEFAULT ;
                Item item = conditions.get(i) ;
                SortCondition sc = scBuilder(item) ;
                x.add(sc) ;
            }
            Op sub = build(list, 2) ;
            Op op = new OpOrder(sub, x) ;
            return op ;
        }
    } ;

    SortCondition scBuilder(Item item)
    {
        int direction = Query.ORDER_DEFAULT ;
        if ( item.isTagged("asc") || item.isTagged("desc") )
        {
            BuilderLib.checkList(item) ;
            BuilderLib.checkLength(2, item.getList(), "Direction corrupt") ;
            if ( item.isTagged("asc") )
                direction = Query.ORDER_ASCENDING ;
            else
                direction = Query.ORDER_DESCENDING ;
            item = item.getList().get(1) ; 
        }
        Expr expr = BuilderExpr.buildExpr(item) ;
        if ( expr.isVariable() )
            return  new SortCondition(expr.getExprVar().asVar(), direction) ;
        else
            return new SortCondition(expr, direction) ;
    }
    
    
    final protected Build buildProject = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "project") ;
            List<Var> x = BuilderNode.buildVars(list.get(1).getList()) ; 
            Op sub = build(list, 2) ;
            return new OpProject(sub, x) ;
        }
    } ;

    
    final protected Build buildDistinct = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "distinct") ;
            Op sub = build(list, 1) ;
            return new OpDistinct(sub) ;
        }
    } ;

    final protected Build buildReduced = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, list, "reduced") ;
            Op sub = build(list, 1) ;
            return new OpReduced(sub) ;
        }
    } ;

    final protected Build buildAssign = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(3, list, "assign") ;
            VarExprList x = BuilderExpr.buildNamedExprOrExprList(list.get(1)) ; 
            Op sub = build(list, 2) ;
            return OpAssign.assign(sub, x) ;
        }
    } ;

    

    final protected Build buildSlice = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(4, list, "slice") ;
            long start = BuilderNode.buildInt(list, 1, -1) ;
            long length = BuilderNode.buildInt(list, 2, -1) ;

            if ( start == -1 )
                start = Query.NOLIMIT ;
            if ( length == -1 )
                length = Query.NOLIMIT ;

            Op sub = build(list, 3) ;
            return new OpSlice(sub, start, length) ;
        }
    } ;

    final protected Build buildNull = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(1, list, Tags.tagNull) ;
            return OpNull.create() ;
        }
    } ;

    final protected Build buildLabel = new Build()
    {
        public Op make(ItemList list)
        {
            BuilderLib.checkLength(2, 3, list, Tags.tagLabel) ;
            Item label = list.get(1) ;
            Object str = null ;
            if ( label.isSymbol() )
                str = label.getSymbol() ;
            else if ( label.isNode() )
            {
                if ( label.getNode().isLiteral() )
                {
                    if ( label.getNode().getLiteralLanguage() == null ||
                        label.getNode().getLiteralLanguage().equals("") ) ;
                    str = label.getNode().getLiteralLexicalForm() ;
                }
                else
                    str = label.getNode() ;
            }
            else
                BuilderLib.broken("No a symbol or a node") ;
            
            if ( str == null )
                str = label.toString() ;
            
            Op op = null ;
            
            if ( list.size() == 3 )
                op = build(list, 2) ;
            return OpLabel.create(str, op) ;
//            if ( op == null )
//                return new OpLabel(str) ;
//            else
//                return new OpLabel(str , op) ;
        }
    } ;
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
