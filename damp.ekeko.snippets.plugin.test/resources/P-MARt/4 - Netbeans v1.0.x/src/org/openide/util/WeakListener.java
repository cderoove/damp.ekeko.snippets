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

package org.openide.util;


import java.beans.*;
import java.lang.ref.*;
import java.util.EventListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.openide.filesystems.*;
import org.openide.loaders.OperationListener;
import org.openide.loaders.OperationEvent;

import org.openide.nodes.*;

/** Property change listener that delegates to another one
* change listener but hold only weak reference to that
* listener, so it does not prevent it to be finalized.
*
* @author Jaroslav Tulach
*/
public abstract class WeakListener extends Object implements java.util.EventListener {
    /** queue with all weak references */
    private static ReferenceQueue QUEUE = new ReferenceQueue ();
    /** how often clean */
    private static int CLEANER_TIME = 25000;
    /** clearner task */
    private static RequestProcessor.Task CLEANER_TASK = RequestProcessor.postRequest(
                new Cleaner (), CLEANER_TIME, Thread.MIN_PRIORITY
            );

    /** weak reference to listener */
    private Reference ref;
    /** class of the listener */
    Class listenerClass;
    /** weak reference to source */
    private Reference source;

    /**
    * @param listenerClass class of the listener
    * @param l listener to delegate to
    */
    protected WeakListener (Class listenerClass, java.util.EventListener l) {
        this.listenerClass = listenerClass;
        ref = new ListenerReference (l, this);
    }

    /** Setter for source
    */
    final void setSource (Object source) {
        if (source == null) {
            this.source = null;
        } else {
            this.source = new WeakReference (source);
        }
    }

    /** Method name to use for removing the listener.
    * @return name of method of the source object that should be used
    *   to remove the listener from listening on source of events
    */
    protected abstract String removeMethodName ();

    /** Getter for the target listener.
    * @param event the event the we want to distribute
    * @return null if there is no listener because it has been finalized
    */
    protected final java.util.EventListener get (java.util.EventObject ev) {
        if (ref == null) return null;

        EventListener l = (EventListener)ref.get ();
        if (l != null) {
            return l;
        } else {
            if (ev != null) {
                removeListener (ev.getSource ());
                ref = null;
            }
            return null;
        }
    }

    /** Removes a listener if the source field is filled.
    */
    private void removeListener () {
        Reference s = this.source;
        if (s == null) return;

        Object src = s.get ();
        if (src != null) {
            removeListener (src);
        }
    }

    /** Tries to find a removePropertyChangeListener method and invoke it
    * @param source the source object
    */
    private void removeListener (Object source) {
        try {
            java.lang.reflect.Method m = source.getClass ().getMethod (
                                             removeMethodName (),
                                             new Class [] { listenerClass }
                                         );
            // removePropertyChangeListener (this)
            m.invoke (source, new Object[] { this });
        } catch (Exception ex) {
            // ignore failure
        }
    }

    /** To string.
    */
    public String toString () {
        java.lang.ref.Reference r = ref;
        if (r == null) {
            return super.toString () + " " + NbBundle.getBundle(WeakListener.class).getString("MSG_ToNowhere");
        } else {
            return super.toString () + " "+ NbBundle.getBundle(WeakListener.class).getString("MSG_To") + r.get ();
        }
    }


    //
    // Methods for establishing connections
    //

    /** the implementation of weak listener factory.
    */
    private static Factory factory;

    static {
        try {
            Class c = Class.forName ("org.openide.util.WeakListener13"); // NOI18N
            factory = (Factory)c.newInstance ();
        } catch (Throwable t) {
            // use 1.2 version
            factory = new WeakListener12 ();
        }
    }

