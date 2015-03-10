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

package org.openide.explorer.propertysheet;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.border.*;
import org.openide.awt.*;
import org.openide.explorer.propertysheet.editors.NodePropertyEditor;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Node;
import org.openide.util.Utilities;


/**
* This class encapsulates working with indexed properties.
*/
class IndexedPropertyEditor extends Object implements NodePropertyEditor {

    static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (
                IndexedPropertyEditor.class);

    // -----------------------------------------------------------------------------
    // Private variables

    private PropertyDetails details;

    /** The PropertyEditor of the array items type */
    private PropertyEditor delegate;

    /** If true, indexed acces should be used,
    * if false, setter/getter for whole array should be used */
    private boolean isIndexedValue;

    private Class itemType;

    private Object[] array;

    private Node[] nodes;

    private PropertyChangeSupport propertySupport = new PropertyChangeSupport (this);


    // -----------------------------------------------------------------------------
    // init

    public IndexedPropertyEditor(PropertyDetails details) {
        this.details = details;
        delegate = details.getIndexedPropertyEditor ();
        itemType = details.getIndexedValueType ();
        updateArray ();
    }

    // -----------------------------------------------------------------------------
    // NodePropertyEditor implementation

    public void attach (Node[] nodes) {
        this.nodes = nodes;
    }


    // -----------------------------------------------------------------------------
    // PropertyEditor implementation

    public void setValue(Object value) {

        if (!value.getClass ().isArray ())
            throw new IllegalArgumentException ();
        array = Utilities.toObjectArray (value);
    }

    public Object getValue() {
        return array;
    }

    public boolean isPaintable() {
        return false;
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
    }

    public String getJavaInitializationString(int index) {
        if (array[index] == null) return "null"; // NOI18N
        try {
            delegate.setValue (array [index]);
            return delegate.getJavaInitializationString ();
        } catch (NullPointerException e) {
            return "null"; // NOI18N
        }
    }

    public String getJavaInitializationString() {
        if (array == null) return ""; // NOI18N
        StringBuffer buf = new StringBuffer ("new "); // NOI18N
        buf.append (itemType.getName ());

        // empty array
        if (array.length == 0) {
            buf.append ("[0]"); // NOI18N
        } else
            // non-empty array
        {
            buf.append ("[] {\n\t"); // NOI18N
            for (int i = 0; i < array.length; i++) {
                try {
                    delegate.setValue (array[i]);
                    buf.append (delegate.getJavaInitializationString ());
                } catch (NullPointerException e) {
                    buf.append ("null"); // NOI18N
                }
                if (i != array.length - 1)
                    buf.append (",\n\t"); // NOI18N
                else
                    buf.append ("\n"); // NOI18N
            }
            buf.append ("}"); // NOI18N
        }
        return buf.toString ();
    }

    public String getAsText() {
        if (array == null) return "null"; // NOI18N
        StringBuffer buf = new StringBuffer ("["); // NOI18N
        for (int i = 0; i < array.length; i++) {
            try {
                delegate.setValue (array[i]);
                buf.append (delegate.getJavaInitializationString ());
            } catch (NullPointerException e) {
                buf.append ("null"); // NOI18N
            }
            if (i != array.length - 1)
                buf.append (", "); // NOI18N
        }
        buf.append ("]"); // NOI18N

        return buf.toString ();
    }

    public void setAsText(String text) throws java.lang.IllegalArgumentException {
    }

    public String[] getTags() {
        return null;
    }

    public Component getCustomEditor() {
        if (delegate.supportsCustomEditor ())
            return new CustomIndexedPropertyPanel ();
        else
            return new IndexedPropertyPanel ();
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener (listener);
    }


    // other methods ........................................................................

    void firePropertyChange () {
        propertySupport.firePropertyChange ("value", null, null); // NOI18N
    }

    /**
    * Returns if property has indexed setter.
    */
    public boolean isIndexed () {
        return details.canWrite (PropertyDetails.INDEXED);
    }

    /**
    * Sets value of array through nonindexed setter.
    */
    void updateArray () {
        array = null;
        try {
            if (details.canRead (PropertyDetails.NORMAL))
                setValue (details.getPropertyValue ());
        } catch (Exception e) {
        }
        return;
    }

