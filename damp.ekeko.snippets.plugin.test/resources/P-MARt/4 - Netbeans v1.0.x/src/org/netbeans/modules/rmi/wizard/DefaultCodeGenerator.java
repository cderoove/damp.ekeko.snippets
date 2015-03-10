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

package org.netbeans.modules.rmi.wizard;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.src.*;
import org.openide.execution.*;

/**
 *
 * @author  mryzl
 */

public class DefaultCodeGenerator implements Generator {

    FileObject targetObject, implObject;
    RMIWizardData data;

    // General settings.
    String generalComment = "/** Generated using the Netbeans RMI module.\n*/\n\n";

    // Interface settings.
    String interfaceComment = "Remote interface.\n";

    // Implementation settings.
    String implComment = "Remote implementation.\n";

    // Startup settings.
    String startupComment = "Remote server.\n.";

    // Imports.
    String rmiImports = "import java.rmi.*;\n";
    String serverImports = "import java.rmi.server.*;\n";
    String activationImports = "import java.rmi.activation.*;\n";

    /** Creates new DefaultCodeGenerator. */
    public DefaultCodeGenerator() {
    }

    /** Data for generator. */
    public void setData(Object settings) {
        if (settings instanceof RMIWizardData) {
            data = (RMIWizardData) settings;
        }
    }

