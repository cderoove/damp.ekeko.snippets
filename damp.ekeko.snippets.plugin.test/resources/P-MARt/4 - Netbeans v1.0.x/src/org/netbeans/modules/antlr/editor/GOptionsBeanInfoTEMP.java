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

package org.netbeans.modules.antlr.editor;

import java.beans.*;
import java.awt.Image;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for plain options
*
* @author Miloslav Metelka
* @version 1.00
*/
public class GOptionsBeanInfoTEMP extends PlainOptionsBeanInfo {

    /** Propertydescriptors */
    private static PropertyDescriptor[] descriptors;
    /** Additional beaninfo */
    private static BeanInfo[] additional;


    public GOptionsBeanInfoTEMP() {
        super("/orgm/netbeans/developer/modules/text/resources/htmlOptions");
    }

    Class getBeanClass() {
        return HTMLOptions.class;
    }

}

/*
* Log
*  1    Gandalf-post-FCS1.0         4/7/00   Jesse Glick     
* $
*/
