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
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.*;

import org.openide.*;
import org.openide.cookies.ConnectionCookie;
import org.openide.nodes.Node;
import org.openide.src.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.java.settings.JavaSynchronizationSettings;

/** This class and its inner classes maintain the basic functionality and support
* for the synchronization between java sources.
*
* @author Petr Hamernik
*/
public class JavaConnections {
    /** Constants used for definitions of the TYPE_XXX constants */
    static final int ADD = 0x0001;
    static final int REMOVE = 0x0002;
    static final int CHANGE = 0x0004;

    public static final int TYPE_FIELDS_ADD = ADD;
    public static final int TYPE_FIELDS_REMOVE = REMOVE;
    public static final int TYPE_FIELDS_CHANGE = CHANGE;
    public static final int TYPE_FIELDS = (ADD | REMOVE | CHANGE);

    public static final int TYPE_METHODS_ADD = ADD << 4;
    public static final int TYPE_METHODS_REMOVE = REMOVE << 4;
    public static final int TYPE_METHODS_CHANGE = CHANGE << 4;
    public static final int TYPE_METHODS = (ADD | REMOVE | CHANGE) << 4;

    public static final int TYPE_CLASSES_ADD = ADD << 8;
    public static final int TYPE_CLASSES_REMOVE = REMOVE << 8;
    public static final int TYPE_CLASSES_CHANGE = CHANGE << 8;
    public static final int TYPE_CLASSES = (ADD | REMOVE | CHANGE) << 8;

    public static final int TYPE_CONSTRUCTORS_ADD = ADD << 12;
    public static final int TYPE_CONSTRUCTORS_REMOVE = REMOVE << 12;
    public static final int TYPE_CONSTRUCTORS_CHANGE = CHANGE << 12;
    public static final int TYPE_CONSTRUCTORS = (ADD | REMOVE | CHANGE) << 12;

    public static final int TYPE_INITIALIZERS_ADD = ADD << 16;
    public static final int TYPE_INITIALIZERS_REMOVE = REMOVE << 16;
    public static final int TYPE_INITIALIZERS_CHANGE = CHANGE << 16;
    public static final int TYPE_INITIALIZERS = (ADD | REMOVE | CHANGE) << 16;

    public static final int TYPE_SOURCE_CHECK_SELF = 0x100000;
    public static final int TYPE_SOURCE_CHECK_DEEP = 0x200000;

    public static final int ADD_MASK = 0x11111;
    public static final int REMOVE_MASK = 0x22222;
    public static final int CHANGE_MASK = 0x44444;

    public static final int TYPE_ALL = 0xFFFFF;

    static final JavaSynchronizationSettings SETTINGS =
        (JavaSynchronizationSettings)JavaSynchronizationSettings.findObject(JavaSynchronizationSettings.class);

    /** The basic type of connection between two sources.
    */
    public static class Type implements ConnectionCookie.Type {

        /** The filter of events */
        int filter;

        static final long serialVersionUID =-6323669534600303244L;
        /** Creates new type of java connection.
        * @param filter The filter - consist of TYPE_XXX constants.
        */
        public Type(int filter) {
            this.filter = filter;
        }

        /** The class that is passed into the listener's <CODE>notify</CODE>
        * method when an event of this type is fired.
        *
        * @return ImplementsEvent.class
        */
        public Class getEventClass () {
            return Event.class;
        }

        /** Implements connection is persistent.
        * @return always <CODE>true</CODE>
        */
        public boolean isPersistent () {
            return true;
        }

        /** Test whether this type overlaps with the specified one.
        * @return <CODE>true</CODE> if the types are overlapped
        */
        public boolean overlaps(ConnectionCookie.Type type) {
            if (type instanceof Type)
                return (filter & ((Type)type).filter) != 0;
            return false;
        }

        /**
        * @return the filter of this type which was passed in the constructor
        */
        public int getFilter() {
            return filter;
        }

        public boolean equals(Object o) {
            return (o instanceof Type) && (((Type)o).filter == filter);
        }

        public int hashCode() {
            return filter;
        }
    };

    /** The basic type of connection between two sources
    * - interface and implementation.
    */
    public static final Type IMPLEMENTS = new Type(TYPE_METHODS_ADD | TYPE_METHODS_CHANGE);

    //================================================

    public static class Change {
        int changeType;

        Element oldElement;
        Element newElement;
        Element[] elements;

        Change(int changeType) {
            this(changeType, null, null, null);
        }

        Change(int changeType, Element[] elements) {
            this(changeType, null, null, elements);
        }

        Change(int changeType, Element oldElement, Element newElement) {
            this(changeType, oldElement, newElement, null);
        }

