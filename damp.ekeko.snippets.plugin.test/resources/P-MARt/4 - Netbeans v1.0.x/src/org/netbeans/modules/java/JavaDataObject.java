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
import java.util.*;
import java.beans.*;

import javax.swing.text.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.TopManager;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.Task;
import org.openide.actions.OpenAction;
import org.openide.text.EditorSupport;
import org.openide.execution.NbClassLoader;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.ExternalCompiler;
import org.openide.src.*;
import org.openide.src.nodes.SourceChildren;
import org.openide.src.nodes.SourceElementFilter;
import org.openide.src.nodes.ElementNodeFactory;
import org.openide.src.nodes.FilterFactory;
import org.openide.util.actions.SystemAction;
import org.openide.util.TaskListener;
import org.openide.util.WeakListener;
import org.openide.util.Utilities;

import org.netbeans.modules.java.settings.JavaSettings;
import org.netbeans.modules.java.settings.ExternalCompilerSettings;

/** Data object representing a Java source file, perhaps with associated class files.
* May be subclassed.
* @author Petr Hamernik, Jaroslav Tulach, Ian Formanek
*/
public class JavaDataObject extends MultiDataObject implements ElementCookie {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6035788991669336965L;

    public static final byte CONNECT_NOT = 0;
    public static final byte CONNECT_CONFIRM = 1;
    public static final byte CONNECT_AUTO = 2;

    public static final byte CONNECT_DEFAULT = CONNECT_CONFIRM;

    private static final String EA_CONNECTION = "SourceSync"; // NOI18N

    private static final String[] SKIP_REGISTRATION_PREFIX = { "java.", "javax."}; // NOI18N

    transient protected JavaEditor editorSupport;
    transient protected ExecSupport execSupport;
    transient protected ConnectionSupport connectionSupport;

    transient private JavaInstanceSupport instanceSupport;

    transient protected SourceElement sourceElement;
    transient protected AbstractNode alteranteParent;
    transient SourceElementImpl sourceElementImpl;
    transient private FileChangeListener fileChangeListener;
    transient private FileChangeListener changeListenerHook;

    private static JavaSettings jopts;
    private static ExternalCompilerSettings ecopts;

    /** Create new data object.
    * @param pf primary file object for this data object (i.e. the source file)
    * @param loader the associated loader
    */
    public JavaDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
        init();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    private void init() {
        MultiDataObject.Entry entry = getPrimaryEntry();
        CookieSet cookies = getCookieSet();

        editorSupport = createJavaEditor ();
        cookies.add(editorSupport);

        sourceElementImpl = new SourceElementImpl(this);
        sourceElement = new SourceElement(sourceElementImpl);

        execSupport = new JavaExecSupport(entry);

        cookies.add(execSupport);

        cookies.add(new JCompilerSupport.Compile(entry));
        cookies.add(new JCompilerSupport.Build(entry));
        cookies.add(new JCompilerSupport.Clean(entry));

        connectionSupport = new ConnectionSupport(entry,
                            new ConnectionCookie.Type[] { new JavaConnections.Type(JavaConnections.TYPE_ALL)});

        cookies.add(connectionSupport);
        cookies.add(connectionListener);

        instanceSupport = new JavaInstanceSupport(entry);
        updateInstanceCookie();

        cookies.addChangeListener(new ChangeListener() {
                                      public void stateChanged(ChangeEvent evt) {
                                          sourceElementImpl.fireCookiesChange();
                                      }
                                  });

        addPropertyChangeListener(new PropertyChangeListener() {
                                      public void propertyChange(PropertyChangeEvent evt) {
                                          String propName = evt.getPropertyName();
                                          if ((propName == null) || (propName.equals(PROP_FILES))) {
                                              updateInstanceCookie();
                                          }
                                      }
                                  });

	fileChangeListener = WeakListener.fileChange(changeListenerHook = new FileChangeAdapter() {
                                                  public void fileChanged(FileEvent e) {
                                                      if (!editorSupport.isDocumentLoaded()) {
                                                          SourceElementImpl src = getSourceElementImpl();
                                                          if (src.dataRef.get() != null) {
                                                              src.setDirty(true);
                                                              src.prepare();
                                                          }
                                                      }
                                                  }
	}, entry.getFile());

        addSourceChangeListener(null);
    }

