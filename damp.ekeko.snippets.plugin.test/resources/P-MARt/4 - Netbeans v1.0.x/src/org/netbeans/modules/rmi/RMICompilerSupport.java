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
import java.lang.reflect.InvocationTargetException;

import org.openide.ServiceType;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.*;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  mryzl
 */

public class RMICompilerSupport extends org.netbeans.modules.java.JCompilerSupport {

    /** Name of the property stubCompilerType. */
    public static final String PROP_STUB_COMPILER_TYPE = "stubCompilerType"; // NOI18N

    /** extended attribute for the type of compiler */
    private static final String EA_STUB_COMPILER_MANAGER = "NetBeansAttrDataObjectStubCompilerManager"; // NOI18N

    /** entry to be associated with */
    protected MultiDataObject.Entry entry;

    /** entry to be associated with */
    private Class cookie;

    /** Utility field used by bound properties. */
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport (this);

    /** Creates new RMICompilerSupport. */
    RMICompilerSupport(MultiDataObject.Entry entry, Class cookie) {
        super(entry, cookie);
        this.entry = entry;
        this.cookie = cookie;
    }

    /**
    */
    public void addToJob(CompilerJob job, Compiler.Depth depth) {
        CompilerJob j2 = new CompilerJob(job.getInitialDepth());
        super.addToJob(j2, depth);
        job.dependsOn(j2);

        CompilerType sct = getStubCompilerType(entry);
        if (sct == null) sct = defaultStubCompilerType();

        Class xcookie;

        // compile with depth == 1 is build
        if (cookie == CompilerCookie.Compile.class) {
            xcookie = (Compiler.DEPTH_ONE == depth ? CompilerCookie.Build.class : cookie);
        } else {
            xcookie = cookie;
        }

        try {
            DataObject dobj = DataObject.find (entry.getFile ());
            sct.prepareJob(job, xcookie, dobj);
        } catch (DataObjectNotFoundException ex) {
            // no data object for the entry => stop the compilation
            ex.printStackTrace();
        }
    }

    /** Getter for property stubCompilerType.
     *@return Value of property stubCompilerType.
     */
    public static CompilerType getStubCompilerType(MultiDataObject.Entry entry) {
        CompilerType.Handle man = (CompilerType.Handle)entry.getFile ().getAttribute (EA_STUB_COMPILER_MANAGER);
        if (man == null) return null;
        ServiceType type = man.getServiceType ();
        if (type instanceof CompilerType) {
            return (CompilerType)type;
        } else {
            return null;
        }
    }

    /** Setter for property stubCompilerType.
     *@param stubCompilerType New value of property stubCompilerType.
     */
    public static void setStubCompilerType(MultiDataObject.Entry entry, CompilerType stubCompilerType) throws IOException {
        entry.getFile ().setAttribute (
            EA_STUB_COMPILER_MANAGER, stubCompilerType == null ? null : new CompilerType.Handle (stubCompilerType)
        );
    }

    /**
    * @return default stub compiler.
    */
    protected static CompilerType defaultStubCompilerType() {
        return CompilerType.find(RMIStubCompilerType.class);
    }

    /** Helper method that creates default properties for compilation of
    * given file entry.
    *
    * @param set sheet set to add properties to
    * @param entry entry properties should work with
    * @deprecated Please use the safer version that accepts a CompilerSupport object.
    */
    public static void addProperties (Sheet.Set set, MultiDataObject.Entry entry) {
        RMICompilerSupport supp = (RMICompilerSupport) entry.getDataObject ().getCookie (RMICompilerSupport.class);
        if (supp != null) addProperties (set, supp);
    }

    /**
    */
    public static void addProperties (Sheet.Set set, RMICompilerSupport supp) {
        set.put(createStubCompilerProperty (supp));
    }

    /** Creates the compiler property for entry.
    * @param entry the entry
    * @return the property
    */
    private static PropertySupport createStubCompilerProperty (final RMICompilerSupport supp) {
        java.util.ResourceBundle bundle = NbBundle.getBundle(RMICompilerSupport.class);

        return new PropertySupport.ReadWrite (
                   PROP_STUB_COMPILER_TYPE,
                   CompilerType.class,
                   bundle.getString("PROP_STUB_COMPILER"), // NOI18N
                   bundle.getString("HINT_STUB_COMPILER") // NOI18N
               ) {
                   public Object getValue() {
                       CompilerType ct = getStubCompilerType(supp.entry);
                       if (ct == null)
                           return supp.defaultStubCompilerType ();
                       else
                           return ct;
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       try {
                           setStubCompilerType(supp.entry, (CompilerType) val);
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
    public static class Compile extends RMICompilerSupport
        implements CompilerCookie.Compile {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Compile (Entry entry) {
            super (entry, CompilerCookie.Compile.class);
        }
    }

    /** Build cookie support.
    */
    public static class Build extends RMICompilerSupport
        implements CompilerCookie.Build {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Build (Entry entry) {
            super (entry, CompilerCookie.Build.class);
        }
    }

    /** Clean cookie support.
    */
    public static class Clean extends RMICompilerSupport
        implements CompilerCookie.Clean {

        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Clean (Entry entry) {
            super (entry, CompilerCookie.Clean.class);
        }

        /**
        */
        public void addToJob(CompilerJob job, Compiler.Depth depth) {
            try {
                RMICompilerSupport.defaultStubCompilerType().prepareJob(job, CompilerCookie.Clean.class, DataObject.find(entry.getFile()));
            } catch (DataObjectNotFoundException e) {
                // hmmm
            }
        }
    }
}

/*
* <<Log>>
*  12   Gandalf-post-FCS1.8.2.2     3/20/00  Martin Ryzl     localization
*  11   Gandalf-post-FCS1.8.2.1     3/8/00   Martin Ryzl     hide stubs feature
*  10   Gandalf-post-FCS1.8.2.0     2/24/00  Ian Formanek    Post FCS changes
*  9    Gandalf   1.8         1/21/00  Martin Ryzl     compilation fixed (new 
*       API)
*  8    Gandalf   1.7         12/23/99 Jaroslav Tulach mergeInto deleted from 
*       the CompilerJob.
*  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         10/8/99  Martin Ryzl     some bugfixes
*  5    Gandalf   1.4         10/8/99  Martin Ryzl     now extends directly 
*       org.openide.loaders.CompilerSupport
*  4    Gandalf   1.3         10/7/99  Martin Ryzl     
*  3    Gandalf   1.2         10/7/99  Martin Ryzl     
*  2    Gandalf   1.1         10/6/99  Martin Ryzl     debug info removed
*  1    Gandalf   1.0         10/6/99  Martin Ryzl     
* $ 
*/ 
