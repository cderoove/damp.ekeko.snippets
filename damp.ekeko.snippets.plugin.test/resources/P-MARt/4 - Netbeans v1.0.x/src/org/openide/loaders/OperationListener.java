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

/** Listener to operations on data objects. Can be attached to
* the {@link DataLoaderPool} and will receive information about operations taken on all
* {@link DataObject}s.
*
* @author Jaroslav Tulach
*/
public interface OperationListener extends java.util.EventListener {
    /** Object has been recognized by
    * {@link DataLoaderPool#findDataObject}.
    * This allows listeners
    * to attach additional cookies, etc.
    *
    * @param ev event describing the action
    */
    public void operationPostCreate (OperationEvent ev);

    /** Object has been successfully copied.
    * @param ev event describing the action
    */
    public void operationCopy (OperationEvent.Copy ev);

    /** Object has been successfully moved.
    * @param ev event describing the action
    */
    public void operationMove (OperationEvent.Move ev);

    /** Object has been successfully deleted.
    * @param ev event describing the action
    */
    public void operationDelete (OperationEvent ev);

    /** Object has been successfully renamed.
    * @param ev event describing the action
    */
    public void operationRename (OperationEvent.Rename ev);

    /** A shadow of a data object has been created.
    * @param ev event describing the action
    */
    public void operationCreateShadow (OperationEvent.Copy ev);

    /** New instance of an object has been created.
    * @param ev event describing the action
    */
    public void operationCreateFromTemplate (OperationEvent.Copy ev);
}

/*
 * Log
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         3/31/99  Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/31/99  Jaroslav Tulach Added 
 *       operationPostCreate to OperationListener
 *  3    Gandalf   1.2         3/10/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         1/15/99  Jaroslav Tulach 
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
