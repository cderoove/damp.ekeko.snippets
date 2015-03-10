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

package org.netbeans.core.windows.nodes;

import java.awt.Image;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WorkspaceImpl;

/** Node for a mode.
*
* @author Dafe Simonek
*/
public final class ModeContext extends AbstractNode
    implements PropertyChangeListener {
    /** bounds property name */
    public static final String PROP_BOUNDS = "bounds"; // NOI18N
    /** visibility flag name */
    public static final String PROP_VISIBLE = "visible"; // NOI18N

    /** Asociation with the mode implementation. */
    ModeImpl mode;

    /** Default constructor
    * @param mode mode to provide context to
    */
    public ModeContext (ModeImpl mode) {
        super (Children.LEAF);
        this.mode = mode;
        setName(mode.getDisplayName());
        setShortDescription (NbBundle.getBundle (ModeContext.class).getString ("HINT_ModeContext"));
        // set default icon base (used when mode has no icon)
        setIconBase("/org/netbeans/core/resources/actions/window"); // NOI18N
        // weak listener on changes in mode
        mode.addPropertyChangeListener(
            WeakListener.propertyChange(this, mode)
        );
        // PENDING
        /*getCookieSet ().add (new OpenCookie () {
          public void open() {
            ((WindowManagerImpl) TopManager.getDefault().getWindowManager()).setCurrentWorkspace(WorkspaceContext.this.workspace);
          }
    });*/
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ModeContext.class);
    }

    /** Overrides superclass version to return icon of
    * the mode */
    public Image getIcon (int type) {
        Image modeIcon = mode.getIcon();
        return (modeIcon == null) ? super.getIcon(type) : modeIcon;
    }

    public Image getOpenedIcon (int type) {
        return getIcon(type);
    }

    /** Creates properties for this data object */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault ();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        final ResourceBundle ourBundle = NbBundle.getBundle(ModeContext.class);
        // display name of the mode
        ps.put(new PropertySupport.ReadWrite (
                   PROP_DISPLAY_NAME,
                   String.class,
                   ourBundle.getString("PROP_modeDisplayName"),
                   ourBundle.getString("HINT_modeDisplayName")
               ) {
                   public Object getValue() {
                       return mode.getDisplayName();
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof String) {
                           mode.setDisplayName((String)val);
                       } else {
                           throw new IllegalArgumentException();
                       }
                   }
               });
        // programmatic name of the mode
        ps.put (new PropertySupport.Name(
                    this,
                    ourBundle.getString("PROP_modeName"),
                    ourBundle.getString("HINT_modeName")
                ));
        // mode bounds
        ps.put(new PropertySupport.ReadWrite (
                   PROP_BOUNDS,
                   Rectangle.class,
                   ourBundle.getString("PROP_modeBounds"),
                   ourBundle.getString("HINT_modeBounds")
               ) {
                   public Object getValue() {
                       return mode.getBounds();
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof Rectangle) {
                           mode.setBounds((Rectangle)val);
                       } else {
                           throw new IllegalArgumentException();
                       }
                   }
               });
        // mode visibility flag
        ps.put(new PropertySupport.ReadOnly (
                   PROP_VISIBLE,
                   Boolean.TYPE,
                   ourBundle.getString("PROP_modeVisible"),
                   ourBundle.getString("HINT_modeVisible")
               ) {
                   public Object getValue() {
                       return new Boolean(mode.getOpenedTopComponents().length > 0);
                   }
               });
        // PENDING - container type etc will be here in future
        return sheet;
    }

    /** Context menu that should be assigned to this Node.
    * @return the popup menu
    */
    public SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (PropertiesAction.class)
               };
    }

    /** Default action.
    */
    public SystemAction getDefaultAction () {
        return SystemAction.get (PropertiesAction.class);
    }

    /** Reaction to the change in mode. Refreshes the node */
    public void propertyChange (PropertyChangeEvent che) {
        if (che.getPropertyName().equals(ModeImpl.PROP_TOP_COMPONENTS)) {
            firePropertyChange(
                PROP_VISIBLE,
                null,
                new Boolean(mode.getOpenedTopComponents().length > 0)
            );
        } else if (che.getPropertyName().equals(ModeImpl.PROP_BOUNDS)) {
            firePropertyChange(PROP_BOUNDS, null, mode.getBounds());
        } else if (che.getPropertyName().equals(ModeImpl.PROP_DISPLAY_NAME)) {
            setName((String)che.getNewValue());
        }
    }

}

/*
* Log
*  7    Gandalf   1.6         1/16/00  Jesse Glick     Tool tips.
*  6    Gandalf   1.5         1/13/00  David Simonek   i18n
*  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
*  4    Gandalf   1.3         1/6/00   David Simonek   Children removed, 
*       visibility RO property added
*  3    Gandalf   1.2         11/6/99  David Simonek   new WeakListener strategy
*       followed...
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/23/99  David Simonek   
* $
*/