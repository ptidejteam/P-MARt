package org.apache.velocity.test;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;

/**
 * Tests if we can hand Velocity an arbitrary class for logging.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: MethodInvocationExceptionTestCase.java 291585 2005-09-26 08:56:23Z henning $
 */
public class MethodInvocationExceptionTestCase extends TestCase
{
   /**
     * Default constructor.
     */
    public MethodInvocationExceptionTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        /*
         *  init() Runtime with defaults
         */
        Velocity.init();
    }

    public static Test suite ()
    {
        return new TestSuite(MethodInvocationExceptionTestCase.class);
    }

    /**
     * Runs the test :
     *
     *  uses the Velocity class to eval a string
     *  which accesses a method that throws an
     *  exception.
     */
    public void testNormalMethodInvocationException ()
            throws Exception
    {
        String template = "$woogie.doException() boing!";

        VelocityContext vc = new VelocityContext();

        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        try
        {
            Velocity.evaluate( vc,  w, "test", template );
            fail("No exception thrown");
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
    }


    public void testGetterMethodInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        /*
         *  second test - to ensure that methods accessed via get+ construction
         *  also work
         */

        String template = "$woogie.foo boing!";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, second test.");
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
    }


    public void testCapitalizedGetterMethodInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        String template = "$woogie.Foo boing!";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, third test.");
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
    }

    public void testSetterMethodInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        String template = "#set($woogie.foo = 'lala') boing!";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
            fail("No exception thrown, set test.");
        }
        catch( MethodInvocationException mie )
        {
            System.out.println("Caught MIE (good!) :" );
            System.out.println("  reference = " + mie.getReferenceName() );
            System.out.println("  method    = " + mie.getMethodName() );

            Throwable t = mie.getWrappedThrowable();
            System.out.println("  throwable = " + t );

            if( t instanceof Exception)
            {
                System.out.println("  exception = " + ( (Exception) t).getMessage() );
            }
        }
    }


    /**
     * test that no exception is thrown when in parameter to macro.
     * This is the way we expect the system to work, but it would be better
     * to throw an exception.
     */
    public void testMacroInvocationException ()
            throws Exception
    {
        VelocityContext vc = new VelocityContext();
        vc.put("woogie", this );

        StringWriter w = new StringWriter();

        String template = "#macro (macro1 $param) $param #end  #macro1($woogie.getFoo())";

        try
        {
            Velocity. evaluate( vc,  w, "test", template );
        }
        catch( MethodInvocationException mie )
        {
            fail("Shouldn't have thrown exception, macro param test.");
        }
    }

    public void doException()
        throws Exception
    {
        throw new NullPointerException();
    }

    public void getFoo()
        throws Exception
    {
        throw new Exception("Hello from getFoo()" );
    }

    public void  setFoo( String foo )
        throws Exception
    {
        throw new Exception("Hello from setFoo()");
    }
}
