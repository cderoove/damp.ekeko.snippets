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
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.text.*;

import org.openide.*;
import org.openide.awt.JMenuPlus;
import org.openide.util.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.cookies.*;


import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.*;

/**
 * Provides implementation of actions on VCS files and directories.
 * @author  Pavel Buzek
 */
public class CvsAction extends VcsAction implements ActionListener {

    private Debug E=new Debug("CvsAction", true); // NOI18N
    private Debug D=E;

    /**
     * @associates String 
     */
    private Hashtable additionalVars = new Hashtable();
    private boolean concurrentExecution = false;

    static final long serialVersionUID =-6595569895749032164L;
    /** Creates new VcsActionImpl */
    public CvsAction(CvsFileSystem fileSystem) {
        setFileSystem(fileSystem);
    }

    //-------------------------------------------
    public String getName(){
        return g("CTL_Version_Control"); // NOI18N
    }

    //-------------------------------------------
    public void performAction(Node[] nodes){
        //D.deb("performAction()"); // NOI18N
    }

    //-------------------------------------------
    public boolean enable(Node[] nodes){
        //D.deb("enable()"); // NOI18N
        return true;
    }

    //-------------------------------------------
    public HelpCtx getHelpCtx(){
        //D.deb("getHelpCtx()"); // NOI18N
        return null;
    }

    public void doLock (Vector files){
        UserCommand cmd=fileSystem.getCommand("LOCK"); // NOI18N
        if (cmd != null) doCommand(files, "LOCK"); // NOI18N
    }

    public void doUnlock (Vector files){
        UserCommand cmd=fileSystem.getCommand("UNLOCK"); // NOI18N
        if (cmd != null) doCommand(files, "UNLOCK"); // NOI18N
    }

    public void doEdit (Vector files){
        UserCommand cmd=fileSystem.getCommand("EDIT"); // NOI18N
        if (cmd != null) doCommand(files, "EDIT"); // NOI18N
    }

    public void doDetails (Vector files){
    }

    /*
    //-------------------------------------------
    public void doListSub(String path){
      D.deb("doListSub('"+path+"')"); // NOI18N
      VcsCache cache=fileSystem.getCache();
      String dirName=""; // NOI18N
      if( cache.isDir(path) ){
        dirName=path;
      }
      else{
        dirName=MiscStuff.getDirNamePart(path);
      }
      RetrievingDialog rd=new RetrievingDialog(fileSystem, dirName, new JFrame(), false );
      MiscStuff.centerWindow(rd);
      Thread t=new Thread(rd,"VCS-RetrievingThread-"+dirName); // NOI18N
      t.start();
}
    */

