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

package org.netbeans.modules.javadoc.comments;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.beans.PropertyEditor;

import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.src.JavaDoc;
import org.openide.src.MemberElement;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.SourceException;
import org.openide.util.NbBundle;


/**
 *  This is a singleton supporting adding of properties for Java doc 
 *  comments into property sheet of java element nodes.
 * 
 *  @author Petr Hrebejk
 */
abstract class JavaDocPropertySupport extends PropertySupport {

    /** Source of the localized human presentable strings. */
    static ResourceBundle bundle = NbBundle.getBundle(JavaDocPropertySupport.class);


    private Node node;

    /** Constructs a new ElementProp - support for properties of
     * element hierarchy nodes.
     *
     * @param name The name of the property
     * @param type The class type of the property
     * @param canW The canWrite flag of the property
     */
    JavaDocPropertySupport( Node node, String name, java.lang.Class type, boolean canW ) {
        super(name, type,
              bundle.getString("PROP_" + name),
              bundle.getString("HINT_" + name),
              true, canW);

        this.node = node;
    }

    /** Setter for the value. This implementation only tests
     * if the setting is possible.
     *
     * @param val the value of the property
     * @exception IllegalAccessException when this ElementProp was constructed
     *            like read-only.
     */
    public void setValue (Object val) throws IllegalArgumentException,
        IllegalAccessException, InvocationTargetException {
        if (!canWrite())
            throw new IllegalAccessException(bundle.getString("MSG_Cannot_Write"));
    }

}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/16/99  Petr Hrebejk    Tag descriptions editing
 *       in HTML editor + localization
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $ 
 */ 