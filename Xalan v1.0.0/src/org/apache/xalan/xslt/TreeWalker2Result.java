/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 *     the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xalan.xslt;

import org.apache.xalan.xpath.xml.TreeWalker;
import org.apache.xalan.xpath.xml.MutableAttrListImpl;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * <meta name="usage" content="internal"/>
 * Handle a walk of a tree, but screen out attributes for 
 * the result tree.
 */
class TreeWalker2Result extends TreeWalker
{
  ElemTemplateElement m_elem;
  XSLTEngineImpl m_processor;
  Node m_startNode;
  
  /**
   * Constructor.
   */
  public TreeWalker2Result(XSLTEngineImpl processor, ElemTemplateElement elem) 
  {
    super(processor.m_resultTreeHandler);
    m_elem = elem;
    m_processor = processor;
  }
  
    
  /**
   * Perform a pre-order traversal non-recursive style.
   */
  public void traverse(Node pos) throws SAXException 
  {
    m_startNode = pos;
    super.traverse(pos);
  }
  
  protected void startNode(Node node)
    throws SAXException 
  {
    if((Node.ELEMENT_NODE == node.getNodeType()) && (m_startNode == node))
    {
      String elemName = node.getNodeName();
      m_processor.m_resultTreeHandler.startElement (elemName);
        
      for(Node parent = node; parent != null; parent = parent.getParentNode())
      {
        if(Node.ELEMENT_NODE != parent.getNodeType())
          continue;
      
        NamedNodeMap atts = ((Element)parent).getAttributes();

        int n = atts.getLength();
        for(int i = 0; i < n; i++)
        {
          String nsDeclPrefix = null;
          Attr attr = (Attr)atts.item(i);
          String name = attr.getName();
          String value = attr.getValue();
          
          if (name.startsWith("xmlns:"))
          {
            // get the namespace prefix 
            nsDeclPrefix = name.substring(name.indexOf(":") + 1);
          }
          else if(name.equals("xmlns"))
          {
            nsDeclPrefix="";
          }
          
          if((nsDeclPrefix == null) && (node != parent))
            continue;
          /*
          else if(nsDeclPrefix != null)
          {
            String desturi = m_processor.getResultNamespaceForPrefix(nsDeclPrefix);
            // Look for an alias for this URI. If one is found, use it as the result URI   
            String aliasURI = m_elem.m_stylesheet.lookForAlias(value);
            if(aliasURI.equals(desturi)) // TODO: Check for extension namespaces
            {
              continue;
            }
          }
          */
          
          // Make sure namespace is not in the excluded list then
          // add to result tree
          // if((nsDeclPrefix == null)  ||!(m_elem.shouldExcludeResultNamespaceNode(m_elem, nsDeclPrefix, value)))
          if(!m_processor.m_pendingAttributes.contains(name))
          {
            if(nsDeclPrefix == null)
            {  
              m_processor.m_pendingAttributes.addAttribute(name, "CDATA", value);
            }
            else
            {
              String desturi = m_processor.getResultNamespaceForPrefix(nsDeclPrefix);
              if(null == desturi)
              {
                m_processor.m_pendingAttributes.addAttribute(name, "CDATA", value);
              }
              else if(!desturi.equals(value))
              {
                m_processor.m_pendingAttributes.addAttribute(name, "CDATA", value);
              }
            }
          }
        }

      }
      m_elem.processResultNS(m_processor);           
      
    }
    else
    {
      super.startNode(node);
    }
  }

}
