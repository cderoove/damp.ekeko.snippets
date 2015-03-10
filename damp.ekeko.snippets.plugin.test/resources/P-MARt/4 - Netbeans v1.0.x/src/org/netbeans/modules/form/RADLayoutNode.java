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

package org.netbeans.modules.form;

import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.cookies.*;
import org.openide.loaders.InstanceSupport;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;
import org.netbeans.modules.form.actions.*;

import java.awt.Image;
import java.text.MessageFormat;

/**
*
* @author Ian Formanek
*/
public class RADLayoutNode extends AbstractNode implements FormLayoutCookie, FormCookie {

    private final static MessageFormat nameFormat = new MessageFormat (NbBundle.getBundle (RADLayoutNode.class).getString ("FMT_LayoutName"));

    private RADVisualContainer container;

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    public RADLayoutNode (RADVisualContainer container) {
        super (Children.LEAF);
        this.container = container;
        getCookieSet ().add (this);
        container.setLayoutNodeReference (this);
        updateState ();
    }

    /** Provides access to layout node */
    public RADLayoutNode getLayoutNode () {
        return this;
    }

    public RADVisualContainer getRADContainer () {
        return container;
    }

    public void fireLayoutPropertiesChange () {
        firePropertyChange (null, null, null);
    }

    void updateState () {
        Class compClass = container.getBeanClass ();
        DesignLayout dl = container.getDesignLayout ();
        Class layClass = dl.getLayoutClass ();
        setName (nameFormat.format (
                     new Object[] {
                         dl.getDisplayName (),
                         layClass.getName (),
                         Utilities.getShortClassName (layClass),
                         container.getName (),
                         compClass.getName (),
                         Utilities.getShortClassName (compClass),
                     }
                 )
                );
        fireIconChange ();
    }

    /** Lazily initialize set of node's actions (overridable).
    * The default implementation returns <code>null</code>.
    * <p><em>Warning:</em> do not call {@link #getActions} within this method.
    * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
    * @return array of actions for this node, or <code>null</code> to use the default node actions
    */
    protected SystemAction [] createActions () {
        return new SystemAction [] {
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class),
               };
    }

    public Image getIcon (int iconType) {
        return container.getDesignLayout ().getIcon (iconType);
    }

    public Image getOpenedIcon (int iconType) {
        return getIcon (iconType);
    }

    public HelpCtx getHelpCtx () {
        HelpCtx help = InstanceSupport.findHelp (new InstanceSupport.Instance (container.getContainer ()));
        if (help != null)
            return help;
        else
            return new HelpCtx (RADLayoutNode.class);
    }

    public Node.PropertySet[] getPropertySets () {
        return container.getDesignLayout ().getPropertySet ();
    }

    /** Get a cookie from the node.
    * Uses the cookie set as determined by {@link #getCookieSet}.
    *
    * @param type the representation class
    * @return the cookie or <code>null</code>
    */
    public Node.Cookie getCookie (Class type) {
        Node.Cookie inh = super.getCookie (type);
        if (inh == null) {
            if (CompilerCookie.class.isAssignableFrom (type) ||
                    SaveCookie.class.isAssignableFrom (type) ||
                    ExecCookie.class.isAssignableFrom (type) ||
                    DebuggerCookie.class.isAssignableFrom (type) ||
                    CloseCookie.class.isAssignableFrom (type) ||
                    ArgumentsCookie.class.isAssignableFrom (type) ||
                    PrintCookie.class.isAssignableFrom (type)) {
                return container.getFormManager ().getFormObject ().getCookie (type);
            }
        }
        return inh;
    }

    /** Test whether there is a customizer for this node. If true,
    * the customizer can be obtained via {@link #getCustomizer}.
    *
    * @return <CODE>true</CODE> if there is a customizer
    */
    public boolean hasCustomizer () {
        return container.getDesignLayout ().getCustomizerClass () != null;
    }

    /** Get the customizer component.
    * @return the component, or <CODE>null</CODE> if there is no customizer
    */
    public java.awt.Component getCustomizer () {
        Class customizerClass = container.getDesignLayout ().getCustomizerClass ();
        if (customizerClass == null) return null;
        Object customizer;
        try {
            customizer = customizerClass.newInstance ();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
        if (!(customizer instanceof java.awt.Component) ||
                !(customizer instanceof java.beans.Customizer)) return null;
        ((java.beans.Customizer)customizer).setObject (container.getDesignLayout ());
        return (java.awt.Component)customizer;
    }


    // -------------------------------------------------------------------------------
    // FormCookie implementation

    /** Focuses the source editor */
    public void gotoEditor() {
        container.getFormManager ().getFormEditorSupport ().gotoEditor ();
    }

    /** Focuses the form */
    public void gotoForm() {
        container.getFormManager ().getFormEditorSupport ().gotoForm ();
    }

}

/*
 * Log
 *  18   Gandalf   1.17        1/5/00   Ian Formanek    NOI18N
 *  17   Gandalf   1.16        10/27/99 Ian Formanek    SetLayout and 
 *       CustomizeLayout removed from popup menu (HIIR feedback)
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        7/28/99  Ian Formanek    Fixed bug 2890 - Go to 
 *       Source and Go to Form actions are not enabled in a context menu of a 
 *       component
 *  14   Gandalf   1.13        7/25/99  Ian Formanek    Fixed bug with too many 
 *       tools actions (namely those on DataObject.class) being enabled on the 
 *       node
 *  13   Gandalf   1.12        7/20/99  Jesse Glick     Context help.
 *  12   Gandalf   1.11        7/13/99  Ian Formanek    firing layout node 
 *       changes access
 *  11   Gandalf   1.10        7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  10   Gandalf   1.9         7/5/99   Ian Formanek    CustomizeLayout action
 *  9    Gandalf   1.8         6/27/99  Ian Formanek    Many form actions 
 *       (compile, save, ...) are now enabled on form and component inspector
 *  8    Gandalf   1.7         6/22/99  Ian Formanek    Added support for 
 *       customizers
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  5    Gandalf   1.4         5/20/99  Ian Formanek    
 *  4    Gandalf   1.3         5/16/99  Ian Formanek    Fixed bug 1828 - 
 *       Changing layout of a component doesn't change the textual 
 *       represenatation of layout of the component in Component Inspector
 *  3    Gandalf   1.2         5/16/99  Ian Formanek    
 *  2    Gandalf   1.1         5/15/99  Ian Formanek    
 *  1    Gandalf   1.0         5/12/99  Ian Formanek    
 * $
 */
