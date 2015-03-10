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

package org.openide.modules;

import java.text.MessageFormat;
import java.util.*;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.openide.util.NbBundle;
import org.openide.loaders.DataLoader;
import org.openide.options.SystemOption;
import org.openide.TopManager;
import org.openide.filesystems.FileSystem;
import org.openide.debugger.Debugger;
import org.openide.ServiceType;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.datatransfer.ExClipboard;

/** Class representing one specially-treated section in a module's manifest file.
 * For example, one section may describe a single action provided by the module.
*
* @author Jaroslav Tulach
*/
public abstract class ManifestSection extends Object {
    /** name of the class file, e.g. foo/Bar.class */
    String name;
    /** name of the class, e.g. foo.bar */
    private String className;
    /** instance of the class or exeception if not possible to create it */
    private Object result;

    /** Package private constructor to allow creation only from this package.
    * @param name name of section, should be a class file, e.g. foo/Bar.class
    * @param attr attributes for one section
    * @throws IllegalModuleException if the name is not valid for an OpenIDE section
    */
    ManifestSection (String name, Attributes attr) throws IllegalModuleException {
        this.name = name;
        className = ModuleDescription.createPackageName (name);
    }

    /** Getter for instance of this class.
    * @return the insttance
    * @exception Exception if there is an error
    */
    Object createInstance () throws Exception {
        return java.beans.Beans.instantiate (
                   TopManager.getDefault ().currentClassLoader (), className
               );
    }

    /** Getter for the only installed instance.
    *
    * @return the insttance
    * @exception Exception if there is an error
    */
    synchronized Object getInstance () throws Exception {
        if (result instanceof Exception) {
            ((Exception)result).fillInStackTrace ();
            throw (Exception)result;
        }
        if (result == null) {
            try {
                result = createInstance ();
            } catch (Exception ex) {
                // remember the exception
                result = ex;
                throw ex;
            } catch (Throwable ex) {
                result = new ClassNotFoundException (exceptionMessage (ex));
                throw (Exception) result;
            }
        }
        return result;
    }

    /** Creates a description of an exception.
    * @param ex exception
    * @return the string representation of the exception
    */
    static String exceptionMessage (Throwable ex) {
        StringWriter sw = new StringWriter ();
        PrintWriter w = new PrintWriter (sw);

        w.println (ex.getClass ().getName ());
        ex.printStackTrace (w);
        w.close ();

        return sw.toString ();
    }

    /** Abstract method of sections that invokes right method of the given
    * iterator.
    * @exception Exception if the section should be removed from list of section
    */
    abstract void invokeIterator (Iterator it) throws Exception;

