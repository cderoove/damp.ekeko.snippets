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

import java.io.IOException;
import java.text.MessageFormat;

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.ExternalCompiler;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.modules.rmi.settings.RMISettings;

/** RMI stub compiler.
*
* @author Martin Ryzl
*/
public class RMIStubCompiler extends ExternalCompiler {

    /** Stub compiler type. */
    private RMIStubCompilerType myType;

    /** File name. */
    protected String fname;

    /** Copy of type */
    protected final Object type;

    /**
    * @param fo FileObject to compile
    * @param fname name of file if different from fo's name (or null)
    * @param nbDescriptor external process
    * @param err error expression
    */
    public RMIStubCompiler(FileObject fo, String fname, Object type, NbProcessDescriptor nbDescriptor,ExternalCompiler.ErrorExpression err) {
        super(fo, type, nbDescriptor, err);
        this.type = type;
        this.fname = fname;
    }

    /**
    * @param fo FileObject to compile
    * @param fname name of file if different from fo's name (or null)
    * @param nbDescriptor external process
    * @param err error expression
    */
    public RMIStubCompiler(Compiler[] dependencies, FileObject fo, String fname, Object type,NbProcessDescriptor nbDescriptor,ExternalCompiler.ErrorExpression err) {
        super(dependencies, fo, type, nbDescriptor, err);
        this.type = type;
        this.fname = fname;
    }

    /**
    * @param fo FileObject to compile
    * @param fname name of file if different from fo's name (or null)
    * @param nbDescriptor external process
    * @param err error expression
    */
    public RMIStubCompiler(CompilerJob job, FileObject fo, String fname, Object type, NbProcessDescriptor nbDescriptor, ExternalCompiler.ErrorExpression err) {
        super(job, fo, type, nbDescriptor, err);
        this.type = type;
        this.fname = fname;
    }

    /**
     */
    public Class compilerGroupClass() {
        return RMIStubCompilerGroup.class;
    }

    /** RMIC requires filename as package.name
     *
     */
    public String getFileName() {
        if (fname != null) return fname;
        else return getFileObject().getPackageName('.');
    }

    /** Checks if stub and skeleton are up to date.
     *
     */
    public boolean isUpToDate() {

        // always compile - it is necessary because user can change to
        // different stub model
        return false;
    }

    /** Clean given file object.
     * @param formatString - use this format to create stub name then delete it.
     */
    private void cleanStub(String formatString) throws IOException {
        FileObject masterfo = getFileObject();
        Object[] objs = new Object[] { masterfo.getName() };
        String stubName = MessageFormat.format(formatString, objs);
        FileObject fo = masterfo.getParent().getFileObject(stubName, RMIDataLoader.CLASS_EXTENSION);
        deleteFileObject(fo);
        fo = masterfo.getParent().getFileObject(stubName, RMIDataLoader.JAVA_EXTENSION);
        deleteFileObject(fo);
    }

    /** Delete file object.
    */
    private static void deleteFileObject(FileObject fo) throws IOException {
        FileLock lock = null;
        if (fo == null) {
            return;
        } else {
            try {
                lock = fo.lock();
                fo.delete(lock);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
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
        try {
            return getFileObject().getFileSystem().getRoot();
        } catch (FileStateInvalidException ex) {
            return new Object();
        }
    }

    /** @return <tt>true</tt> if the compilers are equal */
    public boolean equals(Object obj) {
        if (obj instanceof RMIStubCompiler) {
            RMIStubCompiler rsc = (RMIStubCompiler) obj;
            // if compiled objects are the same
            if (rsc.getFileName().equals(getFileName())) return true;
            // and compilers also the same
            if (rsc.getCompilerDescriptor().equals(getCompilerDescriptor())) return true;
        }
        return false;
    }

    public int hashCode() {
        String name = getFileName();
        return (name.hashCode()) ^ ((getCompilerDescriptor() == null) ? 0 : getCompilerDescriptor().hashCode());
    }

}

/*
 * <<Log>>
 *  7    Gandalf-post-FCS1.5.1.0     3/20/00  Martin Ryzl     localization
 *  6    Gandalf   1.5         1/24/00  Martin Ryzl     compilation of inner 
 *       classes added
 *  5    Gandalf   1.4         1/21/00  Martin Ryzl     compilation fixed (new 
 *       API)
 *  4    Gandalf   1.3         12/23/99 Jaroslav Tulach mergeInto deleted from 
 *       the CompilerJob.
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/7/99  Martin Ryzl     
 *  1    Gandalf   1.0         10/6/99  Martin Ryzl     
 * $
 */
