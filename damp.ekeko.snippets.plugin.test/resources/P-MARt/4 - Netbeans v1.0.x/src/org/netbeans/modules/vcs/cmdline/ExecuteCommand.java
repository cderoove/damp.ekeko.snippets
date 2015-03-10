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

import org.netbeans.modules.vcs.cmdline.exec.*;
import org.netbeans.modules.vcs.*;
import org.netbeans.modules.vcs.util.*;
import org.openide.util.*;

/** Execute command.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class ExecuteCommand extends Thread {
    private Debug E=new Debug("ExecuteCommand", true); // NOI18N
    private Debug D=E;

    private VcsFileSystem fileSystem=null;
    private UserCommand cmd=null;
    private Hashtable vars=null;

    private RegexListener stdoutListener=null;
    private RegexListener stderrListener=null;

    private NoRegexListener stdoutNoRegexListener=null;
    private NoRegexListener stderrNoRegexListener=null;

    private OutputContainer errorContainer = null;

    private int exitStatus=0;


    //-------------------------------------------
    public ExecuteCommand(VcsFileSystem fileSystem, UserCommand cmd, Hashtable vars){
        super("VCS-ExecuteCommand-"+cmd.getName()); // NOI18N
        this.fileSystem=fileSystem;
        this.cmd=cmd;
        this.vars=vars;
    }


    //-------------------------------------------
    public void setErrorContainer(OutputContainer errorContainer) {
        this.errorContainer = errorContainer;
    }

    //-------------------------------------------
    public OutputContainer getErrorContainer() {
        return this.errorContainer;
    }

    //-------------------------------------------
    public void setOutputListener(RegexListener listener){
        stdoutListener=listener;
    }


    //-------------------------------------------
    public void setErrorListener(RegexListener listener){
        stderrListener=listener;
    }


    //-------------------------------------------
    public void setOutputNoRegexListener(NoRegexListener listener){
        stdoutNoRegexListener=listener;
    }


    //-------------------------------------------
    public void setErrorNoRegexListener(NoRegexListener listener){
        stderrNoRegexListener=listener;
    }


    //-------------------------------------------
    public int getExitStatus(){
        return exitStatus;
    }


    //-------------------------------------------
    /**
     * Execute a command-line command.
     */
    private void runCommand(String exec){
        E.deb("runCommand: "+exec); // NOI18N
        //D.deb("run("+cmd.getName()+")"); // NOI18N
        //String exec=cmd.getExec();
        //fileSystem.debug(cmd.getName()+": "+exec); // NOI18N

        Variables v=new Variables();
        exec=v.expand(vars,exec, true);

        //fileSystem.debugClear();
        fileSystem.debug(cmd.getName()+": "+exec); // NOI18N
        if (stdoutNoRegexListener != null) stdoutNoRegexListener.match(cmd.getName()+": "+exec); // NOI18N

        ExternalCommand ec=new ExternalCommand(exec);
        ec.setTimeout(cmd.getTimeout());
        ec.setInput(cmd.getInput());
        //D.deb(cmd.getName()+".getInput()='"+cmd.getInput()+"'"); // NOI18N

        String dataRegex=cmd.getDataRegex();
        try{
            ec.addStdoutRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stdout match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                              //fileSystem.debug(cmd.getName()+":stdout: "+MiscStuff.arrayToString(elements)); // NOI18N
                                              if( stdoutListener!=null ){
                                                  stdoutListener.match(elements);
                                              }
                                          }
                                      },dataRegex);
        }
        catch (BadRegexException e){
            E.err(e,"bad regex"); // NOI18N
        }

        String errorRegex=cmd.getErrorRegex();
        try{
            ec.addStderrRegexListener(new RegexListener () {
                                          public void match(String[] elements){
                                              //D.deb("stderr match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                              if (!cmd.getDisplayOutput()) {
                                                  fileSystem.debug(cmd.getName()+":stderr: "+MiscStuff.arrayToString(elements)); // NOI18N
                                              }
                                              if( stderrListener!=null ){
                                                  stderrListener.match(elements);
                                              }
                                          }
                                      },errorRegex);
        }
        catch (BadRegexException e){
            E.err(e,"bad regex"); // NOI18N
        }

        if (stdoutNoRegexListener != null) ec.addStdoutNoRegexListener(stdoutNoRegexListener);
        if (stderrNoRegexListener != null) ec.addStderrNoRegexListener(stdoutNoRegexListener);

        E.deb("ec="+ec); // NOI18N
        exitStatus=ec.exec();
        E.deb("Command exited with exit status = "+exitStatus); // NOI18N
        D.deb("errorContainer = "+errorContainer); // NOI18N
        switch (exitStatus){
        case ExternalCommand.SUCCESS:
            fileSystem.debug(cmd.getName()+": "+g("MSG_Command_succeeded")); // NOI18N
            //if( fileSystem.isAdditionalCommand(cmd.getName())==false ){
            if(cmd.getDoRefresh() && fileSystem.getDoAutoRefresh((String)vars.get("DIR"))) { // NOI18N
                //D.deb("Now refresh folder after CheckIn,CheckOut,Lock,Unlock... commands for convenience"); // NOI18N
                fileSystem.setAskIfDownloadRecursively(false); // do not ask if using auto refresh
                fileSystem.getCache().refreshDir((String)vars.get("DIR")); // NOI18N
            }
            if (!cmd.getDoRefresh()) fileSystem.removeNumDoAutoRefresh((String)vars.get("DIR")); // NOI18N
            fileSystem.setLastCommandState(true);
            //if (errorContainer != null) errorDialog.removeCommandOut(); //cancelDialog(); -- not necessary
            break;
        case ExternalCommand.FAILED_ON_TIMEOUT:
            fileSystem.debug(cmd.getName()+": "+g("MSG_Timeout")); // NOI18N
            if (errorContainer != null) errorContainer.match(cmd.getName()+": "+g("MSG_Timeout")); // NOI18N
        case ExternalCommand.FAILED:
            //D.deb("exec failed "+ec.getExitStatus()); // NOI18N
            fileSystem.debug(cmd.getName()+": "+g("MSG_Command_failed")); // NOI18N
            if (errorContainer != null) errorContainer.match(cmd.getName()+": "+g("MSG_Command_failed")); // NOI18N
            ErrorCommandDialog errorDialog = fileSystem.getErrorDialog();
            if (errorDialog != null && errorContainer != null) {
                errorDialog.putCommandOut(errorContainer);
                errorDialog.showDialog();
            }
            fileSystem.removeNumDoAutoRefresh((String)vars.get("DIR")); // NOI18N
            fileSystem.setLastCommandState(false);
            break;
        }

        D.deb("run("+cmd.getName()+") finished"); // NOI18N
    }

    /**
     * Loads class of given name with some arguments and execute its list() method.
     * @param className the name of the class to be loaded
     * @param tokens the arguments
     */
    private void runClass(String className, StringTokenizer tokens) {

        E.deb("runClass: "+className); // NOI18N
        boolean success = true;
        Class execClass = null;
        try {
            execClass =  Class.forName(className, true,
                                       org.openide.TopManager.getDefault().currentClassLoader());
        } catch (ClassNotFoundException e) {
            fileSystem.debug ("EXEC: " + g("ERR_ClassNotFound", className)); // NOI18N
            if (stderrNoRegexListener != null)
                stderrNoRegexListener.match("EXEC: " + g("ERR_ClassNotFound", className)); // NOI18N
            success = false;
            return;
        }
        D.deb(execClass+" loaded"); // NOI18N
        VcsAdditionalCommand execCommand = null;
        try {
            execCommand = (VcsAdditionalCommand) execClass.newInstance();
        } catch (InstantiationException e) {
            fileSystem.debug ("EXEC: "+g("ERR_CanNotInstantiate", execClass)); // NOI18N
            if (stderrNoRegexListener != null)
                stderrNoRegexListener.match("EXEC: "+g("ERR_CanNotInstantiate", execClass)); // NOI18N
            success = false;
            return;
        } catch (IllegalAccessException e) {
            fileSystem.debug ("EXEC: "+g("ERR_IllegalAccessOnClass", execClass)); // NOI18N
            if (stderrNoRegexListener != null)
                stderrNoRegexListener.match("EXEC: "+g("ERR_IllegalAccessOnClass", execClass)); // NOI18N
            success = false;
            return;
        }
        E.deb("VcsAdditionalCommand created."); // NOI18N
        String[] args = new String[tokens.countTokens()];
        int i = 0;
        while(tokens.hasMoreTokens()) {
            args[i++] = tokens.nextToken();
        }
        if (success) {
            vars.put("DATAREGEX", cmd.getDataRegex()); // NOI18N
            vars.put("ERRORREGEX", cmd.getErrorRegex()); // NOI18N
            vars.put("INPUT", cmd.getInput()); // NOI18N
            vars.put("TIMEOUT", new Long(cmd.getTimeout())); // NOI18N
            success = execCommand.exec(vars, args, stdoutNoRegexListener, stderrNoRegexListener,
                                       stdoutListener, cmd.getDataRegex(), new RegexListener () {
                                           public void match(String[] elements){
                                               //D.deb("stderr match:"+MiscStuff.arrayToString(elements)); // NOI18N
                                               fileSystem.debug(cmd.getName()+":stderr: "+MiscStuff.arrayToString(elements)); // NOI18N
                                               if( stderrListener!=null ){
                                                   stderrListener.match(elements);
                                               }
                                           }
                                       }, cmd.getErrorRegex()
                                      );
        }
        D.deb("class finished with "+success+", errorContainer = "+errorContainer); // NOI18N
        if (success) {
            fileSystem.debug(cmd.getName()+": "+g("MSG_Command_succeeded")); // NOI18N
            //if( fileSystem.isAdditionalCommand(cmd.getName())==false ){
            if(cmd.getDoRefresh() && fileSystem.getDoAutoRefresh((String)vars.get("DIR"))) { // NOI18N
                //D.deb("Now refresh folder after CheckIn,CheckOut,Lock,Unlock... commands for convenience"); // NOI18N
                fileSystem.setAskIfDownloadRecursively(false); // do not ask if using auto refresh
                fileSystem.getCache().refreshDir((String)vars.get("DIR")); // NOI18N
            }
            if (!cmd.getDoRefresh()) fileSystem.removeNumDoAutoRefresh((String)vars.get("DIR")); // NOI18N
            fileSystem.setLastCommandState(true);
            //if (errorDialog != null) errorDialog.removeCommandOut();  //cancelDialog();  -- not necessary
        } else {
            fileSystem.debug(cmd.getName()+": "+g("MSG_Command_failed")); // NOI18N
            if (errorContainer != null) errorContainer.match(cmd.getName()+": "+g("MSG_Command_failed")); // NOI18N
            ErrorCommandDialog errorDialog = fileSystem.getErrorDialog();
            if (errorDialog != null && errorContainer != null) {
                errorDialog.putCommandOut(errorContainer);
                errorDialog.showDialog();
            }
            fileSystem.removeNumDoAutoRefresh((String)vars.get("DIR")); // NOI18N
            fileSystem.setLastCommandState(false);
        }
    }

    /**
     * Execute the command.
     */
    public void run() {
        String exec=cmd.getExec().trim();
        fileSystem.setLastCommandFinished(false);
        fileSystem.debug(cmd.getName()+": "+exec); // NOI18N
        if (stdoutNoRegexListener != null) stdoutNoRegexListener.match(cmd.getName()+": "+exec); // NOI18N

        StringTokenizer tokens = new StringTokenizer(exec);
        String first = tokens.nextToken();
        E.deb("first = "+first); // NOI18N
        if (first != null && (first.toLowerCase().endsWith(".class"))) // NOI18N
            runClass(first.substring(0, first.length() - ".class".length()), tokens); // NOI18N
        else
            runCommand(exec);
        fileSystem.setLastCommandFinished(true);
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
 *  18   Gandalf-post-FCS1.16.2.0    3/23/00  Martin Entlicher Do not debug output when
 *       written to the output window.
 *  17   Gandalf   1.16        2/10/00  Martin Entlicher 
 *  16   Gandalf   1.15        1/18/00  Martin Entlicher 
 *  15   Gandalf   1.14        1/15/00  Ian Formanek    NOI18N
 *  14   Gandalf   1.13        1/7/00   Martin Entlicher 
 *  13   Gandalf   1.12        1/6/00   Martin Entlicher 
 *  12   Gandalf   1.11        1/5/00   Martin Entlicher 
 *  11   Gandalf   1.10        12/29/99 Martin Entlicher 
 *  10   Gandalf   1.9         12/28/99 Martin Entlicher 
 *  9    Gandalf   1.8         12/16/99 Martin Entlicher 
 *  8    Gandalf   1.7         12/14/99 Martin Entlicher Output Listener added
 *  7    Gandalf   1.6         11/30/99 Martin Entlicher 
 *  6    Gandalf   1.5         11/23/99 Martin Entlicher Changed for 
 *       VcsFilesystem instead of CvsFileSystem
 *  5    Gandalf   1.4         10/25/99 Pavel Buzek     
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/7/99  Martin Entlicher Fixed runClass
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
