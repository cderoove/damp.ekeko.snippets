/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vcs.advanced;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.text.*;

import org.openide.*;
import org.openide.util.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;


import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.*;

/**
 * Provides implementation of actions on VCS files and directories.
 * @author  Pavel Buzek
 */
public class CommandLineAction  extends VcsAction implements ActionListener {

    private Debug E=new Debug("CommandLineVcsAction", false); // NOI18N
    private Debug D=E;
    //private CommandLineVcsFileSystem fileSystem=null;


    /**
     * @associates String 
     */
    private Hashtable additionalVars = new Hashtable();

    static final long serialVersionUID =-2475922263910649869L;
    /** Creates new VcsActionImpl */
    public CommandLineAction (CommandLineVcsFileSystem fileSystem) {
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
        for(int i=0;i<nodes.length;i++){
            //D.deb("nodes["+i+"]="+nodes[i]); // NOI18N
        }
        return true;
    }

    //-------------------------------------------
    public HelpCtx getHelpCtx(){
        //D.deb("getHelpCtx()"); // NOI18N
        return null;
    }

    //-------------------------------------------
    public CommandLineVcsFileSystem getFileSystem() {
        return (CommandLineVcsFileSystem) fileSystem;
    }

    //-------------------------------------------
    public void doList(String path){
        //D.deb("doList('"+path+"')"); // NOI18N
        VcsCache cache=fileSystem.getCache();

        if( cache.isDir(path) ){
            cache.refreshDir(path);
            return ;
        }

        String dirName=MiscStuff.getDirNamePart(path);
        cache.refreshDir(dirName);
    }

    /*
    //-------------------------------------------
    public void doListSub(String path){
      //D.deb("doListSub('"+path+"')"); // NOI18N
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
    public void doDetails(Vector files){
        //D.deb("doDetails() TODO"); // NOI18N
        fileSystem.debugClear();
        fileSystem.debug("DETAILS: TODO");
    }


    //-------------------------------------------
    public void doLock(Vector files){
        UserCommand cmd = fileSystem.getCommand("LOCK"); // NOI18N
        if (cmd != null) doCommand(files, "LOCK"); // NOI18N
    }


    //-------------------------------------------
    public void doUnlock(Vector files){
        UserCommand cmd = fileSystem.getCommand("UNLOCK"); // NOI18N
        if (cmd != null) doCommand(files, "UNLOCK"); // NOI18N
    }

    //-------------------------------------------
    public void doEdit (Vector files){
        D.deb("doEdit("+files+")");
        UserCommand cmd=fileSystem.getCommand("EDIT"); // NOI18N
        D.deb("command = "+cmd);
        if (cmd != null) doCommand(files, "EDIT"); // NOI18N
    }

    //-------------------------------------------
    protected void doCommand(Vector files, UserCommand cmd){
        //D.deb("doCommand("+files+","+cmd+")"); // NOI18N
        boolean[] askForEachFile = null;
        if (files.size() > 1) {
            askForEachFile = new boolean[1];
            askForEachFile[0] = true;
        }
        Hashtable vars=fileSystem.getVariablesAsHashtable();
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

            vars.put("DIR",path); // NOI18N
            String osName=System.getProperty("os.name");
            //D.deb("osName="+osName); // NOI18N
            if( osName.indexOf("Win")>=0 ){ // NOI18N
                String winPath=path.replace('/','\\');
                //D.deb("winPath="+winPath); // NOI18N
                vars.put("DIR",winPath); // NOI18N
                path = winPath;
            }
            vars.put("FILE",file); // NOI18N
            if (additionalVars != null) {
                Enumeration keys = additionalVars.keys();
                while(keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    vars.put(key, additionalVars.get(key));
                }
            }
            //if (path.length() == 0) vars.put("DIR", "."); // NOI18N

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
            //ErrorCommandDialog errDlg = fileSystem.getErrorDialog(); //new ErrorCommandDialog(cmd, new JFrame(), false);
            OutputContainer container = new OutputContainer(cmd);
            ExecuteCommand ec=new ExecuteCommand(fileSystem,cmd,vars);
            ec.setErrorNoRegexListener(container);
            ec.setOutputNoRegexListener(container);
            ec.setErrorContainer(container);
            ec.start();
            cache.setFileStatus(fullName,"Unknown");
            synchronized(vars) {
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
        UserCommand cmd=fileSystem.getCommand(name);
        Hashtable vars=fileSystem.getVariablesAsHashtable();
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

            vars.put("DIR",path); // NOI18N
            String osName=System.getProperty("os.name");
            //D.deb("osName="+osName); // NOI18N
            if( osName.indexOf("Win")>=0 ){ // NOI18N
                String winPath=path.replace('/','\\');
                //D.deb("winPath="+winPath); // NOI18N
                vars.put("DIR",winPath); // NOI18N
                path = winPath;
            }
            vars.put("FILE",file); // NOI18N
            if (additionalVars != null) {
                Enumeration keys = additionalVars.keys();
                while(keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    vars.put(key, additionalVars.get(key));
                }
            }
            //if (path.length() == 0) vars.put("DIR", "."); // NOI18N

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
            AdditionalCommandDialog acd=new AdditionalCommandDialog(fileSystem,cmd,vars,new JFrame(),false);
            MiscStuff.centerWindow(acd);
            Thread t=new Thread(acd,"VCS-AdditionalCommand-"+name); // NOI18N
            t.start();
            synchronized(vars) {
                vars = new Hashtable(vars);
            }
        }
    }

    public VcsFile parseFromCache(String[] cacheRecord) {
        UserCommand list=fileSystem.getCommand("LIST"); // NOI18N
        return CommandLineVcsDirReader.matchToFile(cacheRecord,list);
    }

    //-------------------------------------------
    protected JMenuItem createItem(String name){
        JMenuItem item=null ;
        UserCommand cmd=fileSystem.getCommand(name);

        if( name.equals("DETAILS")==true ){ // NOI18N
            item=new JMenuItem("Details");
            item.setActionCommand(name);
            item.addActionListener(this);
            return item;
        } else if( name.equals("LIST_SUB")==true ){ // NOI18N
            item=new JMenuItem(g("CTL_MenuItem_LIST_SUB")); // NOI18N
            item.setActionCommand(name);
            item.addActionListener(this);
            return item;
        }

        if( cmd==null ){
            //E.err("Command "+name+" not configured."); // NOI18N
            item=new JMenuItem("'"+name+"' not configured.");
            item.setEnabled(false);
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
        return item;
    }

    //-------------------------------------------
    public JMenuItem getPopupPresenter(){
        JMenu menu=new JMenu("Version Control");
        JMenuItem item=null;

        Vector commands = fileSystem.getCommands();
        int len = commands.size();
        int lastOrder = 0;
        boolean onDir = isOnDirectory();
        boolean onRoot = isOnRoot();
        for(int i = 0; i < len; i++) {
            UserCommand uc = (UserCommand) commands.get(i);
            /*
            if (!onDir && uc.getName().equals("LIST_SUB")) {
              lastOrder++;
              continue;
        } */
            if (onDir && !uc.getOnDir() || !onDir && !uc.getOnFile() ||
                    !onRoot && uc.getOnRoot()) {
                lastOrder++;
                continue;
            }
            int order = uc.getOrder();
            for(int j = lastOrder+1; j < order; j++) menu.addSeparator();
            lastOrder = order;
            item=createItem(uc.getName());
            menu.add(item);
        }
        return menu;
    }



    //-------------------------------------------
    String g(String s) {
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.advanced.Bundle").getString (s);
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
        for(int i=0;i<nodes.length;i++){
            //D.deb("nodes["+i+"]="+nodes[i]); // NOI18N
            DataObject dd=(DataObject)(nodes[i].getCookie(DataObject.class));
            addImportantFiles(dd,files);
            FileObject ff = dd.getPrimaryFile();
            mimeType = ff.getMIMEType();
        }
        //D.deb("files="+files); // NOI18N

        if( nodes.length<1 ){
            E.err("internal error nodes.length<1 TODO");
            return ;
        }

        String path=getNodePath(nodes[0]);
        //D.deb("path='"+path+"'"); // NOI18N

        if (mimeType != null) additionalVars.put("MIMETYPE", mimeType); // NOI18N
        D.deb("I have MIMETYPE = "+mimeType); // NOI18N

        if (cmd.equals("LIST")) {            doList     (path); // NOI18N
        } else if (cmd.equals("LIST_SUB")) { doListSub  (path); // NOI18N
        } else {                             doCommand (files, cmd);
        }
        /*
    } else if(cmd.equals("DETAILS")){  doDetails  (files);
    } else if(cmd.equals("CHECKIN")){  doCheckIn  (files);
    } else if(cmd.equals("CHECKOUT")){ doCheckOut (files);
    } else if(cmd.equals("LOCK")){     doLock     (files);
    } else if(cmd.equals("UNLOCK")){   doUnlock   (files);
    } else if(cmd.equals("ADD")){      doAdd      (files);
    } else if(cmd.equals("REMOVE")){   doRemove   (files);
    } else if( fileSystem.isAdditionalCommand(cmd) ){ doAdditionalCommand(cmd,files);
    } else{
          E.err("Invalid command cmd='"+cmd+"'.");
    }
        */
    }

}

