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

package org.netbeans.modules.java;

import java.util.*;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;

import org.openide.TopManager;
import org.openide.src.*;

/** This class support special type of Java connections -
* between interfaces and implementation
* @author Petr Hamernik
*/
class InterfaceConnection extends Object {
    private static final ClassElement[] EMPTY_ARRAY_CLASSES = new ClassElement[0];
    private static final boolean DEBUG = false;

    static boolean synchronizeInterfaces(JavaConnections.Event evt, LinkedList changeProcessors, SourceElementImpl source) {
        JavaConnections.Change[] changes = evt.getChanges();
        TreeMap cacheMap = new TreeMap();
        boolean ret = false;
        Map changedMethodMap = new HashMap();

        for (int i = 0; i < changes.length; i++) {
            try {
                switch (changes[i].getChangeType()) {
                case JavaConnections.TYPE_METHODS_ADD:
                    Element[] elements = changes[i].getElements();
                    for (int j = 0; j < elements.length; j++) {
                        MethodElement m = (MethodElement) elements[j];
                        ClassElement interf = m.getDeclaringClass();
                        ClassElement[] classes = findClassImplementing(interf, source, cacheMap);
                        if (classes.length > 0) {
                            ret = true;
                            m = (MethodElement)m.clone();
                            m.setBody(JavaConnections.SETTINGS.getGenerateReturnAsString(m.getReturn()));
                            m.setModifiers(Modifier.PUBLIC);
                            MethodElement.Key key = new MethodElement.Key(m);

                            if (changedMethodMap.containsKey(key)) {
                                if (compareMethods((MethodElement)changedMethodMap.get(key), m)) {
                                    continue;
                                }
                            }
                            for (int k = 0; k < classes.length; k++) {
                                ClassElement c = classes[k];
                                MethodElement updatedMethod = findMatchingMethod(c, m);
                                if (updatedMethod == null) {
                                    changeProcessors.add(new AddMethodProcessor(interf, classes[k], m));
                                } else if (!compareMethods(updatedMethod, m)) {
                                    changeProcessors.add(new ChangeMethodProcessor(interf, c, updatedMethod, m));
                                }
                                changedMethodMap.put(key, m);
                            }
                        }
                    }
                    break;

                case JavaConnections.TYPE_METHODS_CHANGE:
                    MethodElement oldMethod = (MethodElement) changes[i].getOldElement();
                    MethodElement newMethod = (MethodElement) changes[i].getNewElement();

                    ClassElement interf = newMethod.getDeclaringClass();
                    ClassElement[] classes = findClassImplementing(interf, source, cacheMap);
                    if (classes.length > 0) {
                        MethodElement addingMethod = null;
                        ret = true;
                        for (int k = 0; k < classes.length; k++) {
                            ClassElement c = classes[k];
                            MethodElement updatedMethod = findMatchingMethod(c, oldMethod);
                            if (updatedMethod == null) {
                                // next try: there can be method matching the new signature.
                                updatedMethod = findMatchingMethod(c, newMethod);
                                if (updatedMethod == null) {
                                    // there's nothing that matches old or new signature.
                                    if (addingMethod == null) {
                                        addingMethod = (MethodElement) newMethod.clone();
                                        addingMethod.setBody(JavaConnections.SETTINGS.getGenerateReturnAsString(addingMethod.getReturn()));
                                        addingMethod.setModifiers(Modifier.PUBLIC);
                                    }
                                    MethodElement.Key key = new MethodElement.Key(addingMethod);
                                    if (changedMethodMap.containsKey(key)) {
                                        if (compareMethods((MethodElement)changedMethodMap.get(key), addingMethod)) {
                                            continue;
                                        }
                                    }
                                    changeProcessors.add(new AddMethodProcessor(interf, classes[k], addingMethod));
                                    changedMethodMap.put(key, addingMethod);
                                    continue;
                                }
                            }
                            if (compareMethods(updatedMethod, newMethod)) {
                                continue;
                            }
                            MethodElement.Key key = new MethodElement.Key(newMethod);

                            if (changedMethodMap.containsKey(key) && 
				compareMethods((MethodElement)changedMethodMap.get(key), newMethod)) {
                                continue;
                            }
                            changeProcessors.add(new ChangeMethodProcessor(interf, c, updatedMethod, newMethod));
                            changedMethodMap.put(key, newMethod);
                        }
                    }
                    break;
                }
            }
            catch (SourceException e) {
                TopManager.getDefault().notifyException(e);
            }
        }
        return ret;
    }

