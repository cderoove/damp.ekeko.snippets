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

package org.netbeans.beaninfo.editors;

import java.util.ArrayList;
import java.util.Enumeration;
import java.lang.reflect.Array;
import java.io.*;
import java.text.MessageFormat;

import org.openide.compiler.*;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.loaders.*;

/** Support for property editor for DebuggerType.
*
* @author   Jaroslav Tulach
*/

public class CompilerTypeEditor extends ServiceTypeEditor {

    public static final CompilerType NO_COMPILER = new NoCompiler ();

    public CompilerTypeEditor () {
        super (CompilerType.class, "LAB_ChooseCompiler", NO_COMPILER); // NOI18N
    }

    public static final class NoCompiler extends CompilerType {

        static final long serialVersionUID =-7936401242412288011L;
        private NoCompiler () {
        }

        protected String displayName () {
            return NbBundle.getBundle (CompilerTypeEditor.class).getString ("LAB_NoCompiler");
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (NoCompiler.class);
        }

        public void prepareJob (CompilerJob ign1, Class ign2, DataObject dob) {
            TopManager.getDefault ().setStatusText (MessageFormat.format
                                                    (NbBundle.getBundle (CompilerTypeEditor.class).getString ("MSG_NoCompiler"),
                                                     new Object[] { dob.getPrimaryFile ().getPackageNameExt ('/', '.') }));
        }

        private Object readResolve () throws ObjectStreamException {
            return NO_COMPILER;
        }

    }

    public static final class NoCompilerBeanInfo extends NoServiceTypeBeanInfo {

        protected String iconResource () {
            return "/org/netbeans/beaninfo/editors/resources/noCompiler.gif"; // NOI18N
        }

    }

}


/*
 * Log
 *  7    Gandalf   1.6         1/12/00  Jesse Glick     More user-friendly null 
 *       service types.
 *  6    Gandalf   1.5         11/26/99 Patrik Knakal   
 *  5    Gandalf   1.4         11/8/99  Jesse Glick     Context help.
 *  4    Gandalf   1.3         10/29/99 Jesse Glick     Added "(no compiler)" 
 *       etc. to service type selection panel.
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/15/99  Jaroslav Tulach Custom editors for 
 *       services.
 *  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
 * $
 */
