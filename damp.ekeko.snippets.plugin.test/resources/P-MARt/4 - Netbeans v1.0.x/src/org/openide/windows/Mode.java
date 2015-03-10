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

package org.openide.windows;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.Serializable;
import java.beans.PropertyChangeListener;

/** A window-management mode in which a
* <code>TopComponent</code> can be.
*
* <p>{@link TopComponent} defines default modes, which are always present.
* Modules can add their own modes by calling
* {@link TopComponent#createMode} in their initialization code.<P>
* Modules can also get a list of current modes by calling
* {@link TopComponent#modes}.<p>
* 
* Mode is valid till someone keeps a reference to it.
*
* Each mode must have a unique name.
*/
public interface Mode extends Serializable {

    static final long serialVersionUID =-2650968323666215654L;
    // OK - now we have no types of mode - some types will be in implementation
    // and user will have a chance to play withrows it, but there will be no
    // programmatical access to set the type of mode (type of containing frame -
    // multitab, side-by-side, panel, internal frame...)

    /** Name of property for bounds of the mode */
    public static final String PROP_BOUNDS = "bounds"; // NOI18N

    /** Name of property for the unique programmatic name of this mode. */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of property for the display name of this mode. */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N

    /** Get the diplay name of the mode.
    * This name will be used by a container to create its title.
    * @return human-presentable name of the mode
    */
    public String getDisplayName ();

    /** Get the programmatic name of the mode.
    * This name should be unique, as it is used to find modes etc.
    * @return programmatic name of the mode
    */
    public String getName ();

    /** Get the icon of the mode. It will be used by component container
    * implementations as the icon (e.g. for display in tabs).
    * @return the icon of the mode (or <code>null</code> if no icon was specified)
    */
    public Image getIcon ();

    /** Attaches a component to a mode for this workspace.
    * If the component is in different mode on this desktop, it is 
    * removed from the original and moved to this one.
    *
    * @param c component
    */
    public boolean dockInto (TopComponent c);

    /* Allows implementor to specify some restrictive policy which and how much
    * top components can be docked to this mode.
    * @return true if given top component can be docked to this mode,
    * false otherwise */
    public boolean canDock (TopComponent tc);

    /** Sets the bounds of the mode.
    * @param s the bounds for the mode 
    */
    public void setBounds (Rectangle s);

    /** Getter for current bounds of the mode.
    * @return the bounds of the mode
    */
    public Rectangle getBounds ();

    /** Getter for asociated workspace.
    * @return The workspace instance to which is this mode asociated.
    */
    public Workspace getWorkspace ();

    /** @return array of top components which are currently
    * docked in this mode. May return empty array if no top component
    * is docked in this mode.
    */
    public TopComponent[] getTopComponents ();

    /** Add a property change listener.
    * @param list the listener to add
    */
    public void addPropertyChangeListener (PropertyChangeListener list);

    /** Remove a property change listener.
    * @param list the listener to remove
    */
    public void removePropertyChangeListener (PropertyChangeListener list);

}

/*
* Log
*  6    Gandalf   1.5         1/13/00  David Simonek   i18n
*  5    Gandalf   1.4         11/3/99  David Simonek   completely rewritten 
*       serialization of windowing system...
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         8/18/99  Ian Formanek    Generated serial version 
*       UID
*  2    Gandalf   1.1         7/29/99  David Simonek   further ws serialization 
*       changes
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/