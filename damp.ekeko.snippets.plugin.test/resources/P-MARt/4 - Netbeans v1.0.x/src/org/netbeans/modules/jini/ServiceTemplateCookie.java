/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import java.text.MessageFormat;

/**
 *
 */
public interface ServiceTemplateCookie extends CodeCookie {

    public static final int LOOKUP_LOCATOR = 1;

    public static final int GROUPS = 10;
    public static final int GROUP_ITEM = 11;

    public static final int ENTRY_CLASSES = 20;
    public static final int ENTRY_CLASS_ITEM = 21;
    public static final int ENTRY_OBJECT_ITEM = 22;

    public static final int SERVICE_TYPES = 30;
    public static final int SERVICE_TYPE_ITEM = 31;

    public static final int SERVICES = 40;
    public static final int SERVICE_ITEM = 41;

    /**
     * @return source object 
     */
    public Object getSource();

    /**
     * @return source object or null if it is not right type
     */
    public Object getSource(int type);

    /**
     * @return source object or null if it is not right type
     */
    public Object getSource(Class type);

    /**
     * @return type
     */
    public int getType();

    /**
     */
    public class Default implements ServiceTemplateCookie {

        Object obj;
        int type;

        public Default(Object obj, int type) {
            this.obj = obj;
            this.type = type;
        }

        public Object getSource() {
            return obj;
        }

        public Object getSource(int type) {
            if (this.type == type) return obj;
            return null;
        }

        public Object getSource(Class clazz) {
            if (this.obj.getClass().equals(clazz)) return obj;
            return null;
        }
        public int getType() {
            //      System.err.println("cookie type = " + type);
            return type;
        }
    }
}


/*
* <<Log>>
*  3    Gandalf   1.2         2/2/00   Petr Kuzel      Jini module upon 1.1alpha
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         6/11/99  Martin Ryzl     
* $ 
*/ 

