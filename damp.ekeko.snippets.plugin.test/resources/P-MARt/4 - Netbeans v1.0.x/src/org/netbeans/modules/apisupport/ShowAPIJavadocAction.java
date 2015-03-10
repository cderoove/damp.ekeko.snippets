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

package org.netbeans.modules.apisupport;

import java.lang.reflect.Modifier;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.InstanceCookie;
import org.openide.execution.NbfsURLConnection;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.src.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.TopComponent;

public class ShowAPIJavadocAction extends CookieAction {

    private static final long serialVersionUID =-2438079845105114834L;

    public String getName () {
        if (lastCalced == null) {
            return "Show API Javadoc";
        } else {
            String name = lastCalced;
            int idx = name.lastIndexOf ((int) '.');
            if (idx != -1) name = name.substring (idx + 1);
            return "Javadoc: " + name.replace ('$', '.');
        }
    }

    protected String iconResource () {
        return "/org/netbeans/modules/apisupport/resources/ShowAPIJavadocIcon.gif";
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.utils");
    }

    protected Class[] cookieClasses () {
        return new Class[] { SourceCookie.class, ClassElement.class, MemberElement.class, InstanceCookie.class };
    }

    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    private static int sequence = 0;
    private static Node lastCalcedNode = null;
    private static String lastCalced = null;
    protected boolean enable (Node[] nodes) {
        if (! super.enable (nodes)) return false;
        final int currentSequence = ++sequence;
        //System.err.println("sequence=" + sequence);
        final Node n = nodes[0];
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              if (sequence > currentSequence) {
                                                  //System.err.println("Preempted before even doing check");
                                                  return;
                                              }
                                              String classname = findClass (n);
                                              if (sequence > currentSequence) {
                                                  //System.err.println("Ran out after checking");
                                                  return;
                                              }
                                              //System.err.println("setting enabled; currentSequence=" + currentSequence);
                                              setEnabled (classname != null);
                                              lastCalcedNode = n;
                                              lastCalced = classname;
                                              firePropertyChange ("name", null, null);
                                          }
                                      }, 500);
        return false;
    }
    // 1.2 Javac bug:
    protected void firePropertyChange (String name, Object old, Object nue) {
        super.firePropertyChange (name, old, nue);
    }

    protected void performAction (Node[] nodes) {
        if (! super.enable (nodes)) {
            System.err.println("performed when not enabled");
            return;
        }
        Node n = nodes[0];
        String classname;
        if (n == lastCalcedNode) {
            //System.err.println("using last-calculated");
            classname = lastCalced;
        } else {
            //System.err.println("recalcing");
            classname = findClass (n);
        }
        if (classname == null) {
            System.err.println("enabled, but now cannot find it");
            return;
        }
        String resource = classname.replace ('.', '/').replace ('$', '.') + ".html";
        FileObject fo = FileSystemCapability.DOC.findResource (resource);
        if (fo == null) fo = FileSystemCapability.DOC.findResource ("api/" + resource);
        if (fo == null) {
            System.err.println ("Could not find docs for " + resource +
                                " in Documentation Repository!");
            return;
        }
        try {
            URL url = NbfsURLConnection.encodeFileObject (fo);
            TopManager.getDefault ().showUrl (url);
        } catch (FileStateInvalidException fsie) {
            fsie.printStackTrace ();
        }
    }

    private static String findClass (Node n) {
        ClassElement clazz = (ClassElement) n.getCookie (ClassElement.class);
        if (clazz == null) {
            //System.err.println("no ClassElement, trying MemberElement...");
            MemberElement mem = (MemberElement) n.getCookie (MemberElement.class);
            if (mem != null) {
                //System.err.println("got MemberElement");
                clazz = mem.getDeclaringClass ();
            }
        }
        if (clazz == null) {
            //System.err.println("neither, trying SourceCookie...");
            SourceCookie source = (SourceCookie) n.getCookie (SourceCookie.class);
            if (source == null) {
                //System.err.println("No SourceCookie, trying InstanceCookie");
                InstanceCookie inst = (InstanceCookie) n.getCookie (InstanceCookie.class);
                if (inst == null) {
                    //System.err.println("No InstanceCookie");
                    return null;
                }
                try {
                    clazz = ClassElement.forClass (inst.instanceClass ());
                    if (clazz == null) {
                        //System.err.println("Instance class not found");
                        return null;
                    }
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace ();
                    return null;
                } catch (IOException ioe) {
                    ioe.printStackTrace ();
                    return null;
                }
            } else {
                SourceElement element = source.getSource ();
                switch (element.getStatus ()) {
                case SourceElement.STATUS_ERROR:
                    // fallthrough
                case SourceElement.STATUS_PARTIAL:
                    //System.err.println("bad parse, ignoring");
                    return null;
                case SourceElement.STATUS_NOT:
                    /*
                    System.err.println("partial parse, will finish and then try again");
                    element.prepare ().addTaskListener (new TaskListener () {
                      public void taskFinished (Task ignore) {
                        SwingUtilities.invokeLater (new Runnable () {
                          public void run () {
                            ShowAPIJavadocAction action = (ShowAPIJavadocAction) SystemAction.get (ShowAPIJavadocAction.class);
                            action.setEnabled (action.enable (TopComponent.getRegistry ().getActivatedNodes ()));
                          }
                        });
                      }
                });
                    return null;
                    */
                    // fallthrough
                case SourceElement.STATUS_OK:
                    //System.err.println("full parse (or delayed)");
                    if (element.getClasses ().length == 0) {
                        //System.err.println("\tno classes found");
                        return null;
                    }
                    // [PENDING] ought to check specifically for main public one
                    clazz = element.getClasses ()[0];
                    break;
                default:
                    throw new InternalError ("illegal parsing state for SourceElement: " + element.getStatus ());
                }
            }
        }
        Type type = testCache (Type.createClass (clazz.getName ()));
        if (type == null) {
            //System.err.println("no found type");
            return null;
        }
        ClassElement clazz2 = ClassElement.forName (type.getFullString ());
        if (clazz2 == null) {
            System.err.println("could not find clazz for " + type.getFullString ());
            return null;
        }
        SourceElement source = clazz2.getSource ();
        if (source == null) {
            System.err.println("clazz " + clazz2.getName () + " had no source");
            return null;
        }
        String pkg = source.getPackage ().getFullName ();
        if (pkg == null) {
            System.err.println("pkg==null");
            return null;
        }
        if (! pkg.equals ("")) pkg += '.';
        String classname = clazz2.getName ().getName ();
        while ((clazz2 = clazz2.getDeclaringClass ()) != null) {
            classname = clazz2.getName ().getName () + '$' + classname;
        }
        return pkg + classname;
    }

    // [PENDING] Using a weak map would seem to make sense, but for some reason it
    // seems that storing somekey -> null in the weak map "sticks" temporarily, but
    // is erased a few seconds later...even while regular keys stay around as expected.
    // This does not seem to have anything to do with garbage collection. Using
    // a regular hash map all works well, so this (hopefully minor) memory leak is
    // reasonable given the likely performance benefit.
    private static Map cache = Collections.synchronizedMap (new /*Weak*/HashMap ());
    private static Type testCache (Type type) {
        if (cache.containsKey (type)) {
            Type hit = (Type) cache.get (type);
            //System.err.println("Cache hit: " + type + " -> " + hit);
            return hit;
        }
        //System.err.println("Cache miss: " + type);
        cache.put (type, null);
        Type toRet = test (type);
        cache.put (type, toRet);
        //System.err.println("Cache add: " + type + " -> " + toRet);
        //System.err.println("\tfound: " + cache.containsKey (type) + " " + cache.get (type));
        return toRet;
    }

    private static Type test (Type type) {
        //System.err.println("testing: " + type);
        if (type == null) {
            //System.err.println("\twas null anyway");
            return null;
        }
        if (type.isArray ()) {
            //System.err.println("\ttrying element type");
            return testCache (type.getElementType ());
        }
        if (type.isPrimitive ()) {
            //System.err.println("\twas primitive");
            return null;
        }
        ClassElement clazz = ClassElement.forName (type.getFullString ());
        if (clazz == null) {
            // Due to a parser bug, this will actually happen sometimes,
            // though it shouldn't: method-local inner classes get here.
            //System.err.println("No such clazz");
            return null;
        }
        //System.err.println("Got clazz: " + clazz.getName ());
        if ((Modifier.isPublic (clazz.getModifiers ()) || Modifier.isProtected (clazz.getModifiers ()))
                && clazz.getName ().getFullName ().startsWith ("org.openide.")) {
            //System.err.println("\tsucceeded");
            return Type.createClass (clazz.getName ());
        } else {
            Identifier superid = clazz.getSuperclass ();
            //System.err.println("\ttrying on superclass " + superid);
            Type supe = superid == null ? null : testCache (Type.createClass (superid));
            if (supe != null) {
                //System.err.println("\tsucceeded with superclass");
                return supe;
            } else {
                //System.err.println("\ttrying with interfaces");
                Identifier[] xfaces = clazz.getInterfaces ();
                for (int i = 0; i < xfaces.length; i++) {
                    Type xface = testCache (Type.createClass (xfaces[i]));
                    if (xface != null) return xface;
                }
                //System.err.println("\tno luck with interfaces");
                return null;
            }
        }
    }

}

