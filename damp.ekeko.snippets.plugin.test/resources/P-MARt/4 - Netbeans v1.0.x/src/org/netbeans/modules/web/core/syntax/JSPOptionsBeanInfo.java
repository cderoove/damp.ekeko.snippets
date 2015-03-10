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

package org.netbeans.modules.web.core.syntax;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
 *
 * @author Petr Jiricka, Libor Karmolis
 */
public class JSPOptionsBeanInfo extends org.netbeans.modules.editor.options.PlainOptionsBeanInfo {

    public JSPOptionsBeanInfo () {
        super ("/org/netbeans/modules/editor/resources/htmlOptions"); // NOI18N
    }

    protected Class getBeanClass() {
        return JSPOptions.class;
    }
}

/*
 * Log
 *  3    Gandalf   1.2         1/12/00  Petr Jiricka    I18N
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */
