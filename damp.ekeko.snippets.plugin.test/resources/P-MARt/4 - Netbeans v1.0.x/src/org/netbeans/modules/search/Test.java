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

package org.netbeans.modules.search;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.search.*;

import org.netbeans.modules.search.types.*;

/**
 * Test routine for search support. Search whole repository.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class Test extends Object implements NodeAcceptor {

    Node repositoryNode;

    /** Creates new Test */
    public Test() {
        repositoryNode = TopManager.getDefault().getPlaces().nodes().repository();
    }

    public void test() throws InterruptedException {
        Node node = TopManager.getDefault().getPlaces().nodes().repository();

        SearchEngine engine = SearchEngine.getDefault();

        FullTextType text = new FullTextType();
        text.setMatchString("author"); // NOI18N

        //    FullTextType_1 text_1 = new FullTextType_1();
        //    text_1.setMatchString("Ted"); // NOI18N

        SearchTask task = engine.search(new Node[] {node}, new SearchType[] {text, /*text_1*/}, this);

        System.err.println("Search started.");

        Thread.currentThread().join(13000);

        task.stop();

        System.err.println("Finished.");

    }

    private void testDialog() {

        System.err.println("Dialog start.");

        HashSet set = new HashSet();
        set.add(FullTextType.class);
        CriteriaModel model = new CriteriaModel(new Node[] {repositoryNode}, set);
        CriteriaView view = new CriteriaView(model);

        DialogDescriptor desc = new DialogDescriptor(view, view.getName(), true,
                                DialogDescriptor.DEFAULT_OPTION, "OK", // NOI18N
                                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx("ID"), new AL()); // NOI18N

        //    Object options[] = new Object[] {"Help","b","c"}; // NOI18N

        //    desc.setAdditionalOptions(options);

        Dialog dlg = TopManager.getDefault().createDialog(desc);
        dlg.setModal(true);
        dlg.show();

        System.err.println("Dialog done.");
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        Test me = new Test();
        //    me.test();
        me.testDialog();
    }

    public boolean acceptNodes(org.openide.nodes.Node[] p1) {
        System.err.println("Accepted: " + p1[0] );
        return true;
    }

    private class AL implements ActionListener {

        public void actionPerformed(final java.awt.event.ActionEvent e) {

            String command = e.getActionCommand();
            if (command.equals("OK")) { // NOI18N
                System.err.println("Ok");
            } else if (command.equals("Cancel")) { // NOI18N
                System.err.println("NO");
            } else {
                System.err.println("Unknown command: " + command);
            }

        }
    }

}


/*
* Log
*  4    Gandalf   1.3         1/13/00  Radko Najman    I18N
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

