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

package org.netbeans.modules.javadoc.comments;

import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.util.ResourceBundle;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.src.ClassElement;
import org.openide.cookies.SourceCookie;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;

//import org.netbeans.modules.java.JavaDataObject;

/**
* Auto comment action.
*
* @author   Petr Hrebejk
*/
public class AutoCommentAction extends CookieAction {

    // Resource bundle
    private static final ResourceBundle bundle = NbBundle.getBundle( AutoCommentAction.class );


    static final long serialVersionUID =4989490116568783623L;
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return bundle.getString ("CTL_AUTOCOMMENT_MenuItem");
    }

    /** Cookie classes contains one class returned by cookie () method.
    */
    protected final Class[] cookieClasses () {
        return new Class[] { SourceCookie.Editor.class };
    }

    /** All must be DataFolders or JavaDataObjects
    */
    protected int mode () {
        return MODE_ALL;
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (AutoCommentAction.class);
    }

    /*
    protected boolean enable( Node[] activatedNodes ) {
      if (activatedNodes.length != 1 )
        return false;
      else
        return true;
}
    */

    /** This method is called by one of the "invokers" as a result of
    * some user's action that should lead to actual "performing" of the action.
    * This default implementation calls the assigned actionPerformer if it
    * is not null otherwise the action is ignored.
    */
    public void performAction ( Node[] nodes ) {

        AutoCommentTopComponent acTopComponent = AutoCommentTopComponent.getDefault();

        acTopComponent.open();
        acTopComponent.requestFocus();

        acTopComponent.setAutoCommenter( new AutoCommenter( nodes ));
    }
}


/*
 * Log
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/16/99  Petr Hrebejk    Default Comment changed 
 *       to Auto-Correct
 *  4    Gandalf   1.3         8/13/99  Petr Hrebejk    Window serialization 
 *       added & Tag change button in Jdoc editor removed 
 *  3    Gandalf   1.2         8/6/99   Petr Hrebejk    Icon Resource fixed
 *  2    Gandalf   1.1         7/30/99  Petr Hrebejk    Autocomment made 
 *       TopComponent
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $
 */
