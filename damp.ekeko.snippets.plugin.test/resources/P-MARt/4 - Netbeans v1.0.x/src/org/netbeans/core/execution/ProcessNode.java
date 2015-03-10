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

import java.awt.Image;
import java.awt.Toolkit;
import java.util.Hashtable;

import org.openide.execution.Executor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeListener;

/** Node which represents list of running processes.
* Final only for better performance.
*
* @author Ales Novak
*/
public final class ProcessNode extends AbstractNode implements Runnable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5090696227895147340L;

    static final java.util.ResourceBundle getBundle() {
        return org.openide.util.NbBundle.getBundle(ProcessNode.class);
    }

    /** reference to node signalizing that no processes run */
    static AbstractNode noProcesses;

    /** Reference to an instance of this class */
    static ProcessNode processNode;

    /**
    *  @param parent is a parent node
    */
    public ProcessNode () {
        super(new Children.Array());
        processNode = this;
        setIconBase("/org/netbeans/core/resources/processes"); // NOI18N
        addNodeListener(new NodeListener() {
                            public void childrenAdded(NodeMemberEvent ev) {
                            }
                            public void childrenRemoved(NodeMemberEvent ev) {
                                if ((getChildren().getNodesCount() == 0) &&
                                        (ev.getDelta().length > 0) &&
                                        (ev.getDelta()[0] != noProcesses)) {
                                    org.openide.util.RequestProcessor.postRequest(ProcessNode.this);
                                }
                            }
                            public void childrenReordered(NodeReorderEvent ev) {
                            }
                            public void nodeDestroyed(NodeEvent ev) {
                            }
                            public void propertyChange(java.beans.PropertyChangeEvent ev) {
                            }
                        });
        ExecutionEngine.getExecutionEngine().addExecutionListener(createListener());
        setName(getBundle().getString("Processes"));
        setShortDescription(getBundle().getString("Processes_HINT"));
        addNoProcessNode();
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ProcessNode.class);
    }

    public Node.Handle getHandle () {
        return new ProcessHandle();
    }

    static final class ProcessHandle implements Node.Handle {
        static final long serialVersionUID =-6979883764640743928L;

        public Node getNode () {
            return getExecutionNode();
        }
    }

    /** creates new ExecutionListener that listens on ExecutionEngine
    * @return new @see ExecutionListener
    */
    private ExecutionListener createListener() {
        return new ExecutionListener() {

                   /** called after begin of new execution */
                   public void startedExecution(ExecutionEvent ev) {
                       DefaultSysProcess proc = ev.getProcess();
                       ProcessNodeItem item = new ProcessNodeItem(proc);
                       removeNoProcessNode();
                       getChildren ().add(new Node[] { item });
                   }

                   /** called after end of execution */
                   public void finishedExecution(ExecutionEvent ev) {
                   }
               };
    }

    /** adds node signalizing that no processes run */
    void addNoProcessNode() {
        getChildren ().add(new Node[] { getNoProcessesNode() } );
    }

    /** removes node signalizing that no processes run */
    void removeNoProcessNode() {
        getChildren ().remove(new Node[] { getNoProcessesNode() });
    }

    /** @return filter node for the execution node with the given parent */
    public static Node getExecutionNode() {
        if (processNode == null) {
            processNode = new ProcessNode();
        }
        return processNode;
    }

    public static Node getNoProcessesNode() {
        if (noProcesses == null) {
            noProcesses = new AbstractNode(Children.LEAF);
            noProcesses.setName(getBundle().getString("CTL_No_processes"));
            noProcesses.setShortDescription(getBundle().getString("HINT_No_processes"));
            noProcesses.setIconBase("/org/netbeans/core/resources/noProcesses"); // NOI18N
        }
        return noProcesses;
    }

    public void run() {
        addNoProcessNode();
    }
}

/*
 * Log
 *  19   Gandalf   1.18        2/10/00  David Simonek   Icon for "no processes" 
 *       added
 *  18   Gandalf   1.17        1/12/00  Ales Novak      i18n
 *  17   Gandalf   1.16        11/17/99 Ales Novak      #4438
 *  16   Gandalf   1.15        11/11/99 Jesse Glick     Display miscellany.
 *  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        10/1/99  Ales Novak      major change of 
 *       execution
 *  13   Gandalf   1.12        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  12   Gandalf   1.11        7/30/99  David Simonek   again serialization of 
 *       nodes repaired
 *  11   Gandalf   1.10        7/8/99   Jesse Glick     Context help.
 *  10   Gandalf   1.9         6/28/99  Ian Formanek    Removed obsoleted import
 *  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/9/99   Ian Formanek    setDisplayName -> 
 *       setName as recommended for AbstractNode
 *  7    Gandalf   1.6         5/6/99   Ales Novak      displaying of 
 *       NoProcesses node
 *  6    Gandalf   1.5         3/24/99  Ales Novak      
 *  5    Gandalf   1.4         3/5/99   Ales Novak      
 *  4    Gandalf   1.3         3/3/99   David Simonek   
 *  3    Gandalf   1.2         1/21/99  Ales Novak      
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
