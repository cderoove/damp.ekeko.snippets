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
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.*;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport.ReadWrite;
import org.openide.util.Utilities;

/**
* A panel able to show and change property values (all the same property type, maybe for multiple nodes).
* If an application is interested in knowing when the {@link PropertyDetails} changes the value of a property,
* it can register itself as a listener for property change events by calling
* {@link #addPropertyChangeListener}. You may call {@link #getInputState} to determine
* if a property value is currently being read or changed.
*
* @author Jan Jancura, Jaroslav Tulach
* @version 0.34, May 21, 1998
*/
class PropertyDisplayer extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4059243024814666953L;


    // private helper variables ...................................................

    /** Links to proper PropertyDetails object. */
    private PropertyDetails               propertyDetails;
    /** proper property editor for this property */
    private PropertyEditor                propertyEditor;
    /** Value of this property. */
    private Object                        value;
    /** string representation of value of this property */
    private String                        stringValue;
    /** is true if the property value can be edited as text */
    private boolean                       canEditAsText;
    private String                        rollingItem = null;
    /** index of edited property in the indexed property or -1 */
    private int                           propertyIndex = -1;
    /** index of edited property in the indexed property or -1 */
    private Object                        lock;

    /**
     * @associates SheetButtonListener 
     */
    private Vector                        listener = new Vector (10, 10);

    // Properties

    /** is true if the write component may be automatically changed
        to read comp. after entering some input */
    private boolean                       switchAutomatically = false;
    /** is false when component for showing value is showen
        (not component for changing value) */
    private boolean                       isWriteState = false;
    private int                           paintingStyle;
    /** Foreground color of values. */
    private Color                         foregroundColor;
    /** Foreground color of disabled values. */
    private Color                         disabledColor;
    /** Is plastic property value. */
    private boolean                       plastic;
    /** Text for tool tip. */
    private String                        toolTip;
    private long                          lastDeselectTime;
    private SheetButtonListener           readButtonListener;

    // private variables for visual controls ...........................................

    /** component for showing property value is stored here */
    private SheetButton                   readComponent = null;
    private Component                     propertyCustomEditor = null;


    // constructors ....................................................................

    {
        readButtonListener = new SheetButtonListener () {
                                 public void sheetButtonEntered (ActionEvent e) {}
                                 public void sheetButtonExited (ActionEvent e) {}

                                 public void sheetButtonClicked (ActionEvent e) {
                                     setWriteState ();
                                 }
                             };
    }

    /**
    * Construct a displayer.
    */
    PropertyDisplayer () {
        lock = this;
        paintingStyle = PropertySheet.PAINTING_PREFERRED;
        setLayout (new BorderLayout ());
        setValueAsObject (null);
        setWriteState ();
    }

    /**
    * Construct a displayer for the given property details.
    */
    PropertyDisplayer (PropertyDetails propertyDetails) {
        this (
            propertyDetails,
            new PropertyValue (propertyDetails),
            PropertySheet.PAINTING_PREFERRED,
            null,
            null,
            null,
            false
        );
    }

    /**
    * Constructs PropertyDisplayer.
    */
    PropertyDisplayer (
        PropertyDetails propertyDetails,
        int paintingStyle,
        Object lock,
        Color foregroundColor,
        Color disabledColor,
        boolean plastic
    ) {
        this (
            propertyDetails,
            new PropertyValue (propertyDetails),
            paintingStyle,
            lock,
            foregroundColor,
            disabledColor,
            plastic
        );
    }

    /**
    * Constructs PropertyDisplayer.
    */
    PropertyDisplayer (
        PropertyDetails aPropertyDetails,
        PropertyValue propertyValue,
        int aPaintingStyle,
        Object lock,
        Color foregroundColor,
        Color disabledColor,
        boolean plastic
    ) {
        this.lock = lock;
        this.plastic = plastic;
        this.foregroundColor = foregroundColor;
        this.disabledColor = disabledColor;
        if (this.lock == null) this.lock = this;
        propertyDetails = aPropertyDetails;
        paintingStyle = aPaintingStyle;
        propertyEditor = propertyDetails.getPropertyEditor ();
        setLayout (new BorderLayout ());
        setValue (propertyValue);
        setSwitchAutomatically (true);
    }


    // main public methods ................................................................

    /**
    * Set the value of displayed property.
    * @param value the new value
    */
    public void setValueAsObject (Object value) {
        this.value = value;
        setValueAsProperty (
            new ReadWrite (
                "", // NOI18N
                (PropertyDisplayer.this.value == null) ?
                Object.class :
                PropertyDisplayer.this.value.getClass (),
                "", // NOI18N
                "" // NOI18N
            ) {
                public Object getValue () {
                    return PropertyDisplayer.this.value;
                }
                public void setValue (Object val) throws IllegalArgumentException {
                    PropertyDisplayer.this.value = val;
                }
            }
        );
    }

    /**
    * Set the value of the displayed details based on a node property.
    * @param value the node property used to set the displayed property (or property set)
    */
    public void setValueAsProperty (Property value) {
        if (value == null) return;
        propertyDetails = new PropertyDetails (
                              null,
                              value
                          );
        propertyEditor = propertyDetails.getPropertyEditor ();
        setValue (new PropertyValue (propertyDetails));
    }


    /**
    * Sets a new value of property as PropertyValue.
    */
    void setValue (PropertyValue value) {
        this.value = null;
        if (readComponent != null) releaseReadComponent (readComponent);
        if (isWriteState) {
            readComponent = null;
            setWriteState (); // [Mila] uncommented again
        } else {
            removeAll ();
            add ("Center", readComponent = getReaderComponent (value)); // NOI18N
            attachReadComponent (readComponent);
        }
    }

    /**
    * Get the value of the displayed property.
    * @return the value
    */
    public Object getValue () {
        return value;
    }

    /**
    * Set whether component is being used to enter a value.
    * @param b <code>true</code> to enter a value, <code>false</code> to only display it
    */
    public void setInputState (boolean b) {
        if (b) setWriteState ();
        else setReadState ();
    }

    /**
    * Test whether component is displaying or entering a value.
    * @return <code>true</code> if entering, <code>false</code> if only displaying
    */
    public boolean getInputState () {
        return isWriteState;
    }

    /*
    * Sets tool tip.
    */
    public void setToolTipText (String text) {
        toolTip = text;
        if (readComponent != null) readComponent.setToolTipText (text);
    }

    /*
    * Sets enabled property value.
    */
    public void setEnabled (boolean enabled) {
        if (readComponent != null) readComponent.setEnabled (enabled);
        super.setEnabled (enabled);
    }

    /**
    * Set whether the component is in the pressed state.
    * @param pressed <code>true</code> if so
    */
    public void setPressed (boolean pressed) {
        if (readComponent != null) readComponent.setPressed (pressed);
    }

    /**
    * Set whether to switch beetween the read and wtite component automatically.
    * For example, this would apply to an input line which should be replaced
    * by property sheet button after hitting Enter.
    * @param b <code>true</code> if so
    */
    public void setSwitchAutomatically (boolean b) {
        if (b == switchAutomatically) return;
        switchAutomatically = b;
        if (switchAutomatically)
            readComponent.addSheetButtonListener (readButtonListener);
        else
            readComponent.removeSheetButtonListener (readButtonListener);
    }

    /**
    * Test whether to close automatically.
    * @return <code>true</code> if so
    * @see #setCloseAutomatically
    */
    public boolean getSwitchAutomatically () {
        return switchAutomatically;
    }

    /**
    * Get the time of the last deselection.
    * @return the time
    */
    public long getLastDeselectTime () {
        return lastDeselectTime;
    }


    // private helper methods ....................................................................

    /**
    * Switches from reading component to writing one.
    */
    void setWriteState () {
        rollingItem = null;

        removeAll ();
        JComponent c = getWriterComponent ();
        //    if (toolTip != null) c.setToolTipText (toolTip);
        add ("Center", c); // NOI18N

        Container co = getParent ();
        if (co != null) {
            co.invalidate ();
            co.getParent ().validate ();
        }
        repaint ();
        c.requestDefaultFocus ();
        isWriteState = true;
    }

    /**
    * Switches from writing component to reading one.
    */
    void setReadState () {
        if (!isWriteState) return;

        removeAll ();
        if (readComponent == null) {
            PropertyValue value = new PropertyValue (propertyDetails);
            readComponent = getReaderComponent (value);
            attachReadComponent (readComponent);
        }
        add ("Center", readComponent); // NOI18N

        Component c = getParent ();
        if (c != null) {
            c.invalidate ();
            c = getParent ();
            if (c != null) c.validate ();
        }
        repaint ();

        isWriteState = false;
        lastDeselectTime = System.currentTimeMillis ();
        requestFocus();
    }

    // reader component ........................

    /**
    * Creates Reader component.
    */
    private SheetButton getReaderComponent (PropertyValue value) {
        stringValue = null;
        canEditAsText = true;

        SheetButton c = null;
        if (!value.canRead ()) {//except., prop.editor == null or nonequals values
            if (value.getException () != null) //exception while canRead ()
                c = getTextView (getExceptionString (value.getException ()));
            else
                c =  getTextView (getTypeString (propertyDetails.getValueType ()));
        } else
            try {
                propertyEditor.setValue (this.value = value.getValue ());
                stringValue = propertyEditor.getAsText ();
                if (stringValue == null)
                    canEditAsText = false;
                if ( propertyEditor.isPaintable () &&
                        ( (paintingStyle == PropertySheet.PAINTING_PREFERRED) ||
                          ( (paintingStyle == PropertySheet.STRING_PREFERRED) &&
                            (stringValue == null)
                          )
                        )
                   )
                    c = getPaintView ();
                else
                    c = getTextView (
                            stringValue == null ?
                            getTypeString (propertyDetails.getValueType ()) :
                            stringValue
                        );
            } catch (Throwable e) {
                //exception while setValue () | getAsText () | isPaintable ()
                if (e instanceof ThreadDeath)
                    throw (ThreadDeath)e;
                if (this.value == null) {
                    // PE can not show null value
                    c = getTextView ("null"); // NOI18N
                } else
                    TopManager.getDefault ().notify (
                        new NotifyDescriptor.Exception (
                            e,
                            PropertySheet.bundle.getString("PS_ExcIn") +
                            " " + propertyEditor.getClass ().getName () + // NOI18N
                            " " + PropertySheet.bundle.getString ("PS_Editor") + ".")
                    );
            }
        return c;
    }

    /**
    * Creates SheetButton with text representing current value of property.
    */
    private SheetButton getTextView (String str) {
        SheetButton c = new SheetButton (str, plastic, plastic);
        if (toolTip != null) c.setToolTipText (toolTip);
        if (foregroundColor != null) c.setActiveForeground (foregroundColor);
        if (disabledColor != null) c.setInactiveForeground (disabledColor);
        return c;
    }

    /**
    * Creates SheetButton with PropertyShow representing current value of property.
    */
    private SheetButton getPaintView () {
        PropertyShow propertyShow = new PropertyShow (propertyEditor);
        SheetButton c = new SheetButton ();
        c.add (propertyShow);
        if (toolTip != null) c.setToolTipText (toolTip);
        return c;
    }

    // writer component ........................

    /**
    * This method returns property value editor Component like input line (if property supports
    * setAsText (String string) method) or some others.
    *
    * @return property value editor Component
    */
    private JComponent getWriterComponent () {
        if (propertyEditor == null) return new JLabel ("null"); // NOI18N
        try {
            propertyEditor.setValue (
                this.value = new PropertyValue (propertyDetails).getValue ()
            );
        } catch (Throwable e) {//exception while getAsText ()
            if (e instanceof ThreadDeath)
                throw (ThreadDeath)e;
        }
        boolean existsCustomEditor = propertyEditor.supportsCustomEditor ();
        if ((propertyEditor instanceof EnhancedPropertyEditor) && (((EnhancedPropertyEditor)propertyEditor).hasInPlaceCustomEditor ()))
            return getInput (getInPlace (), existsCustomEditor);

        if (!propertyDetails.canWrite ()) {
            if (!existsCustomEditor) return new JLabel ("null"); // NOI18N
            return getInput (null, true);// read-only
        }

        String[] tags;// tags
        int k;
        boolean editable = (propertyEditor instanceof EnhancedPropertyEditor) &&
                           (((EnhancedPropertyEditor)propertyEditor).supportsEditingTaggedValues ());
        if ( ((tags = propertyEditor.getTags ()) != null) && ((k = tags.length) > 0) )
            return getInput (getInputTag (tags, rollingItem = stringValue, editable), existsCustomEditor);

        if (canEditAsText) return getInput (getInputLine ((stringValue == null) ? "???" : stringValue, true), // NOI18N
                                                existsCustomEditor);
        if (existsCustomEditor) return getInput (null, true);
        return new JLabel ("null"); // NOI18N
    }

    /**
    * This is helper method for method getWriterComponent () which returns Panel with Choice
    * in the "Center" and enhanced property editor open button on the "East". This Panel
    * is then returned as property value editor Component.
    *
    * @param tags There are lines for Choice stored.
    * @param selected Line to be selected.
    *
    * @return Choice Component
    */
    private JComponent getInputTag (String[] tags, final String selected, boolean editable) {
        final JComboBox inputChoice = new JComboBox (tags);

        int i, k = tags.length;
        for (i = 0; i < k; i++) if (tags [i].equals (selected)) break;
        if (i < k) inputChoice.setSelectedIndex (i); // setSelectedItem cann't be used...
        // inputChoice.setSelectedItem (selected); selects the first item if item
        //  isn't in list...

        inputChoice.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                if (!isWriteState) return;
                String s = (String) inputChoice.getSelectedItem ();
                if ( (s != null) && 
                     !s.equals (selected)
                ) setAsText (s);
                else if (switchAutomatically) setReadState ();
            }
        });
        FocusListener focusListener = new FocusAdapter () {
            public void focusLost (FocusEvent e) {
                if (!isWriteState) return;
                String s = (String) inputChoice.getSelectedItem ();
                if ( (s != null) && 
                     !s.equals (selected)
                ) setAsText (s);
                else if (switchAutomatically) setReadState ();
            }
        };

        if (editable) {
            inputChoice.setEditable (true);
            inputChoice.getEditor ().setItem (selected);
            inputChoice.getEditor ().selectAll ();
        } else {
            // patch
            inputChoice.addFocusListener (focusListener);
            inputChoice.putClientProperty (
                "JComboBox.lightweightKeyboardNavigation", // NOI18N
                "Lightweight" // NOI18N
            );  // ugly hack for JDK bug 4199622
        }

        inputChoice.setToolTipText (toolTip);
        return inputChoice;
    }

    private Component getInPlace () {
        Component c = ((EnhancedPropertyEditor) propertyEditor).
                      getInPlaceCustomEditor ();
        propertyEditor.addPropertyChangeListener (
            new PropertyChangeListener () {
                public void propertyChange (PropertyChangeEvent e) {
                    propertyEditor.removePropertyChangeListener (this);
                    if (!isWriteState) return;
                    Object o = propertyEditor.getValue ();
                    synchronized (lock) {
                        if (switchAutomatically) setReadState ();
                        setPropertyValue (o);
                        notifyPropertyChange (e);
                    }
                    if (!switchAutomatically) 
                      propertyEditor.addPropertyChangeListener (this);
                }
            }
        );
        c.addFocusListener (new FocusAdapter () {
            public void focusLost (FocusEvent e) {
                if (!isWriteState) return;
                if (switchAutomatically) setReadState ();
            }
        });
        if (c instanceof JComponent) ((JComponent) c).setToolTipText (toolTip);
        return c;
    }

    JTextField textField;

    /**
    * This is helper method for method getWriterComponent () which returns Panel with TextField
    * in the "Center" and enhanced property editor open button on the "East". This Panel
    * is then returned as property value editor Component.
    *
    * @param String propertyStringValue initial property value.
    * @param boolean editable is true if string editing should be allowed.
    * @param boolean existsCustomEditor is true if enhanced property editor open button
    *  should be showen.
    *
    * @return Panel Component
    */
    private JComponent getInputLine (final String propertyStringValue, boolean editable) {
        textField = new JTextField (propertyStringValue);
        textField.setEditable (editable);

        textField.addActionListener (new ActionListener (){
            public void actionPerformed (ActionEvent e) {
                if (!isWriteState) return;
                setAsText (textField.getText ());
            }
        });
        textField.selectAll ();
        textField.addFocusListener (new FocusAdapter () {
            public void focusLost (FocusEvent e) {
                if (!isWriteState) return;
                String s = textField.getText ();
                if ( !s.equals (propertyStringValue)) setAsText (s);
                else if (switchAutomatically) setReadState ();
            }
        });

        textField.setToolTipText (toolTip);
        return textField;
    }

    /**
    * This is helper method for method getInput () and getInputTag () which returns Panel
    * with enhanced property editor open button on the "East".
    *
    * @param Component leftComponent this component will be added to the "Center" of this panel
    * @param boolean existsCustomEditor is true if enhanced property editor open button
    *  should be showen.
    *
    * @return Panel Component
    */
    private JComponent getInput (
        Component leftComponent,
        boolean existsCustomEditor
    ) {
        JPanel panel;
        if ( (leftComponent == null) &&
                (propertyEditor != null) &&
                (propertyEditor.isPaintable ()) &&
                (paintingStyle != PropertySheet.ALWAYS_AS_STRING) &&
                (!propertyDetails.isArray ())
           ) {
            panel = new PropertyShow (propertyEditor);
        } else
            panel = new JPanel ();

        panel.setLayout (new BorderLayout());
        if (leftComponent != null) panel.add ("Center", leftComponent); // NOI18N

        if (existsCustomEditor) {
            SheetButton button = new SheetButton ("..."); // NOI18N
            Font currentFont = button.getFont ();
            button.setFont (
                new java.awt.Font (
                    currentFont.getName (),
                    currentFont.getStyle () | java.awt.Font.BOLD,
                    currentFont.getSize ()
                )
            );
            if (switchAutomatically)
                button.addFocusListener (new FocusAdapter () {
                    public void focusLost (FocusEvent e) {
                        if (!isWriteState) return;
                        setReadState ();
                    }
                });
            button.addSheetButtonListener (new CustomPEListener ());
            button.setToolTipText (toolTip);
            panel.add ("East", button); // NOI18N
        }
        panel.setToolTipText (toolTip);
        return panel;
    }

    // other .........................

    private void releaseReadComponent (SheetButton readComponent) {
        int i, k = listener.size ();
        for (i = 0; i < k; i++)
            readComponent.removeSheetButtonListener (
                (SheetButtonListener) listener.elementAt (i)
            );
        readComponent.removeSheetButtonListener (readButtonListener);
    }

    private void attachReadComponent (SheetButton readComponent) {
        int i, k = listener.size ();
        for (i = 0; i < k; i++)
            readComponent.addSheetButtonListener (
                (SheetButtonListener) listener.elementAt (i)
            );      
        if (switchAutomatically)
            readComponent.addSheetButtonListener (readButtonListener);
    }

    boolean rolling (boolean down) {
        if (rollingItem == null) return false;
        String[] tags = propertyEditor.getTags ();
        int i, k = tags.length;
        for (i = 0; i < k; i++) if (tags [i].equals (rollingItem)) break;
        if (i >= (k - 1)) i = 0;
        else i++;
        setAsText (tags [i]);
        return true;
    }

    void setAsText (String value) {
        try {
            propertyEditor.setAsText (value);
        } catch (Throwable ee) {
            if (ee instanceof ThreadDeath) throw (ThreadDeath)ee;
            if (switchAutomatically) setReadState ();
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Exception (
                    ee,
                    PropertySheet.bundle.getString("PS_ExcIn") +
                    " " + propertyEditor.getClass ().getName () + // NOI18N
                    " " + PropertySheet.bundle.getString ("PS_Editor") + "."
                )
            );
            return;
        }
        Object o = propertyEditor.getValue ();
        synchronized (lock) {
            if (switchAutomatically) setReadState ();
            setPropertyValue (o);
            notifyPropertyChange (new PropertyChangeEvent (this, null, null, null));
        }
    }

    private String getExceptionString (Throwable exception) {
        if (exception instanceof java.lang.reflect.InvocationTargetException)
            exception = ((java.lang.reflect.InvocationTargetException) exception).
                        getTargetException ();
        return "<" + exception.getClass().getName() + ">"; // NOI18N
    }

    private String getTypeString (Class clazz) {
        if (propertyDetails.isArray ())
            return "[" + PropertySheet.bundle.getString ("PS_ArrayOf") +" " +
                   propertyDetails.getIndexedValueType ().getName () + "]"; // NOI18N
        return "[" + clazz.getName() + "]"; // NOI18N
    }

    // delegating methods

    /**
    * This method is called when property value should be changed. It redirects this
    * call to the proper PropertyDetails object.
    */
    void setPropertyValue (Object o) {

        //S ystem.out.println ("\nsetPropertyValue : " + o); // NOI18N
        //T hread.dumpStack ();
        try {
            propertyDetails.setPropertyValue (value = o);
        } catch (Exception e) {
            if (switchAutomatically) setReadState ();
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }

    /**
    * Delegate.
    * @return delegated value
    * @see PropertyDetails#supportsDefaultValue
    */
    public boolean supportsDefaultValue () {
        return propertyDetails.supportsDefaultValue ();
    }

    /**
    * Delegate.
    * @see PropertyDetails#restoreDefaultValue
    */
    public void restoreDefaultValue () {
        propertyDetails.restoreDefaultValue ();
        notifyPropertyChange (new PropertyChangeEvent (this, null, null, null));
    }

    /**
    * Get the property details object delegated to.
    * @return the property details
    */
    public PropertyDetails getPropertyDetails () {
        return propertyDetails;
    }

    public String toString () {
        return "PropertyDetails [" + propertyDetails.getName () + "]"; // NOI18N
    }


    // listeners .................................................................

    /** Vector of all PropertyChngeListeners. 
     * @associates PropertyChangeListener*/
    private Vector propertyChangeListeners = new Vector (3,10);

    /**
    * Standart helper method.
    */
    void notifyPropertyChange (PropertyChangeEvent e) {
        int i, k = propertyChangeListeners.size ();
        for (i = 0; i < k; i++)
            ((PropertyChangeListener)propertyChangeListeners.elementAt (i)).
            propertyChange (e);
    }

    /*
    * Standart helper method.
    */
    public void addPropertyChangeListener (PropertyChangeListener al) {
        propertyChangeListeners.addElement (al);
    }

    /*
    * Standart helper method.
    */
    public void removePropertyChangeListener (PropertyChangeListener al) {
        propertyChangeListeners.removeElement (al);
    }

    /**
    * Standart helper method.
    */
    private void fireSheetButtonClicked (ActionEvent e) {
        Vector listener = (Vector) this.listener.clone ();
        int i, k = listener.size ();
        for (i = 0; i < k; i++)
            ((SheetButtonListener)listener.elementAt (i)).sheetButtonClicked (e);
    }

    void addSheetButtonListener (SheetButtonListener sheetListener) {
        listener.addElement (sheetListener);
        if (readComponent != null)
            readComponent.addSheetButtonListener (sheetListener);
    }

    void removeSheetButtonListener (SheetButtonListener sheetListener) {
        listener.removeElement (sheetListener);
        if (readComponent != null)
            readComponent.removeSheetButtonListener (sheetListener);
    }


    // innerclasses ..............................................................

    private final class CustomPEListener implements SheetButtonListener {
        public void sheetButtonEntered (ActionEvent e) {}
        public void sheetButtonExited (ActionEvent e) {}

        public void sheetButtonClicked (ActionEvent e) {
            if (switchAutomatically) setReadState ();

            final PropertyEditor propertyEditor = propertyDetails.getNewPropertyEditor ();
            if (!(propertyEditor instanceof IndexedPropertyEditor))
                try {
                    value = propertyDetails.getPropertyValue ();
                    propertyEditor.setValue (value);
                } catch (Throwable ee) {
                    if (ee instanceof ThreadDeath) throw (ThreadDeath)ee;
                    TopManager.getDefault ().notify (
                        new NotifyDescriptor.Exception (
                            ee,
                            PropertySheet.bundle.getString ("PS_ExcIn") +
                            " " + propertyEditor.getClass ().getName () + // NOI18N
                            " " + PropertySheet.bundle.getString ("PS_Editor") + "."
                        )
                    );
                    return;
                }

            Component propertyCustomEditor = propertyDetails.getPropertyCustomEditor
                                             (propertyEditor);
            if (propertyCustomEditor == null) return;

            Window dialog = null;
            if (propertyCustomEditor instanceof Window) {
                final PropertyChangeListener pcl = new PropertyChangeListener () {
                    public void propertyChange (PropertyChangeEvent ev) {
                        Object o = null;
                        o = propertyEditor.getValue ();
                        synchronized (lock) {
                            setPropertyValue (o);
                            notifyPropertyChange (ev);
                        }
                    }
                };
                propertyEditor.addPropertyChangeListener (pcl);
                ((Window)propertyCustomEditor).addWindowListener (new WindowAdapter () {
                    public void windowClosed (WindowEvent evv) {
                        propertyEditor.removePropertyChangeListener (pcl);
                    }
                });
                dialog = (Window) propertyCustomEditor;
            } else
                if (propertyDetails.canWrite ())
                    dialog = new PropertyDialogManager (
                        PropertySheet.bundle.getString ("PS_EditorTitle") + ": " +
                        propertyDetails.getName () + " (" + // NOI18N
                        Utilities.getShortClassName (propertyDetails.getValueType ()) + ")", // NOI18N
                        propertyCustomEditor,
                        true,
                        PropertyDisplayer.this,
                        propertyEditor,
                        lock
                    ).getDialog ();
                else
                    dialog = new PropertyDialogManager (
                        PropertySheet.bundle.getString ("PS_ViewerTitle") + ": " +
                        propertyDetails.getName () + " (" + // NOI18N
                        Utilities.getShortClassName (propertyDetails.getValueType ()) + ")", // NOI18N
                        propertyCustomEditor,
                        true
                    ).getDialog ();

            dialog.pack ();
            dialog.show ();
        }
    }
}


