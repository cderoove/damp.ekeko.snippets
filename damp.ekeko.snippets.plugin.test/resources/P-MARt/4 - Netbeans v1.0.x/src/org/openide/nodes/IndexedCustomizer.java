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

package org.openide.nodes;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.ResourceBundle;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.explorer.view.NodeRenderer;

/** A dialog for reordering nodes. This dialog can reorder
* nodes for all implementors of the {@link Index} cookie.
* The dialog can invoke reorder actions on a given <code>Index</code>
* implementation immediatelly, or these actions can be accumulated
* and invoked at once, when the dialog is closed.
*
* <p>This class is final only for performance reasons.
*
* @author   Jan Jancura, Ian Formanek, Dafe Simonek
*/
public final class IndexedCustomizer extends JDialog
    implements java.beans.Customizer {
    // variables .....................................................................................

    /** The actual JList control */
    private JList control;
    /** Buttons */
    private JButton buttonUp, buttonDown, buttonClose;

    /** index to sort */
    private Index index;

    private Node[] nodes;

    /** Whether or not change the order immediatelly */
    private boolean immediateReorder = true;

    /** Permutation array, which stores moves in case when
    * immediateReorder property is false */
    private int[] permutation;

    /** Listener to the changes in the nodes */
    private ChangeListener nodeChangesL;

    /** drag and drop support */
    private IndexedDragSource dragSupport;
    private IndexedDropTarget dropSupport;

    // initializations ................................................................................

    static final long serialVersionUID =-8731362267771694641L;
    /** Construct a new customizer. */
    public IndexedCustomizer () {
        super (TopManager.getDefault().getWindowManager().getMainWindow (), true);

        setDefaultCloseOperation (javax.swing.JDialog.DISPOSE_ON_CLOSE);
        // attach cancel also to Escape key
        getRootPane().registerKeyboardAction(
            new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setVisible (false);
                    dispose ();
                }
            },
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0, true),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setTitle(Node.getString("LAB_order"));

        JComponent p = (JComponent)getContentPane ();

        p.setLayout (new java.awt.BorderLayout ());
        p.setBorder (new EmptyBorder (8, 8, 8, 5));

        JLabel l = new JLabel (Node.getString("LAB_listOrder"));
        p.add (l, "North"); // NOI18N

        control = new AutoscrollJList();
        control.addListSelectionListener (new ListSelectionListener () {
                                              public void valueChanged(ListSelectionEvent e) {
                                                  if (control.isSelectionEmpty ()) {
                                                      buttonUp.setEnabled (false);
                                                      buttonDown.setEnabled (false);
                                                  } else {
                                                      int i = control.getSelectedIndex ();
                                                      if (i > 0)  //PENDING - jeste testovat, jestli jsou OrderedCookie.Child
                                                          buttonUp.setEnabled (true);
                                                      else
                                                          buttonUp.setEnabled (false);

                                                      if (i < (nodes.length - 1))
                                                          buttonDown.setEnabled (true);
                                                      else
                                                          buttonDown.setEnabled (false);
                                                  }
                                              }
                                          }
                                         );
        control.setCellRenderer (new IndexedListCellRenderer ());
        control.setVisibleRowCount (15);
        control.setSelectionMode (javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // list has to be scrolling
        p.add (new JScrollPane(control), "Center"); // NOI18N

        JPanel bb = new JPanel ();
        buttonClose = new JButton (Node.getString("Button_close"));
        buttonUp = new JButton (Node.getString("Button_up"));
        buttonDown = new JButton (Node.getString("Button_down"));

        bb.setLayout (new BorderLayout ());
        bb.setBorder (new EmptyBorder (6, 7, 6, 7));

        JPanel x = new JPanel ();
        x.setLayout (new GridLayout (2, 1));
        x.add (buttonUp);
        x.add (buttonDown);

        bb.add ("North", x); // NOI18N
        bb.add ("South", buttonClose); // NOI18N

        buttonUp.addActionListener (new ActionListener () {
                                        public void actionPerformed (ActionEvent e) {
                                            int i = control.getSelectedIndex ();
                                            moveUp (i);
                                            updateList ();
                                            control.setSelectedIndex (i - 1);
                                            control.repaint ();
                                        }
                                    });

        buttonDown.addActionListener (new ActionListener () {
                                          public void actionPerformed (ActionEvent e) {
                                              int i = control.getSelectedIndex ();
                                              moveDown (i);
                                              updateList ();
                                              control.setSelectedIndex (i + 1);
                                              control.repaint ();
                                          }
                                      });

        buttonClose.addActionListener (new ActionListener () {
                                           public void actionPerformed (ActionEvent e) {
                                               if ((!immediateReorder) && (index != null) &&
                                                       (permutation != null)) {
                                                   int[] realPerm = new int[permutation.length];
                                                   for (int i = 0; i < realPerm.length; i++) {
                                                       realPerm[permutation[i]] = i;
                                                       //System.out.println (i + "-->" + permutation[i]); // NOI18N
                                                   }
                                                   index.reorder(realPerm);
                                               }
                                               dispose ();
                                           }
                                       });

        buttonUp.setEnabled (false);
        buttonDown.setEnabled (false);
        p.add (bb, "East"); // NOI18N
        // disable drag support, as DnD crashes all environment
        // under current implementation of VMs on unixes, ans causes
        // some deadlocks on windows...
        //dragSupport = new IndexedDragSource(control);
        //dropSupport = new IndexedDropTarget(this, dragSupport);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = getSize();
        setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);
        buttonClose.requestFocus (); // to get shortcuts to work
    }


    // other methods ................................................................................

    /** Called when an explored context changes and the list needs to be
    * recreated.
    */
    private void updateList () {
        if (index == null)
            return;
        Node[] localNodes = index.getNodes();
        //System.out.println ("Nodes taken, size: " + localNodes.length); // NOI18N
        // obtain nodes with help from permutation array, if
        // conditions met
        if (!immediateReorder) {
            getPermutation();
            int origLength = permutation.length;
            int newLength = localNodes.length;
            if (origLength < newLength) {
                // some nodes added, we must synchronize the permutation
                nodes = new Node[newLength];
                int[] newPerm = new int[newLength];
                System.arraycopy(newPerm, 0, permutation, 0, origLength);
                for (int i = 0; i < newLength; i++) {
                    if (i < origLength)
                        nodes[i] = localNodes[permutation[i]];
                    else {
                        // added nodes....
                        nodes[i] = localNodes[i];
                        newPerm[i] = i;
                    }
                }
                permutation = newPerm;
            } else if (origLength > newLength) {
                // some nodes removed, we must re-initialize the permutation
                nodes = new Node[newLength];
                permutation = new int[newLength];
                for (int i = 0; i < newLength; i++) {
                    nodes[i] = localNodes[i];
                    permutation[i] = i;
                }
            } else {
                // node count is the same, only permute the nodes
                nodes = new Node[newLength];
                for (int i = 0; i < newLength; i++)
                    nodes[i] = localNodes[permutation[i]];
            }
        } else {
            nodes = (Node[])localNodes.clone();
        }
        control.setListData (nodes);
    }

    public Dimension getPreferredSize () {
        return new Dimension (300, super.getPreferredSize ().height);
    }

    /** Will reorders be reflected immediately?
    * @return <code>true</code> if so
    */
    public boolean isImmediateReorder () {
        return immediateReorder;
    }

    /** Set whether reorders will take effect immediately.
    * @param immediateReorder <code>true</code> if so
    */
    public void setImmediateReorder (boolean immediateReorder) {
        if (this.immediateReorder == immediateReorder) return;
        this.immediateReorder = immediateReorder;
        if (immediateReorder) {
            if (permutation != null) {
                index.reorder(permutation);
                permutation = null;
                updateList();
            }
        }
    }

    // implementation of Customizer ............................................................

    /** Set the nodes to reorder.
    * @param bean must implement {@link Index}
    * @throws IllegalArgumentException if not
    */
    public void setObject (Object bean) {
        if (bean instanceof Index) {
            index = (Index)bean;
            // add weak listener to the Index
            nodeChangesL = new ChangeListener() {
                               public void stateChanged (ChangeEvent ev) {
                                   SwingUtilities.invokeLater(new Runnable() {
                                                                  public void run () {
                                                                      updateList();
                                                                  }
                                                              });
                               }
                           };
            updateList ();
            control.invalidate();
            validate();
            index.addChangeListener(WeakListener.change(nodeChangesL, index));
        } else {
            throw new IllegalArgumentException ();
        }
    }

    // I don't change any property...
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    /** Moves up. Performs differently according to
    * immediateReorder property value.
    */
    private void moveUp (final int position) {
        if (index == null) return;
        if (immediateReorder) {
            index.moveUp(position);
        } else {
            getPermutation();
            int temp = permutation[position];
            permutation[position] = permutation[position - 1];
            permutation[position - 1] = temp;
        }
    }

    /** Moves down. Performs differently according to
    * immediateReorder property value.
    */
    private void moveDown (final int position) {
        if (index == null) return;
        if (immediateReorder) {
            index.moveDown(position);
        } else {
            getPermutation();
            int temp = permutation[position];
            permutation[position] = permutation[position + 1];
            permutation[position + 1] = temp;
        }
    }

    /** Safe getter for permutation.
    * Initializes permutation to identical permutation if it is null.<br>
    * index variable must not be null when this method called */
    private int[] getPermutation () {
        if (permutation == null) {
            if (nodes == null)
                nodes = (Node[])index.getNodes().clone();
            permutation = new int[nodes.length];
            for (int i = 0; i < nodes.length; permutation[i] = i++);
        }
        return permutation;
    }

    /** Permute the list as given permutation dictates.
    * Sets selection to the given selected index.
    * Called from dropSupport as result of succesfull DnD operation.
    */
    void performReorder (int[] perm, int selected) {
        if (immediateReorder) {
            index.reorder(perm);
        } else {
            // merge current and reversed given permutation
            // (reverse given permutation first)
            int[] reversed = new int[perm.length];
            for (int i = 0; i < reversed.length; i++)
                reversed[perm[i]] = i;
            int[] orig = getPermutation();
            permutation = new int[orig.length];
            for (int i = 0; i < orig.length; i++) {
                permutation[i] = orig[reversed[i]];
                //        System.out.println(permutation[i] + " ----> " + i); // NOI18N
            }
        }
        updateList();
        control.setSelectedIndex(selected);
        control.repaint();
    }

    /** Implementation of drag functionality in
    * reorder dialog. */
    private static final class IndexedDragSource
                implements DragGestureListener,
        DragSourceListener {
        /** Asociated JList component where the drag will
        * take place */
        JList comp;
        /** User gesture that initiated the drag */
        DragGestureEvent dge;
        /** Out data flavor used to transfer the index */
        DataFlavor myFlavor;

        /** Creates drag source with asociated list where drag
        * will take place.
        * Also creates the default gesture and asociates this with 
        * given component */                              
        IndexedDragSource (JList comp) {
            this.comp = comp;
            // initialize gesture
            DragSource ds = DragSource.getDefaultDragSource();
            ds.createDefaultDragGestureRecognizer(
                comp, DnDConstants.ACTION_MOVE, this);
        }

        /** Initiating the drag */
        public void dragGestureRecognized(DragGestureEvent dge) {
            // check allowed actions
            if ((dge.getDragAction() & DnDConstants.ACTION_MOVE) == 0)
                return;
            // prepare transferable and start the drag
            int index = comp.locationToIndex(dge.getDragOrigin());
            // no index, then no dragging...
            if (index < 0)
                return;
            //      System.out.println("Starting drag..."); // NOI18N
            // create our flavor for transferring the index
            myFlavor =
                new DataFlavor(String.class,
                               NbBundle.getBundle(IndexedCustomizer.class).
                               getString("IndexedFlavor"));
            try {
                dge.startDrag(DragSource.DefaultMoveDrop,
                              new IndexTransferable(myFlavor, index), this);
                // remember the gesture
                this.dge = dge;
            } catch (InvalidDnDOperationException exc) {
                if (System.getProperty ("netbeans.debug.exceptions") != null) exc.printStackTrace();
                // PENDING notify user - cannot start the drag
            }
        }

        public void dragEnter(DragSourceDragEvent dsde) {
        }

        public void dragOver(DragSourceDragEvent dsde) {
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
        }

        public void dragExit(DragSourceEvent dse) {
        }

        public void dragDropEnd(DragSourceDropEvent dsde) {
        }

        /** Utility accessor */
        DragGestureEvent getDragGestureEvent () {
            return dge;
        }

    } // end of IndexedDragSource

    /** Implementation of drop functionality in
    * reorder dialog. */
    private static final class IndexedDropTarget
        implements DropTargetListener {
        /** Asociated JList component for dropping */
        JList comp;
        /** Cell renderer which renders the list items */
        IndexedListCellRenderer cellRenderer;
        /** Indexed dialog */
        IndexedCustomizer dialog;
        /** Drag source support instance */
        IndexedDragSource ids;
        /** last index dragged over */
        int lastIndex = -1;

        /** Creates the instance, makes given component active for
        * drop operation. */
        IndexedDropTarget (IndexedCustomizer dialog, IndexedDragSource ids) {
            this.dialog = dialog;
            this.comp = dialog.control;
            this.cellRenderer =
                (IndexedListCellRenderer)this.comp.getCellRenderer();
            this.ids = ids;
            DropTarget dt =
                new DropTarget(comp,
                               DnDConstants.ACTION_MOVE, this, true);
        }

        /** User is starting to drag over us */
        public void dragEnter(DropTargetDragEvent dtde) {
            if (!checkConditions(dtde))
                dtde.rejectDrag();
            else {
                lastIndex = comp.locationToIndex(dtde.getLocation());
                cellRenderer.draggingEnter(lastIndex,
                                           ids.getDragGestureEvent().getDragOrigin(),
                                           dtde.getLocation());
                comp.repaint(comp.getCellBounds(lastIndex, lastIndex));
            }
        }

        /** User drag over us */
        public void dragOver(DropTargetDragEvent dtde) {
            if (!checkConditions(dtde)) {
                dtde.rejectDrag();
                if (lastIndex >= 0) {
                    cellRenderer.draggingExit();
                    comp.repaint(comp.getCellBounds(lastIndex, lastIndex));
                    lastIndex = -1;
                }
            } else {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                int index = comp.locationToIndex(dtde.getLocation());
                if (lastIndex == index)
                    cellRenderer.draggingOver(index,
                                              ids.getDragGestureEvent().getDragOrigin(),
                                              dtde.getLocation());
                else {
                    if (lastIndex < 0)
                        lastIndex = index;
                    cellRenderer.draggingExit();
                    cellRenderer.draggingEnter(index,
                                               ids.getDragGestureEvent().getDragOrigin(),
                                               dtde.getLocation());
                    comp.repaint(comp.getCellBounds(lastIndex, index));
                    lastIndex = index;
                }
            }
        }

        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        /** User exits the dragging */
        public void dragExit(DropTargetEvent dte) {
            if (lastIndex >= 0) {
                cellRenderer.draggingExit();
                comp.repaint(comp.getCellBounds(lastIndex, lastIndex));
            }
        }

        /** Takes given index transferable and reorders
        * the items as appropriate (and if possible) */
        public void drop(DropTargetDropEvent dtde) {
            // reject all but local moves
            if ((DnDConstants.ACTION_MOVE != dtde.getDropAction()) ||
                    !dtde.isLocalTransfer())
                dtde.rejectDrop();
            int target = comp.locationToIndex(dtde.getLocation());
            if (target < 0) {
                dtde.rejectDrop();
                return;
            }
            Transferable t = dtde.getTransferable();
            //      System.out.println("Dropping..."); // NOI18N
            dtde.acceptDrop(DnDConstants.ACTION_MOVE);
            try {
                int source = Integer.parseInt(
                                 (String)t.getTransferData(ids.myFlavor));
                if (source != target) {
                    performReorder(source, target);
                    dtde.dropComplete(true);
                } else
                    dtde.dropComplete(false);
            } catch (IOException exc) {
                dtde.dropComplete(false);
            } catch (UnsupportedFlavorException exc) {
                dtde.dropComplete(false);
            } catch (NumberFormatException exc) {
                dtde.dropComplete(false);
            }
        }

        /** Actually performs the reordering which results from
        * succesfull drag-drop operation.
        * @param source 
        */
        void performReorder (int source, int target) {
            int[] myPerm = new int[comp.getModel().getSize()];
            // positions will change only between source and target
            // indexes, the rest remains the same
            for (int i = 0; i < Math.min(source, target); i++)
                myPerm[i] = i;
            for (int i = Math.max(source, target) + 1;
                    i < myPerm.length; i++)
                myPerm[i] = i;
            // reorder the rest
            myPerm[source] = target;
            if (source > target) {
                // dragging was up the list
                for (int i = target; i < source; i++)
                    myPerm[i] = i + 1;
            } else {
                // dragging was down the list
                for (int i = source + 1; i < target + 1; i++)
                    myPerm[i] = i - 1;
            }
            // and finally perform the reordering
            dialog.performReorder(myPerm, target);
        }

        /** @return True if conditions to continue with DnD
        * operation were satisfied */    
        boolean checkConditions (DropTargetDragEvent dtde) {
            int index = comp.locationToIndex(dtde.getLocation());
            return DnDConstants.ACTION_MOVE == dtde.getDropAction() &&
                   index >= 0;
        }

    } // end of IndexedDropTarget

    /** This class takes responsibility of presenting the
    * asociated index as transferable object.
    */
    private static final class IndexTransferable
        extends ExTransferable.Single {
        /** Index to transfer */
        int index;

        /** Creates transferable of given index */
        IndexTransferable (DataFlavor flavor, int index) {
            super(flavor);
            this.index = index;
        }

        /* Returns string representation of index */
        protected Object getData ()
        throws IOException, UnsupportedFlavorException {
            return String.valueOf(index);
        }
    } // end of IndexTransferable

    /** Implements drag and drop visual feedback
    * support for node list cell rendeder.
    */
    private static final class IndexedListCellRenderer
        extends NodeRenderer {
        /** Index of currently drag under cell in parent list */
        int dragIndex;
        /** True if move was up the list */
        boolean up;

        static final long serialVersionUID =-5526451942677242944L;

        protected static Border hasFocusBorder;

        static {
            hasFocusBorder = new LineBorder(UIManager.getColor("List.focusCellHighlight")); // NOI18N
        }

        /** Creates new renderer */
        IndexedListCellRenderer () {
            super();
            dragIndex = -1;
        }

        /** DnD operation enters, update visual
        * presentation to the drag under state */
        public void draggingEnter (int index, Point startingLoc, Point currentLoc) {
            //      System.out.println("Entering index: " + index); // NOI18N
            this.dragIndex = index;
            up = startingLoc.y > currentLoc.y;
        }

        /** DnD operation dragging over. */
        public void draggingOver (int index, Point startingLoc, Point currentLoc) {
        }

        /** DnD operation exits, reset visual state
        * back to the normal */
        public void draggingExit () {
            dragIndex = -1;
        }

        public Component getListCellRendererComponent (
            JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
            JComponent result = (JComponent)super.getListCellRendererComponent(
                                    list, value, index, isSelected, cellHasFocus);
            if (index == dragIndex) {
                //        System.out.println("Drawing...."); // NOI18N
                result.setBorder(hasFocusBorder);
            }
            return result;
        }

    } // end of IndexedListCellRenderer

    /** Implements autoscrolling support for JList.
    * However, JList must be contained in some JViewport.
    */
    private static class AutoscrollJList extends JList
        implements Autoscroll {
        /** Autoscroll insets */
        Insets scrollInsets;
        /** Insets for the autoscroll method to decide
        * whether really perform or not */
        Insets realInsets;
        /** Viewport we are in */
        JViewport viewport;

        static final long serialVersionUID =5495776972406885734L;
        /** notify the Component to autoscroll */
        public void autoscroll (Point cursorLoc) {
            JViewport viewport = getViewport();
            Point viewPos = viewport.getViewPosition();
            int viewHeight = viewport.getExtentSize().height;
            if ((cursorLoc.y - viewPos.y) <= realInsets.top)
                // scroll up
                viewport.setViewPosition(new Point(viewPos.x,
                                                   Math.max(viewPos.y - realInsets.top, 0)));
            else if ((viewPos.y + viewHeight - cursorLoc.y) <= realInsets.bottom)
                // scroll down
                viewport.setViewPosition(new Point(viewPos.x,
                                                   Math.min(viewPos.y + realInsets.bottom,
                                                            this.getHeight() - viewHeight)));
        }

        /** @return the Insets describing the autoscrolling
        * region or border relative to the geometry of the
        * implementing Component.
        */
        public Insets getAutoscrollInsets () {
            if (scrollInsets == null) {
                int height = this.getHeight();
                scrollInsets = new Insets(height, 0, height, 0);
                // compute also autoscroll insets for viewport
                Rectangle rect = getViewport().getViewRect();
                realInsets = new Insets(15, 0, 15, 0);
            }
            return scrollInsets;
        }

        /** Asociates given viewport with this list.
        * (Viewport is usually parent containing this component) */
        JViewport getViewport () {
            if (viewport == null) {
                Component comp = this;
                while (!(comp instanceof JViewport) && (comp != null)) {
                    comp = comp.getParent();
                }
                viewport = (JViewport)comp;
            }
            return viewport;
        }

    } // end of AutoscrollJViewport

}

