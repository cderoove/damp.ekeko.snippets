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

package org.netbeans.modules.vcs.cmdline;
import java.io.*;
import java.net.*;
import java.util.*;
import java.beans.*;
import java.text.*;

import org.openide.util.*;

import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.VcsConfigVariable;

/** Single user defined command.
 * 
 * @author Michal Fadljevic, Pavel Buzek
 */
//-------------------------------------------
public class UserCommand extends Object implements Serializable, Cloneable {
    private static Debug E=new Debug("UserCommand", true); // NOI18N
    private static Debug D=E;

    // Properties:
    private String name="";          // e.g. "CHECKIN" // NOI18N
    private String label="";         // e.g. "Check Int" // NOI18N
    private String exec="";          // e.g. "${STCMD} ci -p ${PROJECT} ..." // NOI18N
    private String input="";         // e.g. "Cancel\n" // NOI18N
    private long timeout=240000;      // e.g. 20000
    private String dataRegex="(.*$)"; // e.g. (.*$) // NOI18N
    private String errorRegex="";    // e.g. "Error" // NOI18N
    /**
     * If true, then display output of this command in a window.
     */
    private boolean displayOutput = false; // display output in command in window
    /**
     * If true, do an automatic refresh of the current folder after this command.
     */
    private boolean doRefresh = false;  // whether to do refresh after this command
    /**
     * If true, this command is visible on directories.
     */
    private boolean onDir = true;  // the command is visible on directories
    /**
     * If true, this command is visible on files.
     */
    private boolean onFile = true;  // the command is visible on files
    /**
     * If true, this command is visible only on the root of the filesystem.
     */
    private boolean onRoot = false;  // the command is visible ONLY on the root of the filesystem.
    /**
     * The text of a confirmation message printed before the command is executed.
     */
    private String confirmationMsg = ""; // the confirmation message which is printed before the command executes

    /**
     * The order of this command in the pop-up action menu.
     */
    private int order=-1; // The order in the popup menu

    // for name=="LIST" // NOI18N
    private int statusIndex=-1;
    private int lockerIndex=-1;
    private int attrIndex=-1;
    private int dateIndex=-1;
    private int timeIndex=-1;
    private int sizeIndex=-1;

    private int fileNameIndex=-1;

    static final long serialVersionUID =6658759487911693730L;

    //-------------------------------------------
    public UserCommand(){
        // I am JavaBean...
    }


    //-------------------------------------------
    public Object clone(){
        UserCommand uc = new UserCommand();
        uc.name = name;
        uc.label = label;
        uc.exec = exec;
        uc.input = new String(input);
        uc.timeout = timeout;
        uc.dataRegex = dataRegex;
        uc.errorRegex = errorRegex;
        uc.displayOutput = displayOutput;
        uc.doRefresh = doRefresh;
        uc.onFile = onFile;
        uc.onDir = onDir;
        uc.onRoot = onRoot;
        uc.confirmationMsg = new String(confirmationMsg);
        uc.order = order;

        uc.statusIndex = statusIndex;
        uc.lockerIndex = lockerIndex;
        uc.attrIndex = attrIndex;
        uc.dateIndex = dateIndex;
        uc.timeIndex = timeIndex;
        uc.sizeIndex = sizeIndex;
        uc.fileNameIndex = fileNameIndex;
        return uc;
    }


