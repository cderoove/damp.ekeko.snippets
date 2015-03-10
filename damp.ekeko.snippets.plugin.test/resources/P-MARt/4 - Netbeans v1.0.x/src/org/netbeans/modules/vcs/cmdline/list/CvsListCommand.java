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

package org.netbeans.modules.vcs.cmdline.list;

import org.netbeans.modules.vcs.cmdline.VcsListCommand;
import org.netbeans.modules.vcs.cmdline.Variables;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;

import java.io.*;
import java.util.*;
import java.beans.*;
import java.text.*;
/**
 * List command for CVS.
 * @author  Martin Entlicher
 * @version 
 */
public class CvsListCommand extends VcsListCommand implements RegexListener {

    private Debug E=new Debug("CvsListCommand",true); // NOI18N
    private Debug D=E;

    private String rootDir=null;
    private String dir=null;
    private String cmd=null;
    private String cvsRoot=null;

    private boolean shouldFail=false;

    private Vector files=new Vector(30);

    private StringBuffer dataBuffer=new StringBuffer(4096);
    //private StringBuffer errorBuffer = new StringBuffer(256);
    private NoRegexListener stdoutNRListener = null;
    private NoRegexListener stderrNRListener = null;
    private RegexListener stderrListener = null;

    private String dataRegex = null;
    private String errorRegex = null;
    private String input = null;
    private long timeout = 0;

    private static final String CVS_DIRNAME = "CVS";
    private static final String[] CVS_DIRCONTENT = {"Entries", "Repository", "Root"};

    /** Creates new CvsListCommand */
    public CvsListCommand() {
    }

    private void initVars(Hashtable vars, String[] args) {
        this.cmd = MiscStuff.array2string(args);

        this.rootDir = (String) vars.get("ROOTDIR"); // NOI18N
        if (this.rootDir == null) {
            this.rootDir = "."; // NOI18N
            //vars.put("ROOTDIR","."); // NOI18N
        }
        this.dir = (String) vars.get("DIR"); // NOI18N
        if (this.dir == null) {
            this.dir = ""; // NOI18N
            //vars.put("DIR","."); // NOI18N
        }
        String module = (String) vars.get("MODULE"); // NOI18N
        D.deb("rootDir = "+rootDir+", module = "+module+", dir = "+dir); // NOI18N
        if (dir.equals("")) { // NOI18N
            dir=rootDir;
            if (module != null && module.length() > 0) dir += File.separator + module;
        } else {
            if (module == null)
                dir=rootDir+File.separator+dir;
            else
                dir=rootDir+File.separator+module+File.separator+dir;
        }
        if (dir.charAt(dir.length() - 1) == File.separatorChar)
            dir = dir.substring(0, dir.length() - 1);
        D.deb("dir="+dir); // NOI18N

        String dataRegex = (String) vars.get("DATAREGEX"); // NOI18N
        if (dataRegex != null) this.dataRegex = dataRegex;
        String errorRegex = (String) vars.get("ERRORREGEX"); // NOI18N
        if (errorRegex != null) this.errorRegex = errorRegex;
        D.deb("dataRegex = "+dataRegex+", errorRegex = "+errorRegex); // NOI18N
        this.input = (String) vars.get("INPUT"); // NOI18N
        if (this.input == null) this.input = "Cancel/n"; // NOI18N
        this.timeout = ((Long) vars.get("TIMEOUT")).longValue(); // NOI18N
    }

    private boolean isCVSDir() {
        File d=new File(dir);
        String[] files=d.list();
        if (files != null) {
            for(int i = 0; i < files.length; i++)
                if (files[i].equals("CVS")) return true; // NOI18N
        }
        shouldFail=true ;
        return false;
    }

