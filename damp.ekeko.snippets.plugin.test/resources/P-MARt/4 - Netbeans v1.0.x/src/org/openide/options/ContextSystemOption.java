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

package org.openide.options;

import java.beans.beancontext.BeanContextProxy;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextSupport;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** Provides a group of system options with this as the parent.
* You must still implement {@link SystemOption#displayName}, at least.
* The suboptions are automatically saved as a group.
*
* @author Ales Novak
* @version 1.0, October 30, 1998
*/
public abstract class ContextSystemOption
            extends SystemOption
    implements BeanContextProxy {

    /** Reference to the bean context describing the structure of this option tree. */
    protected OptionBeanContext beanContext = new OptionBeanContext();

    static final long serialVersionUID =-781528552645947127L;
    /** Default constructor. */
    public ContextSystemOption() {
        super();
    }

    /** Add a new option to the set.
    * @param so the option to add
    */
    public final void addOption(SystemOption so) {
        beanContext.add(so);
    }
    /** Remove an option from the set.
    * @param so the option to remove
    */
    public final void removeOption(SystemOption so) {
        beanContext.remove(so);
    }
    /** Get all options in the set.
    * @return the options
    */
    public final SystemOption[] getOptions() {
        // [WARNING] call to beanContext.toArray() can return either SystemOptions
        // or something of another type (I detected BeanContextSupport)
        // It requires deep investigation ...
        int i, j;
        SystemOption[] options;

        Object[] objs = beanContext.toArray();

        // filter out everything not SystemOption
        for(i = 0, j = 0; i < objs.length; i++) {
            if (objs[i] instanceof SystemOption) {
                if (i > j) objs[j] = objs[i];
                j++;
            }
        }
        options = new SystemOption[j];
        System.arraycopy(objs, 0, options, 0, j);
        return options;
    }

    /* Method from interface BeanContextProxy.
    * @return a BeanContext - tree of options
    */
    public final BeanContextChild getBeanContextProxy() {
        return beanContext;
    }

    /* Writes the beanContext variable to an ObjectOutput instance.
    * @param out
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(beanContext);
    }

    /* Reads the beanContext variable from an ObjectInpuit instance.
    * @param in
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        beanContext = (OptionBeanContext) in.readObject();
    }


    /** A hierarchy of SystemOptions.
    * Allows add/remove SystemOption beans only.
    * @warning many methods throws UnsupportedOperationException like BeanContextSupport does.
    */
    static class OptionBeanContext extends BeanContextSupport {

        static final long serialVersionUID =3532434266136225440L;
        /** Overridden from base class.
        * @exception IllegalArgumentException if not targetChild instanceof SystemOption
        */
        public boolean add(Object targetChild) {
            if (! (targetChild instanceof SystemOption)) throw new IllegalArgumentException();
            return super.add(targetChild);
        }
    }
}

/*
 * Log
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/8/99   Martin Ryzl     getOptions patched
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         3/31/99  Ales Novak      
 *  2    Gandalf   1.1         3/22/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
