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
import org.netbeans.modules.vcs.VcsFileSystem;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.exec.*;
import java.util.*;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CvsWatch extends VcsAdditionalCommand {

    private Debug E=new Debug("CvsWatch", true); // NOI18N
    private Debug D=E;

    /**
     * @associates String 
     */
    Hashtable vars = null;
    private NoRegexListener stdoutNRListener = null;
    private NoRegexListener stderrNRListener = null;
    private RegexListener stdoutListener = null;
    private RegexListener stderrListener = null;
    private String dataRegex = null;
    private String errorRegex = null;
    private static final String[] actions = {
        "all", "none", "edit", "unedit", "commit"
    };



    /** Creates new CvsWatch */
    public CvsWatch() {
        D.deb("CvsWatch() called.");
    }

    /**
     * Executes the cvs watch add command to add the watch specified in CvsWatchDialog
     * @param vars variables needed to run cvs commands
     * @param args the arguments,
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
        this.vars = vars;
        this.stdoutNRListener = stdoutNRListener;
        this.stderrNRListener = stderrNRListener;
        this.stdoutListener = stdoutListener;
        this.dataRegex = dataRegex;
        this.stderrListener = stderrListener;
        this.errorRegex = errorRegex;
        javax.swing.JFrame dlgFrame = new javax.swing.JFrame ();
        MiscStuff.centerWindow(dlgFrame);
        CvsWatchDialog dlg = new CvsWatchDialog(dlgFrame, true);
        MiscStuff.centerWindow(dlg);
        dlg.setActions(actions);
        boolean success = dlg.showDialog();
        if (success) {
            D.deb("exec: action = "+dlg.getActions()+", recursive = "+dlg.getRecursive());
            String cmd = MiscStuff.array2string(args);
            success = watchAdd(cmd, dlg.getActions(), dlg.getRecursive());
        } else success = true;
        return success;
    }

    private boolean watchAdd(String cmd, String[] actions, boolean recursive) {
        String actionsStr = new String();
        for(int i = 0; i < actions.length; i++) {
            actionsStr += "-a "+actions[i]+" ";
        }
        vars.put("ACTIONS", actionsStr);
        vars.put("RECURSIVE", (recursive) ? "-R" : "-l");
        Variables v = new Variables();
        String prepared = v.expand(vars, cmd, true);
        D.deb("Cvs Watch Add prepared: "+prepared); // NOI18N
        if (stderrListener != null) {
            String[] command = { "WATCH_ADD: "+prepared }; // NOI18N
            stderrListener.match(command);
        }
        if (stderrNRListener != null) stderrNRListener.match("WATCH_ADD: "+prepared); // NOI18N
        ExternalCommand ec=new ExternalCommand(prepared);
        ec.setTimeout(((Long) vars.get("TIMEOUT")).longValue()); // NOI18N
        if (stdoutNRListener != null) ec.addStdoutNoRegexListener(stdoutNRListener);
        if (stderrNRListener != null) ec.addStderrNoRegexListener(stderrNRListener);
        if (stdoutListener != null) {
            try {
                ec.addStdoutRegexListener(stdoutListener, dataRegex);
            } catch (BadRegexException e) {
                if (stderrListener != null) {
                    String[] elements = { "WATCH_ADD: Bad data regex "+dataRegex+"\n" }; // NOI18N
                    stderrListener.match(elements);
                }
            }
        }
        if (stderrListener != null) {
            try {
                ec.addStderrRegexListener(stderrListener, errorRegex);
            } catch (BadRegexException e) {
                String[] elements = { "WATCH_ADD: Bad data regex "+errorRegex+"\n" }; // NOI18N
                stderrListener.match(elements);
            }
        }
        if ( ec.exec() != ExternalCommand.SUCCESS ){
            E.err("exec failed "+ec.getExitStatus()); // NOI18N
            return false;
        } else return true;
    }
}