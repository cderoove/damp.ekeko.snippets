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

package org.netbeans.modules.corba.idl.editor.coloring;


public class IDLKeywords {

    public static final int FALSE = 0;
    public static final int OBJECT = 1;
    public static final int TRUE = 2;
    public static final int VALUEBASE = 3;
    public static final int ABSTRACT = 4;
    public static final int ANY = 5;
    public static final int ATTRIBUTE = 6;
    public static final int BOOLEAN = 7;
    public static final int CASE = 8;
    public static final int CHAR = 9;
    public static final int CONST = 10;
    public static final int CONTEXT = 11;
    public static final int CUSTOM = 12;
    public static final int DEFAULT = 13;
    public static final int DOUBLE = 14;
    public static final int ENUM = 15;
    public static final int EXCEPTION = 16;
    public static final int FACTORY = 17;
    public static final int FIXED = 18;
    public static final int FLOAT = 19;
    public static final int IN = 20;
    public static final int INOUT = 21;
    public static final int INTERFACE = 22;
    public static final int LONG = 23;
    public static final int MODULE = 24;
    public static final int NATIVE = 25;
    public static final int OCTET = 26;
    public static final int ONEWAY = 27;
    public static final int OUT = 28;
    public static final int PRIVATE = 29;
    public static final int PUBLIC = 30;
    public static final int RAISES = 31;
    public static final int READONLY = 32;
    public static final int SEQUENCE = 33;
    public static final int SHORT = 34;
    public static final int STRING = 35;
    public static final int STRUCT = 36;
    public static final int SUPPORTS = 37;
    public static final int SWITCH = 38;
    public static final int TRUNCATABLE = 39;
    public static final int TYPEDEF = 40;
    public static final int UNION = 41;
    public static final int UNSIGNED = 42;
    public static final int VALUETYPE = 43;
    public static final int VOID = 44;
    public static final int WCHAR = 45;
    public static final int WSTRING = 46;

