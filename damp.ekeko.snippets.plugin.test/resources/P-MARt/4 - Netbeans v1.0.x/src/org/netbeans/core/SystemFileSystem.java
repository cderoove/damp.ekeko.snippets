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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.JMenuItem;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
/*
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;
import org.openide.awt.JInlineMenu;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
*/

/** The system FileSystem - represents system files under $NETBEANS_HOME/system.
*
* @author Jan Jancura, Ian Formanek, Petr Hamernik
*/
public class SystemFileSystem extends MultiFileSystem implements FileSystem.Status {
    // Must be public for BeanInfo to work.

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7761052280240991668L;


    /** Resource for all localized strings in jar file system. */
    //org.openide.util.NbBundle.getBundle(SystemFileSystem.class);

    /** system name of this filesystem */
    private static final String SYSTEM_NAME = "SystemFileSystem"; // NOI18N

    /** name of system folder to be located in the USER_DIR and HOME_DIR */
    private static final String SYSTEM_FOLDER = "system"; // NOI18N

    /** name of file attribute with localizing bundle */
    private static final String ATTR_BUNDLE = "SystemFileSystem.localizingBundle"; // NOI18N

    /** user fs */
    private FileSystem user;
    /** home fs */
    private FileSystem home;

    /** message to format file in netbeans.home */
    private static MessageFormat homeFormat;
    /** message to format file in netbeans.user */
    private static MessageFormat userFormat;

    /** @param fss list of file systems to delegate to
    */
    private SystemFileSystem (FileSystem[] fss) throws PropertyVetoException {
        super (fss);
        user = fss[0];
        home = fss.length > 1 ? fss[1] : null;

        setSystemName (SYSTEM_NAME);
        setHidden (true);
    }


    /** Name of the system */
    public String getDisplayName () {
        return Main.getString ("CTL_SystemFileSystem");
    }


    /** This filesystem cannot be removed from pool, it is persistent.
    */
    public boolean isPersistent () {
        return true;
    }

    public void prepareEnvironment (FileSystem.Environment env)
    throws EnvironmentNotSupportedException {
        if (home != null) home.prepareEnvironment (env);
        if (user != null) user.prepareEnvironment (env);
    }

    public FileSystem.Status getStatus () {
        return this;
    }

    /** Annotate name
    */
    public String annotateName (String s, Set set) {

        // Look for a localized file name.
        // [PENDING] this will mask [Local] annotations, which sometimes will be annoying.
        // Perhaps there should be a file system action which indicates with a readonly checkbox
        // whether the given file is local or not.
        // Note: all files in the set are checked. But please only place the attribute
        // on the primary file, and use this primary file name as the bundle key.
        Iterator it = set.iterator ();
        while (it.hasNext ()) {
            // annotate a name
            FileObject fo = (FileObject) it.next ();

            String bundleName = (String)fo.getAttribute (ATTR_BUNDLE); // NOI18N
            if (bundleName != null) {
                try {
                    bundleName = org.openide.util.Utilities.translate(bundleName);
                    // Note: system class loader used here for efficiency, meaning that
                    // you must have actually loaded the bundle into a JAR or classpath
                    // for this to work; will not work from e.g. test modules.
                    ResourceBundle b = NbBundle.getBundle (bundleName, Locale.getDefault (), TopManager.getDefault ().currentClassLoader ());
                    try {
                        return b.getString (fo.getPackageNameExt ('/', '.'));
                    } catch (MissingResourceException ex) {
                        // ignore--normal
                    }
                } catch (MissingResourceException ex) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                        ex.printStackTrace ();
                    // ignore
                }
            }
        }

