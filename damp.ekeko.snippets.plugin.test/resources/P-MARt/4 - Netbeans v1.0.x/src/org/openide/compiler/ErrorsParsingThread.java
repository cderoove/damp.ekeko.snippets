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

package org.openide.compiler;

import java.io.IOException;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;
import java.util.StringTokenizer;


import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.CharacterIterator;
import org.apache.regexp.ReaderCharacterIterator;
import org.apache.regexp.StringCharacterIterator;

import org.openide.filesystems.*;
import org.openide.TopManager;
import org.openide.util.NbBundle;

/** Connected to stdout/stderr external compiler - parses errors
*
* @author Ales Novak
*/
final class ErrorsParsingThread extends Thread {

    /** user.dir property */
    private static final String USERDIR;
    static {
        String tmp = System.getProperty("user.dir", "." + java.io.File.separator);
        if (! tmp.endsWith(java.io.File.separator))
            tmp = tmp + java.io.File.separator;
        USERDIR = tmp;
    }

    /** format of the error */
    private static MessageFormat unknownFile;

    /** Warning string "warning: "  - errors that contains the string are t
    reated as warnings */
    static final String WARNING = ExternalCompiler.getLocalizedString("FMT_Warning");

    /** reference for firing errors */
    private ExternalCompilerGroup compiler;

    /** parsed stream */
    private BufferedReader parsedReader;

    /** flag for parsing loop whether continue or not */
    private boolean stopParsing = false;

    private int filePos;
    private int linePos;
    private int columnPos;
    private int descriptionPos;
    private int refTextPos;

    /** Regular expression which matches e.g. c:\java */
    RE clsPath;
    /** Error format of a compiler */
    RE errorPattern;
    /** Optional pattern for a file */
    RE filePattern;
    /** Optional pattern for a line */
    RE linePattern;
    /** Optional - read lines from the stream */
    StringBuffer rlines;
    /** Optional */
    ErrorsParsingThread him;

    /**
    * @param <code>is</code> is an InputStream that is parsed
    * @param ExternalCompilerGroup is a refernce to class through
    * that errors are fired
    * @param classPath is a classpath of external process
    */
    ErrorsParsingThread(java.io.InputStream is,
                        ExternalCompilerGroup c,
                        String classPath,
                        ExternalCompiler.ErrorExpression err,
                        ErrorsParsingThread him
                       ) {

        compiler = c;
        parsedReader = new BufferedReader(new InputStreamReader(is));
        rlines = (him == null ? new StringBuffer(150) : him.rlines);
        this.him = him;

        String errExpr = err.getErrorExpression ();
        filePos = err.getFilePos();
        linePos = err.getLinePos();
        columnPos = err.getColumnPos();
        descriptionPos = err.getDescriptionPos();
        refTextPos = -1; // expr.getRefTextPos();

        try {
            RECompiler rec = new RECompiler();
            REProgram rep;
            int ix = errExpr.indexOf("||"); // NOI18N
            if (ix < 0) {
                rep = rec.compile(errExpr);
                errorPattern = new RE(rep, RE.MATCH_MULTILINE);
            } else {
                rep = rec.compile(errExpr.substring(0, ix));
                filePattern = new RE(rep, RE.MATCH_MULTILINE);
                rep = rec.compile(errExpr.substring(ix + 2));
                linePattern = new RE(rep, RE.MATCH_MULTILINE);
            }
            rep = rec.compile(doubleSlashes(sortAndMakeOr(classPath)));
            clsPath = new RE(rep);
        } catch (RESyntaxException e) {
            TopManager.getDefault().notifyException(e);
        }
    }

    /** fires errors */
    private void notifyError(
        String file,
        int line,
        int column,
        String message,
        String ref
    ) {
        FileObject fo = null;
        if (file != null) {
            fo = string2File(file);
            if (fo == null) { // panic - write as is
                Object[] args = new Object[] {
                                    file.replace(java.io.File.separatorChar, '/'),
                                    new Integer(line),
                                    new Integer(column),
                                    message
                                };
                message = getUnknownFile().format(args);
            }
        } else if (message.equals("")) { // NOI18N
            return; // nothing to say
        }

        ErrorEvent ev = new ErrorEvent(compiler,
                                       fo,
                                       line,
                                       column,
                                       message,
                                       ref
                                      );
        compiler.fireErrEvent(ev);
    }

