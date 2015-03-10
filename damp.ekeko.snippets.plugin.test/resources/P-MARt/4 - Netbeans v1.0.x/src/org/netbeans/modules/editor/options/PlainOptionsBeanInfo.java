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


/** BeanInfo for plain options
*
* @author Miloslav Metelka, Ales Novak
*/
public class PlainOptionsBeanInfo extends BaseOptionsBeanInfo {

    public PlainOptionsBeanInfo() {
        this("/org/netbeans/modules/editor/resources/plainOptions"); // NOI18N
    }

    public PlainOptionsBeanInfo(String iconPrefix) {
        super(iconPrefix);
    }

    protected Class getBeanClass() {
        return PlainOptions.class;
    }

    protected String[] getPropNames() {
        return BaseOptions.BASE_PROP_NAMES;
    }

}

/*
* Log
*  9    Gandalf   1.8         1/13/00  Miloslav Metelka Localization
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/17/99  Miloslav Metelka 
*  6    Gandalf   1.5         7/21/99  Miloslav Metelka 
*  5    Gandalf   1.4         7/20/99  Miloslav Metelka 
*  4    Gandalf   1.3         7/9/99   Ales Novak      print options change
*  3    Gandalf   1.2         7/8/99   Jesse Glick     Context help.
*  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package statement
*       to make it compilable
*  1    Gandalf   1.0         6/30/99  Ales Novak      
* $
*/
