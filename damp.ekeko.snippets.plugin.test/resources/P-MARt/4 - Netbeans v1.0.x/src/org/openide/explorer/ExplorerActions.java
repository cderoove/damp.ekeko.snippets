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

package org.openide.explorer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.text.MessageFormat;

import org.openide.util.datatransfer.*;
import org.openide.loaders.*;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.actions.*;
/*import org.openide.actions.CutAction;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.actions.DeleteAction;*/
import org.openide.util.actions.ActionPerformer;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/** This class contains the default implementation of reactions to the standard
* explorer actions. It can be
* attached to any {@link ExplorerManager}. Then this class will listen to changes
* of selected nodes or the explored context of that manager, and update the state
* of cut/copy/paste/delete actions.
* <P>
* An instance of this class can only be attached to one manager at a time. Use
* {@link #attach} and {@link #detach} to make the connection.
*
* @author Jan Jancura, Petr Hamernik, Ian Formanek, Jaroslav Tulach
*/
public class ExplorerActions extends Object {
    /** copy action performer */
    private ActionPerformer copyActionPerformer;
    /** cut action performer */
    private ActionPerformer cutActionPerformer;
    /** delete action performer */
    private ActionPerformer deleteActionPerformer;

    /** tracker for all actions */
    private Listener listener;

    /** the manager we are listening on */
    ExplorerManager manager;

    /** must the delete be confirmed */
    private boolean confirmDelete = true;

    /** actions to work with */
    static CopyAction copy = null;
    static CutAction cut = null;
    static DeleteAction delete = null;
    static PasteAction paste = null;

    /** Attach to new manager.
    * @param m the manager to listen on
    */
    public synchronized void attach (ExplorerManager m) {
        if (manager != null) {
            // first of all detach
            detach ();
        }

        manager = m;

        if (listener == null) listener = new Listener ();
        manager.addPropertyChangeListener (listener);
        ExClipboard clip = TopManager.getDefault().getClipboard();
        clip.addClipboardListener(listener);

        updateActions ();
    }

    /** Detach from manager currently being listened on.
    */
    public synchronized void detach () {
        if (manager == null) return;

        manager.removePropertyChangeListener (listener);
        ExClipboard clip = TopManager.getDefault().getClipboard();
        clip.removeClipboardListener(listener);

        stopActions ();

        manager = null;
    }

    /** Set whether to confirm deletions.
    * @param yes <code>true</code> to confirm deletions
    */
    public final void setConfirmDelete (boolean yes) {
        confirmDelete = yes;
    }

    /** Should deletions be confirmed?
    * @return <code>true</code> if deletions must be confirmed
    */
    public final boolean isConfirmDelete () {
        return confirmDelete;
    }

    /** Stops listening on all actions
    */
    void stopActions () {
        if (copyActionPerformer != null) {
            copy.setActionPerformer (null);
            cut.setActionPerformer (null);
            paste.setPasteTypes (null);
            delete.setActionPerformer (null);
        }
    }

    /** Updates the state of all actions.
    * @param path list of selected nodes
    */
    void updateActions () {
        Node[] path = (manager == null)? (new Node[0]) : (manager.getSelectedNodes ());
        /*    System.out.println("ExplorerActions.updateActions: "+path.length);
            for (int i = 0; i < path.length; i++)
              System.out.println("actions["+i+"]="+path[i].getDisplayName ());

            Thread.dumpStack(); */
        if (copyActionPerformer == null) {
            copyActionPerformer = new CopyCutActionPerformer (true);
            cutActionPerformer = new CopyCutActionPerformer (false);
            deleteActionPerformer = new DeleteActionPerformer();

            copy = new CopyAction ();
            cut = new CutAction ();
            paste = new PasteAction ();
            delete = new DeleteAction();
        }

        int i;
        int k = path != null ? path.length : 0;
        if (k > 0) {
            for (i = 0; i < k; i++) {
                if (!path[i].canCopy ()) {
                    copy.setActionPerformer (null);
                    break;
                }
            }
            if (i == k) copy.setActionPerformer (copyActionPerformer);

            for (i = 0; i < k; i++) {
                if (!path[i].canCut ()) {
                    cut.setActionPerformer (null);
                    break;
                }
            }
            if (i == k) cut.setActionPerformer (cutActionPerformer);

            for (i = 0; i < k; i++) {
                if (!path[i].canDestroy ()) {
                    delete.setActionPerformer (null);
                    break;
                }
            }
            if (i == k) delete.setActionPerformer (deleteActionPerformer);

        } else {
            copy.setActionPerformer (null);
            cut.setActionPerformer (null);
            delete.setActionPerformer (null);
        }
        updatePasteAction(path);
    }

