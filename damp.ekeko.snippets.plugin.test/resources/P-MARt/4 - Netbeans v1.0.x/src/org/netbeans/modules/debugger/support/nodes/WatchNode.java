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

package org.netbeans.modules.debugger.support.nodes;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import javax.swing.JPopupMenu;

import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.TopManager;
import org.openide.debugger.Debugger;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.support.actions.CreateVariableAction;
import org.netbeans.modules.debugger.support.actions.CreateVariableCookie;
import org.netbeans.modules.debugger.support.actions.CreateVariableAccessBreakpointAction;
import org.netbeans.modules.debugger.support.actions.CreateVariableModificationBreakpointAction;
import org.netbeans.modules.debugger.support.DebuggerAdapter;
import org.netbeans.modules.debugger.support.AbstractWatch;


/**
* This class represents watch as a Node.
* This class is final only for performance reasons.
* Can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class WatchNode extends VariableNode {
    /** generated Serialized Version UID */
    static final long               serialVersionUID = 7100337776961302436L;

    protected static String         ICON_BASE =
        "/org/netbeans/core/resources/watch"; // NOI18N
    private static ResourceBundle   bundle;


    // variables .................................................................

    protected AbstractWatch              watch;


    // init ......................................................................

    /**
    * Creates empty Watch context.
    */
    public WatchNode (
        VariableHome variableHome,
        final AbstractWatch watch
    ) {
        super (variableHome, watch, true);
        setIconBase (ICON_BASE);
        this.watch = watch;
    }

    protected void init () {
        this.watch = (AbstractWatch) variable;
        super.init ();
    }


    // implementation of Node ....................................................

    /**
    * Variable can be removed.
    *
    * @return <CODE>true</CODE>
    */
    public boolean canDestroy () {
        return true;
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
        return new SystemAction[] {
                   SystemAction.get (CreateVariableAction.class),
                   null,
                   //      CreateVariableModificationBreakpointAction.get (CreateVariableModificationBreakpointAction.class),
                   //      CreateVariableAccessBreakpointAction.get (CreateVariableAccessBreakpointAction.class),
                   //      null,
                   SystemAction.get (DeleteAction.class),
                   null,
                   SystemAction.get (ToolsAction.class),
                   SystemAction.get (PropertiesAction.class),
               };
    }

    /**
    * Removes the Variable from its parent and deletes it.
    */
    public void destroy () throws IOException {
        //S ystem.out.println("WatchNode.destroy " + getDisplayName ());

        // remove breakpoint
        super.destroy ();
        watch.remove ();
        watch = null;
    }

    /** Creates properties for watch context
    * (remember that watch extends variable context,
    * so we must pay an attention to its properties too)
    * Overrides createProperties from Variable context.
    */
    protected void createProperties () {
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault ();
        Sheet.Set ps = sheet.get (Sheet.PROPERTIES);
        // and add rw property of the same name
        ps.put(new PropertySupport.ReadWrite (
                   AbstractWatch.PROP_VARIABLE_NAME,
                   String.class,
                   getLocalizedString ("PROP_watch_name"),
                   getLocalizedString ("HINT_watch_name")
               ) {
                   public Object getValue () {
                       return watch.getVariableName ();
                   }
                   public void setValue (Object val) throws IllegalArgumentException {
                       try {
                           watch.setVariableName (((String)val).trim ());
                       } catch (ClassCastException e) {
                           throw new IllegalArgumentException ();
                       }
                   }
               });
        // add type and value props
        createCommonProperties(bundle, ps);
        // and scope info, finally
        ps.put(new PropertySupport.ReadOnly (
                   AbstractWatch.PROP_IN_SCOPE,
                   Boolean.TYPE,
                   getLocalizedString ("PROP_watch_in_scope"),
                   getLocalizedString ("HINT_watch_in_scope")
               ) {
                   public Object getValue () {
                       return new Boolean (watch.isInScope ());
                   }
               });
        // and set new sheet
        setSheet(sheet);
    }


    // private methods ...........................................................

    /**
    * Returns watch.
    */
    AbstractWatch getWatch () {
        return watch;
    }

    /**
    * Returns human presentable name of this watch containing
    * informations about value or valide.
    *
    * @return human presentable name of this variable.
    */
    public String getName () {
        String name = watch.getVariableName ();
        if (name == null) return "???"; // NOI18N

        if ( (debugger == null) ||
                (debugger.getState () == Debugger.DEBUGGER_NOT_RUNNING)
           ) return name + " = " + getLocalizedString ("CTL_NoSession");

        String errorMessage = watch.getErrorMessage ();
        if (errorMessage != null)
            return name + " = >" + errorMessage + "<"; // NOI18N
        String type = watch.getType ();
        String innerType = watch.getInnerType ();
        String value = watch.getAsText ();
        if (watch.isObject ()) {
            if (type.equals (innerType))
                return name + " = (" + innerType + ") " + value; // NOI18N
            else
                return name + " = (" + type + ") (" + innerType + ") " + value; // NOI18N
        }
        else
            return name + " = (" + type + ") " + value; // NOI18N
    }

    public void createVariable () {
        Node n = getParentNode ();
        while (!(n instanceof WatchesRootNode)) n = n.getParentNode ();
        ((WatchesRootNode)n).createVariable (watch.getVariable ());
    }

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (WatchNode.class);
        return bundle.getString (s);
    }
}

/*
 * Log
 *  12   Gandalf-post-FCS1.10.3.0    3/28/00  Daniel Prusa    
 *  11   Gandalf   1.10        1/14/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         1/13/00  Daniel Prusa    NOI18N
 *  9    Gandalf   1.8         1/4/00   Jan Jancura     Use trim () on user 
 *       input.
 *  8    Gandalf   1.7         1/4/00   Daniel Prusa    enabling/disabling of 
 *       Create fixed watch action
 *  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
 *  6    Gandalf   1.5         11/5/99  Jan Jancura     Default action updated
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/28/99  Jan Jancura     Add variable a/m 
 *       breakpoint actions
 *  3    Gandalf   1.2         9/15/99  Jan Jancura     
 *  2    Gandalf   1.1         9/2/99   Jan Jancura     
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.16        --/--/98 Jan Jancura     Bugfix 0155
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    popup menu improved
 */
