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

package org.netbeans.modules.search.types;

import org.openide.util.*;
import org.netbeans.modules.search.res.*;

/**
 *
 * @author  Petr Kuzel
 * @version 
 */
public class ObjectNameCustomizer extends TextCustomizer {

    /** Creates new ObjectNameCustomizer */
    public ObjectNameCustomizer() {
        HelpCtx.setHelpIDString(this, org.netbeans.modules.search.types.ObjectNameType.class.toString());
    }

    /** Reuse text customizer. */
    protected String getBorderLabel() {
        return Res.text("LABEL_NAME_CONTAINS"); // NOI18N
    }

}


/*
* Log
*  5    Gandalf   1.4         1/13/00  Radko Najman    I18N
*  4    Gandalf   1.3         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  3    Gandalf   1.2         1/4/00   Petr Kuzel      Bug hunting.
*  2    Gandalf   1.1         12/23/99 Petr Kuzel      
*  1    Gandalf   1.0         12/20/99 Petr Kuzel      
* $ 
*/ 

