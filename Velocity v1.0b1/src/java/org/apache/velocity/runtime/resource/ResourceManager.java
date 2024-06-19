package org.apache.velocity.runtime.resource;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.Runtime;
import org.apache.velocity.runtime.configuration.Configuration;
import org.apache.velocity.runtime.resource.ResourceFactory;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoaderFactory;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;


/**
 * Class to manage the text resource for the Velocity
 * Runtime.
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:paulo.gaspar@krankikom.de">Paulo Gaspar</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ResourceManager.java,v 1.19 2001/03/20 17:28:26 geirm Exp $
 */
public class ResourceManager
{
    /**
     * A template resources.
     */
    public static final int RESOURCE_TEMPLATE = 1;
    
    /**
     * A static content resource.
     */
    public static final int RESOURCE_CONTENT = 2;

    /**
     * Hashtable used to store templates that have been
     * processed. Our simple caching mechanism.
     */
    private static Hashtable globalCache = new Hashtable();
    
    /**
     * The List of templateLoaders that the Runtime will
     * use to locate the InputStream source of a template.
     */
    private static ArrayList resourceLoaders = new ArrayList();
    
    /**
     * This is a list of the template stream source
     * initializers, basically properties for a particular
     * template stream source. The order in this list
     * reflects numbering of the properties i.e.
     * template.loader.1.<property> = <value>
     * template.loader.2.<property> = <value>
     */
    private static ArrayList sourceInitializerList = new ArrayList();
    
    /**
     * This is a map of public name of the template
     * stream source to it's initializer. This is so
     * that clients of velocity can set properties of
     * a template source stream with its public name.
     * So for example, a client could set the 
     * File.resource.path property and this would
     * change the resource.path property for the
     * file template stream source.
     */
    private static Hashtable sourceInitializerMap = new Hashtable();

    /**
     * Each loader needs a configuration object for
     * its initialization, this flags keeps track of whether
     * or not the configuration objects have been created
     * for the resource loaders.
     */
    private static boolean resourceLoaderInitializersActive = false;

    /**
     * Initialize the ResourceManager. It is assumed
     * that assembleSourceInitializers() has been
     * called before this is run.
     */
    public static void initialize() throws Exception
    {
        ResourceLoader resourceLoader;
        
        assembleResourceLoaderInitializers();
        
        for (int i = 0; i < sourceInitializerList.size(); i++)
        {
            Configuration configuration = (Configuration) sourceInitializerList.get(i);
            String loaderClass = configuration.getString("class");

            resourceLoader = ResourceLoaderFactory.getLoader(loaderClass);
            resourceLoader.commonInit(configuration);
            resourceLoader.init(configuration);
            resourceLoaders.add(resourceLoader);

        }
    }

    /**
     * This will produce a List of Hashtables, each
     * hashtable contains the intialization info for
     * a particular resource loader. This Hastable
     * will be passed in when initializing the
     * the template loader.
     */
    private static void assembleResourceLoaderInitializers()
    {
        if (resourceLoaderInitializersActive)
        {
            return;
        }            

        Vector resourceLoaderNames = 
            Runtime.getConfiguration().getVector(Runtime.RESOURCE_LOADER);

        for (int i = 0; i < resourceLoaderNames.size(); i++)
        {
            /*
             * The loader id might look something like the following:
             *
             * file.resource.loader
             *
             * The loader id is the prefix used for all properties
             * pertaining to a particular loader.
             */
            String loaderID = 
                resourceLoaderNames.get(i) + "." + Runtime.RESOURCE_LOADER;

            Configuration loaderConfiguration =
                Runtime.getConfiguration().subset(loaderID);

            /*
             * Add resources to the list of resource loader
             * initializers.
             */
            sourceInitializerList.add(loaderConfiguration);
        }
        
        resourceLoaderInitializersActive = true;
    }

    /**
     * Gets the named resource.  Returned class type corresponds to specified type
     * (i.e. <code>Template</code> to <code>RESOURCE_TEMPLATE</code>).
     *
     * @param resourceName The name of the resource to retrieve.
     * @param resourceType The type of resource (<code>RESOURCE_TEMPLATE</code>,
     *                     <code>RESOURCE_CONTENT</code>, etc.).
     * @return Resource with the template parsed and ready.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if a problem in parse
     */
    public static Resource getResource(String resourceName, int resourceType)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        Resource resource = null;
        ResourceLoader resourceLoader = null;
        
