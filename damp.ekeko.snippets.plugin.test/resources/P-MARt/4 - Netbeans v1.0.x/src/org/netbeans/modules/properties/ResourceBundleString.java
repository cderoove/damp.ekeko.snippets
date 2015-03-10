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

import java.io.Serializable;

import org.openide.util.NbBundle;
import org.openide.TopManager;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class ResourceBundleString implements Serializable {

    public static final String PROP_RESOURCEBUNDLE = "resourceBundle";
    public static final String PROP_KEY            = "key";
    public static final String PROP_ARGUMENTS      = "arguments";

    static final long serialVersionUID =-3456710726871796987L;
    public ResourceBundleString() {
    }

    public ResourceBundleString(ResourceBundleString source) {
        super();
        setResourceBundle(source.getResourceBundle());
        setKey(source.getKey());
        setDefaultValue(source.getDefaultValue());
        setDefaultComment(source.getDefaultComment());
        setArguments(source.getArguments());
    }

    /** Returns the value of the property from the bundle or null if either the bundle or the key is not valid */
    public String getPropertyValue () {
        Element.ItemElem item = getItem();
        if (item == null)
            return null;
        return item.getValue();
    }

    /** Returns the comment for the property from the bundle or null if either the bundle or the key is not valid */
    public String getRealComment() {
        Element.ItemElem item = getItem();
        if (item == null)
            return null;
        return item.getComment();
    }

    /** Returns the item for the property from the bundle or null if either the bundle or the key is not valid */
    private Element.ItemElem getItem() {
        if ((getResourceBundle() == null) || (getKey() == null))
            return null;
        BundleStructure bs = getResourceBundle().getBundleStructure();
        if (bs == null)
            return null;
        String primaryFileName = getResourceBundle().getPrimaryFile().getName();
        PropertiesFileEntry pfe = bs.getEntryByFileName(primaryFileName);
        if (pfe == null)
            return null;
        PropertiesStructure ps = pfe.getHandler().getStructure();
        if (ps == null)
            return null;
        return ps.getItem(getKey());
    }

    /** Attempts to create a new key corresponding to its settings in the resource bundle */
    public void tryToUpdate() {
        if ((getResourceBundle() == null) || (getKey() == null))
            return;
        try {
            BundleStructure bs = getResourceBundle().getBundleStructure();
            if (bs == null)
                throw new PropertiesException(NbBundle.getBundle(ResourceBundleString.class).getString("EXC_UpdatingProp"));
            String primaryFileName = getResourceBundle().getPrimaryFile().getName();
            PropertiesFileEntry pfe = bs.getEntryByFileName(primaryFileName);
            if (pfe == null)
                throw new PropertiesException(NbBundle.getBundle(ResourceBundleString.class).getString("EXC_UpdatingProp"));
            PropertiesStructure ps = pfe.getHandler().getStructure();
            if (ps == null)
                throw new PropertiesException(NbBundle.getBundle(ResourceBundleString.class).getString("EXC_UpdatingProp"));
            Element.ItemElem item = ps.getItem(getKey());
            // PENDING - maybe update this in a separate request
            if (item == null) {
                // does not exist, create it
                ps.addItem(getKey(), getDefaultValue(), getDefaultComment());
                setDefaultValue(null);
                setDefaultComment(null);
            }
            else {
                // exists, set the value to the defaultValue and the comment to defaultComment
                if (getDefaultValue() != null) {
                    item.setValue(getDefaultValue());
                    setDefaultValue(null);
                }
                if (getDefaultComment() != null) {
                    item.setComment(getDefaultComment());
                    setDefaultComment(null);
                }
            }
        }
        catch (PropertiesException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions"))
                e.printStackTrace();
            TopManager.getDefault().notifyException(e);
        }
    }

    public boolean isValid() {
        return (getPropertyValue() != null);
    }

    public void setResourceBundle(PropertiesDataObject resourceBundle) {
        // remove myself from the old listener
        PropertiesDataObject oldResourceBundle = this.resourceBundle;
        this.resourceBundle = resourceBundle;
        if (getPropertyValue() != null) {
            // reset value and comment if the bundle and key is valid
            setDefaultValue(null);
            setDefaultComment(null);
        }
        firePropertyChange(PROP_RESOURCEBUNDLE , oldResourceBundle, resourceBundle);
    }


    public PropertiesDataObject getResourceBundle() {
        return resourceBundle;
    }

    public void setKey(String key) {
        if ((key == null) && (getKey() == null))
            return;
        if ((key != null) && (key.equals(getKey())))
            return;
        String oldKey = this.key;
        this.key = key;
        if (getPropertyValue() != null) {
            setDefaultValue(null);
            setDefaultComment(null);
        }
        firePropertyChange(PROP_KEY , oldKey , key);
    }

    public String getKey() {
        return key;
    }

    /** Not the actual value, but the default value */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** Sets the default value. May be null. */
    public void setDefaultValue(String newValue) {
        this.defaultValue = newValue;
    }

    /** Not the actual comment, but the default comment */
    public String getDefaultComment() {
        return defaultComment;
    }

    /** Sets the default comment. May be null. */
    public void setDefaultComment(String newComment) {
        this.defaultComment = newComment;
    }

    /** Getter for property arguments.
     *@return Value of property arguments.
     */
    public String[] getArguments() {
        if (arguments == null)
            arguments = new String[0];
        return arguments;
    }
    /** Setter for property arguments.
     *@param arguments New value of property arguments.
     */
    public void setArguments(String[] arguments) {
        String[] oldArguments = arguments;
        this.arguments = arguments;
        firePropertyChange(PROP_ARGUMENTS , oldArguments, arguments);
    }

    /** Add a PropertyChangeListener to the listener list.
     *@param l the listener to add. */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        if (propertyChangeSupport == null)
            propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    /** Removes a PropertyChangeListener from the listener list.
     *@param l the listener to remove. */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        if (propertyChangeSupport != null)
            propertyChangeSupport.removePropertyChangeListener( l );
    }

    protected void firePropertyChange(String property, Object oldValue, Object newValue) {
        if (propertyChangeSupport != null)
            propertyChangeSupport.firePropertyChange(property, oldValue, newValue);
    }

    /** resource bundle from which the string is taken */
    private PropertiesDataObject resourceBundle;

    /** resource bundle from which the string is taken */
    private String key;

    /** Java initialization strings for arguments */
    private String[] arguments;

    /** value for the key. null value means that it should be computed from the bundle and the key */
    private String defaultValue;

    /** Comment for the key. null comment means that it should be computed from the bundle and the key */
    private String defaultComment;

    /** Utility field used by bound properties. */
    private transient java.beans.PropertyChangeSupport propertyChangeSupport;
}

/*
 * <<Log>>
 *  9    Gandalf   1.8         11/27/99 Patrik Knakal   
 *  8    Gandalf   1.7         10/27/99 Petr Jiricka    Reports error if bundle 
 *       is not updated.
 *  7    Gandalf   1.6         10/25/99 Petr Jiricka    Fixes in a number of 
 *       areas - saving, UI, cookies, ...
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/17/99  Petr Jiricka    Serialization
 *  4    Gandalf   1.3         8/4/99   Petr Jiricka    Fixed 
 *       NullPointerException when the bundle does not contain the key 
 *  3    Gandalf   1.2         8/2/99   Petr Jiricka    
 *  2    Gandalf   1.1         8/1/99   Petr Jiricka    
 *  1    Gandalf   1.0         7/29/99  Petr Jiricka    
 * $
 */
