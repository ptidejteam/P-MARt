package org.apache.velocity.runtime;

/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2001 The Apache Software Foundation.  All rights
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

import java.util.Hashtable;
import java.util.TreeMap;
import java.io.ByteArrayInputStream;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.VelocimacroProxy;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;
import org.apache.velocity.runtime.Runtime;

import org.apache.velocity.context.InternalContextAdapter;

/**
 *  VelocimacroManager.java
 *
 *   manages VMs in namespaces.  Currently, there are two namespace modes 
 *   supported :
 *   <ul>
 *   <li>  flat namespace : all allowable VMs are in the global namespace
 *   <li>  local namespace : inline VMs are added to it's own template namespace
 *   </ul>
 *
 *   Thanks to <a href="mailto:JFernandez@viquity.com">Jose Alberto Fernandez</a>
 *   for some ideas incorporated here.
 *
 * @author <a href="mailto:geirm@optonline.net">Geir Magnusson Jr.</a>
 * @author <a href="mailto:JFernandez@viquity.com">Jose Alberto Fernandez</a>
 * @version $Id: VelocimacroManager.java,v 1.9 2001/03/20 01:11:15 jon Exp $ 
 */
public class VelocimacroManager
{
    private static String GLOBAL_NAMESPACE = "";

    /*  hash of namespace hashes */
    private Hashtable namespaceHash = new Hashtable();

    /* 
     * big switch for namespaces.  If true, then properties control 
     * usage. If false, no. 
     */
    private boolean namespacesOn = true;
    private boolean  inlineLocalMode = false;

    /**
     *  not much to do but add the global namespace to the hash
     */
    VelocimacroManager()
    {
        /*
         *  add the global namespace to the namespace hash. We always have that.
         */

        addNamespace( GLOBAL_NAMESPACE );
    }

    /**
     *  adds a VM definition to the cache
     * @return boolean if all went ok
     */
    public boolean addVM(String vmName, String macroBody, String argArray[], String namespace )
    {

        MacroEntry me = new MacroEntry( this, vmName,  macroBody,  argArray,  namespace );

        if ( usingNamespaces( namespace ) )
        {
            /*
             *  first, do we have a namespace hash already for this namespace?
             *  if not, add it to the namespaces, and add the VM
             */

            Hashtable local = getNamespace( namespace, true );
            local.put( (String) vmName, me );
         
            return true;
        }
        else
        {
            /*
             * otherwise, add to global template
             */

            (getNamespace( GLOBAL_NAMESPACE )).put( vmName, me );

            return true;
        }
    }

    /**
     * gets a new living VelocimacroProxy object by the 
     * name / source template duple
     */
    public VelocimacroProxy get( String vmName, String namespace )
    {
        if ( usingNamespaces( namespace ) )
        {
            Hashtable local =  getNamespace( namespace, false );
         
            /*
             *  if we have macros defined for this template
             */

            if ( local != null)
            {
                MacroEntry me = (MacroEntry) local.get( vmName );

                if (me != null)
                    return me.createVelocimacro();
            }
        }

        /*
         * if we didn't return from there, we need to simply see 
         * if it's in the global namespace
         */
        
        MacroEntry me = (MacroEntry) (getNamespace( GLOBAL_NAMESPACE )).get( vmName );
        
        if (me != null)
            return me.createVelocimacro();

        return null;
    }

    /**
     * Removes the VMs and the namespace from the manager.
     * Used when a template is reloaded to avoid 
     * accumulating drek
     *
     * @param namespace namespace to dump
     * @return boolean representing success
     */
    public boolean dumpNamespace( String namespace )
    {
        synchronized( this )
        {
            if (usingNamespaces( namespace ) )
            {
                Hashtable h = (Hashtable) namespaceHash.remove( namespace );

                if ( h == null )
                    return false;
            
                h.clear();
               
                return true;
            }

            return false;
        }
    }