    //-------------------------------------------
    protected void doCommand(Vector files, UserCommand cmd){
        D.deb("doCommand("+files+","+cmd+")"); // NOI18N
        boolean[] askForEachFile = null;
        if (files.size() > 1) {
            askForEachFile = new boolean[1];
            askForEachFile[0] = true;
        }
        Hashtable vars=fileSystem.getVariablesAsHashtable();
        ExecuteCommand ec = null;
        //Integer synchAccess = new Integer(0);
        for(int i=0;i<files.size();i++){
            String fullName=(String)files.elementAt(i);
            VcsCache cache=fileSystem.getCache();
            String path=""; // NOI18N
            String file=""; // NOI18N
            //if( fileSystem.folder(fullName) ){
            //path=fullName;
            //file=""; // NOI18N
            //}
            //else{
            path=MiscStuff.getDirNamePart(fullName);
            file=MiscStuff.getFileNamePart(fullName);
            //}

            vars.put("PATH",fullName); // NOI18N
            vars.put("DIR",path); // NOI18N
            vars.put("DIR_S",path); // NOI18N
            if (additionalVars != null) {
                Enumeration keys = additionalVars.keys();
                while(keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    vars.put(key, additionalVars.get(key));
                }
            }
            String osName=System.getProperty("os.name");
            //D.deb("osName="+osName); // NOI18N
            // suppose that CVS server only exists for UNIX/Linux. If it`s server dont change PS
            if( osName.indexOf("Win")>=0 && ((CvsFileSystem) fileSystem).getCvsServerType ().equals (CvsFileSystem.CVS_SERVER_LOCAL)){ // NOI18N
                String winPath=path.replace('/','\\');
                D.deb("winPath="+winPath); // NOI18N
                vars.put("DIR",winPath); // NOI18N
                path = winPath;
            }
            if (path.length() == 0 && file.length() > 0 && file.charAt(0) == '/') file = file.substring (1, file.length ());
            vars.put("FILE",file); // NOI18N

            String confirmation = cmd.getConfirmationMsg();
            Variables v = new Variables();
            confirmation = v.expand(vars, confirmation, true);
            if (confirmation != null && confirmation.length() > 0) {
                if (NotifyDescriptor.Confirmation.NO_OPTION.equals (
                            TopManager.getDefault ().notify (new NotifyDescriptor.Confirmation (
                                                                 confirmation, NotifyDescriptor.Confirmation.YES_NO_OPTION)))) { // NOI18N
                    continue; // The command is cancelled for that file
                }
            }

            String exec=cmd.getExec();
            if (!fileSystem.promptForVariables(exec, vars, askForEachFile)) {
                fileSystem.debug(fileSystem.getBundleProperty("MSG_CommandCanceled")); // NOI18N
                return;
            }

            //synchronized (synchAccess) {
            fileSystem.setNumDoAutoRefresh(fileSystem.getNumDoAutoRefresh(path) + 1, path);
            if (!concurrentExecution && ec != null) {
                try {
                    ec.join();
                } catch (InterruptedException e) {
                    // ignoring the interruption
                }
            }
            //}
            //ErrorCommandDialog errDlg = fileSystem.getErrorDialog(); //new ErrorCommandDialog(cmd, new JFrame(), false);
            OutputContainer container = new OutputContainer(cmd);
            ec = new ExecuteCommand(fileSystem,cmd,vars);
            ec.setErrorNoRegexListener(container);
            ec.setOutputNoRegexListener(container);
            ec.setErrorContainer(container);
            ec.start();
            //cache.setFileStatus(fullName,"Unknown"); // NOI18N
            synchronized (vars) {
                vars = new Hashtable(vars);
            }
        }
    }

