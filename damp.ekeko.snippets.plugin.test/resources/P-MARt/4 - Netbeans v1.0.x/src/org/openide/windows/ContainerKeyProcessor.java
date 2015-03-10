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

package org.openide.windows;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.KeyStroke;
import javax.swing.JComponent;

// [PENDING] who is using this?? --jglick

/* The ContainerKeyProcessor tracks all key events on all components
* of the container passed into its constructor and it will also
* correctly track adding/removing of components in the hierarchy.
*
* @author  Ian Formanek
* @version 0.10, May 29, 1998
*/
class ContainerKeyProcessor extends Object {
    /** the container to listen on */
    private Container container;

    /** Constructs a new ContainerKeyProcessor for specified Container.
    * The ContainerKeyProcessor will track all key events on all components of the container
    * and it will also correctly track adding/removing of components in the hierarchy.
    * @param container The container fopr which we are doing the key processing
    */
    public ContainerKeyProcessor (Container container) {
        this.container = container;

        keyListener = new KeyAdapter () {
                          public void keyPressed (KeyEvent ev) {
                              if (!ev.isConsumed ()) {
                                  // if not yet consumed, post it to shortcut processing
                                  TopComponent.FocusMan.process (ev, ContainerKeyProcessor.this.container);
                              }
                          }
                      };

        containerListener = new ContainerListener () {
                                public void componentAdded (ContainerEvent e) {
                                    addListeners (e.getChild ());
                                }

                                public void componentRemoved (ContainerEvent e) {
                                    removeListeners (e.getChild ());
                                }
                            };

        addListeners (container);
    }

    /** Removes all listeners from all components.
    */
    public void close () {
        removeListeners (container);
    }


    private void addListeners (Component comp) {
        if (!(comp instanceof JComponent)) {
            // JComponents are handled by our focus manager
            comp.addKeyListener (keyListener);
        }

        if (comp instanceof Container) {
            ((Container)comp).addContainerListener (containerListener);
            Component comps[] = ((Container)comp).getComponents ();
            for (int i = 0; i < comps.length; i++)
                addListeners (comps[i]);
        }
    }

    private void removeListeners (Component comp) {
        comp.removeKeyListener (keyListener);
        if (comp instanceof Container) {
            ((Container)comp).removeContainerListener (containerListener);
            Component comps[] = ((Container)comp).getComponents ();
            for (int i = 0; i < comps.length; i++)
                removeListeners (comps[i]);
        }
    }

    /** The key listener */
    private KeyListener keyListener;
    /** The container listener */
    private ContainerListener containerListener;
}

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/16/99  Jesse Glick     Processing keystrokes 
 *       with real ActionEvents, handling dialogs better too.
 *  3    Gandalf   1.2         7/11/99  David Simonek   window system change...
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
