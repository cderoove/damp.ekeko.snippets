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
import java.io.ObjectInputStream;

import javax.swing.text.*;

import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.actions.OpenAction;
import org.openide.util.actions.SystemAction;
import org.openide.text.EditorSupport;
import org.openide.src.SourceElement;
import org.openide.compiler.Compiler;
import org.openide.compiler.ExternalCompiler;
import org.openide.execution.NbProcessDescriptor;

import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaEditor;
import org.netbeans.modules.java.JavaDataLoader;

import com.sun.jsp.compiler.ClassName;
import com.sun.jsp.compiler.Main;
import com.sun.jsp.JspException;

/** Dataobject representing a servlet generated from a JSP page
*
* @author Petr Jiricka
*/
public class JspServletDataObject extends JavaDataObject {

    public static final String EA_ORIGIN_JSP_PAGE = "NetBeansAttrOriginJspPage"; // NOI18N

    /** New instance.
    * @param pf primary file object for this data object
    */
    public JspServletDataObject(FileObject pf, MultiFileLoader loader)
    throws DataObjectExistsException {
        super(pf, loader);
        init();
    }

    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    private void init() {
        changeCookies(getSourceJspPage());
    }

    private void changeCookies(DataObject jspPage) {
        CookieSet cookies = getCookieSet();

        // remove exec+debugger cookie
        Node.Cookie es = getCookie(ExecCookie.class); // same as debugger cookie
        if (es != null)
            cookies.remove(es);

        // remove compiler
        Node.Cookie cs;
        cs = getCookie(CompilerCookie.Compile.class);
        if (cs != null) cookies.remove(cs);
        cs = getCookie(CompilerCookie.Build.class);
        if (cs != null) cookies.remove(cs);
        cs = getCookie(CompilerCookie.Clean.class);
        if (cs != null) cookies.remove(cs);

        // now add the cookies corresponding to the JSP page
        if (jspPage != null) {
            cookies.add(new JspServletExecSupport(jspPage));

            cookies.add(new JspServletCompilerSupport.Compile(jspPage));
            cookies.add(new JspServletCompilerSupport.Build(jspPage));
            cookies.add(new JspServletCompilerSupport.Clean(jspPage));
        }

    }

    /** Help context for this object.
    * @return help context
    */
    public org.openide.util.HelpCtx getHelpCtx () {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    protected JavaEditor createJavaEditor() {
        return new JspServletEditor (getPrimaryEntry ());
    }

    /** Sets the source JSP page for this servlet */
    public void setSourceJspPage(DataObject jspPage) throws IOException {
        changeCookies(jspPage);
        getPrimaryFile().setAttribute(EA_ORIGIN_JSP_PAGE, jspPage);
    }

    /** Returns the source JSP page for this servlet */
    public DataObject getSourceJspPage() {
        return (DataObject)getPrimaryFile().getAttribute(EA_ORIGIN_JSP_PAGE);
    }


    /** Searches for the class file with the name corresponding to this JspServletDataObject.
    * Then checks whether the name of the actual class in the file matches this file's name.<br>
    * @return corresponding class file, if found, otherwise null<br>
    * PENDING: Should also check package */
    /*  public FileObject getCorrespondingClass() {
        FileObject classFo = getClassFile();
        if (classFo == null)
          return null;
        // make sure that the actual class in the class file, which may be different from 
        // the class file name, matches this servlet's name. 
        try {                         
          String classFileName = JspCompileUtil.getFileObjectFileName(classFo);
          String className = ClassName.getClassName(classFileName);
          int lastDot = className.lastIndexOf('.');
          if (className.substring(lastDot + 1).equals(getPrimaryFile().getName()))
            return classFo;
          else 
            return null;
        }
        catch (FileStateInvalidException e) {
          return null;
        }  
        catch (JspException e) {
          return null;
        }  
      }*/


    /** Gets the class, does not check the actual class name */
    /*  private FileObject getClassFile() {
        String baseClassName = getPrimaryFile().getName().substring
          (0, getPrimaryFile().getName().lastIndexOf("_jsp_"));
    System.out.println("looking for class " + baseClassName);
        try {
    System.out.println("root " + getPrimaryFile().getFileSystem().toString());
          return getPrimaryFile().getFileSystem().getRoot().getFileObject(baseClassName, JavaDataLoader.CLASS_EXTENSION);
        }
        catch (IllegalArgumentException e) {
    e.printStackTrace();
          return null;
        }
        catch (FileStateInvalidException e) {
    e.printStackTrace();
          return null;
        }  
      }*/

    /*
      private String setArgument(String arguments, String argumentName, String newValue) {
    System.out.println("Old args " + arguments);
        int argIndex = arguments.indexOf(argumentName + " ");
        if (argIndex != -1) {
          int blankIndex = arguments.indexOf(" ", argIndex + (argumentName + " ").length());
          if (blankIndex == -1)
            return arguments.substring(0, argIndex + (argumentName + " ").length()) + newValue;
          else  
            return arguments.substring(0, argIndex + (argumentName + " ").length()) + newValue + 
              arguments.substring(blankIndex);
        }
        else
          return argumentName + " " + newValue + " " + arguments;
      }
      
      private static ExternalCompilerSettings getExternalCompilerSettings() {
        if (ecopts == null) {
          ecopts = (ExternalCompilerSettings) ExternalCompilerSettings.findObject(ExternalCompilerSettings.class, true);
        }
        return ecopts;
      }*/

}

/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Petr Jiricka    i18n phase 1
 *  10   Gandalf   1.9         1/6/00   Petr Jiricka    Cleanup
 *  9    Gandalf   1.8         1/3/00   Petr Jiricka    Removed methods for 
 *       acquiring generated class
 *  8    Gandalf   1.7         12/29/99 Petr Jiricka    Various compilation 
 *       fixes
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/12/99 Petr Jiricka    Removed debug messages
 *  5    Gandalf   1.4         10/10/99 Petr Jiricka    Compilation fixes
 *  4    Gandalf   1.3         10/10/99 Petr Jiricka    Changed compiler style
 *  3    Gandalf   1.2         10/4/99  Petr Jiricka    
 *  2    Gandalf   1.1         9/27/99  Petr Jiricka    
 *  1    Gandalf   1.0         9/22/99  Petr Jiricka    
 * $
 */





















