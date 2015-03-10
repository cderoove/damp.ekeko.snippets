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

package org.netbeans.modules.form.compat2.layouts;

import java.awt.*;
import java.beans.*;
import java.util.Iterator;

import org.netbeans.modules.form.util2.NbVersion;
import org.netbeans.modules.form.util2.NbVersionNotCompatibleException;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;

/** An abstract superclass of design-time supports for LayoutManagers.
*
* @author   Ian Formanek
*/
public abstract class DesignLayout extends Object implements java.io.Externalizable, Cloneable {

    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -1684801460008455378L;

    /** Netbeans class version */
    public static final NbVersion nbClassVersion = new NbVersion (1, 0);

    /** A constant for design-mode of layout. The design-mode might add
    * an additional advanced design-time behaviour of the layout.
    * E.g. a BorderLayout in design-mode marks the directions (North, South, ...)
    * and keeps the sizes same regardless of what components are added to the layout. */
    public final static int DESIGN_MODE = 1;
    /** A constant for real-mode of layout. The layout in real-mode
    * behaves exactly the same way as during the run-time of the application. */
    public final static int REAL_MODE = 2;

    /** Default icons for DesignLayout. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          DesignLayout.class.getResource ("/org/netbeans/modules/form/resources/formLayout.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit ().getImage (
                            DesignLayout.class.getResource ("/org/netbeans/modules/form/resources/formLayout32.gif")); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // private variables

    /** The container which is managed by this DesignLayout.
    * It is transient so as the setRADContainer is called after deserialization */
    transient private RADVisualContainer container;
    /** array of listeners */
    transient private PropertyChangeSupport support = new PropertyChangeSupport (this);
    /** The curent mode of this layout
    * @see #DESIGN_MODE
    * @see #REAL_MODE */
    transient private int mode = DESIGN_MODE;

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD


    /** Can be called to create a clone of the design layout.
    * The default implemetation creates new instance using the default constructor and 
    * clones all changed properties. If other behavior is desired, this method can be overriden.
    * @return a new instance of this design layout with the same properties
    */
    public Object clone () throws CloneNotSupportedException {
        try {
            DesignLayout newLayout = (DesignLayout)getClass ().newInstance ();
            java.util.List changedProps = getChangedProperties ();
            java.util.Map newChanged = new java.util.HashMap (changedProps.size () * 2);
            for (Iterator it = changedProps.iterator (); it.hasNext ();) {
                Node.Property prop = (Node.Property)it.next ();
                try {
                    newChanged.put (prop.getName (), FormUtils.cloneObject (prop.getValue ()));
                } catch (Exception e) {
                    // ignore property with problem
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
                }
            }
            newLayout.initChangedProperties (newChanged);
            return newLayout;
        } catch (Exception e) {
            throw new CloneNotSupportedException ();
        }
    }

    /** Assigns this DesignLayout to the specified RADVisualContainer.
    * @param cont The RADVisualContainer that represents a container that will be
    *             managed by this layout or null as a notification that this layout
    *             is not a designLayout for its current container anymore
    */
    public void setRADContainer (RADVisualContainer cont) {
        container = cont;
        if ((cont != null) && (cont.getFormManager () != null))
            mode = cont.getFormManager ().getMode ();
    }

    /** @return The container managed by this DesignLayout */
    public RADVisualContainer getRADContainer() {
        return container;
    }

    /** @return The FormManager2 that manages this form */
    protected FormManager2 getFormManager() {
        return container.getFormManager ();
    }

    /** A utility method for subclasses that allows quick access to the
    * @return The AWT container component of the container managed
    * by this DesignLayout or null if not yet initialized
    */
    final protected Container getContainer() {
        if (getRADContainer () != null) {
            return getRADContainer ().getContainer ();
        } else {
            return null;
        }
    }

