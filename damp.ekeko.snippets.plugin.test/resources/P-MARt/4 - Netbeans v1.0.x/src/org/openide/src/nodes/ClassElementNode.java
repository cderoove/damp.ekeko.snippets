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
import java.util.Set;
import java.util.Arrays;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;

import org.openide.TopManager;
import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.*;

/** Node representing a Java class.
* @see ClassElement
*
* @author Petr Hamernik
*/
public class ClassElementNode extends MemberElementNode {

    /** Return value of getIconAffectingProperties method. */
    private static final String[] ICON_AFFECTING_PROPERTIES = new String[] {
                PROP_CLASS_OR_INTERFACE
            };

    /** Menu labels */
    private static final String MENU_CREATE_BLOCK;
    private static final String MENU_CREATE_VARIABLE;
    private static final String MENU_CREATE_CONSTRUCTOR;
    private static final String MENU_CREATE_METHOD;
    private static final String MENU_CREATE_CLASS;
    private static final String MENU_CREATE_INTERFACE;

    static {
        ResourceBundle bundle = NbBundle.getBundle(ClassElementNode.class);
        MENU_CREATE_BLOCK = bundle.getString("MENU_CREATE_BLOCK");
        MENU_CREATE_VARIABLE = bundle.getString("MENU_CREATE_VARIABLE");
        MENU_CREATE_CONSTRUCTOR = bundle.getString("MENU_CREATE_CONSTRUCTOR");
        MENU_CREATE_METHOD = bundle.getString("MENU_CREATE_METHOD");
        MENU_CREATE_CLASS = bundle.getString("MENU_CREATE_CLASS");
        MENU_CREATE_INTERFACE = bundle.getString("MENU_CREATE_INTERFACE");
    }

    /** Create a new class node.
    * @param element class element to represent
    * @param children node children
    * @param writeable <code>true</code> to be writable
    */
    public ClassElementNode(ClassElement element, Children children, boolean writeable) {
        super(element, children, writeable);
        setElementFormat (((ClassElement)element).isInterface() ?
                          sourceOptions.getInterfaceElementFormat() :
                          sourceOptions.getClassElementFormat());
    }

    /* Resolve the current icon base.
    * @return icon base string.
    */
    protected String resolveIconBase() {
        return ((ClassElement)element).isInterface() ? INTERFACE : CLASS;
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
        return ((ClassElement)element).isInterface() ?
               sourceOptions.getInterfaceElementLongFormat() :
               sourceOptions.getClassElementLongFormat();
    }

    /* Creates property set for this node */
    protected Sheet createSheet () {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get(Sheet.PROPERTIES);
        ps.put(createModifiersProperty(writeable));
        ps.put(createNameProperty(writeable));
        if (((ClassElement)element).isClass())
            ps.put(createSuperclassProperty(writeable));
        ps.put(createInterfacesProperty(writeable));
        return sheet;
    }

    /** Remove this class from its declaring class or source file.
    *
    * @exception IOException if the containing element refuses to delete it
    */
    public void destroy() throws IOException {
        SourceEditSupport.invokeAtomicAsUser(element, new SourceEditSupport.ExceptionalRunnable() {
                                                 public void run() throws SourceException {
                                                     ClassElement el = (ClassElement) element;
                                                     if (el.getDeclaringClass() != null) {
                                                         el.getDeclaringClass().removeClass(el);
                                                     }
                                                     else {
                                                         el.getSource().removeClass(el);
                                                     }
                                                 }
                                             });
        super.destroy();
    }

    public Component getCustomizer() {
        return new ClassCustomizer((ClassElement)element);
    }

    public boolean hasCustomizer() {
        return true;
    }

