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

package org.openide.util;

import java.util.Map;
import java.util.Locale;
import java.util.Date;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.HashMap;
import java.text.Format;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.ParseException;
import java.text.MessageFormat;

/**
* The message formatter, which uses map's keys in place of numbers.
* This class extends functionality of MessageFormat by allowing the user to
* specify strings in place of MessageFormatter's numbers. It then uses given
* map to translate keys to values.
*
* You will usually use this formatter as follows:
* 	<code>MapFormat.format("Hello {name}", map);</code>
*
* Notes: if map does not contain value for key specified, it substitutes
* the value by <code>"null"</code> word to qualify that something goes wrong.
*
* @author   Slavek Psenicka
* @version  1.0, March 11. 1999
*/

public class MapFormat extends Format {

    private static final int BUFSIZE = 255;

    /** Locale region settings used for number and date formatting */
    private Locale locale = Locale.getDefault();

    /** Left delimiter */
    private String ldel = "{"; // NOI18N

    /** Right delimiter */
    private String rdel = "}"; // NOI18N

    /** Used formatting map */
    private Map argmap;

    /** Offsets to {} expressions */
    private int[] offsets;

    /** Keys enclosed by {} brackets */
    private String[] arguments;

    /** Max used offset */
    private int maxOffset;

    /** Should be thrown an exception if key was not found? */
    private boolean throwex = false;

    /** Exactly match brackets? */
    private boolean exactmatch = true;

    /** Array with to-be-skipped blocks */
    private RangeList skipped;

    static final long serialVersionUID =-7695811542873819435L;
    /**
    * Constructor.
    * For common work use  <code>format(pattern, arguments) </code>.
    * @param pattern String to be parsed.
    */
    public MapFormat(Map arguments) {
        super();
        setMap(arguments);
    }

    /**
    * Designated method. It gets the string, initializes HashFormat object
    * and returns converted string. It scans  <code>pattern</code>
    * for {} brackets, then parses enclosed string and replaces it
    * with argument's  <code>get()</code> value.
    * @param pattern String to be parsed.
    * @param arguments Map with key-value pairs to replace.
    * @return Formatted string
    */
    public static String format(String pattern, Map arguments) {
        MapFormat temp = new MapFormat(arguments);
        return temp.format(pattern);
    }

    /**
    * Search for comments and quotation marks.
    * Prepares internal structures.
    * @param pattern String to be parsed.
    * @param lmark Left mark of to-be-skipped block.
    * @param rmark Right mark of to-be-skipped block or null if does not exist (// comment).
    */	
    private void process(String pattern, String lmark, String rmark)
    {
        int idx = 0;
        while (true) {
            int ridx = -1, lidx = pattern.indexOf(lmark,idx);
            if (lidx >= 0) {
                if (rmark != null) {
                    ridx = pattern.indexOf(rmark,lidx + lmark.length());
                } else ridx = pattern.length();
            } else break;
            if (ridx >= 0) {
                skipped.put(new Range(lidx, ridx-lidx));
                if (rmark != null) idx = ridx+rmark.length();
                else break;
            } else break;
        }
    }

    /** Returns the value for given key. Subclass may define its own beahvior of
    * this method. For example, if key is not defined, subclass can return <not defined> 
    * string.
    * 
    * @param key Key.
    * @return Value for this key.
    */
    protected Object processKey(String key) {
        try {
            return argmap.get(key);
        } catch (Exception exc) {
            return key;
        }
    }

    /**
    * Scans the pattern and prepares internal variables.
    * @param newPattern String to be parsed.
    * @exception IllegalArgumentException if number of arguments exceeds BUFSIZE or 
    * parser found unmatched brackets (this exception should be switched off 
    * using setExactMatch(false)).
    */
    public String processPattern(String newPattern)
    throws IllegalArgumentException
    {
        int idx = 0, offnum = -1;
        StringBuffer outpat = new StringBuffer();
        offsets = new int[BUFSIZE];
        arguments = new String[BUFSIZE];
        maxOffset = -1;

        skipped = new RangeList();
        process(newPattern, "\"", "\""); // NOI18N

        while (true) {
            int ridx = -1, lidx = newPattern.indexOf(ldel,idx);
            Range ran = skipped.getRangeContainingOffset(lidx);
            if (ran != null) {
                outpat.append(newPattern.substring(idx, ran.getEnd()));
                idx = ran.getEnd(); continue;
            }

            if (lidx >= 0) {
                ridx = newPattern.indexOf(rdel,lidx + ldel.length());
            } else break;

            if (++offnum >= BUFSIZE) throw new IllegalArgumentException(NbBundle.getBundle(MapFormat.class).getString("MSG_TooManyArguments"));
            if (ridx < 0) {
                if (exactmatch) throw new IllegalArgumentException(NbBundle.getBundle(MapFormat.class).getString("MSG_UnmatchedBraces") +" "+lidx);
                else break;
            }
            outpat.append(newPattern.substring(idx, lidx));
            offsets[offnum] = outpat.length();
            arguments[offnum] = newPattern.substring(lidx + ldel.length(), ridx);
            idx = ridx + rdel.length();
            maxOffset++;
        }

        outpat.append(newPattern.substring(idx));
        return outpat.toString();
    }

