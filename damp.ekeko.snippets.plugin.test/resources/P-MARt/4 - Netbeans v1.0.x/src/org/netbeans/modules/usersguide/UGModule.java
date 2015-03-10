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

package org.netbeans.modules.usersguide;

import java.io.IOException;
import java.util.*;
import javax.swing.JSeparator;

import org.openide.TopManager;
import org.openide.loaders.*;
import org.openide.modules.ModuleInstall;

public class UGModule extends ModuleInstall {

    // Old:
    private static final String BROWSE_NAME = "UsersGuideBrowse"; // NOI18N
    private static final String SEP_NAME = "UsersGuideSeparator"; // NOI18N
    private static final String EXTRACT_NAME = "UsersGuideExtract"; // NOI18N

    static final long serialVersionUID =-5353846631812949705L;

    public void updated (int release, String specVersion) {
        try {
            InstanceDataObject.remove (getMenuFolder (), EXTRACT_NAME, "org.netbeans.modules.usersguide.UGExtractAction"); // NOI18N
            InstanceDataObject.remove (getMenuFolder (), BROWSE_NAME, "org.netbeans.modules.usersguide.UGBrowseAction"); // NOI18N
            InstanceDataObject.remove (getMenuFolder (), SEP_NAME, JSeparator.class);
        } catch (IOException ioe) {
        }
    }

    private static DataFolder getMenuFolder () throws IOException {
        return DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "Help"); // NOI18N
    }

}

/*
 * Log
 *  10   Gandalf   1.9         1/12/00  Jesse Glick     I18N.
 *  9    Gandalf   1.8         12/20/99 Jesse Glick     Reorganized Help | 
 *       Features to be Help | Documentation, killing old UG browse action, 
 *       better labelling of master help set, etc.
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         11/6/99  Jesse Glick     UGExtractAction is gone.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  4    Gandalf   1.3         7/20/99  Jesse Glick     Bug #2345.
 *  3    Gandalf   1.2         7/19/99  Jesse Glick     Implemented extracting 
 *       to disk. Using pending icons. Now at top of help menu, not bottom.
 *  2    Gandalf   1.1         6/24/99  Jesse Glick     
 *  1    Gandalf   1.0         6/24/99  Jesse Glick     
 * $
 */
