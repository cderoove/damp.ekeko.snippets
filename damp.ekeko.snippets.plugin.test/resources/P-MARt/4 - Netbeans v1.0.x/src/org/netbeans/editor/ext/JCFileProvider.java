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

package org.netbeans.editor.ext;

import java.io.Serializable;
import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Iterator;

/**
* Java completion resolver that operates over two files.
* One is skeleton file and it's read at once during 
* the build() methods. The other file is class body
* file and it's read lazily as necessary.
*
* File structures:
*   Skeleton file:
*     class skeletons:
*       String: class name
*       String: package name
*       int: body seek offset
*       int: body len
*
*   Body file:
*     class bodies:
*       int: modifiers
*       String: super class name
*       String: super class package name
*       int: field count
*       field count * field body
*         field body:
*           !!! dodelat
*               
*  
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCFileProvider extends JCompletion.AbstractProvider {

    static final int VERSION = 1;

    static final int OPCODE_ADD = 1; // adding new class

    public static final String SKEL_FILE_EXT = ".jcs"; // NOI18N
    public static final String BODY_FILE_EXT = ".jcb"; // NOI18N

    /** Global cache saving the string creations */
    private static final StringCache strCache = new StringCache(200, 5003);

    static {
        // pre-cache standard strings
        strCache.putSurviveString(""); // NOI18N
        Iterator i = JCompletion.getPrimitiveClassIterator();
        while (i.hasNext()) {
            strCache.putSurviveString(((JCClass)i.next()).getName());
        }
    }


    FileStorage skels;

    FileStorage bodies;

    HashMap classes;

    int fileVersion;

    public JCFileProvider(String fileNamePrefix) {
        this(fileNamePrefix + SKEL_FILE_EXT,
             fileNamePrefix + BODY_FILE_EXT);
    }

    public JCFileProvider(String fileNameSkels, String fileNameBodies) {
        skels = new FileStorage(fileNameSkels, strCache);
        bodies = new FileStorage(fileNameBodies, strCache);
    }

    public void reset() {
        try {
            skels.resetFile();
            bodies.resetFile();
            // write version
            skels.open(true);
            skels.putInteger(VERSION);
            skels.close();
        } catch (IOException e) {
            throw new Error("JCFileProvider: IOException thrown during Java Completion file provider reset.\n" + e); // NOI18N
        }
    }

    protected boolean appendClass(JCClass c) {
        try {
            skels.putInteger(OPCODE_ADD);
            writeClass(c);
            skels.write();
            bodies.write();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean append(JCClassProvider cp) {
        try {

            if (skels.getFileLength() <= 0) { // reset if necessary
                reset();
            }

            skels.open(true);
            bodies.open(true);

            if (!super.append(cp)) {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        } finally {
            boolean ok = true;
            try {
                skels.close();
            } catch (IOException e) {
                e.printStackTrace();
                ok = false;
            }

            try {
                bodies.close();
            } catch (IOException e) {
                e.printStackTrace();
                ok = false;
            }

            return ok;
        }
    }

    public Iterator getClasses() {
        int skelsFileLen;
        try {
            skels.open(false);
            skels.seek(0);
            skelsFileLen = skels.getFileLength();
            if (skelsFileLen < 4) { // file exists but was not reset
                reset();
                return new ArrayList().iterator(); // return empty iterator
            }
            skels.read(skelsFileLen);
        } catch (IOException e) {
            throw new Error("JCFileProvider: Java Completion cannot read class skeletons\n" + e); // NOI18N
        } finally {
            try {
                skels.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileVersion = skels.getInteger();
        ArrayList clsList = new ArrayList();

        while (skels.getOffset() < skelsFileLen) { // till the last class
            int opcode = skels.getInteger();
            if (opcode == OPCODE_ADD) {
                Cls cls = new Cls();
                clsList.add(cls);
            } else {
                break; // Unsupported operation code
            }
        }

        skels.resetBytes(); // GC possibly large skels bytes array
        return clsList.iterator();
    }

    void writeClass(JCClass c) throws IOException {
        // write body
        bodies.putInteger(c.getTagOffset());
        writeClassName(c.getSuperclass(), bodies);

        // Write implemented interfaces
        JCClass[] interfaces = c.getInterfaces();
        bodies.putInteger(interfaces.length);
        for (int i = 0; i < interfaces.length; i++) {
            writeClassName(interfaces[i], bodies);
        }

        // Write declared fields
        JCField[] fields = c.getFields();
        bodies.putInteger(fields.length);
        for (int i = 0; i < fields.length; i++) {
            writeField(fields[i]);
        }

        // Write constructors
        JCConstructor[] constructors = c.getConstructors();
        bodies.putInteger(constructors.length);
        for (int i = 0; i < constructors.length; i++) {
            writeConstructor(constructors[i]);
        }

        // Write methods
        JCMethod[] methods = c.getMethods();
        bodies.putInteger(methods.length);
        for (int i = 0; i < methods.length; i++) {
            writeMethod(methods[i]);
        }

        // write skeleton
        writeClassName(c, skels);
        int modifiers = c.getModifiers();
        if (c.isInterface()) {
            modifiers |= JCompletion.INTERFACE_BIT;
        }
        skels.putInteger(modifiers);

        skels.putInteger(bodies.getFilePointer());
        skels.putInteger(bodies.getOffset());
    }

    void writeType(JCType t) {
        writeClassName(t.getClazz(), bodies);
        bodies.putInteger(t.getArrayDepth());
    }

    void writeParameter(JCParameter p) {
        bodies.putString(p.getName());
        writeType(p.getType());
    }

    void writeField(JCField f) {
        bodies.putString(f.getName());
        writeType(f.getType());
        bodies.putInteger(f.getTagOffset());
        bodies.putInteger(f.getModifiers());
    }

    void writeConstructor(JCConstructor c) {
        bodies.putInteger(c.getTagOffset());
        bodies.putInteger(c.getModifiers());

        JCParameter[] parameters = c.getParameters();
        bodies.putInteger(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            writeParameter(parameters[i]);
        }

        JCClass[] exceptions = c.getExceptions();
        bodies.putInteger(exceptions.length);
        for (int i = 0; i < exceptions.length; i++) {
            writeClassName(exceptions[i], bodies);
        }
    }

    void writeMethod(JCMethod m) {
        writeConstructor(m);
        bodies.putString(m.getName());
        writeType(m.getReturnType());
    }

    /** Write name and package of the given class */
    void writeClassName(JCClass c, FileStorage fs) {
        fs.putString(c.getFullName());
        fs.putInteger(c.getPackageName().length());
    }


    private JCClass getSimpleClass(String fullName, int packageNameLen) {
        JCClass c = null;
        if (packageNameLen == 0) {
            c = JCompletion.getPrimitiveClass(fullName);
        }
        if (c == null) {
            String fullNameIntern = fullName.intern();
            if (fullName != fullNameIntern) { // update cache with interned string
                strCache.putSurviveString(fullNameIntern);
            }
            c = JCompletion.getSimpleClass(fullNameIntern, packageNameLen);
        }
        return c;
    }

    JCClass readSimpleClass(FileStorage fs) {
        String fullName = fs.getString();
        int packageNameLen = fs.getInteger();
        return getSimpleClass(fullName, packageNameLen);
    }

    final class Cls extends JCompletion.AbstractClass {

        /** Seek position in the file of the class body */
        int bodySeekPointer;

        /** Length of the class body in the file */
        int bodyLen;

        public Cls() {
            JCClass c = readSimpleClass(skels);
            fullName = c.getFullName();
            name = c.getName();
            packageName = c.getPackageName();
            modifiers = skels.getInteger();
            bodySeekPointer = skels.getInteger();
            bodyLen = skels.getInteger();
        }

        /** Init internal representation */
        protected void init() {
            body = new Body();

            // set the right seek position and read
            try {
                bodies.open(false);
                bodies.seek(bodySeekPointer);
                bodies.read(bodyLen);
                bodies.close();
            } catch (IOException e) {
                throw new Error("JCFileProvider: Java Completion cannot read class body of class " + this); // NOI18N
            }

            body.tagOffset = bodies.getInteger();
            body.superClass = readSimpleClass(bodies);

            int cnt = bodies.getInteger();
            body.interfaces = (cnt > 0) ? new JCClass[cnt]
                              : JCompletion.EMPTY_CLASSES;
            for (int i = 0; i < cnt; i++) {
                body.interfaces[i] = readSimpleClass(bodies);
            }

            cnt = bodies.getInteger();
            body.fields = (cnt > 0) ? new JCField[cnt]
                          : JCompletion.EMPTY_FIELDS;
            for (int i = 0; i < cnt; i++) {
                body.fields[i] = new Fld(this);
            }

            cnt = bodies.getInteger();
            body.constructors = (cnt > 0) ? new JCConstructor[cnt]
                                : JCompletion.EMPTY_CONSTRUCTORS;
            for (int i = 0; i < cnt; i++) {
                body.constructors[i] = new Ctr(this);
            }

            cnt = bodies.getInteger();
            body.methods = (cnt > 0) ? new JCMethod[cnt]
                           : JCompletion.EMPTY_METHODS;
            for (int i = 0; i < cnt; i++) {
                body.methods[i] = new Mtd(this);
            }

            try {
                bodies.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    final class Typ extends JCompletion.BaseType {

        Typ() {
            clazz = readSimpleClass(bodies);
            arrayDepth = bodies.getInteger();
        }

    }

    /** Description of the declared field */
    final class Fld extends JCompletion.BaseField {

        Fld(JCClass clazz) {
            this.clazz = clazz;
            name = bodies.getString();
            type = new Typ();
            tagOffset = bodies.getInteger();
            modifiers = bodies.getInteger();
        }

    }

    private void readBC(JCompletion.BaseConstructor bc) {
        bc.tagOffset = bodies.getInteger();
        bc.modifiers = bodies.getInteger();

        int cnt = bodies.getInteger();
        bc.parameters = (cnt > 0) ? new JCParameter[cnt]
                        : JCompletion.EMPTY_PARAMETERS;
        for (int i = 0; i < cnt; i++) {
            bc.parameters[i] = new Prm();
        }

        cnt = bodies.getInteger();
        bc.exceptions = (cnt > 0) ? new JCClass[cnt]
                        : JCompletion.EMPTY_CLASSES;
        for (int i = 0; i < cnt; i++) {
            bc.exceptions[i] = readSimpleClass(bodies);
        }
    }

    /** Read constructor */
    final class Ctr extends JCompletion.BaseConstructor {

        Ctr(JCClass clazz) {
            this.clazz = clazz;
            readBC(this);
        }

    }

    /** Read method */
    final class Mtd extends JCompletion.BaseMethod {

        Mtd(JCClass clazz) {
            this.clazz = clazz;
            readBC(this);

            name = bodies.getString();
            returnType = new Typ();

        }

    }

    /** Description of the method parameter */
    public class Prm extends JCompletion.BaseParameter {

        Prm() {
            name = bodies.getString();
            type = new Typ();
        }

    }

    public String toString() {
        return "strCache=" + strCache; // NOI18N
    }

}

/*
 * Log
 *  10   Gandalf   1.9         1/13/00  Miloslav Metelka Localization
 *  9    Gandalf   1.8         11/8/99  Miloslav Metelka 
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/10/99 Miloslav Metelka 
 *  6    Gandalf   1.5         9/15/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/18/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/30/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/22/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

