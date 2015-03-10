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

package org.openide.nodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.datatransfer.*;
import org.openide.util.actions.SystemAction;

/** A basic implementation of a node.
*
* <p>It simplifies creation of the display name, based on a message
* format and the system name. It also simplifies working with icons:
* one need only specify the base name and all icons will be loaded
* when needed. Other common requirements are handled as well.
*
* @author Jaroslav Tulach */
public class AbstractNode extends Node {
    /** messages to create a resource identification for each type of
    * icon from the base name for the icon.
    */
    private static final MessageFormat[] icons = {
        // color 16x16
        new MessageFormat ("{0}.gif"), // NOI18N
        // color 32x32
        new MessageFormat ("{0}32.gif"), // NOI18N
        // mono 16x16
        new MessageFormat ("{0}.gif"), // NOI18N
        // mono 32x32
        new MessageFormat ("{0}32.gif"), // NOI18N
        // opened color 16x16
        new MessageFormat ("{0}Open.gif"), // NOI18N
        // opened color 32x32
        new MessageFormat ("{0}Open32.gif"), // NOI18N
        // opened mono 16x16
        new MessageFormat ("{0}Open.gif"), // NOI18N
        // opened mono 32x32
        new MessageFormat ("{0}Open32.gif"), // NOI18N
    };
    /** To index normal icon from previous array use
    *  + ICON_BASE.
    */
    private static final int ICON_BASE = -1;
    /** for indexing opened icons */
    private static final int OPENED_ICON_BASE = 3;

    /** empty array of paste types */
    private static final PasteType[] NO_PASTE_TYPES = {};

    /** empty array of new types */
    private static final NewType[] NO_NEW_TYPES = {};

    /** empty array of property sets */
    private static final PropertySet[] NO_PROPERTY_SETS = {};

    /** Message format to use for creation of the display name.
    * It permits conversion of text from
    * {@link #getName} to the one sent to {@link #setDisplayName}. The format can take
    * one parameter, <code>{0}</code>, which will be filled by a value from <CODE>getName()</CODE>.
    *
    * <p>The default format just uses the simple name; subclasses may
    * change it, though it will not take effect until the next {@link #setName} call.
    *
    * <p>Can be set to <CODE>null</CODE>. Then there is no connection between the
    * name and display name; they may be independently modified.  */
    protected MessageFormat displayFormat;

    /** default icon base for all nodes */
    private static final String DEFAULT_ICON_BASE = "/org/openide/resources/defaultNode"; // NOI18N

    /** Resource base for icons, used as parameter to MessageFormat.format, so
    * it is stored as array  */
    private Object[] iconBase = { DEFAULT_ICON_BASE };
    /** array of cookies for this node */
    private CookieSet cookieSet;
    /** set of properties to use */
    private Sheet sheet;
    /** Actions for the node. They are used only for the pop-up menus
    * of this node.
    */
    protected SystemAction[] systemActions;
    /** default action */
    private SystemAction defaultAction;

    /** listener for changes in the sheet */
    private PropertyChangeListener sheetL = new PropertyChangeListener () {
                                                public void propertyChange (PropertyChangeEvent ev) {
                                                    AbstractNode.this.firePropertySetsChange (null, null);
                                                }
                                            };
    /** listener for changes in the cookie set */
    private ChangeListener cookieL = new ChangeListener () {
                                         public void stateChanged (ChangeEvent ev) {
                                             AbstractNode.this.fireCookieChange ();
                                         }
                                     };

    /** Create a new abstract node with a given child set.
    * @param children the children to use for this node
    */
    public AbstractNode(Children children) {
        super (children);
    }

    /** Clone the node. If the object implements {@link Cloneable},
    * that is used; otherwise a {@link FilterNode filter node}
    * is created.
    *
    * @return copy of this node
    */
    public Node cloneNode () {
        try {
            if (this instanceof Cloneable) {
                return (Node)clone ();
            }
        } catch (CloneNotSupportedException ex) {
        }
        return new FilterNode (this);
    }

    /** Set the system name. Fires a property change event.
    * Also may change the display name according to {@link #displayFormat}.
    *
    * @param s the new name
    */
    public void setName (String s) {
        super.setName (s);

        MessageFormat mf = displayFormat;
        if (mf != null) {
            setDisplayName (mf.format (new Object[] { s }));
        }
    }