    /** Method which allows the design layout to provide list of properties to be saved with the form.
    * @return list of Node.Property objects
    */
    public java.util.List getChangedProperties () {
        return new java.util.ArrayList (0);
    }

    /** Method which is called after the layout is loaded with the form to initialize its properties.
    * @param cahngedProperties map of <String, Object> pairs, where the String is a name of property and the Object its value
    */
    public void initChangedProperties (java.util.Map changedProperties) {
    }

    /** Sets the current mode of this layout.
    * Descendants must override this method to provide any additional
    * functionality needed when layout mode is switched.
    * @see #DESIGN_MODE
    * @see #REAL_MODE
    * @see #getMode
    */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /** Sets the current mode of this layout.
    * @see #DESIGN_MODE
    * @see #REAL_MODE
    * @see #getMode
    */
    public int getMode() {
        return mode;
    }

    /** A design layout may provide a customizer for advanced layout
    * customization (e.g. for GridBagLayout).
    * Might return null, if the customizer is not available.
    * The default implementation returns null.
    * @return customizer for this design layout or null if it has no customizer
    */
    public Class getCustomizerClass() {
        return null;
    }

    /** An icon of the design-layout. This icon will be used on the ComponentPalette
    * for this layout's item.
    * @param  type the desired type of the icon (BeanInfo.ICON_???)
    * @return layout's icon.
    */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16))
            return defaultIcon;
        else
            return defaultIcon32;
    }

    /** Returns a description of the constraints for the
    * specified position. The constraints description object will be used
    * for adding, getting textual description of the constraints and
    * the popup menu.
    * The default implementation just returns the default constraints
    * description.
    * @param position The position within the container for which the
    *                 constraints description should be returned.
    */
    public abstract ConstraintsDescription getConstraintsDescription(Point position);

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    public abstract String getDisplayName();

    /** Returns the global layout's properties (i.e. the properties
    * that are not different for different components in the layout.
    * The default implementation returns empty array.
    * @return the global layout properties
    */
    public Node.PropertySet[] getPropertySet () {
        return new Node.PropertySet[0];
    }

    /** Returns the layout's properties for specified component.
    * The default implementation returns empty array.
    * @param component the RADVisualComponent of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getComponentProperties(RADVisualComponent component) {
        return new Node.Property[0];
    }

    /** Returns a class of the layout that this DesignLayout represents (e.g.
    * returns FlowLayout.class from DesignFlowLayout).
    * @return a class of the layout represented by this DesignLayout or null if the 
    *         design layout does not represent a "real" layout (e.g. support layouts for JTabbedPane, ...)
    */
    public abstract Class getLayoutClass();

    // -----------------------------------------------------------------------------
    // Child components management

    /** Adds specified component to this layout. The constraints for the component
    * are acquired from the component by method getConstraints().
    * The default implementation adds the visual representation of the specified component
    * to the position acquired from the component by calling getComponentIndex ().
    * @param component The component to add
    * @see RADVisualComponent#getConstraints
    * @see RADVisualComponent#setConstraints
    * @see RADVisualComponent#getComponentIndex
    * @see #removeComponent
    */
    public void addComponent (RADVisualComponent component) {
        getContainer().add(getFormManager ().getVisualRepresentation (component), component.getComponentIndex ());
    }

    /** Adds specified component to this layout with specified constraints.
    * The constraints for the component are saved in the component by calling setConstraints.
    * @param component The component component to add
    * @param desc The constraints to add the component with
    * @see RADVisualComponent#getConstraints
    * @see RADVisualComponent#setConstraints
    * @see #removeComponent
    */
    public void addComponent (RADVisualComponent component, ConstraintsDescription desc) {
        component.setConstraints (getClass (), desc);
        addComponent (component);
    }

    /** Removes specified component from this layout.
    * The default implementation removes the visual representation of the specified component
    * from the container.
    * @param component The component to remove
    * @see #addComponent
    */
    public void removeComponent (RADVisualComponent component) {
        getContainer().remove(getFormManager ().getVisualRepresentation (component));
    }

    /** Called to update the layout. Should readd all the visual components to
    * reflect any global or ordering changes.
    * The default implementation simply removes all visual representations of all
    * children from the container, calls the single-argument version of addComponent () 
    * on all the children again and validates and repaints the container.
    */
    public void updateLayout () {
        getContainer ().removeAll ();
        RADVisualComponent[] children = getRADContainer ().getSubComponents ();
        for (int i=0; i < children.length; i++) {
            addComponent (children[i]);
        }
        getContainer ().validate ();
        getContainer ().repaint ();
    }

    // -----------------------------------------------------------------------------
    // Drag'n'drop support

    /** A design layout that supports moving components should redefine
    * this method and return true.
    * @return true if the design layout supports moving, false otherwise
    * @see #moveTo
    */
    public boolean canMove () {
        return false;
    }

    /** A design layout that supports moving components should redefine
    * this method to modify the constraints according to the supplied
    * delta positions (difference between the old and the new position).
    * @param desc    the constraints to move from
    * @param deltaX  the delat move in x axis
    * @param deltaY  the delta move in y axis
    * @param hotSpot The hotspot on the component (the point where the user started 
    *                the drag on the component)
    * @return        the new modified ConstraintsDescription
    * @see #canMove
    */
    public ConstraintsDescription moveTo (ConstraintsDescription desc, int deltaX, int deltaY, Point hotSpot) {
        throw new IllegalStateException ();
    }

    /** A design layout that supports resizing components should redefine
    * this method and return true.
    * The resizing includes a initial drag-resize when adding a new component.
    * @return true if the design layout supports resizing, false otherwise
    * @see #resizeTo
    */
    public boolean canResize () {
        return false;
    }

    /** A design layout that supports resizing components should redefine
    * this method to modify the constraints according to the supplied
    * new size.
    * @param desc    the constraints to resize from
    * @param width   the width to resize to
    * @param height  the height to resize to
    * @return        the new modified ConstraintsDescription
    * @see #canResize
    */
    public ConstraintsDescription resizeTo (ConstraintsDescription desc, int width, int height) {
        throw new IllegalStateException ();
    }

    /** A design layout that supports *BOTH* resizing and moving components
    * should redefine this method to modify the constraints according to the supplied
    * new bounds.
    * @param desc the constraints to resize from
    * @param bounds the bounds to resize to
    * @see #canMove
    * @see #canResize
    */
    public ConstraintsDescription resizeToBounds (ConstraintsDescription desc, Rectangle bounds) {
        return null;
    }

    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "move" action of the specified component to the specified constraints.
    * @param component The RADVisualComponent that is being dragged
    * @param desc The "drag to" constraints or null for ending the marking operation
    */
    public void markMoveTo (RADVisualComponent component, ConstraintsDescription desc) {
    }

    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "resize" action to the specified constraints.
    * @param desc The "resize to" constraints or null to notify about cancelling the resize operation
    */
    public void markResizeTo (ConstraintsDescription desc) {
    }

    /** Called to inform the Layout that it should provide a design-time visual
    * feedback for a "resize" action of the specified component to the specified constraints.
    * @param component The RADVisualComponent that is being resized
    * @param desc The "resize to" constraints
    */
    //  public void markResizeTo (RADVisualComponent component, ConstraintsDescription desc) {
    //  }

    // -----------------------------------------------------------------------------
    // Code generation

    /** Generates the code for initialization of this layout, e.g. panel1.setLayout (new BorderLayout ());.
    * @param cont   The container that is managed by this layout
    * @return the init code for the layout or null if it should not be generated
    */
    public abstract String generateInitCode(RADVisualContainer cont);

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public abstract String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp);

    /** Support function used for creating name of container with a dot (if necessarily).
    */
    protected String createContainerGenName(RADVisualContainer container) {
        return container.getContainerGenName();
    }

    // -----------------------------------------------------------------------------
    // listeners

    /** Fire a property change event. For one property listed in
    * the array of properties that can be obtained by
    * <CODE>getProperties ()</CODE>.
    * @param component the component which layout property changed or null if layout's own property changed
    * @param name name of changed property
    * @param o old value
    * @param n new value
    */
    public final void firePropertyChange (RADVisualComponent component, String name, Object o, Object n) {
        if (component == null) {
            if (container.getLayoutNodeReference () != null) {
                container.getLayoutNodeReference ().fireLayoutPropertiesChange ();
            }
            getFormManager().fireLayoutChanged (container, null, name, o, n);
        } else {
            getFormManager().fireLayoutChanged (container, component, name, o, n);
            component.notifyPropertiesChange ();
        }
    }

    // -----------------------------------------------------------------------------
    // Serialization

    /** Writes the object to the stream.
    * @param oo output stream to write to
    * @exception IOException Includes any I/O exceptions that may occur
    */
    public void writeExternal (java.io.ObjectOutput oo)
    throws java.io.IOException {
        // store version
        oo.writeObject (nbClassVersion);
    }

    /** Reads the object from stream.
    * @param oi input stream to read from
    * @exception IOException Includes any I/O exceptions that may occur
    * @exception ClassNotFoundException if the class of the read object is not found
    */
    public void readExternal (java.io.ObjectInput oi)
    throws java.io.IOException, ClassNotFoundException {
        org.netbeans.modules.form.FormUtils.DEBUG(">> DesignLayout: readExternal: START"); // NOI18N
        // check the version
        NbVersion classVersion = (NbVersion) oi.readObject ();
        if (!nbClassVersion.isCompatible (classVersion))
            throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
        org.netbeans.modules.form.FormUtils.DEBUG("<< DesignLayout: readExternal: END"); // NOI18N
    }

    // ------------------------------------------------------------------------
    // ConstraintsDescription innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    * The instances of ConstraintsDescription should be DesingLayout instance-independent,
    * i.e. it should be possible to use one ConstraintsDescription instance in multiple
    * different DesignLayout instances (of appropriate type).  
    */
    public static class ConstraintsDescription implements java.io.Externalizable {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = 2035633473805900956L;

        /** Netbeans class version */
        public static final NbVersion nbClassVersion = new NbVersion (1, 0);

        /** Returns a textual descriptions of constraints represented by this
        * class. E.g. for BorderLayout, it is a text "Center" or "North".
        * @return textual descriptions of the constraints
        */
        public String getConstraintsString() {
            return ""; // NOI18N
        }

        /** Returns the constraints represented by this class. E.g. for
        * BorderLayout, it is a String "Center" or "North".
        * The default implementation simply returns null.
        * @return the constraints represented by this class or null if the
        *         constraints are empty / not appliable
        */
        public Object getConstraintsObject() {
            return null;
        }

        /**
        * @return the Constraints class and constraints string
        */
        public String toString() {
            return getClass() + ": " + getConstraintsString(); // NOI18N
        }

        /** Two ConstraintsDescriptions are equal if its constraints objects
        * (returned from getConstraintsObject) are equal or both null.
        * @return true if this ConstraintsDescriptions is equal to the one passed as a parameter
        */
        public boolean equals (Object o) {
            if (!(o instanceof ConstraintsDescription))
                return false;
            Object con = ((ConstraintsDescription)o).getConstraintsObject ();
            if (getConstraintsObject () == null)
                return (con == null) ? true : false;
            else
                return (con == null) ? false : con.equals (getConstraintsObject ());
        }

        /** The hashCode of a ConstraintsDescription is the hashCode of its constraints objects
        * (returned from getConstraintsObject) or (if it is null) its class.
        * @return the hashCode of this ConstraintsDescription
        */
        public int hashCode () {
            return (getConstraintsObject () != null) ? getConstraintsObject ().hashCode () : getClass().hashCode ();
        }

        // -----------------------------------------------------------------------------
        // Serialization

        /** Writes the object to the stream.
        * @param oo output stream to write to
        * @exception IOException Includes any I/O exceptions that may occur
        */
        public void writeExternal (java.io.ObjectOutput oo)
        throws java.io.IOException {
            // store version
            oo.writeObject (nbClassVersion);
        }

        /** Reads the object from stream.
        * @param oi input stream to read from
        * @exception IOException Includes any I/O exceptions that may occur
        * @exception ClassNotFoundException if the class of the read object is not found
        */
        public void readExternal (java.io.ObjectInput oi)
        throws java.io.IOException, ClassNotFoundException {
            org.netbeans.modules.form.FormUtils.DEBUG(">> ConstraintsDescription: readExternal: START"); // NOI18N
            // check the version
            NbVersion classVersion = (NbVersion) oi.readObject ();
            if (!nbClassVersion.isCompatible (classVersion))
                throw new NbVersionNotCompatibleException (classVersion, nbClassVersion);
            org.netbeans.modules.form.FormUtils.DEBUG("<< ConstraintsDescription: readExternal: END"); // NOI18N
        }

        // -----------------------------------------------------------------------------
        // XML Persistence

        /** Called to load property value from specified XML subtree. If succesfully loaded,
        * the value should be available via the getValue method.
        * An IOException should be thrown when the value cannot be restored from the specified XML element
        * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
        * @exception IOException thrown when the value cannot be restored from the specified XML element
        */
        public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        }

        /** Called to store current property value into XML subtree. The property value should be set using the
        * setValue method prior to calling this method.
        * @param doc The XML document to store the XML in - should be used for creating nodes only
        * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null 
        *         if the value does not need to save any additional data and can be created using the default constructor
        */
        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            return null;
        }
    }

}

