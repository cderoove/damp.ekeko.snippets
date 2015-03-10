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

import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;

/** CVS list command.
 * @author Pavel Buzek
 */
//-------------------------------------------
public class CvsList implements RegexListener {
    private Debug E=new Debug("CvsList",true); // NOI18N
    private Debug D=E;

    private String rootDir=null;
    private String dir=null;
    private String cmd=null;
    private String cvsRoot=null;

    private boolean shouldFail=false;

    private Vector files=new Vector(30);

    /**
     * @associates String 
     */
    private Hashtable filesByName=new Hashtable(30);


    //-------------------------------------------
    public CvsList(){
        this.rootDir=System.getProperty("ROOTDIR",".");
        this.dir=System.getProperty("DIR","");

        String osName=System.getProperty("os.name");
        if( osName.indexOf("Win")>=0 ) // NOI18N
            this.cmd=System.getProperty("CMD","cmd /X /C \"set CVSROOT=:local:${CVSROOT}&& cvs status -l\"");
        else
            this.cmd=System.getProperty("CMD","sh -c \"CVSROOT=\\\"${CVSROOT}\\\"; export CVSROOT; cd \\\"${DIR}\\\"; cvs status -l\"");
        System.out.println("Command: "+cmd); // NOI18N
        this.cvsRoot=System.getProperty("CVSROOT","/home/mfadljevic/Repository");

        if(dir.equals("")){ // NOI18N
            dir=rootDir;
        }
        else{
            dir=rootDir+File.separator+dir;
        }
        D.deb("dir="+dir); // NOI18N

        if( rootDir==null || dir==null || cmd==null || cvsRoot==null ){
            System.err.println("Please set up all properties ROOTDIR, DIR, CMD, CVSROOT");
            System.err.println("e.g. java -DROOTDIR=/home/mfadljevic/wrk/p1 -DDIR=src "+
                               "-DCVSROOT=/home/mfadljevic/Repository "+ // NOI18N
                               "-DCMD='bash -c \"export CVSROOT=${CVSROOT}; cd ${DIR}; cvs status -l\" ' CvsList"); // NOI18N
            System.exit(1);
        }

    }


    StringBuffer buffer=new StringBuffer(4096);


    //-------------------------------------------
    public void match(String[] elements){
        buffer.append(elements[0]+"\n"); // NOI18N
    }


    //-------------------------------------------
    private void runStatusCommand(){
        Hashtable vars=new Hashtable(5);
        vars.put("DIR",dir); // NOI18N
        vars.put("CVSROOT",cvsRoot); // NOI18N

        Variables v=new Variables();
        String prepared=v.expand(vars,cmd, true);

        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(10000);

        String dataRegex="^(.*)$"; // NOI18N
        try{
            ec.addStdoutRegexListener(this,dataRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex"); // NOI18N
            System.err.println("CvsList: Bad regex "+dataRegex);
            shouldFail=true ;
        }


        String errorRegex="^(.*)$"; // NOI18N
        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                              System.err.println("CvsList: stderr: "+elements[0]);
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex"); // NOI18N
            System.err.println("CvsList: Bad regex "+errorRegex);
            shouldFail=true ;
        }


        //D.deb("ec="+ec); // NOI18N
        if( ec.exec() != ExternalCommand.SUCCESS ){
            //E.err("exec failed "+ec.getExitStatus()); // NOI18N
            shouldFail=true;
        }
    }


    //-------------------------------------------
    private void fillHashtable(){
        String data=new String(buffer);

        int pos=0;
        int index=0;
        while( (index=data.indexOf("====",pos)) >=0 ){ // NOI18N
            int fileIndex=data.indexOf("File:",pos); // NOI18N
            int statusIndex=data.indexOf("Status:",pos); // NOI18N

            int nextIndex=data.indexOf("====",statusIndex); // NOI18N
            if( nextIndex<0 ){
                nextIndex=data.length()-1;
            }

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

                //D.deb("fileName="+fileName+", fileStatus="+fileStatus); // NOI18N

                filesByName.put(fileName,fileStatus);
            }
            pos=nextIndex;
        }
    }


    //-------------------------------------------
    private void addLocalFiles(){
        File d=new File(dir);
        String[] files=d.list();
        for(int i=0;i<files.length;i++){
            String fileName=files[i];
            //D.deb("fileName="+fileName); // NOI18N

            if( new File(d+File.separator+fileName).isDirectory() ){
                fileName+="/"; // NOI18N
            }
            if( fileName.equals("CVS/") ){ // NOI18N
                continue;
            }

            if( filesByName.get(fileName)==null ){
                //D.deb("adding "+fileName); // NOI18N
                filesByName.put(fileName,"Not-in-project"); // NOI18N
            }
        }
    }


    //-------------------------------------------
    private void print(){
        //D.deb("filesByName="+filesByName); // NOI18N
        for(Enumeration e = filesByName.keys(); e.hasMoreElements() ;) {
            String fileName=(String)e.nextElement();
            String fileStatus=(String)filesByName.get(fileName);
            System.out.println(fileName+" "+fileStatus); // NOI18N
        }
    }


    //-------------------------------------------
    private void runCommand(){
        runStatusCommand();
        fillHashtable();
        addLocalFiles();
        print();
        System.exit(0);
    }


    //-------------------------------------------
    public static void main(String[]args){
        CvsList cmd=new CvsList();
        cmd.runCommand();
    }


}

/*
 * Log
 *  6    Gandalf   1.5         1/18/00  Martin Entlicher 
 *  5    Gandalf   1.4         1/15/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         1/6/00   Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