    /** Creates a weak listener. The sample usage code is
    * <PRE>
    *   AnySource source;
    *   AnyListener listener;
    *   
    *   AnyListener weak = WeakListener.any (listener, source);
    *   source.addAnyListener (weak);
    * </PRE>
    * @param l the listener to delegate to
    * @param source the source that the listener should detach from when listener <CODE>l</CODE>
    *    is no longer used, can be <CODE>null</CODE>
    */
    public static NodeListener node (NodeListener l, Object source) {
        return factory.node (l, source);
    }

    public static PropertyChangeListener propertyChange (PropertyChangeListener l, Object source) {
        return factory.propertyChange (l, source);
    }

    public static VetoableChangeListener vetoableChange (VetoableChangeListener l, Object source) {
        return factory.vetoableChange (l, source);
    }

    public static FileChangeListener fileChange (FileChangeListener l, Object source) {
        return factory.fileChange (l, source);
    }

    public static FileStatusListener fileStatus (FileStatusListener l, Object source) {
        return factory.fileStatus (l, source);
    }

    public static RepositoryListener repository (RepositoryListener l, Object source) {
        return factory.repository (l, source);
    }

    public static DocumentListener document (DocumentListener l, Object source) {
        return factory.document (l, source);
    }

    public static ChangeListener change (ChangeListener l, Object source) {
        return factory.change (l, source);
    }

    public static FocusListener focus (FocusListener l, Object source) {
        return factory.focus (l, source);
    }

    public static OperationListener operation (OperationListener l, Object source) {
        return factory.operation (l, source);
    }

    /** Weak property change listener
    * @deprecated use appropriate method instead
    */
    public static class PropertyChange extends WeakListener
        implements PropertyChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public PropertyChange (PropertyChangeListener l) {
            super (PropertyChangeListener.class, l);
        }

        /** Constructor.
        * @param clazz required class
        * @param l listener to delegate to
        */
        PropertyChange (Class clazz, PropertyChangeListener l) {
            super (clazz, l);
        }

        /** Tests if the object we reference to still exists and
        * if so, delegate to it. Otherwise remove from the source
        * if it has removePropertyChangeListener method.
        */
        public void propertyChange (PropertyChangeEvent ev) {
            PropertyChangeListener l = (PropertyChangeListener)super.get (ev);
            if (l != null) l.propertyChange (ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removePropertyChangeListener"; // NOI18N
        }
    }

    /** Weak vetoable change listener
    * @deprecated use appropriate method instead
    */
    public static class VetoableChange extends WeakListener
        implements VetoableChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public VetoableChange (VetoableChangeListener l) {
            super (VetoableChangeListener.class, l);
        }

        /** Tests if the object we reference to still exists and
        * if so, delegate to it. Otherwise remove from the source
        * if it has removePropertyChangeListener method.
        */
        public void vetoableChange (PropertyChangeEvent ev) throws PropertyVetoException {
            VetoableChangeListener l = (VetoableChangeListener)super.get (ev);
            if (l != null) l.vetoableChange (ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeVetoableChangeListener"; // NOI18N
        }
    }

    /** Weak file change listener.
    * @deprecated use appropriate method instead
    */
    public static class FileChange extends WeakListener
        implements FileChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public FileChange (FileChangeListener l) {
            super (FileChangeListener.class, l);
        }

        /** Fired when a new folder has been created. This action can only be
        * listened in folders containing the created file up to the root of
        * file system.
        *
        * @param fe the event describing context where action has taken place
        */
        public void fileFolderCreated (FileEvent ev) {
            FileChangeListener l = (FileChangeListener)super.get (ev);
            if (l != null) l.fileFolderCreated (ev);
        }

        /** Fired when a new file has been created. This action can only be
        * listened in folders containing the created file up to the root of
        * file system.
        *
        * @param fe the event describing context where action has taken place
        */
        public void fileDataCreated (FileEvent ev) {
            FileChangeListener l = (FileChangeListener)super.get (ev);
            if (l != null) l.fileDataCreated (ev);
        }