    /** Updates paste action.
    * @param path selected nodes
    */
    private void updatePasteAction (Node[] path) {
        ExplorerManager man = manager;

        if (man == null) {
            paste.setPasteTypes (null);
            return;
        }

        if (path != null && ((path.length > 1) ||
                             ((path.length == 1) && (path [0].isLeaf())))
           ) {
            paste.setPasteTypes(null);
            return;
        } else {
            Node pan = man.getExploredContext ();
            Node[] selectedNodes = man.getSelectedNodes ();
            if (selectedNodes != null && (selectedNodes.length == 1) && (!selectedNodes[0].isLeaf()))
                pan = selectedNodes[0];

            Clipboard clipboard = TopManager.getDefault().getClipboard();
            Transferable trans = clipboard.getContents(TopManager.getDefault()); //hahaha !! who is the requestor ??!
            if (trans != null) {

                // First, just ask the node if it likes this transferable, whatever it may be.
                // If it does, then fine.
                PasteType[] pasteTypes = pan.getPasteTypes(trans);
                if (pasteTypes.length != 0) {
                    paste.setPasteTypes(pasteTypes);
                    return;
                }

                boolean flavorSupported = false;
                try {

                    flavorSupported = trans.isDataFlavorSupported(ExTransferable.multiFlavor);

                } catch (java.lang.Exception e) {
                    // patch to get the Netbeans start under Solaris
                    // [PENDINGworkaround]
                }

                if (flavorSupported) {
                    // The node did not accept this multitransfer as is--try to break it into
                    // individual transfers and paste them in sequence instead.
                    try {
                        MultiTransferObject obj = (MultiTransferObject) trans.getTransferData(ExTransferable.multiFlavor);
                        int count = obj.getCount();
                        boolean ok = true;
                        Transferable[] t = new Transferable[count];
                        PasteType[] p = new PasteType[count];

                        for (int i = 0; i < count; i++) {
                            t[i] = obj.getTransferableAt(i);
                            pasteTypes = pan.getPasteTypes(t[i]);
                            if (pasteTypes.length == 0) {
                                ok = false;
                                break;
                            }
                            // [PENDING] this is ugly! ideally should be some way of comparing PasteType's for similarity?
                            p[i] = pasteTypes[0];
                        }
                        if (ok) {
                            paste.setPasteTypes(new PasteType[] { new MultiPasteType(t, p) } );
                            return;
                        }
                    }
                    catch (UnsupportedFlavorException e) {
                    }
                    catch (IOException e) {
                    }
                }
            }
            if (paste != null) paste.setPasteTypes(null);
            return;
        }
    }

    /** Paste type used when in clipbopard is MultiTransferable */
    private static class MultiPasteType extends PasteType {
        /** Array of transferables */
        Transferable[] t;

        /** Array of paste types */
        PasteType[] p;

        /** Constructs new MultiPasteType for the given content of the clipboard */
        MultiPasteType(Transferable[] t, PasteType[] p) {
            this.t = t;
            this.p = p;
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should be cleared.
        */
        public Transferable paste() throws IOException {
            int size = p.length;
            Transferable[] arr = new Transferable[size];
            for (int i = 0; i < size; i++) {
                Transferable newTransferable = p[i].paste();
                if (newTransferable != null) {
                    arr[i] = newTransferable;
                } else {
                    // keep the orginal
                    arr[i] = t[i];
                }
            }
            return new ExTransferable.Multi (arr);
        }
    }

