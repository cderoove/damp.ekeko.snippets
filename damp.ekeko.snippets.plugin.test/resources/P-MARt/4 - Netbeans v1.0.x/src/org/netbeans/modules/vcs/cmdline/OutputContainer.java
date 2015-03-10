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

import org.netbeans.modules.vcs.cmdline.exec.*;

import java.util.Vector;

/**
 *
 * @author  Martin Entlicher
 * @version 
 */
public class OutputContainer extends Object implements NoRegexListener {
    /**
     * @associates String 
     */
    private Vector messages = null;

    /** Creates new OutputContainer */
    public OutputContainer (UserCommand uc) {
        this(uc.getName());
    }

    /** Creates new OutputContainer */
    public OutputContainer (String cmdName) {
        messages = new Vector();
        match(cmdName+":"); // NOI18N
    }

    public void match(String element){
        messages.addElement(element);
        // printMessage(MiscStuff.arrayToSpaceSeparatedString(elements) );
    }

    public Vector getMessages() {
        return messages;
    }

}