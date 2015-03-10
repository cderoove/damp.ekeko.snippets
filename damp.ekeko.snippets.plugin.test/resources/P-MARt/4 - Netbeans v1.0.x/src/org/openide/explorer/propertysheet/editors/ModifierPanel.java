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

package org.openide.explorer.propertysheet.editors;

import java.awt.event.*;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;
import java.util.ResourceBundle;

import javax.swing.*;

import org.openide.util.NbBundle;

/** JPanel extension containing components which allows visual
 * editing Modifier object.
 * This class has two main properties: mask (possible values mask) 
 * and modifier (current value).
 *
 * @author Petr Hamernik
 */
class ModifierPanel extends javax.swing.JPanel {

    // ------------------------- Statics -------------------------------

    /** Resource bundle. */
    static final ResourceBundle bundle = NbBundle.getBundle(ModifierPanel.class);

    /** Name of 'mask' property */
    public static final String PROP_MASK = "mask"; // NOI18N

    /** Name of 'modifier' property (current value) */
    public static final String PROP_MODIFIER = "modifier"; // NOI18N

    /** Names of modifiers */
    private static final String MODIFIER_NAMES[] = {
        "abstract", "final", "static", "synchronized", "transient", "volatile", "native" // NOI18N
    };

    /** Values of modifiers */
    private static final int MODIFIER_VALUES[] = {
        Modifier.ABSTRACT, Modifier.FINAL, Modifier.STATIC, Modifier.SYNCHRONIZED,
        Modifier.TRANSIENT, Modifier.VOLATILE, Modifier.NATIVE
    };

    /** Count of the modifiers */
    private static final int MODIFIER_COUNT = MODIFIER_VALUES.length;

    /** Names of accessibility */
    private static final String ACCESS_NAMES[] = {
        "<default>", "private", "protected", "public" // NOI18N
    };

    /** Values of accessibility */
    private static final int ACCESS_VALUES[] = {
        0, Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC
    };

    /** Mask of access modifiers */
    private static final int ACCESS_MASK = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC;

    /** Mask of all possible modifiers. */
    static final int EDITABLE_MASK = ACCESS_MASK | Modifier.ABSTRACT |
                                     Modifier.FINAL | Modifier.STATIC | Modifier.SYNCHRONIZED |
                                     Modifier.TRANSIENT | Modifier.VOLATILE | Modifier.NATIVE;

    // ------------------ Instance Fields --------------------------

    /** Current mask */
    private int mask;

    /** Current value */
    private int modifier;

    /** Current access values shown in the combo box */
    private int currentAccessValues[];

    /** Current access names shown in the combo box */
    private String currentAccessNames[];

    /** JCheckBox array */
    private JCheckBox[] checks;

    /** listener for visual changes */
    private ActionListener listener;

    /** Ignored flag - used during firing change events */
    private boolean ignored = false;

    /** Property support for changes in the ModifiersPanel bean */
    private PropertyChangeSupport propertyChangeSupport;


