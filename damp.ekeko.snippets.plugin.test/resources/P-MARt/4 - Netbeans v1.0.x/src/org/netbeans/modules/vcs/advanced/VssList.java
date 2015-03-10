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

/** VSS list command wrapper.
 * 
 * @author Martin Entlicher
 */
//-------------------------------------------
public class VssList {
    private Debug E=new Debug("VssList", false);
    private Debug D=E;

    private String dir=null, rootdir=null;
    private String[] args=null;

    /**
     * @associates String 
     */
    private Hashtable filesByName=new Hashtable();

    private boolean shouldFail=false;


    //-------------------------------------------
    public VssList(String[] args){
        this.dir=System.getProperty("DIR","");
        this.rootdir=System.getProperty("ROOTDIR",".");
        if (dir.equals("")) dir=rootdir;
        else                dir=rootdir+File.separator+dir;
        this.args=args;
    }


    //-------------------------------------------
    private String array2string(String[] sa){
        StringBuffer sb=new StringBuffer(255);
        for(int i=0;i<sa.length;i++){
            sb.append(sa[i]+" ");
        }
        return new String(sb);
    }

    //-------------------------------------------
    private void getFiles(){
        String cmd=array2string(args);
        Hashtable vars=new Hashtable(5);
        vars.put("DIR",rootdir);
        Variables v=new Variables();
        String prepared=v.expand(vars,cmd, true);

        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(60000);
        ec.setInput("Cancel\n");

        String dataRegex="^(.*)$";
        try{
            ec.addStdoutRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stdout match:"+MiscStuff.arrayToString(elements));
                                              String line=elements[0];
                                              String subdir=line.trim();
                                              String fname;
                                              File d=new File(dir), f;
                                              int istat, ispace;

                                              int ips = subdir.indexOf("\\");
                                              int isp = subdir.indexOf(" ");
                                              if( !subdir.startsWith("$/") && subdir.indexOf(" item(s)") < 0 &&
                                                      (ips < 0 || (isp > 0 && ips > isp)) ){
                                                  if( subdir.startsWith("$") ){
                                                      fname = subdir.substring(1, subdir.length());
                                                      f = new File(d+File.separator+fname);
                                                      /*
                                                             if (f.exists()) System.out.println(fname+"/"+" Current");
                                                             else            System.out.println(fname+"/"+" Missing");
                                                      */
                                                      if (f.exists()) filesByName.put(fname+"/","Current");
                                                      else            filesByName.put(fname+"/","Missing");
                                                  }
                                                  else{
                                                      if ((istat = subdir.indexOf(" ")) < 0) {
                                                          f = new File(d+File.separator+subdir);
                                                          /*
                                                                        if (f.exists()) System.out.println(subdir+" Current");
                                                                        else            System.out.println(subdir+" Missing");
                                                          */
                                                          if (f.exists()) filesByName.put(subdir,"Current");
                                                          else            filesByName.put(subdir,"Missing");
                                                      } else {
                                                          while(subdir.charAt(++istat) == ' ');
                                                          istat = subdir.indexOf(" ", istat);
                                                          if (istat < 0) istat = subdir.length();
                                                          //System.out.println(subdir.substring(0, istat));
                                                          ispace = subdir.indexOf(" ");
                                                          if (ispace < 0) ispace = subdir.length();
                                                          filesByName.put(subdir.substring(0, ispace), subdir.substring(ispace, subdir.length()).trim());
                                                      }
                                                  }
                                              }

                                          }

                                      },dataRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex");
            System.err.println("VssList: Bad regex "+dataRegex);
            shouldFail=true ;
        }

        String errorRegex="^(.*)$";
        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements));
                                              System.err.println("VssList: "+elements[0]);
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex");
            System.err.println("VssList: Bad regex "+errorRegex);
            shouldFail=true ;
        }

        //D.deb("ec="+ec);
        if( ec.exec() != ExternalCommand.SUCCESS ){
            //E.err("exec failed "+ec.getExitStatus());
            shouldFail=true;
        }

    }

    //-------------------------------------------
    private void addLocalFiles(){
        File d=new File(dir);
        String[] files=d.list();
        if (files == null) return;
        for(int i=0;i<files.length;i++){
            String fileName=files[i];
            //D.deb("fileName="+fileName);

            if( new File(d+File.separator+fileName).isDirectory() ){
                fileName+="/";
            }
            if( filesByName.get(fileName)==null ){
                //D.deb("adding "+fileName);
                filesByName.put(fileName,"Not-in-project");
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
        getFiles();
        addLocalFiles();
        print();
        /*    if( shouldFail ){
          System.err.println("VssList: Error occured.");
    }
        System.exit( shouldFail ? 1:0 );
        */
        System.exit( 0 );
    }

    //-------------------------------------------
    public static void main(String[]args){
        if( args.length<1 ){
            System.err.println
            ("\n"+
             "Usage: java VssList [-DDIR=some/path] command [par1 par2 ...]\n"+
             "\n"+
             "Example:\n"+
             "java -DDIR=src_modules/com VssList ss dir "/*+
             "-p 'mfadljevic@nbteamsrv/Corona/Gandalf/Development/${DIR}' -rp /home/mfadljevic/ "+
             "-pwdfile /home/mfadljevic/.starteam-passwd "*/);
            System.exit(1);
        }
        VssList cmd=new VssList(args);
        cmd.runCommand();
    }

}

/*
 * <<Log>>
 *  7    Gandalf   1.6         1/19/00  Martin Entlicher 
 *  6    Gandalf   1.5         10/25/99 Pavel Buzek     copyright
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/30/99  Pavel Buzek     
 *  3    Gandalf   1.2         9/10/99  Martin Entlicher 
 *  2    Gandalf   1.1         9/8/99   Pavel Buzek     class model changed, 
 *       customization improved, several bugs fixed
 *  1    Gandalf   1.0         8/18/99  Ian Formanek    
 * $
 */
