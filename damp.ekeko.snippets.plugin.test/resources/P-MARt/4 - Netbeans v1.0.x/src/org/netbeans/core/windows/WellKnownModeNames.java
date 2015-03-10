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

package org.netbeans.core.windows;

/** Specifies default modes presented in the system.
* Default modes are defined as a modes used by implementation,
* not modules.
*
* @author Dafe Simonek
*/
public interface WellKnownModeNames {

    /** Name of the mode represented by main explorer view */
    public static final String EXPLORER = "explorer"; // NOI18N
    /** Name of the mode represented by execution view */
    public static final String EXECUTION = "execution"; // NOI18N
    /** Name of the mode represented by output view */
    public static final String OUTPUT = "output"; // NOI18N
    /** Name of the mode represented by editor view */
    public static final String EDITOR = "editor"; // NOI18N
    /** Name of the mode represented by property sheet view */
    public static final String PROPERTIES = "properties"; // NOI18N

}

/*
* Log
*  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
*  3    Gandalf   1.2         11/30/99 David Simonek   neccessary changes needed
*       to change main explorer to new UI style  (tabs are full top components 
*       now, visual workspace added, layout of editing workspace chnaged a bit)
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         7/11/99  David Simonek   
* $
*/