    //-------------------------------------------
    public void doAdditionalCommand(String name,Vector files){
        //D.deb("doAdditionalCommand('"+name+"',"+files+")"); // NOI18N
        boolean[] askForEachFile = null;
        if (files.size() > 1) {
            askForEachFile = new boolean[1];
            askForEachFile[0] = true;
        }
        Hashtable vars=fileSystem.getVariablesAsHashtable();
        Thread t = null;
        for(int i=0;i<files.size();i++){
            String fullName=(String)files.elementAt(i);

            String path=""; // NOI18N
            String file=""; // NOI18N
            //if( fileSystem.folder(fullName) ){
            //path=fullName;
            //file=""; // NOI18N
            //}
            //else{
            path=MiscStuff.getDirNamePart(fullName);
            file=MiscStuff.getFileNamePart(fullName);
            //}

            UserCommand cmd=fileSystem.getCommand(name);

            vars.put("PATH",fullName); // NOI18N
            vars.put("DIR",path); // NOI18N
            String osName=System.getProperty("os.name");

            //D.deb("osName="+osName); // NOI18N
            // suppose that CVS server only exists for UNIX/Linux. If it`s server dont change PS
            if( osName.indexOf("Win")>=0 && ((CvsFileSystem) fileSystem).getCvsServerType ().equals (CvsFileSystem.CVS_SERVER_LOCAL)){ // NOI18N
                String winPath=path.replace('/','\\');
                //D.deb("winPath="+winPath); // NOI18N
                vars.put("DIR",winPath); // NOI18N
                path = winPath;
            }
            if (path.length() == 0 && file.length() > 1 && file.substring (0,1).equals ("/")) // NOI18N
                file = file.substring (1, file.length ());
            vars.put("FILE",file); // NOI18N

            String confirmation = cmd.getConfirmationMsg();
            Variables v = new Variables();
            confirmation = v.expand(vars, confirmation, true);
            if (confirmation != null && confirmation.length() > 0) {
                if (NotifyDescriptor.Confirmation.NO_OPTION.equals (
                            TopManager.getDefault ().notify (new NotifyDescriptor.Confirmation (
                                                                 confirmation, NotifyDescriptor.Confirmation.YES_NO_OPTION)))) { // NOI18N
                    continue; // The command is cancelled for that file
                }
            }

            String exec=cmd.getExec();
            if (!fileSystem.promptForVariables(exec, vars, askForEachFile)) {
                fileSystem.debug(fileSystem.getBundleProperty("MSG_CommandCanceled")); // NOI18N
                return;
            }

            fileSystem.setNumDoAutoRefresh(fileSystem.getNumDoAutoRefresh(path) + 1, path);
            if (!concurrentExecution && t != null) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    // ignoring the interruption
                }
            }
            AdditionalCommandDialog acd=new AdditionalCommandDialog(fileSystem,cmd,vars,new JFrame(),false);
            MiscStuff.centerWindow(acd);
            t = new Thread(acd,"VCS-AdditionalCommand-"+name); // NOI18N
            t.start();
            synchronized (vars) {
                vars = new Hashtable(vars);
            }
        }
    }

    public VcsFile parseFromCache(String[] cacheRecord) {
        UserCommand list=fileSystem.getCommand("LIST"); // NOI18N
        return CommandLineVcsDirReader.matchToFile(cacheRecord,list);
    }

    //-------------------------------------------
    private JMenuItem createItem(String name){
        JMenuItem item=null ;
        UserCommand cmd=fileSystem.getCommand(name);

        if( name.equals("DETAILS")==true ){ // NOI18N
            item=new JMenuItem(g("CTL_MenuItem_DETAILS")); // NOI18N
            item.setActionCommand(name);
            item.addActionListener(this);
            assignHelp (item, name);
            return item;
        } else if( name.equals("LIST_SUB")==true ){ // NOI18N
            item=new JMenuItem(g("CTL_MenuItem_LIST_SUB")); // NOI18N
            item.setActionCommand(name);
            item.addActionListener(this);
            assignHelp (item, name);
            return item;
        }

        if( cmd==null ){
            //E.err("Command "+name+" not configured."); // NOI18N
            item=new JMenuItem("'"+name+"' not configured."); // NOI18N
            item.setEnabled(false);
            assignHelp (item, "UNCONFIG"); // NOI18N
            return item;
        }

        Hashtable vars=fileSystem.getVariablesAsHashtable();
        String label=cmd.getLabel();
        if (label.indexOf('$') >= 0) {
            Variables v = new Variables();
            label = v.expandFast(vars, label, true);
        }
        item=new JMenuItem(label);
        item.setActionCommand(cmd.getName());
        item.addActionListener(this);
        assignHelp (item, name);
        return item;
    }

    private void assignHelp (JMenuItem item, String commandName) {
        HelpCtx.setHelpIDString (item, CvsAction.class.getName () + "." + commandName); // NOI18N
    }
    
    /*
    private void adjustMenuPosition(JMenuItem menu) {
        Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle menuBounds = menu.getBounds();
        Point point = menu.getLocationOnScreen();
        if (menuBounds.height + point.y > screen.height) {
            point.y = screen.height - menuBounds.height;
            menu.setLocation(point);
        }
    }
    */

    //-------------------------------------------
    public JMenuItem getPopupPresenter(){
        JMenu menu=new JMenuPlus("CVS"); // NOI18N
        HelpCtx.setHelpIDString (menu, CvsAction.class.getName ());
        JMenuItem item=null;

        item=createItem("LIST"); // NOI18N
        menu.add(item);
        if (isOnDirectory()) {
            item=createItem("LIST_SUB"); // NOI18N
            menu.add(item);
        }
        menu.addSeparator();

        if (isOnRoot()) {
            item=createItem("INIT"); // NOI18N
            menu.add(item);
            /*
            item=createItem("COMMIT_MODULE"); // NOI18N
            menu.add(item);
            item=createItem("UPDATE_MODULE"); // NOI18N
            menu.add(item);
            */
            //item=createItem("IMPORT_MODULE"); // NOI18N
            //menu.add(item);
            item=createItem("CHECKOUT_MODULE"); // NOI18N
            menu.add(item);
            menu.addSeparator();
        }

        item=createItem("COMMIT"); // NOI18N
        menu.add(item);
        item=createItem("UPDATE"); // NOI18N
        menu.add(item);
        item=createItem("ADD"); // NOI18N
        menu.add(item);
        item=createItem("REMOVE"); // NOI18N
        menu.add(item);
        menu.addSeparator();

        if (isOnDirectory()) {
            item=createItem("IMPORT"); // NOI18N
            menu.add(item);
        }
        item=createItem("CHECKOUT"); // NOI18N
        menu.add(item);
        menu.addSeparator();

        item=createItem("LOCK"); // NOI18N
        menu.add(item);
        item=createItem("UNLOCK1"); // NOI18N
        menu.add(item);
        menu.addSeparator();

        JMenu submenu = new JMenuPlus(g("SubmenuEditing"));
        menu.add(submenu);
        item=createItem("EDIT"); // NOI18N
        submenu.add(item);
        item=createItem("UNEDIT"); // NOI18N
        submenu.add(item);
        item=createItem("EDITORS"); // NOI18N
        submenu.add(item);
        menu.addSeparator();

        submenu = new JMenuPlus(g("SubmenuWatches"));
        menu.add(submenu);
        item=createItem("WATCH_ON"); // NOI18N
        submenu.add(item);
        item=createItem("WATCH_OFF"); // NOI18N
        submenu.add(item);
        item=createItem("WATCH_ADD"); // NOI18N
        submenu.add(item);
        item=createItem("WATCHERS"); // NOI18N
        submenu.add(item);
        menu.addSeparator();

        item=createItem("STATUS"); // NOI18N
        menu.add(item);

        item=createItem("LOG"); // NOI18N
        menu.add(item);
        menu.addSeparator();

        if (isOnDirectory()) {
            item=createItem("CHECKOUT_REV_DIR"); // NOI18N
            menu.add(item);
            item=createItem("UPDATE_REV_DIR"); // NOI18N
            menu.add(item);
            item=createItem("COMMIT_REV_DIR"); // NOI18N
            menu.add(item);
        } else {
            item=createItem("CHECKOUT_REV"); // NOI18N
            menu.add(item);
            item=createItem("UPDATE_REV"); // NOI18N
            menu.add(item);
            item=createItem("COMMIT_REV"); // NOI18N
            menu.add(item);
        }
        menu.addSeparator();

        if (!isOnDirectory()) {
            item=createItem("MERGE"); // NOI18N
            menu.add(item);
        }
        item=createItem("REM_STICKY"); // NOI18N
        menu.add(item);
        menu.addSeparator();

        item=createItem("TAGS"); // NOI18N
        menu.add(item);

        if (!isOnDirectory()) {
            item=createItem("BRANCHES"); // NOI18N
            menu.add(item);

            item=createItem("DIFF"); // NOI18N
            menu.add(item);
        }

        //adjustMenuPosition(menu);
        return menu;
    }

    //-------------------------------------------
    String g(String s) {
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.cmdline.Bundle").getString (s);
    }
    String  g(String s, Object obj) {
        return MessageFormat.format (g(s), new Object[] { obj });
    }
    String g(String s, Object obj1, Object obj2) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2 });
    }
    String g(String s, Object obj1, Object obj2, Object obj3) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2, obj3 });
    }

    //-------------------------------------------
    public void actionPerformed(final java.awt.event.ActionEvent e){
        //D.deb("actionPerformed("+e+")"); // NOI18N
        String cmd= e.getActionCommand();
        //D.deb("cmd="+cmd); // NOI18N
        Node[] nodes=getActivatedNodes();
        Vector files=new Vector(10);
        String mimeType = null;
        EditorCookie ec = null;
        for(int i=0;i<nodes.length;i++){
            //D.deb("nodes["+i+"]="+nodes[i]); // NOI18N
            DataObject dd=(DataObject)(nodes[i].getCookie(DataObject.class));
            addImportantFiles(dd,files);
            FileObject ff = dd.getPrimaryFile();
            mimeType = ff.getMIMEType();
            //ec = (EditorCookie) nodes[i].getCookie(EditorCookie.class);
        }
        //D.deb("files="+files); // NOI18N

        if( nodes.length<1 ){
            E.err("internal error nodes.length<1 TODO"); // NOI18N
            return ;
        }

        String path=getNodePath(nodes[0]);
        //D.deb("path='"+path+"'"); // NOI18N

        if (mimeType != null) additionalVars.put("MIMETYPE", mimeType); // NOI18N
        D.deb("I have MIME = "+mimeType); // NOI18N

        if (cmd.equals("LIST")) {            doList     (path); // NOI18N
        } else if (cmd.equals("LIST_SUB")) { doListSub  (path); // NOI18N
        } else {                             doCommand (files, cmd);
        }
    }

}

