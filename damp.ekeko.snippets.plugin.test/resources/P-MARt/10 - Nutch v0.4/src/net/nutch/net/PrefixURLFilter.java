/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

// $Id: PrefixURLFilter.java,v 1.1 2004/08/17 16:34:30 guehene Exp $

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

import net.nutch.util.*;

/** Filters URLs based on a file of URL prefixes. The config file is
 * named by the Nutch configuration property "urlfilter.prefix.file".
 *
 * <p>The format of this file is one URL per line.</p>
 */
public class PrefixURLFilter implements URLFilter {

  private static final Logger LOG =
    LogFormatter.getLogger("net.nutch.net.PrefixURLFilter");

  private TrieStringMatcher trie;

  public PrefixURLFilter() throws IOException {
    String file = NutchConf.get("urlfilter.prefix.file");
    Reader reader = NutchConf.getConfResourceAsReader(file);

    if (reader == null) {
      LOG.severe("Can't find resource: " + file);
    } else {
      trie = readConfigurationFile(reader);
    }
  }

  public PrefixURLFilter(String filename) throws IOException {
    trie = readConfigurationFile(new FileReader(filename));
  }

  public String filter(String url) {
    if (trie.shortestMatch(url) == null)
      return null;
    else
      return url;
  }

  private static TrieStringMatcher readConfigurationFile(Reader reader)
    throws IOException {
    
    BufferedReader in=new BufferedReader(reader);
    List urlprefixes = new ArrayList();
    String line;

    while((line=in.readLine())!=null) {
      if (line.length() == 0)
        continue;

      char first=line.charAt(0);
      switch (first) {
      case ' ' : case '\n' : case '#' :           // skip blank & comment lines
        continue;
      default :
	urlprefixes.add(line);
      }
    }

    return new PrefixStringMatcher(urlprefixes);
  }

  public static void main(String args[])
    throws IOException {
    
    PrefixURLFilter filter;
    if (args.length >= 1)
      filter = new PrefixURLFilter(args[0]);
    else
      filter = new PrefixURLFilter();
    
    BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
    String line;
    while((line=in.readLine())!=null) {
      String out=filter.filter(line);
      if(out!=null) {
        System.out.println(out);
      }
    }
  }
  
}
