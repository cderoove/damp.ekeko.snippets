/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.*;

/** Represents a group of modules.
 *
 * @author  Petr Hrebejk
 */
class ModuleGroup extends Object
    implements org.openide.nodes.Node.Cookie {

    private static final String ATTR_NAME = "name"; // NOI18N

    /** Holds the DOM node for this group */
    private Node node;

    /** Holds value of property name. */
    private String name;

    /** Holds value of property items. */
    private Collection items;

    /** Creates new ModuleGroup */
    public ModuleGroup( ) {
        this( null );
    }

    /** Creates new ModuleGroup */
    public ModuleGroup( Node node ) {
        items = new ArrayList( 11 );
        if ( node != null ) {
            this.node = node;
            setName( getAttribute( ATTR_NAME ) );
        }
    }

    /** Adds a ModuleGroup into group items
     */
    void addItem( ModuleGroup group ) {
        items.add( group );
    }

    /** Adds a ModuleUpdate into group items
     */
    void addItem( ModuleUpdate update ) {
        items.add( update );
    }

    // GETTERS AND SETTERS ------------------------------------------------------

    /** Getter for property name.
     *@return Value of property name.
     */
    public String getName() {
        return name;
    }

    /** Setter for property name.
     *@param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** Getter for property items.
     *@return Value of property items.
     */
    public Collection getItems() {
        return items;
    }

    // UTILITY METHODS ----------------------------------------------------------

    /** Utility method gets the atribute of node
     *@param attribute Name of the desired attribute
     */
    private String getAttribute( String attribute ) {
        Node attr = node.getAttributes().getNamedItem( attribute );
        return attr == null ? null : attr.getNodeValue();
    }

}
/*
 * Log
 *  3    Gandalf   1.2         1/12/00  Petr Hrebejk    i18n
 *  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
