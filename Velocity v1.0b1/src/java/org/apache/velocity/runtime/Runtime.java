package org.apache.velocity.runtime;

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

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log.Logger;

import org.apache.velocity.Template;

import org.apache.velocity.runtime.log.LogManager;
import org.apache.velocity.runtime.log.LogSystem;

import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.VelocimacroFactory;

import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ContentResource;
import org.apache.velocity.runtime.resource.ResourceManager;

import org.apache.velocity.util.SimplePool;
import org.apache.velocity.util.StringUtils;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;

import org.apache.velocity.runtime.configuration.Configuration;

/**
 * This is the Runtime system for Velocity. It is the
 * single access point for all functionality in Velocity.
 * It adheres to the mediator pattern and is the only
 * structure that developers need to be familiar with
 * in order to get Velocity to perform.
 *
 * The Runtime will also cooperate with external
 * systems like Turbine. Runtime properties can
 * set and then the Runtime is initialized.
 *
 * Turbine for example knows where the templates
 * are to be loaded from, and where the velocity
 * log file should be placed.
 *
 * So in the case of Velocity cooperating with Turbine
 * the code might look something like the following:
 *
 * <pre>
 * Runtime.setProperty(Runtime.FILE_RESOURCE_LOADER_PATH, templatePath);
 * Runtime.setProperty(Runtime.RUNTIME_LOG, pathToVelocityLog);
 * Runtime.init();
 * </pre>
 *
 * <pre>
 * -----------------------------------------------------------------------
 * N O T E S  O N  R U N T I M E  I N I T I A L I Z A T I O N
 * -----------------------------------------------------------------------
 * Runtime.init()
 * 
 * If Runtime.init() is called by itself the Runtime will
 * initialize with a set of default values.
 * -----------------------------------------------------------------------
 * Runtime.init(String/Properties)
 *
 * In this case the default velocity properties are layed down
 * first to provide a solid base, then any properties provided
 * in the given properties object will override the corresponding
 * default property.
 * -----------------------------------------------------------------------
 * </pre>
 *
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:jlb@houseofdistraction.com">Jeff Bowden</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magusson Jr.</a>
 * @version $Id: Runtime.java,v 1.103 2001/03/20 01:52:54 jon Exp $
 */
public class Runtime implements RuntimeConstants
{    
    /**
     *  VelocimacroFactory object to manage VMs
     */
    private static VelocimacroFactory vmFactory = new VelocimacroFactory();

    /** 
     * The Runtime logger.
     */
    private static LogSystem logSystem = null;

    /** 
     * The caching system used by the Velocity Runtime 
     */
    private static Hashtable globalCache;
    
    /** 
     * The Runtime parser pool 
     */
    private static SimplePool parserPool;
    
    /** 
     * Indicate whether the Runtime has been fully initialized.
     */
    private static boolean initialized;

    /**
     * These are the properties that are laid down over top
     * of the default properties when requested.
     */
    private static Configuration overridingProperties = null;

    /**
     * The logging systems initialization may be defered if
     * it is to be initialized by an external system. There
     * may be messages that need to be stored until the
     * logger is instantiated. They will be stored here
     * until the logger is alive.
     */
    private static Vector pendingMessages = new Vector();

    /**
     * This is a hashtable of initialized directives.
     * The directives that populate this hashtable are
     * taken from the RUNTIME_DEFAULT_DIRECTIVES
     * property file. This hashtable is passed
     * to each parser that is created.
     */
    private static Hashtable runtimeDirectives;

    /**
     * Object that houses the configuration options for
     * the velocity runtime. The Configuration object allows
     * the convenient retrieval of a subset of properties.
     * For example all the properties for a resource loader
     * can be retrieved from the main Configuration object
     * using something like the following:
     *
     * Configuration loaderConfiguration = 
     *         configuration.subset(loaderID);
     *
     * And a configuration is a lot more convenient to deal
     * with then conventional properties objects, or Maps.
     */
    private static Configuration configuration = new Configuration();

