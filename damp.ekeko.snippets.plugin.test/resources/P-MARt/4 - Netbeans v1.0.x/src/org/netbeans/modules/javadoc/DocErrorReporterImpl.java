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

package org.netbeans.modules.javadoc;

import java.util.ResourceBundle;

import com.sun.javadoc.*;

import org.openide.windows.OutputWriter;
import org.openide.util.NbBundle;

/**
 * This class implements error, warning and notice printing.
 *
 * @author Petr Hrebejk
 */
class DocErrorReporterImpl implements DocErrorReporter {

    /** The ResourceBundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( DocErrorReporterImpl.class );

    private OutputWriter errWriter;

    DocErrorReporterImpl ( OutputWriter errWriter ) {
        this.errWriter = errWriter;
    }

    /**
     * Print error message, increment error count.
     *
     * @param msg message to print
     */
    public void printError(String msg) {
        errWriter.println ( bundle.getString("MSG_Error") + " " + msg );
    }

    /**
     * Print warning message, increment warning count.
     *
     * @param msg message to print
     */
    public void printWarning(String msg) {
        errWriter.println (bundle.getString("MSG_Warning") + " " + msg );
    }

    /**
     * Print a message.
     *
     * @param msg message to print
     */
    public void printNotice(String msg) {
        errWriter.println (bundle.getString("MSG_Notice") + " " + msg );
    }
}

/*
 * Log
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/15/99  Petr Hrebejk    Localization
 *  3    Gandalf   1.2         6/11/99  Petr Hrebejk    
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
