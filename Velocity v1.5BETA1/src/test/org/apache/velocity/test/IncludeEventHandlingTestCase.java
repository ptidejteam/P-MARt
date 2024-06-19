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


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.app.event.RuntimeServicesAware;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;

/**
 *  Tests event handling
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: IncludeEventHandlingTestCase.java 307403 2005-10-09 11:34:52Z henning $
 */
public class IncludeEventHandlingTestCase extends BaseTestCase implements IncludeEventHandler,RuntimeServicesAware
{

     /**
     * VTL file extension.
     */
    private static final String TMPL_FILE_EXT = "vm";

    /**
     * Comparison file extension.
     */
    private static final String CMP_FILE_EXT = "cmp";

    /**
     * Comparison file extension.
     */
    private static final String RESULT_FILE_EXT = "res";

    /**
     * Path for templates. This property will override the
     * value in the default velocity properties file.
     */
    private final static String FILE_RESOURCE_LOADER_PATH = TEST_COMPARE_DIR + "/includeevent";

    /**
     * Results relative to the build directory.
     */
    private static final String RESULTS_DIR = TEST_RESULT_DIR + "/includeevent";

    /**
     * Results relative to the build directory.
     */
    private static final String COMPARE_DIR = TEST_COMPARE_DIR + "/includeevent/compare";


    private static final int PASS_THROUGH=0;
    private static final int RELATIVE_PATH=1;
    private static final int BLOCK=2;

    private int EventHandlerBehavior = PASS_THROUGH;

    /**
     * Default constructor.
     */
    public IncludeEventHandlingTestCase(String name)
    {
        super(name);
    }

    public void setUp()
            throws Exception
    {
        assureResultsDirectoryExists(RESULTS_DIR);

        Velocity.addProperty(
            Velocity.FILE_RESOURCE_LOADER_PATH, FILE_RESOURCE_LOADER_PATH);

        Velocity.init();


    }


    public static Test suite ()
    {
        return new TestSuite(IncludeEventHandlingTestCase.class);
    }

    /**
     * Runs the test.
     */
    public void testIncludeEventHandling ()
            throws Exception
    {
        Template template1 = RuntimeSingleton.getTemplate(
            getFileName(null, "test1", TMPL_FILE_EXT));

        Template template2 = RuntimeSingleton.getTemplate(
            getFileName(null, "subdir/test2", TMPL_FILE_EXT));

        Template template3 = RuntimeSingleton.getTemplate(
            getFileName(null, "test3", TMPL_FILE_EXT));

        FileOutputStream fos1 =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "test1", RESULT_FILE_EXT));

        FileOutputStream fos2 =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "test2", RESULT_FILE_EXT));

        FileOutputStream fos3 =
            new FileOutputStream (
                getFileName(RESULTS_DIR, "test3", RESULT_FILE_EXT));

        Writer writer1 = new BufferedWriter(new OutputStreamWriter(fos1));
        Writer writer2 = new BufferedWriter(new OutputStreamWriter(fos2));
        Writer writer3 = new BufferedWriter(new OutputStreamWriter(fos3));

        /*
         *  lets make a Context and add the event cartridge
         */

        Context context = new VelocityContext();

        /*
         *  Now make an event cartridge, register the
         *  input event handler and attach it to the
         *  Context
         */

        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(this);
        ec.attachToContext( context );


        // BEHAVIOR A: pass through #input and #parse with no change
        EventHandlerBehavior = PASS_THROUGH;

        template1.merge(context, writer1);
        writer1.flush();
        writer1.close();

        // BEHAVIOR B: pass through #input and #parse with using a relative path
        EventHandlerBehavior = RELATIVE_PATH;

        template2.merge(context, writer2);
        writer2.flush();
        writer2.close();

        // BEHAVIOR C: refuse to pass through #input and #parse
        EventHandlerBehavior = BLOCK;

        template3.merge(context, writer3);
        writer3.flush();
        writer3.close();

        if (!isMatch(RESULTS_DIR, COMPARE_DIR, "test1",
                RESULT_FILE_EXT, CMP_FILE_EXT) ||
            !isMatch(RESULTS_DIR, COMPARE_DIR, "test2",
                RESULT_FILE_EXT, CMP_FILE_EXT) ||
            !isMatch(RESULTS_DIR, COMPARE_DIR, "test3",
                RESULT_FILE_EXT, CMP_FILE_EXT)
                )
        {
            fail("Output incorrect.");
        }
    }


    public void setRuntimeServices( RuntimeServices rs )
     {
     }

    /**
     * Sample handler with different behaviors for the different tests.
     */
    public String includeEvent( String includeResourcePath, String currentResourcePath, String directiveName)
    {
        if (EventHandlerBehavior == PASS_THROUGH)
            return includeResourcePath;


        // treat as relative path
        else if (EventHandlerBehavior == RELATIVE_PATH)
        {

            // strip the starting slash from includeResourcePath, if it exists
            if (includeResourcePath.startsWith("/") || includeResourcePath.startsWith("\\") )
                includeResourcePath = includeResourcePath.substring(1);

            int slashpos1 = currentResourcePath.lastIndexOf("/");
            int slashpos2 = currentResourcePath.lastIndexOf("\\");
            int lastslashpos = -1;
            if ( (slashpos1 != -1) && (slashpos2 != -1) && (slashpos1 <= slashpos2) )
                lastslashpos = slashpos2;

            else if ( (slashpos1 != -1) && (slashpos2 != -1) && (slashpos1 > slashpos2) )
                lastslashpos = slashpos1;

            else if ( (slashpos1 != -1) && (slashpos2 == -1) )
                lastslashpos = slashpos1;

            else if ( (slashpos1 == -1) && (slashpos2 != -1) )
                lastslashpos = slashpos2;

            // root of resource tree
            if ( (lastslashpos == -1) || (lastslashpos == 0) )
                return includeResourcePath;

            // prepend path to the input path
            else
                return currentResourcePath.substring(0,lastslashpos) + "/" + includeResourcePath;



        } else if (EventHandlerBehavior == BLOCK)
            return null;

        // should never happen
        else
            return null;


    }


}
