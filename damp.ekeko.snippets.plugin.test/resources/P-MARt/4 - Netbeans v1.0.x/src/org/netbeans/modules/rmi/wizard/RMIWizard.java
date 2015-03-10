/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi.wizard;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.event.*;

import org.openide.*;

/**
 *
 * @author  mryzl
 */
public class RMIWizard extends Object
    implements WizardDescriptor.Iterator, ActionListener, PropertyChangeListener {

    RMIWizardData data;

    /** Creates new RMIWizzard. */
    public RMIWizard() {
        this(new RMIWizardData());
    }

    /** Creates new RMIWizzard. */
    public RMIWizard(RMIWizardData data) {
        this(data,
             new WizardDescriptor.Panel[] {new RMITypePanel(), new RMINamePanel(), new RMIMethodsPanel(), new SelectExecutorPanel()}
            );
    }

    public RMIWizard(RMIWizardData data, WizardDescriptor.Panel[] panels) {
        this.data = data;
        this.panels = panels;
    }
    // Interface WizardDescriptor.Iterator

    /** Get the current panel.
    * @return the panel
    */
    public WizardDescriptor.Panel current () {
        return new AbstractWizardPanel();
    }

    /** Get the name of the current panel.
    * @return the name
    */
    public String name () {
        return "Wizard";
    }

    /** Test whether there is a next panel.
    * @return <code>true</code> if so
    */
    public boolean hasNext () {
        return false;
    }

    /** Test whether there is a previous panel.
    * @return <code>true</code> if so
    */
    public boolean hasPrevious () {
        return true;
    }

    /** Move to the next panel.
    * I.e. increment its index, need not actually change any GUI itself.
    * @exception NoSuchElementException if the panel does not exist
    */
    public void nextPanel () {
    }

    /** Move to the previous panel.
    * I.e. decrement its index, need not actually change any GUI itself.
    * @exception NoSuchElementException if the panel does not exist
    */
    public void previousPanel () {
    }

    /** Add a listener to changes of the current panel.
    * The listener is notified when the possibility to move forward/backward changes.
    * @param l the listener to add
    */
    public void addChangeListener (ChangeListener l) {
    }

    /** Remove a listener to changes of the current panel.
    * @param l the listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
    }

    // End of Interface WizardDescriptor.Iterator

    public void actionPerformed(ActionEvent ev) {
    }

    /** Implementation of property change listener.
    */
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(DialogDescriptor.PROP_VALUE)) {
            Object option = pce.getNewValue();
            if (option == WizardDescriptor.FINISH_OPTION || option == WizardDescriptor.CANCEL_OPTION) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }

    Dialog dialog;
    WizardDescriptor.Panel panels[];

    /**
    */
    public void run() {
        WizardDescriptor descriptor = new WizardDescriptor(panels, data);
        descriptor.setTitleFormat (new java.text.MessageFormat ("RMI Wizard [{1}]"));
        descriptor.addPropertyChangeListener(this);

        dialog = TopManager.getDefault().createDialog(descriptor);
        dialog.show();
        if (descriptor.getValue() == WizardDescriptor.FINISH_OPTION) {
            try {
                data.getGenerator().generate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /** For debugging.
     */
    public static void main(String[] args) {
        new RMIWizard().run();
    }

}

/*
* <<Log>>
*  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         8/16/99  Martin Ryzl     debug prints were removed
*  7    Gandalf   1.6         7/28/99  Martin Ryzl     added selection of 
*       executor
*  6    Gandalf   1.5         7/27/99  Martin Ryzl     new version of generator 
*       is working
*  5    Gandalf   1.4         7/27/99  Martin Ryzl     
*  4    Gandalf   1.3         7/22/99  Martin Ryzl     first working version
*  3    Gandalf   1.2         7/20/99  Martin Ryzl     
*  2    Gandalf   1.1         7/19/99  Martin Ryzl     
*  1    Gandalf   1.0         7/19/99  Martin Ryzl     
* $ 
*/ 

