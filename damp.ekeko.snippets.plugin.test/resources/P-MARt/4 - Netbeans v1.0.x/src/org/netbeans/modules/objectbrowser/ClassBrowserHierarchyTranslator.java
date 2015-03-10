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


import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarButton;
import org.openide.awt.ToolbarToggleButton;
import org.openide.cookies.FilterCookie;
import org.openide.cookies.ElementCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectFilter;
import org.openide.loaders.DataLoader;
import org.openide.src.nodes.SourceElementFilter;
import org.openide.src.nodes.ClassElementFilter;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.DataFlavor;
import java.lang.reflect.Modifier;
import java.beans.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.infobus.*;
import javax.swing.*;
import javax.swing.border.*;

/**
* Producer of class hierarchy.
*
* @author   Jan Jancura
*/
public class ClassBrowserHierarchyTranslator extends JPanel implements HierarchyTranslator, InfoBusMember, InfoBusDataProducer {

    /** Icons for buttons. */
    static protected Icon  iClass;
    static protected Icon  iInterface;
    static protected Icon  iOtherDOs;
    static protected Icon  iBean;
    static protected Icon  iMethod;
    static protected Icon  iVariable;
    static protected Icon  iConstructor;
    static protected Icon  iPublic;
    static protected Icon  iPackage;
    static protected Icon  iProtected;
    static protected Icon  iPrivate;
    static protected Icon  iSorted;
    static protected Icon  iExpand;
    static protected Icon  iDetails;
    static protected Icon  iTree;

    static {
        try {
            iClass = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/class.gif")); // NOI18N
            iInterface = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/interface.gif")); // NOI18N
            iOtherDOs = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/otherDOs.gif")); // NOI18N
            iBean = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/bean.gif")); // NOI18N
            iMethod = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/method.gif")); // NOI18N
            iVariable = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/variable.gif")); // NOI18N
            iConstructor = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/constructor.gif")); // NOI18N
            iPublic = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/public.gif")); // NOI18N
            iPackage = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/package.gif")); // NOI18N
            iProtected = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/protected.gif")); // NOI18N
            iPrivate = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/private.gif")); // NOI18N
            iSorted = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/sorted.gif")); // NOI18N

            iExpand = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/expand.gif")); // NOI18N
            iDetails = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/details.gif")); // NOI18N
            iTree = new ImageIcon (ClassBrowserHierarchyTranslator.class.getResource ("/org/netbeans/modules/objectbrowser/resources/tree.gif")); // NOI18N
        } catch (Throwable w) {
            w.printStackTrace ();
        }
    }


    // variables ............................................................................

    private String                           infoBusName = PROPERTY_DEFAULT_INFO_BUS_NAME;
    private transient InfoBusMemberSupport   ibms;
    private boolean                          connected = false;
    private boolean                          designTime = false;

    private transient PropertyChangeSupport  pcs;


    // init .................................................................................

    static final long serialVersionUID =7707795518722709661L;
    public ClassBrowserHierarchyTranslator () {
        init ();
        setPreferredSize (new Dimension (1, 1));
    }

    protected void init () {
        pcs = new PropertyChangeSupport (this);
        ibms = new InfoBusMemberSupport (this);
        if (!designTime) connect ();
    }


    // HierarchyTranslator implementation ...................................................

    public String[] getFilterNames () {
        return new String[] {
                   "Package", // NOI18N
                   "Object", // NOI18N
                   "Member", // NOI18N
               };
    }

    public String[] getFilterComments () {
        return new String[] {
                   "Check this option to see packages in this view.", // NOI18N
                   "Check this option to see classes and interfaces in this view.", // NOI18N
                   "Check this option to see variables and methods in this view.", // NOI18N
               };
    }

