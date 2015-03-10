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

package org.openide.src.nodes;

import java.util.ResourceBundle;

import org.openide.src.ElementFormat;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/*
* TODO:
* <UL>
*  <LI> weak listeners for listening on format changes - all element nodes should react on it.
* </UL>
*/
/** Display options for the hierarchy of source elements.
* These options determine the display name format
* of each kind of element.
* <p>Also included are read-only properties for the "long formats",
* which are in practice used for {@link ElementNode#getHintElementFormat}.
* <p>Changes to settings will fire property change events.
*
* @author Petr Hamernik
*/
public final class SourceOptions extends SystemOption {

    /** Resource bundle. */
    static final ResourceBundle bundle = NbBundle.getBundle(SourceOptions.class);

    /** Kinds of the format. */
    private static final byte T_INITIALIZER = 0;
    private static final byte T_FIELD = 1;
    private static final byte T_CONSTRUCTOR = 2;
    private static final byte T_METHOD = 3;
    private static final byte T_CLASS = 4;
    private static final byte T_INTERFACE = 5;

    /** Names of all properties. */
    static final String[] PROP_NAMES = {
        "initializerElementFormat", "fieldElementFormat", // NOI18N
        "constructorElementFormat", "methodElementFormat", // NOI18N
        "classElementFormat", "interfaceElementFormat" // NOI18N
    };

    /** default values for the formats - short form. */
    private static final ElementFormat[] DEFAULT_FORMATS_SHORT = new ElementFormat[6];

    /** default values for the formats - long form. */
    private static final ElementFormat[] DEFAULT_FORMATS_LONG = new ElementFormat[6];

    /** Current values of the display formats for all kind of elements */
    private static ElementFormat[] formats = new ElementFormat[6];

    static {
        for (int i = 0; i < 6; i++) {
            DEFAULT_FORMATS_SHORT[i] = new ElementFormat(bundle.getString("SHORT_"+PROP_NAMES[i]));
            DEFAULT_FORMATS_LONG[i] = new ElementFormat(bundle.getString("LONG_"+PROP_NAMES[i]));
            formats[i] = DEFAULT_FORMATS_SHORT[i];
        }
    }

    /** Property name of the initializer display format. */
    public static final String PROP_INITIALIZER_FORMAT = PROP_NAMES[T_INITIALIZER];

    /** Property name of the field display format. */
    public static final String PROP_FIELD_FORMAT = PROP_NAMES[T_FIELD];

    /** Property name of the constructor display format. */
    public static final String PROP_CONSTRUCTOR_FORMAT = PROP_NAMES[T_CONSTRUCTOR];

    /** Property name of the method display format. */
    public static final String PROP_METHOD_FORMAT = PROP_NAMES[T_METHOD];

    /** Property name of the class display format. */
    public static final String PROP_CLASS_FORMAT = PROP_NAMES[T_CLASS];

    /** Property name of the interface display format. */
    public static final String PROP_INTERFACE_FORMAT = PROP_NAMES[T_INTERFACE];

    /** Property name of the 'categories usage' property. */
    public static final String PROP_CATEGORIES_USAGE = "categoriesUsage"; // NOI18N

    /** CategoriesUsage property current value */
    private static boolean categories = true;

    static final long serialVersionUID =-2120623049071035434L;
    /** @return display name
    */
    public String displayName () {
        return bundle.getString("MSG_sourceOptions");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (SourceOptions.class);
    }

    // ============= public methods ===================

    /** Set the initializer format.
    * @param format the new format
    */
    public void setInitializerElementFormat(ElementFormat format) {
        setElementFormat(T_INITIALIZER, format);
    }

    /** Get the initializer format.
    * @return the current format
    */
    public ElementFormat getInitializerElementFormat() {
        return formats[T_INITIALIZER];
    }

    /** Set the field format.
    * @param format the new format
    */
    public void setFieldElementFormat(ElementFormat format) {
        setElementFormat(T_FIELD, format);
    }

    /** Get the field format.
    * @return the current format
    */
    public ElementFormat getFieldElementFormat() {
        return formats[T_FIELD];
    }

    /** Set the constructor format.
    * @param format the new format
    */
    public void setConstructorElementFormat(ElementFormat format) {
        setElementFormat(T_CONSTRUCTOR, format);
    }