/*
 * Log
 *  21   Gandalf   1.20        1/15/00  David Simonek   DnD disabled completely
 *  20   Gandalf   1.19        1/13/00  Jesse Glick     NOI18N
 *  19   Gandalf   1.18        1/12/00  Jesse Glick     NOI18N
 *  18   Gandalf   1.17        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/5/99  David Simonek   DnD disabled for unixes
 *  15   Gandalf   1.14        8/27/99  Jaroslav Tulach 
 *  14   Gandalf   1.13        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  13   Gandalf   1.12        7/25/99  Ian Formanek    Exceptions printed to 
 *       console only on "netbeans.debug.exceptions" flag
 *  12   Gandalf   1.11        6/10/99  Jaroslav Tulach Commented println
 *  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   Gandalf   1.9         4/29/99  David Simonek   now compiled ok again
 *  9    Gandalf   1.8         4/21/99  David Simonek   drag and drop support 
 *       added
 *  8    Gandalf   1.7         4/9/99   Ian Formanek    Removed debug printlns
 *  7    Gandalf   1.6         4/2/99   David Simonek   listening on Index 
 *       changes added
 *  6    Gandalf   1.5         3/27/99  David Simonek   
 *  5    Gandalf   1.4         3/17/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/9/99   Jaroslav Tulach Does not need button bar
 *       button
 *  3    Gandalf   1.2         3/4/99   Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