    public Node translate (Node[] nodes, final boolean[] filter, HierarchyTranslator.Filter subFilter) {
        /*
        if ((nodes != null) && (nodes.length > 0)) 
          S ystem.out.println ("translate " + nodes [0]);  
        else  
          S ystem.out.println ("translate " + nodes);  
        S ystem.out.println ("  filter " + filter [0] + ":" + filter [1] + ":" + filter [2]);  
        S ystem.out.println ("  subfilter " + subFilter);  
        */  

        if (nodes == null) {
            if (!filter [0]) return new AbstractNode (Children.LEAF) {
                                        public String toString () {
                                            return "Root of packages [" + super.toString () + "]"; // NOI18N
                                        }
                                    };
            return getPackagesHierarchy (filter, subFilter);
        }
        if (nodes.length < 1) return new AbstractNode (Children.LEAF) {
                                         public String toString () {
                                             return "Empty root of " + (filter [0] ? "packages" : (filter [1] ? "objects" : "members")) + " [" + super.toString () + "]"; // NOI18N
                                         }
                                     };

        if (getType (nodes [0]) != -1)
            return getHierarchyForPackage (
                       (DataFolder) nodes [0].getCookie (DataFolder.class),
                       filter,
                       subFilter
                   );
        return getHierarchyForObject (
                   nodes [0],
                   filter,
                   subFilter
               );
    }

    public HierarchyTranslator.Filter getFilter (
        Node[] nodes,
        boolean[] filter,
        ExplorerBean explorerBean
    ) {
        if (filter [0]) return new PackageFilter (explorerBean);
        if (filter [1]) {
            ObjectFilter objectFilter = new ObjectFilter ();
            return objectFilter;
        }
        return new MemberFilter ();
    }


    // properties ...........................................................................

    public void setInfoBusName (String name) throws PropertyVetoException {
        String old = infoBusName;
        disconnect ();
        infoBusName = name;
        connect ();
        pcs.firePropertyChange ("infoBusName", old, infoBusName); // NOI18N
    }

    public String getInfoBusName () {
        return infoBusName;
    }


    // helper methods .................................................................

    public void addNotify () {
        if (designTime) connect ();
        super.addNotify ();
    }

    public void removeNotify () {
        if (designTime) disconnect ();
        super.removeNotify ();
    }

    public String toString () {
        return "ClassBrowserHierarchyTranslator " + super.hashCode (); // NOI18N
    }

    private Node getPackagesHierarchy (
        boolean[]                  filter,
        HierarchyTranslator.Filter subFilter
    ) {
        if (((PackageFilter)subFilter).isTree ())
            return new TreeNode (
                       TopManager.getDefault ().getPlaces ().nodes ().repository ()
                   );
        else
            return TopManager.getDefault ().getPlaces ().nodes ().packages (
                       ((PackageFilter) subFilter).getDataFilter ()
                   );
    }

    DataObjectFilter ch;
    Node nnn;
    boolean otherDOs;

