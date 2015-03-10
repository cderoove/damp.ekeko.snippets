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

package org.netbeans.modules.vcs;
import org.netbeans.modules.vcs.*;
import org.netbeans.modules.vcs.util.*;
import org.openide.util.*;
import org.openide.filesystems.FileObject;

import java.util.*;
import java.io.*;
import java.text.*;

import org.netbeans.modules.vcs.cmdline.UserCommand;

/**
 *
 * @author  Pavel Buzek
 * @version 
 */

public class VcsConfigVariable extends Object implements Cloneable, Serializable {
    private static Debug E=new Debug("VcsConfigVariable", true); // NOI18N
    private static Debug D=E;

    /**
     * The variable name.
     */
    private String name;          // The variable name
    /**
     * The label of this variable in the Customizer.
     */
    private String label;         // The label of that variable in the Customizer
    /**
     * The value of the variable.
     */
    private String value;         // The value of this variable
    /**
     * Whether this variable is basic or not. Basic variables can be set in the Customizer.
     */
    private boolean basic;       // Whether this variable is basic or not (basic variables can be set in the Customizer)
    /**
     * Whether this variable is a local file. Browse buttom will be created for this variable.
     */
    private boolean localFile;   // Whether this variable is a local file. Browse buttom will be created for this variable.
    /**
     * Whether this variable is a local directory. Browse buttom will be created for this variable.
     */
    private boolean localDir;    // Whether this variable is a local directory. Browse buttom will be created for this variable.
    /**
     * The custom selector for this variable.
     * Select button will be created in the customizer and selector executed on its action.
     */
    private String customSelector;// The custom selector for this variable
    /**
     * The order of the variable in the Customizer.
     */
    private int order;           // The order of the variable in the Customizer

    static final long serialVersionUID =4230769028627379053L;

    /** Creates new VcsConfigVariable with zero order.
    * @param name the variable name
    * @param label the label of that variable in the Customizer
    * @param value the value of this variable
    * @param basic whether this variable is basic or not. Basic variables can be set in the Customizer.
    * @param localFile whether this variable is a local file. Browse buttom will be created for this variable.
    * @param localDir whether this variable is a local directory. Browse buttom will be created for this variable.
    * @param customSelector the custom selector for this variable. It can be a subclass of <code>VcsVariableSelector</code> or an executable.
    */
    public VcsConfigVariable(String name, String label, String value,
                             boolean basic, boolean localFile, boolean localDir,
                             String customSelector) {
        this(name, label, value, basic, localFile, localDir, customSelector, 0);
    }

    /** Creates new VcsConfigVariable
    * @param name the variable name
    * @param label the label of that variable in the Customizer
    * @param value the value of this variable
    * @param basic whether this variable is basic or not. Basic variables can be set in the Customizer.
    * @param localFile whether this variable is a local file. Browse buttom will be created for this variable.
    * @param localDir whether this variable is a local directory. Browse buttom will be created for this variable.
    * @param customSelector the custom selector for this variable. It can be a subclass of <code>VcsVariableSelector</code> or an executable.
    * @param order the order of this variable in the Customizer.
    */
    public VcsConfigVariable(String name, String label, String value,
                             boolean basic, boolean localFile, boolean localDir,
                             String customSelector, int order) {
        this.name = name;
        this.label = label;
        this.value = value;
        this.basic = basic;
        this.localFile = localFile;
        this.localDir = localDir;
        this.customSelector = customSelector;
        this.order = order;
    }

    public String getName () { return name; }
    public void setName (String name) { this.name = name; }
    public String getLabel () { return label;  }
    public void setLabel (String label) { this.label = label;  }
    public String getValue () { return value;  }
    public void setValue (String value) { this.value = value;  }
    public boolean isBasic () { return basic;  }
    public void setBasic (boolean basic) { this.basic = basic;  }
    public boolean isLocalFile() { return localFile;  }
    public void setLocalFile (boolean localFile) { this.localFile = localFile;  }
    public boolean isLocalDir() { return localDir;  }
    public void setLocalDir (boolean localDir) { this.localDir = localDir;  }
    public String getCustomSelector () { return customSelector; }
    public void setCustomSelector (String customSelector) { this.customSelector = customSelector; }
    public void setOrder(int order) { this.order = order; }
    public int getOrder() { return this.order; }

