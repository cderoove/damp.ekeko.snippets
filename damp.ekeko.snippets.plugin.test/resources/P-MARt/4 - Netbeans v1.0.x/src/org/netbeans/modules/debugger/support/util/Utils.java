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

package org.netbeans.modules.debugger.support.util;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystemCapability;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.nodes.Node;
import org.openide.src.*;
import org.openide.cookies.LineCookie;
import org.openide.cookies.EditorCookie;
import org.openide.execution.NbClassPath;
import org.openide.execution.NbProcessDescriptor;
import org.openide.debugger.DebuggerInfo;
import org.openide.util.MapFormat;

import java.util.HashMap;
import java.io.*;
import java.util.ResourceBundle;

import org.netbeans.modules.debugger.support.ProcessDebuggerType;
import org.netbeans.modules.debugger.support.ProcessDebuggerInfo;


/**
* Helper methods for debugging.
*
* @author  Jan Jancura
*/
public class Utils {

    static ResourceBundle                     bundle = org.openide.util.NbBundle.getBundle
            (org.netbeans.modules.debugger.support.AbstractDebugger.class);


    /**
    * Returns localized Exception.
    */
    public static RuntimeException localizeException (Exception e, String text) {
        return new ExceptionHack (e, text);
    }

    /**
    * Removes leading and ending quote character, if there is any
    */
    private static String removeQuotationMarks (String s) {
        if (s == null)
            return null;
        if (s.startsWith ("\"")) // NOI18N
            s = s.substring (1, s.length ());
        if (s.endsWith("\"")) // NOI18N
            s = s.substring (0, s.length () - 1);
        return s;
    }

    /**
    * Creates map of arguments for NbProcessDescriptor.
    */
    public static HashMap processDebuggerInfo (
        DebuggerInfo   info,
        String         debuggerOptions,
        String         main
    ) {
        // create starting string
        String repository = removeQuotationMarks (NbClassPath.createRepositoryPath (FileSystemCapability.DEBUG).getClassPath ());
        String library = removeQuotationMarks (NbClassPath.createLibraryPath ().getClassPath ());
        String classPath = removeQuotationMarks (NbClassPath.createClassPath ().getClassPath ());
        String bootClassPath = removeQuotationMarks (NbClassPath.createBootClassPath ().getClassPath ());
        String pathSeparator = System.getProperty ("path.separator"); // NOI18N
        String javaHome = System.getProperty ("java.home"); // NOI18N
        boolean classic = false;
        if (info instanceof ProcessDebuggerInfo) {
            ProcessDebuggerInfo processInfo = (ProcessDebuggerInfo)info;
            if (processInfo.getClassPath () != null)
                classPath = removeQuotationMarks (processInfo.getClassPath ());
            if (processInfo.getBootClassPath () != null)
                bootClassPath = removeQuotationMarks (processInfo.getBootClassPath ());
            if (processInfo.getRepositoryPath () != null)
                repository = removeQuotationMarks (processInfo.getRepositoryPath ());
            if (processInfo.getLibraryPath () != null)
                library = removeQuotationMarks (processInfo.getLibraryPath ());
            classic = processInfo.isClassic ();
        }
        //    if (System.getProperty ("java.vm.name", "?").indexOf ("HotSpot") >= 0)
        //      classic = true;
        HashMap map = new HashMap ();
        map.put (ProcessDebuggerType.CLASSIC_SWITCH, classic ? "-classic " : ""); // NOI18N
        map.put (ProcessDebuggerType.DEBUGGER_OPTIONS, debuggerOptions);
        map.put (ProcessDebuggerType.REPOSITORY_SWITCH, repository);
        map.put (ProcessDebuggerType.LIBRARY_SWITCH,
                 ((library.length () > 0) && (repository.length () > 0)) ? pathSeparator + library : library
                );
        map.put (ProcessDebuggerType.CLASS_PATH_SWITCH,
                 ((classPath.length () > 0) && ((library.length () + repository.length ()) > 0)) ? pathSeparator + classPath : classPath
                );
        map.put (ProcessDebuggerType.BOOT_CLASS_PATH_SWITCH, bootClassPath);
        map.put (ProcessDebuggerType.BOOT_CLASS_PATH_SWITCH_SWITCH, (bootClassPath.length () < 1) ? "" : "-Xbootclasspath:"); // NOI18N
        map.put (ProcessDebuggerType.MAIN_SWITCH, main);
        map.put (ProcessDebuggerType.QUOTE_SWITCH, "\""); // NOI18N
        map.put (ProcessDebuggerType.JAVA_HOME_SWITCH, javaHome);
        map.put (ProcessDebuggerType.FILE_SEPARATOR_SWITCH, System.getProperty ("file.separator")); // NOI18N
        map.put (ProcessDebuggerType.PATH_SEPARATOR_SWITCH, pathSeparator);
        return map;
    }

