package org.apache.html.dom;


import org.w3c.dom.*;
import org.w3c.dom.html.*;


/**
 * @version $Revision: 1.1 $ $Date: 2006/02/02 02:35:48 $
 * @author <a href="mailto:arkin@openxml.org">Assaf Arkin</a>
 * @see org.w3c.dom.html.HTMLOptionElement
 * @see ElementImpl
 */
public final class HTMLOptionElementImpl
    extends HTMLElementImpl
    implements HTMLOptionElement
{

    

    public boolean getDefaultSelected()
    {
        // ! NOT FULLY IMPLEMENTED !
        return getBinary( "default-selected" );
    }
    
    
    public void setDefaultSelected( boolean defaultSelected )
    {
        // ! NOT FULLY IMPLEMENTED !
        setAttribute( "default-selected", defaultSelected );
    }

  
    public String getText()
    {
        Node    child;
        String    text;
        
        // Find the Text nodes contained within this element and return their
        // concatenated value. Required to go around comments, entities, etc.
        child = getFirstChild();
        text = "";
        while ( child != null )
        {
            if ( child instanceof Text )
                text = text + ( (Text) child ).getData();
            child = child.getNextSibling();
        }
        return text;
    }
    
    
    public void setText( String text )
    {
        Node    child;
        Node    next;
        
        // Delete all the nodes and replace them with a single Text node.
        // This is the only approach that can handle comments and other nodes.
        child = getFirstChild();
        while ( child != null )
        {
            next = child.getNextSibling();
            removeChild( child );
            child = next;
        }
        insertBefore( getOwnerDocument().createTextNode( text ), getFirstChild() );
    }
    
    
    public int getIndex()
    {
        Node        parent;
        NodeList    options;
        int            i;
        
        // Locate the parent SELECT. Note that this OPTION might be inside a
        // OPTGROUP inside the SELECT. Or it might not have a parent SELECT.
        // Everything is possible. If no parent is found, return -1.
        parent = getParentNode();
        while ( parent != null && ! ( parent instanceof HTMLSelectElement ) )
            parent = parent.getParentNode();
        if ( parent != null )
        {
            // Use getElementsByTagName() which creates a snapshot of all the
            // OPTION elements under the SELECT. Access to the returned NodeList
            // is very fast and the snapshot solves many synchronization problems.
            options = ( (HTMLElement) parent ).getElementsByTagName( "OPTION" );
            for ( i = 0 ; i < options.getLength() ; ++i )
                if ( options.item( i ) == this )
                    return i;
        }
        return -1;
    }
    
    
    public void setIndex( int index )
    {
        Node        parent;
        NodeList    options;
        Node        item;
        
        // Locate the parent SELECT. Note that this OPTION might be inside a
        // OPTGROUP inside the SELECT. Or it might not have a parent SELECT.
        // Everything is possible. If no parent is found, just return.
        parent = getParentNode();
        while ( parent != null && ! ( parent instanceof HTMLSelectElement ) )
            parent = parent.getParentNode();
        if ( parent != null )
        {
            // Use getElementsByTagName() which creates a snapshot of all the
            // OPTION elements under the SELECT. Access to the returned NodeList
            // is very fast and the snapshot solves many synchronization problems.
            // Make sure this OPTION is not replacing itself.
            options = ( (HTMLElement) parent ).getElementsByTagName( "OPTION" );
            if ( options.item( index ) != this )
            {
                // Remove this OPTION from its parent. Place this OPTION right
                // before indexed OPTION underneath it's direct parent (might
                // be an OPTGROUP).
                getParentNode().removeChild( this );
                item = options.item( index );
                item.getParentNode().insertBefore( this, item );
            }
        }
    }
  
  
    public boolean getDisabled()
    {
        return getBinary( "disabled" );
    }
    
    
    public void setDisabled( boolean disabled )
    {
        setAttribute( "disabled", disabled );
    }

    
      public String getLabel()
    {
        return capitalize( getAttribute( "label" ) );
    }
    
    
    public void setLabel( String label )
    {
        setAttribute( "label", label );
    }

    
    public boolean getSelected()
    {
        return getBinary( "selected" );
    }
  
  
    public void setSelected( boolean selected )
    {
        setAttribute( "selected", selected );
    }
    
        
    public String getValue()
    {
        return getAttribute( "value" );
    }
    
    
    public void setValue( String value )
    {
        setAttribute( "value", value );
    }

    
    /**
     * Constructor requires owner document.
     * 
     * @param owner The owner HTML document
     */
    public HTMLOptionElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }


}

