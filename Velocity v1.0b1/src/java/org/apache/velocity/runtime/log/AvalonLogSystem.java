package org.apache.velocity.runtime.log;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.io.File;

import java.net.URL;

import org.apache.log.Category;
import org.apache.log.Formatter;
import org.apache.log.Priority;
import org.apache.log.Logger;
import org.apache.log.LogKit;
import org.apache.log.LogTarget;
import org.apache.log.output.FileOutputLogTarget;

import org.apache.velocity.util.StringUtils;

import org.apache.velocity.runtime.Runtime;

/**
 * Implementation of a Avalon logger.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: AvalonLogSystem.java,v 1.3 2001/03/19 06:15:54 jon Exp $
 */
public class AvalonLogSystem implements LogSystem
{
    private Logger logger = null;
    private String logPath = "";
    
    /**
     *  default CTOR.  Initializes itself using the property RUNTIME_LOG
     *  from the Velocity properties
     */
    public AvalonLogSystem()
    {
        /*
         *  since this is a Velocity-provided logger, we will
         *  use the Runtime configuration
         */
        String logfile = (String) Runtime.getProperty( Runtime.RUNTIME_LOG );

        /*
         *  now init.  If we can't, panic!
         */
        try
        {
            init( logfile );

            logVelocityMessage( 0, 
                "AvalonLogSystem initialized using logfile " + logPath );
        }
        catch( Exception e )
        {
            System.out.println( 
                "PANIC : error configuring AvalonLogSystem : " + e );
        }
    }

    /**
     *  initializes the log system using the logfile argument
     *
     *  @param logFile   file for log messages
     */
    public void init(String logFile)
        throws Exception
    {
        String targetName = "velocity";
        String priority = "DEBUG";
                
        Category category = LogKit.createCategory( 
            targetName, LogKit.getPriorityForName( priority ) );
        
        /*
         * Just create a FileOutputLogTarget, this is taken
         * from the SAR deployer in Avalon.
         */
        FileOutputLogTarget target = new FileOutputLogTarget();
        File logFileLocation = new File (logFile);
        
        logPath = logFileLocation.getAbsolutePath();

        target.setFilename( logPath );
        target.setFormatter(new VelocityFormatter());
        target.setFormat("%{time} %{message}\\n%{throwable}" );
        
        LogTarget logTargets[] = null;
                
        if ( null != target ) 
        {
            logTargets = new LogTarget[] { target };
        }            
                
        logger = LogKit.createLogger( category, logTargets );
    }
    
    /**
     *  logs messages
     *
     *  @param level severity level
     *  @param message complete error message
     */
    public void logVelocityMessage(int level, String message)
    {
        switch (level) 
        {
            case LogSystem.WARN_ID:
                logger.warn( message );
                break;
            case LogSystem.INFO_ID:
                logger.info(message);
                break;
            case LogSystem.DEBUG_ID:
                logger.debug(message);
                break;
            case LogSystem.ERROR_ID:
                logger.error(message);
                break;
            default:
                logger.info(message);
                break;
        }
    }
}