    /** Get the constructor format.
    * @return the current format
    */
    public ElementFormat getConstructorElementFormat() {
        return formats[T_CONSTRUCTOR];
    }

    /** Set the method format.
    * @param format the new format
    */
    public void setMethodElementFormat(ElementFormat format) {
        setElementFormat(T_METHOD, format);
    }

    /** Get the method format.
    * @return the current format
    */
    public ElementFormat getMethodElementFormat() {
        return formats[T_METHOD];
    }

    /** Set the class format.
    * @param format the new format
    */
    public void setClassElementFormat(ElementFormat format) {
        setElementFormat(T_CLASS, format);
    }

    /** Get the class format.
    * @return the current format
    */
    public ElementFormat getClassElementFormat() {
        return formats[T_CLASS];
    }

    /** Set the interface format.
    * @param format the new format
    */
    public void setInterfaceElementFormat(ElementFormat format) {
        setElementFormat(T_INTERFACE, format);
    }

    /** Get the interface format.
    * @return the current format
    */
    public ElementFormat getInterfaceElementFormat() {
        return formats[T_INTERFACE];
    }

    // ============= getters for long form of formats =================

    /** Get the initializer format for longer hints.
    * @return the current format
    */
    public ElementFormat getInitializerElementLongFormat() {
        return DEFAULT_FORMATS_LONG[T_INITIALIZER];
    }

    /** Get the field format for longer hints.
    * @return the current format
    */
    public ElementFormat getFieldElementLongFormat() {
        return DEFAULT_FORMATS_LONG[T_FIELD];
    }

    /** Get the constructor format for longer hints.
    * @return the current format
    */
    public ElementFormat getConstructorElementLongFormat() {
        return DEFAULT_FORMATS_LONG[T_CONSTRUCTOR];
    }

    /** Get the method format for longer hints.
    * @return the current format
    */
    public ElementFormat getMethodElementLongFormat() {
        return DEFAULT_FORMATS_LONG[T_METHOD];
    }

    /** Get the class format for longer hints.
    * @return the current format
    */
    public ElementFormat getClassElementLongFormat() {
        return DEFAULT_FORMATS_LONG[T_CLASS];
    }

    /** Get the interface format for longer hints.
    * @return the current format
    */
    public ElementFormat getInterfaceElementLongFormat() {
        return DEFAULT_FORMATS_LONG[T_INTERFACE];
    }

    // ============= categories of elements usage ===================

    /** Set the property whether categories under class elements should be used or not.
    * @param cat if <CODE>true</CODE> the elements under class elements are divided into
    *     categories: fields, constructors, methods. Otherwise (<CODE>false</CODE>) all elements
    *     are placed directly under class element.
    */
    public void setCategoriesUsage(boolean cat) {
        categories = cat;
    }

    /** Test whether categiries under class elements are used or not.
    * @return <CODE>true</CODE> if the elements under class elements are divided into
    *     categories: fields, constructors, methods. Otherwise <CODE>false</CODE> (all elements
    *     are placed directly under class element).
    */
    public boolean getCategoriesUsage() {
        return categories;
    }

    // ============= private methods ===================

    /** Sets the format for the given index.
    * @param index One of the constants T_XXX
    * @param format the new format for the specific type.
    */
    private void setElementFormat(byte index, ElementFormat format) {
        ElementFormat old = formats[index];
        formats[index] = format;
        firePropertyChange (PROP_NAMES[index], old, formats[index]);
    }
}


/*
* Log
*  13   src-jtulach1.12        1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  11   src-jtulach1.10        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  10   src-jtulach1.9         7/2/99   Jesse Glick     Help IDs for system 
*       options.
*  9    src-jtulach1.8         6/28/99  Petr Hamernik   new hierarchy under 
*       ClassChildren
*  8    src-jtulach1.7         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    src-jtulach1.6         4/2/99   Jesse Glick     [JavaDoc]
*  6    src-jtulach1.5         3/22/99  Petr Hamernik   
*  5    src-jtulach1.4         3/20/99  Petr Hamernik   
*  4    src-jtulach1.3         3/15/99  Petr Hamernik   
*  3    src-jtulach1.2         3/12/99  Petr Hamernik   
*  2    src-jtulach1.1         3/12/99  Petr Hamernik   
*  1    src-jtulach1.0         3/11/99  Petr Hamernik   
* $
*/
