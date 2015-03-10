/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.io.IOException;
import java.util.Enumeration;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.actions.InstantiateAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.WeakListener;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class ResourceBundlePanel extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {

    static final long serialVersionUID =7271261496973809990L;
    /** Creates new form ResourceBundlePanel */
    public ResourceBundlePanel() {
        this(true);
    }

    public ResourceBundlePanel(boolean initComps) {
        if (initComps)
            initComponents ();
        HelpCtx.setHelpIDString (this, ResourceBundlePanel.class.getName ());
    }


    /** Returns the current value. Tries to "validate" this value, i.e. inserts or updates the property in the
    * property bundle. Throws IllegalStateException if the value could not be made valid. */
    public Object getPropertyValue() throws IllegalStateException {
        // try to fix the value first
        value.tryToUpdate();

        PropertiesModule.setLastBundleUsed(value.getResourceBundle());
        String myValue = value.getPropertyValue();
        if (myValue == null)
            throw new IllegalStateException();
        else {
            return value;
        }
    }

    /** Simply returns the current value. */
    public ResourceBundleString getValue() {
        return value;
    }

    public void setValue(ResourceBundleString str) {
        if (str == null)
            throw new IllegalArgumentException();
        value = str;
        pcl = new PropertyChangeListener() {
                  public void propertyChange(PropertyChangeEvent evt) {
                      boolean bc = (evt == null || evt.getPropertyName().equals("resourceBundle"));
                      if (bc) {
                          bList = new PropertyBundleListener() {
                                      public void bundleChanged(PropertyBundleEvent evt) {
                                          switch (evt.getChangeType()) {
                                          case PropertyBundleEvent.CHANGE_STRUCT:
                                          case PropertyBundleEvent.CHANGE_ALL:
                                              updateStatus(true);
                                              break;
                                          case PropertyBundleEvent.CHANGE_FILE:
                                          case PropertyBundleEvent.CHANGE_ITEM:
                                              break;
                                          }
                                      }
                                  };
                          value.getResourceBundle().getBundleStructure().addPropertyBundleListener(
                              new WeakListenerPropertyBundle(bList));
                      }
                      updateStatus(bc);
                  }
              };
        value.addPropertyChangeListener(new WeakListener.PropertyChange(pcl));
        updateStatus(true);
    }

    public void updateStatus(boolean bundleChanged) {
        if (updatingStatus)
            return;
        if (bundleChanged) {
            updatingStatus = true;
            bundleText_().setText(getBundleName(value.getResourceBundle()));
            String saveKey = null;
            String saveValue = value.getDefaultValue();
            if (value.getPropertyValue() == null)
                saveKey = value.getKey();
            fillKeys();
            if (saveKey != null) {
                value.setKey(saveKey);
                value.setDefaultValue(saveValue);
            }
            updatingStatus = false;
        }
        if (value.getKey() != null)
            keyComboBox_().setSelectedItem(value.getKey());
        fillValue();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(8, 8, 8, 8)));

        resourcePanel = new javax.swing.JPanel ();
        resourcePanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints2;
        resourcePanel.setBorder (new javax.swing.border.TitledBorder(
                                     new javax.swing.border.EtchedBorder(), "Resource Bundle"));

        bundleText = new javax.swing.JTextField ();
        bundleText.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              bundleTextActionPerformed (evt);
                                          }
                                      }
                                     );
        bundleText.addFocusListener (new java.awt.event.FocusAdapter () {
                                         public void focusLost (java.awt.event.FocusEvent evt) {
                                             bundleTextFocusLost (evt);
                                         }
                                     }
                                    );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints2.weightx = 1.0;
        resourcePanel.add (bundleText, gridBagConstraints2);

        browseButton = new javax.swing.JButton ();
        browseButton.setPreferredSize (new java.awt.Dimension(85, 27));
        browseButton.setText (org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("browseButton.Browse"));
        browseButton.setLabel (org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("browseButton.Browse"));
        browseButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                browseButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (8, 0, 8, 8);
        resourcePanel.add (browseButton, gridBagConstraints2);

        newButton = new javax.swing.JButton ();
        newButton.setPreferredSize (new java.awt.Dimension(85, 27));
        newButton.setText (org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("newButton.New"));
        newButton.setLabel (org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("newButton.New"));
        newButton.addActionListener (new java.awt.event.ActionListener () {
                                         public void actionPerformed (java.awt.event.ActionEvent evt) {
                                             newButtonActionPerformed (evt);
                                         }
                                     }
                                    );

        gridBagConstraints2 = new java.awt.GridBagConstraints ();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets (0, 0, 8, 8);
        resourcePanel.add (newButton, gridBagConstraints2);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        add (resourcePanel, gridBagConstraints1);

        stringPanel = new javax.swing.JPanel ();
        stringPanel.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints3;
        stringPanel.setBorder (new javax.swing.border.TitledBorder(
                                   new javax.swing.border.EtchedBorder(), "String"));

        keyLabel = new javax.swing.JLabel ();
        keyLabel.setText (org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("keyLabel.Key"));

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        stringPanel.add (keyLabel, gridBagConstraints3);

        keyComboBox = new javax.swing.JComboBox ();
        keyComboBox.setEditable (true);
        keyComboBox.addActionListener (new java.awt.event.ActionListener () {
                                           public void actionPerformed (java.awt.event.ActionEvent evt) {
                                               keyComboBoxActionPerformed (evt);
                                           }
                                       }
                                      );

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets (0, 0, 8, 8);
        gridBagConstraints3.weightx = 1.0;
        stringPanel.add (keyComboBox, gridBagConstraints3);

        valueLabel = new javax.swing.JLabel ();
        valueLabel.setText (org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("valueLabel.Value"));

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridheight = 0;
        gridBagConstraints3.insets = new java.awt.Insets (0, 8, 0, 8);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
        stringPanel.add (valueLabel, gridBagConstraints3);

        valueScrollPane = new javax.swing.JScrollPane ();

        valueTextArea = new javax.swing.JTextArea ();
        valueTextArea.setColumns (30);
        valueTextArea.setRows (5);
        valueTextArea.addFocusListener (new java.awt.event.FocusAdapter () {
                                            public void focusLost (java.awt.event.FocusEvent evt) {
                                                valueTextAreaFocusLost (evt);
                                            }
                                        }
                                       );

        valueScrollPane.setViewportView (valueTextArea);

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.gridwidth = 0;
        gridBagConstraints3.gridheight = -1;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets (0, 0, 8, 8);
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        stringPanel.add (valueScrollPane, gridBagConstraints3);

        paramsButton = new javax.swing.JButton ();
        paramsButton.setText (org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("ResourceBundlePanel.paramsButton.text"));
        paramsButton.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent evt) {
                                                paramsButtonActionPerformed (evt);
                                            }
                                        }
                                       );

        gridBagConstraints3 = new java.awt.GridBagConstraints ();
        gridBagConstraints3.insets = new java.awt.Insets (0, 0, 8, 8);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        stringPanel.add (paramsButton, gridBagConstraints3);


        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridwidth = 0;
        gridBagConstraints1.gridheight = -1;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add (stringPanel, gridBagConstraints1);

    }//GEN-END:initComponents

    private void paramsButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paramsButtonActionPerformed
        paramsButtonActionPerformed_(evt);
    }//GEN-LAST:event_paramsButtonActionPerformed

    protected void paramsButtonActionPerformed_ (java.awt.event.ActionEvent evt) {
        final Dialog[] dial = new Dialog[1];
        final ParamsPanel pPanel = new ParamsPanel();
        String oComment = value.getDefaultComment();
        if (oComment == null)
            oComment = value.getRealComment();
        pPanel.setComment(oComment);
        pPanel.setArguments(value.getArguments());
        DialogDescriptor dd = new DialogDescriptor(
                                  pPanel,
                                  org.openide.util.NbBundle.getBundle(ResourceBundlePanel.class).getString("CTL_ParamsPanelTitle"),
                                  true,
                                  DialogDescriptor.OK_CANCEL_OPTION,
                                  DialogDescriptor.OK_OPTION,
                                  new ActionListener() {
                                      public void actionPerformed(ActionEvent ev) {
                                          if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                                              value.setArguments(pPanel.getArguments());
                                              String nComment = pPanel.getComment();
                                              if (!nComment.equals(value.getRealComment()))
                                                  value.setDefaultComment(nComment);
                                              dial[0].setVisible(false);
                                              dial[0].dispose();
                                          } else if (ev.getSource() == DialogDescriptor.CANCEL_OPTION) {
                                              dial[0].setVisible(false);
                                              dial[0].dispose();
                                          }
                                      }
                                  }
                              );
        dial[0] = TopManager.getDefault().createDialog(dd);
        dial[0].show();
    }

    private void newButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
        newButtonActionPerformed_(evt);
        // Add your handling code here:
    }//GEN-LAST:event_newButtonActionPerformed

    /** Instantiate a template object.
    * Asks user for the target file's folder and creates the file.
    * @param obj the template to use
    * @return the generated DataObject
    * @exception UserCancelException if the user cancels the action
    * @exception IOException on I/O error
    * @see DataObject#createFromTemplate
    */
    public static PropertiesDataObject instantiateTemplate (DataObject obj) throws IOException {
        // Create component for for file name input
        ObjectNameInputPanel p = new ObjectNameInputPanel ();

        DataFilter filter = new DataFilter () {
                                public boolean acceptDataObject (DataObject oj) {
                                    return oj instanceof DataFolder;
                                }
                            };
        Node ds = TopManager.getDefault ().getPlaces ().nodes ().repository (filter);

        ResourceBundle bundle = NbBundle.getBundle (InstantiateAction.class);

        // selects one folder from data systems
        DataFolder df = (DataFolder)TopManager.getDefault ().getNodeOperation ().select (
                            bundle.getString ("CTL_Template_Dialog_Title"),
                            bundle.getString ("CTL_Template_Dialog_RootTitle"),
                            ds, new NodeAcceptor () {
                                public boolean acceptNodes (Node[] nodes) {
                                    if (nodes == null || nodes.length != 1) {
                                        return false;
                                    }
                                    DataFolder cookie = (DataFolder)nodes[0].getCookie (DataFolder.class);
                                    return cookie != null && !cookie.getPrimaryFile ().isReadOnly ();
                                }
                            }, p
                        )[0].getCookie(DataFolder.class);

        String name = p.getText ();
        DataObject newObject;

        if (name.equals ("")) {
            newObject = obj.createFromTemplate (df);
        } else {
            newObject = obj.createFromTemplate (df, name);
        }

        try {
            return (PropertiesDataObject)newObject;
        }
        catch (ClassCastException e) {
            throw new UserCancelException();
        }
    }

    protected void newButtonActionPerformed_ (java.awt.event.ActionEvent evt) {
        try {
            FileSystem defaultFs = TopManager.getDefault().getRepository().getDefaultFileSystem();
            FileObject fo = defaultFs.findResource("Templates/Other/properties.properties");
            if (fo == null)
                throw new IOException(NbBundle.getBundle(ResourceBundlePanel.class).getString("EXC_TemplateNotFound"));
            DataObject templ = null;
            try {
                templ = DataObject.find(fo);
            }
            catch (DataObjectNotFoundException e) {
                throw new IOException(NbBundle.getBundle(ResourceBundlePanel.class).getString("EXC_TemplateNotFound"));
            }
            PropertiesDataObject newDo = instantiateTemplate(templ);
            if (newDo != null) {
                changeBundle(newDo);
            }
        }
        catch (UserCancelException e) {
            // that's fine
        }
        catch (IOException e) {
            TopManager.getDefault().notifyException(e);
        }
    }

    protected void changeBundle(PropertiesDataObject newBundle) {
        value.setResourceBundle(newBundle);
    }

    private void valueTextAreaFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueTextAreaFocusLost
        valueTextAreaFocusLost_(evt);
    }//GEN-LAST:event_valueTextAreaFocusLost

    protected void valueTextAreaFocusLost_ (java.awt.event.FocusEvent evt) {
        if ((value.getPropertyValue() == null) || (!value.getPropertyValue().equals(valueTextArea_().getText())))
            value.setDefaultValue(valueTextArea_().getText());
    }

    private void bundleTextFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_bundleTextFocusLost
        bundleTextFocusLost_(evt);
    }//GEN-LAST:event_bundleTextFocusLost

    protected void bundleTextFocusLost_ (java.awt.event.FocusEvent evt) {
        PropertiesDataObject dObj = findDataObject(bundleText_().getText());
        if (dObj != null) {
            changeBundle(dObj);
        }
        else
            bundleText_().setText(getBundleName(value.getResourceBundle()));
    }

    private void editButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        // add it here
    }//GEN-LAST:event_editButtonActionPerformed

    private void keyComboBoxActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyComboBoxActionPerformed
        keyComboBoxActionPerformed_(evt);
    }//GEN-LAST:event_keyComboBoxActionPerformed

    protected void keyComboBoxActionPerformed_ (java.awt.event.ActionEvent evt) {
        String key = (String)keyComboBox_().getSelectedItem();
        if (key != null) {
            value.setKey(key);
            updateStatus(false);
        }
    }

    private void browseButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        browseButtonActionPerformed_(evt);
    }//GEN-LAST:event_browseButtonActionPerformed

    protected void browseButtonActionPerformed_ (java.awt.event.ActionEvent evt) {
        DataFilter filter = new DataFilter () {
                                public boolean acceptDataObject (DataObject oj) {
                                    return (oj instanceof DataFolder || oj instanceof PropertiesDataObject);
                                }
                            };
        Node ds = TopManager.getDefault ().getPlaces ().nodes ().repository (filter);

        // selects one PropertiesDataObject
        try {
            PropertiesDataObject dObj = (PropertiesDataObject)TopManager.getDefault ().getNodeOperation ().select (
                                            NbBundle.getBundle(ResourceBundlePanel.class).getString ("CTL_SelectPropDO_Dialog_Title"),
                                            NbBundle.getBundle(ResourceBundlePanel.class).getString ("CTL_SelectPropDO_Dialog_RootTitle"),
                                            ds, new NodeAcceptor () {
                                                public boolean acceptNodes (Node[] nodes) {
                                                    if (nodes == null || nodes.length != 1) {
                                                        return false;
                                                    }
                                                    Node.Cookie cookie = (nodes[0].getCookie(DataObject.class));
                                                    if (cookie == null)
                                                        return false;
                                                    return (cookie instanceof PropertiesDataObject);
                                                }
                                            }
                                        )[0].getCookie(DataObject.class);
            if (dObj != null) {
                changeBundle(dObj);
            }
        }
        catch (UserCancelException e) {
            // that's fine
        }
    }

    private void bundleTextActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bundleTextActionPerformed
        bundleTextActionPerformed_(evt);
    }//GEN-LAST:event_bundleTextActionPerformed

    protected void bundleTextActionPerformed_ (java.awt.event.ActionEvent evt) {
        PropertiesDataObject dObj = findDataObject(bundleText_().getText());
        if (dObj != null) {
            changeBundle(dObj);
        }
        else
            bundleText_().setText(getBundleName(value.getResourceBundle()));
    }

    private void fillKeys() {
        String selected = value.getKey();
        if (keyComboBox_().getItemCount() != 0)
            keyComboBox_().removeAllItems();
        if (value.getResourceBundle() != null) {
            BundleStructure bs = value.getResourceBundle().getBundleStructure();
            if (bs != null) {
                String keys[] = bs.getKeys();
                for (int i = 0; i < keys.length; i++)
                    keyComboBox_().addItem(keys[i]);
                if (selected != null)
                    keyComboBox_().setSelectedItem(selected);
            }
        }
    }

    private void fillValue() {
        if (value.getDefaultValue() != null) {
            valueTextArea_().setText(value.getDefaultValue());
        }
        else {
            String val = value.getPropertyValue();
            if (val == null)
                val = "";
            valueTextArea_().setText(val);
        }
    }

    private PropertiesDataObject findDataObject(String bundleName) {
        try {
            FileObject fo = null;
            String resourceName = bundleName + ".properties";
            for (Enumeration en = TopManager.getDefault().getRepository().getFileSystems(); en.hasMoreElements();) {
                fo = ((FileSystem)en.nextElement()).findResource(resourceName);
                if (fo != null)
                    break;
            }
            if (fo == null)
                return null;
            DataObject obj = TopManager.getDefault().getLoaderPool().findDataObject(fo);
            if (obj instanceof PropertiesDataObject)
                return (PropertiesDataObject)obj;
            else
                return null;
        }
        catch (IOException e) {
            return null;
        }
    }

    private String getBundleName(DataObject object) {
        if (object == null)
            return "";
        else
            return object.getPrimaryFile().getPackageName('.');
    }

    // component accessors
    protected JTextField bundleText_() {
        return bundleText;
    }

    protected JComboBox keyComboBox_() {
        return keyComboBox;
    }

    protected JTextArea valueTextArea_() {
        return valueTextArea;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel resourcePanel;
    private javax.swing.JTextField bundleText;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton newButton;
    private javax.swing.JPanel stringPanel;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JComboBox keyComboBox;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JScrollPane valueScrollPane;
    private javax.swing.JTextArea valueTextArea;
    private javax.swing.JButton paramsButton;
    // End of variables declaration//GEN-END:variables

    protected ResourceBundleString value;

    private PropertyChangeListener pcl;
    private PropertyBundleListener bList;
    private boolean updatingStatus = false;

    private static class ObjectNameInputPanel extends JPanel {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 1980214734060402958L;

        JTextField text;

        public ObjectNameInputPanel () {
            BorderLayout lay = new BorderLayout ();
            lay.setVgap(5);
            lay.setHgap(5);
            setLayout (lay);
            // label and text field with mnemonic
            String labelText = NbBundle.getBundle (InstantiateAction.class).getString ("CTL_Template_Name");
            JLabel label = new JLabel(labelText.replace('&', ' '));
            text = new JTextField ();
            label.setDisplayedMnemonic(labelText.charAt(labelText.indexOf('&') + 1));
            label.setLabelFor(text);
            add (BorderLayout.WEST, label);
            add (BorderLayout.CENTER, text);
        }

        public void requestFocus () {
            text.requestFocus ();
        }

        public String getText () {
            return text.getText ();
        }

        public void setText (String s) {
            setText (s);
        }
    }


}