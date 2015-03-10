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

import java.io.*;
import java.util.*;
import java.beans.*;
import java.text.*;
import javax.swing.JFrame;
import java.net.URL;
import java.net.URLClassLoader;

import org.netbeans.modules.vcs.cmdline.exec.*;
import org.netbeans.modules.vcs.cmdline.VcsListCommand;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.*;
import org.openide.util.*;

/** Read single VCS directory.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class CommandLineVcsDirReader implements VcsDirReader {
    private Debug E=new Debug("CommandLineVcsDirReader", true); // NOI18N
    private Debug D=E;

    private VcsFileSystem fileSystem=null;
    private UserCommand list=null ;
    private Hashtable vars=null ;
    private VcsDir dir=null ;

    private boolean shouldFail=false ;

    /**
     * @associates String 
     */
    private Vector /*String[]*/ rawData=new Vector(40);

    private DirReaderListener listener=null ;


    //-------------------------------------------
    public CommandLineVcsDirReader(DirReaderListener listener,VcsFileSystem fileSystem,UserCommand list,Hashtable vars){
        //super("VCS-DirReader-"+((String)vars.get("DIR"))); // NOI18N
        this.listener=listener;
        this.fileSystem=fileSystem;
        this.list=list;
        this.vars=vars;
        String path = (String)vars.get("DIR"); // NOI18N
        D.deb ("DIR="+(String)vars.get("DIR")); // NOI18N
        dir=new VcsDir();
        path = path.replace ('\\', '/');
        dir.setPath (path);
        dir.setName (MiscStuff.getFileNamePart (path));
        //if (path.length() == 0) vars.put("DIR", "."); // NOI18N
        D.deb ("DIR="+(String)vars.get("DIR")); // NOI18N
    }


    //-------------------------------------------
    public Vector getRawData(){
        return rawData;
    }


    private void runCommand(String exec, OutputContainer container){
        ExternalCommand ec=new ExternalCommand(exec);
        ec.setTimeout(list.getTimeout());
        ec.setInput(list.getInput());
        //D.deb("list.getInput()='"+list.getInput()+"'"); // NOI18N

        String dataRegex=list.getDataRegex();
        //D.deb("dataRegex="+list.getDataRegex()); // NOI18N
        try{
            ec.addStdoutRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stdout match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                              fileSystem.debug("stdout: "+MiscStuff.arrayToString(elements)); // NOI18N
                                              rawData.addElement(elements);
                                              VcsFile file=matchToFile(elements,list);
                                              if(file instanceof VcsDir) {
                                                  String parent = dir.getPath ();
                                                  ((VcsDir)file).setPath (((parent.length() > 0) ? parent + "/" : "") + file.getName ()); // NOI18N
                                                  ((VcsDir)file).setLoaded(false);
                                              }
                                              D.deb("file="+file+", status = "+file.getStatus()); // NOI18N
                                              dir.add( file );
                                          }
                                      },dataRegex);
        }
        catch (BadRegexException e){
            E.err(e,"bad regex"); // NOI18N
        }

        String errorRegex=list.getErrorRegex();
        //D.deb("errorRegex="+list.getErrorRegex()); // NOI18N
        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                              fileSystem.debug("stderr: "+MiscStuff.arrayToString(elements)); // NOI18N
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
        }
        catch (BadRegexException e){
            E.err(e,"bad regex"); // NOI18N
        }

        ec.addStdoutNoRegexListener(container);
        ec.addStderrNoRegexListener(container);

        D.deb("ec="+ec); // NOI18N
        int exitStatus=ec.exec();

        if( shouldFail ){
            fileSystem.debug("LIST: "+g("MSG_Match_on_stderr")); // NOI18N
            exitStatus=ExternalCommand.FAILED;
        }

        switch (exitStatus){
        case ExternalCommand.SUCCESS:
            fileSystem.debug("LIST: "+g("MSG_Command_succeeded")); // NOI18N
            break ;
        case ExternalCommand.FAILED_ON_TIMEOUT:
            fileSystem.debug("LIST: "+g("MSG_Timeout")); // NOI18N
        case ExternalCommand.FAILED:
            fileSystem.debug
            ("LIST: "+g("MSG_List_command_failed")); // NOI18N
            shouldFail=true ;
        }
    }

    private void runClass(String className, StringTokenizer tokens, OutputContainer container) {
        E.deb("runClass: "+className); // NOI18N
        E.deb("Creating new CvsListCommand"); // NOI18N
        Class listClass = null;
        try {
            listClass =  Class.forName(className, true,
                                       org.openide.TopManager.getDefault().currentClassLoader());
        } catch (ClassNotFoundException e) {
            fileSystem.debug ("LIST: "+g("ERR_ClassNotFound", className)); // NOI18N
            container.match("LIST: "+g("ERR_ClassNotFound", className)); // NOI18N
            shouldFail = true;
            return;
        }
        E.deb(listClass+" loaded"); // NOI18N
        VcsListCommand listCommand = null;
        try {
            listCommand = (VcsListCommand) listClass.newInstance();
        } catch (InstantiationException e) {
            fileSystem.debug ("LIST: "+g("ERR_CanNotInstantiate", listClass)); // NOI18N
            container.match("LIST: "+g("ERR_CanNotInstantiate", listClass)); // NOI18N
            shouldFail = true;
            return;
        } catch (IllegalAccessException e) {
            fileSystem.debug ("LIST: "+g("ERR_IllegalAccessOnClass", listClass)); // NOI18N
            container.match(g("LIST: "+"ERR_IllegalAccessOnClass", listClass)); // NOI18N
            shouldFail = true;
            return;
        }
        E.deb("VcsListCommand created."); // NOI18N
        String[] args = new String[tokens.countTokens()];
        int i = 0;
        while(tokens.hasMoreTokens()) {
            args[i++] = tokens.nextToken();
        }

        Hashtable filesByName = new Hashtable();
        if (!shouldFail) {
            vars.put("DATAREGEX", list.getDataRegex()); // NOI18N
            vars.put("ERRORREGEX", list.getErrorRegex()); // NOI18N
            vars.put("INPUT", list.getInput()); // NOI18N
            vars.put("TIMEOUT", new Long(list.getTimeout())); // NOI18N
            shouldFail = !listCommand.list(vars, args, filesByName, container, container, null, list.getDataRegex(),
                                           new RegexListener () {
                                               public void match(String[] elements){
                                                   //D.deb("stderr match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                                   fileSystem.debug("stderr: "+MiscStuff.arrayToString(elements)); // NOI18N
                                                   //shouldFail=true ;
                                               }
                                           }, list.getErrorRegex());
            E.deb("shouldFail = "+shouldFail+" after list with "+filesByName.size()+" elements"); // NOI18N
            /*
            for(Enumeration e = filesByName.keys(); e.hasMoreElements() ;) {
              String fileName=(String)e.nextElement();
              String fileStatus=(String)filesByName.get(fileName);
              E.deb("filesByName: "+fileName+" | "+fileStatus);
        }
            */
        }
        //if (!shouldFail) {
        //String[] elements = new String[2];
        D.deb("adding to dir="+dir); // NOI18N
        for(Enumeration e = filesByName.keys(); e.hasMoreElements() ;) {
            String fileName=(String)e.nextElement();
            String[] elements=(String[])filesByName.get(fileName);
            //elements[0] = fileName;
            //elements[1] = fileStatus;
            E.deb("Processing: "+fileName+"|"+elements); // NOI18N
            fileSystem.debug("stdout: "+MiscStuff.arrayToString(elements)); // NOI18N
            rawData.addElement(elements);
            VcsFile file=matchToFile(elements,list);
            if(file instanceof VcsDir) {
                String parent = dir.getPath ();
                ((VcsDir)file).setPath (((parent.length() > 0) ? parent + "/" : "") + file.getName ()); // NOI18N
                ((VcsDir)file).setLoaded(false);
            }
            D.deb("adding file="+file); // NOI18N
            dir.add( file );
        }
        //}
        if (shouldFail) {
            fileSystem.debug("LIST: "+g("MSG_List_command_failed")); // NOI18N
            container.match("LIST: "+g("MSG_List_command_failed")); // NOI18N
        } else {
            fileSystem.debug("LIST: "+g("MSG_Command_succeeded")); // NOI18N
            container.match("LIST: "+g("MSG_Command_succeeded")); // NOI18N
        }

    }

    //-------------------------------------------
    public void run(){
        Variables v=new Variables();
        String exec=list.getExec();
        exec=v.expand(vars,exec, true).trim();
        fileSystem.debug("LIST: "+exec); // NOI18N

        ErrorCommandDialog errDlg = fileSystem.getErrorDialog(); //new ErrorCommandDialog(list, new JFrame(), false);
        OutputContainer container = new OutputContainer(list);
        container.match("LIST: "+exec); // NOI18N

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
            if( dir.getName ().equals("") ){ // NOI18N
                D.deb("root dir patch"); // NOI18N
                dir.setLoaded(true); // failed, but loaded
                listener.readDirFinished( dir, rawData, !shouldFail);
            } else {
                dir.setStatus (g("MSG_VCS_command_failed")); // NOI18N
                dir.setLoaded(true); // failed, but loaded
                listener.readDirFinished( dir, rawData, !shouldFail);
            }
        }
        else{
            //errDlg.removeCommandOut();
            //errDlg.cancelDialog();
            //fileSystem.debug("LIST command finished successfully"); // NOI18N
            dir.setLoaded(true);
            listener.readDirFinished( dir, rawData, !shouldFail);
        }

        // After refresh I should ensure, that the next automatic refresh will work if something happens in numbering
        fileSystem.removeNumDoAutoRefresh((String)vars.get("DIR")); // NOI18N
        //D.deb("run(LIST) '"+dir.name+"' finished"); // NOI18N
    }


    //-------------------------------------------
    public static VcsFile matchToFile(String[] elements, UserCommand list){
        //System.err.println("matchToFile("+MiscStuff.arrayToString(elements)+")");

        VcsFile file=new VcsFile();

        int fileNameIndex=list.getFileName();
        if( MiscStuff.withinRange(0,fileNameIndex,elements.length-1) ){
            String name=elements[fileNameIndex].trim();
            file.setName (name);
            if( name.endsWith("/") ){ // NOI18N
                file=new VcsDir(name.substring(0,name.length()-1));
            }
        }
        int statusIndex=list.getStatus();
        if( MiscStuff.withinRange(0,statusIndex,elements.length-1) ){
            String status=elements[statusIndex].trim();
            file.setStatus (status);
        }
        int lockerIndex=list.getLocker();
        if( MiscStuff.withinRange(0,lockerIndex,elements.length-1) ){
            String locker=elements[lockerIndex].trim();
            file.setLocker (locker);
        }
        int attrIndex=list.getAttr();
        if( MiscStuff.withinRange(0,attrIndex,elements.length-1) ){
            String attr=elements[attrIndex].trim();
            file.setAttr (attr);
        }
        int dateIndex=list.getDate();
        if( MiscStuff.withinRange(0,dateIndex,elements.length-1) ){
            String date=elements[dateIndex].trim();
            file.setDate (date);
        }
        int timeIndex=list.getTime();
        if( MiscStuff.withinRange(0,timeIndex,elements.length-1) ){
            String time=elements[timeIndex].trim();
            file.setTime (time);
        }
        int sizeIndex=list.getSize();
        if( MiscStuff.withinRange(0,sizeIndex,elements.length-1) ){
            String size=elements[sizeIndex].trim();
            try {
                file.setSize (Integer.parseInt(size));
            }
            catch (NumberFormatException e) {
                file.setSize (0);
            }
        }
        //System.err.println("file="+file);
        return file;
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

}

