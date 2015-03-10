/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.searcher;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.nutch.util.LogFormatter;
import net.nutch.analysis.NutchAnalysis;

import net.nutch.io.Writable;

/** A Nutch query. */
public final class Query implements Writable {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.searcher.Query");

  /** A query clause. */
  public static class Clause {
    public static final String DEFAULT_FIELD = "DEFAULT";

    private static final byte REQUIRED_BIT = 1;
    private static final byte PROHIBITED_BIT = 2;
    private static final byte PHRASE_BIT = 4;

    private boolean isRequired;
    private boolean isProhibited;
    private String field = DEFAULT_FIELD;
    private float weight = 1.0f;
    private Object termOrPhrase; 

    public Clause(Term term, boolean isRequired, boolean isProhibited) {
      this.isRequired = isRequired;
      this.isProhibited = isProhibited;
      this.termOrPhrase = term;
    }

    public Clause(Phrase phrase, boolean isRequired, boolean isProhibited) {
      this.isRequired = isRequired;
      this.isProhibited = isProhibited;
      this.termOrPhrase = phrase;
    }

    public boolean isRequired() { return isRequired; }
    public boolean isProhibited() { return isProhibited; }

    public String getField() { return field; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) {  this.weight = weight; }

    public boolean isPhrase() { return termOrPhrase instanceof Phrase; }

    public Phrase getPhrase() { return (Phrase)termOrPhrase; }
    public Term getTerm() { return (Term)termOrPhrase; }

    public void write(DataOutput out) throws IOException {
      byte bits = 0;
      if (isPhrase())
        bits |= PHRASE_BIT;
      if (isRequired)
        bits |= REQUIRED_BIT;
      if (isProhibited)
        bits |= PROHIBITED_BIT;
      out.writeByte(bits);
      out.writeUTF(field);
      out.writeFloat(weight);
      
      if (isPhrase())
        getPhrase().write(out);
      else
        getTerm().write(out);
    }

    public static Clause read(DataInput in) throws IOException {
      byte bits = in.readByte();
      boolean required = ((bits & REQUIRED_BIT) != 0);
      boolean prohibited = ((bits & PROHIBITED_BIT) != 0);

      String field = in.readUTF();
      float weight = in.readFloat();

      Clause clause;
      if ((bits & PHRASE_BIT) == 0) {
        clause = new Clause(Term.read(in), required, prohibited);
      } else {
        clause = new Clause(Phrase.read(in), required, prohibited);
      }

      clause.field = field;
      clause.weight = weight;
      return clause;
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer();
//       if (isRequired)
//         buffer.append("+");
//       else
      if (isProhibited)
        buffer.append ("-");

      if (!DEFAULT_FIELD.equals(field)) {
        buffer.append(field);
        buffer.append(":");
      }

      buffer.append(termOrPhrase.toString());

      return buffer.toString();
    }

    public boolean equals(Object o) {
      if (!(o instanceof Clause)) return false;
      Clause other = (Clause)o;
      return
        (this.isRequired == other.isRequired) &&
        (this.isProhibited == other.isProhibited) &&
        (this.weight == other.weight) &&
        (this.termOrPhrase == null ? other.termOrPhrase == null :
         this.termOrPhrase.equals(other.termOrPhrase));
    }
        
    public int hashCode() {
      return
        (this.isRequired ? 0 : 1) ^
        (this.isProhibited ? 2 : 4) ^
        Float.floatToIntBits(this.weight) ^
        (this.termOrPhrase != null ? termOrPhrase.hashCode() : 0);
    }
    
  }

  /** A single-term query clause. */
  public static class Term {
    private String text;

    public Term(String text) {
      this.text = text;
    }

    public void write(DataOutput out) throws IOException {
      out.writeUTF(text);
    }

    public static Term read(DataInput in) throws IOException {
      String text = in.readUTF();
      return new Term(text);
    }