    /**
    * Returns true if HotSpot is installed for given JVM.
    */
    public static boolean hasHotSpot (String path) {
        try {
            int i = path.lastIndexOf (File.separatorChar);
            String sub = path.substring (0 ,i);
            File f = new File (sub + File.separatorChar + "hotspot"); // NOI18N
            if (f.exists ())
                return true;
            // Solaris ...
            i = sub.lastIndexOf (File.separatorChar);
            f = new File (sub.substring (0, i) + File.separatorChar + "lib" // NOI18N
                          + File.separatorChar + "sparc" + File.separatorChar + "hotspot"); // NOI18N
            if (f.exists ())
                return true;
        }
        catch (Exception e) {
        }

        boolean found = false;
        try {
            Process process = Runtime.getRuntime ().exec (path.concat (" -version")); // NOI18N
            // err stream
            BufferedReader bufferedReader = new BufferedReader (
                                                new InputStreamReader (process.getErrorStream ())
                                            );
            String line = bufferedReader.readLine ();
            while (line != null) {
                if (line.toLowerCase ().indexOf ("hotspot") > -1) { // NOI18N
                    found = true;
                    break;
                }
                line = bufferedReader.readLine ();
            }
            bufferedReader.close ();
            if (!found) {
                // stdout stream
                bufferedReader = new BufferedReader (new InputStreamReader (process.getInputStream ()));
                line = bufferedReader.readLine ();
                while (line != null) {
                    if (line.toLowerCase ().indexOf ("hotspot") > -1) { // NOI18N
                        found = true;
                        break;
                    }
                    line = bufferedReader.readLine ();
                }
                bufferedReader.close ();
            }
        }
        catch (IOException e) {}
        catch (SecurityException  e) {}
        return found;
    }

    /**
    * Sets current line in editor.
    */
    public static String getClassName (
        String className
    ) {
        int i = className.lastIndexOf ('.');
        return ((i > 0) ? className.substring (i + 1) : className);
    }

    /**
    * Sets current line in editor.
    */
    public static String getTopClassName (
        String className
    ) {
        // String cn = getClassName (className);
        int i = className.indexOf ('$');
        return ((i > 0) ? className.substring (0, i) : className);
    }

    /**
    * Sets current line in editor.
    */
    public static String getPackageName (
        String className
    ) {
        int i = className.lastIndexOf ('.');
        return ((i > 0) ? className.substring (0, i) : ""); // NOI18N
    }

    /**
    * Return line for given params.
    * className "java.lang.Object$1" sourceName "Object.java".
    */
    public static Line getLineForSource (
        String className,
        String sourceName,
        int lineNumber
    ) {
        if (sourceName == null) return getLine (className, lineNumber);
        int i = sourceName.lastIndexOf ('.');
        if (i > 0)
            sourceName = sourceName.substring (0, i);
        return getLine (
                   getPackageName (className),
                   sourceName,
                   lineNumber
               );
    }