    private Node getHierarchyForPackage (
        DataFolder df,
        boolean[] filter,
        HierarchyTranslator.Filter subFilter
    ) {

        boolean newOtherDOs = ((ObjectFilter)subFilter).otherDOs;
        if (ch == null) {
            // create new DOF
            ch = new DataObjectFilter ();
            otherDOs = true;
            //S ystem.out.println ("@ClassBrowserHierarchyTranslator.create "); // NOI18N
        } else
            if (newOtherDOs != otherDOs)
                // big changes !
                ch.setDataFolder (null);

        if (otherDOs != newOtherDOs) {
            //S ystem.out.println ("@ClassBrowserHierarchyTranslator.change F! "); // NOI18N
            DataLoader[] loaders = TopManager.getDefault ().getLoaderPool ().toArray ();
            ArrayList as = new ArrayList ();
            int i, k = loaders.length;
            for (i = 0; i < k; i++) {
                Class c = loaders [i].getRepresentationClass ();
                if (ElementCookie.class.isAssignableFrom (c))
                    ch.addLoader (c);
                else
                    if (newOtherDOs)
                        ch.addLoader (c);
                    else
                        ch.removeLoader (c);
            }
            otherDOs = newOtherDOs;
        }

        // create SourceElementFilter
        int f = 0;
        int m = 0;
        if (((ObjectFilter)subFilter).classes)    f += SourceElementFilter.CLASS;
        if (((ObjectFilter)subFilter).interfaces) f += SourceElementFilter.INTERFACE;

        if (((ObjectFilter)subFilter).publicC)    m += SourceElementFilter.PUBLIC;
        if (((ObjectFilter)subFilter).privateC)   m += SourceElementFilter.PRIVATE;
        if (((ObjectFilter)subFilter).protectedC) m += SourceElementFilter.PROTECTED;
        if (((ObjectFilter)subFilter).packageC)   m += SourceElementFilter.PACKAGE;

        SourceElementFilter sef = new SourceElementFilter();
        sef.setOrder (new int[] {f});
        sef.setModifiers (m);
        sef.setAllClasses (true);

        // customize DOF
        //S ystem.out.println ("@ClassBrowserHierarchyTranslator.setDataFolder " + df.getPrimaryFile ()); // NOI18N
        DataFolder oldDf = ch.getDataFolder ();
        if ( (oldDf != null) && (df != null) &&
                (oldDf.equals (df))
           )
            ch.putFilter (SourceElementFilter.class, sef);
        else {
            // optimalization magic...
            ch.setDataFolder (null);
            // to prevent refreshing of lod dataFolder
            ch.putFilter (SourceElementFilter.class, sef);
            ch.setDataFolder (df);
        }

        if (nnn == null)
            nnn = new AbstractNode (ch) {
                  public String toString () {
                      return "Root of objects [" + super.toString () + "]"; // NOI18N
                  }
              };
        return nnn;
    }

    private Node getHierarchyForObject (
        Node n,
        boolean[] filter,
        HierarchyTranslator.Filter subFilter
    ) {
        ElementCookie ec = (ElementCookie) n.getCookie (ElementCookie.class);
        Node root = null;
        if (ec == null)
            root = n.cloneNode ();
        else
            root = ec.getElementsParent ().cloneNode ();

        FilterCookie fc = (FilterCookie) root.getCookie (FilterCookie.class);
        if (fc != null) {
            if (ClassElementFilter.class.isAssignableFrom (fc.getFilterClass ())) {
                ClassElementFilter cef = new ClassElementFilter ();
                int me = 0, fi = 0, co = 0;
                int m = 0;

                if (((MemberFilter) subFilter).publicC)    m += SourceElementFilter.PUBLIC;
                if (((MemberFilter) subFilter).privateC)   m += SourceElementFilter.PRIVATE;
                if (((MemberFilter) subFilter).protectedC) m += SourceElementFilter.PROTECTED;
                if (((MemberFilter) subFilter).packageC)   m += SourceElementFilter.PACKAGE;

                if (((MemberFilter) subFilter).bean) {
                    if (((MemberFilter) subFilter).sorted) {
                        cef.setSorted (true);
                        cef.setOrder (new int[] {256, 512, 1024});
                    } else {
                        cef.setSorted (false);
                        cef.setOrder (new int[] {256 + 512 + 1024});
                    }
                } else {
                    if (((MemberFilter) subFilter).method)      me = ClassElementFilter.METHOD;
                    if (((MemberFilter) subFilter).variable)    fi = ClassElementFilter.FIELD;
                    if (((MemberFilter) subFilter).constructor) co = ClassElementFilter.CONSTRUCTOR;


                    if (((MemberFilter) subFilter).sorted) {
                        cef.setSorted (true);
                        cef.setOrder (new int[] {fi, co, me});
                    } else {
                        cef.setSorted (false);
                        cef.setOrder (new int[] {me + fi + co});
                    }
                }
                cef.setModifiers (m);
                cef.setAllClasses (true);
                fc.setFilter (cef);
            }
        }
        return root;
    }