        /** Fired when a file has been changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged (FileEvent ev) {
            FileChangeListener l = (FileChangeListener)super.get (ev);
            if (l != null) l.fileChanged (ev);
        }

        /** Fired when a file has been deleted.
        * @param fe the event describing context where action has taken place
        */
        public void fileDeleted (FileEvent ev) {
            FileChangeListener l = (FileChangeListener)super.get (ev);
            if (l != null) l.fileDeleted (ev);
        }

        /** Fired when a file has been renamed.
        * @param fe the event describing context where action has taken place
        *           and the original name and extension.
        */
        public void fileRenamed (FileRenameEvent ev) {
            FileChangeListener l = (FileChangeListener)super.get (ev);
            if (l != null) l.fileRenamed (ev);
        }

        /** Fired when a file attribute has been changed.
        * @param fe the event describing context where action has taken place,
        *           the name of attribute and old and new value.
        */
        public void fileAttributeChanged (FileAttributeEvent ev) {
            FileChangeListener l = (FileChangeListener)super.get (ev);
            if (l != null) l.fileAttributeChanged (ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeFileChangeListener"; // NOI18N
        }
    }

    /** Weak file status listener.
    * @deprecated use appropriate method instead
    */
    public static class FileStatus extends WeakListener
        implements FileStatusListener {
        /** Constructor.
        */
        public FileStatus (FileStatusListener l) {
            super (FileStatusListener.class, l);
        }

        /** Notifies listener about change in annotataion of a few files.
         * @param ev event describing the change
         */
        public void annotationChanged(FileStatusEvent ev) {
            FileStatusListener l = (FileStatusListener)super.get (ev);
            if (l != null) l.annotationChanged (ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeFileStatusListener"; // NOI18N
        }

    }

    /** Weak file system pool listener.
    * @deprecated use appropriate method instead
    */
    public static class Repository extends WeakListener
        implements RepositoryListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Repository (RepositoryListener l) {
            super (RepositoryListener.class, l);
        }


        /** Called when new file system is added to the pool.
        * @param ev event describing the action
        */
        public void fileSystemAdded (RepositoryEvent ev) {
            RepositoryListener l = (RepositoryListener)super.get (ev);
            if (l != null) l.fileSystemAdded (ev);
        }

        /** Called when a file system is deleted from the pool.
        * @param ev event describing the action
        */
        public void fileSystemRemoved (RepositoryEvent ev) {
            RepositoryListener l = (RepositoryListener)super.get (ev);
            if (l != null) l.fileSystemRemoved (ev);
        }

        /** Called when a Repository is reordered. */
        public void fileSystemPoolReordered(RepositoryReorderedEvent ev) {
            RepositoryListener l = (RepositoryListener)super.get (ev);
            if (l != null) l.fileSystemPoolReordered (ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeRepositoryListener"; // NOI18N
        }

    }

    /** Weak document modifications listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    public static final class Document extends WeakListener
        implements DocumentListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Document (final DocumentListener l) {
            super (DocumentListener.class, l);
        }

        /** Gives notification that an attribute or set of attributes changed.
        * @param ev event describing the action
        */
        public void changedUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);
            if (l != null) l.changedUpdate(ev);
        }

        /** Gives notification that there was an insert into the document.
        * @param ev event describing the action
        */
        public void insertUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);
            if (l != null) l.insertUpdate(ev);
        }

        /** Gives notification that a portion of the document has been removed.
        * @param ev event describing the action
        */
        public void removeUpdate(DocumentEvent ev) {
            final DocumentListener l = docGet(ev);
            if (l != null) l.removeUpdate(ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeDocumentListener"; // NOI18N
        }

        /** Getter for the target listener.
        * @param event the event the we want to distribute
        * @return null if there is no listener because it has been finalized
        */
        private DocumentListener docGet (DocumentEvent ev) {
            if (super.ref == null) return null;

            DocumentListener l = (DocumentListener)super.ref.get ();
            if (l != null) {
                return l;
            } else {
                super.removeListener (ev.getDocument());
                super.ref = null;
                return null;
            }
        }
    } // end of Document inner class

    /** Weak swing change listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    public static final class Change extends WeakListener
        implements ChangeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Change (ChangeListener l) {
            super (ChangeListener.class, l);
        }

        /** Called when new file system is added to the pool.
        * @param ev event describing the action
        */
        public void stateChanged (final ChangeEvent ev) {
            ChangeListener l = (ChangeListener)super.get(ev);
            if (l != null) l.stateChanged (ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeChangeListener"; // NOI18N
        }

    }

    /** Weak version of listener for changes in one node.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    public static final class Node extends WeakListener.PropertyChange
        implements NodeListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Node (NodeListener l) {
            super (NodeListener.class, l);
        }

        /** Delegates to the original listener.
        */
        public void childrenAdded (NodeMemberEvent ev) {
            NodeListener l = (NodeListener)super.get (ev);
            if (l != null) l.childrenAdded (ev);
        }

        /** Delegates to the original listener.
        */
        public void childrenRemoved (NodeMemberEvent ev) {
            NodeListener l = (NodeListener)super.get (ev);
            if (l != null) l.childrenRemoved (ev);
        }

        /** Delegates to the original listener.
        */
        public void childrenReordered (NodeReorderEvent ev) {
            NodeListener l = (NodeListener)super.get (ev);
            if (l != null) l.childrenReordered (ev);
        }

        /** Delegates to the original listener.
        */
        public void nodeDestroyed (NodeEvent ev) {
            NodeListener l = (NodeListener)super.get (ev);
            if (l != null) l.nodeDestroyed (ev);
        }


        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeNodeListener"; // NOI18N
        }

    }



    /** Weak version of focus listener.
    * This class if final only for performance reasons,
    * can be happily unfinaled if desired.
    * @deprecated use appropriate method instead
    */
    public static final class Focus extends WeakListener
        implements FocusListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Focus (FocusListener l) {
            super (FocusListener.class, l);
        }

        /** Delegates to the original listener.
        */
        public void focusGained(FocusEvent ev) {
            FocusListener l = (FocusListener)super.get (ev);
            if (l != null) l.focusGained (ev);
        }

        /** Delegates to the original listener.
        */
        public void focusLost(FocusEvent ev) {
            FocusListener l = (FocusListener)super.get (ev);
            if (l != null) l.focusLost (ev);
        }

        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeFocusListener"; // NOI18N
        }

    }

    /** Weak property change listener
    */
    final static class Operation extends WeakListener
        implements OperationListener {
        /** Constructor.
        * @param l listener to delegate to
        */
        public Operation (OperationListener l) {
            super (PropertyChangeListener.class, l);
        }


        /** Method name to use for removing the listener.
        * @return name of method of the source object that should be used
        *   to remove the listener from listening on source of events
        */
        protected String removeMethodName () {
            return "removeOperationListener"; // NOI18N
        }

        /** Object has been recognized by
         * {@link DataLoaderPool#findDataObject}.
         * This allows listeners
         * to attach additional cookies, etc.
         *
         * @param ev event describing the action
         */
        public void operationPostCreate(OperationEvent ev) {
            OperationListener l = (OperationListener)super.get (ev);
            if (l != null) l.operationPostCreate (ev);
        }
        /** Object has been successfully copied.
         * @param ev event describing the action
         */
        public void operationCopy(OperationEvent.Copy ev) {
            OperationListener l = (OperationListener)super.get (ev);
            if (l != null) l.operationCopy (ev);
        }
        /** Object has been successfully moved.
         * @param ev event describing the action
         */
        public void operationMove(OperationEvent.Move ev) {
            OperationListener l = (OperationListener)super.get (ev);
            if (l != null) l.operationMove (ev);
        }
        /** Object has been successfully deleted.
         * @param ev event describing the action
         */
        public void operationDelete(OperationEvent ev) {
            OperationListener l = (OperationListener)super.get (ev);
            if (l != null) l.operationDelete (ev);
        }
        /** Object has been successfully renamed.
         * @param ev event describing the action
         */
        public void operationRename(OperationEvent.Rename ev) {
            OperationListener l = (OperationListener)super.get (ev);
            if (l != null) l.operationRename (ev);
        }

        /** A shadow of a data object has been created.
         * @param ev event describing the action
         */
        public void operationCreateShadow (OperationEvent.Copy ev) {
            OperationListener l = (OperationListener)super.get (ev);
            if (l != null) l.operationCreateShadow (ev);
        }
        /** New instance of an object has been created.
         * @param ev event describing the action
         */
        public void operationCreateFromTemplate(OperationEvent.Copy ev) {
            OperationListener l = (OperationListener)super.get (ev);
            if (l != null) l.operationCreateFromTemplate (ev);
        }
    }

    /** Reference that also holds ref to WeakListener.
    */
    private static final class ListenerReference extends WeakReference {
        final WeakListener weakListener;

        public ListenerReference (
            Object ref,
            WeakListener weakListener
        ) {
            super (ref, QUEUE);
            this.weakListener = weakListener;
        }
    }

    /** Class that periodically runs cleaning of the queue.
    */
    private static final class Cleaner extends Object implements Runnable {
        public void run () {
            for (;;) {
                ListenerReference lr = (ListenerReference)QUEUE.poll();

                if (lr == null) break;

                lr.weakListener.removeListener ();
            }

            CLEANER_TASK.schedule (CLEANER_TIME);
        }
    }


    /** Factory for 1.2 or 1.3 implementation.
    */
    static interface Factory {
        public NodeListener node (NodeListener l, Object source);
        public PropertyChangeListener propertyChange (PropertyChangeListener l, Object source);
        public VetoableChangeListener vetoableChange (VetoableChangeListener l, Object source);
        public FileChangeListener fileChange (FileChangeListener l, Object source);
        public FileStatusListener fileStatus (FileStatusListener l, Object source);
        public RepositoryListener repository (RepositoryListener l, Object source);
        public DocumentListener document (DocumentListener l, Object source);
        public ChangeListener change (ChangeListener l, Object source);
        public FocusListener focus (FocusListener l, Object source);
        public OperationListener operation (OperationListener l, Object source);
    }
}

