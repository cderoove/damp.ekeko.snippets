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

package tst;/*
 * X.java
 *
 * Created on July 19, 1999, 7:01 PM
 */

import org.openide.compiler.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class X extends Object {
    public static void main (String args[]) {
        //antlr.Tool.main( new String[] { "-o","z:\\p\\antlr","z:\\p\\antlr\\calc.g" } );
        //org.netbeans.qa.Utils.perl("z:\\bats\\antlr.pl "+" "+"z:\\p\\antlr" +" "+ "z:\\p\\antlr\\calc.g");
        //org.netbeans.qa.Utils.perl("z:\\bats\\antlr.pl z:\\p\\antlr z:\\p\\antlr\\calc.g");
        threadEx();
    }

    static void threadEx() {
        org.openide.execution.Executor executor = org.openide.execution.Executor.find(org.openide.execution.ThreadExecutor.class);
        org.openide.execution.ExecutorTask task = executor.execute("antlr.Tool", new String[] { "-o","z:\\p\\antlr\\tst\\","z:\\p\\antlr\\tst\\calc.g"} );
        task.waitFinished();
        System.out.println("ANTLR Tool exited with exit status: "+ task.result());
    }
}