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

package org.openide.filesystems;

import java.beans.*;
import java.util.*;

import org.openide.TopManager;
import org.openide.util.enum.FilterEnumeration;

/** This class defines the capabilities of a file system to
* take part in different operations. Some file systems are
* not designed to allow compilation on them, some do not want
* to be present in class path when executing or debugging 
* a program. 
* <P>
* Moreover there can be additional capabilities to check
* and this class defines ways how one can communicated with
* a file system to find out whether the system is "capable"
* enough to be used in the operation.
*
* @author Jaroslav Tulach
*/
public class FileSystemCapability extends Object {
    /** Object that is capable of every thing.
    */
    public static final FileSystemCapability ALL = new FileSystemCapability () {
                public boolean capableOf (FileSystemCapability c) {
                    return true;
                }
            };

    /** Well known capability of being compiled */
    public static final FileSystemCapability COMPILE = new FileSystemCapability ();
    /** Well known ability to be executed */
    public static final FileSystemCapability EXECUTE = new FileSystemCapability ();
    /** Well known ability to be debugged */
    public static final FileSystemCapability DEBUG = new FileSystemCapability ();
    /** Well known ability to contain documentation files */
    public static final FileSystemCapability DOC = new FileSystemCapability ();


    /** Basic operation that tests whether this object
    * is capable to do different capability.
    * <P>
    * The default implementation claims that it is 
    * capable to handle only identical capability (==).
    * 
    * @param c capability to test 
    * @return true if yes
    */
    public boolean capableOf (FileSystemCapability c) {
        return c == this;
    }

    /** All filesystems that are capable of this capability.
    * @return enumeration of FileSystems that satifies this capability
    */
    public Enumeration fileSystems () {
        return new FilterEnumeration (TopManager.getDefault ().getRepository ().fileSystems ()) {
                   public boolean accept (Object o) {
                       FileSystem fs = (FileSystem)o;
                       return fs.getCapability().capableOf(FileSystemCapability.this);
                   }
               };
    }