    static final long serialVersionUID =6884758007403225916L;
    /** Creates new form ModifiersPanel */
    public ModifierPanel() {
        mask = EDITABLE_MASK;
        modifier = 0;
        currentAccessValues = ACCESS_VALUES;
        currentAccessNames = ACCESS_NAMES;

        listener = new ActionListener() {
                       public void actionPerformed(ActionEvent evt) {
                           if ((checks[0].isSelected()) && ((modifier & MODIFIER_VALUES[0]) == 0))
                               checks[1].setSelected(false);
                           if ((checks[1].isSelected()) && ((modifier & MODIFIER_VALUES[1]) == 0))
                               checks[0].setSelected(false);
                           if (!ignored)
                               updateValue();
                       }
                   };

        ignored = true;
        initComponents();

        modifPanel.setBorder (new javax.swing.border.CompoundBorder(
                                  new javax.swing.border.TitledBorder(bundle.getString("LAB_Modifiers")),
                                  new javax.swing.border.EmptyBorder(new java.awt.Insets(3, 3, 3, 3))
                              ));

        propertyChangeSupport = new PropertyChangeSupport(this);
        updateAccess();
        updateModifiers();
        updateComponents();
        ignored = false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        modifPanel = new javax.swing.JPanel ();
        jPanel2 = new javax.swing.JPanel ();
        jLabel1 = new javax.swing.JLabel ();
        accessCombo = new javax.swing.JComboBox ();
        setLayout (new java.awt.BorderLayout ());
        setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 7, 6, 7)));

        modifPanel.setLayout (new java.awt.GridLayout (4, 2, 4, 4));
        checks = new JCheckBox[MODIFIER_COUNT];
        for (int i = 0; i < MODIFIER_COUNT; i++) {
            checks[i] = new JCheckBox(MODIFIER_NAMES[i]);
            modifPanel.add(checks[i]);
            checks[i].setEnabled((this.mask & MODIFIER_VALUES[i]) != 0);
            checks[i].addActionListener(listener);
        }



        add (modifPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout (new java.awt.BorderLayout (8, 8));
        jPanel2.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

        jLabel1.setText (bundle.getString("LAB_AccessRights"));

        jPanel2.add (jLabel1, java.awt.BorderLayout.WEST);

        accessCombo.addActionListener(listener);

        jPanel2.add (accessCombo, java.awt.BorderLayout.CENTER);


        add (jPanel2, java.awt.BorderLayout.NORTH);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel modifPanel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox accessCombo;
    // End of variables declaration//GEN-END:variables

    /** Add a PropertyChangeListener to the listener list.
     *@param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener (l);
    }

    /** Removes a PropertyChangeListener from the listener list.
     *@param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener (l);
    }

    /** Getter for property mask.
     *@return Value of property mask.
     */
    public int getMask() {
        return mask;
    }

    /** Setter for property mask.
     *@param mask New value of property mask.
     */
    public void setMask(int mask) {
        if (this.mask != mask) {
            int oldMask = this.mask;
            this.mask = mask & EDITABLE_MASK;
            updateAccess();
            updateModifiers();
            propertyChangeSupport.firePropertyChange (PROP_MASK, new Integer (oldMask), new Integer (mask));
            setModifier(modifier & mask);
        }
    }

    /** Update access ComboBox values depending on new 'mask' property
     */
    private void updateAccess() {
        int selValue = modifier & ACCESS_MASK;
        int selIndex = 0;

        int counter = 1;
        for (int i = 1; i < ACCESS_VALUES.length; i++) {
            if ((ACCESS_VALUES[i] & mask) != 0)
                counter++;
        }
        currentAccessValues = new int[counter];
        currentAccessNames = new String[counter];

        currentAccessValues[0] = ACCESS_VALUES[0];
        currentAccessNames[0] = ACCESS_NAMES[0];
        counter = 1;

        for (int i = 1; i < ACCESS_VALUES.length; i++) {
            if ((ACCESS_VALUES[i] & mask) != 0) {
                currentAccessValues[counter] = ACCESS_VALUES[i];
                currentAccessNames[counter] = ACCESS_NAMES[i];
                if (ACCESS_VALUES[i] == selValue) {
                    selIndex = counter;
                }
                counter++;
            }
        }

        ignored = true;
        accessCombo.setModel(new DefaultComboBoxModel(currentAccessNames));
        accessCombo.setSelectedIndex(selIndex);
        ignored = false;
    }

    /** Update enable status of all modifiers check boxes
     */
    private void updateModifiers() {
        for (int i = 0; i < MODIFIER_COUNT; i++) {
            checks[i].setEnabled((mask & MODIFIER_VALUES[i]) != 0);
        }
    }

    /** Getter for property modifier.
     *@return Value of property modifier.
     */
    public int getModifier() {
        return modifier;
    }

    /** Setter for property modifier.
     *@param modifier New value of property modifier.
     */
    public void setModifier(int modifier) {
        if (this.modifier != modifier) {
            boolean accessUsed = false;
            for (int i = 1; i < ACCESS_VALUES.length; i++) {
                if ((ACCESS_VALUES[i] & modifier) != 0) {
                    if (accessUsed)
                        throw new IllegalArgumentException();
                    else
                        accessUsed = true;
                }
            }
            int oldModifier = this.modifier;
            this.modifier = modifier;
            ignored = true;
            updateComponents();
            ignored = false;
            propertyChangeSupport.firePropertyChange (PROP_MODIFIER, new Integer (oldModifier), new Integer (modifier));
        }
    }

    /** Update the components inside the ModifierPanel depending on new value
     * of 'modifier' property.
     */
    private void updateComponents() {
        int selIndex = 0;
        for (int i = 1; i < currentAccessValues.length; i++) {
            if ((currentAccessValues[i] & modifier) != 0) {
                selIndex = i;
                break;
            }
        }
        accessCombo.setSelectedIndex(selIndex);

        for (int i = 0; i < MODIFIER_COUNT; i++) {
            checks[i].setSelected((modifier & MODIFIER_VALUES[i]) != 0);
        }
    }

    /** Updates the value depending on the status of the components. */
    private void updateValue() {
        int newValue = 0;

        newValue |= currentAccessValues[accessCombo.getSelectedIndex()];

        for (int i = 0; i < MODIFIER_COUNT; i++) {
            if (checks[i].isSelected() & checks[i].isEnabled())
                newValue |= MODIFIER_VALUES[i];
        }
        if (modifier != newValue) {
            int oldValue = modifier;
            modifier = newValue;
            propertyChangeSupport.firePropertyChange(PROP_MODIFIER,
                    new Integer(oldValue), new Integer(modifier));
        }
    }

    /** Sets the 'modifier' property as a text.
    * @param string The text form of modifier like 'public static final'
    *     Optionally delimited by comma.
    * @exception IllegalArgumentException if parameter cannot be parsed.
    */
    public void setText(String string) throws IllegalArgumentException {
        int newValue = 0;
        int oldValue = modifier;

        StringTokenizer tukac = new StringTokenizer(string, ", ", false); // NOI18N
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            boolean known = false;
            for (int i = 0; i < MODIFIER_COUNT; i++) {
                if ((MODIFIER_VALUES[i] & mask) != 0) {
                    if (token.equals(MODIFIER_NAMES[i])) {
                        if (((MODIFIER_VALUES[i] == Modifier.FINAL) && ((newValue & Modifier.ABSTRACT) != 0)) ||
                                ((MODIFIER_VALUES[i] == Modifier.ABSTRACT) && ((newValue & Modifier.FINAL) != 0)))
                            break;
                        newValue |= MODIFIER_VALUES[i];
                        known = true;
                        break;
                    }
                }
            }
            if ((newValue & ACCESS_MASK) == 0) {
                for (int i = 1; i <= 3; i++) {
                    if ((ACCESS_VALUES[i] & mask) != 0) {
                        if (token.equals(ACCESS_NAMES[i])) {
                            newValue |= ACCESS_VALUES[i];
                            known = true;
                            break;
                        }
                    }
                }
            }
            if (!known)
                throw new IllegalArgumentException();
        }
        if (oldValue != newValue) {
            modifier = newValue;
            ignored = true;
            updateComponents();
            ignored = false;
            propertyChangeSupport.firePropertyChange(PROP_MODIFIER,
                    new Integer(oldValue), new Integer(modifier));
        }
    }
}
