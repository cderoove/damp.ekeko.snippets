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
import org.openide.loaders.*;

import org.netbeans.modules.search.res.*;

/**
 * Test DataObject name. Reuse TextType.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ObjectNameType extends TextType {

    public static final long serialVersionUID = 1L;

    /** Creates new FullTextType */
    public ObjectNameType() {
    }

    /**
    * @return true if object passes the test.
    */
    public boolean test (DataObject dobj) {
        return match(dobj.getName());
    }

    /** @return string desribing current state.
    */
    public String toString() {
        return "ObjectNameType: substring:" + matchString + " REstring:" + reString + " re:" + re; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (ObjectNameType.class);
    }

    public String getTabText() {
        return Res.text("OBJECTNAME_CRITERION"); // NOI18N
    }

}


/*
* Log
*  6    Gandalf   1.5         1/18/00  Jesse Glick     Context help.
*  5    Gandalf   1.4         1/13/00  Radko Najman    I18N
*  4    Gandalf   1.3         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  3    Gandalf   1.2         1/4/00   Petr Kuzel      Bug hunting.
*  2    Gandalf   1.1         12/23/99 Petr Kuzel      
*  1    Gandalf   1.0         12/20/99 Petr Kuzel      
* $ 
*/ 

