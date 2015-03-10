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
import java.io.*;
import java.util.*;
import java.beans.*;
import java.text.*;

import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.cmdline.exec.*;

/** PVCS list command wrapper
 * 
 * @author Martin Entlicher
 */

public class PvcsList implements RegexListener {
    private Debug E=new Debug("PvcsList",true);
    private Debug D=E;

    private String dir=null;
    private String rootDir=null;
    private String pvcsRoot=null;
    private String configFile=null;
    private String cmd=null;
    private String workDir="work";
    private String archiveDir="archives";
    private String archiveEnd="-arc";

    private boolean shouldFail=false;

    /**
     * @associates String 
     */
    private Hashtable filesByName=new Hashtable();


    //-------------------------------------------
    public PvcsList(String[] args){
        this.dir=System.getProperty("DIR","");
        this.pvcsRoot=System.getProperty("PVCSROOT","");
        this.rootDir=System.getProperty("ROOTDIR","."/*pvcsRoot+File.separator+workDir*/);
        pvcsRoot+=File.separator+archiveDir;
        this.configFile=System.getProperty("VCSCONFIG","vcs.cfg");
        if (configFile.indexOf(File.separator) < 0)
            configFile=rootDir+File.separator+configFile;
        this.cmd="vlog -C${CONFIGFILE} ${FILE}";
    }


    StringBuffer buffer=new StringBuffer();


    //-------------------------------------------
    public void match(String[] elements){
        buffer.append(elements[0]+"\n");
        //System.out.println("match: elements[0]="+elements[0]);
    }


    private void fillRemoteFiles() {
        File cf=new File(configFile);
        if (!cf.canRead()) {
            System.err.println("PvcsList: Can not read Configuration File "+configFile);
            shouldFail=true;
            System.exit(1);
        }
        if (!(new File(pvcsRoot)).exists()) {
            System.err.println("PvcsList: can not find the archive "+pvcsRoot);
            shouldFail=true;
            System.exit(1);
        }
        Hashtable vars = new Hashtable();
        vars.put("CONFIGFILE", configFile);
        Variables v = new Variables();
        File f = new File(pvcsRoot+File.separator+dir);
        FilenameFilter archiveFileFilter = new FilenameFilter() {
                                               public boolean accept(File dir, String name) {
                                                   return (name.endsWith(archiveEnd) || (new File(dir, name)).isDirectory());
                                               }
                                           };
        //System.out.println("Pvcs Archive = "+f);
        String[] files=f.list(archiveFileFilter);
        if (files == null) return;
        String dataRegex="^(Locks:.*)$";
        String errorRegex="^(.*)$";
        for(int i=0;i<files.length;i++){
            String fileName=files[i];
            //System.out.println("Examining file:"+fileName);
            if( new File(f+File.separator+fileName).isDirectory() ){
                fileName+="/";
                filesByName.put(fileName, "Current");
                continue;
            }
            int archInd = fileName.lastIndexOf(archiveEnd);
            if (archInd>0) fileName=fileName.substring(0, archInd);
            vars.put("FILE", fileName);
            String prepared=v.expand(vars,cmd, true);
            ExternalCommand ec=new ExternalCommand(prepared);
            ec.setTimeout(10000);
            try{
                ec.addStdoutRegexListener(this,dataRegex);
            }
            catch (BadRegexException e){
                System.err.println("PvcsList: Bad regex "+dataRegex);
                shouldFail=true ;
            }
            try{
                ec.addStderrRegexListener(new RegexListener () {
                                              public void match(String[] elements){
                                                  System.err.println("PvcsList: stderr: "+elements[0]);
                                                  //shouldFail=true ;
                                              }
                                          },errorRegex);
            }
            catch (BadRegexException e){
                System.err.println("PvcsList: Bad regex "+errorRegex);
                shouldFail=true ;
            }
            if( ec.exec() != ExternalCommand.SUCCESS ){
                shouldFail=true;
            }
            String status = new String(buffer);
            //System.out.println("fileName="+fileName+", status="+status);
            if (status != null && status.length() >= "Locks:".length()) {
                status = status.substring("Locks:".length(), status.length()).trim();
                if (status == null || status.length() <= 0) status = "Missing";
                //System.out.println("Adding to Hashtable:"+fileName+",'"+status+"'");
                filesByName.put(fileName, status);
            }
            if (buffer.length() > 0) buffer.delete(0, buffer.length());
        }
    }


    //-------------------------------------------
    private void fillLocalFiles() {
        File d=new File(rootDir+File.separator+dir);
        String[] files=d.list();

        if (files == null) return;
        for(int i=0;i<files.length;i++){
            String fileName=files[i];

            if( new File(d+File.separator+fileName).isDirectory() ){
                fileName+="/";
            }
            if( filesByName.get(fileName)==null ){
                filesByName.put(fileName,"Not-in-project");
            }else if (((String) filesByName.get(fileName)).compareTo("Missing") == 0) {
                filesByName.put(fileName, "Current");
            }
        }
    }

    //-------------------------------------------
    private void print(){
        for(Enumeration e = filesByName.keys(); e != null && e.hasMoreElements() ;) {
            String fileName=(String)e.nextElement();
            String fileStatus=(String)filesByName.get(fileName);
            System.out.println(fileName+" "+fileStatus);
        }
    }

    //-------------------------------------------
    private void runCommand(){
        fillRemoteFiles();
        fillLocalFiles();
        print();
        if( shouldFail ){
            System.err.println("PvcsList: Error occured.");
        }
        System.exit( shouldFail ? 1:0 );

        //System.exit( 0 );
    }

    //-------------------------------------------
    public static void main(String[]args){
        if( args.length<0 ){
            System.err.println
            ("\n"+
             "Usage: java PvcsList [-DDIR=some/path] [-DROOTDIR=some/path] [-DPVCSROOT=some/path] [-DVCSCONFIG=<path to vcs.cfg>] \n"+
             "\n"+
             "Example:\n"+
             "java -DDIR=src_modules/com -DROOTDIR=/home/martin -DPVCSROOT=/home/pvcs/vm/sampledb/work -DVCSCONGIG=vcs.cfg PvcsList\n");
            System.exit(1);
        }
        PvcsList cmd=new PvcsList(args);
        cmd.runCommand();
    }

}

/*
 * <<Log>>
 *  8    Gandalf   1.7         1/19/00  Martin Entlicher 
 *  7    Gandalf   1.6         10/25/99 Pavel Buzek     copyright
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/30/99  Pavel Buzek     
 *  4    Gandalf   1.3         9/15/99  Martin Entlicher Additional Error output 
 *       added
 *  3    Gandalf   1.2         9/8/99   Pavel Buzek     class model changed, 
 *       customization improved, several bugs fixed
 *  2    Gandalf   1.1         8/18/99  Ian Formanek    
 *  1    Gandalf   1.0         8/7/99   Ian Formanek    
 * $
 */