    //-----------------------------------
    private void runStatusCommand(Hashtable vars){

        /*
        vars.put("DIR",dir);
        vars.put("CVSROOT",cvsRoot);
        */
        Variables v=new Variables();
        String prepared=v.expand(vars,cmd, true);

        D.deb("prepared = "+prepared); // NOI18N
        D.deb("DIR = '"+(String) vars.get("DIR")+"'"+", dir = '"+this.dir+"'"); // NOI18N
        ExternalCommand ec=new ExternalCommand(prepared);
        if (stderrListener != null) {
            String[] command = { "LIST: "+prepared }; // NOI18N
            stderrListener.match(command);
        }
        if (stderrNRListener != null) stderrNRListener.match("LIST: "+prepared); // NOI18N
        ec.setTimeout(timeout);
        ec.setInput(input);

        try{
            D.deb("stdout dataRegex = "+dataRegex); // NOI18N
            ec.addStdoutRegexListener(this,dataRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex"); // NOI18N
            //errorBuffer.append("CvsList: Bad data regex "+dataRegex+"\n"); // NOI18N
            if (stderrListener != null) {
                String[] elements = { "CvsList: Bad data regex "+dataRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
            //System.err.println("CvsList: Bad regex "+dataRegex);
            shouldFail=true ;
        }

        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                              //errorBuffer.append(elements[0]+"\n"); // NOI18N
                                              //System.err.println("CvsList: stderr: "+elements[0]);
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
            if (this.stderrListener != null) ec.addStderrRegexListener(stderrListener, errorRegex);
            ec.addStderrRegexListener(this,dataRegex); // Because of "Examining" status // NOI18N
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex"); // NOI18N
            //errorBuffer.append("CvsList: Bad error regex "+errorRegex+"\n"); // NOI18N
            if (stderrListener != null) {
                String[] elements = { "CvsList: Bad error regex "+errorRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
            //System.err.println("CvsList: Bad regex "+errorRegex);
            shouldFail=true ;
        }

        if (this.stdoutNRListener != null) ec.addStdoutNoRegexListener(stdoutNRListener);
        if (this.stderrNRListener != null) ec.addStderrNoRegexListener(stderrNRListener);

        //D.deb("ec="+ec); // NOI18N
        if( ec.exec() != ExternalCommand.SUCCESS ){
            //E.err("exec failed "+ec.getExitStatus()); // NOI18N
            shouldFail=true;
        }
    }

    //------------------------------------------
    private boolean furtherExamining(String data, int index) {
        while(Character.isWhitespace(data.charAt(index))) index++;
        return (data.charAt(index) == '.');
    }

    //-------------------------------------------
    private void fillHashtable(Hashtable filesByName) {
        String data=new String(dataBuffer);
        String examiningStr = "status: Examining"; // NOI18N

        int pos=0;
        int index=0;
        /* I expect file listing in the form: File: <filename> Status: <status>
         * There has to be info line about examining directories.
         * (Regex ^(File:.*Status:.*$)|(cvs status.*)) 
         */
        int fileIndex;
        int examIndex = data.indexOf(examiningStr, pos);
        if (examIndex < 0) {
            examiningStr = "server: Examining"; // NOI18N
            examIndex = data.indexOf(examiningStr, pos);
        }
        boolean examining = true;
        if (examIndex < 0) {
            E.err("Warning: No examining info from cvs status command !"); // NOI18N
            examining = false;
        } else {
            examining = furtherExamining(data, examIndex += examiningStr.length());
            examIndex = data.indexOf(examiningStr, examIndex);
        }
        fileIndex=data.indexOf("File:",pos); // NOI18N
        while( examining && fileIndex >=0 ){
            //int fileIndex=data.indexOf("File:",pos); // NOI18N
            int statusIndex=data.indexOf("Status:",pos); // NOI18N

            int nextIndex=data.indexOf("\n",statusIndex); // NOI18N
            if( nextIndex<0 ){
                nextIndex=data.length()-1;
            }

            D.deb("fillHashtable: fileIndex = "+fileIndex+", statusIndex = "+statusIndex); // NOI18N
            if( fileIndex>=0 && statusIndex>=0 ){
                fileIndex+="File:".length(); // NOI18N
                String fileName=data.substring(fileIndex,statusIndex).trim();
                int i=-1;
                if( (i=fileName.indexOf("no file")) >=0  ){ // NOI18N
                    fileName=fileName.substring(i+7).trim();
                }
                //D.deb("fileName="+fileName); // NOI18N

                String fileDetails=data.substring(index,nextIndex);
                //D.deb("fileDetails="+fileDetails); // NOI18N

                int eolIndex=data.indexOf("\n",statusIndex); // NOI18N
                String fileStatus="Unknown"; // NOI18N

                if( statusIndex>=0 && eolIndex>=0 ){
                    statusIndex+="Status:".length(); // NOI18N
                    fileStatus=data.substring(statusIndex,eolIndex).trim();
                }
                //D.deb("fileStatus="+fileStatus); // NOI18N

                D.deb("fillHashTable: "+"fileName="+fileName+", fileStatus="+fileStatus); // NOI18N

                String[] fileStatuses = new String[2];
                fileStatuses[0] = fileName;
                fileStatuses[1] = fileStatus;
                filesByName.put(fileName,fileStatuses);
            }
            pos=nextIndex;
            fileIndex=data.indexOf("File:",pos); // NOI18N
            if (examIndex > 0 && examIndex < fileIndex) {
                examining = furtherExamining(data, examIndex += examiningStr.length());
                examIndex = data.indexOf(examiningStr, examIndex);
            }
        }
    }

    /**
    * Test if the directory was checked out by CVS or not.
    * @param dir the directory name to test
    * @return <code>true</code> if the directory was created by CVS, <code>false</code> if not.
    */
    private boolean isCVSDirectory(File dir) {
        File subdir = new File(dir, CVS_DIRNAME);
        if (!subdir.isDirectory()) return false;
        for(int i = 0; i < CVS_DIRCONTENT.length; i++) {
            File cvsFile = new File(subdir, CVS_DIRCONTENT[i]);
            if (!cvsFile.isFile()) return false;
        }
        return true;
    }

    /**
     * Add local directories with no status information.
     * @param filesByName the files container
     */
    private void addLocalFiles(Hashtable filesByName){
        File d=new File(dir);
        String[] files=d.list();
        if (files != null) {
            String[] fileStatuses = new String[2];
            fileStatuses[1] = ""; // NOI18N
            for(int i=0;i<files.length;i++){
                String fileName=files[i];
                //D.deb("fileName="+fileName); // NOI18N

                File dirfile = new File(d+File.separator+fileName);
                if( dirfile.isDirectory() ){
                    fileName+="/"; // NOI18N
                } else continue;
                if( fileName.equals(CVS_DIRNAME+"/") ){ // NOI18N
                    continue;
                }

                if (!isCVSDirectory(dirfile)) continue;
                if( filesByName.get(fileName)==null ){
                    D.deb("adding "+fileName); // NOI18N
                    fileStatuses[0] = fileName;
                    filesByName.put(fileName, fileStatuses.clone());
                }
            }
        }
    }

    //--------------------------------------------
    /**
     * List files of CVS Repository.
     * @param vars Variables used by the command
     * @param args Command-line arguments
     * filesByName listing of files with statuses
     * @param stdoutNRListener listener of the standard output of the command
     * @param stderrNRListener listener of the error output of the command
     * @param stdoutListener listener of the standard output of the command which
     *                       satisfies regex <CODE>dataRegex</CODE>
     * @param dataRegex the regular expression for parsing the standard output
     * @param stderrListener listener of the error output of the command which
     *                       satisfies regex <CODE>errorRegex</CODE>
     * @param errorRegex the regular expression for parsing the error output
     */
    public boolean list(Hashtable vars, String[] args, Hashtable filesByName,
                        NoRegexListener stdoutNRListener, NoRegexListener stderrNRListener,
                        RegexListener stdoutListener, String dataRegex,
                        RegexListener stderrListener, String errorRegex) {

        this.stdoutNRListener = stdoutNRListener;
        this.stderrNRListener = stderrNRListener;
        this.stderrListener = stderrListener;
        this.dataRegex = dataRegex;
        this.errorRegex = errorRegex;
        initVars(vars, args);
        /*if (isCVSDir())*/ runStatusCommand(vars);
        if (!shouldFail) fillHashtable(filesByName);
        addLocalFiles(filesByName);
        if (shouldFail) {
            //errorBuffer.append(allOutputBuffer.toString());
            //filesByName.put("ERROR", errorBuffer.toString()); // NOI18N
        }
        return !shouldFail;
    }

    public void match(String[] elements) {
        dataBuffer.append(elements[0]+"\n"); // NOI18N
        D.deb("match: append line '"+elements[0]+"'"); // NOI18N
    }
}

/*
 * Log
 *  26   Gandalf-post-FCS1.24.1.0    3/23/00  Martin Entlicher 
 *  25   Gandalf   1.24        3/9/00   Martin Entlicher List only directories 
 *       which are in CVS repository.
 *  24   Gandalf   1.23        2/10/00  Martin Entlicher Local directories added 
 *       with no status information.
 *  23   Gandalf   1.22        1/18/00  Martin Entlicher 
 *  22   Gandalf   1.21        1/17/00  Martin Entlicher NOI18N + parsing 
 *       examining info corrected
 *  21   Gandalf   1.20        1/15/00  Ian Formanek    NOI18N
 *  20   Gandalf   1.19        1/6/00   Martin Entlicher 
 *  19   Gandalf   1.18        12/28/99 Martin Entlicher 
 *  18   Gandalf   1.17        12/14/99 Martin Entlicher Listeners added
 *  17   Gandalf   1.16        12/8/99  Martin Entlicher 
 *  16   Gandalf   1.15        12/2/99  Martin Entlicher 
 *  15   Gandalf   1.14        12/1/99  Martin Entlicher 
 *  14   Gandalf   1.13        11/30/99 Martin Entlicher 
 *  13   Gandalf   1.12        11/10/99 Martin Entlicher 
 *  12   Gandalf   1.11        11/9/99  Martin Entlicher 
 *  11   Gandalf   1.10        11/4/99  Martin Entlicher 
 *  10   Gandalf   1.9         10/26/99 Martin Entlicher 
 *  9    Gandalf   1.8         10/26/99 Martin Entlicher 
 *  8    Gandalf   1.7         10/26/99 Martin Entlicher 
 *  7    Gandalf   1.6         10/26/99 Martin Entlicher 
 *  6    Gandalf   1.5         10/25/99 Pavel Buzek     
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/13/99 Martin Entlicher 
 *  3    Gandalf   1.2         10/10/99 Pavel Buzek     
 *  2    Gandalf   1.1         10/9/99  Martin Entlicher 
 *  1    Gandalf   1.0         10/7/99  Martin Entlicher initial revision
 * $
 */