    /** converts string to FileObject
    * e.g z:\java\huh\Suck.java to file object in fs z:\java with package huh
    * name Suck and extension java
    */
    private FileObject string2File(String ffile) {

        String classPathEntry;
        String tmp;
        String file = ffile = ffile.replace('/', File.separatorChar);
        for (int i = 0; i < 2; i++) { // for goto used

            if (clsPath.match(ffile)) { // should be true
                tmp = ffile.substring(clsPath.getParen(0).length());
                String[] names = new String[3];
                parseFilename(tmp, names);
                FileObject fo = org.openide.TopManager.
                                getDefault().getRepository ().find(names[0], names[1], names[2]);
                return fo;        // check [classPathEntry -eq fo.getFileSystem()] ?
            } else if (i == 1) {
                String[] names = new String[3];
                parseFilename(ffile, names);
                return org.openide.TopManager.
                       getDefault().getRepository ().find(names[0], names[1], names[2]);
            } else {
                // Microsoft jvc.exe feature fix
                int k = 0;
                while (Character.isWhitespace(file.charAt(k++)));
                file = USERDIR + file.substring(k - 1);
            }
        }
        return null;
    }

    /** Sorts given string (in format first-class-path-entry:second....) to
    * (longest-class-path-entry) and replaces : to ||
    */
    static String sortAndMakeOr(String path) {

        // prepare path
        int len = path.length() - 1;
        while (path.charAt(len--) == java.io.File.pathSeparatorChar);
        path = path.substring(0, len + 2);

        ArrayList list = new ArrayList(10);
        // tokenize Strings by : or ;
        StringTokenizer stok = new StringTokenizer(path, File.pathSeparator);

        while (stok.hasMoreTokens()) {
            list.add(stok.nextToken());
        }

        if (list.size() > 0) {

            // create a String array to sort them
            String[] a = new String[list.size()];
            a = (String[]) list.toArray(a);

            // sort
            Arrays.sort(a, new StringComparator());

            // create new class path
            // run index down to zero
            StringBuffer sb = new StringBuffer(300);
            for (int i = list.size(); --i > 0; ) {
                sb.append((String) list.get(i));
                sb.append('|');
            }
            sb.append((String) list.get(0));
            return sb.toString();
        } else {
            return path;
        }
    }

    /** Compares to object by lenght of Stringd returned by toString(). */
    static final class StringComparator implements Comparator {
        public boolean equals(Object o) {
            return super.equals(o);
        }

        public int compare(Object o1, Object o2) {
            return (o1.toString().length() - o2.toString().length());
        }
    }

    /** replaces all occurences of "\" by "\\" (doubles slashes) */ // NOI18N
    private static String doubleSlashes(String s) {
        StringBuffer buf = new StringBuffer(s.length() + 4);
        int i = 0;
        char c;
        while (i < s.length()) {
            c = s.charAt(i);
            if (c != '\\') {
                buf.append(c);
            } else {
                buf.append("\\\\"); // NOI18N
            }
            i++;
        }
        return buf.toString();
    }

    /** parses \huh\Go.java to pack huh name Go ext java
    * @param filename
    * @param names is an array of three Strings - pack, name, ext
    * it is output parameter
    * precondition: params != null && names.length() == 3
    */
    private static void parseFilename(String filename, String[] names) {
        String tmp = filename;
        String ext;
        String name;
        String pack;

        if (tmp.startsWith(File.separator)) tmp = tmp.substring(1);

        int i;
        if ((i = tmp.lastIndexOf('.')) < 0) {
            ext = "";    // we don't want the case // NOI18N
            i = tmp.length() + 1;
        }
        else ext = tmp.substring(++i);

        int j =  tmp.lastIndexOf(File.separatorChar);
        if (j < 0) {
            pack = ""; // NOI18N
            name = tmp.substring(0, --i);
        } else {
            name = tmp.substring(++j, --i);
            pack = tmp.substring(0, --j);
        }

        names[0] = pack.replace(File.separatorChar, '.');
        names[1] = name;
        names[2] = ext;
    }

