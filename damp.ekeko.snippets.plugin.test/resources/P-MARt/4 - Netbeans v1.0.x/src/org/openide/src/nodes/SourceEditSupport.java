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
import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.Set;

import org.openide.*;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;

/** This class defines utilities for editing source using hierarchy API,
* e.g. creation new types for class elements, runAtomicAsUser support, ...
*
* @author Petr Hamernik
*/
class SourceEditSupport {

    static final ResourceBundle bundle = NbBundle.getBundle(SourceEditSupport.class);

    static final String[] MENU_NAMES = {
        bundle.getString("MENU_CREATE_BLOCK"), bundle.getString("MENU_CREATE_VARIABLE"),
        bundle.getString("MENU_CREATE_CONSTRUCTOR"), bundle.getString("MENU_CREATE_METHOD"),
        bundle.getString("MENU_CREATE_CLASS"), bundle.getString("MENU_CREATE_INTERFACE")
    };

    /* Get the new types that can be created in this node.
    * For example, a node representing a Java package will permit classes to be added.
    * @return array of new type operations that are allowed
    */
    public static NewType[] createNewTypes(ClassElement element) {
        if (element.isClass()) {
            // class new types
            return new NewType[] {
                       new ElementNewType(element, (byte) 0),
                       new ElementNewType(element, (byte) 1),
                       new ElementNewType(element, (byte) 2),
                       new ElementNewType(element, (byte) 3),
                       new ElementNewType(element, (byte) 4),
                       new ElementNewType(element, (byte) 5)
                   };
        }
        else {
            // interface new types
            return new NewType[] {
                       new ElementNewType(element, (byte) 1),
                       new ElementNewType(element, (byte) 3),
                       new ElementNewType(element, (byte) 4),
                       new ElementNewType(element, (byte) 5)
                   };
        }
    }

    /** New types for class element */
    static class ElementNewType extends NewType {
        /** Class element where to create new element */
        ClassElement element;

        /** The kind of element to create */
        byte kind;

        /** Creates new type
        * @param element Where to create new element.
        * @param kind The kind of the element to create
        */
        public ElementNewType(ClassElement element, byte kind) {
            this.element = element;
            this.kind = kind;
        }

        /** Get the name of the new type.
        * @return localized name.
        */
        public String getName() {
            return MENU_NAMES[kind];
        }

        /** Help context */
        public org.openide.util.HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx (SourceEditSupport.class.getName () + ".newElement" + kind); // NOI18N
        }

