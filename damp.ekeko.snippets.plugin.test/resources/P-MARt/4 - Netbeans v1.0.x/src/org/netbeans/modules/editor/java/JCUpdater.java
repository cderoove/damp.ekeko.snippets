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

package org.netbeans.modules.editor.java;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.editor.ext.JCClass;
import org.netbeans.editor.ext.JCompletion;
import org.netbeans.editor.ext.JCUtilities;
import org.openide.src.ClassElement;
import org.openide.src.ConstructorElement;
import org.openide.src.MethodElement;
import org.openide.src.FieldElement;
import org.openide.src.Identifier;
import org.openide.src.SourceElement;
import org.openide.src.Type;
import org.openide.src.MethodParameter;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.enum.QueueEnumeration;

/**
* Updating of JC classes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCUpdater extends Thread {

    private static ResourceBundle bundle;

    private static final String BUNDLE_TITLE = "JC_title"; // NOI18N
    private static final String BUNDLE_INSPECTING = "JC_inspecting"; // NOI18N
    private static final String BUNDLE_BUILDING_CLASS = "JC_building_class"; // NOI18N
    private static final String BUNDLE_INIT_PARSER = "JC_init_parser"; // NOI18N
    private static final String BUNDLE_UPDATING_DB = "JC_updating_db"; // NOI18N
    private static final String BUNDLE_UPDATING_MEMORY = "JC_updating_memory"; // NOI18N
    private static final String BUNDLE_ENTRIES = "JC_entries"; // NOI18N
    private static final String BUNDLE_CLASSES_FOUND = "JC_classes_found"; // NOI18N
    private static final String BUNDLE_CLASSES_DONE = "JC_classes_done"; // NOI18N
    private static final String BUNDLE_CLASSES_DONE_OF = "JC_classes_done_of"; // NOI18N

    Node[] activatedNodes;

    ProgressPanel progress;

    Map lookupCache;

    int doneCnt;

    public JCUpdater(Node[] activatedNodes) {
        this.activatedNodes = activatedNodes;

        //    setPriority(Thread.MIN_PRIORITY);
        setName("ParserDB Updater"); // NOI18N
    }


    public void run() {
        try {
            progress = new ProgressPanel();
            progress.setDialogVisible(true);

            lookupCache = new HashMap(1009);

            Node[] nodes = (Node[])activatedNodes.clone();
            activatedNodes = null;

            for (int i = 0; i < nodes.length; i++) {
                Node n = nodes[i];

                progress.invokeSetBuilding(getBundleString(BUNDLE_INSPECTING));
                progress.invokeSetCurrentClass(""); // NOI18N
                progress.invokeSetDoneString(""); // NOI18N

                if (!processNode(n, progress)) {
                    break;
                }

                nodes[i] = null;
            }

            lookupCache.clear();
            lookupCache = null;

            progress.destroy();
            progress = null;
        } catch (Throwable t) {
            System.err.println("Exception occurred during parser database rebuilding"); // NOI18N
            t.printStackTrace();
        }
    }

    boolean processNode(Node n, ProgressPanel progress) {
        QueueEnumeration qe = new QueueEnumeration();
        int cnt = 0;
        boolean root = false;
        String fsName = null;
        DataFolder df = (DataFolder)n.getCookie(DataFolder.class);

        try {
            if (df != null) { // data folder
                inspectFolder(df, qe);
                cnt = doneCnt;
                doneCnt = 0;
                FileObject fo = df.getPrimaryFile();
                fsName = fo.getFileSystem().getSystemName();
                root = fo.isRoot();

            } else {
                SourceCookie sc = (SourceCookie)n.getCookie(SourceCookie.class);
                if (sc != null) {
                    cnt = 1;
                    qe.put(sc);
                    fsName = ((DataObject)n.getCookie(DataObject.class)).getPrimaryFile(
                             ).getFileSystem().getSystemName();
                }
            }
        } catch (FileStateInvalidException e) {
            // fsName will be null
        }

        // process queue
        if (cnt > 0 && fsName != null) {
            return updateProvider(qe, cnt, fsName, root, progress);
        }

        return true;
    }

    void inspectFolder(DataFolder df, QueueEnumeration qe) {
        DataObject[] children = df.getChildren();
        children = (DataObject[])children.clone();
        for (int i = 0; i < children.length; i++) {
            DataObject dob = children[i];
            if (dob instanceof DataFolder) {
                inspectFolder((DataFolder)dob, qe);
            } else {
                SourceCookie sc = (SourceCookie)dob.getCookie(SourceCookie.class);
                if (sc != null) {
                    qe.put(sc);
                    doneCnt++;
                    progress.invokeSetCurrentClass(doneCnt + ' ' + getBundleString(BUNDLE_CLASSES_FOUND));
                }
            }
            children[i] = null;
        }
    }


    boolean updateProvider(final QueueEnumeration qe, final int classCnt,
                           String fsName, boolean isRoot, final ProgressPanel progress) {

        // Get provider
        JCStorage storage = JCStorage.getStorage();
        JCStorageElement element = storage.findFileSystemElement(fsName);

        if (isRoot || element == null) { // not yet created
            progress.setDialogVisible(false);

            // Display dialog with options
            JCProviderPanel pp = new JCProviderPanel();
            if (element != null) {
                pp.setNamePrefix(element.getName());
                pp.setClassLevel(element.getClassLevel());
                pp.setFieldLevel(element.getFieldLevel());
                pp.setMethodLevel(element.getMethodLevel());
            }
            DialogDescriptor dd = new DialogDescriptor(pp,
                                  getBundleString(BUNDLE_TITLE) + ' ' + fsName);
            Dialog d = TopManager.getDefault().createDialog(dd);
            d.pack();
            d.setVisible(true);
            Object o = dd.getValue();
            if (o == DialogDescriptor.OK_OPTION) {
                element = storage.addElement(pp.getNamePrefix(), fsName,
                                             pp.getClassLevel(), pp.getFieldLevel(), pp.getMethodLevel());
            } else { // cancel pressed
                d.setVisible(false);
                d.dispose();
                d = null;
                return false;
            }

            progress.setDialogVisible(true);
        }

        final int classLevel = element.getClassLevel();
        final int fieldLevel = element.getFieldLevel();
        final int methodLevel = element.getMethodLevel();
        storage.checkProvider(element);

        // Update provider classes
        final String doneString = " " + getBundleString(BUNDLE_CLASSES_DONE); // NOI18N
        final String ofString = " " + getBundleString(BUNDLE_CLASSES_DONE_OF) + " "; // NOI18N

        final JCompletion.ListProvider classes = new JCompletion.ListProvider() {

                    protected boolean appendClass(JCClass c) {
                        super.appendClass(c);
                        progress.invokeSetCurrentClass(c.getFullName());
                        return !progress.cancelled;
                    }

                    public boolean notifyAppend(JCClass c, boolean appendFinished) {
                        if (appendFinished) {
                            doneCnt++;
                            progress.invokeSetDoneString(doneCnt + ofString + classCnt + doneString);
                        } else { // not finished
                            progress.invokeSetCurrentClass(c.getFullName());
                        }
                        return !progress.cancelled;
                    }

                };


        // Build class list
        progress.invokeSetBuilding(getBundleString(BUNDLE_BUILDING_CLASS));
        progress.invokeSetDoneString(getBundleString(BUNDLE_INIT_PARSER));

        boolean ok = true;
        doneCnt = 0;
        while (ok && qe.hasMoreElements()) {
            SourceCookie sc = (SourceCookie)qe.nextElement();
            SourceElement se = sc.getSource();
            ClassElement ce[] = se.getAllClasses();
            if (ce != null) {
                for (int i = 0; i < ce.length; i++) {
                    JCClass classToAppend = JCExtension.parseClassElement(ce[i], classLevel,
                                            fieldLevel, methodLevel, lookupCache, false);
                    if (classToAppend != null) {
                        if (!classes.append(new JCompletion.SingleProvider(classToAppend))) {
                            ok = false;
                            break;
                        }
                    }
                }
            }

            doneCnt++;
            progress.invokeSetDoneString(doneCnt + ofString + classCnt + doneString);
        }

        if (!ok) {
            return false;
        }

        // Update database
        doneCnt = 0;
        progress.invokeSetBuilding(getBundleString(BUNDLE_UPDATING_DB));
        element.getProvider().append(classes);

        // Update in-memory copy
        doneCnt = 0;
        progress.invokeSetBuilding(getBundleString(BUNDLE_UPDATING_MEMORY));
        JCompletion.getFinder().append(classes);

        return true;
    }

    String getBundleString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(JCProviderPanel.class);
        }
        return bundle.getString(s);
    }


    class ProgressPanel extends JCProgressPanel
        implements ActionListener {

        private DialogDescriptor descriptor;

        Dialog dialog;

        boolean cancelled;

        static final long serialVersionUID =6306529202240892814L;
        ProgressPanel() {
        }

        DialogDescriptor getDescriptor() {
            if (descriptor == null) {
                descriptor = createDescriptor();
            }
            return descriptor;
        }

        DialogDescriptor createDescriptor() {
            return new DialogDescriptor(this,
                                        getBundleString("JC_progress_title"), // NOI18N
                                        false,
                                        new Object[] { DialogDescriptor.CANCEL_OPTION },
                                        DialogDescriptor.CANCEL_OPTION, DialogDescriptor.BOTTOM_ALIGN,
                                        new HelpCtx(JCExtension.class),
                                        this
                                       );
        }


        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() instanceof javax.swing.JButton) { // how better? !!!
                cancelPressed();
            }
        }

        void cancelPressed() {
            cancelled = true;
            setDialogVisible(false);
        }

        void setDialogVisible(final boolean visible) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        if (visible && dialog == null) {
                            dialog = TopManager.getDefault().createDialog(getDescriptor());
                            dialog.setSize(300, 200);
                        }

                        if (dialog != null) {
                            dialog.setVisible(visible);
                        }
                    }
                }
            );
        }

        void destroy() {
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
                dialog = null;
            }
            descriptor = null;
        }
    }

}

/*
 * Log
 *  12   Gandalf   1.11        1/13/00  Miloslav Metelka Localization
 *  11   Gandalf   1.10        12/28/99 Miloslav Metelka 
 *  10   Gandalf   1.9         11/14/99 Miloslav Metelka 
 *  9    Gandalf   1.8         11/11/99 Miloslav Metelka 
 *  8    Gandalf   1.7         11/8/99  Miloslav Metelka 
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/15/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         7/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

