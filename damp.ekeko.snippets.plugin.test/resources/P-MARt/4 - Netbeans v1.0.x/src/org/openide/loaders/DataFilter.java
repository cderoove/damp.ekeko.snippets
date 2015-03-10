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

package org.openide.loaders;


/** Allows certain data objects to be excluded from being displayed.
*
* @author Jaroslav Tulach
* @version 0.12 Mar 31, 1998
*/
public interface DataFilter extends java.io.Serializable {
    static final long serialVersionUID =-9024620024149209784L;
    /** Should the data object be displayed or not?
    * @param obj the data object
    * @return <CODE>true</CODE> if the object should be displayed,
    *    <CODE>false</CODE> otherwise
    */
    public boolean acceptDataObject (DataObject obj);

    /** Default filter that accepts everything.
    */
    public static final DataFilter ALL = new DataFilterAll ();
}

/*
 * Log
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         3/9/99   Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/4/99   Petr Hamernik   
 *  2    Gandalf   1.1         2/1/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Ales Novak      extends Serializable
 *  0    Tuborg    0.12        --/--/98 Jaroslav Tulach renamed
 */
