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

package org.netbeans.modules.java;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import sun.tools.java.*;
import sun.tools.java.Identifier;
import sun.tools.java.Package;
import sun.tools.javac.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.src.SourceElement;
import org.openide.util.io.NullOutputStream;

/** Supplies our compiler environment.
* @author Ales Novak
* @version 0.21 June 19, 1998
*/
public class CoronaEnvironment extends BatchEnvironment {

    /** may be null */
    private ErrConsumer errConsumer;
    /** list of errors */
    ErrorMessage errors;
    /** a variable indicating not to throw CompilerException if there were syntax errors for example */
    boolean thereWereErrors;
    /** file with errors */
    String errorFileName;
    /** vector of deprecated files 
     * @associates Object*/
    Vector deprecationFiles;
    /** our srcClassPath refernce */
    ClassPath srcPath;
    /** our binClassPath reference */
    ClassPath binPath;
    /** hashtable with all registered files (FileObject, CoronaClassFile) 
     * @associates CoronaClassFile*/
    private Hashtable list = new Hashtable ();

    /** err offsets */
    private static int OFFSET_BITS = sun.tools.java.Constants.WHEREOFFSETBITS;
    private static long OFFSET_BITS_MASK = sun.tools.java.Constants.MAXFILESIZE;

    /** Creates new Environment.
    * @param srcClassPath - java files are searched here
    * @param binClassPath - class files are searched here
    * @param consumer error consumer
    */
    public CoronaEnvironment(CoronaClassPath srcClassPath, CoronaClassPath binClassPath, ErrConsumer consumer) {
        super(new NullOutputStream (), srcClassPath, binClassPath);
        srcClassPath.attachToEnvironment (this);
        binClassPath.attachToEnvironment (this);
        errConsumer = consumer;
        deprecationFiles = new Vector();
        srcPath = srcClassPath;
        binPath = binClassPath;
    }

    /**
    * @return a String with classpath
    */
    private static String getPath() {
        char c = java.io.File.separatorChar;
        return System.getProperty("java.class.path"); // + c + ".." + c + "lib" + c + "tools.jar";
    }

    /** Obtains a CoronaClassFile for a file object. Keeps the list of
    * all created objects.
    *
    * @param fo file object
    * @return the file
    */
    public /* synchronized */ CoronaClassFile getClassFile (FileObject fo) {
        CoronaClassFile cf = (CoronaClassFile)list.get (fo);
        if (cf == null) {
            cf =  new CoronaClassFile (fo);
            list.put (fo, cf);
        }
        return cf;
    }

    /** Test if the file has been proceeded.
    * @param fo file object to test
    * @return true if the has been proceeded.
    */
    public boolean proceeded (FileObject fo) {
        return list.get (fo) != null;
    }


    /** sets the error consumer */
    public void setConsumer (ErrConsumer err) {
        errConsumer = err;
    }

    /** inserts error to the list of errors at position i */
    public boolean insertError(long i, String string) {

        if (errConsumer == null) {
            return false;
        }

        if (errors == null || errors.where > i) {
            ErrorMessage errorMessage1 = new ErrorMessage(i, string);
            errorMessage1.next = errors;
            errors = errorMessage1;
        } else {
            if (errors.where == i && errors.message.equals(string))
                return false;
            ErrorMessage msg1, msg2;
            for (msg1 = errors; (msg2 = msg1.next) != null && msg2.where < i; msg1 = msg2);
            ErrorMessage msg3;
            while ((msg3 = msg1.next) != null && msg3.where == i) {
                if (msg3.message.equals(string))
                    return false;
                msg1 = msg3;
            }
            ErrorMessage msg4 = new ErrorMessage(i, string);
            msg4.next = msg1.next;
            msg1.next = msg4;
        }
        return true;
    }