    /**
    * Return line for given params.
    */
    public static Line getLine(String className,int lineNumber) {
        // PATCH: ClassElement.forName cannot return ClassName$1
        ClassElement cls = ClassElement.forName (
                               getTopClassName (className)
                           );

        if (cls == null) return null;
        LineCookie lineCookie = (LineCookie) cls.getCookie (LineCookie.class);
        if (lineCookie == null) return null;
        Line.Set set = lineCookie.getLineSet ();
        return set.getOriginal (lineNumber - 1);
    }

    /**
    * Return line for given params.
    */
    private static Line getLine (
        String packageName,
        String className,
        int lineNumber
    ) {
        return getLine (
                   packageName + '.' + className,
                   lineNumber
               );
    }

    /**
    * Shows given line in editor, and returns it.
    */
    public static Line showInEditor (
        Line line
    ) {
        if (line == null) return null;
        try {
            line.show (line.SHOW_GOTO, 0);
        } catch (Throwable e ) {
            if (e instanceof ThreadDeath)
                throw (ThreadDeath)e;
            TopManager.getDefault ().notify (new NotifyDescriptor.Exception (
                                                 e,
                                                 bundle.getString ("EXC_Editor")
                                             ));
        }
        return line;
    }

    /**
    * Obtains exception name from errorText.
    */
    public static String getExceptionName (String errorText) {
        try {
            int index1, index2;
            index1 = errorText.indexOf (':')+1;
            String s = errorText.substring (index1);
            s = s.trim ();
            index1 = s.indexOf (':');
            index2 = s.indexOf (' ');
            if ((index2 >= 0) && (index2 < index1))
                index1 = index2;
            index2 = s.indexOf ('\n');
            if ((index2 >= 0) && (index2 < index1))
                index1 = index2;
            s = (s.substring (0, index1)).trim();
            if (s.length() == 0)
                return null;
            else
                return s;
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static String getCurrentClassName (Node[] nodes) {
        if (nodes == null) return "";
        if (nodes.length != 1) return "";
        Node n = nodes [0];
        Element e = (Element) n.getCookie (Element.class);
        return getClassNameForElement (e);
    }

    public static String getClassNameForElement (Element e) {
        if (e == null) return "";
        if (e instanceof ClassElement)
            return getClassName ((ClassElement) e);
        if (e instanceof ConstructorElement)
            return getClassName (((ConstructorElement) e).getDeclaringClass ());
        if (e instanceof FieldElement)
            return getClassName (((FieldElement) e).getDeclaringClass ());
        return "";
    }

    private static String getClassName (ClassElement e) {
        String f = e.getName ().getFullName ();
        if (!e.isInner ()) return f;
        Identifier ident = e.getSource ().getPackage ();
        String c;
        if (ident == null) c = ""; // NOI18N
        else c = ident.getFullName ();
        if (c.length () > 0)
            return c + '.' + f.substring (c.length () + 1).replace ('.', '$');
        return f.replace ('.', '$');
    }

    public static String getCurrentMethodName (Node[] nodes) {
        if (nodes == null) return "";
        if (nodes.length != 1) return "";

        Node n = nodes [0];
        ConstructorElement e = (ConstructorElement) n.getCookie (
                                   ConstructorElement.class
                               );
        if (e == null) return "";
        String name = ((ConstructorElement) e).getName ().getName ();
        String cls = getCurrentClassName (nodes);
        int i = cls.lastIndexOf ('.');
        if ((i >= 0) && cls.substring (i + 1).equals (name))
            return "<init>";
        return name;
    }

    public static String getCurrentFieldName (Node[] nodes) {
        if (nodes == null) return "";
        if (nodes.length != 1) return "";

        Node n = nodes [0];
        FieldElement e = (FieldElement) n.getCookie (
                             FieldElement.class
                         );
        if (e == null) return "";
        return ((FieldElement) e).getName ().getName ();
    }

    public static int getCurrentLineNumber (Node[] nodes) {
        if (nodes == null) return 0;
        if (nodes.length != 1) return 0;

        Node n = nodes [0];
        EditorCookie e = (EditorCookie) n.getCookie (
                             EditorCookie.class
                         );
        if ((e == null) || (e.getOpenedPanes () == null) || (e.getOpenedPanes ().length < 1)) return 0;
        return NbDocument.findLineNumber (
                   e.getDocument (),
                   e.getOpenedPanes () [0].getCaret ().getDot ()
               ) + 1;
    }

    public static String getCurrentIdentifier (Node[] nodes) {
        if (nodes == null) return "";
        if (nodes.length != 1) return "";

        Node n = nodes [0];
        EditorCookie e = (EditorCookie) n.getCookie (
                             EditorCookie.class
                         );
        if ((e == null) || (e.getOpenedPanes () == null) || (e.getOpenedPanes ().length < 1)) return "";
        String s = e.getOpenedPanes () [0].getSelectedText ();
        if (s == null) return "";
        return s;
        /*    return NbDocument.findLineNumber (
              e.getDocument (),
              e.getOpenedPanes () [0].getCaret ().getDot ()
            );*/
    }


    // innerclasses .........................................................................

    /**
    * Localised Exception.
    */
    /**
    * Hack for using Exception dialog with Details button.
    */
    static class ExceptionHack extends RuntimeException {

        /** Original exception. */
        private Throwable t;
        /** Localized text. */
        private String text;

        ExceptionHack (Throwable t, String text) {
            super (""); // NOI18N
            this.t = t;
            this.text = text;
        }

        public String getMessage () {
            return text + " "; // NOI18N
        }

        public String getLocalizedMessage () {
            return text;
        }

        public void printStackTrace (java.io.PrintStream s) {
            t.printStackTrace (s);
        }

        public void printStackTrace (java.io.PrintWriter s) {
            t.printStackTrace (s);
        }
    }
}
/*
 * Log
 *  22   Gandalf-post-FCS1.18.3.2    4/20/00  Daniel Prusa    a correction in 
 *       getClassName 
 *  21   Gandalf-post-FCS1.18.3.1    4/17/00  Daniel Prusa    getLine
 *  20   Gandalf-post-FCS1.18.3.0    3/28/00  Daniel Prusa    
 *  19   Gandalf   1.18        2/11/00  Daniel Prusa    a small correction of 
 *       previous update
 *  18   Gandalf   1.17        2/10/00  Daniel Prusa    Update of HotSpot 
 *       detection (Solaris)
 *  17   Gandalf   1.16        1/25/00  Daniel Prusa    quotation characters
 *  16   Gandalf   1.15        1/20/00  Daniel Prusa    hasHotSpot updated for 
 *       Solaris
 *  15   Gandalf   1.14        1/18/00  Daniel Prusa    {java.home} switch
 *  14   Gandalf   1.13        1/15/00  Daniel Prusa    catch in GetLine
 *  13   Gandalf   1.12        1/13/00  Daniel Prusa    NOI18N
 *  12   Gandalf   1.11        1/6/00   Daniel Prusa    Quote character switch 
 *       added
 *  11   Gandalf   1.10        1/5/00   Jan Jancura     Bug 4276
 *  10   Gandalf   1.9         12/9/99  Daniel Prusa    getExceptionName method 
 *       added, getTopClassName changed
 *  9    Gandalf   1.8         11/29/99 Jan Jancura     Better recognition of 
 *       HotSpot
 *  8    Gandalf   1.7         11/8/99  Jan Jancura     Somma classes renamed
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/7/99  Jan Jancura     Unification of debugger 
 *       types.
 *  5    Gandalf   1.4         9/2/99   Jan Jancura     
 *  4    Gandalf   1.3         7/30/99  Jaroslav Tulach 
 *  3    Gandalf   1.2         6/11/99  Jan Jancura     
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 */