    /*
     * This is the primary initialization method in the Velocity
     * Runtime. The systems that are setup/initialized here are
     * as follows:
     * 
     * <ul>
     *   <li>Logging System</li>
     *   <li>ResourceManager</li>
     *   <li>Parser Pool</li>
     *   <li>Global Cache</li>
     *   <li>Static Content Include System</li>
     *   <li>Velocimacro System</li>
     * </ul>
     */
    public synchronized static void init()
        throws Exception
    {
        if (initialized == false)
        {
            try
            {
                initializeProperties();
                initializeLogger();
                ResourceManager.initialize();
                initializeDirectives();
                initializeParserPool();
                initializeGlobalCache();
                
                /*
                 *  initialize the VM Factory.  It will use the properties 
                 * accessable from Runtime, so keep this here at the end.
                 */
                vmFactory.initVelocimacro();
                
                info("Velocity successfully started.");
                
                initialized = true;
            }
            catch (Exception e)
            {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the Velocity Runtime with properties file.
     * The properties file may be in the file system proper,
     * or the properties file may be in the classpath.
     */
    private static void setDefaultProperties()
    {
        ClassLoader classLoader = Runtime.class.getClassLoader();
        try
        {
            InputStream inputStream = classLoader
                .getResourceAsStream( DEFAULT_RUNTIME_PROPERTIES );
            
            configuration.load( inputStream );
            
            info ("Default Properties File: " + 
                new File(DEFAULT_RUNTIME_PROPERTIES).getPath());
        }
        catch (IOException ioe)
        {
            System.err.println("Cannot get Velocity Runtime default properties!");
        }
    }

    /**
     * Allows an external system to set a property in
     * the Velocity Runtime.
     *
     * @param String property key
     * @param String property value
     */
    public static void setProperty(String key, Object value)
    {
        if (overridingProperties == null)
        {
            overridingProperties = new Configuration();
        }            
            
        overridingProperties.setProperty( key, value );
    }        

    /**
     * Add a property to the configuration. If it already
     * exists then the value stated here will be added
     * to the configuration entry. For example, if
     *
     * resource.loader = file
     *
     * is already present in the configuration and you
     *
     * addProperty("resource.loader", "classpath")
     *
     * Then you will end up with a Vector like the
     * following:
     *
     * ["file", "classpath"]
     *
     * @param String key
     * @param String value
     */
    public static void addProperty(String key, Object value)
    {
        configuration.addProperty(key, value);
    }
    
    /**
     *  Allows an external caller to get a property.  The calling
     *  routine is required to know the type, as this routine
     *  will return an Object, as that is what properties can be.
     *
     *  @param key property to return
     */
    public static Object getProperty( String key )
    {
        return configuration.getProperty( key );
    }

    /**
     * Initialize Velocity properties, if the default
     * properties have not been laid down first then
     * do so. Then proceed to process any overriding
     * properties. Laying down the default properties
     * gives a much greater chance of having a
     * working system.
     */
    private static void initializeProperties()
    {
        /* 
         * Always lay down the default properties first as
         * to provide a solid base.
         */
        if (configuration.isInitialized() == false)
        {
            setDefaultProperties();
        }            
    
        if( overridingProperties != null)
        {        
            configuration.combine(overridingProperties);
        }
    }
    
    /**
     * Initialize the Velocity Runtime with a Properties
     * object.
     *
     * @param Properties
     */
    public static void init(Properties p) throws Exception
    {
        overridingProperties = Configuration.convertProperties(p);
        init();
    }
    
    /**
     * Initialize the Velocity Runtime with the name of
     * Configuration object.
     *
     * @param Properties
     */
    public static void init(String configurationFile)
        throws Exception
    {
        overridingProperties = new Configuration(configurationFile);
        init();
    }

    /**
     * Initialize the Velocity logging system.
     *
     * @throws Exception
     */
    private static void initializeLogger() throws Exception
    { 
        /*
         * Initialize the logger. We will eventually move all
         * logging into the logging manager.
         */
        if (logSystem == null)
        {
            logSystem = LogManager.createLogSystem();
        }

        /*
         * Dump the pending messages
         */
        dumpPendingMessages();
    }

    /*
     * Dump the pending messages
     */
    private static void dumpPendingMessages()
    {
        if ( !pendingMessages.isEmpty())
        {
            /*
             *  iterate and log each individual message...
             */
            for( Enumeration e = pendingMessages.elements(); e.hasMoreElements(); )
            {
                Object[] data = (Object[]) e.nextElement();
                log(((Integer) data[0]).intValue(), data[1]);
            }
            pendingMessages = new Vector();
        }
    }
    
    /**
     * This methods initializes all the directives
     * that are used by the Velocity Runtime. The
     * directives to be initialized are listed in
     * the RUNTIME_DEFAULT_DIRECTIVES properties
     * file.
     *
     * @throws Exception
     */
    private static void initializeDirectives() throws Exception
    {
        /*
         * Initialize the runtime directive table.
         * This will be used for creating parsers.
         */
        runtimeDirectives = new Hashtable();
        
        Properties directiveProperties = new Properties();
        
        /*
         * Grab the properties file with the list of directives
         * that we should initialize.
         */
        ClassLoader classLoader = Runtime.class.getClassLoader();
        InputStream inputStream = classLoader
            .getResourceAsStream(DEFAULT_RUNTIME_DIRECTIVES);
    
        if (inputStream == null)
            throw new Exception("Error loading directive.properties! " +
                                "Something is very wrong if these properties " +
                                "aren't being located. Either your Velocity " +
                                "distribution is incomplete or your Velocity " +
                                "jar file is corrupted!");
        
        directiveProperties.load(inputStream);
        
        /*
         * Grab all the values of the properties. These
         * are all class names for example:
         *
         * org.apache.velocity.runtime.directive.Foreach
         */
        Enumeration directiveClasses = directiveProperties.elements();
        
        while (directiveClasses.hasMoreElements())
        {
            String directiveClass = (String) directiveClasses.nextElement();
        
            try
            {
                /*
                 * Attempt to instantiate the directive class. This
                 * should usually happen without error because the
                 * properties file that lists the directives is
                 * not visible. It's in a package that isn't 
                 * readily accessible.
                 */
                Class clazz = Class.forName(directiveClass);
                Directive directive = (Directive) clazz.newInstance();
                runtimeDirectives.put(directive.getName(), directive);
                
                Runtime.info("Loaded Pluggable Directive: " 
                    + directiveClass);
            }
            catch (Exception e)
            {
                Runtime.error("Error Loading Pluggable Directive: " 
                    + directiveClass);    
            }
        }
    }

    /**
     * Initializes the Velocity parser pool.
     * This still needs to be implemented.
     */
    private static void initializeParserPool()
    {
        parserPool = new SimplePool(NUMBER_OF_PARSERS);
        for (int i=0;i<NUMBER_OF_PARSERS ;i++ )
        {
            parserPool.put (createNewParser());
        }
        Runtime.info ("Created: " + NUMBER_OF_PARSERS + " parsers.");
    }

    /**
     * Returns a JavaCC generated Parser.
     *
     * @return Parser javacc generated parser
     */
    public static Parser createNewParser()
    {
        Parser parser = new Parser();
        parser.setDirectives(runtimeDirectives);
        return parser;
    }

    /**
     * Parse the input stream and return the root of
     * AST node structure.
     *
     * @param InputStream inputstream retrieved by a resource loader
     * @param String name of the template being parsed
     */
    public static SimpleNode parse(InputStream inputStream, String templateName )
        throws ParseException
    {
        SimpleNode ast = null;
        Parser parser = (Parser) parserPool.get();
        
        if (parser != null)
        {
            try
            {
                ast = parser.parse(inputStream, templateName);
            }
            finally
            {
                parserPool.put(parser);
            }
        }
        else
        {
            error("Runtime : ran out of parsers!");
        }
        return ast;
    }
    
    /**
     * Initialize the global cache use by the Velocity
     * runtime. Cached templates will be stored here,
     * as well as cached content pulled in by the #include
     * directive. Who knows what else we'll find to
     * cache.
     */
    private static void initializeGlobalCache()
    {
        globalCache = new Hashtable();
    }
    
    /**
     * Returns a <code>Template</code> from the resource manager
     *
     * @param name The file name of the desired template.
     * @return     The template.
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     * @throws ParseErrorException if template cannot be parsed due
     *          to syntax (or other) error.
     * @throws Exception if an error occurs in template initialization
     */
    public static Template getTemplate(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return (Template) ResourceManager
            .getResource(name,ResourceManager.RESOURCE_TEMPLATE);
    }

    /**
     * Returns a static content resource from the
     * resource manager.
     *
     * @param name Name of content resource to get
     * @return parsed ContentResource object ready for use
     * @throws ResourceNotFoundException if template not found
     *          from any available source.
     */
    public static ContentResource getContent(String name)
        throws ResourceNotFoundException, ParseErrorException, Exception
    {
        return (ContentResource) ResourceManager
            .getResource(name,ResourceManager.RESOURCE_CONTENT);
    }

    /**
     * Added this to check and make sure that the configuration
     * is initialized before trying to get properties from it.
     * This occurs when there are errors during initialization
     * and the default properties have yet to be layed down.
     */
    private static boolean showStackTrace()
    {
        if (configuration.isInitialized())
        {
            return getBoolean(RUNTIME_LOG_WARN_STACKTRACE, false);
        }            
        else
        {
            return false;
        }            
    }

    /**
     * Handle logging.
     *
     * @param String message to log
     */
    private static void log(int level, Object message)
    {
        String out = "";
     
        /*
         * Start with the appropriate prefix
         */
        switch( level ) 
        {
            case LogSystem.DEBUG_ID :
                out = DEBUG_PREFIX;
                break;
            case LogSystem.INFO_ID :
                out = INFO_PREFIX;
                break;
            case LogSystem.WARN_ID :
                out = WARN_PREFIX;
                break;
            case LogSystem.ERROR_ID : 
                out = ERROR_PREFIX;
                break;
            default :
                out = UNKNOWN_PREFIX;
                break;
        }

        /*
         *  now,  see if the logging stacktrace is on
         *  and modify the message to suit
         */
        if ( showStackTrace() &&
            (message instanceof Throwable || message instanceof Exception) )
        {
            out += StringUtils.stackTrace((Throwable)message);
        }
        else
        {
            out += message.toString();    
        }            

        /*
         *  now, if we have a log system, log it
         *  otherwise, queue it up for later
         */
        if (logSystem != null)
        {
            logSystem.logVelocityMessage( level, out);
        }
        else
        {
            Object[] data = new Object[2];
            data[0] = new Integer(level);
            data[1] = out;
            pendingMessages.addElement(data);
        }
    }

    /**
     * Log a warning message.
     *
     * @param Object message to log
     */
    public static void warn(Object message)
    {
        log(LogSystem.WARN_ID, message);
    }
    
    /** 
     * Log an info message.
     *
     * @param Object message to log
     */
    public static void info(Object message)
    {
        log(LogSystem.INFO_ID, message);
    }
    
    /**
     * Log an error message.
     *
     * @param Object message to log
     */
    public static void error(Object message)
    {
        log(LogSystem.ERROR_ID, message);
    }
    
    /**
     * Log a debug message.
     *
     * @param Object message to log
     */
    public static void debug(Object message)
    {
        log(LogSystem.DEBUG_ID, message);
    }

    /**
     * String property accessor method with default to hide the
     * configuration implementation.
     * 
     * @param String key property key
     * @param String defaultValue  default value to return if key not 
     *               found in resource manager.
     * @return String  value of key or default 
     */
    public static String getString( String key, String defaultValue)
    {
        return configuration.getString(key, defaultValue);
    }

    /**
     * Returns the appropriate VelocimacroProxy object if strVMname
     * is a valid current Velocimacro.
     *
     * @param String vmName  Name of velocimacro requested
     * @return String VelocimacroProxy 
     */
    public static Directive getVelocimacro( String vmName, String templateName  )
    {
        return vmFactory.getVelocimacro( vmName, templateName );
    }

   /**
     * Adds a new Velocimacro. Usually called by Macro only while parsing.
     *
     * @param String name  Name of velocimacro 
     * @param String macro  String form of macro body
     * @param String argArray  Array of strings, containing the 
     *                         #macro() arguments.  the 0th is the name.
     * @return boolean  True if added, false if rejected for some 
     *                  reason (either parameters or permission settings) 
     */
    public static boolean addVelocimacro( String name, 
                                          String macro, 
                                          String argArray[], 
                                          String sourceTemplate )
    {    
        return vmFactory.addVelocimacro(  name, macro,  argArray,  sourceTemplate );
    }
 
    /**
     *  Checks to see if a VM exists
     *
     * @param name  Name of velocimacro
     * @return boolean  True if VM by that name exists, false if not
     */
    public static boolean isVelocimacro( String vmName, String templateName )
    {
        return vmFactory.isVelocimacro( vmName, templateName );
    }

    /**
     *  tells the vmFactory to dump the specified namespace.  This is to support
     *  clearing the VM list when in inline-VM-local-scope mode
     */
    public static boolean dumpVMNamespace( String namespace )
    {
        return vmFactory.dumpVMNamespace( namespace );
    }

    /* --------------------------------------------------------------------
     * R U N T I M E  A C C E S S O R  M E T H O D S
     * --------------------------------------------------------------------
     * These are the getXXX() methods that are a simple wrapper
     * around the configuration object. This is an attempt
     * to make a the Velocity Runtime the single access point
     * for all things Velocity, and allow the Runtime to
     * adhere as closely as possible the the Mediator pattern
     * which is the ultimate goal.
     * --------------------------------------------------------------------
     */

    /**
     * String property accessor method to hide the configuration implementation
     * @param key  property key
     * @return   value of key or null
     */
    public static String getString(String key)
    {
        return configuration.getString( key );
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param String key property key
     * @return int value
     */
    public static int getInt( String key )
    {
        return configuration.getInt( key );
    }

    /**
     * Int property accessor method to hide the configuration implementation.
     *
     * @param key  property key
     * @param int default value
     * @return int  value
     */
    public static int getInt( String key, int defaultValue )
    {
        return configuration.getInt( key, defaultValue );
    }

    /**
     * Boolean property accessor method to hide the configuration implementation.
     * 
     * @param String key  property key
     * @param boolean default default value if property not found
     * @return boolean  value of key or default value
     */
    public static boolean getBoolean( String key, boolean def )
    {
        return configuration.getBoolean( key, def );
    }

    /**
     * Return the velocity runtime configuration object.
     *
     * @return Configuration configuration object which houses
     *                       the velocity runtime properties.
     */
    public static Configuration getConfiguration()
    {
        return configuration;
    }        
}