    /**
     *  public switch to let external user of manager to control namespace
     *  usage indep of properties.  That way, for example, at startup the 
     *  library files are loaded into global namespace
     */
    public void setNamespaceUsage( boolean b )
    {
        namespacesOn = b;
        return;
    }

    public void setTemplateLocalInlineVM( boolean b )
    {
        inlineLocalMode = b;
    }

    /**
     *  returns the hash for the specified namespace.  Will not create a new one
     *  if it doesn't exist
     *
     *  @param namespace  name of the namespace :)
     *  @return namespace Hashtable of VMs or null if doesn't exist
     */
    private Hashtable getNamespace( String namespace )
    {
        return getNamespace( namespace, false );
    }

    /**
     *  returns the hash for the specified namespace, and if it doesn't exist
     *  will create a new one and add it to the namespaces
     *
     *  @param namespace  name of the namespace :)
     *  @param addIfNew  flag to add a new namespace if it doesn't exist
     *  @return namespace Hashtable of VMs or null if doesn't exist
     */
    private Hashtable getNamespace( String namespace, boolean addIfNew )
    {
        Hashtable h = (Hashtable)  namespaceHash.get( namespace );

        if (h == null && addIfNew)               
            h = addNamespace( namespace );
  
        return h;
    }

    /**
     *   adds a namespace to the namespaces
     *
     *  @param namespace name of namespace to add
     *  @return Hash added to namespaces, ready for use
     */
    private Hashtable addNamespace( String namespace )
    {
        Hashtable h = new Hashtable();
        Object oh;

        if ((oh = namespaceHash.put( namespace, h )) != null)
        {
          /*
           * There was already an entry on the table, restore it!
           * This condition should never occur, given the code
           * and the fact that this method is private.
           * But just in case, this way of testing for it is much
           * more efficient than testing before hand using get().
           */
          namespaceHash.put( namespace, oh );
          /*
           * Should't we be returning the old entry (oh)?
           * The previous code was just returning null in this case.
           */
          return null;
        }
        
        return h;
    }

    /**
     *  determines if currently using namespaces.
     *
     *  @param namespace currently ignored
     *  @return true if using namespaces, false if not
     */
    private boolean usingNamespaces( String namespace )
    {
        /*
         *  if the big switch turns of namespaces, then ignore the rules
         */

        if ( !namespacesOn )
            return false;

        /*
         *  currently, we only support the local template namespace idea
         */

        if ( inlineLocalMode )
            return true;

        return false;
    }

    /**
     *  wrapper class for holding VM information
     */
    protected class MacroEntry
    {
        String macroname;
        String[] argarray;
        String macrobody;
        String sourcetemplate;
        SimpleNode nodeTree = null;
        VelocimacroManager manager = null;

        MacroEntry(VelocimacroManager vmm, String vmName, String macroBody, String argArray[],  String sourceTemplate)
        {
            this.macroname = vmName;
            this.argarray = argArray;
            this.macrobody = macroBody;
            this.sourcetemplate = sourceTemplate;
            this.manager = vmm;
        }

        public SimpleNode getNodeTree()
        {
            return nodeTree;
        }

        VelocimacroProxy createVelocimacro()
        {
            VelocimacroProxy vp = new VelocimacroProxy();
            vp.setName( this.macroname );
            vp.setArgArray(  this.argarray ); 
            vp.setMacrobody( this.macrobody );
            vp.setNodeTree( this.nodeTree);

            return vp;
        }

        void setup( InternalContextAdapter ica)
        {
            /*
             *  if not parsed yet, parse!
             */
            
            if( nodeTree == null)
                parseTree( ica );
        }

        void parseTree( InternalContextAdapter ica)
        {
            try 
            {

                ByteArrayInputStream  inStream = new ByteArrayInputStream( macrobody.getBytes() );
                nodeTree = Runtime.parse( inStream, "VM:" + macroname );
                nodeTree.init(ica,null);
            } 
            catch ( Exception e ) 
            {
                Runtime.error("VelocimacroManager.parseTree() : exception " + macroname + 
                              " : "  + StringUtils.stackTrace(e));
            }
        }
    }
}
