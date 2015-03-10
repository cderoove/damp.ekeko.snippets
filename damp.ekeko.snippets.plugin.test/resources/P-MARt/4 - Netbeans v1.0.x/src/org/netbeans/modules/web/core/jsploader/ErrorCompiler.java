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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.ErrorEvent;
import org.openide.cookies.CompilerCookie;
import org.openide.filesystems.FileObject;

import com.sun.jsp.JspException;
import com.sun.jsp.compiler.ParseException;

/** Compiler which fires an error event, that's it.
*
* @author Petr Jiricka
*/
public class ErrorCompiler extends Compiler {

    protected final FileObject file;
    protected final Throwable throwable;
    protected boolean inclStackTrace;

    public ErrorCompiler(FileObject file,
                         Throwable throwable, boolean inclStackTrace) {
        super();
        this.file = file;
        this.throwable = throwable;
        this.inclStackTrace = inclStackTrace;
    }

    public FileObject getFileObject() {
        return file;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean includeStackTrace() {
        return inclStackTrace;
    }

    /**
     */
    public Class compilerGroupClass() {
        return Group.class;
    }

    /** Checks if the class corresponding to this JSP is up to date
     *
     */
    public boolean isUpToDate() {
        return false;
    }

    /** See {@link Compilable#equals(java.lang.Object)}
    */
    public boolean equals (Object other) {
        if (!(other instanceof ErrorCompiler))
            return false;
        ErrorCompiler comp2 = (ErrorCompiler)other;
        return (comp2.file == file &&
                comp2.throwable.equals(throwable) &&
                (comp2.inclStackTrace == inclStackTrace));
    }

    public int hashCode() {
        return ((throwable == null) ? 0 : throwable.hashCode());
    }

    /** Identifier for type of compiler. This method allows subclasses to specify
     * the type this compiler belongs to. Compilers that belong to the same class
     * will be compiled together by one external process.
     * <P>
     * It is necessary for all compilers of the same type to have same process
     * descriptor and error expression.
     * <P>
     * This implementation returns the process descriptor, so all compilers
     * with the same descriptor will be compiled at once.
     *
     * @return key to define type of the compiler (file object representing root of filesystem) 
     *         or null if there are any errors
     * @see ExternalCompilerGroup#createProcess
     */
    public Object compilerGroupKey () {
        return throwable; // just one compiler per group
    }

    /** Compiler group for actual throwing the errorevent specified by the compiler. */
    public static class Group extends CompilerGroup {

        public Group() {
            super();
        }

        private ErrorCompiler comp = null;

        public void add(Compiler c) throws IllegalArgumentException {
            if (!(c instanceof ErrorCompiler))
                throw new IllegalArgumentException();
            if (comp != null)
                throw new IllegalArgumentException();
            comp = ((ErrorCompiler)c);
        }

        public boolean start() {
            fireErrorEvent(constructError(this, comp.getThrowable(), comp.getFileObject(),
                                          comp.includeStackTrace()));
            return false;
        }

        /** Error creation - create a compiler error event from a throwable,
        * special treatment of JspExceptions and ParseExceptions */
        public static ErrorEvent constructError(CompilerGroup group,
                                                Throwable th, FileObject file, boolean includeStackTrace) {
            ErrorEvent ee = constructAccurateError(group, th, file);
            if (ee != null)
                return ee;
            return new ErrorEvent(group, file, -1, -1,
                                  getThrowableMessage(th, includeStackTrace), ""); // NOI18N
        }

        private static String getThrowableMessage(Throwable throwable,
                boolean includeStackTrace) {
            if (includeStackTrace) {
                StringWriter swriter = new StringWriter();
                PrintWriter pw = new PrintWriter(swriter);
                throwable.printStackTrace(pw);
                pw.close();
                return swriter.toString();
            }
            else {
                return throwable.getMessage();
            }
        }

        /** Error creation - create a compiler error event from a JSPException which contains file and line
        * number information.
        */
        private static ErrorEvent constructAccurateError(CompilerGroup group,
                Throwable ex, FileObject file) {
            while (!(ex instanceof ParseException)) {
                if (!(ex instanceof JspException)) return null;
                ex = ((JspException)ex).getException();
            }

            // now I know it is ParseException, which starts with error location description
            String m1 = ex.getMessage();
            int lpar = m1.indexOf('(');
            if (lpar == -1) return null;
            int comma = m1.indexOf(',', lpar);
            if (comma == -1) return null;
            int rpar = m1.indexOf(')', comma);
            if (rpar == -1) return null;
            String line = m1.substring(lpar + 1, comma);
            String col = m1.substring(comma + 1, rpar);
            FileObject realFo = JspCompileUtil.findFileObjectForFile(m1.substring(0, lpar));
            if (realFo == null)
                realFo = file;
            try {
                return new ErrorEvent(group, realFo, Integer.parseInt(line) + 1, Integer.parseInt(col),
                                      m1.substring(rpar + 1).trim(), "");  // NOI18N
                // pending - should also include a line of code
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
    } // end of inner class Group

}

/*
 * Log
 *  9    Gandalf   1.8         1/17/00  Petr Jiricka    Debug outputs removed
 *  8    Gandalf   1.7         1/15/00  Petr Jiricka    Ensuring correct 
 *       compiler implementation - hashCode and equals
 *  7    Gandalf   1.6         1/13/00  Petr Jiricka    More i18n
 *  6    Gandalf   1.5         1/12/00  Petr Jiricka    Fully I18n-ed
 *  5    Gandalf   1.4         1/12/00  Petr Jiricka    i18n phase 1
 *  4    Gandalf   1.3         1/6/00   Petr Jiricka    Cleanup
 *  3    Gandalf   1.2         1/3/00   Petr Jiricka    API-related changes
 *  2    Gandalf   1.1         12/28/99 Petr Jiricka    Reflecting compilation 
 *       API changes
 *  1    Gandalf   1.0         12/20/99 Petr Jiricka    
 * $
 */