/*
 * Log
 *  19   Gandalf   1.18        1/13/00  Ian Formanek    NOI18N
 *  18   Gandalf   1.17        1/12/00  Ian Formanek    NOI18N
 *  17   Gandalf   1.16        1/4/00   Jan Jancura     Refresh PS when Property
 *       set is changed.
 *  16   Gandalf   1.15        12/22/99 Jan Jancura     Bug 4973
 *  15   Gandalf   1.14        12/9/99  Jan Jancura     PropertyPanel 
 *       implementation + Bug 3961
 *  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        9/29/99  Jan Jancura     Bug 2400
 *  12   Gandalf   1.11        9/15/99  Jaroslav Tulach More private things & 
 *       support for default property.
 *  11   Gandalf   1.10        7/29/99  Jan Jancura     Display value for null.
 *  10   Gandalf   1.9         7/13/99  Ian Formanek    Bold font on "..." 
 *       button for opening CustomPropertyEditor
 *  9    Gandalf   1.8         6/30/99  Ian Formanek    Fixed last change
 *  8    Gandalf   1.7         6/30/99  Ian Formanek    reflecting changes of 
 *       enhanced PropertyEditor interfaces
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         6/3/99   Jaroslav Tulach NodePropertyEditor & 
 *       NodeCustomizer
 *  5    Gandalf   1.4         5/12/99  Jan Jancura     focus management updated
 *  4    Gandalf   1.3         3/20/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/20/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         3/4/99   Jaroslav Tulach QuickSorter removed
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.27        --/--/98 Jaroslav Tulach JComboBox first adds its elements and after that adds listener, save so much pain
 */
