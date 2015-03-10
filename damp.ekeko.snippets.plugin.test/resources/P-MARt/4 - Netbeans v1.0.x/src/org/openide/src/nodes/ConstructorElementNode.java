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

/** Node representing a constructor.
* @see ConstructorElement
* @author Petr Hamernik
*/
public class ConstructorElementNode extends MemberElementNode {
    /** Create a new constructor node.
    * @param element constructor element to represent
    * @param writeable <code>true</code> to be writable
    */
    public ConstructorElementNode(ConstructorElement element, boolean writeable) {
        super(element, Children.LEAF, writeable);
        setElementFormat (sourceOptions.getConstructorElementFormat());
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        int modif = ((ConstructorElement)element).getModifiers();
        if (Modifier.isPrivate(modif))
            return CONSTRUCTOR_PRIVATE;
        else if (Modifier.isProtected(modif))
            return CONSTRUCTOR_PROTECTED;
        else if (Modifier.isPublic(modif))
            return CONSTRUCTOR_PUBLIC;
        else
            return CONSTRUCTOR_PACKAGE;
    }

    /* This method resolve the appropriate hint format for the type
    * of the element. It defines the short description.
    */
    protected ElementFormat getHintElementFormat() {
        return sourceOptions.getConstructorElementLongFormat();
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createModifiersProperty(writeable));
        ps.put(createNameProperty(false));
        ps.put(createParametersProperty(writeable));
        ps.put(createExceptionsProperty(writeable));
        return sheet;
    }

    /* Removes the element from the class and calls superclass.
    *
    * @exception IOException if SourceException is thrown
    *            from the underlayed Element.
    */
    public void destroy() throws IOException {
        if (!(element instanceof MethodElement)) {
            SourceEditSupport.invokeAtomicAsUser(element, new SourceEditSupport.ExceptionalRunnable() {
                                                     public void run() throws SourceException {
                                                         ConstructorElement el = (ConstructorElement) element;
                                                         el.getDeclaringClass().removeConstructor(el);
                                                     }
                                                 });
        }
        super.destroy();
    }

    /** Indicate that this node cannot be renamed.
    * An constructor must have the same name like class
    * @return <code>false</code>
    */
    public boolean canRename() {
        return false;
    }

    public Component getCustomizer() {
        return new MethodCustomizer((ConstructorElement)element);
    }

    public boolean hasCustomizer() {
        return true;
    }

    /** Create a node property for constructor parameters.
    * @param canW <code>false</code> to force property to be read-only
    * @return the property
    */
    protected Node.Property createParametersProperty(boolean canW) {
        return new ElementProp(PROP_PARAMETERS, MethodParameter[].class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return ((ConstructorElement)element).getParameters();
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof MethodParameter[]))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         ((ConstructorElement)element).setParameters((MethodParameter[])val);
                                     }
                                 });
                   }
               };
    }

    /** Create a node property for constructor exceptions.
    * @param canW <code>false</code> to force property to be read-only
    * @return the property
    */
    protected Node.Property createExceptionsProperty(boolean canW) {
        return new ElementProp(PROP_EXCEPTIONS, Identifier[].class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return ((ConstructorElement)element).getExceptions();
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof Identifier[]))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         ((ConstructorElement)element).setExceptions((Identifier[])val);
                                     }
                                 });
                   }
               };
    }
}

/*
* Log
*  12   src-jtulach1.11        11/29/99 Petr Hamernik   customizers
*  11   src-jtulach1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   src-jtulach1.9         9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  9    src-jtulach1.8         7/6/99   Jesse Glick     Removing unused imports.
*  8    src-jtulach1.7         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    src-jtulach1.6         4/30/99  Petr Hamernik   property editors moved 
*       away
*  6    src-jtulach1.5         4/21/99  Petr Hamernik   canRename returns false
*  5    src-jtulach1.4         4/20/99  Petr Hamernik   constructor name is 
*       read-only
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         4/1/99   Jan Jancura     Object browser support
*  2    src-jtulach1.1         3/18/99  Petr Hamernik   
*  1    src-jtulach1.0         3/18/99  Petr Hamernik   
* $
*/
