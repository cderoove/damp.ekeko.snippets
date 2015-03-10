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

import java.io.IOException;
import java.beans.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Node representing an initializer (static or nonstatic).
* @see InitializerElement
*
* @author Petr Hamernik
*/
public class InitializerElementNode extends ElementNode {
    /** Return value of getIconAffectingProperties method. */
    private static final String[] ICON_AFFECTING_PROPERTIES = new String[] {
                PROP_STATIC
            };

    /** Create a new initializer node.
    * @param element initializer element to represent
    * @param writeable <code>true</code> to be writable
    */
    public InitializerElementNode(InitializerElement element, boolean writeable) {
        super(element, Children.LEAF, writeable);
        setElementFormat (sourceOptions.getInitializerElementFormat());
        superSetName("<initializer>"); // NOI18N
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        return ((InitializerElement)element).isStatic() ? INITIALIZER_ST : INITIALIZER;
    }

    /* This method is used for resolving the names of the properties,
    * which could affect the icon (such as "modifiers").
    * @return the appropriate array.
    */
    protected String[] getIconAffectingProperties() {
        return ICON_AFFECTING_PROPERTIES;
    }

    /* This method resolve the appropriate hint format for the type
    * of the element. It defines the short description.
    */
    protected ElementFormat getHintElementFormat() {
        return sourceOptions.getInitializerElementLongFormat();
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createStaticProperty(writeable));
        return sheet;
    }

    /** Indicate that this node cannot be renamed.
    * An initializer has no name.
    * @return <code>false</code>
    */
    public boolean canRename() {
        return false;
    }

    /* Removes the element from the class and calls superclass.
    *
    * @exception IOException if SourceException is thrown
    *            from the underlayed Element.
    */
    public void destroy() throws IOException {
        SourceEditSupport.invokeAtomicAsUser(element, new SourceEditSupport.ExceptionalRunnable() {
                                                 public void run() throws SourceException {
                                                     InitializerElement el = (InitializerElement) element;
                                                     el.getDeclaringClass().removeInitializer(el);
                                                 }
                                             });
        super.destroy();
    }

    /** Create a property for whether or not the initializer is static.
    * @param canW <code>false</code> to force property to be read-only
    * @return the property
    */
    protected Node.Property createStaticProperty(boolean canW) {
        return new ElementProp(ElementProperties.PROP_STATIC, Boolean.TYPE, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return new Boolean(((InitializerElement)element).isStatic());
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);

                       if (!(val instanceof Boolean))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         ((InitializerElement)element).setStatic(((Boolean)val).booleanValue());
                                     }
                                 });
                   }
               };
    }
}

/*
* Log
*  8    src-jtulach1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    src-jtulach1.6         9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  6    src-jtulach1.5         7/6/99   Jesse Glick     Removing unused imports.
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         4/1/99   Jan Jancura     Object Browser support
*  2    src-jtulach1.1         3/18/99  Petr Hamernik   
*  1    src-jtulach1.0         3/18/99  Petr Hamernik   
* $
*/
