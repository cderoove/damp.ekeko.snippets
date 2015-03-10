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

import org.netbeans.modules.vcs.cmdline.exec.NoRegexListener;

import java.util.*;

/**
 *
 * @author  Martin Entlicher
 * @version
 */
public abstract class VcsVariableSelector extends Object {

    /**
     * This method is used to start the selector.
     * @param vars the VCS variables
     * @param variable the name of the selected variable
     * @param args the command line parametres
     * @param stdoutNRListener listener of the standard output of the command
     * @param stderrNRListener listener of the error output of the command
     * @return the selected value or null when an error occures.
     */
    public abstract String exec(Hashtable vars, String variable, String[] args,
                                NoRegexListener stdoutNRListener,
                                NoRegexListener stderrNRListener);
}