    private static int getType (Node n) {// VERY VERY PENDING (DO NOT SEE!)
        DataFolder df = (DataFolder) n.getCookie (DataFolder.class);
        if (df != null) return df.getPrimaryFile ().isRoot () ? 0 : 1;
        return -1;
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
        ibms.getInfoBus ().addDataProducer (this);
        connected = true;
        ibms.getInfoBus ().fireItemAvailable (PROPERTY_DEFAULT_TRANSLATOR_ITEM_NAME, null, this);
    }

    private void disconnect () {
        if (!connected) return;
        if (ibms.getInfoBus () == null) return;
        ibms.getInfoBus ().fireItemRevoked (PROPERTY_DEFAULT_TRANSLATOR_ITEM_NAME, this);
        ibms.getInfoBus ().removeDataProducer (this);
        try {
            ibms.leaveInfoBus ();
        } catch (InfoBusMembershipException e) {
            throw new InternalError ();
        } catch (PropertyVetoException e) {
            throw new InternalError ();
        }
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

    public void propertyChange (PropertyChangeEvent e) {
    }

    public void dataItemRequested (InfoBusItemRequestedEvent e) {
        if (connected && e.getDataItemName ().equals (PROPERTY_DEFAULT_TRANSLATOR_ITEM_NAME))
            e.setDataItem (this);
    }


    // innerclasses .........................................................................

    static class PackageFilter implements HierarchyTranslator.Filter, PropertyChangeListener {

        private PropertyChangeSupport         pcs;
        private ExplorerBean                  explorerBean;
        private JComboBox                     cbFilter;
        private ToolbarButton                 bExpandAll;

        private PackagesFilter                packagesFilter;
        private DataFilter                    dataFilter;
        private PropertyChangeListener        listener;
        private ObjectBrowserSettings         obs = new ObjectBrowserSettings ();
        private boolean                       doNotListen = false;


        public PackageFilter (ExplorerBean explorerBean) {
            pcs = new PropertyChangeSupport (this);
            this.explorerBean = explorerBean;
            packagesFilter = obs.getPackageFilter ();
            dataFilter = packagesFilter.getDataFilter ();
            obs.addPropertyChangeListener (new WeakListener.PropertyChange (this));
        }

        DataFilter getDataFilter () {
            return dataFilter;
        }

        boolean isTree () {
            return explorerBean.getExplorerViewClassName ().equals (
                       "org.openide.explorer.view.BeanTreeView"); // NOI18N
        }

        public java.awt.Component getComponent () {
            ResourceBundle bundle = NbBundle.getBundle (ClassBrowserHierarchyTranslator.class);
            JPanel pp = new JPanel (new BorderLayout ());
            pp.setBorder (new CompoundBorder (
                              new EtchedBorder (EtchedBorder.LOWERED),
                              new EmptyBorder (4, 4, 4, 4)
                          ));
            JPanel p = new JPanel (new FlowLayout (FlowLayout.LEFT, 0, 0));

            cbFilter = new JComboBox (packagesFilter.filterNames) {
                           public boolean isFocusTraversable() {
                               return false;
                           }
                           public float getAlignmentY () {
                               return 0;
                           }
                       };
            if (packagesFilter.index >= 0)
                cbFilter.setSelectedIndex (packagesFilter.index);
            Dimension ps = cbFilter.getPreferredSize ();
            ps.width = 100;
            cbFilter.setPreferredSize (ps);
            cbFilter.setMaximumSize (ps);

            cbFilter.addActionListener (new ActionListener () {
                                            public void actionPerformed (ActionEvent e) {
                                                if (doNotListen) return;
                                                packagesFilter.setSelected (cbFilter.getSelectedIndex ());
                                                dataFilter = packagesFilter.getDataFilter ();
                                                pcs.firePropertyChange ("filter", null, null); // NOI18N
                                            }
                                        });
            cbFilter.setEnabled (!isTree ());
            p.add (cbFilter);

            JPanel s = new JPanel () {
                           public float getAlignmentX () {
                               return 0;
                           }
                           public float getAlignmentY () {
                               return 0;
                           }
                       };
            s.setMaximumSize (new Dimension (10, 10));
            p.add (s);

            ToolbarToggleButton b = new ToolbarToggleButton (iTree, isTree ());
            b.setToolTipText (bundle.getString ("CTL_ShowAsTree"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         boolean v;
                                         if (((ToolbarToggleButton)e.getSource ()).isSelected () == (v = isTree ())) return;
                                         try {
                                             if (v)
                                                 explorerBean.setExplorerViewClassName (
                                                     "org.openide.explorer.view.ListView" // NOI18N
                                                 );
                                             else
                                                 explorerBean.setExplorerViewClassName (
                                                     "org.openide.explorer.view.BeanTreeView" // NOI18N
                                                 );
                                             bExpandAll.setEnabled (!v);
                                         } catch (PropertyVetoException ee) {
                                             ee.printStackTrace ();
                                         }
                                     }
                                 });
            p.add (b);

            bExpandAll = new ToolbarButton (iExpand);
            bExpandAll.setToolTipText (bundle.getString ("CTL_ExpandAll"));
            bExpandAll.addActionListener (new ActionListener () {
                                              public void actionPerformed (ActionEvent e) {
                                                  explorerBean.expandAll ();
                                              }
                                          });
            bExpandAll.setEnabled (isTree ());
            p.add (bExpandAll);

            s = new JPanel () {
                    public float getAlignmentX () {
                        return 0;
                    }
                    public float getAlignmentY () {
                        return 0;
                    }
                };
            s.setMaximumSize (new Dimension (10, 10));
            p.add (s);
            pp.add (p, "West"); // NOI18N

            ToolbarButton bb = new ToolbarButton (iDetails);
            bb.setEnabled (!isTree ());
            bb.setToolTipText (bundle.getString ("CTL_SetPackageFilter"));
            bb.addActionListener (new ActionListener () {
                                      public void actionPerformed (ActionEvent e) {
                                          final PackageFilterPanel pfp = new PackageFilterPanel ();
                                          final Dialog[] d = new Dialog [1];
                                          pfp.setPackagesFilter (packagesFilter);
                                          DialogDescriptor descr = new DialogDescriptor (
                                                                       pfp,
                                                                       NbBundle.getBundle (ClassBrowserHierarchyTranslator.class).getString ("CTL_Package_filter"),
                                                                       true, // modal
                                                                       new ActionListener () {
                                                                           public void actionPerformed (ActionEvent e) {
                                                                               if (e.getSource ().equals (DialogDescriptor.OK_OPTION)) {
                                                                                   dataFilter = (packagesFilter = pfp.getPackagesFilter ()).getDataFilter ();
                                                                                   doNotListen = true;
                                                                                   cbFilter.setModel (new DefaultComboBoxModel (
                                                                                                          packagesFilter.filterNames
                                                                                                      ));
                                                                                   doNotListen = false;
                                                                                   if (packagesFilter.index >= 0)
                                                                                       cbFilter.setSelectedIndex (packagesFilter.index);
                                                                               }
                                                                               d [0].setVisible (false);
                                                                               d [0].dispose ();
                                                                           }
                                                                       }
                                                                   );
                                          descr.setHelpCtx (new HelpCtx (ClassBrowserHierarchyTranslator.class.getName () + ".dialog")); // NOI18N
                                          (d [0] = TopManager.getDefault ().createDialog (descr)).show ();
                                      }
                                  });
            p.add (bb);
            pp.add (bb, "East"); // NOI18N
            return pp;
        }

        public void propertyChange (PropertyChangeEvent e) {
            packagesFilter = obs.getPackageFilter ();
            dataFilter = packagesFilter.getDataFilter ();
            if (cbFilter != null) {
                doNotListen = true;
                cbFilter.setModel (new DefaultComboBoxModel (
                                       packagesFilter.filterNames
                                   ));
                doNotListen = false;
                if (packagesFilter.index >= 0)
                    cbFilter.setSelectedIndex (packagesFilter.index);
            }
        }

        public void addPropertyChangeListener (PropertyChangeListener e) {
            pcs.addPropertyChangeListener (e);
        }

        public void removePropertyChangeListener (PropertyChangeListener e) {
            pcs.removePropertyChangeListener (e);
        }
    }

