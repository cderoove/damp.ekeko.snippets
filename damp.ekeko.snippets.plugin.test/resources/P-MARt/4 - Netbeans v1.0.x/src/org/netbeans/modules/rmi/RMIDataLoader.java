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
import java.text.MessageFormat;
import java.util.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;
import org.openide.actions.*;
import org.openide.src.*;

import org.netbeans.modules.rmi.settings.*;
import org.netbeans.modules.java.*;

/** The DataLoader for RMIDataObjects.
* This class is final only for performance reasons,
* can be happily unfinaled if desired.
*
* @author Martin Ryzl, Dafe Simonek
*/
public final class RMIDataLoader extends JavaDataLoader {

    transient private static final boolean DEBUG = false;

    /** Serial version UID. */
    static final long serialVersionUID = -3325329576021256358L;

    /** Resource bundle. */
    static final ResourceBundle bundle = NbBundle.getBundle(RMIDataLoader.class);

    /** Extension for rmi object. */
    public static final String RMI_EXTENSION = "rmi"; // NOI18N
    /** Default extension for remote implementation. */
    public static final String IMPL_SUFFIX = "Impl"; // NOI18N
    /** Inner class divider. */
    static final char INNER_CLASS_DIVIDER = '$';

    /** Yes option. */
    public static final String OPTION_YES = bundle.getString("CTL_OPTION_YES"); // NOI18N
    /** Yes to all option. */
    public static final String OPTION_YES_ALL = bundle.getString("CTL_OPTION_YES_ALL"); // NOI18N
    /** No option. */
    public static final String OPTION_NO = bundle.getString("CTL_OPTION_NO"); // NOI18N
    /** No to all option. */
    public static final String OPTION_NO_ALL = bundle.getString("CTL_OPTION_NO_ALL"); // NOI18N

    /** List of extensions recognized by this loader */
    private static ExtensionList extensions;

    static private Parsing.Listener parsingListener = null;
    static private RMISettings settings = (RMISettings) RMISettings.findObject(RMISettings.class, true);

    /** Storage for reusableDispose. Contains (FileObject, RMIDataObject) pairs. */
    static HashMap reusableSet = new HashMap(8);

    /** All objects. */
    private static WeakSet allSet = new WeakSet(64);

    /** Creates a new RMIDataLoader
     * 
     */ 
    public RMIDataLoader() {
        this (RMIDataObject.class);
    }

    /** Creates a new RMIDataLoader
     * 
     */ 
    public RMIDataLoader(Class recognizedObject) {
        super (recognizedObject);
    }

    protected void initialize () {
        setParsingListener();
        settings.addPropertyChangeListener(new SettingsListener());

        setDisplayName(bundle.getString("PROP_RMILoader_Name")); // NOI18N
        setActions (new SystemAction [] {
                        SystemAction.get(OpenAction.class),
                        SystemAction.get(CustomizeBeanAction.class),
                        SystemAction.get(FileSystemAction.class),
                        null,
                        SystemAction.get(CompileAction.class),
                        null,
                        SystemAction.get(ExecuteAction.class),
                        null,
                        SystemAction.get(CutAction.class),
                        SystemAction.get(CopyAction.class),
                        SystemAction.get(PasteAction.class),
                        null,
                        SystemAction.get(DeleteAction.class),
                        SystemAction.get (RenameAction.class),
                        null,
                        SystemAction.get(SaveAsTemplateAction.class),
                        null,
                        SystemAction.get(ToolsAction.class),
                        SystemAction.get(PropertiesAction.class),
                    });
    }

    /** For a given file finds a primary file.
    * @param fo the file to find primary file for
    *
    * @return the primary file for the file or null if the file is not
    *   recognized by this loader
    */
    protected FileObject findPrimaryFile (FileObject fo) {

        // ignore folder
        if (fo.isFolder()) return null;

        final String ext = fo.getExt(), fname = fo.getName();
        String name = null;
        FileObject primaryFO;
        boolean rmiFile = false; //
        int index;

        // only for .class, .java or *.rmi
        if (ext.equals(CLASS_EXTENSION) || ext.equals(JAVA_EXTENSION) || ext.equals(RMI_EXTENSION)) {

            // check for inner class
            if ((index = fname.indexOf(INNER_CLASS_DIVIDER)) != -1) {
                name = fname.substring(0, index);
            } else {
                if (isHideStubs()) name = checkStub(fname);
                if (name == null) name = fname;
            }
        }

        // if .rmi doesn't exist it is not an RMI object
        if ((rmiFile = (fo.getParent().getFileObject(name, RMI_EXTENSION)) != null)) {
            if ((primaryFO = fo.getParent().getFileObject(name, JAVA_EXTENSION)) != null) {
                return primaryFO;
            }
        }

        // not recognized
        return null;
    }