    /**
    * Sets a new value of item on index specified.
    */
    void setItem (Object o, int index) {
        try {
            if (details.canWrite (PropertyDetails.INDEXED)) {
                details.setPropertyValue (o, index);
                IndexedPropertyEditor.this.firePropertyChange ();
                updateArray ();
            } else
                if (details.canWrite (PropertyDetails.NORMAL) &&
                        details.canRead (PropertyDetails.NORMAL)
                   ) {
                    array [index] = o;
                    Object[] a = (Object [])Array.newInstance (
                                     Utilities.getObjectType (details.getIndexedValueType ()),
                                     array.length
                                 );
                    System.arraycopy (array, 0, a, 0, array.length);
                    array = a;

                    Class itemClass = details.getIndexedValueType ();
                    if (!itemClass.isPrimitive ()) details.setPropertyValue (array);
                    else details.setPropertyValue (Utilities.toPrimitiveArray (array));
                    IndexedPropertyEditor.this.firePropertyChange ();
                    updateArray ();
                }
        } catch (Exception ee) {
        }
    }



    // innerclasses ........................................................................

    /**
    * Abstract panel showing value of one item of array.
    */
    abstract class AbstractIndexedPropertyPanel extends JPanel {

        /** Input component. */
        private Component inputComponent;
        Object value;
        int index;
        SpinButton spinButton;
        JTextField tfSize, tfIndex;

        static final long serialVersionUID =4649444616108622547L;
        /**
        */
        protected void init () {

            setLayout (new BorderLayout (3, 3));
            setBorder (new EmptyBorder (6, 6, 0, 6));
            JPanel p = new JPanel (new BorderLayout ());
            p.setBorder (new CompoundBorder (
                             new EtchedBorder (),
                             new EmptyBorder (2, 2, 2, 2)
                         ));
            JPanel pp = new JPanel ();
            pp.setLayout (new GridLayout (2, 1, 3, 3));
            JPanel p1 = new JPanel ();
            p1.setLayout (new BorderLayout (3, 3));
            p1.add ("West", new JLabel (bundle.getString ("CTL_Index")));
            tfIndex = new JTextField ("0", 4); // NOI18N
            tfIndex.addActionListener (new ActionListener () {
                                           public void actionPerformed (ActionEvent e) {
                                               JTextField tField = (JTextField)e.getSource ();
                                               String s = tField.getText ();
                                               int i = 0;
                                               try {
                                                   i = Integer.parseInt (s);
                                               } catch (NumberFormatException ee) {
                                                   tField.setText ("0"); // NOI18N
                                                   return;
                                               }
                                               setInput (i);
                                               spinButton.setValue (i);
                                           }
                                       });
            p1.add (tfIndex, "Center"); // NOI18N
            pp.add (p1);
            p1 = new JPanel ();
            p1.setLayout (new BorderLayout (3, 3));
            p1.add (new JLabel ("Size :"), "West"); // NOI18N
            tfSize = new JTextField ("", 4); // NOI18N
            tfSize.addActionListener (new ActionListener () {
                                          public void actionPerformed (ActionEvent e) {
                                              JTextField tField = (JTextField)e.getSource ();
                                              String s = tField.getText ();
                                              int i = 0;
                                              try {
                                                  i = Integer.parseInt (s);
                                              } catch (NumberFormatException ee) {
                                                  tField.setText ("0"); // NOI18N
                                                  return;
                                              }
                                              Object[] a = (Object [])Array.newInstance (
                                                               Utilities.getObjectType (details.getIndexedValueType ()),
                                                               i
                                                           );
                                              if (array != null)
                                                  System.arraycopy (array, 0, a, 0, Math.min (array.length, a.length));
                                              array = a;
                                              Class itemClass = details.getIndexedValueType ();
                                              if (!itemClass.isPrimitive ()) details.setPropertyValue (array);
                                              else details.setPropertyValue (Utilities.toPrimitiveArray (array));
                                              IndexedPropertyEditor.this.firePropertyChange ();
                                              updateArray ();
                                              spinButton.setMaximum (array.length - 1);
                                              spinButton.setValue (index);
                                          }
                                      });
            if (!details.canWrite (PropertyDetails.NORMAL))
                tfSize.setEnabled (false);
            if (array == null) tfSize.setText (bundle.getString ("CTL_Unknown"));
            else tfSize.setText ("" + array.length); // NOI18N
            p1.add (tfSize, "Center"); // NOI18N
            pp.add (p1);
            p.add (pp, "Center"); // NOI18N
            spinButton = new SpinButton ();
            //        spinButton.setBoundsIgnored (true);
            spinButton.setMinimum (0);
            if (array != null) spinButton.setMaximum (array.length - 1);
            else spinButton.setMaximum (Integer.MAX_VALUE);
            spinButton.addSpinButtonListener (new SpinButtonAdapter () {
                                                  public void moveUp () {
                                                      setInput (++index);
                                                      tfIndex.setText ("" + index); // NOI18N
                                                  }
                                                  public void moveDown () {
                                                      setInput (--index);
                                                      tfIndex.setText ("" + index); // NOI18N
                                                  }
                                              });
            p.add (spinButton, "East"); // NOI18N
            add (p, "North"); // NOI18N
            setInput (0);
        }

