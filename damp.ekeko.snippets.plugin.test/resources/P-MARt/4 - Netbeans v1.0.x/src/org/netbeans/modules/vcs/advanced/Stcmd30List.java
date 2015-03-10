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

/** StarTeam 3.0 list command wrapper.
 * This command should be used instead of 'stcmd30 list' command,
 * since 'stcmd30 list' does not report direct subdirs.
 * The only way how to ask him for subdirs is to request 'list'
 * command on some fake subdir and listen on 'stderr'.
 * 
 * <p>
 * Study the getSubdirs() method for more.
 * <p>
 * Sure Unix users can use simple 'stcmd30-list.sh' shell script with 
 * similar to this: <pre>
#!/bin/sh
DIR=$1

# Get the subdirs first:
FILES=`echo Cancel| stcmd30 list -nologo -p "mfadljevic@filesrv/Corona/Gandalf/Development/${DIR}/fakedir" -rp /home/mfadljevic/ -pwdfile ~/.starteam-passwd 2>&1 |grep -v -e "Error occurred\|subfolder:\|Existing subfolders\|Respond"`
for F in ${FILES} ;do
    echo "Current                   rw 01/01/99 00:00:00 PM G...        0 $F/"
done

# Now get the files:
stcmd30 list -nologo -p "mfadljevic@filesrv/Corona/Gandalf/Development/${DIR}" -rp /home/mfadljevic/ -pwdfile ~/.starteam-passwd 2>&1 | grep -v Folder:

#end of file
</pre>
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class Stcmd30List {
    private Debug E=new Debug("Stcmd30List", false);
    //private Debug D=E;

    private String dir=null;
    private String[] args=null;

    private boolean shouldFail=false;


    //-------------------------------------------
    public Stcmd30List(String[] args){
        this.dir=System.getProperty("DIR","");
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
    private void getSubdirs(){
        String cmd=array2string(args);
        Hashtable vars=new Hashtable(5);
        String fakeDir=dir+ (dir.equals("")?"":"/") + "reallyfakedir";
        vars.put("DIR",fakeDir);
        Variables v=new Variables();
        String prepared=v.expand(vars,cmd, true);

        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(60000);
        ec.setInput("Cancel\n");

        String errorRegex="^(.*)$";
        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements));
                                              String line=elements[0];
                                              if( line.indexOf("Error occurred")<0 &&
                                                      line.indexOf("subfolder:")<0 &&
                                                      line.indexOf("Existing subfolders:")<0 ){
                                                  String subdir=line.trim();
                                                  System.out.println("Current                   rw 01/01/99 00:00:00 PM G...        0 "+
                                                                     subdir+"/");
                                              }
                                          }
                                      },errorRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex");
            System.err.println("Stcmd30List: Bad regex "+errorRegex);
        }

        //D.deb("ec="+ec);
        if( ec.exec() != ExternalCommand.SUCCESS ){
            //...this always fails, but it is ok
        }

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
                                              if( line.indexOf("Folder:")<0 ){
                                                  System.out.println(line);
                                              }
                                          }
                                      },dataRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex");
            System.err.println("Stcmd30List: Bad regex "+dataRegex);
            shouldFail=true ;
        }

        String errorRegex="^(.*)$";
        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements));
                                              System.err.println("Stcmd30List: "+elements[0]);
                                              shouldFail=true ;
                                          }
                                      },errorRegex);
        }
        catch (BadRegexException e){
            //E.err(e,"bad regex");
            System.err.println("Stcmd30List: Bad regex "+errorRegex);
            shouldFail=true ;
        }

        //D.deb("ec="+ec);
        if( ec.exec() != ExternalCommand.SUCCESS ){
            //E.err("exec failed "+ec.getExitStatus());
            shouldFail=true;
        }

    }

    //-------------------------------------------
    private void runCommand(){
        getFiles();
        if( shouldFail ){
            return ;
        }
        getSubdirs();
        if( shouldFail ){
            System.err.println("Stcmd30List: Error occured.");
        }
        System.exit( shouldFail ? 1:0 );
    }

    //-------------------------------------------
    public static void main(String[]args){
        if( args.length<1 ){
            System.err.println
            ("\n"+
             "Usage: java Stcmd30List [-DDIR=some/path] command [par1 par2 ...]\n"+
             "\n"+
             "Example:\n"+
             "java Stcmd30List -DDIR=src_modules/com stcmd30 list -nologo "+
             "-p 'mfadljevic@filesrv/Corona/Gandalf/Development/${DIR}' -rp /home/mfadljevic/ "+
             "-pwdfile /home/mfadljevic/.starteam-passwd ");
            System.exit(1);
        }
        Stcmd30List cmd=new Stcmd30List(args);
        cmd.runCommand();
    }

}

/*
 * <<Log>>
 *  11   Gandalf   1.10        1/19/00  Martin Entlicher 
 *  10   Gandalf   1.9         10/25/99 Pavel Buzek     copyright
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         9/30/99  Pavel Buzek     
 *  7    Gandalf   1.6         9/8/99   Pavel Buzek     class model changed, 
 *       customization improved, several bugs fixed
 *  6    Gandalf   1.5         8/31/99  Pavel Buzek     
 *  5    Gandalf   1.4         6/1/99   Michal Fadljevic 
 *  4    Gandalf   1.3         5/24/99  Michal Fadljevic 
 *  3    Gandalf   1.2         5/14/99  Michal Fadljevic 
 *  2    Gandalf   1.1         5/13/99  Michal Fadljevic 
 *  1    Gandalf   1.0         5/6/99   Michal Fadljevic 
 * $
 */