    /** Change the icon.
    * One need only specify the base resource name;
    * the real name of the icon is obtained by the applying icon message
    * formats.
    *
    * <p>For example, for the base <code>/resource/MyIcon</code>, the
    * following images may be used according to the icon state and
    * {@link java.beans.BeanInfo#getIcon presentation type}:
    *
    * <ul><li><code>/resource/MyIcon.gif</code><li><code>/resource/MyIconOpen.gif</code>
    * <li><code>/resource/MyIcon32.gif</code><li><code>/resource/MyIconOpen32.gif</code></ul>
    *
    * <P>
    * This method may be used to dynamically switch between different sets
    * of icons for different configurations. If the set is changed,
    * an icon property change event is fired.
    *
    * @param base base resouce name */
    public void setIconBase (String base) {
        this.iconBase = new Object[] { base };
        fireIconChange ();
        fireOpenedIconChange ();
    }

    /** Find an icon for this node. Uses an {@link #setIconBase icon set}.
    *
    * @param type constants from {@link java.beans.BeanInfo}
    *
    * @return icon to use to represent the bean
    */
    public Image getIcon (int type) {
        return findIcon (type, ICON_BASE);
    }

    /** Finds an icon for this node when opened. This icon should represent the node
    * only when it is opened (when it can have children).
    *
    * @param type as in {@link #getIcon}
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon (int type) {
        return findIcon (type, OPENED_ICON_BASE);
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /** module test mode? if so, use repo classloader */
    private static Boolean moduleTestIcons = null;
    /** Tries to find the right icon for the iconbase.
    * @param type type of icon (from BeanInfo constants)
    * @param ib base where to scan in the array
    */
    private Image findIcon (int type, int ib) {
        ClassLoader loader = getClass ().getClassLoader ();

        TopManager top;
        if (
            //getClass () != AbstractNode.class &&
            (top = TopManager.getDefault ()) != null
        ) {
            if (moduleTestIcons == null)
                moduleTestIcons = Boolean.valueOf (System.getProperty ("netbeans.module.test")); // NOI18N
            // if looking for an icon for subclass => use class's classloader
            // if looking for AbstractNode => use systemClassLoader () because
            //    the icon is probably added by some module
            loader = moduleTestIcons.booleanValue () ? top.currentClassLoader () : top.systemClassLoader ();
        }

        String res = icons[type + ib].format (iconBase);
        Image im = IconManager.getIcon (res, loader);

        if (im != null) return im;

        // try the first icon
        res = icons[java.beans.BeanInfo.ICON_COLOR_16x16 + ib].format (iconBase);

        im = IconManager.getIcon (res, loader);

        if (im != null) return im;

        if (ib == OPENED_ICON_BASE) {
            // try closed icon also
            return findIcon (type, ICON_BASE);
        }

        // if still not found return default icon
        return IconManager.getDefaultIcon ();
    }

    /** Can this node be renamed?
    * @return <code>false</code>
    */
    public boolean canRename () {
        return false;
    }

    /** Can this node be destroyed?
    * @return <CODE>false</CODE>
    */
    public boolean canDestroy () {
        return false;
    }

    /** Set the set of properties.
    * A listener is attached to the provided sheet
    * and any change of the sheet is propagated to the node by
    * firing a {@link #PROP_PROPERTY_SETS} change event.
    *
    * @param s the sheet to use
    */
    protected final synchronized void setSheet (Sheet s) {
        if (sheet != null) {
            sheet.removePropertyChangeListener (sheetL);
        }

        s.addPropertyChangeListener (sheetL);
        sheet = s;

        firePropertySetsChange (null, null);
    }

    /** Initialize a default
    * property sheet; commonly overridden. If {@link #getSheet}
    * is called and there is not yet a sheet,
    * this method is called to allow a subclass
    * to specify its properties.
    * <P>
    * <em>Warning:</em> Do not call <code>getSheet</code> in this method.
    * <P>
    * The default implementation returns an empty sheet.
    *
    * @return the sheet with initialized values (never <code>null</code>)
    */
    protected Sheet createSheet () {
        return new Sheet ();
    }

    /** Get the current property sheet. If the sheet has been
    * previously set by a call to {@link #setSheet}, that sheet
    * is returned. Otherwise {@link #createSheet} is called.
    *
    * @return the sheet (never <code>null</code>)
    */
    protected final Sheet getSheet () {
        Sheet s = sheet;
        if (s != null) return s;
        synchronized (this) {
            if (sheet != null) return sheet;

            // sets empty sheet and adds a listener to it
            setSheet (createSheet ());
            return sheet;
        }
    }


    /** Get a list of property sets.
    *
    * @return the property sets for this node
    * @see #getSheet
    */
    public PropertySet[] getPropertySets () {
        Sheet s = getSheet ();
        if (s == null) return NO_PROPERTY_SETS;
        return s.toArray ();
    }