    /* Accumulate the paste types that this node can handle
    * for a given transferable.
    * <P>
    * The default implementation simply tests whether the transferable supports
    * {@link NodeTransfer#nodePasteFlavor}, and if so, it obtains the paste types
    * from the {@link NodeTransfer.Paste transfer data} and inserts them into the set.
    *
    * @param t a transferable containing clipboard data
    * @param s a set of {@link PasteType}s that will have added to it all types
    *    valid for this node
    */
    protected void createPasteTypes (final Transferable t, java.util.List s) {
        if (writeable) {
            for (int i = 0; i <= 1; i++) {
                final boolean delete = (i == 1);
                final Element addingElement = (Element) NodeTransfer.cookie(t,
                                              delete ? NodeTransfer.MOVE : NodeTransfer.COPY, Element.class);

                if (addingElement != null) {
                    s.add(new PasteType() {
                              public Transferable paste() throws IOException {
                                  pasteElement(addingElement, delete);
                                  return delete ? ExTransferable.EMPTY : null;
                              }
                          });
                }
            }
        }
        super.createPasteTypes(t, s);
    }

    /** Paste element into this class.
    * @param addingElement Element to add.
    * @param delete Whether element should be deleted from the original class
    * @exception IOException if any proble occured
    */
    void pasteElement(final Element addingElement, final boolean delete) throws IOException {
        SourceEditSupport.invokeAtomicAsUser(element, new SourceEditSupport.ExceptionalRunnable() {
                                                 public void run() throws SourceException {
                                                     ClassElement clazz = (ClassElement) element;
                                                     if (addingElement instanceof InitializerElement) {
                                                         InitializerElement e = (InitializerElement)addingElement;
                                                         clazz.addInitializer(e);
                                                     }
                                                     if (addingElement instanceof FieldElement) {
                                                         clazz.addField((FieldElement)addingElement);
                                                     }
                                                     else if (addingElement instanceof MethodElement) {
                                                         clazz.addMethod((MethodElement)addingElement);
                                                     }
                                                     else if (addingElement instanceof ConstructorElement) {
                                                         clazz.addConstructor((ConstructorElement)addingElement);
                                                     }
                                                     else if (addingElement instanceof ClassElement) {
                                                         clazz.addClass((ClassElement)addingElement);
                                                     }
                                                 }
                                             });
        if (delete) {
	    final ClassElement origClazz;
	    final SourceElement src;
	    
	    if (addingElement instanceof InitializerElement) {
		origClazz = ((InitializerElement)addingElement).getDeclaringClass();
	    } else if (addingElement instanceof ClassElement) {
		origClazz = (ClassElement)addingElement;
	    } else if (addingElement instanceof MemberElement) {
		origClazz = ((MemberElement)addingElement).getDeclaringClass();
	    } else {
		origClazz = null;
	    }

	    if (origClazz != null) {
		src = origClazz.getSource();
	    } else {
		src = null;
	    }
	    
	    SourceEditSupport.ExceptionalRunnable r = new SourceEditSupport.ExceptionalRunnable() {
                 public void run() throws SourceException {
                    if (addingElement instanceof InitializerElement) {
                        InitializerElement e = (InitializerElement)addingElement;
                        if (origClazz != null)
                            origClazz.removeInitializer(e);
                    } else if (addingElement instanceof MemberElement) {
                        if (origClazz != null) {
                            if (addingElement instanceof FieldElement) {
                                origClazz.removeField((FieldElement)addingElement);
                            } else if (addingElement instanceof MethodElement) {
                                origClazz.removeMethod((MethodElement)addingElement);
                            } else if (addingElement instanceof ConstructorElement) {
                                origClazz.removeConstructor((ConstructorElement)addingElement);
                            } else if (addingElement instanceof ClassElement) {
				ClassElement parent = origClazz.getDeclaringClass();
				if (parent == null) {
				    if (src != null) {
					src.removeClass((ClassElement)addingElement);
				    }
				} else {
				    parent.removeClass((ClassElement)addingElement);
				}
                            }
	                }
                    }
                }
	    };
	    if (src == null) {
		try {
		    r.run();
		} catch (SourceException e) {
		    throw new IOException(e.getMessage());
		}
	    } else {
        	SourceEditSupport.invokeAtomicAsUser(addingElement, r);
	    }
         }
    }


