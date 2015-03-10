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

package  org.netbeans.modules.web.wizards.wizardfw;


public class MultiLineLabel extends javax.swing.JTextArea {

    public MultiLineLabel(String text) {
        this();
        this.setText(text);
    }

    public MultiLineLabel() {
        super();
        this.setLineWrap (true);
        this.setWrapStyleWord(true);
        this.setBackground ((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("Label.background"));		 // NOI18N
        this.setEditable (false);
        // this.setFont (new java.awt.Font ("SansSerif", 0, 11));
        this.setFont (new java.awt.Font ("Dialog", 0, 11));			 // NOI18N
    }
}


