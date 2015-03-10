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

package org.netbeans.core;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.awt.*;
import org.netbeans.core.awt.*;
import org.openide.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;

/** File Selector
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
* @version 0.13, Jun 07, 1998
*/
class FileSelector extends CoronaDialog implements PropertyChangeListener {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 6524404012203099065L;
    /** manages tree */
    private ExplorerManager manager;
    /** tree */
    private BeanTreeView tree;
    /** base panel */
    private JPanel base;
    /** selected nodes */
    private Node[] nodes;
    /** flag for cancel */
    boolean cancelFlag;
    /** instead of enable button */
    private boolean accepted;

    /** The OK Button */
    private ButtonBarButton okButton;
    /** The Cancel Button */
    private ButtonBarButton cancelButton;

    /** aceptor */
    private NodeAcceptor acceptor;

    /** reference to Frame that keeps our selected nodes synchronized with nodes actions */
    //  static TopFrameHack hack;

    /**
    * @param title is a title of the dialog
    * @param root the base object to start browsing from
    * @param acceptor decides whether we have valid selection or not
    * @param top is a Component we just place on the top of the dialog
    * it can be null
    */
    FileSelector (String title, String rootLabel, Node root, final NodeAcceptor acceptor, Component top) {
        super (null);
        java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle("org.openide.Bundle"); // NOI18N

        this.acceptor = acceptor;

        ExplorerPanel ep = new ExplorerPanel ();
        getCustomPane ().setLayout (new BorderLayout ());
        getCustomPane ().add ("Center", ep); // NOI18N
        manager = ep.getExplorerManager ();


        setDefaultCloseOperation (JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener (new WindowAdapter () {
                               public void windowClosing (WindowEvent evt) {
                                   cancelFlag = true;
                                   setVisible (false);
                               }
                           }
                          );

        // attach cancel also to Escape key
        getRootPane().registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    buttonPressed (1);
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // attach cancel also to Escape key
        getRootPane().registerKeyboardAction(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    buttonPressed (0);
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        okButton = new ButtonBarButton(bundle.getString("CTL_OK"));
        cancelButton = new ButtonBarButton(bundle.getString("CTL_CANCEL"));
        getButtonBar().setButtons(
            new ButtonBarButton[0],
            new ButtonBarButton[] { okButton, cancelButton }
        );
        setTitle (title);

        manager.setRootContext (root);//s[0]);
        // CustomPane
        BorderLayout layout = new BorderLayout();
        layout.setHgap(7);
        layout.setVgap(6);
        ep.setLayout(layout);
        ep.setBorder(new EmptyBorder(6, 7, 6, 7));

        // component to place at the top
        try {
            Node[] roots;
            if (
                root instanceof DataSystem &&
                (roots = root.getChildren ().getNodes ()).length > 0
            ) {
                JComboBox combo = new JComboBox(roots);
                combo.setSelectedIndex (0);
                combo.setRenderer(new FileSelectRenderer());
                combo.addItemListener(new ItemListener() {
                                          public void itemStateChanged(ItemEvent evt) {
                                              Node o = (Node) evt.getItem();
                                              manager.setRootContext(o);
                                          }
                                      });
                manager.setSelectedNodes (new Node[] { roots[0] });

                // North - "Create In" // NOI18N
                JPanel comboPanel = new JPanel();
                layout = new BorderLayout();
                layout.setHgap(5);          // space between label and drop-down list
                comboPanel.setLayout(layout);

                // support for mnemonics (defaults to first char)
                JLabel label = new JLabel(rootLabel.replace('&', ' '));
                label.setDisplayedMnemonic(rootLabel.charAt(rootLabel.indexOf('&') + 1));
                label.setLabelFor(combo);
                comboPanel.add(label, "West"); // NOI18N
                comboPanel.add(combo, "Center"); // NOI18N
                ep.add (comboPanel, "North"); // NOI18N
            } else {
                manager.setSelectedNodes (new Node[] { root });
                JLabel label = new JLabel(rootLabel.replace('&', ' '));
                ep.add (label, "North"); // NOI18N
            }
        } catch (java.beans.PropertyVetoException e) {
            throw new InternalError ();
        }


        // Center
        tree = new BeanTreeView ();
        tree.setPopupAllowed (false);
        ep.add(tree, "Center"); // NOI18N

        // South

        if (top != null) {
            ep.add (top, "South"); // NOI18N
        }

        cancelFlag = false;
        accepted = true;
        manager.addPropertyChangeListener (this);

        center();

        if (top != null) top.requestFocus ();

        if (acceptor.acceptNodes (manager.getSelectedNodes())) {
            enableButton ();
        } else {
            disableButton ();
        }

    }

    /** Changing properties */
    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals (ExplorerManager.PROP_SELECTED_NODES)) {
            if (acceptor.acceptNodes (manager.getSelectedNodes())) {
                enableButton ();
            } else {
                disableButton ();
            }
        }
    }

