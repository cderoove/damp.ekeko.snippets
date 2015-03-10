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

import org.netbeans.editor.ext.JCompletion;
import org.netbeans.editor.ext.JCClassProvider;

/**
* JC Element for one class provider
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCStorageElement implements java.io.Serializable {

    /** Prefix name of the java completion provider file */
    String name;

    /** System name of file system */
    String fileSystemName;

    /** Storage level for classes */
    private int classLevel;

    /** Storage level for fields */
    private int fieldLevel;

    /** Storage level for methods */
    private int methodLevel;

    /** Provider */
    private transient JCClassProvider provider;

    static final long serialVersionUID =-8525995534921942711L;
    public JCStorageElement() {
    }

    public JCStorageElement(String name) {
        this.name = name;
    }

    public JCStorageElement(String name, String fileSystemName,
                            int classLevel, int fieldLevel, int methodLevel) {
        this.name = name;
        this.fileSystemName = fileSystemName;
        this.classLevel = classLevel;
        this.fieldLevel = fieldLevel;
        this.methodLevel = methodLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileSystemName() {
        return fileSystemName;
    }

    public void setFileSystemName(String fileSystemName) {
        this.fileSystemName = fileSystemName;
    }

    public int getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(int classLevel) {
        this.classLevel = classLevel;
    }

    public int getFieldLevel() {
        return fieldLevel;
    }

    public void setFieldLevel(int fieldLevel) {
        this.fieldLevel = fieldLevel;
    }

    public int getMethodLevel() {
        return methodLevel;
    }

    public void setMethodLevel(int methodLevel) {
        this.methodLevel = methodLevel;
    }

    public JCClassProvider getProvider() {
        return provider;
    }

    public void setProvider(JCClassProvider provider) {
        this.provider = provider;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof JCStorageElement) {
            return name.equals(((JCStorageElement)o).name);
        }

        if (o instanceof String) {
            return name.equals((String)o);
        }

        return false;
    }

    public String toString() {
        return "name='" + name + "', fileSystemName='" + fileSystemName // NOI18N
               + "', classLevel=" + classLevel + ", fieldLevel=" + fieldLevel // NOI18N
               + ", methodLevel=" + methodLevel + ", provider=" + provider; // NOI18N
    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Miloslav Metelka Localization
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/15/99  Miloslav Metelka 
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

