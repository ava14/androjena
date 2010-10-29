/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/** Byte-oriented operations.  Packing and unpacking integers
 *  is in network order (Big endian - which is the preferred order in Java)
 *  {@link "http://en.wikipedia.org/wiki/Endianness"}
 */  

public class Bytes
{
    private Bytes() {}
    
    // Binary search - see atlas.lib.Alg
    
    // http://en.wikipedia.org/wiki/Endianness
    // Java is, by default, network order (big endian)
    // i.e what you get from ByteBuffer.allocate/.allocateDirect();
    
    public static void main(String ... args)
    {
        ByteBuffer bb = ByteBuffer.allocate(8) ;
        System.out.println("Native order = "+ByteOrder.nativeOrder()) ;
        System.out.println("Default order = "+bb.order()) ;
        //bb.order(ByteOrder.BIG_ENDIAN) ;
        //bb.order(ByteOrder.LITTLE_ENDIAN) ;
        System.out.println("Order = "+bb.order()) ;
        bb.asLongBuffer().put(0x0102030405060708L) ;
        for ( int i = 0 ; i < bb.capacity(); i++ )
            System.out.printf("0x%02X ",bb.get(i)) ;
        // Comes out hight to low : 0x01 0x02 0x03 0x04 0x05 0x06 0x07 0x08 
    }
    
    /** Compare two byte arrays which may be of different lengths */ 
    public static int compare(byte[] x1, byte[] x2)
    {
        int n = Math.min(x1.length, x2.length) ;
        
        for ( int i = 0 ; i < n ; i++ )
        {
            byte b1 = x1[i] ;
            byte b2 = x2[i] ;
            if ( b1 == b2 )
                continue ;
            // Treat as unsigned values in the bytes. 
            return (b1&0xFF) - (b2&0xFF) ;  
        }

        return x1.length - x2.length ;
    }

    public static int compareByte(byte b1, byte b2)
    {
        return (b1&0xFF) - (b2&0xFF) ;  
    }
    
    public static byte[] copyOf(byte[] bytes)
    {
        // Java6: Arrays.copyOf(bytes, bytes.length)
        return copyOf(bytes, 0, bytes.length) ;
//        byte[] newByteArray = new byte[bytes.length] ;
//        System.arraycopy(bytes, 0, newByteArray, 0, bytes.length) ;
//        return newByteArray ;
    }
    
    public static byte[] copyOf(byte[] bytes, int start)
    {
        return copyOf(bytes, start, bytes.length-start) ;
    }
    
    public static byte[] copyOf(byte[] bytes, int start, int length)
    {
        byte[] newByteArray = new byte[length] ;
        System.arraycopy(bytes, start, newByteArray, 0, length) ;
        return newByteArray ;
    }
    
    final public static byte[] hexDigits = {
        '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' , '8' ,
        '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F' 
//         , 'g' , 'h' ,
//        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
//        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
//        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
        };
    
    /** Get an int from a byte array (network order)
     * @param b Byte Array
     */
    public static final int getInt(byte[]b)
    { return getInt(b, 0) ; }
    
    /** Get an int from a byte array (network order)
     * @param b Byte Array
     * @param idx Starting point of bytes 
     */
    public static final int getInt(byte[]b, int idx)
    {
        return assembleInt(b[idx+0],
                           b[idx+1],
                           b[idx+2],
                           b[idx+3]) ;
    }

    /** Get a long from a byte array (network order)
     * @param b Byte Array
     */
    public static final long getLong(byte[]b)
    { return getLong(b, 0) ; }
    
    /** Get a long from a byte array (network order)
     * @param b Byte Array
     * @param idx Starting point of bytes 
     */
    public static final long getLong(byte[]b, int idx)
    {
        return assembleLong(b[idx+0],
                            b[idx+1],
                            b[idx+2],
                            b[idx+3],
                            b[idx+4],
                            b[idx+5],
                            b[idx+6],
                            b[idx+7]) ;

    }

    /** Put an int into a byte array
     * @param value The integer
     * @param b byte array 
     */
    public static final void setInt(int value, byte[]b)
    { setInt(value, b, 0) ; }
    
    /** Put an int into a byte array from a given position
     * @param x The integer
     * @param b byte array 
     * @param idx starting point
     */
    public static final void setInt(int x, byte[]b, int idx)
    {
//        b[idx+0] = byte3(value) ;
//        b[idx+1] = byte2(value) ;
//        b[idx+2] = byte1(value) ;
//        b[idx+3] = byte0(value) ;
      b[idx+0] = (byte)((x >> 24)&0xFF) ;
      b[idx+1] = (byte)((x >> 16)&0xFF);
      b[idx+2] = (byte)((x >>  8)&0xFF);
      b[idx+3] = (byte)(x &0xFF);

    }
    
    
    /** Put a long into a byte array
     * @param value The integer
     * @param b byte array 
     */
    public static final void setLong(long value, byte[]b)
    { setLong(value, b, 0) ; }
    
    /** Put a long into a byte array from a given position
     * @param value The integer
     * @param b byte array 
     * @param idx starting point
     */
    public static final void setLong(long value, byte[]b, int idx)
    {
        int lo = (int)(value&0xFFFFFFFFL) ;
        int hi = (int)(value>>>32) ;
        setInt(hi, b, idx) ;
        setInt(lo, b, idx+4) ;
    }

    /** int to byte array */
    public static byte[] packInt(int val)
    {
        byte[] valBytes = new byte[Integer.SIZE/Byte.SIZE] ;
        setInt(val, valBytes, 0) ;
        return valBytes ;
    }
    