        private Change(int changeType, Element oldElement, Element newElement, Element[] elements) {
            this.changeType = changeType;
            this.oldElement = oldElement;
            this.newElement = newElement;
            this.elements = elements;
        }

        public int getChangeType() {
            return changeType;
        }

        public Element getNewElement() {
            return newElement;
        }

        public Element getOldElement() {
            return oldElement;
        }

        public Element[] getElements() {
            return elements;
        }
    }

    public static class Event extends ConnectionCookie.Event {
        Change[] changes;

        static final long serialVersionUID =-3347417315663192416L;

        Event(Node n, Change[] changes, Type type) {
            super(n, type);
            this.changes = changes;
        }

        public Change[] getChanges() {
            return changes;
        }
    }

    /** This class represents one change during connection
    * synchronization between two classes. If user accept it,
    * there could be called <CODE>process</CODE> method to 
    * make the change.
    */
    public abstract static class ChangeProcessor {
        /** Display name of the change */
        private String displayName;

        /** Create new change
        * @param displayName Display name of the change
        */
        public ChangeProcessor(String displayName) {
            this.displayName = displayName;
        }

        /**
        * @return Display name of the change
        */
        public String getDisplayName() {
            return displayName;
        }

        /** Process the change.
        * @exception SourceException if any problem occured with
        *     modification of the source code.
        */
        public abstract void process() throws SourceException;
    }

    private static Dialog confirmChangesDialog;
    private static DialogDescriptor confirmChangesDescriptor;
    static ConnectionPanel connectionPanel;
    static JButton processButton;
    static JButton processAllButton;

