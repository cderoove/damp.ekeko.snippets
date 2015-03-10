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
import java.io.*;
import java.util.*;

/** Wait for the specified amount of time then ring the bell.
 * 
 * @author Michal Fadljevic
 */
//-------------------------------------------
public class WatchDog extends Thread {
    private Debug E=new Debug("WatchDog", false); // NOI18N
    private Debug D=new Debug("WatchDog", true); // NOI18N

    private long timeout=0;
    private Thread wakeThread=null;
    private Process toKill = null;
    private boolean ring=true;


    //-------------------------------------------
    public WatchDog(String name, long timeout, Thread wakeThread, Process toKill){
        super(name);
        this.timeout=timeout;
        this.wakeThread=wakeThread;
        this.toKill = toKill;
    }


    //-------------------------------------------
    public void cancel(){
        ring=false;
        this.interrupt();
    }


    //-------------------------------------------
    public void run(){
        D.deb("run(), toKill = "+toKill); // NOI18N
        try{
            if(timeout<=0){
                return;
            }
            D.deb("sleep("+timeout+") thread = "+currentThread()); // NOI18N
            sleep(timeout);
            D.deb("wake after sleep, ring = "+ring+", interrupted = "+isInterrupted()); // NOI18N
            if( ring ){
                D.deb("wakeThread.interrupt()"); // NOI18N
                wakeThread.interrupt();
                if (toKill != null) toKill.destroy();
            }
        }
        catch(InterruptedException e){
            D.deb("Watch dog thread interrupted. There will be no ringing..."); // NOI18N
        }
        D.deb("run() finished toKill = "+toKill); // NOI18N
    }

}

/*
 * Log
 *  7    Gandalf   1.6         2/8/00   Martin Entlicher Destroy itself on 
 *       cancel.
 *  6    Gandalf   1.5         1/15/00  Ian Formanek    NOI18N
 *  5    Gandalf   1.4         1/6/00   Martin Entlicher 
 *  4    Gandalf   1.3         10/25/99 Pavel Buzek     
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/12/99 Pavel Buzek     
 *  1    Gandalf   1.0         9/30/99  Pavel Buzek     
 * $
 */
