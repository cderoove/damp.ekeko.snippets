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

package org.netbeans.modules.java.settings;

import org.openide.compiler.ExternalCompiler;

/** ErrorExpression class encapsulates the settings for compiler error output parsing.
*
* @author  Ian Formanek
*/

public class ErrorDescriptions implements java.io.Serializable {

    static final long serialVersionUID =-4252742938238160778L;
    public ErrorDescriptions () {
        expressions = new java.util.Vector ();
        expressions.addElement (selectedExpression = ExternalCompiler.JAVAC);
        expressions.addElement (ExternalCompiler.JVC);
        expressions.addElement (ExternalCompiler.JIKES);
    }

    /** Creates a copy of specified ErrorDescription with only changing the selected expression.
    * WARNING: Does not create a copy of ErrorExpressions
    */
    public ErrorDescriptions (ErrorDescriptions createFrom, ExternalCompiler.ErrorExpression newSelected) {
        expressions = createFrom.expressions;
        selectedExpression = newSelected;
    }

    /** Creates a copy of specified ErrorDescription. This is a complete copy of this error description, so changes in the
    * copy do not influence the original.
    */
    public ErrorDescriptions (ErrorDescriptions createFrom) {
        expressions = new java.util.Vector ();
        for (java.util.Enumeration e = createFrom.expressions.elements (); e.hasMoreElements (); ) {
            ExternalCompiler.ErrorExpression orig = (ExternalCompiler.ErrorExpression) e.nextElement ();
            ExternalCompiler.ErrorExpression copy = (ExternalCompiler.ErrorExpression) orig.clone();
            if (createFrom.selectedExpression.equals (orig))
                selectedExpression = copy;
            expressions.addElement(copy);
        }
    }

    public ExternalCompiler.ErrorExpression getSelectedExpression () {
        return selectedExpression;
    }

    public void setSelectedExpression (ExternalCompiler.ErrorExpression newSel) {
        selectedExpression = newSel;
    }

    public ExternalCompiler.ErrorExpression[] getExpressions () {
        ExternalCompiler.ErrorExpression[] ret = new ExternalCompiler.ErrorExpression[expressions.size ()];
        expressions.copyInto (ret);
        return ret;
    }

    java.util.Vector getExpressionsVector () {
        return expressions;
    }

    private java.util.Vector expressions;
    private ExternalCompiler.ErrorExpression selectedExpression;
}

/*
 * Log
 *  6    src-jtulach1.5         11/27/99 Patrik Knakal   
 *  5    src-jtulach1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    src-jtulach1.2         3/29/99  Ian Formanek    Fixed to compile
 *  2    src-jtulach1.1         3/28/99  Ales Novak      
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