/*
* Log
*  21   Gandalf   1.20        1/12/00  Pavel Buzek     I18N
*  20   Gandalf   1.19        1/5/00   Jaroslav Tulach Added operation listener.
*  19   Gandalf   1.18        11/5/99  Jaroslav Tulach 1.3 works better.
*  18   Gandalf   1.17        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  17   Gandalf   1.16        11/4/99  Jaroslav Tulach nodes (..., ...);  
*  16   Gandalf   1.15        10/29/99 Jaroslav Tulach MultiFileSystem + 
*       FileStatusEvent
*  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  14   Gandalf   1.13        9/30/99  Jaroslav Tulach OpenSupport is attached 
*       to setValid veto change of its data object.
*  13   Gandalf   1.12        9/17/99  Jaroslav Tulach toString changed.
*  12   Gandalf   1.11        8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  11   Gandalf   1.10        7/11/99  David Simonek   window system change...
*  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  9    Gandalf   1.8         3/27/99  David Simonek   
*  8    Gandalf   1.7         3/26/99  David Simonek   Focus added
*  7    Gandalf   1.6         2/19/99  Petr Hamernik   
*  6    Gandalf   1.5         2/11/99  Ian Formanek    Renamed FileSystemPool ->
*       Repository
*  5    Gandalf   1.4         2/5/99   Jaroslav Tulach NodeListener
*  4    Gandalf   1.3         2/4/99   Jaroslav Tulach Properties and explorer
*  3    Gandalf   1.2         1/13/99  David Simonek   
*  2    Gandalf   1.1         1/12/99  Jaroslav Tulach Modules are loaded by 
*       URLClassLoader
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
