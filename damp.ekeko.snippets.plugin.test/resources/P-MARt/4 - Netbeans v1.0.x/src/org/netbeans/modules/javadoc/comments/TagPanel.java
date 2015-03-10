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

import java.util.Vector;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.openide.src.JavaDoc;
import org.openide.src.JavaDocTag;

/** Panel with standard tags
 *
 * @author Petr Hrebejk
 * @version
 */

abstract class TagPanel extends javax.swing.JPanel {
    /**
     * @associates JTextComponent 
     */
    protected ArrayList htmlComponents = new ArrayList();

    private static JavaDocEditorPanel editorPanel;

    TagPanel( JavaDocEditorPanel editorPanel ) {
        this.editorPanel = editorPanel;
    }

    abstract void setData( JavaDocTag tag );

    abstract String getCardName();

    abstract JavaDocTag getTag( String tagName );

    void addHTMLComponent( JTextComponent component ) {
        htmlComponents.add( component );
    }

    void handleFormatButton( String begTag, String endTag ) {

        for ( int i = 0; i < htmlComponents.size(); i++ ) {
            JTextComponent component = (JTextComponent)htmlComponents.get( i );
            if ( component.hasFocus() ) {
                StringBuffer sb = new StringBuffer( component.getText());

                int caretPosition = component.getCaretPosition();
                sb.insert( component.getSelectionStart(), begTag );
                sb.insert( component.getSelectionEnd() + begTag.length(), endTag );
                component.setText( sb.toString() );
                component.setCaretPosition( caretPosition + begTag.length() );
                break;
            }
        }
    }

    void commitTagChange() {
        editorPanel.commitTagChange();
    }

    void enableHTMLButtons( boolean enable ) {
        editorPanel.enableButtons( enable );
    }

    abstract void grabFirstFocus();
}

/*
 * Log
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/16/99  Petr Hrebejk    Tag descriptions editing
 *       in HTML editor + localization
 *  2    Gandalf   1.1         8/13/99  Petr Hrebejk    Window serialization 
 *       added & Tag change button in Jdoc editor removed 
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $
 */
