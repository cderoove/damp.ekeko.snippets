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

import java.awt.Component;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Node representing a field (variable).
* @see FieldElement
* @author Petr Hamernik
*/
public class FieldElementNode extends MemberElementNode {
    /** Create a new field node.
    * @param element field element to represent
    * @param writeable <code>true</code> to be writable
    */
    public FieldElementNode(FieldElement element, boolean writeable) {
        super(element, Children.LEAF, writeable);
        setElementFormat (sourceOptions.getFieldElementFormat());
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        int modif = ((FieldElement)element).getModifiers();
        if (!Modifier.isStatic(modif)) {
            // non-static field...
            if (Modifier.isPrivate(modif))
                return FIELD_PRIVATE;
            else if (Modifier.isProtected(modif))
                return FIELD_PROTECTED;
            else if (Modifier.isPublic(modif))
                return FIELD_PUBLIC;
            else
                return FIELD_PACKAGE;
        }
        else {
            // static field...
            if (Modifier.isPrivate(modif))
                return FIELD_ST_PRIVATE;
            else if (Modifier.isProtected(modif))
                return FIELD_ST_PROTECTED;
            else if (Modifier.isPublic(modif))
                return FIELD_ST_PUBLIC;
            else
                return FIELD_ST_PACKAGE;
        }
    }

    /* This method resolve the appropriate hint format for the type
    * of the element. It defines the short description.
    */
    protected ElementFormat getHintElementFormat() {
        return sourceOptions.getFieldElementLongFormat();
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createModifiersProperty(writeable));
        ps.put(createNameProperty(writeable));
        ps.put(createTypeProperty(writeable));
        ps.put(createInitValueProperty(writeable));
        return sheet;
    }

    /* Removes the element from the class and calls superclass.
    *
    * @exception IOException if SourceException is thrown
    *            from the underlayed Element.
    */
    public void destroy() throws IOException {
        SourceEditSupport.invokeAtomicAsUser(element, new SourceEditSupport.ExceptionalRunnable() {
                                                 public void run() throws SourceException {
                                                     FieldElement el = (FieldElement) element;
                                                     el.getDeclaringClass().removeField(el);
                                                 }
                                             });
        super.destroy();
    }

    public Component getCustomizer() {
        return new FieldCustomizer((FieldElement)element);
    }

    public boolean hasCustomizer() {
        return true;
    }

    /** Create a property for the field type.
    * @param canW <code>false</code> to force property to be read-only
    * @return the property
    */
    protected Node.Property createTypeProperty(boolean canW) {
        return new ElementProp(PROP_TYPE, Type.class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return ((FieldElement)element).getType();
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Type))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         ((FieldElement)element).setType((Type)val);
                                     }
                                 });
                   }
               };
    }

    /** Create a property for the field init value.
    * @param canW <code>false</code> to force property to be read-only
    * @return the property
    */
    protected Node.Property createInitValueProperty(boolean canW) {
        return new ElementProp(PROP_INIT_VALUE, String.class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return ((FieldElement)element).getInitValue();
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof String))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         ((FieldElement)element).setInitValue((String)val);
                                     }
                                 });
                   }
               };
    }
}
/*
* Log
*  11   src-jtulach1.10        11/29/99 Petr Hamernik   customizers
*  10   src-jtulach1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    src-jtulach1.8         9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  8    src-jtulach1.7         7/6/99   Jesse Glick     Removing unused imports.
*  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  6    src-jtulach1.5         5/5/99   Petr Hamernik   init value property
*  5    src-jtulach1.4         4/30/99  Petr Hamernik   property editors moved 
*       away
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         4/1/99   Jan Jancura     
*  2    src-jtulach1.1         3/18/99  Petr Hamernik   
*  1    src-jtulach1.0         3/18/99  Petr Hamernik   
* $
*/
