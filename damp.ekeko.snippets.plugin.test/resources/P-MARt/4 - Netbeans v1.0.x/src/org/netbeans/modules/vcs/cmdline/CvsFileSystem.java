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

package org.netbeans.modules.vcs.cmdline;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.beans.*;
import java.text.*;
import javax.swing.*;

import org.openide.util.actions.*;
import org.openide.util.NbBundle;
import org.openide.*;
import org.netbeans.modules.vcs.*;
import org.netbeans.modules.vcs.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.Status;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.AbstractFileSystem;
import org.openide.filesystems.DefaultAttributes;

/** CVS filesystem.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class CvsFileSystem extends VcsFileSystem implements java.beans.PropertyChangeListener {
    private Debug D = new Debug ("CvsFileSystem", true); // NOI18N

    public VcsFactory getVcsFactory () {
        return new CvsFactory ();
    }

    public final static String CVS_SERVER_LOCAL = "local"; // NOI18N
    public final static String CVS_SERVER_SERVER = "server"; // NOI18N
    public final static String CVS_SERVER_PSERVER = "pserver"; // NOI18N
    // suppose that CVS server only exists for UNIX/Linux.
    // The same presumption is made in CvsAction.doCommand () method
    final static String PS_SERVER = "/"; // NOI18N
    final static String PS_LOCAL = java.io.File.separator;

    private static String last_cvsServerType = CVS_SERVER_LOCAL;
    private static String last_cvsRoot = System.getProperty("user.home"); // NOI18N
    private static String last_cvsServer = ""; // NOI18N
    private static String last_cvsUser = System.getProperty ("user.name");
    private static String last_cvsPassword = ""; // NOI18N
    private static String last_cvsModule = ""; // NOI18N
    private static String last_cvsModuleName = ""; // NOI18N
    private static String last_cvsExePath = null; // NOI18N
    private static String last_cygwinPath = null; // NOI18N
    //private static boolean last_useUnixShell = false;

    private String cvsServerType = new String(last_cvsServerType);
    private String cvsRoot = new String(last_cvsRoot);
    private String cvsServer = new String(last_cvsServer);
    private String cvsUser = new String(last_cvsUser);
    private String cvsPassword = new String(last_cvsPassword);
    private String cvsModule = new String(last_cvsModule);
    private String cvsModuleName = new String(last_cvsModuleName);
    private String cvsExePath = (last_cvsExePath == null) ? null : new String(last_cvsExePath);
    private String cygwinPath = (last_cygwinPath == null) ? null : new String(last_cygwinPath);
    //private boolean useUnixShell = last_useUnixShell;

    public String getCvsRoot () { return cvsRoot;}
    public String getCvsUserName () { return cvsUser; }
    public String getCvsServer () { return cvsServer; }
    public String getCvsServerType () { return cvsServerType; }
    public String getCvsPassword () { return cvsPassword; }
    public String getCvsModule () { return cvsModule; }
    public String getCvsModuleName () { return cvsModuleName; }
    public String getCvsExePath () { return cvsExePath; }
    public String getCygwinPath () { return cygwinPath; }
    //public boolean isUseUnixShell () { return useUnixShell; }

    /**
     * The name of the directory with CVS info.
     */
    private static final String CVS_DIRNAME = "CVS";
    /**
     * The names of files in CVS directory.
     */
    private static final String[] CVS_DIRCONTENT = {"Entries", "Repository", "Root"};
    /**
     * Whether the information dialog about need of checkout is invoked.
     */
    private transient boolean needToCheckoutInvoked = false;

    String getCvcRootForServerType () {
        return cvsServerType.equals (CVS_SERVER_LOCAL) ? cvsRoot : cvsUser+"@"+cvsServer+":"+cvsRoot; // NOI18N
    }

    public void setCvsRoot (String root) {
        cvsRoot = root;
        last_cvsRoot = new String(root);
        VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("CVS_REPOSITORY"); // NOI18N
        var.setValue (root);
        var = (VcsConfigVariable) variablesByName.get ("CVSROOT"); // NOI18N
        String result = getCvcRootForServerType ();
        var.setValue (result);
        D.deb (result);
    }

    public void setCvsServer (String server) {
        cvsServer = server;
        last_cvsServer = new String(server);
        setCvsRoot (cvsRoot);
    }

    protected void setCvsServerType (String serverType) {
        D.deb ("serverType ("+serverType+")"); // NOI18N
        cvsServerType = serverType;
        last_cvsServerType = new String(serverType);
        VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("SERVERTYPE"); // NOI18N
        var.setValue (serverType);
        //var = (VcsConfigVariable) variablesByName.get ("PS"); // NOI18N
        //ResourceBundle bu = NbBundle.getBundle ("org.netbeans.modules.vcs.cmdline.CommandLines"); // NOI18N
        /*
        if(serverType==CVS_SERVER_LOCAL) {
          //var.setValue (PS_LOCAL);
          getCommand ("LIST").setDataRegex (bu.getString ("REGEX_LIST"));
    } else {
          //var.setValue (PS_SERVER);
          getCommand ("LIST").setDataRegex (bu.getString ("REGEX_LIST_SRV"));
    }
        */
        setCvsModule (cvsModule);
        //setCvsRoot (cvsRoot);
    }

    void setCvsUserName (String user) {
        cvsUser = user;
        last_cvsUser = new String(user);
        setCvsRoot (cvsRoot);
    }

    void setCvsPassword (String password) {
        cvsPassword = password;
    }

    public void setCvsModule (String module) {
        cvsModule = module;
        last_cvsModule = new String(module);
        VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("MODULE_S"); // NOI18N
        //if (module.length() > 0) module += "/"; // NOI18N
        var.setValue (module);
        D.deb("MODULE_S = "+var.getValue()); // NOI18N
        String osName = System.getProperty("os.name");
        module = module.replace('/', File.separatorChar);
        var = (VcsConfigVariable) variablesByName.get ("MODULE"); // NOI18N
        var.setValue (module);
        D.deb("MODULE = "+var.getValue()); // NOI18N
    }

    public void setCvsModuleName (String moduleName) {
        cvsModuleName = moduleName;
        last_cvsModuleName = new String(moduleName);
        VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("MODULE_NAME"); // NOI18N
        var.setValue (moduleName);
        D.deb("MODULE_NAME = "+moduleName);
    }

    void setCvsExePath (String exePath) {
        if (isUseUnixShell()) cvsExePath = exePath.replace('\\', '/');
        else cvsExePath = exePath;
        D.deb("cvsExePath = "+cvsExePath);
        last_cvsExePath = new String(cvsExePath);
        VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("CVS_EXE"); // NOI18N
        var.setValue (cvsExePath);
    }

    protected void setUseUnixShell (boolean unixShell) {
        //useUnixShell = unixShell;
        //last_useUnixShell = unixShell;
        super.setUseUnixShell(unixShell);
        setCvsExePath(((VcsConfigVariable) variablesByName.get ("CVS_EXE")).getValue());
        initCommands ();
    }

    void setCygwinPath (String path) {
        cygwinPath = path;
        last_cygwinPath = new String(path);
        VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("SHELL"); // NOI18N
        var.setValue (path);
        int index = path.lastIndexOf(File.separatorChar);
        D.deb("path = "+path); // NOI18N
        String bin = path.substring(0, index);
        D.deb("index = "+index+", bin = "+bin); // NOI18N
        bin = bin.replace(File.separatorChar, '/');
        var = (VcsConfigVariable) variablesByName.get ("CYGWINBIN"); // NOI18N
        var.setValue (bin);
    }

    //-------------------------------------------
    static final long serialVersionUID =3105697696081480308L;
    public CvsFileSystem() {
        super ();
        D.deb ("CvsFileSystem()"); // NOI18N
        setConfiguration ();
        setCvsServerType (last_cvsServerType);
        addPropertyChangeListener(this);
        needToCheckoutInvoked = false;
        D.deb("constructor done.");
    }

    private void setConfiguration () {
        //System.out.println("CvsFileSystem   readConfiguration ()"); // NOI18N
        ResourceBundle bu = NbBundle.getBundle ("org.netbeans.modules.vcs.cmdline.CommandLines"); // NOI18N
        Vector vars = new Vector ();
        String osName;
        String os = System.getProperty("os.name");
        if(os.indexOf("Win") >= 0) { // NOI18N
            osName = "_WIN"; // NOI18N
        } else osName = "_UNIX"; // NOI18N
        D.deb("OS = "+os+" => osName = "+osName); // NOI18N

        vars.add (new VcsConfigVariable ("MODULE", "", "", false, false, false, "")); // NOI18N
        vars.add (new VcsConfigVariable ("MODULE_S", "", "", false, false, false, "")); // NOI18N
        vars.add (new VcsConfigVariable ("MODULE_NAME", "", "", false, false, false, bu.getString ("VAR_MODULE_SELECT"+osName))); // NOI18N
        //vars.add (new VcsConfigVariable ("CP", "", bu.getString ("VAR_CP"+osName), false, false, false));
        vars.add (new VcsConfigVariable ("PS", "", ""+java.io.File.separator, false, false, false, "")); // NOI18N
        //vars.add (new VcsConfigVariable ("CPS", "", "${classpath.separator}", false, false, false)); // NOI18N
        vars.add (new VcsConfigVariable ("CVSROOT", "", "", false, false, false, "")); // NOI18N
        vars.add (new VcsConfigVariable ("CVS_REPOSITORY", "", "", false, false, false, "")); // NOI18N
        vars.add (new VcsConfigVariable ("NUR", "", "\"", false, false, false, "")); // NOI18N

        D.deb("OK 1"); // NOI18N
        vars.add (new VcsConfigVariable ("MODPATH", "", bu.getString ("VAR_MODPATH"+osName), false, false, false, ""));
        D.deb("OK 2"); // NOI18N
        vars.add (new VcsConfigVariable ("RUN", "", bu.getString ("VAR_RUN"+osName), false, false, false, ""));
        D.deb("OK 3"); // NOI18N
        vars.add (new VcsConfigVariable ("RUNCDM", "", bu.getString ("VAR_RUNCDM"+osName), false, false, false, ""));
        D.deb("OK 4"); // NOI18N
        vars.add (new VcsConfigVariable ("WORKDIR", "", bu.getString ("VAR_WORKDIR"+osName), false, false, false, ""));
        vars.add (new VcsConfigVariable ("SERVERTYPE", "", "local", false, false, false, "")); // NOI18N
        vars.add (new VcsConfigVariable ("SHOWLOCALFILES", "", "true", false, false, false, "")); // NOI18N
        vars.add (new VcsConfigVariable ("WRAPPER", "", "org.netbeans.modules.vcs.cmdline.CvsList", false, false, false, "")); // NOI18N
        vars.add (new VcsConfigVariable ("CHECKOUT_CMD", "", bu.getString ("VAR_CHECKOUT_CMD"+osName), false, false, false, ""));
        vars.add (new VcsConfigVariable ("DIFF_CMD", "", bu.getString ("VAR_DIFF_CMD"+osName), false, false, false, ""));
        vars.add (new VcsConfigVariable ("LOG_INFO_CMD", "", bu.getString ("VAR_LOG_INFO_CMD"+osName), false, false, false, ""));
        vars.add (new VcsConfigVariable ("ADD_TAG_CMD", "", bu.getString ("VAR_ADD_TAG_CMD"+osName), false, false, false, ""));
        if (last_cvsExePath != null) {
            vars.add (new VcsConfigVariable ("CVS_EXE", "", last_cvsExePath, false, false, false, ""));
            cvsExePath = new String(last_cvsExePath);
        } else {
            vars.add (new VcsConfigVariable ("CVS_EXE", "", bu.getString ("VAR_CVS_EXE"), false, false, false, ""));
            cvsExePath = bu.getString ("VAR_CVS_EXE");
        }
        if (last_cygwinPath != null) {
            vars.add (new VcsConfigVariable ("SHELL", "", last_cygwinPath, false, false, false, ""));
            cygwinPath = new String(last_cygwinPath);
        } else {
            vars.add (new VcsConfigVariable ("SHELL", "", bu.getString ("VAR_SHELL"), false, false, false, ""));
            cygwinPath = bu.getString ("VAR_SHELL");
        }
        vars.add (new VcsConfigVariable ("CYGWINBIN", "", bu.getString ("VAR_CYGWINBIN"), false, false, false, ""));
        D.deb("OK 5"); // NOI18N
        super.setVariables (vars);
        if (last_cygwinPath != null) {
            setCygwinPath(cygwinPath);
        }
        //D.deb("FileSeperator = "+java.io.File.separator); // NOI18N
        //D.deb("PS = "+ ((VcsConfigVariable) variablesByName.get("PS")).getValue()); // NOI18N

        D.deb("Calling initCommands()"); // NOI18N
        initCommands();
    }

    private void initCommands () {
        String osName;
        String os = System.getProperty("os.name");
        if(os.indexOf("Win") >= 0) { // NOI18N
            if (isUseUnixShell()) osName = "_CYGWIN"; // NOI18N
            else osName = "_WIN"; // NOI18N
        } else osName = "_UNIX"; // NOI18N
        D.deb("OS = "+os+" => osName = "+osName); // NOI18N

        ResourceBundle bu = NbBundle.getBundle ("org.netbeans.modules.vcs.cmdline.CommandLines"); // NOI18N
        //VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("CP"); // NOI18N
        //var.setValue (bu.getString ("VAR_CP"+osName));
        VcsConfigVariable var = (VcsConfigVariable) variablesByName.get ("MODPATH"); // NOI18N
        D.deb("var = "+var);
        var.setValue (bu.getString ("VAR_MODPATH"+osName));
        var = (VcsConfigVariable) variablesByName.get ("RUN"); // NOI18N
        var.setValue (bu.getString ("VAR_RUN"+osName));
        var = (VcsConfigVariable) variablesByName.get ("RUNCDM"); // NOI18N
        var.setValue (bu.getString ("VAR_RUNCDM"+osName));
        var = (VcsConfigVariable) variablesByName.get ("WORKDIR"); // NOI18N
        var.setValue (bu.getString ("VAR_WORKDIR"+osName));

        var = (VcsConfigVariable) variablesByName.get ("CHECKOUT_CMD"); // NOI18N
        var.setValue (bu.getString ("VAR_CHECKOUT_CMD"+osName));
        var = (VcsConfigVariable) variablesByName.get ("DIFF_CMD"); // NOI18N
        var.setValue (bu.getString ("VAR_DIFF_CMD"+osName));
        var = (VcsConfigVariable) variablesByName.get ("LOG_INFO_CMD"); // NOI18N
        var.setValue (bu.getString ("VAR_LOG_INFO_CMD"+osName));
        var = (VcsConfigVariable) variablesByName.get ("ADD_TAG_CMD"); // NOI18N
        var.setValue (bu.getString ("VAR_ADD_TAG_CMD"+osName));

        var = (VcsConfigVariable) variablesByName.get ("PS"); // NOI18N
        if (isUseUnixShell())
            var.setValue ("/"); // NOI18N
        else
            var.setValue (java.io.File.separator);
        var = (VcsConfigVariable) variablesByName.get ("CD"); // NOI18N
        if (isUseUnixShell())
            var.setValue ("cd"); // NOI18N
        else {
            if (osName.equals("_WIN")) // NOI18N
                var.setValue ("cd /D"); // NOI18N
            else
                var.setValue ("cd"); // NOI18N
        }

        UserCommand cmd;
        Vector commands = new Vector ();
        String moduleName = " $[? MODULE_NAME] [${MODULE_NAME}] [.]"; // NOI18N

        D.deb("UserCommand.");
        cmd= new UserCommand ();
        D.deb("cmd = "+cmd);
        cmd.setName ("LIST"); // NOI18N
        D.deb("cmd = "+cmd);
        try {
            D.deb("refresh label = "+g("CMD_Refresh"));
        } catch(Exception e) {
            D.deb("Exception: "+e);
            e.printStackTrace();
        }
        cmd.setLabel (g("CMD_Refresh")); // NOI18N
        D.deb("cmd = "+cmd);
        cmd.setExec (bu.getString ("EXEC_LIST"+osName));
        D.deb("cmd = "+cmd);
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (300000);
        cmd.setDataRegex (bu.getString ("REGEX_LIST"));
        cmd.setErrorRegex ("(Error.*)"); // NOI18N
        cmd.setStatus (1);
        cmd.setLocker (-1);
        cmd.setAttr (-1);
        cmd.setDate (-1);
        cmd.setTime (-1);
        cmd.setSize (-1);
        cmd.setFileName (0);
        commands.add (cmd);

        D.deb("cmd 1 = "+cmd);

        cmd= new UserCommand ();
        cmd.setName ("LIST_SUB"); // NOI18N
        cmd.setLabel (g("CMD_RefreshRecursively")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_LIST_SUB"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (600000);
        cmd.setDataRegex (bu.getString ("REGEX_LIST_SUB"));
        cmd.setErrorRegex ("(Error.*)"); // NOI18N
        cmd.setStatus (1);
        cmd.setLocker (-1);
        cmd.setAttr (-1);
        cmd.setDate (-1);
        cmd.setTime (-1);
        cmd.setSize (-1);
        cmd.setFileName (0);
        commands.add (cmd);

        D.deb("cmd 2 = "+cmd);

        cmd= new UserCommand ();
        cmd.setName ("CHECKOUT"); // NOI18N
        cmd.setLabel (g("CMD_CheckOut")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_CHECKOUT"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("CHECKOUT_MODULE"); // NOI18N
        cmd.setLabel (g("CMD_CheckOutModule")+moduleName); // NOI18N
        cmd.setExec (bu.getString ("EXEC_CHECKOUT_MODULE"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("LOGIN"); // NOI18N
        cmd.setLabel (g("CMD_Login")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_LOGIN"));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("INIT"); // NOI18N
        cmd.setLabel (g("CMD_Init")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_INIT"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (60000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        commands.add (cmd);

        D.deb("cmd 6 = "+cmd);

        cmd= new UserCommand ();
        cmd.setName ("UPDATE"); // NOI18N
        cmd.setLabel (g("CMD_Update")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_UPDATE"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (60000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        /*
        cmd= new UserCommand ();
        cmd.setName ("UPDATE_MODULE"); // NOI18N
        cmd.setLabel (g("CMD_UpdateModule")+moduleName); // NOI18N
        cmd.setExec (bu.getString ("EXEC_UPDATE_MODULE"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (60000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);
        */

        cmd= new UserCommand ();
        cmd.setName ("COMMIT"); // NOI18N
        cmd.setLabel (g("CMD_Commit")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_COMMIT"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        /*
        cmd= new UserCommand ();
        cmd.setName ("COMMIT_MODULE"); // NOI18N
        cmd.setLabel (g("CMD_CommitModule")+moduleName); // NOI18N
        cmd.setExec (bu.getString ("EXEC_COMMIT_MODULE"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);
        */

        cmd= new UserCommand ();
        cmd.setName ("REMOVE"); // NOI18N
        cmd.setLabel (g("CMD_Remove")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_REMOVE"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        //cmd.setConfirmationMsg(bu.getString("VAR_REMOVE_MSG"));
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("LOCK"); // NOI18N
        cmd.setLabel (g("CMD_Lock")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_LOCK"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (false);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("UNLOCK1"); // NOI18N
        cmd.setLabel (g("CMD_Unlock")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_UNLOCK"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (false);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("EDIT"); // NOI18N
        cmd.setLabel (g("CMD_Edit")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_EDIT"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (false);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("UNEDIT"); // NOI18N
        cmd.setLabel (g("CMD_Unedit")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_UNEDIT"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (false);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("EDITORS"); // NOI18N
        cmd.setLabel (g("CMD_Editors")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_EDITORS"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("WATCH_ON"); // NOI18N
        cmd.setLabel (g("CMD_WatchOn")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_WATCH_ON"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (false);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("WATCH_OFF"); // NOI18N
        cmd.setLabel (g("CMD_WatchOff")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_WATCH_OFF"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (false);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("WATCH_ADD"); // NOI18N
        cmd.setLabel (g("CMD_WatchSet")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_WATCH_ADD"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (false);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("WATCHERS"); // NOI18N
        cmd.setLabel (g("CMD_Watchers")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_WATCHERS"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("STATUS"); // NOI18N
        cmd.setLabel (g("CMD_Status")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_STATUS"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("LOG"); // NOI18N
        cmd.setLabel (g("CMD_Log")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_LOG"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("ADD"); // NOI18N
        cmd.setLabel (g("CMD_Add")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_ADD"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("IMPORT"); // NOI18N
        cmd.setLabel (g("CMD_Import")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_IMPORT"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("IMPORT_MODULE"); // NOI18N
        cmd.setLabel (g("CMD_ImportModule")+moduleName); // NOI18N
        cmd.setExec (bu.getString ("EXEC_IMPORT_MODULE"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("CHECKOUT_REV"); // NOI18N
        cmd.setLabel (g("CMD_CheckOutRevision")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_CHECKOUT_REV"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("CHECKOUT_REV_DIR"); // NOI18N
        cmd.setLabel (g("CMD_CheckOutRevision")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_CHECKOUT_REV_DIR"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDisplayOutput (true);
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("UPDATE_REV"); // NOI18N
        cmd.setLabel (g("CMD_UpdateRevision")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_UPDATE_REV"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("UPDATE_REV_DIR"); // NOI18N
        cmd.setLabel (g("CMD_UpdateRevision")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_UPDATE_REV_DIR"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("COMMIT_REV"); // NOI18N
        cmd.setLabel (g("CMD_CommitToBranch")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_COMMIT_REV"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("COMMIT_REV_DIR"); // NOI18N
        cmd.setLabel (g("CMD_CommitToBranch")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_COMMIT_REV_DIR"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("MERGE"); // NOI18N
        cmd.setLabel (g("CMD_MergeWithBranch")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_MERGE"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        cmd.setDoRefresh(true);
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("REM_STICKY"); // NOI18N
        cmd.setLabel (g("CMD_RemoveStickyTag")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_REM_STICKY"+osName));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (90000);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        commands.add (cmd);


        cmd= new UserCommand ();
        cmd.setName ("DIFF"); // NOI18N
        cmd.setLabel (g("CMD_Diff")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_DIFF"));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(^[0-9]+(,[0-9]+|)[d][0-9]+$)|(^[0-9]+(,[0-9]+|)[c][0-9]+(,[0-9]+|)$)|(^[0-9]+[a][0-9]+(,[0-9]+|)$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("BRANCHES"); // NOI18N
        cmd.setLabel (g("CMD_ViewBranches")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_BRANCHES"));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        commands.add (cmd);

        cmd= new UserCommand ();
        cmd.setName ("TAGS"); // NOI18N
        cmd.setLabel (g("CMD_AddTag")); // NOI18N
        cmd.setExec (bu.getString ("EXEC_TAGS"));
        cmd.setInput (""); // NOI18N
        cmd.setTimeout (0);
        cmd.setDataRegex ("(.*$)"); // NOI18N
        cmd.setErrorRegex ("(.*$)"); // NOI18N
        commands.add (cmd);

        D.deb("cmd last = "+cmd);

        setCommands (commands);
        D.deb("initCommands done.");
    }

    public Hashtable getVariablesByName() {
        return variablesByName;
    }

    public FilenameFilter getLocalFileFilter() {
        return new FilenameFilter() {
                   public boolean accept(File dir, String name) {
                       return !name.equalsIgnoreCase("CVS"); // NOI18N
                   }
               };
    }

    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() != FileSystem.PROP_VALID) return;
        if (isValid()) {
            D.deb("Filesystem added to the repository, setting refresh time to "+refreshTimeToSet); // NOI18N
            setRefreshTime(refreshTimeToSet);
            D.deb("calling WARN");
            warnDirectoriesDoNotExists();
            D.deb("calling WARN finished.");
        } else {
            D.deb("Filesystem is not valid any more, setting refresh time to 0"); // NOI18N
            setRefreshTime(0);
        }
    }

    /**
    * Test if the directory was checked out by CVS or not.
    * @param dir the directory name to test
    * @return <code>true</code> if the directory was created by CVS, <code>false</code> if not.
    */
    private boolean isCVSDirectory(File dir) {
        D.deb("TESTING CVS dir "+dir);
        File subdir = new File(dir, CVS_DIRNAME);
        if (!subdir.isDirectory()) return false;
        for(int i = 0; i < CVS_DIRCONTENT.length; i++) {
            File cvsFile = new File(subdir, CVS_DIRCONTENT[i]);
            if (!cvsFile.isFile()) return false;
        }
        return true;
    }

    private boolean isCVSRoot(File dir) {
        if (isCVSDirectory(dir)) return true;
        File[] subfiles = dir.listFiles();
        if (subfiles == null) return false;
        for(int i = 0; i < subfiles.length; i++) {
            if (subfiles[i].isDirectory() && isCVSDirectory(subfiles[i])) return true;
        }
        return false;
    }

    /* Scans children for given name
     */
    public String[] children (String name) {
        if (name.length() == 0) { // We're on the root
            if (!isCVSRoot(getRootDirectory())) {
                D.deb("I'm NOT in directory checked-out by CVS !!");
                if (!needToCheckoutInvoked) {
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                               public void run() {
                                                                   TopManager.getDefault ().notify (new NotifyDescriptor.Message (
                                                                                                        g("DLG_CVS_NeedToCheckOutFirst"), NotifyDescriptor.Message.INFORMATION_MESSAGE));
                                                                   needToCheckoutInvoked = false;
                                                               }
                                                           });
                    needToCheckoutInvoked = true;
                }
                if (cache.isDir(name)) {
                    String[] vcsFiles = null;
                    String[] files = null;
                    vcsFiles = cache.getLocalFilesAndSubdirs(name);
                    D.deb("vcsFiles="+MiscStuff.arrayToString(vcsFiles)); // NOI18N

                    String p = ""; // NOI18N
                    try{
                        p = getRootDirectory().getCanonicalPath();
                    }
                    catch (IOException e){
                        //E.err(e,"getCanonicalPath() failed"); // NOI18N
                        return new String[0]; // I failed, return nothing.
                    }
                    files = cache.dirsFirst(p+File.separator+name,vcsFiles);
                    D.deb("files="+MiscStuff.arrayToString(files)); // NOI18N
                    return files;
                } else {
                    return new String[0];
                }
            }
        }
        return super.children(name);
    }

    private void readObject(ObjectInputStream in) throws
        ClassNotFoundException, IOException, NotActiveException{
        in.defaultReadObject();
        initCommands();
    }


    //-------------------------------------------
    private void writeObject(ObjectOutputStream out) throws IOException {
        //D.deb("writeObject() - saving bean"); // NOI18N
        // cache is transient
        out.defaultWriteObject();
    }

    protected String g(String s) {
        D.deb("getting: "+s);
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.cmdline.BundleCVS").getString (s);
    }


}

/*
 * Log
 *  44   Gandalf-post-FCS1.42.2.0    3/23/00  Martin Entlicher The filesystem remembers
 *       the last data,  perform check whether the CVS info is presented in the 
 *       working directory,  new variables CVS_REPOSITORY and MODULE_NAME added,
 *        recursive refresh as one command added,  module commands and watches 
 *       commands added.
 *  43   Gandalf   1.42        2/11/00  Martin Entlicher 
 *  42   Gandalf   1.41        2/10/00  Martin Entlicher Warning of nonexistent 
 *       directories when mounted.
 *  41   Gandalf   1.40        2/10/00  Martin Entlicher Fix a problem in setting
 *       the PS variable.
 *  40   Gandalf   1.39        2/9/00   Martin Entlicher A small fix of variables
 *       CVS_EXE and PS.
 *  39   Gandalf   1.38        2/8/00   Martin Entlicher Fixed path to cvs.exe 
 *       when using CygWIN and command loading changed.
 *  38   Gandalf   1.37        1/17/00  Martin Entlicher 
 *  37   Gandalf   1.36        1/15/00  Ian Formanek    NOI18N
 *  36   Gandalf   1.35        1/6/00   Martin Entlicher 
 *  35   Gandalf   1.34        1/5/00   Martin Entlicher 
 *  34   Gandalf   1.33        12/29/99 Martin Entlicher Path to CygWIN bin 
 *       directory added and timeouts changed.  
 *  33   Gandalf   1.32        12/21/99 Martin Entlicher Set the refresh time 
 *       after mounting into the Repository, added support for Cygwin and new 
 *       variables.
 *  32   Gandalf   1.31        12/14/99 Martin Entlicher Timeouts changed
 *  31   Gandalf   1.30        12/8/99  Martin Entlicher Added MODPATH variable, 
 *       MODULE does not contain the last File.separator.
 *  30   Gandalf   1.29        12/2/99  Martin Entlicher 
 *  29   Gandalf   1.28        11/30/99 Martin Entlicher 
 *  28   Gandalf   1.27        11/27/99 Patrik Knakal   
 *  27   Gandalf   1.26        11/24/99 Martin Entlicher 
 *  26   Gandalf   1.25        11/23/99 Martin Entlicher Several new commands 
 *       added.
 *  25   Gandalf   1.24        11/10/99 Martin Entlicher Changed for better 
 *       listing of local files
 *  24   Gandalf   1.23        11/9/99  Martin Entlicher 
 *  23   Gandalf   1.22        11/4/99  Martin Entlicher 
 *  22   Gandalf   1.21        11/2/99  Martin Entlicher 
 *  21   Gandalf   1.20        11/2/99  Martin Entlicher 
 *  20   Gandalf   1.19        10/26/99 Martin Entlicher 
 *  19   Gandalf   1.18        10/26/99 Martin Entlicher 
 *  18   Gandalf   1.17        10/25/99 Pavel Buzek     
 *  17   Gandalf   1.16        10/25/99 Pavel Buzek     
 *  16   Gandalf   1.15        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        10/13/99 Martin Entlicher 
 *  14   Gandalf   1.13        10/13/99 Pavel Buzek     
 *  13   Gandalf   1.12        10/13/99 Pavel Buzek     commands added
 *  12   Gandalf   1.11        10/13/99 Martin Entlicher variable MODULE_S added
 *  11   Gandalf   1.10        10/12/99 Pavel Buzek     
 *  10   Gandalf   1.9         10/10/99 Pavel Buzek     
 *  9    Gandalf   1.8         10/9/99  Pavel Buzek     
 *  8    Gandalf   1.7         10/9/99  Pavel Buzek     
 *  7    Gandalf   1.6         10/8/99  Pavel Buzek     
 *  6    Gandalf   1.5         10/7/99  Martin Entlicher Added DIFF and UNIX 
 *       commands
 *  5    Gandalf   1.4         10/7/99  Pavel Buzek     
 *  4    Gandalf   1.3         10/7/99  Pavel Buzek     
 *  3    Gandalf   1.2         10/7/99  Pavel Buzek     
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