    /** Class which performs copy and cut actions */
    class CopyCutActionPerformer extends Object implements
        org.openide.util.actions.ActionPerformer {
        /** determine if adapter is used for copy or cut action. */
        boolean copyCut;

        /** Create new adapter */
        public CopyCutActionPerformer (boolean b) {
            copyCut = b;
        }

        /** Perform copy or cut action. */
        public void performAction(org.openide.util.actions.SystemAction action) {
            Transferable trans = null;
            Node[] sel = manager.getSelectedNodes ();

            if (sel.length != 1) {
                Transferable[] arrayTrans = new Transferable[sel.length];

                for (int i = 0; i < sel.length; i++)
                    if ((arrayTrans[i] = getTransferableOwner(sel[i])) == null) return;
                trans = new ExTransferable.Multi (arrayTrans);
            }
            else {
                trans = getTransferableOwner(sel[0]);
            }
            if (trans != null) {
                Clipboard clipboard = TopManager.getDefault().getClipboard();

                clipboard.setContents(trans, new StringSelection ("")); // NOI18N
            }
        }

        private Transferable getTransferableOwner(Node node) {
            try {
                return copyCut ? node.clipboardCopy() : node.clipboardCut();
            } catch (java.io.IOException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                    e.printStackTrace();
                }
                org.openide.TopManager.getDefault().notify(
                    new org.openide.NotifyDescriptor.Exception(e)
                );
                return null;
            }
        }
    }// innerclass CopyCutActionAdapter

    /** Class which performs delete action */
    class DeleteActionPerformer extends Object implements
        org.openide.util.actions.ActionPerformer {
        /** Perform delete action. */
        public void performAction(org.openide.util.actions.SystemAction action) {
            final Node[] sel = manager.getSelectedNodes ();
            if ((sel == null) || (sel.length == 0))
                return;

            boolean isConfirmed = !confirmDelete;
            // ask for confirmation if not confirmed
            if (isConfirmed == false) {
                Object params;
                String title;
                MessageFormat mf;

                if (sel.length == 1) {
                    if (sel[0].getCookie (DataShadow.class) != null) {
                        mf = new MessageFormat (
                                 ExplorerManager.explorerBundle.getString(
                                     "MSG_ConfirmDeleteShadow" // NOI18N
                                 )
                             );
                        title = ExplorerManager.explorerBundle.getString(
                                    "MSG_ConfirmDeleteShadowTitle" // NOI18N
                                );
                        DataShadow obj = (DataShadow)sel[0].getCookie (DataShadow.class);
                        params = new Object[] {
                                     obj.getName (), // name of the shadow
                                     sel[0].getDisplayName (), // name of original
                                     obj.getPrimaryFile ().toString (), // full name of file for shadow
                                     obj.getOriginal ().getPrimaryFile ().toString () // full name of original file
                                 };
                    } else if (sel[0].getCookie(DataFolder.class) != null) {
                        mf = new MessageFormat(
                                 ExplorerManager.explorerBundle.getString(
                                     "MSG_ConfirmDeleteFolder" // NOI18N
                                 )
                             );
                        title = ExplorerManager.explorerBundle.getString(
                                    "MSG_ConfirmDeleteFolderTitle" // NOI18N
                                );
                        params = new Object[] { sel[0].getDisplayName() };
                    } else {
                        mf = new MessageFormat(
                                 ExplorerManager.explorerBundle.getString(
                                     "MSG_ConfirmDeleteObject" // NOI18N
                                 )
                             );
                        title = ExplorerManager.explorerBundle.getString(
                                    "MSG_ConfirmDeleteObjectTitle" // NOI18N
                                );
                        params = new Object[] { sel[0].getDisplayName() };
                    }
                }
                else {
                    mf = new MessageFormat(ExplorerManager.explorerBundle.getString(
                                               "MSG_ConfirmDeleteObjects" // NOI18N
                                           ));
                    title = ExplorerManager.explorerBundle.getString(
                                "MSG_ConfirmDeleteObjectsTitle" // NOI18N
                            );
                    params = new Object[] { new Integer(sel.length) };
                }
                NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                                            mf.format(params),
                                            title,
                                            NotifyDescriptor.YES_NO_OPTION
                                        );
                isConfirmed = NotifyDescriptor.YES_OPTION.equals (
                                  TopManager.getDefault().notify(desc)
                              );
            }
            // perform action if confirmed
            if (isConfirmed) {

                // clear selected nodes
                try {
                    manager.setSelectedNodes(new Node[] {});
                } catch (java.beans.PropertyVetoException e) {
                    // never thrown, setting empty selected nodes cannot be vetoed
                }
                try {
                    TopManager.getDefault().getRepository ().getDefaultFileSystem().runAtomicAction (new FileSystem.AtomicAction () {
                                public void run () {
                                    for (int i=0; i<sel.length; i++) {
                                        try {
                                            sel[i].destroy ();
                                        } catch (java.io.IOException e) {
                                            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                                                e.printStackTrace();
                                            }
                                            org.openide.TopManager.getDefault().notify(
                                                new org.openide.NotifyDescriptor.Exception(e)
                                            );
                                        }
                                    }
                                }
                            });
                }
                catch (IOException e) {
                }
                delete.setActionPerformer (null); // fixes bug #673
            }
        }
    }// innerclass DeleteActionAdapter

    /** delay for updating actions */
    private static final int DELAY = 150;

    private class Listener
        implements PropertyChangeListener, ClipboardListener, Runnable {
        /** a task for updating with delay */
        private RequestProcessor.Task task = RequestProcessor.createRequest(this);
        {
            // priority on the level of AWT
            task.setPriority (Thread.MAX_PRIORITY - 1);
        }

        public void propertyChange (PropertyChangeEvent e) {
            task.schedule (DELAY);
        }

        /** This method is called when content of clipboard is changed.
        * @param ev event describing the action
        */
        public void clipboardChanged (ClipboardEvent ev) {
            if (!ev.isConsumed ()) {
                updatePasteAction(manager.getSelectedNodes ());
            }
        }

        /** Updates the actions.
        */
        public void run() {
            updateActions ();
        }
    }
}

