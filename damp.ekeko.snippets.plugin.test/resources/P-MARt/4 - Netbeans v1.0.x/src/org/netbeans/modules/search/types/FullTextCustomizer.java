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
public class FullTextCustomizer extends TextCustomizer {

    /** Creates new FullTextCustomizer. */
    public FullTextCustomizer() {
        HelpCtx.setHelpIDString(this, org.netbeans.modules.search.types.FullTextType.class.toString());
    }

    /** Reuse text customizer. */
    protected String getBorderLabel() {
        return Res.text("LABEL_TEXT_CONTAINS"); // NOI18N
    }

}


/*
* Log
*  10   Gandalf   1.9         1/13/00  Radko Najman    I18N
*  9    Gandalf   1.8         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  8    Gandalf   1.7         1/4/00   Petr Kuzel      Bug hunting.
*  7    Gandalf   1.6         12/23/99 Petr Kuzel      Architecture improved.
*  6    Gandalf   1.5         12/20/99 Petr Kuzel      L&F fixes.
*  5    Gandalf   1.4         12/17/99 Petr Kuzel      Bundling.
*  4    Gandalf   1.3         12/16/99 Petr Kuzel      
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package name
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