        /**
        * Returns component displaying value of one item of array.
        */
        abstract Component getInputComponent (Object value);

        /**
        * Notifies about removing of InputComponent.
        */
        void inputComponentRemoved () {
        }

        /**
        * Sete index of array.
        */
        void setInput (int i) {
            index = i;
            value = null;
            boolean ok = false;
            if ((array != null) && (array.length > i)) {
                value = array [i];
                ok = true;
            } else
                try {
                    if (details.canRead (PropertyDetails.INDEXED)) {
                        value = details.getPropertyValue (i);
                        ok = true;
                    }
                } catch (Exception e) {
                }

            if (ok) {
                Component c = getInputComponent (value);
                if (c != inputComponent) {
                    if (inputComponent != null) remove (inputComponent);
                    add ("Center", inputComponent = c); // NOI18N
                } else
                    inputComponent.invalidate ();
                validate ();
            } else {
                inputComponentRemoved ();
                JPanel p1 = new JPanel (new FlowLayout (FlowLayout.CENTER));
                if (inputComponent != null) remove (inputComponent);
                p1.add (new JLabel (bundle.getString ("CTL_NoItem")));
                add ("Center", inputComponent = p1); // NOI18N
                validate ();
            }
        }
    }

    /**
    * Abstract panel showing value of one item of array as Custom ProeprtyEditor  component.
    */
    class CustomIndexedPropertyPanel extends AbstractIndexedPropertyPanel {

        PropertyChangeListener myListener;
        boolean listenerOn = false;
        Component customizer;
        PropertyEditor editor;

        static final long serialVersionUID =-711591659589018688L;
        CustomIndexedPropertyPanel () {
            // I'm listening not only on variable changing but on changes
            // of JComponent properties too.
            myListener = new PropertyChangeListener () {
                             public void propertyChange (PropertyChangeEvent e) {
                                 Object o = editor.getValue ();
                                 setItem (o, index);
                             }
                         };
            editor = details.getNewIndexedPropertyEditor ();
            init ();
        }

        void inputComponentRemoved () {
            if ((customizer != null) && (listenerOn))
                editor.removePropertyChangeListener (myListener);
        }

        Component getInputComponent (Object value) {
            /*      if (customizer != null) {
                    if (listenerOn) editor.removePropertyChangeListener (myListener);
            System.out.println ("getInputComponent " + value);
                    editor.setValue (value);
                    editor.addPropertyChangeListener (myListener);
                    listenerOn = true;
                    return (Component)customizer;
                  }*/
            try {
                delegate.setValue (value);
            } catch (NullPointerException e) {
            }
            if (listenerOn) editor.removePropertyChangeListener (myListener);
            try {
                editor.setValue (value);
            } catch (NullPointerException e) {
            }
            customizer = editor.getCustomEditor ();
            editor.addPropertyChangeListener (myListener);
            listenerOn = true;
            return customizer;
        }

        public void removeNotify () {
            if (listenerOn) editor.removePropertyChangeListener (myListener);
            listenerOn = false;
            super.removeNotify ();
        }

        public void addNotify () {
            if (listenerOn) editor.removePropertyChangeListener (myListener);
            super.addNotify ();
            editor.addPropertyChangeListener (myListener);
            listenerOn = true;
        }
    }

    /**
    * Abstract panel showing value of one item of array as ProeprtyDisplayer component.
    */
    class IndexedPropertyPanel extends AbstractIndexedPropertyPanel {

        PropertyDetails innerDetails;
        PropertyDisplayer displayer;

        static final long serialVersionUID =2643947136839393931L;
        IndexedPropertyPanel () {
            innerDetails = new PropertyDetails (
                               nodes,
                               new PropertySupport.ReadWrite (
                                   "", // NOI18N
                                   details.getIndexedValueType (),
                                   "", // NOI18N
                                   "" // NOI18N
                               ) {
                                   public Object getValue () {
                                       return value;
                                   }
                                   public void setValue (Object val) throws IllegalArgumentException {
                                       value = val;
                                       setItem (val, index);
                                   }
                               }
                           );
            displayer = new PropertyDisplayer (innerDetails);
            displayer.setSwitchAutomatically (false);
            init ();
        }

        Component getInputComponent (Object value) {
            displayer.setValue (new PropertyValue (innerDetails));
            displayer.setInputState (true);
            return displayer;
        }
    }
}

/** Log
 *  2    Tuborg    1.1         08/13/98 Ian Formanek
 *  1    Tuborg    1.0         08/13/98 Ian Formanek
 * $
 */
