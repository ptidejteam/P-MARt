package org.apache.velocity.runtime.resource;

/*
 * Copyright 2000-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.collections.map.LRUMap;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;

/**
 * Default implementation of the resource cache for the default
 * ResourceManager.  The cache uses a <i>least recently used</i> (LRU)
 * algorithm, with a maximum size specified via the
 * <code>resource.manager.cache.size</code> property (idenfied by the
 * {@link
 * org.apache.velocity.runtime.RuntimeConstants#RESOURCE_MANAGER_CACHE_SIZE}
 * constant).  This property get be set to <code>0</code> or less for
 * a greedy, unbounded cache (the behavior from pre-v1.5).
 *
 * @author <a href="mailto:geirm@apache.org">Geir Magnusson Jr.</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: ResourceCacheImpl.java 383711 2006-03-07 00:01:00Z nbubna $
 */
public class ResourceCacheImpl implements ResourceCache
{
    /**
     * Cache storage, assumed to be thread-safe.
     */
    protected Map cache = new Hashtable();

    /**
     * Runtime services, generally initialized by the
     * <code>initialize()</code> method.
     */
    protected RuntimeServices rsvc = null;
    
    public void initialize( RuntimeServices rs )
    {
        rsvc = rs;

        int maxSize =
            rsvc.getInt(RuntimeConstants.RESOURCE_MANAGER_DEFAULTCACHE_SIZE, 89);
        if (maxSize > 0)
        {
            // Create a whole new Map here to avoid hanging on to a
            // handle to the unsynch'd LRUMap for our lifetime.
            Map lruCache = Collections.synchronizedMap(new LRUMap(maxSize));
            lruCache.putAll(cache);
            cache = lruCache;
        }
        rsvc.getLog().debug("ResourceCache: initialized ("+this.getClass()+')');
    }
    
    public Resource get( Object key )
    {
        return (Resource) cache.get( key );
    }
    
    public Resource put( Object key, Resource value )
    {
        return (Resource) cache.put( key, value );
    }
    
    public Resource remove( Object key )
    {
        return (Resource) cache.remove( key );
    }
    
    public Iterator enumerateKeys()
    {
        return cache.keySet().iterator();
    }
}
