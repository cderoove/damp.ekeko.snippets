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

package org.netbeans.modules.editor.java;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import org.netbeans.editor.ext.JCClass;
import org.netbeans.editor.ext.JCType;
import org.netbeans.editor.ext.JCConstructor;
import org.netbeans.editor.ext.JCMethod;
import org.netbeans.editor.ext.JCField;
import org.netbeans.editor.ext.JCParameter;
import org.netbeans.editor.ext.JCompletion;
import org.netbeans.editor.ext.JCFinder;
import org.netbeans.editor.ext.JCCellRenderer;
import org.netbeans.editor.ext.JCQuery;
import org.openide.src.ClassElement;
import org.openide.src.ConstructorElement;
import org.openide.src.MethodElement;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.SourceElement;
import org.openide.src.Type;
import org.openide.src.MethodParameter;
import org.openide.src.nodes.ElementNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.enum.QueueEnumeration;

/**
* Extended Java Completion support
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCExtension {

    public static boolean equals(JCClass cls1, ClassElement cls2) {
        return cls1.getFullName().equals(cls2.getName().getFullName());
    }

    public static boolean equals(JCType typ1, Type typ2) {
        // Get array depth of the type
        int arrayDepth = 0;
        while(typ2.isArray()) {
            arrayDepth++;
            typ2 = typ2.getElementType();
        }

        String fn = typ2.isPrimitive() ? typ2.getFullString()
                    : typ2.getClassName().getFullName();
        return (typ1.getArrayDepth() == arrayDepth)
               && typ1.getClazz().getFullName().equals(fn);
    }

    public static boolean equals(JCField fld1, FieldElement fld2) {
        return fld1.getName().equals(fld2.getName().getFullName())
               && equals(fld1.getType(), fld2.getType());
    }

    public static boolean equals(JCConstructor ctr1, ConstructorElement ctr2) {
        JCParameter[] parms1 = ctr1.getParameters();
        MethodParameter[] parms2 = ctr2.getParameters();
        if (parms2 == null || parms1.length != parms2.length) {
            return false;
        }
        for (int i = parms1.length - 1; i >= 0; i--) {
            if (!equals(parms1[i].getType(), parms2[i].getType())) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(JCMethod mtd1, MethodElement mtd2) {
        return mtd1.getName().equals(mtd2.getName().getFullName())
               && equals((JCConstructor)mtd1, (ConstructorElement)mtd2);
    }

    public static FieldElement findFieldElement(JCField fld, ClassElement ce) {
        FieldElement[] fes = ce.getFields();
        if (fes != null) {
            for (int i = 0; i < fes.length; i++) {
                if (equals(fld, fes[i])) {
                    return fes[i];
                }
            }
        }
        return null;
    }

    public static ConstructorElement findConstructorElement(JCConstructor ctr, ClassElement ce) {
        ConstructorElement[] ces = ce.getConstructors();
        if (ces != null) {
            for (int i = 0; i < ces.length; i++) {
                if (equals(ctr, ces[i])) {
                    return ces[i];
                }
            }
        }
        return null;
    }

    public static MethodElement findMethodElement(JCMethod mtd, ClassElement ce) {
        MethodElement[] mes = ce.getMethods();
        if (mes != null) {
            for (int i = 0; i < mes.length; i++) {
                if (equals(mtd, mes[i])) {
                    return mes[i];
                }
            }
        }
        return null;
    }

    /** Get or create the simple class from identifier.
    * @param i identifier holding the class information
    * @param lookupCache cache holding the [name, simple-class] pairs for already found classes
    * @param preferFinder whether the package name finding should be first tried
    *   through the finder or through the ClassElement.forName()
    */
    static JCClass getIdentifierClass(Identifier i, Map lookupCache, boolean preferFinder) {
        String fn = i.getFullName(); // Get identifier's full name

        // First try the lookupCache
        JCClass cls = (JCClass)lookupCache.get(fn);
        if (cls != null) {
            return cls;
        }

        // Try finder if it's preferred
        if (preferFinder) {
            JCFinder finder = JCompletion.getFinder();
            if (finder != null) {
                cls = finder.getExactClass(fn); // try to find through the current finder
                if (cls != null) {
                    cls = JCompletion.getSimpleClass(cls); // be sure to have simple class
                    lookupCache.put(fn, cls);
                    return cls;
                }
            }
        }

        int pkgLen = -1; // will try to find the correct package length
        // Try to get class element based on fullname
        ClassElement ce;
        try {
            ce = ClassElement.forName(fn);
        } catch (Throwable t) { // Parser sometimes sensitive to forName call !!!
            System.err.println("Error occurred during name resolving"); // NOI18N
            t.printStackTrace();
            return JCompletion.INVALID_CLASS;
        }

        // Try to split the fullname by finding the package of the source element
        if (ce != null) {
            SourceElement se = ce.getSource();
            if (se != null) {
                Identifier pi = null;
                pi = se.getPackage();

                if (pi != null) {
                    String pkgName = pi.getFullName();
                    if (fn.startsWith(pkgName)) { // validity check
                        pkgLen = pkgName.length();
                    }
                }
            }
        }

        // If failed try to resolve through the current finder
        if (pkgLen < 0) {
            JCFinder finder = JCompletion.getFinder();
            if (finder != null) {
                if (!preferFinder) { // already tried for preferFinder == true
                    cls = finder.getExactClass(fn);
                }
                if (cls == null) { // not exact fullname, try by name only
                    List clsList = finder.findClasses(null, fn, true);
                    if (clsList != null && clsList.size() == 1) { // only when exactly one match
                        cls = (JCClass)clsList.get(0);
                    }
                }
                if (cls != null) {
                    cls = JCompletion.getSimpleClass(cls); // be sure to have simple class
                    lookupCache.put(fn, cls);
                    return cls;
                }
            }
        }

        // Last instance - guess the package from the full name by heuristic
        // Go from the last name to the previous if it has a first capital letter
        if (pkgLen < 0) {
            pkgLen = Math.max(fn.lastIndexOf('.'), 0);
            String pkgName = fn.substring(0, pkgLen);
            int lastDotInd = pkgName.lastIndexOf('.');
            if (lastDotInd < fn.length() - 1) { // '.' would be last in fn
                while (lastDotInd >= 0 && Character.isUpperCase(fn.charAt(lastDotInd + 1))) {
                    pkgName = pkgName.substring(0, lastDotInd);
                    pkgLen = lastDotInd;
                    lastDotInd = pkgName.lastIndexOf('.');
                }
            }
        }

        // Get the class
        cls = JCompletion.getSimpleClass(fn, pkgLen);
        lookupCache.put(fn, cls);
        return cls;
    }

    /*
       DEBUG to getIdentifierClass() that test if the class is reachable
              if (pkgName != null && clsName != null) {
                try { // !!! Try to create the class without init
                  Class.forName(pkgName + '.' + clsName.replace('.', '$'), false, JCExtension.class.getClassLoader());
                } catch (ClassNotFoundException e) {
                  System.out.println("Class not found: '" + pkgName + '.' + clsName + "', fn='" + fn + "'"); // NOI18N
                }
              }
    */

    static JCType getType(Type t, Map lookupCache, boolean preferFinder) {
        // Get array depth of the type
        int arrayDepth = 0;
        while(t.isArray()) {
            arrayDepth++;
            t = t.getElementType();
        }

        JCClass c;
        if (t.isPrimitive()) {
            c = JCompletion.getPrimitiveClass(t.getFullString());
        } else {
            c = getIdentifierClass(t.getClassName(), lookupCache, preferFinder);
        }
        return JCompletion.getType(c, arrayDepth);
    }

    /** Create the new instance of JCClass based on the information provided by the class element.
    */
    static JCClass parseClassElement(ClassElement ce, int classLevel, int fieldLevel,
                                     int methodLevel, Map lookupCache, boolean preferFinder) {
        JCClass sc = getIdentifierClass(ce.getName(), lookupCache, preferFinder);
        if (sc.equals(JCompletion.INVALID_CLASS)) { // error occurred
            return null;
        }

        return new NbJCClass(sc.getName(), sc.getPackageName(),
                             ce.isInterface(), ce.getModifiers(),
                             ce, classLevel, fieldLevel, methodLevel, lookupCache, preferFinder);
    }

    static class NbJCClass extends JCompletion.AbstractClass {

        public NbJCClass(String name, String packageName,
                         boolean iface, int modifiers,
                         ClassElement ce, int classLevel, int fieldLevel,
                         int methodLevel, Map lookupCache, boolean preferFinder) {
            super(name, packageName, iface, modifiers);
            body = new Body(); // init directly

            ArrayList lst = new ArrayList();
            Identifier sc = ce.getSuperclass();
            body.superClass = (sc != null) ? getIdentifierClass(sc, lookupCache, preferFinder)
                              : (iface ? JCompletion.INVALID_CLASS : JCompletion.OBJECT_CLASS);

            // Get interfaces
            Identifier[] cei = ce.getInterfaces();
            body.interfaces = JCompletion.EMPTY_CLASSES;
            if (cei != null) {
                for (int i = 0; i < cei.length; i++) {
                    if (cei[i] != null) {
                        //          if (getLevel(cei[i].getModifiers()) >= classLevel) { NOT SPECIF  IED IN SOURCE
                        lst.add(getIdentifierClass(cei[i], lookupCache, preferFinder));
                        //          }
                    }
                }
                body.interfaces = new JCClass[lst.size()];
                lst.toArray(body.interfaces);
                lst.clear();
            }

            // Get fields
            FieldElement[] cef = ce.getFields();
            body.fields = JCompletion.EMPTY_FIELDS;
            if (cef != null) {
                for (int i = 0; i < cef.length; i++) {
                    if (cef[i] != null) {
                        if (JCompletion.getLevel(cef[i].getModifiers()) >= fieldLevel) {
                            lst.add(new JCompletion.BaseField(this, cef[i].getName().getName(),
                                                              getType(cef[i].getType(), lookupCache, preferFinder),
                                                              cef[i].getModifiers())
                                   );
                        }
                    }
                }
                body.fields = new JCField[lst.size()];
                lst.toArray(body.fields);
                lst.clear();
            }

            // Get constructors
            ConstructorElement[] cec = ce.getConstructors();
            body.constructors = JCompletion.EMPTY_CONSTRUCTORS;
            if (cec != null) {
                for (int i = 0; i < cec.length; i++) {
                    if (cec[i] != null) {
                        if (JCompletion.getLevel(cec[i].getModifiers()) >= methodLevel) {
                            // get constructor parameters
                            JCParameter[] parameters = JCompletion.EMPTY_PARAMETERS;
                            MethodParameter[] cep = cec[i].getParameters();
                            if (cep != null) {
                                parameters = new JCParameter[cep.length];
                                for (int j = 0; j < cep.length; j++) {
                                    int type;
                                    parameters[j] = new JCompletion.BaseParameter(
                                                        cep[j].getName(),
                                                        getType(cep[j].getType(), lookupCache, preferFinder)
                                                    );
                                }
                            }

                            // get thrown exceptions - don't restrict to classes level
                            JCClass[] exceptions = JCompletion.EMPTY_CLASSES;
                            Identifier[] cee = cec[i].getExceptions();
                            exceptions = new JCClass[cee.length];
                            for (int j = 0; j < cee.length; j++) {
                                exceptions[j] = getIdentifierClass(cee[j], lookupCache, preferFinder);
                            }

                            lst.add(new JCompletion.BaseConstructor(this, cec[i].getModifiers(),
                                                                    parameters, exceptions));
                        }
                    }
                }
                body.constructors = new JCConstructor[lst.size()];
                lst.toArray(body.constructors);
                lst.clear();
            }

            // Get methods
            MethodElement[] cem = ce.getMethods();
            body.methods = JCompletion.EMPTY_METHODS;
            if (cem != null) {
                for (int i = 0; i < cem.length; i++) {
                    if (cem[i] != null) {
                        int mods = cem[i].getModifiers();
                        if (iface) { // all interface methods are public
                            mods |= Modifier.PUBLIC;
                        }

                        if (JCompletion.getLevel(mods) >= methodLevel) {
                            // get constructor parameters
                            JCParameter[] parameters = JCompletion.EMPTY_PARAMETERS;
                            MethodParameter[] cep = cem[i].getParameters();
                            if (cep != null) {
                                parameters = new JCParameter[cep.length];
                                for (int j = 0; j < cep.length; j++) {
                                    int type;
                                    parameters[j] = new JCompletion.BaseParameter(
                                                        cep[j].getName(),
                                                        getType(cep[j].getType(), lookupCache, preferFinder)
                                                    );
                                }
                            }

                            // get thrown exceptions - don't restrict to classes level
                            JCClass[] exceptions = JCompletion.EMPTY_CLASSES;
                            Identifier[] cee = cem[i].getExceptions();
                            exceptions = new JCClass[cee.length];
                            for (int j = 0; j < cee.length; j++) {
                                exceptions[j] = getIdentifierClass(cee[j], lookupCache, preferFinder);
                            }

                            lst.add(new JCompletion.BaseMethod(this,
                                                               cem[i].getName().getName(), mods,
                                                               getType(cem[i].getReturn(), lookupCache, preferFinder),
                                                               parameters, exceptions));
                        }
                    }
                }
            }
            body.methods = new JCMethod[lst.size()];
            lst.toArray(body.methods);
            lst.clear();
        }

        protected void init() {
            // already done
        }

    }

}

/*
 * Log
 *  16   Gandalf   1.15        1/13/00  Miloslav Metelka Localization
 *  15   Gandalf   1.14        1/11/00  Miloslav Metelka 
 *  14   Gandalf   1.13        12/28/99 Miloslav Metelka 
 *  13   Gandalf   1.12        11/15/99 Miloslav Metelka getIdentifierClass()
 *  12   Gandalf   1.11        11/14/99 Miloslav Metelka 
 *  11   Gandalf   1.10        11/11/99 Miloslav Metelka 
 *  10   Gandalf   1.9         11/8/99  Miloslav Metelka 
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         9/30/99  Miloslav Metelka 
 *  7    Gandalf   1.6         9/15/99  Miloslav Metelka 
 *  6    Gandalf   1.5         8/27/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/18/99  Miloslav Metelka 
 *  4    Gandalf   1.3         8/18/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

