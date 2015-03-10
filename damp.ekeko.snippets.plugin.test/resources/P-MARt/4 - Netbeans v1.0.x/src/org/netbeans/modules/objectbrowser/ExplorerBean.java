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

package org.netbeans.modules.objectbrowser;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.infobus.*;

import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarToggleButton;
import org.openide.awt.ToolbarButton;
import org.openide.explorer.ExplorerActions;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.*;
import org.openide.windows.TopComponent;


/**
* Encapsulation of Explorer views. This bean has property explorerViewClassName which
* define the type of explorer view. ExplorerBean adds to Explorer views special functionality
* used in Object Browser like communication with other ExplorerBeans and setting filter.
*
* @author Jan Jancura
*/
public class ExplorerBean extends JPanel implements InfoBusMember,
    InfoBusDataConsumer, InfoBusDataProducer, Provider {


    // static ............................................................................

    public static final String        PROPERTY_EXPLORER_VIEW_CLASS_NAME = "explorerViewClassName"; // NOI18N

    /** Icons for buttons. */
    static protected Icon             iFirst;
    static protected Icon             iSecond;
    static protected Icon             iThird;
    static protected Icon             iFourth;

    static ExplorerActions            explorerActions = new ExplorerActions ();

    static {
        try {
            iFirst = new ImageIcon (ExplorerBean.class.getResource ("/org/netbeans/modules/objectbrowser/resources/package.gif")); // NOI18N
            iSecond = new ImageIcon (ExplorerBean.class.getResource ("/org/netbeans/modules/objectbrowser/resources/class.gif")); // NOI18N
            iThird = new ImageIcon (ExplorerBean.class.getResource ("/org/netbeans/modules/objectbrowser/resources/interface.gif")); // NOI18N
            iFourth = new ImageIcon (ExplorerBean.class.getResource ("/org/netbeans/modules/objectbrowser/resources/variable.gif")); // NOI18N
        } catch (Throwable w) {
            w.printStackTrace ();
        }
    }

    static Component findFocusable (Container c) {
        int i, k = c.getComponentCount ();
        Component cc;
        for (i = 0; i < k; i++)
            if ((cc = c.getComponent (i)).isFocusTraversable ())
                return cc;
            else
                if (cc instanceof Container) return findFocusable ((Container) cc);
        return null;
    }


    // variables ............................................................................

    // property variables
    private String                           infoBusName = HierarchyTranslator.PROPERTY_DEFAULT_INFO_BUS_NAME;
    private String                           inputName;
    private String                           outputName;
    private String                           title;
    private boolean                          toolbarVisible;
    private transient PropertyChangeSupport  pcs;

    // InfoBus support
    private transient InfoBusMemberSupport   ibms;
    private boolean                          connected = false;
    private boolean                          designTime = false;
    private transient HierarchyTranslator    translator;

    // Explorer support
    private transient ExplorerManager        explorerManager;
    private Component                        explorerView;
    private PropertyChangeListener           explorerListener;
    private TopComponent                     topComponent;

    // current state encaps.
    private boolean[]                        filter = new boolean [0];
    private HierarchyTranslator.Filter       subFilter;
    private FilterListener                   filterListener = new FilterListener ();
    private Node[]                           rootNodes;
    private boolean                          isFacused = false;


    // init .................................................................................

    static final long serialVersionUID =-5840264857341760916L;
    public ExplorerBean() {
        setLayout (new BorderLayout ());
        setBorder (new CompoundBorder (
                       new LineBorder (getBackground ()),
                       new org.netbeans.modules.objectbrowser.BevelBorder (EtchedBorder.LOWERED)
                   ));
        rootNodes = null;
        init ();
        try {
            setExplorerViewClassName ("org.openide.explorer.view.BeanTreeView"); // NOI18N
        } catch (PropertyVetoException e) {
        }
    }

    protected void init () {
        pcs = new PropertyChangeSupport (this);
        ibms = new InfoBusMemberSupport (this);
        explorerManager = new ExplorerManager ();
        explorerManager.addPropertyChangeListener (explorerListener =
                    new PropertyChangeListener () {
                        public void propertyChange (PropertyChangeEvent e) {
                            // fire selection changes
                            if ( (outputName != null) && (e.getPropertyName () != null) && connected &&
                                    e.getPropertyName ().equals (ExplorerManager.PROP_SELECTED_NODES)
                               ) {
                                ibms.getInfoBus ().fireItemAvailable (outputName, null, ExplorerBean.this);
                            }
                            if ((topComponent != null) && isFacused) {
                                topComponent.setActivatedNodes (explorerManager.getSelectedNodes ());
                            }
                        }
                    }
                                                  );
        if (!designTime)
            connect ();
    }


    // properties ...........................................................................

    public String getExplorerViewClassName () {
        if (explorerView == null) return "none"; // NOI18N
        return explorerView.getClass ().getName ();
    }

    public void setExplorerViewClassName (String name) throws PropertyVetoException {
        // create new view
        Class c;
        try {
            c = Class.forName (name);
        } catch (ClassNotFoundException e) {
            throw new PropertyVetoException (
                name + " ClassNotFoundException occurred while creating ExplorerView.", // NOI18N
                new PropertyChangeEvent (
                    this,
                    PROPERTY_EXPLORER_VIEW_CLASS_NAME,
                    getExplorerViewClassName (),
                    name
                )
            );
        }
        Constructor cc;
        try {
            cc = c.getConstructor (new Class [] {}); //1 ExplorerManager.class});
        } catch (Exception e) {
            throw new InternalError ();
        }
        Object o;
        try {
            o = cc.newInstance (new Object [] {});
        } catch (InstantiationException e) {
            throw new PropertyVetoException (
                "InstantiationException occurred while creating ExplorerView.", // NOI18N
                new PropertyChangeEvent (
                    this,
                    PROPERTY_EXPLORER_VIEW_CLASS_NAME,
                    getExplorerViewClassName (),
                    name
                )
            );
        } catch (java.lang.reflect.InvocationTargetException e) {
            e.getTargetException ().printStackTrace ();
            throw new PropertyVetoException (
                "java.lang.reflect.InvocationTargetException occurred while creating ExplorerView.", // NOI18N
                new PropertyChangeEvent (
                    this,
                    PROPERTY_EXPLORER_VIEW_CLASS_NAME,
                    getExplorerViewClassName (),
                    name
                )
            );
        } catch (IllegalAccessException e) {
            throw new PropertyVetoException (
                "IllegalAccessException occurred while creating ExplorerView.", // NOI18N
                new PropertyChangeEvent (
                    this,
                    PROPERTY_EXPLORER_VIEW_CLASS_NAME,
                    getExplorerViewClassName (),
                    name
                )
            );
        }
        // PENDING explorerView.deinitialize ();
        explorerView = (Component)o;

        // customize view
        if (explorerView instanceof JComponent)
            ((JComponent) explorerView).setBorder (new EtchedBorder (EtchedBorder.LOWERED));
        if (explorerView instanceof org.openide.explorer.view.ListView) {
            ((org.openide.explorer.view.ListView) explorerView).setTraversalAllowed (false);
        }
        if (explorerView instanceof org.openide.explorer.view.TreeView) {
            ((org.openide.explorer.view.TreeView) explorerView).setRootVisible (false);
        }
        Component f = findFocusable ((Container) explorerView);
        f.addFocusListener (new FocusAdapter () {
                                public void focusGained (FocusEvent e) {
                                    if (topComponent != null) {
                                        topComponent.setActivatedNodes (explorerManager.getSelectedNodes ());
                                    }
                                    explorerActions.attach (explorerManager);
                                    setBorder (new CompoundBorder (
                                                   new LineBorder (Color.black),
                                                   new BevelBorder (EtchedBorder.LOWERED)
                                               ));
                                    isFacused = true;
                                }
                                public void focusLost (FocusEvent e) {
                                    setBorder (new CompoundBorder (
                                                   new LineBorder (getBackground ()),
                                                   new BevelBorder (EtchedBorder.LOWERED)
                                               ));
                                    isFacused = false;
                                    //        explorerActions.detach ();
                                }
                            });

        // empty nodes
        explorerManager.removePropertyChangeListener (explorerListener);
        explorerManager = new ExplorerManager ();
        explorerManager.addPropertyChangeListener (explorerListener);

        // change view
        repaintView ();

        // empty nodes
        refreshNodes ();
    }

    public String getInputName () {
        return inputName;
    }

    public void setInputName (String name) {
        String old = inputName;
        inputName = name;
        pcs.firePropertyChange ("inputName", old, inputName); // NOI18N
    }

    public String getOutputName () {
        return outputName;
    }

    public void setOutputName (String name) {
        String old = outputName;
        outputName = name;
        pcs.firePropertyChange ("outputName", old, outputName); // NOI18N
    }

    public String getInfoBusName () {
        return infoBusName;
    }

    public void setInfoBusName (String name) throws PropertyVetoException {
        String old = infoBusName;
        disconnect ();
        infoBusName = name;
        connect ();
        pcs.firePropertyChange ("infoBusName", old, infoBusName); // NOI18N
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        String old = title;
        this.title = title;
        pcs.firePropertyChange ("title", old, title); // NOI18N
        repaintView ();
    }

    public boolean getToolbarVisible () {
        return toolbarVisible;
    }

    public void setToolbarVisible (boolean b) {
        boolean old = b;
        toolbarVisible = b;
        pcs.firePropertyChange ("toolbarVisible", new Boolean (old), new Boolean (toolbarVisible)); // NOI18N
        repaintView ();
    }

    public FilterSettings getFilterSettings () {
        String[] n = null;
        String[] h = null;
        if (translator != null) {
            n = translator.getFilterNames ();
            h = translator.getFilterComments ();
        }
        return new FilterSettings (filter, n, h);
    }

    public void setFilterSettings (FilterSettings fs) {
        filter = fs.getFilter ();
        repaintView ();
        refreshNodes ();
    }


    // helper methods .................................................................

    public void addNotify () {
        if (designTime) connect ();
        topComponent = (TopComponent) SwingUtilities.getAncestorOfClass (TopComponent.class, this);
        super.addNotify ();
    }

    public void removeNotify () {
        if (designTime) disconnect ();
        topComponent = (TopComponent) SwingUtilities.getAncestorOfClass (TopComponent.class, this);
        super.removeNotify ();
    }

    void expandAll () {
        ((org.openide.explorer.view.TreeView) explorerView).expandAll ();
    }

    /**
    * Creates visual rep.
    * title + toolbarVisible + subFilter => repaint
    */
    private void repaintView () {
        if (explorerView == null) return;
        removeAll ();
        JComponent inn = this;
        if (title != null) {
            JLabel l = new JLabel (title, SwingConstants.CENTER);
            l.setBorder (new EtchedBorder (EtchedBorder.LOWERED));
            add (l, "North"); // NOI18N
            inn = new JPanel (new BorderLayout ());
            add (inn, "Center"); // NOI18N
        }
        if (toolbarVisible) {
            Component c = null;
            if (subFilter != null) c = subFilter.getComponent ();
            if (c == null) {
                JPanel j = new JPanel ();
                j.setBorder (new EtchedBorder (EtchedBorder.LOWERED));
                inn.add (j, "North"); // NOI18N
            } else
                inn.add (c, "North"); // NOI18N
        }
        inn.add (explorerView, "Center"); // NOI18N
        validate ();
    }

    private void setTranslator () {
        Object old = translator;
        translator = (HierarchyTranslator) ibms.getInfoBus ().findDataItem (
                         HierarchyTranslator.PROPERTY_DEFAULT_TRANSLATOR_ITEM_NAME,
                         null,
                         this
                     );
        if (old == translator) return;

        /*
        S ystem.out.println ("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        S ystem.out.println ("ExplorerBean.setNewTranslator: " + translator + " old: " + old);
        T hread.dumpStack ();
        S ystem.out.println ("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        */

        if (translator != null) {
            int i, k = translator.getFilterNames ().length;
            if (k != filter.length) {
                filter = new boolean [k];
                for (i = 0; i < k; i++)
                    filter [i] = true;
            }
        }
        refreshNodes ();
        repaintView ();
    }

    /**
    * Updates sub filter.
    * called from refreshNodes () only
    */
    private void setSubFilter (HierarchyTranslator.Filter f) {
        if ( (subFilter != null) &&
                subFilter.getClass ().equals (f.getClass ())
           ) return;
        if (subFilter != null) subFilter.removePropertyChangeListener (filterListener);
        f.addPropertyChangeListener (filterListener);
        subFilter = f;
    }

    /**
    * Updates sub filter & nodes.
    * rootNodes => explorerManager, setSubFilter ()
    */
    private void refreshNodes () {
        if (translator != null) {
            setSubFilter (translator.getFilter (rootNodes, filter, this));
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                Node nnn = translator.translate (rootNodes, filter, subFilter);   //S ystem.out.println ("Explorer bean - setRoot " + nnn); // NOI18N
                                                //Thread.dumpStack ();
                                                explorerManager.setRootContext (nnn);
                                                try {
                                                    explorerManager.setSelectedNodes (new Node[] {});
                                                } catch (PropertyVetoException e) {
                                                }
                                            }
                                        });
        } else
            if (rootNodes == null) {                               //S ystem.out.println ("Explorer bean - set empty root"); // NOI18N
                explorerManager.setRootContext (new AbstractNode (Children.LEAF));
                try {
                    explorerManager.setSelectedNodes (new Node[] {});
                } catch (PropertyVetoException e) {
                }
            } else {                                                //S ystem.out.println ("Explorer bean - set special root"); // NOI18N
                explorerManager.setRootContext (
                    new AbstractNode (new Children.Array () {
                                          protected Node[] createNodes () {
                                              int i, k = rootNodes.length;
                                              Node[] nn = new Node [k];
                                              for (i = 0; i < k; i++)
                                                  nn [i] = rootNodes [i].cloneNode ();
                                              return nn;
                                          }
                                          public String toString () {
                                              return "ExplorerBean.Full node " + title + "[" + super.toString () + "]"; // NOI18N
                                          }
                                      })
                );
                try {
                    explorerManager.setSelectedNodes (new Node[] {});
                } catch (PropertyVetoException e) {
                }
            }
    }

    /** Get the explorer manager.
    * @return the manager
    */
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }

    public String toString () {
        if (title != null) return "ExplorerBean " + title; // NOI18N
        return super.toString ();
    }


    // InfoBus support .................................................................

    private void connect () {
        if (connected) return;
        try {
            ibms.joinInfoBus (infoBusName);
        } catch (InfoBusMembershipException e) {
            throw new InternalError ();
        } catch (PropertyVetoException e) {
            throw new InternalError ();
        }
        ibms.getInfoBus ().addDataConsumer (this);
        setTranslator ();
        if (outputName != null) ibms.getInfoBus ().addDataProducer (this);
        connected = true;
    }

    private void disconnect () {
        if (!connected) return;
        if (ibms.getInfoBus () == null) return;
        ibms.getInfoBus ().removeDataConsumer (this);
        ibms.getInfoBus ().removeDataProducer (this);
        try {
            ibms.leaveInfoBus ();
        } catch (InfoBusMembershipException e) {
            throw new InternalError ();
        } catch (PropertyVetoException e) {
            throw new InternalError ();
        }
        setTranslator ();
        connected = false;
    }

    public void setInfoBus (InfoBus newInfoBus) throws PropertyVetoException {
        ibms.setInfoBus (newInfoBus);
    }

    public InfoBus getInfoBus () {
        return ibms.getInfoBus ();
    }

    public void addInfoBusVetoableListener (VetoableChangeListener vcl) {
        ibms.addInfoBusVetoableListener (vcl);
    }

    public void removeInfoBusVetoableListener (VetoableChangeListener vcl) {
        ibms.removeInfoBusVetoableListener (vcl);
    }

    public void addInfoBusPropertyListener (PropertyChangeListener pcl) {
        ibms.addInfoBusPropertyListener (pcl);
    }

    public void removeInfoBusPropertyListener (PropertyChangeListener pcl) {
        ibms.removeInfoBusPropertyListener (pcl);
    }

    public void dataItemAvailable (InfoBusItemAvailableEvent e) {
        if (e.getDataItemName ().equals (HierarchyTranslator.PROPERTY_DEFAULT_TRANSLATOR_ITEM_NAME)) {
            setTranslator ();
            return;
        }
        if (inputName == null) return;
        if (!e.getDataItemName ().equals (inputName)) return;
        rootNodes = (Node[]) e.requestDataItem (this, null);
        refreshNodes ();
    }

    public void dataItemRevoked (InfoBusItemRevokedEvent e) {
        if (e.getDataItemName ().equals (HierarchyTranslator.PROPERTY_DEFAULT_TRANSLATOR_ITEM_NAME))
            setTranslator ();
    }

    public void propertyChange (PropertyChangeEvent e) {
    }

    public void dataItemRequested (InfoBusItemRequestedEvent e) {
        if ((outputName != null) && e.getDataItemName ().equals (outputName)) {   //S ystem.out.println("ExplorerBean.ItemRequested " + title + " : " + explorerManager.getSelectedNodes ().length); // NOI18N
            e.setDataItem (explorerManager.getSelectedNodes ());
        }
    }


    // innerclasses .........................................................................

    public static class FilterSettings implements Serializable {
        static final long serialVersionUID = 8392364514965433706L;

        private boolean[] filter;
        private String[] names;
        private String[] hints;

        public FilterSettings (boolean[] fs) {
            filter = fs;
        }

        public FilterSettings (boolean[] fs, String[] n, String[] h) {
            filter = fs;
            names = n;
            hints = h;
        }

        String[] getFilterNames () {
            return names;
        }

        String[] getFilterComments () {
            return hints;
        }

        FilterSettings forFilter (boolean[] b) {
            FilterSettings fs = new FilterSettings (b);
            fs.names = names;
            fs.hints = hints;
            return fs;
        }

        boolean[] getFilter () {
            return filter;
        }
    }

    class FilterListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent e) {
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                Node n = translator.translate (rootNodes, filter, subFilter);  //S ystem.out.println ("Explorer bean - change filter " + n); // NOI18N
                                                explorerManager.setRootContext (new AbstractNode (Children.LEAF));
                                                explorerManager.setRootContext (n);
                                            }
                                        });
        }
    }
}

