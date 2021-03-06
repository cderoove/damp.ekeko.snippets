/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.MessageFormat;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumn;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.FileEntry;
import org.openide.loaders.OpenSupport;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.text.EditorSupport;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;


/** Support for opening properties files (OpenCookie) in visual editor */
public class PropertiesOpen extends OpenSupport implements OpenCookie {

    /** Main properties dataobject */
    PropertiesDataObject obj;
    PropertyChangeListener modifL;

    private PropertiesTableModel tableModel = null;
    private Dialog dialog;
    private boolean closingLast = false;


    /** Constructor */
    public PropertiesOpen(PropertiesFileEntry fe) {
        super(fe);
        this.obj = (PropertiesDataObject)fe.getDataObject();
        this.obj.addPropertyChangeListener(new WeakListener.PropertyChange(modifL =
                                               new ModifiedListener()));
    }

    void setRef(CloneableTopComponent.Ref ref) {
        allEditors = ref;
    }

    /** Only here because of a compiler bug */
    public final CloneableTopComponent openCloneableTopComponentPublic() {
        return openCloneableTopComponent();
    }
    /** A method to create a new component. Must be overridden in subclasses.
    * @return the cloneable top component for this support
    */
    protected CloneableTopComponent createCloneableTopComponent () {
        return new PropertiesCloneableTopComponent(obj);
    }

    PropertiesFileEntry getEntry() {
        return (PropertiesFileEntry)entry;
    }

    /** Opens the table at a given key */
    public PropertiesOpenAt getOpenerForKey(PropertiesFileEntry entry, String key) {
        return new PropertiesOpenAt(entry, key);
    }

    public synchronized boolean hasOpenComponent() {
        if (closingLast) return false;
        java.util.Enumeration en = allEditors.getComponents ();
        return en.hasMoreElements ();
    }

    public PropertiesTableModel getTableModel() {
        if (tableModel == null)
            tableModel = new PropertiesTableModel(obj);
        return tableModel;
    }


