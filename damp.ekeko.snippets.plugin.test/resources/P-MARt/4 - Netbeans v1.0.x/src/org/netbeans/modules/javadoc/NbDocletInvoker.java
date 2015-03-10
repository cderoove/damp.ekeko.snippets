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

package org.netbeans.modules.javadoc;

import java.util.*;
import java.net.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import java.io.File;
import java.io.IOException;

import org.openide.execution.*;
import org.openide.windows.OutputWriter;

import com.sun.javadoc.*;
import com.sun.tools.doclets.HtmlDocWriter;

/**
 * Class creates, controls and invokes doclets.
 */
class NbDocletInvoker {

    private final Class docletClass;
    private final String docletClassName;
    private final ClassLoader appClassLoader;

    private OutputWriter errWriter;

    private static class DocletInvokeException extends Exception {static final long serialVersionUID =-6185095454840685769L;
    }

    // Should never be called.
    // only here to keep the compiler happy.
    private NbDocletInvoker() {
        docletClassName = null;
        docletClass = null;
        appClassLoader = null;
    }

    private String appendPath(String path1, String path2) {
        if (path1 == null || path1.length() == 0) {
            return path2 == null ? "." : path2; // NOI18N
        } else if (path2 == null || path2.length() == 0) {
            return path1;
        } else {
            return path1  + File.pathSeparator + path2;
        }
    }

    NbDocletInvoker(String docletClassName, String docletPath, OutputWriter err ) {

        errWriter = err;

        HtmlDocWriter.configuration = null;

        this.docletClassName = docletClassName;

        // construct class loader
        String cpString = null;   // make sure env.class.path defaults to dot

        // do prepends to get correct ordering
        cpString = appendPath(System.getProperty("env.class.path"), cpString);
        cpString = appendPath(System.getProperty("java.class.path"), cpString);
        cpString = appendPath(docletPath, cpString);
        URL[] urls = pathToURLs(cpString);
        appClassLoader = new URLClassLoader(urls);

        // attempt to find doclet
        Class dc = null;
        try {
            dc = appClassLoader.loadClass(docletClassName);
        } catch (ClassNotFoundException exc) {
            //Res.error("main.doclet_class_not_found", docletClassName); // NOI18N
            //Main.exit();
        }
        docletClass = dc;
    }

    /**
     * Generate documentation here.  Return true on success.
     */
    boolean start(RootDoc root) {
        Object retVal;
        String methodName = "start"; // NOI18N
        Class[] paramTypes = new Class[1];
        Object[] params = new Object[1];
        try {
            paramTypes[0] = Class.forName("com.sun.javadoc.RootDoc"); // NOI18N
        } catch (ClassNotFoundException exc) {
            return false; // should never happen
        }
        params[0] = root;
        try {
            retVal = invoke(methodName, null, paramTypes, params);
        } catch (DocletInvokeException exc) {
            return false;
        }
        if (retVal instanceof Boolean) {
            return ((Boolean)retVal).booleanValue();
        } else {
            //            Res.error("main.must_return_boolean", // NOI18N
            //		      docletClassName, methodName);
            return false;
        }
    }

    /**
     * Check for doclet added options here. Zero return means
     * option not known.  Positive value indicates number of
     * arguments to option.  Negative value means error occurred.
     */
    int optionLength(String option) {
        Object retVal;
        String methodName = "optionLength"; // NOI18N
        Class[] paramTypes = new Class[1];
        Object[] params = new Object[1];
        paramTypes[0] = option.getClass();
        params[0] = option;
        try {
            retVal = invoke(methodName, new Integer(0), paramTypes, params);
        } catch (DocletInvokeException exc) {
            return -1;
        }
        if (retVal instanceof Integer) {
            return ((Integer)retVal).intValue();
        } else {
            //            Res.error("main.must_return_int", docletClassName, methodName); // NOI18N
            return -1;
        }
    }

