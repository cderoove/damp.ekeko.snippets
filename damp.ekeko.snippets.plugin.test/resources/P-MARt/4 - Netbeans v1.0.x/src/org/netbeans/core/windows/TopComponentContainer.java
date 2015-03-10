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

package org.netbeans.core.windows;

import java.awt.*;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.windows.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/** Interface for top component containers. It's associated with exactly one
* mode. Implementors of this interface should manage the task of containing
* multiple top components, switching between them etc.
* Implementors should also listen to the property changes of the contained
* top components and the mode which they represent.
*
* @author Dafe Simonek
*/
interface TopComponentContainer {

    /** @return an array containing all top components which are currently
    * managed by this container */
    public TopComponent[] getTopComponents ();

    /** Adds given top component to this container.
    * Beware, the code must be written to ensure that top component
    * will receive addNotify() before componentActivated(). 
    *
    * @param tc top component to add
    * @return false if top component already exixts in container or 
    * cannot be added due to container-dependent restrictions,
    * true otherwise
    */
    public boolean addTopComponent (TopComponent tc);

    /** @return true if new top component can be added, false otherwise */
    public boolean canAdd (TopComponent tc);

    /** Removes specified top component from this container.
    * Beware! The code in this method must be written to ensure
    * that each top component will receive componentDeactivated() before
    * removeNotify().
    *
    * @param tc top component to remove
    * @return the count of remaining contained components
    */
    public int removeTopComponent (TopComponent tc);

    /** @return true if container contains specified top component,
    * false otherwise
    */
    public boolean containsTopComponent (TopComponent tc);

    /** @return Currently selected top component or null if no top
    * component is selected at a time */ 
    public TopComponent getSelectedTopComponent ();

    /** The component requests focus. Move it to be visible. When in iconified
    * state, deiconify first.
    */
    public void requestFocus (TopComponent tc);

    /** Whole mode requests focus. Move it to be visible. When in iconified
    * state, deiconify first.
    */
    public void requestFocus ();

    /** Sets the state of container - normal x iconified
    * @param state new state of the comtainer (constants from FRAME.xxx)
    */
    public void setState (int state);

    /** @return current state (normal x iconified) of the container
    */
    public int getState ();

    /** Sets visibility status for container
    * @param visible true if container should be made visible,
    * false otherwise
    */
    public void setVisible (boolean visible);

    /** @return true if container currently visible, false otherwise. */
    public boolean isVisible ();

    /** @return true if container is currently visible on the screen
    * (is visible and its parent in container heirarchy is showing),
    * false otherwise */
    public boolean isShowing ();

    /** Sets the bounds for container.
    * @bounds New bounds.
    */
    public void setBounds (Rectangle bounds);

    /** Request for UI update (when changing L&F) */
    public void updateUI ();

    /** Closes the container and frees system resources. */
    public void dispose ();

    /** Adds given container listener for listening to the container
    * events */
    public void addContainerListener (ContainerListener cl);

    /** Removes given container listener */
    public void removeContainerListener (ContainerListener cl);

    /** Called when first phase of WS deserialization is done.
    * TC container will usually finish the deserialization of all
    * contained top components here.
    */
    public void validateData ();

}

/*
* Log
*  7    Gandalf   1.6         12/17/99 David Simonek   #1913, #2970
*  6    Gandalf   1.5         11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  5    Gandalf   1.4         11/3/99  David Simonek   completely rewritten 
*       serialization of windowing system...
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         9/13/99  David Simonek   request focus for the 
*       mode added
*  2    Gandalf   1.1         8/14/99  David Simonek   bugfixes, #3347, #3274 
*       etc.
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/