    /** Opens the dialog with found changes and ask user
    * to confirm them.
    * @param changes The list of the ChangeProcessor objects
    * @param synchType the current synchronization type
    * @return new value of synchronizationType after dialog closed.
    */
    public static synchronized byte showChangesDialog(List changes, byte synchType) {
        if (confirmChangesDescriptor == null) {
            processButton = new JButton (Util.getString("LAB_processButton"));
            processAllButton = new JButton (Util.getString("LAB_processAllButton"));
            final Object [] options = new Object [] {
                                          processButton,
                                          processAllButton
                                      };
            final Object [] additionalOptions = new Object [] {
                                                    new JButton (Util.getString("LAB_closeButton"))
                                                };
            connectionPanel = new ConnectionPanel();
            confirmChangesDescriptor = new DialogDescriptor(
                                           connectionPanel,
                                           Util.getString("LAB_ConfirmDialog"),
                                           true,
                                           options,
                                           processButton,
                                           DialogDescriptor.RIGHT_ALIGN,
                                           new HelpCtx (JavaConnections.class.getName () + ".dialog"), // NOI18N
                                           new ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   if (e.getSource() instanceof Component) {
                                                       Component root;

                                                       // hack to avoid multiple calls for disposed dialogs:
                                                       root = SwingUtilities.getRoot((Component)e.getSource());
                                                       if (confirmChangesDialog == null || !root.isDisplayable()) {
                                                           return;
                                                       }
                                                   }
                                                   if (options[0].equals(e.getSource())) {
                                                       int min = connectionPanel.changesList.getMinSelectionIndex();
                                                       int max = connectionPanel.changesList.getMaxSelectionIndex();
                                                       for (int i = max; i >= min; i--) {
                                                           if (connectionPanel.changesList.isSelectedIndex(i)) {
                                                               ChangeProcessor p = (ChangeProcessor)connectionPanel.listModel.getElementAt(i);
                                                               try {
                                                                   p.process();
                                                               }
                                                               catch (SourceException ee) {
                                                                   TopManager.getDefault().notify(
                                                                       new NotifyDescriptor.Exception(ee));
                                                                   //TopManager.getDefault().notifyException(ee);
                                                                   continue;
                                                               }
                                                               connectionPanel.listModel.removeElementAt(i);
                                                           }
                                                       }
                                                       if (connectionPanel.listModel.isEmpty()) {
                                                           confirmChangesDialog.setVisible(false);
                                                       }
                                                   }
                                                   else if (options[1].equals(e.getSource())) {
                                                       Enumeration en = connectionPanel.listModel.elements();
                                                       while (en.hasMoreElements()) {
                                                           ChangeProcessor processor = (ChangeProcessor) en.nextElement();
                                                           try {
                                                               processor.process();
                                                           }
                                                           catch (SourceException ee) {
                                                               TopManager.getDefault().notify(
                                                                   new NotifyDescriptor.Exception(ee));
                                                           }
                                                       }
                                                       confirmChangesDialog.setVisible(false);
                                                       connectionPanel.setChanges(null);
                                                   }
                                                   else if (additionalOptions[0].equals(e.getSource())) {
                                                       confirmChangesDialog.setVisible(false);
                                                       connectionPanel.setChanges(null);
                                                   }
                                               }
                                           }
                                       );
            confirmChangesDescriptor.setAdditionalOptions (additionalOptions);
        }
        processButton.setEnabled(false);
        processAllButton.requestFocus();
        connectionPanel.setChanges(changes);
        connectionPanel.setRadio(synchType);

        try {
            if (confirmChangesDialog != null) {
                throw new IllegalStateException();
            }
            confirmChangesDialog = TopManager.getDefault().createDialog(confirmChangesDescriptor);
            confirmChangesDialog.show();
            return connectionPanel.getRadio();
        } finally {
            confirmChangesDialog.dispose();
            confirmChangesDialog = null;
            confirmChangesDescriptor = null;
        }
    }

    public static void compareMethods(final ClassElement dest,
                                      ClassElement src, List changeProcessors,
                                      String addMessage, String updateMessage) {
        final MethodElement[] oldMethods = dest.getMethods();
        final MethodElement[] newMethods = src.getMethods();
        int newSize = newMethods.length;
        MethodElementImpl[] newMethodsImpl = new MethodElementImpl[newSize];
        for (int i = 0; i < newSize; i++) {
            MethodElementImpl impl = new MethodElementImpl();
            MethodElement m = newMethods[i];
            impl.type = m.getReturn();
            impl.name = m.getName();
            impl.parameters = m.getParameters();
            impl.exceptions = m.getExceptions();
            impl.mod = m.getModifiers();
            newMethodsImpl[i] = impl;
        }
        int[] result = ElementsCollection.pairElements(oldMethods, newMethodsImpl, ElementsCollection.Method.COMPARATORS);

        MessageFormat addMsg = new MessageFormat(addMessage);
        MessageFormat updateMsg = new MessageFormat(updateMessage);

        for (int i = 0; i < newSize; i++) {
            final MethodElement m = newMethods[i];
            if (result[i] == -1) {
                changeProcessors.add(new ChangeProcessor(addMsg.format(new Object[] { m.getName().toString() })) {
                                         public void process() throws SourceException {
                                             dest.addMethod(m);
                                         }
                                     });
            }
            else {
                final MethodElement oldMethod = oldMethods[result[i]];
                final MethodElementImpl impl = (MethodElementImpl) oldMethod.getCookie(MethodElementImpl.class);
                final boolean[] changes = new boolean[] {
                                              !impl.type.compareTo(m.getReturn(), false),
                                              impl.parameters.length != m.getParameters().length,
                                              impl.exceptions.length != m.getExceptions().length,
                                              impl.mod != m.getModifiers(),
                                              !impl.name.compareTo(m.getName(), false)
                                          };
                for (int j = 0; (j < impl.parameters.length) && !changes[1]; j++)
                    if (!impl.parameters[j].compareTo(m.getParameters()[j], false, false))
                        changes[1] = true;
                for (int j = 0; (j < impl.exceptions.length) && !changes[2]; j++)
                    if (!impl.exceptions[j].compareTo(m.getExceptions()[j], false))
                        changes[2] = true;

                boolean ch = false;
                for (int j = 0; (j < changes.length) && !ch; j++)
                    ch |= changes[j];

                if (ch) {
                    changeProcessors.add(new ChangeProcessor(updateMsg.format(new Object[] { impl.name.toString() })) {
                                             public void process() throws SourceException {
                                                 if (changes[0])
                                                     oldMethod.setReturn(m.getReturn());
                                                 if (changes[1])
                                                     oldMethod.setParameters(m.getParameters());
                                                 if (changes[2])
                                                     oldMethod.setExceptions(m.getExceptions());
                                                 if (changes[3])
                                                     oldMethod.setModifiers(m.getModifiers());
                                                 if (changes[4])
                                                     oldMethod.setName(m.getName());
                                             }
                                         });
                }
            }
        }
    }

    static class ConnectionPanel extends JPanel {
        private JPanel changesPanel;
        private JScrollPane jScrollPane1;
        private JList changesList;
        private JPanel bottomPanel;
        private JPanel radioPanel;
        private JRadioButton radioDisable;
        private JRadioButton radioConfirm;
        private JRadioButton radioAuto;

        DefaultListModel listModel;

        static final long serialVersionUID =-799308237208355590L;

        /** Initializes the Form */
        public ConnectionPanel() {
            setLayout (new java.awt.BorderLayout ());
            setBorder(new EmptyBorder(5, 5, 5, 5));

            changesPanel = new JPanel ();
            changesPanel.setLayout (new java.awt.BorderLayout (5, 5));
            changesPanel.setBorder(new TitledBorder(Util.getString("LAB_ChangesList")));

            jScrollPane1 = new JScrollPane ();

            listModel = new DefaultListModel();

            changesList = new JList (listModel);
            changesList.setToolTipText (Util.getString("HINT_ChangesList"));
            changesList.setCellRenderer(new ChangesListCellRenderer());
            changesList.addListSelectionListener(new ListSelectionListener() {
                                                     public void valueChanged(ListSelectionEvent e) {
                                                         processButton.setEnabled(!changesList.isSelectionEmpty());
                                                     }
                                                 });

            jScrollPane1.setViewportView (changesList);

            changesPanel.add (jScrollPane1, "Center"); // NOI18N

            add (changesPanel, "Center"); // NOI18N

            radioPanel = new JPanel ();
            radioPanel.setLayout (new java.awt.GridLayout (3, 1));
            radioPanel.setBorder(new CompoundBorder(
                                     new TitledBorder(Util.getString("LAB_SynchMode")),
                                     new EmptyBorder(5, 5, 5, 5))
                                );

            radioDisable = new JRadioButton ();
            radioDisable.setText (Util.getString("LAB_radioDisable"));

            radioPanel.add (radioDisable);

            radioConfirm = new JRadioButton ();
            radioConfirm.setText (Util.getString("LAB_radioConfirm"));

            radioPanel.add (radioConfirm);

            radioAuto = new JRadioButton ();
            radioAuto.setText (Util.getString("LAB_radioAuto"));

            radioPanel.add (radioAuto);

            ButtonGroup group = new ButtonGroup();
            group.add(radioDisable);
            group.add(radioConfirm);
            group.add(radioAuto);

            add (radioPanel, "South"); // NOI18N
        }

        public java.awt.Dimension getPreferredSize() {
            java.awt.Dimension d = super.getPreferredSize();
            if (d.height < 300)
                d.height = 300;
            return d;
        }

        byte getRadio() {
            if (radioDisable.isSelected())
                return JavaDataObject.CONNECT_NOT;
            else if (radioConfirm.isSelected())
                return JavaDataObject.CONNECT_CONFIRM;
            else
                return JavaDataObject.CONNECT_AUTO;
        }

        void setRadio(byte button) {
            switch (button) {
            case JavaDataObject.CONNECT_NOT:
                radioDisable.setSelected(true);
                break;
            case JavaDataObject.CONNECT_CONFIRM:
                radioConfirm.setSelected(true);
                break;
            case JavaDataObject.CONNECT_AUTO:
                radioAuto.setSelected(true);
                break;
            }
        }

        synchronized void setChanges(List changes) {
            listModel.clear();
            if (changes != null) {
                Iterator it = changes.iterator();
                while (it.hasNext())
                    listModel.addElement(it.next());
            }
        }
    }

    static class ChangesListCellRenderer extends DefaultListCellRenderer {

        static final long serialVersionUID =-8439520404877315783L;

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if ((comp instanceof JLabel) && (value instanceof ChangeProcessor)) {
                ((JLabel)comp).setText(((ChangeProcessor)value).getDisplayName());
            }
            return comp;
        }
    }
}