/*
 * <<Log>>
 *  21   Gandalf-post-FCS1.17.2.2    4/4/00   Martin Entlicher Synchronized access to 
 *       vars.
 *  20   Gandalf-post-FCS1.17.2.1    3/29/00  Martin Entlicher Variable input changed
 *  19   Gandalf-post-FCS1.17.2.0    3/23/00  Martin Entlicher Not ask the user for 
 *       variable values when not necessary, Support for confirmation message 
 *       added, variables can be in menu items, popup presenter construction 
 *       enhanced.
 *  18   Gandalf   1.17        2/10/00  Martin Entlicher Default actions deleted 
 *       and automatic refresh support.
 *  17   Gandalf   1.16        1/27/00  Martin Entlicher NOI18N
 *  16   Gandalf   1.15        1/26/00  Martin Entlicher 
 *  15   Gandalf   1.14        12/28/99 Martin Entlicher Yury changes.
 *  14   Gandalf   1.13        12/21/99 Martin Entlicher Do not run the command 
 *       when "Cancel" is pressed on the variable input window.
 *  13   Gandalf   1.12        12/14/99 Martin Entlicher 
 *  12   Gandalf   1.11        12/8/99  Martin Entlicher Added variable MIMETYPE.
 *  11   Gandalf   1.10        11/30/99 Martin Entlicher 
 *  10   Gandalf   1.9         11/27/99 Patrik Knakal   
 *  9    Gandalf   1.8         10/25/99 Pavel Buzek     copyright
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/12/99 Martin Entlicher 
 *  6    Gandalf   1.5         9/30/99  Pavel Buzek     
 *  5    Gandalf   1.4         9/9/99   Martin Entlicher Fixed DIR variable value
 *  4    Gandalf   1.3         9/9/99   Pavel Buzek     
 *  3    Gandalf   1.2         9/9/99   Pavel Buzek     
 *  2    Gandalf   1.1         9/8/99   Martin Entlicher Added support for 
 *       ErrorCommandDialog
 *  1    Gandalf   1.0         9/8/99   Pavel Buzek     
 * $
 */
