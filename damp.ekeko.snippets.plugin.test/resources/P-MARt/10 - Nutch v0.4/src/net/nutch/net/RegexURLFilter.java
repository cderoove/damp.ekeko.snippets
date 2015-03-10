/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Pattern;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.MalformedPatternException;

import net.nutch.util.*;

/** Filters URLs based on a file of regular expressions.  The config file is
 * named by the Nutch configuration property "urlfilter.regex.file".
 *
 * <p>The format of this file is:
 * <pre>
 * [+-]<regex>
 * </pre>
 * where plus means go ahead and index it and minus means no.
 */

public class RegexURLFilter implements URLFilter {

  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.net.RegexURLFilter");

  private static class Rule {
    public Perl5Pattern pattern;
    public boolean sign;
    public String regex;	
  }

  private List rules;
  private PatternMatcher matcher = new Perl5Matcher();

  public RegexURLFilter() throws IOException, MalformedPatternException {
    String file = NutchConf.get("urlfilter.regex.file");
    Reader reader = NutchConf.getConfResourceAsReader(file);

    if (reader == null) {
      LOG.severe("Can't find resource: " + file);
    } else {
      rules=readConfigurationFile(reader);
    }
  }

  public RegexURLFilter(String filename)
    throws IOException, MalformedPatternException {
    rules = readConfigurationFile(new FileReader(filename));
  }

  public synchronized String filter(String url) {
    Iterator i=rules.iterator();
    while(i.hasNext()) {
      Rule r=(Rule) i.next();
      if (matcher.contains(url,r.pattern)) {
        //System.out.println("Matched " + r.regex);
        return r.sign ? url : null;
      }
    };
        
    return null;   // assume no go
  }

  //
  // Format of configuration file is
  //    
  // [+-]<regex>
  //
  // where plus means go ahead and index it and minus means no.
  // 

  private static List readConfigurationFile(Reader reader)
    throws IOException, MalformedPatternException {

    BufferedReader in=new BufferedReader(reader);
    Perl5Compiler compiler=new Perl5Compiler();
    List rules=new ArrayList();
    String line;
       
    while((line=in.readLine())!=null) {
      if (line.length() == 0)
        continue;
      char first=line.charAt(0);
      boolean sign=false;
      switch (first) {
      case '+' : 
        sign=true;
        break;
      case '-' :
        sign=false;
        break;
      case ' ' : case '\n' : case '#' :           // skip blank & comment lines
        continue;
      default :
        throw new IOException("Invalid first character: "+line);
      }

      String regex=line.substring(1);

      Rule rule=new Rule();
      rule.pattern=(Perl5Pattern) compiler.compile(regex);
      rule.sign=sign;
      rule.regex=regex;
      rules.add(rule);
    }

    return rules;
  }

  public static void main(String args[])
    throws IOException, MalformedPatternException {

    RegexURLFilter filter=new RegexURLFilter();
    BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
    String line;
    while((line=in.readLine())!=null) {
      String out=filter.filter(line);
      if(out!=null) {
        System.out.print("+");
        System.out.println(out);
      } else {
        System.out.print("-");
        System.out.println(line);
      }
    }
  }

}
