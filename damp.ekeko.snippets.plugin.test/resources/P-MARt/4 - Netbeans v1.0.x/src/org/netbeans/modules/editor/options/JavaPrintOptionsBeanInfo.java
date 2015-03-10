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

package org.netbeans.modules.editor.options;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class JavaPrintOptionsBeanInfo extends BasePrintOptionsBeanInfo {

    public JavaPrintOptionsBeanInfo() {
        super("/org/netbeans/modules/editor/resources/javaOptions"); // NOI18N
    }

    public JavaPrintOptionsBeanInfo(String iconPrefix) {
        super(iconPrefix);
    }

    protected Class getBeanClass() {
        return JavaPrintOptions.class;
    }


}

/*
* Log
*  5    Gandalf   1.4         1/13/00  Miloslav Metelka Localization
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         8/17/99  Miloslav Metelka 
*  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
*  1    Gandalf   1.0         7/9/99   Ales Novak      
* $
*/