    /**
    * Formats object.
    * @param obj Object to be formatted into string
    * @return Formatted object
    */
    private String formatObject(Object obj)
    {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return NumberFormat.getInstance(locale).format(obj); // fix
        } else if (obj instanceof Date) {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(obj);//fix
        } else if (obj instanceof String) return (String)obj;
        return obj.toString();
    }

    /**
    * Formats the parsed string by inserting table's values.
    * @param table Map with key-value pairs to replace.
    * @param result Buffer to be used for result.
    * @return Formatted string
    */
    public StringBuffer format(Object pat, StringBuffer result, FieldPosition fpos)
    {
        String pattern = processPattern((String)pat);
        int lastOffset = 0;
        for (int i = 0; i <= maxOffset; ++i) {
            int offidx = offsets[i];
            result.append(pattern.substring(lastOffset, offsets[i]));
            lastOffset = offidx;

            String key = arguments[i];
            String obj = formatObject(processKey(key));
            if (obj == null) {
                if (throwex) throw new IllegalArgumentException(MessageFormat.format(NbBundle.getBundle(MapFormat.class).getString("MSG_FMT_ObjectForKey"), new Object [] {new Integer (key)}));
                else obj = ldel+key+rdel;
            }

            result.append(obj);
        }

        result.append(pattern.substring(lastOffset, pattern.length()));
        return result;
    }

    /**
    * Parses the string. Does not yet handle recursion (where
    * the substituted strings contain %n references.)
    */
    public Object parseObject (String text, ParsePosition status) {
        return parse(text);
    }

    /**
    * Parses the string. Does not yet handle recursion (where
    * the substituted strings contain {n} references.)
    * @return New format.
    */
    public String parse(String source)
    {
        StringBuffer sbuf = new StringBuffer(source);
        Iterator key_it = argmap.keySet().iterator();
        skipped = new RangeList();
        process(source, "\"", "\""); // NOI18N
        while (key_it.hasNext()) {
            String it_key = (String)key_it.next();
            String it_obj = formatObject(argmap.get(it_key));
            int it_idx = -1;
            do {
                it_idx = sbuf.toString().indexOf(it_obj, ++it_idx);
                if (it_idx >= 0 && !skipped.containsOffset(it_idx)) {
                    sbuf.replace(it_idx, it_idx+it_obj.length(), ldel+it_key+rdel);
                    skipped = new RangeList();
                    process(sbuf.toString(), "\"", "\""); // NOI18N
                }
            } while (it_idx != -1);
        }

        return sbuf.toString();
    }

    /** Should formatter throw exception if object for key was not found?
    * If given map does not contain object for key specified, it could
    * throw an exception. Returns true if throws. If not, key is left unchanged.
    */
    public boolean willThrowExceptionIfKeyWasNotFound()
    {
        return throwex;
    }

    /** Should formatter throw exception if object for key was not found?
    * If given map does not contain object for key specified, it could
    * throw an exception. If does not throw, key is left unchanged.
    * @param flag If true, formatter throws IllegalArgumentException.
    */
    public void setThrowExceptionIfKeyWasNotFound(boolean flag)
    {
        throwex = flag;
    }

    /** Do you require both brackets in expression?
    * If not, use setExactMatch(false) and formatter will ignore missing right
    * bracket. Advanced feature.
    */
    public boolean isExactMatch()
    {
        return exactmatch;
    }

    /** Do you require both brackets in expression?
    * If not, use setExactMatch(false) and formatter will ignore missing right
    * bracket. Advanced feature.
    * @param flag If true, formatter will ignore missing right bracket (default = false)
    */
    public void setExactMatch(boolean flag)
    {
        exactmatch = flag;
    }

    /** Returns string used as left brace */
    public String getLeftBrace()
    {
        return ldel;
    }

    /** Sets string used as left brace
    * @param delimiter Left brace.
    */
    public void setLeftBrace(String delimiter)
    {
        ldel = delimiter;
    }

    /** Returns string used as right brace */
    public String getRightBrace()
    {
        return rdel;
    }

    /** Sets string used as right brace
    * @param delimiter Right brace.
    */
    public void setRightBrace(String delimiter)
    {
        rdel = delimiter;
    }

    /** Returns argument map */
    public Map getMap()
    {
        return argmap;
    }

    /** Sets argument map
    * This map should contain key-value pairs with key values used in
    * formatted string expression. If value for key was not found, formatter leave
    * key unchanged (except if you've set setThrowExceptionIfKeyWasNotFound(true),
    * then it fires IllegalArgumentException.
    * 
    * @param delimiter Right brace.
    */
    public void setMap(Map map)
    {
        argmap = map;
    }

    /**
    * Range of expression in string.
    * Used internally to store information about quotation marks and comments
    * in formatted string.
    *
    * @author   Slavek Psenicka
    * @version  1.0, March 11. 1999
    */
    class Range extends Object
    {
        /** Offset of expression */
        private int offset;

        /** Length of expression */
        private int length;

        /** Constructor */
        public Range(int off, int len)
        {
            offset = off;
            length = len;
        }

        /** Returns offset */
        public int getOffset()
        {
            return offset;
        }

        /** Returns length of expression */
        public int getLength()
        {
            return length;
        }

        /** Returns final position of expression */
        public int getEnd()
        {
            return offset+length;
        }

        public String toString()
        {
            return "("+offset+", "+length+")"; // NOI18N
        }
    }

    /**
    * List of ranges.
    * Used internally to store information about quotation marks and comments
    * in formatted string.
    *
    * @author   Slavek Psenicka
    * @version  1.0, March 11. 1999
    */
    class RangeList
    {
        /** Map with Ranges 
         * @associates Range*/
        private HashMap hmap;

        /** Constructor */
        public RangeList()
        {
            hmap = new HashMap();
        }

        /** Returns true if offset is enclosed by any Range object in list */
        public boolean containsOffset(int offset)
        {
            return (getRangeContainingOffset(offset) != null);
        }

        /** Returns enclosing Range object in list for given offset */
        public Range getRangeContainingOffset(int offset)
        {
            if (hmap.size() == 0) return null;
            int offit = offset;
            while (offit-- >= 0) {
                Integer off = new Integer(offit);
                if (hmap.containsKey(off)) {
                    Range ran = (Range)hmap.get(off);
                    if (ran.getEnd() - offset > 0) return ran;
                }
            }

            return null;
        }

        /** Puts new range into list */
        public void put(Range range)
        {
            hmap.put(new Integer(range.getOffset()), range);
        }

        public String toString()
        {
            return hmap.toString();
        }
    }
}