/*
 * Log
 *  21   src-jtulach1.20        1/13/00  Radko Najman    I18N
 *  20   src-jtulach1.19        12/15/99 Jan Jancura     
 *  19   src-jtulach1.18        12/15/99 Jan Jancura     Bug 4012
 *  18   src-jtulach1.17        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  17   src-jtulach1.16        10/4/99  Jan Jancura     Bug 2783
 *  16   src-jtulach1.15        8/19/99  Jan Jancura     Bug 3434
 *  15   src-jtulach1.14        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  14   src-jtulach1.13        7/27/99  Jan Jancura     
 *  13   src-jtulach1.12        7/2/99   Jan Jancura     
 *  12   src-jtulach1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   src-jtulach1.10        5/17/99  Jan Jancura     Bug 1535, 1765
 *  10   src-jtulach1.9         5/12/99  Jan Jancura     
 *  9    src-jtulach1.8         5/6/99   Jan Jancura     
 *  8    src-jtulach1.7         4/26/99  Jan Jancura     
 *  7    src-jtulach1.6         4/16/99  Jan Jancura     
 *  6    src-jtulach1.5         4/9/99   Jan Jancura     Bug 1508
 *  5    src-jtulach1.4         4/8/99   Jan Jancura     
 *  4    src-jtulach1.3         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  3    src-jtulach1.2         4/2/99   Jan Jancura     
 *  2    src-jtulach1.1         3/23/99  Jan Jancura     
 *  1    src-jtulach1.0         3/23/99  Jan Jancura     
 * $
 */
