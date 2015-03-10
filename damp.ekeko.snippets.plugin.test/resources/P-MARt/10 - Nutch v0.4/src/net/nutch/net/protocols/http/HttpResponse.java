/* Copyright (c) 2004 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.net.protocols.http;

import net.nutch.net.protocols.Response;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import net.nutch.fetcher.FetcherConstants;
import net.nutch.fetcher.FetcherStatus;
import net.nutch.util.GZIPUtils;


/** An HTTP response. */
public class HttpResponse implements Response {
  private URL url;
  private final Http http;
  private int code;
  private int numContinues;
  private Map headers;
  private byte[] content;
  private byte[] compressedContent;
  private MiscHttpAccounting httpAccounting;

  public URL getUrl() { return url; }

  /** Returns the response code. */
  public int getCode() { return code; }

  /** Returns the value of a named header. */
  public String getHeader(String name) { return (String)headers.get(name); }

  /** Returns the full content of the response. */
  public byte[] getContent() { return content; }

  /** 
   * Returns the compressed version of the content if the server
   * transmitted a compressed version, or <code>null</code>
   * otherwise. 
   */
  public byte[] getCompressedContent() { 
    return compressedContent; 
  }

  /**
   * Returns the number of 100/Continue headers encountered 
   */
  public int getNumContinues() {
    return numContinues;
  }

  HttpResponse(Http http, URL url) 
    throws IOException, HttpException {
    this(http, url, null, null, Http.HTTP_VER_LATEST);
  }

  HttpResponse(Http http, URL url, InetAddress addr,
           MiscHttpAccounting httpAccounting,
           int httpVersion) 
    throws IOException, HttpException {

    this.url = url;
    this.httpAccounting= httpAccounting;
    this.http = http;

    if (!"http".equals(url.getProtocol()))
      throw new IOException("Not an HTTP url:" + url);

    if ( (httpVersion < 0) || (httpVersion > Http.HTTP_VER_LATEST) )
      httpVersion= Http.HTTP_VER_LATEST;

    if (Http.LOG.isLoggable(Level.FINE))
      Http.LOG.fine("fetching " + url);

    String path = "".equals(url.getFile()) ? "/" : url.getFile();

    // some servers will redirect a request with a host line like
    // "Host: <hostname>:80" to "http://<hpstname>/<orig_path>"- they
    // don't want the :80...
    int port;
    String portString;
    if (url.getPort() == -1) {
      port= 80;
      portString= "";
    } else {
      port= url.getPort();
      portString= ":" + port;
    }

    Socket socket = null;

    try {
      socket = new Socket();                    // create the socket
      socket.setSoTimeout(this.http.timeout);

      if (addr == null) {
       	addr=InetAddress.getByName(this.http.proxyenabled?this.http.proxyHost:url.getHost());
        if (httpAccounting != null) 
          httpAccounting.setAddr(addr);
      }

      // connect
      InetSocketAddress sockAddr= new InetSocketAddress(addr, this.http.proxyenabled?this.http.proxyPort:port);
      socket.connect(sockAddr, this.http.timeout);

      OutputStream req = socket.getOutputStream(); // make request

      StringBuffer reqStr = new StringBuffer("GET ");
      if(this.http.proxyenabled){
      	reqStr.append(url.getProtocol()).append("://").append(url.getHost()).append(portString).append(path);
      } else {
      	reqStr.append(path);
      }

      if (httpVersion == Http.HTTP_VER_1_1)
        reqStr.append(" HTTP/1.1\r\n");
      else 
        reqStr.append(" HTTP/1.0\r\n");

      reqStr.append("Host: ");
      reqStr.append(url.getHost());
      reqStr.append(portString);
      reqStr.append("\r\n");

      if (httpVersion == Http.HTTP_VER_1_1) {
        reqStr.append("Accept-Encoding: x-gzip, gzip\r\n");
        reqStr.append("Connection: close\r\n");
      }

      if ((this.http.agentString == null) || (this.http.agentString.length() == 0)) {
        Http.LOG.severe("User-agent is not set!");
      } else {
        reqStr.append("User-Agent: ");
        reqStr.append(this.http.agentString);
        reqStr.append("\r\n");
      }

      reqStr.append("\r\n");
      byte[] reqBytes= reqStr.toString().getBytes();

      if (httpAccounting != null) 
        httpAccounting.incrementBytesSent(reqBytes.length);

      req.write(reqBytes);
      req.flush();
        
      PushbackInputStream in =                  // process response
        new PushbackInputStream(
          new BufferedInputStream(socket.getInputStream(), Http.BUFFER_SIZE), 
          Http.BUFFER_SIZE) ;

      StringBuffer line = new StringBuffer();

      numContinues= -1;
      boolean haveSeenNonContinueStatus= false;
      while (!haveSeenNonContinueStatus) {
        numContinues++;
        // parse status code line
        this.code = parseStatusLine(in, line); 
        // parse headers
        this.headers = parseHeaders(in, line); 
        haveSeenNonContinueStatus= code != 100; // 100 is "Continue"
      }

      String transferCoding= getHeader("Transfer-Encoding");
      if ("chunked".equals(transferCoding)) {
        Http.LOG.fine("fetching chunked!");
        try {
          readChunkedContent(in, line);
        } catch (EOFException e) {
          throw new ChunkEOFException("");
        }
      } else {
        Http.LOG.fine("fetching plain!");
        readPlainContent(in);
      }

      String contentEncoding= getHeader("Content-Encoding");
      if ("gzip".equals(contentEncoding)
          || "x-gzip".equals(contentEncoding)) {
        Http.LOG.fine("uncompressing....");
        compressedContent= content;
        FetcherStatus.logTraceMisc(FetcherConstants.MISC_INFORMATIONAL, 
                                   "about to decompress: " + url);

        content= GZIPUtils.unzipBestEffort(compressedContent, 
                                           this.http.maxContentLength);
        if (content == null)
          throw new DecompressionException("unzipBestEffort returned null");

        if (Http.LOG.isLoggable(Level.FINE))
          Http.LOG.fine("fetched " + compressedContent.length
                        + " bytes of compressed content (expanded to "
                        + content.length + " bytes) from " + url);
      } else {
        if (Http.LOG.isLoggable(Level.FINE))
          Http.LOG.fine("fetched " + content.length + " bytes from " + url);
      }

    } finally {
      if (socket != null)
        socket.close();
    }

  }

