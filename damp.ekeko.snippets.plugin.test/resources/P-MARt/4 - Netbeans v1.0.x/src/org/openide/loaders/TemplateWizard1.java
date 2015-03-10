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

package org.openide.loaders;

import java.io.IOException;
import java.util.*;

import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;

import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.WizardDescriptor;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.explorer.view.*;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/** Dialog that can be used in create from template.
*
* @author  Jaroslav Tulach
* @version 
*/
final class TemplateWizard1 extends javax.swing.JPanel
    implements DataFilter, WizardDescriptor.Panel {
    /** listener to changes in the wizard */
    private ChangeListener listener;
    /** selected template */
    private DataObject template;
    /** action listener to store action associated with ENTER */
    private java.awt.event.ActionListener previousEnterAction;
    /** selection model */
    private DefaultTreeSelectionModel selectionModel;

    /** Creates new form NewFromTemplatePanel */
    public TemplateWizard1 () {
        initComponents ();

        setName (org.openide.util.NbBundle.getBundle(TemplateWizard1.class).getString("LAB_TemplateChooserPanelName"));

        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));
        templatesPanel.setBorder (new javax.swing.border.CompoundBorder(
                                      new javax.swing.border.TitledBorder(org.openide.util.NbBundle.getBundle(TemplateWizard1.class).getString("LAB_SelectTemplateBorder")),
                                      new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))
                                  )
                                 );

        browser.setBorder (new javax.swing.border.CompoundBorder(
                               new javax.swing.border.TitledBorder(org.openide.util.NbBundle.getBundle(TemplateWizard1.class).getString("LAB_TemplateDescriptionBorder")),
                               new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8))
                           )
                          );

        selectionModel = new DefaultTreeSelectionModel ();
        selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        templatesTree.setSelectionModel(selectionModel);
        templatesModel.setNode(createTemplatesNode ());
        templatesTree.setSelectionPath(new TreePath (templatesModel.getRoot ()));

        selectionModel.addTreeSelectionListener (new javax.swing.event.TreeSelectionListener () {
                    public void valueChanged (javax.swing.event.TreeSelectionEvent evt) {
                        templatesTreeValueChanged (evt);
                    }
                });

        // browser
        noBrowser.setText (org.openide.util.NbBundle.getBundle(TemplateWizard1.class).getString("MSG_NoDescription"));

        java.awt.CardLayout card = (java.awt.CardLayout)browserPanel.getLayout();
        card.show (browserPanel, "noBrowser"); // NOI18N
    }

    /** When requested focus => transferred to tree.
    */
    public void requestFocus() {
        templatesTree.requestFocus ();
    }

    /** Preffered size */
    public java.awt.Dimension getPreferredSize() {
        return TemplateWizard.PREF_DIM;
    }

    /** Creates node that displays all templates.
    */
    private Node createTemplatesNode () {
        DataFolder templates = TopManager.getDefault().getPlaces().folders().templates();

        Children ch = templates.createNodeChildren(this);

        return new FilterNode (templates.getNodeDelegate(), ch);
    }

    /** Updates description to reflect the one associated with given object.
    * @param obj object
    */
    private void updateDescription (DataObject obj) {
        if (obj == null) {
            obj = TopManager.getDefault ().getPlaces ().folders ().templates ();
        }

        java.net.URL url = TemplateWizard.getDescription (obj);
        java.awt.CardLayout card = (java.awt.CardLayout)browserPanel.getLayout();
        if (url != null) {
            browser.setURL(url);
            card.show (browserPanel, "browser"); // NOI18N
        } else {
            card.show (browserPanel, "noBrowser"); // NOI18N
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        templatesModel = new TemplatesModel ();
        templatesPanel = new javax.swing.JPanel ();
        templatesScroll = new javax.swing.JScrollPane ();
        templatesTree = new javax.swing.JTree ();
        browserPanel = new javax.swing.JPanel ();
        browser = new org.openide.awt.HtmlBrowser ();
        noBrowser = new javax.swing.JLabel ();

        setLayout (new java.awt.GridLayout (1, 2, 8, 0));

        templatesPanel.setLayout (new java.awt.BorderLayout ());


        templatesTree.setModel (templatesModel);
        templatesTree.setCellRenderer (new org.openide.explorer.view.NodeRenderer ());

        templatesScroll.setViewportView (templatesTree);

        templatesPanel.add (templatesScroll, java.awt.BorderLayout.CENTER);


        add (templatesPanel);

        browserPanel.setLayout (new java.awt.CardLayout ());

        browser.setToolbarVisible (false);
        browser.setStatusLineVisible (false);
        browser.setName ("browser"); // NOI18N

        browserPanel.add (browser, "browser"); // NOI18N

        noBrowser.setHorizontalAlignment (javax.swing.SwingConstants.CENTER);

        browserPanel.add (noBrowser, "noBrowser"); // NOI18N


        add (browserPanel);

    }//GEN-END:initComponents

    private void nameFocusGained (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFocusGained
    }//GEN-LAST:event_nameFocusGained

    private void templatesTreeValueChanged (javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_templatesTreeValueChanged
        if (listener != null) {
            listener.stateChanged (new ChangeEvent (this));

            updateDescription (template);
        }
    }//GEN-LAST:event_templatesTreeValueChanged


    private void packagesListValueChanged (javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_packagesListValueChanged
    }//GEN-LAST:event_packagesListValueChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.openide.explorer.view.NodeTreeModel templatesModel;
    private javax.swing.JPanel templatesPanel;
    private javax.swing.JScrollPane templatesScroll;
    private javax.swing.JTree templatesTree;
    private javax.swing.JPanel browserPanel;
    private org.openide.awt.HtmlBrowser browser;
    private javax.swing.JLabel noBrowser;
    // End of variables declaration//GEN-END:variables



    /** Should the data object be displayed or not?
    * @param obj the data object
    * @return <CODE>true</CODE> if the object should be displayed,
    *    <CODE>false</CODE> otherwise
    */
    public boolean acceptDataObject(DataObject obj) {
        return (obj instanceof DataFolder) || obj.isTemplate();
    }

    //
    // Wizard
    //

    /** Get the component displayed in this panel.
    * @return the component
    */
    public java.awt.Component getComponent () {
        return this;
    }

    /** Help for this panel.
    * @return the help or <code>null</code> if no help is supplied
    */
    public HelpCtx getHelp () {
        return new HelpCtx (TemplateWizard1.class);
    }

    /** Provides the wizard panel with the current data--either
    * the default data or already-modified settings, if the user used the previous and/or next buttons.
    * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
    * @param settings the object representing wizard panel state, as originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}
    */
    public void readSettings (Object settings) {
        TemplateWizard wizard = (TemplateWizard)settings;

        template = wizard.getTemplate ();
        updateDescription (template);

        // now try to find out the path
        // a bit ugly code to do that
        DataObject obj = template;
        DataObject stop = TopManager.getDefault().getPlaces().folders().templates();
        LinkedList ll = new LinkedList ();
        for (;;) {
            if (obj == null) {
                // seems that the template is not one of templates
                return;
            }

            if (obj == stop) {
                // the last object found
                break;
            }

            String key = obj.getNodeDelegate().getName ();
            ll.addFirst(key);
            obj = obj.getFolder();
        }

        // go thru all the nodes and find
        Node node = Visualizer.findNode (templatesModel.getRoot());
        java.util.ListIterator it = ll.listIterator();
        while (it.hasNext()) {
            String name = (String)it.next ();
            node = node.getChildren ().findChild (name);
            if (node == null) {
                // end it
                return;
            }

            Object v = Visualizer.findVisualizer(node);
            it.set (v);
        }
        ll.addFirst(templatesModel.getRoot ());

        Object[] path = ll.toArray();

        final TreePath tp = new TreePath (path);

        RequestProcessor.postRequest(new Runnable () {
                                         public void run () {
                                             selectionModel.setSelectionPath (tp);
                                             updateDescription (template);
                                         }
                                     }, 300);
    }

    /** Provides the wizard panel with the opportunity to update the
    * settings with its current customized state.
    * Rather than updating its settings with every change in the GUI, it should collect them,
    * and then only save them when requested to by this method.
    * Also, the original settings passed to {@link #readSettings} should not be modified (mutated);
    * rather, the (copy) passed in here should be mutated according to the collected changes.
    * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
    * @param settings the object representing a settings of the wizard
    */
    public void storeSettings (Object settings) {
        if (template != null) {
            TemplateWizard wizard = (TemplateWizard)settings;
            wizard.setTemplateImpl (template, false);
        }
    }

    /** Test whether the panel is finished and it is safe to proceed to the next one.
    * If the panel is valid, the "Next" (or "Finish") button will be enabled.
    * @return <code>true</code> if the user has entered satisfactory information
    */
    public boolean isValid () {
        boolean enable = false;
        TreePath tp = templatesTree.getSelectionPath();
        if (tp != null) {
            Node n = Visualizer.findNode (tp.getLastPathComponent());
            template = (DataObject)n.getCookie (DataObject.class);
            enable = template != null && template.isTemplate();
        }

        javax.swing.KeyStroke ks = javax.swing.KeyStroke.getKeyStroke(
                                       java.awt.event.KeyEvent.VK_ENTER, 0
                                   );

        if (enable) {
            // ENTER should invoke default button
            if (previousEnterAction == null) {
                previousEnterAction = templatesTree.getActionForKeyStroke(ks);
            }
            templatesTree.unregisterKeyboardAction(ks);
        } else {
            if (previousEnterAction != null) {
                templatesTree.registerKeyboardAction(
                    previousEnterAction, ks, WHEN_FOCUSED
                );
                previousEnterAction = null;
            }
        }

        return enable;
    }

    /** Add a listener to changes of the panel's validity.
    * @param l the listener to add
    * @see #isValid
    */
    public void addChangeListener (ChangeListener l) {
        if (listener != null) throw new IllegalStateException ();
        listener = l;
    }

    /** Remove a listener to changes of the panel's validity.
    * @param l the listener to remove
    */
    public void removeChangeListener (ChangeListener l) {
        listener = null;
    }


    /** Model for displaying only objects till template.
    */
    private static final class TemplatesModel extends NodeTreeModel {
        public int getChildCount (Object o) {
            Node n = Visualizer.findNode(o);
            DataObject obj = (DataObject)n.getCookie (DataObject.class);

            return obj == null || obj.isTemplate () ? 0 : super.getChildCount (o);
        }

        public boolean  isLeaf (Object o) {
            Node n = Visualizer.findNode(o);
            DataObject obj = (DataObject)n.getCookie (DataObject.class);

            return obj == null || obj.isTemplate ();
        }
    }
}