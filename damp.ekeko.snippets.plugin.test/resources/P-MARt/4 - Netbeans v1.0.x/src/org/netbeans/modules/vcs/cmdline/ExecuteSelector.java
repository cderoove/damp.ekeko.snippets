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

import java.util.*;
import java.text.*;

import org.openide.util.*;

import org.netbeans.modules.vcs.cmdline.exec.*;
import org.netbeans.modules.vcs.*;
import org.netbeans.modules.vcs.util.*;

/** Execute the variable selector.
 *
 * @author  Martin Entlicher
 * @version 
 */
public class ExecuteSelector extends Thread {
    private Debug E = new Debug("ExecuteSelector", true); // NOI18N
    private Debug D = E;

    private VcsFileSystem fileSystem = null;
    private String exec = null;
    private String variable = null;
    private Hashtable vars = null;
    private String selection = null;

    private NoRegexListener stdoutNoRegexListener = null;
    private NoRegexListener stderrNoRegexListener = null;

    private OutputContainer errorContainer = null;


    /** Creates new ExecuteSelector
     */
    public ExecuteSelector(VcsFileSystem fileSystem, String exec, String variable, Hashtable vars) {
        super("VCS-ExecuteSelector-"+variable); // NOI18N
        this.fileSystem = fileSystem;
        this.exec = exec;
        this.variable = variable;
        this.vars = vars;
    }

    public void setErrorContainer(OutputContainer errorContainer) {
        this.errorContainer = errorContainer;
    }

    public OutputContainer getErrorContainer() {
        return this.errorContainer;
    }

    public void setOutputNoRegexListener(NoRegexListener listener){
        stdoutNoRegexListener = listener;
    }

    public void setErrorNoRegexListener(NoRegexListener listener){
        stderrNoRegexListener = listener;
    }

    public String getSelection() {
        return selection;
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
        VcsVariableSelector execCommand = null;
        try {
            execCommand = (VcsVariableSelector) execClass.newInstance();
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
        E.deb("VcsVariableSelector created."); // NOI18N
        String[] args = new String[tokens.countTokens()];
        int i = 0;
        while(tokens.hasMoreTokens()) {
            args[i++] = tokens.nextToken();
        }
        if (success) {
            //vars.put("DATAREGEX", cmd.getDataRegex()); // NOI18N
            //vars.put("ERRORREGEX", cmd.getErrorRegex()); // NOI18N
            //vars.put("INPUT", cmd.getInput()); // NOI18N
            //vars.put("TIMEOUT", new Long(cmd.getTimeout())); // NOI18N
            selection = execCommand.exec(vars, variable, args, stdoutNoRegexListener, stderrNoRegexListener);
            success = selection != null;
        }
        D.deb("class finished with "+success+", errorContainer = "+errorContainer); // NOI18N
        if (success) {
            fileSystem.debug(g("MSG_VariableSelector")+": "+g("MSG_Command_succeeded")); // NOI18N
            //if( fileSystem.isAdditionalCommand(cmd.getName())==false ){
            fileSystem.setLastCommandState(true);
            //if (errorDialog != null) errorDialog.removeCommandOut();  //cancelDialog();  -- not necessary
        } else {
            fileSystem.debug(g("MSG_VariableSelector")+": "+g("MSG_Command_failed")); // NOI18N
            if (errorContainer != null) errorContainer.match(g("MSG_VariableSelector")+": "+g("MSG_Command_failed")); // NOI18N
            ErrorCommandDialog errorDialog = fileSystem.getErrorDialog();
            if (errorDialog != null && errorContainer != null) {
                errorDialog.putCommandOut(errorContainer);
                errorDialog.showDialog();
            }
            //fileSystem.removeNumDoAutoRefresh((String)vars.get("DIR")); // NOI18N
            fileSystem.setLastCommandState(false);
        }
    }

    /**
     * Execute the command.
     */
    public void run() {
        fileSystem.setLastCommandFinished(false);
        String vsName = g("MSG_VariableSelector");
        fileSystem.debug(vsName+": "+exec); // NOI18N
        if (stdoutNoRegexListener != null) stdoutNoRegexListener.match(vsName+": "+exec); // NOI18N

        StringTokenizer tokens = new StringTokenizer(exec);
        String first = tokens.nextToken();
        E.deb("first = "+first); // NOI18N
        if (first != null && (first.toLowerCase().endsWith(".class"))) {// NOI18N
            runClass(first.substring(0, first.length() - ".class".length()), tokens); // NOI18N
        } else {
            // Ignoring system executables
            fileSystem.debug(g("MSG_VariableSelectorIgnored"));
            //runCommand(exec);
        }
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