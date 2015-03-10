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

package org.netbeans.editor;

/** Draw layer applies changes to draw context during painting process.
* Each extended UI has its own set of layers.
* It can currently include changes to font bold and italic attributes,
* and foreground and background color (and probably more in future).
* These changes are made by draw layer to draw context
* in <CODE>updateContext()</CODE> method.
* Draw layers form double-linked lists. Renderer goes through
* this list every time it draws the tokens of the text.
*
* @author Miloslav Metelka
* @version 1.00
*/


public abstract class DrawLayer {

    /** Name of this layer. The name of the layer must be unique among
    * layers installed into ExtUI
    */
    private String name;

    /** Layers with higher visibility are evaluated after the layers
    * with lower visibility. So the layers with higher visibility can hide
    * layers with lower visibility. There can be layers with the same
    * visibility. In that case the layer added later will be visible
    * if they would be both active.
    */
    private int visibility;

    /** Is this layer currently active in drawing */
    protected boolean active;

    /** Next position where the layer should be notified
    * to update its state. Fill in -1 to keep it from being notified.
    */
    int nextUpdateStatusPos = -1;

    /** Extend the drawing by the last given context till the end of line.
    * This is useful for bookmarks and other markings
    */
    protected boolean extendEOL;

    /** Extend the drawing by one half of space character for empty lines.
    * This is useful for caret layer to see the selection
    * on empty lines.
    */
    protected boolean extendEmptyLine;

    /** Call updateStatus() automatically at the end of line
    * to possibly deactivate the layer.
    */
    protected boolean updateStatusEOL;


    public DrawLayer(String name, int visibility) {
        this.name = name;
        this.visibility = visibility;
    }

    /** This method is called each time the paint begins for all layers
    * in the layer chain regardless whether they are currently active
    * or not. It is intended to prepare the layer. It's not necessary
    * to set layer activity here although it can be performed here.
    */
    protected abstract void init(DrawContext ctx);

    /** This method tries to update the mark's status.
    * It is called at least once at the begining of each drawing.
    * Generally the layer can become activated or deactivated. It can also set 
    * nextUpdateStatusPos to be asked for state update later when drawing
    * reaches nextUpdateStatusPos.
    * If the draw mark is found during painting process, this method is called
    * with the mark parameter set to the found mark.
    * when drawer finds the corresponding activation mark.
    * Layer shouldn't make any changes in the draw context which is passed
    * only to provide necessary info about state of drawing. Changes
    * to context should be done in updateContext() only.
    */
    protected abstract void updateStatus(DrawContext ctx, MarkFactory.DrawMark mark);

    /** Update draw context by setting colors, fonts and possibly other draw
    * properties.
    * The method can use information from the context to find where the painting
    * process is currently located.
    */
    protected abstract void updateContext(DrawContext ctx);

    /** Name of the layer */
    public String getName() {
        return name;
    }

    public int getVisibility() {
        return visibility;
    }

    /** Is this layer currently active */
    public final boolean isActive() {
        return active;
    }

    /** Get next update status position */
    public final int getNextUpdateStatusPos() {
        return nextUpdateStatusPos;
    }

    /** Set the next position where the updateStatus() will be called
    * automatically by the drawing engine.
    * This method can be called to ask the drawing engine to call the updateStatus()
    * method when the drawing reaches the position equal to nextUpdateStatusPos.
    * However setNextUpdateStatusPos() can be called effectively only
    * inside the body of the updateStatus() method. If called somewhere else
    * (like for example inside the updateContext()), it doesn't have
    * the desired effect because the drawing engine only checks the nextUpdateStatusPos
    * immediately after the call to updateStatus() finishes.
    */
    public void setNextUpdateStatusPos(int nextUpdateStatusPos) {
        this.nextUpdateStatusPos = nextUpdateStatusPos;
    }

    public String toString() {
        return "Layer " + getClass() + ", name='" + name + "', visibility=" + visibility; // NOI18N
    }

}

/*
 * Log
 *  6    Gandalf-post-FCS1.4.1.0     3/8/00   Miloslav Metelka 
 *  5    Gandalf   1.4         1/13/00  Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/15/99  Miloslav Metelka 
 *  2    Gandalf   1.1         5/5/99   Miloslav Metelka 
 *  1    Gandalf   1.0         4/23/99  Miloslav Metelka 
 * $
 */