        /* 
         * Check to see if the resource was placed in the cache.
         * If it was placed in the cache then we will use
         * the cached version of the resource. If not we
         * will load it.
         */
        
        if (globalCache.containsKey(resourceName))
        {
            resource = (Resource) globalCache.get(resourceName);

            /* 
             * The resource knows whether it needs to be checked
             * or not, and the resource's loader can check to
             * see if the source has been modified. If both
             * these conditions are true then we must reload
             * the input stream and parse it to make a new
             * AST for the resource.
             */
            if ( resource.requiresChecking() )
            {
                /*
                 *  touch() the resource to reset the counters
                 */

                resource.touch();

                if(  resource.isSourceModified() )
                {
                    try
                    {
                        /*
                         *  read how old the resource is _before_
                         *  processing (=>reading) it
                         */
                        long howOldItWas = resourceLoader.getLastModified( resource );

                        /*
                         *  read in the fresh stream and parse
                         */

                        resource.process();

                        /*
                         *  now set the modification info and reset
                         *  the modification check counters
                         */
                        
                        resource.setLastModified( howOldItWas );
                    }
                    catch( ResourceNotFoundException rnfe )
                    {
                        Runtime.error("ResourceManager.getResource() exception: " + rnfe);
                        throw rnfe;
                    }
                    catch( ParseErrorException pee )
                    {
                        Runtime.error("ResourceManager.getResource() exception: " + pee);
                        throw pee;
                    }
                    catch( Exception eee )
                    {
                        Runtime.error("ResourceManager.getResource() exception: " + eee);
                        throw eee;
                    }
                }
            }

            return resource;
        }
        else
        {
            /*
             *  it's not in the cache
             */

            try
            {
                resource = ResourceFactory.getResource(resourceName, resourceType);
                resource.setName(resourceName);
                
                /* 
                 * Now we have to try to find the appropriate
                 * loader for this resource. We have to cycle through
                 * the list of available resource loaders and see
                 * which one gives us a stream that we can use to
                 * make a resource with.
                 */
                
                //! Bug this is being run more then once!
                
                long howOldItWas = 0;  // Initialize to avoid warnings

                for (int i = 0; i < resourceLoaders.size(); i++)
                {
                    resourceLoader = (ResourceLoader) resourceLoaders.get(i);
                    resource.setResourceLoader(resourceLoader);
                    
                    /*
                     *  catch the ResourceNotFound exception
                     *  as that is ok in our new multi-loader environment
                     */

                    try 
                    {
                        if (resource.process())
                         {
                             /*
                              *  FIXME  (gmj)
                              *  moved in here - technically still 
                              *  a problem - but the resource needs to be 
                              *  processed before the loader can figure 
                              *  it out due to to the new 
                              *  multi-path support - will revisit and fix
                              */
                             Runtime.info("ResourceManager : found " + resourceName + 
                                          " with loader " + resourceLoader.getClassName());
           
                             howOldItWas = resourceLoader.getLastModified( resource );
                             break;
                         }
                    }
                    catch( ResourceNotFoundException rnfe )
                    {
                        /*
                         *  that's ok - it's possible to fail in
                         *  multi-loader environment
                         */
                    }
                }
                
                /*
                 * Return null if we can't find a resource.
                 */
                if (resource.getData() == null)
                    throw new ResourceNotFoundException("Unable to find resource '" + resourceName + "'");

                resource.setLastModified( howOldItWas );
                 
                resource.setModificationCheckInterval(
                    resourceLoader.getModificationCheckInterval());
                
                resource.touch();
                
                /*
                 * Place the resource in the cache if the resource
                 * loader says to.
                 */
                
                if (resourceLoader.isCachingOn())
                    globalCache.put(resourceName, resource);
            }
            catch( ResourceNotFoundException rnfe2 )
            {
                Runtime.error("ResourceManager : unable to find resource '" + resourceName + "' in any resource loader.");
                throw rnfe2;
            }
            catch( ParseErrorException pee )
            {
                Runtime.error("ResourceManager.getResource() parse exception: " + pee);
                throw pee;
            }
            catch( Exception ee )
            {
                Runtime.error("ResourceManager.getResource() exception: " + ee);
                throw ee;
            }
        }
        return resource;
    }
}