    /* * activates hack * /
    public void show() {
      hack.activated();
      super.show();
} */

    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = 260;
        return dim;
    }

    /**
    * @return selected nodes
    */
    public Node[] getNodes() {
        return nodes;
    }

    /** enables ok button */
    void enableButton () {
        accepted = true;
        okButton.setEnabled(true);
    }

    /** disables ok button */
    void disableButton () {
        accepted = false;
        okButton.setEnabled(false);
    }

    /** Called when user presses a button on the ButtonBar.
    * @param evt The ButtonBarEvent.
    */
    protected void buttonPressed(org.netbeans.core.awt.ButtonBar.ButtonBarEvent evt) {
        int index = getButtonBar().getButtonIndex(evt.getButton());
        buttonPressed (index);
    }

    /** Button pressed with index.
    */
    private void buttonPressed (int index) {
        switch (index) {
        case 0 :
            if (accepted) nodes = manager.getSelectedNodes ();
            else {
                // do not do dispose
                return;
            }
            break;
        case 1 :
            cancelFlag = true;
            break;
        }
        dispose();
    }


    /** Renderer used in list box of exit dialog */
    private static class FileSelectRenderer extends JLabel implements ListCellRenderer {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -7071698027341621636L;

        protected static Border hasFocusBorder;
        protected static Border noFocusBorder;

        public FileSelectRenderer() {
            setOpaque(true);
            setBorder(noFocusBorder);
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
            noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        }

        public java.awt.Component getListCellRendererComponent(JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
        {
            if (!(value instanceof Node)) return this;

            Node node = (Node)value;

            ImageIcon icon = new ImageIcon(node.getIcon(BeanInfo.ICON_COLOR_16x16));
            setIcon(icon);

            setText(node.getDisplayName());
            if (isSelected){
                super.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                super.setForeground(UIManager.getColor("List.selectionForeground")); // NOI18N
            }
            else {
                super.setBackground(list.getBackground());
                super.setForeground(list.getForeground());
            }

            setBorder(cellHasFocus ? hasFocusBorder : noFocusBorder);

            return this;
        }
    }
}

/*
 * Log
 *  17   Gandalf   1.16        1/13/00  Jaroslav Tulach I18N
 *  16   Gandalf   1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        9/23/99  Jaroslav Tulach 
 *  14   Gandalf   1.13        9/23/99  Jaroslav Tulach #3962
 *  13   Gandalf   1.12        8/27/99  Ian Formanek    Removed obsoleted 
 *       imports
 *  12   Gandalf   1.11        8/13/99  Jaroslav Tulach New Main Explorer
 *  11   Gandalf   1.10        8/9/99   Jaroslav Tulach 
 *  10   Gandalf   1.9         8/5/99   Jaroslav Tulach Combo works only on 
 *       filesystems.
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         3/22/99  Jaroslav Tulach Fixed creation from 
 *       template
 *  7    Gandalf   1.6         3/9/99   Jaroslav Tulach ButtonBar  
 *  6    Gandalf   1.5         3/9/99   Jaroslav Tulach 
 *  5    Gandalf   1.4         3/9/99   Jan Jancura     Bundles moved
 *  4    Gandalf   1.3         1/20/99  Jaroslav Tulach 
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
 *       ide.loaders.*
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 David Peroutka  graphics design, basic support for mnemonics
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    improved focus
 *  0    Tuborg    0.13        --/--/98 Jan Formanek    bugfixed
 */
