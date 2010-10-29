/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.math.BigDecimal;
import java.nio.ByteBuffer;

import org.openjena.atlas.lib.BitsLong ;
import org.openjena.atlas.lib.Bytes ;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

import com.hp.hpl.jena.sparql.core.NodeConst;
import com.hp.hpl.jena.sparql.util.ALog;

import com.hp.hpl.jena.tdb.TDBException;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

final
public class NodeId
{
    // SPECIALs - never stored.
    public static final NodeId NodeDoesNotExist = new NodeId(-8) ;
    public static final NodeId NodeIdAny = new NodeId(-9) ;
    
//    public static final NodeId n0 = new NodeId(0) ; 
//    public static final NodeId n1 = new NodeId(1) ; 
//    public static final NodeId n2 = new NodeId(2) ; 
//
//    public static final NodeId n3 = new NodeId(3) ; 
//    public static final NodeId n4 = new NodeId(4) ; 
//    public static final NodeId n5 = new NodeId(5) ; 

    
    // NB If there is any sort of cache with a NodeId in it, then there is an object created
    // by boxing anyway (unless swap to using Trove with it's hardcoded int/long implementation)
    // Therefore the cost of a NodeId is not as great as it might be.
    // Could recycle them (but the value field wil not be final) 
    
    private static boolean enableInlineLiterals = true ;
    
    public static final int SIZE = SystemTDB.SizeOfLong ;
    final long value ;
    
    public static NodeId create(long value)
    {
        // All creation of NodeIds must go through this.
        if ( value == NodeDoesNotExist.value )
            return NodeDoesNotExist ;
        if ( value == NodeIdAny.value )
            return NodeIdAny ;
        return new NodeId(value) ;
    }
    
    public static NodeId create(byte[] b)       { return create(b, 0) ; } 
    public static NodeId create(ByteBuffer b)   { return create(b, 0) ; } 
    
    // Chance for a cache? (Small Java objects are really not that expensive these days.)
    public static NodeId create(byte[] b, int idx)
    {
        long value = Bytes.getLong(b, idx) ;
        return create(value) ;
    }
    
    public static NodeId create(ByteBuffer b, int idx)
    {
        long value = b.getLong(idx) ;
        return create(value) ;
    }
    public NodeId(long v) { value = v ;}
    
    public void toByteBuffer(ByteBuffer b, int idx) { b.putLong(idx, value) ; }
    
    public void toBytes(byte[] b, int idx) { Bytes.setLong(value, b, idx) ; }
 
    public boolean isDirect() { return type() != NONE && type() != SPECIAL ; }
                                                       
    public int type()
    {
        return (int)BitsLong.unpack(value, 56, 64) ;
    }

    static long setType(long value, int type)
    {
        return BitsLong.pack(value, type, 56, 64) ;
    }

    
    // Masked?
    public long getId()     { return value ; }
    
    @Override
    public int hashCode()
    { 
        // Ensure the type byte has an effect on the bottom 32 bits.
        return ((int)value) ^ ((int)(value >> 32)) ; 
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( !(other instanceof NodeId ) ) return false ;
        return value == ((NodeId)other).value ;
    }
    
    @Override
    public String toString()
    { 
        if ( this == NodeDoesNotExist ) return "[DoesNotExist]" ;
        if ( this == NodeIdAny ) return "[Any]" ;
        
        return String.format("[%016X]", value) ; 
    }
    
    // ---- Encoding special - inlines.
    /* The long is formated as:
     * 8 bits of type
     * 56 bits of value
     * 
     * (potential change
     *   1 bit: 0 => reference, 1 => inline
     *   7 bits of inline type.
     *   56 bits fo value)  
     *  
     *  Type 0 means the node is in the object table.
     *  Types 1+ store the value of the node in the 56 bits remaining.
     *  
     *  If a value would not fit, it will be stored externally so there is no
     *  guarantee that all integers, say, are store inline. 
     *  
     *  Integer format: signed 56 bit number.
     *  Decimal format: 8 bits scale, 48bits of signed valued.

     *  Date format:
     *  DateTime format:
     *  Boolean format:
     */
    
    // Type codes.
    // Better would be high bit 1 => encoded value.
    // enums.
    public static final int NONE               = 0 ;
    public static final int INTEGER            = 1 ;
    public static final int DECIMAL            = 2 ;
    public static final int DATE               = 3 ;
    public static final int DATETIME           = 4 ;
    public static final int BOOLEAN            = 5 ;
    public static final int SHORT_STRING       = 6 ;
    public static final int SPECIAL            = 0xFF ;
    