    //-------------------------------------------
    public void   setName(String name){ this.name=name;}
    public String getName(){ return name;}
    public void   setLabel(String label){ this.label=label;}
    public String getLabel(){ return label;}
    public void   setExec(String exec){ this.exec=exec;}
    public String getExec(){ return exec;}
    public void   setInput(String input){ this.input=input;}
    public String getInput(){ return input;}
    public void   setTimeout(long timeout){ this.timeout=timeout;}
    public long   getTimeout(){ return timeout;}
    public void   setDataRegex(String dataRegex){ this.dataRegex=dataRegex;}
    public String getDataRegex(){ return dataRegex;}
    public void   setErrorRegex(String errorRegex){ this.errorRegex=errorRegex;}
    public String getErrorRegex(){ return errorRegex;}
    public void   setDisplayOutput (boolean displayOutput) { this.displayOutput=displayOutput; }
    public boolean getDisplayOutput () { return displayOutput;}
    public void   setDoRefresh (boolean doRefresh) { this.doRefresh=doRefresh; }
    public boolean getDoRefresh () { return doRefresh; }
    public void   setOrder (int order) { this.order = order; }
    public int    getOrder () { return order; }
    public boolean getOnFile () { return onFile; }
    public void    setOnFile (boolean onFile) { this.onFile = onFile; }
    public boolean getOnDir () {return onDir; }
    public void    setOnDir (boolean onDir) { this.onDir = onDir; }
    public boolean getOnRoot () { return onRoot; }
    public void    setOnRoot (boolean onRoot) { this.onRoot = onRoot; }
    public String getConfirmationMsg() { return confirmationMsg; }
    public void setConfirmationMsg(String confirmationMsg) { this.confirmationMsg = confirmationMsg; }


    //-------------------------------------------
    public void setStatus(int index){ this.statusIndex=index; }
    public int  getStatus(){ return statusIndex; }
    public void setLocker(int index){ this.lockerIndex=index; }
    public int  getLocker(){ return lockerIndex; }
    public void setAttr(int index){ this.attrIndex=index; }
    public int  getAttr(){ return attrIndex; }
    public void setDate(int index){ this.dateIndex=index; }
    public int  getDate(){ return dateIndex; }
    public void setTime(int index){ this.timeIndex=index; }
    public int  getTime(){ return timeIndex; }
    public void setSize(int index){ this.sizeIndex=index; }
    public int  getSize(){ return sizeIndex; }
    public void setFileName(int index){ this.fileNameIndex=index; }
    public int  getFileName(){ return fileNameIndex; }


    //-------------------------------------------
    public String toString(){
        return name+"("+label+")("+timeout+")='"+exec+"'"+ // NOI18N
               ( name.equals("LIST") ? // NOI18N
                 ("[["+statusIndex+","+lockerIndex+","+ attrIndex+","+ // NOI18N
                  dateIndex+","+ timeIndex+", "+ sizeIndex+","+fileNameIndex+"]]") : ""); // NOI18N
    }



    //-------------------------------------------
    private static int positionToInt(String s){
        int result=-1;
        try{
            result=Integer.parseInt(s);
        }
        catch(NumberFormatException e){
            E.err(g("EXC_Invalid_format_of_number",s)); // NOI18N
        }
        return result;
    }

    /**
     * Sort a vector of commands or variables by the order property.
     * @param commands the commands or variables to sort
     * @return new sorted vector of commands or variables
     */
    public static Vector sortCommands(Vector commands) {
        //D.deb("sortCommands ()"); // NOI18N
        Vector sorted;
        //D.deb("commands = "+ commands); // NOI18N
        if (commands == null) return commands;
        Object[] cmds = null;
        cmds = (Object[]) commands.toArray();
        //D.deb("Doing sort ..."); // NOI18N
        java.util.Arrays.sort(cmds, new Comparator() {
                                  public int compare(Object o1, Object o2) {
                                      if (o1 instanceof UserCommand)
                                          return ((UserCommand) o1).getOrder() - ((UserCommand) o2).getOrder();
                                      if (o1 instanceof VcsConfigVariable)
                                          return ((VcsConfigVariable) o1).getOrder() - ((VcsConfigVariable) o2).getOrder();
                                      return 0; // the elements are not known to me
                                  }
                                  public boolean equals(Object o) {
                                      return false;
                                  }
                              });
        //D.deb("Sort finished."); // NOI18N
        sorted = new Vector();
        for(int i = 0; i < cmds.length; i++) {
            sorted.addElement(cmds[i]);
        }
        //D.deb("sorted vector = "+sorted); // NOI18N
        return sorted;
    }