    /** Perform generation. */
    public void generate() throws SourceException, IOException {
        if (data == null) throw new IOException("No data.");

        targetObject = data.getTargetFolder().getPrimaryFile();

        FileSystem fs;
        try {
            fs = targetObject.getFileSystem();
        } catch (FileStateInvalidException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
        fs.runAtomicAction(new org.openide.filesystems.FileSystem.AtomicAction() {
                               public void run() throws IOException {
                                   try {
                                       generateInterface();
                                       generateImpl();
                                       //          generateStartup();
                                       //          generatePolicy();
                                   }
                                   catch (SourceException e) {
                                       e.printStackTrace();
                                       throw new IOException(e.getMessage());
                                   }
                               }
                           });

        // set executor
        if (implObject != null) {
            DataObject dob = DataObject.find(implObject);
            if (dob instanceof MultiDataObject) {
                ExecSupport.setExecutor(((MultiDataObject)dob).getPrimaryEntry(), data.getExecutor());
            }
        }
    }

    /**
    */
    public String getPackageString() {
        String pkg = targetObject.getPackageName('.');
        if (pkg.length() > 0) return "package " + pkg + ";\n\n";
        return "";
    }

    /** Create interface.
    */
    protected void generateInterface() throws SourceException, IOException {
        // simply prepare source text and use createFile
        StringBuffer sb = new StringBuffer(8192);
        sb.append(getGeneralComment());
        sb.append(getPackageString());
        sb.append(getRmiImports());
        sb.append('\n');
        sb.append(getInterface(null));
        createFile(data.interfaceName, "java", sb.toString());
    }

    /** Create implementation.
    */
    protected void generateImpl() throws SourceException, IOException {
        // simply prepare source text and use createFile
        StringBuffer sb = new StringBuffer(8192);
        sb.append(getGeneralComment());
        sb.append(getPackageString());
        sb.append(getRmiImports());
        sb.append(getServerImports());
        sb.append('\n');
        if (data.getType() == RMIWizardData.TYPE_ACTIVATABLE) sb.append(getActivationImports());
        sb.append(getImpl(null));
        createFile(data.implName, "rmi", sb.toString());
        implObject = createFile(data.implName, "java", sb.toString());
    }

    /** Creates interface class.
    * @param ce - class element. If null, it will be created.
    * @return class element
    */
    public ClassElement getInterface(ClassElement ce) throws SourceException {
        if (ce == null) ce = new ClassElement();
        MethodElement[] methods = (MethodElement[]) data.getMethods().clone();

        ce.getJavaDoc().setRawText(getInterfaceComment());
        ce.setName(Identifier.create(data.interfaceName));
        ce.setClassOrInterface(ClassElement.INTERFACE);
        ce.setModifiers(Modifier.PUBLIC);
        ce.setInterfaces(new Identifier[] { Identifier.create("java.rmi.Remote") });
        prepareInterfaceMethods(methods);
        ce.setMethods(methods);
        return ce;
    }

    /** Creates implementation class.
    * @param ce - class element. If null, it will be created.
    * @return class element
    */
    public ClassElement getImpl(ClassElement ce) throws SourceException {
        if (ce == null) ce = new ClassElement();
        MethodElement[] methods = (MethodElement[]) data.getMethods().clone();
        ce.getJavaDoc().setRawText(getImplComment());
        ce.setName(Identifier.create(data.implName));
        ce.setClassOrInterface(ClassElement.CLASS);
        ce.setModifiers(Modifier.PUBLIC);
        Identifier superclass = getImplSuperclass();
        ce.setSuperclass(superclass);
        ce.setInterfaces(new Identifier[] { Identifier.create(data.interfaceName) });
        prepareImplMethods(methods);
        ce.setMethods(methods);
        ce.setConstructors(getImplConstructors());
        return ce;
    }

    /** Adjust methods properties to be suitable for interfaces. They can only
    * have public or abstract modifiers.
    * @param methods - methods to be processed
    * @return prepared methods
    */
    protected MethodElement[] prepareInterfaceMethods(MethodElement[] methods) throws SourceException {
        for(int i = 0; i < methods.length; i++) {
            methods[i].setModifiers(Modifier.PUBLIC);
            methods[i].setBody(null);
        }
        return methods;
    }

    /** Adjust methods properties to be suitable for implementation. They should
    * have public modifier.
    * @param methods - methods to be processed
    * @return prepared methods
    */
    protected MethodElement[] prepareImplMethods(MethodElement[] methods) throws SourceException {
        for(int i = 0; i < methods.length; i++) {
            setImplMethodModifiers(methods[i]);
            setImplMethodBody(methods[i]);
            setImplMethodComment(methods[i]);
        }
        return methods;
    }

    /** Set modifiers for implementation methods. For example, they
    * should be public and of course not abstract.
    * @param me - method
    * @return properly set method
    */
    protected MethodElement setImplMethodModifiers(MethodElement me) throws SourceException {
        int mod = me.getModifiers();
        mod |= Modifier.PUBLIC;
        mod &= ~Modifier.PROTECTED;
        mod &= ~Modifier.PRIVATE;
        mod &= ~Modifier.ABSTRACT;
        me.setModifiers(Modifier.PUBLIC);
        return me;
    }

    /** Set body for implementation methods.
    * @param me - method
    * @return properly set method
    */
    protected MethodElement setImplMethodBody(MethodElement me) throws SourceException {
        me.setBody("\n");
        return me;
    }

    /** Set comment for implementation methods.
    * @param me - method
    * @return properly set method
    */
    protected MethodElement setImplMethodComment(MethodElement me) throws SourceException {
        SrcSupport.commentMethod(me, null);
        return me;
    }


    /** Creates constructors for implementation.
    *
    * @return constructors
    */
    protected ConstructorElement[] getImplConstructors() throws SourceException {
        ConstructorElement[] ces = null;
        switch (data.getType()) {
        case RMIWizardData.TYPE_UNICAST_REMOTE_OBJECT:
            ces = new ConstructorElement[] {
                      SrcSupport.getRMIConstructorElement(data.implName, "", "", true),
                      SrcSupport.getRMIConstructorElement(data.implName, "int port", "port", true),
                      SrcSupport.getRMIConstructorElement(data.implName, "int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf", "port, csf, ssf", true),
                  };
            break;
        case RMIWizardData.TYPE_ACTIVATABLE:
            ces = new ConstructorElement[] {
                      SrcSupport.getRMIConstructorElement(data.implName, "ActivationID id, MarshalledObject obj", "id, 0", true),
                  };
            break;
        default:
            ces = new ConstructorElement[] {
                      SrcSupport.getRMIConstructorElement(data.implName, "", "", false),
                  };
            break;
        }
        // comment it
        for(int i = 0; i < ces.length; i++) {
            SrcSupport.commentMethod(ces[i], "Creates new instance.\n");
        }
        return ces;
    }

    /** Gets superclass for implementation.
    *
    * @return superclass or null 
    */
    protected Identifier getImplSuperclass() throws SourceException {
        switch (data.getType()) {
        case RMIWizardData.TYPE_UNICAST_REMOTE_OBJECT:
            return Identifier.create("java.rmi.server.UnicastRemoteObject");
        case RMIWizardData.TYPE_ACTIVATABLE:
            return Identifier.create("java.rmi.activation.Activatable");
        default:
            return null;
        }
    }

    /** Getter for general comment.
    * @return comment
    */
    public String getGeneralComment() {
        return generalComment;
    }

    /** Setter for interface comment.
    * @param comment - comment
    */
    public void setGeneralComment(String comment) {
        this.generalComment = comment;
    }

    /** Getter for interface comment.
    * @return comment
    */
    public String getInterfaceComment() {
        return interfaceComment;
    }

    /** Setter for interface comment.
    * @param comment - comment
    */
    public void setImplComment(String comment) {
        this.implComment = comment;
    }

    /** Getter for interface comment.
    * @return comment
    */
    public String getImplComment() {
        return implComment;
    }

    /** Setter for interface comment.
    * @param comment - comment
    */
    public void setStartupComment(String comment) {
        this.startupComment = comment;
    }

    /** Getter for interface comment.
    * @return comment
    */
    public String getStartupComment() {
        return startupComment;
    }

    /** Setter for interface comment.
    * @param comment - comment
    */
    public void setInterfaceComment(String comment) {
        this.interfaceComment = comment;
    }

    /** General RMI imports getter.
    * @return imports
    */
    public String getRmiImports() {
        return rmiImports;
    }

    /** General RMI imports setter.
    * @param imports imports
    */
    public void setRmiImports(String imports) {
        this.rmiImports = rmiImports;
    }

    /** Server RMI imports getter.
    * @return imports
    */
    public String getServerImports() {
        return serverImports;
    }

    /** Activatable RMI imports getter.
    * @return imports
    */
    public String getActivationImports() {
        return activationImports;
    }


    /** Create file with name and ext and write text into.
    * @param name - name of the file
    * @param ext - extension
    * @text - data to be written to the file
    * @exception IOException if io problem occurs
    */
    protected FileObject createFile(String name, String ext, String text) throws IOException {
        FileObject fo = createFile(name, ext);
        FileLock lock = null;
        PrintStream ps = null;
        try {
            ps = new PrintStream(fo.getOutputStream(lock = fo.lock()));
            ps.println(text);
        } finally {
            if (ps != null) ps.close ();
            if (lock != null) lock.releaseLock ();
        }
        return fo;
    }

    /** Create FileObject with given name and ext.
    * @param name - name of the FileObject
    * @param ext - extension
    * @return file object
    * @exception IOException if io problem occurs
    */
    protected FileObject createFile(String name, String ext) throws IOException {
        return targetObject.createData(name, ext);
    }
}

/*
* <<Log>>
*  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         8/18/99  Martin Ryzl     
*  5    Gandalf   1.4         7/29/99  Martin Ryzl     executor selection is 
*       working
*  4    Gandalf   1.3         7/28/99  Martin Ryzl     added selection of 
*       executor
*  3    Gandalf   1.2         7/27/99  Martin Ryzl     new version of generator 
*       is working
*  2    Gandalf   1.1         7/27/99  Martin Ryzl     compilation corrected
*  1    Gandalf   1.0         7/27/99  Martin Ryzl     
* $ 
*/ 
