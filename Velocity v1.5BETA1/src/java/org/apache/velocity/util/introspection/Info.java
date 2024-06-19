package org.apache.velocity.util.introspection;
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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
 *  Little class to carry in info such as template name, line and column
 *  for information error reporting from the uberspector implementations
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: Info.java 291585 2005-09-26 08:56:23Z henning $
 */
public class Info
{
    private int line;
    private int column;
    private String templateName;

    /**
     * @param source Usually a template name.
     * @param line The line number from <code>source</code>.
     * @param column The column number from <code>source</code>.
     */
    public Info(String source, int line, int column)
    {
        this.templateName = source;
        this.line = line;
        this.column = column;
    }

    /**
     * Force callers to set the location information.
     */
    private Info()
    {
    }

    public String getTemplateName()
    {
        return templateName;
    }

    public int getLine()
    {
        return line;
    }

    public int getColumn()
    {
        return column;
    }

    /**
     * Formats a textual representation of this object as <code>SOURCE
     * [line X, column Y]</code>.
     */
    public String toString()
    {
        return getTemplateName() + " [line " + getLine() + ", column " +
            getColumn() + ']';
    }
}