    private static MethodElement findMatchingMethod(ClassElement clazz, MethodElement elem) {
        MethodParameter[] params = elem.getParameters();
        Type[] paramTypes = new Type[params.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramTypes[i] = params[i].getType();
        }
        return clazz.getMethod(elem.getName(), paramTypes);
    }

    static ClassElement[] findClassImplementing(ClassElement interf, SourceElementImpl source, Map cache) {
        Identifier id = interf.getName();
        String key = id.getFullName();

        //    System.out.println("find("+key+")"); // NOI18N

        ClassElement[] ret = (ClassElement[])cache.get(key);
        //    System.out.println("  get =>"+(ret == null ? " null" :  ""+ret.length)); // NOI18N
        if (ret == null) {
            LinkedList list = new LinkedList();
            ClassElement[] classes = source.getAllClasses();
            for (int i = 0; i < classes.length; i++) {
                if (classes[i].isInterface())
                    continue;
                Identifier[] interfaces = classes[i].getInterfaces();
                for (int j = 0; j < interfaces.length; j++) {
                    if (interfaces[j].compareTo(id, false)) {
                        //            System.out.println("  found:"+classes[i]); // NOI18N
                        list.add(classes[i]);
                        break;
                    }
                }
            }
            ret = (ClassElement[])list.toArray(EMPTY_ARRAY_CLASSES);
            cache.put(key, ret);
        }
        return ret;
    }

    static void interfacesAdded(Identifier[] interfaces, ClassElement cl, SourceElementImpl source) {
        if (!JavaConnections.SETTINGS.isEnabled()) // disabled
            return;

        if (source.getJavaDataObject().getSynchronizationType() == JavaDataObject.CONNECT_NOT)
            return;

        Map changedMethods = new HashMap();
        LinkedList methodList = new LinkedList();
        addAllMethods(interfaces, methodList);
        MethodElement[] allMethods = new MethodElement[methodList.size()];
        methodList.toArray(allMethods);

        LinkedList changesProcessors = new LinkedList();
        for (int i = 0; i < allMethods.length; i++) {
            ClassElement interf = allMethods[i].getDeclaringClass();
            MethodElement m = (MethodElement)allMethods[i].clone();
            try {
                m.setBody(JavaConnections.SETTINGS.getGenerateReturnAsString(m.getReturn()));
                m.setModifiers(Modifier.PUBLIC);
            }
            catch (SourceException e) {
            }

            MethodElement updatedMethod = findMatchingMethod(cl, m);
            MethodElement.Key key = new MethodElement.Key(m);
            if (changedMethods.containsKey(key) &&
                    compareMethods((MethodElement)changedMethods.get(key), m)) {
                continue;
            }
            if (updatedMethod == null) {
                changesProcessors.add(new AddMethodProcessor(interf, cl, m));
            }
            else {
                if (compareMethods(updatedMethod, m)) {
                    continue;
                }
                changesProcessors.add(new ChangeMethodProcessor(interf, cl, updatedMethod, m));
            }
            changedMethods.put(key, m);
        }
        source.getJavaDataObject().startChangeProcessors(changesProcessors);
    }

    private static void addAllMethods(Identifier[] interfaces, LinkedList allMethods) {
        for (int i = 0; i < interfaces.length; i++) {
            ClassElement interf = ClassElement.forName(interfaces[i].getFullName());
            if ((interf != null) && (interf.isInterface())) {
                MethodElement[] methods = interf.getMethods();
                for (int j = 0; j < methods.length; j++) {
                    allMethods.add(methods[j]);
                }
                Identifier[] recurseInterfaces = interf.getInterfaces();
                if (recurseInterfaces.length > 0)
                    addAllMethods(recurseInterfaces, allMethods);
            }
        }
    }