    public String toString () {
        String strBasic = ""; // NOI18N
        if (isBasic ()) strBasic = "(basic)"; // NOI18N
        if (isLocalFile ()) strBasic += "(local file)"; // NOI18N
        if (isLocalDir ()) strBasic += "(local directory)"; // NOI18N
        return name+"("+label+")"+strBasic+"="+value; // NOI18N
    }

    public Object clone () {
        return new VcsConfigVariable (name, label, value, basic, localFile, localDir, customSelector, order);
    }


    /** Read list of VCS variables from properties. Variables are stored as
    * var.<NAME>.value and optionaly var.<NAME>.label, var.<NAME>.basic,
    * var.<NAME>.localFile or var.<NAME>.localDir.
    * If there is only value specified, label is empty string and basic, localFile
    * and localDir are false.
    */
    public static Vector readVariables(Properties props){
        Vector result=new Vector(20);
        String VAR_PREFIX = "var."; // NOI18N
        for(Iterator iter=props.keySet().iterator(); iter.hasNext();){
            String key=(String)iter.next();
            if(key.startsWith(VAR_PREFIX) && key.endsWith(".value")) { // NOI18N
                int startIndex = VAR_PREFIX.length ();
                int endIndex=key.length()-".value".length (); // NOI18N

                String name=key.substring( startIndex, endIndex );
                String value=(String)props.get(key);

                String label=(String)props.get(VAR_PREFIX + name + ".label"); // NOI18N
                if(label==null) label=""; // NOI18N

                String strBasic=(String)props.get(VAR_PREFIX + name + ".basic"); // NOI18N
                boolean basic = (strBasic!=null) && (strBasic.equalsIgnoreCase ("true")); // NOI18N

                String strLocalFile=(String)props.get(VAR_PREFIX + name + ".localFile"); // NOI18N
                boolean localFile = (strLocalFile != null) && (strLocalFile.equalsIgnoreCase ("true")); // NOI18N

                String strLocalDir=(String)props.get(VAR_PREFIX + name + ".localDir"); // NOI18N
                boolean localDir = (strLocalDir != null) && (strLocalDir.equalsIgnoreCase ("true")); // NOI18N

                String customSelector=(String)props.get(VAR_PREFIX + name + ".selector"); // NOI18N
                if (customSelector == null) customSelector = "";

                String orderStr = (String) props.get(VAR_PREFIX + name + ".order"); // NOI18N
                int order = -1;
                if (orderStr != null) {
                    try {
                        order = Integer.parseInt(orderStr);
                    } catch (NumberFormatException e) {
                        // ignoring
                        order = -1;
                    }
                }
                result.addElement(new VcsConfigVariable (name, label, value, basic, localFile, localDir, customSelector, order));
            }
        }
        result = UserCommand.sortCommands(result);
        return result;
    }

