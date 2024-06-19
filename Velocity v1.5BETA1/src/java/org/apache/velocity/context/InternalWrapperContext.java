package org.apache.velocity.context;

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

/**
 *  interface for internal context wrapping functionality
 *  
 *  @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 *  @version $Id: InternalWrapperContext.java 291585 2005-09-26 08:56:23Z henning $ 
 */
public interface InternalWrapperContext
{
    /** returns the wrapped user context */
    public Context getInternalUserContext();

    /** returns the base full context impl */
    public InternalContextAdapter getBaseContext();
    
}
