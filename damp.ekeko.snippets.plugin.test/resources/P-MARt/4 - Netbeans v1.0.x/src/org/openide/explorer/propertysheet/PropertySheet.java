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

package org.openide.explorer.propertysheet;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.awt.SplittedPanel;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarButton;
import org.openide.awt.ToolbarToggleButton;
import org.openide.awt.MouseUtils;
import org.openide.awt.JPopupMenuPlus;
import org.openide.util.datatransfer.PasteType;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;

/**
* Implements a "property sheet" for a set of selected beans.
*
* <P>
* <TABLE BORDER COLS=3 WIDTH=100%>
* <TR><TH WIDTH=15%>Property<TH WIDTH=15%>Property Type<TH>Description
* <TR><TD> <code>paintingStyle</code> <TD> <code>int</code>     <TD> style of painting properties ({@link #ALWAYS_AS_STRING}, {@link #STRING_PREFERRED}, {@link #PAINTING_PREFERRED})
* <TR><TD> <code>currentPage</code>   <TD> <code>int</code>     <TD> currently showed page (e.g. properties, expert, events)
* <TR><TD> <code>expert</code>        <TD> <code>boolean</code> <TD> expert mode as in the JavaBeans specifications
* </TABLE>
*
* @author   Jan Jancura, Jaroslav Tulach
* @version  1.23, Sep 07, 1998
*/
public class PropertySheet extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7698351033045864945L;


    // public constants ........................................................

    /** Property giving current sorting mode. */
    public static final String    PROPERTY_SORTING_MODE = "sortingMode"; // NOI18N
    /** Property giving current value color. */
    public static final String    PROPERTY_VALUE_COLOR = "valueColor"; // NOI18N
    /** Property giving current disabled property color. */
    public static final String    PROPERTY_DISABLED_PROPERTY_COLOR = "disabledPropertyColor"; // NOI18N
    /** Property with the current page index. */
    public static final String    PROPERTY_CURRENT_PAGE = "currentPage"; // NOI18N
    /** Property for "plastic" mode. */ // NOI18N
    public static final String    PROPERTY_PLASTIC = "plastic"; // NOI18N
    /** Property for the painting style. */
    public static final String    PROPERTY_PROPERTY_PAINTING_STYLE = "propertyPaintingStyle"; // NOI18N
    /** Property for whether only writable properties should be displayed. */
    public static final String    PROPERTY_DISPLAY_WRITABLE_ONLY = "displayWritableOnly"; // NOI18N

    /** Constant for showing properties as a string always. */
    public static final int       ALWAYS_AS_STRING = 1;
    /** Constant for preferably showing properties as string. */
    public static final int       STRING_PREFERRED = 2;
    /** Constant for preferably painting property values. */
    public static final int       PAINTING_PREFERRED = 3;

    /** Constant for unsorted sorting mode. */
    public static final int       UNSORTED = 0;
    /** Constant for by-name sorting mode. */
    public static final int       SORTED_BY_NAMES = 1;
    /** Constant for by-type sorting mode. */
    public static final int       SORTED_BY_TYPES = 2;

    /** Icon for the toolbar. */
    static protected Icon         iNoSort;
    static protected Icon         iAlphaSort;
    static protected Icon         iTypeSort;
    static protected Icon         iDisplayWritableOnly;
    static protected Icon         iCustomize;
    /** "No properties" text. */ // NOI18N
    private static String         text;

    /** Standart variable for localisation. */
    static java.util.ResourceBundle bundle = NbBundle.getBundle (
                PropertySheet.class
            );

    static {
        iNoSort = new ImageIcon (PropertySheet.class.getResource ("/org/openide/resources/propertysheet/unsorted.gif")); // NOI18N
        iAlphaSort = new ImageIcon (PropertySheet.class.getResource ("/org/openide/resources/propertysheet/sortedByNames.gif")); // NOI18N
        iTypeSort = new ImageIcon (PropertySheet.class.getResource ("/org/openide/resources/propertysheet/sortedByTypes.gif")); // NOI18N
        iDisplayWritableOnly = new ImageIcon (PropertySheet.class.getResource ("/org/openide/resources/propertysheet/showWritableOnly.gif")); // NOI18N
        iCustomize = new ImageIcon (PropertySheet.class.getResource ("/org/openide/resources/propertysheet/customize.gif")); // NOI18N

        text = getString ("CTL_NoProperties");
    }

    static String getString (String str) {
        return bundle.getString (str);
    }

    /**
    * Returns description of the property. This description is showen
    * in the tool tip for example.
    */
    static String getDescription (PropertyDetails propertyDetails) {
        StringBuffer sb = new StringBuffer ();
        String shortDescription = propertyDetails.getShortDescription ();
        if (shortDescription == null) shortDescription = ""; // NOI18N
        // Handle Swing 1.1.1 HTML tooltips.
        if (shortDescription.startsWith ("<html>")) { // NOI18N
            shortDescription = shortDescription.substring (6);
            sb.append ("<html>"); // NOI18N
        }
        if (propertyDetails.getPropertyEditor () == null)
            sb.append (getString ("CTL_NoPropertyEditor")).append (' ');
        else {
            // [PENDING] should be localized
            try {
                sb.append (propertyDetails.canRead () ? "(r/" : "(-/"); // NOI18N
            } catch (Exception e) {
                sb.append ("(?/"); // NOI18N
            }
            try {
                sb.append (propertyDetails.canWrite () ? "w) " : "-) "); // NOI18N
            } catch (Exception e) {
                sb.append ("?) "); // NOI18N
            }
        }
        sb.append (shortDescription);
        return new String (sb);
    }

    /**
    * Returns description of the property value. This description is showen
    * in the tool tip for example.
    */
    static String getValueDescription (PropertyDetails propertyDetails) {
        return Utilities.getClassName (propertyDetails.getValueType ());
    }

    /** Comparator which compares types */
    private final static java.util.Comparator SORTER_TYPE = new java.util.Comparator () {
                public int compare (Object l, Object r) {
                    String s1 = ((PropertyDetails)l).getValueType().getName();
                    String s2 = ((PropertyDetails)r).getValueType().getName();
                    int s = s1.compareToIgnoreCase (s2);
                    if (s != 0) return s;

                    s1 = ((PropertyDetails)l).getName();
                    s2 = ((PropertyDetails)r).getName();
                    return s1.compareToIgnoreCase(s2);
                }
            };

    /** Comparator which compares PropertyDeatils names */
    private final static java.util.Comparator SORTER_NAME = new java.util.Comparator () {
                public int compare (Object l, Object r) {
                    String s1 = ((PropertyDetails)l).getName();
                    String s2 = ((PropertyDetails)r).getName();
                    return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
                }
            };


    // properties ...............................................................................

    /** Style of showing property value (text x painting). */
    private int                             propertyPaintingStyle = PropertySheetSettings.propertyPaintingStyle;

    /** Is plastic property value. */
    private boolean                         plastic = PropertySheetSettings.plastic;

    /** When it's true only writable properties are showen. */
    private boolean                         displayWritableOnly = PropertySheetSettings.displayWritableOnly;

    private java.util.Comparator            sorter = null;

    private int                             sortingMode = PropertySheetSettings.sortingMode;

    /** Foreground color of values. */
    private Color                           valueColor = PropertySheetSettings.valueColor;

    /** Foreground color of disabled properties. */
    private Color                           disabledPropertyColor = PropertySheetSettings.disabledColor;

    // private helper variables ..................................................................

    /** Stores info. about currently selected beans */
    private transient BeansDetails          beansDetails;

    /** Names of pages. */
    private transient String[]              tab;

    /** Hints of pages. */
    private transient String[]              hints;

    /** Stores objects for showing property values. */
    private transient PropertyDisplayer[][] propertyDisplayer;

    /** Stores info. about properties. */
    private transient PropertyDetails[][]   propertyDetails;

    /** Values of all properties. */
    private transient PropertyValue[][]     propertyValue;

    /** Currently selected page of prop. sheet. */
    private int                             pageIndex = -1;

    /** Lastly selected name of page of prop. sheet. */
    private String                          selectedTabName = null;

    /** Synch. lock. */
    private transient Integer               lock = new Integer (0);

    private transient PropertyDisplayer     lastSelectedLine;

    private transient boolean               ignorePropertyChanges = false;


    // private variables for visual controls ...........................................

    private transient JTabbedPane           pages;
    private transient EmptyPanel            emptyPanel;
    private transient NamesPanel[]          namesPanel = new NamesPanel [0];
    private transient NamesPanel[]          valuesPanel = new NamesPanel [0];
    private transient ToolbarToggleButton   bNoSort, bAlphaSort, bTypeSort, bDisplayWritableOnly;
    private transient ToolbarButton         customizer;

    private transient PropertyChangeListener settingsListener;
    private transient BeansListener         beansListener = new BeansListener ();
    private transient ChangeListener        tabListener =
        new ChangeListener () {
            public void stateChanged (ChangeEvent e) {
                int index = pages.getSelectedIndex ();
                setCurrentPage (index);
            }
        };

    private transient JPopupMenu popupMenu;

    {
        popupMenu = new JPopupMenuPlus ();
        //    popupMenu.add (new CopyAction ().getPopupPresenter ());
        //    popupMenu.add (new PasteAction ().getPopupPresenter ());
        //    popupMenu.addSeparator ();
        popupMenu.add (new SetDefaultValueAction ().getPopupPresenter ());
    }


    // init .............................................................................

    /* When the view is deserialized, it is called before the initializeManager method.
    * @see #initializeManager */
    /** Create and initialize the property sheet.
    * When the {@link PropertySheetView} is created, this is called from its constructor.
    */
    public PropertySheet() {
        propertyDetails = new PropertyDetails [0][];
        propertyDisplayer = new PropertyDisplayer [0][];
        tab = new String [0];

        // visual problems ........................................
        setLayout (new BorderLayout ());

        pages = new JTabbedPane ();
        emptyPanel = new EmptyPanel (text);
        pages.setTabPlacement (JTabbedPane.BOTTOM);
        add ("Center", emptyPanel); // NOI18N

        // Toolbar
        JPanel p = new JPanel (new FlowLayout (FlowLayout.LEFT, 0, 0));
        p.add (bNoSort = new ToolbarToggleButton (iNoSort));
        bNoSort.setToolTipText (getString ("CTL_NoSort"));
        bNoSort.setSelected (true);
        bNoSort.addActionListener (new ActionListener () {
                                       public void actionPerformed (ActionEvent e) {
                                           sortingMode = UNSORTED;
                                           sorter = null;
                                           bNoSort.setSelected (true);
                                           bAlphaSort.setSelected (false);
                                           bTypeSort.setSelected (false);
                                           if (pageIndex != -1)
                                               resort (pageIndex);
                                       }
                                   });

        p.add (bAlphaSort = new ToolbarToggleButton (iAlphaSort));
        bAlphaSort.setToolTipText (getString ("CTL_AlphaSort"));
        bAlphaSort.addActionListener (new ActionListener () {
                                          public void actionPerformed (ActionEvent e) {
                                              sortingMode = SORTED_BY_NAMES;
                                              sorter = SORTER_NAME;
                                              bNoSort.setSelected (false);
                                              bAlphaSort.setSelected (true);
                                              bTypeSort.setSelected (false);
                                              if (pageIndex != -1)
                                                  resort (pageIndex);
                                          }
                                      });

        p.add (bTypeSort = new ToolbarToggleButton (iTypeSort));
        bTypeSort.setToolTipText (getString ("CTL_TypeSort"));
        bTypeSort.addActionListener (new ActionListener () {
                                         public void actionPerformed (ActionEvent e) {
                                             sortingMode = SORTED_BY_TYPES;
                                             sorter = SORTER_TYPE;
                                             bNoSort.setSelected (false);
                                             bAlphaSort.setSelected (false);
                                             bTypeSort.setSelected (true);
                                             if (pageIndex != -1)
                                                 resort (pageIndex);
                                         }
                                     });
        try {
            setSortingMode (sortingMode);
        } catch (PropertyVetoException e) {
            try {
                setSortingMode (UNSORTED);
            } catch (PropertyVetoException ee) {
            }
        }

        Toolbar.Separator ts = new Toolbar.Separator ();
        p.add (ts);
        ts.updateUI ();

        bDisplayWritableOnly = new ToolbarToggleButton (
                                   iDisplayWritableOnly,
                                   displayWritableOnly
                               );
        bDisplayWritableOnly.setToolTipText (getString ("CTL_VisibleWritableOnly"));
        bDisplayWritableOnly.addItemListener (new ItemListener() {
                                                  public void itemStateChanged (ItemEvent e) {
                                                      setDisplayWritableOnly (bDisplayWritableOnly.isSelected ());
                                                  }
                                              });
        p.add(bDisplayWritableOnly);

        ts = new Toolbar.Separator ();
        p.add (ts);
        ts.updateUI ();

        p.add (customizer = new ToolbarButton (iCustomize));
        customizer.setToolTipText (getString ("CTL_Customize"));
        customizer.setEnabled (false);
        customizer.addActionListener (new ActionListener () {
                                          public void actionPerformed (ActionEvent e) {
                                              invokeCustomization ();
                                          }
                                      });
        add ("North", p); // NOI18N

        // close input component on ESC
        registerKeyboardAction (new ActionListener () {
                                    public void actionPerformed (ActionEvent e) {
                                        if (pageIndex >= 0) removeInputComponent (pageIndex);
                                    }
                                }, KeyStroke.getKeyStroke (java.awt.event.KeyEvent.VK_ESCAPE, 0),
                                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        PropertySheetSettings.getDefault ().addPropertyChangeListener (settingsListener =
                    new PropertyChangeListener () {
                        public void propertyChange (PropertyChangeEvent e) {
                            String name = e.getPropertyName ();

                            if (name == null) return;

                            if (name.equals (PROPERTY_VALUE_COLOR)) {
                                setValueColor ((Color)e.getNewValue ());
                            } else
                                if (name.equals (PROPERTY_DISABLED_PROPERTY_COLOR)) {
                                    setDisabledPropertyColor ((Color)e.getNewValue ());
                                } else
                                    if (name.equals (PROPERTY_SORTING_MODE)) {
                                        try {
                                            setSortingMode (((Integer)e.getNewValue ()).intValue ());
                                        } catch (PropertyVetoException ee) {
                                        }
                                    } else
                                        if (name.equals (PROPERTY_PLASTIC)) {
                                            setPlastic (((Boolean)e.getNewValue ()).booleanValue ());
                                        } else
                                            if (name.equals (PROPERTY_PROPERTY_PAINTING_STYLE)) {
                                                setPropertyPaintingStyle (((Integer)e.getNewValue ()).intValue ());
                                            } else
                                                if (name.equals (PROPERTY_DISPLAY_WRITABLE_ONLY)) {
                                                    setDisplayWritableOnly (((Boolean)e.getNewValue ()).booleanValue ());
                                                }
                        }
                    }
                                                                      );

        setNodes (new Node [0]);
    }


    // public methods ........................................................................

    /**
    * Set the nodes explored by this property sheet.
    *
    * @param node nodes to be explored
    */
    public void setNodes (Node[] node) {
        postSetNodes (node);
    }

    /**
    * Set property paint mode.
    * @param style one of {@link #ALWAYS_AS_STRING}, {@link #STRING_PREFERRED}, or {@link #PAINTING_PREFERRED}
    */
    public void setPropertyPaintingStyle (int style) {
        propertyPaintingStyle = style;
        if (pageIndex != -1) resort (pageIndex);
    }

    /**
    * Get property paint mode.
    *
    * @return the mode
    * @see #setPropertyPaintingStyle
    */
    public int getPropertyPaintingStyle () {
        return propertyPaintingStyle;
    }

    /**
    * Set the sorting mode.
    *
    * @param sortingMode one of {@link #UNSORTED}, {@link #SORTED_BY_NAMES}, {@link #SORTED_BY_TYPES}
    */
    public void setSortingMode (int sortingMode) throws PropertyVetoException {

        switch (sortingMode) {
        case UNSORTED:
            sorter = null;
            break;
        case SORTED_BY_NAMES:
            sorter = SORTER_NAME;
            break;
        case SORTED_BY_TYPES:
            sorter = SORTER_TYPE;
            break;
        default:
            throw new PropertyVetoException (bundle.getString ("EXC_Unknown_sorting_mode"),
                                             new PropertyChangeEvent (this, PROPERTY_SORTING_MODE,
                                                                      new Integer (this.sortingMode), new Integer (sortingMode)));
        }

        this.sortingMode = sortingMode;
        bNoSort.setSelected (sortingMode == UNSORTED);
        bAlphaSort.setSelected (sortingMode == SORTED_BY_NAMES);
        bTypeSort.setSelected (sortingMode == SORTED_BY_TYPES);
        if (pageIndex != -1) resort (pageIndex);
    }

    /**
    * Get the sorting mode.
    *
    * @return the mode
    * @see #setSortingMode
    */
    public int getSortingMode () {
        return sortingMode;
    }

    /**
    * Set the currently selected page.
    *
    * @param index index of the page to select
    */
    public void setCurrentPage (int index) {
        if (pageIndex == index) return;
        if (pageIndex >= 0) removeInputComponent (pageIndex);
        pageIndex = index;
        if (index < 0) return;
        if (propertyDetails [index] == null) refreshTab (index);
        else updateTab (index);
        if (index != pages.getSelectedIndex ()) pages.setSelectedIndex (index);
        selectedTabName = pages.getTitleAt (index);
    }

    /**
    * Set the currently selected page.
    *
    * @param str name of the tab to select
    */
    public boolean setCurrentPage (String str) {
        int index = pages.indexOfTab (str);
        if (index < 0) return false;
        setCurrentPage (index);
        return true;
    }

    /**
    * Get the currently selected page.
    * @return index of currently selected page
    */
    public int getCurrentPage () {
        return pages.getSelectedIndex ();
    }

    /**
    * Set whether buttons in sheet should be plastic.
    * @param plastic true if so
    * @see SheetButton#setPlastic
    */
    public void setPlastic (boolean plastic) {
        this.plastic = plastic;
        if (pageIndex != -1) resort (pageIndex);
    }

    /**
    * Test whether buttons in sheet are plastic.
    * @return <code>true</code> if so
    */
    public boolean getPlastic () {
        return plastic;
    }

    /**
    * Set the foreground color of values.
    * @param color the new color
    */
    public void setValueColor (Color color) {
        this.valueColor = color;
        if (pageIndex != -1) resort (pageIndex);
    }

    /**
    * Get the foreground color of values.
    * @return the color
    */
    public Color getValueColor () {
        return valueColor;
    }

    /**
    * Set the foreground color of disabled properties.
    * @param color the new color
    */
    public void setDisabledPropertyColor (Color color) {
        disabledPropertyColor = color;
        if (pageIndex != -1) resort (pageIndex);
    }

    /**
    * Get the foreground color of disabled properties.
    * @return the color
    */
    public Color getDisabledPropertyColor () {
        return disabledPropertyColor;
    }

    /**
    * Set whether only writable properties are displayed.
    * @param b <code>true</code> if this is desired
    */
    public void setDisplayWritableOnly (boolean b) {
        if (displayWritableOnly == b) return;
        displayWritableOnly = b;
        if (pageIndex != -1) resort (pageIndex);
        bDisplayWritableOnly.setSelected (displayWritableOnly);
    }

    /**
    * Test whether only writable properties are currently displayed.
    * @return <code>true</code> if so
    */
    public boolean getDisplayWritableOnly () {
        return displayWritableOnly;
    }


    // private helper methods ....................................................................

    /**
    * Refreshs propertysheet.
    */
    private synchronized void refreshPropertySheet() {
        pages.removeChangeListener (tabListener);

        int i, k = pages.getTabCount ();

        ignorePropertyChanges = true;
        tab = beansDetails.getPropertySetDisplayNames ();
        hints = beansDetails.getPropertySetHints ();
        ignorePropertyChanges = false;

        int l = tab.length;
        int t = Math.min (k, l);
        int tt = Math.max (k, l);

        if (l != k) {
            NamesPanel[] oldNames = namesPanel;
            NamesPanel[] oldValues = valuesPanel;
            namesPanel = new NamesPanel [l];
            valuesPanel = new NamesPanel [l];
            System.arraycopy (oldNames, 0, namesPanel, 0, t);
            System.arraycopy (oldValues, 0, valuesPanel, 0, t);
        }

        propertyValue = new PropertyValue [l][];
        propertyDisplayer = new PropertyDisplayer [l][];
        propertyDetails = new PropertyDetails [l][];

        for (i = 0; i < t; i++) {// old pages => rename
            propertyValue [i] = null;
            propertyDisplayer [i] = null;
            propertyDetails [i] = null;
            pages.setTitleAt (i, tab [i]);
        }
        for (i = k; i < l; i++) {// newly added pages
            propertyValue [i] = null;
            propertyDisplayer [i] = null;
            propertyDetails [i] = null;
            createTab (i);
        }
        for (i = k - 1; i >= l; i--) {// removed pages
            pages.removeTabAt (i);
        }

        // empty? => add emptyPanel!
        if (k != l)
            if (l == 0) {
                remove (pages);
                add (emptyPanel, "Center"); // NOI18N
            } else {
                remove (emptyPanel);
                add (pages, "Center"); // NOI18N
            }

        pageIndex = -1;
        if (l > 0) {
            if (!setCurrentPage (selectedTabName))
                setCurrentPage (0);
        }

        pages.invalidate ();
        invalidate ();
        Component f = getParent ();
        if (f != null) f.validate ();
        else validate ();
        pages.repaint ();
        repaint ();

        pages.addChangeListener (tabListener);
    }

    void resort (int index) {
        int i, k = propertyDetails.length;
        refreshTab (index);
        pages.invalidate ();
        Component f = getParent ();
        if (f != null) f.validate ();
        else validate ();
        pages.repaint ();
    }

    /** Creates one tab in tabbed pane.
    * Must be init.: tab[] (with values), hints [] (with values).
    */
    private void createTab (int index) {
        JPanel p = new JPanel ();
        p.setLayout (new BorderLayout ());
        p.add (new EmptyPanel (text), "Center"); // NOI18N
        pages.addTab (tab [index], null, p, hints [index]);
    }

    /** Creates components in one tab.
    * Must be init.: tab[] (with values), hints [] (with values),
    *               namesPanel[] & valuesPanel[] (only declared).
    * Initializes: namesPanel [index] & valuesPanel [index] & creates visual rep. of one tab.
    */
    private void createPane (int index, boolean empty) {
        JPanel p = (JPanel) pages.getComponentAt (index);
        Component c = p.getComponent (0);
        if (empty) {
            if ((c != null) && (c instanceof EmptyPanel))
                return;
            p.removeAll ();
            p.add (new EmptyPanel (text), "Center"); // NOI18N
            return;
        }

        if (namesPanel [index] == null) {
            namesPanel [index] = new NamesPanel ();
            valuesPanel [index] = new NamesPanel (namesPanel [index]);
        } else {
            namesPanel [index].removeAll ();
            valuesPanel [index].removeAll ();
        }
        if ((c == null) || !(c instanceof JScrollPane)) {
            p.removeAll ();
            JScrollPane scrollPane = new JScrollPane ();
            SplittedPanel splittedPanel = new ScrollableSplittedPanel (scrollPane, namesPanel [index]);
            splittedPanel.add (namesPanel [index], SplittedPanel.ADD_LEFT);
            splittedPanel.add (valuesPanel [index], SplittedPanel.ADD_RIGHT);

            scrollPane.setViewportView (splittedPanel);
            scrollPane.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getVerticalScrollBar ().setUnitIncrement (25);
            p.add (scrollPane, "Center"); // NOI18N
        }
    }


    /**
    * Refreshs properties in one tab.
    * Out: propertyValue [index][],
    *      propertyDisplayer [index][],
    *      propertyDetails [index][]
    *
    * @param index Index of tab to refresh.
    */
    private void refreshTab (int index) {
        SheetButton sheetButton;
        ignorePropertyChanges = true;

        propertyDetails [index]   = beansDetails.getPropertyDetails (index);
        int i, k = propertyDetails [index].length;
        if (displayWritableOnly) {
            Vector v =  new Vector (k);
            for (i = 0; i < k; i++)
                if (propertyDetails [index][i].canWrite ()) v.addElement (propertyDetails [index][i]);
            propertyDetails [index] = new PropertyDetails [k = v.size ()];
            v.copyInto (propertyDetails [index]);
        }
        if (sorter != null) java.util.Arrays.sort (propertyDetails [index], sorter);

        createPane (index, k == 0);

        propertyValue [index]     = new PropertyValue [k];
        propertyDisplayer [index] = new PropertyDisplayer [k];

        for (i = 0; i < k; i++) {
            sheetButton = new SheetButton (
                              propertyDetails [index][i].getName (),
                              plastic,
                              plastic
                          );

            final PropertyDetails pd = propertyDetails [index][i];
            sheetButton.setFocusTransferable (true);
            sheetButton.addMouseListener (
                new MouseUtils.PopupMouseAdapter () {
                    public void showPopup (MouseEvent ev) {
                        if (MouseUtils.isRightMouseButton (ev)) {
                            keepFocus = true;
                            setActions (pd);
                            popupMenu.show ((java.awt.Component) ev.getSource (), ev.getX(), ev.getY ());
                        }
                    }
                }
            );
            sheetButton.addFocusListener (new FocusListener () {
                                              public void focusGained (FocusEvent fe) {
                                                  JComponent jc = (JComponent)fe.getComponent ();
                                                  jc.scrollRectToVisible (new Rectangle (jc.getSize ()));
                                              }
                                              public void focusLost (FocusEvent fe) {
                                                  removeActions (pd);
                                              }
                                          });

            propertyValue [index][i] = new PropertyValue (propertyDetails [index][i]);

            sheetButton.setToolTipText (getDescription (propertyDetails [index][i]));

            propertyDisplayer [index][i] = new PropertyDisplayer (
                                               propertyDetails [index][i],
                                               propertyValue [index][i],
                                               propertyPaintingStyle,
                                               lock,
                                               valueColor,
                                               disabledPropertyColor,
                                               plastic
                                           );
            propertyDisplayer [index][i].setToolTipText (
                getValueDescription (propertyDetails [index][i])
            );
            propertyDisplayer [index][i].addPropertyChangeListener (beansListener);

            SheetListener sheetListener = new SheetListener (
                                              sheetButton, propertyDisplayer [index][i]);
            sheetButton.addSheetButtonListener (sheetListener);
            propertyDisplayer [index][i].addSheetButtonListener (sheetListener);

            if (!displayWritableOnly) {
                sheetButton.setEnabled (propertyDetails [index][i].canWrite ());
                propertyDisplayer [index][i].setEnabled (propertyDetails [index][i].canEdit ());
            }
            sheetButton.setInactiveForeground (disabledPropertyColor);

            namesPanel [index].add (sheetButton);
            valuesPanel [index].add (propertyDisplayer [index][i]);
        }
        if (k > 0) {
            namesPanel [index].repaint ();
            valuesPanel [index].repaint ();
        }
        repaint ();
        ignorePropertyChanges = false;
    }

    /**
    * Updates values of the properties in one tab.
    *
    * @param index Index of tab to refresh.
    */
    void updateTab (int index) {
        ignorePropertyChanges = true;
        int i, k = propertyDetails [index].length;
        PropertyValue newValue;

        for (i = 0; i < k; i++) {
            if ((newValue = new PropertyValue (propertyDetails [index][i])).
                    equals (propertyValue [index][i]))
                continue;

            /*S ystem.out.println ("Property " +
            propertyDetails [index][i].getName () +
            " changes to: " + newValue);*/ // NOI18N

            propertyValue [index][i] = newValue;
            boolean editable = propertyDetails [index][i].canEdit ();

            propertyDisplayer [index][i].setValue (newValue);
            propertyDisplayer [index][i].setEnabled (editable);
            propertyDisplayer [index][i].validate ();
        }// for
        ignorePropertyChanges = false;
    }

    /**
    * Invokes the customization on the currently selected Node (JavaBean).
    */
    void invokeCustomization () {
        beansDetails.customize ();
    }

    /**
    * Sets or removes input component for one property displayer.
    *
    * @param displayer Displayer for whis to set or remove the input component.
    */
    void setInputComponent (PropertyDisplayer dis) {
        if (lastSelectedLine == dis) {
            long t = System.currentTimeMillis () - dis.getLastDeselectTime ();
            if (t < 400) return;
        }
        boolean open = dis.getInputState (); // needed if focus losd dont work
        removeInputComponent (pageIndex);
        if (!open) {
            dis.setInputState (true);
            lastSelectedLine = dis;
        }
    }

    /**
    * Removes input component.
    */
    void removeInputComponent (int index) {
        if (lastSelectedLine != null) {
            lastSelectedLine.setReadState ();
            lastSelectedLine = null;
        }
    }


    PasteType[]           oldPaste;
    CopyAction            copy = new CopyAction ();
    PasteAction           paste = new PasteAction ();
    ActionPerformer       oldCopy;
    boolean               keepFocus;
    SetDefaultValueAction setDefault = new SetDefaultValueAction ();
    {
        setDefault.setSurviveFocusChange (false);
    }

    void setActions (final PropertyDetails pd) {

        // Enable / Disable PasteAction
        /*    PasteType[] pt = paste.getPasteTypes ();
            if ( (pt != null) &&
                 ((pt.length != 1) || !(pt [0].getClass ().equals (BeanPasteType.class)))
            ) {
              oldPaste = pt;
            }
            Clipboard clipboard = TopManager.getDefault ().getClipboard ();
            Transferable trans = clipboard.getContents (TopManager.getDefault ());
            if (trans != null) {
              DataFlavor[] flavor = trans.getTransferDataFlavors ();
              int ii, kk = (flavor == null) ? 0 : flavor.length;
              for (ii = 0; ii < kk; ii++)
                if ((flavor [ii] instanceof TransferFlavors.BeanFlavor) &&
                    (Utilities.getObjectType (pd.getValueType ()).isAssignableFrom (
                    ((TransferFlavors.BeanFlavor)flavor [ii]).getRepresentationClass ()))
                ) break;
              if (ii != kk)
                paste.setPasteTypes (new PasteType [] {
                  new BeanPasteType (pd, trans, flavor [ii])
                });
              else
                paste.setPasteTypes (null);
            } else
              paste.setPasteTypes (null);
        */
        // Enable / Disable DefalutValueAction
        try {
            if (pd.supportsDefaultValue () && pd.canWrite ())
                setDefault.setActionPerformer (
                    new ActionPerformer () {
                    public void performAction (SystemAction a) {
                        pd.restoreDefaultValue ();
                    }
                });
            else
                setDefault.setActionPerformer (null);
        } catch (Exception e) {
            setDefault.setActionPerformer (null);
        }

        // Enable / Disable CopyAction
        /*    ActionPerformer copyAp = new ActionPerformer () {
              public void performAction (SystemAction a) {
                Clipboard clip = TopManager.getDefault().getClipboard();
                try {
                  TransferableOwner to = new BeanTransferableOwner (pd.getPropertyValue ());
                  clip.setContents (to, to);
                } catch (Exception e) {
                }
              }
            };
            ActionPerformer ap = copy.getActionPerformer ();
            if ((ap != null) && !(ap.getClass ().equals (copyAp.getClass ()))) {
              oldCopy = ap;
            }
            copy.setActionPerformer (copyAp);
        */
    }

    void removeActions (PropertyDetails pd) {

        if (keepFocus) {
            keepFocus = false;
            return;
        }
        paste.setPasteTypes (oldPaste);
        oldPaste = null;
        setDefault.setActionPerformer (null);
        copy.setActionPerformer (oldCopy);
        oldCopy = null;
    }

    public void addNotify () {
        PropertySheetSettings.getDefault ().addPropertyChangeListener (
            settingsListener
        );
        super.addNotify ();
    }

    public void removeNotify () {
        if (beansDetails != null) {
            beansDetails.removePropertyChangeListener (beansListener);
            beansDetails.removeNodeListener (beansListener);
        }
        PropertySheetSettings.getDefault ().removePropertyChangeListener (
            settingsListener
        );
        super.removeNotify ();
    }

    public Dimension getPreferredSize () {
        return new Dimension (200, 300);
    }


    // innerclasses ..........................................................................

    /**
    * Listens on BeansDetails on property change & property set changes.
    */
    private class BeansListener extends NodeAdapter {
        /*
        * This method is called when some property are changed. Then all properties are tested
        * for changing of its value.
        */
        public void propertyChange (PropertyChangeEvent evt) {
            if (ignorePropertyChanges) return;
            if (pageIndex != -1) {
                final String n = evt.getPropertyName ();
                final int thisPageIndex = pageIndex;
                javax.swing.SwingUtilities.invokeLater ( new Runnable() {
                    public void run() {
                        if ((n != null) && n.equals (Node.PROP_PROPERTY_SETS)) {
                            beansDetails.refresh ();
                            refreshPropertySheet ();
                        } else {
                            updateTab (thisPageIndex);
                        }
                    }
                } );
            }
        }
    }

    /**
    * Scrollable enhancement of SplittedPanel.
    */
    class ScrollableSplittedPanel extends SplittedPanel implements Scrollable {

        static final long serialVersionUID = -623600999659692948L;

        Component scroll;
        Container element;

        ScrollableSplittedPanel (Component scroll, Container element) {
            this.scroll = scroll;
            this.element = element;
        }

        /**
        * Returns the preferred size of the viewport for a view component.
        *
        * @return The preferredSize of a JViewport whose view is this Scrollable.
        */
        public Dimension getPreferredScrollableViewportSize () {
            return super.getPreferredSize ();
        }

        /**
        * @param visibleRect The view area visible within the viewport
        * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
        * @param direction Less than zero to scroll up/left, greater than zero for down/right.
        * @return The "unit" increment for scrolling in the specified direction
        */
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            Component[] c = element.getComponents ();
            if (c.length < 1) return 1;
            Dimension d = c [0].getSize ();
            if (orientation == SwingConstants.VERTICAL) return d.height;
            else return d.width;
        }

        /**
        * @param visibleRect The view area visible within the viewport
        * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
        * @param direction Less than zero to scroll up/left, greater than zero for down/right.
        * @return The "block" increment for scrolling in the specified direction.
        */
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            if (orientation == SwingConstants.VERTICAL) return scroll.getSize ().height;
            else return scroll.getSize ().width;
        }


        /**
        * Return true if a viewport should always force the width of this
        * Scrollable to match the width of the viewport.
        *
        * @return True if a viewport should force the Scrollables width to match its own.
        */
        public boolean getScrollableTracksViewportWidth () {
            return true;
        }

        /**
        * Return true if a viewport should always force the height of this
        * Scrollable to match the height of the viewport.
        *
        * @return True if a viewport should force the Scrollables height to match its own.
        */
        public boolean getScrollableTracksViewportHeight () {
            return false;
        }
    }

    /**
    * Creates connection between two butons in the same line in property sheet. It propagates
    * buttonEntered and buttonExited events between them, and action clicked too.
    */
    class SheetListener implements SheetButtonListener {

        private SheetButton            nameComponent;
        private PropertyDisplayer      valueComponent;

        SheetListener (
            SheetButton          nameComponent,
            PropertyDisplayer    valueComponent
        ) {
            this.nameComponent =     nameComponent;
            this.valueComponent =    valueComponent;
        }

        public void sheetButtonEntered (ActionEvent e) {
            if (e.getSource () == nameComponent) valueComponent.setPressed (true);
            else nameComponent.setPressed (true);
        }

        public void sheetButtonExited (ActionEvent e) {
            if (e.getSource () == nameComponent) valueComponent.setPressed (false);
            else nameComponent.setPressed (false);
        }

        public void sheetButtonClicked (ActionEvent e) {
            if ((e.getID () == ActionEvent.ACTION_FIRST) && valueComponent.rolling (true)) return;
            if (e.getSource () == nameComponent) {
                setInputComponent (valueComponent);
            } else {
                removeInputComponent (0);
                lastSelectedLine = valueComponent;
                if (!lastSelectedLine.getInputState ()) {
                    lastSelectedLine.setInputState (true);
                }
            }
        }
    }

    /** Nodes instance of nodes */
    private transient RunNodes runNodes;

    /** Post the request for redraw of new nodes.
    * @param nodes nodes to display in the sheet
    */
    void postSetNodes (Node[] nodes) {
        if (runNodes == null) {
            runNodes = new RunNodes ();
        }
        runNodes.postNodes (nodes);
    }

    /** Request for introspection of nodes.
    */
    class RunNodes implements Runnable {
        /** Previous request with change of nodes.
        */
        private RequestProcessor.Task runTask;

        /** nodes to display */
        private Node[] nodes;

        public void postNodes (Node[] nodes) {
            this.nodes = nodes;

            if (runTask == null) {
                runTask = RequestProcessor.createRequest (this);
            }

            runTask.schedule (250);
        }

        /** cycle for work with beans */
        public void run () {
            Node[] n = nodes;
            if (n != null) {
                synchronized (lock) {
                    try {
                        if (beansDetails != null) {
                            beansDetails.removePropertyChangeListener (beansListener);
                            beansDetails.removeNodeListener (beansListener);
                        }
                        try {
                            customizer.setEnabled (false);
                            beansDetails = new BeansDetails (n);
                        } catch (IntrospectionException e) {
                            // empty beans details
                            beansDetails = new BeansDetails ();
                        }
                        beansDetails.addPropertyChangeListener (beansListener);
                        beansDetails.addNodeListener (beansListener);
                        customizer.setEnabled (beansDetails.hasCustomizer ());
                        SwingUtilities.invokeLater (new Runnable () {
                                                        public void run () {
                                                            refreshPropertySheet ();
                                                        }
                                                    });
                    } catch (Throwable e) {
                        if (e instanceof ThreadDeath)
                            throw (ThreadDeath)e;
                        TopManager.getDefault().notifyException(e);
                    }
                }
            }
        }
    }

    /**
    * Supports adding of serialized JavaBeans.
    */
    private class BeanPasteType extends PasteType {
        PropertyDetails details;
        Transferable transferable;
        DataFlavor flavor;

        /** Constructs new BeanPasteType for the specific type of operation paste.
        */
        public BeanPasteType (PropertyDetails details, Transferable transferable, DataFlavor flavor) {
            this.details = details;
            this.transferable = transferable;
            this.flavor = flavor;
        }

        /* @return Human presentable name of this paste type. */
        public String getName() {
            return PropertySheet.this.bundle.getString ("CTL_Paste");
        }

        /* @return help */
        public org.openide.util.HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx (BeanPasteType.class);
        }

        /** Performs the paste action.
        * @return Transferable which should be inserted into the clipboard after
        *         paste action. It can be null, which means that clipboard content
        *         should stay the same.
        */
        public Transferable paste () throws IOException {

            String data;
            try {
                Object o = transferable.getTransferData (flavor);
                if (o instanceof String)
                    o = java.beans.Beans.instantiate (
                            TopManager.getDefault ().currentClassLoader (),
                            (String) o
                        );
                details.setPropertyValue (o);
                beansListener.propertyChange (
                    new PropertyChangeEvent (this, null, null, null)
                );
            }
            catch (java.awt.datatransfer.UnsupportedFlavorException e) {
                throw new InternalError();
            }
            catch (ClassCastException e) {
                throw new IOException (e.getMessage ());
            }
            catch (ClassNotFoundException e) {
                throw new IOException (e.getMessage ());
            }
            return null;
        }
    }

    class BeanTransferableOwner implements Transferable {

        /** our data */
        private Object object;

        private DataFlavor flavor;

        /**
        * @param action is a being processed action
        * @param copy is a flag indicating what kind of action we are doing
        * true if copy, false if cut.
        */
        public BeanTransferableOwner (Object object) {
            this.object = object;
            //      flavor = new TransferFlavors.BeanFlavor (object.getClass ());
        }

        /** Returns an array of DataFlavor objects indicating the flavors the data can be provided in. The array should be
        * ordered according to preference for providing the data (from most richly descriptive to least descriptive).
        * @return  an array of data flavors in which this data can be transferred
        */
        public DataFlavor[] getTransferDataFlavors () {
            return new DataFlavor[] {flavor};
        }

        /** Returns whether or not the specified data flavor is supported for this object.
        * @param flavor - the requested flavor for the data
        * @return boolean indicating wjether or not the data flavor is supported
        */
        public boolean isDataFlavorSupported (DataFlavor flavor) {
            return false; // PENDING flavor instanceof TransferFlavors.BeanFlavor;
        }

        /**Returns an object which represents the data to be transferred. The class of the object returned is defined by the
        * representation class of the flavor.
        * @param flavor - the requested flavor for the data
        * @exception IOException if the data is no longer available in the requested flavor.
        * @exception UnsupportedFlavorException if the requested data flavor is not supported.
        */
        public Object getTransferData (DataFlavor flavor) throws
            UnsupportedFlavorException, IOException {
            if (isDataFlavorSupported (flavor)) return object;
            throw new UnsupportedFlavorException (flavor);
        }
    }
}