/*
 * Log
 *  18   Gandalf-post-FCS1.10.1.6    4/3/00   Jesse Glick     Preventing recursion.
 *  17   Gandalf-post-FCS1.10.1.5    4/3/00   Jesse Glick     Keeping a cache of 
 *       analysis results.
 *  16   Gandalf-post-FCS1.10.1.4    3/30/00  Jesse Glick     
 *  15   Gandalf-post-FCS1.10.1.3    3/30/00  Jesse Glick     Using InstanceCookie as 
 *       a last resort.
 *  14   Gandalf-post-FCS1.10.1.2    3/28/00  Jesse Glick     Slowing down javadoc 
 *       action to avoid loading down the system.
 *  13   Gandalf-post-FCS1.10.1.1    3/28/00  Jesse Glick     1.2 compiler bug 
 *       workaround.
 *  12   Gandalf-post-FCS1.10.1.0    3/28/00  Jesse Glick     Rewritten to use a 
 *       source parse rather than loading the class.
 *  11   Gandalf   1.10        1/24/00  Jesse Glick     #3964 fixed.
 *  10   Gandalf   1.9         1/11/00  Jesse Glick     TopComponent template.  
 *       New icons for CompilerTypeTester and ShowAPIJavadocAction.
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         10/7/99  Jesse Glick     Inexplicable compile 
 *       errors--org.openide.nodes.Node import does not work.
 *  7    Gandalf   1.6         10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  6    Gandalf   1.5         10/5/99  Jesse Glick     Will have API docs in an
 *       NBM.
 *  5    Gandalf   1.4         9/30/99  Jesse Glick     Package rename and misc.
 *  4    Gandalf   1.3         9/24/99  Jesse Glick     Temporarily suppressing 
 *       exception.
 *  3    Gandalf   1.2         9/21/99  Jesse Glick     Small bugfixes.
 *  2    Gandalf   1.1         9/14/99  Jesse Glick     Context help.
 *  1    Gandalf   1.0         9/12/99  Jesse Glick     
 * $
 */
