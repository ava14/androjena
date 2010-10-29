/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.openjena.atlas.lib.BitsLong ;



public class DecimalNode
{
    //private static Logger log = LoggerFactory.getLogger(DecimalNode.class) ;
    
    BigDecimal decimal = null ;
    
    // 8 bits of scale, signed 48 bits of value.
    // Not finance industry accuracy nor XSD (18 places minimum) but still useful.
    
    static final long MAX = (1L<<48) ;
    static final BigInteger MAX_I = BigInteger.valueOf(MAX) ;
    static final BigInteger MIN_I = BigInteger.valueOf(-MAX) ;
    
    // Bits counts
    static private int SCALE_LO = 56-8 ;
    static private int SCALE_HI = 56 ;    // Exclusive index

    static private int VALUE_LO = 0 ;
    static private int VALUE_HI = VALUE_LO+48 ;
    
    // Decimal precision is 47 bits (it's signed) or around 14 places.
    private int scale ;     // Limted to byte value range. +255 - -256  
    private long value ;    // 48 bits of precision (8 bits type, 8 bits scale).
    

    public static DecimalNode valueOf(BigDecimal decimal)
    {
        int scale = decimal.scale() ;
        BigInteger bigInt = decimal.unscaledValue() ;
        if ( bigInt.compareTo(MAX_I) > 0 || bigInt.compareTo(MIN_I) < 0 )
        {
            // This check also makes sure that bigInt.logValue is safe. 
            // Too big for 56 bits
            //log.warn("Value out of range: ("+decimal.scale()+","+decimal.unscaledValue()+")") ;
            return null ;
        }
        return valueOf(bigInt.longValue(), scale) ;
    }
    
    public static DecimalNode valueOf(long binValue, int scale)
    {
        if ( scale >= 128 || scale < -128 )
        {
            //log.warn("Scale out of range: ("+binValue+","+scale+")") ;
            return null ;
        }
        
        if ( Math.abs(binValue) > MAX )
        {
            //log.warn("Value out of range: ("+binValue+","+scale+")") ;
            return null ;
        }
        
        return new DecimalNode(binValue, scale) ;
    }
    
    private DecimalNode(long value, int scale)
    {
        this.scale = scale ;
        this.value = value ;
    }
    
    public long pack()  { return pack(value, scale) ; }

    public static long pack(long value, int scale)
    {
        // pack : DECIMAL , sign, scale, value
        long v = BitsLong.pack(0, NodeId.DECIMAL, 56, 64) ;
        v = BitsLong.pack(v, scale, SCALE_LO, SCALE_HI) ;
        v = BitsLong.pack(v, value, VALUE_LO, VALUE_HI) ;
        return v ;
    }

    public static DecimalNode unpack(long v)
    {
        //assert BitsLong.unpack(v, 56, 64) == NodeId.DECIMAL ;
        int scale =  (int)BitsLong.unpack(v, SCALE_LO, SCALE_HI) ;
        long value = BitsLong.unpack(v, VALUE_LO, VALUE_HI) ;
        return new DecimalNode(value, scale) ;
    }
    
    public static BigDecimal unpackAsBigDecimal(long v)
    {
        // Can I say tuples-in-java?  Or "Multiple return values"?
        //assert BitsLong.unpack(v, 56, 64) == NodeId.DECIMAL ;
        int scale =  (int)BitsLong.unpack(v, SCALE_LO, SCALE_HI) ;
        long value = BitsLong.unpack(v, VALUE_LO, VALUE_HI) ;
        // Sign extend value.
        if ( BitsLong.isSet(value, 47) )
            value = value | -1L<<48 ;
        
        return BigDecimal.valueOf(value, scale) ;
    }
    
    public BigDecimal get()
    {
        if ( decimal == null )
            decimal = BigDecimal.valueOf(value, scale) ;
        return decimal ;
    }

    @Override
    public String toString()
    {
        return get().toPlainString() ;
    }
    
    public int getScale()
    {
        return scale ;
    }

    public long getValue()
    {
        return value ;
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