/*
 * Log
 *  19   Gandalf-post-FCS1.17.1.0    4/4/00   Martin Entlicher setPath fix
 *  18   Gandalf   1.17        2/10/00  Martin Entlicher Automatic refresh after 
 *       last command only.
 *  17   Gandalf   1.16        1/18/00  Martin Entlicher 
 *  16   Gandalf   1.15        1/15/00  Ian Formanek    NOI18N
 *  15   Gandalf   1.14        1/6/00   Martin Entlicher 
 *  14   Gandalf   1.13        1/3/00   Martin Entlicher 
 *  13   Gandalf   1.12        12/29/99 Martin Entlicher Debug messages added.
 *  12   Gandalf   1.11        12/28/99 Martin Entlicher 
 *  11   Gandalf   1.10        12/21/99 Martin Entlicher 
 *  10   Gandalf   1.9         12/14/99 Martin Entlicher Output listener added
 *  9    Gandalf   1.8         11/30/99 Martin Entlicher 
 *  8    Gandalf   1.7         11/23/99 Martin Entlicher 
 *  7    Gandalf   1.6         10/25/99 Pavel Buzek     
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/12/99 Martin Entlicher 
 *  4    Gandalf   1.3         10/12/99 Pavel Buzek     
 *  3    Gandalf   1.2         10/10/99 Pavel Buzek     
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
