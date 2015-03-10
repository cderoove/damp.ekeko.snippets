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

import java.awt.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.compiler.ExternalCompiler;

/** PropertyEditor for errorExpression property of
* ExternalCompilerSettings class
*
* @author  Ales Novak
*/
public class ErrorDescriptionsPropertyEditor extends PropertyEditorSupport {
    public ErrorDescriptionsPropertyEditor() {
        support = new PropertyChangeSupport(this);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = (ErrorDescriptions) value;
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText() {
        return value.getSelectedExpression().getName();
    }

    public void setAsText(String string) {
        ExternalCompiler.ErrorExpression[] exprs = value.getExpressions();
        for (int i = 0; i < exprs.length; i++) {
            if (string.equals(exprs[i].getName())) {
                setValue(new ErrorDescriptions(value, exprs[i]));
                break;
            }
        }
    }

    public String getJavaInitializationString() {
        return "???"; // NOI18N
    }

    public String[] getTags() {
        ExternalCompiler.ErrorExpression[] exprs = value.getExpressions();
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

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }

    private ErrorDescriptions value;
    private PropertyChangeSupport support;
}



/*
 * Log
 *  5    src-jtulach1.4         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  4    src-jtulach1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    src-jtulach1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         3/28/99  Ales Novak      
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
