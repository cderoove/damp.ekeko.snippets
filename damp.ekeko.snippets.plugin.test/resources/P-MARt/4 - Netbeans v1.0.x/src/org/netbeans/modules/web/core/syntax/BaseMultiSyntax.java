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

package org.netbeans.modules.web.core.syntax;

import java.util.Arrays;
import java.util.ArrayList;
import org.netbeans.editor.Syntax;

/**
* Composition of several syntaxes together. There are several different
* situations in which this class can be used efficiently:
* 1) Syntax that wants to use some other syntax internally for
*   recognizing one or more tokens. Example is java analyzer that would
*   like to use html-syntax for detail parsing block comments.
*
* 2) Master syntax that will manage two or more slave syntaxes. Example is the
*   master syntax managing java-syntax and html-syntax. The result would
*   be the same like in the previous example but it's more independent.
*
* 3) Master syntax that handles switching of the two or more other syntaxes. Only one 
*   slave syntax is active at one time.
*
* 4) An aribitrary combination and nesting of the previous examples.
*
* The multi-syntax implements several aproaches to let everything work
* as expected:
* 1) Token ID shifting. In order to distinguish the tokens from different syntaxes,
*  all the token-ids (except the system ones that are lower than zero)
*  of the particular slave syntax are shifted. The token-ids of the master syntax
*  have no shift. The first registered slave syntax has the token-id-shift
*  equal to the master syntax's <tt>getHighestTokenID() + 1</tt>.
*  The token-id-shift of each next registered slave syntax
*  is the shift of the previously registered one plus
*  <tt>getHighestTokenID() + 1</tt>.
*  Although it could seem logically correct to merge some tokenIDs from
*  different syntaxes (with the same meaning) into one tokenID, this must NOT
*  be done! The obtained token-id must NOT be changed at all! Otherwise
*  the <tt>translateTokenID()</tt> method would stop working correctly.
*
* 2) Correct resolving of the token names. The token-id-shifting enables
*  to identify precisely from which syntax the token comes from and call
*  the appropriate <tt>getTokenName()</tt> method of the particular slave
*  syntax.
*
* 3) Assigning different token names to some token-ids of some slave syntaxes.
*  This is needed to enable different coloring of the tokens from different slave
*  syntaxes having the same name for example. It's done by calling
*  <tt>changeTokenName()</tt>.
*
* 4) The <tt>translateTokenID()</tt> provides "unshifting" of the "global"
*   token-id to the original token-id defined in the particular syntax.
*
*
* @author Miloslav Metelka, Petr Jiricka
* @version 1.00
*/

public class BaseMultiSyntax extends Syntax {

    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /** Slave syntaxes that can be used for scanning. They can
    * be added by registerSyntax().
    */
    private SyntaxInfo slaveSyntaxChain;

    /** Last chain member of the slaveSyntaxChain */
    private SyntaxInfo slaveSyntaxChainEnd;

    /** IDs of all the tokens with the changed name. */
    private int[] changedTokenIDs = EMPTY_INT_ARRAY;

    /** Changed token names in the same order like changedTokenIDs array. */
    private String[] changedTokenNames = EMPTY_STRING_ARRAY;

    /** Register a particular slave syntax. */
    protected SyntaxInfo registerSyntax(Syntax slaveSyntax) {
        // Compute current token-id shift
        int tokenIDShift;
        if (slaveSyntaxChainEnd != null) {
            tokenIDShift = slaveSyntaxChainEnd.tokenIDShift
                           + slaveSyntaxChainEnd.syntax.getHighestTokenID() + 1;
        } else { // no slave syntaxes - take shift of the master syntax
            tokenIDShift = getHighestTokenID() + 1;
        }

        slaveSyntaxChainEnd = new SyntaxInfo(slaveSyntax, tokenIDShift, slaveSyntaxChainEnd);
        if (slaveSyntaxChain == null) {
            slaveSyntaxChain = slaveSyntaxChainEnd;
        }
        highestTokenID = slaveSyntaxChainEnd.tokenIDShift + slaveSyntaxChainEnd.syntax.getHighestTokenID();
        return slaveSyntaxChainEnd;
    }