    /** Create a node property for the superclass of this class.
    * @param canW if <code>false</code>, property will be read-only
    * @return the property
    */
    protected Node.Property createSuperclassProperty(boolean canW) {
        return new ElementProp(PROP_SUPERCLASS, String.class, canW) {
                   /** Gets the value */
                   public Object getValue () {
                       Identifier id = ((ClassElement)element).getSuperclass();
                       return id == null ? "" : id.getFullName(); // NOI18N
                   }

                   /** Sets the value */
                   public void setValue(final Object val) throws IllegalArgumentException,
                       IllegalAccessException, InvocationTargetException {
                       super.setValue(val);
                       if (!(val instanceof String))
                           throw new IllegalArgumentException();

                       runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                     public void run() throws SourceException {
                                         String str = ((String) val).trim();
                                         Identifier superclass = str.equals("") ? null: Identifier.create(str); // NOI18N
                                         ((ClassElement)element).setSuperclass(superclass);
                                     }
                                 });
                   }
               };
    }

    /** Create a node property for the implemented interfaces of this class.
    * (Or, extended interfaces if this is itself an interface.)
    * @param canW if <code>false</code>, property will be read-only
    * @return the property
    */
    protected Node.Property createInterfacesProperty(boolean canW) {
        ElementProp prop = new ElementProp(PROP_INTERFACES, Identifier[].class, canW) {
                               /** Gets the value */
                               public Object getValue () {
                                   return ((ClassElement)element).getInterfaces();
                               }

                               /** Sets the value */
                               public void setValue(final Object val) throws IllegalArgumentException,
                                   IllegalAccessException, InvocationTargetException {
                                   super.setValue(val);
                                   if (!(val instanceof Identifier[]))
                                       throw new IllegalArgumentException();

                                   runAtomic(element, new SourceEditSupport.ExceptionalRunnable() {
                                                 public void run() throws SourceException {
                                                     ((ClassElement)element).setInterfaces((Identifier[])val);
                                                 }
                                             });
                               }
                           };

        if (((ClassElement)element).isInterface()) {
            prop.setDisplayName(bundle.getString("PROP_superInterfaces"));
            prop.setShortDescription(bundle.getString("HINT_superInterfaces"));
        }
        return prop;
    }

    /* Get the new types that can be created in this node.
    * For example, a node representing a Java package will permit classes to be added.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes() {
        if (writeable) {
            return SourceEditSupport.createNewTypes((ClassElement)element);
        }
        else {
            // no new types
            return super.getNewTypes();
        }
    }
}

/*
* Log
*  22   src-jtulach1.21        1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  21   src-jtulach1.20        1/9/00   Petr Hamernik   user fault tolerance - 
*       trim() is used in the string properties (name, superclass)
*  20   src-jtulach1.19        11/29/99 Petr Hamernik   customizers
*  19   src-jtulach1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  18   src-jtulach1.17        9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  17   src-jtulach1.16        7/6/99   Jesse Glick     Removing unused imports.
*  16   src-jtulach1.15        7/1/99   Petr Hamernik   Clipboard operations on 
*       source Elements
*  15   src-jtulach1.14        6/28/99  Petr Hamernik   new hierarchy under 
*       ClassChildren
*  14   src-jtulach1.13        6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  13   src-jtulach1.12        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  12   src-jtulach1.11        5/14/99  Petr Hamernik   NewTypes improved (Class 
*       x Interface new types)
*  11   src-jtulach1.10        5/12/99  Petr Hamernik   Identifier implementation
*       updated
*  10   src-jtulach1.9         5/10/99  Petr Hamernik   New types improved
*  9    src-jtulach1.8         4/30/99  Petr Hamernik   property editors moved 
*       away
*  8    src-jtulach1.7         4/27/99  Jesse Glick     new HelpCtx () -> 
*       HelpCtx.DEFAULT_HELP.
*  7    src-jtulach1.6         4/21/99  Petr Hamernik   superclass property bug 
*       fixed
*  6    src-jtulach1.5         4/20/99  Petr Hamernik   superclass property 
*       changed
*  5    src-jtulach1.4         4/2/99   Jesse Glick     [JavaDoc]
*  4    src-jtulach1.3         4/1/99   Jan Jancura     Object browser support
*  3    src-jtulach1.2         3/19/99  Jaroslav Tulach 
*  2    src-jtulach1.1         3/18/99  Petr Hamernik   
*  1    src-jtulach1.0         3/18/99  Petr Hamernik   
* $
*/