    public static int match(char[] buffer, int offset, int len) {
        if (len > 11)
            return -1;
        if (len <= 1)
            return -1;
        switch (buffer[offset++]) {
        case 'F':
            return (len == 5
                    && buffer[offset++] == 'A'
                    && buffer[offset++] == 'L'
                    && buffer[offset++] == 'S'
                    && buffer[offset++] == 'E')
                   ? FALSE : -1;
        case 'O':
            return (len == 6
                    && buffer[offset++] == 'b'
                    && buffer[offset++] == 'j'
                    && buffer[offset++] == 'e'
                    && buffer[offset++] == 'c'
                    && buffer[offset++] == 't')
                   ? OBJECT : -1;
        case 'T':
            return (len == 4
                    && buffer[offset++] == 'R'
                    && buffer[offset++] == 'U'
                    && buffer[offset++] == 'E')
                   ? TRUE : -1;
        case 'V':
            return (len == 9
                    && buffer[offset++] == 'a'
                    && buffer[offset++] == 'l'
                    && buffer[offset++] == 'u'
                    && buffer[offset++] == 'e'
                    && buffer[offset++] == 'B'
                    && buffer[offset++] == 'a'
                    && buffer[offset++] == 's'
                    && buffer[offset++] == 'e')
                   ? VALUEBASE : -1;
        case 'a':
            if (len <= 2)
                return -1;
            switch (buffer[offset++]) {
            case 'b':
                return (len == 8
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 't')
                       ? ABSTRACT : -1;
            case 'n':
                return (len == 3
                        && buffer[offset++] == 'y')
                       ? ANY : -1;
            case 't':
                return (len == 9
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'b'
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'e')
                       ? ATTRIBUTE : -1;
            default:
                return -1;
            }
        case 'b':
            return (len == 7
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 'l'
                    && buffer[offset++] == 'e'
                    && buffer[offset++] == 'a'
                    && buffer[offset++] == 'n')
                   ? BOOLEAN : -1;
        case 'c':
            if (len <= 3)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                return (len == 4
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 'e')
                       ? CASE : -1;
            case 'h':
                return (len == 4
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'r')
                       ? CHAR : -1;
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
                    return (len == 7
                            && buffer[offset++] == 'e'
                            && buffer[offset++] == 'x'
                            && buffer[offset++] == 't')
                           ? CONTEXT : -1;
                default:
                    return -1;
                }
            case 'u':
                return (len == 6
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'm')
                       ? CUSTOM : -1;
            default:
                return -1;
            }
        case 'd':
            if (len <= 5)
                return -1;
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
                return (len == 6
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 'b'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'e')
                       ? DOUBLE : -1;
            default:
                return -1;
            }
        case 'e':
            if (len <= 3)
                return -1;
            switch (buffer[offset++]) {
            case 'n':
                return (len == 4
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 'm')
                       ? ENUM : -1;
            case 'x':
                return (len == 9
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'p'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'n')
                       ? EXCEPTION : -1;
            default:
                return -1;
            }
        case 'f':
            if (len <= 4)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                return (len == 7
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 'y')
                       ? FACTORY : -1;
            case 'i':
                return (len == 5
                        && buffer[offset++] == 'x'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'd')
                       ? FIXED : -1;
            case 'l':
                return (len == 5
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 't')
                       ? FLOAT : -1;
            default:
                return -1;
            }
        case 'i':
            if (buffer[offset++] != 'n')
                return -1;
            if (len == 2)
                return IN;
            if (len <= 4)
                return -1;
            switch (buffer[offset++]) {
            case 'o':
                return (len == 5
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 't')
                       ? INOUT : -1;
            case 't':
                return (len == 9
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 'f'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'e')
                       ? INTERFACE : -1;
            default:
                return -1;
            }
        case 'l':
            return (len == 4
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 'n'
                    && buffer[offset++] == 'g')
                   ? LONG : -1;
        case 'm':
            return (len == 6
                    && buffer[offset++] == 'o'
                    && buffer[offset++] == 'd'
                    && buffer[offset++] == 'u'
                    && buffer[offset++] == 'l'
                    && buffer[offset++] == 'e')
                   ? MODULE : -1;
        case 'n':
            return (len == 6
                    && buffer[offset++] == 'a'
                    && buffer[offset++] == 't'
                    && buffer[offset++] == 'i'
                    && buffer[offset++] == 'v'
                    && buffer[offset++] == 'e')
                   ? NATIVE : -1;
        case 'o':
            if (len <= 2)
                return -1;
            switch (buffer[offset++]) {
            case 'c':
                return (len == 5
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 't')
                       ? OCTET : -1;
            case 'n':
                return (len == 6
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'w'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'y')
                       ? ONEWAY : -1;
            case 'u':
                return (len == 3
                        && buffer[offset++] == 't')
                       ? OUT : -1;
            default:
                return -1;
            }
        case 'p':
            if (len <= 5)
                return -1;
            switch (buffer[offset++]) {
            case 'r':
                return (len == 7
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'v'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'e')
                       ? PRIVATE : -1;
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
            if (len <= 5)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                return (len == 6
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 's'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 's')
                       ? RAISES : -1;
            case 'e':
                return (len == 8
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'd'
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'y')
                       ? READONLY : -1;
            default:
                return -1;
            }
        case 's':
            if (len <= 4)
                return -1;
            switch (buffer[offset++]) {
            case 'e':
                return (len == 8
                        && buffer[offset++] == 'q'
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'e')
                       ? SEQUENCE : -1;
            case 'h':
                return (len == 5
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 't')
                       ? SHORT : -1;
            case 't':
                if (len <= 5)
                    return -1;
                if (buffer[offset++] != 'r')
                    return -1;
                switch (buffer[offset++]) {
                case 'i':
                    return (len == 6
                            && buffer[offset++] == 'n'
                            && buffer[offset++] == 'g')
                           ? STRING : -1;
                case 'u':
                    return (len == 6
                            && buffer[offset++] == 'c'
                            && buffer[offset++] == 't')
                           ? STRUCT : -1;
                default:
                    return -1;
                }
            case 'u':
                return (len == 8
                        && buffer[offset++] == 'p'
                        && buffer[offset++] == 'p'
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 's')
                       ? SUPPORTS : -1;
            case 'w':
                return (len == 6
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'h')
                       ? SWITCH : -1;
            default:
                return -1;
            }
        case 't':
            if (len <= 6)
                return -1;
            switch (buffer[offset++]) {
            case 'r':
                return (len == 11
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'c'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'b'
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'e')
                       ? TRUNCATABLE : -1;
            case 'y':
                return (len == 7
                        && buffer[offset++] == 'p'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'd'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'f')
                       ? TYPEDEF : -1;
            default:
                return -1;
            }
        case 'u':
            if (len <= 4)
                return -1;
            if (buffer[offset++] != 'n')
                return -1;
            switch (buffer[offset++]) {
            case 'i':
                return (len == 5
                        && buffer[offset++] == 'o'
                        && buffer[offset++] == 'n')
                       ? UNION : -1;
            case 's':
                return (len == 8
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'g'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 'd')
                       ? UNSIGNED : -1;
            default:
                return -1;
            }
        case 'v':
            if (len <= 3)
                return -1;
            switch (buffer[offset++]) {
            case 'a':
                return (len == 9
                        && buffer[offset++] == 'l'
                        && buffer[offset++] == 'u'
                        && buffer[offset++] == 'e'
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'y'
                        && buffer[offset++] == 'p'
                        && buffer[offset++] == 'e')
                       ? VALUETYPE : -1;
            case 'o':
                return (len == 4
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'd')
                       ? VOID : -1;
            default:
                return -1;
            }
        case 'w':
            if (len <= 4)
                return -1;
            switch (buffer[offset++]) {
            case 'c':
                return (len == 5
                        && buffer[offset++] == 'h'
                        && buffer[offset++] == 'a'
                        && buffer[offset++] == 'r')
                       ? WCHAR : -1;
            case 's':
                return (len == 7
                        && buffer[offset++] == 't'
                        && buffer[offset++] == 'r'
                        && buffer[offset++] == 'i'
                        && buffer[offset++] == 'n'
                        && buffer[offset++] == 'g')
                       ? WSTRING : -1;
            default:
                return -1;
            }
        default:
            return -1;
        }
    }

}