    /** Check stub.
    * @return name of the main file or null
    */
    static String checkStub(final String name) {
        String[] formats = settings.getStubFormats();

        for(int i = 0; i < formats.length; i++) {
            MessageFormat fmt = new MessageFormat(formats[i]);
            try {
                Object[] objs = fmt.parse(name);
                if (objs != null) return objs[0].toString();
            } catch (java.text.ParseException ex) {
                // continue
            }
        }
        return null;
    }

    /**
    */
    protected boolean isHideStubs() {
        return settings.isHideStubs();
    }

    /** Creates the right data object for given primary file.
    * It is guaranteed that the provided file is realy primary file
    * returned from the method findPrimaryFile.
    *
    * @param primaryFile the primary file
    * @return the data object for this file
    * @exception DataObjectExistsException if the primary file already has data object
    */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        RMIDataObject rmido = (RMIDataObject) reusableSet.remove(primaryFile);
        if (rmido == null) {
            rmido = new RMIDataObject(primaryFile, this);
            allSet.add(rmido);
        }
        return rmido;
    }

    /** Creates the right primary entry for given primary file.
    *
    * @param primaryFile primary file recognized by this loader
    * @return primary entry for that file
    */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new RMIFileEntry(obj, primaryFile);
    }

    /** Creates right secondary entry for given file. The file is said to
    * belong to an object created by this loader.
    *
    * @param secondaryFile secondary file for which we want to create entry
    * @return the entry
    */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        if (secondaryFile.getExt().equals(RMI_EXTENSION)) return new FileEntry(obj, secondaryFile);
        return super.createSecondaryEntry (obj, secondaryFile);
    }

    /** @return The list of extensions this loader recognizes
    * (default list constains class, ser extensions)
    */
    public ExtensionList getExtensions () {
        if (extensions == null) {
            extensions = new ExtensionList();
            extensions.addExtension(RMI_EXTENSION);
            extensions.addExtension(CLASS_EXTENSION);
            extensions.addExtension(JAVA_EXTENSION);
        }
        return extensions;
    }

    /** Sets the extension list for this data loader.
    * @param ext new list of extensions.
    */
    public void setExtensions(ExtensionList ext) {
        extensions = ext;
    }

    /** This entry defines the format for replacing the text during
    * instantiation the data object.
    */
    public class RMIFileEntry extends JavaDataLoader.JavaFileEntry {

        /** Serial version UID. */
        static final long serialVersionUID = 2704205254221237611L;

        /** Creates new JavaFileEntry */
        protected RMIFileEntry(MultiDataObject obj, FileObject file) {
            super(obj, file);
        }

        protected void modifyMap(Map map, FileObject target, String n, String e) {
            super.modifyMap(map, target, n, e);

            RMIDataObject obj = (RMIDataObject) getDataObject();
            String iName = n;
            int i;

            // interface name
            if ((i = iName.indexOf(IMPL_SUFFIX)) != -1)
                iName = iName.substring(0, i);
            else
                iName = RMIHelper.REMOTE;

            map.put("INTERFACENAME", iName);   // NOI18N
            map.put("STRING", "\""); // NOI18N

            // string versions of package and name
            String string;
            if ((string = (String) map.get("NAME")) != null) { // NOI18N
                map.put("NAMESTRING", "\"" + string + "\""); // NOI18N
            }
            if ((string = (String) map.get("PACKAGE")) != null) { // NOI18N
                map.put("PACKAGESTRING", "\"" + string + "\""); // NOI18N
            }
            if ((string = getFSPath(getFile())) != null) {
                map.put("FSPATH", string); // NOI18N
            }
        }
    }

    /** Returns path to the filesystem of the file object.
    * @return path to the filesystem*/
    public static String getFSPath(FileObject fo) {
        final StringBuffer sb = new StringBuffer();

        // getPackage
        try {
            FileSystem fs = fo.getFileSystem();
            fo.getFileSystem().prepareEnvironment(new FileSystem.Environment() {
                                                      public void addClassPath(String element) {
                                                          sb.append(element);
                                                      }
                                                  });
            return sb.toString();
        } catch (FileStateInvalidException ex) {
        } catch (EnvironmentNotSupportedException ex) {
        }
        return null;
    }

    /** Mark JDO as RMI.
    * @param jdo - data object to mark
    * @param set - if true, set it as RMI, if false, unset
    */
    public static void markRMI(JavaDataObject jdo, boolean set) throws IOException, java.beans.PropertyVetoException {
        try {
            if (jdo == null) return;
            FileObject fo = jdo.getPrimaryFile();
            if (set) {
                try {
                    // mark it!
                    fo.getParent().createData(fo.getName(), RMI_EXTENSION);
                } catch (Exception ex) {
                    // ignore it
                }
                if (!(jdo instanceof RMIDataObject)) {
                    if (FileUtil.findBrother(fo, RMI_EXTENSION) != null) jdo.setValid(false);
                }
            } else {
                if (jdo instanceof RMIDataObject) {
                    FileLock lock = null;
                    try {
                        FileObject rmi = FileUtil.findBrother(fo, RMI_EXTENSION);
                        lock = rmi.lock();
                        rmi.delete(lock);
                    } catch (Exception ex) {
                        // ignore it
                    } finally {
                        if (lock != null) lock.releaseLock();
                    }
                    if (FileUtil.findBrother(fo, RMI_EXTENSION) == null) jdo.setValid(false);
                }
            }
        } catch (java.beans.PropertyVetoException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /** Add parsing listener.
    */
    protected static void setParsingListener() {
        boolean isEnabled = settings.isDetectRemote();
        if (isEnabled) {
            if (parsingListener == null) {
                parsingListener = new ParsingListener();
                Parsing.addParsingListener(parsingListener);
            }
        } else { // not enabled
            if (parsingListener != null) {
                Parsing.removeParsingListener(parsingListener);
                parsingListener = null;
            }
        }
    }

    /** The listener interface for everybody who want to control all
    * parsed JavaDataObjects.
    */
    protected static class ParsingListener implements Parsing.Listener {

        volatile NotifyDescriptor nd = null;
        final MessageFormat format = new MessageFormat(bundle.getString("FMT_CONVERT_MESSAGE")); // NOI18N
        final Object[] params = new Object[1];

        /** Method which is called everytime when some object is parsed.
        * @param evt The event with the details.
        */
        public void objectParsed(final Parsing.Event evt) {
            RequestProcessor.postRequest(new Runnable() {
                                             public void run() {
                                                 try {
                                                     // dont forget evt until everything is done !
                                                     final JavaDataObject jdo = evt.getJavaDataObject();
                                                     // only non RMI
                                                     if (jdo instanceof RMIDataObject) return;
                                                     SourceElement se = jdo.getSource();
                                                     final ClassElement ce = se.getClass(Identifier.create(jdo.getPrimaryFile().getPackageName('.')));
                                                     if (ce.isClass()) {
                                                         if (RMIHelper.implementsClass(ce, RMIHelper.REMOTE)) {
                                                             // ask user if necessary, eventually modify settings
                                                             if (getConfirm()) {
                                                                 RequestProcessor.postRequest(new Runnable() {

                                                                                                  public void run() {
                                                                                                      try {
                                                                                                          Object result = TopManager.getDefault().notify(getDescriptor(ce));
                                                                                                          if (result == OPTION_YES) {
                                                                                                              markRMI(jdo, true);
                                                                                                          }
                                                                                                          if (result == OPTION_YES_ALL) {
                                                                                                              markRMI(jdo, true);
                                                                                                              setConfirm(false);
                                                                                                          }
                                                                                                          if (result == OPTION_NO_ALL) {
                                                                                                              setDetect(false);
                                                                                                          }
                                                                                                      } catch (Exception ex) {
                                                                                                          TopManager.getDefault().notifyException(ex);
                                                                                                      }
                                                                                                  }

                                                                                              });
                                                             } else {
                                                                 // confirm without asking
                                                                 try {
                                                                     markRMI(jdo, true);
                                                                 } catch (Exception ex) {
                                                                     TopManager.getDefault().notifyException(ex);
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 } catch (NullPointerException ex) {
                                                     // ignore it
                                                 }
                                             }
                                         });
        }

        /** Getter for confirm.
        * @return true if the action should be confirmed.
        */
        protected boolean getConfirm() {
            return settings.isConfirmConvert();
        }

        /** Setter for confirm.
        * @param confirm if true, subsequent actions should be confirmed
        */
        protected void setConfirm(boolean confirm) {
            settings.setConfirmConvert(confirm);
        }

        /** Setter for detect.
        * @param detect if true, detect all parsing events and check for implements
        *               Remote
        */
        protected void setDetect(boolean detect) {
            settings.setDetectRemote(detect);
        }

        /** Get notify descriptor.
        * @return a NotifyDesriptor for the confirmation dialog
        */
        protected NotifyDescriptor getDescriptor(ClassElement ce) {
            if (nd == null) {
                nd = new NotifyDescriptor(
                         "",   // NOI18N
                         bundle.getString("CTL_CONFIRMATION_TITLE"), // NOI18N
                         NotifyDescriptor.DEFAULT_OPTION,
                         NotifyDescriptor.QUESTION_MESSAGE,
                         new Object[] { OPTION_YES, OPTION_YES_ALL, OPTION_NO, OPTION_NO_ALL },
                         null
                     );
            }
            params[0] = ce.getName().getFullName();
            nd.setMessage(
                format.format(params)
            );
            return nd;
        }
    }

    /** Listens on RMISettings. */
    private static class SettingsListener implements java.beans.PropertyChangeListener {
        public void propertyChange(final java.beans.PropertyChangeEvent ev) {
            if (RMISettings.PROP_HIDE_STUBS.equals(ev.getPropertyName())) {
                RequestProcessor.postRequest(new HideStubChanger());
            }
            if (RMISettings.PROP_DETECT_REMOTE.equals(ev.getPropertyName())) {
                setParsingListener();
            }
        }
    }

    /** Update all RMI objects. */
    private static class HideStubChanger implements Runnable {
        public void run() {
            final boolean hideStubs = settings.isHideStubs();
            Set set = new HashSet(allSet);  // clone
            Iterator it = set.iterator();
            while (it.hasNext()) {
                RMIDataObject rmido = (RMIDataObject) it.next();
                if (rmido != null) {
                    if (hideStubs) rmido.aquireStubs();
                    else rmido.dropStubs();
                }
            }
        }
    }
}

/*
 * <<Log>>
 *  33   Gandalf-post-FCS1.29.1.2    4/17/00  Martin Ryzl     data loader now ignores 
 *       folders
 *  32   Gandalf-post-FCS1.29.1.1    3/20/00  Martin Ryzl     localization
 *  31   Gandalf-post-FCS1.29.1.0    3/8/00   Martin Ryzl     hide stubs feature
 *  30   src-jtulach1.29        1/28/00  Martin Ryzl     one semicolon removed 
 *       (problems with compilation by fastjavac)
 *  29   src-jtulach1.28        1/25/00  Martin Ryzl     objects recognitiot 
 *       fixed
 *  28   src-jtulach1.27        1/24/00  Martin Ryzl     compilation of inner 
 *       classes added
 *  27   src-jtulach1.26        1/5/00   Jaroslav Tulach Change in notify 
 *       descriptor.
 *  26   src-jtulach1.25        11/27/99 Patrik Knakal   
 *  25   src-jtulach1.24        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   src-jtulach1.23        10/15/99 Martin Ryzl     BUG #4519 fixed
 *  23   src-jtulach1.22        10/13/99 Petr Kuzel      HOTFIXed due to compile 
 *       errs
 *  22   src-jtulach1.21        10/13/99 Martin Ryzl     ugly deadlock in 
 *       ParsingListener removed
 *  21   src-jtulach1.20        10/13/99 Martin Ryzl     markRMI corrected
 *  20   src-jtulach1.19        10/12/99 Martin Ryzl     debug info removed
 *  19   src-jtulach1.18        10/12/99 Martin Ryzl     Automatic detection of 
 *       RMI
 *  18   src-jtulach1.17        10/1/99  Jaroslav Tulach Loaders extends 
 *       SharedClassObject
 *  17   src-jtulach1.16        8/31/99  Ian Formanek    Correctly provides 
 *       FileSystemAction on its data objects
 *  16   src-jtulach1.15        7/27/99  Martin Ryzl     
 *  15   src-jtulach1.14        7/20/99  Martin Ryzl     
 *  14   src-jtulach1.13        6/9/99   Ian Formanek    ToolsAction
 *  13   src-jtulach1.12        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  12   src-jtulach1.11        5/12/99  Martin Ryzl     
 *  11   src-jtulach1.10        5/5/99   Martin Ryzl     
 *  10   src-jtulach1.9         5/4/99   Martin Ryzl     
 *  9    src-jtulach1.8         4/23/99  Martin Ryzl     debug info removed
 *  8    src-jtulach1.7         4/21/99  Martin Ryzl     
 *  7    src-jtulach1.6         4/20/99  Martin Ryzl     
 *  6    src-jtulach1.5         4/15/99  Martin Ryzl     
 *  5    src-jtulach1.4         4/15/99  Martin Ryzl     
 *  4    src-jtulach1.3         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  3    src-jtulach1.2         3/23/99  Martin Ryzl     
 *  2    src-jtulach1.1         3/19/99  Martin Ryzl     
 *  1    src-jtulach1.0         3/17/99  David Simonek   
 * $
 */