    /** long to byte array */
    public static byte[] packLong(long val)
    {
        byte[] valBytes = new byte[Long.SIZE/Byte.SIZE] ;
        setLong(val, valBytes, 0) ;
        return valBytes ;
    }
    
    /** Make an int order of args -- high to low */
    static private int assembleInt(byte b3, byte b2, byte b1, byte b0)
    {
        return ( ((b3 & 0xFF) << 24) |
                      ((b2 & 0xFF) << 16) |
                      ((b1 & 0xFF) <<  8) |
                      ((b0 & 0xFF) <<  0)
                    );
    }

    /** Make a long order of args -- high to low */
    static private Long assembleLong(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0)
    {
        
        return  (((long)b7 & 0xFF) << 56) |
                (((long)b6 & 0xFF) << 48) |
                (((long)b5 & 0xFF) << 40) |
                (((long)b4 & 0xFF) << 32) |
                (((long)b3 & 0xFF) << 24) |
                (((long)b2 & 0xFF) << 16) |
                (((long)b1 & 0xFF) <<  8) |
                (((long)b0 & 0xFF) <<  0) ;
    }

    private static byte byte3(int x) { return (byte)(x >> 24); }
    private static byte byte2(int x) { return (byte)(x >> 16); }
    private static byte byte1(int x) { return (byte)(x >>  8); }
    private static byte byte0(int x) { return (byte)(x >>  0); }

    /** Return the UTF-8 bytes for a string */
    public static byte[] string2bytes(String x)
    {
        try
        {
            return x.getBytes("UTF-8") ;
        } catch (UnsupportedEncodingException ex)
        {
            // Impossible.
            ex.printStackTrace();
            return null ;
        }
    }
    
    /** Return the string for some UTF-8 bytes */
    public static String bytes2string(byte[] x)
    {
        try
        {
            return new String(x, "UTF-8") ;
        } catch (UnsupportedEncodingException ex)
        {
            // Impossible-ish.
            ex.printStackTrace();
            return null ;
        }
    }

    /** Encode a string into a ByteBuffer : on return position is the end of the encoding */
    public static int toByteBuffer(CharSequence s, ByteBuffer bb)
    {
        CharsetEncoder enc = Chars.getEncoder();

        // Blocking finite Pool - does not happen.
        // Plain Pool (sync wrapped) - might - allocate an extra one. 
        if ( enc == null ) 
            enc = Chars.createEncoder() ;
        //        enc = enc.onMalformedInput(CodingErrorAction.REPLACE)
        //                 .onUnmappableCharacter(CodingErrorAction.REPLACE);

        int x = toByteBuffer(s, bb, enc) ;
        Chars.putEncoder(enc) ;
        return x ;
    }
    
    /** Encode a string into a ByteBuffer : on return position is the end of the encoding */
    public static int toByteBuffer(CharSequence s, ByteBuffer bb, CharsetEncoder enc)
    {
        int start = bb.position() ;
        CharBuffer cBuff = CharBuffer.wrap(s);
        enc.reset();
        CoderResult r = enc.encode(cBuff, bb, true) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("Bytes.toByteBuffer: encode overflow (1)") ;
        r = enc.flush(bb) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("Bytes.toByteBuffer: encode overflow (2)") ;
//        if ( r.isUnderflow() )
//            throw new InternalErrorException("Bytes.toByteBuffer: encode underflow") ;
        int finish = bb.position() ;
        return finish-start ;
    }
    
    /** Decode a string into a ByteBuffer */
    public static String fromByteBuffer(ByteBuffer bb)
    {
        CharsetDecoder dec = Chars.getDecoder();
        if ( dec == null )
            dec = Chars.createDecoder() ;
        String x = fromByteBuffer(bb, dec) ;
        Chars.putDecoder(dec) ;
        return x ;
    }
    
    /** Decode a string into a ByteBuffer */
    public static String fromByteBuffer(ByteBuffer bb, CharsetDecoder dec)
    {
        if ( bb.remaining() == 0 )
            return "" ;

        dec.reset();
        CharBuffer cBuff = CharBuffer.allocate(bb.remaining()) ;
        CoderResult r = dec.decode(bb, cBuff, true) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("fromByteBuffer: decode overflow (1)") ;
        r = dec.flush(cBuff) ;
        if ( r.isOverflow() )
            throw new InternalErrorException("fromByteBuffer: decode overflow (2)") ;
        cBuff.flip();
        return cBuff.toString() ;
    }

    /** Return a hex string representing the bytes, zero padded to length of byte array. */
    public static String asHex(byte[] bytes)
    {
        return asHex(bytes, 0, bytes.length) ; 
    }
    
    public static String asHex(byte[] bytes, int start, int finish)
    {
        StringBuilder sw = new StringBuilder() ;
        for ( int i = start ; i < finish ; i++ )
        {
            byte b = bytes[i] ;
            int hi = (b & 0xF0) >> 4 ;
            int lo = b & 0xF ;
            if ( i != start ) sw.append(' ') ;
            sw.append(Chars.hexDigits[hi]) ;
            sw.append(Chars.hexDigits[lo]) ;
        }
        return sw.toString() ;
    }
    
    public static int hexCharToInt(char c)
    {
        if ( '0' <= c && c <= '9' )   
            return c-'0' ;
        else if ( 'A' <= c && c <= 'F' )
            return c-'A'+10 ;
        else if ( 'a' <= c && c <= 'f' )
            return c-'a'+10 ;
        else
            throw new IllegalArgumentException("Bad index char : "+c) ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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