    /** stops parsing
    * @param hardStop ignored
    */
    void stopParsing(boolean hardStop) {
        stopParsing = true;
        try {
            join();
        } catch (InterruptedException ex) { // ignore
        }
    }

    /** reads input stream, parses it and reports errors */
    public void run() {
        try {
            if (errorPattern != null) {
                run1();
            } else {
                // run2();
            }
        } finally {
            try {
                parsedReader.close();
            } catch (IOException e) {
                TopManager.getDefault().notifyException(e);
            }
        }
    }

    /*  private void run2() {
        
        
        BufferedReader breader = new BufferedReader(new InputStreamReader(parsedStream));
        String s;
        
        try {
          for (;;) {
            s = breader.readLine();
            if (s == null) {
              break;
            }
            synchronized (rlines) {
              rlines.append(s).append('\n');
            }
          }
        } catch (IOException e) {
          TopManager.getDefault().notifyException(e);
          return;
        }
        
        if (him == null) {
          return;
        }
                
        StringBuffer sbuff = new StringBuffer(150);
        int idx = 0;
        String currentFile = null;
        String nextFile = null;
        
        for (;;) { // all files
          sbuff.setLength(0);
          REMatch fi = filePattern.match(rlines, idx, 0, sbuff);
          currentFile = nextFile;
          if (fi == null) {        
            break;
          } else {
            nextFile = fi.toString(1);
            REMatch result;
            int iidx = 0;
            for (;;) { // all lines in one file
              result = linePattern.match(sbuff, iidx);
              if (result == null) {
                break;
              }
              
              int line;
              int column;
              String msg;
              
              if (linePos == -1) {
                line = 1;
              } else {
                try {
                  line = Integer.parseInt(result.toString(linePos));
                } catch (NumberFormatException ex) {
                  line = 1;
                }
              }

              if (columnPos == -1) {
                column = 1;
              } else {
                try {
                  column = Integer.parseInt(result.toString(columnPos));
                } catch (NumberFormatException ex) {
                  column = 1;
                }
              }

              if (descriptionPos == -1) {
                msg = "";
              } else {
                String sg = result.toString(descriptionPos);
                msg = sg == null ? "" : sg.trim();
              }
              
              notifyError(currentFile.trim(), line, column, msg, "");
              iidx = result.getEndIndex();
            }
          }
          idx = fi.getEndIndex();
        }
        
        String rst = parsedStream.substring(idx);
        int iidx = 0;
        for (;;) {
          REMatch result = linePattern.match(rst, iidx);
          if (result == null) {
            break;
          }

          int line;
          int column;
          String msg;

          if (linePos == -1) {
            line = 1;
          } else {
            try {
              line = Integer.parseInt(result.toString(linePos));
            } catch (NumberFormatException ex) {
              line = 1;
            }
          }

          if (columnPos == -1) {
            column = 1;
          } else {
            try {
              column = Integer.parseInt(result.toString(columnPos));
            } catch (NumberFormatException ex) {
              column = 1;
            }
          }

          if (descriptionPos == -1) {
            msg = "";
          } else {
            String sg = result.toString(descriptionPos);
            msg = sg == null ? "" : sg.trim();
          }

          notifyError(currentFile.trim(), line, column, msg, "");
          
          iidx = result.getEndIndex();
        }
      }
    */
    private void run1() {
        String file;

        int line;
        int column;
        String msg;
        String refText;
        String tmp;
        CharacterIterator chi = new ReaderCharacterIterator(parsedReader); //new StringCharacterIterator("test\\Print.java:10 Class test.ljlkjvoid not found.\n      public static ljlkjvoid main(String[] args) throws Exception {\n        ^\n        D:\\java\\test\\test\\Print.java:10: Return required at end of test.ljlkjvoid main(java.lang.String[]).\n          public static ljlkjvoid main(String[] args) throws Exception {\n            ^\n            D:\\java\\test\\test\\Print.java:14: Invalid declaration.\n              for (int i = 0; i < 10; i++) System.out.printl n(\"HI\");\n            ^\n            D:\\java\\test\\test\\Print.java:14: Class java.lang.System. out not found.\n              for (int i = 0; i < 10; i++) System.out.printl n(\"HI\");\n            ^\n            D:\\java\\test\\test\\Print.java:14: Invalid declaration.\n              for (int i = 0; i < 10; i++) System.out.printl n(\"HI\");\n            ^\n              5 error\n"); // NOI18N

        int idx = 0; // index of last processed char
        while (errorPattern.match(chi, idx)) {

            if (filePos == -1) {
                break;
            }

            file = errorPattern.getParen(filePos);

            if (linePos == -1) {
                line = 1;
            } else {
                try {
                    line = Integer.parseInt(errorPattern.getParen(linePos));
                } catch (NumberFormatException ex) {
                    line = 1;
                }
            }

            if (columnPos == -1) {
                column = 1;
            } else {
                try {
                    column = Integer.parseInt(errorPattern.getParen(columnPos));
                } catch (NumberFormatException ex) {
                    column = 1;
                }
            }

            if (descriptionPos == -1) {
                msg = ""; // NOI18N
            } else {
                String sg = errorPattern.getParen(descriptionPos);
                msg = sg == null ? "" : sg.trim(); // NOI18N
            }

            if (refTextPos == -1) {
                refText = ""; // NOI18N
            } else {
                String sg = errorPattern.getParen(refTextPos);
                refText = sg == null ? "" : sg.trim(); // NOI18N
            }

            // everything between idx and result.getBeginIndex() must be printed out
            String rst = chi.substring(idx, errorPattern.getParenStart(0));
            if (!rst.equals("")) { // NOI18N
                notifyError(null, -1, -1, trimString(rst), null);
            }

            if (msg.indexOf(WARNING) >= 0) {
                notifyError(null, -1, -1, errorPattern.getParen(0), null);
            } else {
                // trim for JIKES compiler
                notifyError(file != null ? file.trim() : null, line, column, msg, refText);
            }
            idx = errorPattern.getParenEnd(0);
        }
        // print the rest

        String rst = chi.substring(idx);
        if (!rst.equals("")) { // NOI18N
            notifyError(null, 0, 0, trimString(rst), null);
        }
    }

