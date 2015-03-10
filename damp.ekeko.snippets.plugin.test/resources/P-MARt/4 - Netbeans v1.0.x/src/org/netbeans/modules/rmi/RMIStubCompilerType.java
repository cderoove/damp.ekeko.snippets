/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.beans.*;
import java.util.*;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.execution.NbProcessDescriptor;
import org.openide.src.*;
import org.openide.util.*;


/**
 *
 * @author  mryzl
 */

public class RMIStubCompilerType extends CompilerType {

    /** Serial version UID. */
    static final long serialVersionUID = 3511317103361547621L;

    /** Resource bundle. */
    private static final ResourceBundle bundle = NbBundle.getBundle(RMIStubCompilerType.class);

    /** Name of the property stubCompiler. */
    public static final String PROP_StubCompiler = "stubCompiler"; // NOI18N

    /** Holds value of property stubCompiler. */
    private NbProcessDescriptor stubCompiler;

    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport (this);

    /** Default process. */
    public static final String DEFAULT_COMPILER_PROCESS = "{java.home}{/}..{/}bin{/}rmic"; // NOI18N

    /** Default arguments. */
    public static final String DEFAULT_COMPILER_ARGUMENTS = "-classpath {java.home}{/}..{/}lib{/}rt.jar{:}{filesystems}{:}{classpath}{:}{bootclasspath} -d {" +  // NOI18N
            RMIStubCompilerGroup.TAG_PACKAGEROOT + "} {files}"; // NOI18N

    /** Info. */
    public static final String INFO = bundle.getString("MSG_CompilerHint"); // NOI18N

    {
        setStubCompiler(new NbProcessDescriptor(
                            DEFAULT_COMPILER_PROCESS,
                            DEFAULT_COMPILER_ARGUMENTS,
                            INFO
                        ));
    }

    /** Creates new RMIStubCompilerType. */
    public RMIStubCompilerType() {
    }

    public void prepareJob(CompilerJob job, Class type, DataObject obj) {
        //    ((RMIDataObject)obj).createCompiler(job, type);
        String[] classes = getRemoteClasses(obj);
        FileObject fo = obj.getPrimaryFile();
        if (classes.length > 0) {
            for(int i = 0; i < classes.length; i++) {
                new RMIStubCompiler(job, fo, classes[i], type, getStubCompiler(), ExternalCompiler.JAVAC);
            }
        } else {
            new RMIStubCompiler(job, fo, fo.getPackageName('.'), type, getStubCompiler(), ExternalCompiler.JAVAC);
        }
    }

    /** Alternative method of inocation
    */
    public void prepareJob(org.openide.compiler.Compilable c, Class type, DataObject obj) {
        String[] classes = getRemoteClasses(obj);
        FileObject fo = obj.getPrimaryFile();
        if (classes.length > 0) {
            for(int i = 0; i < classes.length; i++) {
                RMIStubCompiler cmp = new RMIStubCompiler(fo, classes[i], type, getStubCompiler(), ExternalCompiler.JAVAC);
                cmp.dependsOn (c);
            }
        } else {
            RMIStubCompiler cmp = new RMIStubCompiler(fo, fo.getPackageName('.'), type, getStubCompiler(), ExternalCompiler.JAVAC);
            cmp.dependsOn (c);
        }
    }

    /** Return names of remote classes in the file.
    */
    protected static String[] getRemoteClasses(DataObject obj) {
        LinkedList list = new LinkedList();

        SourceCookie sc = (SourceCookie) obj.getCookie(SourceCookie.class);
        SourceElement se = sc.getSource();
        Identifier pkg = se.getPackage();
        String pname = null;
        int index = 0;
        if (pkg != null) {
            pname = pkg.getFullName();
            index = pname.length();
        }

        ClassElement[] ces = se.getAllClasses();
        for(int i = 0; i < ces.length; i++) {
            if (RMIHelper.implementsClass(ces[i], RMIHelper.REMOTE)) {
                String name = ces[i].getName().getFullName();
                if (pkg != null) {
                    name = name.substring(index + 1);
                    name.replace('.', '$');
                    name = pname + '.' + name;
                } else {
                    name.replace('.', '$');
                }
                list.add(name);
            }
        }

        return (String[])list.toArray(new String[list.size()]);
    }

    /** Getter for property stubCompiler.
     *@return Value of property stubCompiler.
     */
    public NbProcessDescriptor getStubCompiler() {
        return stubCompiler;
    }

    /** Setter for property stubCompiler.
     *@param stubCompiler New value of property stubCompiler.
     */
    public void setStubCompiler(NbProcessDescriptor stubCompiler) {
        NbProcessDescriptor oldStubCompiler = this.stubCompiler;
        this.stubCompiler = stubCompiler;
        propertyChangeSupport.firePropertyChange (PROP_StubCompiler , oldStubCompiler ,stubCompiler);
    }

    /** human presentable name */
    public String displayName() {
        return bundle.getString("CTL_RMIStubCompilerType"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(RMIStubCompilerType.class);
    }
}

/*
* <<Log>>
*  10   Gandalf-post-FCS1.8.1.0     3/20/00  Martin Ryzl     localization
*  9    Gandalf   1.8         1/31/00  Martin Ryzl     problems with compilation
*       files with null package removed
*  8    Gandalf   1.7         1/28/00  Martin Ryzl     if RMICompiler used with 
*       non remote object it writes error message
*  7    Gandalf   1.6         1/24/00  Martin Ryzl     compilation of inner 
*       classes added
*  6    Gandalf   1.5         1/21/00  Martin Ryzl     compilation fixed (new 
*       API)
*  5    Gandalf   1.4         12/23/99 Jaroslav Tulach mergeInto deleted from 
*       the CompilerJob.
*  4    Gandalf   1.3         11/27/99 Patrik Knakal   
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/6/99  Martin Ryzl     debug info removed
*  1    Gandalf   1.0         10/6/99  Martin Ryzl     
* $ 
*/ 
