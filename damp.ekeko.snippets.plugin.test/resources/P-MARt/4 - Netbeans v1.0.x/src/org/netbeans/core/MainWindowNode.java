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

package org.netbeans.core;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

/** Data object that is created when file Filesystem.system is found on a disk.
* This object represents all filesystems in the filesystem pool, allow access
* to their contents.
* Final only for performance reasons, can be unfinaled
*
* @author Ian Formanek, Jaroslav Tulach, Petr Hamernik,
* Jan Jancura, Dafe Simonek
*/
final class MainWindowNode extends AbstractNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3999551852670168828L;
    /** property name for window location */
    public static final String PROP_LOCATION = "location"; // NOI18N
    /** property name for window size */
    public static final String PROP_SIZE = "size"; // NOI18N

    /** Default icon base for control panel. */
    private static final String MWN_ICON_BASE =
        "/org/netbeans/core/resources/mainWindow"; // NOI18N

    /** bundle to obtain text information from */
    private static java.util.ResourceBundle bundle;

    /** The Corona IDE's MainWindow */
    private transient Frame mainWindow;

    /** location of the main window */
    private Point windowLocation;
    /** size of the main window */
    private Dimension windowSize;

    /** Constructs a new MainWindowNode.
    */
    public MainWindowNode() {
        super (new Children.Array ());

        mainWindow = TopManager.getDefault ().getWindowManager ().getMainWindow();
        windowLocation = mainWindow.getLocation();
        windowSize = mainWindow.getSize();

        init ();
    }

    /**
    * Initialization.
    */
    private void init () {
        setName(NbBundle.getBundle(MainWindowNode.class).
                getString("CTL_MainWindow"));
        setIconBase(MWN_ICON_BASE);
        createProperties();

        mainWindow.addComponentListener(new ComponentAdapter() {
                                            public void componentResized(ComponentEvent e) {
                                                Dimension oldSize = windowSize;
                                                windowSize = mainWindow.getSize();
                                                firePropertyChangeHelper (
                                                    PROP_SIZE, oldSize, windowSize
                                                );
                                            }
                                            public void componentMoved(ComponentEvent e) {
                                                Point oldLocation = windowLocation;
                                                windowLocation = mainWindow.getLocation();
                                                firePropertyChangeHelper (
                                                    PROP_LOCATION, oldLocation, windowLocation
                                                );
                                            }
                                        }
                                       );
    }

    /** Setter for a MainWindow's Size property
    * @param value new Size property value
    */
    public void setSize(Dimension value) {
        if (windowSize.equals(value))
            return;
        Dimension oldValue = windowSize;
        mainWindow.setSize(value);
        firePropertyChange(PROP_SIZE, oldValue, value);
    }

    /** Getter for a MainWindow's Size property
    * @return current Size property value
    */
    public Dimension getSize() {
        return windowSize;
    }

    /** Setter for a MainWindow's Location property
    * @param value new Location property value
    */
    public void setLocation(Point value) {
        if (windowLocation.equals(value))
            return;
        Point oldValue = windowLocation;
        mainWindow.setLocation(value);
        firePropertyChange(PROP_LOCATION, oldValue, value);
    }

    /** Getter for a MainWindow's Location property
    * @return current Location property value
    */
    public Point getLocation() {
        return windowLocation;
    }

    /** Read and initialize the object.
    */
    private void readObject (ObjectInputStream ois)
    throws IOException, ClassNotFoundException {
        ois.defaultReadObject ();

        mainWindow = TopManager.getDefault ().getWindowManager ().getMainWindow();

        init ();
    }

    /** Helper for refiring property change.
    */
    void firePropertyChangeHelper (String name, Object o, Object n) {
        firePropertyChange (name, o, n);
    }

    /** Creates array of subnodes.
    */
    public Node[] createInitNodes() {
        return new Node[] {
                   //      NbTopManager.getMenuNode(this),
                   //  CoronaTopManager.getToolbarNode(this),
               };
    }

    /** Method that prepares properties. Called from initialize.
    */
    protected void createProperties () {
        final java.util.ResourceBundle topBundle =
            NbBundle.getBundle(MainWindowNode.class);
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault();
        sheet.get(Sheet.PROPERTIES).put(
            new PropertySupport.ReadWrite (
                PROP_LOCATION, Point.class,
                topBundle.getString("MWO_PROP_LOCATION"),
                topBundle.getString("MWO_HINT_LOCATION")
            ) {
                public Object getValue () throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    return getLocation();
                }

                public void setValue (Object val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!(val instanceof Point))
                        throw new IllegalArgumentException();
                    setLocation((Point)val);
                }
            }
        );
        sheet.get(Sheet.PROPERTIES).put(
            new PropertySupport.ReadWrite (
                PROP_SIZE, Dimension.class,
                topBundle.getString("MWO_PROP_SIZE"),
                topBundle.getString("MWO_HINT_SIZE")
            ) {
                public Object getValue () throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    return getSize();
                }

                public void setValue (Object val) throws IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException {
                    if (!(val instanceof Dimension))
                        throw new IllegalArgumentException();
                    setSize((Dimension)val);
                }
            }
        ); // end of put call
        // and set new sheet
        setSheet(sheet);
    }

}

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Jaroslav Tulach I18N
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         8/1/99   Ian Formanek    access modifiers cleaned
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  5    Gandalf   1.4         3/18/99  Jaroslav Tulach 
 *  4    Gandalf   1.3         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
 *       ide.loaders.*
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    moved to package org.netbeans.core
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach changed number of constructor parameters
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    added OutputWindow to the MainWindow context
 *  0    Tuborg    0.17        --/--/98 Jaroslav Tulach isLinkAllowed changed to isShadowAllowed
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.21        --/--/98 Jan Jancura     WorkspaceNode moved out
 *  0    Tuborg    0.22        --/--/98 Petr Hamernik   init of subnodes changed
 *  0    Tuborg    0.23        --/--/98 Jan Formanek    a lot of code moved to MainWindowHandler
 *  0    Tuborg    0.24        --/--/98 Jan Jancura     new property model
 *  0    Tuborg    1.00        --/--/98 Jaroslav Tulach redesigned to new DataObjects
 *  0    Tuborg    1.01        --/--/98 Jaroslav Tulach extends SystemObject
 *  0    Tuborg    1.02        --/--/98 Jaroslav Tulach is not leaf, serializable correction
 *  0    Tuborg    1.03        --/--/98 Jan Palka       add shortcutNode to initial Nodes[]
 *  0    Tuborg    1.04        --/--/98 Petr Hamernik   changed to be node.
 *  0    Tuborg    1.05        --/--/98 Jan Formanek    reflecting getMainWindow changes
 *  0    Tuborg    1.06        --/--/98 Ales Novak      bugfix
 *  0    Tuborg    1.07        --/--/98 Jan Formanek    shortcuts and Workspaces moved one level up (out of this node)
 */
