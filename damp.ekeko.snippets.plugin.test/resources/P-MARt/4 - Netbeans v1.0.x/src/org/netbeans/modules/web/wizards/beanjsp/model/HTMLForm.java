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

package  org.netbeans.modules.web.wizards.beanjsp.model;

import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.wizards.beanjsp.ui.*;
import org.netbeans.modules.web.util.*;

import org.openide.util.*;

import java.beans.*;
import java.util.*;

public class HTMLForm {

    public final static int FS_COLUMN = 0;
    public final static int FS_GRID_2 = 1;
    public final static int FS_NOLAYOUT = 2;

    public static Vector getFormStyles() {

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);

        Vector formStyles = new Vector();
        formStyles.addElement(resBundle.getString("JBW_FS_COLUMN"));					 // NOI18N
        formStyles.addElement(resBundle.getString("JBW_FS_GRID_2"));					 // NOI18N
        formStyles.addElement(resBundle.getString("JBW_FS_NOLAYOUT"));				 // NOI18N
        return formStyles;
    }

}
