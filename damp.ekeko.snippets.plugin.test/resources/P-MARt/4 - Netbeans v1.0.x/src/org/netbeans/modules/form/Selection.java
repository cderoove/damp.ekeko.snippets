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

package org.netbeans.modules.form;

/** An interface for selection wrappers.
*
* @author   Ian Formanek
*/
public interface Selection {
    /** Called to change the selection state of this selection wrapper.
    * @param sel the new selection state
    * @param conn true for connection mode, false for plain selection
    */
    public void setSelected (boolean sel, boolean conn);

    /** @return the selection state of this selection wrapper - true if selected, false otherwise
    */
    public boolean isSelected ();

    public void setResizable (boolean resizable);
    public boolean isResizable ();
    public void setMovable (boolean movable);
    public boolean isMovable ();

    public interface ResizeListener {
        public void resizeStarted (RADVisualComponent node);
        public void resizeTo (java.awt.Rectangle rect);
        public void resizeCancelled ();
        public void resizeFinished ();
    }

    public interface MoveListener {
        public void moveStarted (RADVisualComponent node);
        public void moveTo (java.awt.Point point, java.awt.Point hotSpot);
        public void moveCancelled ();
        public void moveFinished ();
    }

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         5/14/99  Ian Formanek    
 * $
 */
