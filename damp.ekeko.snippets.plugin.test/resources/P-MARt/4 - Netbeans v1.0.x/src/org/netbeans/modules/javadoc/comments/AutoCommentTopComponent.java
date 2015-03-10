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

import java.io.*;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Just a top component which contains AutoCommentPanel
 *
 * @author  phrebejk
 * @version 
 */
public class AutoCommentTopComponent
            extends TopComponent
            implements Externalizable
{


    /** The only AutoCommentTopComponent allowed in the system */
    private static AutoCommentTopComponent acTopComponent;

    /** The panel contained by acTopComponent */
    private static AutoCommentPanel acPanel;

    /** Resource bundle for this class */
    private static final ResourceBundle bundle = NbBundle.getBundle( AutoCommentAction.class );

    static final long serialVersionUID =3696398508351593122L;
    /** Creates new AutoCommentTopComponent */
    public  AutoCommentTopComponent() {
        setLayout( new BorderLayout() );
        add( acPanel = new AutoCommentPanel(), BorderLayout.CENTER );
    }

    void setAutoCommenter( AutoCommenter aCommenter ) {
        acPanel.setAutoCommenter( aCommenter );
    }

    public static AutoCommentTopComponent getDefault() {
        if ( acTopComponent == null ) {

            acTopComponent = new AutoCommentTopComponent();

            Workspace workspace = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
            Mode myMode = workspace.createMode("AutoComment", bundle.getString ("CTL_AUTOCOMMENT_WindowTitle"), null );
            myMode.setBounds(new Rectangle( 100, 200, 350, 400 ) );
            myMode.dockInto( acTopComponent );

            acTopComponent.setName( bundle.getString ("CTL_AUTOCOMMENT_WindowTitle") );

        }

        return acTopComponent;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (AutoCommentTopComponent.class);
    }

    // Implementation of Eternalizable -----------------------------------------

    public void readExternal(final ObjectInput in)
    throws java.io.IOException, java.lang.ClassNotFoundException {

        super.readExternal( in );
        acTopComponent = this;
        acTopComponent.setAutoCommenter( new AutoCommenter());

    }

    public void writeExternal(final ObjectOutput out)
    throws java.io.IOException {
        super.writeExternal( out );
    }

}
/*
 * Log
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/16/99  Petr Hrebejk    Tag descriptions editing
 *       in HTML editor + localization
 *  2    Gandalf   1.1         8/17/99  Petr Hrebejk    @return tag check
 *  1    Gandalf   1.0         8/13/99  Petr Hrebejk    
 * $
 */
