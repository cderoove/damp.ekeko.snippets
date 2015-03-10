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

package org.netbeans.modules.javadoc.comments;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.openide.src.JavaDoc;
import org.openide.src.SourceException;
import org.openide.src.MemberElement;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/**
* A property editor for JavaDoc the simple version - no extra tags
*
* @version  0.10, 17 Jun 1998
*/
public class JavaDocEditor extends PropertyEditorSupport  {

    // static .....................................................................................

    // the bundle to use
    static ResourceBundle bundle = NbBundle.getBundle (
                                       JavaDocEditor.class);

    // variables ..................................................................................

    //  private String        text;
    //  private List          tagList;
    private MemberElement element;

    private JavaDoc javaDoc;

    private JavaDocEditorPanel editorPanel = null;

    private PropertyChangeSupport support;


    // init .......................................................................................

    public JavaDocEditor( MemberElement element ) {
        this.element = element;
        //System.out.println("JD Editor created"); // NOI18N
        //tagList = new ArrayList();
        support = new PropertyChangeSupport (this);
    }

    // main methods .......................................................................................

    public Object getValue () {
        return javaDoc;
    }

    public void setValue (Object object) {
        javaDoc = org.openide.src.JavaDocSupport.createJavaDoc( ((JavaDoc) object).getRawText() );
        support.firePropertyChange ("", null, null); // NOI18N
    }

    public String getAsText () {
        return null;
        //return javaDoc.isEmpty() ? "" : javaDoc.getRawText(); // NOI18N
    }

    public void setAsText (String string)  {
        //javaDoc.setRawText( string );
        return;
    }

    public String getJavaInitializationString () {
        // PENDING : Do something reasonable
        return null;
        /*
        return "new java.awt.Font (\"" + font.getName () + "\", " + font.getStyle () +
               ", " + font.getSize () + ")";
        */
    }

    public String[] getTags () {
        return null;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public Component getCustomEditor () {

        // if ( editorPanel == null  )
        editorPanel = new JavaDocEditorPanel( javaDoc, element );
        return editorPanel;
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Petr Hrebejk    i18n mk3  
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         1/4/00   Petr Hrebejk    Bug fix 5007
 *  3    Gandalf   1.2         1/3/00   Petr Hrebejk    Various bugfixes - 4709,
 *       4978, 5017, 4981, 4976, 5016, 4740,  5005
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $
 */




