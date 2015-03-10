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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.net.URL;

import javax.swing.SwingUtilities;
import javax.swing.undo.*;
import javax.swing.event.*;

import org.openide.TopManager;

import org.openide.awt.UndoRedo;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.awt.ToolbarPool;

/** Represents one user workspace that holds a list of modes into which
* components can be assigned.
* Created by WindowManager.
* When serialized only keeps "weak" reference to this workspace does not
* stores the content of the workspace (it is responsibility of window manager).
*
* @author Jaroslav Tulach
*/
public interface Workspace extends Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2987897537843190271L;

    /** Name of property for modes in the workspace */
    public static final String PROP_MODES = "modes"; // NOI18N

    /** Name of property for the programmatic name of this workspace. */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of property for the display name of this workspace. */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N

    /** Get unique programmatical name of this workspace.
    * @return unique name of the workspace
    */
    public String getName ();

    /** Get human-presentable name of the workspace which
    * will be used for displaying.
    * @return the display name of the workspace
    */
    public String getDisplayName ();

    /** Array of all modes on this workspace.
    */
    public Set getModes ();

    /** Get bounds of the workspace. Returned value has slighly different
    * meaning for SDI and MDI mode. Modules should use this method for
    * correct positioning of their windows.
    * @return In SDI, returns bounds relative to whole screen, retunrns bounds
    * of the part of screen below main window (or above main window, if main
    * window is on bottom part of the screen).<br>
    * In MDI, bounds are relative to the main window; returned value represents
    * 'client area' of the main window */
    public Rectangle getBounds ();

    /** Activates this workspace to be current one.
    * This leads to change of current workspace of the WindowManager.
    */
    public void activate ();

    /** Create a new mode.
    * @param name a unique programmatic name of the mode 
    * @param displayName a human presentable (probably localized) name
    *                    of the mode (may be used by
                         {@link org.openide.actions.DockingAction}, e.g.)
    * @param icon an url to the icon to use for the mode (e.g. on a tab or window corner);
    *             may be <code>null</code>
    * @return the new mode
    */
    public Mode createMode (String name, String displayName, URL icon);

    /** Search all modes on this workspace by name.
    * @param name the name of the mode to search for
    * @return the mode with that name, or <code>null</code> if no such mode
    *         can be found
    */
    public Mode findMode (String name);

    /** Finds mode the component is in on this workspace.
    *
    * @param c component to find mode for
    * @return the mode or null if the component is not visible on this workspace
    */
    public Mode findMode (TopComponent c);

    /** Removes this workspace from set of workspaces
    * in window manager. 
    */
    public void remove ();

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
 *  38   Gandalf   1.37        1/13/00  David Simonek   i18n
 *  37   Gandalf   1.36        12/6/99  David Simonek   method getBounds() added
 *  36   Gandalf   1.35        11/3/99  David Simonek   completely rewritten 
 *       serialization of windowing system...
 *  35   Gandalf   1.34        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  34   Gandalf   1.33        7/29/99  David Simonek   further ws serialization
 *       changes
 *  33   Gandalf   1.32        7/28/99  David Simonek   method remove() added
 *  32   Gandalf   1.31        7/11/99  David Simonek   window system change...
 *  31   Gandalf   1.30        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  30   Gandalf   1.29        5/26/99  Ian Formanek    
 *  29   Gandalf   1.28        5/17/99  David Simonek   cloning single windows 
 *       now ok
 *  28   Gandalf   1.27        5/15/99  David Simonek   improving serialization 
 *       to allow component to resolve to null
 *  27   Gandalf   1.26        5/15/99  David Simonek   information messages 
 *       during deserializing
 *  26   Gandalf   1.25        5/14/99  Libor Kramolis  
 *  25   Gandalf   1.24        5/12/99  Libor Kramolis  
 *  24   Gandalf   1.23        5/12/99  David Simonek   toolbar pool 
 *       initialization now lazy
 *  23   Gandalf   1.22        5/12/99  Libor Kramolis  
 *  22   Gandalf   1.21        5/11/99  David Simonek   changes to made window 
 *       system correctly serializable
 *  21   Gandalf   1.20        4/7/99   David Simonek   docking action now 
 *       disabled on non top components
 *  20   Gandalf   1.19        4/1/99   David Simonek   
 *  19   Gandalf   1.18        3/30/99  David Simonek   Fixed bug in 
 *       windowActivated()
 *  18   Gandalf   1.17        3/30/99  Jesse Glick     [JavaDoc]
 *  17   Gandalf   1.16        3/29/99  Jesse Glick     [JavaDoc]
 *  16   Gandalf   1.15        3/29/99  Jesse Glick     [JavaDoc]
 *  15   Gandalf   1.14        3/26/99  David Simonek   small bug when reopening
 *       in single fixed
 *  14   Gandalf   1.13        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  13   Gandalf   1.12        3/25/99  David Simonek   changes in window 
 *       system, initial positions, bugfixes
 *  12   Gandalf   1.11        3/22/99  David Simonek   
 *  11   Gandalf   1.10        3/19/99  David Simonek   
 *  10   Gandalf   1.9         3/19/99  David Simonek   
 *  9    Gandalf   1.8         3/17/99  David Simonek   slightly changed window 
 *       system
 *  8    Gandalf   1.7         3/14/99  Jaroslav Tulach 
 *  7    Gandalf   1.6         3/14/99  David Simonek   
 *  6    Gandalf   1.5         3/11/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         3/10/99  Jaroslav Tulach UndoRedo
 *  4    Gandalf   1.3         3/5/99   Ales Novak      
 *  3    Gandalf   1.2         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  2    Gandalf   1.1         2/16/99  Jaroslav Tulach Positions of windows are
 *       relative to screen size.
 *  1    Gandalf   1.0         2/12/99  Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.32        --/--/98 Jaroslav Tulach Added component events listener
 *  0    Tuborg    0.33        --/--/98 Ales Novak      externalization
 *  0    Tuborg    0.34        --/--/98 Ales Novak      factory for TopFrameTransferableOwner
 *  0    Tuborg    0.35        --/--/98 Ales Novak      cloneable
 *  0    Tuborg    0.36        --/--/98 Petr Hamernik   bugfixes
 *  0    Tuborg    0.37        --/--/98 Petr Hamernik   bugfixes
 *  0    Tuborg    0.38        --/--/98 Petr Hamernik   placing the frames
 *  0    Tuborg    0.39        --/--/98 Jan Formanek    commented out removing Frame on iconify
 *  0    Tuborg    0.40        --/--/98 Jaroslav Tulach Workspace.Element instead of TopFrames
 *  0    Tuborg    0.42        --/--/98 Petr Hamernik   some small improvements (calling positioning frame method)
 *  0    Tuborg    0.43        --/--/98 Jaroslav Tulach list of all Workspace.Elements
 *  0    Tuborg    0.44        --/--/98 Ales Novak      closing MultiObjectFrame
 */
