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

package org.netbeans.modules.autoupdate;

import java.awt.Component;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.DefaultListCellRenderer;

/** Just sets the right icon to IndexItem

 @author Petr Hrebejk
*/
class SelectedListCellRenderer extends DefaultListCellRenderer {

    private static ImageIcon updateIco = new ImageIcon (SelectedListCellRenderer.class.getResource ("/org/netbeans/modules/autoupdate/resources/updateModule.gif")); // NOI18N
    private static ImageIcon newIco = new ImageIcon (SelectedListCellRenderer.class.getResource ("/org/netbeans/modules/autoupdate/resources/newModule.gif")); // NOI18N
    private static ImageIcon okIco = new ImageIcon (SelectedListCellRenderer.class.getResource ("/org/netbeans/modules/autoupdate/resources/ok.gif")); // NOI18N

    static final long serialVersionUID =-4278857657314562123L;
    public Component getListCellRendererComponent( JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        JLabel cr = (JLabel)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

        if ( ((ModuleUpdate)value).isDownloadOK() )
            cr.setIcon( okIco );
        else
            cr.setIcon( ((ModuleUpdate)value).isNew() ? newIco : updateIco );
        cr.setText( ((ModuleUpdate)value).getName() );
        return cr;
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/11/99 Petr Hrebejk    Version before Beta 5
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $ 
 */ 