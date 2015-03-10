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

import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Enumeration;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.ext.JavaSyntaxSupport;
import org.netbeans.editor.ext.JCClass;
import org.netbeans.editor.ext.JCType;
import org.netbeans.editor.ext.JCompletion;
import org.netbeans.editor.ext.JCFinder;
import org.netbeans.editor.ext.JCField;
import org.netbeans.editor.ext.JCParameter;
import org.netbeans.editor.ext.JCUtilities;
import org.netbeans.editor.ext.JCPackage;
import org.netbeans.editor.ext.JCMethod;
import org.netbeans.editor.ext.JCConstructor;
import org.netbeans.editor.ext.JCView;
import org.netbeans.editor.ext.JCQuery;
import org.netbeans.modules.editor.KitSupport;
import org.openide.TopManager;
import org.openide.src.SourceElement;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.FieldElement;
import org.openide.src.Element;
import org.openide.src.MemberElement;
import org.openide.src.InitializerElement;
import org.openide.src.MethodElement;
import org.openide.src.ConstructorElement;
import org.openide.src.Type;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Node;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.Breakpoint;
import org.openide.text.Line;


/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbJavaSyntaxSupport extends JavaSyntaxSupport {

    private static final String PACKAGE_SUMMARY = "package-summary"; // NOI18N

    private HashMap jcLookupCache = new HashMap(307);

    private boolean jcValid;

    public NbJavaSyntaxSupport(BaseDocument doc) {
        super(doc);
    }

    protected void documentModified(DocumentEvent evt) {
        super.documentModified(evt);
        jcValid = false;
    }

    protected int getMethodStartPosition(int pos) {
        DataObject dob = KitSupport.getDataObject(doc);
        if (dob != null) {
            try {
                SourceCookie.Editor sce = (SourceCookie.Editor)dob.getCookie(SourceCookie.Editor.class);
                if (sce != null) {
                    Element elem = sce.findElement(pos);
                    if (elem != null) {
                        javax.swing.text.Element swingElem = sce.sourceToText(elem);
                        if (swingElem != null) {
                            return swingElem.getStartOffset();
                        }
                    }
                }
            } catch (NullPointerException e) { // due to some bug in parser !!! [PENDING]
            }
        }

        return 0;
    }

    public int findGlobalDeclarationPosition(String varName, int varPos) {
        Element e = getElementAtPos(varPos);
        if (e instanceof MemberElement) {
            MemberElement me = (MemberElement)e;
            while (me != null) {
                if (me instanceof ClassElement) {
                    ClassElement ce = (ClassElement)me;
                    FieldElement[] fields = ce.getFields();
                    if (fields != null) {
                        for (int i = 0; i < fields.length; i++) {
                            if (fields[i].getName().getFullName().equals(varName)) {
                                DataObject dob = KitSupport.getDataObject(doc);
                                if (dob != null) {
                                    SourceCookie.Editor sce = (SourceCookie.Editor)dob.getCookie(SourceCookie.Editor.class);
                                    if (sce != null) {
                                        javax.swing.text.Element elem = sce.sourceToText(fields[i]);
                                        if (elem != null) {
                                            return elem.getStartOffset();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                me = me.getDeclaringClass();
            }
        }
        return -1;
    }

    private Element getElementAtPos(int pos) {
        DataObject dob = KitSupport.getDataObject(doc);
        if (dob != null) {
            SourceCookie.Editor sce = (SourceCookie.Editor)dob.getCookie(SourceCookie.Editor.class);
            if (sce != null) {
                return sce.findElement(pos);
            }
        }
        return null;
    }

    /** Get the class element(s) according to the current position */
    private ClassElement getClassElement(int pos) {
        ClassElement ce = null;
        Element elem = getElementAtPos(pos);
        if (elem instanceof ClassElement) {
            ce = (ClassElement)elem;
        } else if (elem instanceof MemberElement) {
            ce = ((MemberElement)elem).getDeclaringClass();
        } else if (elem instanceof InitializerElement) {
            ce = ((InitializerElement)elem).getDeclaringClass();
        }
        return ce;
    }

    public JCClass getClass(int pos) {
        ClassElement ce = getClassElement(pos);
        if (ce != null) {
            return JCompletion.getFinder().getExactClass(ce.getName().getFullName());
        }
        return null;
    }

    public boolean isStaticBlock(int pos) {
        Element elem = getElementAtPos(pos);
        if (elem instanceof MethodElement) {
            return (((MethodElement)elem).getModifiers() & Modifier.STATIC) != 0;
        } else if (elem instanceof FieldElement) {
            return (((FieldElement)elem).getModifiers() & Modifier.STATIC) != 0;
        } else if (elem instanceof InitializerElement) {
            return true;
        }
        return false;
    }

    private ClassElement[] getAllClassElements() {
        DataObject dob = KitSupport.getDataObject(doc);
        if (dob != null) {
            SourceCookie sc = (SourceCookie)dob.getCookie(SourceCookie.class);
            if (sc != null) {
                SourceElement se = sc.getSource();
                if (se != null) {
                    return se.getAllClasses();
                }
            }
        }
        return null;
    }

    public void refreshCompletion() {
        if (!jcValid) {
            JCFinder finder = JCompletion.getFinder();
            ClassElement[] ces = getAllClassElements(); // all class elements from the document
            if (ces != null) {
                for (int i = 0; i < ces.length; i++) {
                    JCClass classToAppend = JCExtension.parseClassElement(ces[i], JCompletion.PRIVATE_LEVEL,
                                            JCompletion.PRIVATE_LEVEL, JCompletion.PRIVATE_LEVEL, jcLookupCache, true);
                    if (classToAppend != null) {
                        finder.append(new JCompletion.SingleProvider(classToAppend));
                        //            refreshParents(classToAppend);  !!! takes too much time
                    }
                }
            }
            jcValid = true;
        }
    }

    private void refreshParents(JCClass cls) {
        JCFinder finder = JCompletion.getFinder();
        ArrayList clsList = new ArrayList();
        clsList.add(cls);
        while (true) {
            cls = cls.getSuperclass();
            if (cls == null || cls.equals(JCompletion.INVALID_CLASS) || clsList.indexOf(cls) >= 0) {
                break;
            }
            JCClass fc = finder.getExactClass(cls.getFullName());
            if (fc == null) {
                ClassElement ce = ClassElement.forName(cls.getFullName());
                if (ce != null) {
                    fc = JCExtension.parseClassElement(ce, JCompletion.PRIVATE_LEVEL,
                                                       JCompletion.PRIVATE_LEVEL, JCompletion.PRIVATE_LEVEL, jcLookupCache, true);
                }
                if (fc == null) {
                    break; // break the whole loop if not found
                }
                finder.append(new JCompletion.SingleProvider(fc));
            }
            cls = fc;
            clsList.add(cls);
        }
    }

    protected Map buildGlobalVariableMap(int pos) {
        refreshCompletion();
        JCFinder finder = JCompletion.getFinder();
        JCClass cls = getClass(pos);
        if (cls != null) {
            HashMap varMap = new HashMap();
            List fldList = finder.findFields(cls, "", false, false, true); // NOI18N
            for (int i = fldList.size() - 1; i >= 0; i--) {
                JCField fld = (JCField)fldList.get(i);
                varMap.put(fld.getName(), fld.getType());
            }
            return varMap;
        }
        return null;
    }

    private ClassElement recurseClasses(ClassElement[] classes, String name) {
        for (int i = 0; i < classes.length; i++) {
            ClassElement ce = classes[i];
            if (ce.getName().getFullName().replace('$', '.').equals(name)) {
                return ce;
            }
            ClassElement inner = recurseClasses(ce.getClasses(), name);
            if (inner != null) {
                return inner;
            }
        }
        return null;
    }

    private DataObject getDataObject(FileObject fo) {
        DataObject dob = null;
        if (fo != null) {
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
            }
        }
        return dob;
    }

    private DataObject getDataObject(Repository rep, JCClass cls) {
        DataObject dob = null;
        String clsName = cls.getName();
        int dotInd = clsName.indexOf('.', 0);
        if (dotInd >= 0) {
            clsName = clsName.substring(0, dotInd);
        }
        FileObject fo = rep.find(cls.getPackageName(), clsName, "java"); // NOI18N
        if (fo != null) {
            dob = getDataObject(fo);
        }
        return dob;
    }

    private SourceElement getSourceElement(DataObject classDOB) {
        SourceElement se = null;
        if (classDOB != null) {
            SourceCookie sc = (SourceCookie)classDOB.getCookie(SourceCookie.class);
            if (sc != null) {
                se = sc.getSource();
            }
        }
        return se;
    }

    private ClassElement getClassElement(DataObject classDOB, JCClass cls) {
        SourceElement se = getSourceElement(classDOB);
        ClassElement ce = null;
        if (se != null) {
            ce = recurseClasses(se.getClasses(), cls.getFullName());
        }
        return ce;
    }

    private void openAtElement(final DataObject classDOB, final Element e) {
        new Thread() {
            public void run() {
                OpenCookie oc = (e != null)
                                ? (OpenCookie)e.getCookie(OpenCookie.class)
                                : (OpenCookie)classDOB.getCookie(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                    return;
                }
            }
        }.start();
    }

    private String getSourceName(JCClass cls, boolean shortName) {
        String name = cls.getName();
        int icInd = name.indexOf('.');
        if (icInd >= 0) { // inner class name
            name = name.substring(0, icInd);
        }
        return shortName ? name : cls.getPackageName() + '.' + name;
    }

    /** Open the source according to the given object.
    * @param item completion object that is decoded and the appropriate
    *   source is opened.
    * @param findDeclaration find the declaration behaior that is different
    *   for the fields - it opens the source of the type of the field.
    * @param simulate simulate the opening but don't open in reality.
    * @return for simulate mode return the short display name (without package name)
    *   of the item or null if the item is not recognized.
    *   For non-simulate mode return the null if the item was successfully opened
    *   or the display name if the item's object can't be found.
    */
    public String openSource(Object item, boolean findDeclaration, boolean simulate) {
        TopManager tm = TopManager.getDefault();
        Repository rep = tm.getRepository();
        DataObject dob = null;
        Element elem = null;
        String ret = null;
        boolean found = false;

        if (item instanceof JCPackage) {
            if (!findDeclaration) {
                String pkgName = ((JCPackage)item).getName();
                FileObject fo = rep.find(pkgName, null, null);
                if (fo != null) {
                    dob = getDataObject(fo);
                    if (dob != null) {
                        Node node = dob.getNodeDelegate();
                        if (node != null) {
                            found = true;
                            if (!simulate) {
                                tm.getNodeOperation().explore(node); // explore the package
                            }
                        }
                        dob = null; // don't try to open dob
                    }
                }
                if (simulate || !found) {
                    ret = pkgName;
                }

            }
        } else if (item instanceof JCClass) {
            JCClass cls = (JCClass)item;
            if (!JCompletion.isPrimitiveClass(cls)) {
                dob = getDataObject(rep, cls);
                if (dob != null) {
                    found = true;
                    elem = getClassElement(dob, cls);
                }
                if (simulate || !found) {
                    ret = getSourceName(cls, simulate);
                }
            }

        } else if (item instanceof JCField) {
            JCField fld = (JCField)item;
            JCClass cls = findDeclaration ? fld.getClazz() : fld.getType().getClazz();
            if (!JCompletion.isPrimitiveClass(cls)) {
                dob = getDataObject(rep, cls);
                if (dob != null) {
                    found = true;
                    ClassElement ce = getClassElement(dob, cls);
                    if (ce != null) {
                        elem = JCExtension.findFieldElement(fld, ce);
                    }
                }
                if (simulate || !found) {
                    ret = getSourceName(cls, simulate);
                }
            }

        } else if (item instanceof JCMethod) {
            JCMethod mtd = (JCMethod)item;
            JCClass cls = mtd.getClazz();
            if (!JCompletion.isPrimitiveClass(cls)) {
                dob = getDataObject(rep, cls);
                if (dob != null) {
                    found = true;
                    ClassElement ce = getClassElement(dob, cls);
                    if (ce != null) {
                        elem = JCExtension.findMethodElement(mtd, ce);
                    }
                }
                if (simulate || !found) {
                    ret = getSourceName(cls, simulate);
                }
            }

        } else if (item instanceof JCConstructor) {
            JCConstructor ctr = (JCConstructor)item;
            JCClass cls = ctr.getClazz();
            if (!JCompletion.isPrimitiveClass(cls)) {
                dob = getDataObject(rep, cls);
                if (dob != null) {
                    found = true;
                    ClassElement ce = getClassElement(dob, cls);
                    if (ce != null) {
                        elem = JCExtension.findConstructorElement(ctr, ce);
                    }
                }
                if (simulate || !found) {
                    ret = getSourceName(cls, simulate);
                }
            }
        }

        // Add the current (probably opened) componetn to jump-list
        if (dob != null) {
            if (!simulate) {
                openAtElement(dob, elem);
                KitSupport.addJumpListEntry(dob);
            }
        }
        return ret;
    }

    private FileSystem[] getDocFileSystems() {
        Enumeration en = TopManager.getDefault().getRepository().getFileSystems();
        ArrayList fsList = new ArrayList();
        while (en.hasMoreElements()) {
            FileSystem fs = (FileSystem)en.nextElement();
            if (fs.getCapability().capableOf(FileSystemCapability.DOC)) {
                fsList.add(fs);
            }
        }

        FileSystem[] ret = new FileSystem[fsList.size()];
        fsList.toArray(ret);
        return ret;
    }

    private FileObject[] getDocFileObjects(String pkg, String name) {
        FileSystem[] fss = getDocFileSystems();
        ArrayList foList = new ArrayList();
        String apiPkg = "api." + pkg; // NOI18N
        for (int i = 0; i < fss.length; i++) {
            FileObject fo = fss[i].find(pkg, name, "html"); // NOI18N
            if (fo == null) {
                fo = fss[i].find(apiPkg, name, "html"); // NOI18N
            }
            if (fo != null) {
                foList.add(fo);
            }
        }

        FileObject[] ret = new FileObject[foList.size()];
        foList.toArray(ret);
        return ret;
    }

    public URL[] getJavaDocURLs(Object obj) {
        ArrayList urlList = new ArrayList();
        if (obj instanceof JCPackage) {
            JCPackage pkg = (JCPackage)obj;
            FileObject[] fos = getDocFileObjects(pkg.getName(), PACKAGE_SUMMARY);
            for (int i = 0; i < fos.length; i++) {
                try {
                    urlList.add(fos[i].getURL());
                } catch (FileStateInvalidException e) {
                }
            }
        } else if (obj instanceof JCClass) {
            JCClass cls = (JCClass)obj;
            FileObject[] fos = getDocFileObjects(cls.getPackageName(), cls.getName());
            for (int i = 0; i < fos.length; i++) {
                try {
                    urlList.add(fos[i].getURL());
                } catch (FileStateInvalidException e) {
                }
            }
        } else if (obj instanceof JCConstructor) { // covers JCMethod too
            JCConstructor ctr = (JCConstructor)obj;
            JCClass cls = ctr.getClazz();
            FileObject[] fos = getDocFileObjects(cls.getPackageName(), cls.getName());
            for (int i = 0; i < fos.length; i++) {
                try {
                    URL url = fos[i].getURL();
                    StringBuffer sb = new StringBuffer("#"); // NOI18N
                    sb.append((obj instanceof JCMethod) ? ((JCMethod)ctr).getName() : cls.getName());
                    sb.append('(');
                    JCParameter[] parms = ctr.getParameters();
                    int cntM1 = parms.length - 1;
                    for (int j = 0; j <= cntM1; j++) {
                        sb.append(parms[j].getType().format(true));
                        if (j < cntM1) {
                            sb.append(", "); // NOI18N
                        }
                    }
                    sb.append(')');
                    try {
                        urlList.add(new URL(url.toString() + sb));
                    } catch (MalformedURLException e) {
                    }
                } catch (FileStateInvalidException e) {
                }
            }
        } else if (obj instanceof JCField) {
            JCField fld = (JCField)obj;
            JCClass cls = fld.getClazz();
            FileObject[] fos = getDocFileObjects(cls.getPackageName(), cls.getName());
            for (int i = 0; i < fos.length; i++) {
                try {
                    URL url = fos[i].getURL();
                    try {
                        urlList.add(new URL(url.toString() + '#' + fld.getName()));
                    } catch (MalformedURLException e) {
                    }
                } catch (FileStateInvalidException e) {
                }
            }
        }

        URL[] ret = new URL[urlList.size()];
        urlList.toArray(ret);
        return ret;
    }

    public Debugger getDebugger() {
        try {
            return TopManager.getDefault().getDebugger();
        } catch (DebuggerNotFoundException e) {
            return null;
        }
    }

    public Breakpoint getBreakpoint(int pos) {
        Debugger debugger = getDebugger();
        Line line = KitSupport.getLine(doc, pos);
        if (debugger != null && line != null) {
            return debugger.findBreakpoint(line);
        }
        return null;
    }

    public Breakpoint createBreakpoint(int pos) {
        Debugger debugger = getDebugger();
        Line line = KitSupport.getLine(doc, pos);
        if (debugger != null && line != null) {
            return debugger.createBreakpoint(line);
        }
        return null;
    }


}

/*
 * Log
 *  19   Gandalf   1.18        1/26/00  Miloslav Metelka refreshParents() not 
 *       called
 *  18   Gandalf   1.17        1/18/00  Miloslav Metelka 
 *  17   Gandalf   1.16        1/13/00  Miloslav Metelka Localization
 *  16   Gandalf   1.15        1/11/00  Miloslav Metelka 
 *  15   Gandalf   1.14        1/10/00  Miloslav Metelka 
 *  14   Gandalf   1.13        1/7/00   Miloslav Metelka 
 *  13   Gandalf   1.12        1/4/00   Miloslav Metelka 
 *  12   Gandalf   1.11        12/28/99 Miloslav Metelka 
 *  11   Gandalf   1.10        11/14/99 Miloslav Metelka 
 *  10   Gandalf   1.9         11/11/99 Miloslav Metelka 
 *  9    Gandalf   1.8         11/9/99  Miloslav Metelka 
 *  8    Gandalf   1.7         11/8/99  Miloslav Metelka 
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/10/99 Miloslav Metelka 
 *  5    Gandalf   1.4         9/15/99  Miloslav Metelka 
 *  4    Gandalf   1.3         9/10/99  Miloslav Metelka 
 *  3    Gandalf   1.2         8/18/99  Miloslav Metelka 
 *  2    Gandalf   1.1         8/18/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/30/99  Miloslav Metelka 
 * $
 */