    /**
     * Let doclet check that all options are OK. Returning true means
     * options are OK.  If method does not exist, assume true.
     */
    boolean validOptions(String options[][]) {
        //return true;

        Object retVal;
        String methodName = "validOptions"; // NOI18N
        DocErrorReporter reporter = new DocErrorReporterImpl( errWriter );
        Class[] paramTypes = new Class[2];
        Object[] params = new Object[2];
        paramTypes[0] = options.getClass();
        try {
            paramTypes[1] = Class.forName("com.sun.javadoc.DocErrorReporter"); // NOI18N
        } catch (ClassNotFoundException exc) {
            return false; // should never happen
        }
        params[0] = options;
        params[1] = reporter;
        try {
            retVal = invoke(methodName, Boolean.TRUE, paramTypes, params);
        } catch (DocletInvokeException exc) {
            return false;
        }
        if (retVal instanceof Boolean) {
            return ((Boolean)retVal).booleanValue();
        } else {
            //Res.error("main.must_return_boolean", docletClassName, methodName); // NOI18N
            return false;
        }
    }

    /**
     * Utility method for calling doclet functionality
     */
    private Object invoke(String methodName, Object returnValueIfNonExistent,
                          Class[] paramTypes, Object[] params)
    throws DocletInvokeException {
        Method meth;
        try {
            meth = docletClass.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException exc) {
            if (returnValueIfNonExistent == null) {
                //                  Res.error("main.doclet_method_not_found", // NOI18N
                //                              docletClassName, methodName);
                throw new DocletInvokeException();
            } else {
                return returnValueIfNonExistent;
            }
        } catch (SecurityException exc) {
            //Res.error("main.doclet_method_not_accessible", // NOI18N
            //			  docletClassName, methodName);
            throw new DocletInvokeException();
        }
        if (!Modifier.isStatic(meth.getModifiers())) {
            //    Res.error("main.doclet_method_must_be_static", // NOI18N
            //            docletClassName, methodName);
            throw new DocletInvokeException();
        }
        try {
            Thread.currentThread().setContextClassLoader(appClassLoader);
            return meth.invoke(null , params);
        } catch (IllegalArgumentException exc) {
            //      Res.error("main.internal_error_exception_thrown", // NOI18N
            //	  docletClassName, methodName, exc.toString());
            throw new DocletInvokeException();
        } catch (IllegalAccessException exc) {
            //    Res.error("main.doclet_method_not_accessible", // NOI18N
            //  docletClassName, methodName);
            throw new DocletInvokeException();
        } catch (NullPointerException exc) {
            //      Res.error("main.internal_error_exception_thrown", // NOI18N
            //docletClassName, methodName, exc.toString());
            throw new DocletInvokeException();
        } catch (InvocationTargetException exc) {
            //                Res.error("main.exception_thrown", // NOI18N
            // docletClassName, methodName, exc.toString());
            exc.getTargetException().printStackTrace();
            throw new DocletInvokeException();
        }
    }

    /**
     * Utility method for converting a search path string to an array
     * of directory and JAR file URLs.
     *
     * @param path the search path string
     * @return the resulting array of directory and JAR file URLs
     */
    static URL[] pathToURLs(String path) {
        StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
        URL[] urls = new URL[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            URL url = fileToURL(new File(st.nextToken()));
            if (url != null) {
                urls[count++] = url;
            }
        }
        if (urls.length != count) {
            URL[] tmp = new URL[count];
            System.arraycopy(urls, 0, tmp, 0, count);
            urls = tmp;
        }
        return urls;
    }

    /**
     * Returns the directory or JAR file URL corresponding to the specified
     * local file name.
     *
     * @param file the File object
     * @return the resulting directory or JAR file URL, or null if unknown
     */
    static URL fileToURL(File file) {
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            name = file.getAbsolutePath();
        }
        name = name.replace(File.separatorChar, '/');
        if (!name.startsWith("/")) { // NOI18N
            name = "/" + name; // NOI18N
        }
        // If the file does not exist, then assume that it's a directory
        if (!file.isFile()) {
            name = name + "/"; // NOI18N
        }
        try {
            return new URL("file", "", name); // NOI18N
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("file"); // NOI18N
        }
    }
}




/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Petr Hrebejk    i18n mk3  
 *  6    Gandalf   1.5         1/12/00  Petr Hrebejk    i18n
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/11/99  Petr Hrebejk    
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
