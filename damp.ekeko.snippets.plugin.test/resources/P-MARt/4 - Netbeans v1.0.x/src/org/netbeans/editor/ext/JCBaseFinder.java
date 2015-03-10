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

package org.netbeans.editor.ext;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
* Java completion support finder
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCBaseFinder extends JCompletion.AbstractProvider
    implements JCFinder {

    static final Comparator CLASS_NAME_COMPARATOR = new DefaultClassNameComparator();

    private static final int PACKAGE_PRE_ALLOC = 1009;

    private static final int CLASS_PRE_ALLOC = 5003;

    protected JCPackage[] allPackages;

    protected JCClass[] allClassesByName;

    /**
     * @associates JCPackage 
     */
    protected HashMap allPackagesMap = new HashMap(PACKAGE_PRE_ALLOC);

    /**
     * @associates JCClass 
     */
    protected HashMap allClassesMap = new HashMap(CLASS_PRE_ALLOC);

    public Iterator getClasses() {
        return new ArrayList(Arrays.asList(getAllClasses())).iterator();
    }

    public boolean append(JCClassProvider cp) {
        if (!super.append(cp)) {
            return false;
        }
        return true;
    }

    protected boolean appendClass(JCClass cls) {
        if (!cheapUpdate(cls)) { // try cheap update first
            invalidate(); // reset all if failed
        }
        return true;
    }

    public void reset() {
        allClassesMap.clear();
        invalidate();
    }

    protected void invalidate() {
        allPackagesMap.clear();
        allPackages = null;
        allClassesByName = null;
    }

    public JCPackage getExactPackage(String packageName) {
        if (allPackages == null) { // not initialized yet
            build();
        }
        return (JCPackage)allPackagesMap.get(packageName);
    }

    public JCClass getExactClass(String classFullName) {
        return (JCClass)allClassesMap.get(classFullName);
    }

    protected JCPackage[] getAllPackages() {
        if (allPackages == null) {
            build();
        }
        return allPackages;
    }

    protected JCClass[] getAllClassesByName() {
        if (allClassesByName == null) {
            build();
        }
        return allClassesByName;
    }

    protected JCClass[] getAllClasses() {
        JCClass[] allClasses = (JCClass[])getAllClassesByName().clone();
        Arrays.sort(allClasses);
        return allClasses;
    }

    private boolean cheapUpdate(JCClass cls) {
        Object o = allClassesMap.put(cls.getFullName(), cls);
        if (allClassesByName != null && o != null) { // inited and class already there, can do cheap update
            String pkgName = cls.getPackageName();
            JCPackage pkg = (JCPackage)allPackagesMap.get(pkgName);
            if (pkg == null) { // strange - package missing in package map
                return false;
            }
            JCClass[] clist = pkg.getClasses();
            int ind = Arrays.binarySearch(clist, cls);
            if (ind < 0) { // strange - class is missing in the package class list
                return false;
            }
            clist[ind] = cls;

            // Update allClassesByName array - can be more with the same name
            ind = Arrays.binarySearch(allClassesByName, cls, CLASS_NAME_COMPARATOR);
            // adjust start index
            if (ind < 0) { // not exact match
                ind = -ind - 1;
            }

            // position to start of matching classes
            String name = cls.getName();
            while (ind >= 0 && ind < allClassesByName.length) {
                if (!allClassesByName[ind].getName().startsWith(name)) {
                    break;
                }
                ind--;
            }
            ind++;

            // replace the matching class in the list
            boolean updated = false;
            while (ind < allClassesByName.length) {
                if (cls.equals(allClassesByName[ind])) {
                    allClassesByName[ind] = cls;
                    updated = true;
                    break;
                }
                if (!name.equals(allClassesByName[ind].getName())) {
                    break;
                }
                ind++;
            }
            return updated;
        }
        return false;
    }

    private void addPackage(JCPackage pkg, boolean force) {
        if (force || !allPackagesMap.containsKey(pkg)) {
            allPackagesMap.put(pkg.getName(), pkg);
            String name = pkg.getName();
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                addPackage(new JCompletion.BasePackage(name.substring(0, i)), false);
            }
        }
    }

    protected void build() {
        // Build class array and class by name array
        JCClass[] allClasses = new JCClass[allClassesMap.size()];
        allClassesByName = new JCClass[allClasses.length];

        Iterator itr = allClassesMap.values().iterator();
        int ind = 0;
        while (itr.hasNext()) {
            allClasses[ind] = (JCClass)itr.next();
            allClassesByName[ind] = allClasses[ind];
            ind++;
        }

        Arrays.sort(allClasses);
        Arrays.sort(allClassesByName, CLASS_NAME_COMPARATOR);

        // Build package array
        allPackagesMap.clear();
        allPackages = JCompletion.EMPTY_PACKAGES;

        if (allClasses.length > 0) {
            ArrayList pkgClassList = new ArrayList();
            JCPackage curPkg = new JCompletion.BasePackage(allClasses[0].getPackageName());

            for (int i = 0; i < allClasses.length; i++) {
                String pkgName = allClasses[i].getPackageName();
                if (!curPkg.equals(pkgName)) {
                    JCClass classes[] = new JCClass[pkgClassList.size()];
                    pkgClassList.toArray(classes);
                    curPkg.setClasses(classes);
                    pkgClassList.clear();
                    addPackage(curPkg, true);
                    curPkg = new JCompletion.BasePackage(pkgName);
                }
                pkgClassList.add(allClasses[i]);
            }
            JCClass classes[] = new JCClass[pkgClassList.size()];
            pkgClassList.toArray(classes);
            curPkg.setClasses(classes);
            addPackage(curPkg, true);

            allPackages = new JCPackage[allPackagesMap.size()];
            itr = allPackagesMap.values().iterator();
            ind = 0;
            while (itr.hasNext()) {
                allPackages[ind] = (JCPackage)itr.next();
                ind++;
            }
        }
        Arrays.sort(allPackages);
    }

    public List findPackages(String name, boolean exactMatch, boolean subPackages) {
        List ret = new ArrayList();
        if (exactMatch) {
            JCPackage pkg = getExactPackage(name);
            if (pkg != null) {
                ret.add(pkg);
            }
            if (!subPackages) {
                return ret;
            }
        }

        JCPackage packages[] = getAllPackages();
        JCPackage key = new JCompletion.BasePackage(name);
        int ind = Arrays.binarySearch(packages, key);
        int nameLen = name.length();

        // adjust start index
        if (ind < 0) { // not exact match
            ind = -ind - 1;
        }

        // position to start of matching package names
        while (ind >= 0 && ind < packages.length) {
            if (!packages[ind].getName().startsWith(name)) {
                break;
            }
            ind--;
        }
        ind++;

        // add the matching packages to the list
        int reqDotCount = key.getDotCount();
        while (ind < packages.length) {
            String pkgName = packages[ind].getName();

            if (!pkgName.startsWith(name)) {
                break;
            }

            if (exactMatch ?
                    (pkgName.length() > nameLen && pkgName.charAt(nameLen) == '.')
                    : (subPackages || packages[ind].getDotCount() == reqDotCount)
               ) {
                ret.add(packages[ind]);
            }

            ind++;
        }

        return ret;
    }

    /** Find classes by name and possibly in some package
    * @param pkg package where the classes should be searched for. It can be null
    * @param begining of the name of the class. The package name must be omitted.
    * @param exactMatch whether the given name is the exact requested name
    *   of the class or not.
    * @return list of the matching classes
    */
    public List findClasses(JCPackage pkg, String name, boolean exactMatch) {
        List ret = new ArrayList();
        JCClass[] classes;
        int ind;
        JCClass key = new JCompletion.SimpleClass(name, ""); // NOI18N
        int nameLen = name.length();
        if (pkg != null) {
            classes = pkg.getClasses();
        } else { // pkg is null
            classes = getAllClassesByName();
        }
        ind = Arrays.binarySearch(classes, key, CLASS_NAME_COMPARATOR);

        // adjust start index
        if (ind < 0) { // not exact match
            ind = -ind - 1;
        }

        // position to start of matching classes
        while (ind >= 0 && ind < classes.length) {
            if (!classes[ind].getName().startsWith(name)) {
                break;
            }
            ind--;
        }
        ind++;

        // add the matching classes to the list
        while (ind < classes.length) {
            String className = classes[ind].getName();
            if (!className.startsWith(name)) {
                break;
            }

            if (!exactMatch || className.length() == nameLen) {
                ret.add(classes[ind]);
            }
            ind++;
        }

        return ret;
    }

    /** Get non-static outer classes to search
    * the fields and methods there. The original class is added
    * as the first member of the resulting list.
    */
    private List getOuterClasses(JCClass cls) {
        JCFinder finder = JCompletion.getFinder();
        ArrayList outers = new ArrayList();
        outers.add(cls);
        int lastDotInd = cls.getName().lastIndexOf('.');
        while ((cls.getModifiers() & Modifier.STATIC) == 0 && lastDotInd >= 0) {
            int pkgLen = cls.getPackageName().length();
            String fullName = cls.getFullName().substring(0,
                              ((pkgLen > 0) ? (pkgLen + 1) : 0) + lastDotInd);
            cls = finder.getExactClass(fullName);
            if (cls != null) {
                outers.add(cls);
                lastDotInd = cls.getName().lastIndexOf('.');
            } else {
                break;
            }
        }
        return outers;
    }

    /** Find fields by name in a given class.
    * @param cls class which is searched for the fields.
    * @param name start of the name of the field
    * @param exactMatch whether the given name of the field is exact
    * @param staticOnly whether search for the static fields only
    * @return list of the matching fields
    */
    public List findFields(JCClass cls, String name, boolean exactMatch,
                           boolean staticOnly, boolean inspectOuterClasses) {
        TreeSet ts = new TreeSet();
        List clsList = getClassList(cls);
        String pkgName = cls.getPackageName();
        for (int i = clsList.size() - 1; i >= 0; i--) {
            cls = getExactClass(((JCClass)clsList.get(i)).getFullName());
            if (cls != null) {
                boolean difPkg = !cls.getPackageName().equals(pkgName);
                List outerList = (i == 0 && inspectOuterClasses && cls.getName().indexOf('.') >= 0)
                                 ? getOuterClasses(cls) : null;
                int outerInd = (outerList != null) ? (outerList.size() - 1) : -1;
                do {
                    if (outerInd >= 0) {
                        cls = (JCClass)outerList.get(outerInd--);
                    }
                    JCField[] fields = cls.getFields();
                    for (int j = 0; j < fields.length; j++) {
                        JCField fld = fields[j];
                        int mods = fld.getModifiers();
                        if ((staticOnly && (mods & Modifier.STATIC) == 0)
                                || (i > 0 && (mods & Modifier.PRIVATE) != 0)
                                || (difPkg && (mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0)
                           ) {
                            continue;
                        }
                        if (exactMatch) {
                            if (!fld.getName().equals(name)) {
                                continue;
                            }
                        } else {
                            if (!fld.getName().startsWith(name)) {
                                continue;
                            }
                        }
                        ts.add(fld);
                    }
                } while (outerInd >= 0);
            }
        }

        return new ArrayList(ts);
    }

    /** Find methods by name in a given class.
    * @param cls class which is searched for the methods.
    * @param name start of the name of the method
    * @param exactMatch whether the given name of the method is exact
    * @param staticOnly whether search for the static methods only
    * @return list of the matching methods
    */
    public List findMethods(JCClass cls, String name, boolean exactMatch,
                            boolean staticOnly, boolean inspectOuterClasses) {
        TreeSet ts = new TreeSet();
        List clsList = getClassList(cls);
        String pkgName = cls.getPackageName();
        for (int i = clsList.size() - 1; i >= 0; i--) {
            cls = getExactClass(((JCClass)clsList.get(i)).getFullName());
            if (cls != null) {
                boolean difPkg = !cls.getPackageName().equals(pkgName);
                List outerList = (i == 0 && inspectOuterClasses && cls.getName().indexOf('.') >= 0)
                                 ? getOuterClasses(cls) : null;
                int outerInd = (outerList != null) ? (outerList.size() - 1) : -1;
                do {
                    if (outerInd >= 0) {
                        cls = (JCClass)outerList.get(outerInd--);
                    }
                    JCMethod[] methods = cls.getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        JCMethod mtd = methods[j];
                        int mods = mtd.getModifiers();
                        if ((staticOnly && (mods & Modifier.STATIC) == 0)
                                || (i > 0 && (mods & Modifier.PRIVATE) != 0)
                                || (difPkg && (mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0)
                           ) {
                            continue;
                        }
                        if (exactMatch) {
                            if (!mtd.getName().equals(name)) {
                                continue;
                            }
                        } else { // match begining
                            if (!mtd.getName().startsWith(name)) {
                                continue;
                            }
                        }
                        ts.add(mtd);
                    }
                } while (outerInd >= 0);
            }
        }

        return new ArrayList(ts);
    }

    private List getClassList(JCClass cls) {
        JCFinder finder = JCompletion.getFinder();
        cls = finder.getExactClass(cls.getFullName());
        List ret;
        if (cls != null) {
            ret = cls.isInterface()
                  ? JCUtilities.getAllInterfaces(cls)
                  : JCUtilities.getSuperclasses(cls);
            ret.add(0, cls);

        } else { // class not found
            ret = new ArrayList(); // return empty list
        }

        return ret;
    }

    public String dumpClasses() {
        StringBuffer sb = new StringBuffer(8192); // expect huge growth
        JCClass[] ac = getAllClasses();
        for (int i = 0; i < ac.length; i++) {
            sb.append(JCUtilities.dumpClass(ac[i]));
            sb.append("\n\n"); // NOI18N
        }
        return sb.toString();
    }


    public static final class DefaultClassNameComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            return ((JCClass)o1).getName().compareTo(((JCClass)o2).getName());
        }

    }


}

/*
 * Log
 *  18   Gandalf-post-FCS1.16.1.0    4/5/00   Miloslav Metelka fixed interfaces 
 *       tracking
 *  17   Gandalf   1.16        1/13/00  Miloslav Metelka Localization
 *  16   Gandalf   1.15        11/24/99 Miloslav Metelka 
 *  15   Gandalf   1.14        11/14/99 Miloslav Metelka 
 *  14   Gandalf   1.13        11/9/99  Miloslav Metelka 
 *  13   Gandalf   1.12        11/8/99  Miloslav Metelka 
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/10/99 Miloslav Metelka 
 *  10   Gandalf   1.9         9/30/99  Miloslav Metelka 
 *  9    Gandalf   1.8         9/15/99  Miloslav Metelka 
 *  8    Gandalf   1.7         8/18/99  Miloslav Metelka 
 *  7    Gandalf   1.6         8/18/99  Miloslav Metelka 
 *  6    Gandalf   1.5         8/9/99   Miloslav Metelka getExactPackage() patch
 *  5    Gandalf   1.4         7/30/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/29/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/22/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

