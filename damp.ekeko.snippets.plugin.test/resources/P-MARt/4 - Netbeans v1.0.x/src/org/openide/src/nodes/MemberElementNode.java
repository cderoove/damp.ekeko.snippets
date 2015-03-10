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
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.editors.ModifierEditor;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Node representing some type of member element.
*
* @author Petr Hamernik
*/
public abstract class MemberElementNode extends ElementNode {
    /** Create a new node.
    *
    * @param element member element to represent
    * @param children list of children
    * @param writeable <code>true</code> to be writable
    */
    public MemberElementNode(MemberElement element, Children children, boolean writeable) {
        super(element, children, writeable);
        superSetName(element.getName().getName());
    }

    /** Set the node's (system) name.
    * Attempts to change the element's name as well using {@link MemberElement#setName}.
    * Read-only elements cannot have their name set.
    * The display name will also be updated according to the proper format,
    * if necessary (typically it will be).
    *
    * @param str the new element and node name
    */
    public void setName(final String str) {
        try {
            if (testJavaId(str)) {
                SourceEditSupport.invokeAtomicAsUser(element, new SourceEditSupport.ExceptionalRunnable() {
                                                         public void run() throws SourceException {
                                                             ((MemberElement)element).setName(Identifier.create(str));
                                                             superSetName(str);
                                                         }
                                                     });
            }
        }
        catch (IOException e) {
            MessageFormat fmt = new MessageFormat(bundle.getString("MSG_ElementCantRename"));
            String[] params = new String[] { ((MemberElement)element).getName().toString(), e.getMessage() };
            if (params[1] == null)
                params[1] = ""; // NOI18N

            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(fmt.format(params), NotifyDescriptor.ERROR_MESSAGE)
            );
        }
    }

    /** Tests if the given string is java identifier and if not, notifies
    * the user.
    * @return true if it is ok.
    */
    boolean testJavaId(String str) {
        boolean ok = Utilities.isJavaIdentifier(str);
        if (!ok) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(bundle.getString("MSG_Not_Valid_Identifier"),
                                             NotifyDescriptor.ERROR_MESSAGE)
            );
        }
        return ok;
    }

    /** Create a node property for the modifiers of the element.
    * This property will typically display with a custom editor
    * allowing individual modifiers to be examined.
    * @param canW if <code>false</code>, the property will be read-only irrespective of
    *       the underlying element's ability to change the modifiers
    * @return the property
    */
    protected Node.Property createModifiersProperty(boolean canW) {
        return new ElementProp(PROP_MODIFIERS, Integer.class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return new Integer(((MemberElement) element).getModifiers());
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);

                       if (!(val instanceof Integer))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         ((MemberElement)element).setModifiers(((Integer)val).intValue());
                                     }
                                 });
                   }

                   /** Define property editor for this property. */
                   public PropertyEditor getPropertyEditor () {
                       return new ModifierEditor(((MemberElement)element).getModifiersMask());
                   }
               };
    }

    /** Create a node property representing the element's name.
    * @param canW if <code>false</code>, property will be read-only
    * @return the property.
    */
    protected Node.Property createNameProperty(boolean canW) {
        return new ElementProp(ElementProperties.PROP_NAME, String.class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       return ((MemberElement)element).getName().getName();
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof String))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         String sourceName = ((String) val).trim();
                                         String fullName = sourceName;

                                         String prevName = ((MemberElement)element).getName().getFullName();
                                         int dot = prevName.lastIndexOf("."); // NOI18N
                                         if (dot != -1) {
                                             fullName = prevName.substring(0, dot + 1) + sourceName;
                                         }

                                         if (testJavaId(sourceName)) {
                                             ((MemberElement)element).setName(Identifier.create(fullName, sourceName));
                                         }
                                     }
                                 });
                   }
               };
    }
}

/*
* Log
*  15   src-jtulach1.14        1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  14   src-jtulach1.13        1/9/00   Petr Hamernik   user fault tolerance - 
*       trim() is used in the string properties (name, superclass)
*  13   src-jtulach1.12        1/8/00   Petr Hamernik   fixed 2791
*  12   src-jtulach1.11        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  11   src-jtulach1.10        9/24/99  Petr Hamernik   for renaming of elements 
*       is used runAtomicAsUser
*  10   src-jtulach1.9         9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  9    src-jtulach1.8         7/13/99  Petr Hamernik   ConstrainedModifiers 
*       removed
*  8    src-jtulach1.7         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    src-jtulach1.6         5/12/99  Petr Hamernik   ide.src.Identifier 
*       changed
*  6    src-jtulach1.5         4/30/99  Petr Hamernik   property editors moved 
*       away
*  5    src-jtulach1.4         4/21/99  Petr Hamernik   renaming checks the 
*       JavaIdentifier
*  4    src-jtulach1.3         4/20/99  Petr Hamernik   setName improved
*  3    src-jtulach1.2         4/20/99  Petr Hamernik   small bugfix - setName 
*       calling
*  2    src-jtulach1.1         4/2/99   Jesse Glick     [JavaDoc]
*  1    src-jtulach1.0         3/18/99  Petr Hamernik   
* $
*/