    /** Encode a node as an inline literal.  Return null if it can't be done */
    public static NodeId inline(Node node)
    {
        if ( node == null )
        {
            ALog.warn(NodeId.class, "Null node: "+node) ;
            return null ;
        }
        
        if ( ! node.isLiteral() ) return null ;
        if ( node.getLiteralDatatype() == null ) return null ;
        
        if ( ! enableInlineLiterals ) return null ;
        
        LiteralLabel lit = node.getLiteral() ;
        
        // Decimal is a valid supertype of integer but we handle integers and decimals differently.
        
        if ( node.getLiteralDatatype().equals(XSDDatatype.XSDdecimal) )
        {
            // Check lexical form.
            if ( ! XSDDatatype.XSDdecimal.isValidLiteral(lit) ) 
                return null ;
            
            BigDecimal decimal = new BigDecimal(lit.getLexicalForm()) ;
            // Does range checking.
            DecimalNode dn = DecimalNode.valueOf(decimal) ;
            if ( dn != null )
                // setType
                return new NodeId(dn.pack()) ;
            else
                return null ;
        }
        else    // Not decimal.
        {
            if ( XSDDatatype.XSDinteger.isValidLiteral(lit) )
            {
                long v = ((Number)lit.getValue()).longValue() ;
                v = IntegerNode.pack(v) ;
                // Value -1 is "does not fit"
                if ( v != -1 )
                    return new NodeId(v) ;
                else
                    return null ;
            }
        }
        
        if ( XSDDatatype.XSDdateTime.isValidLiteral(lit) ) 
        {
            long v = DateTimeNode.packDateTime(lit.getLexicalForm()) ;
            if ( v == -1 )
                return null ; 
            v = setType(v, DATETIME) ; 
            return new NodeId(v) ;
        }
        
        if ( XSDDatatype.XSDdate.isValidLiteral(lit) )
        {
            long v = DateTimeNode.packDate(lit.getLexicalForm()) ;
            if ( v == -1 )
                return null ; 
            v = setType(v, DATE) ; 
            return new NodeId(v) ;
        }
        
        if ( XSDDatatype.XSDboolean.isValidLiteral(lit) )
        {
            long v = 0 ;
            boolean b = ((Boolean)lit.getValue()).booleanValue() ;
            //return new NodeValueBoolean(b, node) ;
            v = setType(v, BOOLEAN) ;
            if ( b )
                v = v | 0x01 ;
            return new NodeId(v) ;
        }
        
        return null ;
    }
    
    /** Decode an inline nodeID, return null if not an inline node */
    public static Node extract(NodeId nodeId)
    {
        if ( nodeId == NodeId.NodeDoesNotExist )
            return null ;
        
        long v = nodeId.value ;
        int type = nodeId.type() ;

        switch (type)
        {
            case NONE:      return null ;
            case SPECIAL:   return null ;
                
            case INTEGER:
            {
                long val = IntegerNode.unpack(v) ;
//                
//                long val = BitsLong.clear(v, 56, 64) ;
//                // Sign extends to 64 bits.
//                if ( BitsLong.isSet(val, 55) )
//                    val = BitsLong.set(v, 56, 64) ;
                Node n = Node.createLiteral(Long.toString(val), null, XSDDatatype.XSDinteger) ;
                return n ;
            }
            case DECIMAL:
            {
                BigDecimal d = DecimalNode.unpackAsBigDecimal(v) ;
                String x = d.toEngineeringString() ;
                return Node.createLiteral(x, null, XSDDatatype.XSDdecimal) ;
            }

            case DATETIME:
            {
                long val = BitsLong.clear(v, 56, 64) ;
                String lex = DateTimeNode.unpackDateTime(val) ; 
                return Node.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
            }
            case DATE:
            {
                long val = BitsLong.clear(v, 56, 64) ;
                String lex = DateTimeNode.unpackDate(val) ;
                return Node.createLiteral(lex, null, XSDDatatype.XSDdate) ;
            }
            case BOOLEAN:
            {
                long val = BitsLong.clear(v, 56, 64) ;
                if ( val == 0 ) return NodeConst.nodeFalse ; 
                if ( val == 1 ) return NodeConst.nodeTrue ;
                throw new TDBException("Unrecognized boolean node id : "+val) ;
            }
            default:
                throw new TDBException("Unrecognized node id type: "+type) ;
        }
    }
    
    public static final boolean isAny(NodeId nodeId) { return nodeId == NodeIdAny || nodeId == null ; }
    public static final boolean doesNotExist(NodeId nodeId) { return nodeId == NodeDoesNotExist ; }
    
    //public reset(long value) { this.value = value ; }
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