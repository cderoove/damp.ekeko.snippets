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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.AbstractAction;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.modules.*;
import org.openide.options.*;
import org.openide.filesystems.*;
import org.openide.actions.*;
import org.openide.util.actions.*;
import org.openide.util.datatransfer.*;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import org.netbeans.core.windows.WindowManagerImpl;

/** Node representing modules */
public class ModuleNode extends AbstractNode {

    private static final ResourceBundle bundle = NbBundle.getBundle( ModuleNode.class );

    /** Icon bases */
    private static final String MODULE_ITEM_ICON_BASE="/org/netbeans/core/resources/moduleItem"; // NOI18N
    // private static final String MODULE_ITEM_DISABLED_BASE="/org/netbeans/core/resources/moduleItemDisabled"; // NOI18N
    private static final String MODULE_TEST_ITEM_ICON_BASE="/org/netbeans/core/resources/testModuleItem"; // NOI18N
    private static final String MODULES_ICON_BASE="/org/netbeans/core/resources/modules"; // NOI18N

    /** Last directory used by the install new module file chooser. */
    private static File lastChosenDir = null;


    /** New types */
    private static NewType[] newTypes = null;


    /** Creates a new ModulesNode */
    public ModuleNode() {
        super(new Modules ());
        setName(Main.getString("CTL_Modules_name"));
        setShortDescription (Main.getString ("CTL_Modules_hint"));
        setIconBase (MODULES_ICON_BASE);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ModuleNode.class);
    }

    public Node cloneNode () {
        return new ModuleNode ();
    }

    protected SystemAction[] createActions () {

        return new SystemAction[] {
                   /*
                   SystemAction.get (AutoLoadModulesAction.class),
                   null,
                   */
                   SystemAction.get (NewAction.class)
               };

    }

    /** Add a sorting property. */
    protected Sheet createSheet () {
        Sheet.Set set = new Sheet.Set ();
        set.setName ("sorting"); // NOI18N
        set.setDisplayName (bundle.getString ("LBL_ModuleNode_sheet_sorting"));
        set.put (((Modules) getChildren ()).createSortingProperty ());
        Sheet sheet = new Sheet ();
        sheet.put (set);
        return sheet;
    }

    public NewType[] getNewTypes () {

        if ( newTypes == null ) {
            newTypes = new NewType[ Boolean.getBoolean ("netbeans.module.test") ? 2 : 1 ]; // NOI18N

            newTypes[0] = new NewType () {
                              public String getName () {
                                  return Main.getString ("CTL_NewModuleByFile");
                              }

                              public void create () throws IOException {
                                  addFile ();
                              }
                          };

            if ( Boolean.getBoolean ("netbeans.module.test")) { // NOI18N
                newTypes[1] = new NewType () {
                                  public String getName () {
                                      return Main.getString( "CTL_NewTestModule");
                                  }

                                  public void create () throws IOException {
                                      TestModuleItem.createNew();
                                  }
                              };
            }

        }

        return newTypes;


        /* Old menu item
        new NewType () {
          public String getName () {
            return Main.getString ("CTL_NewModuleByURL");
          }

          public void create () throws IOException {
            addURL ();
          }
    }
        */

    }

    /** Allows to add new module by specifying its URL.
    */
    /*
    void addURL () throws IOException {
      // PENDING - should be implemented by browser
      NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine (Main.getString ("CTL_ModuleURL"), Main.getString ("CTL_AddModule"));
      if (TopManager.getDefault ().notify (nd) == NotifyDescriptor.OK_OPTION) {
        // ok
        URL url = new URL (nd.getInputText ());
        ModuleInstaller.addModule (url);
      }
}
    */

    /** Allows to add new module by specifying its file.
    */
    void addFile () throws IOException {
        final JFileChooser chooser = new JFileChooser ();
        if (lastChosenDir != null) chooser.setCurrentDirectory (lastChosenDir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setApproveButtonText (Main.getString ("CTL_ModuleSelect"));
        chooser.setApproveButtonToolTipText (Main.getString ("CTL_ModuleSelectToolTip"));
        chooser.setFileFilter (new javax.swing.filechooser.FileFilter () {
                                   public String getDescription () {
                                       return Main.getString ("CTL_ModuleSelectFilter");
                                   }
                                   public boolean accept (File f) {
                                       return f.isDirectory () || f.getName ().endsWith (".jar"); // NOI18N
                                   }
                               });

        int result = chooser.showOpenDialog (WindowManagerImpl.mainWindow ());
        lastChosenDir = chooser.getCurrentDirectory ();
        if (result == JFileChooser.APPROVE_OPTION) {
            // install the file
            //URL u = chooser.getSelectedFile ().toURL ();
            ModuleInstaller.installFromFile ( chooser.getSelectedFile() );
        }

    }

    /** Class representing node of one standard module
     */
    static class Item extends AbstractNode {

        protected ModuleItem item;

        public Item (ModuleItem item) {
            super (Children.LEAF);
            this.item = item;

            ModuleDescription moduleDesc = item.getDescription ();
            setName (moduleDesc.getName ());
            setIconBase ( MODULE_ITEM_ICON_BASE );
            //setIconBase (item.isEnabled() ? MODULE_ITEM_ICON_BASE : MODULE_ITEM_DISABLED_BASE );
            setDefaultAction (SystemAction.get (PropertiesAction.class));
        }

        public HelpCtx getHelpCtx () {
            String id = (String) Help.getDefault ().getHomesByCode ().get (item.getDescription ().getCodeName ());
            if (id != null && item.isEnabled ())
                return new HelpCtx (id);
            else
                return new HelpCtx (Item.class);
        }

        // [PENDING - properties, short description]

        public void destroy () {

            Collection depModules = ModuleInstaller.getDependentModules( item );

            if ( depModules.size() > 0 ) {
                StringBuffer sb = new StringBuffer( 200 );

                sb.append( bundle.getString( "MSG_UninstallOthers" ) ).append( "\n" );

                Iterator it = depModules.iterator();
                while( it.hasNext() ) {
                    sb.append( ((ModuleItem)it.next()).getDescription().getCodeNameBase() );
                    sb.append( "\n" ); // NOI18N
                }

                NotifyDescriptor nd = new NotifyDescriptor.Confirmation( sb.toString(), NotifyDescriptor.OK_CANCEL_OPTION );
                TopManager.getDefault().notify( nd );
                if ( nd.getValue() == DialogDescriptor.CANCEL_OPTION )
                    return;
                else {
                    it = depModules.iterator();
                    while( it.hasNext() ) {
                        ModuleInstaller.deleteModule( (ModuleItem)it.next() );
                    }
                }
            }

            ModuleInstaller.deleteModule( item );
        }

        public boolean canDestroy () {
            return item.canDestroy();
        }

        /** Creates properties.
        */
        protected Sheet createSheet () {
            Sheet s = Sheet.createDefault ();
            Sheet.Set ss = s.get (Sheet.PROPERTIES);

            Sheet.Set sse = Sheet.createExpertSet ();
            s.put(sse);

            try {
                PropertySupport p;

                p = new PropertySupport.ReadOnly (
                        "name", // NOI18N
                        String.class,
                        Main.getString ("PROP_modules_name"),
                        Main.getString ("HINT_modules_name")
                    ) {
                        public Object getValue () {
                            return item.getDescription ().getName ();
                        }

                    };
                ss.put (p);

                p = new PropertySupport.ReadWrite (
                        "enabled", // NOI18N
                        Boolean.TYPE,
                        Main.getString ("PROP_modules_enabled"),
                        Main.getString ("HINT_modules_enabled")
                    ) {
                        public Object getValue () {
                            return new Boolean (item.isEnabled ());
                        }

                        public void setValue (Object o) {

                            if ( !((Boolean)o).booleanValue () ) {

                                // Ask about other modules to disable
                                Collection depModules = ModuleInstaller.getDependentModules( item );

                                if ( depModules.size() > 0 ) {

                                    StringBuffer sb = new StringBuffer( 200 );

                                    sb.append( bundle.getString( "MSG_DisableOthers" ) ).append( "\n" );

                                    Iterator it = depModules.iterator();
                                    while( it.hasNext() ) {
                                        sb.append( ((ModuleItem)it.next()).getDescription().getCodeNameBase() );
                                        sb.append( "\n" ); // NOI18N
                                    }

                                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation( sb.toString(), NotifyDescriptor.OK_CANCEL_OPTION );
                                    TopManager.getDefault().notify( nd );
                                    if ( nd.getValue() == DialogDescriptor.CANCEL_OPTION )
                                        return;
                                    else {
                                        it = depModules.iterator();
                                        while( it.hasNext() ) {
                                            ((ModuleItem)it.next() ).setEnabled( false );
                                        }
                                    }
                                }
                            }
                            else { // Test if some other modules should be enabled

                                if ( ModuleInstaller.resolveOrdering (Collections.nCopies (1, item),
                                                                      ModuleInstallerSupport.ENABLED_MODULE | ModuleInstallerSupport.DISABLED_MODULE,
                                                                      true ).isEmpty () ) {
                                    NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                                                                      bundle.getString( "MSG_Module_Missed_Enabled" ) + "\n" +
                                                                      ModuleInstaller.getMissed(
                                                                          ModuleInstallerSupport.ENABLED_MODULE | ModuleInstallerSupport.DISABLED_MODULE,
                                                                          Collections.nCopies (1, item) ) );
                                    TopManager.getDefault().notify( nd );


                                    return;
                                }

                                Collection orderedModules = ModuleInstaller.checkDependenciesOnDisabled ( item.getDescription() );

                                if ( orderedModules != null ) {
                                    Iterator it = orderedModules.iterator();
                                    while( it.hasNext() ) {
                                        ((ModuleItem)it.next() ).setEnabled( true );
                                    }
                                }
                                else
                                    return;
                            }

                            item.setEnabled (((Boolean)o).booleanValue ());
                            // setIconBase (item.isEnabled() ? MODULE_ITEM_ICON_BASE : MODULE_ITEM_DISABLED_BASE );
                        }
                    };
                ss.put (p);

                p = new PropertySupport.ReadOnly (
                        "specVersion", // NOI18N
                        String.class,
                        Main.getString ("PROP_modules_specversion"),
                        Main.getString ("HINT_modules_specversion")
                    ) {
                        public Object getValue () {
                            return item.getDescription ().getSpecVersion ();
                        }

                    };
                ss.put (p);

                p = new PropertySupport.ReadOnly (
                        "implVersion", // NOI18N
                        String.class,
                        Main.getString ("PROP_modules_implversion"),
                        Main.getString ("HINT_modules_implversion")
                    ) {
                        public Object getValue () {
                            return item.getDescription ().getImplVersion ();
                        }

                    };
                ss.put (p);

                p = new PropertySupport.ReadOnly (
                        "codeName", // NOI18N
                        String.class,
                        Main.getString ("PROP_modules_codename"),
                        Main.getString ("HINT_modules_codename")
                    ) {
                        public Object getValue () {
                            return item.getDescription ().getCodeName ();
                        }

                    };
                sse.put (p);

                p = new PropertySupport.ReadOnly (
                        "url", // NOI18N
                        java.net.URL.class,
                        Main.getString ("PROP_modules_url"),
                        Main.getString ("HINT_modules_url")
                    ) {
                        public Object getValue () {
                            return item.getLoaderURL ();
                        }

                    };
                sse.put (p);

            } catch (Exception e) {
                e.printStackTrace ();
                throw new InternalError ();
            }

            return s;
        }


    }

    /** Class rerpesenting a testmodule */
    static class TestItem extends Item  {

        private static final SystemAction[] SYSTEM_ACTIONS = new SystemAction[] {
                    SystemAction.get( ReinstallTestModuleAction.class ),
                    SystemAction.get( PropertiesAction.class) };

        public TestItem (TestModuleItem item) {
            super( item );

            this.item = item;

            customizeSheet();

            ModuleDescription moduleDesc = item.getDescription ();
            setName (moduleDesc.getName ());
            setIconBase (MODULE_TEST_ITEM_ICON_BASE);
        }

        public Node.Cookie getCookie( Class clazz ) {
            if ( clazz == TestModuleItem.class )
                return (Node.Cookie) item;

            return super.getCookie( clazz );
        }

        public SystemAction[] getActions() {
            return SYSTEM_ACTIONS;
        }

        void customizeSheet () {
            Sheet.Set ss = getSheet().get (Sheet.PROPERTIES);
            ss.remove( "enabled" ); // NOI18N

            PropertySupport p = new PropertySupport.ReadOnly (
                                    "enabled", // NOI18N
                                    Boolean.TYPE,
                                    Main.getString ("PROP_modules_enabled"),
                                    Main.getString ("HINT_modules_enabled")
                                ) {
                                    public Object getValue () {
                                        //return new Boolean ( item.isEnabled ());
                                        return new Boolean ( true );
                                    }
                                };
            ss.put (p);

        }
    }

    /** Children that contains modules installed it has to
     * dissingushg between modules and test modules.
     */
    private static class Modules extends Children.Keys implements PropertyChangeListener {

        /** sorting style
        * preferably would be stored permanently somewhere but there is nowhere good to store it
        */
        private int sortedModuleList = SORT_UNSORTED;
        private static final int SORT_UNSORTED = 0;
        private static final int SORT_DISPLAYNAME = 1;
        private static final int SORT_CODENAME = 2;
        private static final int SORT_ENABLED = 3;
        private static final int SORT_URL = 4;

        /** Refreshed list of nodes acc. to current sorting and contents. */
        private void refreshKeys () {
            ModuleItem[] items = ModuleInstaller.getModuleItems (
                                     ModuleInstallerSupport.ENABLED_MODULE |
                                     ModuleInstallerSupport.DISABLED_MODULE |
                                     ModuleInstallerSupport.TEST_MODULE );
            if (sortedModuleList != SORT_UNSORTED) {
                items = (ModuleItem[]) items.clone ();
                Arrays.sort (items, new Comparator () {
                                 public int compare (Object o1, Object o2) {
                                     ModuleItem m1 = (ModuleItem) o1;
                                     ModuleItem m2 = (ModuleItem) o2;
                                     switch (sortedModuleList) {
                                     case SORT_CODENAME:
                                         return m1.getDescription ().getCodeName ().compareTo (m2.getDescription ().getCodeName ());
                                     case SORT_ENABLED:
                                         if (m1.isEnabled () && ! m2.isEnabled ()) return -1;
                                         if (! m1.isEnabled () && m2.isEnabled ()) return 1;
                                         if ((m1 instanceof TestModuleItem) && ! (m2 instanceof TestModuleItem)) return 1;
                                         if (! (m1 instanceof TestModuleItem) && (m2 instanceof TestModuleItem)) return -1;
                                         // fallthrough
                                     case SORT_DISPLAYNAME:
                                         return m1.getDescription ().getName ().compareTo (m2.getDescription ().getName ());
                                     case SORT_URL:
                                         if ((m1 instanceof TestModuleItem) && ! (m2 instanceof TestModuleItem)) return 1;
                                         if (! (m1 instanceof TestModuleItem) && (m2 instanceof TestModuleItem)) return -1;
                                         if ((m1 instanceof TestModuleItem) && (m2 instanceof TestModuleItem)) return 0;
                                         return m1.getLoaderURL ().toString ().compareTo (m2.getLoaderURL ().toString ());
                                     default:
                                         return 0;
                                     }
                                 }
                             });
            }
            setKeys (items);
        }

        Node.Property createSortingProperty () {
            return new PropertySupport.ReadWrite ("sorted", Integer.TYPE, // NOI18N
                                                  bundle.getString ("PROP_ModuleNode_sorted"),
                                                  bundle.getString ("HINT_ModuleNode_sorted")) {
                       public Object getValue () {
                           return new Integer (sortedModuleList);
                       }
                       public void setValue (Object o) {
                           sortedModuleList = ((Integer) o).intValue ();
                           refreshKeys ();
                       }
                       public boolean supportsDefaultValue () {
                           return true;
                       }
                       public void restoreDefaultValue () {
                           setValue (new Integer (SORT_UNSORTED));
                       }
                       public PropertyEditor getPropertyEditor () {
                           return new PropertyEditorSupport () {
                                      private final String[] tags = new String[] {
                                                                        bundle.getString ("LBL_ModuleNode_SORT_UNSORTED"),
                                                                        bundle.getString ("LBL_ModuleNode_SORT_DISPLAYNAME"),
                                                                        bundle.getString ("LBL_ModuleNode_SORT_CODENAME"),
                                                                        bundle.getString ("LBL_ModuleNode_SORT_ENABLED"),
                                                                        bundle.getString ("LBL_ModuleNode_SORT_URL"),
                                                                    };
                                      public String[] getTags () {
                                          return tags;
                                      }
                                      public String getAsText () {
                                          return tags[((Integer) this.getValue ()).intValue ()];
                                      }
                                      public void setAsText (String text) {
                                          for (int i = 0; i < tags.length; i++) {
                                              if (tags[i].equals (text)) {
                                                  this.setValue (new Integer (i));
                                                  return;
                                              }
                                          }
                                          throw new IllegalArgumentException ();
                                      }
                                  };
                       }
                   };
        }

        /** Initializes content */
        public void addNotify () {
            ModuleInstaller.addPropertyChangeListener (this);
            refreshKeys ();
        }

        /** Releases listener. */
        public void removeNotify () {
            ModuleInstaller.removePropertyChangeListener (this);
        }

        /** Reacts to changes */
        public void propertyChange (PropertyChangeEvent ev) {
            refreshKeys ();
        }

        /** Generates node for the ModuleItem key */
        protected Node[] createNodes (Object key) {
            if ( key instanceof TestModuleItem ) {
                Node[] nodes = new Node[] { new TestItem ((TestModuleItem)key) };
                return nodes;
            }
            else
                return new Node[] { new Item ((ModuleItem)key) };
        }
    }

    /** Action that refreshes the modules in the modules directory.
    */
    public static final class AutoLoadModulesAction extends CallableSystemAction {

        static final long serialVersionUID =-6598195161786990764L;

        public String getName () {
            return Main.getString ("CTL_AutoLoadModules");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (AutoLoadModulesAction.class);
        }

        public void performAction () {
            ModuleInstaller.autoLoadModules ();
        }
    }

    /** Action deinstalls and installs again the test module. Used by the module developer
     * after compilation to test the changes.
     */ 
    public static final class ReinstallTestModuleAction extends CookieAction {

        private static final Class[] cookieClasses = new Class[] { TestModuleItem.class };

        static final long serialVersionUID =7167727704611723438L;
        public String getName () {
            return Main.getString ("CTL_ReinstallTestModule");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (ReinstallTestModuleAction.class);
        }

        protected Class[] cookieClasses() {
            return cookieClasses;
        }

        protected boolean enable( Node[] nodes ) {
            return true;
        }

        protected int mode() {
            return CookieAction.MODE_ALL;
        }

        public void performAction ( Node activatedNodes[] ) {

            for ( int i = 0; i < activatedNodes.length; i++ ) {
                TestModuleItem item = (TestModuleItem)activatedNodes[i].getCookie( TestModuleItem.class );
                item.reinstall();
            }

            //ModuleInstaller.autoLoadModules ();
        }
    }

}


