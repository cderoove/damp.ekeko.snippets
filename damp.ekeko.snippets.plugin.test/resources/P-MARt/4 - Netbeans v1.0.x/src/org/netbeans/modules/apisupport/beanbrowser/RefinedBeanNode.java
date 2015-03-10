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

package org.netbeans.modules.apisupport.beanbrowser;

import java.beans.IntrospectionException;
//import java.io.Serializable;

//import org.openide.actions.CustomizeBeanAction;
import org.openide.nodes.BeanNode;
//import org.openide.util.actions.SystemAction;

/** The variant of BeanNode to use.
* Currently does nothing beyond BeanNode, but it could.
*/
public class RefinedBeanNode extends BeanNode {

    //private Object bean;

    public RefinedBeanNode (Object bean) throws IntrospectionException {
        super (bean);
        //this.bean = bean;
    }

}

/*
 * Log
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  6    Gandalf   1.5         9/16/99  Jesse Glick     Took out old code.
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  3    Gandalf   1.2         5/27/99  Jesse Glick     BeanNode in build327 has
 *       CustomizeBeanAction already.
 *  2    Gandalf   1.1         5/25/99  Jesse Glick     Fully cleaned up name 
 *       handling, looks much nicer now. Much safer too.
 *  1    Gandalf   1.0         5/24/99  Jesse Glick     
 * $
 */
