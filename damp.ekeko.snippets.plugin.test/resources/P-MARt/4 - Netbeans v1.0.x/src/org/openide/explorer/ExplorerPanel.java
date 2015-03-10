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

package org.openide.explorer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.text.MessageFormat;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.TopManager;

/** Simple top component capable of displaying an Explorer.
* Holds one instance of {@link ExplorerManager} and 
* implements {@link ExplorerManager.Provider} to allow child components to share 
* the same explorer manager.
* <p>Uses {@link java.awt.BorderLayout} by default.
* Pays attention to the selected nodes and explored context as indicated by the manager.
* Cut/copy/paste actions are sensitive to the activation state of the component.
* <p>It is up to you to add a view and other UI apparatus to the panel.
*
* @author Jaroslav Tulach
*/
public class ExplorerPanel extends TopComponent implements ExplorerManager.Provider {
    /** serial version UID */
    static final long serialVersionUID = 5526588776650701459L;

    /** The message formatter for Explorer title */
    private static MessageFormat formatExplorerTitle;

    /** the instance of the explorer manager*/
    private ExplorerManager manager;

    /** listens on the selected nodes in the ExporerManager */
    transient private PropertyChangeListener managerListener;

    /** action handler for cut/copy/paste/delete */
    private static ExplorerActions actions = new ExplorerActions ();

    /** Initialize the explorer panel with the provided manager.
    * @param explorer the explorer manager to use
    */
    public ExplorerPanel (ExplorerManager manager) {
        this ();
        this.manager = manager;
    }

    /** Default constructor. Uses newly created manager.
    */
    public ExplorerPanel () {
        setLayout (new java.awt.BorderLayout ());
    }

    /* Add a listener to the explorer panel in addition to the normal
    * open behaviour.
    */
    public void open () {
        open(TopManager.getDefault().getWindowManager().getCurrentWorkspace());
    }

    /* Add a listener to the explorer panel in addition to the normal
    * open behaviour.
    */
    public void open (Workspace workspace) {
        super.open(workspace);
        if (managerListener == null) {
            managerListener = new PropL ();
            getExplorerManager ().addPropertyChangeListener (managerListener);
        }
        setActivatedNodes (getExplorerManager ().getSelectedNodes ());
        updateTitle ();
    }

    /* Remove listeners to the explorer panel.
    */
    public boolean canClose (Workspace workspace, boolean last) {
        boolean result = super.canClose(workspace, last);
        if (result && last) {
            getExplorerManager ().removePropertyChangeListener (managerListener);
            managerListener = null;
        }
        return result;
    }

    /* Provides the explorer manager to all who are interested.
    * @return the manager
    */
    public ExplorerManager getExplorerManager () {
        if (manager == null) {
            synchronized (this) {
                if (manager == null) {
                    manager = new ExplorerManager ();
                }
            }
        }
        return manager;
    }

    /* Activates copy/cut/paste actions.
    */
    protected void componentActivated () {
        actions.attach (getExplorerManager ());
    }

    /* Deactivates copy/cut/paste actions.
    */
    protected void componentDeactivated () {
        actions.detach ();
    }

    /** Called when the explored context changes.
    * The default implementation updates the title of the window.
    */
    protected void updateTitle () {
        String name = ""; // NOI18N

        ExplorerManager em = getExplorerManager ();
        if (em != null) {
            Node n = em.getExploredContext();
            if (n != null) {
                String nm = n.getDisplayName();
                if (nm != null) {
                    name = nm;
                }
            }
        }

        if (formatExplorerTitle == null) {
            formatExplorerTitle = new MessageFormat (
                                      ExplorerManager.explorerBundle.getString ("explorerTitle")
                                  );
        }
        setName(formatExplorerTitle.format (
                    new Object[] { name }
                ));
    }

    /** Get context help for an explorer window.
    * Looks at the manager's node selection.
    * @return the help context
    * @see #getHelpCtx(Node[],HelpCtx)
    */
    public HelpCtx getHelpCtx () {
        return getHelpCtx (getExplorerManager ().getSelectedNodes (),
                           new HelpCtx (ExplorerPanel.class));
    }

