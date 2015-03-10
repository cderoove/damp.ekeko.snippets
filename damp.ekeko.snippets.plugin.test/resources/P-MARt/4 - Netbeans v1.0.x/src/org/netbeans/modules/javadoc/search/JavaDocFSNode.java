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

package org.netbeans.modules.javadoc.search;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.beans.BeanInfo;
import java.beans.PropertyEditor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.util.actions.SystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.nodes.Node;
import org.openide.nodes.BeanNode;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.filesystems.FileSystem;

/** The basic node for representing features included in BanInfo. It recognizes
* the type of the BiFeature and creates properties according to it.
* @author Petr Hrebejk
*/
class JavaDocFSNode extends BeanNode implements Node.Cookie {

    // private variables ..........................................................................

    private FileSystem fileSystem;

    // static variables ...........................................................................

    // Resource bundle
    private static ResourceBundle bundle = NbBundle.getBundle (JavaDocFSNode.class);

    // constructors .......................................................................

    /**
    * Creates empty BreakpointContext.
    */
    public JavaDocFSNode ( Object bean, Children children ) throws java.beans.IntrospectionException {
        super ( bean, children );
        getCookieSet().add( this );
        fileSystem = (FileSystem)bean;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (JavaDocNode.class);
    }

    /** Setter for parent node. Is protected for subclasses. Fires info about
    * change of the parent.
    *
    * @param n new parent node
    */
    /*
    protected void setParentNode (Node n) {
      super.setParentNode (n);
}
    */
    /*
    private void init () {
      createProperties ();
      getCookieSet().add ( this );

}
    */

    public Node.Cookie getCookie( Class type ) {
        return getCookieSet().getCookie( type );
    }

    /** Creates properties for this node */
    protected void createProperties () {


    }
    // implementation of Node ..........................................................

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    SystemAction[] staticActions;

    public SystemAction[] getActions () {
        if (staticActions == null) {
            staticActions = new SystemAction[] {
                                SystemAction.get (UnmountJavaDocFSAction.class),
                                /*
                                null
                                SystemAction.get (DeleteAction.class),
                                null,
                                SystemAction.get (ToolsAction.class),
                                SystemAction.get (PropertiesAction.class),
                                */
                            };
        }
        return staticActions;
    }

    /**
    * the feature cannot be removed it can only be disabled from BeanInfo
    *
    * @return <CODE>true</CODE>
    */
    public boolean canDestroy () {
        return false;
    }

    /**
    * Deletes breakpoint and removes the node too.
    * Ovverrides destroy() from abstract node.
    */
    public void destroy () throws IOException {
        // remove node
        // super.destroy ();
    }

    /** It has default action - it is the toggle of value for include to bean info
    * @return <CODE>true</CODE>
    */
    public boolean hasDefaultAction () {
        return false;
    }

    /** Returns the fileSystem which this node represents */
    FileSystem getFileSystem() {
        return fileSystem;
    }
}

/*
 * Log
 *
 */
