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

import org.netbeans.modules.vcs.*;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;

import org.openide.util.*;
import java.text.*;

import java.util.*;
/**
 * Read VCS directory recursively.
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CommandLineVcsDirReaderRecursive implements VcsDirReader {
    private Debug E=new Debug("CommandLineVcsDirReaderRecursive", true); // NOI18N
    private Debug D=E;

    private VcsFileSystem fileSystem = null;
    private UserCommand listSub = null;
    private Hashtable vars = null;
    private VcsDir dir = null;
    private String path = null;

    private boolean shouldFail=false;

    private VcsDirContainer rawData = null;

    private DirReaderListener listener = null;


    /** Creates new CommandLineVcsDirReaderRecursive */
    public CommandLineVcsDirReaderRecursive(DirReaderListener listener, VcsFileSystem fileSystem,
                                            UserCommand listSub, Hashtable vars) {
        this.listener = listener;
        this.fileSystem = fileSystem;
        this.listSub = listSub;
        this.vars = vars;
        String path = (String)vars.get("DIR"); // NOI18N
        this.path = path;
        D.deb ("DIR="+(String)vars.get("DIR")); // NOI18N
        dir = new VcsDir();
        path = path.replace ('\\', '/');
        dir.setPath (path);
        dir.setName(MiscStuff.getFileNamePart(path));
        //if (path.length() == 0) vars.put("DIR", "."); // NOI18N
        D.deb("DIR="+(String)vars.get("DIR")); // NOI18N
    }

    public void runCommand(String exec, OutputContainer container) {
        fileSystem.debug("LIST_SUB: "+g("MSG_List_command_failed")); // NOI18N
        shouldFail=true ;
    }

    public void runClass(String className, StringTokenizer tokens, OutputContainer container) {
        E.deb("runClass: "+className); // NOI18N
        E.deb("Creating new CvsListCommand"); // NOI18N
        Class listClass = null;
        try {
            listClass =  Class.forName(className, true,
                                       org.openide.TopManager.getDefault().currentClassLoader());
        } catch (ClassNotFoundException e) {
            fileSystem.debug ("LIST_SUB: "+g("ERR_ClassNotFound", className)); // NOI18N
            container.match("LIST_SUB: "+g("ERR_ClassNotFound", className)); // NOI18N
            shouldFail = true;
            return;
        }
        E.deb(listClass+" loaded"); // NOI18N
        VcsListRecursiveCommand listCommand = null;
        try {
            listCommand = (VcsListRecursiveCommand) listClass.newInstance();
        } catch (InstantiationException e) {
            fileSystem.debug ("LIST_SUB: "+g("ERR_CanNotInstantiate", listClass)); // NOI18N
            container.match("LIST_SUB: "+g("ERR_CanNotInstantiate", listClass)); // NOI18N
            shouldFail = true;
            return;
        } catch (IllegalAccessException e) {
            fileSystem.debug ("LIST_SUB: "+g("ERR_IllegalAccessOnClass", listClass)); // NOI18N
            container.match(g("LIST_SUB: "+"ERR_IllegalAccessOnClass", listClass)); // NOI18N
            shouldFail = true;
            return;
        }
        E.deb("VcsListCommand created."); // NOI18N
        String[] args = new String[tokens.countTokens()];
        int i = 0;
        while(tokens.hasMoreTokens()) {
            args[i++] = tokens.nextToken();
        }

        VcsDirContainer filesByName = new VcsDirContainer(path);
        if (!shouldFail) {
            vars.put("DATAREGEX", listSub.getDataRegex()); // NOI18N
            vars.put("ERRORREGEX", listSub.getErrorRegex()); // NOI18N
            vars.put("INPUT", listSub.getInput()); // NOI18N
            vars.put("TIMEOUT", new Long(listSub.getTimeout())); // NOI18N
            shouldFail = !listCommand.listRecursively(vars, args, filesByName, container, container,
                         null, listSub.getDataRegex(),
                         new RegexListener () {
                             public void match(String[] elements){
                                 //D.deb("stderr match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                 fileSystem.debug("stderr: "+MiscStuff.arrayToString(elements)); // NOI18N
                                 //shouldFail=true ;
                             }
                         }, listSub.getErrorRegex());
            //E.deb("shouldFail = "+shouldFail+" after list with "+filesByName.size()+" elements"); // NOI18N
            /*
            for(Enumeration e = filesByName.keys(); e.hasMoreElements() ;) {
              String fileName=(String)e.nextElement();
              String fileStatus=(String)filesByName.get(fileName);
              E.deb("filesByName: "+fileName+" | "+fileStatus);
        }
            */
        }
        rawData = new VcsDirContainer();
        putFilesToDirRecursively(dir, filesByName, rawData);
        if (shouldFail) {
            fileSystem.debug("LIST_SUB: "+g("MSG_List_command_failed")); // NOI18N
            container.match("LIST_SUB: "+g("MSG_List_command_failed")); // NOI18N
        } else {
            fileSystem.debug("LIST_SUB: "+g("MSG_Command_succeeded")); // NOI18N
            container.match("LIST_SUB: "+g("MSG_Command_succeeded")); // NOI18N
        }

    }

    private void putFilesToDirRecursively(VcsDir dir, VcsDirContainer filesByName,
                                          VcsDirContainer rawData) {
        D.deb("putFilesToDirRecursively("+filesByName.getPath()+")");
        if (dir == null || filesByName == null) return;
        if (rawData.getElement() == null) rawData.setElement(new Vector());
        putFilesToDir(dir, (Hashtable) filesByName.getElement(), (Vector) rawData.getElement());
        D.deb("putFilesToDirRecursively: dir = "+dir);
        String[] subdirs = filesByName.getSubdirs();
        D.deb("subdirs = "+MiscStuff.array2string(subdirs));
        for(int i = 0; i < subdirs.length; i++) {
            VcsDirContainer subFilesByName = filesByName.getDirContainer(subdirs[i]);
            String path = subFilesByName.getPath();
            VcsDir subdir = dir.getDir(subdirs[i]);
            if (subdir == null) {
                D.deb("subdir "+subdirs[i]+" does not exist in dir = "+dir);
                subdir = new VcsDir(subdirs[i]);
                dir.addSubdir(subdir);
            }
            subdir.setPath(path);
            D.deb("subdir path = "+path);
            VcsDirContainer subRawData = rawData.addSubdir(path);
            putFilesToDirRecursively(subdir, subFilesByName, subRawData);
            D.deb("putFilesToDirRecursively("+filesByName.getPath()+") after adding "+subdir+"\n\t\t dir = "+dir);
        }
    }

    private void putFilesToDir(VcsDir dir, Hashtable filesByName, Vector rawData) {
        if (filesByName == null) return;
        for(Enumeration e = filesByName.keys(); e.hasMoreElements() ;) {
            String fileName = (String)e.nextElement();
            String[] elements = (String[])filesByName.get(fileName);
            //elements[0] = fileName;
            //elements[1] = fileStatus;
            //E.deb("Processing: "+fileName+"|"+elements); // NOI18N
            //fileSystem.debug("stdout: "+MiscStuff.arrayToString(elements)); // NOI18N
            rawData.addElement(elements);
            VcsFile file = CommandLineVcsDirReader.matchToFile(elements, listSub);
            if(file instanceof VcsDir) {
                String parent = dir.getPath ();
                ((VcsDir)file).setPath (((parent.length() > 0) ? parent + "/" : "") + file.getName ()); // NOI18N
                ((VcsDir)file).setLoaded(false);
            }
            //D.deb("adding file="+file); // NOI18N
            dir.add(file);
        }
    }

    public void run() {
        Variables v = new Variables();
        String exec = listSub.getExec();
        exec = v.expand(vars,exec, true).trim();
        fileSystem.debug("LIST_SUB: "+exec); // NOI18N

        ErrorCommandDialog errDlg = fileSystem.getErrorDialog(); //new ErrorCommandDialog(list, new JFrame(), false);
        OutputContainer container = new OutputContainer(listSub);
        container.match("LIST_SUB: "+exec); // NOI18N

        StringTokenizer tokens = new StringTokenizer(exec);
        String first = tokens.nextToken();
        E.deb("first = "+first); // NOI18N
        if (first != null && (first.toLowerCase().endsWith(".class"))) { // NOI18N
            runClass(first.substring(0, first.length() - ".class".length()), tokens, container); // NOI18N
        } else
            runCommand(exec, container);

        if(shouldFail){
            errDlg.putCommandOut(container);
            errDlg.showDialog();
            fileSystem.setPassword(null);
            fileSystem.debug(g("ERR_LISTFailed")); // NOI18N
            D.deb("failed reading of dir="+dir); // NOI18N
            if(!dir.getName ().equals("")) { // NOI18N
                dir.setStatus (g("MSG_VCS_command_failed")); // NOI18N
            }
            dir.setLoadedRecursive(true); // failed, but loaded
            listener.readDirFinishedRecursive(dir, rawData, !shouldFail);
        }
        else{
            //errDlg.removeCommandOut();
            //errDlg.cancelDialog();
            //fileSystem.debug("LIST command finished successfully"); // NOI18N
            dir.setLoadedRecursive(true);
            listener.readDirFinishedRecursive(dir, rawData, !shouldFail);
        }

        // After refresh I should ensure, that the next automatic refresh will work if something happens in numbering
        fileSystem.removeNumDoAutoRefresh((String)vars.get("DIR")); // NOI18N
        //D.deb("run(LIST) '"+dir.name+"' finished"); // NOI18N
    }

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
}