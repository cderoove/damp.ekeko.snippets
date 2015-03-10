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

package org.netbeans.modules.vcs.cmdline.commands;

import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;

import java.util.*;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CvsRevisionCommand extends VcsAdditionalCommand {

    private Debug E=new Debug("CvsRevisionCommand",true); // NOI18N
    private Debug D=E;

    private static final String BRANCH_OPTION = "-b"; // NOI18N
    private CvsLogInfo logInfo = new CvsLogInfo();

    /** Creates new CvsRevisionCommand */
    public CvsRevisionCommand() {
    }

    /**
     * Executes the command passed through arguments.
     * @param vars variables needed to run cvs commands
     * @param args the arguments should be the command to execute
     * @param stdoutNRListener listener of the standard output of the command
     * @param stderrNRListener listener of the error output of the command
     * @param stdoutListener listener of the standard output of the command which
     *                       satisfies regex <CODE>dataRegex</CODE>
     * @param dataRegex the regular expression for parsing the standard output
     * @param stderrListener listener of the error output of the command which
     *                       satisfies regex <CODE>errorRegex</CODE>
     * @param errorRegex the regular expression for parsing the error output
     * @return true if the command was succesfull,
     *         false if some error has occured.
     */
    public boolean exec(Hashtable vars, String[] args,
                        NoRegexListener stdoutNRListener, NoRegexListener stderrNRListener,
                        RegexListener stdoutListener, String dataRegex,
                        RegexListener stderrListener, String errorRegex) {
        int startArg = 0;
        boolean branch = args.length > 0 && args[0].equalsIgnoreCase(BRANCH_OPTION);
        if (branch) startArg++;
        String updateCmdName = null;
        String cmdName = null;
        String input = (String) vars.get("INPUT"); // NOI18N
        if (input == null) input = ""; // NOI18N
        long timeout = ((Long) vars.get("TIMEOUT")).longValue(); // NOI18N
        CvsRevisionChooser crc = new CvsRevisionChooser(new javax.swing.JFrame (), true);
        MiscStuff.centerWindow(crc);
        if (args.length > startArg) {
            cmdName = args[startArg];
            if (cmdName.charAt(0) == '"') {
                cmdName = cmdName.substring(1, cmdName.length());
                startArg++;
                int quoteIndex = args[startArg].indexOf('"');
                while(args.length >= startArg && quoteIndex < 0) {
                    cmdName += " "+args[startArg]; // NOI18N
                    startArg++;
                    quoteIndex = args[startArg].indexOf('"');
                }
                cmdName += " "+args[startArg].substring(0, quoteIndex); // NOI18N
            }
            crc.setCommandName(cmdName);
            D.deb("Setting command name = "+cmdName); // NOI18N
            startArg++;
        }
        String argsCmd[] = null;
        if (args.length > startArg) {
            argsCmd = new String[1];
            argsCmd[0] = args[startArg];
            startArg++;
        }
        boolean success = this.logInfo.updateLogInfo(vars, argsCmd, stdoutNRListener, stderrNRListener);
        if (success) {
            if (branch) crc.setRevisions(logInfo.getBranchesWithSymbolicNames());
            else crc.setRevisions(logInfo.getRevisionsWithSymbolicNames());
            success = crc.showDialog();
        } else return false;
        if (success) {
            String revision = crc.getRevision();
            D.deb("I have revision = "+revision); // NOI18N
            if (revision == null) {
                E.err("None revision was selected, the command was cancelled."); // NOI18N
                return true;
            }
            argsCmd = null;
            if (args.length > startArg) {
                argsCmd = new String[args.length - startArg];
                for(int i = startArg; i < args.length; i++) argsCmd[i-startArg] = args[i];
            }
            String cmd=MiscStuff.array2string(argsCmd);
            vars.put("REVISION", revision); // NOI18N
            Variables v=new Variables();
            String prepared=v.expand(vars,cmd, true);
            D.deb("prepared = "+prepared); // NOI18N
            if (stderrListener != null) {
                String[] command = { "REVISION COMMAND: "+prepared }; // NOI18N
                stderrListener.match(command);
            }
            if (stderrNRListener != null) stderrNRListener.match("REVSION COMMAND: "+prepared); // NOI18N
            ExternalCommand ec=new ExternalCommand(prepared);
            ec.setTimeout(timeout);
            ec.setInput(input);
            if (stdoutNRListener != null) ec.addStdoutNoRegexListener(stdoutNRListener);
            if (stderrNRListener != null) ec.addStderrNoRegexListener(stderrNRListener);
            if (stdoutListener != null) {
                try {
                    ec.addStdoutRegexListener(stdoutListener, dataRegex);
                } catch (BadRegexException e) {
                    if (stderrListener != null) {
                        String[] elements = { "REVISION COMMAND: Bad data regex "+dataRegex+"\n" }; // NOI18N
                        stderrListener.match(elements);
                    }
                }
            }
            if (stderrListener != null) {
                try {
                    ec.addStderrRegexListener(stderrListener, errorRegex);
                } catch (BadRegexException e) {
                    String[] elements = { "REVISION COMMAND: Bad error regex "+errorRegex+"\n" }; // NOI18N
                    stderrListener.match(elements);
                }
            }
            if ( ec.exec() != ExternalCommand.SUCCESS ){
                E.err("exec failed "+ec.getExitStatus()); // NOI18N
                success = false;
            } else success = true;
        } else success = true; // Command was canceled
        return success;
    }
}