  private void readPlainContent(InputStream in) 
    throws HttpException, IOException {

    int contentLength = Integer.MAX_VALUE;    // get content length
    String contentLengthString = (String)headers.get("Content-Length");
    if (contentLengthString != null) {
      contentLengthString = contentLengthString.trim();
      try {
        contentLength = Integer.parseInt(contentLengthString);
      } catch (NumberFormatException e) {
        throw new ContentLengthParseException(contentLengthString);
      }
    }
    if (contentLength > this.http.maxContentLength)   // limit download size
      contentLength  = this.http.maxContentLength;

    ByteArrayOutputStream out = new ByteArrayOutputStream(Http.BUFFER_SIZE);
    byte[] bytes = new byte[Http.BUFFER_SIZE];
    int length = 0;                           // read content
    for (int i = in.read(bytes); i != -1; i = in.read(bytes)) {

      if (httpAccounting != null) 
        httpAccounting.incrementBytesRead(i);

      out.write(bytes, 0, i);
      length += i;
      if (length >= contentLength)
        break;
    }
    this.content = out.toByteArray();
  }

  private void readChunkedContent(PushbackInputStream in,  
                                  StringBuffer line) 
    throws HttpException, IOException {
    boolean doneChunks= false;
    int contentBytesRead= 0;
    byte[] bytes = new byte[Http.BUFFER_SIZE];
    ByteArrayOutputStream out = new ByteArrayOutputStream(Http.BUFFER_SIZE);

    while (!doneChunks) {
      Http.LOG.fine("Http: starting chunk");

      Http.readLine(in, line, false);

      if (httpAccounting != null) 
        httpAccounting.incrementBytesRead(line.length());

      String chunkLenStr;
      // LOG.fine("chunk-header: '" + line + "'");

      int pos= line.indexOf(";");
      if (pos < 0) {
        chunkLenStr= line.toString();
      } else {
        chunkLenStr= line.substring(0, pos);
        // LOG.fine("got chunk-ext: " + line.substring(pos+1));
      }
      chunkLenStr= chunkLenStr.trim();
      int chunkLen;
      try {
        chunkLen= Integer.parseInt(chunkLenStr, 16);
      } catch (NumberFormatException e){ 
        throw new ContentLengthParseException(line.toString());
      }

      if (chunkLen == 0) {
        doneChunks= true;
        break;
      }

      if ( (contentBytesRead + chunkLen) > this.http.maxContentLength )
        chunkLen= this.http.maxContentLength - contentBytesRead;

      // read one chunk
      int chunkBytesRead= 0;
      while (chunkBytesRead < chunkLen) {

        int toRead= (chunkLen - chunkBytesRead) < Http.BUFFER_SIZE ?
                    (chunkLen - chunkBytesRead) : Http.BUFFER_SIZE;
        int len= in.read(bytes, 0, toRead);

        if (len == -1) 
          throw new ChunkEOFException("after " + contentBytesRead
                                      + " bytes in successful chunks"
                                      + " and " + chunkBytesRead 
                                      + " in current chunk");

        // DANGER!!! Will printed GZIPed stuff right to your
        // terminal!
        // LOG.fine("read: " +  new String(bytes, 0, len));

        if (httpAccounting != null) 
          httpAccounting.incrementBytesRead(len);

        out.write(bytes, 0, len);
        chunkBytesRead+= len;  
      }

      Http.readLine(in, line, false);

      if (httpAccounting != null) 
        httpAccounting.incrementBytesRead(line.length());

    }

    if (!doneChunks) {
      if (contentBytesRead != this.http.maxContentLength) 
        throw new ChunkEOFException("!doneChunk && didn't max out");
      return;
    }

    this.content= out.toByteArray();
    parseHeaders(in, line);

  }

