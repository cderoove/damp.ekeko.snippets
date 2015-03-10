/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import java.awt.datatransfer.StringSelection;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.text.MessageFormat;

import org.openide.nodes.Node;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.TopManager;

import net.jini.core.lookup.*;
import net.jini.core.entry.Entry;

/**
 *
 */
public class ServiceTemplateAction extends CookieAction {
    /**
     * @associates String 
     */
    ArrayList locators, entryClasses, serviceTypes, groups, services, serviceEntries, entryVars;
    static int entryNum, phaseNum;
    static String indent = "  ";

    StringBuffer code = new StringBuffer(4096);
    MessageFormat decl = new MessageFormat("{0} {1} = new {0} ({2});\n");
    MessageFormat declArray = new MessageFormat("{0}[] {1} = new {0}[] '{'{2}'}';\n");
    MessageFormat declNull = new MessageFormat("{0} {1} = null;\n");

    String varName, sIDvarName, stvarName, evarName;

    protected void performAction(Node[] activatedNodes) {

        code.delete(0, code.length());

        entryClasses = new ArrayList(10);
        serviceTypes = new ArrayList(10);
        services = new ArrayList(10);
        serviceEntries = new ArrayList(10);
        entryVars = new ArrayList(10);

        Iterator it;

        for(int i = 0; i < activatedNodes.length; i++) {
            ServiceTemplateCookie stc = (ServiceTemplateCookie)activatedNodes[i].getCookie(ServiceTemplateCookie.class);


            // Ugly, isn't it?

            switch (stc.getType()) {
                //      case LOOKUP_LOCATOR:

            case ServiceTemplateCookie.ENTRY_CLASSES: {
                    Class[] classes = (Class[])stc.getSource();
                    for(int j = 0; j < classes.length; j++) entryClasses.add(classes[j]);
                }; break;

            case ServiceTemplateCookie.ENTRY_CLASS_ITEM: {
                    Class clazz = (Class)stc.getSource();
                    entryClasses.add(clazz);
                }; break;

            case ServiceTemplateCookie.ENTRY_OBJECT_ITEM: {
                    serviceEntries.add(stc.getSource());
                }; break;

            case ServiceTemplateCookie.SERVICE_TYPES: {
                    Class[] classes = (Class[])stc.getSource();
                    for(int j = 0; j < classes.length; j++) serviceTypes.add(classes[j]);
                }; break;

            case ServiceTemplateCookie.SERVICE_TYPE_ITEM: {
                    Class clazz = (Class)stc.getSource();
                    serviceTypes.add(clazz);
                }; break;

            case ServiceTemplateCookie.SERVICES: {
                    ServiceMatches sm = (ServiceMatches)stc.getSource();
                    for(int j = 0; j < sm.totalMatches; j++) services.add(sm.items[j]);
                }; break;

            case ServiceTemplateCookie.SERVICE_ITEM:
                services.add(stc.getSource());
            }
        }

        // service ID
        sIDvarName = varName = "serviceID" + phaseNum;
        if (services.size() > 0) {
            ServiceItem si = (ServiceItem) services.get(0);
            String param = "0x" + Long.toHexString(si.serviceID.getMostSignificantBits()) + "L, 0x" +
                           Long.toHexString(si.serviceID.getLeastSignificantBits()) + "L";

            Object obj[] = new Object[] {
                               ServiceID.class.getName(),
                               varName,
                               param
                           };
            code.append(decl.format(obj));
        } else {
            code.append(declNull.format(new Object[] { ServiceID.class.getName(), varName }));
        }
        // service ID

        code.append('\n');

        // service types

        stvarName = varName = "serviceTypes" + phaseNum;
        if (serviceTypes.size() > 0) {
            StringBuffer stParams = new StringBuffer(1024);
            stParams.append('\n');
            it = serviceTypes.iterator();
            while (it.hasNext()) {
                Class cl = (Class) it.next();
                stParams.append(indent);
                stParams.append(cl.getName());
                stParams.append(".class");
                if (it.hasNext()) stParams.append(",");
                stParams.append('\n');
            }
            code.append(declArray.format(new Object[] {
                                             Class.class.getName(),
                                             varName,
                                             stParams,
                                         }));
        } else {
            code.append(declNull.format(new Object[] {
                                            Class.class.getName() + "[]",
                                            varName
                                        }));
        }

        //service types

        code.append('\n');

        // entries
        it = serviceEntries.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            String className = entry.getClass().getName();
            varName = createEntryVar(extractClassName(className), entryNum++ );
            entryVars.add(varName);
            removeFromList(entryClasses, entry.getClass());
            code.append(decl.format(new Object[] { className, varName, "" }));
            code.append(fillEntry(varName, entry.getClass(), entry));
            code.append('\n');
        }
        it = entryClasses.iterator();
        while (it.hasNext()) {
            Class eclass = (Class) it.next();
            String className = eclass.getName();
            varName = createEntryVar(extractClassName(className), entryNum++ );
            entryVars.add(varName);
            code.append(decl.format(new Object[] { className, varName, "" }));
            code.append(fillEntry(varName, eclass, null));
            code.append('\n');
        }
        // entries

