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

package org.netbeans.modules.form.palette;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.awt.ToolbarToggleButton;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.InstanceSupport;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.form.*;

/**
* The ComponentPalette is a visual component that manages
* a add component/selection mode for FormEditor.
* The current state of the palette is returned by getSelectedItem() method.
*
* @author   Ian Formanek, Jan Jancura
*/
public class ComponentPalette extends JPanel {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 6131327264550974618L;

    private final static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (ComponentPalette.class);

    private static final int DEFAULT_WIDTH = 420;
    private static final int MAX_WIDTH = 1024;
    private static String KEY_PALETTE_MODE = "PALETTE MODE"; // NOI18N
    private static String KEY_SELECTED_BUTTON = "SELECTED BUTTON"; // NOI18N
    private static String KEY_SELECTED_PALETTE_ITEM = "SELECTED PALETTE ITEM"; // NOI18N
    private static ComponentPalette sharedInstance;

    // -----------------------------------------------------------------------------
    // Private variables

    private NodeListener categoryNodeListener;

    //private static int                                    mode = PaletteAction.MODE_SELECTION;
    /** The button that represents Selection mode */
    private ToolbarToggleButton                           selectionButton;
    /** The button that represents Selection mode */
    private ToolbarToggleButton                           connectionButton;
    /** Points to the tabbs */
    private JTabbedPane                                   tabbedArea = null;

    /** Provides palette content (Palette categories and palette items). */
    private PaletteNode                                   paletteNode;

    /** A mapping PaletteItem -> ToolbarToggleButton */
    private HashMap                                       itemToButton = new HashMap(50);
    /** A mapping ToolbarToggleButton -> PaletteItem 
     * @associates PaletteItem*/
    private HashMap                                       buttonToItem = new HashMap(50);
    /** A mapping Category Node -> Container in palette containing buttons for items in this category 
     * @associates ScrollPalette*/
    private HashMap                                       categoryToPanel = new HashMap (10);

    // PBUZEK: move into paletteAction shared values:
    /** The currently selected button */
    //private static ToolbarToggleButton                    selectedButton;
    /** The currently selected palette node */
    //private static PaletteItem                            selectedPaletteItem = null;

    private ActionListener                                buttonListener;

    /** task that controls the updating of component pallete */
    private RequestProcessor.Task refreshTask;

    /** set of nodes that has to be updated 
     * @associates PaletteCategoryNode*/
    private Set refreshNodes = new HashSet (11);

    /** item that signals that the component is preparing or null */
    private JLabel wait;

    // -----------------------------------------------------------------------------
    // Constructors