/*
 * Log
 *  23   Gandalf   1.22        1/12/00  Ian Formanek    NOI18N
 *  22   Gandalf   1.21        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  21   Gandalf   1.20        10/5/99  Ian Formanek    Cloning of layout 
 *       supported
 *  20   Gandalf   1.19        9/24/99  Ian Formanek    Smarter code generation 
 *       - fixes bug 4016 - The setLayout code should not be generated if the 
 *       layout is already set on the container to prevent loosing components 
 *       already on the panel.
 *  19   Gandalf   1.18        9/24/99  Ian Formanek    generateInitCode method 
 *       clarified
 *  18   Gandalf   1.17        9/17/99  Ian Formanek    Fixed bug 1825 - 
 *       Property sheets are not synchronized 
 *  17   Gandalf   1.16        7/31/99  Ian Formanek    Cleaned up comments
 *  16   Gandalf   1.15        7/13/99  Ian Formanek    XML Persistence
 *  15   Gandalf   1.14        7/13/99  Ian Formanek    Added changedProperties 
 *       to support XML Serialization
 *  14   Gandalf   1.13        6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  13   Gandalf   1.12        6/22/99  Ian Formanek    Modified customizers
 *  12   Gandalf   1.11        6/10/99  Ian Formanek    Regeneration on layout 
 *       changes
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         5/31/99  Ian Formanek    Design/Test Mode
 *  9    Gandalf   1.8         5/15/99  Ian Formanek    
 *  8    Gandalf   1.7         5/14/99  Ian Formanek    
 *  7    Gandalf   1.6         5/14/99  Ian Formanek    
 *  6    Gandalf   1.5         5/12/99  Ian Formanek    
 *  5    Gandalf   1.4         5/11/99  Ian Formanek    Build 318 version
 *  4    Gandalf   1.3         5/10/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  2    Gandalf   1.1         3/29/99  Ian Formanek    Uses FormUtils.DEBUG to 
 *       print messages
 *  1    Gandalf   1.0         3/28/99  Ian Formanek    
 * $
 */
