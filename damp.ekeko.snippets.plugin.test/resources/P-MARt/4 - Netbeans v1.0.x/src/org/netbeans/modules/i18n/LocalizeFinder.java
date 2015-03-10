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

package org.netbeans.modules.i18n;

import org.netbeans.editor.Finder;
import org.netbeans.editor.FinderFactory;
import org.apache.regexp.*;

/** Finder for localize find
*
* @author Petr Jiricka
*/
public class LocalizeFinder extends FinderFactory.AbstractFinder implements FinderFactory.StringFinder {

    private static final int STATE_JAVA = 0;
    private static final int STATE_JAVA_A_SLASH = 1;
    private static final int STATE_LINECOMMENT = 2;
    private static final int STATE_BLOCKCOMMENT = 3;
    private static final int STATE_BLOCKCOMMENT_A_STAR = 4;
    private static final int STATE_STRING = 5;
    private static final int STATE_STRING_A_BSLASH = 6;

    protected int currentLineStart = 0;
    protected int currentStringStart = -1;
    protected int currentStringEnd = -1;

    protected int foundStringLength = -1;
    protected int foundLineStart = -1;
    protected int foundLineLength = -1;

    private int state = STATE_JAVA;

    public LocalizeFinder() {
        initRegExp();
    }

    public int getFoundLength() {
        return foundStringLength;
    }

    public int getLineStart() {
        return foundLineStart;
    }

    public int getLineLength() {
        return foundLineLength;
    }

    public void initialize() {
        state = STATE_JAVA;
        initJava();
        currentLineStart = 0;
        currentStringStart = -1;
        currentStringEnd = -1;
        //System.out.println("initialize called");
    }

    public void reset() {
        super.reset();
        //System.out.println("reset called");
    }

    public final int find(int bufferStartPos, char buffer[],
                          int offset1, int offset2, int reqPos, int limitPos) {
        //System.out.println("find - position " + reqPos);
        //reset last search
        foundStringLength = -1;
        foundLineStart = -1;
        foundLineLength = -1;
        // now serious work
        int offset = reqPos - bufferStartPos;
        int limitOffset = limitPos - bufferStartPos - 1;
        while (offset >= offset1 && offset < offset2) {
            offset = scan(bufferStartPos + offset, buffer[offset], (offset == limitOffset)) - bufferStartPos;
            if (found) {
                break;
            }
        }
        return bufferStartPos + offset;
    }

    /** Returns the position to continue search, not relative offset */
    protected int scan(int position, char ch, boolean lastChar) {
        // if a line ends
        if ((ch == '\n') || lastChar) {
            // change the state
            switch (state) {
            case STATE_JAVA:
            case STATE_JAVA_A_SLASH:
            case STATE_LINECOMMENT:
            case STATE_STRING:
            case STATE_STRING_A_BSLASH:
                if (state != STATE_JAVA)
                    initJava();
                state = STATE_JAVA;
                break;
            case STATE_BLOCKCOMMENT:
            case STATE_BLOCKCOMMENT_A_STAR:
                state = STATE_BLOCKCOMMENT;
                break;
            }
            // return correct value
            if (currentStringStart != -1) {
                // found
                found = true;
                if (currentStringEnd == -1)
                    currentStringEnd = position - 1;
                foundStringLength = currentStringEnd - currentStringStart;
                foundLineStart = currentLineStart;
                foundLineLength = position - 1 - currentLineStart;
                int remember = currentStringStart;
                currentStringStart = -1;
                //System.out.println("found - position " + remember);
                return remember;
            }
            else {
                // not found
                if (!lastChar) {
                    currentLineStart = position + 1;
                    return position + 1;
                }
                else
                    return position + 1;

            }
        }
        // other chars than '\n' and lastChar
        switch (state) {
        case STATE_JAVA:
            lastJavaString.append(ch);
            switch (ch) {
            case '/':
                state = STATE_JAVA_A_SLASH;
                break;
            case '"':
                state = STATE_STRING;
                if (currentStringStart == -1 && lastJavaStringNotLocalized()) {
                    currentStringStart = position;
                    currentStringEnd = -1;
                }
                break;
            }
            break;
        case STATE_JAVA_A_SLASH:
            lastJavaString.append(ch);
            switch (ch) {
            case '/':
                state = STATE_LINECOMMENT;
                break;
            case '*':
                state = STATE_BLOCKCOMMENT;
                break;
            }
            break;
        case STATE_LINECOMMENT:
            break;
        case STATE_STRING:
            switch (ch) {
            case '\\':
                state = STATE_STRING_A_BSLASH;
                break;
            case '"':
                state = STATE_JAVA;
                initJava();
                if ((currentStringEnd == -1) && (currentStringStart != -1))
                    currentStringEnd = position + 1;
                break;
            }
            break;
        case STATE_STRING_A_BSLASH:
            state = STATE_STRING;
            break;
        case STATE_BLOCKCOMMENT:
            switch (ch) {
            case '*':
                state = STATE_BLOCKCOMMENT_A_STAR;
                break;
            }
            break;
        case STATE_BLOCKCOMMENT_A_STAR:
            switch (ch) {
            case '/':
                state = STATE_JAVA;
                initJava();
                break;
            default:
                state = STATE_BLOCKCOMMENT;
                break;
            }
            break;
        default:
            throw new InternalError();
        }
        return position + 1;
    }

    private void initJava() {
        lastJavaString = new StringBuffer();
    }

    private void initRegExp() {
        RECompiler rec = new RECompiler();
        String regx = "((getString|getBundle)([:space:]*)\\(([:space:])*\")";
        //System.out.println(regx);
        try {
            rep = rec.compile(regx);
        }
        catch (RESyntaxException e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions"))
                e.printStackTrace();
            throw new InternalError("the regexp didn't compile");
        }
    }

    private boolean lastJavaStringNotLocalized() {
        String lastStr = lastJavaString.toString();
        //System.out.println("javaString :" + lastStr);

        RE re = new RE(rep, RE.MATCH_MULTILINE);
        CharacterIterator chi = new StringCharacterIterator(lastStr);
        if (re.match(chi, 0))
            return false;
        return true;
    }

    private StringBuffer lastJavaString;

    private REProgram rep;

}

/*
 * <<Log>>
 */
