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

package org.netbeans.modules.debugger.support.util;

import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

//import org.openide.util.RequestProcessor.Task;
//import org.openide.util.RequestProcessor;


/**
* Keeps weakly objects, and calls off-line their validate () method.
*
* @author Jan Jancura
*/
public class Validator {

    // variables .................................................................

    private WeakHashMap                 objects = new WeakHashMap ();

    /**
     * @associates Task 
     */
    private HashMap                     tasks = new HashMap ();
    private int                         waitingTasks = 0;
    private PropertyChangeSupport       pcs;

    // init ......................................................................

    public Validator () {
        pcs = new PropertyChangeSupport (this);
    }


    // main methods ..............................................................

    public void add (Object object) {
        //if (objects.containsKey (object)) return;

        //S ystem.out.println("add " + ((AbstractVariable) object).getVariableName () + " : " + object);
        objects.put (object, null);
    }

    public void remove (Object object) {
        objects.remove (object);
    }

    public synchronized void validate () {
        //S ystem.out.println("=========================================================");
        //S ystem.out.println(this);
        //T hread.dumpStack ();
        Iterator i = new ArrayList (objects.keySet ()).iterator ();
        while (i.hasNext ()) {
            final Object o = (Object) i.next ();
            synchronized (o) {
                Task task = (Task) tasks.get (o);
                if (task != null) {
                    //S ystem.out.println ("Validator.task living! " + ((AbstractVariable) o).getVariableName () + " : " + o);
                    continue;
                }
                task = RequestProcessor.postRequest (new Runnable () {
                                                         public void run () {
                                                             synchronized (o) {
                                                                 if (!o.canValidate ()) {
                                                                     //S ystem.out.println ("Validator.kill task! " + ((AbstractVariable) o).getVariableName () + " : " + o);
                                                                     removeTask (o);
                                                                     return;
                                                                 }
                                                                 o.validate ();
                                                                 //S ystem.out.println ("Validator.done task! " + ((AbstractVariable) o).getVariableName () + " : " + o);
                                                                 removeTask (o);
                                                             }
                                                         }
                                                     });
                //S ystem.out.println ("Validator.add task! " + ((AbstractVariable) o).getVariableName () + " : " + o);
                addTask (o, task);
            }
        }
        //S ystem.out.println("=========================================================");
    }

    public synchronized void clear () {
        Iterator i = new ArrayList (objects.keySet ()).iterator ();
        while (i.hasNext ()) {
            final Object o = (Object) i.next ();
            synchronized (o) {
                if (o.canRemove ()) {
                    RequestProcessor.Task task = (RequestProcessor.Task) tasks.get (o);
                    if (task != null) {
                        task.cancel ();
                        removeTask (o);
                    }
                    remove (o);
                }
            }
        }
    }

    public boolean isValidated () {
        return waitingTasks == 0;
    }

    public void addPropertyChangeListener (PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener (pcl);
    }

    public void removePropertyChangeListener (PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener (pcl);
    }


    // helper methods ............................................................

    private void addTask (Object object, Task task) {
        tasks.put (object, task);
        waitingTasks++;
        if (waitingTasks == 1)
            pcs.firePropertyChange (null, null, null);
    }

    private void removeTask (Object object) {
        tasks.remove (object);
        waitingTasks--;
        if (waitingTasks == 0)
            pcs.firePropertyChange (null, null, null);
    }


    // innerclasses ..............................................................

    public interface Object {
        public void validate ();
        public boolean canValidate ();
        public boolean canRemove ();
    }

}

/*
* Log
*  8    Gandalf-post-FCS1.5.4.1     3/30/00  Daniel Prusa    
*  7    Gandalf-post-FCS1.5.4.0     3/28/00  Daniel Prusa    
*  6    Gandalf   1.5         1/14/00  Daniel Prusa    NOI18N
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         7/21/99  Jan Jancura     
*  3    Gandalf   1.2         6/9/99   Jan Jancura     
*  2    Gandalf   1.1         6/5/99   Jan Jancura     
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/