    static boolean sourceCheck(LinkedList changeProcessors, SourceElementImpl source) {
        ClassElement[] classes = source.getAllClasses();
        int i;
        boolean changed = false;

        try {
            for (i = 0; i < classes.length; i++) {
                ClassElement cls = classes[i];

                if (!cls.isClassOrInterface()) {
                    // do not sync interfaces.
                    continue;
                }
                ClassElement superClass = null;
                Set allInterfaces = new HashSet(13);
                Set implInterfaces = new HashSet(13);

                Identifier superid = cls.getSuperclass();

                collectInterfaces(cls, allInterfaces, implInterfaces, true, false);
                if (superid != null) {
                    superClass = ClassElement.forName(superid.getFullName());
                    allInterfaces.removeAll(implInterfaces);
                }
                // Get all methods from all implemented interfaces, throwing out
                // methods with the same declarations.
                HashMap implement = new HashMap(13);
                Iterator itfIter = allInterfaces.iterator();
                while (itfIter.hasNext()) {
                    ClassElement itf = (ClassElement)itfIter.next();
                    MethodElement methods[] = itf.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        MethodElement m = methods[j];
                        MethodElement.Key key = new MethodElement.Key(m);
                        if (!implement.containsKey(key)) {
                            implement.put(key, new Object[] { m, itf });
                        }
                    }
                }

                // if the class has an abstract superclass, go up through inheritance
                // chain and add all abstract methods from abstract superclasses.
                for (ClassElement s = superClass; s != null && (s.getModifiers() & Modifier.ABSTRACT) > 0;
                        s = (s.getSuperclass() == null) ? null : ClassElement.forName(s.getSuperclass().getFullName()) ) {
                    MethodElement[] mets = s.getMethods();
                    for (int j = 0; j < mets.length; j++) {
                        MethodElement m = mets[j];
                        if ((m.getModifiers() & Modifier.ABSTRACT) > 0) {
                            MethodElement.Key k = new MethodElement.Key(m);
                            if (!implement.containsKey(k)) {
                                implement.put(k, new Object[] { m, s});
                            }
                        }
                    }
                }

                // Now, exclude all methods from already implemented interfaces:
                itfIter = implInterfaces.iterator();
                while (itfIter.hasNext()) {
                    ClassElement itf = (ClassElement)itfIter.next();
                    MethodElement methods[] = itf.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        MethodElement m = methods[j];
                        MethodElement.Key key = new MethodElement.Key(m);
                        implement.remove(key);
                    }
                }
                // implement now contains all unique methods that are to be implemented
                // by the class. We have to filter out methods, that have been implemented
                // by one of the superclasses.
                Iterator mIter = implement.keySet().iterator();
                HashMap unimplemented = new HashMap();

                while (mIter.hasNext()) {
                    Object[] val = (Object[])implement.get(mIter.next());
                    MethodElement m = (MethodElement)val[0];
                    MethodParameter[] params = m.getParameters();
                    Type[] paramTypes = new Type[params.length];
                    ClassElement implementingSuper;

                    for (int j = 0; j < params.length; j++) {
                        paramTypes[j] = params[j].getType();
                    }

                    implementingSuper = superClass == null ? null : findMethodInSuperclasses(superClass, m.getName(), paramTypes);
                    if (implementingSuper != null) {
                        //if (DEBUG) System.out.println("Method " + m.toString() + "is implemented in superclass " + implementingSuper.getName().getFullName());
                    } else {
                        //if (DEBUG) System.out.println("Method " + m.toString() + "is not implemented.\n\tattaching to interface " + ((ClassElement)val[1]).getName().getFullName());
                        unimplemented.put(m, val[1]);
                    }
                }
                if (!unimplemented.isEmpty()) {
                    changed |= implementMethods(cls, unimplemented, changeProcessors);
                }
            }
            //if (DEBUG) System.out.println("SOurce check done.");
        } catch (SourceException e) {
            TopManager.getDefault().notifyException(e);
        }
        return changed;
    }

    private static boolean implementMethods(ClassElement cls, Map unimplemented, LinkedList changeProcessors) throws SourceException {
        boolean changed = false;
        ClassElementImpl cimpl = (ClassElementImpl)cls.getCookie(ElementImpl.class);
        ElementsCollection.Method col = cimpl.methods;
        MemberElement[] oldArray = new MemberElement[col.array != null ? col.array.size() : 0];
        MemberElement[] newArray = new MemberElement[unimplemented.size()];

        if (col.array != null) {
            oldArray = (MemberElement[])col.array.toArray(oldArray);
        }
        newArray = (MemberElement[])unimplemented.keySet().toArray(newArray);
        int results[] = col.pairElements(oldArray, newArray, col.getComparators());
        for (int i = 0; i < results.length; i++) {
            MethodElement newm = (MethodElement)newArray[i];
            ClassElement itf = (ClassElement)unimplemented.get(newm);

            if (results[i] == -1) {
                newm = (MethodElement)newm.clone();
                newm.setBody(JavaConnections.SETTINGS.getGenerateReturnAsString(newm.getReturn()));
                newm.setModifiers(Modifier.PUBLIC);
                // entirely new/unrecognized method
                //if (DEBUG) System.out.println("Unimplemented (new) method: " + newm.toString());
                changeProcessors.add(new AddMethodProcessor(itf, cls, newm));
                changed = true;
            } else {
                // we need to include only methods that have somehow changed in the change
                // processor list.
                MethodElement oldm = (MethodElement)oldArray[results[i]];

                if (!compareMethods(oldm, newm)) {
                    //if (DEBUG) System.out.println("Altered method: " + oldm.toString() + " -> " + newm.toString());
                    newm = (MethodElement)newm.clone();
                    newm.setBody(JavaConnections.SETTINGS.getGenerateReturnAsString(newm.getReturn()));
                    newm.setModifiers(Modifier.PUBLIC);
                    changeProcessors.add(new ChangeMethodProcessor(itf, cls, oldm, newm));
                    changed = true;
                } else {
                    //if (DEBUG) System.out.println("Method " + newm.toString() + " hasn't been changed.");
                }
            }
        }
        //if (DEBUG) System.out.println("Class synchronization finished.");
        return changed;
    }

    /** Compares old implementation of method (a) with the new declaration suggested
      from the interface (b). Returns true if the method declarations completely match,
      so it is not necessary to update the source.
      Note: since interface methods are public abstract by definition, no check is made
        on method modifiers. 
    */
    private static boolean compareMethods(MethodElement a, MethodElement b) {
        if (!a.getName().compareTo(b.getName(), false)) {
            return false;
        }
        if (!a.getReturn().compareTo(b.getReturn(), false)) {
            return false;
        }

        MethodParameter[] paramA = a.getParameters();
        MethodParameter[] paramB = b.getParameters();

        if (paramA.length != paramB.length) {
            return false;
        }
        int i;

        for (i = 0; i < paramA.length; i++) {
            if (!paramA[i].compareTo(paramB[i], true, false)) {
                return false;
            }
        }

        Identifier[] excA = a.getExceptions();
        Identifier[] excB = b.getExceptions();

        if (excA.length != excB.length) {
            return false;
        }
        for (i = 0; i < excA.length; i++) {
            if (!excA[i].compareTo(excB[i], false)) {
                return false;
            }
        }
        return true;
    }

    private static ClassElement findMethodInSuperclasses(ClassElement cls, Identifier name, Type[] paramTypes) {
        MethodElement el = cls.getMethod(name, paramTypes);
        if (el != null) {
            // return cls iff the method is not abstract. Otherwise it means that the most
            // direct superclass mentioning this method declares it as abstract (and that
            // it is not implemented at all).
            return (el.getModifiers() & Modifier.ABSTRACT) > 0 ? null : cls;
        }
        Identifier superclass = cls.getSuperclass();
        if (superclass == null) {
            return null;
        }
        cls = ClassElement.forName(superclass.getFullName());
        if (cls != null) {
            return findMethodInSuperclasses(cls, name, paramTypes);
        } else {
            return null;
        }
    }

    private static void collectInterfaces(ClassElement cls, Set include, Set exclude, boolean first, boolean excludeOnly) {
        Identifier[] itfs = cls.getInterfaces();
        Identifier s = cls.getSuperclass();
        ClassElement c;

        if (s != null) {
            c = ClassElement.forName(s.getFullName());
            if (c != null) {
                collectInterfaces(c, include, exclude, false, false);
            }
        }

        if (!excludeOnly && (first || (cls.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) > 0) ||
                cls.isInterface()) {
            // include all implemented interfaces
            //if (DEBUG) System.out.println("Including interfaces: ");
            for (int i = 0; i < itfs.length; i++) {
                c = ClassElement.forName(itfs[i].getFullName());
                if (c == null) continue;
                if (exclude.contains(c)) continue;
                //if (DEBUG) System.out.println("- " + c.getName().getFullName());
                include.add(c);
                collectInterfaces(c, include, exclude, false, false);
            }
        } else {
            // exclude all implemented interfaces
            for (int i = 0; i < itfs.length; i++) {
                //if (DEBUG) System.out.println("Excluding interfaces: ");
                c = ClassElement.forName(itfs[i].getFullName());
                if (c == null) continue;
                //if (DEBUG) System.out.println("- " + c.getName().getFullName());
                exclude.add(c);
                // only do exclusions at lower levels, this class is not abstract so all
                // its interfaces/superclasses need be fully implemented.
                collectInterfaces(c, include, exclude, false, true);
            }
        }
    }

    static class AddMethodProcessor extends JavaConnections.ChangeProcessor {
        final static MessageFormat format = new MessageFormat(Util.getString("MSG_AddMethod"));
        MethodElement method;
        ClassElement clazz;

        AddMethodProcessor(ClassElement interf, ClassElement clazz, MethodElement method) {
            super(format.format(new Object[] {
                                    interf.getName().getName(), clazz.getName().getName(), method.getName().getName()
                                }));
            this.clazz = clazz;
            this.method = method;
        }

        public void process() throws SourceException {
            method.setParameters(fillNames(method.getParameters()));
            clazz.addMethod(method);
        }
    }

    static class ChangeMethodProcessor extends JavaConnections.ChangeProcessor {
        final static MessageFormat format = new MessageFormat(Util.getString("MSG_UpdateMethod"));
        MethodElement clazzMethod;
        MethodElement interfMethod;

        ChangeMethodProcessor(ClassElement interf, ClassElement clazz,
                              MethodElement clazzMethod, MethodElement interfMethod) {
            super(format.format(new Object[] {
                                    interf.getName().getName(),
                                    clazz.getName().getName(),
                                    clazzMethod.getName().getName()
                                }));
            this.clazzMethod = clazzMethod;
            this.interfMethod = interfMethod;
        }

        public void process() throws SourceException {
            int oldModif = clazzMethod.getModifiers();

            if ((oldModif & Modifier.PUBLIC) == 0) {
                clazzMethod.setModifiers(oldModif | Modifier.PUBLIC);
            }
            clazzMethod.setName(interfMethod.getName());
            clazzMethod.setExceptions(interfMethod.getExceptions());
            clazzMethod.setParameters(fillNames(interfMethod.getParameters()));
            clazzMethod.setReturn(interfMethod.getReturn());
        }
    }

    static MethodParameter[] fillNames(MethodParameter[] params) {
        int paramIndex = 1;
        for (int i = 0; i < params.length; i++) {
            if (params[i].getName().length() == 0) {
                params[i] = new MethodParameter("p"+(i+1), params[i].getType(), params[i].isFinal()); // NOI18N
            }
        }
        return params;
    }
}
/*
 * Log
 *  18   Gandalf-post-FCS1.15.1.1    4/18/00  Svatopluk Dedic Interfaces are excluded 
 *       from manual sync
 *  17   Gandalf-post-FCS1.15.1.0    4/14/00  Svatopluk Dedic Checks for duplicit 
 *       changes inherited from several interfaces
 *  16   Gandalf   1.15        3/8/00   Svatopluk Dedic 
 *  15   Gandalf   1.14        3/8/00   Svatopluk Dedic Fixed explicit 
 *       synchronization with abstract superclasses
 *  14   Gandalf   1.13        2/16/00  Svatopluk Dedic Improved checking when 
 *       interface extends more than one superinterface.
 *  13   Gandalf   1.12        2/15/00  Svatopluk Dedic Debug output removed
 *  12   Gandalf   1.11        2/14/00  Svatopluk Dedic 
 *  11   Gandalf   1.10        1/14/00  Petr Hamernik   fixed #3726
 *  10   Gandalf   1.9         1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  9    Gandalf   1.8         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  7    Gandalf   1.6         9/10/99  Petr Hamernik   minor changes
 *  6    Gandalf   1.5         8/18/99  Petr Hamernik   i18n
 *  5    Gandalf   1.4         7/19/99  Petr Hamernik   exception notification 
 *       changed
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/7/99   Petr Hamernik   public -> package 
 *       private
 *  2    Gandalf   1.1         6/4/99   Petr Hamernik   synchronization update
 *  1    Gandalf   1.0         6/2/99   Petr Hamernik   
 * $
 */