    static class ObjectFilter implements HierarchyTranslator.Filter {
        PropertyChangeSupport pcs;

        boolean classes = true;
        boolean interfaces = true;
        boolean otherDOs = true;
        boolean publicC = true;
        boolean privateC = true;
        boolean protectedC = true;
        boolean packageC = true;


        public ObjectFilter() {
            pcs = new PropertyChangeSupport (this);
        }

        public java.awt.Component getComponent () {
            ResourceBundle bundle = NbBundle.getBundle (ClassBrowserHierarchyTranslator.class);
            JToolBar p = new JToolBar ();
            p.setBorder (new CompoundBorder (
                             new EtchedBorder (EtchedBorder.LOWERED),
                             new EmptyBorder (4, 4, 4, 4)
                         ));
            p.setFloatable (false);

            ToolbarToggleButton b = new ToolbarToggleButton (iClass, true);
            b.setToolTipText (bundle.getString ("CTL_Class"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         classes = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("class", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            b = new ToolbarToggleButton (iInterface, true);
            b.setToolTipText (bundle.getString ("CTL_Interface"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         interfaces = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("interface", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            b = new ToolbarToggleButton (iOtherDOs, true);
            b.setToolTipText (bundle.getString ("CTL_OtherDOs"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         otherDOs = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("otherDOs", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            JPanel s = new JPanel () {
                           public float getAlignmentX () {
                               return 0;
                           }
                           public float getAlignmentY () {
                               return 0;
                           }
                       };
            s.setMaximumSize (new Dimension (10, 10));
            p.add (s);
            b = new ToolbarToggleButton (iPublic, true);
            b.setToolTipText (bundle.getString ("CTL_Public"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         publicC = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("public", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            b = new ToolbarToggleButton (iPackage, true);
            b.setToolTipText (bundle.getString ("CTL_Package"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         packageC = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("package", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            b = new ToolbarToggleButton (iProtected, true);
            b.setToolTipText (bundle.getString ("CTL_Protected"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         protectedC = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("protected", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            b = new ToolbarToggleButton (iPrivate, true);
            b.setToolTipText (bundle.getString ("CTL_Private"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         privateC = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("private", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            return p;
        }

        public void addPropertyChangeListener (PropertyChangeListener e) {
            pcs.addPropertyChangeListener (e);
        }

        public void removePropertyChangeListener (PropertyChangeListener e) {
            pcs.removePropertyChangeListener (e);
        }
    }

    static class MemberFilter implements HierarchyTranslator.Filter {
        PropertyChangeSupport pcs;

        boolean bean = false;
        boolean method = true;
        boolean variable = true;
        boolean constructor = true;
        boolean publicC = true;
        boolean privateC = true;
        boolean protectedC = true;
        boolean packageC = true;
        boolean sorted = true;

        private ToolbarToggleButton bMethod;
        private ToolbarToggleButton bVariable;
        private ToolbarToggleButton bConstructor;
        private ToolbarToggleButton bPublic;
        private ToolbarToggleButton bPackage;
        private ToolbarToggleButton bProtected;
        private ToolbarToggleButton bPrivate;

        public MemberFilter () {
            pcs = new PropertyChangeSupport (this);
        }

        public java.awt.Component getComponent () {
            ResourceBundle bundle = NbBundle.getBundle (ClassBrowserHierarchyTranslator.class);
            JToolBar p = new JToolBar ();
            p.setBorder (new CompoundBorder (
                             new EtchedBorder (EtchedBorder.LOWERED),
                             new EmptyBorder (4, 4, 4, 4)
                         ));
            p.setFloatable (false);

            ToolbarToggleButton b = new ToolbarToggleButton (iBean, false);
            b.setToolTipText (bundle.getString ("CTL_Bean"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         bean = ((AbstractButton)e.getSource ()).isSelected ();
                                         bMethod.setEnabled (!bean);
                                         bVariable.setEnabled (!bean);
                                         bConstructor.setEnabled (!bean);
                                         bPublic.setEnabled (!bean);
                                         bPackage.setEnabled (!bean);
                                         bProtected.setEnabled (!bean);
                                         bPrivate.setEnabled (!bean);
                                         pcs.firePropertyChange ("bean", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            JPanel s = new JPanel () {
                           public float getAlignmentX () {
                               return 0;
                           }
                           public float getAlignmentY () {
                               return 0;
                           }
                       };
            s.setMaximumSize (new Dimension (10, 10));
            p.add (s);
            bMethod = new ToolbarToggleButton (iMethod, true);
            bMethod.setToolTipText (bundle.getString ("CTL_Method"));
            bMethod.addActionListener (new ActionListener () {
                                           public void actionPerformed (ActionEvent e) {
                                               method = ((AbstractButton)e.getSource ()).isSelected ();
                                               pcs.firePropertyChange ("method", null, null); // NOI18N
                                           }
                                       });
            p.add (bMethod);
            bVariable = new ToolbarToggleButton (iVariable, true);
            bVariable.setToolTipText (bundle.getString ("CTL_Variable"));
            bVariable.addActionListener (new ActionListener () {
                                             public void actionPerformed (ActionEvent e) {
                                                 variable = ((AbstractButton)e.getSource ()).isSelected ();
                                                 pcs.firePropertyChange ("variable", null, null); // NOI18N
                                             }
                                         });
            p.add (bVariable);
            bConstructor = new ToolbarToggleButton (iConstructor, true);
            bConstructor.setToolTipText (bundle.getString ("CTL_Constructor"));
            bConstructor.addActionListener (new ActionListener () {
                                                public void actionPerformed (ActionEvent e) {
                                                    constructor = ((AbstractButton)e.getSource ()).isSelected ();
                                                    pcs.firePropertyChange ("constructor", null, null); // NOI18N
                                                }
                                            });
            p.add (bConstructor);
            s = new JPanel () {
                    public float getAlignmentX () {
                        return 0;
                    }
                    public float getAlignmentY () {
                        return 0;
                    }
                };
            s.setMaximumSize (new Dimension (10, 10));
            p.add (s);
            bPublic = new ToolbarToggleButton (iPublic, true);
            bPublic.setToolTipText (bundle.getString ("CTL_Public"));
            bPublic.addActionListener (new ActionListener () {
                                           public void actionPerformed (ActionEvent e) {
                                               publicC = ((AbstractButton)e.getSource ()).isSelected ();
                                               pcs.firePropertyChange ("public", null, null); // NOI18N
                                           }
                                       });
            p.add (bPublic);
            bPackage = new ToolbarToggleButton (iPackage, true);
            bPackage.setToolTipText (bundle.getString ("CTL_Package"));
            bPackage.addActionListener (new ActionListener () {
                                            public void actionPerformed (ActionEvent e) {
                                                packageC = ((AbstractButton)e.getSource ()).isSelected ();
                                                pcs.firePropertyChange ("package", null, null); // NOI18N
                                            }
                                        });
            p.add (bPackage);
            bProtected = new ToolbarToggleButton (iProtected, true);
            bProtected.setToolTipText (bundle.getString ("CTL_Protected"));
            bProtected.addActionListener (new ActionListener () {
                                              public void actionPerformed (ActionEvent e) {
                                                  protectedC = ((AbstractButton)e.getSource ()).isSelected ();
                                                  pcs.firePropertyChange ("protected", null, null); // NOI18N
                                              }
                                          });
            p.add (bProtected);
            bPrivate = new ToolbarToggleButton (iPrivate, true);
            bPrivate.setToolTipText (bundle.getString ("CTL_Private"));
            bPrivate.addActionListener (new ActionListener () {
                                            public void actionPerformed (ActionEvent e) {
                                                privateC = ((AbstractButton)e.getSource ()).isSelected ();
                                                pcs.firePropertyChange ("private", null, null); // NOI18N
                                            }
                                        });
            p.add (bPrivate);
            s = new JPanel () {
                    public float getAlignmentX () {
                        return 0;
                    }
                    public float getAlignmentY () {
                        return 0;
                    }
                };
            s.setMaximumSize (new Dimension (10, 10));
            p.add (s);
            b = new ToolbarToggleButton (iSorted, true);
            b.setToolTipText (bundle.getString ("CTL_Sorted"));
            b.addActionListener (new ActionListener () {
                                     public void actionPerformed (ActionEvent e) {
                                         sorted = ((AbstractButton)e.getSource ()).isSelected ();
                                         pcs.firePropertyChange ("sorted", null, null); // NOI18N
                                     }
                                 });
            p.add (b);
            return p;
        }

        public void addPropertyChangeListener (PropertyChangeListener e) {
            pcs.addPropertyChangeListener (e);
        }

        public void removePropertyChangeListener (PropertyChangeListener e) {
            pcs.removePropertyChangeListener (e);
        }
    }
}

/*
 * Log
 *  31   src-jtulach1.30        1/13/00  Radko Najman    I18N
 *  30   src-jtulach1.29        12/15/99 Jan Jancura     Bug 4906
 *  29   src-jtulach1.28        12/13/99 Jan Jancura     
 *  28   src-jtulach1.27        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  27   src-jtulach1.26        8/18/99  Jan Jancura     Localization
 *  26   src-jtulach1.25        8/17/99  Jan Jancura     Disable madifiers for 
 *       patterns  package toolbar repaired  change curent filter bug repaired
 *  25   src-jtulach1.24        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  24   src-jtulach1.23        8/5/99   Jan Jancura     
 *  23   src-jtulach1.22        7/30/99  Jan Jancura     
 *  22   src-jtulach1.21        7/28/99  Jan Jancura     
 *  21   src-jtulach1.20        7/27/99  Jan Jancura     
 *  20   src-jtulach1.19        7/21/99  Jan Jancura     
 *  19   src-jtulach1.18        7/16/99  Ian Formanek    Fixed bug #1800 - You 
 *       can drag off the explorer toolbar. 
 *  18   src-jtulach1.17        7/9/99   Jesse Glick     Context help.
 *  17   src-jtulach1.16        7/2/99   Jan Jancura     Beans & sorting support
 *  16   src-jtulach1.15        6/11/99  Ian Formanek    Fixed names of 
 *       packages...
 *  15   src-jtulach1.14        6/10/99  Jan Jancura     OB settings & save of 
 *       filters
 *  14   src-jtulach1.13        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   src-jtulach1.12        5/13/99  Jan Jancura     
 *  12   src-jtulach1.11        5/12/99  Jan Jancura     
 *  11   src-jtulach1.10        5/7/99   Jan Jancura     
 *  10   src-jtulach1.9         5/7/99   Jan Jancura     
 *  9    src-jtulach1.8         5/6/99   Jan Jancura     
 *  8    src-jtulach1.7         4/21/99  Jan Jancura     
 *  7    src-jtulach1.6         4/16/99  Jan Jancura     
 *  6    src-jtulach1.5         4/9/99   Jan Jancura     Borders in toolbars 
 *       changed
 *  5    src-jtulach1.4         4/8/99   Jan Jancura     
 *  4    src-jtulach1.3         4/6/99   Ian Formanek    Fixed last change
 *  3    src-jtulach1.2         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  2    src-jtulach1.1         4/2/99   Jan Jancura     
 *  1    src-jtulach1.0         3/23/99  Jan Jancura     
 * $
 */