    /**
     * Write the configuration properties into the file.
     * @param file the file into which the properties will be stored.
     * @param label the label to use.
     * @param vars the variables to save.
     * @param advanced the advanced configuration properties (commands).
     * @param cust the advanced customizer used to write the advanced properties.
     */
    public static void writeConfiguration (FileObject file, String label, Vector vars,
                                           Object advanced, VcsAdvancedCustomizer cust) {
        Properties props=new Properties();
        props.setProperty ("label", label); // NOI18N
        props.setProperty ("debug", "true"); // NOI18N
        for(int i=0; i<vars.size (); i++) {
            VcsConfigVariable var = (VcsConfigVariable) vars.get (i);
            props.setProperty ("var." + var.getName () + ".value", var.getValue ()); // NOI18N
            if(!var.getLabel ().equals ("")) { // NOI18N
                props.setProperty ("var." + var.getName () + ".label", var.getLabel ()); // NOI18N
                props.setProperty ("var." + var.getName () + ".basic", "" + var.isBasic ()); // NOI18N
            } else if(var.isBasic ())
                props.setProperty ("var." + var.getName () + ".basic", "true"); // NOI18N
            props.setProperty ("var." + var.getName() + ".localFile", "" + var.isLocalFile()); // NOI18N
            props.setProperty ("var." + var.getName() + ".localDir", "" + var.isLocalDir()); // NOI18N
            props.setProperty ("var." + var.getName() + ".selector", "" + var.getCustomSelector()); // NOI18N
            props.setProperty ("var." + var.getName() + ".order", "" + var.getOrder()); // NOI18N
        }
        cust.writeConfig (props, advanced);
        try{
            OutputStream out = file.getOutputStream(file.lock());
            props.store (out, g("MSG_User_defined_configuration")); // NOI18N
            out.close();
        }
        catch(IOException e){
            E.err(e,g("EXC_Problems_while_writting_user_defined_configuration",file.getName())); // NOI18N
        }
    }

    /** Read list of available confugurations from the directory.
    * All files with extension ".properties" are considered to be configurations.
    * @return the available configurations.
    */
    public static Vector readConfigurations(FileObject file){
        Vector res = new Vector(5);
        FileObject[] ch = file.getChildren();
        for(int i = 0; i < ch.length; i++) {
            if (ch[i].getExt().equalsIgnoreCase("properties")) res.addElement(ch[i].getName()+"."+ch[i].getExt());
        }
        return res;
    }

    /** Open file and load properties from it.
     * @param configRoot the directory which contains properties.
     * @param name the name of properties to read.
     */
    public static Properties readPredefinedProperties(FileObject configRoot, String name){
        Properties props=new Properties();
        FileObject config = configRoot.getFileObject(name);
        if (config == null) {
            E.err(g("EXC_Problems_while_reading_predefined_properties",name)); // NOI18N
            return props;
        }
        try{
            InputStream in = config.getInputStream();
            props.load(in);
            in.close();
        }
        catch(FileNotFoundException e) {
            E.err(g("EXC_Problems_while_reading_predefined_properties",name)); // NOI18N
        }
        catch(IOException e){
            E.err(g("EXC_Problems_while_reading_predefined_properties",name)); // NOI18N
        }
        return props;
    }

    /*
    public static void main (String args[]) {
      if(args.length >0) {
        Properties props = readPredefinedProperties (args[0]);
        Enumeration en = readVariables (props).elements ();
        while(en.hasMoreElements ()) {
          VcsConfigVariable var = (VcsConfigVariable) en.nextElement ();
          System.out.println ("var(name="+var.getName ()+", label="+var.getLabel ()+", basic="+var.isBasic ()+", value="+var.getValue ()); // NOI18N
        }
      }
}
    */

    //-------------------------------------------
    static String g(String s) {
        return NbBundle.getBundle
               ("org.netbeans.modules.vcs.cmdline.Bundle").getString (s);
    }
    static String g(String s, Object obj) {
        return MessageFormat.format (g(s), new Object[] { obj });
    }
    static String g(String s, Object obj1, Object obj2) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2 });
    }
    static String g(String s, Object obj1, Object obj2, Object obj3) {
        return MessageFormat.format (g(s), new Object[] { obj1, obj2, obj3 });
    }
}

/*
 * Log
 *  11   Gandalf-post-FCS1.8.1.1     4/4/00   Martin Entlicher order property not 
 *       showed
 *  10   Gandalf-post-FCS1.8.1.0     3/23/00  Martin Entlicher Javadoc added, order 
 *       property added, custom selector added.
 *  9    Gandalf   1.8         3/8/00   Martin Entlicher VCS properties read from
 *       filesystem
 *  8    Gandalf   1.7         1/17/00  Martin Entlicher Internationalization
 *  7    Gandalf   1.6         1/6/00   Martin Entlicher 
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         11/24/99 Martin Entlicher Added localFile and 
 *       localDir properties.
 *  4    Gandalf   1.3         10/25/99 Pavel Buzek     copyright and log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
