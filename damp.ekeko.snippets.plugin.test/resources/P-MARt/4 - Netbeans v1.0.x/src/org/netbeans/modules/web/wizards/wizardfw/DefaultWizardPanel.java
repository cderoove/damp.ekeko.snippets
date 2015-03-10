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

package  org.netbeans.modules.web.wizards.wizardfw;

import org.netbeans.modules.web.util.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;

public class DefaultWizardPanel extends javax.swing.JPanel implements WizardDescriptor.Panel {

    /** Default preferred width of the panel - should be the same for all panels within one wizard */
    // protected static final int DEFAULT_WIDTH = 700;
    protected static final int DEFAULT_WIDTH = 650;
    /** Default preferred height of the panel - should be the same for all panels within one wizard */
    protected static final int DEFAULT_HEIGHT = 400;

    /**
     * @associates ChangeListener 
     */
    private Vector listvec;

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** @return preferred size of the wizard panel - it should be the same for all panels within one Wizard
    * so that the wizard dialog does not change its size when switching between panels */
    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public java.awt.Component getComponent() {
        return this;
    }

    public void readSettings (Object settings) {
    }

    public void storeSettings (Object settings) {
    }

    public boolean isValid () {
        return true;
    }

    /** Fire a {@link PropertyChangeEvent} to each listener.
    * @param propertyName the programmatic name of the property that was changed
    * @param oldValue the old value of the property
    * @param newValue the new value of the property
    */
    protected void fireChange() {
        Vector vecclone = (Vector)listvec.clone();
        Enumeration enum = vecclone.elements();
        ChangeEvent evt = new ChangeEvent(this);
        while(enum.hasMoreElements()) {
            ChangeListener elist = (ChangeListener)enum.nextElement();
            elist.stateChanged(evt);
        }
    }


    public void addChangeListener (ChangeListener listener) {
        if (listvec == null) listvec = new Vector(1);
        listvec.add(listener);
    }

    public void removeChangeListener (ChangeListener listener) {
        if (listvec != null) listvec.remove(listener);
    }

}
