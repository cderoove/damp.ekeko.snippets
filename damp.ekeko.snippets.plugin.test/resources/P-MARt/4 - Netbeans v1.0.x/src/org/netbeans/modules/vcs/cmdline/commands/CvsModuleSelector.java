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

import java.util.*;

import org.openide.*;
import org.openide.util.*;

import org.netbeans.modules.vcs.cmdline.*;
import org.netbeans.modules.vcs.cmdline.exec.*;
import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.VcsVariableSelector;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class CvsModuleSelector extends VcsVariableSelector implements RegexListener {
    private Debug E = new Debug("CvsModuleSelector", true); // NOI18N
    private Debug D = E;

    private Hashtable vars;
    private NoRegexListener stdoutNRListener;
    private NoRegexListener stderrNRListener;
    private String dataRegex = "^(.*)$";
    private StringBuffer outputBuffer = new StringBuffer();
    private volatile boolean cmdSuccess = false;
    private volatile boolean dlgSuccess = false;
    private volatile boolean dlgFinished = false;

    private static transient String lastPrepared = null;
    private static transient String[] lastModules = null;

    /** Creates new CvsModuleSelector */
    public CvsModuleSelector() {
    }

    /**
     * Find out whether we need to run the command which get module info.
     * If we have this information from previous call, we don't have to call it again.
     * @param args the command we want to run.
     * @return true or false
     */
    private boolean needToRunCommand(String[] args) {
        if (lastPrepared == null || lastModules == null) return true;
        Variables v = new Variables();
        String cmd = MiscStuff.array2string(args);
        String prepared = v.expand(vars, cmd, true);
        return !prepared.equals(lastPrepared);
    }

    /**
     * This method is used to start the selector.
     * @param vars the VCS variables
     * @param variable the name of the selected variable
     * @param args the command line parametres
     * @param stdoutNRListener listener of the standard output of the command
     * @param stderrNRListener listener of the error output of the command
     * @return the selected value, empty string when the selection was canceled
     *         or null when an error occures.
     */
    public String exec(Hashtable vars, String variable, String[] args,
                       NoRegexListener stdoutNRListener,
                       NoRegexListener stderrNRListener) {
        D.deb("exec for "+variable);
        this.vars = vars;
        this.stdoutNRListener = stdoutNRListener;
        this.stderrNRListener = stderrNRListener;
        /*
        if (!runCommand(args)) return null;
        String[] modules = getModules();
        if (modules == null || modules.length <= 0) {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              NotifyDescriptor nd = new NotifyDescriptor.Message (org.openide.util.NbBundle.getBundle(CvsModuleSelectorDialog.class).getString("CvsModuleSelectorDialog.NoModules"));
              TopManager.getDefault ().notify (nd);
            }
          });
          return null;
    }
        */
        javax.swing.JFrame dlgFrame = new javax.swing.JFrame ();
        MiscStuff.centerWindow(dlgFrame);
        CvsModuleSelectorDialog dlg = new CvsModuleSelectorDialog(dlgFrame, true);
        MiscStuff.centerWindow(dlg);
        final String[] fargs = args;
        final CvsModuleSelectorDialog fdlg = dlg;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                   public void run() {
                                                       javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                                                                                  public void run() {
                                                                                                      new Thread("ModuleSelector-Command") {
                                                                                                          public void run() {
                                                                                                              String[] modules = null;
                                                                                                              if (needToRunCommand(fargs)) {
                                                                                                                  fdlg.waitingForModules();
                                                                                                                  cmdSuccess = runCommand(fargs);
                                                                                                                  if (cmdSuccess) modules = getModules();
                                                                                                              } else {
                                                                                                                  cmdSuccess = true;
                                                                                                                  modules = lastModules;
                                                                                                              }
                                                                                                              fdlg.setModules(modules);
                                                                                                          }
                                                                                                      }.start();
                                                                                                  }
                                                                                              });
                                                   }
                                               });
        //dlg.setModules(modules);
        Thread showThread = new Thread() {
                                public void run() {
                                    dlgSuccess = fdlg.showDialog();
                                    dlgFinished = true;
                                }
                            };
        javax.swing.SwingUtilities.invokeLater(showThread);
        try {
            while(!dlgFinished) {
                Thread.sleep(200);
            }
            D.deb("showThread is alive = "+showThread.isAlive()+", joining him");
            showThread.join();
        } catch (InterruptedException e) {
            // Interrupted
            dlgSuccess = false;
        }
        D.deb("dlgSuccess = "+dlgSuccess+", cmdSuccess = "+cmdSuccess);
        if (dlgSuccess && cmdSuccess) {
            return dlg.getSelection();
        } else {
            return (dlgSuccess) ? null : "";
        }
    }

    private boolean runCommand(String[] args) {
        Variables v = new Variables();
        String cmd = MiscStuff.array2string(args);
        String prepared = v.expand(vars, cmd, true);
        D.deb("prepared: "+prepared); // NOI18N
        ExternalCommand ec=new ExternalCommand(prepared);
        //ec.setTimeout(((Long) vars.get("TIMEOUT")).longValue()); // NOI18N
        if (stdoutNRListener != null) ec.addStdoutNoRegexListener(stdoutNRListener);
        if (stderrNRListener != null) ec.addStderrNoRegexListener(stderrNRListener);
        if (stderrNRListener != null) stderrNRListener.match(NbBundle.getBundle
                    ("org.netbeans.modules.vcs.cmdline.Bundle").getString ("MSG_VariableSelector")+
                    ": "+prepared); // NOI18N
        try{
            ec.addStdoutRegexListener(this, dataRegex);
        } catch (BadRegexException e) {
            if (stderrNRListener != null) {
                stderrNRListener.match("Bad data regex "+dataRegex); // NOI18N
            }
            return false;
        }
        if ( ec.exec() != ExternalCommand.SUCCESS ){
            E.err("exec failed "+ec.getExitStatus()); // NOI18N
            return false;
        } else {
            lastPrepared = prepared;
            return true;
        }
    }

    private String[] getModules() {
        Vector modules = new Vector();
        String output = outputBuffer.toString();
        int pos = 0;
        int index = 0;
        for(; pos >= 0 && pos < output.length(); pos = output.indexOf('\n', index)) {
            while(pos < output.length() && output.charAt(pos) == '\n') pos++;
            index = output.indexOf(' ', pos);
            if (index == pos) continue;
            int index2 = output.indexOf('\t', pos);
            if (index < 0 && index2 < 0) break;
            if (index < 0) index = output.length();
            if (index2 < 0) index2 = output.length();
            index = Math.min(index, index2);
            String module = output.substring(pos, index);
            D.deb("module = '"+module+"'");
            modules.add(module);
        }
        if (modules.size() == 0) return null;
        String[] modulesStrs = (String[]) modules.toArray(new String[0]);
        lastModules = modulesStrs;
        return modulesStrs;
    }

    public void match(String[] elements) {
        D.deb("match: "+elements[0]);
        if (elements[0].length() > 0 && elements[0].charAt(0) != '#')
            outputBuffer.append(elements[0]+"\n");
    }
}