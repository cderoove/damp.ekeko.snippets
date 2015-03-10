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

package org.openide.util;

import java.util.Dictionary;
import java.util.Hashtable;
import java.beans.PropertyEditorSupport;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

import javax.swing.SwingConstants;
import javax.swing.DebugGraphics;
import javax.swing.ListSelectionModel;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

/** The class defines several property editors for swing.J* classes.
*
* @version 0.10, September 24, 1998
*/
final class SwingEditors extends Object implements SwingConstants, WindowConstants {

    /** Common bundle for editors. */
    static final java.util.ResourceBundle bundle =
        org.openide.util.NbBundle.getBundle(SwingEditors.class);

    /** The central position in an area. Used for
    * both compass-direction constants (NORTH, etc.)
    * and box-orientation constants (TOP, etc.).
    */
    public static final String PROP_CENTER = bundle.getString("CENTER");


    // Box-orientation constants used to specify a position.
    public static final String PROP_TOP = bundle.getString("TOP");
    public static final String PROP_LEFT = bundle.getString("LEFT");
    public static final String PROP_BOTTOM = bundle.getString("BOTTOM");
    public static final String PROP_RIGHT = bundle.getString("RIGHT");

    // Compass-direction constants used to specify a position.
    public static final String PROP_NORTH = bundle.getString("NORTH");
    public static final String PROP_NORTH_EAST = bundle.getString("NORTH_EAST");
    public static final String PROP_EAST = bundle.getString("EAST");
    public static final String PROP_SOUTH_EAST = bundle.getString("SOUTH_EAST");
    public static final String PROP_SOUTH = bundle.getString("SOUTH");
    public static final String PROP_SOUTH_WEST = bundle.getString("SOUTH_WEST");
    public static final String PROP_WEST = bundle.getString("WEST");
    public static final String PROP_NORTH_WEST = bundle.getString("NORTH_WEST");

    // These constants specify a horizontal or
    // vertical orientation. For example, they are
    // used by scrollbars and sliders.
    public static final String PROP_HORIZONTAL = bundle.getString("HORIZONTAL");
    public static final String PROP_VERTICAL = bundle.getString("VERTICAL");

    // Log graphics operations.
    public static final String PROP_LOG_OPTION   = bundle.getString("LOG_OPTION");
    // Flash graphics operations.
    public static final String PROP_FLASH_OPTION = bundle.getString("FLASH_OPTION");
    // Show buffered operations in a seperate Frame.
    public static final String PROP_BUFFERED_OPTION = bundle.getString("BUFFERED_OPTION");
    // Don't debug graphics operations.
    public static final String PROP_NONE_OPTION = bundle.getString("NONE_OPTION");

    // A value for the selectionMode property: select one list index
    // at a time.
    public static final String PROP_SINGLE_SELECTION = bundle.getString("SINGLE_SELECTION");
    // A value for the selectionMode property: select one contiguous
    // range of indices at a time.
    public static final String PROP_SINGLE_INTERVAL_SELECTION = bundle.getString("SINGLE_INTERVAL_SELECTION");
    // A value for the selectionMode property: select one or more
    // contiguous ranges of indices at a time.
    public static final String PROP_MULTIPLE_INTERVAL_SELECTION = bundle.getString("MULTIPLE_INTERVAL_SELECTION");

    // JTable
    // Do not auto resize column when table is resized.
    public static final String PROP_AUTO_RESIZE_OFF = bundle.getString("AUTO_RESIZE_OFF");
    // Auto resize last column only when table is resized
    public static final String PROP_AUTO_RESIZE_LAST_COLUMN = bundle.getString("AUTO_RESIZE_LAST_COLUMN");
    // Proportionately resize all columns when table is resized
    public static final String PROP_AUTO_RESIZE_ALL_COLUMNS = bundle.getString("AUTO_RESIZE_ALL_COLUMNS");

    // WindowConstants
    // The do-nothing default window close operation
    public static final String PROP_DO_NOTHING_ON_CLOSE = bundle.getString("DO_NOTHING_ON_CLOSE");
    // The hide-window default window close operation
    public static final String PROP_HIDE_ON_CLOSE = bundle.getString("HIDE_ON_CLOSE");
    // The dispose-window default window close operation
    public static final String PROP_DISPOSE_ON_CLOSE = bundle.getString("DISPOSE_ON_CLOSE");

