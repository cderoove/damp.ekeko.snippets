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

package org.netbeans.modules.form.palette;

import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.nodes.Node;

/** The PaletteAction is an action that has a state represented by
* a PaletteNode value. The state is a PaletteNode that represents
* a Javabean to be added as a new component to the Form, or null for
* Selection mode. R/W property addMode representates tihs state...
* PaletteAction has table of all currently used DesignLayouts, and It can find
* proper DesignLayout for given class of LayoutManager (see method
* getDesignLayout ()).
*
* @author   Ian Formanek, Jan Jancura
*/
public class PaletteAction extends SystemAction implements Presenter.Toolbar { //, Presenter.Menu
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6466826799827497471L;

    /** In selection mode, clicking on a form selects components */
    public final static int MODE_SELECTION = 0;
    /** In connection mode, clicking on a form connects components (event -> method,property) */
    public final static int MODE_CONNECTION = 1;
    /** In add mode, clicking on a form adds a new component to the form */
    public final static int MODE_ADD = 2;

    // SystemAction interface ...................................................................

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (PaletteAction.class).getString ("CTL_Component_palette");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PaletteAction.class);
    }

    /** @return resource for the action icon */
    protected String iconResource () {
        return "/org/netbeans/modules/form/resources/paletteAction.gif"; // NOI18N
    }

    /** Returns a JMenuItem that presents the Action, that implements this
    * interface, in a MenuBar.
    * @return the JMenuItem representation for the Action
    */
    public javax.swing.JMenuItem getMenuPresenter() {
        return null;
    }

    /** Returns a Component that presents the Action, that implements this
    * interface, in a ToolBar.
    * @return the Component representation for the Action
    */
    public java.awt.Component getToolbarPresenter() {
        return new ComponentPalette ();
    }


    // Palette methods ...................................................................

    /** Getter method for the paletteMode property
    * @return Current Mode property value - one of MODE_SELECTION, MODE_CONNECTION, MODE_ADD constants
    *
    public int getPaletteMode () {
      return getComponentPalette ().getMode();
}

    /** Setter method for the paletteMode property
    * @param mode the desired new Mode property value - one of MODE_SELECTION, MODE_CONNECTION, MODE_ADD constants
    *
    public void setPaletteMode (int mode) {
      getComponentPalette ().setMode(mode);
}

    /** Do nothing. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {}

    /** Getter method for the AddComponent property.
    * @return Current Mode property value
    */
    /*  public PaletteNode getAddComponent() {
        return getComponentPalette ().getAddNode();
      } */

    /** Sets the node that would be added as a new component to the form
    * @param value The PaletteNode that represents the addComponent
    *              or null for selection mode.
    */
    /*  public void setAddComponent(PaletteNode value) {
        getComponentPalette ().setAddNode(value);
      } */

    /** Returns the proper DesignLayout class for the LayoutManager class.
    * @param layoutManagerClass The DesignLayout class for whis the LayoutManager class is looking
    *  for.
    * @return Class of proper DesignLayout.
    */
    /*  public Class getDesignLayout (Class layoutManagerClass) {
        return PaletteContext.getPaletteContext (). getDesignLayout (layoutManagerClass);
      } */

    /** Returns the proper DesignLayout class for the LayoutManager class.
    * @param layoutManagerClass The DesignLayout class for whis the LayoutManager class is looking
    *  for.
    * @return Class of proper DesignLayout.
    */
    /*  public java.awt.Image getIconForClass (Class cl, int iconType) {
        PaletteNode node = getComponentPalette ().getNodeForClass (cl);
        if (node == null)
          return null;
        return node.getIcon (iconType);
      } */

}

/*
 * Log
 *  9    Gandalf   1.8         1/5/00   Ian Formanek    NOI18N
 *  8    Gandalf   1.7         11/10/99 Pavel Buzek     mode and 
 *       selectedPaletteItem properties made static
 *  7    Gandalf   1.6         11/8/99  Pavel Buzek     instead of creating new 
 *       ComponentPalette take the default instance
 *  6    Gandalf   1.5         11/4/99  Jaroslav Tulach Component palette is 
 *       faster/better/etc.
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/14/99  Ian Formanek    
 *  2    Gandalf   1.1         3/26/99  Jesse Glick     SystemAction.actionPerformed(ActionEvent)
 *        is now abstract; you must explicitly provide an empty body if that is 
 *       desired.
 *  1    Gandalf   1.0         3/24/99  Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    bugfix, got rid of addComponent variable
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    cleaned up
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   dataobject removed
 */
