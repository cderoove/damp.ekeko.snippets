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
import javax.swing.*;
import java.util.ResourceBundle;

import org.openide.src.*;
import org.openide.util.NbBundle;
/** Just sets the right icon to IndexItem

 @author Petr Hrebejk
*/
class AutoCommentListCellRenderer extends DefaultListCellRenderer {

    private static final ResourceBundle bundle = NbBundle.getBundle( AutoCommentListCellRenderer.class );

    private static final int offsetPublic = 0;
    private static final int offsetPackage = 1;
    private static final int offsetProtected = 2;
    private static final int offsetPrivate = 3;

    private static final int iconNothing = 0;
    private static final int iconClass = 1;
    private static final int iconInterface = 2;
    private static final int iconField = 3;
    private static final int iconConstructor = iconField + offsetPrivate + 1;
    private static final int iconMethod = iconConstructor + offsetPrivate + 1;

    private static ImageIcon[] memberIcons = new ImageIcon[ iconMethod + offsetPrivate + 1 ];

    static {
        try {
            memberIcons[ iconClass ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/class.gif") ); // NOI18N
            memberIcons[ iconInterface ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/interface.gif") ); // NOI18N

            memberIcons[ iconField + offsetPublic ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/variablePublic.gif") ); // NOI18N
            memberIcons[ iconField + offsetPackage ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/variablePackage.gif") ); // NOI18N
            memberIcons[ iconField + offsetProtected ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/variableProtected.gif") ); // NOI18N
            memberIcons[ iconField + offsetPrivate ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/variablePrivate.gif") ); // NOI18N

            memberIcons[ iconConstructor + offsetPublic ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/constructorPublic.gif") ); // NOI18N
            memberIcons[ iconConstructor + offsetPackage ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/constructorPackage.gif") ); // NOI18N
            memberIcons[ iconConstructor + offsetProtected ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/constructorProtected.gif") ); // NOI18N
            memberIcons[ iconConstructor + offsetPrivate ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/constructorPrivate.gif") ); // NOI18N

            memberIcons[ iconMethod + offsetPublic ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/methodPublic.gif") ); // NOI18N
            memberIcons[ iconMethod + offsetPackage ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/methodPackage.gif") ); // NOI18N
            memberIcons[ iconMethod + offsetProtected ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/methodProtected.gif") ); // NOI18N
            memberIcons[ iconMethod + offsetPrivate ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/methodPrivate.gif") ); // NOI18N

        }
        catch (Throwable w) {
            w.printStackTrace ();
        }
    }

    private static ImageIcon[] icons = new ImageIcon[ 5 ];

    static {
        try {
            icons[ 1 ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/ok.gif")); // NOI18N
            icons[ 2 ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/missing.gif")); // NOI18N
            icons[ 4 ] = new ImageIcon (AutoCommentListCellRenderer.class.getResource ("/org/netbeans/modules/javadoc/comments/resources/error.gif")); // NOI18N
        }
        catch (Throwable w) {
            w.printStackTrace ();
        }
    }

    private static ImageIcon waitIcon = new ImageIcon (
                                            AutoCommentListCellRenderer.class.getResource ("/org/openide/resources/src/wait.gif")); // NOI18N

    static final long serialVersionUID =-5753071739523904697L;
    public Component getListCellRendererComponent( JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if ( value == AutoCommentPanel.WAIT_STRING ) {
            final JLabel cr = (JLabel)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            cr.setIcon( waitIcon );
            cr.setText( bundle.getString( "CTL_Wait" ) );
            return cr;
        }

        final JLabel cr = (JLabel)super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
        cr.setIcon( memberIcons[ resolveIconIndex((AutoCommenter.Element)value) ] );
        cr.setText(((AutoCommenter.Element)value).getName() );

        final JLabel iconLabel = new JLabel ();
        iconLabel.setIcon (icons[ ((AutoCommenter.Element)value).getErrorNumber() ] );

        JPanel pan = new JPanel () {
                         public Dimension getPreferredSize () {
                             return new Dimension (cr.getPreferredSize ().width + iconLabel.getPreferredSize ().width, cr.getPreferredSize ().height);
                         }
                     };
        pan.setLayout (new BorderLayout ());
        pan.add (cr, BorderLayout.CENTER);
        pan.add (iconLabel, BorderLayout.WEST);
        pan.setBackground (list.getBackground ());

        return pan;
    }

    private int resolveIconIndex( AutoCommenter.Element el ) {
        MemberElement me = el.getSrcElement();

        if ( me instanceof ClassElement )
            return ((ClassElement) me).isInterface() ? iconInterface : iconClass;
        else if ( me instanceof MethodElement )
            return iconMethod;
        else if ( me instanceof ConstructorElement )
            return iconConstructor;
        else if ( me instanceof FieldElement )
            return iconField;
        else
            return iconNothing;

    }
}

/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Petr Hrebejk    i18n
 *  6    Gandalf   1.5         1/3/00   Petr Hrebejk    Various bugfixes - 4709,
 *       4978, 5017, 4981, 4976, 5016, 4740,  5005
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/17/99  Petr Hrebejk    @return tag check
 *  2    Gandalf   1.1         8/13/99  Petr Hrebejk    Exception icon added
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $ 
 */ 