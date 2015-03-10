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

package org.openide.actions;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.lang.ref.*;
import javax.swing.*;


import org.openide.*;
import org.openide.awt.Actions;
import org.openide.explorer.view.MenuView;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.Mutex;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import org.openide.util.WeakListener;

/** Creates a new object from template in the selected folder.
* @see DataObject#isTemplate
*
* @author Petr Hamernik, Dafe Simonek
*/
public class NewTemplateAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5408651725508985475L;
    /** Last selected folder reference to (DataFolder). */
    private static Reference where;

    /** wizard */
    private static TemplateWizard wizard;

    /** Getter for wizard
    */
    static TemplateWizard getWizard () {
        if (wizard == null) {
            wizard = new TemplateWizard ();
        }
        return wizard;
    }

    protected void performAction (Node[] activatedNodes) {
        try {
            getWizard ().instantiate ();
        } catch (IOException ex) {
            TopManager.getDefault ().notifyException (ex);
        }
    }

    /* Enables itself only when activates node is DataFolder.
    */
    protected boolean enable (Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1))
            return false;
        DataFolder cookie = (DataFolder)activatedNodes[0].getCookie(DataFolder.class);
        if (cookie != null && !cookie.getPrimaryFile ().isReadOnly ()) {
            where = new WeakReference (cookie);
            return true;
        }
        return false;
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("NewTemplate");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (NewTemplateAction.class);
    }

    /* Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/new.gif"; // NOI18N
    }

    /* Creates presenter that invokes the associated presenter.
    */
    public JMenuItem getMenuPresenter() {
        return new Actions.MenuItem (this, true) {
                   public void setEnabled (boolean e) {
                       super.setEnabled (true);
                   }
               };
    }

    /* Creates presenter that invokes the associated presenter.
    */
    public Component getToolbarPresenter() {
        return new Actions.ToolbarButton (this) {
                   public void setEnabled (boolean e) {
                       super.setEnabled (true);
                   }
               };
    }

    /* Creates presenter that displayes submenu with all
    * templates.
    */
    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new MenuView.Menu (getTemplateRoot (), new TemplateActionListener (), false);
        Actions.connect (menu, this, true);
        //    menu.setName (getName ());
        return menu;
    }

    /** Create a hierarchy of templates.
    * @return a node representing all possible templates
    */
    public static Node getTemplateRoot () {
        DataFolder f = TopManager.getDefault ().getPlaces ().folders ().templates ();
        // listener used as filter (has method acceptDataObject)
        Children ch = f.createNodeChildren (new TemplateActionListener ());
        // filter the children
        ch = new TemplateChildren (new AbstractNode (ch));
        // create the root
        return new AbstractNode (ch);
    }


    /** Actions listener which instantiates the template */
    private static class TemplateActionListener
        implements MenuView.Acceptor, DataFilter {

        static final long serialVersionUID =1214995994333505784L;
        public boolean accept (Node n) {
            DataObject obj = (DataObject)n.getCookie (DataObject.class);
            if (obj == null || !obj.isTemplate ()) {
                // do not accept
                return false;
            }

            DataFolder folderToUse = (DataFolder)where.get ();
            if (folderToUse == null) {
                return false;
            }

            try {
                getWizard ().instantiate (obj, folderToUse);
            } catch (IOException e) {
                String msg = e.getMessage();
                if ((msg == null) || msg.equals("")) // NOI18N
                    msg = ActionConstants.BUNDLE.getString("EXC_TemplateFailed");
                TopManager.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
            }

            // ok
            return true;
        }

        /** Data filter impl.
        */
        public boolean acceptDataObject (DataObject obj) {
            return obj.isTemplate () || obj instanceof DataFolder;
        }
    }

    /** Filter node children, that stops on data objects (does not go futher)
    */
    private static class TemplateChildren extends FilterNode.Children {
        public TemplateChildren (Node or) {
            super (or);
        }

        protected Node copyNode (Node n) {
            DataFolder df = (DataFolder)n.getCookie (DataFolder.class);
            if (df == null || df.isTemplate ()) {
                // on normal nodes stop recursion
                return new FilterNode (n, LEAF);
            } else {
                // on folders use normal filtering
                return new FilterNode (n, new TemplateChildren (n));
            }
        }
    }
}

/*
 * Log
 *  29   Gandalf   1.28        1/16/00  Jaroslav Tulach TemplatesExplorer 
 *       removed, startup faster
 *  28   Gandalf   1.27        1/12/00  Ian Formanek    NOI18N
 *  27   Gandalf   1.26        1/9/00   Petr Hamernik   fixed 2829
 *  26   Gandalf   1.25        11/24/99 Jaroslav Tulach New "New From Template" 
 *       Dialog
 *  25   Gandalf   1.24        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   Gandalf   1.23        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  23   Gandalf   1.22        7/21/99  Ian Formanek    Creating subpackages 
 *       from name only for Folders and SourceCookie providers
 *  22   Gandalf   1.21        7/20/99  Ian Formanek    Creation of new objects 
 *       enhanced with creation of inter-folders
 *  21   Gandalf   1.20        7/6/99   Ian Formanek    Removed obsoleted code
 *  20   Gandalf   1.19        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  19   Gandalf   1.18        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  18   Gandalf   1.17        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  17   Gandalf   1.16        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  16   Gandalf   1.15        5/14/99  Petr Hamernik   Bug 1399
 *  15   Gandalf   1.14        5/13/99  Jaroslav Tulach Bug 1579
 *  14   Gandalf   1.13        5/2/99   Ian Formanek    Fixed last change
 *  13   Gandalf   1.12        5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  12   Gandalf   1.11        3/29/99  Ian Formanek    
 *  11   Gandalf   1.10        3/26/99  Jesse Glick     [JavaDoc]
 *  10   Gandalf   1.9         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  9    Gandalf   1.8         3/21/99  Jaroslav Tulach Now has assigned key in 
 *       menu.
 *  8    Gandalf   1.7         3/9/99   Jaroslav Tulach Template Action
 *  7    Gandalf   1.6         3/9/99   Jaroslav Tulach Node actions releases 
 *       sometimes its listeners.
 *  6    Gandalf   1.5         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  5    Gandalf   1.4         2/5/99   Jaroslav Tulach Changed new from 
 *       template action
 *  4    Gandalf   1.3         1/20/99  Jaroslav Tulach 
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