    void updateInstanceCookie() {
        CookieSet cookies = getCookieSet();

        FileObject clazz = Util.findFile(getPrimaryFile(), JavaDataLoader.CLASS_EXTENSION);
        JavaInstanceSupport prev = (JavaInstanceSupport) cookies.getCookie(InstanceCookie.class);

        if ((prev == null) && (clazz != null)) {
            cookies.add(instanceSupport);
        }
        else if ((prev != null) && (clazz == null)) {
            cookies.remove(instanceSupport);
        }
    }
    
    /** Attaches a file change listener to the primary (source) file.
     * Optionally removes the listener from previously used file object.
     * @param previousPrimary if not null, then the method removes change listener from this
     *        file object.
     */
    void addSourceChangeListener(FileObject previousPrimary) {
      if (previousPrimary != null) {
        previousPrimary.removeFileChangeListener(fileChangeListener);
      }
      getPrimaryEntry().getFile().addFileChangeListener(fileChangeListener);
    }

    // =================== Connections ====================================

    public byte getSynchronizationType() {
        Object o = getPrimaryFile().getAttribute(EA_CONNECTION);
        return ((o != null) && (o instanceof Byte)) ?
               ((Byte)o).byteValue() : CONNECT_DEFAULT;
    }

    public void setSynchronizationType(byte type) {
        FileObject fo = getPrimaryFile();
        try {
            if ((type != CONNECT_CONFIRM) && (type != CONNECT_NOT) && (type != CONNECT_AUTO))
                throw new IllegalArgumentException();
            fo.setAttribute(EA_CONNECTION, (type == CONNECT_DEFAULT) ? null : new Byte(type));
        }
        catch (IOException e) {
        }
    }

    int getAllListenersMask() {
        if (!JavaConnections.SETTINGS.isEnabled()) // disabled
            return 0;

        Iterator it = connectionSupport.getRegisteredTypes().iterator();
        int mask = 0;
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof JavaConnections.Type) {
                mask |= ((JavaConnections.Type)o).getFilter();
            }

