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

package org.netbeans.modules.vcs;

import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.util.MiscStuff;
import org.netbeans.modules.vcs.util.Debug;

import org.openide.*;
import org.openide.util.actions.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.loaders.*;

import java.util.*;
import javax.swing.*;

/**
 *
 * @author  Pavel Buzek
 * @version 
 */
public abstract class VcsAction extends NodeAction {
    private Debug E=new Debug("VcsAction", true); // NOI18N
    private Debug D=E;

    protected VcsFileSystem fileSystem = null;

    public void setFileSystem(VcsFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public abstract VcsFile parseFromCache (String[] cacheRecord);
    //public abstract void doList(String path);
    public abstract void doDetails(Vector files);
    //public abstract void doCheckIn(Vector files);
    //public abstract void doCheckOut(Vector files);
    public abstract void doLock(Vector files);
    public abstract void doUnlock(Vector files);
    public abstract void doEdit(Vector files);
    //public abstract void doAdd(Vector files);
    //public abstract void doRemove(Vector files);
    public abstract JMenuItem getPopupPresenter();
    public abstract void doAdditionalCommand(String name, Vector files);
    protected abstract void doCommand(Vector files, UserCommand cmd);

    /**
     * Get the path of a node.
     * @param n the node to retrieve the path of
     * @return the path
     */
    protected String getNodePath(Node n){
        StringBuffer sb=new StringBuffer(80);
        Node parent=null;
        while( (parent=n.getParentNode())!= null ){
            if(parent.getParentNode()!=null){
                sb.insert(0,"/"); // NOI18N
                sb.insert(0,n.getName());
            }
            n=parent;
        }
        int len=sb.length();
        if( len<=1 ){
            return ""; // NOI18N
        }
        int from=(sb.charAt(0)=='/' ? 1:0);
        int to=(sb.charAt(len-1)=='/' ? len-1:len);
        String res=sb.substring(from,to);
        return res;
    }

    /**
     * Do refresh of a directory.
     * @param path the directory path
     */
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

    /**
     * Do recursive refresh of a directory.
     * @param path the directory path
     */
    public void doListSub(String path) {
        //D.deb("doListSub('"+path+"')"); // NOI18N
        UserCommand cmd = fileSystem.getCommand("LIST_SUB");
        VcsCache cache = fileSystem.getCache();
        String dirName = ""; // NOI18N
        if (cache.isDir(path)) {
            dirName = path;
        }
        else{
            dirName = MiscStuff.getDirNamePart(path);
        }
        if (cmd != null && cmd.getExec().trim().length() > 0) {
            if( cache.isDir(path) ){
                cache.refreshDirRecursive(path);
                return ;
            }
            cache.refreshDirRecursive(dirName);
        } else {
            RetrievingDialog rd = new RetrievingDialog(fileSystem, dirName, new JFrame(), false);
            MiscStuff.centerWindow(rd);
            Thread t = new Thread(rd,"VCS-RetrievingThread-"+dirName); // NOI18N
            t.start();
        }
    }

    /**
     * Do a command on a set of files.
     * @param cmd the command name
     * @param files the set of files to perform the command on
     */
    protected void doCommand(Vector files, String cmd){
        UserCommand command = fileSystem.getCommand(cmd);
        if (command == null) {
            return;
        }
        if(command.getDisplayOutput ()) {
            final String fcmd = cmd;
            final Vector ffiles = files;
            new Thread("VCS - command Thread") {
                public void run() {
                    doAdditionalCommand (fcmd, ffiles);
                }
            }.start();
        } else {
            final UserCommand fcommand = command;
            final Vector ffiles = files;
            new Thread("VCS - command Thread") {
                public void run() {
                    doCommand(ffiles, fcommand);
                }
            }.start();
        }
    }

    /**
     * Test if the selected node is a directory.
     * @return <code>true</code> if at least one selected node is a directory,
     *         <code>false</code> otherwise.
     */
    protected boolean isOnDirectory() {
        Node[] nodes = getActivatedNodes();
        for (int i = 0; i < nodes.length; i++) {
            DataObject dd = (DataObject) (nodes[i].getCookie(DataObject.class));
            if (dd.getPrimaryFile().isFolder()) return true;
        }
        return false;
    }

    /**
     * Test if the selected node is the root node.
     * @return <code>true</code> if at least one selected node is the root node,
     *         <code>false</code> otherwise.
     */
    protected boolean isOnRoot() {
        Node[] nodes = getActivatedNodes();
        for (int i = 0; i < nodes.length; i++) {
            String path = getNodePath(nodes[i]);
            if (path.length() == 0) return true;
        }
        return false;
    }

    /**
     * Add files marked as important.
     * @param dd the data object from which the files are read.
     * @param res the Vector of <code>VcsFile</code> objects which are important.
     */
    protected void addImportantFiles(DataObject dd, Vector res){
        Set ddFiles=dd.files();
        for(Iterator it=ddFiles.iterator();it.hasNext();){
            FileObject ff=(FileObject)it.next();
            String fileName = ff.getPackageNameExt('/','.');
            //VcsFile file = fileSystem.getCache().getFile(fileName);
            //D.deb("file = "+file+" for "+fileName);
            //if (file == null || file.isImportant()) {
            if (fileSystem.isImportant(fileName)) {
                D.deb(fileName+" is important");
                res.addElement(fileName);
            }
            else D.deb(fileName+" is NOT important");
        }
    }

}

/*
 * Log
 *  6    Gandalf-post-FCS1.3.2.1     4/4/00   Martin Entlicher Command run in their own
 *       thread.
 *  5    Gandalf-post-FCS1.3.2.0     3/23/00  Martin Entlicher addImportantFiles() and 
 *       isOnRoot() added, some methods moved from CvsAction.
 *  4    Gandalf   1.3         2/10/00  Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     copyright and log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
