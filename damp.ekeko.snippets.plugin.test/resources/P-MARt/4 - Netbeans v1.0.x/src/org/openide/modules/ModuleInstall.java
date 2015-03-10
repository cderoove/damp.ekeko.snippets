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

package org.openide.modules;

import org.openide.util.SharedClassObject;

/**
* Provides hooks for a custom module that may be inserted into the IDE.
* This interface should be implemented by the main class of a module.
*
* <p>Simple modules will likely not need a main class--just a few entries in the manifest file.
* Even modules with a main class need not do anything in it that is already covered by manifest entries;
* only additional special functionality need be handled here.
*
* <p>Specify this class in the manifest file with <code>OpenIDE-Module-Install</code>.
*
* <p>Modules wishing to keep state associated with the installation of the module
* may do so by implementing not only this class but also {@link java.io.Externalizable}.
* In this case, they are responsible for reading and writing their own state
* properly (probably using {@link java.io.ObjectOutput#writeObject} and {@link java.io.ObjectInput#readObject}).
* Note that state which is logically connected to the user's configuration of the module on
* a possibly project-specific basis should <em>not</em> be stored this way, but rather
* using a system option. (Even if this information is not to be displayed, it should
* still be stored as hidden properties of the system option, so as to be switched properly
* during project switches.)
* @author Petr Hamernik, Jaroslav Tulach
*/
public class ModuleInstall extends SharedClassObject {

    static final long serialVersionUID =-5615399519545301432L;
    /**
     * Called when the module is first installed into the IDE.
     * Should perform whatever setup functions are required.
     * The default implementation calls restored.
     * <p>Typically, would do one-off functions, and then also call {@link #restored}.
    */
    public void installed () {
        restored ();
    }

    /**
     * Called when an already-installed module is restored (at IDE startup time).
     * Should perform whatever initializations are required.
     */
    public void restored () {}

    /**
     * Called when the module is loaded and the version is higher than
     * by the previous load
     * The default implementation calls restored().
     * @release The major release number of the <B>old</B> module code name or -1 if not specified.
     * @specVersion The specification version of the this <B>old</B> module.
    */
    public void updated ( int release, String specVersion ) {
        restored ();
    }

    /**
     * Called when the module is uninstalled (from a running IDE).
     * Should remove whatever functionality from the IDE that it had registered.
    */
    public void uninstalled () {}

    /**
     * Called when the IDE is about to exit. The default implementation returns <code>true</code>.
     * The module may cancel the exit if it is not prepared to be shut down.
    * @return <code>true</code> if it is ok to exit the IDE
    */
    public boolean closing () { return true; }

    /**
     * Called when all modules agreed with closing and the IDE will be closed.
    */
    public void close () {}
}

/*
 * Log
 *  11   Gandalf   1.10        1/16/00  Ian Formanek    Removed semicolons after
 *       methods body to prevent fastjavac from complaining
 *  10   Gandalf   1.9         11/26/99 Patrik Knakal   
 *  9    Gandalf   1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/1/99  Petr Hrebejk    org.openide.modules.ModuleInstall
 *        changed to class + some methods added
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    manifest tags changed to
 *       NetBeans-
 *  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         6/5/99   Jesse Glick     [JavaDoc]
 *  4    Gandalf   1.3         3/8/99   Jesse Glick     For clarity: Module -> 
 *       ModuleInstall; NetBeans-Module-Main -> NetBeans-Module-Install.
 *  3    Gandalf   1.2         3/8/99   Jesse Glick     Property update.
 *  2    Gandalf   1.1         3/5/99   Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