        /** Creates new element */
        public void create () throws IOException {
            final Identifier outerName = element.getName();
            final boolean outerIsClass = element.isClass();

            Element newElement = null;

            try {
                switch (kind) {
                case 0:
                    {
                        // Adding initializer
                        InitializerElement e = new InitializerElement();
                        e.setStatic(true);
                        e.setBody("\n"); // NOI18N
                        newElement = e;
                        break;
                    }
                case 1:
                    {
                        // Adding field
                        FieldElement e = new FieldElement();
                        e.setType(Type.INT);
                        e.setName(Identifier.create("newField")); // NOI18N
                        e.setModifiers(Modifier.PUBLIC + (outerIsClass ? 0 : Modifier.STATIC));
                        if (openCustomizer(new FieldCustomizer(e), "TIT_NewField")) // NOI18N
                            newElement = e;
                        break;
                    }
                case 2:
                    {
                        // Adding constructor
                        ConstructorElement e = new ConstructorElement();
                        e.setName(Identifier.create(((ClassElement)element).getName().getName()));
                        e.setModifiers(Modifier.PUBLIC);
                        e.setBody("\n"); // NOI18N
                        if (openCustomizer(new MethodCustomizer(e), "TIT_NewConstructor")) // NOI18N
                            newElement = e;
                        break;
                    }
                case 3:
                    {
                        // Adding method
                        MethodElement e = new MethodElement();
                        e.setReturn(Type.VOID);
                        e.setName(Identifier.create("newMethod")); // NOI18N
                        e.setModifiers(Modifier.PUBLIC);
                        e.setBody(outerIsClass ? "\n" : null); // NOI18N
                        if (openCustomizer(new MethodCustomizer(e), "TIT_NewMethod")) // NOI18N
                            newElement = e;
                        break;
                    }
                case 4:
                    {
                        // Adding inner class
                        ClassElement e = new ClassElement();
                        e.setName(Identifier.create(outerName.getFullName() + ".InnerClass", "InnerClass")); // NOI18N
                        e.setModifiers(Modifier.PUBLIC);
                        e.setClassOrInterface(true);
                        if (openCustomizer(new ClassCustomizer(e), "TIT_NewClass")) // NOI18N
                            newElement = e;
                        break;
                    }
                case 5:
                    {
                        // Adding inner interface
                        ClassElement e = new ClassElement();
                        e.setName(Identifier.create(outerName.getFullName() + ".InnerInterface", "InnerInterface")); // NOI18N
                        e.setModifiers(Modifier.PUBLIC);
                        e.setClassOrInterface(false);
                        if (openCustomizer(new ClassCustomizer(e), "TIT_NewInterface")) // NOI18N
                            newElement = e;
                        break;
                    }
                }
            }
            catch (SourceException exc) {
                // shouldn't happen - memory implementation
                // is not based on java source.
            }

            if (newElement == null)
                return;

            final Element addingElement = newElement;

            SourceEditSupport.invokeAtomicAsUser(element, new SourceEditSupport.ExceptionalRunnable() {
                                                     public void run() throws SourceException {
                                                         switch (kind) {
                                                         case 0:
                                                             ((ClassElement)element).addInitializer((InitializerElement)addingElement);
                                                             return;
                                                         case 1:
                                                             ((ClassElement)element).addField((FieldElement)addingElement);
                                                             return;
                                                         case 2:
                                                             ((ClassElement)element).addConstructor((ConstructorElement)addingElement);
                                                             return;
                                                         case 3:
                                                             ((ClassElement)element).addMethod((MethodElement)addingElement);
                                                             return;
                                                         case 4:
                                                         case 5:
                                                             element.addClass((ClassElement)addingElement);
                                                             return;
                                                         }
                                                     }
                                                 });
        }
    }

    /** Show dialog and allow user to modify new element.
    * @param customizer The component to be displayed
    * @param titleKey the key to resource bundle for the title of dialog
    * @return <CODE>true</CODE> if user pressed OK button,
    *     otherwise <CODE>false</CODE> (for CANCEL)
    */
    static boolean openCustomizer(Component customizer, String titleKey) {
        NotifyDescriptor desriptor = new NotifyDescriptor(
                                         customizer,
                                         ElementNode.bundle.getString(titleKey),
                                         NotifyDescriptor.OK_CANCEL_OPTION,
                                         NotifyDescriptor.PLAIN_MESSAGE,
                                         null, null);

        Object ret = TopManager.getDefault().notify(desriptor);
        return (ret == NotifyDescriptor.OK_OPTION);
    }

    /** Invokes the runnable using NbDocument.runAtomicAsUser.
    * @exception IOException If SourceException occured inside the runnable.
    */
    static void invokeAtomicAsUser(Element element, final ExceptionalRunnable exRun) throws IOException {
        final SourceException[] ex = { null };
        try {
            SourceElement source = SourceEditSupport.findSource(element);
            Runnable run = new Runnable() {
                               public void run() {
                                   try {
                                       exRun.run();
                                   }
                                   catch (SourceException e) {
                                       ex[0] = e;
                                   }
                               }
                           };
            source.runAtomicAsUser(run);
        }
        catch (SourceException e) {
            ex[0] = e;
        }
        if (ex[0] != null) {
            throw new IOException(ex[0].getMessage());
        }
    }

    /** This interface is used like runnable, but its method run
    * could throw BadLocationException.
    * @exception SourceException
    */
    static interface ExceptionalRunnable {
        public void run() throws SourceException;
    }

    /** Find the source for the specifier element.
    * @exception SourceException if SourceElement cannot be found
    */
    static SourceElement findSource(Element element) throws SourceException {
        SourceElement source = null;
        ClassElement clazz = null;
        if (element instanceof ClassElement) {
            clazz = (ClassElement) element;
        }
        else if (element instanceof MemberElement) {
            clazz = ((MemberElement) element).getDeclaringClass();
        }
        else if (element instanceof InitializerElement) {
            clazz = ((InitializerElement) element).getDeclaringClass();
        }
        else if (element instanceof SourceElement) {
            return (SourceElement) element;
        }
        if (clazz != null) {
            source = clazz.getSource();
            if (source != null)
                return source;
        }
        throw new SourceException();
    }


}

/*
* Log
*  9    Gandalf   1.8         1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  8    Gandalf   1.7         1/11/00  Jesse Glick     Context help.
*  7    Gandalf   1.6         1/5/00   Jaroslav Tulach Deleted all 
*       NotifyDescriptior constructors that take Icon as argument.
*  6    Gandalf   1.5         11/29/99 Petr Hamernik   Adding elements using 
*       customizer
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/5/99  Petr Hamernik   deadlock on Solaris fix
*  3    Gandalf   1.2         9/29/99  Petr Hamernik   ClassCastException fixed
*  2    Gandalf   1.1         9/21/99  Petr Hamernik   when entering new element
*       - user is asked for name.
*  1    Gandalf   1.0         9/13/99  Petr Hamernik   
* $
*/