/*
 * Log
 *  17   Gandalf-post-FCS1.13.1.2    4/17/00  Svatopluk Dedic Fixed ClassCastException
 *       when pressing ESC
 *  16   Gandalf-post-FCS1.13.1.1    4/3/00   Svatopluk Dedic Fixed NPE
 *  15   Gandalf-post-FCS1.13.1.0    3/8/00   Svatopluk Dedic SourceException risen 
 *       during sync processing displays only info box
 *  14   Gandalf   1.13        3/8/00   Svatopluk Dedic Static reference to 
 *       Confirmation dialog is being cleared after it closes.
 *  13   Gandalf   1.12        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  12   Gandalf   1.11        11/27/99 Patrik Knakal   
 *  11   Gandalf   1.10        10/27/99 Petr Hamernik   another bug in comparing
 *       methods
 *  10   Gandalf   1.9         10/27/99 Petr Hamernik   fixed bug in comparing 
 *       methods
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  8    Gandalf   1.7         9/15/99  Petr Hamernik   compareMethods 
 *       implemented
 *  7    Gandalf   1.6         9/10/99  Petr Hamernik   some comments
 *  6    Gandalf   1.5         8/18/99  Petr Hamernik   i18n
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         7/23/99  Petr Hamernik   java connection changes
 *  3    Gandalf   1.2         7/8/99   Jesse Glick     Context help.
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/2/99   Petr Hamernik   
 * $
 */