    private synchronized void closeDocuments() {
        closingLast = true;
        closeEntry((PropertiesFileEntry)obj.getPrimaryEntry());
        for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext(); ) {
            closeEntry((PropertiesFileEntry)it.next());
        }
        closingLast = false;
    }

    private void closeEntry(PropertiesFileEntry entry) {
        PropertiesEditorSupport pes = entry.getPropertiesEditor();
        if (pes.hasOpenEditorComponent())
            return;
        else {
            pes.close(); // PENDING - shouldn't close the editor support
            entry.getHandler().reparseNowBlocking();
        }
    }


    /** Should test whether all data is saved, and if not, prompt the user
    * to save.
    *
    * @return <code>true</code> if everything can be closed
    */
    protected boolean canClose () {
        SaveCookie savec = (SaveCookie) obj.getCookie(SaveCookie.class);
        if (savec != null) {
            if (!shouldAskSave())
                return true;
            MessageFormat format = new MessageFormat(NbBundle.getBundle(PropertiesOpen.class).getString("MSG_SaveFile"));
            String msg = format.format(new Object[] { obj.getName()});
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_CANCEL_OPTION);
            Object ret = TopManager.getDefault().notify(nd);

            if (NotifyDescriptor.CANCEL_OPTION.equals(ret))
                return false;

            if (NotifyDescriptor.YES_OPTION.equals(ret)) {
                try {
                    savec.save();
                }
                catch (IOException e) {
                    TopManager.getDefault().notifyException(e);
                    return false;
                }
            }
        }
        return true;
    }

    /** Returns true if closing this editor whithout saving would result in loss of data
    *  because al least one of the modified files is not open in the code editor.
    *  Should be called only if the object has SaveCookie
    */                  
    private boolean shouldAskSave() {
        // for each entry : if there is a SaveCookie and no open editor component, return true.
        // if passed for all entries, return false
        PropertiesFileEntry entry = (PropertiesFileEntry)obj.getPrimaryEntry();
        SaveCookie savec = (SaveCookie)entry.getCookie(SaveCookie.class);
        if ((savec != null) && !entry.getPropertiesEditor().hasOpenEditorComponent())
            return true;
        for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext(); ) {
            entry = (PropertiesFileEntry)it.next();
            savec = (SaveCookie)entry.getCookie(SaveCookie.class);
            if ((savec != null) && !entry.getPropertiesEditor().hasOpenEditorComponent())
                return true;
        }
        return false;
    }

    /** Class for opening at a given key. */
    public class PropertiesOpenAt implements OpenCookie {

        private String key;
        private PropertiesFileEntry entry;

        PropertiesOpenAt(PropertiesFileEntry entry, String key) {
            this.entry = entry;
            this.key   = key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void open() {
            PropertiesCloneableTopComponent editor = (PropertiesCloneableTopComponent)openCloneableTopComponentPublic();
            PropertiesOpen.this.open();
            BundleStructure bs = obj.getBundleStructure();
            // find the entry
            int entryIndex = bs.getEntryIndexByFileName(entry.getFile().getName());
            int rowIndex   = bs.getKeyIndexByName(key);
            if ((entryIndex != -1) && (rowIndex != -1)) {
                editor.editCellAt(rowIndex, entryIndex + 1);
            }
            editor.requestFocus();
        }

    }

    public static class PropertiesCloneableTopComponent extends CloneableTopComponent {

        private PropertiesDataObject dobj;
        private transient PropertyChangeListener cookieL;
        private transient JPanel mainPanel;

        private static Image icon = null;

        /** The string which will be appended to the name of top component
        * when top component becomes modified */
        protected String modifiedAppendix = " *";

        static final long serialVersionUID =2836248291419024296L;
        /** Default constructor for deserialization */
        public PropertiesCloneableTopComponent() {
        }

        /** Constructor
        * @param obj data object we belong to
        */
        public PropertiesCloneableTopComponent (final PropertiesDataObject obj) {
            super (obj);
            dobj  = obj;
            initMe();
        }

        public Image getIcon () {
            if (icon == null)
                icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource (
                            "/org/netbeans/modules/properties/propertiesEditorMode.gif"));
            return icon;
        }

        private void initMe() {
            // add to OpenSupport - patch for a bug in deserialization
            dobj.getOpenSupport().setRef(getReference());

            setName(dobj.getNodeDelegate().getDisplayName());

            // listen to saving
            dobj.addPropertyChangeListener(new WeakListener.PropertyChange(cookieL =
                                               new PropertyChangeListener() {
                                                   public void propertyChange(PropertyChangeEvent evt) {
                                                       if (DataObject.PROP_COOKIE.equals(evt.getPropertyName()))
                                                           setName(dobj.getNodeDelegate().getDisplayName());
                                                   }
                                               }));

            initComponents();

            // dock into editor mode if possible
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for (int i = currentWs.length; --i >= 0; ) {
                Mode editorMode = currentWs[i].findMode(EditorSupport.EDITOR_MODE);
                if (editorMode == null) {
                    editorMode = currentWs[i].createMode(
                                     EditorSupport.EDITOR_MODE, getName(),
                                     EditorSupport.class.getResource(
                                         "/org/openide/resources/editorMode.gif"
                                     )
                                 );
                }
                editorMode.dockInto(this);
            }
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (PropertiesCloneableTopComponent.class);
        }

        public void editCellAt(final int row,final int column) {
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run() {
                                               ((BundleEditPanel)mainPanel).stopEditing();
                                               ((BundleEditPanel)mainPanel).getTable().editCellAt(row, column);
                                           }
                                       });
        }

        /** Inits the subcomponents. */
        private void initComponents() {
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            setLayout (gridbag);

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel = new BundleEditPanel(dobj, dobj.getOpenSupport().getTableModel());
            gridbag.setConstraints(mainPanel, c);
            add (mainPanel);
        }

        /** Set the name of this top component. Handles saved/not saved state.
        * Notifies the window manager.
        * @param displayName the new display name
        */
        public void setName (final String name) {
            String saveAwareName = name;
            if (dobj != null)
                if (dobj.getCookie(SaveCookie.class) != null)
                    saveAwareName = name + modifiedAppendix;
                else
                    saveAwareName = name;

            if ((saveAwareName != null) && (saveAwareName.equals(getName())))
                return;
            super.setName(saveAwareName);
        }

        /** When closing last view, also close the document.
         * @return <code>true</code> if close succeeded
        */
        protected boolean closeLast () {
            if (!dobj.getOpenSupport().canClose ()) {
                // if we cannot close the last window
                return false;
            }
            dobj.getOpenSupport().closeDocuments();

            return true;
        }

        /** Is called from the clone method to create new component from this one.
        * This implementation only clones the object by calling super.clone method.
        * @return the copy of this object
        */
        protected CloneableTopComponent createClonedObject () {

            PropertiesCloneableTopComponent pctc = new PropertiesCloneableTopComponent (dobj);


            /*String PROPERTIES_MODE = "org.netbeans.modules.properties";
            Workspace cur = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
            Mode m = cur.findMode(PROPERTIES_MODE);
            if (m == null) {
              m = cur.createMode(PROPERTIES_MODE, 
                                 NbBundle.getBundle(PropertiesOpen.class).getString("LAB_PropertiesModeName"),
                                 null);
        } */
            // PENDING
            //m.setBounds(new Rectangle(x, y, width, height));

            return pctc;
        }

        /** This method is called when parent window of this component has focus,
        * and this component is preferred one in it.
        * Override this method to perform special action on component activation.
        * (Typical thing to do here is set performers for your actions)
        * Remember to call superclass to
        */
        protected void componentActivated () {
        }

        /**
        * This method is called when parent window of this component losts focus,
        * or when this component losts preferrence in the parent window.
        * Override this method to perform special action on component deactivation.
        * (Typical thing to do here is unset performers for your actions)
        */
        protected void componentDeactivated () {
        }

        /** Serialize this top component.
        * Subclasses wishing to store state must call the super method, then write to the stream.
        * @param out the stream to serialize to
        */
        public void writeExternal (ObjectOutput out)
        throws IOException {
            super.writeExternal(out);
            out.writeObject(dobj);
        }

        /** Deserialize this top component.
        * Subclasses wishing to store state must call the super method, then read from the stream.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal(in);
            dobj = (PropertiesDataObject)in.readObject();
            initMe();
        }

    }


    /** Listens to modifications and updates save cookie. */
    private final class ModifiedListener implements SaveCookie, PropertyChangeListener {

        /** Gives notification that the DataObject was changed.
        * @param ev PropertyChangeEvent
        */
        public void propertyChange(PropertyChangeEvent ev) {
            if ((ev.getSource() == obj) &&
                    (DataObject.PROP_MODIFIED.equals(ev.getPropertyName()))) {

                if (((Boolean) ev.getNewValue()).booleanValue()) {
                    addSaveCookie();
                } else {
                    removeSaveCookie();
                }
            }
        }

        /******* Implementation of the Save Cookie *********/

        public void save () throws IOException {
            // do saving job
            saveDocument();
        }

        /** Save the document in this thread.
        * Create "orig" document for the case that the save would fail.
        * @exception IOException on I/O error
        */
        public void saveDocument () throws IOException {
            final FileObject file = obj.getPrimaryEntry().getFile();
            PropertiesFileEntry pfe = (PropertiesFileEntry)obj.getPrimaryEntry();
            SaveCookie save = (SaveCookie)pfe.getCookie(SaveCookie.class);
            if (save != null)
                save.save();
            for (Iterator it = obj.secondaryEntries().iterator(); it.hasNext();) {
                save = (SaveCookie)((PropertiesFileEntry)it.next()).getCookie(SaveCookie.class);
                if (save != null)
                    save.save();
            }
        }

        /** Adds save cookie to the DO.
        */
        private void addSaveCookie() {
            if (obj.getCookie(SaveCookie.class) == null) {
                obj.getCookieSet().add(this);
            }
        }
        /** Removes save cookie from the DO.
        */
        private void removeSaveCookie() {
            if (obj.getCookie(SaveCookie.class) == this) {
                obj.getCookieSet().remove(this);
            }
        }

    } // end of SavingManager inner class

}
