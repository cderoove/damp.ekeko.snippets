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

package org.netbeans.modules.vcs.cmdline.exec;

import org.netbeans.modules.vcs.util.*;
import org.netbeans.modules.vcs.cmdline.*;
import java.io.*;
import java.util.*;


/** Test external command execution...
 * 
 * @author Michal Fadljevic
 */

//-------------------------------------------
public class TestCommand {
    private Debug E=new Debug("TestCommand", false); // NOI18N
    private Debug D=E;

    private Properties props=null;

    /**
     * @associates String 
     */
    private Hashtable userVariables=null;
    private boolean execute=false;
    private String cmd=""; // NOI18N
    private String input=null;
    private long timeout=1;
    private String file=null;
    private String message=null;

    //-------------------------------------------
    public TestCommand(){
        props=new Properties();
        FileInputStream fin=null;
        try{
            fin=new FileInputStream("env.txt"); // NOI18N
            props.load(fin);
        }
        catch(IOException exc){
            E.err(exc,"env.txt not found"); // NOI18N
            System.exit(1);
        }

        userVariables=new Hashtable();
        for(Iterator iter=props.keySet().iterator(); iter.hasNext();){
            String key=(String)iter.next();
            //D.deb("key="+key); // NOI18N
            if( key.startsWith("user.var.")==true ){ // NOI18N
                String var=key.substring(9);
                String value=(String)props.get(key);
                //D.deb(var+"='"+value+"'"); // NOI18N
                userVariables.put(var,value);
            }
        }
    }

    //-------------------------------------------
    private long getLongProperty(String name, long def){
        long l=def;
        String val=props.getProperty(name);
        if( val==null ){
            //D.deb("No such property "+name); // NOI18N
            return l;
        }

        try{
            l=Long.parseLong(val);
        }
        catch (NumberFormatException e){
            E.err(e,""); // NOI18N
        }
        return l;
    }

    //-------------------------------------------
    private String arrayToString(String []sa){
        StringBuffer sb=new StringBuffer();
        sb.append("["); // NOI18N
        for(int i=0;i<sa.length;i++){
            sb.append(sa[i]);
            if(i<sa.length-1){
                sb.append(","); // NOI18N
            }
        }
        sb.append("]"); // NOI18N
        return new String(sb);
    }

    //-------------------------------------------
    public void runTest(){
        String cmd1=cmd;

        if( cmd.charAt(0)!='$' ){
            cmd1="${"+cmd+"}"; // NOI18N
        }

        if(file!=null){
            userVariables.put("FILE",file); // NOI18N
        }

        if(message!=null){
            userVariables.put("MESSAGE",message); // NOI18N
        }

        Variables v=new Variables();
        String cmd2=v.expand(userVariables,cmd1, true);

        ExternalCommand ec=new ExternalCommand(cmd2);

        if(execute==true){
            long l=getLongProperty("user.var."+cmd+".timeout", 1); // NOI18N
            ec.setTimeout(l);

            String inputText=props.getProperty("user.var."+cmd+".input");
            //D.deb("inputText="+inputText); // NOI18N
            if( input==null ){
                input=inputText;
            }
            ec.setInput(input);

            String dataRegex=props.getProperty("user.var."+cmd+".data.regex");
            D.deb("dataRegex="+dataRegex); // NOI18N
            if( dataRegex!=null ){
                try{
                    ec.addStdoutRegexListener(new RegexListener () {
                                                  public void match(String[] elements){
                                                      D.deb("stdout match:"+arrayToString(elements)); // NOI18N
                                                  }
                                              },dataRegex);
                }
                catch (BadRegexException e){
                    E.err(e,"bad regex"); // NOI18N
                }
            }

            String errorRegex=props.getProperty("user.var."+cmd+".error.regex");
            D.deb("errorRegex="+errorRegex); // NOI18N
            if( errorRegex!=null ){
                try{
                    ec.addStderrRegexListener(new RegexListener () {
                                                  public void match(String[] elements){
                                                      D.deb("stderr match:"+arrayToString(elements)); // NOI18N
                                                  }
                                              },errorRegex);
                }
                catch (BadRegexException e){
                    E.err(e,"bad regex"); // NOI18N
                }
            }

            D.deb("ec="+ec); // NOI18N
            if( ec.exec() != ExternalCommand.SUCCESS ){
                D.deb("exec failed "+ec.getExitStatus()); // NOI18N
                return ;
            }
            D.deb("OK"); // NOI18N
        }
        else{
            D.deb("ec="+ec); // NOI18N
        }

    }


    //-------------------------------------------
    public static void main(String[]args) {
        if(args.length==0){
            System.out.println
            ("Usage: TestCommand [-exec] [-timout 2000] "+ // NOI18N
             "[-input \"some text\"] [-cmd command] [-file Foo.java] "+ // NOI18N
             "[-message \"some text\"]"); // NOI18N
            System.exit(1);
        }

        TestCommand test=new TestCommand();
        for(int i=0;i<args.length;i++){
            if( args[i].equals("-exec") ){ // NOI18N
                test.execute=true;}
            else if( args[i].equals("-timeout") ){ // NOI18N
                test.timeout=Long.parseLong(args[++i]);
            } else if( args[i].equals("-input") ){ // NOI18N
                test.input=args[++i];
            } else if( args[i].equals("-cmd") ){ // NOI18N
                test.cmd=args[++i];
            } else if( args[i].equals("-file") ){ // NOI18N
                test.file=args[++i];
            } else if( args[i].equals("-message") ){ // NOI18N
                test.message=args[++i];
            } else {
                test.D.deb("ignored "+args[i]); // NOI18N
            }
        }
        test.runTest();
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/18/00  Martin Entlicher 
 *  5    Gandalf   1.4         1/15/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         1/6/00   Martin Entlicher 
 *  3    Gandalf   1.2         10/25/99 Pavel Buzek     
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