    /** Copy this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not copy
    */
    public Transferable clipboardCopy () throws IOException {
        return NodeTransfer.transferable (this, NodeTransfer.CLIPBOARD_COPY);
    }

    /** Cut this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not cut
    */
    public Transferable clipboardCut () throws IOException {
        return NodeTransfer.transferable (this, NodeTransfer.CLIPBOARD_CUT);
    }

    /**
    * This implementation only calls clipboardCopy supposing that 
    * copy to clipboard and copy by d'n'd are similar.
    *
    * @return transferable to represent this node during a drag
    * @exception IOException when the
    *    cut cannot be performed
    */
    public Transferable drag () throws IOException {
        return clipboardCopy ();
    }


    /** Can this node be copied?
    * @return <code>true</code>
    */
    public boolean canCopy () {
        return true;
    }

    /** Can this node be cut?
    * @return <code>false</code>
    */
    public boolean canCut () {
        return false;
    }

    /** Accumulate the paste types that this node can handle
    * for a given transferable.
    * <P>
    * The default implementation simply tests whether the transferable supports
    * intelligent pasting via {@link NodeTransfer#findPaste}, and if so, it obtains the paste types
    * from the {@link NodeTransfer.Paste transfer data} and inserts them into the set.
    * <p>Subclass implementations should typically call super (first or last) so that they
    * add to, rather than replace, a superclass's available paste types; especially as the
    * default implementation in <code>AbstractNode</code> is generally desirable to retain.
    *
    * @param t a transferable containing clipboard data
    * @param s a list of {@link PasteType}s that will have added to it all types
    *    valid for this node (ordered as they will be presented to the user)
    */
    protected void createPasteTypes (Transferable t, List s) {
        NodeTransfer.Paste p = NodeTransfer.findPaste (t);
        if (p != null) {
            // adds all its types into the set
            s.addAll (Arrays.asList (p.types (this)));
        }
    }

    /** Determine which paste operations are allowed when a given transferable is in the clipboard.
    * Subclasses should override {@link #createPasteTypes}.
    *
    * @param t the transferable in the clipboard
    * @return array of operations that are allowed
    */
    public final PasteType[] getPasteTypes (Transferable t) {
        List s = new LinkedList ();
        createPasteTypes (t, s);
        return (PasteType[])s.toArray (NO_PASTE_TYPES);
    }

    /** Default implementation that tries to delegate the implementation
    * to the createPasteTypes method. Simply calls the method and 
    * tries to take the first provided argument. Ignores the action
    * argument and index.
    *
    * @param t the transferable 
    * @param action the drag'n'drop action to do DnDConstants.ACTION_MOVE, ACTION_COPY, ACTION_LINK
    * @param index index between children the drop occured at or -1 if not specified
    * @return null if the transferable cannot be accepted or the paste type
    *    to execute when the drop occures
    */
    public PasteType getDropType (Transferable t, int action, int index) {
        java.util.List s = new LinkedList ();
        createPasteTypes (t, s);
        return s.isEmpty () ? null : (PasteType)s.get (0);
    }

    /* List new types that can be created in this node.
    * @return new types
    */
    public NewType[] getNewTypes () {
        return NO_NEW_TYPES;
    }

    /* Get the default action.
    * @return if there is a default action set, then returns it
    */
    public SystemAction getDefaultAction () {
        return defaultAction;
    }

    /** Set a default action for the node.
    * @param action the new default action
    */
    public void setDefaultAction (SystemAction action) {
        defaultAction = action;
    }

    /** Get all actions for the node.
    * Initialized with {@link #createActions}, or with the superclass's list.
    *
    * @return actions for the node
    */
    public SystemAction[] getActions () {
        if (systemActions == null) {
            systemActions = createActions ();
            if (systemActions == null) {
                systemActions = super.getActions ();
            }
        }
        return systemActions;
    }

    /** Lazily initialize set of node's actions (overridable).
    * The default implementation returns <code>null</code>.
    * <p><em>Warning:</em> do not call {@link #getActions} within this method.
    * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
    * @return array of actions for this node, or <code>null</code> to use the default node actions
    */
    protected SystemAction[] createActions () {
        return null;
    }

    /** Does this node have a customizer?
    * @return <CODE>false</CODE>
    */
    public boolean hasCustomizer () {
        return false;
    }

    /** Get the customizer.
    * @return <code>null</code> in the default implementation
    */
    public java.awt.Component getCustomizer () {
        return null;
    }

