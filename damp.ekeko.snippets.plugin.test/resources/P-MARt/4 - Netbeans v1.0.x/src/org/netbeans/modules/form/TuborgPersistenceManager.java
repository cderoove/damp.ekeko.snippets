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

package org.netbeans.modules.form;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.forminfo.*;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.compat2.*;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.util.*;

/**
*
* @author Ian Formanek
*/
public class TuborgPersistenceManager extends PersistenceManager {
    /** Constant used for signing the classes loaded by default classloader */
    final static byte LOADER_DEFAULT = 0x01;

    // Constants to recognize form files saved from Tuborg
    private static byte MAGIC_0 = -84;
    private static byte MAGIC_1 = -19;
    private static byte MAGIC_2 = 0;
    private static byte MAGIC_3 = 5;

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    /** A method which allows the persistence manager to provide infotrmation on whether
    * is is capable to store info about advanced features provided from Developer 3.0 
    * - all persistence managers except the one providing backward compatibility with 
    * Developer 2.X should return true from this method.
    * @return true if this PersistenceManager is capable to store advanced form features, false otherwise
    */
    public boolean supportsAdvancedFeatures () {
        return false;
    }

    /** A method which allows the persistence manager to check whether it can read
    * given form format.
    * @return true if this PersistenceManager can load form stored in the specified form, false otherwise
    * @exception IOException if any problem occured when accessing the form
    */
    public boolean canLoadForm (FormDataObject formObject) throws IOException {
        InputStream is = null;
        try {
            is = formObject.getFormEntry ().getFile ().getInputStream();
            byte[] bytes = new byte[4];
            int len = is.read (bytes);
            return ((len == 4) && (bytes[0] == MAGIC_0) && (bytes[1] == MAGIC_1) && (bytes[2] == MAGIC_2) && (bytes[3] == MAGIC_3));
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath)t;
            }
            return false;
        }
        finally {
            if (is != null)
                is.close();
        }
    }

    /** Called to actually load the form stored in specified formObject.
    * @param formObject the FormDataObject which represents the form files
    * @return the FormManager2 representing the loaded form or null if some problem occured
    * @exception IOException if any problem occured when loading the form
    */
    public FormManager2 loadForm (FormDataObject formObject) throws IOException {
        FormManager2 formManager2 = null;
        InputStream is = null;
        try {
            is = formObject.getFormEntry ().getFile ().getInputStream();
        } catch (FileNotFoundException e) {
            throw new IOException ("Form file not found"); // NOI18N
        }

        ObjectInputStream ois = null;
        try {
            ois = new org.openide.util.io.NbObjectInputStream(is);

            // deserialization from stream
            Object deserializedForm = null;
            try {
                deserializedForm = ois.readObject ();
            } catch (ClassNotFoundException e) {
                throw new IOException ("Form type not found: "+e.getMessage ()); // NOI18N
            }

            // create new objects from Backward compatibility classes
            RADForm radForm = null;

            if (deserializedForm == null) {
                throw new IOException ("The form file does not contain a valid form"); // NOI18N
            } else if (! (deserializedForm instanceof DesignForm)) {
                throw new IOException ("Unknown form type: "+deserializedForm.getClass ().getName ()); // NOI18N
            } else {
                FormInfo info = null;
                if (deserializedForm instanceof JFrameForm) {
                    info = new JFrameFormInfo ();
                } else if (deserializedForm instanceof JDialogForm) {
                    info = new JDialogFormInfo ();
                } else if (deserializedForm instanceof JPanelForm) {
                    info = new JPanelFormInfo ();
                } else if (deserializedForm instanceof JAppletForm) {
                    info = new JAppletFormInfo ();
                } else if (deserializedForm instanceof JInternalFrameForm) {
                    info = new JInternalFrameFormInfo ();
                } else if (deserializedForm instanceof FrameForm) {
                    info = new FrameFormInfo ();
                } else if (deserializedForm instanceof DialogForm) {
                    info = new DialogFormInfo ();
                } else if (deserializedForm instanceof PanelForm) {
                    info = new PanelFormInfo ();
                } else if (deserializedForm instanceof AppletForm) {
                    info = new AppletFormInfo ();
                }

                radForm = new RADForm (info);
                formManager2 = new FormManager2 (formObject, radForm);
                RADVisualContainer topComp = (RADVisualContainer)radForm.getTopLevelComponent ();
                RADFormNode topNode = ((DesignForm)deserializedForm).formManager.rootNode;

                // process non-visual components
                Object[] nonVisualNodes = topNode.nonVisualsNode.nodeArray.array;
                RADComponent[] nonVisualsComps = new RADComponent [nonVisualNodes.length];
                for (int i = 0; i < nonVisualNodes.length; i++) {
                    RADNode node = (RADNode)nonVisualNodes[i];
                    if (node instanceof RADMenuNode) {
                        nonVisualsComps[i] = new RADMenuComponent ();
                        nonVisualsComps[i].initialize (formManager2);
                        nonVisualsComps[i].setComponent (node.beanClass);
                        nonVisualsComps[i].setName (node.componentName);
                        convertComponent (node, nonVisualsComps[i]);
                        RADComponent[] subs = createMenuHierarchy ((RADMenuNode)node, formManager2);
                        ((RADMenuComponent)nonVisualsComps[i]).initSubComponents (subs);
                    } else {
                        nonVisualsComps[i] = new RADComponent ();
                        nonVisualsComps[i].initialize (formManager2);
                        nonVisualsComps[i].setComponent (node.beanClass);
                        nonVisualsComps[i].setName (node.componentName);
                        convertComponent (node, nonVisualsComps[i]);
                    }
                }
                formManager2.initNonVisualComponents (nonVisualsComps);

                convertComponent (topNode, topComp);
                RADComponent[] subComps = createHierarchy ((((DesignForm)deserializedForm).formManager.rootNode), formManager2);
                topComp.initSubComponents (subComps);
                topComp.setDesignLayout (((RADContainerNode)topNode).designLayout);
                String menu = ((RADFormNode)topNode).menu;
                if ((menu != null) && (topComp instanceof RADVisualFormContainer)) {
                    ((RADVisualFormContainer)topComp).setFormMenu (menu);
                }
            }
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    if (System.getProperty ("netbeans.debug.form") != null) {
                        e.printStackTrace ();
                    }
                }
            }
        }
        return formManager2;
    }

    /** Called to actually save the form represented by specified FormManager2 into specified formObject.
    * @param formObject the FormDataObject which represents the form files
    * @param manager the FormManager2 representing the form to be saved
    * @exception IOException if any problem occured when saving the form
    */
    public void saveForm (FormDataObject formObject, FormManager2 manager) throws IOException {
        FileLock lock = null;
        try {
            lock = formObject.getFormEntry ().getFile ().lock ();
            OutputStream os = formObject.getFormEntry ().getFile ().getOutputStream(lock);

            // we first save into memory to prevent corrupting the form file
            // if something goes wrong
            java.io.ByteArrayOutputStream barros = new java.io.ByteArrayOutputStream (10000);
            ObjectOutputStream oos = new X2ObjectOutputStream(barros);

            oos.writeObject (createOldForm (formObject, manager));
            oos.close ();

            // now it is safely written in memory, so we can save it to the file
            barros.writeTo(os);

        } catch (IOException e) {
            TopManager.getDefault ().notify (new NotifyDescriptor.Message (
                                                 java.text.MessageFormat.format (
                                                     NbBundle.getBundle (FormEditorSupport.class).getString ("ERR_SavingForm"),
                                                     new Object[] { formObject.getName (), e.getClass ().getName ()}
                                                 ),
                                                 NotifyDescriptor.ERROR_MESSAGE
                                             )
                                            );
            e.printStackTrace ();
        } finally {
            if (lock != null) {
                lock.releaseLock ();
            }
        }

    }

    // -----------------------------------------------------------------------------
    // private methods -> Backward compatibility

    private static RADComponent[] createHierarchy (RADContainerNode node, FormManager2 formManager2) {
        RADNode nodes[] = node.getSubNodes ();
        RADComponent[] comps = new RADComponent [nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] instanceof RADContainerNode) {
                comps[i] = new RADVisualContainer ();
            } else if (nodes[i] instanceof RADVisualNode) {
                comps[i] = new RADVisualComponent ();
            } else {
                comps[i] = new RADComponent ();
            }
            comps[i].initialize (formManager2);
            comps[i].setComponent (nodes[i].beanClass);
            comps[i].setName (nodes[i].componentName);
            convertComponent (nodes[i], comps[i]);
            if (nodes[i] instanceof RADContainerNode) {
                RADComponent[] subs = createHierarchy ((RADContainerNode)nodes[i], formManager2);
                ((ComponentContainer)comps[i]).initSubComponents (subs);
                ((RADVisualContainer)comps[i]).setDesignLayout (((RADContainerNode)nodes[i]).designLayout);
            }
        }

        return comps;
    }

    private static RADComponent[] createMenuHierarchy (RADMenuNode node, FormManager2 formManager2) {
        Object nodes[] = node.getSubNodes ();
        RADComponent[] comps = new RADComponent [nodes.length];
        for (int i = 0; i < nodes.length; i++) {

            Class beanClass;
            if (nodes[i] instanceof RADMenuNode.MenuSeparatorNode) {
                comps[i] = new RADMenuItemComponent ();
                if ((node.type == RADMenuItemNode.T_MENU) || (node.type == RADMenuItemNode.T_POPUPMENU)) {
                    beanClass = org.netbeans.modules.form.Separator.class;
                } else {
                    beanClass = javax.swing.JSeparator.class;
                }
                comps[i].initialize (formManager2);
                comps[i].setComponent (beanClass);
                if ((node.type == RADMenuItemNode.T_JMENU) || (node.type == RADMenuItemNode.T_JPOPUPMENU)) {
                    // only swing separators have instance variable
                    comps[i].setName (formManager2.getVariablesPool ().getNewName (beanClass));
                }
            } else if (nodes[i] instanceof RADNode) {
                beanClass = ((RADNode)nodes[i]).beanClass;
                if (nodes[i] instanceof RADMenuNode) {
                    comps[i] = new RADMenuComponent ();
                } else if (nodes[i] instanceof RADMenuItemNode) {
                    comps[i] = new RADMenuItemComponent ();
                } else {
                    comps[i] = new RADComponent ();
                }
                comps[i].initialize (formManager2);
                comps[i].setComponent (beanClass);
                comps[i].setName (((RADNode)nodes[i]).componentName);
                convertComponent ((RADNode)nodes[i], comps[i]);
                if (nodes[i] instanceof RADMenuNode) {
                    RADComponent[] subs = createMenuHierarchy ((RADMenuNode)nodes[i], formManager2);
                    ((ComponentContainer)comps[i]).initSubComponents (subs);
                }
            }
        }

        return comps;
    }

    private static void convertComponent (RADNode node, RADComponent comp) {
        Map origChanged = node.changedValues;
        BeanInfo bi = comp.getBeanInfo ();
        PropertyDescriptor[] pds = bi.getPropertyDescriptors ();
        for (Iterator it = origChanged.keySet ().iterator (); it.hasNext (); ) {
            Object key = it.next ();
            for (int i = 0; i < pds.length; i++) {
                if (key.equals (pds[i].getName ())) {
                    try {
                        comp.restorePropertyValue (pds[i], origChanged.get (key));
                    } catch (IllegalArgumentException e) {
                        // [PENDING]
                    } catch (IllegalAccessException e) {
                        // [PENDING]
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // [PENDING]
                    }
                    break;
                }
            }
        }

        Hashtable eventHandlers = node.eventHandlers;
        comp.initDeserializedEvents (eventHandlers);

        // process constraints on visual components
        if (node instanceof RADVisualNode) {
            HashMap map = ((RADVisualNode)node).constraints;
            ((RADVisualComponent)comp).initConstraints (map);
        }
    }

    // -----------------------------------------------------------------------------
    // private methods -> Forward compatibility

    private static DesignForm createOldForm (FormDataObject formObject, FormManager2 formManager2) {
        DesignForm designForm = null;
        FormInfo info = formManager2.getRADForm ().getFormInfo ();
        if (info instanceof JFrameFormInfo) {
            designForm = new JFrameForm ();
        } else if (info instanceof JDialogFormInfo) {
            designForm = new JDialogForm ();
        } else if (info instanceof JPanelFormInfo) {
            designForm = new JPanelForm ();
        } else if (info instanceof JAppletFormInfo) {
            designForm = new JAppletForm ();
        } else if (info instanceof JInternalFrameFormInfo) {
            designForm = new JInternalFrameForm ();
        } else if (info instanceof FrameFormInfo) {
            designForm = new FrameForm ();
        } else if (info instanceof DialogFormInfo) {
            designForm = new DialogForm ();
        } else if (info instanceof PanelFormInfo) {
            designForm = new PanelForm ();
        } else if (info instanceof AppletFormInfo) {
            designForm = new AppletForm ();
        }

        FormManager formManager = new FormManager ();

        RADFormNode rootNode = new RADFormNode ();

        NonVisualsNode nonVisualsNode = new NonVisualsNode ();

        RADNodeArray nodeArray = new RADNodeArray ();
        // >> RADNodeArray
        // 1. containerNode [RADContainer]
        // 2. array [Object[]]
        nodeArray.containerNode = nonVisualsNode;
        RADComponent[] nonVisualComponents = formManager2.getNonVisualComponents ();
        RADNode[] nonVisualNodes = new RADNode[nonVisualComponents.length];
        for (int i = 0; i < nonVisualComponents.length; i++) {
            if (nonVisualComponents[i] instanceof RADMenuComponent) {
                nonVisualNodes[i] = new RADMenuNode ();
            } else {
                nonVisualNodes[i] = new RADNode ();
            }
            convertComponentBack (nonVisualComponents[i], nonVisualNodes [i]);
        }
        nodeArray.array = nonVisualNodes;

        // >> NonVisualsNode
        // 1. designForm [DesignForm]
        // 2. rootNode [RADFormNode]
        nonVisualsNode.formManager = formManager;
        nonVisualsNode.nodeArray = nodeArray;

        // >> RADFormNode
        // From RADFormNode:
        //  1. form [DesignForm]
        //  2. nonVisualsNode [NonVisualsNode]
        //  3. menu [String]
        rootNode.form = designForm;
        rootNode.nonVisualsNode = nonVisualsNode;
        rootNode.menu = null;
        if (formManager2.getRADForm ().getTopLevelComponent () instanceof RADVisualFormContainer) {
            rootNode.menu = ((RADVisualFormContainer)formManager2.getRADForm ().getTopLevelComponent ()).getFormMenu ();
        }

        convertComponentBack (formManager2.getRADForm ().getTopLevelComponent (), rootNode);

        // >> FormManager
        // 1. designForm [DesignForm]
        // 2. rootNode [RADFormNode]
        formManager.designForm = designForm;
        formManager.rootNode = rootNode;


        designForm.formManager = formManager;

        return designForm;
    }

    private static void convertComponentBack (RADComponent comp, RADNode contNode) {
        if (contNode instanceof RADMenuNode) {
            // >> RADMenuNode
            //  1. nodeArray [RADNodeArray]

            RADNodeArray nodeArray = new RADNodeArray ();
            // >> RADNodeArray
            // 1. containerNode [RADContainer]
            // 2. array [Object[] ]
            nodeArray.containerNode = (RADMenuNode)contNode;
            RADComponent[] children = ((RADMenuComponent)comp).getSubBeans ();
            RADNode[] childrenNodes = new RADNode [children.length];

            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof RADMenuComponent) {
                    childrenNodes[i] = new RADMenuNode ();
                } else if (children[i] instanceof RADMenuItemComponent) {
                    childrenNodes[i] = new RADMenuItemNode ();
                } else {
                    childrenNodes[i] = new RADNode (); // [PENDING - can X2 menu contain RADNodes?]
                }
                convertComponentBack (children[i], childrenNodes[i]);
            }

            nodeArray.array = childrenNodes;

            ((RADMenuNode)contNode).nodeArray = nodeArray;
        }

        if (contNode instanceof RADContainerNode) {
            // >> RADContainerNode
            //  1. nodeArray [RADNodeArray]
            //  2. designLayout [DesignLayout]
            //  3. radLayoutNode [RADLayoutNode]

            RADNodeArray nodeArray = new RADNodeArray ();
            // >> RADNodeArray
            // 1. containerNode [RADContainer]
            // 2. array [Object[] ]
            nodeArray.containerNode = (RADContainerNode)contNode;
            RADComponent[] children = ((RADVisualContainer)comp).getSubComponents ();
            RADNode[] childrenNodes = new RADNode [children.length];

            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof RADVisualContainer) {
                    childrenNodes[i] = new RADContainerNode ();
                } else if (children[i] instanceof RADVisualComponent) {
                    childrenNodes[i] = new RADVisualNode ();
                } else {
                    childrenNodes[i] = new RADNode ();
                }
                convertComponentBack (children[i], childrenNodes[i]);
            }

            nodeArray.array = childrenNodes;

            DesignLayout designLayout = ((RADVisualContainer)comp).getDesignLayout ();

            org.netbeans.modules.form.compat2.RADLayoutNode layoutNode = new org.netbeans.modules.form.compat2.RADLayoutNode ();
            // >> RADLayoutNode
            // 1. designLayout [DesignLayout]
            // 2. layoutName [String] (can be "" ???) // NOI18N
            layoutNode.designLayout = designLayout;
            layoutNode.layoutName = ""; // [PENDING - check] // NOI18N

            ((RADContainerNode)contNode).nodeArray = nodeArray;
            ((RADContainerNode)contNode).designLayout = designLayout;
            ((RADContainerNode)contNode).radLayoutNode = layoutNode;
        }

        if (contNode instanceof RADVisualNode) {
            // >> RADVisualNode
            // 1. designLayout [DesignLayout]
            // 2. layoutName [String] (can be "" ???) // NOI18N
            ((RADVisualNode)contNode).constraints = ((RADVisualComponent)comp).getConstraintsMap ();
        }

        Hashtable changedProperties = new Hashtable ();
        Hashtable handlersTable = new Hashtable ();
        HashMap propertiesMap = new HashMap ();
        RADComponent.RADProperty[] newProperties = comp.getAllProperties ();
        for (int i = 0; i < newProperties.length; i++) {
            if (newProperties[i].isChanged ()) {
                Object changedValue = null;
                try {
                    changedValue = newProperties[i].getValue ();
                } catch (Exception e) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
                    // problem getting value => ignore this property
                    continue;
                }
                PropertyDescriptor desc = newProperties[i].getPropertyDescriptor ();
                String propName = desc.getName ();
                changedProperties.put (propName, propName);
                propertiesMap.put (propName, changedValue);
            }
        }

        // >> RADNode
        // 1. beanClass [Class]
        // 2. changedProperties [Hashtable]
        // 3. handlersTable [Hashtable]
        // 4. componentName [String]
        // 5. hasHiddenState [boolean]
        // 6. propertiesMap [Map]
        contNode.beanClass = comp.getBeanClass ();
        contNode.changedProperties = changedProperties;
        contNode.handlersTable = comp.getEventsList ().getEventNames ();
        contNode.componentName = comp.getName ();
        contNode.hasHiddenState = false; // [PENDING]
        contNode.propertiesMap = propertiesMap;
    }

    // -----------------------------------------------------------------------------
    // X2 Object Streams
    public static class X2ObjectOutputStream extends ObjectOutputStream {
        public X2ObjectOutputStream (OutputStream os) throws IOException {
            super (os);
        }

        /** Calls super annotateClass and then write the index of the classloader of this class.
        * @see NbObjectInputStream
        */
        protected void annotateClass(Class cl) throws IOException {
            super.annotateClass(cl);
            writeByte(LOADER_DEFAULT);
        }
    }

    // -----------------------------------------------------------------------------
    // Safe Serialization

    public static void writeSafely (ObjectOutput oo, Object obj)
    throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream (200);
        ObjectOutputStream oos = new X2ObjectOutputStream (bos); // [PENDING!!!]
        oos.writeObject (obj);
        oos.flush ();
        bos.close ();

        oo.writeInt (bos.size ());
        oo.write (bos.toByteArray ());
    }

    public static Object readSafely (ObjectInput oi)
    throws IOException, ClassNotFoundException {
        int size = oi.readInt ();
        byte[] byteArray = new byte [size];
        oi.readFully (byteArray, 0, size);

        ByteArrayInputStream bis = new ByteArrayInputStream (byteArray);
        ObjectInputStream ois = new org.openide.util.io.NbObjectInputStream (bis);
        Object obj = ois.readObject ();
        bis.close ();

        return obj;
    }

}