        //
        evarName = varName = "entries" + phaseNum;
        if (entryVars.size() > 0) {
            StringBuffer stParams = new StringBuffer(1024);
            stParams.append('\n');
            it = entryVars.iterator();
            while (it.hasNext()) {
                String entryName = (String) it.next();
                stParams.append(indent);
                stParams.append(entryName);
                if (it.hasNext()) stParams.append(",");
                stParams.append('\n');
            }
            code.append(declArray.format(new Object[] {
                                             Entry.class.getName(),
                                             varName,
                                             stParams,
                                         }));
        } else {
            code.append(declNull.format(new Object[] {
                                            Entry.class.getName() + "[]",
                                            varName
                                        }));
        }

        //

        code.append('\n');
        code.append(decl.format(new Object[] {
                                    ServiceTemplate.class.getName(),
                                    "serviceTemplate" + phaseNum,
                                    sIDvarName + ", " + stvarName + ", " + evarName,
                                }));
        code.append('\n');

        //    System.out.println(code.toString());

        TopManager.getDefault().getClipboard().setContents(new StringSelection(code.toString()), null);
    }

    //
    public static boolean isWrapper(Class clazz) {
        String name = clazz.getName();
        if (name.equals("java.lang.Boolean") ||
                name.equals("java.lang.Byte") ||
                name.equals("java.lang.Character") ||
                name.equals("java.lang.Double") ||
                name.equals("java.lang.Float") ||
                name.equals("java.lang.Integer") ||
                name.equals("java.lang.Long") ||
                name.equals("java.lang.Short") ||
                name.equals("java.lang.String")) return true;
        return false;
    }

    public String fillEntry(String varName, Class clazz, Object obj) {
        StringBuffer entrycode = new StringBuffer(128);
        MessageFormat var = new MessageFormat("{0}.{1} = {2};\n");
        Object data[] = new Object[3];
        data[0] = varName;
        Field[] fields = clazz.getFields();
        for(int i = 0; i < fields.length; i++) {
            if (!valid(fields[i])) continue;
            data[1] = fields[i].getName();
            data[2] = "null";
            if (obj != null) {
                try {
                    data[2] = valueToString(fields[i], obj);
                } catch (Exception ex) {
                }
            }
            entrycode.append(var.format(data));
        }
        return entrycode.toString();
    }

    public static String valueToString(Field f, Object obj) throws Exception {
        Class clazz = f.getType();
        String name = clazz.getName();
        if (isWrapper(clazz)) {
            Object val = f.get(obj);
            if (val == null) return "null";
            String value = val.toString();
            if (name.equals("java.lang.String")) return "\"" +  value + "\"";
            if (name.equals("java.lang.Character")) return "new java.lang.Character('" + value + "')";
            if (name.equals("java.lang.Byte")) return "new java.lang.Byte((byte)" + value + ")";
            if (name.equals("java.lang.Short")) return "new java.lang.Short((short)" + value + ")";
            if (name.equals("java.lang.Float")) return "new java.lang.Float(" + value + "F)";
            if (name.equals("java.lang.Long")) return "new java.lang.Long(" + value + "L)";
            return "new " + name + "(" + value + ")";
        }

        PropertyEditor pe = PropertyEditorManager.findEditor(f.getType());
        if (pe != null) {
            pe.setValue(f.get(obj));
            String initS =  pe.getJavaInitializationString();
            if (initS != null) {
                return initS;
            }
        }

        return "null";
    }

    public static boolean valid(Field f) {
        return (f.getModifiers() & (Modifier.STATIC|Modifier.FINAL)) == 0;
    }

    public String createEntryVar(String classname, int number) {
        StringBuffer sb = new StringBuffer(64);
        sb.append(classname);
        sb.append(number);
        sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        return sb.toString();
    }

    public static String extractClassName(String name) {
        int i = name.lastIndexOf('.');

        return name.substring(i + 1);
    }

    public static void removeFromList(List list, Object key) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            if (key.equals(it.next())) it.remove();
        }
    }




    //


    /*
    protected boolean enable(Node[] activatedNodes) {
      return true;
}
    */

    protected Class[] cookieClasses() {
        return new Class[] { ServiceTemplateCookie.class };
    }

    protected int mode() {
        return MODE_ALL;
    }

    public String getName() {
        return Util.getString("PROP_Service_Template_Action_Name");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ServiceTemplateAction.class);
    }

    protected HashSet parseEntry(Entry entry) {
        HashSet hs = new HashSet();
        return hs;
    }
}


/*
* <<Log>>
*  3    Gandalf   1.2         2/2/00   Petr Kuzel      Jini module upon 1.1alpha
*  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         6/11/99  Martin Ryzl     
* $ 
*/ 