/*
 * Log
 *  33   Gandalf-post-FCS1.28.2.3    4/4/00   Martin Entlicher Concurrent execution of 
 *       commands forbidden (bad for commit command).
 *  32   Gandalf-post-FCS1.28.2.2    4/4/00   Martin Entlicher Synchronized access to 
 *       vars.
 *  31   Gandalf-post-FCS1.28.2.1    3/29/00  Martin Entlicher Submenus added.
 *  30   Gandalf-post-FCS1.28.2.0    3/23/00  Martin Entlicher Support for confirmation
 *       message added, not asking for reason and other input variables for each
 *       file if not necessary, in command labels may be variables, module 
 *       commands added, watches commands added, order of import and checkout 
 *       changed, import is visible only on directories.
 *  29   Gandalf   1.28        2/10/00  Martin Entlicher Lock and Edit actions 
 *       added, actions structure changed.
 *  28   Gandalf   1.27        1/17/00  Martin Entlicher 
 *  27   Gandalf   1.26        1/15/00  Ian Formanek    NOI18N
 *  26   Gandalf   1.25        1/11/00  Jesse Glick     Context help.
 *  25   Gandalf   1.24        1/7/00   Martin Entlicher Do not show Refresh 
 *       recursively on files
 *  24   Gandalf   1.23        1/6/00   Martin Entlicher 
 *  23   Gandalf   1.22        1/5/00   Martin Entlicher 
 *  22   Gandalf   1.21        12/28/99 Martin Entlicher 
 *  21   Gandalf   1.20        12/21/99 Martin Entlicher Command canceled when 
 *       "Cancel" is pressed on variable input.
 *  20   Gandalf   1.19        12/14/99 Martin Entlicher 
 *  19   Gandalf   1.18        11/27/99 Patrik Knakal   
 *  18   Gandalf   1.17        11/23/99 Martin Entlicher Several new commands 
 *       added.
 *  17   Gandalf   1.16        11/9/99  Martin Entlicher 
 *  16   Gandalf   1.15        11/9/99  Martin Entlicher 
 *  15   Gandalf   1.14        10/26/99 Martin Entlicher 
 *  14   Gandalf   1.13        10/26/99 Martin Entlicher 
 *  13   Gandalf   1.12        10/26/99 Martin Entlicher 
 *  12   Gandalf   1.11        10/25/99 Pavel Buzek     
 *  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  10   Gandalf   1.9         10/13/99 Martin Entlicher 
 *  9    Gandalf   1.8         10/13/99 Pavel Buzek     
 *  8    Gandalf   1.7         10/13/99 Pavel Buzek     
 *  7    Gandalf   1.6         10/13/99 Martin Entlicher Variable DIR_S added
 *  6    Gandalf   1.5         10/7/99  Martin Entlicher DIFF action added
 *  5    Gandalf   1.4         10/7/99  Pavel Buzek     
 *  4    Gandalf   1.3         10/7/99  Pavel Buzek     
 *  3    Gandalf   1.2         10/7/99  Pavel Buzek     
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
*/