package org.apache.velocity.runtime.parser.node;

/*
 * Copyright 2000-2006 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.app.event.EventHandlerUtil;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.ParserVisitor;

/**
 * Node for the #set directive
 *
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @version $Id: ASTSetDirective.java 383711 2006-03-07 00:01:00Z nbubna $
 */
public class ASTSetDirective extends SimpleNode
{
    private String leftReference = "";
    private Node right = null;
    private ASTReference left = null;
    boolean logOnNull = false;

    public ASTSetDirective(int id)
    {
        super(id);
    }

    public ASTSetDirective(Parser p, int id)
    {
        super(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    /**
     *  simple init.  We can get the RHS and LHS as the the tree structure is static
     */
    public Object init(InternalContextAdapter context, Object data)
            throws Exception
    {
        /*
         *  init the tree correctly
         */

        super.init( context, data );

        right = getRightHandSide();
        left = getLeftHandSide();

        logOnNull = rsvc.getBoolean(RuntimeConstants.RUNTIME_LOG_REFERENCE_LOG_INVALID, true);
 
        /*
         *  grab this now.  No need to redo each time
         */
        leftReference = left.getFirstToken().image.substring(1);

        return data;
    }        

    /**
     *   puts the value of the RHS into the context under the key of the LHS
     */
    public boolean render( InternalContextAdapter context, Writer writer)
        throws IOException, MethodInvocationException
    {
        /*
         *  get the RHS node, and its value
         */

        Object value = right.value(context);

        /*
         * it's an error if we don't have a value of some sort AND 
         * it is not allowed by configuration 
         */

        if( !rsvc.getBoolean(RuntimeConstants.SET_NULL_ALLOWED,false) ) 
        {
            if ( value == null )
            {
                /*
                 *  first, are we supposed to say anything anyway?
                 */
                if(logOnNull)
                {
                    boolean doit = EventHandlerUtil.shouldLogOnNullSet( rsvc, context, left.literal(), right.literal() );
    
                    if (doit && log.isInfoEnabled())
                    {
                        log.info("RHS of #set statement is null. Context will not be modified. " 
                                      + context.getCurrentTemplateName() + " [line " + getLine() 
                                      + ", column " + getColumn() + "]");
                    }
                }
    
                return false;
            }                
        }      

        if ( value == null )
        {
            /*
             * if RHS is null it doesn't matter if LHS is simple or complex
             * because the LHS is removed from context
             */
            context.remove( leftReference );
        }
        else
        {
            /*
             *  if the LHS is simple, just punch the value into the context
             *  otherwise, use the setValue() method do to it.
             *  Maybe we should always use setValue()
             */
            
            if (left.jjtGetNumChildren() == 0)
            {
                context.put( leftReference, value);
            }
            else
            {
                left.setValue(context, value);
            }
        }
        
        return true;
    }

    /**
     *  returns the ASTReference that is the LHS of the set statememt
     */
    private ASTReference getLeftHandSide()
    {
        return (ASTReference) jjtGetChild(0);

     //   return (ASTReference) jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);
    }

    /**
     *  returns the RHS Node of the set statement
     */
    private Node getRightHandSide()
    {
        return jjtGetChild(1);
//        return jjtGetChild(0).jjtGetChild(0).jjtGetChild(1).jjtGetChild(0);
    }
}
