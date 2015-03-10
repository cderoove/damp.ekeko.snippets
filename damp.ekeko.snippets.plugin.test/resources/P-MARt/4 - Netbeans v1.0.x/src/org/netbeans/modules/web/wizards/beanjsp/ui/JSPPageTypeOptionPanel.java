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

package  org.netbeans.modules.web.wizards.beanjsp.ui;

import javax.swing.*;
import java.beans.*;

import java.awt.*;
import java.io.File;

import org.netbeans.modules.web.wizards.beanjsp.model.*;
import org.netbeans.modules.web.wizards.wizardfw.*;
import org.netbeans.modules.web.wizards.beanjsp.ide.netbeans.*;

import org.netbeans.modules.web.util.*;
import org.netbeans.modules.web.wizards.beanjsp.util.*;

import org.openide.util.*;

public class JSPPageTypeOptionPanel extends StandardWizardPanel {

    public JSPPageTypeOptionPanel() {
        super();
        initComponents ();

    }

    /*public HelpCtx getHelp () {
      return new HelpCtx (JSPPageTypeOptionPanel.class);
}
    */
    private void initComponents () {

        //// get resource bundle
        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        this.setTopMessage(resBundle.getString("JBW_PageTypeOptionMsg"));								 // NOI18N

        //// create components
        pageTypeQuestionLabel = new MultiLineLabel(resBundle.getString("JBW_pageTypeQuetionMsg"));	 // NOI18N
        standardPageRadioB = new JRadioButton(resBundle.getString("JBW_standardPage"));				 // NOI18N
        errorPageRadioB = new JRadioButton(resBundle.getString("JBW_errorPage"));						 // NOI18N
        ButtonGroup group = new ButtonGroup();
        group.add(standardPageRadioB);
        group.add(errorPageRadioB);

        //// layout and add components
        arrangeComponents();

        setDefaults();
    }

    //// layout components in
    private void arrangeComponents() {
        arrangeCompsWithGridBag();
    }

    private void addGridBagComponent(Container parent, Component comp,
                                     int gridx, int gridy, int gridwidth, int gridheight,
                                     double weightx, double weighty,
                                     int anchor, int fill,
                                     Insets insets, int ipadx, int ipady
                                    ) {
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridx = gridx;
        cons.gridy = gridy;
        cons.gridwidth = gridwidth;
        cons.gridheight = gridheight;
        cons.weightx = weightx;
        cons.weighty = weighty;
        cons.anchor = anchor;
        cons.fill = fill;
        cons.insets = insets;
        cons.ipadx = ipadx;
        cons.ipady = ipady;
        parent.add(comp,cons);
    }

    private void arrangeCompsWithGridBag() {

        this.contentPane.setLayout(new GridBagLayout());

        Component topGlue = Box.createGlue();
        Component bottomGlue = Box.createGlue();

        addGridBagComponent(this.contentPane,topGlue,
                            0,0,2,1,
                            100,100,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);


        addGridBagComponent(this.contentPane,pageTypeQuestionLabel,
                            0,1,2,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

        addGridBagComponent(this.contentPane,standardPageRadioB,
                            0,2,1,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,20,2,2),5,5	);

        addGridBagComponent(this.contentPane,errorPageRadioB,
                            0,3,1,1,
                            100,0,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,20,2,2),5,5	);


        addGridBagComponent(this.contentPane,bottomGlue,
                            0,4,2,1,
                            100,100,
                            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                            new Insets(2,2,2,2),5,5	);

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables

    // private javax.swing.JLabel pageTypeQuestionLabel;
    private MultiLineLabel pageTypeQuestionLabel;
    private javax.swing.JRadioButton standardPageRadioB;
    private javax.swing.JRadioButton errorPageRadioB;

    // model and other variables



    public void setDefaults() {
        boolean yes = JSPPageWizard.simpleJSPPage.isErrorPage();
        standardPageRadioB.setSelected(!yes);
        errorPageRadioB.setSelected(yes);
    }


    public void readSettings(Object setting) {
        boolean yes = JSPPageWizard.simpleJSPPage.isErrorPage();
        errorPageRadioB.setSelected(yes);
    }

    public void storeSettings(Object setting) {
        JSPPageWizard.simpleJSPPage.setErrorPage(errorPageRadioB.isSelected());
    }


    // ---------------------------------------------------------------------------------------
    // WizardDescriptor.Panel implementation

    public static void main(String[] args) {
        if(Debug.TEST) {
            JFrame testFrame = new JFrame("This is Test Frame");			 // NOI18N
            testFrame.getContentPane().add(new JSPPageTypeOptionPanel(),SwingConstants.CENTER);
            testFrame.setSize(600,500);
            testFrame.pack();
            testFrame.show();
        }

    }

}