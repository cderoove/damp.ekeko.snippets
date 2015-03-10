/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Token;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import net.nutch.util.*;

import net.nutch.searcher.Query.*;

/** Construct n-grams for frequently occuring terms and phrases while indexing.
 * Optimize phrase queries to use the n-grams. Single terms are still indexed
 * too, with n-grams overlaid.  This is achieved through the use of {@link
 * Token#setPositionIncrement(int)}.*/
public class CommonGrams {
  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.analysis.CommonGrams");
  private static final char SEPARATOR = '-';
  private static final HashMap COMMON_TERMS = new HashMap();

  static { init(); }

  private CommonGrams() {}                        // no public ctor

  private static class Filter extends TokenFilter {
    private HashSet common;
    private Token previous;
    private LinkedList gramQueue = new LinkedList();
    private LinkedList nextQueue = new LinkedList();
    private StringBuffer buffer = new StringBuffer();

    /** Construct an n-gram producing filter. */
    public Filter(TokenStream input, HashSet common) {
      super(input);
      this.common = common;
    }

    /** Inserts n-grams into a token stream. */
    public Token next() throws IOException {
      if (gramQueue.size() != 0)                  // consume any queued tokens
        return (Token)gramQueue.removeFirst();

      final Token token = popNext();
      if (token == null)
        return null;

      if (!isCommon(token)) {                     // optimize simple case
        previous = token;
        return token;
      }

      gramQueue.add(token);                       // queue the token

      ListIterator i = nextQueue.listIterator();
      Token gram = token;
      while (isCommon(gram)) {
        if (previous != null && !isCommon(previous)) // queue prev gram first
          gramQueue.addFirst(gramToken(previous, gram));

        Token next = peekNext(i);
        if (next == null)
          break;

        gram = gramToken(gram, next);             // queue next gram last
        gramQueue.addLast(gram);
      }

      previous = token;
      return (Token)gramQueue.removeFirst();
    }

    /** True iff token is for a common term. */
    private boolean isCommon(Token token) {
      return common != null && common.contains(token.termText());
    }

    /** Pops nextQueue or, if empty, reads a new token. */
    private Token popNext() throws IOException {
      if (nextQueue.size() > 0)
        return (Token)nextQueue.removeFirst();
      else
        return input.next();
    }

    /** Return next token in nextQueue, extending it when empty. */
    private Token peekNext(ListIterator i) throws IOException {
      if (!i.hasNext()) {
        Token next = input.next();
        if (next == null)
          return null;
        i.add(next);
        i.previous();
      }
      return (Token)i.next();
    }

    /** Construct a compound token. */
    private Token gramToken(Token first, Token second) {
      buffer.setLength(0);
      buffer.append(first.termText());
      buffer.append(SEPARATOR);
      buffer.append(second.termText());
      Token result = new Token(buffer.toString(),
                               first.startOffset(), second.endOffset(),
                               "gram");
      result.setPositionIncrement(0);
      return result;
    }
  }

  /** Construct using the provided config file. */
  private static void init() {
    try {
      Reader reader = NutchConf.getConfResourceAsReader
        (NutchConf.get("analysis.common.terms.file"));
      BufferedReader in = new BufferedReader(reader);
      String line;
      while ((line = in.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("#") || "".equals(line)) // skip comments
          continue;
        TokenStream ts = new NutchDocumentTokenizer(new StringReader(line));
        Token token = ts.next();
        if (token == null) {
          LOG.warning("Line does not contain a field name: " + line);
          continue;
        }
        String field = token.termText();
        token = ts.next();
        if (token == null) {
          LOG.warning("Line contains only a field name, no word: " + line);
          continue;
        }
        String gram = token.termText();
        while ((token = ts.next()) != null) {
          gram = gram + SEPARATOR + token.termText();
        }
        HashSet table = (HashSet)COMMON_TERMS.get(field);
        if (table == null) {
          table = new HashSet();
          COMMON_TERMS.put(field, table);
        }
        table.add(gram);
      }
    } catch (IOException e) {
      throw new RuntimeException(e.toString());
    }
  }

  /** Construct a token filter that inserts n-grams for common terms.  For use
   * while indexing documents.  */
  public static TokenFilter getFilter(TokenStream ts, String field) {
    return new Filter(ts, (HashSet)COMMON_TERMS.get(field));
  }

  /** Utility to convert an array of Query.Terms into a token stream. */
  private static class ArrayTokens extends TokenStream {
    private Term[] terms;
    private int index;

    public ArrayTokens(Phrase phrase) { this.terms = phrase.getTerms(); }
    
    public Token next() {
      if (index == terms.length)
        return null;
      else
        return new Token(terms[index].toString(), index, ++index);
    }
  }

  /** Optimizes phrase queries to use n-grams when possible. */
  public static String[] optimizePhrase(Phrase phrase, String field) {
    //LOG.info("Optimizing " + phrase + " for " + field);
    ArrayList result = new ArrayList();
    TokenStream ts = getFilter(new ArrayTokens(phrase), field);
    Token token, prev=null;
    int position = 0;
    try {
      while ((token = ts.next()) != null) {
        if (token.getPositionIncrement() != 0 && prev != null)
          result.add(prev.termText());
        prev = token;
        position += token.getPositionIncrement();
        if ((position + arity(token.termText())) == phrase.getTerms().length)
          break;
      }
    } catch (IOException e) {
      throw new RuntimeException(e.toString());
    }
    if (prev != null)
      result.add(prev.termText());

//     LOG.info("Optimized: ");
//     for (int i = 0; i < result.size(); i++) {
//       LOG.info(result.get(i) + " ");
//     }

    return (String[])result.toArray(new String[result.size()]);


  }

  private static int arity(String gram) {
    int index = 0;
    int arity = 0;
    while ((index = gram.indexOf(SEPARATOR, index+1)) != -1) {
      arity++;
    }
    return arity;
  }

  /** For debugging. */
  public static void main(String[] args) throws Exception {
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < args.length; i++) {
      text.append(args[i]);
      text.append(' ');
    }
    TokenStream ts =
      new NutchDocumentTokenizer(new StringReader(text.toString()));
    ts = getFilter(ts, "url");
    Token token;
    while ((token = ts.next()) != null) {
      System.out.println("Token: " + token);
    }
    String[] optimized = optimizePhrase(new Phrase(args), "url");
    System.out.print("Optimized: ");
    for (int i = 0; i < optimized.length; i++) {
      System.out.print(optimized[i] + " ");
    }
    System.out.println();
  }
  
}
