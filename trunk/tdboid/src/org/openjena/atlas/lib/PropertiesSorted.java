/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.util.* ;

/** Sorted output */
public class PropertiesSorted extends Properties
{
    private Comparator<String> comparator = null ;
    
    //public SortedProperties() { super() ; }
    
    public PropertiesSorted(Comparator<String> comparator)
    { 
        super() ;
        this.comparator = comparator ;
    }
    
    
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Enumeration<Object> keys()
    {
        // Old world - enumeration, untyped. But we know they are strings (Propetries hides non-strings in get) 
        Enumeration keys = super.keys() ;
        List<String> keys2 = new ArrayList<String>(super.size()) ;
        
        for( ; keys.hasMoreElements() ; )
        {
            Object obj = keys.nextElement() ;
            if ( obj instanceof String )
                keys2.add((String)obj);
        }
        // Keys are comparable because they are strings.
        if ( comparator == null )
            Collections.sort(keys2);
        else
            Collections.sort(keys2, comparator) ;
        
        return new IteratorToEnumeration(keys2.listIterator()) ;
    }
    
    static class IteratorToEnumeration<T>  implements Enumeration<T>
    {
        private Iterator<T> iterator ;

        public IteratorToEnumeration(Iterator<T> iterator)
        {
            this.iterator = iterator ;
        }
        
        public boolean hasMoreElements()
        {
            return iterator.hasNext() ;
        }

        public T nextElement()
        {
            return iterator.next();
        }
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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