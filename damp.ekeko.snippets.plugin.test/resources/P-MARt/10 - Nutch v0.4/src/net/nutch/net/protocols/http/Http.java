/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;

import net.nutch.net.protocols.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.nutch.util.LogFormatter;
import net.nutch.util.NutchConf;

/** A simple HTTP client. */
public class Http {

  public static final Logger LOG =
  LogFormatter.getLogger("net.nutch.net.Http");

  private static final int DEFAULT_PORT = 80;
  private static final int CODE_OK = 200;
  static final int BUFFER_SIZE = 16384;
  private static final int MAX_REDIRECTS = 5;

  /** Reserved value for HTTP version number, does not denote any version */
  public static final int HTTP_VER_NOTSET= -1;
  /** HTTP version 1.0 (the earliest version we use) */
  public static final int HTTP_VER_1_0= 0;
  /** HTTP version 1.1 */
  public static final int HTTP_VER_1_1= 1;
  /** Always indicates the latest HTTP version we support, currently 1.1 */
  public static final int HTTP_VER_LATEST;

  String proxyHost=NutchConf.get("http.proxy.host");
  int proxyPort=NutchConf.getInt("http.proxy.port",8080);
  boolean proxyenabled=(proxyHost!=null && proxyHost.length()>0);
  
  int timeout = NutchConf.getInt("http.timeout", 10000);
  int maxContentLength= NutchConf.getInt("http.content.limit",64*1024);

  String agentString = NutchConf.get("http.agent.name");
  private String agentEmail = NutchConf.get("http.agent.email");

  static {
    if (NutchConf.getBoolean("http.version.1.1", true)) 
      HTTP_VER_LATEST= HTTP_VER_1_1;
    else 
      HTTP_VER_LATEST= HTTP_VER_1_0;
  }

  /**
   *  Returns the HTTP version code which represents a lesser version
   *  of HTTP, or HTTP_VER_NOTSET if both equal that value.
   */
  public static int minHttpVersion(int ver1, int ver2) {
    if (ver1 < ver2) {
      if (ver1 == HTTP_VER_NOTSET) 
        return ver2;
      return ver1;
    }
    if (ver2 == HTTP_VER_NOTSET) 
      return ver1;
    return ver2;
  }

  /** Set the timeout. */
  public void setTimeout(int timeout) {this.timeout = timeout;}

  /** Set the point at which content is truncated. */
  public void setMaxContentLength(int length) {this.maxContentLength = length;}

  /** Set the agent name */
  public void setAgentString(String agentString) {
    this.agentString = agentString;
  }

  /** set the return email address */
  public void setAgentEmail(String agentEmail) {this.agentEmail = agentEmail;}

  /** 
   * Make a single HTTP request and return its response, not following
   * redirects and not translating HTTP errors to exceptions.  If
   * <code>addr</code> is not null, that address will be used.  If
   * <code>httpAccounting</code> is not <code>null</code>, the it's
   * fields will be upated during this request.  The request will be issued 
   * using the HTTP version specified by <code>httpVersion</code>.
   */
  public Response getRawResponse(URL url, InetAddress addr,
                                 MiscHttpAccounting httpAccounting,
                                 int httpVersion)
    throws IOException, HttpException {
    return new HttpResponse(this, url, addr, httpAccounting, httpVersion);
  }

  /** Returns the content of a URL.  Follow redirects and translate HTTP errors
   * to exceptions. */
  public Response getResponse(URL url) throws IOException, HttpException {

    int redirects = 0;
    URL target = url;

    while (true) {
      Response response = new HttpResponse(this, target);   // make a request

      int code = response.getCode();

      if (code == 200) {                          // got a good response
        return response;                          // return it

      } else if (code >= 300 && code < 400) {     // handle redirect
        if (redirects == MAX_REDIRECTS)
          throw new HttpException("Too many redirects: " + url);
        target = new URL(response.getHeader("Location"));
        redirects++;                
        LOG.fine("redirect to " + target); 

      } else {                                    // convert to exception
        throw new HttpError(code);
      }
    } 
  }

  static int readLine(PushbackInputStream in, StringBuffer line,
                      boolean allowContinuedLine)
    throws IOException {
    line.setLength(0);
    for (int c = in.read(); c != -1; c = in.read()) {
      switch (c) {
        case '\r':
          if (peek(in) == '\n') {
            in.read();
          }
        case '\n': 
          if (line.length() > 0) {
            // at EOL -- check for continued line if the current
            // (possibly continued) line wasn't blank
            if (allowContinuedLine) 
              switch (peek(in)) {
                case ' ' : case '\t':                   // line is continued
                  in.read();
                  continue;
              }
          }
          return line.length();      // else complete
        default :
          line.append((char)c);
      }
    }
    throw new EOFException();
  }

  private static int peek(PushbackInputStream in) throws IOException {
    int value = in.read();
    in.unread(value);
    return value;
  }

  /** For debugging. */
  public static void main(String[] args) throws Exception {
    int timeout = -1;
    boolean verbose = false;
    String urlString = null;

    String usage = "Usage: Http [-verbose] [-timeout N] url";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }
      

    for (int i = 0; i < args.length; i++) {       // parse command line
      if (args[i].equals("-timeout")) {           // found -timeout option
        timeout = Integer.parseInt(args[++i]) * 1000;
      } else if (args[i].equals("-verbose")) {    // found -verbose option
        verbose = true;
      } else if (i != args.length-1) {
        System.err.println(usage);
        System.exit(-1);
      } else                                      // root is required parameter
        urlString = args[i];
    }

    Http http = new Http();

    if (timeout != -1)                            // set timeout
      http.setTimeout(timeout);
    // set log level
    if (verbose) {
      LOG.setLevel(Level.FINE);
    }

    Response response = http.getResponse(new URL(urlString));

    System.out.println("Code = " + response.getCode());
    System.out.println("Content Type: " + response.getHeader("Content-Type"));
    System.out.println("Content Length: " + response.getHeader("Content-Length"));
    System.out.println("Content:");
    
    String content = new String(response.getContent());

    System.out.println(content);

  }

}
