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

package org.netbeans.core;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.netbeans.core.awt.ButtonBar;
import org.netbeans.core.awt.ButtonBarButton;
import org.openide.TopManager;
import org.openide.util.HelpCtx;

/** The CoronaDialog is a standard dialog that contains a ButtonBar on the South.
* All the "add(Component)" and "setLayout(LayoutManager)" requests should be
* performed on the Container acquired from getContentPane() (similar to
* getContentPane() in JFrame and JDialog).
* The dialog can be both instantiated OR subclassed.
*
* @author   Ian Formanek, Jaroslav Tulach, Jan Jancura, Petr Hamernik
* @version  0.56, May 16, 1998
*/
public class CoronaDialog extends JDialog {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5301615034186604735L;
    /** Constructs a new modal CoronaDialog with empty button bar.
    * Descendants can initialize the ButtonBar later by calling setButtons()
    * on the buttonBar acquired by protected method getButtonBar.
    * @param parent The Frame parent of the dialog or null if the parent should be the Corona's MainWIndow
    * @param modal If the dialog is modal
    */
    public CoronaDialog (Frame parent, boolean modal) {
        this(parent, ButtonBar.EMPTY, modal);
    }

    /** Constructs a new modal CoronaDialog with empty button bar.
    * Descendants can initialize the ButtonBar later by calling setButtons()
    * on the buttonBar acquired by protected method getButtonBar.
    * @param parent The Frame parent of the dialog or null if the parent should be the Corona's MainWIndow
    */
    public CoronaDialog (Frame parent) {
        this(parent, ButtonBar.EMPTY, true);
    }

    /** Constructs a new modal CoronaDialog with given preset mode of ButtonBar.
    * @param mode The ButtonBar mode
    * @param parent The Frame parent of the dialog or null if the parent should be the Corona's MainWIndow
    */
    public CoronaDialog (Frame parent, int mode) {
        this(parent, mode, true);
    }

    /** Constructs a new CoronaDialog with given preset mode of ButtonBar.
    * @param parent The Frame parent of the dialog or null if the parent should be the Corona's MainWIndow
    * @param bb button bar to use
    * @param modal If the dialog is modal
    */
    public CoronaDialog (Frame parent, ButtonBar bb, boolean modal) {
        super(
            parent == null ? NbTopManager.getDefault ().getWindowManager ().getMainWindow () : parent,
            modal
        );

        getContentPane().setLayout(new BorderLayout());
        buttonBar = bb;
        buttonBar.addButtonBarListener(new ButtonBar.ButtonBarListener() {
                                           public void buttonPressed(ButtonBar.ButtonBarEvent evt) {
                                               CoronaDialog.this.buttonPressed(evt);
                                           }
                                       }
                                      );

        inside = new JPanel();
        inside.setLayout(new BorderLayout());
        getContentPane().add (inside, BorderLayout.CENTER);

        getContentPane().add (buttonBar,
                              (bb.getOrientation() == ButtonBar.HORIZONTAL) ? BorderLayout.SOUTH : BorderLayout.EAST);

    }

    /** Constructs a new CoronaDialog with given preset mode of ButtonBar.
    * @param parent The Frame parent of the dialog or null if the parent should be the Corona's MainWIndow
    * @param mode The ButtonBar mode
    * @param modal If the dialog is modal
    */
    public CoronaDialog (Frame parent, int mode, boolean modal) {
        this (parent, new ButtonBar (mode), modal);
    }

    /** Method that allows getting the inside pane that can be used
    * for adding/removing components to the Dialog.
    * This pane should be used *exclusively* for this - it is a
    * equivalent of getContentpane in swing windows.
    * @return The pane to be used for setting layout and adding components
    */
    public JPanel getCustomPane() {
        return inside;
    }

    /** A method that allows descendants to acquire a reference to ButtonBar
    * so that the ButtonBar can be initialized / altered.
    * @return The ButtonBar of this dialog
    */
    protected ButtonBar getButtonBar() {
        return buttonBar;
    }

    /** Default implementation of close() from TopWindow - returns true.
    *
    * Called when one wants to close the window and never
    * restore it anymore. Can initiate interaction with user and
    * he can stop closing.
    * In such a case the method should return <CODE>false</CODE> to
    * indicate the failure.
    *
    * @return true if the window has been successfully closed and false
    *   if the user has canceled the action.
    */
    public boolean close () {
        return true;
    }

    /** Every window must have its own help page. The suggested model
    * is described in <CODE>HelpCtx</CODE> and <CODE>Help</CODE> classes.
    * @return the help page reference
    */
    public HelpCtx getHelp () {
        return helpCtx;
    }

    /** Sets the help context for this window.
    * Every window must have its own help page. The suggested model
    * is described in <CODE>HelpCtx</CODE> and <CODE>Help</CODE> classes.
    * @return the help page reference
    * @see org.openide.Help
    * @see org.openide.HelpCtx
    */
    protected void setHelp(HelpCtx help) {
        helpCtx = help;
    }

    /** Reshapes the dialog so that is is sized according to its preferrddSize
    * and places it into the center of the screen
    */
    public void center() {
        // standard way how to place the dialog to the center of the screen
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
    }

    /** Called when user presses a button on the ButtonBar.
    * @param evt The ButtonBarEvent.
    */
    protected void buttonPressed(ButtonBar.ButtonBarEvent evt) {
    }

    /** <CODE>addNotify</CODE> redefined */
    public void addNotify() {
        super.addNotify();
        getRootPane().requestDefaultFocus(); // patch to make default button work
    }

    /** The "inside" part of the Dialog */ // NOI18N
    private JPanel inside;

    /** the help context for this dialog */
    private HelpCtx helpCtx = HelpCtx.DEFAULT_HELP;

    /** The ButtonBar of this dialog */
    private ButtonBar buttonBar;

}

/*
 * Log
 *  8    Gandalf   1.7         1/13/00  Jaroslav Tulach I18N
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  4    Gandalf   1.3         3/9/99   Jaroslav Tulach ButtonBar  
 *  3    Gandalf   1.2         2/4/99   Petr Hamernik   
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.33        --/--/98 Jan Formanek    buttons id changed to int (index)
 *  0    Tuborg    0.34        --/--/98 Jaroslav Tulach does not implement TopWindow interface
 *  0    Tuborg    0.36        --/--/98 Jan Jancura     parent in constructor may be null
 *  0    Tuborg    0.38        --/--/98 Jan Formanek    removed pack() from constructor
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    reflecting changes in ButtonBar - constructors and buttonPressed
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    were changed
 *  0    Tuborg    0.50        --/--/98 Jan Formanek    added getCustomPane() and removed redirection of add/setLayout
 *  0    Tuborg    0.52        --/--/98 Jan Jancura     added one constructor
 *  0    Tuborg    0.53        --/--/98 Petr Hamernik   JPanel instead of Panel
 *  0    Tuborg    0.54        --/--/98 Jaroslav Tulach added constructor that takes ButtonBar
 *  0    Tuborg    0.55        --/--/98 Jan Formanek    reflecting getMainWindow changes
 */