    public String toString() {
      return text;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Term)) return false;
      Term other = (Term)o;
      return text == null ? other.text == null : text.equals(other.text);
    }

    public int hashCode() {
      return text != null ? text.hashCode() : 0;
    }
  }

  /** A phrase query clause. */
  public static class Phrase {
    private Term[] terms;

    public Phrase(Term[] terms) {
      this.terms = terms;
    }

    public Phrase(String[] terms) {
      this.terms = new Term[terms.length];
      for (int i = 0; i < terms.length; i++) {
        this.terms[i] = new Term(terms[i]);
      }
    }

    public Term[] getTerms() { return terms; }

    public void write(DataOutput out) throws IOException {
      out.writeByte(terms.length);
      for (int i = 0; i < terms.length; i++)
        terms[i].write(out);
    }

    public static Phrase read(DataInput in) throws IOException {
      int length = in.readByte();
      Term[] terms = new Term[length];
      for (int i = 0; i < length; i++)
        terms[i] = Term.read(in);
      return new Phrase(terms);
    }

    public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append("\"");
      for (int i = 0; i < terms.length; i++) {
        buffer.append(terms[i].toString());
        if (i != terms.length-1)
          buffer.append(" ");
      }
      buffer.append("\"");
      return buffer.toString();
    }

    public boolean equals(Object o) {
      if (!(o instanceof Phrase)) return false;
      Phrase other = (Phrase)o;
      if (!(this.terms.length == this.terms.length))
        return false;
      for (int i = 0; i < terms.length; i++) {
        if (!this.terms[i].equals(other.terms[i]))
          return false;
      }
      return true;
    }

    public int hashCode() {
      int hashCode = terms.length;
      for (int i = 0; i < terms.length; i++) {
        hashCode ^= terms[i].hashCode();
      }
      return hashCode;
    }

  }


  private ArrayList clauses = new ArrayList();

  private static final Clause[] CLAUSES_PROTO = new Clause[0];

  /** Return all clauses. */
  public Clause[] getClauses() {
    return (Clause[])clauses.toArray(CLAUSES_PROTO);
  }

  /** Add a required term. */
  public void addRequiredTerm(String term) {
    clauses.add(new Clause(new Term(term), true, false));
  }

  /** Add a prohibited term. */
  public void addProhibitedTerm(String term) {
    clauses.add(new Clause(new Term(term), false, true));
  }

  /** Add a required phrase. */
  public void addRequiredPhrase(String[] terms) {
    if (terms.length == 0) {                      // ignore empty phrase
    } else if (terms.length == 1) {
      addRequiredTerm(terms[0]);                  // optimize to term query
    } else {
      clauses.add(new Clause(new Phrase(terms), true, false));
    }
  }

  /** Add a prohibited phrase. */
  public void addProhibitedPhrase(String[] terms) {
    if (terms.length == 0) {                      // ignore empty phrase
    } else if (terms.length == 1) {
      addProhibitedTerm(terms[0]);                // optimize to term query
    } else {
      clauses.add(new Clause(new Phrase(terms), false, true));
    }
  }

  public void write(DataOutput out) throws IOException {
    out.writeByte(clauses.size());
    for (int i = 0; i < clauses.size(); i++)
      ((Clause)clauses.get(i)).write(out);
  }
  
  public static Query read(DataInput in) throws IOException {
    Query result = new Query();
    result.readFields(in);
    return result;
  }

  public void readFields(DataInput in) throws IOException {
    clauses.clear();
    int length = in.readByte();
    for (int i = 0; i < length; i++)
      clauses.add(Clause.read(in));
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < clauses.size(); i++) {
      buffer.append(clauses.get(i).toString());
      if (i != clauses.size()-1)
        buffer.append(" ");
    }
    return buffer.toString();
  }

  public boolean equals(Object o) {
    if (!(o instanceof Query)) return false;
    Query other = (Query)o;
    return this.clauses.equals(other.clauses);
  }
  
  public int hashCode() {
    return this.clauses.hashCode();
  }


  /** Flattens a query into the set of text terms that it contains.  These are
   * terms which should be higlighted in matching documents. */
  public String[] getTerms() {
    ArrayList result = new ArrayList();
    for (int i = 0; i < clauses.size(); i++) {
      Clause clause = (Clause)clauses.get(i);
      if (!clause.isProhibited()) {
        if (clause.isPhrase()) {
          Term[] terms = clause.getPhrase().getTerms();
          for (int j = 0; j < terms.length; j++) {
            result.add(terms[j].toString());
          }
        } else {
          result.add(clause.getTerm().toString());
        }
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }


  /** Parse a query from a string. */
  public static Query parse(String query) throws IOException {
    return NutchAnalysis.parseQuery(query);
  }

  /** For debugging. */
  public static void main(String[] args) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.print("Query: ");
      String line = in.readLine();
      Query query = parse(line);
      System.out.println("Parsed: " + query);
      System.out.println("Translated: " + QueryTranslator.translate(query));
    }
  }
}
