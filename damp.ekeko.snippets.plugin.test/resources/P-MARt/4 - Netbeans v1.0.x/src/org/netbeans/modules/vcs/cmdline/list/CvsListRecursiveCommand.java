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

import org.netbeans.modules.vcs.cmdline.VcsListRecursiveCommand;
import org.netbeans.modules.vcs.cmdline.Variables;
import org.netbeans.modules.vcs.VcsDirContainer;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;
import org.netbeans.modules.vcs.cmdline.commands.CvsModuleParser;

import java.io.*;
import java.util.*;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CvsListRecursiveCommand extends VcsListRecursiveCommand implements RegexListener {

    private Debug E=new Debug("CvsListRecursiveCommand",true); // NOI18N
    private Debug D=E;

    private static final String[] examiningStrs = {"status: Examining", "server: Examining"}; // NOI18N

    private String rootDir = null;
    private String dir = null;
    private String dirPath = null;
    private String cmd = null;
    private String cvsRoot = null;
    private String cvsRepository = null;
    private String relMount = null;

    private boolean shouldFail = false;

    private StringBuffer dataBuffer=new StringBuffer(4096);
    //private StringBuffer errorBuffer = new StringBuffer(256);
    private NoRegexListener stdoutNRListener = null;
    private NoRegexListener stderrNRListener = null;
    private RegexListener stderrListener = null;

    private String dataRegex = null;
    private String errorRegex = null;
    private String input = null;
    private long timeout = 0;
    //private Hashtable modulesPaths = new Hashtable();
    private CvsModuleParser moduleParser = new CvsModuleParser();
    private StringBuffer moduleDefs = new StringBuffer();

    /**
     * @associates String 
     */
    private Vector examiningPaths = new Vector();
    private String lastPathConverted = null;
    private boolean lastPathFileDependent = false;
    private String lastWorkingPath = null;

    /*
    private VcsDirContainer filesByNameCont = null;
    private VcsDirContainer filesByNameContPath = null;
    private Hashtable filesByName = null;
    */

    /** Creates new CvsListRecursiveCommand */
    public CvsListRecursiveCommand() {
    }

    private void initVars(Hashtable vars, String[] args) {
        this.cmd = MiscStuff.array2string(args);

        this.rootDir = (String) vars.get("ROOTDIR"); // NOI18N
        if (this.rootDir == null) {
            this.rootDir = "."; // NOI18N
            //vars.put("ROOTDIR","."); // NOI18N
        }
        this.cvsRepository = (String) vars.get("CVS_REPOSITORY");
        if (this.cvsRepository == null) {
            this.cvsRepository = "";
        }
        cvsRepository = cvsRepository.replace('\\', '/');
        this.dir = (String) vars.get("DIR"); // NOI18N
        if (this.dir == null) {
            this.dir = ""; // NOI18N
            //vars.put("DIR","."); // NOI18N
        }
        this.dirPath = new String(dir.replace(java.io.File.separatorChar, '/')); // I have to be sure that I make new object
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
        if (module.length() > 0) this.relMount = "/"+module.replace('\\', '/');
        else relMount = "";
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
    /*
      private void addModulePath(String moduleDef) {
        int index = moduleDef.indexOf(' ');
        if (index < 0) return;
        int len = moduleDef.length();
        String moduleName = moduleDef.substring(0, index);
        String moduleDir = moduleName;
        while(true) {
          while(index < len && Character.isWhitespace(moduleDef.charAt(index))) index++;
          if (index >= len) return;
          if (moduleDef.regionMatches(index, "-a", 0, "-a".length()) return;
          if (moduleDef.regionMatches(index, "-d", 0, "-d".length()) {
            index += "-d".length();
            while(index < len && Character.isWhitespace(moduleDef.charAt(index))) index++;
            if (index >= len) return;
            int index2 = moduleDef.indexOf(' ', index);
            if (index2 < 0) return;
            moduleDir = moduleDef.substring(index, index2);
          }
          if (moduleDef.charAt(index) == '-') { // an other option
            index += 2;
            while(index < len && Character.isWhitespace(moduleDef.charAt(index))) index++;
            if (index >= len) return;
            int index2 = moduleDef.indexOf(' ', index);
            if (index2 < 0) return;
            index = index2;
          } else break;
        }
        D.deb("Found module going into directory "+moduleDir);
        
      }
    */
    
    private void addModuleDefs() {
        String defs = moduleDefs.toString();
        int lastIndex = 0;
        int beginIndex = 0;
        int index = defs.indexOf('\n', lastIndex);
        while (index >= 0) {
            index++;
            if (defs.length() <= index || !Character.isWhitespace(defs.charAt(index))) {
                D.deb("addModule("+defs.substring(beginIndex, index).replace('\n', ' ')+")");
                moduleParser.addModule(defs.substring(beginIndex, index).replace('\n', ' '));
                beginIndex = index;
            }
            lastIndex = index;
            index = defs.indexOf('\n', lastIndex);
        }
        moduleParser.resolveModuleLinks();
    }

    private void getModulesPaths(Hashtable vars) {
        Variables v=new Variables();
        String cmd = "${RUN} \\\"${CVS_EXE}\\\" checkout -c ${NUR}";
        String prepared=v.expand(vars,cmd, true);
        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(timeout);
        String mDataRegex = "^(.*)$";
        try{
            D.deb("stdout dataRegex = "+mDataRegex); // NOI18N
            ec.addStdoutRegexListener(new RegexListener () {
                                          public void match(String[] elements) {
                                              //D.deb("getModulesPaths() match = "+elements[0]);
                                              //moduleParser.addModule(elements[0]);
                                              moduleDefs.append(elements[0]+"\n");
                                          }
                                      },mDataRegex);
        }
        catch (BadRegexException e){
            if (stderrListener != null) {
                String[] elements = { "CvsList: Bad data regex "+mDataRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
            shouldFail=true ;
        }

        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
            if (this.stderrListener != null) ec.addStderrRegexListener(stderrListener, errorRegex);
            //ec.addStderrRegexListener(this,dataRegex); // Because of "Examining" status // NOI18N
        }
        catch (BadRegexException e){
            if (stderrListener != null) {
                String[] elements = { "CvsList: Bad error regex "+errorRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
            shouldFail=true ;
        }

        if( ec.exec() != ExternalCommand.SUCCESS ){
            shouldFail=true;
        } else {
            addModuleDefs();
        }

    }

    //-----------------------------------
    private void runStatusCommand(Hashtable vars){
        Variables v=new Variables();
        String prepared=v.expand(vars,cmd, true);

        D.deb("prepared = "+prepared); // NOI18N
        D.deb("DIR = '"+(String) vars.get("DIR")+"'"+", dir = '"+this.dir+"'"); // NOI18N
        ExternalCommand ec=new ExternalCommand(prepared);
        if (stderrListener != null) {
            String[] command = { "LIST_SUB: "+prepared }; // NOI18N
            stderrListener.match(command);
        }
        if (stderrNRListener != null) stderrNRListener.match("LIST_SUB: "+prepared); // NOI18N
        ec.setTimeout(timeout);
        ec.setInput(input);

        try{
            D.deb("stdout dataRegex = "+dataRegex); // NOI18N
            ec.addStdoutRegexListener(this,dataRegex);
        }
        catch (BadRegexException e){
            if (stderrListener != null) {
                String[] elements = { "CvsList: Bad data regex "+dataRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
            shouldFail=true ;
        }

        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
            if (this.stderrListener != null) ec.addStderrRegexListener(stderrListener, errorRegex);
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements) {
                                              if (elements[0] == null || elements[0].length() == 0) return;
                                              int index = -1;
                                              for(int i = 0; i < examiningStrs.length; i++) {
                                                  D.deb("Comparing elements[0] = "+elements[0]+" to examining = "+examiningStrs[i]);
                                                  index = elements[0].indexOf(examiningStrs[i]);
                                                  if (index >= 0) {
                                                      index += examiningStrs[i].length();
                                                      break;
                                                  }
                                                  D.deb("Comp. unsuccessfull");
                                              }
                                              if (index >= 0) {
                                                  while (index < elements[0].length() && Character.isWhitespace(elements[0].charAt(index))) index++;
                                                  String path = elements[0].substring(index);
                                                  if (path.equals(".")) path = "";
                                                  D.deb("Got examining: "+path);
                                                  examiningPaths.add(path);
                                              }
                                          }
                                      }, dataRegex);
            //ec.addStderrRegexListener(this,dataRegex); // Because of "Examining" status // NOI18N
        }
        catch (BadRegexException e){
            if (stderrListener != null) {
                String[] elements = { "CvsList: Bad error regex "+errorRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
            shouldFail=true ;
        }

        if (this.stdoutNRListener != null) ec.addStdoutNoRegexListener(stdoutNRListener);
        if (this.stderrNRListener != null) ec.addStderrNoRegexListener(stderrNRListener);

        if( ec.exec() != ExternalCommand.SUCCESS ){
            shouldFail=true;
        }
    }


    /**
     * Get the path of file from the output information at given index.
     * @param data the output data
     * @param index the index to the file information
     */
    private String getFilePath(String data, int index, String fileName) {
        int begin = index;
        while(Character.isWhitespace(data.charAt(begin))) begin++; // skip the space
        while(!Character.isWhitespace(data.charAt(begin))) begin++; // skip the revision number
        while(Character.isWhitespace(data.charAt(begin))) begin++; // skip the space
        int end = data.indexOf('\n', begin);
        D.deb("getFilePath(): end = "+end);
        if (end < 0) return null;
        String path = data.substring(begin, end);
        D.deb("getFilePath(): path = "+path);
        int nameIndex = path.lastIndexOf('/');
        D.deb("nameIndex = "+nameIndex);
        if (nameIndex < 0) return null;
        if (nameIndex == 0) return (cvsRepository.length() > 0) ? null : "";
        path = path.substring(0, nameIndex);
        index = path.indexOf(cvsRepository/*+relMount*/);
        D.deb(index+" = "+path+".indexOf("+cvsRepository+"+"+relMount+")");
        if (index < 0) return null;
        D.deb("getFilePath(): path = "+path+", index = "+index+", cvsRepository = "+cvsRepository);
        if (path.length() <= cvsRepository.length()/* + relMount.length()*/)
            return "";
        else {
            path = path.substring(index + cvsRepository.length() + 1);
            String working = null;
            if (!lastPathFileDependent && lastPathConverted != null && path.equals(lastPathConverted)) {
                working = lastWorkingPath;
            } else {
                boolean[] fileDependent = new boolean[1];
                fileDependent[0] = lastPathFileDependent;
                String[] workings = moduleParser.convertRepPathToWorking(path, fileName, fileDependent);
                if (workings != null) {
                    D.deb("getFilePath(): got workings = "+MiscStuff.arrayToString(workings));
                    String relMount0 = relMount+((dirPath.length() == 0) ? "" : "/"+dirPath);
                    if (relMount0.length() > 0) {
                        if (relMount0.charAt(0) == '/') relMount0 = relMount0.substring(1);
                        relMount0 += "/";
                    }
                    for(int i = 0; i < workings.length; i++) {
                        for(Enumeration enum = examiningPaths.elements(); enum.hasMoreElements(); ) {
                            String exPath = relMount0 + (String) enum.nextElement();
                            if (exPath.endsWith("/")) exPath = exPath.substring(0, exPath.length() - 1);
                            D.deb("Comparing working = "+workings[i]+" to exPath = "+exPath);
                            if (workings[i].equals(exPath)) {
                                working = workings[i];
                                break;
                            }
                        }
                        if (working != null) break;
                    }
                }
                lastPathFileDependent = fileDependent[0];
                lastPathConverted = path;
                lastWorkingPath = working;
                //return working;
            }
            if (working == null) working = path;
            D.deb("Have working = "+working+", return "+working.substring(relMount.length()));
            return working.substring(relMount.length());
        }
    }


    /**
     * Get files and their statuses from the command output.
     * @param filesByNameCont the container of files.
     */
    private void fillHashtable(VcsDirContainer filesByNameCont) {
        String data=new String(dataBuffer);
        Hashtable filesByName = new Hashtable();
        VcsDirContainer filesByNameContPath = filesByNameCont;
        String last_filePath = filesByNameContPath.getPath();
        int pos=0;
        int index=0;
        /* I expect file listing in the form: File: <filename> Status: <status>
         * Followed by Repository Revision: <revision path>
         * I suppose that revision path is the same as the working path.
         * (Regex ^(File:.*Status:.*$)|(Repository Revision.*)) 
         */
        filesByNameCont.setPath(dirPath);
        filesByName = new Hashtable();
        filesByNameCont.setElement(filesByName);
        while(pos < data.length()) {
            //int examIndex = getExaminingInfo(data, pos);
            int fileIndex=data.indexOf("File:",pos); // NOI18N
            int statusIndex=data.indexOf("Status:",pos); // NOI18N
            if (fileIndex < 0 || statusIndex < 0) {
                pos = data.length();
                continue;
            }
            int nextIndex=data.indexOf("\n",statusIndex); // NOI18N
            if (nextIndex < 0) {
                nextIndex = data.length()-1;
            }
            //D.deb("fillHashtable: fileIndex = "+fileIndex+", statusIndex = "+statusIndex); // NOI18N
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
            int repositoryIndex = data.indexOf("Repository revision:", statusIndex);
            if (repositoryIndex < 0) {
                pos = data.length();
                continue;
            }
            repositoryIndex += "Repository revision:".length();
            String filePath = getFilePath(data, repositoryIndex, fileName);
            D.deb("fillHashtable(): have filePath = "+filePath);
            if (filePath != null && !filePath.equals(last_filePath)) {
                VcsDirContainer parent = filesByNameCont.getParent(filePath);
                if (parent != null) filesByNameContPath = parent.addSubdir(filePath);
                else filesByNameContPath = filesByNameCont.addSubdirRecursive(filePath);
                D.deb("parent = "+parent+((parent == null) ? "" : " path = "+parent.getPath()));
                addDirName(filePath, filesByNameCont);
                filesByName = (Hashtable) filesByNameContPath.getElement();
                if (filesByName == null) {
                    filesByName = new Hashtable();
                    filesByNameContPath.setElement(filesByName);
                }
                D.deb("created new Container with path: "+filePath);
                last_filePath = filePath;
            }

            //D.deb("fillHashTable: "+"fileName="+fileName+", fileStatus="+fileStatus); // NOI18N

            String[] fileStatuses = new String[2];
            fileStatuses[0] = fileName;
            fileStatuses[1] = fileStatus;
            filesByName.put(fileName,fileStatuses);
            pos = repositoryIndex;
            //fileIndex=data.indexOf("File:",pos); // NOI18N
            //if (examIndex > 0 && examIndex < fileIndex) {
            //examining = furtherExamining(data, examIndex += examiningStr.length());
            //examIndex = data.indexOf(examiningStr, examIndex);
            //}
        }
    }

    /**
     * Add the directory name to the proper container. Process the directory path recursively if necessary.
     * @param filePath the directory full path
     */
    private void addDirName(String filePath, VcsDirContainer filesByNameCont) {
        if (filePath.length() == 0) return;
        String[] fileStatuses = new String[2];
        String dirName = MiscStuff.getFileNamePart(filePath) + "/";
        String dirPath = MiscStuff.getDirNamePart(filePath);
        D.deb("dirName = "+dirName+", dirPath = "+dirPath);
        fileStatuses[0] = dirName;
        fileStatuses[1] = "";
        VcsDirContainer dirParent = filesByNameCont.getContainerWithPath(dirPath);
        if (dirParent == null) {
            //E.err("dirParent = null should NOT happen.");
            // parent is somewhere out, don't care about this.
            return;
        } else {
            Hashtable filesByName = (Hashtable) dirParent.getElement();
            D.deb("Adding dir '"+dirName+"' to container with path "+dirParent.getPath());
            if (filesByName == null) {
                filesByName = new Hashtable();
                dirParent.setElement(filesByName);
            } else {
                if (filesByName.get(dirName) != null) return; // the directory is already there
            }
            filesByName.put(dirName, fileStatuses);
            if (dirParent == filesByNameCont) return;
            addDirName(dirPath, filesByNameCont); // We have to ensure that all subdirectories are there
        }
    }

    /**
     * Add local directories with no status information.
     * @param filesByName the files container
     */
    private void addLocalFiles(String dir, VcsDirContainer filesByNameCont) {
        File d = new File(dir);
        String[] files = d.list();
        Hashtable filesByName = (Hashtable) filesByNameCont.getElement();
        if (filesByName == null) {
            filesByName = new Hashtable();
            filesByNameCont.setElement(filesByName);
        }
        if (files != null) {
            String[] fileStatuses = new String[2];
            fileStatuses[1] = ""; // NOI18N
            for(int i=0;i<files.length;i++){
                String fileName=files[i];
                //D.deb("fileName="+fileName); // NOI18N

                if( new File(d+File.separator+fileName).isDirectory() ){
                    fileName+="/"; // NOI18N
                } else continue;
                if( fileName.equals("CVS/") ){ // NOI18N
                    continue;
                }

                if( filesByName.get(fileName)==null ){
                    //D.deb("addLocalFiles: adding "+fileName+", to Container with path = "+filesByNameCont.getPath()); // NOI18N
                    fileStatuses[0] = fileName;
                    filesByName.put(fileName, fileStatuses.clone());
                }
                VcsDirContainer subdir = filesByNameCont.getDirContainer(files[i]);
                if (subdir == null) subdir = filesByNameCont.addSubdir(filesByNameCont.getPath()+"/"+files[i]);
                //D.deb("addLocalFiles: call add of "+dir+File.separator+files[i]+" to container "+subdir.getPath());
                addLocalFiles(dir+File.separator+files[i], subdir);
            }
        }
    }

    /**
     * List files of CVS Repository recursively.
     * @param vars Variables used by the command
     * @param args Command-line arguments
     * @param filesByNameCont listing of files with statuses. For each directory there is a <code>Hashtable</code>
     *                        with files.
     * @param stdoutNRListener listener of the standard output of the command
     * @param stderrNRListener listener of the error output of the command
     * @param stdoutListener listener of the standard output of the command which
     *                       satisfies regex <CODE>dataRegex</CODE>
     * @param dataRegex the regular expression for parsing the standard output
     * @param stderrListener listener of the error output of the command which
     *                       satisfies regex <CODE>errorRegex</CODE>
     * @param errorRegex the regular expression for parsing the error output
     */
    public boolean listRecursively(Hashtable vars, String[] args, VcsDirContainer filesByNameCont,
                                   NoRegexListener stdoutNRListener, NoRegexListener stderrNRListener,
                                   RegexListener stdoutListener, String dataRegex,
                                   RegexListener stderrListener, String errorRegex) {

        this.stdoutNRListener = stdoutNRListener;
        this.stderrNRListener = stderrNRListener;
        this.stderrListener = stderrListener;
        this.dataRegex = dataRegex;
        this.errorRegex = errorRegex;
        initVars(vars, args);
        /*
        this.filesByNameCont = filesByNameCont;
        this.filesByNameContPath = filesByNameCont;
        this.filesByName = new Hashtable();
        */
        getModulesPaths(vars);
        runStatusCommand(vars);
        /*if (!shouldFail)*/ fillHashtable(filesByNameCont);
        //addLocalFiles(dir, filesByNameCont);
        return !shouldFail;
    }

    /**
     * Matches the standard output of the command.
     * @param elements a line of output
     */
    public void match(String[] elements) {
        dataBuffer.append(elements[0]+"\n"); // NOI18N
        D.deb("match: append line '"+elements[0]+"'"); // NOI18N
    }
}