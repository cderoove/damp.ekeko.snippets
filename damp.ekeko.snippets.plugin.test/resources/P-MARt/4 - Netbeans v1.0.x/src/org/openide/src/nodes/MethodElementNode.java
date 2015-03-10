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

/** Node for a method element.
* @see MethodElement
* @author Petr Hamernik
*/
public class MethodElementNode extends ConstructorElementNode {
    /** Create a new method node.
    * @param element method element to represent
    * @param writeable <code>true</code> to be writable
    */
    public MethodElementNode(MethodElement element, boolean writeable) {
        super(element, writeable);
        setElementFormat(sourceOptions.getMethodElementFormat());
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        int modif = ((MethodElement)element).getModifiers();
        if (Modifier.isStatic(modif)) {
            // static method...
            if (Modifier.isPrivate(modif))
                return METHOD_ST_PRIVATE;
            else if (Modifier.isProtected(modif))
                return METHOD_ST_PROTECTED;
            else if (Modifier.isPublic(modif))
                return METHOD_ST_PUBLIC;
            else
                return METHOD_ST_PACKAGE;
        }
        else {
            // non-static method...
            if (Modifier.isPrivate(modif))
                return METHOD_PRIVATE;
            else if (Modifier.isProtected(modif))
                return METHOD_PROTECTED;
            else if (Modifier.isPublic(modif))
                return METHOD_PUBLIC;
            else
                return METHOD_PACKAGE;
        }
    }

    /** Indicate that this node cannot be renamed.
    * An constructor must have the same name like class
    * @return <code>true</code>
    */
    public boolean canRename() {
        return true;
    }


    /* This method resolve the appropriate hint format for the type
    * of the element. It defines the short description.
    */
    protected ElementFormat getHintElementFormat() {
        return sourceOptions.getMethodElementLongFormat();
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createModifiersProperty(writeable));
        ps.put(createNameProperty(writeable));
        ps.put(createParametersProperty(writeable));
        ps.put(createReturnProperty(writeable));
        ps.put(createExceptionsProperty(writeable));
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
                                                     MethodElement el = (MethodElement) element;
                                                     el.getDeclaringClass().removeMethod(el);
                                                 }
                                             });
        super.destroy();
    }

    /** Create a property for the method return value.
    * @param canW <code>false</code> to force property to be read-only
    * @return the property
    */
    protected Node.Property createReturnProperty(boolean canW) {
        return new ElementProp(PROP_RETURN, Type.class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return ((MethodElement)element).getReturn();
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Type))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         ((MethodElement)element).setReturn((Type)val);
                                     }
                                 });
                   }
               };
    }
}

/*
* Log
*  11   src-jtulach1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   src-jtulach1.9         9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  9    src-jtulach1.8         7/18/99  Petr Hamernik   canRename bugfix + 
*       display name correction
*  8    src-jtulach1.7         7/6/99   Jesse Glick     Removing unused imports.
*  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  6    src-jtulach1.5         4/30/99  Petr Hamernik   property editors moved 
*       away
*  5    src-jtulach1.4         4/2/99   Jesse Glick     [JavaDoc]
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         4/1/99   Jan Jancura     Object Browser support
*  2    src-jtulach1.1         3/18/99  Petr Hamernik   
*  1    src-jtulach1.0         3/18/99  Petr Hamernik   
* $
*/
