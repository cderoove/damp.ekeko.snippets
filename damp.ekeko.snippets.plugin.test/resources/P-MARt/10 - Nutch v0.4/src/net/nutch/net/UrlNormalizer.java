/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net;

import java.net.URL;
import java.net.MalformedURLException;
// import java.net.URI;
// import java.net.URISyntaxException;

import java.util.logging.Logger;
import net.nutch.util.LogFormatter;

/** Converts URLs to a normal form . */
public class UrlNormalizer {
  public static final Logger LOG =
    LogFormatter.getLogger("net.nutch.net.UrlNormalizer");

  public static String normalize(String urlString)
    throws MalformedURLException {

    if ("".equals(urlString))                     // permit empty
      return urlString;

    urlString = urlString.trim();                 // remove extra spaces

    URL url = new URL(urlString);

    String protocol = url.getProtocol();
    String host = url.getHost();
    int port = url.getPort();
    String file = url.getFile();

    boolean changed = false;

    if (!urlString.startsWith(protocol))        // protocol was lowercased
      changed = true;

    if ("http".equals(protocol) || "ftp".equals(protocol)) {
      
      if (host != null) {
        String newHost = host.toLowerCase();    // lowercase host
        if (!host.equals(newHost)) {
          host = newHost;
          changed = true;
        }
      }

      if (port == url.getDefaultPort()) {       // uses default port
        port = -1;                              // so don't specify it
        changed = true;
      }

      if (file == null || "".equals(file)) {    // add a slash
        file = "/";
        changed = true;
      }

      if (url.getRef() != null) {                 // remove the ref
        changed = true;
      }

    }

    if (changed)
      urlString = new URL(protocol, host, port, file).toString();

    return urlString;
  }

}