    static final long serialVersionUID =7249158494688903041L;
    /**
    * Constructs a new empty ComponentPalette.
    */
    public ComponentPalette () {
        if (sharedInstance == null) {
            sharedInstance = this;
        }

        buttonListener = new ActionListener () {
                             public void actionPerformed (ActionEvent evt) {
                                 ToolbarToggleButton pressedButton = (ToolbarToggleButton) evt.getSource();
                                 if (pressedButton == selectionButton) {
                                     if (getMode () == PaletteAction.MODE_SELECTION) {
                                         setSelectedButton (selectionButton);
                                         selectionButton.setSelected (true);
                                         return;
                                     }
                                     setSelectedItem (null);
                                     setMode (PaletteAction.MODE_SELECTION);
                                 } else if (pressedButton == connectionButton) {
                                     if (getMode () == PaletteAction.MODE_CONNECTION) {
                                         setSelectedButton (connectionButton);
                                         connectionButton.setSelected (true);
                                         return;
                                     }
                                     setSelectedItem (null);
                                     setMode (PaletteAction.MODE_CONNECTION);
                                 } else {
                                     if (getSelectedButton () == pressedButton) {
                                         setSelectedItem (null);
                                         setMode (PaletteAction.MODE_SELECTION);
                                     } else {
                                         setSelectedItem ((PaletteItem)buttonToItem.get (evt.getSource ()));
                                         setMode (PaletteAction.MODE_ADD);
                                     }
                                 }
                             }
                         };

        paletteNode = PaletteNode.getPaletteNode ();
        paletteNode.addNodeListener (new NodeAdapter () {
                                         public void childrenAdded (NodeMemberEvent evt) {
                                             updatePaletteInEventQueue ();
                                         }

                                         public void childrenRemoved (NodeMemberEvent evt) {
                                             updatePaletteInEventQueue ();
                                         }

                                         public void childrenReordered (NodeReorderEvent evt) {
                                             updatePaletteInEventQueue ();
                                         }
                                     }
                                    );

        categoryNodeListener = new NodeAdapter () {
                                   public void childrenAdded (NodeMemberEvent evt) {
                                       updateCategoryInEventQueue ((PaletteCategoryNode)evt.getSource (), 200);
                                   }

                                   public void childrenRemoved (NodeMemberEvent evt) {
                                       updateCategoryInEventQueue ((PaletteCategoryNode)evt.getSource (), 200);
                                   }

                                   public void childrenReordered (NodeReorderEvent evt) {
                                       updateCategoryInEventQueue ((PaletteCategoryNode)evt.getSource (), 200);
                                   }

                                   public void propertyChange (PropertyChangeEvent evt) {
                                       if (Node.PROP_NAME.equals (evt.getPropertyName ()) || Node.PROP_DISPLAY_NAME.equals (evt.getPropertyName ())) {
                                           updateCategoryInEventQueue ((PaletteCategoryNode)evt.getSource (), 200);
                                       }
                                   }
                               };

        // create selection button ...
        setLayout (new BorderLayout ());

        JPanel selectionPanel = new JPanel ();
        selectionPanel.setLayout (new BorderLayout ());
        selectionPanel.setBorder (new javax.swing.border.EmptyBorder (3, 3, 3, 3));

        selectionButton = new ToolbarToggleButton(
                              new ImageIcon(getClass ().getResource ("/org/netbeans/modules/form/resources/selectionMode.gif")), // NOI18N
                              true);
        selectionButton.addActionListener(buttonListener);
        selectionButton.setToolTipText (bundle.getString ("CTL_SelectionButtonHint"));
        HelpCtx.setHelpIDString (selectionButton, ComponentPalette.class.getName () + ".selectionButton"); // NOI18N
        selectionPanel.add (selectionButton, BorderLayout.NORTH);

        connectionButton = new ToolbarToggleButton(
                               new ImageIcon(getClass ().getResource ("/org/netbeans/modules/form/resources/connectionMode.gif")), // NOI18N
                               false);
        connectionButton.addActionListener(buttonListener);
        connectionButton.setToolTipText (bundle.getString ("CTL_ConnectionButtonHint"));
        HelpCtx.setHelpIDString (connectionButton, ComponentPalette.class.getName () + ".connectionButton"); // NOI18N
        selectionPanel.add (connectionButton, BorderLayout.SOUTH);

        add (selectionPanel, BorderLayout.WEST);

        setSelectedButton (selectionButton);

        HelpCtx.setHelpIDString (this, ComponentPalette.class.getName ());

        wait = new JLabel (bundle.getString("MSG_PreparingPalette"));
        wait.setHorizontalAlignment(SwingConstants.CENTER);
        add (wait, BorderLayout.CENTER);


        refreshTask = RequestProcessor.createRequest (new Runnable () {
                          public void run () {
                              Set s = refreshNodes;
                              refreshNodes = new HashSet (11);
                              if (tabbedArea != null) {
                                  remove (tabbedArea);
                              }

                              Iterator it = s.iterator ();
                              while (it.hasNext ()) {
                                  PaletteCategoryNode node = (PaletteCategoryNode)it.next ();
                                  updateCategory (node);
                              }


                              if (wait != null) {
                                  remove (wait);
                                  wait = null;
                              }

                              add(tabbedArea, BorderLayout.CENTER);
                              invalidate ();
                              validate ();
                              repaint ();
                          }
                      });
        refreshTask.setPriority (0);

        updatePaletteInEventQueue ();
    }

    public static ComponentPalette getDefault () {
        if (sharedInstance == null) {
            sharedInstance = new ComponentPalette ();
        }
        return sharedInstance;
    }


    public static DataFolder getPaletteFolder () {
        return PaletteNode.getPaletteFolder ();
    }

    private void updatePaletteInEventQueue () {
        // it might be invoked from different thread, so we must use invokeLater to perform visual operations
        EventQueue.invokeLater(new Runnable () {
                                   public void run () {
                                       updatePalette ();
                                   }
                               });
    }

    private void updateCategoryInEventQueue (final PaletteCategoryNode node, int time) {
        refreshNodes.add (node);
        refreshTask.schedule (time);
    }

