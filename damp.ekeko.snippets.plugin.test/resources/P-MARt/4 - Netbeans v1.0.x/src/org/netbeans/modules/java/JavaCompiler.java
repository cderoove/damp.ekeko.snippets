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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Field;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import javax.swing.Timer;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.ClassNotFound;
import sun.tools.java.Constants;
import sun.tools.java.Environment;
import sun.tools.javac.ErrorConsumer;
import sun.tools.javac.SourceClass;
import sun.tools.javac.BatchParser;
import sun.tools.javac.BatchEnvironment;

import org.openide.compiler.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileUtil;
import org.openide.util.enum.ArrayEnumeration;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.java.settings.CompilerSettings;

/** This is a base class in Corona for compiling java sources. It is derived (but not inherits) from Sun's class Main in javac package. Behavior of this class should be the same.
*
* @author Ales Novak, Jaroslav Tulach
*/
public class JavaCompiler {

    /** environment to share between invocations */
    private static WeakReference defaultEnv;

    static {
        initiate();
    }

    private JavaCompiler() {
    }

    /** Retrieves well known Identifiers. */
    private static void initiate() {

        try {
            Class ident = Class.forName("sun.tools.java.Identifier"); // NOI18N
            Class type = Class.forName("sun.tools.java.Type"); // NOI18N
            Class.forName("sun.tools.java.Scanner"); // NOI18N

            Field typeHashField = type.getDeclaredField("typeHash"); // NOI18N
            typeHashField.setAccessible(true);
            Hashtable hash = new Util.WeakHashtable((Hashtable) typeHashField.get(null));
            typeHashField.set(null, hash);

            Field identHash = ident.getDeclaredField("hash"); // NOI18N
            identHash.setAccessible(true);
            hash = new Util.WeakHashtable((Hashtable) identHash.get(null));
            identHash.set(null, hash);

        } catch (Throwable e) {
            if (Boolean.getBoolean("netbeans.debug.memory")) { // NOI18N
                e.printStackTrace();
            }
        }
    }

    /** Creates well formed Environment */
    static CoronaEnvironment createEnvironment(ErrConsumer econs) {
        CoronaClassPath ccp;
        return new CoronaEnvironment (
                   ccp = new CoronaClassPath(false, null),
                   new CoronaClassPath(true, ccp),
                   econs
               );
    }

    /** getter for defaultEnv, which is shared Environment */
    static CoronaEnvironment getSharedEnv() {
        WeakReference wr = defaultEnv;
        CoronaEnvironment cenv;
        if (wr == null) {
            return null;
        } else if ((cenv = (CoronaEnvironment) wr.get()) == null) {
            return null;
        } else {
            return cenv;
        }
    }

    /** setter for shared Environment */
    static void setSharedEnv(CoronaEnvironment env) {
        defaultEnv = new WeakReference(env);
    }
}


/*
 * Log
 *  25   src-jtulach1.24        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  24   src-jtulach1.23        1/11/00  Ales Novak      "field is final" 
 *       exception prints out if netbeans.debug.memory=true property is 
 *       specified
 *  23   src-jtulach1.22        1/10/00  Ales Novak      new compiler API 
 *       deployed
 *  22   src-jtulach1.21        1/5/00   Ales Novak      equals methods
 *  21   src-jtulach1.20        11/30/99 Ales Novak      cleaning is 
 *       FileSystem.AtomicAction   processing of javac errors moved into 
 *       JavaCompilerGroup
 *  20   src-jtulach1.19        11/24/99 Ales Novak      improved processing of 
 *       exceptions
 *  19   src-jtulach1.18        11/9/99  Ales Novak      CoronaEnvironment kept 
 *       through WeakReference
 *  18   src-jtulach1.17        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  17   src-jtulach1.16        10/5/99  Ales Novak      isUpToDate method bugfix
 *  16   src-jtulach1.15        9/29/99  Ales Novak      CompilerType used
 *  15   src-jtulach1.14        8/12/99  Ales Novak      class files could be 
 *       'perfectly'  examined about their source file
 *  14   src-jtulach1.13        8/5/99   Ales Novak      BUILD actions remove 
 *       possibly generated class files
 *  13   src-jtulach1.12        8/4/99   Ales Novak      bugfix #1658
 *  12   src-jtulach1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   src-jtulach1.10        6/8/99   Ales Novak      FSCapabilities deployed
 *  10   src-jtulach1.9         5/28/99  Ales Novak      environment creation 
 *       changed
 *  9    src-jtulach1.8         4/13/99  Ales Novak      bugfix #1445
 *  8    src-jtulach1.7         4/9/99   Ian Formanek    Compiler progress 
 *       notifications improved
 *  7    src-jtulach1.6         4/8/99   Ales Novak      
 *  6    src-jtulach1.5         4/2/99   Ales Novak      
 *  5    src-jtulach1.4         4/1/99   Ales Novak      
 *  4    src-jtulach1.3         3/31/99  Ales Novak      
 *  3    src-jtulach1.2         3/29/99  Petr Hamernik   
 *  2    src-jtulach1.1         3/29/99  Petr Hamernik   
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 * Beta Change History:
 *  0    Tuborg    0.18        --/--/98 Jaroslav Tulach changed constructor
 *  0    Tuborg    0.19        --/--/98 Petr Hamernik   depth of compilation added
 *  0    Tuborg    0.20        --/--/98 Petr Hamernik   small change is build flag of compile
 *  0    Tuborg    0.20        --/--/98 Petr Hamernik   small changes in superclass
 */