    /**
     * Set the order property of each element in the vector to the
     * proper values if some values are negative.
     * The Vector has to be sorted by <CODE>sortCommands</CODE>.
     * @param commands the vector of <CODE>UserCommand</CODE> elements.
     */
    public static void setOrder(Vector commands) {
        //D.deb("setOrder()"); // NOI18N
        int len = commands.size();
        if (len <= 0) return;
        int nonNegativeIndex = 0;
        UserCommand uc = (UserCommand) commands.get(nonNegativeIndex);
        while (uc != null && uc.getOrder() < 0) {
            nonNegativeIndex++;
            uc = (nonNegativeIndex < len) ? (UserCommand) commands.get(nonNegativeIndex) : null;
        }
        //D.deb("nonNegativeIndex = "+nonNegativeIndex); // NOI18N
        if (nonNegativeIndex == 0) return; // All values are non negative
        if (uc != null) {
            int first = uc.getOrder();
            if (first < nonNegativeIndex) {
                shiftCommands(commands, nonNegativeIndex, nonNegativeIndex - first);
            }
        }
        for(int i = 0; i < nonNegativeIndex; i++) {
            uc = (UserCommand) commands.get(i);
            //D.deb("setting order for "+uc+" to "+i); // NOI18N
            uc.setOrder(i);
        }
    }

    public static void shiftCommands(Vector commands, int index, int shift) {
        for(int i = index; i < commands.size(); i++) {
            UserCommand uc = (UserCommand) commands.get(i);
            if (uc != null) uc.setOrder(uc.getOrder() + shift);
        }
    }

