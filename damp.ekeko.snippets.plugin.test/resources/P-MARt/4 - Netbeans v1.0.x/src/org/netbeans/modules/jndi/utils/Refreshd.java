/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jndi.utils;

import java.util.Vector;
import javax.naming.Context;
import javax.naming.NamingException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.jndi.JndiRootNode;
import org.netbeans.modules.jndi.JndiDisabledNode;
import org.netbeans.modules.jndi.JndiChildren;
import org.netbeans.modules.jndi.JndiNode;
import org.netbeans.modules.jndi.JndiException;
import org.netbeans.modules.jndi.JndiKey;
import org.netbeans.modules.jndi.WaitNode;
import org.netbeans.modules.jndi.gui.NotFoundPanel;
import org.netbeans.modules.jndi.gui.TimeOutPanel;
/**
 *
 * @author  tzezula
 * @version 
 */
public class Refreshd extends Thread {
    Vector newItems;
    int stepSleepTime;
    int cycleSleepTime;

    /** Creates new Refreshd */
    public Refreshd() {
        super();
        this.setName("jndi.refreshd");
        this.setDaemon(true);
        this.newItems = new Vector();
    }

    /** The main loop for daemon thread*/
    public void run(){
        APCTarget target;

        while (true){
            synchronized(this.newItems){
                if (this.newItems.size()==0)
                    try{
                        this.newItems.wait();
                    }catch(InterruptedException ie){}
            }
            synchronized(this.newItems){
                target = (APCTarget)this.newItems.firstElement();
                this.newItems.removeElementAt(0);
            }
            if (target == null) continue;
            try{
                target.preAction();
                target.performAction();
                target.postAction();
            }catch(NamingException namingException){
                //Handle Exception
                // Remove node and put the FailedNode instead
                Node parent = ((JndiChildren)target).getOwner().getParentNode();
                JndiNode failedNode = (JndiNode)((JndiChildren)target).getOwner();
                if (parent != null && parent!= JndiRootNode.getDefault()){
                    JndiKey key = (JndiKey)failedNode.getKey();
                    key.failed = true;
                    ((JndiChildren)parent.getChildren()).updateKey(key);
                }
                else if (parent != null){
                    JndiRootNode root = (JndiRootNode) parent;
                    try{
                        JndiDisabledNode newNode = new JndiDisabledNode(failedNode.getInitialDirContextProperties());
                        root.getChildren().remove(new Node[]{failedNode});
                        root.getChildren().add(new Node[]{newNode});
                    }catch(NamingException innerNamingException){
                        // This exception should not happen
                        JndiRootNode.notifyForeignException(innerNamingException);
                    }
                }
            }

            catch(Exception foreignException){
                JndiRootNode.notifyForeignException(foreignException);
            }
        }
    }

    /** Adds the Refreshable to newList
     *  @param Refreshable newItem
     */
    public void addNewItem(APCTarget item){
        synchronized(this.newItems){
            this.newItems.addElement(item);
            this.newItems.notify();
        }
    }

    /** Removes the Refreshable, this method has to be called
     *  when the node is releasing!!
     *  @param Refreshable item to release
     */
    public void removeItem(APCTarget item){
        //Find the item and remove it
    }

    /** Sets the cycle sleep time in miliseconds
     * @param int sleep time
     */
    public void setCycleSleepTime(int sleep){
        this.cycleSleepTime=sleep;
    }

    /** Sets the cycle sleep time in milisoconds
     *  @param int sleep time
     */
    public void setStepSleepTime(int sleep){
        this.stepSleepTime=sleep;
    }

    /** Returns the cycle sleep time,
     *  time gived up after one refresh cycle in miliseconds
     *  @return int time
     */
    public int getCycleSleepTime(){
        return this.cycleSleepTime;
    }

    /** Returns the step sleep time,
     *  time geved up after refreshing one node in miliseconds
     *  @return int time
     */
    public int getStepSleepTime(){
        return this.stepSleepTime;
    }

}