            // temporary-begin-------
            mask |= JavaConnections.IMPLEMENTS.getFilter();
            // end ------------------

        }
        return mask;
    }

    private ConnectionCookie.Listener connectionListener = new ConnectionCookie.Listener() {
                public void notify(final ConnectionCookie.Event event) throws IllegalArgumentException, ClassCastException {
                    JavaConnections.Event javaEvt = null;
                    boolean shouldPerform = false;

                    if (event instanceof JavaConnections.Event) {
                        javaEvt = (JavaConnections.Event)event;
                        shouldPerform = javaEvt.getType().overlaps(
                                            new JavaConnections.Type(JavaConnections.TYPE_SOURCE_CHECK_DEEP | JavaConnections.TYPE_SOURCE_CHECK_SELF));
                    }

                    if (!JavaConnections.SETTINGS.isEnabled() && !shouldPerform)
                        return;
                    // Sync is rejected if synchronization is disabled and the event is not an explicit
                    // request for self-check.
                    if ((getSynchronizationType() == CONNECT_NOT) && !shouldPerform)
                        return;

                    SourceElementImpl.PARSING_RP.post(new Runnable() {
                                                          public void run() {
                                                              LinkedList changeProcessors = new LinkedList();
                                                              if (connectionNotify(event, changeProcessors)) {
                                                                  // PEDNING
                                                                  // this should be changed - asynchronous starting connectionNotify method can't
                                                                  // throw IllegalArgumentException to the notify method.

                                                                  //            throw new IllegalArgumentException();
                                                              }
                                                              startChangeProcessors(changeProcessors);
                                                          }
                                                      });
                }
            };

    void startChangeProcessors(LinkedList changeProcessors) {
        if (changeProcessors.size() > 0) {
            if (getSynchronizationType() == CONNECT_AUTO) {
                Iterator it = changeProcessors.iterator();
                while (it.hasNext()) {
                    JavaConnections.ChangeProcessor p = (JavaConnections.ChangeProcessor) it.next();
                    try {
                        p.process();
                    } catch (SourceException e) {
                        // eat the exception
                        // TopManager.getDefault().notify(new NotifyDescriptor.Exception(e));
                    }
                }
            }
            else { // CONNECT_CONFIRM
                byte oldSynch = getSynchronizationType();
                byte newSynch = JavaConnections.showChangesDialog(changeProcessors, oldSynch);
                if (oldSynch != newSynch)
                    setSynchronizationType(newSynch);
            }
        }
    }

    protected boolean connectionNotify(ConnectionCookie.Event event, LinkedList changeProcessors) {
        if (event.getType().getEventClass().isAssignableFrom(JavaConnections.Event.class)) {
            // there are two main sources of notification: external notifications that something this source
            // depends on has changed, and explicit notifications that this source should check itself for changes.
            JavaConnections.Event evt = (JavaConnections.Event)event;
            boolean selfcheck = evt.getType().overlaps(new JavaConnections.Type(JavaConnections.TYPE_SOURCE_CHECK_DEEP | JavaConnections.TYPE_SOURCE_CHECK_SELF));
            if (selfcheck) {
                return InterfaceConnection.sourceCheck(changeProcessors, sourceElementImpl);
            } else {
                return InterfaceConnection.synchronizeInterfaces((JavaConnections.Event) event, changeProcessors, sourceElementImpl);
            }
        }
        return false;
    }

    protected void registerForName(Identifier id, JavaConnections.Type type) {
        registration(id, type, true);
    }

    protected void unregisterForName(Identifier id, JavaConnections.Type type) {
        registration(id, type, false);
    }

    private void registration(final Identifier id, final JavaConnections.Type type, final boolean register) {
        // check the excludes - the interfaces which are supposed to be stable ("java.*" and "javax.*") // NOI18N
        final String name = id.getFullName();
        for (int i = 0; i < SKIP_REGISTRATION_PREFIX.length; i++) {
            if (name.startsWith(SKIP_REGISTRATION_PREFIX[i]))
                return;
        }

        SourceElementImpl.PARSING_RP.post(new Runnable() {
                                              public void run() {
                                                  try {
                                                      ClassElement sourceClass = ClassElement.forName(name);
                                                      if (sourceClass == null)
                                                          return;

                                                      ConnectionCookie cookie = (ConnectionCookie)sourceClass.getCookie(ConnectionCookie.class);
                                                      if (cookie == null)
                                                          return;

                                                      if (register)
                                                          cookie.register(type, getNodeDelegate());
                                                      else
                                                          cookie.unregister(type, getNodeDelegate());
                                                  }
                                                  catch (Exception e) {
                                                      if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                                          e.printStackTrace();
                                                      }
                                                  }
                                              }
                                          });
    }

    /** Set whether this object is considered modified.
    * @param modif <code>true</code> to consider it modified
    */
    public void setModified(boolean modif) {
        super.setModified(modif);
        if (modif)
            sourceElementImpl.setDirty(modif);
    }

    /** Starts the task which update source element
    * depending on the new name and location of the JavaDataObject.
    * @param jdo The javadataobject which should be updated
    * @param dest The current primary file object
    * @param oldName The previous name of the object
    * @param save If document should be saved after updating
    */
    static void updateSourceStart(final JavaDataObject jdo,
                                  final FileObject dest,
                                  final String oldName, final boolean save) {
        Task task = jdo.getSourceElementImpl().prepare();
        task.addTaskListener(new TaskListener() {
                                 public void taskFinished(Task t) {
                                     updateSource(jdo, dest, oldName, save);
                                 }
                             });
    }

    /** Update package and name of mail class in the source
    * depending on the new name and location of the JavaDataObject.
    * @param jdo The javadataobject which should be updated
    * @param dest The current primary file object
    * @param oldName The previous name of the object
    * @param save If document should be saved after updating
    */
    static void updateSource(JavaDataObject jdo, FileObject dest, String oldName, boolean save) {
        try {
	    if (!jdo.isValid() || !dest.isValid()) {
		return;
	    }
            SourceElementImpl impl = jdo.getSourceElementImpl();	    
            boolean wasLoaded = jdo.editorSupport.isDocumentLoaded();

            // update package
            FileObject packFO = dest.getParent();
            Identifier oldPackage = impl.getPackage();
            Identifier newPackage = (packFO.isRoot()) ? null : Identifier.create(packFO.getPackageName('.'));
            if ((oldPackage != newPackage) ||
                    (oldPackage != null) && !oldPackage.equals(newPackage)) {
                impl.setPackage(newPackage);
            }

            // update class rename
            ClassElement[] classes = impl.getClasses();
            if (!dest.getName().equals(oldName)) {
                for (int i = 0; i < classes.length; i++) {
                    if (classes[i].getName().getName().equals(oldName)) {
                        StringBuffer fullName = new StringBuffer(packFO.getPackageName('.'));
                        if (fullName.length() > 0)
                            fullName.append('.');
                        fullName.append(dest.getName());
                        classes[i].setName(Identifier.create(fullName.toString(), dest.getName()));
                        break;
                    }
                }
	    }
            if (save) {
                SaveCookie savec = (SaveCookie) jdo.getCookie(SaveCookie.class);
                if (savec != null) {
                    savec.save();
                }
                if (!wasLoaded) {
                    jdo.editorSupport.close();
                }
            }
        }
        catch (SourceException e) {
        }
        catch (IOException e) {
        }
    }

    // ==================== Handle methods =======================
    
    /* Copies primary and secondary files to new folder.
     * May ask for user confirmation before overwriting.
     * @param df the new folder
     * @return data object for the new primary
     * @throws IOException if there was a problem copying
     * @throws UserCancelException if the user cancelled the copy
    */
    protected synchronized DataObject handleCopy (final DataFolder df) throws IOException {
        DataObject obj = super.handleCopy(df);
        if (obj instanceof JavaDataObject) {
            String oldName = getPrimaryFile().getName();
            JavaDataObject jdo = (JavaDataObject)obj;
            updateSourceStart(jdo, jdo.getPrimaryFile(), oldName, true);
        }
        return obj;
    }

    /* Renames all entries and changes their files to new ones.
    */
    protected FileObject handleRename (String name) throws IOException {
        if (!Utilities.isJavaIdentifier(name))
            throw new IOException(Util.getString("MSG_Not_Valid_FileName"));

        boolean prevModif = isModified();
        FileObject oldFo = getPrimaryFile();
        String oldName = oldFo.getName();
	FileObject fo = super.handleRename(name);
	addSourceChangeListener(oldFo);
        updateSourceStart(this, fo, oldName, !prevModif);
        return fo;
    }

    /* Moves primary and secondary files to a new folder.
     * May ask for user confirmation before overwriting.
     * @param df the new folder
     * @return the moved primary file object
     * @throws IOException if there was a problem moving
     * @throws UserCancelException if the user cancelled the move
    */
    protected FileObject handleMove (DataFolder df) throws IOException {
        boolean prevModif = isModified();
        FileObject oldFo = getPrimaryFile();
        String oldName = oldFo.getName();
        FileObject fo = super.handleMove(df);
        addSourceChangeListener(oldFo);
        updateSourceStart(this, fo, oldName, !prevModif);
        return fo;
    }

    /* Creates new object from template.
    * @exception IOException
    */
    protected DataObject handleCreateFromTemplate (DataFolder df, String name) throws IOException {
        if (!Utilities.isJavaIdentifier(name))
            throw new IOException(Util.getString("MSG_Not_Valid_FileName"));

        DataObject obj = super.handleCreateFromTemplate(df, name);
        if (obj instanceof JavaDataObject) {
            String oldName = getPrimaryFile().getName();
            JavaDataObject jdo = (JavaDataObject)obj;
            updateSourceStart(jdo, jdo.getPrimaryFile(), oldName, true);
        }
        return obj;
    }

    /** Create the editor support for this data object.
    * By default, creates a <code>JavaEditor</code> with the source file entry;
    * subclasses may override this.
    * @return the editor support
    */
    protected JavaEditor createJavaEditor () {
        return new JavaEditor (getPrimaryEntry ());
    }

    /* Help context for this object.
    * @return help context
    */
    public org.openide.util.HelpCtx getHelpCtx () {
        return new org.openide.util.HelpCtx (JavaDataObject.class);
    }

    /** Provide node that should represent this data object.
    * This implementation creates and returns a {@link JavaNode}.
    * Subclasses may wish to return a specialized subclass of this node type.
    * You should probably make the default action be {@link OpenAction}.
    * @return the node representation for this data object
    */
    protected Node createNodeDelegate () {
        JavaNode node = new JavaNode (this);
        node.setDefaultAction (SystemAction.get (OpenAction.class));
        return node;
    }

    /** Get the parsed representation of this source file.
    * May not be fully parsed yet; the source element itself indicates its status.
    * @return the source element for this Java source file
    */
    public SourceElement getSource() {
        return sourceElement;
    }

    /**
    * @return the SourceElementImpl for this java source.
    */
    SourceElementImpl getSourceElementImpl() {
        return sourceElementImpl;
    }

    /** Get the current editor support.
    * Ought not be subclasses; use {@link #createJavaEditor}.
    * @return the editor support
    */
    public JavaEditor getJavaEditor() {
        return editorSupport;
    }

    public ConnectionSupport getConnectionSupport() {
        return connectionSupport;
    }

    private static JavaSettings getJavaSettings() {
        if (jopts == null) {
            jopts = (JavaSettings) JavaSettings.findObject(JavaSettings.class, true);
        }
        return jopts;
    }

    private static ExternalCompilerSettings getExternalCompilerSettings() {
        if (ecopts == null) {
            ecopts = (ExternalCompilerSettings) ExternalCompilerSettings.findObject(ExternalCompilerSettings.class, true);
        }
        return ecopts;
    }

    /** Create a compiler handling Java source files.
    * Subclasses requiring special compilation support may override this
    * (or may provide a {@link org.openide.compiler.Compiler.Manager} to do so).
    * Or, subclasses may override it to retrieve the default compiler,
    * then create postprocessors depending on it.
    * @param job the job to add to
    * @param type the class of compilation (e.g. <code>CompileCookie.Compile</code>)
    * @return a compiler to handle this file
    */
    protected Compiler createCompiler(CompilerJob job, Class type) {
        return null;
    }

    /** Create a compiler handling Java source files.
    *
    *
    * @param job the job to add to
    * @param type the class of compilation (e.g. <code>CompileCookie.Compile</code>)
    * @return a compiler to handle this file
    */
    public static Compiler createCompilerForFileObject(Compiler[] dependencies,
            CompilerJob job,
            FileObject fo,
            Class type) {
        return null;
    }

    /** Remove a secondary entry from the list. Access method
     * @param fe the entry to remove
    */
    final void removeSecondaryEntryAccess(Entry fe) {
        removeSecondaryEntry(fe);
    }

    // =============== implementation of ElementCookie ========================

    /*
     * Get the alternate node representation.
     * @return the node
     * @see org.openide.loaders.DataObject#getNodeDelegate
    */
    public Node getElementsParent () {
        if (alteranteParent != null) return alteranteParent;
        synchronized (this) {
            if (alteranteParent != null) return alteranteParent;

            /* Changed for multiple factories
            JavaElementNodeFactory cef = new JavaElementNodeFactory ();
            cef.setGenerateForTree (true);
            */
            ElementNodeFactory cef = getBrowserFactory();
            SourceChildren sourceChildren = new SourceChildren (cef);
            SourceElementFilter sourceElementFilter = new SourceElementFilter();
            sourceElementFilter.setAllClasses (true);
            sourceChildren.setFilter (sourceElementFilter);
            sourceChildren.setElement (getSource ());

            alteranteParent = new AbstractNode (sourceChildren);
            CookieSet cs = alteranteParent.getCookieSet();
            cs.add (sourceChildren);
            return alteranteParent;
        }
    }

    // =============== The mechanism for regeisteing node factories ==============

    private static ArrayList explorerFactories = new ArrayList();

    /**
     * @associates JavaElementNodeFactory 
     */
    private static ArrayList browserFactories = new ArrayList();

    static {
        explorerFactories.add( JavaElementNodeFactory.DEFAULT );

        JavaElementNodeFactory cef = new JavaElementNodeFactory();
        cef.setGenerateForTree (true);

        browserFactories.add( cef ) ;
    }

    public static void addExplorerFilterFactory( FilterFactory factory ) {
        addFactory( explorerFactories, factory );
    }

    public static void removeExplorerFilterFactory( FilterFactory factory ) {
        removeFactory( explorerFactories, factory );
    }

    static ElementNodeFactory getExplorerFactory( ) {
        return (ElementNodeFactory)explorerFactories.get( explorerFactories.size() - 1);
    }

    public static void addBrowserFilterFactory( FilterFactory factory ) {
        addFactory( browserFactories, factory );
    }

    public static void removeBrowserFilterFactory( FilterFactory factory ) {
        removeFactory( browserFactories, factory );
    }

    static ElementNodeFactory getBrowserFactory() {
        return (ElementNodeFactory)browserFactories.get( browserFactories.size() - 1 );
    }

    private static synchronized void addFactory( List factories, FilterFactory factory ) {
        factory.attachTo( (ElementNodeFactory)factories.get( factories.size() - 1 ) );
        factories.add( factory );
    }

    private static synchronized void removeFactory( List factories, FilterFactory factory ) {
        int index = factories.indexOf( factory );

        if ( index <= 0 )
            return;
        else if ( index == factories.size() - 1 )
            factories.remove( index );
        else {
            ((FilterFactory)factories.get( index + 1 )).attachTo( (ElementNodeFactory)factories.get( index - 1 ) );
            factories.remove( index );
        }

    }

    /*
    public boolean isTemplate() {
      if (!super.isTemplate()) {
        return false;
      }
      if (isTemplateHelper != -1) {
        return isTemplateHelper == 0 ? false : true;
      }

      FileObject primary = getPrimaryFile();
      String ext = primary.getExt();
      FileObject parent = getPrimaryFile().getParent();
      FileObject fo = null;
      String current;

      current = primary.getName() + '_' + java.util.Locale.getDefault().toString();

      int lastUnderbar = current.lastIndexOf('_');
      while (lastUnderbar >= 1) {
        fo = parent.getFileObject(current, ext);
        if (fo != null) {
          break;
        }
        current = current.substring(0, lastUnderbar);
        lastUnderbar = current.lastIndexOf('_');
      }

      if (fo == null || fo == primary) {
        isTemplateHelper = 1;
      } else {
        isTemplateHelper = 0;
      }
      return isTemplateHelper == 0 ? false : true;
}
    */

    // =============== Instance ==========================

    static class JavaInstanceSupport implements InstanceCookie {
        MultiDataObject.Entry entry;

        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public JavaInstanceSupport(MultiDataObject.Entry entry) {
            this.entry = entry;
        }

        /* The class of the instance represented by this cookie.
        * Can be used to test whether the instance is of valid
        * class before it is created.
        *
        * @return the class of the instance
        * @exception IOException an I/O error occured
        * @exception ClassNotFoundException the class has not been found
        */
        public Class instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            try {
                // find class by class loader
                Class clazz = findClass (instanceName ());
                if (clazz == null)
                    throw new ClassNotFoundException ();
                return clazz;
            }
            catch (Throwable t) {
                if (t instanceof IOException)
                    throw (IOException) t;
                else if (t instanceof ClassNotFoundException)
                    throw (ClassNotFoundException) t;
                else if (t instanceof RuntimeException)
                    throw (RuntimeException) t;
                else if (t instanceof ThreadDeath)
                    throw (ThreadDeath) t;
                else
                    // turn other throwables into class not found ex.
                    throw new ClassNotFoundException(t.getMessage());
            }
        }

        /* The bean name for the instance.
        * @return the name for the instance
        */
        public String instanceName () {
            return entry.getFile().getPackageName ('.');
        }

        /*
        * @return an object to work with
        * @exception IOException an I/O error occured
        * @exception ClassNotFoundException the class has not been found
        */
        public Object instanceCreate ()
        throws java.io.IOException, ClassNotFoundException {
            try {
                // create new instance
                return instanceClass ().newInstance ();
            }
            catch (IOException ex) {
                throw ex;
            }
            catch (ClassNotFoundException ex) {
                throw ex;
            }
            catch (RuntimeException ex) {
                throw ex;
            }
            catch (ThreadDeath t) {
                throw t;
            }
            catch (Throwable t) {
                // turn other throwables into class not found ex.
                throw new ClassNotFoundException(t.getMessage());
            }
        }

        /** Finds a class for given name.
        * @param name name of the class
        * @return the class for the name
        * @exception ClassNotFoundException if the class cannot be found
        */
        private Class findClass (String name) throws ClassNotFoundException {
            try {
                return Class.forName(name, true, TopManager.getDefault().currentClassLoader());
            }
            catch (ClassNotFoundException ex) {
                throw ex;
            }
            catch (RuntimeException ex) {
                throw ex;
            }
            catch (ThreadDeath t) {
                throw t;
            }
            catch (Throwable t) {
                // turn other throwables into class not found ex.
                throw new ClassNotFoundException (t.getMessage ());
            }
        }
    }
}