    /** Find a resource in repository, ignoring not capable file systems.
    * @param resName name of the resource
    */
    public FileObject findResource (String resName) {
        Enumeration en = fileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.findResource(resName);
            if (fo != null) {
                // object found
                return fo;
            }
        }
        return null;
    }

    /** Searches for the given resource among all file systems
    * that satifies this capability, returning all matches.
    * @param name name of the resource
    * @return enumeration of {@link FileObject}s
    */
    public Enumeration findAllResources(String name) {
        Vector v = new Vector(8);
        Enumeration en = fileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.findResource(name);
            if (fo != null) {
                v.addElement(fo);
            }
        }
        return v.elements();
    }

    /** Finds file when its name is provided. It scans in the list of
    * file systems and asks them for the specified file by a call to
    * {@link FileSystem#find find}. The first object that is found is returned or <CODE>null</CODE>
    * if none of the file systems contain such a file.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return {@link FileObject} that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public final FileObject find (String aPackage, String name, String ext) {
        Enumeration en = fileSystems ();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.find (aPackage, name, ext);
            if (fo != null) {
                // object found
                return fo;
            }
        }
        return null;
    }

    /** Finds all files among all file systems with this capability
    * that match a given name, returning all matches.
    * All file systems are queried with {@link FileSystem#find}.
    *
    * @param aPackage package name where each package is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain the name of a package and not a file in it
    * @param ext extension of the file or <CODE>null</CODE> if one needs
    *    a package and not a file name
    *
    * @return enumeration of {@link FileObject}s
    */
    public final Enumeration findAll (String aPackage, String name, String ext) {
        Enumeration en = fileSystems ();
        Vector ret = new Vector();
        while (en.hasMoreElements ()) {
            FileSystem fs = (FileSystem)en.nextElement ();
            FileObject fo = fs.find (aPackage, name, ext);
            if (fo != null) {
                ret.addElement(fo);
            }
        }
        return ret.elements();
    }

    /** Adds PropertyChange listener. Every class which implements changes of capabilities
    * has to implement it's property change support.
    * @param l the listener to be added.
    */
    public synchronized void addPropertyChangeListener (PropertyChangeListener l) {}

    /** Removes PropertyChange listener. Every class which implements changes of capabilities
    * has to implement it's property change support.
    * @param l the listener to be removed.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {}


    /** Default implementation of capabilities, that behaves like
    * JavaBean and allows to set whether the well known 
    * capabilities (like compile, execute) should be enabled
    * or not.
    */
    public static class Bean extends FileSystemCapability implements java.io.Serializable {
        /** change listeners */
        private transient PropertyChangeSupport supp;

        /** compilation */
        private boolean compilation = true;
        /** execution */
        private boolean execution = true;
        /** debugging */
        private boolean debug = true;
        /** doc */
        private boolean doc = false;

        static final long serialVersionUID =627905674809532736L;
        /** Checks for well known capabilities and if they are allowed.
        * 
        * @param c capability to test 
        * @return true if yes
        */
        public boolean capableOf (FileSystemCapability c) {
            if (c == COMPILE) return compilation;
            if (c == EXECUTE) return execution;
            if (c == DEBUG) return debug;
            if (c == DOC) return doc;
            if (c == ALL) return true;

            if (!(c instanceof Bean)) {
                return false;
            }

            // try match of values
            Bean b = (Bean)c;

            return
                compilation == b.compilation &&
                execution == b.execution &&
                debug == b.debug &&
                doc == b.doc;
        }

        /** Getter for value of compiling capability.
        */
        public boolean getCompile () {
            return compilation;
        }

        /** Setter for allowing compiling capability.
        */
        public void setCompile(boolean val) {
            if (val != compilation) {
                compilation = val;
                if (supp != null) {
                    supp.firePropertyChange ("compile", new Boolean (!val), new Boolean (val)); // NOI18N
                }
            }
        }

        /** Getter for value of executiong capability.
        */
        public boolean getExecute () {
            return execution;
        }

        /** Setter for allowing executing capability.
        */
        public void setExecute (boolean val) {
            if (val != execution) {
                execution = val;
                if (supp != null) {
                    supp.firePropertyChange ("execute", new Boolean (!val), new Boolean (val)); // NOI18N
                }
            }
        }

        /** Getter for value of debugging capability.
        */
        public boolean getDebug () {
            return debug;
        }

        /** Setter for allowing debugging capability.
        */
        public void setDebug (boolean val) {
            if (val != debug) {
                debug = val;
                if (supp != null) {
                    supp.firePropertyChange ("debug", new Boolean (!val), new Boolean (val)); // NOI18N
                }
            }
        }

        /** Getter for value of doc capability.
        */
        public boolean getDoc () {
            return doc;
        }

        /** Setter for allowing debugging capability.
        */
        public void setDoc (boolean val) {
            if (val != doc) {
                doc = val;
                if (supp != null) {
                    supp.firePropertyChange ("doc", new Boolean (!val), new Boolean (val)); // NOI18N
                }
            }
        }

        /** Adds listener.
        */
        public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
            if (supp == null) {
                supp = new PropertyChangeSupport (this);
            }
            supp.addPropertyChangeListener (l);
        }

        /** Removes listener.
        */
        public void removePropertyChangeListener (PropertyChangeListener l) {
            if (supp != null) {
                supp.removePropertyChangeListener (l);
            }
        }
    }
}

/*
* Log
*  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
*  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  6    Gandalf   1.5         7/30/99  Petr Hrebejk    Skeletons of 
*       PropertyChange support methods added to FileSystemCapability
*  5    Gandalf   1.4         7/26/99  Petr Hrebejk    Default value of DOC 
*       Capability set to false.
*  4    Gandalf   1.3         6/9/99   Jaroslav Tulach Executables can be in 
*       menu & toolbars.
*  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         6/8/99   Ales Novak      #2131
*  1    Gandalf   1.0         6/7/99   Jaroslav Tulach 
* $
*/