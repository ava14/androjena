/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import org.openjena.atlas.lib.cache.* ;


public class CacheFactory
{
    public static <Key, Value> Cache<Key, Value> createCache(Getter<Key, Value> getter, int maxSize)
    {
        Cache<Key, Value> cache = createCache(0.75f, maxSize) ;
        return createCacheWithGetter(cache, getter) ;
    }

    public static <Key, Value> Cache<Key, Value> createCache(int maxSize)
    {
        return createCache(0.75f, maxSize) ;
    }
    
    public static <Key, Value> Cache<Key, Value> createCacheUnbounded()
    {
        return new CacheUnbounded<Key, Value>() ;
    }
    
    public static <Key, Value> Cache<Key, Value> createCache(float loadFactor, int maxSize)
    {
        return new CacheLRU<Key, Value>(0.75f, maxSize) ;
    }
    
    public static <Key, Value> Cache<Key, Value> createCacheWithGetter(Cache<Key, Value> cache, Getter<Key, Value> getter)
    {
        return new CacheWithGetter<Key, Value>(cache, getter) ;
    }

    public static <Key, Value> Cache<Key, Value> createSimpleCache(int size)
    {
        return new CacheSimple<Key, Value>(size) ; 
    }
    
    public static <Key, Value> CacheStats<Key, Value> createStats(Cache<Key, Value> cache)
    {
        if ( cache instanceof CacheStats<?,?>)
            return (CacheStats<Key, Value>) cache ;
        return new CacheStatsAtomic<Key, Value>(cache) ;
    }

    public static <Key, Value> Cache<Key, Value> createSync(Cache<Key, Value> cache)
    {
        if ( cache instanceof CacheSync<?,?>)
            return cache ;
        return new CacheSync<Key, Value>(cache) ;
    }

    
    public static <Obj> CacheSet<Obj> createCacheSet(int size)
    {
        return new CacheSetLRU<Obj>(size) ;
    }

    public static <Obj> CacheSet<Obj> createSync(CacheSet<Obj> cache)
    {
        if ( cache instanceof CacheSetSync<?>)
            return cache ;
        return new CacheSetSync<Obj>(cache) ;
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