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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.beans.*;
import java.lang.ref.SoftReference;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.*;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.options.ControlPanel;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.netbeans.core.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.*;
import org.openide.nodes.*;
import org.openide.nodes.Children;
import org.openide.windows.TopComponent;

import org.netbeans.core.windows.WorkspaceImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.ModeImpl;

/** Class that provides operations on nodes. Any part of system can
* ask for opening a customizer or explorer on any node. These actions
* are accessible thru this class.
*/
public class NbNodeOperation extends TopManager.NodeOperation {
    /** default explorer class */
    private static final String EXPLORER_NAME = "org.netbeans.core.NbMainExplorer$ExplorerTab"; // NOI18N
    /** name of class for to work as explorer */
    private static String explorerName;
    /** default properties class */
    private static final String PROPERTIES_NAME = "org.netbeans.core.NbNodeOperation$Sheet"; // NOI18N
    /** name of class for to work as property displayer */
    private static String propertiesName;


    /** Shows an explorer on the given root Node.
    * @param n the Node that will be the rootContext of the explored hierarchy
    */
    public void explore (Node n) {
        try {
            Object o = createObject (explorerName, EXPLORER_NAME);
            ExplorerManager.Provider p = (ExplorerManager.Provider)o;
            p.getExplorerManager ().setRootContext (n);

            TopComponent c = (TopComponent)o;
            c.open ();

        } catch (ClassCastException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
    }

    /** Tries to open customization for specified node. The dialog is
    * open in modal mode and the function returns after successful
    * customization.
    *
    * @param n the node to customize
    * @return <CODE>true</CODE> if the node has customizer,
    * <CODE>false</CODE> if not
    */
    public boolean customize (Node n) {
        java.awt.Component customizer = n.getCustomizer ();
        if (customizer == null ) return false;
        if (customizer instanceof java.awt.Window) {
            ((java.awt.Window)customizer).pack ();
            customizer.setVisible (true);
            return true;
        }
        Dialog dialog =
            new org.openide.explorer.propertysheet.PropertyDialogManager (
                //TopManager.getDefault ().getMainWindow (),
                NbBundle.getBundle (NbNodeOperation.class).getString ("CTL_Customizer_dialog_title"),
                customizer, true).getDialog();
        dialog.pack();
        dialog.show();
        return true;
    }

    /** Opens a modal propertySheet on given Node
    * @param n the node to show properties for
    */
    public void showProperties (Node n) {
        try {
            Object o = createObject (propertiesName, PROPERTIES_NAME);
            ExplorerManager.Provider p = (ExplorerManager.Provider)o;
            p.getExplorerManager ().setRootContext (n);
            p.getExplorerManager ().setSelectedNodes (new Node[] { n });

            openProperties((TopComponent)o);

        } catch (Exception ex) {
            TopManager.getDefault ().notifyException (ex);
        }
    }

    /** Opens a modal propertySheet on given set of Nodes
    * @param n the array of nodes to show properties for
    */
    public void showProperties (Node[] nodes) {
        try {
            Object o = createObject (propertiesName, PROPERTIES_NAME);
            ExplorerManager.Provider p = (ExplorerManager.Provider)o;
            ExplorerManager m = p.getExplorerManager ();

            if (nodes.length == 0) {
                m.setRootContext (new AbstractNode (Children.LEAF));
            } else {
                m.setRootContext (NodeOp.findRoot (nodes[0]));
                m.setSelectedNodes (nodes);
            }

            openProperties((TopComponent)o);

        } catch (Exception ex) {
            TopManager.getDefault ().notifyException (ex);
        }
    }

    /** Opens explorer for specified root in modal mode. The set
    * of selected components is returned as a result. The acceptor
    * should be asked each time selected nodes changes to accept or
    * reject the current result. This should affect for example the
    * <EM>OK</EM> button.
    *
    * @param title is a title that will be displayed as a title of the window
    * @param root the root to explore
    * @param acceptor the class that is asked for accepting or rejecting
    *    current selection
    * @param top is a component that will be displayed on the top
    * @return array of selected (and accepted) nodes
    *
    * @exception UserCancelException selection interrupted by user
    */
    public Node[] select (String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top)
    throws org.openide.util.UserCancelException {
        FileSelector selector = new FileSelector (title, rootTitle, root, acceptor, top);
        selector.show();
        if (selector.cancelFlag)
            throw new org.openide.util.UserCancelException ();
        return selector.getNodes ();
    }

    /** Helper method, opens properties top component in single mode
    * and requests a focus for it */
    private void openProperties (TopComponent tc) {
        WindowManagerImpl wm =
            (WindowManagerImpl)TopManager.getDefault().getWindowManager();
        WorkspaceImpl curWorkspace = (WorkspaceImpl)wm.getCurrentWorkspace();
        String modeName = wm.findUnusedModeName(tc.getName(), curWorkspace);
        curWorkspace.createMode(
            modeName, modeName, null, ModeImpl.SINGLE, true
        ).dockInto(tc);
        tc.open();
        tc.requestFocus();
    }

    /** Creates instance of class either from first name or
    * if it fails from the second, surely valid name.
    * @param n1 first name to try (or null)
    * @param n2 second name
    * @return the created object
    */
    private static Object createObject (String n1, String n2) {
        TopManager top = TopManager.getDefault ();

        if (n1 != null) {
            // try this name
            try {
                Object o = Beans.instantiate (top.currentClassLoader (), n1);
                return o;
            } catch (Exception e) {
                top.notifyException (e);
            }
            // never mind, try default
        }

        try {
            Object o = Beans.instantiate (top.currentClassLoader (), n2);
            return o;
        } catch (Exception e) {
            throw new InternalError ();
        }
    }

    /** Default view for properties.
    */
    public static class Sheet extends ExplorerPanel {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 7807519514644165460L;

        /** shared sheet */
        private static Sheet sharedSheet;
        /** The SheetFrame's icon SoftReference (Image) */
        private static SoftReference sheetIcon;
        /** listener to the property changes */
        transient private Listener listener;
        /** Should property sheet listen to the global changes ? */
        boolean global;

        /** Constructor for new sheet.
        * The sheet does not listen to global changes */
        public Sheet () {
            // PENDING this probably should be false,
            // this is only for testing purposes
            this (false);
        }

        /** @param global should the content change when global properties changes?
        */
        public Sheet (boolean global) {
            this.global = global;
            add ("Center", new org.openide.explorer.propertysheet.PropertySheetView ()); // NOI18N

            initIcon ();

            // name listener
            listener = new Listener ();
            getExplorerManager ().addPropertyChangeListener (listener);

            updateGlobalListening(true);
        }

        /** Provides the shared instance of SheetFrame */
        public static Sheet getDefault () {
            if (sharedSheet == null)
                sharedSheet = new Sheet (true);
            return sharedSheet;
        }

        public HelpCtx getHelpCtx () {
            return getHelpCtx (getExplorerManager ().getSelectedNodes (),
                               new HelpCtx (Sheet.class));
        }

        /** Provides the icon to be shown as this Window's icon.
        * @return the window's icon
        */
        private void initIcon () {
            Image icon = sheetIcon == null ? null : (Image)sheetIcon.get ();
            if (icon == null) {
                icon = java.awt.Toolkit.getDefaultToolkit ().getImage (
                           getClass ().getResource (
                               org.openide.util.Utilities.isLargeFrameIcons() ?
                               "/org/netbeans/core/resources/frames/properties32.gif" : // NOI18N
                               "/org/netbeans/core/resources/frames/properties.gif" // NOI18N
                           )
                       );
                sheetIcon = new SoftReference (icon);
            }
            setIcon (icon);
        }

        /** Changes name of the component to reflect currently displayed nodes.
        * Called when set of displayed nodes has changed.
        */
        protected void updateTitle () {
            Node[] nodes = getExplorerManager ().getSelectedNodes ();
            // different naming for global and local sheets
            if (global) {
                setName(
                    java.text.MessageFormat.format(
                        NbBundle.getBundle(NbNodeOperation.class).getString("CTL_FMT_GlobalProperties"),
                        new Object[] {
                            new Integer (nodes.length),
                            nodes.length == 0 ? "" : nodes[0].getDisplayName() // NOI18N
                        }
                    )
                );
            } else {
                setName(
                    java.text.MessageFormat.format(
                        NbBundle.getBundle(NbNodeOperation.class).getString("CTL_FMT_LocalProperties"),
                        new Object[] {
                            new Integer (nodes.length),
                            nodes.length == 0 ? "" : nodes[0].getDisplayName() // NOI18N
                        }
                    )
                );
            }
        }

        /** Reshapes the window so that is is sized according to its preferredSize
        * and places it into the center of the screen
        *
        public void center() {
          // standard way how to place the dialog to the center of the screen
          pack();
          Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
          Dimension dialogSize = getSize();
          setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }*/

        public Dimension getPreferredSize () {
            return new Dimension (250, 400);
        }

        /** Serialize this property sheet */
        public void writeExternal (ObjectOutput out)
        throws IOException {
            super.writeExternal(out);
            out.writeObject(new Boolean(global));
        }

        /** Deserialize this property sheet. */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal(in);
            global = ((Boolean)in.readObject()).booleanValue();
            // start global listening if needed, but wait until
            // deserialization is done (ExplorerManager is uses
            // post-deserialization validating too, so we are forced
            // to use it)
            ((ObjectInputStream)in).registerValidation(
                new ObjectInputValidation () {
                    public void validateObject () {
                        updateGlobalListening(false);
                    }
                }, 0
            );
        }

        /** Resolve to singleton instance, if needed. */
        public Object readResolve ()
        throws ObjectStreamException {
            return global ? getDefault() : this;
        }

        /** Helper, listener variable must be initialized before
        * calling this */
        private void updateGlobalListening (boolean activateNow) {
            if (global) {
                TopComponent.getRegistry().addPropertyChangeListener(
                    WeakListener.propertyChange(listener, TopComponent.getRegistry ())
                );
                if (activateNow)
                    listener.activate ();
            }
        }

        /** Change listener to changes in selected nodes. And also
        * nodes listener to listen to global changes of the nodes.
        */
        private class Listener extends Object
            implements PropertyChangeListener {
            public void propertyChange (PropertyChangeEvent ev) {
                if (ev.getPropertyName ().equals (ExplorerManager.PROP_SELECTED_NODES)) {
                    //updateTitle();
                } else if (ev.getPropertyName ().equals (TopComponent.Registry.PROP_ACTIVATED_NODES)) {
                    try {
                        ExplorerManager man = getExplorerManager ();
                        Node[] arr = (Node[]) ev.getNewValue();
                        if (arr.length > 0) {
                            man.setRootContext (NodeOp.findRoot (arr[0]));
                        }
                        man.setSelectedNodes (arr);
                        updateTitle();
                    } catch (PropertyVetoException e) {
                        // ignore it
                    }
                }
            }

            public void activate () {
                try {
                    ExplorerManager man = getExplorerManager ();
                    Node[] arr = TopComponent.getRegistry ().getActivatedNodes();
                    if (arr.length > 0) {
                        man.setRootContext (NodeOp.findRoot (arr[0]));
                    }
                    man.setSelectedNodes (arr);
                } catch (PropertyVetoException e) {
                    // ignore it
                }
            }

        }

    }
}

