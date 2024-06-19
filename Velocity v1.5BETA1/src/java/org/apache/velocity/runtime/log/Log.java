/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.velocity.runtime.log;


/**
 * Convenient wrapper for LogChute functions. This implements
 * the RuntimeLogger methods (and then some).  It is hoped that
 * use of this will fully replace use of the RuntimeLogger. 
 *
 * @author <a href="mailto:nbubna@apache.org">Nathan Bubna</a>
 * @version $Id: Log.java 345574 2005-11-18 21:11:38Z wglass $
 */
public class Log
{

    private LogChute chute;

    /**
     * Creates a new Log that wraps a HoldingLogChute.
     */
    public Log()
    {
        this.chute = new HoldingLogChute();
    }

    /**
     * Creates a new Log that wraps the specified LogChute.
     */
    public Log(LogChute chute)
    {
        if (chute == null)
        {
            throw new NullPointerException("The LogChute cannot be set to null!");
        }
        this.chute = chute;
    }

    /**
     * Updates the LogChute wrapped by this Log instance.
     */
    protected void setLogChute(LogChute newLogChute)
    {
        this.chute = newLogChute;
    }

    /**
     * Returns the LogChute wrapped by this Log instance.
     */
    protected LogChute getLogChute()
    {
        return this.chute;
    }

    private void log(int level, Object message)
    {
        chute.log(level, String.valueOf(message));
    }

    private void log(int level, Object message, Throwable t)
    {
        chute.log(level, String.valueOf(message), t);
    }

    /**
     * Returns true if trace level messages will be printed by the LogChute.
     */
    public boolean isTraceEnabled()
    {
        return chute.isLevelEnabled(LogChute.TRACE_ID);
    }

    /**
     * Log a trace message.
     */
    public void trace(Object message)
    {
        log(LogChute.TRACE_ID, message);
    }

    /**
     * Log a trace message and accompanying Throwable.
     */
    public void trace(Object message, Throwable t)
    {
        log(LogChute.TRACE_ID, message, t);
    }

    /**
     * Returns true if debug level messages will be printed by the LogChute.
     */
    public boolean isDebugEnabled()
    {
        return chute.isLevelEnabled(LogChute.DEBUG_ID);
    }

    /**
     * Log a debug message.
     */
    public void debug(Object message)
    {
        log(LogChute.DEBUG_ID, message);
    }

    /**
     * Log a debug message and accompanying Throwable.
     */
    public void debug(Object message, Throwable t)
    {
        log(LogChute.DEBUG_ID, message, t);
    }

    /**
     * Returns true if info level messages will be printed by the LogChute.
     */
    public boolean isInfoEnabled()
    {
        return chute.isLevelEnabled(LogChute.INFO_ID);
    }

    /**
     * Log an info message.
     */
    public void info(Object message)
    {
        log(LogChute.INFO_ID, message);
    }

    /**
     * Log an info message and accompanying Throwable.
     */
    public void info(Object message, Throwable t)
    {
        log(LogChute.INFO_ID, message, t);
    }

    /**
     * Returns true if warn level messages will be printed by the LogChute.
     */
    public boolean isWarnEnabled()
    {
        return chute.isLevelEnabled(LogChute.WARN_ID);
    }

    /**
     * Log a warning message.
     */
    public void warn(Object message)
    {
        log(LogChute.WARN_ID, message);
    }

    /**
     * Log a warning message and accompanying Throwable.
     */
    public void warn(Object message, Throwable t)
    {
        log(LogChute.WARN_ID, message, t);
    }

    /**
     * Returns true if error level messages will be printed by the LogChute.
     */
    public boolean isErrorEnabled()
    {
        return chute.isLevelEnabled(LogChute.ERROR_ID);
    }

    /**
     * Log an error message.
     */
    public void error(Object message)
    {
        log(LogChute.ERROR_ID, message);
    }

    /**
     * Log an error message and accompanying Throwable.
     */
    public void error(Object message, Throwable t)
    {
        log(LogChute.ERROR_ID, message, t);
    }

}