    /** Parses attributes and creates its description.
    * @param name name of the section
    * @param attr attribues
    * @return the section or null if attributes does not represent section
    * @exception IllegalModuleException if the attributes are not valid
    */
    static ManifestSection createSection (String name, Attributes attr) throws IllegalModuleException {

        // Analyze the section
        String sectionName = attr.getValue (ModuleDescription.TAG_SECTION_CLASS);
        if (sectionName == null) {
            // no section tag
            return null;
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_ACTION)) {
            return new ActionSection(name, attr);
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_OPTION)) {
            return new OptionSection(name, attr);
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_LOADER)) {
            return new LoaderSection(name, attr);
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_FILESYSTEM)) {
            return new FileSystemSection(name, attr);
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_NODE)) {
            return new NodeSection(name, attr);
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_SERVICE)) {
            return new ServiceSection(name, attr);
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_DEBUGGER)) {
            return new DebuggerSection(name, attr);
        } else if (sectionName.equalsIgnoreCase(ModuleDescription.SECTION_CLIPBOARD_CONVERTOR)) {
            return new ClipboardConvertorSection (name, attr);
        } else {
            throw new IllegalModuleException
            (MessageFormat.format
             (NbBundle.getBundle (ManifestSection.class).getString ("EXC_IllegalModuleClass"),
              new Object[] { name, sectionName }));
        }
    }

    /** Iterator over different types of sections.
     * @see ModuleDescription#forEachSection
    */
    public static interface Iterator {
        /** Process action section.
        * @param as the section
        * @exception InstantiationException if there is an error and the section should be ignored
        */
        public void processAction (ActionSection as) throws InstantiationException;

        /** Process option section.
        * @param os the section
        * @exception InstantiationException if there is an error and the section should be ignored
        */
        public void processOption (OptionSection os) throws InstantiationException;

        /** Process loader section.
        * @param is the section
        * @exception InstantiationException if there is an error and the section should be ignored
        */
        public void processLoader (LoaderSection ls) throws InstantiationException;

        /** Process debugger section.
        * @param ds the section
        * @exception InstantiationException if there is an error and the section should be ignored
        */
        public void processDebugger (DebuggerSection ds) throws InstantiationException;

        /** Process service section.
        * @param es the section
        * @exception Instantiationception if there is an error and the section should be ignored
        */
        public void processService (ServiceSection es) throws InstantiationException;

        /** Process file system section.
        * @param fs the section
        * @exception InstantiationException if there is an error and the section should be ignored
        */
        public void processFileSystem (FileSystemSection fs) throws InstantiationException;

        /** Process node section.
        * @param es the section
        * @exception InstantiationException if there is an error and the section should be ignored
        */
        public void processNode (NodeSection es) throws InstantiationException;

        /** Process clipboard convertor section.
        * @param ccs the section
        * @exception InstantiationException if there is an error and the section should be ignored
        */
        public void processClipboardConvertor (ClipboardConvertorSection ccs) throws InstantiationException;
    }

    /** Module section for an Action.
     * @see SystemAction
    */
    public static final class ActionSection extends ManifestSection {
        /** name of toolbar to place the action to */
        private String toolbar;
        /** name of menu to place the action to */
        private String menu;
        /** text description of key to assign to this action */
        private String key;

        ActionSection(String name, Attributes attrs) throws IllegalModuleException {
            super(name, attrs);
        }

        /** Get the action object.
         * @return the action
        * @exception InstantiationException if the action cannot be created
        */
        public SystemAction getAction () throws InstantiationException {
            try {
                return (SystemAction)super.getInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }


        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processAction (this);
        }
    }

    /** Module section for an Option.
     * @see SystemOption
     */
    public static final class OptionSection extends ManifestSection {
        OptionSection (String name, Attributes attrs) throws IllegalModuleException {
            super(name, attrs);
        }

        /** Get the system option.
         * @return the option
        * @exception InstantiationException if the action cannot be created
        */
        public SystemOption getOption () throws InstantiationException {
            try {
                return (SystemOption)super.getInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processOption (this);
        }
    }

    /** Module section for a Data Loader.
     * @see DataLoader
     */
    public static final class LoaderSection extends ManifestSection {
        /** class name(s) of data object to
        * be inserted after loader that recognizes its
        */
        private String[] installAfter;
        /** class name(s) of data object to be inserted before its recognizing
        * data loader
        */
        private String[] installBefore;

        LoaderSection (String name, Attributes attrs) throws IllegalModuleException {
            super (name, attrs);
            String val = attrs.getValue (ModuleDescription.TAG_INSTALL_AFTER);
            StringTokenizer tok;
            List res;
            if (val != null) {
                tok = new StringTokenizer (val, ", "); // NOI18N
                res = new LinkedList ();
                while (tok.hasMoreTokens ()) {
                    String clazz = tok.nextToken ();
                    if (! clazz.equals ("")) // NOI18N
                        res.add (clazz);
                }
                installAfter = (String[]) res.toArray (new String[res.size ()]);
            } else {
                installAfter = null;
            }
            val = attrs.getValue (ModuleDescription.TAG_INSTALL_BEFORE);
            if (val != null) {
                tok = new StringTokenizer (val, ", "); // NOI18N
                res = new LinkedList ();
                while (tok.hasMoreTokens ()) {
                    String clazz = tok.nextToken ();
                    if (! clazz.equals ("")) // NOI18N
                        res.add (clazz);
                }
                installBefore = (String[]) res.toArray (new String[res.size ()]);
            } else {
                installBefore = null;
            }
        }

        /** Get the data loader.
         * @throws InstantiationException if the loader could not be created
        * @return the loader
        */
        public DataLoader getLoader () throws InstantiationException {
            try {
                return (DataLoader)super.getInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Get the representation class(es) of the loader(s) that this one should be installed after.
        * @return a list of class names, or <code>null</code>
        */
        public String[] getInstallAfter () {
            return installAfter;
        }

        /** Get the representation class(es) of the loader(s) that this one should be installed before.
        * @return a list of class names, or <code>null</code>
        */
        public String[] getInstallBefore () {
            return installBefore;
        }

        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processLoader (this);
        }
    }

    /** Module section for a Debugger.
     * @see Debugger
     */
    public static final class DebuggerSection extends ManifestSection {
        DebuggerSection (String name, Attributes attrs) throws IllegalModuleException {
            super(name, attrs);
        }

        /** Get the debugger object.
         * @return the debugger
         * @throws InstantiationException if the debugger could not be created
        */
        public Debugger getDebugger () throws InstantiationException {
            try {
                return (Debugger)super.getInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processDebugger (this);
        }
    }

    /** Module section for an Service.
     * @see Service
     */
    public static final class ServiceSection extends ManifestSection {
        /** attributes associated with this section */
        private Attributes attr;

        ServiceSection(String name, Attributes attrs) throws IllegalModuleException {
            super(name, attrs);
            this.attr = attrs;
        }


        // UNUSED--
        /** Get the display name of the Service.
        * If none was specified, a default name will be created.
        *
        * @return the display name
        public String getName() {
          String s = (String)NbBundle.getLocalizedValue (attr, ModuleDescription.TAG_SERVICE_NAME);
          if (s == null) {
            return MessageFormat.format (
              NbBundle.getBundle (ManifestSection.class).getString ("CTL_Service_Unknown"),
              new Object[] { name }
            );
          } else {
            return s;
          }
    }
        */

        /** Is this service default? That means should it be placed
        * in front of other services?
        *
        * @return true if it is 
        */
        public boolean isDefault () {
            return Boolean.valueOf (attr.getValue (ModuleDescription.TAG_SERVICE_DEFAULT)).booleanValue ();
        }

        /** Getter for the default instance of this service type.
         * @return the service type
         * @throws InstantiationException if the Service could not be created
        */
        public ServiceType getServiceType () throws InstantiationException {
            try {
                return (ServiceType)super.getInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Create a new Service of the specified type.
         * @return the Service
         * @throws InstantiationException if the Service could not be created
        */
        public ServiceType createServiceType () throws InstantiationException {
            try {
                return (ServiceType)super.createInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processService (this);
        }
    }

    /** Module section for a File System.
     * @see FileSystem
    */
    public static final class FileSystemSection extends ManifestSection {
        /** attributes associated with this section */
        private Attributes attr;

        /** Constructor */
        FileSystemSection (String name, Attributes attr) throws IllegalModuleException {
            super (name, attr);
            this.attr = attr;
        }

        /** Get the display name of the file system.
         * This could be used e.g. in a context menu on the Repository.
         * If none was specified, a default name will be created.
        *
        * @return the name
        */
        public String getName() {
            String s = (String)NbBundle.getLocalizedValue (attr, ModuleDescription.TAG_FILESYSTEM_NAME);
            if (s == null) {
                return MessageFormat.format (
                           NbBundle.getBundle (ManifestSection.class).getString ("CTL_Repository_Unknown"),
                           new Object[] { this.name }
                       );
            } else {
                return s;
            }
        }

        /** Get a help context for the file system.
         * If none was specified, a default context will be created.
        * @return the help context
        */
        public HelpCtx getHelpCtx () {
            String s = attr.getValue (ModuleDescription.TAG_FILESYSTEM_HELP);
            return s == null ?
                   org.openide.util.HelpCtx.DEFAULT_HELP :
                   // [PENDING] this constructor looks for a JavaHelp tag, but docs say /com/mycom/index.html!
                   new org.openide.util.HelpCtx(s);
        }

        /** Create a new file system.
        * @return the file system
        * @throws InstantiationException if it could not be created
        */
        public FileSystem createFileSystem () throws InstantiationException {
            try {
                return (FileSystem)super.createInstance ();
            } catch (Exception ex) {
                if (System.getProperty("netbeans.debug.exceptions") != null) ex.printStackTrace();
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processFileSystem (this);
        }

    }

    /** Module section for a node.
    * @see Node
    */
    public static final class NodeSection extends ManifestSection {
        /** Type to add an entry to the root nodes. */
        public static final String TYPE_ROOTS = "roots"; // NOI18N
        /** Type to add an entry to the Environment (in the Explorer). */
        public static final String TYPE_ENVIRONMENT = "environment"; // NOI18N
        /** Type to add an entry to the Session settings. */
        public static final String TYPE_SESSION = "session"; // NOI18N


        /** type of the node */
        private String type;

        NodeSection (String name, Attributes attrs) throws IllegalModuleException {
            super(name, attrs);
            type = attrs.getValue (ModuleDescription.TAG_NODE_TYPE);
        }

        /** Get the environment node.
        * @return the node
        * @exception InstantiationException if the node could not be created
        */
        public Node getNode () throws InstantiationException {
            try {
                return (Node)super.getInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Get the node type. Determines where the node should be placed in
        * the IDE.
        * @return one of {@link #TYPE_ROOTS}, {@link #TYPE_ENVIRONMENT}, or {@link #TYPE_SESSION}, or <code>null</code> if unspecified
        */
        public String getType () {
            return type;
        }

        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processNode (this);
        }
    }

    /** Module section for a Clipboard convertor.
    * @see ExClipboard.Convertor
    */
    public static final class ClipboardConvertorSection extends ManifestSection {
        ClipboardConvertorSection (String name, Attributes attrs) throws IllegalModuleException {
            super(name, attrs);
        }

        /** Get the convertor.
        * @return the convertor
        * @exception InstantiationException if the object could not be created
        */
        public ExClipboard.Convertor getConvertor () throws InstantiationException {
            try {
                return (ExClipboard.Convertor)super.getInstance ();
            } catch (Exception ex) {
                throw new InstantiationException (ManifestSection.exceptionMessage (ex));
            }
        }

        /** Calls the right method in the iterator.
        */
        void invokeIterator (Iterator it) throws Exception {
            it.processClipboardConvertor (this);
        }
    }

}

/*
* Log
*  16   Gandalf   1.15        1/12/00  Ian Formanek    NOI18N
*  15   Gandalf   1.14        12/8/99  Petr Hamernik   compilable by Javac V8 
*       (jdk1.3)
*  14   Gandalf   1.13        11/25/99 Jesse Glick     Rewrite of 
*       LoaderPoolNode, specifically the management of loader ordering. Now 
*       permits multiple -before and -after dependencies, and should be more 
*       robust. Also made LoaderPoolItemNode's properly deletable and fixed a 
*       timing-related NullPointerException when uninstalling modules.
*  13   Gandalf   1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  12   Gandalf   1.11        10/10/99 Petr Hamernik   console debug messages 
*       removed.
*  11   Gandalf   1.10        10/4/99  Jesse Glick     Removed unused service 
*       type name.
*  10   Gandalf   1.9         9/10/99  Jaroslav Tulach Service section.
*  9    Gandalf   1.8         8/27/99  Jesse Glick     Fixed #3590.  Cleaned up 
*       random stuff in ManifestSection.  Removed deprecated call from 
*       ModuleDescription test constructor.
*  8    Gandalf   1.7         6/28/99  Jaroslav Tulach Debugger types are like 
*       Executors
*  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  6    Gandalf   1.5         5/27/99  Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         5/27/99  Jaroslav Tulach Executors rearanged.
*  4    Gandalf   1.3         5/11/99  Jesse Glick     [JavaDoc]
*  3    Gandalf   1.2         5/7/99   Jesse Glick     Module localization.
*  2    Gandalf   1.1         4/27/99  Jesse Glick     new HelpCtx () -> 
*       HelpCtx.DEFAULT_HELP.
*  1    Gandalf   1.0         4/7/99   Ian Formanek    
* $
*/
