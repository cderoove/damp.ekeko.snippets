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

package org.netbeans.modules.javadoc.settings;

import sun.tools.util.ModifierFilter;

/** Constants for member acces in javadoc

 @author Petr Hrebejk
*/
public interface  MemberConstants {

    public static final long PUBLIC = ModifierFilter.PUBLIC;
    public static final long PROTECTED = ModifierFilter.PUBLIC | ModifierFilter.PROTECTED;
    public static final long PACKAGE = ModifierFilter.PUBLIC | ModifierFilter.PROTECTED | ModifierFilter.PACKAGE;
    public static final long PRIVATE = ModifierFilter.ALL_ACCESS;

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $ 
 */ 