    /** Change token name for particular tokenID in some slave syntax. This
    * method can be called AFTER all the slave syntaxes were registered.
    * @param slaveSyntax slave syntax to which the tokenID belongs. Master syntax
    *   token-ids can't be shifted.
    * @param tokenID tokenID for which the token name should be changed
    * @param tokenName changed token name
    */
    protected void changeTokenName(Syntax slaveSyntax, int tokenID, String tokenName) {
        // Find a token shift for the
        SyntaxInfo syntaxItem = slaveSyntaxChain;
        while (syntaxItem != null) {
            if (syntaxItem.syntax == slaveSyntax) {
                tokenID += syntaxItem.tokenIDShift;
                break;
            }
            syntaxItem = syntaxItem.next;
        }

        // Update the arrays
        int[] ctisa = new int[changedTokenIDs.length + 1];
        String[] ctnsa = new String[changedTokenNames.length + 1];
        addAndSortByNumber(tokenID, tokenName, changedTokenIDs, changedTokenNames,
                           ctisa, ctnsa);
        changedTokenIDs = ctisa;
        changedTokenNames = ctnsa;
    }


    public String getTokenName(int tokenID) {
        // First test the changed token-ids by binary search
        int low = 0;
        int high = changedTokenIDs.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            long midVal = changedTokenIDs[mid];

            if (midVal < tokenID) {
                low = mid + 1;
            } else if (midVal > tokenID) {
                high = mid - 1;
            } else {
                return changedTokenNames[mid];
            }
        }

        // Search for the shifted IDs
        SyntaxInfo syntaxItem = slaveSyntaxChainEnd;
        while (syntaxItem != null) {
            if (tokenID >= syntaxItem.tokenIDShift) {
                return syntaxItem.syntax.getTokenName(tokenID - syntaxItem.tokenIDShift);
            }
            syntaxItem = syntaxItem.prev;
        }

        return super.getTokenName(tokenID);
    }


    /** Add the number together with object to the arrays of ints and objects. */
    private void addAndSortByNumber(int numToAdd, Object objToAdd,
                                    int[] numArray, Object[] objArray, int[] targetNumArray, Object[] targetObjArray) {
        ArrayList arl = new ArrayList();
        // Add all array members to the list
        for (int i = 0; i < numArray.length; i++) {
            arl.add(new NumberAndObject(numArray[i], objArray[i]));
        }
        arl.add(new NumberAndObject(numToAdd, objToAdd)); // add the new member to the list

        // Convert list to array to sort it
        NumberAndObject[] naoa = new NumberAndObject[arl.size()];
        arl.toArray(naoa);
        Arrays.sort(naoa); // sort array by number

        // Assign the target arrays
        for (int i = 0; i < naoa.length; i++) {
            targetNumArray[i] = naoa[i].number;
            targetObjArray[i] = naoa[i].object;
        }
    }


    /** Extended info about one slave syntax */
    static class SyntaxInfo {

        SyntaxInfo(Syntax syntax, int tokenIDShift, SyntaxInfo prevChainEnd) {
            this.syntax = syntax;
            this.tokenIDShift = tokenIDShift;

            if (prevChainEnd != null) {
                prev = prevChainEnd;
                prevChainEnd.next = this;
            }
        }

        /** The slave syntax itself */
        Syntax syntax;

        /** shift of the token IDs for this slave syntax */
        int tokenIDShift;

        /** Whether this syntax is actively scanning the text. There can be possibly more
        * syntaxes scanning the in a nested way.
        */
        // boolean active;

        /** Next member in the chain */
        SyntaxInfo next;

        /** Previous member in the chain */
        SyntaxInfo prev;

    }

    /** Helper class to sort the changedTokenIDs and changedTokenNames arrays. */
    static class NumberAndObject implements Comparable {

        int number;

        Object object;

        NumberAndObject(int number, Object object) {
            this.number = number;
            this.object = object;
        }

        public int hashCode() {
            return number;
        }

        public boolean equals(Object o) {
            return (compareTo(o) == 0);
        }

        public int compareTo(Object o) {
            if (o instanceof NumberAndObject) {
                return number - ((NumberAndObject)o).number;
            }
            return -1;
        }

    }

}

/*
 * Log
 *  1    Gandalf   1.0         2/10/00  Petr Jiricka    
 * $
 */

