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

package org.netbeans.editor.ext;

/**
* Various methods related to the java keyword matching
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaKeywords {

    private JavaKeywords() {
        // no instances
    }

    // Data types
    public static final int BOOLEAN = 0;
    public static final int BYTE = 1;
    public static final int CHAR = 2;
    public static final int DOUBLE = 3;
    public static final int FLOAT = 4;
    public static final int INT = 5;
    public static final int LONG = 6;
    public static final int SHORT = 7;

    // Void type
    public static final int VOID = 8;

    // Rest of the keywords
    public static final int ABSTRACT = 9;
    public static final int BREAK = 10;
    public static final int CASE = 11;
    public static final int CATCH = 12;
    public static final int CLASS = 13;
    public static final int CONST = 14;
    public static final int CONTINUE = 15;
    public static final int DEFAULT = 16;
    public static final int DO = 17;
    public static final int ELSE = 18;
    public static final int EXTENDS = 19;
    public static final int FALSE = 20;
    public static final int FINAL = 21;
    public static final int FINALLY = 22;
    public static final int FOR = 23;
    public static final int GOTO = 24;
    public static final int IF = 25;
    public static final int IMPLEMENTS = 26;
    public static final int IMPORT = 27;
    public static final int INSTANCEOF = 28;
    public static final int INTERFACE = 29;
    public static final int NATIVE = 30;
    public static final int NEW = 31;
    public static final int NULL = 32;
    public static final int PACKAGE = 33;
    public static final int PRIVATE = 34;
    public static final int PROTECTED = 35;
    public static final int PUBLIC = 36;
    public static final int RETURN = 37;
    public static final int STATIC = 38;
    public static final int SUPER = 39;
    public static final int SWITCH = 40;
    public static final int SYNCHRONIZED = 41;
    public static final int THIS = 42;
    public static final int THROW = 43;
    public static final int THROWS = 44;
    public static final int TRANSIENT = 45;
    public static final int TRUE = 46;
    public static final int TRY = 47;
    public static final int VOLATILE = 48;
    public static final int WHILE = 49;

    /** Checks whether the given keyword is a type.
    * @return true when the keyword is a data type.
    *   It returns false when the given value corresponds to other keyword
    *   or when it's -1.
    */
    public static boolean isType(int keyword) {
        return (keyword >= 0 && keyword < VOID);
    }

    /** Checks whether the given keyword is a type or void.
    * @return true when the keyword is a data type or void type.
    *   It returns false when the given value corresponds to other keyword
    *   or when it's -1.
    */
    public static boolean isTypeOrVoid(int keyword) {
        return (keyword >= 0 && keyword <= VOID);
    }

    public static boolean isKeyword(String buffer) {
        return isKeyword(buffer, 0, buffer.length());
    }

    public static boolean isKeyword(String buffer, int offset, int len) {
        return (getKeyword(buffer, offset, len) >= 0);
    }

    public static int getKeyword(String buffer) {
        return getKeyword(buffer, 0, buffer.length());
    }

    public static boolean isKeyword(char[] buffer) {
        return isKeyword(buffer, 0, buffer.length);
    }

    public static boolean isKeyword(char[] buffer, int offset, int len) {
        return (getKeyword(buffer, offset, len) >= 0);
    }

    public static int getKeyword(char[] buffer) {
        return getKeyword(buffer, 0, buffer.length);
    }

    public static int getKeyword(char[] buffer, int offset, int len) {
        if (len > 12)
            return -1;
        if (len <= 1)
            return -1;
        switch (buffer[offset++]) {
        case 'a':
            return (len == 8
                    && buffer[offset++] == 'b'
                    && buffer[offset++] == 's'
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'r'
                    && buffer[offset++] == 'a'
                    && buffer[offset++] == 'c'
                    && buffer[offset++] == 't')
                   ? ABSTRACT : -1;
        case 'b':
            if (len <= 3)
                return -1;
            switch (buffer[offset++]) {
            case 'o':
                return (len == 7
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'n')
                       ? BOOLEAN : -1;
            case 'r':
                return (len == 5
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'k')
                       ? BREAK : -1;
            case 'y':
                return (len == 4
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'e')
                       ? BYTE : -1;
            default:
                return -1;
            }
        case 'c':
            if (len <= 3)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                switch (buffer[offset++]) {
                case 's':
                    return (len == 4
                            && buffer[offset++] == 'e')
                           ? CASE : -1;
                case 't':
                    return (len == 5
                            && buffer[offset++] == 'c'
                            && buffer[offset++] == 'h')
                           ? CATCH : -1;
                default:
                    return -1;
                }
            case 'h':
                return (len == 4
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'r')
                       ? CHAR : -1;
            case 'l':
                return (len == 5
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 's')
                       ? CLASS : -1;
            case 'o':
                if (len <= 4)
                    return -1;
                if (buffer[offset++] != 'n')
                    return -1;
                switch (buffer[offset++]) {
                case 's':
                    return (len == 5
                            && buffer[offset++] == 't')
                           ? CONST : -1;
                case 't':
                    return (len == 8
                            && buffer[offset++] == 'i'
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 'u'
                            && buffer[offset++] == 'e')
                           ? CONTINUE : -1;
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'd':
            switch (buffer[offset++]) {
            case 'e':
                return (len == 7
                        && buffer[offset++] == 'f'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 't')
                       ? DEFAULT : -1;
            case 'o':
                if (len == 2)
                    return DO;
                switch (buffer[offset++]) {
                case 'u':
                    return (len == 6
                            && buffer[offset++] == 'b'
                            && buffer[offset++] == 'l'
                            && buffer[offset++] == 'e')
                           ? DOUBLE : -1;
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'e':
            if (len <= 3)
                return -1;
            switch (buffer[offset++]) {
            case 'l':
                return (len == 4
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 'e')
                       ? ELSE : -1;
            case 'x':
                return (len == 7
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'd'
                        && buffer[offset++] == 's')
                       ? EXTENDS : -1;
            default:
                return -1;
            }
        case 'f':
            if (len <= 2)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                return (len == 5
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 'e')
                       ? FALSE : -1;
            case 'i':
                if (len <= 4)
                    return -1;
                if (buffer[offset++] != 'n'
                        || buffer[offset++] != 'a'
                        || buffer[offset++] != 'l')
                    return -1;
                if (len == 5)
                    return FINAL;
                if (len <= 6)
                    return -1;
                if (buffer[offset++] != 'l'
                        || buffer[offset++] != 'y')
                    return -1;
                if (len == 7)
                    return FINALLY;
                return -1;
            case 'l':
                return (len == 5
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 't')
                       ? FLOAT : -1;
            case 'o':
                return (len == 3
                        && buffer[offset++] == 'r')
                       ? FOR : -1;
            default:
                return -1;
            }
        case 'g':
            return (len == 4
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'o')
                   ? GOTO : -1;
        case 'i':
            switch (buffer[offset++]) {
            case 'f':
                return (len == 2)
                       ? IF : -1;
            case 'm':
                if (len <= 5)
                    return -1;
                if (buffer[offset++] != 'p')
                    return -1;
                switch (buffer[offset++]) {
                case 'l':
                    return (len == 10
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'm'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 's')
                           ? IMPLEMENTS : -1;
                case 'o':
                    return (len == 6
                            && buffer[offset++] == 'r'
                            && buffer[offset++] == 't')
                           ? IMPORT : -1;
                default:
                    return -1;
                }
            case 'n':
                if (len <= 2)
                    return -1;
                switch (buffer[offset++]) {
                case 's':
                    return (len == 10
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 'a'
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 'c'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'o'
                            && buffer[offset++] == 'f')
                           ? INSTANCEOF : -1;
                case 't':
                    if (len == 3)
                        return INT;
                    switch (buffer[offset++]) {
                    case 'e':
                        return (len == 9
                                && buffer[offset++] == 'r'
                                && buffer[offset++] == 'f'
                                && buffer[offset++] == 'a'
                                && buffer[offset++] == 'c'
                                && buffer[offset++] == 'e')
                               ? INTERFACE : -1;
                    default:
                        return -1;
                    }
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'l':
            return (len == 4
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 'n'
                    && buffer[offset++] == 'g')
                   ? LONG : -1;
        case 'n':
            if (len <= 2)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                return (len == 6
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'v'
                        && buffer[offset++] == 'e')
                       ? NATIVE : -1;
            case 'e':
                return (len == 3
                        && buffer[offset++] == 'w')
                       ? NEW : -1;
            case 'u':
                return (len == 4
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'l')
                       ? NULL : -1;
            default:
                return -1;
            }
        case 'p':
            if (len <= 5)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                return (len == 7
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'k'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'g'
                        && buffer[offset++] == 'e')
                       ? PACKAGE : -1;
            case 'r':
                if (len <= 6)
                    return -1;
                switch (buffer[offset++]) {
                case 'i':
                    return (len == 7
                            && buffer[offset++] == 'v'
                            && buffer[offset++] == 'a'
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 'e')
                           ? PRIVATE : -1;
                case 'o':
                    return (len == 9
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'c'
                            && buffer[offset++] == 't'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'd')
                           ? PROTECTED : -1;
                default:
                    return -1;
                }
            case 'u':
                return (len == 6
                        && buffer[offset++] == 'b'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'c')
                       ? PUBLIC : -1;
            default:
                return -1;
            }
        case 'r':
            return (len == 6
                    && buffer[offset++] == 'e'
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'u'
                    && buffer[offset++] == 'r'
                    && buffer[offset++] == 'n')
                   ? RETURN : -1;
        case 's':
            if (len <= 4)
                return -1;
            switch (buffer[offset++]) {
            case 'h':
                return (len == 5
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 't')
                       ? SHORT : -1;
            case 't':
                return (len == 6
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'c')
                       ? STATIC : -1;
            case 'u':
                return (len == 5
                        && buffer[offset++] == 'p'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'r')
                       ? SUPER : -1;
            case 'w':
                return (len == 6
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'h')
                       ? SWITCH : -1;
            case 'y':
                return (len == 12
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'h'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'z'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'd')
                       ? SYNCHRONIZED : -1;
            default:
                return -1;
            }
        case 't':
            if (len <= 2)
                return -1;
            switch (buffer[offset++]) {
            case 'h':
                if (len <= 3)
                    return -1;
                switch (buffer[offset++]) {
                case 'i':
                    return (len == 4
                            && buffer[offset++] == 's')
                           ? THIS : -1;
                case 'r':
                    if (len <= 4)
                        return -1;
                    if (buffer[offset++] != 'o'
                            || buffer[offset++] != 'w')
                        return -1;
                    if (len == 5)
                        return THROW;
                    if (buffer[offset++] != 's')
                        return -1;
                    if (len == 6)
                        return THROWS;
                    return -1;
                default:
                    return -1;
                }
            case 'r':
                switch (buffer[offset++]) {
                case 'a':
                    return (len == 9
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 's'
                            && buffer[offset++] == 'i'
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 't')
                           ? TRANSIENT : -1;
                case 'u':
                    return (len == 4
                            && buffer[offset++] == 'e')
                           ? TRUE : -1;
                case 'y':
                    return (len == 3)
                           ? TRY : -1;
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'v':
            if (len <= 3)
                return -1;
            if (buffer[offset++] != 'o')
                return -1;
            switch (buffer[offset++]) {
            case 'i':
                return (len == 4
                        && buffer[offset++] == 'd')
                       ? VOID : -1;
            case 'l':
                return (len == 8
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'e')
                       ? VOLATILE : -1;
            default:
                return -1;
            }
        case 'w':
            return (len == 5
                    && buffer[offset++] == 'h'
                    && buffer[offset++] == 'i'
                    && buffer[offset++] == 'l'
                    && buffer[offset++] == 'e')
                   ? WHILE : -1;
        default:
            return -1;
        }
    }

    public static int getKeyword(String buffer, int offset, int len) {
        if (len > 12)
            return -1;
        if (len <= 1)
            return -1;
        switch (buffer.charAt(offset++)) {
        case 'a':
            return (len == 8
                    && buffer.charAt(offset++) == 'b'
                    && buffer.charAt(offset++) == 's'
                    && buffer.charAt(offset++) == 't'
                    && buffer.charAt(offset++) == 'r'
                    && buffer.charAt(offset++) == 'a'
                    && buffer.charAt(offset++) == 'c'
                    && buffer.charAt(offset++) == 't')
                   ? ABSTRACT : -1;
        case 'b':
            if (len <= 3)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'o':
                return (len == 7
                        && buffer.charAt(offset++) == 'o'
                        && buffer.charAt(offset++) == 'l'
                        && buffer.charAt(offset++) == 'e'
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 'n')
                       ? BOOLEAN : -1;
            case 'r':
                return (len == 5
                        && buffer.charAt(offset++) == 'e'
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 'k')
                       ? BREAK : -1;
            case 'y':
                return (len == 4
                        && buffer.charAt(offset++) == 't'
                        && buffer.charAt(offset++) == 'e')
                       ? BYTE : -1;
            default:
                return -1;
            }
        case 'c':
            if (len <= 3)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'a':
                switch (buffer.charAt(offset++)) {
                case 's':
                    return (len == 4
                            && buffer.charAt(offset++) == 'e')
                           ? CASE : -1;
                case 't':
                    return (len == 5
                            && buffer.charAt(offset++) == 'c'
                            && buffer.charAt(offset++) == 'h')
                           ? CATCH : -1;
                default:
                    return -1;
                }
            case 'h':
                return (len == 4
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 'r')
                       ? CHAR : -1;
            case 'l':
                return (len == 5
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 's'
                        && buffer.charAt(offset++) == 's')
                       ? CLASS : -1;
            case 'o':
                if (len <= 4)
                    return -1;
                if (buffer.charAt(offset++) != 'n')
                    return -1;
                switch (buffer.charAt(offset++)) {
                case 's':
                    return (len == 5
                            && buffer.charAt(offset++) == 't')
                           ? CONST : -1;
                case 't':
                    return (len == 8
                            && buffer.charAt(offset++) == 'i'
                            && buffer.charAt(offset++) == 'n'
                            && buffer.charAt(offset++) == 'u'
                            && buffer.charAt(offset++) == 'e')
                           ? CONTINUE : -1;
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'd':
            switch (buffer.charAt(offset++)) {
            case 'e':
                return (len == 7
                        && buffer.charAt(offset++) == 'f'
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 'u'
                        && buffer.charAt(offset++) == 'l'
                        && buffer.charAt(offset++) == 't')
                       ? DEFAULT : -1;
            case 'o':
                if (len == 2)
                    return DO;
                switch (buffer.charAt(offset++)) {
                case 'u':
                    return (len == 6
                            && buffer.charAt(offset++) == 'b'
                            && buffer.charAt(offset++) == 'l'
                            && buffer.charAt(offset++) == 'e')
                           ? DOUBLE : -1;
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'e':
            if (len <= 3)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'l':
                return (len == 4
                        && buffer.charAt(offset++) == 's'
                        && buffer.charAt(offset++) == 'e')
                       ? ELSE : -1;
            case 'x':
                return (len == 7
                        && buffer.charAt(offset++) == 't'
                        && buffer.charAt(offset++) == 'e'
                        && buffer.charAt(offset++) == 'n'
                        && buffer.charAt(offset++) == 'd'
                        && buffer.charAt(offset++) == 's')
                       ? EXTENDS : -1;
            default:
                return -1;
            }
        case 'f':
            if (len <= 2)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'a':
                return (len == 5
                        && buffer.charAt(offset++) == 'l'
                        && buffer.charAt(offset++) == 's'
                        && buffer.charAt(offset++) == 'e')
                       ? FALSE : -1;
            case 'i':
                if (len <= 4)
                    return -1;
                if (buffer.charAt(offset++) != 'n'
                        || buffer.charAt(offset++) != 'a'
                        || buffer.charAt(offset++) != 'l')
                    return -1;
                if (len == 5)
                    return FINAL;
                if (len <= 6)
                    return -1;
                if (buffer.charAt(offset++) != 'l'
                        || buffer.charAt(offset++) != 'y')
                    return -1;
                if (len == 7)
                    return FINALLY;
                return -1;
            case 'l':
                return (len == 5
                        && buffer.charAt(offset++) == 'o'
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 't')
                       ? FLOAT : -1;
            case 'o':
                return (len == 3
                        && buffer.charAt(offset++) == 'r')
                       ? FOR : -1;
            default:
                return -1;
            }
        case 'g':
            return (len == 4
                    && buffer.charAt(offset++) == 'o'
                    && buffer.charAt(offset++) == 't'
                    && buffer.charAt(offset++) == 'o')
                   ? GOTO : -1;
        case 'i':
            switch (buffer.charAt(offset++)) {
            case 'f':
                return (len == 2)
                       ? IF : -1;
            case 'm':
                if (len <= 5)
                    return -1;
                if (buffer.charAt(offset++) != 'p')
                    return -1;
                switch (buffer.charAt(offset++)) {
                case 'l':
                    return (len == 10
                            && buffer.charAt(offset++) == 'e'
                            && buffer.charAt(offset++) == 'm'
                            && buffer.charAt(offset++) == 'e'
                            && buffer.charAt(offset++) == 'n'
                            && buffer.charAt(offset++) == 't'
                            && buffer.charAt(offset++) == 's')
                           ? IMPLEMENTS : -1;
                case 'o':
                    return (len == 6
                            && buffer.charAt(offset++) == 'r'
                            && buffer.charAt(offset++) == 't')
                           ? IMPORT : -1;
                default:
                    return -1;
                }
            case 'n':
                if (len <= 2)
                    return -1;
                switch (buffer.charAt(offset++)) {
                case 's':
                    return (len == 10
                            && buffer.charAt(offset++) == 't'
                            && buffer.charAt(offset++) == 'a'
                            && buffer.charAt(offset++) == 'n'
                            && buffer.charAt(offset++) == 'c'
                            && buffer.charAt(offset++) == 'e'
                            && buffer.charAt(offset++) == 'o'
                            && buffer.charAt(offset++) == 'f')
                           ? INSTANCEOF : -1;
                case 't':
                    if (len == 3)
                        return INT;
                    switch (buffer.charAt(offset++)) {
                    case 'e':
                        return (len == 9
                                && buffer.charAt(offset++) == 'r'
                                && buffer.charAt(offset++) == 'f'
                                && buffer.charAt(offset++) == 'a'
                                && buffer.charAt(offset++) == 'c'
                                && buffer.charAt(offset++) == 'e')
                               ? INTERFACE : -1;
                    default:
                        return -1;
                    }
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'l':
            return (len == 4
                    && buffer.charAt(offset++) == 'o'
                    && buffer.charAt(offset++) == 'n'
                    && buffer.charAt(offset++) == 'g')
                   ? LONG : -1;
        case 'n':
            if (len <= 2)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'a':
                return (len == 6
                        && buffer.charAt(offset++) == 't'
                        && buffer.charAt(offset++) == 'i'
                        && buffer.charAt(offset++) == 'v'
                        && buffer.charAt(offset++) == 'e')
                       ? NATIVE : -1;
            case 'e':
                return (len == 3
                        && buffer.charAt(offset++) == 'w')
                       ? NEW : -1;
            case 'u':
                return (len == 4
                        && buffer.charAt(offset++) == 'l'
                        && buffer.charAt(offset++) == 'l')
                       ? NULL : -1;
            default:
                return -1;
            }
        case 'p':
            if (len <= 5)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'a':
                return (len == 7
                        && buffer.charAt(offset++) == 'c'
                        && buffer.charAt(offset++) == 'k'
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 'g'
                        && buffer.charAt(offset++) == 'e')
                       ? PACKAGE : -1;
            case 'r':
                if (len <= 6)
                    return -1;
                switch (buffer.charAt(offset++)) {
                case 'i':
                    return (len == 7
                            && buffer.charAt(offset++) == 'v'
                            && buffer.charAt(offset++) == 'a'
                            && buffer.charAt(offset++) == 't'
                            && buffer.charAt(offset++) == 'e')
                           ? PRIVATE : -1;
                case 'o':
                    return (len == 9
                            && buffer.charAt(offset++) == 't'
                            && buffer.charAt(offset++) == 'e'
                            && buffer.charAt(offset++) == 'c'
                            && buffer.charAt(offset++) == 't'
                            && buffer.charAt(offset++) == 'e'
                            && buffer.charAt(offset++) == 'd')
                           ? PROTECTED : -1;
                default:
                    return -1;
                }
            case 'u':
                return (len == 6
                        && buffer.charAt(offset++) == 'b'
                        && buffer.charAt(offset++) == 'l'
                        && buffer.charAt(offset++) == 'i'
                        && buffer.charAt(offset++) == 'c')
                       ? PUBLIC : -1;
            default:
                return -1;
            }
        case 'r':
            return (len == 6
                    && buffer.charAt(offset++) == 'e'
                    && buffer.charAt(offset++) == 't'
                    && buffer.charAt(offset++) == 'u'
                    && buffer.charAt(offset++) == 'r'
                    && buffer.charAt(offset++) == 'n')
                   ? RETURN : -1;
        case 's':
            if (len <= 4)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'h':
                return (len == 5
                        && buffer.charAt(offset++) == 'o'
                        && buffer.charAt(offset++) == 'r'
                        && buffer.charAt(offset++) == 't')
                       ? SHORT : -1;
            case 't':
                return (len == 6
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 't'
                        && buffer.charAt(offset++) == 'i'
                        && buffer.charAt(offset++) == 'c')
                       ? STATIC : -1;
            case 'u':
                return (len == 5
                        && buffer.charAt(offset++) == 'p'
                        && buffer.charAt(offset++) == 'e'
                        && buffer.charAt(offset++) == 'r')
                       ? SUPER : -1;
            case 'w':
                return (len == 6
                        && buffer.charAt(offset++) == 'i'
                        && buffer.charAt(offset++) == 't'
                        && buffer.charAt(offset++) == 'c'
                        && buffer.charAt(offset++) == 'h')
                       ? SWITCH : -1;
            case 'y':
                return (len == 12
                        && buffer.charAt(offset++) == 'n'
                        && buffer.charAt(offset++) == 'c'
                        && buffer.charAt(offset++) == 'h'
                        && buffer.charAt(offset++) == 'r'
                        && buffer.charAt(offset++) == 'o'
                        && buffer.charAt(offset++) == 'n'
                        && buffer.charAt(offset++) == 'i'
                        && buffer.charAt(offset++) == 'z'
                        && buffer.charAt(offset++) == 'e'
                        && buffer.charAt(offset++) == 'd')
                       ? SYNCHRONIZED : -1;
            default:
                return -1;
            }
        case 't':
            if (len <= 2)
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'h':
                if (len <= 3)
                    return -1;
                switch (buffer.charAt(offset++)) {
                case 'i':
                    return (len == 4
                            && buffer.charAt(offset++) == 's')
                           ? THIS : -1;
                case 'r':
                    if (len <= 4)
                        return -1;
                    if (buffer.charAt(offset++) != 'o'
                            || buffer.charAt(offset++) != 'w')
                        return -1;
                    if (len == 5)
                        return THROW;
                    if (buffer.charAt(offset++) != 's')
                        return -1;
                    if (len == 6)
                        return THROWS;
                    return -1;
                default:
                    return -1;
                }
            case 'r':
                switch (buffer.charAt(offset++)) {
                case 'a':
                    return (len == 9
                            && buffer.charAt(offset++) == 'n'
                            && buffer.charAt(offset++) == 's'
                            && buffer.charAt(offset++) == 'i'
                            && buffer.charAt(offset++) == 'e'
                            && buffer.charAt(offset++) == 'n'
                            && buffer.charAt(offset++) == 't')
                           ? TRANSIENT : -1;
                case 'u':
                    return (len == 4
                            && buffer.charAt(offset++) == 'e')
                           ? TRUE : -1;
                case 'y':
                    return (len == 3)
                           ? TRY : -1;
                default:
                    return -1;
                }
            default:
                return -1;
            }
        case 'v':
            if (len <= 3)
                return -1;
            if (buffer.charAt(offset++) != 'o')
                return -1;
            switch (buffer.charAt(offset++)) {
            case 'i':
                return (len == 4
                        && buffer.charAt(offset++) == 'd')
                       ? VOID : -1;
            case 'l':
                return (len == 8
                        && buffer.charAt(offset++) == 'a'
                        && buffer.charAt(offset++) == 't'
                        && buffer.charAt(offset++) == 'i'
                        && buffer.charAt(offset++) == 'l'
                        && buffer.charAt(offset++) == 'e')
                       ? VOLATILE : -1;
            default:
                return -1;
            }
        case 'w':
            return (len == 5
                    && buffer.charAt(offset++) == 'h'
                    && buffer.charAt(offset++) == 'i'
                    && buffer.charAt(offset++) == 'l'
                    && buffer.charAt(offset++) == 'e')
                   ? WHILE : -1;
        default:
            return -1;
        }
    }


}

/*
 * Log
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/30/99  Miloslav Metelka 
 *  3    Gandalf   1.2         9/15/99  Miloslav Metelka 
 *  2    Gandalf   1.1         9/10/99  Miloslav Metelka 
 *  1    Gandalf   1.0         8/27/99  Miloslav Metelka 
 * $
 */

