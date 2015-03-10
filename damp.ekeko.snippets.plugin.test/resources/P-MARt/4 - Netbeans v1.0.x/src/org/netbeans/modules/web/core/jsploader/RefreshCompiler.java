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
import java.io.File;

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.ErrorEvent;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

/** Compiler which refreshes a directory after a file has (possibly) been created on the disk.
*
* @author Petr Jiricka
*/
public class RefreshCompiler extends Compiler {

  protected final FileObject refreshFolder;

  public RefreshCompiler(FileObject refreshFolder) {
    super();
    this.refreshFolder = refreshFolder;
  }

  public FileObject getRefreshFolder() {
    return refreshFolder;
  }

  /**
   */
  public Class compilerGroupClass() {
    return Group.class;
  }

  /** Checks if the class corresponding to this JSP is up to date
   */
  public boolean isUpToDate() {
    return false;
  }

  /** See {@link Compilable#equals(java.lang.Object)} 
  */
  public boolean equals (Object other) {
    if (!(other instanceof RefreshCompiler))
      return false;
    RefreshCompiler comp2 = (RefreshCompiler)other;
    return (comp2.refreshFolder == refreshFolder);
  }
  
  public int hashCode() {
    return refreshFolder.hashCode();
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
    return refreshFolder; 
  }
  
  public String toString() {
    String fs = "?"; // NOI18N
    try {
      fs = refreshFolder.getFileSystem().toString();
    }
    catch (Exception e) {}
    return "RefreshCompiler for " + refreshFolder.getPackageNameExt('/','.') + " on " + fs; // NOI18N
  }
  
  /** Compiler group for servlet code generation. */
  public static class Group extends CompilerGroup {
    
    public Group() {
      super();
    }
    
    private FileObject refreshFolder;
    
    public void add(Compiler c) throws IllegalArgumentException {
      if (!(c instanceof RefreshCompiler))
        throw new IllegalArgumentException();
      refreshFolder = ((RefreshCompiler)c).getRefreshFolder();
    }
    
    public boolean start() {
      if (refreshFolder != null)
        refreshFolder.refresh();
      return true;
    }
    
  } // end of inner class Group
  
}

/*
 * Log
 *  4    Gandalf   1.3         1/15/00  Petr Jiricka    Ensuring correct 
 *       compiler implementation - hashCode and equals
 *  3    Gandalf   1.2         1/12/00  Petr Jiricka    Fully I18n-ed
 *  2    Gandalf   1.1         1/12/00  Petr Jiricka    i18n phase 1
 *  1    Gandalf   1.0         1/10/00  Petr Jiricka    
 * $
 */