    // JOptionPane
    // Type meaning look and feel should not supply any options -- only
    // use the options from the JOptionPane.
    public static final String PROP_DEFAULT_OPTION = bundle.getString("DEFAULT_OPTION");
    // Type used for showConfirmDialog.
    public static final String PROP_YES_NO_OPTION = bundle.getString("YES_NO_OPTION");
    // Type used for showConfirmDialog.
    public static final String PROP_YES_NO_CANCEL_OPTION = bundle.getString("YES_NO_CANCEL_OPTION");
    // Type used for showConfirmDialog.
    public static final String PROP_OK_CANCEL_OPTION = bundle.getString("OK_CANCEL_OPTION");
    // Used for error messages.
    public static final String PROP_ERROR_MESSAGE = bundle.getString("ERROR_MESSAGE");
    // Used for information messages.
    public static final String PROP_INFORMATION_MESSAGE = bundle.getString("INFORMATION_MESSAGE");
    // Used for warning messages.
    public static final String PROP_WARNING_MESSAGE = bundle.getString("WARNING_MESSAGE");
    // Used for questions.
    public static final String PROP_QUESTION_MESSAGE = bundle.getString("QUESTION_MESSAGE");
    // No icon is used.
    public static final String PROP_PLAIN_MESSAGE = bundle.getString("PLAIN_MESSAGE");

    /** Map of supported property names and property editor classes. */
    protected static final Dictionary editors = new Hashtable(17);

    static {
        editors.put("horizontalAlignment", HorizontalPropertyEditor.class); // NOI18N
        editors.put("verticalAlignment", VerticalPropertyEditor.class); // NOI18N
        editors.put("horizontalTextPosition", HorizontalPropertyEditor.class); // NOI18N
        editors.put("verticalTextPosition", VerticalPropertyEditor.class); // NOI18N
        editors.put("optionType", OptionTypePropertyEditor.class); // JOptionPane // NOI18N
        editors.put("messageType", MessageTypePropertyEditor.class); // JOptionPane // NOI18N
        editors.put("debugGraphicsOptions", DebugGraphicsPropertyEditor.class); // JComponent // NOI18N
        editors.put("selectionMode", SelectionModePropertyEditor.class); // JList // NOI18N
        editors.put("orientation", OrientationPropertyEditor.class);  // JSplitPane // NOI18N
        editors.put("autoResizeMode", AutoResizePropertyEditor.class); // JTable // NOI18N
        editors.put("defaultCloseOperation", DefaultCloseOperationPropertyEditor.class); // JInternalFrame, JDialog // NOI18N
    }

    /** no instances */
    private SwingEditors() {
    }

    /** Gets property editor by property name.
    * @param propertyName a name of the property
    * @return a property editor class or null if unknown property
    */
    private static Class getPropertyEditor(String propertyName) {
        return (Class) editors.get(propertyName);
    }

    /** Mutates a swing beaninfo to beaninfo with editors.
    * @param beaninfo a source BeanInfo
    * @return modified BeanInfo
    */
    public static BeanInfo scanAndSetBeanInfo(BeanInfo beaninfo) {
        // only swing
        if (beaninfo != null) { // && beaninfo.getClass().getName().startsWith("javax.swing")) { // NOI18N
            PropertyDescriptor[] scan = beaninfo.getPropertyDescriptors();
            PropertyDescriptor d;
            Class editor;
            for (int i = scan.length; -- i >= 0;) {
                if ((editor = getPropertyEditor(scan[i].getName())) != null) {
                    scan[i].setPropertyEditorClass(editor);
                }
            }
        }
        return beaninfo;
    }