    /** trims String - patch for Microsoft jvc
    * @param s a String to trim
    * @return trimmed String
    */
    private static String trimString(String s) {
        int idx = 0;
        char c;
        final int slen = s.length();

        if (slen == 0) {
            return s;
        }

        do {
            c = s.charAt(idx++);
        } while ((c == '\n' || c == '\r') &&
                 (idx < slen));

        s = s.substring(--idx);
        idx = s.length() - 1;

        if (idx < 0) {
            return s;
        }

        do {
            c = s.charAt(idx--);
        } while ((c == '\n' || c == '\r') &&
                 (idx >= 0));

        return s.substring(0, idx + 2);
    }

    static MessageFormat getUnknownFile() {
        if (unknownFile == null) {
            unknownFile = new MessageFormat(ExternalCompiler.getLocalizedString("MSG_Unknown_file")); // NOI18N
        }
        return unknownFile;
    }
}

/*
 * Log
 *  15   Gandalf-post-FCS1.12.1.1    3/27/00  Ales Novak      a method removed
 *  14   Gandalf-post-FCS1.12.1.0    3/17/00  Ales Novak      #5944 #5904
 *  13   src-jtulach1.12        1/17/00  Ales Novak      better warnings 
 *       recognition for fastjavac
 *  12   src-jtulach1.11        1/13/00  Ian Formanek    NOI18N
 *  11   src-jtulach1.10        1/12/00  Ian Formanek    NOI18N
 *  10   src-jtulach1.9         1/10/00  Ales Novak      pending newlines trimmed
 *       in the parsed stream
 *  9    src-jtulach1.8         12/17/99 Ales Novak      #4238
 *  8    src-jtulach1.7         11/9/99  Ales Novak      StringIndexOutOfBoundsException
 *       
 *  7    src-jtulach1.6         11/1/99  Ales Novak      StringIndexOutOfBoundsException
 *       
 *  6    src-jtulach1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    src-jtulach1.4         9/14/99  Ales Novak      upgrade to 
 *       org.netbeans.lib.regexp.*
 *  4    src-jtulach1.3         7/30/99  Ales Novak      OROMatcher is left - GNU
 *       regexp is used
 *  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         3/31/99  Ales Novak      
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
