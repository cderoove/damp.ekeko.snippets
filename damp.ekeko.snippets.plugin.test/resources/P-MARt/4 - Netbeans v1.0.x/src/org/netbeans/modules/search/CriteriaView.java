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

package org.netbeans.modules.search;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.explorer.propertysheet.*;

import org.openidex.search.*;

import org.netbeans.modules.search.res.*;


/**
 * Creates tabbed dialog based on passed criteria. 
 * Mark changed criteria as customized.
 *
 * <p>Listens: PROP_CUSTOMIZED on model
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CriteriaView extends javax.swing.JPanel implements PropertyChangeListener {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** This shows state of: */
    private CriteriaModel model;

    private JTabbedPane pane;

    /** Nb equalent for dialog. */
    private DialogDescriptor desc;
    private JButton okButton;
    private JButton cancelButton;

    /** Java equavalent. */
    private Dialog dlg;

    private int returnStatus = RET_CANCEL;

    /** Creates new form CriteriaView upon model */
    public CriteriaView(CriteriaModel model) {

        this.model = model;

        initComponents();

        setName(Res.text("TITLE_CUSTOMIZE")); // NOI18N

        //listen for "customized" // NOI18N
        model.addPropertyChangeListener(this);

        okButton = new JButton();
        okButton.setIcon (Res.icon("SEARCH")); // NOI18N
        okButton.setText (Res.text("BUTTON_SEARCH")); // NOI18N
        okButton.setEnabled(model.isCustomized());

        cancelButton = new JButton();
        cancelButton.setText (Res.text("BUTTON_CANCEL")); // NOI18N

        Object options[] = new Object[] {
                               okButton, cancelButton
                           };

        //create representing dialog descriptor
        desc = new DialogDescriptor(this, getName(), true,
                                    options, options[0],
                                    DialogDescriptor.BOTTOM_ALIGN, model.getHelpCtx(), new AL()
                                   );

    }

    /** Create dynammically set of tabs representing criteria customizers. */
    private void initComponents() {

        setLayout(new BorderLayout());

        pane = new JTabbedPane();
        pane.setModel(model.getTabModel());

        // for each criterion create one tab

        Iterator it = model.getCustomizers();

        while(it.hasNext()) {

            CriterionModel next = (CriterionModel) it.next();
            Component comp = new CriterionView(next);
            try {
                pane.add(comp);
            } catch (ArrayIndexOutOfBoundsException ex) {
                //TODO why? a bug in JTabbedPane?
            }
        }

        //    pane.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(4, 4, 4, 4)));
        add(pane, "Center"); // NOI18N
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus () {
        return returnStatus;
    }


    private void doClose (int retStatus) {
        model.removePropertyChangeListener(this);
        returnStatus = retStatus;

        dlg.setVisible(false);
        dlg.dispose();

    }

    /** Wrap itself to DialogDescriptor and show itself.
    */ // I have overwritten deprecated method -> Ignore such warning.
    public void show()  {
        dlg = TopManager.getDefault().createDialog(desc);
        dlg.setModal(true);
        dlg.pack();
        dlg.show();
    }



    public void propertyChange(final java.beans.PropertyChangeEvent event) {
        if (CriteriaModel.PROP_CUSTOMIZED.equals(event.getPropertyName())) {
            okButton.setEnabled(((Boolean)event.getNewValue()).booleanValue());
        } else if (event.getPropertyName().equals("help")) {
            desc.setHelpCtx( (HelpCtx) event.getNewValue());
        }

        for (int i = 0; i<pane.getTabCount(); i++ ) {
            pane.setTitleAt(i, model.getTabText(i));
            pane.setIconAt(i, null);
        }

    }

    /** Listen for provided options*/
    private class AL implements ActionListener {

        public void actionPerformed(final java.awt.event.ActionEvent e) {

            if (e.getSource() == okButton) {
                doClose(RET_OK);

            } else {
                doClose(RET_CANCEL);
            }
        }
    }

}


/*
* Log
*  14   Gandalf-post-FCS1.12.1.0    4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  13   Gandalf   1.12        1/17/00  Petr Kuzel      A layout bug fixed.
*  12   Gandalf   1.11        1/13/00  Radko Najman    I18N
*  11   Gandalf   1.10        1/10/00  Petr Kuzel      Buttons enabling.
*  10   Gandalf   1.9         1/6/00   Petr Kuzel      Tools menu position and 
*       debug removed.
*  9    Gandalf   1.8         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  8    Gandalf   1.7         1/4/00   Petr Kuzel      Bug hunting.
*  7    Gandalf   1.6         12/23/99 Petr Kuzel      Architecture improved.
*  6    Gandalf   1.5         12/20/99 Petr Kuzel      L&F fixes.
*  5    Gandalf   1.4         12/17/99 Petr Kuzel      Bundling.
*  4    Gandalf   1.3         12/16/99 Petr Kuzel      
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

