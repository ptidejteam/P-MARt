package org.apache.velocity.runtime.parser.node;

/*
 * Copyright 2000-2004 The Apache Software Foundation.
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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.ParserVisitor;

import java.math.BigDecimal;


/**
 * Handles floating point numbers.  The value will be either a Double
 * or a BigDecimal.
 *
 * @author <a href="mailto:wglass@forio.com">Will Glass-Husain</a>
 */
public class ASTFloatingPointLiteral extends SimpleNode 
{

    // This may be of type Double or BigDecimal
    private Number value = null;

    public ASTFloatingPointLiteral(int id) 
    {
        super(id);
    }

    public ASTFloatingPointLiteral(Parser p, int id) 
    {
        super(p, id);
    }


    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data) 
    {
        return visitor.visit(this, data);
    }

    /**
     *  Initialization method - doesn't do much but do the object
     *  creation.  We only need to do it once.
     */
    public Object init( InternalContextAdapter context, Object data)
        throws Exception
    {
        /*
         *  init the tree correctly
         */

        super.init( context, data );

        /**
         * Determine the size of the item and make it a Double or BigDecimal as appropriate.
         */
         String str = getFirstToken().image;
         try 
         {
             value = new Double( str );

         } catch ( NumberFormatException E1 ) 
         {

            // if there's still an Exception it will propogate out
            value = new BigDecimal( str );

        }

        return data;
    }

    public Object value( InternalContextAdapter context)
    {
        return value;
    }


}
