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

package org.netbeans.core.execution;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;

/**
*
* @author Ales Novak
*/
final class WindowTable extends HashMap {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1494996298725028533L;

    /** window listener */
    private WindowListener winListener;

    /** maps ThreadGroup:ArrayList, ArrayList keeps windows 
     * @associates ArrayList*/
    private HashMap windowMap;

    /**
    *default constructor
    */
    public WindowTable () {
        super(13);
        windowMap = new HashMap(13);
        winListener = new WindowAdapter() {
                          public void windowClosed(WindowEvent ev) {
                              Window win;
                              removeWindow(win = (Window)ev.getSource());
                              win.removeWindowListener(this);
                          }
                      };
    }

    public synchronized void putTaskWindow(Window win, TaskThreadGroup grp) {
        ArrayList vec;
        if ((vec = (ArrayList) windowMap.get(grp)) == null) {
            vec = new ArrayList();
            windowMap.put(grp, vec);
        }
        vec.add(win);
        win.addWindowListener(winListener);
        super.put(win, grp);
    }

    public TaskThreadGroup getThreadGroup(Window win) {
        return (TaskThreadGroup) super.get(win);
    }

    /** closes windows opened by grp ThreadGroup */
    void closeGroup(ThreadGroup grp) {
        Window win;
        ArrayList vec = (ArrayList) windowMap.get(grp);
        if (vec == null) return;
        Iterator ee = vec.iterator();
        while (ee.hasNext()) {
            (win = (Window) ee.next()).setVisible(false);
            remove(win);
            win.dispose();
        }
        windowMap.remove(grp);
    }

    /** return true if the ThreadGroup has any windows */
    boolean hasWindows(ThreadGroup grp) {
        ArrayList vec = (ArrayList) windowMap.get(grp);
        if ((vec == null) || (vec.size() == 0) || hiddenWindows(vec)) {
            return false;
        }
        return true;
    }

    /**
    * @param vec is a ArrayList of windows
    * @param grp is a ThreadGroup that belongs to the ArrayList
    * @return true if all windows in the ArrayList vec are invisible
    */
    private boolean hiddenWindows(ArrayList vec) {
        Iterator ee = vec.iterator();
        Window win;
        while (ee.hasNext()) {
            win = (Window) ee.next();
            if (win.isVisible()) return false;
        }
        // windows will be removed later
        return true;
    }

    /** removes given window */
    private void removeWindow(Window win) {
        Object obj = get(win); // obj is threadgroup
        if (obj == null) return;
        remove(win);
        ArrayList vec = (ArrayList) windowMap.get(obj);
        if (vec == null) return;
        vec.remove(win);
    }
}

/*
 * Log
 *  4    src-jtulach1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    src-jtulach1.2         4/23/99  Ales Novak      ConcurrentModifEx fix
 *  2    src-jtulach1.1         4/10/99  Ales Novak      
 *  1    src-jtulach1.0         3/31/99  Ales Novak      
 * $
 */
