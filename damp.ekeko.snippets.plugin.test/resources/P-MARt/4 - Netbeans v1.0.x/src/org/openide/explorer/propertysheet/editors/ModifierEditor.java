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

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;

import org.openide.src.ElementProperties;
import org.openide.util.HelpCtx;

/** Property editors for java modifiers.
*
* @author Petr Hamernik
*/
public class ModifierEditor extends JPanel implements PropertyEditor {

    /** Instance of custom property editor - visual panel. */
    private ModifierPanel panel;

    /** Properties change support */
    private PropertyChangeSupport support;

    /** Serial version UID */
    static final long serialVersionUID = 6324048239020120791L;

    /** Creates new modifiers editor with full mask.
    */
    public ModifierEditor() {
        this(ModifierPanel.EDITABLE_MASK);
    }

    /** Creates new modifiers editor.
    * @param mask The mask of modifier values which should be possible to change.
    */
    public ModifierEditor(int mask) {
        setLayout(new BorderLayout());

        support = new PropertyChangeSupport(this);

        panel = new ModifierPanel();
        panel.setMask(mask & ModifierPanel.EDITABLE_MASK);
        add(panel, BorderLayout.CENTER);

        panel.addPropertyChangeListener(new PropertyChangeListener() {
                                            public void propertyChange(PropertyChangeEvent evt) {
                                                if (ModifierPanel.PROP_MODIFIER.equals(evt.getPropertyName())) {
                                                    support.firePropertyChange(ElementProperties.PROP_MODIFIERS,
                                                                               evt.getOldValue(), evt.getNewValue()
                                                                              );
                                                }
                                            }
                                        });

        HelpCtx.setHelpIDString (this, ModifierEditor.class.getName ());
    }

    /** Set the mask of editable modifiers.
    * @param newMask new value of the mask.
    */
    public void setMask(int newMask) {
        panel.setMask(newMask);
    }

    /** Set new value */
    public void setValue(Object object) throws IllegalArgumentException {
        if (object instanceof Integer) {
            panel.setModifier(((Integer) object).intValue());
        }
        else {
            throw new IllegalArgumentException ();
        }
    }

    /** @return the java source code representation
    * of the current value.
    */
    public String getJavaInitializationString() {
        return new Integer(panel.getModifier()).toString();
    }

    /** Get the value */
    public Object getValue() {
        return new Integer(panel.getModifier());
    }

    /** @return <CODE>false</CODE> */
    public boolean isPaintable() {
        return false;
    }

    /** Does nothing. */
    public void paintValue(Graphics g, Rectangle rectangle) {
    }

    /** @return textual representition of current value of the modifiers. */
    public String getAsText() {
        return Modifier.toString(panel.getModifier());
    }

    /** Parse the text and sets the modifier editor value */
    public void setAsText(String string) throws IllegalArgumentException {
        panel.setText(string);
    }

    /** @return <CODE>null</CODE> */
    public String[] getTags() {
        return null;
    }

    /** @return <CODE>this</CODE> */
    public Component getCustomEditor() {
        return this;
    }

    /** @return <CODE>true</CODE> */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Remove property change listener */
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    /** Add property change listener */
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }
}

/*
* Log
*  5    Gandalf   1.4         11/25/99 Petr Hamernik   rewritten using 
*       ModifierPanel component
*  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  2    Gandalf   1.1         7/29/99  Ian Formanek    Fixed bug 2830 - change 
*       variable modifier if in default throw exception
*  1    Gandalf   1.0         7/13/99  Petr Hamernik   
* $
*/