/*
 * Log
 *  30   Gandalf   1.29        4/14/00  Jesse Glick     Modified to permit 
 *       loading of forms with old package names. On behalf of Trung.
 *  29   Gandalf   1.28        1/13/00  Ian Formanek    NOI18N #2
 *  28   Gandalf   1.27        1/5/00   Ian Formanek    NOI18N
 *  27   Gandalf   1.26        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  26   Gandalf   1.25        10/8/99  Petr Hamernik   closing input stream 
 *       fixed (canLoad method)
 *  25   Gandalf   1.24        9/24/99  Ian Formanek    New system of changed 
 *       properties in RADComponent - Fixes bug 3584 - Form Editor should try to
 *       enforce more order in the XML elements in .form.
 *  24   Gandalf   1.23        9/10/99  Ian Formanek    Separators improved
 *  23   Gandalf   1.22        9/8/99   Ian Formanek    Fixed last change
 *  22   Gandalf   1.21        9/7/99   Ian Formanek    Improved error messages 
 *       if form loading fails
 *  21   Gandalf   1.20        7/25/99  Ian Formanek    Variables management 
 *       moved to RADComponent
 *  20   Gandalf   1.19        7/14/99  Ian Formanek    saving menu
 *  19   Gandalf   1.18        7/14/99  Ian Formanek    loaded menu is set on 
 *       the form
 *  18   Gandalf   1.17        7/14/99  Ian Formanek    Loads Tuborg menus
 *  17   Gandalf   1.16        7/11/99  Ian Formanek    supportsAdvancedFeatures
 *       added
 *  16   Gandalf   1.15        7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  15   Gandalf   1.14        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  14   Gandalf   1.13        5/31/99  Ian Formanek    Fixed bug which caused 
 *       the stored properties to be nulls
 *  13   Gandalf   1.12        5/24/99  Ian Formanek    Non-Visual components
 *  12   Gandalf   1.11        5/24/99  Ian Formanek    
 *  11   Gandalf   1.10        5/16/99  Ian Formanek    Fixed bug 1829 - 
 *       Duplicate variable declaration .
 *  10   Gandalf   1.9         5/16/99  Ian Formanek    Persistence 
 *       failure-proofness improved
 *  9    Gandalf   1.8         5/16/99  Ian Formanek    
 *  8    Gandalf   1.7         5/15/99  Ian Formanek    
 *  7    Gandalf   1.6         5/15/99  Ian Formanek    Fixed problem which 
 *       prevented opening forms in Build 321
 *  6    Gandalf   1.5         5/14/99  Ian Formanek    
 *  5    Gandalf   1.4         5/13/99  Ian Formanek    
 *  4    Gandalf   1.3         5/12/99  Ian Formanek    
 *  3    Gandalf   1.2         5/11/99  Ian Formanek    Build 318 version
 *  2    Gandalf   1.1         5/10/99  Ian Formanek    
 *  1    Gandalf   1.0         5/4/99   Ian Formanek    
 * $
 */