/*
 * Log
 *  21   Gandalf-post-FCS1.19.1.0    3/28/00  Svatopluk Dedic notifyException replaced
 *       with NotifyDescriptor.Exception
 *  20   Gandalf   1.19        1/16/00  Ian Formanek    NOI18N
 *  19   Gandalf   1.18        1/15/00  Jaroslav Tulach DataShadow enhancements
 *  18   Gandalf   1.17        1/12/00  Ian Formanek    NOI18N
 *  17   Gandalf   1.16        12/9/99  Jaroslav Tulach #4930
 *  16   Gandalf   1.15        12/2/99  Jaroslav Tulach Performance 
 *       improvements.
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        9/20/99  Jesse Glick     Permitting a node to 
 *       explicitly accept a multi-object paste transferable as a single unit if
 *       it wishes, rather than considering each transferable component 
 *       separately.
 *  13   Gandalf   1.12        8/13/99  Martin Entlicher VCS TEST
 *  12   Gandalf   1.11        8/13/99  Martin Entlicher VCS Test
 *  11   Gandalf   1.10        8/13/99  Martin Entlicher VCS Test
 *  10   Gandalf   1.9         6/10/99  Ian Formanek    more safe to possible 
 *       problems
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/14/99  Jaroslav Tulach Fix 1739
 *  7    Gandalf   1.6         4/2/99   Jaroslav Tulach 
 *  6    Gandalf   1.5         3/20/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  4    Gandalf   1.3         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  3    Gandalf   1.2         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.19        --/--/98 Petr Hamernik   Paste action redesigned
 *  0    Tuborg    0.20        --/--/98 Jan Formanek    System.out.println("ClipboardException") replaced with notifyException
 *  0    Tuborg    0.21        --/--/98 Jan Formanek    added compile action
 *  0    Tuborg    0.30        --/--/98 Jan Formanek    SWITCHED TO NODES
 *  0    Tuborg    0.33        --/--/98 Jan Formanek    bugfix
 *  0    Tuborg    0.34        --/--/98 Jan Formanek    another bugfix (ClipboardOperations)
 *  0    Tuborg    0.35        --/--/98 Jaroslav Tulach ClipboardOperation moved to node.
 *  0    Tuborg    0.36        --/--/98 Jan Formanek    delete action support added
 *  0    Tuborg    0.37        --/--/98 Jan Formanek    compile action removed
 *  0    Tuborg    0.38        --/--/98 Jaroslav Tulach does not react to change of clipboard if the event is consumed
 *  0    Tuborg    0.39        --/--/98 Petr Hamernik   save action
 */