    /** reports error */
    public void reportError(Object object, long i, String string1, String string2)
    {
        if (object == null)
        {
            if (errorFileName != null)
            {
                flushErrors();
                errorFileName = null;
            }
            if (string1.startsWith("warn.")) // NOI18N
            {
                if (warnings())
                {
                    nwarnings++;
                    output(string2);
                }
                return;
            }
            output(new StringBuffer("error: ").append(string2).toString()); // NOI18N
            nerrors++;
            flags |= 65536;
            return;
        } else
            if (object instanceof String)
            {
                String string3 = (String)object;
                if (!string3.equals(errorFileName))
                {
                    flushErrors();
                    errorFileName = string3;
                }
                if (string1.startsWith("warn.")) // NOI18N
                {
                    if (string1.indexOf("is.deprecated") >= 0) // NOI18N
                    {
                        ndeprecations++;
                        if (!deprecationFiles.contains(object))
                            deprecationFiles.addElement(object);
                        if (!deprecation())
                            return;
                    }
                    nwarnings++;
                    if (!warnings())
                        return;
                }
                else
                {
                    nerrors++;
                    flags |= 65536;
                }
                insertError(i, string2);
                return;
            }
        if (object instanceof ClassFile)
        {
            reportError(((ClassFile)object).getPath(), i, string1, string2);
            return;
        }
        if (object instanceof Identifier)
        {
            reportError(object.toString(), i, string1, string2);
            return;
        }
        if (object instanceof ClassDeclaration)
        {
            try
            {
                reportError(((ClassDeclaration)object).getClassDefinition(this), i, string1, string2);
            }
            catch (ClassNotFound classNotFound)
            {
                reportError(((ClassDeclaration)object).getName(), i, string1, string2);
                return;
            }
            return;
        }
        if (object instanceof ClassDefinition)
        {
            ClassDefinition classDefinition = (ClassDefinition)object;
            if (!string1.startsWith("warn.")) // NOI18N
                classDefinition.setError();
            reportError(classDefinition.getSource(), i, string1, string2);
            return;
        }
        if (object instanceof MemberDefinition)
        {
            ClassDeclaration cdecl;
            cdecl = ((MemberDefinition) object).getClassDeclaration();
            reportError(cdecl, i, string1, string2);
            return;
        }
        output(":error=" + string1 + ":" + string2); // NOI18N
    }



    /** Method thanks to we have to create our own class
    */
    public void flushErrors() {

        if (errors == null) {
            return;
        }

        if (errConsumer == null) {
            errors = null;
            return;
        }

        CoronaClassFile cfile = null;

        //read the file
        cfile = (CoronaClassFile) srcPath.getFile(errorFileName + ".java"); // NOI18N
        char[] data;
        if (cfile.pis != null) {
            data = cfile.pis.getString(0).toCharArray();
        } else {
            try {
                InputStream input = cfile.getInputStream();
                try {
                    data = readDataFromIs(input);
                } finally {
                    input.close();
                }
            } catch (IOException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
                errors = null;
                return;
            }
        }
        int ilength = data.length;

        //report the errors
        for (ErrorMessage msg = errors; msg != null; msg = msg.next)  {
            ErrorMessage tmpmsg;

            while ((tmpmsg = msg.next) != null && msg.where == tmpmsg.where && msg.message.equals(tmpmsg.message)) {
                msg = msg.next;
                if (nwarnings > 1) nwarnings--;
                else  nerrors--;
            }
            long ln = msg.where >>> OFFSET_BITS;
            int off = (int) ((long) msg.where & OFFSET_BITS_MASK);

            int i;
            int j;
            if (off > ilength) {
                off = ilength;
            }

            for (i = off - 1; i > 0 && data[i - 1] != '\n' && data[i - 1] != '\r'; i--) /* null body */ ;
            for (j = off; j < ilength && data[j] != '\n' && data[j] != '\r'; j++) /* null body */ ;

            if (i < 0) i = 0; // if off is 0, the i would stay on -1. FIxed bug #3756 - java.lang.StringIndexOutOfBoundsException during compilation

            String ref = new String(data, i, j - i);
            errConsumer.pushError(cfile.getFile(), (int) ln, off - 1 - i, msg.message, ref);
            // else - instanceof ClasFile - err can be only in java - java are "only" in // NOI18N
            // CoronaClassPath - always - CoronaClassFile
        }
        errors = null;
    }

    private char[] readDataFromIs(InputStream in) throws java.io.IOException {
        char[] data = new char[1024];
        int off = 0;

        InputStreamReader ir = new InputStreamReader(in);
        int ilength;
        for (;;) {
            ilength = ir.read(data, off, data.length - off);
            if (ilength < 0) {
                char[] ret = new char[off];
                System.arraycopy(data, 0, ret, 0, off);
                data = ret;
                break;
            }
            off += ilength;
            if (off >= data.length) { // overflow
                char[] aux = new char[2 * data.length];
                System.arraycopy(data, 0, aux, 0, data.length);
                data = aux;
            }
        }
        return data;
    }

    // std hck
    public void output(String s) {
        if (errConsumer != null) {
            errConsumer.pushError(null, -1, -1, s, null);
        }
    }


    /** method for compile method of Compiler */
    public Object getDeprElement() {
        return deprecationFiles.elementAt(0);
    }

