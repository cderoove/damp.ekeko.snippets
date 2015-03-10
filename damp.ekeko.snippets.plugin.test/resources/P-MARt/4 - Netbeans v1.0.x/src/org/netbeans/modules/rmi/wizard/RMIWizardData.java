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
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.src.*;
import org.openide.execution.*;

/**
 *
 * @author  mryzl
 */

public class RMIWizardData extends Object {

    // action types
    /** Create new RMI Object */
    public static final int WIZARD = 0;

    /** Encapsulates given class, or public class of file. */
    public static final int ENCAPSULATION = 1;

    /** Migrates public class of file. */
    public static final int MIGRATION = 2;

    public int wizardType = WIZARD;

    // source of the migration
    public SourceElement source;
    public String sourceName;

    // types of the remote object
    public static final int TYPE_UNICAST_REMOTE_OBJECT = 0;
    public static final int TYPE_ACTIVATABLE = 1;
    public static final int TYPE_OTHER = 2;

    public int type = TYPE_UNICAST_REMOTE_OBJECT;
    public boolean lockType = false;

    // -- Names --
    public static final String NAME_IMPL = "Impl";
    public static final String NAME_STUP = "Startup";
    public static final String NOPACKAGE = "<no package>";

    public DataFolder targetFolder;
    public String name;
    public String interfaceName;
    public String implName;
    public String stupName;
    public String policyName;
    public boolean usePolicy = false;

    // -- Methods --
    public static final ElementFormat METHOD_HEADER_FORMAT = new ElementFormat("{m,,\" \"}{r} {n}({p,,,\", \"}){e,\" throws \",}");
    public MethodElement[] methods = {};

    // -- Executor --
    public Executor executor;

    /** Generator. */
    Generator generator;

    /** Creates new RMIWizardData. */
    public RMIWizardData(Generator generator) {
        this.generator = generator;
        generator.setData(this);
    }

    /** Creates new RMIWizardData. */
    public RMIWizardData() {
        this(new DefaultCodeGenerator());
    }

    /**
    */
    public String toString() {
        return "RMIWizardData[type=" + type +
               ",interfaceName=" + interfaceName +
               ",implName=" + implName +
               ",stupName=" + stupName +
               ",policyName=" + policyName +
               ",usePolicy=" + usePolicy +
               "]";
    }

    public Generator getGenerator() {
        return generator;
    }

    // -- Properties --

    /** Setter for type
    * @param type - type of the remote object
    */
    public void setType(int type) {
        this.type = type;
    }

    /** Getter for the type.
    * @return type
    */
    public int getType() {
        return type;
    }

    /** Setter for targetFolder.
    * @param targetFolder - target folder for files
    */
    public void setTargetFolder(DataFolder targetFolder) {
        this.targetFolder = targetFolder;
    }

    /** Getter for targetFolder.
    * @return target folder
    */
    public DataFolder getTargetFolder() {
        return targetFolder;
    }

    /**  Getter for executor.
    * @return executor
    */
    public Executor getExecutor() {
        return executor;
    }

    /**  Setter for executor.
    * @param executor executor
    */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /** Setter for methods.
    * @param methods - methods to be created
    */
    public void setMethods(MethodElement[] methods) {
        this.methods = methods;
    }

    /** Getter for methods.
    * @return methods
    */
    public MethodElement[] getMethods() {
        return methods;
    }

    // -- END of properties --
}

/*
* <<Log>>
*  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  8    Gandalf   1.7         7/29/99  Martin Ryzl     executor selection is 
*       working
*  7    Gandalf   1.6         7/28/99  Martin Ryzl     
*  6    Gandalf   1.5         7/27/99  Martin Ryzl     new version of generator 
*       is working
*  5    Gandalf   1.4         7/27/99  Martin Ryzl     
*  4    Gandalf   1.3         7/22/99  Martin Ryzl     first working version
*  3    Gandalf   1.2         7/20/99  Martin Ryzl     
*  2    Gandalf   1.1         7/20/99  Martin Ryzl     
*  1    Gandalf   1.0         7/19/99  Martin Ryzl     
* $ 
*/ 