    //-------------------------------------------
    public static Vector readCommands(Properties props){
        Vector result=new Vector(20);

        for(Iterator iter=props.keySet().iterator(); iter.hasNext();){
            String key=(String)iter.next();
            if( key.startsWith("cmd")==true && // NOI18N
                    key.endsWith(".label")==true // NOI18N
              ){
                int startIndex="cmd".length()+1; // NOI18N
                int endIndex=key.length()-".label".length(); // NOI18N

                String name=key.substring( startIndex, endIndex );
                String label=(String)props.get(key);

                UserCommand uc=new UserCommand();
                uc.setName(name);
                uc.setLabel(label);

                String exec=(String)props.get("cmd."+uc.getName()+".exec"); // NOI18N
                if( exec != null ){
                    uc.setExec(exec);
                }

                String input=(String)props.get("cmd."+uc.getName()+".input"); // NOI18N
                if( input != null ){
                    uc.setInput(input);
                }

                String timeoutStr=(String)props.get("cmd."+uc.getName()+".timeout"); // NOI18N
                if( timeoutStr != null ){
                    try{
                        uc.setTimeout(Long.parseLong(timeoutStr));
                    }
                    catch (NumberFormatException e){
                        E.err(g("EXC_Invalid_timeout_value",timeoutStr)); // NOI18N
                    }
                }

                String dataRegex=(String)props.get("cmd."+uc.getName()+".data.regex"); // NOI18N
                if( dataRegex != null ){
                    uc.setDataRegex(dataRegex);
                }

                if (uc.getName().equals("LIST") || uc.getName().equals("LIST_SUB")) { // NOI18N
                    // List command:
                    String status=(String)props.get("cmd."+uc.getName()+".data.status.index"); // NOI18N
                    if( status!=null ){
                        uc.setStatus(positionToInt(status));
                    }
                    String locker=(String)props.get("cmd."+uc.getName()+".data.locker.index"); // NOI18N
                    if( locker!=null ){
                        uc.setLocker(positionToInt(locker));
                    }
                    String attr=(String)props.get("cmd."+uc.getName()+".data.attr.index"); // NOI18N
                    if( attr!=null ){
                        uc.setAttr(positionToInt(attr));
                    }
                    String date=(String)props.get("cmd."+uc.getName()+".data.date.index"); // NOI18N
                    if( date!=null ){
                        uc.setDate(positionToInt(date));
                    }
                    String time=(String)props.get("cmd."+uc.getName()+".data.time.index"); // NOI18N
                    if( time!=null ){
                        uc.setTime(positionToInt(time));
                    }
                    String size=(String)props.get("cmd."+uc.getName()+".data.size.index"); // NOI18N
                    if( size!=null ){
                        uc.setSize(positionToInt(size));
                    }
                    String fileName=(String)props.get("cmd."+uc.getName()+".data.fileName.index"); // NOI18N
                    if( fileName!=null ){
                        uc.setFileName(positionToInt(fileName));
                    }
                }

                String errorRegex=(String)props.get("cmd."+uc.getName()+".error.regex"); // NOI18N
                if( errorRegex != null ){
                    uc.setErrorRegex(errorRegex);
                }

                String display=(String)props.get("cmd."+uc.getName()+".display"); // NOI18N
                if( display != null ){
                    if(display.equalsIgnoreCase("true")) { // NOI18N
                        uc.setDisplayOutput (true);
                    }
                    if(display.equalsIgnoreCase("false")) { // NOI18N
                        uc.setDisplayOutput (false);
                    }
                }

                String refresh=(String)props.get("cmd."+uc.getName()+".doRefresh"); // NOI18N
                if ( refresh != null ) {
                    if (refresh.equalsIgnoreCase("true")) { // NOI18N
                        uc.setDoRefresh (true);
                    }
                    if (refresh.equalsIgnoreCase("false")) { // NOI18N
                        uc.setDoRefresh (false);
                    }
                }

                String onFileStr=(String)props.get("cmd."+uc.getName()+".onFile"); // NOI18N
                if ( onFileStr != null ) {
                    if (onFileStr.equalsIgnoreCase("true")) { // NOI18N
                        uc.setOnFile (true);
                    }
                    if (onFileStr.equalsIgnoreCase("false")) { // NOI18N
                        uc.setOnFile (false);
                    }
                }

                String onDirStr=(String)props.get("cmd."+uc.getName()+".onDir"); // NOI18N
                if ( onDirStr != null ) {
                    if (onDirStr.equalsIgnoreCase("true")) { // NOI18N
                        uc.setOnDir (true);
                    }
                    if (onDirStr.equalsIgnoreCase("false")) { // NOI18N
                        uc.setOnDir (false);
                    }
                }

                String onRootStr=(String)props.get("cmd."+uc.getName()+".onRoot"); // NOI18N
                if ( onRootStr != null ) {
                    if (onRootStr.equalsIgnoreCase("true")) { // NOI18N
                        uc.setOnRoot (true);
                    }
                    if (onRootStr.equalsIgnoreCase("false")) { // NOI18N
                        uc.setOnRoot (false);
                    }
                }

                String confirmation = (String) props.get("cmd."+uc.getName()+".confirmationMsg"); // NOI18N
                if (confirmation != null) {
                    uc.setConfirmationMsg(confirmation);
                }

                String order = (String) props.get("cmd."+uc.getName()+".order"); // NOI18N
                int orderNum = -1;
                if (order != null) {
                    try {
                        orderNum = Integer.parseInt(order);
                    } catch (NumberFormatException e) {
                        // There is no order information
                        orderNum = -1;
                    }
                }
                uc.setOrder(orderNum);

                result.addElement(uc);
            }
        }
        //D.deb("going to sort ..."); // NOI18N
        result = UserCommand.sortCommands(result);
        //D.deb("going to set the order ..."); // NOI18N
        setOrder(result);
        //D.deb("Returning result = "+result); // NOI18N
        return result;
    }