    /** Utility method to get context help from a node selection.
    * Tries to find context helps for selected nodes.
    * If there are some, and they all agree, uses that.
    * In all other cases, uses the supplied generic help.
    * @param sel a list of nodes to search for help in
    * @param def the default help to use if they have none or do not agree
    * @return a help context
    */
    public static HelpCtx getHelpCtx (Node[] sel, HelpCtx def) {
        HelpCtx result = null;
        for (int i = 0; i < sel.length; i++) {
            HelpCtx attempt = sel[i].getHelpCtx ();
            if (attempt != null && ! attempt.equals (HelpCtx.DEFAULT_HELP)) {
                if (result == null || result.equals (attempt)) {
                    result = attempt;
                } else {
                    // More than one found, and they conflict. Get general help on the Explorer instead.
                    result = null;
                    break;
                }
            }
        }
        if (result != null)
            return result;
        else
            return def;
    }

    /** Set whether deletions should have to be confirmed on all Explorer panels.
    * @param confirmDelete <code>true</code> to confirm, <code>false</code> to delete at once
    */
    public static void setConfirmDelete (boolean confirmDelete) {
        actions.setConfirmDelete (confirmDelete);
    }

    /** Are deletions confirmed on all Explorer panels?
    * @return <code>true</code> if they must be confirmed
    */
    public static boolean isConfirmDelete () {
        return actions.isConfirmDelete ();
    }

    /** Stores the manager */
    public void writeExternal (java.io.ObjectOutput oo) throws java.io.IOException {
        super.writeExternal (oo);
        oo.writeObject (manager);
    }

    /** Reads the manager */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        super.readExternal (oi);
        manager = (ExplorerManager)oi.readObject ();
    }

    /** Listener on the explorer manager properties.
    * Changes selected nodes of this frame.
    */
    private final class PropL extends Object implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                setActivatedNodes (manager.getSelectedNodes ());
                return;
            }
            if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                updateTitle ();
                return;
            }
        }
    }

}

/*
* Log
*  23   Gandalf   1.22        1/12/00  Ian Formanek    NOI18N
*  22   Gandalf   1.21        11/5/99  Jesse Glick     Rearranged context help 
*       for ExplorerPanel vs. TopComponent.
*  21   Gandalf   1.20        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  20   Gandalf   1.19        8/9/99   Jaroslav Tulach 
*  19   Gandalf   1.18        7/30/99  David Simonek   commentaries removed...
*  18   Gandalf   1.17        7/28/99  David Simonek   canClose updates
*  17   Gandalf   1.16        7/21/99  David Simonek   window system updates...
*  16   Gandalf   1.15        7/20/99  Jesse Glick     
*  15   Gandalf   1.14        7/16/99  Jesse Glick     Window system changes.
*  14   Gandalf   1.13        7/11/99  David Simonek   window system change...
*  13   Gandalf   1.12        6/24/99  Jesse Glick     Context help from node 
*       selection, if possible.
*  12   Gandalf   1.11        6/22/99  Jaroslav Tulach 
*  11   Gandalf   1.10        6/17/99  David Simonek   various serialization 
*       bugfixes
*  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  9    Gandalf   1.8         5/16/99  Jaroslav Tulach Serializes the selection 
*       in the explorer panel.
*  8    Gandalf   1.7         4/16/99  Libor Martinek  
*  7    Gandalf   1.6         4/2/99   Jesse Glick     [JavaDoc]
*  6    Gandalf   1.5         4/2/99   Jaroslav Tulach 
*  5    Gandalf   1.4         3/20/99  Jesse Glick     [JavaDoc]
*  4    Gandalf   1.3         3/16/99  Ian Formanek    Just cleaned private 
*       variable name
*  3    Gandalf   1.2         3/9/99   Ian Formanek    Uses BorderLayout by 
*       default
*  2    Gandalf   1.1         1/7/99   David Simonek   
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