/*
 * Log
 *  85   Gandalf-post-FCS1.80.1.3    4/17/00  Svatopluk Dedic Sources are saved/closed
 *       soon after programmatic changes; bulk cross-package fixed.
 *  84   Gandalf-post-FCS1.80.1.2    4/14/00  Svatopluk Dedic Listens for file changes
 *       when not opened as document and hierarchy is displayed.
 *  83   Gandalf-post-FCS1.80.1.1    3/27/00  Svatopluk Dedic SourceExceptions ignored
 *       during automatic synchronization
 *  82   Gandalf-post-FCS1.80.1.0    3/6/00   Svatopluk Dedic 
 *  81   Gandalf   1.80        2/15/00  Svatopluk Dedic Debug output commented 
 *       out
 *  80   Gandalf   1.79        2/14/00  Svatopluk Dedic 
 *  79   Gandalf   1.78        1/15/00  Petr Hamernik   fixed #3964
 *  78   Gandalf   1.77        1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  77   Gandalf   1.76        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  76   Gandalf   1.75        1/9/00   Petr Hamernik   fixed 3262
 *  75   Gandalf   1.74        1/9/00   Petr Hamernik   fixed 4859
 *  74   Gandalf   1.73        1/8/00   Petr Hamernik   fixed 2829
 *  73   Gandalf   1.72        12/20/99 Ales Novak      JavaDO has new 
 *       properties - default executor and default debugger
 *  72   Gandalf   1.71        11/9/99  Ales Novak      CoronaEnvironment kept 
 *       through WeakReference
 *  71   Gandalf   1.70        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  70   Gandalf   1.69        10/7/99  Petr Hamernik   fixed bugs #2946, #3621 
 *       - cookies changes of JavaDO (e.g.save) is propagated to all 
 *       ElementNodes
 *  69   Gandalf   1.68        10/7/99  Petr Hamernik   Java module has its own 
 *       RequestProcessor for source parsing.
 *  68   Gandalf   1.67        9/30/99  Petr Hamernik   dispose removed
 *  67   Gandalf   1.66        9/29/99  Ales Novak      CompilerType used
 *  66   Gandalf   1.65        9/10/99  Petr Hamernik   skip synchronization 
 *       with java.* and javax.*  interfaces
 *  65   Gandalf   1.64        8/6/99   Petr Hamernik   Working with threads 
 *       improved
 *  64   Gandalf   1.63        8/5/99   Ales Novak      CompilerSupport.Clean 
 *       cookie added
 *  63   Gandalf   1.62        7/23/99  Petr Hamernik   dispose() method added
 *  62   Gandalf   1.61        7/23/99  Petr Hamernik   java connection changes
 *  61   Gandalf   1.60        7/9/99   Petr Hrebejk    Add/Remove factory 
 *       methods made synchronized
 *  60   Gandalf   1.59        7/8/99   Petr Hamernik   changes reflecting 
 *       org.openide.src changes
 *  59   Gandalf   1.58        7/3/99   Petr Hamernik   SourceCookie.Editor - 
 *       1st version
 *  58   Gandalf   1.57        6/30/99  Ales Novak      CORBA support
 *  57   Gandalf   1.56        6/28/99  Petr Hrebejk    Multiple node factories 
 *       added
 *  56   Gandalf   1.55        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  55   Gandalf   1.54        6/22/99  Petr Hamernik   connecting to interfaces
 *       in RequestProcessor
 *  54   Gandalf   1.53        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  53   Gandalf   1.52        6/10/99  Petr Hamernik   last change improved
 *  52   Gandalf   1.51        6/10/99  Petr Hamernik   Instance support 
 *       improved
 *  51   Gandalf   1.50        6/10/99  Petr Hamernik   simple InstanceSupport 
 *       added to CookieSet
 *  50   Gandalf   1.49        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  49   Gandalf   1.48        6/5/99   Petr Hamernik   small improvement
 *  48   Gandalf   1.47        6/4/99   Petr Hamernik   synchronization update
 *  47   Gandalf   1.46        6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  46   Gandalf   1.45        5/19/99  Jan Jancura     Keep reference to 
 *       ElementNode
 *  45   Gandalf   1.44        5/17/99  Petr Hamernik   missing implementation 
 *       added
 *  44   Gandalf   1.43        5/14/99  Petr Hamernik   fixed bugs #1738 and 
 *       #1756
 *  43   Gandalf   1.42        5/13/99  Petr Hamernik   changes in comparing 
 *       Identifier, Type classes
 *  42   Gandalf   1.41        5/12/99  Petr Hamernik   ide.src.Identifier 
 *       changed
 *  41   Gandalf   1.40        5/11/99  Ales Novak      PrintSupport removed
 *  40   Gandalf   1.39        5/6/99   Jesse Glick     [JavaDoc]
 *  39   Gandalf   1.38        4/28/99  Petr Hamernik   printing support
 *  38   Gandalf   1.37        4/28/99  Petr Hamernik   simple synchronization 
 *       using ConnectionCookie
 *  37   Gandalf   1.36        4/21/99  Petr Hamernik   debugs removed
 *  36   Gandalf   1.35        4/21/99  Petr Hamernik   Java module updated
 *  35   Gandalf   1.34        4/15/99  Martin Ryzl     JavaDataObject.createCompiler
 *        added 
 *  34   Gandalf   1.33        4/13/99  Petr Hamernik   SourceCookie added to 
 *       CookieSet
 *  33   Gandalf   1.32        4/13/99  Ales Novak      Parsing during startup 
 *       fix.
 *  32   Gandalf   1.31        4/8/99   Ales Novak      
 *  31   Gandalf   1.30        4/2/99   Jan Jancura     ObjectBrowser support 
 *       II.
 *  30   Gandalf   1.29        4/1/99   Ian Formanek    Rollback to make it 
 *       compilable
 *  29   Gandalf   1.28        4/1/99   Jan Jancura     Object Browser support
 *  28   Gandalf   1.27        3/31/99  Ales Novak      
 *  27   Gandalf   1.26        3/29/99  Petr Hamernik   
 *  26   Gandalf   1.25        3/29/99  Ian Formanek    removed import of 
 *       modules.compiler
 *  25   Gandalf   1.24        3/24/99  Ian Formanek    Added protected method 
 *       getJavaEditor to allow FormEditor to redefine the editing behavior
 *  24   Gandalf   1.23        3/19/99  Ales Novak      
 *  23   Gandalf   1.22        3/10/99  Petr Hamernik   
 *  22   Gandalf   1.21        2/12/99  Petr Hamernik   
 *  21   Gandalf   1.20        2/11/99  Petr Hamernik   
 *  20   Gandalf   1.19        2/11/99  Petr Hamernik   
 *  19   Gandalf   1.18        2/11/99  Petr Hamernik   
 *  18   Gandalf   1.17        2/8/99   Petr Hamernik   
 *  17   Gandalf   1.16        2/8/99   Petr Hamernik   
 *  16   Gandalf   1.15        2/4/99   Petr Hamernik   
 *  15   Gandalf   1.14        2/4/99   Petr Hamernik   
 *  14   Gandalf   1.13        1/29/99  Petr Hamernik   
 *  13   Gandalf   1.12        1/27/99  Petr Hamernik   
 *  12   Gandalf   1.11        1/26/99  Petr Hamernik   
 *  11   Gandalf   1.10        1/26/99  Petr Hamernik   
 *  10   Gandalf   1.9         1/26/99  Petr Hamernik   
 *  9    Gandalf   1.8         1/21/99  Petr Hamernik   
 *  8    Gandalf   1.7         1/20/99  Petr Hamernik   
 *  7    Gandalf   1.6         1/15/99  Petr Hamernik   
 *  6    Gandalf   1.5         1/13/99  Petr Hamernik   
 *  5    Gandalf   1.4         1/7/99   Ian Formanek    
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  2    Gandalf   1.1         1/6/99   Ales Novak      
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach Changed number of parameters in constructor
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    Icon change
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    reflecting changes in cookies
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    templates
 */