/*
* Log
*  12   Gandalf   1.11        1/12/00  Pavel Buzek     I18N
*  11   Gandalf   1.10        1/10/00  Radko Najman    fixed bug #4368
*  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  9    Gandalf   1.8         8/17/99  Ian Formanek    Generated serial version 
*       UID
*  8    Gandalf   1.7         7/27/99  Slavek Psenicka available argument count 
*       increased (now 255)
*  7    Gandalf   1.6         7/1/99   Martin Ryzl     processKey() method added
*        
*  6    Gandalf   1.5         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  5    Gandalf   1.4         4/6/99   Petr Hamernik   substitution works in the
*       comments too
*  4    Gandalf   1.3         4/1/99   Slavek Psenicka Oprava parse: pri 
*       opakovanem vyskytu formatovaneho tokenu se tento rozpoznal pouze jednou.
*  3    Gandalf   1.2         4/1/99   Slavek Psenicka Vynechani komentaru a 
*       uvozenych textu z formatovani i parsovani.
*  2    Gandalf   1.1         4/1/99   Slavek Psenicka Zmena prace; inicializace
*       s mapou a formatovani vice stringu. Doplneni moznosti nebazirovat na 
*       ukonceni tokenu.
*  1    Gandalf   1.0         3/23/99  Slavek Psenicka 
* $
*/