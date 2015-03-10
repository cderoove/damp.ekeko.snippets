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

package org.openide.loaders;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openide.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.filesystems.*;
import org.openide.cookies.CompilerCookie;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/** Support for compilation of data objects.
*
* @author Jaroslav Tulach
*/
public class CompilerSupport extends Object implements CompilerCookie {
    /** extended attribute for the type of compiler */
    private static final String EA_COMPILER_MANAGER = "NetBeansAttrDataObjectCompilerManager"; // NOI18N
    /** Name of property providing a custom {@link CompilerType} for a file. */
    public static final String PROP_COMPILER_TYPE = "compiler"; // NOI18N

    /** entry to be associated with */
    private MultiDataObject.Entry entry;

    /** cookie class for the compilation */
    private Class cookie;

    /** New support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    * @param cookie cookie class for the compilation (e.g. {@link CompilerCookie.Build})
    */
    protected CompilerSupport(MultiDataObject.Entry entry, Class cookie) {
        this.entry = entry;
        this.cookie = cookie;
    }

    /** Supports only {@link Compiler#DEPTH_ONE depth one}.
    */
    public boolean isDepthSupported (Compiler.Depth depth) {
        return Compiler.DEPTH_ONE == depth;
    }

    /* Adds the right compiler to the job.
    */
    public void addToJob (CompilerJob job, Compiler.Depth depth) {
        CompilerType comp = getCompilerType (entry);
        if (comp == null) {
            comp = defaultCompilerType ();
        }

        try {
            Class xcookie;
            if (cookie == CompilerCookie.Compile.class) {
                xcookie = (Compiler.DEPTH_ONE == depth ? CompilerCookie.Build.class : cookie);
            } else {
                xcookie = cookie;
            }
            comp.prepareJob (job, xcookie, DataObject.find (entry.getFile ()));
        } catch (DataObjectNotFoundException ex) {
            // no data object for the entry => stop the compilation
        }
    }

    /** Allows subclasses to override the default compiler that should
    * be used for this support.
    */
    protected CompilerType defaultCompilerType () {
        return CompilerType.getDefault ();
    }

    /** Set compiler manager for a given file.
    * @param entry entry to set the compiler for
    * @param man type to use
    * @exception IOException if compiler cannot be set
    */
    public static void setCompilerType (MultiDataObject.Entry entry, CompilerType man)
    throws IOException {
        entry.getFile ().setAttribute (
            EA_COMPILER_MANAGER, man == null ? null : new CompilerType.Handle (man)
        );
    }

    /** Get compiler manager associated with a given file.
    * @param entry entry to obtain the compiler for
    * @return associated manager or null
    */
    public static CompilerType getCompilerType (MultiDataObject.Entry entry) {
        CompilerType.Handle man = (CompilerType.Handle)entry.getFile ().getAttribute (EA_COMPILER_MANAGER);
        if (man == null) return null;
        ServiceType type = man.getServiceType ();
        if (type instanceof CompilerType) {
            return (CompilerType)type;
        } else {
            return null;
        }
    }

    /** Helper method that creates default properties for compilation of
    * given file entry.
    *
    * @param set sheet set to add properties to
    */
    public void addProperties (Sheet.Set set) {
        set.put (createCompilerProperty ());
    }

    /** Creates the compiler property.
    * @return the property
    */
    private PropertySupport createCompilerProperty () {
        return new PropertySupport.ReadWrite (
                   PROP_COMPILER_TYPE,
                   CompilerType.class,
                   DataObject.getString("PROP_compilerType"),
                   DataObject.getString("HINT_compilerType")
               ) {
                   public Object getValue() {
                       CompilerType ct = getCompilerType(entry);
                       if (ct == null)
                           return defaultCompilerType ();
                       else
                           return ct;
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       try {
                           setCompilerType(entry, (CompilerType) val);
                       } catch (IOException ex) {
                           throw new InvocationTargetException (ex);
                       }
                   }

                   public boolean supportsDefaultValue () {
                       return true;
                   }

                   public void restoreDefaultValue () throws InvocationTargetException {
                       setValue (null);
                   }
               };
    }


    /** Compile cookie support.
    * Note that as a special case, when {@link Compiler#DEPTH_ONE} is requested,
    * a {@link CompilerCookie.Build} will actually be sent to the compiler manager,
    * rather than a {@link CompilerCookie.Compile}, on the assumption that the user
    * wished to force (re-)compilation of the single data object.
    */
    public static class Compile extends CompilerSupport
        implements CompilerCookie.Compile {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Compile (MultiDataObject.Entry entry) {
            super (entry, CompilerCookie.Compile.class);
        }
    }

    /** Build cookie support.
    */
    public static class Build extends CompilerSupport
        implements CompilerCookie.Build {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Build (MultiDataObject.Entry entry) {
            super (entry, CompilerCookie.Build.class);
        }
    }

    /** Clean cookie support.
    */
    public static class Clean extends CompilerSupport
        implements CompilerCookie.Clean {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Clean (MultiDataObject.Entry entry) {
            super (entry, CompilerCookie.Clean.class);
        }
    }

}

/*
* Log
*  20   Gandalf   1.19        1/12/00  Ian Formanek    NOI18N
*  19   Gandalf   1.18        10/29/99 Jesse Glick     Removed deprecated static
*       variants of {Exec,Compiler}Support.addProperties.
*  18   Gandalf   1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  17   Gandalf   1.16        10/7/99  Jesse Glick     Encouraged to use 
*       nonstatic methods to add properties to sheet sets for supports.
*  16   Gandalf   1.15        10/1/99  Jesse Glick     Cleanup of service type 
*       name presentation.
*  15   Gandalf   1.14        9/15/99  Jaroslav Tulach Query when wrong executor
*       or debugger is used.
*  14   Gandalf   1.13        9/10/99  Jaroslav Tulach Changes in services APIs.
*  13   Gandalf   1.12        8/12/99  Ales Novak      Only the 
*       CompilerCookie.Compile class is replaced by the Build class
*  12   Gandalf   1.11        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  11   Gandalf   1.10        4/19/99  Jesse Glick     [JavaDoc]
*  10   Gandalf   1.9         4/16/99  Jesse Glick     Compiler.Manager.find() 
*       now takes class rather than instance of DO.
*  9    Gandalf   1.8         4/2/99   Ales Novak      
*  8    Gandalf   1.7         3/15/99  Jesse Glick     [JavaDoc]
*  7    Gandalf   1.6         3/14/99  Jaroslav Tulach Change of 
*       MultiDataObject.Entry.
*  6    Gandalf   1.5         3/9/99   Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         2/19/99  Jaroslav Tulach More compiler managers.
*  4    Gandalf   1.3         2/4/99   Petr Hamernik   setting of extended file 
*       attributes doesn't require FileLock
*  3    Gandalf   1.2         1/20/99  Petr Hamernik   
*  2    Gandalf   1.1         1/6/99   Ian Formanek    
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
