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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;

import java.text.MessageFormat;

import org.openide.*;
import org.openide.util.*;

//NB import com.sun.jasper.wizardfw.netbeans.api.*;
//NB import com.sun.jasper.wizardfw.netbeans.util.*;


public class DefaultWizard  implements WizardDescriptor.Iterator {

    protected WizardDescriptor descriptor;

    private java.awt.Dialog dialog;

    private Object data;

    //// Iterator impl

    /** Array of items.
       */

    protected WizardDescriptor.Panel[] panels;

    /** Index into the array
    */
    protected int index;


    /** Constructor */

    public DefaultWizard(WizardDescriptor.Panel[] panels, Object data) {
        this.panels = panels;
        index = 0;
        this.data = data;
    }

    public DefaultWizard(WizardDescriptor.Panel[] panels) {
        this(panels,new Object());
    }

    /// Array Iterator Implementation

    /* The current panel.
    */
    public WizardDescriptor.Panel current () {
        return panels[index];
    }

    /* Current name of the panel */
    public String name () {
        Object[] args = {
            new Integer (index + 1),
            new Integer (panels.length)
        };
        MessageFormat mf = new MessageFormat (NbBundle.getBundle (WizardDescriptor.class).getString ("CTL_ArrayIteratorName"));	 //NOI18N
        return mf.format (args);
    }

    /* Is there a next panel?
    * @return true if so
    */
    public boolean hasNext () {
        return index < panels.length - 1;
    }

    /* Is there a previous panel?
    * @return true if so
    */
    public boolean hasPrevious () {
        return index > 0;
    }

    /* Moves to the next panel.
    * @exception NoSuchElementException if the panel does not exist
    */
    public synchronized void nextPanel () {
        if (index + 1 == panels.length) throw new java.util.NoSuchElementException ();
        index++;
    }

    /* Moves to previous panel.
    * @exception NoSuchElementException if the panel does not exist
    */
    public synchronized void previousPanel () {
        if (index == 0) throw new java.util.NoSuchElementException ();
        index--;
    }

    /* Ignores the listener, there are no changes in order of panels.
    */
    public void addChangeListener (ChangeListener l) {
    }

    /* Ignored.
    */
    public void removeChangeListener (ChangeListener l) {
    }

    //// Wizard methods

    public Object getData() {
        return data;
    }

    public boolean onFinish() {
        // Debug.println("Default Finish Called");
        return true;
    }

    public boolean onCancel() {
        // Debug.println("Default Cancel Called");
        return true;
    }

    /** Returns whether the wizard has been completed and the code should be generated. */
    public void executeWizard() {

        PropertyChangeListener listener = new PropertyChangeListener() {
                                              public void propertyChange(PropertyChangeEvent event) {
                                                  if (event.getPropertyName().equals(DialogDescriptor.PROP_VALUE)) {
                                                      Object option = event.getNewValue();
                                                      if (option == WizardDescriptor.FINISH_OPTION || option == WizardDescriptor.CANCEL_OPTION) {
                                                          boolean done = false;
                                                          if(option == WizardDescriptor.FINISH_OPTION) {
                                                              done = onFinish();
                                                          }else {
                                                              done = onCancel();
                                                          }
                                                          if(done) {
                                                              // Debug.println("Closing the Wizard Dialog");
                                                              dialog.setVisible(false);
                                                              dialog.dispose();
                                                          }else {
                                                              // Debug.println("Wizard is Not Done Yet!");
                                                          }
                                                      }
                                                  }
                                              }
                                          };

        descriptor = new WizardDescriptor(this, data);
        descriptor.setOptions (new Object[] { WizardDescriptor.PREVIOUS_OPTION, WizardDescriptor.NEXT_OPTION, WizardDescriptor.FINISH_OPTION, NotifyDescriptor.CANCEL_OPTION });
        descriptor.setAdditionalOptions (new Object[] { });
        descriptor.setClosingOptions (new Object[] { });

        //NB this should come from the wizardfw pacakge bundle
        //java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        // descriptor.setTitleFormat (new java.text.MessageFormat ( resBundle.getString("JBW_WizardTitle")));
        descriptor.setTitleFormat (new java.text.MessageFormat ("{1}"));		 // NOI18N
        // descriptor.setTitleFormat (new java.text.MessageFormat (" Wizard Page [{1}]"));

        descriptor.addPropertyChangeListener(listener);
        dialog = TopManager.getDefault().createDialog(descriptor);
        dialog.show();
    }

}


