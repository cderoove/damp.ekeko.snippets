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

package org.netbeans.modules.search.types;

import java.io.*;
import java.util.*;

import org.openide.util.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.netbeans.modules.search.res.*;

/**
 * Test DataObject loader match.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ObjectTypeType extends DataObjectType {

    public static final long serialVersionUID = 1L;

    public final String PROP_MASK = "mask"; //NOI18N

    transient Class[] mask;

    //stream replacing
    private final static String NAMES_FIELD = "classNames";
    private static final ObjectStreamField[] serialPersistentFields
                         = {new ObjectStreamField(NAMES_FIELD, Vector.class)};

    /** Store itself as sequence of class names. */
    private void writeObject(ObjectOutputStream out) throws IOException {
        Vector classNames = new Vector();
        if (mask != null)
            for (int i = 0; i<mask.length; i++) {
                if (mask[i] == null) continue;
                classNames.add(mask[i].getName());
            }
        out.putFields().put(NAMES_FIELD, classNames);
        out.writeFields();
        //out.writeObject(classNames);
    }

    /** Restore itself from sequence of class names. */
    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {

        Vector classNames = (Vector) in.readFields().get(NAMES_FIELD, new Vector());
        //Vector classNames = (Vector) in.readObject();

        // fill mask array
        Vector classes = new Vector();
        Iterator it = classNames.iterator();
        while(it.hasNext()) {
            try {
                classes.add(Class.forName((String)it.next()));
            } catch (ClassNotFoundException ex) {
                //let it be
            }
        }
        mask = new Class[classes.size()];
        classes.toArray(mask);

    }

    /** Creates new FullTextType */
    public ObjectTypeType() {
        //user cannot enter invalid criterion
        setValid(true);
    }


    /**
    * @return true current DataObject loader is compatible with one in mask
    */
    public boolean test (DataObject dobj) {
        Class ld = dobj.getLoader().getClass();

        for (int i=0; i<mask.length; i++) {
            if (mask[i].isAssignableFrom(ld)) return true;
        }

        return false;
    }

    /**
    * @return string desribing current state.
    */
    public String toString() {
        String classes = new String();

        if (mask == null) return "ObjectTypeType: "; //NOI18N

        for (int i=0; i<mask.length; i++)
            classes = "" + classes + ", "+ mask[i].getName();  //NOI18N

        return "ObjectTypeType: " + classes; // NOI18N
    }

    public void setMask(Class[] mask) {
        Class[] old = this.mask;
        this.mask = mask;

        firePropertyChange(PROP_MASK, old, mask);
    }

    public Class[] getMask() {
        return mask;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }

    public String getTabText() {
        return Res.text("OBJECTTYPE_CRITERION"); // NOI18N
    }

    // test serialization 
    public static void main(String args[]) throws Exception {
      String file = "/home/pkuzel/tmp/ott.ser";

      ObjectTypeType me = new ObjectTypeType();
      me.mask = new Class[] {ObjectTypeType.class, String.class};

      ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
      os.writeObject(me);
      os.close();

      ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
      Object obj = is.readObject();
      System.err.println("Got: "  + obj ); 
      is.close();

      System.err.println("Done.");
    }
}


/*
* Log
*  3    Gandalf-post-FCS1.2         4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  2    Gandalf-post-FCS1.1         3/9/00   Petr Kuzel      I18N
*  1    Gandalf-post-FCS1.0         2/24/00  Ian Formanek    
* $ 
*/ 