    //-------------------------------------------
    public static void writeConfiguration (Properties props, Vector cmds) {
        D.deb("writeConfiguration(): cmds = "+cmds); // NOI18N
        props.setProperty ("timeout", "" + 60000); // NOI18N
        // set commands
        for(int i=0; i<cmds.size (); i++) {
            UserCommand uc = (UserCommand) cmds.get (i);
            props.setProperty ("cmd." + uc.getName () + ".label", uc.getLabel ()); // NOI18N
            props.setProperty ("cmd." + uc.getName () + ".exec", uc.getExec ()); // NOI18N
            props.setProperty ("cmd." + uc.getName () + ".input", uc.getInput ()); // NOI18N
            props.setProperty ("cmd." + uc.getName () + ".timeout", "" + uc.getTimeout ()); // NOI18N
            props.setProperty ("cmd." + uc.getName () + ".data.regex", uc.getDataRegex ()); // NOI18N
            if (uc.getName().equals("LIST") || uc.getName().equals("LIST_SUB")) { // NOI18N
                props.setProperty ("cmd." + uc.getName () + ".data.status.index", "" + uc.getStatus ()); // NOI18N
                props.setProperty ("cmd." + uc.getName () + ".data.locker.index", "" + uc.getLocker ()); // NOI18N
                props.setProperty ("cmd." + uc.getName () + ".data.attr.index", "" + uc.getAttr ()); // NOI18N
                props.setProperty ("cmd." + uc.getName () + ".data.date.index", "" + uc.getDate ()); // NOI18N
                props.setProperty ("cmd." + uc.getName () + ".data.time.index", "" + uc.getTime ()); // NOI18N
                props.setProperty ("cmd." + uc.getName () + ".data.size.index", "" + uc.getSize ()); // NOI18N
                props.setProperty ("cmd." + uc.getName () + ".data.fileName.index", "" + uc.getFileName ()); // NOI18N
            }
            props.setProperty ("cmd." +  uc.getName () + ".error.regex", uc.getErrorRegex ()); // NOI18N
            props.setProperty ("cmd." +  uc.getName () + ".display", "" + uc.getDisplayOutput ()); // NOI18N
            props.setProperty ("cmd." +  uc.getName () + ".doRefresh", "" + uc.getDoRefresh ()); // NOI18N
            props.setProperty ("cmd." +  uc.getName () + ".order", "" + uc.getOrder ()); // NOI18N
            props.setProperty ("cmd." +  uc.getName () + ".onFile", "" + uc.getOnFile ()); // NOI18N
            props.setProperty ("cmd." +  uc.getName () + ".onDir", "" + uc.getOnDir ()); // NOI18N
            props.setProperty ("cmd." +  uc.getName () + ".onRoot", "" + uc.getOnRoot ()); // NOI18N
            props.setProperty ("cmd." +  uc.getName () + ".confirmationMsg", "" + uc.getConfirmationMsg()); // NOI18N
        }
    }

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
 *  15   Gandalf-post-FCS1.12.2.1    4/4/00   Martin Entlicher Do not show order 
 *       property.
 *  14   Gandalf-post-FCS1.12.2.0    3/23/00  Martin Entlicher Default timeout 
 *       increased,  Javadoc added,  properties onRoot and confirmationMsg 
 *       added,  sorting variables as well.
 *  13   Gandalf   1.12        2/16/00  Martin Entlicher small fix in setOrder()
 *  12   Gandalf   1.11        2/8/00   Martin Entlicher clone() fixed.  Added 
 *       properties onDir and onFile.
 *  11   Gandalf   1.10        1/17/00  Martin Entlicher NOI18N
 *  10   Gandalf   1.9         1/15/00  Ian Formanek    NOI18N
 *  9    Gandalf   1.8         1/6/00   Martin Entlicher 
 *  8    Gandalf   1.7         12/8/99  Martin Entlicher 
 *  7    Gandalf   1.6         11/30/99 Martin Entlicher 
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         11/24/99 Martin Entlicher Added doRefresh 
 *       property.
 *  4    Gandalf   1.3         10/25/99 Pavel Buzek     
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
 *       mounted
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
