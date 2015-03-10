/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer;

/** Command is not supported by system.
* System is not able to locate appropriate resources to create a command.
* It cannot find relevant section in definition file, cannot allocate or
* initialize command object. 
*
* @author Slavek Psenicka
*/
public class CommandUnachievableException extends Exception
{
    /** Unsuccessfull command */
    private String cmd;

    /** Creates new exception
    * @param command The text describing the exception
    */
    public CommandUnachievableException (String command) {
        super ();
        cmd = command;
    }

    /** Creates new exception with text specified string.
    * @param command Executed command
    * @param desc The text describing the exception
    */
    public CommandUnachievableException (String command, String desc) {
        super (desc);
        cmd = command;
    }

    /** Returns executed command */
    public String getCommand()
    {
        return cmd;
    }

    public String toString()
    {
        return "unable to perform command \""+cmd+"\"; "+getMessage();
    }
}

/*
 * <<Log>>
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/19/99  Slavek Psenicka English
 *  3    Gandalf   1.2         5/21/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         5/14/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