/*
 * Log
 *  22   Gandalf   1.21        1/13/00  Jaroslav Tulach I18N
 *  21   Gandalf   1.20        1/7/00   Petr Hrebejk    Messages added to 
 *       enabling modules and installing from file
 *  20   Gandalf   1.19        1/5/00   Petr Hrebejk    New module installer
 *  19   Gandalf   1.18        12/22/99 Jesse Glick     Better module sorting.
 *  18   Gandalf   1.17        12/21/99 Jesse Glick     Module list may now be 
 *       sorted, optionally.
 *  17   Gandalf   1.16        11/26/99 Patrik Knakal   
 *  16   Gandalf   1.15        11/10/99 Petr Hrebejk    Unistalling/Disabling of
 *       module now Uninstalls/Disables all dependent modules
 *  15   Gandalf   1.14        10/27/99 Petr Hrebejk    Testing of modules added
 *  14   Gandalf   1.13        10/24/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  13   Gandalf   1.12        10/18/99 Jesse Glick     Minor usability 
 *       improvements--PropertiesAction default on item nodes; file chooser 
 *       always stores current dir.
 *  12   Gandalf   1.11        9/30/99  Jesse Glick     Brief log messages re. 
 *       module installation and uninstallation, for tech support purposes. Also
 *       improved file chooser for New from File.
 *  11   Gandalf   1.10        8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  10   Gandalf   1.9         7/12/99  Jaroslav Tulach To be compilable.
 *  9    Gandalf   1.8         7/9/99   Jesse Glick     Better context help.
 *  8    Gandalf   1.7         7/8/99   Jesse Glick     Context help.
 *  7    Gandalf   1.6         7/2/99   Jaroslav Tulach Enabled add module from 
 *       file
 *  6    Gandalf   1.5         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/8/99   Petr Hrebejk    Node PoupMenu disabled
 *  3    Gandalf   1.2         5/27/99  Jaroslav Tulach auto load of modules.
 *  2    Gandalf   1.1         5/8/99   Ian Formanek    Improved ModuleNode 
 *       properties
 *  1    Gandalf   1.0         4/28/99  Jaroslav Tulach 
 * $
 */