        return annotateNameNoLocalization (s, set);
    }

    /** Annotate name but do not consider using localized name. */
    private String annotateNameNoLocalization (String s, Set set) {

        if (home == null || user == null) {
            // no annotation if not running as multiuser
            return s;
        }

        Iterator it = set.iterator ();
        int cnt = 0;
        while (it.hasNext ()) {
            FileObject fo = (FileObject)it.next ();
            if (!fo.isRoot ()) {
                cnt++;
            }
            if (findSystem (fo) == home) {
                return getHomeFormat ().format (new Object[] { s });
            }
        }

        if (cnt == 0) {
            // only roots
            return s;
        }

        return getUserFormat ().format (new Object[] { s });
    }

    /** Annotate icon
    */
    public java.awt.Image annotateIcon (java.awt.Image im, int type, java.util.Set s) {
        return im;
    }

    /** Add an action to show the unlocalized name, if applicable. */
    /*
    public SystemAction[] getActions () {
      System.err.println("SFS.getActions");
      SystemAction[] orig = super.getActions ();
      SystemAction mine = SystemAction.get (ShowUnlocalizedNameAction.class);
      if (orig == null || orig.length == 0) {
        return new SystemAction[] { mine };
      } else {
        SystemAction[] nue = new SystemAction[orig.length + 1];
        System.arraycopy (orig, 0, nue, 0, orig.length);
        nue[orig.length] = mine;
        return nue;
      }
}

    public static class ShowUnlocalizedNameAction extends NodeAction {
      
      public String getName () {
        System.err.println("SUNA.getName");
        return ""; // NOI18N
      }
      
      protected boolean enable (Node[] nodes) {
        System.err.println("SUNA.enable");
        // [PENDING] shows an extra separator even on unloc. files
        return true;
      }
      
      protected void performAction (Node[] nodes) {
        System.err.println("SUNA.performAction");
        // ignore
      }
      
      public HelpCtx getHelpCtx () {
        return new HelpCtx (ShowUnlocalizedNameAction.class);
      }
      
      public JMenuItem getPopupPresenter () {
        System.err.println("SUNA.getPP");
        JInlineMenu menu = new JInlineMenu ();
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        System.err.println("#1");
        if (nodes.length != 1) return menu;
        DataObject dob = (DataObject) nodes[0].getCookie (DataObject.class);
        System.err.println("#2");
        if (dob == null) return menu;
        FileObject fo = dob.getPrimaryFile ();
        FileSystem fs;
        try {
          System.err.println("#3");
          if (! ((fs = fo.getFileSystem ()) instanceof SystemFileSystem)) return menu;
        } catch (FileStateInvalidException fsie) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
            fsie.printStackTrace ();
          return menu;
        }
        System.err.println("#4");
        if (fo.getAttribute (ATTR_BUNDLE) == null) return menu;
        String unlocName = ((SystemFileSystem) fs).annotateNameNoLocalization (fo.getName (), Collections.singleton (fo));
        JMenuItem item = new JMenuItem ();
        // [PENDING] it does not actually display although item is set correctly! why?
        //item.setEnabled (false);
        item.setText (MessageFormat.format (NbBundle.getBundle (SystemFileSystem.class).getString ("LBL_unlocalized_name"),
                                            new Object[] { unlocName }));
        System.err.println("item.text=" + item.getText ());
        menu.setMenuItems (new JMenuItem[] { item });
        System.err.println("#5");
        return menu;
      }
      
}
    */

    /** Initializes and creates new repository. This repository's system fs is
    * based on the content of ${HOME_DIR}/system and ${USER_DIR}/system directories
    *
    * @param userDir directory where user can write 
    * @param homeDir directory where netbeans has been installed, user need not have write access
    * @return repository
    * @exception PropertyVetoException if something fails
    */
    public static Repository createRepository (String userDir, String homeDir)
    throws java.beans.PropertyVetoException, IOException {
        LocalFileSystem user;
        LocalFileSystem home;

        // only one file system
        user = new LocalFileSystem ();
        user.setRootDirectory (new File (userDir + File.separatorChar + SYSTEM_FOLDER));

        if (userDir.equals (homeDir)) {
            home = null;
        } else {
            // home directory is different
            home = new LocalFileSystem ();
            home.setRootDirectory (new File (homeDir + File.separatorChar + SYSTEM_FOLDER));
        }

        FileSystem[] arr = new FileSystem[home == null ? 1 : 2];
        arr[0] = user;
        if (home != null) {
            arr[1] = home;
        }

        Repository rep = new Repository (
                             new SystemFileSystem (arr)
                         );
        return rep;
    }

    /** The instance of this file system.
    */
    //  public static SystemFileSystem getDefault () {
    //    return (SystemFileSystem)NbTopManager.getDefaultRepository ().getDefaultFileSystem ();
    //  }

    /** Getter for message.
    */
    private static MessageFormat getUserFormat () {
        if (userFormat == null) {
            userFormat = new MessageFormat (NbBundle.getBundle(SystemFileSystem.class).getString("CTL_UserFile"));
        }
        return userFormat;
    }

    /** Getter for message.
    */
    private static MessageFormat getHomeFormat () {
        if (homeFormat == null) {
            homeFormat = new MessageFormat (NbBundle.getBundle(SystemFileSystem.class).getString("CTL_HomeFile"));
        }
        return homeFormat;
    }

    /** Notification that a file has migrated from one file system
    * to another. Usually when somebody writes to file on readonly file
    * system and the file has to be copied to write one. 
    * <P>
    * This method allows subclasses to fire for example FileSystem.PROP_STATUS
    * change to notify that annotation of this file should change.
    *
    * @param fo file object that change its actual file system
    */
    protected void notifyMigration (FileObject fo) {
        fireFileStatusChanged (new FileStatusEvent (this, fo, false, true));
    }

}

/*
 * Log
 *  22   Gandalf   1.21        4/14/00  Ales Novak      repackaging
 *  21   Gandalf   1.20        1/20/00  Pavel Buzek     use currentClassLoader 
 *       for access to bundle (works with module for test)
 *  20   Gandalf   1.19        1/15/00  Ian Formanek    NOI18N
 *  19   Gandalf   1.18        1/14/00  Jesse Glick     No default for 
 *       localizing bundles.
 *  18   Gandalf   1.17        1/13/00  Jesse Glick     More localized file 
 *       names.
 *  17   Gandalf   1.16        1/13/00  Jaroslav Tulach File names can be 
 *       localized.  
 *  16   Gandalf   1.15        1/13/00  Jaroslav Tulach I18N
 *  15   Gandalf   1.14        11/11/99 Jesse Glick     Display miscellany.
 *  14   Gandalf   1.13        11/2/99  Jaroslav Tulach hidden + does not 
 *       annotate when not multiuser
 *  13   Gandalf   1.12        10/29/99 Jaroslav Tulach Supports environment.
 *  12   Gandalf   1.11        10/29/99 Jaroslav Tulach MultiFileSystem + 
 *       FileStatusEvent
 *  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/30/99  Ian Formanek    Updated for new project 
 *       files names
 *  8    Gandalf   1.7         5/28/99  Ian Formanek    Cleaned up
 *  7    Gandalf   1.6         3/26/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  5    Gandalf   1.4         3/9/99   Jan Jancura     Bundles moved
 *  4    Gandalf   1.3         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  3    Gandalf   1.2         1/25/99  Jaroslav Tulach Added default project, 
 *       its desktop and changed default explorer in Main.
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
