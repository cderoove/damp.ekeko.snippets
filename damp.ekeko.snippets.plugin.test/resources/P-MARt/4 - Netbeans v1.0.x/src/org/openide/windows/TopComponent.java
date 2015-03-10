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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.*;
import java.beans.*;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Externalizable;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.undo.UndoableEdit;
import javax.swing.FocusManager;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;

import org.openide.*;
import org.openide.awt.UndoRedo;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.HelpCtx;

/** Embeddable visual component to be displayed in the IDE.
 * This is the basic unit of display in the IDE--windows should not be
 * created directly, but rather use this class.
 * A top component may correspond to a single window, but may also
 * be a tab (e.g.) in a window. It may be docked or undocked,
 * have selected nodes, supply actions, etc.
 *
 * @author Jaroslav Tulach, Petr Hamernik, Jan Jancura
 */
public class TopComponent extends JComponent implements Externalizable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -3022538025284122942L;

    /** instance of registry */
    private static Registry registry;

    /** Behavior in which a top component closed (by the user) in one workspace
    * will be removed from <em>every</em> workspace.
    * Also, {@link #close} is called.
    * This is appropriate for top components such as Editor panes which
    * the user expects to really close (and prompt to save) when closed
    * in any workspace.
    */
    public static final int CLOSE_EACH = 0;
    /** Behavior in which a top component closed (by the user) in one workspace
    * may be left in other workspaces.
    * Only when the last remaining manifestation in any workspace is closed
    * will the object be deleted using {@link #close}.
    * Appropriate for components containing no user data, for which closing
    * the component is only likely to result from the user's wanting to remove
    * it from active view (on the current workspace).
    */
    public static final int CLOSE_LAST = 1;

    /** a set of actions of this component */
    private static SystemAction[] systemActions;

    /** Listener to the data object's node or null */
    private NodeName nodeName;

    /** manager for the component */
    private WindowManager.Component manager;

    /** constant for desired close operation */
    private int closeOperation = CLOSE_LAST;

    /** identification of serialization version
    * Used in CloneableTopComponent readObject method.
    */
    short serialVersion = 1;

    /** Create a top component.
    */
    public TopComponent () {
        FocusMan.install();
        enableEvents (java.awt.AWTEvent.KEY_EVENT_MASK);

        // there is no reason why a top component should have a focus
        // => let's disable it
        setRequestFocusEnabled (false);
        // request creating of our manager - it's here to avoid
        // problems with recreating the connections between top components
        // and their managers during deserialization
        getManager();
    }

    /** Create a top component associated with a data object.
    * Currently the data object is used to set the component's name
    * (which will be updated according to the object's node delegate) by
    * installing NodeName inner class and attaching it to the node delegate.
    * 
    * @param obj the data object
    */
    public TopComponent (DataObject obj) {
        this ();
        Node n = obj.getNodeDelegate ();

        nodeName = new NodeName (this);
        nodeName.attach (n);
    }

    /** Getter for class that allows obtaining of information about components.
    * It allows to find out which component is selected, which nodes are 
    * currently or has been activated and list of all components.
    * 
    * @return the registry of components
    */
    public static final Registry getRegistry () {
        if (registry == null) {
            registry = TopManager.getDefault().getWindowManager().
                       componentRegistry();
        }
        return registry;
    }

    /** Get the set of activated nodes in this component.
    * @return the activated nodes for this component
    */
    public final Node[] getActivatedNodes () {
        return getManager ().getActivatedNodes ();
    }

    /** Set the set of activated nodes in this component.
    * @param nodes activated nodes for this component
    */
    public final void setActivatedNodes (Node[] nodes) {
        getManager ().setActivatedNodes (nodes);
        firePropertyChange ("activatedNodes", null, null); // NOI18N
    }

    /** Get the undo/redo support for this component.
    * The default implementation returns a dummy support that cannot
    * undo anything.
    *
    * @return undoable edit for this component
    */
    public UndoRedo getUndoRedo () {
        return UndoRedo.NONE;
    }

    /** Show the component on current workspace.
    * Note that this method only makes it visible, but does not
    * give it focus. Implemented via call to open(null).
    * @see #requestFocus
    */
    public void open () {
        open(null);
    }

    /** Show the component on given workspace. If given workspace is
    * not active, component will be shown only after given workspace
    * will become visible.
    * Note that this method only makes it visible, but does not
    * give it focus.
    * @param workspace Workspace on which component should be opened.
    * Parameter can be null -> means current workspace.
    * @see #requestFocus
    */
    public void open (Workspace workspace) {
        getManager().open(workspace);
    }

    /** Finds out if this top component is opened at least on one workspace.
    * @return true if given top component is opened on at least
    * one workspace, false otherwise */
    public final boolean isOpened () {
        return getManager().whereOpened().size() > 0;
    }

    /** Finds out whether this top component is opened or not on specified
    * workspace.
    * @return true if given top component is opened on given workspace,
    * false otherwise */
    public final boolean isOpened (Workspace workspace) {
        return getManager().whereOpened().contains(workspace);
    }

    /** Closes the top component on current workspace.
    * First asks canClose() method to see if it is
    * possible to close now. If canClose() returns false, component will not
    * be closed.
    * Semantics of this method depends on top component's closeOperation
    * state. If closeOperation is set to CLOSE_LAST (default), top component
    * will be closed only on current workspace. If it is set to
    * CLOSE_EACH, if will be closed on all workspaces at once.
    *
    * @return true if top component was succesfully closed, false if 
    * top component for some reason refused to close.
    */
    public final boolean close () {
        return close(
                   TopManager.getDefault().getWindowManager().getCurrentWorkspace()
               );
    }

    /** Closes the top component on given workspace, if closeOperation
    * is set to CLOSE_LAST. If it is set to CLOSE_EACH, given parameter 
    * will be ignored and component will be closed on all workspaces
    * at once.
    *
    * @param workspace Workspace on which component should be closed.
    * @return true if top component was succesfully closed, false if 
    * top component for some reason refused to close.
    */
    public final boolean close (Workspace workspace) {
        Set whereOpened = getManager().whereOpened();
        // don't close multiple times
        if ((closeOperation != CLOSE_EACH) && !whereOpened.contains(workspace))
            return true;
        boolean result = false;
        switch (closeOperation) {
        case CLOSE_LAST:
            result = canClose(workspace, whereOpened.size() == 1);
            break;
        case CLOSE_EACH:
            result = canClose(null, true);
            break;
        }
        if (result)
            getManager().close(workspace);
        return result;
    }


    /** This method is called when top component is about to close.
    * Allows subclasses to decide if top component is ready for closing
    * or not.<br>
    * Default implementation always return true.
    * 
    * @param workspace the workspace on which we are about to close or
    *                  null which means that component will be closed
    *                  on all workspaces where it is opened (CLOSE_EACH mode)
    * @param last true if this is last workspace where top component is
    *             opened, false otherwise. If close operation is set to
    *             CLOSE_EACH, then this param is always true
    * @return true if top component is ready to close, false otherwise.
    */
    public boolean canClose (Workspace workspace, boolean last) {
        return true;
    }

    /** Get a list of modes which are allowed for this component.
    * The component can be displayed only in the indicated modes.
    * The current mode should <em>not</em> be included.
    * <p>Subclasses are encouraged to override this method to allow
    * only modes appropriate to the application's needs.
    * The default implementation treats all modes as allowed except the current
    * mode.
    *
    * @param w workspace to check allowed modes on
    * @return an immutable list of allowed modes
    */
    // PENDING - we will priobably remove it
    /*
    public java.util.List getAllowedModes (Workspace w) {
      java.util.List availModes = w.getModes();
      availModes.remove(w.findMode (this));
      return availModes;
}*/

    /** Get the system actions which will appear in
    * the popup menu of this component.
    * <p>Subclasses are encouraged to override this method to specify
    * their own sets of actions.
    * <p>Remember to call the super method when overriding and add your actions
    * to the superclass' ones (in some order),?
    * because the default implementation provides support for standard
    * component actions like save, close, and clone.
    * @return system actions for this component
    */
    public SystemAction[] getSystemActions () {
        // lazy inicialization
        if (systemActions == null)
            systemActions = new SystemAction[] {
                                SystemAction.get(SaveAction.class),
                                SystemAction.get(CloneViewAction.class),
                                null,
                                SystemAction.get(CloseViewAction.class)
                            };
        return systemActions;
    }

    /** Set the close mode for the component.
    * Note that if the {@link #close} is called (rather than a user-initiated close action), the component (and maybe its
    * window) is always removed from all workspaces.<P>
    *
    * @param mode one of {@link #CLOSE_EACH} or {@link #CLOSE_LAST}
    * @throws IllegalArgumentException if an unrecognized close mode was supplied
    */
    public final void setCloseOperation (final int closeOperation) {
        if ((closeOperation != CLOSE_EACH) && (closeOperation != CLOSE_LAST))
            throw new IllegalArgumentException(
                NbBundle.getBundle(TopComponent.class).getString("EXC_UnknownOperation")
            );
        if (this.closeOperation == closeOperation) return;
        this.closeOperation = closeOperation;
        firePropertyChange ("closeOperation", null, null); // NOI18N
    }

    /** Get the current close mode for this component.
    * @return one of {@link #CLOSE_EACH} or {@link #CLOSE_LAST}
    */
    public final int getCloseOperation () {
        return closeOperation;
    }

    /** Called when this component is activated.
    * This happens when the parent window of this component gets focus
    * (and this component is the preferred one in it), <em>or</em> when
    * this component is selected in its window (and its window was already focussed).
    * Override this method to perform some special action on component activation:
    * typically, set performers for relevant actions.
    * Remember to call the super method.
    * The default implementation does nothing.
    */
    protected void componentActivated () {
    }

    /** Called when this component is deactivated.
    * This happens when the parent window of this component loses focus
    * (and this component is the preferred one in the parent),
    * <em>or</em> when this component loses preference in the parent window
    * (and the parent window is focussed).
    * Override this method to perform some special action on component deactivation:
    * typically, unset performers for relevant actions.
    * Remember to call the super method.
    * The default implementation does nothing.
    */
    protected void componentDeactivated () {
    }

    // [PENDING] would a call to setMode in the constructor suffice?
    /** Get the default mode for this component.
    * This is the mode in which it will be opened for the first time.
    * The default implementation returns {@link #SINGLE}.
    *
    * @return the default mode
    */
    // Can we get rid of this?
    //  protected Mode getDefaultMode () {
    //    return SINGLE;
    //  }

    /** Request focus for the window holding this top component.
    * Also makes the component preferred in that window.
    * The component will <em>not</em> be automatically {@link #open opened} first
    * if it is not already.
    */
    public void requestFocus () {
        //System.out.println("RF on tc " + getName() + " called."); // NOI18N
        getManager().requestFocus();
        super.requestFocus();
    }

    /** Set the name of this top component.
    * The default implementation just notifies the window manager.
    * @param displayName the new display name
    */
    public void setName (final String name) {
        if ((name != null) && (name.equals(getName())))
            return;
        // This should also be firing a property change event:
        super.setName(name);
        getManager().nameChanged();
    }

    /** Set the icon of this top component.
    * The icon will be used for
    * the component's representation on the screen, e.g. in a multiwindow's tab.
    * The default implementation just notifies the window manager.
    * @param icon New components' icon.
    */
    public void setIcon (final Image icon) {
        getManager().setIcon(icon);
        firePropertyChange ("icon", null, null); // NOI18N
    }

    /** @return The icon of the top component */
    public Image getIcon () {
        return getManager().getIcon();
    }

    /** Set the (preferred) size of the top component.
    * Use this method to dynamically change the size of a top component
    * (the container should change its size to match).
    * It is possible the new size will not be honored if it is impossible to
    * fit it.
    * @param size new size of this top component
    */
    // use Workspace.findMode (TopComponent).setBounds (..)
    //  public void setRequestedSize (final Dimension size) {
    //    getManager().setRequestedSize(size);
    //  }

    /** Get the help context for this component.
    * Subclasses should generally override this to return specific help.
    * @return the help context
    */
    public org.openide.util.HelpCtx getHelpCtx () {
        return new HelpCtx (TopComponent.class);
    }

    /** Getter for manager for this component. This manager allows to
    * control where is the component shown can be used to destroy and show the
    * component, etc.
    */
    final WindowManager.Component getManager () {
        if (manager == null) {
            synchronized (this) {
                if (manager == null) {
                    manager = TopManager.getDefault ().getWindowManager ().createTopComponentManager (
                                  this
                              );
                }
            }
        }
        return manager;
    }

    /** Serialize this top component.
    * Subclasses wishing to store state must call the super method, then write to the stream.
    * @param out the stream to serialize to
    */
    public void writeExternal (ObjectOutput out)
    throws IOException {
        out.writeObject(new Short (serialVersion));

        out.writeInt (closeOperation);
        out.writeObject (getName());
        out.writeObject (getToolTipText());

        Node.Handle h = nodeName == null ? null : nodeName.node.getHandle ();
        out.writeObject(h);
    }

    /** Deserialize this top component.
    * Subclasses wishing to store state must call the super method, then read from the stream.
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject ();
        if (firstObject instanceof Integer) {
            // backward compatibility read
            serialVersion = 0;

            closeOperation = ((Integer)firstObject).intValue();
            DataObject obj = (DataObject)in.readObject();

            super.setName((String)in.readObject());
            setToolTipText((String)in.readObject());

            // initialize the connection to a data object
            if (obj != null) {
                nodeName = new NodeName (this);
                nodeName.attach (obj.getNodeDelegate ());
            }
        } else {
            // new serialization
            serialVersion = ((Short)firstObject).shortValue ();

            closeOperation = in.readInt ();
            super.setName ((String)in.readObject ());
            setToolTipText ((String)in.readObject ());

            Node.Handle h = (Node.Handle)in.readObject ();
            if (h != null) {
                Node n = h.getNode ();
                nodeName = new NodeName (this);
                nodeName.attach (n);
            }
        }
    }

    /** Delegates instance of replacer class to be serialized instead
    * of top component itself. Replacer class calls writeExternal and
    * constructor, readExternal and readResolve methods properly, so
    8 any top component can behave like any other externalizable object.
    * Subclasses can override this method to perform their
    * serialization differentrly */
    protected Object writeReplace () throws ObjectStreamException {
        return new Replacer(this);
    }

    /** Each top component that wishes to be cloned should implement
    * this interface, so CloneAction can check it and call the cloneComponent
    * method.
    */
    public static interface Cloneable {
        /** Creates a clone of this component
        * @return cloned component.
        */
        public TopComponent cloneComponent ();
    }

    /** This class provides the connection between the node name and
    * a name of the component.
    */
    public static class NodeName extends NodeAdapter {
        /** weak reference to the top component */
        private transient Reference top;
        /** node we are attached to or null */
        private transient Node node;

        /** Constructs new name adapter that
        * can be attached to any node and will listen on changes 
        * of its display name and modify the name of the component.
        *
        * @param top top compoonent to modify its name
        */
        public NodeName (TopComponent top) {
            this.top = new WeakReference (top);
        }

        /** Attaches itself to a given node.
        */
        final void attach (Node n) {
            TopComponent top = (TopComponent)this.top.get ();
            if (top != null) {
                synchronized (top) {
                    // ok no change
                    if (n == node) return;

                    // change the node we are attached to
                    if (node != null) {
                        node.removeNodeListener (this);
                    }
                    node = n;

                    if (n != null) {
                        n.addNodeListener (this);
                        top.setActivatedNodes (new Node[] { n });
                        top.setName (n.getDisplayName ());
                    }
                }
            }
        }


        /** Listens to Node.PROP_DISPLAY_NAME.
        */
        public void propertyChange(PropertyChangeEvent ev) {
            TopComponent top = (TopComponent)this.top.get ();
            if (top == null) {
                // stop listening if top component no longer exists
                if (ev.getSource () instanceof Node) {
                    Node n = (Node)ev.getSource ();
                    n.removeNodeListener (this);
                }
                return;
            }

            // ensure we are attached
            attach (node);

            if (ev.getPropertyName ().equals (Node.PROP_DISPLAY_NAME)) {
                top.setName (node.getDisplayName());
            }
        }
    } // end of NodeName


    /** Our focus manager to catch keys.
    */
    static class FocusMan extends FocusManager {
        /** manager to deletage operations to */
        private FocusManager delegate;

        /** @param d delegate focus manager
        */
        private FocusMan (FocusManager d) {
            delegate = d;
        }

        /** Checks whether the calling thread uses this focus manager.
        * If no, installs new one.
        */
        public static void install () {
            FocusManager fm = getCurrentManager ();
            if (fm == null || fm.getClass () != FocusMan.class) {
                setCurrentManager (new FocusMan (fm));
            }
        }

        /** Processes the key */
        public void processKeyEvent(
            java.awt.Component focusedComponent,
            final java.awt.event.KeyEvent anEvent
        ) {
            if (ShortcutManager.isTransmodalAction (KeyStroke.getKeyStrokeForEvent (anEvent))) {
                //System.err.println ("Got a transmodal action...");
            } else {
                // Bug #500 fixed here. and #2482 too.
                java.awt.Component c = focusedComponent;
                java.awt.Component mw = TopManager.getDefault ().getWindowManager ().getMainWindow ();

                java.awt.Window w = SwingUtilities.windowForComponent (c);
                if (w instanceof java.awt.Dialog) {
                    java.awt.Dialog d = (java.awt.Dialog)w;
                    if (d.isModal ()) {
                        return;
                    }
                }
            }
            /*
                    while (true) {
                      System.err.println ("Considering: " + c);
                      if ((c instanceof TopComponent) || (c == mw)) {
                        //System.err.println ("Fine, MW or TC.");
                        break;
                      } else if (c instanceof java.awt.Dialog) {
                        //System.err.println ("Some window, maybe dialog; skip.");
                        return;
                      } else if (c instanceof java.awt.Window) {
                        // other top window, proceed bugfix of #2482
                        break;
                      } else {
                        c = c.getParent ();
                      }
                    }
                  }
            */
            // delegates to the original focus manager
            delegate.processKeyEvent (focusedComponent, anEvent);
            //System.err.println ("Was consumed by delegate: " + anEvent.isConsumed ());
            if (!anEvent.isConsumed ()) {
                process (anEvent, focusedComponent);
            }
        }

        /** Delegates to old manager */
        public void focusNextComponent(java.awt.Component aComponent) {
            delegate.focusNextComponent (aComponent);
        }

        /** Delegates to old manager */
        public void focusPreviousComponent(java.awt.Component aComponent) {
            delegate.focusPreviousComponent (aComponent);
        }

        /** HashSet of posted events. Ensures that no event will be posted
        * twice. (KeyEvent, KeyEvent)
        * @associates KeyEvent
        */
        private static HashSet posted = new HashSet ();

        /** Processes the event by posting it to shortcut processor.
        * @param ev the event to process
        */
        static void process (final KeyEvent ev, final java.awt.Component comp) {
            // test if ev is not posted
            //System.err.println ("Maybe processing..");
            if (ev.getID () != KeyEvent.KEY_PRESSED || posted.contains (ev)) return;

            // register that this event has been posted
            posted.add (ev);
            //System.err.println ("Posting...");

            // if the event is not consumed, then plans code
            // that will be run after the event is processed by component
            // an will try to find the right shortcut
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                //System.err.println ("Running later; was consumed: " + ev.isConsumed ());
                                                if (!ev.isConsumed ()) {
                                                    // process only if the event is not consumed
                                                    KeyStroke key = KeyStroke.getKeyStrokeForEvent (ev);

                                                    java.awt.Component source = comp; // first guess
                                                    // For menu items, the first guess does not work well--really we want
                                                    // to examine the currently open menu. Note special treatment of submenus
                                                    // which are open, but no items selected--ignore, and use the JMenu instead.
                                                    javax.swing.MenuElement[] menuitems =
                                                        javax.swing.MenuSelectionManager.defaultManager ().getSelectedPath ();
                                                    if (menuitems != null) {
                                                        for (int idx = menuitems.length - 1; idx >= 0; idx--) {
                                                            java.awt.Component menuitem = menuitems[idx].getComponent ();
                                                            if (! (menuitem instanceof javax.swing.JPopupMenu) &&
                                                                    (menuitem != null)) {
                                                                source = menuitem;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    // Provide a reasonably useful action event that identifies what was focused
                                                    // when the key was pressed, as well as what keystroke ran the action.
                                                    java.awt.event.ActionEvent aev = new java.awt.event.ActionEvent
                                                                                     (source, java.awt.event.ActionEvent.ACTION_PERFORMED, org.openide.util.Utilities.keyToString (key));

                                                    if (ShortcutManager.processKeyStroke (key, aev)) {
                                                        ev.consume ();
                                                    }
                                                }
                                                posted.remove (ev);
                                            }
                                        });
        }
    }
    /* Focus watcher hack.
      static {
        class T implements Runnable {
          public void run () {
            TopComponent tc = getRegistry ().getActivated ();
            java.awt.Component c = SwingUtilities.findFocusOwner (tc);
            System.out.println("TopComponent: " + tc);
            System.out.println("Focus owner : " + c);
            org.openide.util.RequestProcessor.postRequest (this, 2000);
          }
        }
        new T ().run ();
      }
    */
    /** Registry of all top components.
    * There is one instance that can be obtained via {@link TopComponent#getRegistry}
    * and it permits listening to the currently selected element, and to
    * the activated nodes assigned to it.
    */
    public static interface Registry {
        /** Name of property for the set of opened components. */
        public static final String PROP_OPENED = "opened"; // NOI18N
        /** Name of property for the selected top component. */
        public static final String PROP_ACTIVATED = "activated"; // NOI18N
        /** Name of property for currently selected nodes. */
        public static final String PROP_CURRENT_NODES = "currentNodes"; // NOI18N
        /** Name of property for lastly activated nodes nodes. */
        public static final String PROP_ACTIVATED_NODES = "activatedNodes"; // NOI18N

        /** Get all opened componets in the system.
        *
        * @return immutable set of {@link TopComponent}s
        */
        public Set getOpened ();

        /** Get the currently selected element.
        * @return the selected top component, or <CODE>null</CODE> if there is none
        */
        public TopComponent getActivated ();

        /** Getter for the currently selected nodes.
        * @return array of nodes or null if no component activated or it returns
        *   null from getActivatedNodes ().
        */
        public Node[] getCurrentNodes ();

        /** Getter for the lastly activated nodes. Comparing
        * to previous method it always remembers the selected nodes
        * of the last component that had ones.
        *
        * @return array of nodes (not null)
        */
        public Node[] getActivatedNodes ();

        /** Add a property change listener.
        * @param l the listener to add
        */
        public void addPropertyChangeListener (PropertyChangeListener l);

        /** Remove a property change listener.
        * @param l the listener to remove
        */
        public void removePropertyChangeListener (PropertyChangeListener l);
    }

    /** Instance of this class is serialized instead of TopComponent itself.
    * Emulates behaviour of serialization of externalizable objects
    * to keep TopComponent serialization compatible with previous versions. */
    private static final class Replacer implements Serializable {
        /** SUID */
        static final long serialVersionUID=-8897067133215740572L;

        /** Asociation with top component which is to be serialized using
        * this replacer */
        transient TopComponent tc;

        public Replacer (TopComponent tc) {
            this.tc = tc;
        }

        private void writeObject (ObjectOutputStream oos)
        throws IOException, ClassNotFoundException {
            // write the name of the top component first
            oos.writeObject(tc.getClass().getName());
            // and now let top component to serialize itself
            tc.writeExternal(oos);
        }

        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            // read the name of top component's class, instantiate it
            // and read its attributes from the stream
            String name = (String)ois.readObject();
            name = org.openide.util.Utilities.translate(name);
            try {
                Class tcClass = Class.forName(
                                    name,
                                    true,
                                    TopManager.getDefault().systemClassLoader()
                                );
                tc = (TopComponent)tcClass.newInstance();
                tc.readExternal(ois);
                // call readResolve() if present and use resolved value
                Method resolveMethod = findReadResolveMethod(tcClass);
                if (resolveMethod != null) {
                    // check exceptions clause
                    Class[] result = resolveMethod.getExceptionTypes();
                    if ((result.length == 1) &&
                            ObjectStreamException.class.equals(result[0])) {
                        // returned value type
                        if (Object.class.equals(resolveMethod.getReturnType())) {
                            // make readResolve accessible (it can have any access modifier)
                            resolveMethod.setAccessible(true);
                            // invoke resolve method and accept its result
                            try {
                                tc = (TopComponent)resolveMethod.invoke(tc, new Class[0]);
                            } finally {
                                resolveMethod.setAccessible(false);
                            }
                        }
                    }
                }
            } catch (Exception exc) {
                // turn all troubles into IOException
                exc.printStackTrace();
                throw new IOException();
            }
        }

        /** Resolve to original top component instance */
        private Object readResolve () throws ObjectStreamException {
            return tc;
        }

        /** Tries to find readResolve method in given class. Finds
        * both public and non-public occurences of the method and
        * searches also in superclasses */
        private static Method findReadResolveMethod (Class clazz) {
            Method result = null;
            // first try public occurences
            try {
                result = clazz.getMethod("readResolve", new Class[0]); // NOI18N
            } catch (NoSuchMethodException exc) {
                // public readResolve does not exist
            }
            // now try non-public occurences; search also in superclasses
            for (Class i = clazz; i != null; i = i.getSuperclass()) {
                try {
                    result = i.getDeclaredMethod("readResolve", new Class[0]); // NOI18N
                    // get out of cycle if method found
                    break;
                } catch (NoSuchMethodException exc) {
                    // readResolve does not exist in current class
                }
            }
            return result;
        }

    } // end of Replacer inner class

}