/*
 * Log
 *  37   Gandalf   1.36        1/13/00  Jaroslav Tulach I18N
 *  36   Gandalf   1.35        1/7/00   David Simonek   global and local 
 *       property sheets now have different titles
 *  35   Gandalf   1.34        12/21/99 David Simonek   shared property sheet 
 *       behaves like real singleton after deserialization
 *  34   Gandalf   1.33        12/17/99 David Simonek   #4861
 *  33   Gandalf   1.32        12/6/99  David Simonek   explore() method updated
 *       (toolbar removed from the view)  
 *  32   Gandalf   1.31        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  31   Gandalf   1.30        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  30   Gandalf   1.29        11/3/99  David Simonek   name change bugfix
 *  29   Gandalf   1.28        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  28   Gandalf   1.27        8/9/99   David Simonek   request focus added to 
 *       showProperties
 *  27   Gandalf   1.26        8/1/99   David Simonek   
 *  26   Gandalf   1.25        7/28/99  David Simonek   workspace serialization 
 *       bugfixes
 *  25   Gandalf   1.24        7/11/99  David Simonek   window system change...
 *  24   Gandalf   1.23        7/2/99   Jesse Glick     Bugfix--now uses 
 *       ExplorerPanel's HelpCtx.
 *  23   Gandalf   1.22        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  22   Gandalf   1.21        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  21   Gandalf   1.20        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  20   Gandalf   1.19        4/2/99   Jaroslav Tulach global properites.
 *  19   Gandalf   1.18        3/29/99  Ian Formanek    Removed obsoleted 
 *       imports of ButtonBar
 *  18   Gandalf   1.17        3/26/99  Jaroslav Tulach 
 *  17   Gandalf   1.16        3/26/99  Jaroslav Tulach 
 *  16   Gandalf   1.15        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  15   Gandalf   1.14        3/18/99  Ian Formanek    Removed obsoleted import
 *  14   Gandalf   1.13        3/18/99  David Simonek   modes...
 *  13   Gandalf   1.12        3/9/99   Jaroslav Tulach ButtonBar  
 *  12   Gandalf   1.11        3/9/99   Ian Formanek    Reflecting ExplorerPanel
 *       using BorderLayout by default
 *  11   Gandalf   1.10        2/27/99  Jaroslav Tulach Shortcut changed to 
 *       Keymap
 *  10   Gandalf   1.9         2/17/99  David Simonek   setRequestedSize method 
 *       added to the window system  getDefaultMode added to the TopComponent
 *  9    Gandalf   1.8         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  8    Gandalf   1.7         2/8/99   Jaroslav Tulach Pernamently updatable 
 *       properties
 *  7    Gandalf   1.6         2/5/99   Jaroslav Tulach 
 *  6    Gandalf   1.5         2/4/99   Jaroslav Tulach Properties and explorer
 *  5    Gandalf   1.4         1/20/99  Petr Hamernik   
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
 *       ide.loaders.*
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