    // -----------------------------------------------------------------------------
    // Public interface

    public synchronized String[] getPaletteCategories () {
        Object[] nodes = categoryToPanel.keySet ().toArray (new PaletteCategoryNode[0]);
        String[] categoryNames = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            categoryNames[i] = ((PaletteCategoryNode)nodes[i]).getDisplayName ();
        }
        return categoryNames;
    }

    public PaletteItem[] getAllItems () {
        Set itemsSet = itemToButton.keySet ();
        PaletteItem[] items = new PaletteItem[itemsSet.size ()];
        itemsSet.toArray (items);
        return items;
    }

    // -----------------------------------------------------------------------------
    // Other methods

    public Dimension getPreferredSize () {
        Dimension pref = super.getPreferredSize ();
        return new Dimension (Math.max (pref.width, DEFAULT_WIDTH), pref.height);
    }

    /**
     * Gets the maximum size of this component.
     * @return A dimension object indicating this component's maximum size.
     * @see #getMinimumSize
     * @see #getPreferredSize
     * @see LayoutManager
     */
    public Dimension getMaximumSize () {
        return new Dimension (MAX_WIDTH, super.getPreferredSize ().height);
    }

    public Dimension getMinimumSize () {
        return new Dimension (DEFAULT_WIDTH, super.getPreferredSize ().height);
    }


    /**
    * Repaints palette with datas from paletteContext variable.
    */
    public synchronized void updatePalette () {

        //    if (tabbedArea != null) remove (tabbedArea);
        // first remove node listeners on existing nodes
        for (Iterator it = categoryToPanel.keySet ().iterator (); it.hasNext (); ) {
            PaletteCategoryNode ctg = (PaletteCategoryNode) it.next ();
            ctg.removeNodeListener (categoryNodeListener);
        }

        for (Iterator it = buttonToItem.keySet ().iterator (); it.hasNext (); ) {
            ((ToolbarToggleButton)it.next ()).removeActionListener (buttonListener);
        }

        itemToButton.clear ();

        buttonToItem.clear ();
        categoryToPanel.clear ();

        // Tabbs and switching ...........................................
        PaletteCategoryNode[] categories = paletteNode.getPaletteCategories ();

        if (tabbedArea != null) {
            remove (tabbedArea);
        }

        tabbedArea = new JTabbedPane();
        tabbedArea.addChangeListener (new ChangeListener () {
                                          public void stateChanged (ChangeEvent evt) {
                                              if (getMode () == PaletteAction.MODE_ADD) {
                                                  setSelectedItem (null); // set selection mode if tabs are switched
                                                  setMode (PaletteAction.MODE_SELECTION);
                                              }
                                          }
                                      }
                                     );

        // create component categories
        for (int i = 0; i < categories.length; i++) {
            // create palette buttons
            ScrollPalettePanel palette = new ScrollPalettePanel();
            palette.setLayout (new FlowLayout (FlowLayout.LEFT));

            categories[i].addNodeListener (categoryNodeListener);

            ScrollPalette sp = new ScrollPalette(palette);
            categoryToPanel.put (categories[i], sp);
            tabbedArea.addTab(categories[i].getDisplayName(), sp);

            int cnt = categories[i].getChildren ().getNodesCount ();

            updateCategoryInEventQueue (categories[i], cnt == 0 ? 5000 : 500);
        }
        /*
            add(tabbedArea, BorderLayout.CENTER);
            invalidate ();
            validate ();
            repaint ();
        */    
    }

    private void updateCategory (PaletteCategoryNode category) {
        String newName = category.getDisplayName ();
        ScrollPalette sp = (ScrollPalette)categoryToPanel.get(category);
        if (sp == null) return;
        final Container categoryPanel = (Container)sp.getView ();
        int tabIndex = tabbedArea.indexOfComponent (sp);

        // update tab name if necessary
        //    System.out.println ("Index of "+newName+", index: "+tabIndex); // NOI18N
        if ((tabIndex != -1) && !newName.equals (tabbedArea.getTitleAt (tabIndex))) {
            tabbedArea.setTitleAt (tabIndex, newName);
        }

        Component[] buttons = categoryPanel.getComponents ();
        for (int i = 0; i < buttons.length; i++) {
            ((ToolbarToggleButton)buttons[i]).removeActionListener (buttonListener);
            PaletteItem it = (PaletteItem)buttonToItem.get (buttons[i]);
            buttonToItem.remove (buttons[i]);
            itemToButton.remove (it);
        }


        Node[] categoryItems = category.getCategoryComponents();

        final java.util.List toAdd = new LinkedList ();

        for (int j = 0; j < categoryItems.length; j++) {
            Node itemNode = categoryItems[j];
            PaletteItem palItem = null;
            // 1. try InstanceDataObject
            InstanceDataObject ido = (InstanceDataObject)itemNode.getCookie (InstanceDataObject.class);
            if (ido != null) {
                try {
                    palItem = new PaletteItem (ido);
                } catch (java.io.IOException e) {
                    e.printStackTrace();   // XXX
                    continue; // problems creating -> do not present in palette [PENDING]
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();  // XXX
                    continue; // problems creating -> do not present in palette [PENDING]
                }

            } else {
                // 2. try InstanceCookie
                InstanceCookie ic = (InstanceCookie)itemNode.getCookie (InstanceCookie.class);
                if (ic != null) {
                    try {
                        palItem = new PaletteItem (ic);
                    } catch (java.io.IOException e) {
                        continue; // problems creating -> do not present in palette [PENDING]
                    } catch (ClassNotFoundException e) {
                        continue; // problems creating -> do not present in palette [PENDING]
                    }
                } else {
                    continue; // not suitable to be presented in palette
                }
            }
            ToolbarToggleButton button = new PaletteButton (itemNode, palItem);
            button.addActionListener (buttonListener);
            toAdd.add (button);
            itemToButton.put (palItem, button);
            buttonToItem.put (button, palItem);
        }

        EventQueue.invokeLater(new Runnable () {
                                   public void run () {
                                       categoryPanel.removeAll ();
                                       Iterator it = toAdd.iterator ();
                                       while (it.hasNext ()) {
                                           Component comp = (Component)it.next();
                                           categoryPanel.add(comp);
                                       }
                                       categoryPanel.invalidate ();
                                       validate ();
                                       repaint ();
                                   }
                               });
    }

    private ToolbarToggleButton getSelectedButton () {
        return (ToolbarToggleButton) PaletteAction.get (PaletteAction.class). getValue (KEY_SELECTED_BUTTON);
    }

    private void setSelectedButton (ToolbarToggleButton button) {
        PaletteAction.get (PaletteAction.class).putValue (KEY_SELECTED_BUTTON, button);
    }

    public int getMode () {
        Integer mode = (Integer) PaletteAction.get (PaletteAction.class).getValue (KEY_PALETTE_MODE);
        if(mode == null) {
            return PaletteAction.MODE_SELECTION;
        } else {
            return mode.intValue();
        }
    }

    public void setMode (int newMode) {
        PaletteAction.get (PaletteAction.class).putValue (KEY_PALETTE_MODE, new Integer (newMode));
        switch (newMode) {
        case PaletteAction.MODE_SELECTION:
            getSelectedButton ().setSelected (false);
            setSelectedButton (selectionButton);
            selectionButton.setSelected (true);
            setSelectedItem (null);
            break;
        case PaletteAction.MODE_CONNECTION:
            getSelectedButton ().setSelected (false);
            setSelectedButton (connectionButton);
            connectionButton.setSelected (true);
            setSelectedItem (null);

            org.openide.nodes.Node root = FormEditor.getComponentInspector ().getExplorerManager ().getRootContext ();
            RADComponentCookie cookie = (RADComponentCookie)root.getCookie (RADComponentCookie.class);
            if (cookie != null) {
                FormManager2 man = cookie.getRADComponent ().getFormManager ();
                man.cancelSelection ();
            }

            break;
        case PaletteAction.MODE_ADD:
            ToolbarToggleButton newButton = (ToolbarToggleButton) itemToButton.get (getSelectedItem ());
            getSelectedButton ().setSelected (false);
            setSelectedButton (newButton);
            newButton.setSelected (true);
            break;
        }
    }

    /** Returns the currently selected PaletteNode (that represents a
    * JavaBean to be added to the form) or null for selection mode.
    * @return the currently selected PaletteNode or null for selection mode
    */
    public PaletteItem getSelectedItem () {
        return (PaletteItem) PaletteAction.get (PaletteAction.class).getValue (KEY_SELECTED_PALETTE_ITEM);
    }

    /** Sets the current add component or selection mode.
    * @param value The new add component or null for selection mode
    */
    public void setSelectedItem (PaletteItem value) {
        PaletteAction.get (PaletteAction.class).putValue (KEY_SELECTED_PALETTE_ITEM, value);
    }

}

