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

package org.openide.explorer.propertysheet.editors;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//import org.openide.compiler.ExternalCompiler;

/** Outer class for editor over ExternalCompiler.ErrorExpression.
*/

public abstract class ExternalCompiler extends Object {
    private ExternalCompiler () {}
    /** Property editor for error expresions.
    */
    public static class ErrorExpressionEditor extends PropertyEditorSupport {
        /** shared list of error expressions in the system 
         * @associates ErrorExpression*/
        private static Collection sharedList;

        static {
            sharedList = new HashSet (17);
            sharedList.add (org.openide.compiler.ExternalCompiler.JAVAC);
            sharedList.add (org.openide.compiler.ExternalCompiler.JIKES);
            sharedList.add (org.openide.compiler.ExternalCompiler.JVC);
        }

        /** list to use for error expressions 
         * @associates Object*/
        private java.util.Collection list;

        /** value to edit */
        private org.openide.compiler.ExternalCompiler.ErrorExpression value;

        /** Constructs property editor with shared array of registered expressions.
        */
        public ErrorExpressionEditor () {
            this (sharedList);
        }

        /** Constructs property editor given list of ErrorExpression. This list will be presented
        * to the user when the editor is used. Also the list is modified when user adds new
        * ErrorExpression.
        *
        * @param list modifiable collection of <CODE>ExternalCompiler.ErrorExpression</CODE>
        */
        public ErrorExpressionEditor (java.util.Collection list) {
            this.list = list;
        }


        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            synchronized (this) {
                this.value = (org.openide.compiler.ExternalCompiler.ErrorExpression)value;
                list.add (value);
            }
            firePropertyChange ();
        }

        public String getAsText() {
            return value.getName();
        }

        public void setAsText(String string) {
            org.openide.compiler.ExternalCompiler.ErrorExpression[] exprs =
                getExpressions();

            for (int i = 0; i < exprs.length; i++) {
                if (string.equals(exprs[i].getName())) {
                    setValue (exprs[i]);
                    break;
                }
            }
        }

        public String getJavaInitializationString() {
            return "new ExternalCompiler.ErrorExpression (" + // NOI18N
                   value.getName () + ", " + // NOI18N
                   value.getErrorExpression () + ", " + // NOI18N
                   value.getFilePos () + ", " + // NOI18N
                   value.getLinePos () + ", " + // NOI18N
                   value.getColumnPos () + ", " + // NOI18N
                   value.getDescriptionPos () +
                   ")"; // NOI18N
        }

        public String[] getTags() {
            org.openide.compiler.ExternalCompiler.ErrorExpression[] exprs =
                getExpressions();
            String[] tags = new String [exprs.length];
            for (int i = 0; i < exprs.length; i++) {
                tags[i] = exprs[i].getName();
            }

            return tags;
        }

        public boolean isPaintable() {
            return false;
        }

        public void paintValue(Graphics g, Rectangle rectangle) {
        }

        public boolean supportsCustomEditor() {
            return true;
        }

        public Component getCustomEditor() {
            return new ErrorExpressionPanel(this);
        }

        synchronized org.openide.compiler.ExternalCompiler.ErrorExpression[] getExpressions () {
            return
                (org.openide.compiler.ExternalCompiler.ErrorExpression[])list.toArray (
                    new org.openide.compiler.ExternalCompiler.ErrorExpression[list.size ()]
                );
        }

        java.util.Collection getExpressionsVector () {
            return list;
        }



    }


}

/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/14/99  Jesse Glick     Dummy wrapper class made
 *       abstract with private constructor for style.
 *  1    Gandalf   1.0         9/14/99  Jaroslav Tulach 
 * $
 */
