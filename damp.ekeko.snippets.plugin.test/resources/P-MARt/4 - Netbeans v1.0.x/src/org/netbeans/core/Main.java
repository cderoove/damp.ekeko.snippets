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
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.*;
import org.openide.cookies.ExecCookie;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.*;
import org.openide.options.ControlPanel;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;
import org.netbeans.core.actions.*;
import org.netbeans.core.windows.WindowManagerImpl;


/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public final class Main extends NbTopManager {

    /** directory for modules */
    static final String DIR_MODULES = "modules"; // NOI18N

    /* The class of the UIManager to be used for netbeans - can be set by command-line argument -ui <class name> */
    private static Class uiClass;

    /* The size of the fonts in the UI - 11pt by default, can be set by command-line argument -fontsize <size> */
    private static int uiFontSize = 11;

    /** The netbeans home dir - acquired from property netbeans.home */
    static String homeDir;
    /** The netbeans user dir - acquired from property netbeans.user / or copied from homeDir if such property does not exist */
    static String userDir;
    /** The netbeans system dir - it is netbeans.home/system or a folder specified via -system option */
    static String systemDir;

    /** Dialog for TipsOfTheDay, stored to be reused */
    static Dialog tipsDialog;
    private static TipsOnStartup tipsComponent;

    /** is there a splash screen or not */
    private static Splash.SplashOutput splash;

    /** The flag whether to create the log - can be set via -nologging
    * command line option */
    private static boolean noLogging = false;

    /** Flag telling if the system clipboard should be used or not.
    * This helps avoid crashes and hangs on Unixes.
    */
    private static boolean noSysClipboard = false;

    /** The flag whether to show the Splash screen on the startup */
    private static boolean noSplash = false;

    /** The Class that logs the IDE events to a log file */
    private static TopLogging logger;

    /** Overrides the default status line method to display first in the splash
    * if there is one
    */
    public void setStatusText (String s) {
        Splash.SplashOutput sout = splash;
        if (sout != null) {
            sout.print (s);
        } else {
            super.setStatusText (s);
        }
    }



    public static void showTipsOfTheDay () {
        if (tipsDialog == null) {
            java.util.ResourceBundle bundle = NbBundle.getBundle(Main.class);
            final Object [] options = new Object [] {
                                          new JButton (bundle.getString("CTL_NEXT_BUTTON")),
                                          new JButton (bundle.getString("CTL_CLOSE_BUTTON"))
                                      };
            tipsComponent = new TipsOnStartup ();
            DialogDescriptor descriptor = new DialogDescriptor (
                                              tipsComponent,
                                              NbBundle.getBundle(Main.class).getString("CTL_START_TITLE"),
                                              true,
                                              options,
                                              options[0],
                                              DialogDescriptor.BOTTOM_ALIGN,
                                              null, // HelpCtx
                                              new ActionListener () {
                                                  public void actionPerformed (ActionEvent evt) {
                                                      if (options[0].equals (evt.getSource ())) {
                                                          tipsComponent.nextTip ();
                                                      } else if (options[1].equals (evt.getSource ())) {
                                                          tipsDialog.setVisible (false);
                                                      }
                                                  }
                                              }
                                          );
            tipsDialog = TopManager.getDefault ().createDialog (descriptor);
        }
        tipsComponent.restore ();
        tipsDialog.show ();

    }

    private static void showSystemInfo() {
        System.out.println("-- " + getString("CTL_System_info") + " ----------------------------------------------------------------");
        TopLogging.printSystemInfo(System.out);
        System.out.println("-------------------------------------------------------------------------------"); // NOI18N
    }

    private static void showHelp() {
        System.out.println(getString("CTL_Cmd_options"));
        System.out.println(getString("CTL_System_option"));
        System.out.println("                      " + getString("CTL_System_option2"));
        System.out.println(getString("CTL_UI_option"));
        System.out.println(getString("CTL_FontSize_option"));
        System.out.println(getString("CTL_Locale_option"));
        System.out.println(getString("CTL_Noinfo_option"));
        System.out.println(getString("CTL_Nologging_option"));
        System.out.println(getString("CTL_Nosysclipboard_option"));
    }

    private static void parseCommandLine(String[] args) {
        boolean noinfo = false;

        // let's go through the command line
        for(int i = 0; i < args.length; i++)
        {
            if (args[i].equalsIgnoreCase("-nosplash")) // NOI18N
                noSplash = true;
            if (args[i].equalsIgnoreCase("-noinfo")) // NOI18N
                noinfo = true;
            else if (args[i].equalsIgnoreCase("-nologging")) // NOI18N
                noLogging = true;
            else if (args[i].equalsIgnoreCase("-nosysclipboard")) // NOI18N
                noSysClipboard = true;
            else if (args[i].equalsIgnoreCase("-system")) { // NOI18N
                systemDir = args[++i];
            }
            else if (args[i].equalsIgnoreCase("-ui")) { // NOI18N
                try {
                    uiClass = Class.forName(args[++i]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.out.println(getString("ERR_UIExpected"));
                } catch (ClassNotFoundException e2) {
                    System.out.println(getString("ERR_UINotFound"));
                }
            } else if (args[i].equalsIgnoreCase("-fontsize")) { // NOI18N
                try {
                    uiFontSize = Integer.parseInt (args[++i]);
                } catch(ArrayIndexOutOfBoundsException e) {
                    System.out.println(getString("ERR_FontSizeExpected"));
                } catch (NumberFormatException e2) {
                    System.out.println(getString("ERR_BadFontSize"));
                }

            } else if (args[i].equalsIgnoreCase("-locale")) { // NOI18N
                String localeParam = args[++i];
                String language;
                String country = ""; // NOI18N
                String variant = ""; // NOI18N
                int index1 = localeParam.indexOf(":"); // NOI18N
                if (index1 == -1)
                    language = localeParam;
                else {
                    language = localeParam.substring(0, index1);
                    int index2 = localeParam.indexOf(":", index1+1); // NOI18N
                    if (index2 != -1) {
                        country = localeParam.substring(index1+1, index2);
                        variant = localeParam.substring(index2+1);
                    }
                    else
                        country = localeParam.substring(index1+1);
                }
                java.util.Locale.setDefault(new java.util.Locale(language, country, variant));
            }
            else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("-help")) { // NOI18N
                showHelp();
                doExit(0);
            }
            else {
                System.out.println(getString("ERR_UnknownOption")+": "+args[i]);
                showHelp();
                doExit(0);
            }
        }

        // ----------------------------
        // show System info
        if (!noinfo)
            showSystemInfo();

    }

    /** Class which encapsulates the TopSecurityManager and rewrite method getClassContext
    * to be accessable from this package.
    */
    private static class SecurityManagerEncapsulation extends SecurityManager {
        protected Class[] getClassContext() {
            return super.getClassContext();
        }
    }


    /**
    * @exception SecurityException if it is called multiple times
    */
    public static void main(String[] args) throws SecurityException {
        //    if (WindowManagerImpl.mainWindow () != null)
        //      throw new SecurityException ();

        // because of KL Group components, we define a property netbeans.design.time
        // which serves instead of Beans.isDesignTime () (which returns false in the IDE)
        System.getProperties ().put ("netbeans.design.time", "true"); // NOI18N

        // Initialize beans - [PENDING - better place for this ?]
        //                    [PENDING - can PropertyEditorManager garbage collect ?]
        java.beans.Introspector.setBeanInfoSearchPath (new String[] {
                    "org.netbeans.beaninfo", // NOI18N
                    "org.netbeans.beaninfo.awt", // NOI18N
                    "org.netbeans.beaninfo.swing", // NOI18N
                    "javax.swing.beaninfo", // NOI18N
                    "sun.beans.infos" // NOI18N
                });
        java.beans.PropertyEditorManager.setEditorSearchPath (
            new String[] { "org.openide.explorer.propertysheet.editors", "org.netbeans.beaninfo.editors", "sun.beans.editors" }); // NOI18N
        java.beans.PropertyEditorManager.registerEditor (String[].class, org.openide.explorer.propertysheet.editors.StringArrayEditor.class);
        java.beans.PropertyEditorManager.registerEditor (org.openide.src.MethodParameter[].class, org.openide.explorer.propertysheet.editors.MethodParameterArrayEditor.class);
        java.beans.PropertyEditorManager.registerEditor (org.openide.src.Identifier[].class, org.openide.explorer.propertysheet.editors.IdentifierArrayEditor.class);


        /* DEBUG
            String[] pes = java.beans.PropertyEditorManager.getEditorSearchPath ();
            System.out.println("Property Editor Search Path: "+pes.length);
            for (int i = 0; i < pes.length; i++) {
              System.out.println("["+i+"] "+ pes[i]);
            }
        */

        // -----------------------------------------------------------------------------------------------------
        // 1. Initialization and checking of netbeans.home and netbeans.user directories

        homeDir = System.getProperty("netbeans.home");
        if (homeDir == null) {
            System.out.println(getString("CTL_Netbeanshome_property"));
            doExit (1);
        }

        if (System.getProperty ("netbeans.user") == null)
            System.getProperties ().put ("netbeans.user", homeDir); // NOI18N

        userDir = System.getProperty ("netbeans.user");

        File homeDirFile = new File (homeDir);
        File userDirFile = new File (userDir);
        if (!homeDirFile.exists ()) {
            System.out.println (getString("CTL_Netbeanshome_notexists"));
            doExit (2);
        }
        if (!homeDirFile.isDirectory ()) {
            System.out.println (getString("CTL_Netbeanshome1"));
            doExit (3);
        }
        if (!userDirFile.exists ()) {
            System.out.println (getString("CTL_Netbeanshome2"));
            doExit (4);
        }
        if (!userDirFile.isDirectory ()) {
            System.out.println (getString("CTL_Netbeanshome3"));
            doExit (5);
        }

        systemDir = userDir + File.separator + "system"; // NOI18N

        File systemDirFile = new File (systemDir);
        if (systemDirFile.exists ()) {
            if (!systemDirFile.isDirectory ()) {
                Object[] arg = new Object[] {userDir};
                System.out.println (new MessageFormat(getString("CTL_CannotCreate_text")).format(arg));
                doExit (6);
            }
        } else {
            // try to create it
            if (!systemDirFile.mkdir ()) {
                Object[] arg = new Object[] {userDir};
                System.out.println (new MessageFormat(getString("CTL_CannotCreateSysDir_text")).format(arg));
                doExit (7);
            }
        }

        // -----------------------------------------------------------------------------------------------------
        // 2. Parse command-line arguments

        // Set up module-versioning properties, which logger prints.
        // IMPORTANT: must use an unlocalized resource here.
        java.util.Properties versions = new java.util.Properties ();
        try {
            versions.load (Main.class.getClassLoader ().getResourceAsStream ("org/netbeans/core/Versioning.properties")); // NOI18N
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace ();
        }
        System.setProperty ("org.openide.specification.version", versions.getProperty ("VERS_Specification_Version")); // NOI18N
        System.setProperty ("org.openide.version", versions.getProperty ("VERS_Implementation_Version")); // NOI18N
        System.setProperty ("org.openide.major.version", versions.getProperty ("VERS_Name")); // NOI18N
        // For TopLogging and MainWindow only:
        System.setProperty ("netbeans.buildnumber", versions.getProperty ("VERS_Build_Number")); // NOI18N

        // parses the command line and changes the settings appropriately
        parseCommandLine(args);

        // -----------------------------------------------------------------------------------------------------
        // 3. Initialization of L&F related things

        // if on window platform, set the WindowsLookAndFeel as the default
        // [IAN] Commented out accorning to HIIE request
        /*    if (Utilities.isWindows ())  {
              try {
                uiClass = Class.forName (UIManager.getSystemLookAndFeelClassName ());
              } catch (ClassNotFoundException e) {
                uiClass = null;
              }
            } else {
              uiClass = null;
            }
        */
        if (uiClass != null) {
            try {
                LookAndFeel customUI = (javax.swing.LookAndFeel)uiClass.newInstance();
                UIManager.setLookAndFeel(customUI);
            } catch (Exception e) {
                System.out.println(getString("ERR_UIError"));
            }
        }

        // Swing static initializations
        //    ToolTipManager.sharedInstance ().setLightWeightPopupEnabled (false); // Deprecated code, no replacement available for now?

        // Modify default font size to the font size passed as a command-line parameter
        java.awt.Font nbDialogPlain = new java.awt.Font ("Dialog", java.awt.Font.PLAIN, uiFontSize); // NOI18N
        java.awt.Font nbSerifPlain = new java.awt.Font ("Serif", java.awt.Font.PLAIN, uiFontSize); // NOI18N
        java.awt.Font nbSansSerifPlain = new java.awt.Font ("SansSerif", java.awt.Font.PLAIN, uiFontSize); // NOI18N
        java.awt.Font nbMonospacedPlain = new java.awt.Font ("Monospaced", java.awt.Font.PLAIN, uiFontSize); // NOI18N
        UIManager.getDefaults ().put ("Button.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("ToggleButton.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("RadioButton.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("CheckBox.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("ColorChooser.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("ComboBox.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("Label.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("List.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("MenuBar.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("MenuItem.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("RadioButtonMenuItem.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("CheckBoxMenuItem.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("Menu.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("PopupMenu.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("OptionPane.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("Panel.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("ProgressBar.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("ScrollPane.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("Viewport.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("TabbedPane.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("Table.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("TableHeader.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("TextField.font", nbSansSerifPlain); // NOI18N
        UIManager.getDefaults ().put ("PasswordField.font", nbMonospacedPlain); // NOI18N
        UIManager.getDefaults ().put ("TextArea.font", nbMonospacedPlain); // NOI18N
        UIManager.getDefaults ().put ("TextPane.font", nbSerifPlain); // NOI18N
        UIManager.getDefaults ().put ("EditorPane.font", nbSerifPlain); // NOI18N
        UIManager.getDefaults ().put ("TitledBorder.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("ToolBar.font", nbDialogPlain); // NOI18N
        UIManager.getDefaults ().put ("ToolTip.font", nbSansSerifPlain); // NOI18N
        UIManager.getDefaults ().put ("Tree.font", nbDialogPlain); // NOI18N

        // -----------------------------------------------------------------------------------------------------
        // 4. Display Splash Screen & manager

        // Set the default TopManager
        TopManager manager = new Main ();
        TopManager.setDefault(manager);

        // default actions for nodes
        NodeOp.setDefaultActions(new SystemAction[] {
                                     SystemAction.get (ToolsAction.class),
                                     SystemAction.get (PropertiesAction.class)
                                 });


        // show the welcome screen
        if (!noSplash) {
            splash = Splash.showSplash ();
        }

        // -----------------------------------------------------------------------------------------------------
        // 5. Start logging

        // do logging
        if (!noLogging) {
            try {
                logger = new TopLogging(systemDir);
            } catch (IOException e) {
                //       System.err.println("Cannot create log file.  Logging disabled."); // [PENDING]
                //        e.printStackTrace();
            }
        }

        // -----------------------------------------------------------------------------------------------------
        // 6. Initialize SecurityManager and ClassLoader

        manager.setStatusText (getString("MSG_IDEInit"));

        final SecurityManagerEncapsulation secMan = new SecurityManagerEncapsulation();

        NbBundle.setClassLoaderFinder(new NbBundle.ClassLoaderFinder() {
                                          public ClassLoader find() {
                                              Class[] classes = secMan.getClassContext();
                                              return classes[Math.min(4, classes.length - 1)].getClassLoader();
                                          }
                                      });



        // -----------------------------------------------------------------------------------------------------
        // 7. Initialize FileSystems

        Repository fsp = null;

        manager.setStatusText (getString("MSG_FSInit"));
        // system FS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        {
            Exception exc = null;
            try {
                fsp = SystemFileSystem.createRepository (userDir, homeDir);
                defaultRepository = fsp;
            } catch (IOException ex) {
                exc = ex;
            } catch (java.beans.PropertyVetoException ex) {
                exc = ex;
            }

            if (exc != null) {
                Object[] arg = new Object[] {systemDir};
                System.out.println (new MessageFormat(getString("CTL_Cannot_mount_systemfs")).format(arg));
                doExit (3);
            }
        }


        // -----------------------------------------------------------------------------------------------------
        // 8. Advance Policy

        java.security.Policy.getPolicy().getPermissions(new java.security.CodeSource(null, null)).implies(new java.security.AllPermission());

        // set security manager
        System.setSecurityManager(new org.netbeans.core.execution.TopSecurityManager());

        // install java.net.Authenticator
        java.net.Authenticator.setDefault (new NbAuthenticator ());

        // -----------------------------------------------------------------------------------------------------
        // 9. Modules

        // versions set in step 2 for logger
        ModuleInstaller.initialize ();


        // -----------------------------------------------------------------------------------------------------
        // 10. Initialization of project (because it can change loader pool and it influences main window menu)

        try {
            NbProjectOperation.openOrCreateProject ();
        } catch (IOException e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
        }

        LoaderPoolNode.installationFinished ();

        // -----------------------------------------------------------------------------------------------------
        // 11. Initialization of main window

        manager.setStatusText (getString("MSG_MainWindowInit"));
        MainWindow.initialize ();

        // force to initialize timer
        // sometimes happened that the timer thread was initialized under
        // a TaskThreadGroup
        // such task never ends or, if killed, timer is over
        Timer timerInit = new Timer(0, new java.awt.event.ActionListener() {
                                        public void actionPerformed(java.awt.event.ActionEvent ev) { }
                                    });
        timerInit.setRepeats(false);
        timerInit.start();

        // start files from start folder
        startFolder (getDefault ().getPlaces ().folders ().startup ());

        // prepare workspaces
        manager.setStatusText (getString ("MSG_DefaultWorkspaceInit"));

        TopManager.getDefault().getWindowManager();

        // -----------------------------------------------------------------------------------------------------
        // 12. Initialization of project
        /*
            try {
              NbProjectOperation.openOrCreateProject ();
            } catch (IOException e) {
              if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
            }
        */
        // -----------------------------------------------------------------------------------------------------
        // 13. Initialize Shortcuts

        ShortcutsEditor.installCurrentBindings ();

        // -----------------------------------------------------------------------------------------------------
        // 14. Open main window

        manager.setStatusText (getString("MSG_WindowShowInit"));
        MainWindow.showWindow ();

        // finish starting
        if (splash != null) {
            Splash.hideSplash ();
            splash = null;
        }

        // -----------------------------------------------------------------------------------------------------
        // 15. Install new modules

        ModuleInstaller.autoLoadModules ();



        // -----------------------------------------------------------------------------------------------------
        // 16. Show TipsOfTheDay

        if (new IDESettings().getShowTipsOnStartup() && (System.getProperty ("netbeans.full.hack") == null) && (System.getProperty ("netbeans.close") == null)) {
            showTipsOfTheDay ();
        }

        // check the web for new modules
        UpdateSupport.performUpdateCheck ();

        // finish starting
        if (System.getProperty ("netbeans.kill") != null) {
            doExit (5);
        }

        // close IDE
        if (System.getProperty ("netbeans.close") != null) {
            org.openide.TopManager.getDefault().exit();
        }

    }

    /** Getter for a text from resource.
    * @param resName resource name
    * @return string with resource
    */
    static String getString (String resName) {
        return NbBundle.getBundle (Main.class).getString (resName);
    }

    /** Getter for a text from resource with one argument.
    * @param resName resource name
    * @return string with resource
    * @param arg the argument
    */
    static String getString (String resName, Object arg) {
        MessageFormat mf = new MessageFormat (getString (resName));
        return mf.format (new Object[] { arg });
    }

    /** Getter for a text from resource with one argument.
    * @param resName resource name
    * @return string with resource
    * @param arg1 the argument
    * @param arg2 the argument
    */
    static String getString (String resName, Object arg1, Object arg2) {
        MessageFormat mf = new MessageFormat (getString (resName));
        return mf.format (new Object[] { arg1, arg2 });
    }

    /** Exits from the VM.
    */
    static void doExit (int code) {
        Runtime.getRuntime ().exit (code);
    }


    /** Starts a folder by executing all of its executable children
    * @param f the folder
    */
    private static void startFolder (DataFolder f) {
        DataObject[] obj = f.getChildren ();
        org.openide.actions.ExecuteAction.execute(obj, true);
    }

}


/*
* Log
*/