/*
 * Log
 *  38   Gandalf   1.37        3/7/00   Tran Duc Trung  fix #5791: cannot add 
 *       serialized bean to component palette
 *  37   Gandalf   1.36        1/20/00  Libor Kramolis  
 *  36   Gandalf   1.35        1/18/00  Libor Kramolis  
 *  35   Gandalf   1.34        1/15/00  Ian Formanek    Bigger default width of 
 *       the component palette
 *  34   Gandalf   1.33        1/13/00  Ian Formanek    NOI18N #2
 *  33   Gandalf   1.32        1/5/00   Ian Formanek    NOI18N
 *  32   Gandalf   1.31        12/9/99  Jaroslav Tulach #4699
 *  31   Gandalf   1.30        11/27/99 Patrik Knakal   
 *  30   Gandalf   1.29        11/24/99 Pavel Buzek     static fields moved into
 *       PaletteAction
 *  29   Gandalf   1.28        11/15/99 Pavel Buzek     selectedButton made 
 *       static
 *  28   Gandalf   1.27        11/10/99 Pavel Buzek     mode and 
 *       selectedPaletteItem properties made static
 *  27   Gandalf   1.26        11/5/99  Jaroslav Tulach ComponentPalette.getDefault
 *        is back.
 *  26   Gandalf   1.25        11/4/99  Jaroslav Tulach Component palette is 
 *       faster/better/etc.
 *  25   Gandalf   1.24        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   Gandalf   1.23        10/11/99 Ian Formanek    Fixed problem with 
 *       doubling ComponentPalette tabs
 *  23   Gandalf   1.22        10/10/99 Ian Formanek    Fixed bug 3823 - After 
 *       adding a category to the component palette in Global Settings, new tab 
 *       does not appear and is unresponsive until it is hidden and reshown.
 *  22   Gandalf   1.21        7/28/99  Ian Formanek    Patched bug where 
 *       NullPointerException was sometimes thrown during installing Form Module
 *  21   Gandalf   1.20        7/20/99  Jesse Glick     Context help 
 *       (optimization for real this time).
 *  20   Gandalf   1.19        7/20/99  Jesse Glick     Context help 
 *       (optimization).
 *  19   Gandalf   1.18        7/19/99  Jesse Glick     Context help.
 *  18   Gandalf   1.17        7/16/99  Ian Formanek    Fixed bug 1855 - 
 *       Tooltips of borders in Component Pallete print *BorderInfo probably 
 *       instead of *Border .
 *  17   Gandalf   1.16        6/11/99  Ian Formanek    This version is part of 
 *       build 337
 *  16   Gandalf   1.15        6/10/99  Ian Formanek    
 *  15   Gandalf   1.14        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        6/7/99   Ian Formanek    Palette nodes extend 
 *       FolderNode
 *  13   Gandalf   1.12        6/7/99   Ian Formanek    Better support of 
 *       instances
 *  12   Gandalf   1.11        6/4/99   Ian Formanek    
 *  11   Gandalf   1.10        5/24/99  Ian Formanek    Provided static access 
 *       to Palette folder
 *  10   Gandalf   1.9         5/20/99  Ian Formanek    Fixed multiplication of 
 *       PaletteItems
 *  9    Gandalf   1.8         5/14/99  Ian Formanek    
 *  8    Gandalf   1.7         5/11/99  Ian Formanek    Build 318 version
 *  7    Gandalf   1.6         5/4/99   Ian Formanek    package change 
 *       (formeditor -> ..)
 *  6    Gandalf   1.5         4/26/99  Ian Formanek    
 *  5    Gandalf   1.4         4/4/99   Ian Formanek    Fixed too many 
 *       unnecessary revalidations
 *  4    Gandalf   1.3         4/2/99   Ian Formanek    Fixed synchronization 
 *       problem causing deadlock during startup
 *  3    Gandalf   1.2         3/30/99  Ian Formanek    Finally nearly works
 *  2    Gandalf   1.1         3/24/99  Ian Formanek    
 *  1    Gandalf   1.0         3/24/99  Ian Formanek    
 * $
 */
