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

/** StarTeam 4.0 list command wrapper.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class Stcmd40List {
    private Debug E=new Debug("Stcmd40List", false);
    private Debug D=E;

    private String dir=null;
    private String[] args=null;

    /**
     * @associates String 
     */
    private Hashtable filesByName=new Hashtable();

    private boolean shouldFail=false;


    //-------------------------------------------
    public Stcmd40List(String[] args){
        this.dir=System.getProperty("DIR",".");
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
        vars.put("DIR",dir);
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
                                              if( subdir.endsWith("/") ){
                                                  /*
                                                  System.out.println("Current                   rw 01/01/99 00:00:00 PM G...        0 "+
                                                     subdir);
                                                  */
                                                  filesByName.put(subdir, "Current                   rw 01/01/99 00:00:00 PM G...        0 ");
                                              }
                                              else if ( subdir.endsWith("\\") ){
                                                  subdir=subdir.substring(0, subdir.lastIndexOf("\\"))+"/";
                                                  /*
                                                  System.out.println("Current                   rw 01/01/99 00:00:00 PM G...        0 "+
                                                       subdir);
                                                  */
                                                  filesByName.put(subdir, "Current                   rw 01/01/99 00:00:00 PM G...        0 ");
                                              }
                                              else{
                                                  if( line.indexOf("Folder:") < 0 ){
                                                      //System.out.println(line);
                                                      int fileInd = line.lastIndexOf(" ");
                                                      if (fileInd<0) fileInd = 0;
                                                      filesByName.put(line.substring(fileInd+1, line.length()), line.substring(0, fileInd).trim());
                                                  }
                                              }

                                          }

                                      },dataRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex");
            System.err.println("Stcmd40List: Bad regex "+dataRegex);
            shouldFail=true ;
        }

        String errorRegex="^(.*)$";
        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements));
                                              System.err.println("Stcmd40List: "+elements[0]);
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex");
            System.err.println("Stcmd40List: Bad regex "+errorRegex);
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
        if (d == null) return;
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
                filesByName.put(fileName,"Not-in-project            rw 01/01/99 00:00:00 PM G...        0 ");
            }
        }
    }

    //-------------------------------------------
    private void print(){
        for(Enumeration e = filesByName.keys(); e != null && e.hasMoreElements() ;) {
            String fileName=(String)e.nextElement();
            String fileStatus=(String)filesByName.get(fileName);
            System.out.println(fileStatus+" "+fileName);
        }
    }

    //-------------------------------------------
    private void runCommand(){
        getFiles();
        //addLocalFiles();
        print();
        if( shouldFail ){
            System.err.println("Stcmd40List: Error occured.");
        }
        System.exit( shouldFail ? 1:0 );

        //System.exit( 0 );
    }

    //-------------------------------------------
    public static void main(String[]args){
        if( args.length<1 ){
            System.err.println
            ("\n"+
             "Usage: java Stcmd40List [-DDIR=some/path] command [par1 par2 ...]\n"+
             "\n"+
             "Example:\n"+
             "java -DDIR=src_modules/com Stcmd40List stcmd list -cf -nologo "+
             "-p 'mfadljevic@nbteamsrv/Corona/Gandalf/Development/${DIR}' -rp /home/mfadljevic/ "+
             "-pwdfile /home/mfadljevic/.starteam-passwd ");
            System.exit(1);
        }
        Stcmd40List cmd=new Stcmd40List(args);
        cmd.runCommand();
    }

}

/*
 * <<Log>>
 *  9    Gandalf   1.8         1/19/00  Martin Entlicher 
 *  8    Gandalf   1.7         10/25/99 Pavel Buzek     copyright
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/30/99  Pavel Buzek     
 *  5    Gandalf   1.4         9/8/99   Martin Entlicher Fixed return status
 *  4    Gandalf   1.3         9/8/99   Pavel Buzek     class model changed, 
 *       customization improved, several bugs fixed
 *  3    Gandalf   1.2         8/18/99  Ian Formanek    
 *  2    Gandalf   1.1         8/7/99   Ian Formanek    Martin Entlicher's 
 *       improvements
 *  1    Gandalf   1.0         7/9/99   Michal Fadljevic initial revision
 * $
 */
