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

package org.netbeans.modules.serialversion;

import java.awt.BorderLayout;
import java.lang.reflect.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.*;
import org.openide.actions.AbstractCompileAction;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.cookies.*;
import org.openide.filesystems.FileSystemCapability;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.src.*;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;
import org.openide.util.enum.*;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;

/** Computes serial version UID for given class, modifies
* source element to contain the field.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public class SVUIDAction extends CookieAction {
    /** name of the UID */
    private static final String NAME = "serialVersionUID";

    /** messages */
    private static MessageFormat found = new MessageFormat (getString ("MSG_FOUND_DATA_OBJECT"));
    private static MessageFormat foundclass = new MessageFormat (getString ("MSG_FOUND_CLASS"));
    private static MessageFormat modified = new MessageFormat (getString ("MSG_MODIFIED"));

    private static InputOutput io = null;

    private static final long serialVersionUID =-2922350967108576270L;
    public String getName () {
        return getString ("CTL_SerialVersionUID");
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    protected int mode () {
        return MODE_ALL;
    }

    protected Class[] cookieClasses () {
        return new Class[] {
                   DataObject.class, ClassElement.class
               };
    }

    protected void performAction (final Node[] nodes) {
        if (io == null || io.isClosed ()) io = TopManager.getDefault ().getIO (getString ("CTL_IO"));
        final PrintWriter pw = new PrintWriter (io.getOut ());

        final QueueEnumeration queue = new QueueEnumeration ();

        final Set prompts = new HashSet (); // Set<PromptableItem>

        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {

                                              class AlterToCompCookie extends AlterEnumeration {
                                                  AlterToCompCookie (Enumeration e) {
                                                      super (e);
                                                  }
                                                  public Object alter (Object o) {
                                                      return ((Node) o).getCookie (CompilerCookie.Compile.class);
                                                  }
                                              }
                                              class FilterNulls extends FilterEnumeration {
                                                  FilterNulls (Enumeration e) {
                                                      super (e);
                                                  }
                                                  public boolean accept (Object o) {
                                                      return o != null;
                                                  }
                                              }
                                              Enumeration enum = new FilterNulls (new AlterToCompCookie (new ArrayEnumeration (nodes)));
                                              CompilerJob job = AbstractCompileAction.createJob (enum, Compiler.DEPTH_INFINITE);
                                              job.setDisplayName ("Compiling for Serial Version checks"); // [PENDING]
                                              if (! job.isUpToDate ()) {
                                                  pw.println ("Compiling..."); // [PENDING]
                                                  if (! job.start ().isSuccessful ()) {
                                                      pw.println ("Compilation failed"); // [PENDING]
                                                      return;
                                                  }
                                              } else {
                                                  pw.println ("No need to compile."); // [PENDING]
                                              }

                                              for (int i = 0; i < nodes.length; i++) {
                                                  final ClassElement elem = (ClassElement) nodes[i].getCookie (ClassElement.class);
                                                  if (elem != null) {
                                                      // check element
                                                      checkClassElement (elem, pw, prompts);
                                                  } else {
                                                      DataObject obj = (DataObject) nodes[i].getCookie (DataObject.class);
                                                      if (obj != null) {
                                                          queue.put (obj);
                                                      }
                                                  }
                                              }

                                              checkDataObjects (queue, pw, prompts);

                                              pw.println (getString ("MSG_FINISHED"));
                                              showPrompts (prompts, pw);

                                          }
                                      });
    }

    /** Test whether the class element has its SerialVersionUID field
    * or not.
    *
    * @param c class element
    * @return the element or <code>null</code> if not present
    */
    private static FieldElement find (ClassElement c) {
        FieldElement f = c.getField (Identifier.create (NAME));
        return f;
    }


    /** Update the svuid field in a class element.
    *
    * @param c class element
    * @param value the value to set to UID to (removes field if 0)
    * @param pw writer
    * @exception SourceException if changes are not allowed
    */
    static void assign (ClassElement c, long value, PrintWriter pw) throws SourceException {
        FieldElement f = find (c);
        String initValue = String.valueOf (value) + 'L';
        boolean changed;
        if (value == 0) {
            if (f != null) {
                c.removeField (f);
                changed = true;
            } else {
                changed = false;
            }
        } else {
            if (f == null) {
                f = new FieldElement ();
                f.setInitValue (initValue);
                f.setType (Type.LONG);
                f.setModifiers (Modifier.FINAL | Modifier.STATIC | Modifier.PRIVATE);
                f.setName (Identifier.create (NAME));

                c.addField (f);

                // find the added field
                f = find (c);
                changed = true;
            } else {
                if (! initValue.equals (f.getInitValue ())) {
                    f.setInitValue (initValue);
                    changed = true;
                } else {
                    changed = false;
                }
            }
        }
    }

    static void save (ClassElement c, PrintWriter pw) {
        // Get the data object and thence save capability.
        DataObject dob = (DataObject) c.getCookie (DataObject.class);
        if (dob != null) {
            SaveCookie save = (SaveCookie) dob.getCookie (SaveCookie.class);
            if (save != null) {
                pw.println
                (modified.format (new Object[] {
                                      c.getName ().getName (),
                                      c.getName ().getFullName ()
                                  })
                );
                try {
                    save.save ();
                } catch (IOException ioe) {
                    ioe.printStackTrace (pw);
                }
            }
        }
    }

    /** Checks data objects if it has source cookie and valid serial version
    * UID. If the data object is folder its content is appended to the 
    * enumeration. Info about processing of objects is written to 
    * given writer.
    *
    * @param queue enumeration of data objects
    * @param writer where to write output
    * @param prompts a Set<PromptableItem> of prompts to add to
    */
    private static void checkDataObjects (QueueEnumeration queue, PrintWriter writer, Set prompts) {
        while (queue.hasMoreElements ()) {
            DataObject obj = (DataObject)queue.nextElement ();
            if (obj instanceof DataFolder) {
                // add its content
                queue.put (((DataFolder)obj).getChildren ());
            } else {
                SourceCookie sc = (SourceCookie)obj.getCookie (SourceCookie.class);
                if (sc != null) {
                    writer.println (
                        found.format (new Object[] { obj, obj.getPrimaryFile () })
                    );
                    SourceElement elem = sc.getSource ();

                    ClassElement[] arr = elem.getAllClasses ();

                    for (int i = 0; i < arr.length; i++) {
                        checkClassElement (arr[i], writer, prompts);
                    }
                }
            }
        }
    }

    /** Checks class element.
    * @param elem the element to check
    * @param writer where to print to
    * @param prompts a Set<PromptableItem> of prompts to add to
    */
    private static void checkClassElement (ClassElement c, PrintWriter pw, Set prompts) {
        try {
            String className;

            SourceElement se = c.getSource ();
            if (se == null) {
                className = c.getName ().getFullName ();
            } else {
                Identifier p = se.getPackage ();
                if (p != null) {
                    String pn = p.getFullName ();
                    String r = c.getName ().getFullName ().substring (pn.length () + 1);
                    className = pn + '.' + r.replace ('.', '$');
                } else {
                    className = c.getName ().getFullName ().replace ('.', '$');
                }
            }
            pw.println (foundclass.format (new Object[] { className }));
            Class clazz = Type.createClass (Identifier.create (className)).toClass (TopManager.getDefault ().currentClassLoader ());
            if (clazz.isInterface () || Modifier.isAbstract (clazz.getModifiers ())) {
                pw.println ("(skipping because interface or abstract)"); // [PENDING]
                return;
            }
            ObjectStreamClass desc = ObjectStreamClass.lookup (clazz);
            if (desc == null) {
                pw.println ("(skipping because not serializable)"); // [PENDING]
                return;
            }
            ObjectStreamField[] flds = desc.getFields ();
            //long currSvuid = desc.getSerialVersionUID ();
            long currSvuid = 0;
            try {
                Field f = clazz.getDeclaredField (NAME);
                boolean access = f.isAccessible ();
                try {
                    if (! access) f.setAccessible (true);
                    currSvuid = f.getLong (null);
                } finally {
                    if (! access) f.setAccessible (false);
                }
            } catch (NoSuchFieldException nsfe) {
                // leave as 0
            } catch (Exception e1) {
                e1.printStackTrace (pw);
            }
            long idealSvuid = 0;
            try {
                Method m = ObjectStreamClass.class.getDeclaredMethod ("computeSerialVersionUID", new Class[] { Class.class });
                boolean access = false;
                try {
                    access = m.isAccessible ();
                    if (! access) m.setAccessible (true);
                    idealSvuid = ((Long) m.invoke (null, new Object[] { clazz })).longValue ();
                } finally {
                    if (! access) m.setAccessible (false);
                }
            } catch (Exception e2) {
                e2.printStackTrace (pw);
            }
            prompts.add (new PromptableItem (c, className, flds, currSvuid, idealSvuid));
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace (pw);
        }
    }

    /** Show a dialog with all possible modifications.
    * @param prompts a Set<PromptableItem>
    */
    private static void showPrompts (Set prompts, PrintWriter pw) {
        if (prompts.size () > 0) {
            final TopComponent t = new TopComponent ();
            // [PENDING] should make SerialPrompts extend TopComponent,
            // and this should override writeReplace to readResolve to null,
            // and know how to close itself
            t.setName (getString ("CTL_svuid_dialog"));
            t.setLayout (new BorderLayout ());
            Runnable closer = new Runnable () {
                                  public void run () {
                                      t.close ();
                                  }
                              };
            t.add (new SerialPrompts (prompts, pw, closer));
            t.open ();
        } else {
            pw.println (getString ("MSG_no_prompts"));
        }
    }

    /** Getter for resources.
    */
    private static String getString (String res) {
        return NbBundle.getBundle (SVUIDAction.class).getString (res);
    }

}