/*
 * Log
 *  33   Gandalf   1.32        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  32   Gandalf   1.31        1/12/00  Ian Formanek    NOI18N
 *  31   Gandalf   1.30        1/10/00  Jan Jancura     Problem with 
 *       initializing properties.
 *  30   Gandalf   1.29        1/4/00   Jan Jancura     Refresh PS when Property
 *       set is changed.
 *  29   Gandalf   1.28        12/10/99 Jan Jancura     Null pointerexception
 *  28   Gandalf   1.27        12/9/99  Jan Jancura     PropertyPanel 
 *       implementation + Bug 3961
 *  27   Gandalf   1.26        11/23/99 Jaroslav Tulach Clears last selected 
 *       line when deselected not to prevent objects from GarbageCollection
 *  26   Gandalf   1.25        11/8/99  Jesse Glick     Apparently during IDE 
 *       shutdown it is possible for a shortDescription to be null (??).
 *  25   Gandalf   1.24        11/8/99  Jesse Glick     Swing 1.1.1 HTML 
 *       tooltips are handled for property short descriptions. BUT the display 
 *       is currently too buggy to be of much use.
 *  24   Gandalf   1.23        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  23   Gandalf   1.22        9/29/99  Jan Jancura     Bug 1659 - all non 
 *       editable properties are now grayed in the propertysheet
 *  22   Gandalf   1.21        9/23/99  Jaroslav Tulach Uses schedule on 
 *       delaying of node change firing.
 *  21   Gandalf   1.20        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  20   Gandalf   1.19        8/1/99   Jaroslav Tulach MainExplorer now listens
 *       to changes in root elements.
 *  19   Gandalf   1.18        7/25/99  Ian Formanek    cleaned icons creation
 *  18   Gandalf   1.17        7/3/99   Ian Formanek    Survives when 
 *       Transferable.getTransferDataFlavors returns null
 *  17   Gandalf   1.16        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  16   Gandalf   1.15        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  15   Gandalf   1.14        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        4/28/99  Jan Jancura     Bug in sorting
 *  13   Gandalf   1.12        4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  12   Gandalf   1.11        4/8/99   Ian Formanek    Changed Object.class -> 
 *       getClass ()
 *  11   Gandalf   1.10        4/2/99   Jaroslav Tulach Preferred size
 *  10   Gandalf   1.9         3/20/99  Jesse Glick     [JavaDoc]
 *  9    Gandalf   1.8         3/20/99  Jesse Glick     [JavaDoc]
 *  8    Gandalf   1.7         3/6/99   David Simonek   
 *  7    Gandalf   1.6         3/4/99   Jaroslav Tulach QuickSorter removed
 *  6    Gandalf   1.5         3/4/99   Jan Jancura     Localization moved
 *  5    Gandalf   1.4         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  4    Gandalf   1.3         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  3    Gandalf   1.2         1/6/99   David Simonek   
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting changes in 
 *       location of package "awt"
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.20        --/--/98 Jaroslav Tulach Added multithreaded support
 *  0    Tuborg    0.23        --/--/98 Ales Novak      Serializable
 *  0    Tuborg    0.24        --/--/98 Jan Formanek    serialization improved, now extends Object and delegates the component
 *  0    Tuborg    0.24        --/--/98 Jan Formanek    via getComponent
 *  0    Tuborg    0.25        --/--/98 Jan Formanek    extends ExplorerViewSupport
 *  0    Tuborg    0.53        --/--/98 Jan Formanek    some fix (hopefully okay) - in method propertyChange
 */
