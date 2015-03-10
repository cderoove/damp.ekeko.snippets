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

import java.util.*;

import net.jini.discovery.*;

/**
 * Wrapper class for groups logic.
 * Handles "", "public" duality problems and special cases: ALL_GROUPS and NO_GROUPS.
 *
 * <p>WARNING: Implementation depends on ALL_GROUPS (null) and NO_GROUPS finals. (String[0])
 *
 * @author  Petr Kuzel
 * @version 
 */
public class Groups implements java.io.Serializable {

    private final static long serialVersionUID = 1;

    public final static Groups ALL = new Groups(-1);
    public final static Groups NONE = new Groups(0);
    public final static Groups PUBLIC = new Groups("");

    private String[] grps;

    static boolean debug = false;

    /** Creates new Groups, remove duplicates, handle "public" and "". */
    public Groups(String[] grps) {

        if (ALL.equals(grps))
            this.grps = LookupDiscovery.ALL_GROUPS;
        else if (NONE.equals(grps))
            this.grps = LookupDiscovery.NO_GROUPS;
        else {
            System.err.println("Adding groups");
            for (int i=0; i<grps.length; i++) {
                addGroup(grps[i]);
            }
        }
    }

    /** Create one group. */
    public Groups(String grp) {
        if (grp == null)
            throw new IllegalArgumentException();

        addGroup(grp);
    }

    /** Prepare spacial cases. */
    private Groups(int type) {
        if (type == 0) {
            this.grps = LookupDiscovery.NO_GROUPS;
        } else {
            this.grps = LookupDiscovery.ALL_GROUPS;
        }
    }


    /** Add new group, handle "", "public" pairing. */
    private void addGroup(String grp) {
        String old[] = grps;
        if (exist(grp)) return;

        String add[] = normalize(grp);

        if (old == null) {
            grps = add;
        } else {
            grps = new String[old.length + add.length];
            System.arraycopy(old, 0, grps, 0, grps.length-1);
            System.arraycopy(add, 0, grps, old.length, add.length);
        }
    }

    /** */
    private boolean exist(String grp) {
        if (grp == null) return true;
        if (grps == null) return false;

        String mygrp[] = normalize(grp);

        for (int i = 0; i<grps.length; i++)
            if (grps[i].equals(mygrp[0])) return true;

        return false;
    }

    /** Normalize "" or "public" to couple "", "public". */
    private String[] normalize(String grp) {
        String mygrp = grp;

        if (grp.equals("") || grp.equals("public"))
            return new String[] { "public", "" }; //NOI18N
        else
            return new String[] {mygrp};
    }

    /**
    * Test equality by String[] equality enriched by NO_GROUPS and ALL_GROUPS rules.
    * @param wisely should be String, String[] or Groups
    */
    public boolean equals(Object obj) {
        if (obj instanceof Groups) {

            Groups peer = (Groups) obj;
            return equals2(peer.grps);

        } else if ( obj instanceof String ) {

            return equals2(new String[] {(String) obj});

        } else if ( obj instanceof String[] ) {

            return equals2((String[]) obj);

        }

        return false;

    }

    /***/
    private boolean equals2(String[] peer) {

        d("x1");
        if (grps == LookupDiscovery.ALL_GROUPS && peer == LookupDiscovery.ALL_GROUPS )
            return true;

        d("x2");
        if (grps == LookupDiscovery.ALL_GROUPS)
            return false;

        d("x3");
        if (peer == LookupDiscovery.ALL_GROUPS)
            return false;

        d("x4");

        if (peer.length == grps.length) {
            for (int i = 0; i<grps.length; i++) {
                String shouldBe = grps[i];
                boolean match = false;
                for (int j = 0; j<peer.length; j++) {
                    if ( grps[i].equals(peer[j]) ) {
                        match = true;
                        break;
                    }
                }
                if (!match) return false;
            }
            return true;
        }

        return false;
    }

    /**
    * @return string eliminating public duplicity and handling ALL_GROUPS and NO_GROUPS. 
    */
    public String toString() {

        String prefix = "";

        if (equals(ALL))
            return "<all groups>";
        else if (equals(NONE))
            return "<no group>";
        else {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i< grps.length; i++) {

                // remove public duality

                if ( ! grps[i].equals("")) {  //NOI18N
                    buf.append(prefix + grps[i]);
                    prefix = ", ";
                }
            }

            return buf.toString();
        }
    }

    /** @return currently hold groups (public are returned as couple). */
    public String[] getGroups() {
        return grps;
    }

    /** @return true if this group contain at least one group id from passed group. */
    public boolean contains(String[] grps) {

        if (equals(grps)) return true;

        //it this represent all it contains any group
        if (equals(ALL)) return true;

        if (equals(NONE)) return false;

        List groups = Arrays.asList(grps);

        for (int i=0; i<grps.length; i++) {
            if (groups.contains(grps[i]))
                return true;
        }

        return false;
    }


    /** Do self test. */
    public static void main(String[] args) {

        Groups ga1 = Groups.ALL;
        Groups ga2 = Groups.ALL;
        Groups gn1 = Groups.NONE;
        Groups gn2 = Groups.NONE;
        Groups gp1 = new Groups("");
        Groups gp2 = new Groups("public");
        Groups g1 = new Groups(new String[] {"", "public", "g1"});
        Groups g2 = new Groups(new String[] { "public", "g2", "sun"});

        System.err.println("Constructing: ");
        debug = true;
        Groups gno = new Groups( new String[] {} );
        debug = false;

        if (! gno.equals(gn1))
            System.err.println("000");

        if ( ! ga1.equals(ga2) )
            System.err.println("1");

        if ( ! gn1.equals(gn2) )
            System.err.println("1n");

        if ( ga1.equals(gp1) || ga1.equals(g1) || ga1.equals(gn1))
            System.err.println("2");

        if ( ! gp1.equals(gp2) )
            System.err.println("3");

        System.err.println("gno: " + gno);
        System.err.println("ga1: " + ga1);
        System.err.println("gp1: " + gp1);
        System.err.println("g1: " + g1);
        System.err.println("g2: " + g2);
        System.err.println("gn1: " + gn1);

        String[] grps = gp2.getGroups();
        for (int i = 0 ; i< grps.length; i++) {
            System.err.println("Group " + grps[i]);
        }
    }

    private void d(String msg) {
        if (debug) System.err.println(msg);
    }
}


/*
* <<Log>>
*  3    Gandalf   1.2         2/7/00   Petr Kuzel      More service details
*  2    Gandalf   1.1         2/3/00   Petr Kuzel      Be smart and documented
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

