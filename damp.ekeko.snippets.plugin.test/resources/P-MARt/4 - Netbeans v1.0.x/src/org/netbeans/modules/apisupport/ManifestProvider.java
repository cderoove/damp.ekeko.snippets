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

package org.netbeans.modules.apisupport;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.jar.*;
import javax.swing.event.*;

import org.openide.execution.Executor;
import org.openide.modules.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListener;

public interface ManifestProvider extends Node.Cookie {

    public Manifest getManifest () throws IOException;

    public void setManifest (Manifest m) throws IOException;

    public void addFiles (Set/* <FileObject> */ files) throws IOException;

    public void removeFiles (Set/* <FileObject> */ files) throws IOException;

    public Set/* <FileObject> */ getFiles () throws IOException;

    public void addChangeListener (ChangeListener list);

    public void removeChangeListener (ChangeListener list);

    public boolean isValid ();

    public Exception getParseException ();

    public File getManifestAsFile ();

    public static class ModuleExecSupport extends ExecSupport {

        public ModuleExecSupport (MultiDataObject.Entry entry) {
            super (entry);
        }

        protected Executor defaultExecutor () {
            return Executor.find (InstallModuleExecutor.class);
        }

    }

    public static abstract class Util extends Object {

        private Util () {}

        public static Exception checkForException (ManifestProvider p) {
            if (! p.isValid ()) {
                Exception e = p.getParseException ();
                if (e != null)
                    return e;
                else
                    return new Exception ("(unspecified manifest syntax error)");
            }
            try {
                new ModuleDescription ("test", p.getManifest ());
                return null;
            } catch (IOException ioe) {
                // Includes IllegalModuleException:
                return ioe;
            }
        }

        public static void updateName (Node n) {
            updateName (n, (ManifestProvider) n.getCookie (ManifestProvider.class));
        }

        public static void updateName (final Node n, final ManifestProvider p) {
            try {
                String displayName = NbBundle.getLocalizedValue (p.getManifest ().getMainAttributes (), ModuleDescription.TAG_NAME);
                // [PENDING] file system annotations
                if (displayName == null)
                    n.setDisplayName (n.getName () + " [no name]");
                else
                    n.setDisplayName (n.getName () + " [" + displayName + "]");
            } catch (IOException ioe) {
                ioe.printStackTrace ();
            }
            ChangeListener list = new ChangeListener () {
                                      public void stateChanged (ChangeEvent ev) {
                                          updateName (n, p);
                                      }
                                  };
            n.setValue ("ManifestProvider.Util.updateName.changeListener", list);
            p.addChangeListener (WeakListener.change (list, p));
        }

        public static void addToSheet (Sheet sheet, ManifestProvider p) {
            Sheet.Set set = new Sheet.Set ();
            set.setName ("openide");
            set.setDisplayName ("Module Properties");
            set.setShortDescription ("General properties of the module.");
            set.put (new GeneralProp (p, ModuleDescription.TAG_MAGIC, "Code Name",
                                      "Code name for the module, e.g. \"com.mycom.mymodule\" or \"com.mycom.mymodule/1\"."));
            set.put (new GeneralProp (p, ModuleDescription.TAG_NAME, "Display Name",
                                      "Display name for this module (in the base locale only!)."));
            set.put (new GeneralProp (p, ModuleDescription.TAG_SPEC_VERSION, "Specification Version",
                                      "Specification version, e.g. 1.0 or 1.13.9.2."));
            set.put (new GeneralProp (p, ModuleDescription.TAG_IMPL_VERSION, "Implementation Version",
                                      "Implementation version; no particular format."));
            // XXX would be nicer as subnodes, but this will do for now
            set.put (new GeneralProp (p, ModuleDescription.TAG_MODULE_DEPENDENCIES, "Module Dependencies",
                                      "Dependencies on other modules, e.g. \"com.mycom.myothermodule/1 > 0.9.1 , org.netbeans.modules.java\"."));
            set.put (new GeneralProp (p, ModuleDescription.TAG_PACKAGE_DEPENDENCIES, "Package Dependencies",
                                      "Dependencies on packages and standard extensions, e.g. \"javax.servlet , javax.ejb > 1.1\"."));
            set.put (new GeneralProp (p, ModuleDescription.TAG_JAVA_DEPENDENCIES, "Java Dependencies",
                                      "Dependencies on Java (APIs and VM), e.g. \"Java = 1.2.1b4, VM > 1.0\"."));
            set.put (new GeneralProp (p, ModuleDescription.TAG_IDE_DEPENDENCIES, "IDE Dependencies",
                                      "Dependencies on the Open APIs, e.g. \"IDE/1 > 1.0\"."));
            sheet.put (set);
            set = new Sheet.Set ();
            set.setName ("openideparse");
            set.setDisplayName ("Manifest Parsing");
            set.setShortDescription ("Information pertaining to the parse status of the manifest file.");
            set.put (new ManifestValidProp (p));
            set.put (new ManifestErrorProp (p));
            sheet.put (set);
        }