    /** Property editor for vertical xxx property.
    * Used constants from <code>SwingConstants</code> are <code>TOP</code>, <code>CENTER</code>  and <code>BOTTOM</code>.
    */
    public static class VerticalPropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_TOP, PROP_CENTER, PROP_BOTTOM};

        /** @return tagged values */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_TOP)) setValue(new Integer(TOP));
            else if (t.equals(PROP_CENTER)) setValue(new Integer(CENTER));
            else if (t.equals(PROP_BOTTOM)) setValue(new Integer(BOTTOM));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case TOP : return PROP_TOP;
            case CENTER : return PROP_CENTER;
            default : return PROP_BOTTOM;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case TOP : return "javax.swing.SwingConstants.TOP"; // NOI18N
            case CENTER : return "javax.swing.SwingConstants.CENTER"; // NOI18N
            default : return "javax.swing.SwingConstants.BOTTOM"; // NOI18N
            }
        }
    }

    /** Property editor for horizontal xxx property.
    * Used constants from <code>SwingConstants</code> are <code>LEFT</code>, <code>CENTER</code>  and <code>RIGHT</code>.
    */
    public static class HorizontalPropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_LEFT, PROP_CENTER, PROP_RIGHT};

        /** @return tagged values */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_LEFT)) setValue(new Integer(LEFT));
            else if (t.equals(PROP_CENTER)) setValue(new Integer(CENTER));
            else if (t.equals(PROP_RIGHT)) setValue(new Integer(RIGHT));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case LEFT : return PROP_LEFT;
            case CENTER : return PROP_CENTER;
            default : return PROP_RIGHT;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case LEFT : return "javax.swing.SwingConstants.LEFT"; // NOI18N
            case CENTER : return "javax.swing.SwingConstants.CENTER"; // NOI18N
            default : return "javax.swing.SwingConstants.RIGHT"; // NOI18N
            }
        }
    }

    /** PropertyEditor for debugGraphicsOptions property. */
    public static class DebugGraphicsPropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_NONE_OPTION, PROP_LOG_OPTION, PROP_FLASH_OPTION, PROP_BUFFERED_OPTION};

        /** @return tags */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_NONE_OPTION)) setValue(new Integer(DebugGraphics.NONE_OPTION));
            else if (t.equals(PROP_LOG_OPTION)) setValue(new Integer(DebugGraphics.LOG_OPTION));
            else if (t.equals(PROP_FLASH_OPTION)) setValue(new Integer(DebugGraphics.FLASH_OPTION));
            else if (t.equals(PROP_BUFFERED_OPTION)) setValue(new Integer(DebugGraphics.BUFFERED_OPTION));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case DebugGraphics.LOG_OPTION : return PROP_LOG_OPTION;
            case DebugGraphics.FLASH_OPTION : return PROP_FLASH_OPTION;
            case DebugGraphics.BUFFERED_OPTION : return PROP_BUFFERED_OPTION;
            default : return PROP_NONE_OPTION;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case DebugGraphics.LOG_OPTION : return "javax.swing.DebugGraphics.LOG_OPTION"; // NOI18N
            case DebugGraphics.FLASH_OPTION : return "javax.swing.DebugGraphics.FLASH_OPTION"; // NOI18N
            case DebugGraphics.BUFFERED_OPTION : return "javax.swing.DebugGraphics.BUFFERED_OPTION"; // NOI18N
            default : return "javax.swing.DebugGraphics.NONE_OPTION"; // NOI18N
            }
        }
    }

    /** Property editor for selectionMode property. */
    public static class SelectionModePropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_SINGLE_SELECTION, PROP_SINGLE_INTERVAL_SELECTION, PROP_MULTIPLE_INTERVAL_SELECTION};

        /** @return tags */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_SINGLE_SELECTION)) setValue(new Integer(ListSelectionModel.SINGLE_SELECTION));
            else if (t.equals(PROP_SINGLE_INTERVAL_SELECTION)) setValue(new Integer(ListSelectionModel.SINGLE_INTERVAL_SELECTION));
            else if (t.equals(PROP_MULTIPLE_INTERVAL_SELECTION)) setValue(new Integer(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case ListSelectionModel.SINGLE_INTERVAL_SELECTION : return PROP_SINGLE_INTERVAL_SELECTION;
            case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : return PROP_MULTIPLE_INTERVAL_SELECTION;
            default : return PROP_SINGLE_SELECTION;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case ListSelectionModel.SINGLE_INTERVAL_SELECTION : return "javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION"; // NOI18N
            case ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : return "javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION"; // NOI18N
            default : return "javax.swing.ListSelectionModel.SINGLE_SELECTION"; // NOI18N
            }
        }
    }

    /** Property editor for orientetion property.
    * Used constants from <code>SwingConstants</code> are <code>VERTICAL</code> and <code>HORIZONTAL</code>.
    */
    public static class OrientationPropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_VERTICAL, PROP_HORIZONTAL};

        /** @return tagged values */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_VERTICAL)) setValue(new Integer(VERTICAL));
            else if (t.equals(PROP_HORIZONTAL)) setValue(new Integer(HORIZONTAL));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case VERTICAL : return PROP_VERTICAL;
            default : return PROP_HORIZONTAL;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case VERTICAL : return "javax.swing.SwingConstants.VERTICAL"; // NOI18N
            default : return "javax.swing.SwingConstants.HORIZONTAL"; // NOI18N
            }
        }
    }

    /** Property editor for JTable's autoResize property.
    */
    public static class AutoResizePropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_AUTO_RESIZE_OFF, PROP_AUTO_RESIZE_LAST_COLUMN, PROP_AUTO_RESIZE_ALL_COLUMNS};

        /** @return tagged values */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_AUTO_RESIZE_OFF)) setValue(new Integer(JTable.AUTO_RESIZE_OFF));
            else if (t.equals(PROP_AUTO_RESIZE_LAST_COLUMN)) setValue(new Integer(JTable.AUTO_RESIZE_LAST_COLUMN));
            else if (t.equals(PROP_AUTO_RESIZE_ALL_COLUMNS)) setValue(new Integer(JTable.AUTO_RESIZE_ALL_COLUMNS));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case JTable.AUTO_RESIZE_OFF : return PROP_AUTO_RESIZE_OFF;
            case JTable.AUTO_RESIZE_LAST_COLUMN : return PROP_AUTO_RESIZE_LAST_COLUMN;
            default : return PROP_AUTO_RESIZE_ALL_COLUMNS;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case JTable.AUTO_RESIZE_OFF : return "javax.swing.JTable.AUTO_RESIZE_OFF"; // NOI18N
            case JTable.AUTO_RESIZE_LAST_COLUMN : return "javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN"; // NOI18N
            default : return "javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS"; // NOI18N
            }
        }
    }

    /** Property editor for JInternalFrame's defaultCloseOperation property.
    */
    public static class DefaultCloseOperationPropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_DO_NOTHING_ON_CLOSE, PROP_HIDE_ON_CLOSE, PROP_DISPOSE_ON_CLOSE};

        /** @return tagged values */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_HIDE_ON_CLOSE)) setValue(new Integer(HIDE_ON_CLOSE));
            else if (t.equals(PROP_DO_NOTHING_ON_CLOSE)) setValue(new Integer(DO_NOTHING_ON_CLOSE));
            else if (t.equals(PROP_DISPOSE_ON_CLOSE)) setValue(new Integer(DISPOSE_ON_CLOSE));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case  DISPOSE_ON_CLOSE : return PROP_DISPOSE_ON_CLOSE;
            case  HIDE_ON_CLOSE : return PROP_HIDE_ON_CLOSE;
            default : return PROP_DO_NOTHING_ON_CLOSE;
            }
        }
        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case  DISPOSE_ON_CLOSE : return "javax.swing.WindowConstants.DISPOSE_ON_CLOSE"; // NOI18N
            case  HIDE_ON_CLOSE : return "javax.swing.WindowConstants.HIDE_ON_CLOSE"; // NOI18N
            default : return "javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE"; // NOI18N
            }
        }
    }

    /** Property editor for JOptionPane's optionType property.
    */
    public static class OptionTypePropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_DEFAULT_OPTION, PROP_YES_NO_OPTION, PROP_YES_NO_CANCEL_OPTION, PROP_OK_CANCEL_OPTION};

        /** @return tagged values */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_DEFAULT_OPTION)) setValue(new Integer(JOptionPane.DEFAULT_OPTION));
            else if (t.equals(PROP_YES_NO_OPTION)) setValue(new Integer(JOptionPane.YES_NO_OPTION));
            else if (t.equals(PROP_YES_NO_CANCEL_OPTION)) setValue(new Integer(JOptionPane.YES_NO_CANCEL_OPTION));
            else if (t.equals(PROP_OK_CANCEL_OPTION)) setValue(new Integer(JOptionPane.OK_CANCEL_OPTION));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case JOptionPane.YES_NO_OPTION : return PROP_YES_NO_OPTION;
            case JOptionPane.YES_NO_CANCEL_OPTION : return PROP_YES_NO_CANCEL_OPTION;
            case JOptionPane.OK_CANCEL_OPTION : return PROP_OK_CANCEL_OPTION;
            default : return PROP_DEFAULT_OPTION;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case JOptionPane.YES_NO_OPTION : return "javax.swing.JOptionPane.YES_NO_OPTION"; // NOI18N
            case JOptionPane.YES_NO_CANCEL_OPTION : return "javax.swing.JOptionPane.YES_NO_CANCEL_OPTION"; // NOI18N
            case JOptionPane.OK_CANCEL_OPTION : return "javax.swing.JOptionPane.OK_CANCEL_OPTION"; // NOI18N
            default : return "javax.swing.JOptionPane.DEFAULT_OPTION"; // NOI18N
            }
        }
    }

    /** Property editor for JOptionPane's messageType property.
    */
    public static class MessageTypePropertyEditor extends PropertyEditorSupport {

        /** tags */
        private static final String[] tags = {PROP_ERROR_MESSAGE, PROP_INFORMATION_MESSAGE, PROP_WARNING_MESSAGE, PROP_QUESTION_MESSAGE, PROP_PLAIN_MESSAGE};

        /** @return tagged values */
        public String[] getTags() {
            return tags;
        }

        /** Sets as text. */
        public void setAsText(String t) {
            if (t.equals(PROP_PLAIN_MESSAGE)) setValue(new Integer(JOptionPane.PLAIN_MESSAGE));
            else if (t.equals(PROP_ERROR_MESSAGE)) setValue(new Integer(JOptionPane.ERROR_MESSAGE));
            else if (t.equals(PROP_INFORMATION_MESSAGE)) setValue(new Integer(JOptionPane.INFORMATION_MESSAGE));
            else if (t.equals(PROP_WARNING_MESSAGE)) setValue(new Integer(JOptionPane.WARNING_MESSAGE));
            else if (t.equals(PROP_QUESTION_MESSAGE)) setValue(new Integer(JOptionPane.QUESTION_MESSAGE));
        }

        /** @return value as a text */
        public String getAsText() {
            switch (((Integer) getValue()).intValue()) {
            case JOptionPane.ERROR_MESSAGE : return PROP_ERROR_MESSAGE;
            case JOptionPane.INFORMATION_MESSAGE : return PROP_INFORMATION_MESSAGE;
            case JOptionPane.WARNING_MESSAGE : return PROP_WARNING_MESSAGE;
            case JOptionPane.QUESTION_MESSAGE : return PROP_QUESTION_MESSAGE;
            default : return PROP_PLAIN_MESSAGE;
            }
        }

        /** @return java initialization string */
        public String getJavaInitializationString() {
            switch (((Integer) getValue()).intValue()) {
            case JOptionPane.ERROR_MESSAGE : return "javax.swing.JOptionPane.ERROR_MESSAGE"; // NOI18N
            case JOptionPane.INFORMATION_MESSAGE : return "javax.swing.JOptionPane.INFORMATION_MESSAGE"; // NOI18N
            case JOptionPane.WARNING_MESSAGE : return "javax.swing.JOptionPane.WARNING_MESSAGE"; // NOI18N
            case JOptionPane.QUESTION_MESSAGE : return "javax.swing.JOptionPane.QUESTION_MESSAGE"; // NOI18N
            default : return "javax.swing.JOptionPane.PLAIN_MESSAGE"; // NOI18N
            }
        }
    }
}

/*
 * Log
 */