/*
 * Log
 *  48   Gandalf   1.47        2/15/00  David Simonek   bug after unmount of 
 *       open editor files fixed (hopefully :-)
 *  47   Gandalf   1.46        1/16/00  David Simonek   finding of readResolve 
 *       method fixed in replacer object
 *  46   Gandalf   1.45        1/15/00  Jaroslav Tulach SUID
 *  45   Gandalf   1.44        1/13/00  David Simonek   i18n
 *  44   Gandalf   1.43        1/12/00  Jesse Glick     All setters now fire 
 *       JComponent property changes.
 *  43   Gandalf   1.42        12/21/99 David Simonek   #5066
 *  42   Gandalf   1.41        12/8/99  Jaroslav Tulach TopComponent enhanced.
 *  41   Gandalf   1.40        12/7/99  David Simonek   serialization changed, 
 *       TopComponent now replaceable,  class Replacer is serialized instead of 
 *       TopComponent itself by default
 *  40   Gandalf   1.39        11/30/99 David Simonek   isOpened () and isOpened
 *       (Workspace) convenience methods added
 *  39   Gandalf   1.38        11/5/99  Jesse Glick     Rearranged context help 
 *       for ExplorerPanel vs. TopComponent.
 *  38   Gandalf   1.37        11/3/99  David Simonek   completely rewritten 
 *       serialization of windowing system...
 *  37   Gandalf   1.36        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  36   Gandalf   1.35        10/7/99  David Simonek   request focus modified
 *  35   Gandalf   1.34        10/6/99  David Simonek   more robust 
 *       serialization of window system (especially editor TCs)
 *  34   Gandalf   1.33        9/6/99   Jaroslav Tulach 
 *  33   Gandalf   1.32        8/17/99  Ian Formanek    if TopComponent has 
 *       CLOSE_EACH the close method does not check if it is opened on the 
 *       workspace on which it is being closed
 *  32   Gandalf   1.31        8/9/99   David Simonek   
 *  31   Gandalf   1.30        7/29/99  David Simonek   #2900 bugfix (by Jesse)
 *  30   Gandalf   1.29        7/28/99  David Simonek   canClose() parameters 
 *       changed
 *  29   Gandalf   1.28        7/20/99  Jesse Glick     Context help (smarter 
 *       guess).
 *  28   Gandalf   1.27        7/16/99  Jesse Glick     Processing keystrokes 
 *       with real ActionEvents, handling dialogs better too.
 *  27   Gandalf   1.26        7/11/99  David Simonek   window system change...
 *  26   Gandalf   1.25        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  25   Gandalf   1.24        5/11/99  David Simonek   changes to made window 
 *       system correctly serializable
 *  24   Gandalf   1.23        4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  23   Gandalf   1.22        4/8/99   David Simonek   debigging comments 
 *       removed...
 *  22   Gandalf   1.21        3/31/99  David Simonek   ugly ugly ugly 
 *       requestFocus bungs fixed
 *  21   Gandalf   1.20        3/30/99  Jesse Glick     [JavaDoc]
 *  20   Gandalf   1.19        3/30/99  Jesse Glick     [JavaDoc]
 *  19   Gandalf   1.18        3/30/99  Jesse Glick     [JavaDoc]
 *  18   Gandalf   1.17        3/29/99  Jesse Glick     [JavaDoc]
 *  17   Gandalf   1.16        3/29/99  Jesse Glick     [JavaDoc]
 *  16   Gandalf   1.15        3/29/99  Jesse Glick     [JavaDoc]
 *  15   Gandalf   1.14        3/27/99  David Simonek   
 *  14   Gandalf   1.13        3/25/99  David Simonek   changes in window 
 *       system, initial positions, bugfixes
 *  13   Gandalf   1.12        3/22/99  David Simonek   
 *  12   Gandalf   1.11        3/19/99  David Simonek   
 *  11   Gandalf   1.10        3/18/99  David Simonek   activated nodes bug 
 *       fixed
 *  10   Gandalf   1.9         3/18/99  David Simonek   changed window system - 
 *       support for modules
 *  9    Gandalf   1.8         3/14/99  Ian Formanek    Temporarily replaced 
 *       with older version to make it compilable
 *  8    Gandalf   1.7         3/14/99  David Simonek   
 *  7    Gandalf   1.6         3/10/99  Jaroslav Tulach UndoRedo
 *  6    Gandalf   1.5         2/17/99  David Simonek   setRequestedSize method 
 *       added to the window system  getDefaultMode added to the TopComponent
 *  5    Gandalf   1.4         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  4    Gandalf   1.3         1/26/99  David Simonek   setName optimized a 
 *       little bit
 *  3    Gandalf   1.2         1/8/99   Jan Jancura     
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 Jan Jancura     componentActivated / Deactivated added
 *  0    Tuborg    0.21        --/--/98 Petr Hamernik   request focus added
 */
