package org.apache.velocity.test.misc;

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

import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;

/**
 * Event handlers that always throws an exception.  Used to test
 * that RuntimeExceptions are passed through.
 * 
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 * @version $Id: ExceptionGeneratingEventHandler.java 365832 2006-01-04 05:45:40Z wglass $
 */
public class ExceptionGeneratingEventHandler implements IncludeEventHandler,
        MethodExceptionEventHandler, NullSetEventHandler, ReferenceInsertionEventHandler
{

    public String includeEvent(String includeResourcePath, String currentResourcePath,
            String directiveName)
    {
        throw new RuntimeException("exception");
    }

    public Object methodException(Class claz, String method, Exception e) throws Exception
    {
        throw new RuntimeException("exception");
    }

    public boolean shouldLogOnNullSet(String lhs, String rhs)
    {
        throw new RuntimeException("exception");
    }

    public Object referenceInsert(String reference, Object value)
    {
        throw new RuntimeException("exception");
    }

}