    public void x_parseFile(ClassFile classfile)
    throws FileNotFoundException
    {
        long l = System.currentTimeMillis();
        dtEnter("parseFile: PARSING SOURCE " + classfile); // NOI18N
        Environment environment = new Environment(this, classfile);
        JavaParser javaParser = null;
        try {
            environment.setCharacterEncoding(getCharacterEncoding());
            if (classfile instanceof CoronaClassFile) {
                CoronaClassFile sourceFile = (CoronaClassFile) classfile;
                FileObject fo = sourceFile.getFile();
                javaParser = createParser(environment, fo);
                sourceFile.pis = javaParser.input;
            } else {
                // classfile found in sys classpath
                throw new IllegalArgumentException();
            }
        } catch(IOException ex) {
            dtEvent("parseFile: IO EXCEPTION " + classfile); // NOI18N
            throw new FileNotFoundException();
        }
        try {
            javaParser.parseFile();
        } catch(Exception exception) {
            throw new CompilerError(exception);
        }
        if(verbose())
        {
            l = System.currentTimeMillis() - l;
            output(Main.getText("benv.parsed_in", classfile.getPath(), Long.toString(l))); // NOI18N
        }
        if(javaParser.getClassesProtected().size() == 0)
        {
            javaParser.getImportsProtected().resolve(environment);
        }
        else
        {
            Enumeration enumeration = javaParser.getClassesProtected().elements();
            ClassDefinition classdefinition = (ClassDefinition)enumeration.nextElement();
            if(classdefinition.isInnerClass())
                throw new CompilerError("BatchEnvironment, first is inner"); // NOI18N
            ClassDefinition classdefinition1 = classdefinition;
            while(enumeration.hasMoreElements())
            {
                ClassDefinition classdefinition2 = (ClassDefinition)enumeration.nextElement();
                if(!classdefinition2.isInnerClass())
                {
                    classdefinition1.addDependency(classdefinition2.getClassDeclaration());
                    classdefinition2.addDependency(classdefinition1.getClassDeclaration());
                    classdefinition1 = classdefinition2;
                }
            }

            if(classdefinition1 != classdefinition)
            {
                classdefinition1.addDependency(classdefinition.getClassDeclaration());
                classdefinition.addDependency(classdefinition1.getClassDeclaration());
            }
        }
        dtExit("parseFile: SOURCE PARSED " + classfile); // NOI18N
    }

    public JavaParser createParser(Environment env, FileObject fo) throws IOException {
        try {
            JavaDataObject jdo = (JavaDataObject) DataObject.find(fo);
            SourceElementImpl source = jdo.getSourceElementImpl();
            if ((source.getStatus() == SourceElement.STATUS_NOT) || source.isDirty()) {
                return new JavaParser(env, fo, true);
            }
        } catch (DataObjectNotFoundException e) {
        } catch (ClassCastException e) {
        }

        return new JavaParser(env, fo, false);
    }

    int getClassesSize() {
        int counter = 0;
        for (Enumeration en = getClasses() ; en.hasMoreElements() ; en.nextElement())
            counter++;
        return counter;
    }

    /** Holds messages with errors in CoronaEnvironment
    *
    * @author Ales Novak
    */
    static class ErrorMessage {
        long where;
        String message;
        ErrorMessage next;

        ErrorMessage(long i, String string) {
            where = i;
            message = string;
        }
    }

}


/*
 * Log
 *  18   Gandalf-post-FCS1.16.2.0    2/24/00  Ian Formanek    Post FCS changes
 *  17   src-jtulach1.16        1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  16   src-jtulach1.15        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  15   src-jtulach1.14        11/25/99 Ales Novak      fix for 3801, 2929
 *  14   src-jtulach1.13        11/25/99 Ales Novak      NullPointerException
 *  13   src-jtulach1.12        11/5/99  Ales Novak      #2206
 *  12   src-jtulach1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        9/14/99  Ian Formanek    Fixed bug 3756 - 
 *       java.lang.StringIndexOutOfBoundsException during compilation
 *  10   src-jtulach1.9         8/27/99  Petr Hamernik   Error message became 
 *       inner class
 *  9    src-jtulach1.8         8/17/99  Ales Novak      #2206
 *  8    src-jtulach1.7         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    src-jtulach1.6         5/28/99  Ales Novak      environment creation 
 *       changed
 *  6    src-jtulach1.5         4/23/99  Petr Hrebejk    Classes temporay made 
 *       public
 *  5    src-jtulach1.4         4/16/99  Petr Hamernik   synchronization under 
 *       Nodes.MUTEX
 *  4    src-jtulach1.3         4/8/99   Ian Formanek    Removed debug prints
 *  3    src-jtulach1.2         3/29/99  Petr Hamernik   
 *  2    src-jtulach1.1         3/29/99  Petr Hamernik   
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 Jaroslav Tulach allows change of output stream and error consumer
 */