        private static class GeneralProp extends PropertySupport.ReadWrite {
            private Attributes.Name tagname;
            private ManifestProvider provider;
            public GeneralProp (ManifestProvider provider, Attributes.Name tagname, String displayName, String shortDescription) {
                super (tagname.toString (), String.class, displayName, shortDescription);
                this.provider = provider;
                this.tagname = tagname;
            }
            public Object getValue () throws InvocationTargetException {
                try {
                    String val = provider.getManifest ().getMainAttributes ().getValue (tagname);
                    return (val == null) ? "" : val;
                } catch (IOException ioe) {
                    throw new InvocationTargetException (ioe);
                }
            }
            public void setValue (Object nue) throws IllegalArgumentException, InvocationTargetException {
                if (! (nue instanceof String)) throw new IllegalArgumentException ();
                try {
                    Manifest mani = provider.getManifest ();
                    if (nue.equals (""))
                        mani.getMainAttributes ().remove (tagname);
                    else
                        mani.getMainAttributes ().put (tagname, nue);
                    provider.setManifest (mani);
                } catch (IOException ioe) {
                    throw new InvocationTargetException (ioe);
                }
            }
            public void restoreDefaultValue () throws IllegalArgumentException, InvocationTargetException {
                setValue ("");
            }
            public boolean supportsDefaultValue () {
                return true;
            }
        }

        private static class ManifestValidProp extends PropertySupport.ReadOnly {
            private ManifestProvider p;
            public ManifestValidProp (ManifestProvider p) {
                super ("manifestValid", Boolean.TYPE, "Valid Manifest", "Whether the manifest is currently valid (both in terms of raw syntax and OpenIDE requirements).");
                this.p = p;
            }
            public Object getValue () {
                return new Boolean (checkForException (p) == null);
            }
        }

        private static class ManifestErrorProp extends PropertySupport.ReadOnly {
            private ManifestProvider p;
            public ManifestErrorProp (ManifestProvider p) {
                super ("manifestError", String.class, "Manifest Error", "Current parse error for the manifest, if any.");
                this.p = p;
            }
            public Object getValue () {
                Exception e = checkForException (p);
                if (e == null)
                    return "<none>";
                else if (e.getLocalizedMessage () == null)
                    return "<unspecified " + Utilities.getShortClassName (e.getClass ()) + " error>";
                else
                    return e.getLocalizedMessage ();
            }
        }

    }

    public static class CategoryChildren extends Children.Keys {

        private ManifestProvider provider;
        private ChangeListener list;

        public CategoryChildren (ManifestProvider provider) {
            this.provider = provider;
            provider.addChangeListener (WeakListener.change (list = new ChangeListener () {
                                            public void stateChanged (ChangeEvent ev) {
                                                addNotify ();
                                            }
                                        }, provider));
        }

        protected void addNotify () {
            setKeys (new Object[] {
                         ModuleDescription.TAG_MAIN,
                         ModuleDescription.TAG_DESCRIPTION,
                         ModuleDescription.SECTION_ACTION,
                         ModuleDescription.SECTION_CLIPBOARD_CONVERTOR,
                         ModuleDescription.SECTION_DEBUGGER,
                         ModuleDescription.SECTION_FILESYSTEM,
                         ModuleDescription.SECTION_LOADER,
                         ModuleDescription.SECTION_NODE,
                         ModuleDescription.SECTION_OPTION,
                         ModuleDescription.SECTION_SERVICE,
                         /*
                         ModuleDescription.TAG_MODULE_DEPENDENCIES,
                         ModuleDescription.TAG_PACKAGE_DEPENDENCIES,
                         ModuleDescription.TAG_JAVA_DEPENDENCIES,
                         ModuleDescription.TAG_IDE_DEPENDENCIES,
                         */
                     });
        }

        protected void removeNotify () {
            setKeys (new Object[] { });
        }

        protected Node[] createNodes (Object key) {
            return new Node[] { new CategoryNode (provider, key) };
        }

    }

}

/*
 * Log
 *  5    Gandalf-post-FCS1.2.1.1     3/28/00  Jesse Glick     More robust module 
 *       install executor.
 *  4    Gandalf-post-FCS1.2.1.0     3/23/00  Jesse Glick     More informative node 
 *       names.
 *  3    Gandalf   1.2         1/26/00  Jesse Glick     Live manifest parsing.
 *  2    Gandalf   1.1         1/26/00  Jesse Glick     Manifest handling 
 *       changed--now more dynamic, synched properly with open document as for 
 *       real file types.
 *  1    Gandalf   1.0         1/22/00  Jesse Glick     
 * $
 */