    /** Set the cookie set.
    * A listener is attached to the provided cookie set,
    * and any change of the sheet is propagated to the node by
    * firing {@link #PROP_COOKIE} change events.
    *
    * @param s the cookie set to use
    */
    protected final synchronized void setCookieSet (CookieSet s) {
        if (cookieSet != null) {
            cookieSet.removeChangeListener (cookieL);
        }

        s.addChangeListener (cookieL);
        cookieSet = s;

        fireCookieChange ();
    }

    /** Get the cookie set.
    *
    * @return the cookie set created by {@link #setCookieSet}, or an empty set (never <code>null</code>)
    */
    public final CookieSet getCookieSet () {
        CookieSet s = cookieSet;
        if (s != null) return s;
        synchronized (this) {
            if (cookieSet != null) return cookieSet;

            // sets empty sheet and adds a listener to it
            setCookieSet (new CookieSet ());
            return cookieSet;
        }
    }

    /** Get a cookie from the node.
    * Uses the cookie set as determined by {@link #getCookieSet}.
    *
    * @param type the representation class
    * @return the cookie or <code>null</code>
    */
    public Node.Cookie getCookie (Class type) {
        CookieSet c = cookieSet;
        if (c == null) return null;

        return c.getCookie (type);
    }

    /** Get a serializable handle for this node.
    * @return a {@link DefaultHandle} in the default implementation
    */
    public Handle getHandle () {
        return DefaultHandle.createHandle (this);
    }
}

/*
* Log
*  31   Gandalf   1.30        2/4/00   Jesse Glick     Icons work for test 
*       modules.
*  30   Gandalf   1.29        1/12/00  Jesse Glick     NOI18N
*  29   Gandalf   1.28        11/11/99 Petr Hrebejk    Temporary rollback of  
*       last change (icon lookup)
*  28   Gandalf   1.27        11/4/99  Jaroslav Tulach Different lookup of 
*       icons.
*  27   Gandalf   1.26        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  26   Gandalf   1.25        9/21/99  Jesse Glick     [JavaDoc]
*  25   Gandalf   1.24        8/17/99  Jan Jancura     Bug in firing in 
*       setIconBase
*  24   Gandalf   1.23        6/30/99  Jaroslav Tulach Drag and drop support
*  23   Gandalf   1.22        6/24/99  Jesse Glick     Nodes can specify context
*       help (not yet retrieved by anything, though).
*  22   Gandalf   1.21        6/9/99   Ian Formanek    Fixed resources for 
*       package change
*  21   Gandalf   1.20        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  20   Gandalf   1.19        5/17/99  Jaroslav Tulach 32x32 icon works
*  19   Gandalf   1.18        5/6/99   Jaroslav Tulach Works also without 
*       TopManager
*  18   Gandalf   1.17        4/28/99  Jesse Glick     [JavaDoc]
*  17   Gandalf   1.16        4/28/99  Jaroslav Tulach XML storage for modules.
*  16   Gandalf   1.15        4/24/99  Ian Formanek    Removed debug print
*  15   Gandalf   1.14        4/24/99  Ian Formanek    Fixed bug which caused 
*       HTML, URL and Properties object not to have correct icon
*  14   Gandalf   1.13        3/19/99  Jaroslav Tulach 
*  13   Gandalf   1.12        3/18/99  Jesse Glick     [JavaDoc]
*  12   Gandalf   1.11        3/18/99  Jaroslav Tulach getIcon searches for 
*       Icon, IconOpen, Icon32 and IconOpen32
*  11   Gandalf   1.10        3/17/99  Jesse Glick     [JavaDoc]
*  10   Gandalf   1.9         3/17/99  Petr Hamernik   
*  9    Gandalf   1.8         3/17/99  Petr Hamernik   accessibility of actions 
*       field
*  8    Gandalf   1.7         3/16/99  Jesse Glick     [JavaDoc]
*  7    Gandalf   1.6         3/4/99   Jaroslav Tulach Changed comment to 
*       reflect Dafe and Slavek wishes.
*  6    Gandalf   1.5         2/25/99  Jaroslav Tulach Change of clipboard 
*       management  
*  5    Gandalf   1.4         2/17/99  Ian Formanek    Updated icons to point to
*       the right package (under ide/resources)
*  4    Gandalf   1.3         1/7/99   Jaroslav Tulach 
*  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
*  2    Gandalf   1.1         1/6/99   Ian Formanek    Fixed outerclass 
*       specifiers uncompilable under JDK 1.2
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