  private int parseStatusLine(PushbackInputStream in, StringBuffer line)
    throws IOException, HttpException {
    Http.readLine(in, line, false);

    // approximate bytes by chars- should be right for HTTP
    if (httpAccounting != null) 
      httpAccounting.incrementBytesRead(line.length());

    int codeStart = line.indexOf(" ");
    int codeEnd = line.indexOf(" ", codeStart+1);

    // handle lines with no plaintext result code, ie:
    // "HTTP/1.1 200" vs "HTTP/1.1 200 OK"
    if (codeEnd == -1) 
      codeEnd= line.length();

    int code;
    try {
      code= Integer.parseInt(line.substring(codeStart+1, codeEnd));
    } catch (NumberFormatException e) {
      throw new BadStatusLineException("bad status line '" + line 
                                       + "': " + e.getMessage(), e);
    }

    int versionCode= -1;
    int servVersionCode= Http.HTTP_VER_NOTSET;
    try {
      int httpMajorVer= 0;
      int httpMinorVer= 0;
        
      if (line.toString().startsWith("HTTP/")) {
        int dotPos= line.indexOf(".");
        httpMajorVer= Integer.parseInt( line.substring(5, dotPos) );
        httpMinorVer= Integer.parseInt( line.substring(dotPos+1, codeStart) );

        if (httpMajorVer == 1) {
          if (httpMinorVer < 1) 
            versionCode= Http.HTTP_VER_1_0;
          else 
            versionCode= Http.HTTP_VER_1_1;
        }
      }

    } catch (NumberFormatException e) {
      ;
    }

    if (versionCode == Http.HTTP_VER_NOTSET) // bogus, always fall back
      servVersionCode= Http.HTTP_VER_1_0;

    if (httpAccounting != null) {
      httpAccounting.setServHttpVersion(servVersionCode);
    }

    return code;
  }


  private void processHeaderLine(StringBuffer line, TreeMap headers)
    throws IOException, HttpException {
    int colonIndex = line.indexOf(":");       // key is up to colon
    if (colonIndex == -1) {
      int i;
      for (i= 0; i < line.length(); i++)
        if (!Character.isWhitespace(line.charAt(i)))
          break;
      if (i == line.length())
        return;
      throw new BadHeaderLineException("No colon in header:" + line);
    }
    String key = line.substring(0, colonIndex);

    int valueStart = colonIndex+1;            // skip whitespace
    while (valueStart < line.length()) {
      int c = line.charAt(valueStart);
      if (c != ' ' && c != '\t')
        break;
      valueStart++;
    }
    String value = line.substring(valueStart);

    headers.put(key, value);
  }

  private Map parseHeaders(PushbackInputStream in, StringBuffer line)
    throws IOException, HttpException {
    TreeMap headers = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    return parseHeaders(in, line, headers);
  }

  // Adds headers to an existing TreeMap
  private Map parseHeaders(PushbackInputStream in, StringBuffer line,
                           TreeMap headers)
    throws IOException, HttpException {
    while (Http.readLine(in, line, true) != 0) {

      // handle HTTP responses with missing blank line after headers
      int pos;
      if ( ((pos= line.indexOf("<!DOCTYPE")) != -1) 
           || ((pos= line.indexOf("<HTML")) != -1) 
           || ((pos= line.indexOf("<html")) != -1) ) {

        in.unread(line.substring(pos).getBytes("UTF-8"));
        line.setLength(pos);

        // approximate bytes by chars- should be right for HTTP
        if (httpAccounting != null) 
          httpAccounting.incrementBytesRead(pos);

        try {
          processHeaderLine(line, headers);
        } catch (Exception e) {
          // fixme:
          e.printStackTrace();
        }

        return headers;
      }

      // approximate bytes by chars- should be right for HTTP
      if (httpAccounting != null) 
        httpAccounting.incrementBytesRead(line.length());

      processHeaderLine(line, headers);
    }
    return headers;
  }
}
