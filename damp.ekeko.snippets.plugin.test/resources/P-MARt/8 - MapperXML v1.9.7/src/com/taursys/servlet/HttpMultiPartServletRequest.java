/**
 * HttpMultiPartServletRequest - Request adapter for multipart type requests
 *
 * Copyright (c) 2002
 *      Marty Phelan, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.taursys.servlet;

import java.util.*;
import java.text.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.security.Principal;

/**
 * HttpMultiPartServletRequest is a wrapper/adapter for multipart type requests.
 * This class is used specifically for handling multipart/form-data type resuests
 * (which are not handled by the standard HttpServletRequest API). Multipart
 * type requests are used when a browser submits form data that contains
 * one or more files to upload. When receiving a multipart/form-data request,
 * this class parses the data stream and extracts the form data and uploaded
 * files.
 * <p>
 * To upload a file using an html form, you must set the
 * <code>enctype="multipart/form-data"</code> in the <code>form</code> tag of
 * the html. Below is an example of the required html:
 * <pre>
 * &lt;html&gt;
 * &lt;head&gt;&lt;/head&gt;
 * &lt;body&gt;
 *   ...
 *   &lt;form enctype="multipart/form-data" method="post" action="MyServeltUrl.sf"&gt;
 *     &lt;input type="text" name="color" value="enter your color choice"/&gt;
 *     ...
 *     &lt;input type="file" name="orderFile"/&gt;
 *     &lt;input type="file" name="photo"/&gt;
 *     &lt;input type="submit" name="action" value="Send Order"/&gt;
 *   &lt;/form&gt;
 * &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * <p>
 * This request wrapper is used by default by the ServletForm to help service
 * an incoming multipart/form-data type request.  To use this adapter in a
 * servlet, simply create an instance of this class, set the HttpServletRequest
 * property to the incoming request and invoke the parseRequest method.  Then
 * used this class as the request object.  Below is an example:
 * <pre>
 * public void doPost(HttpServletRequest request, HttpServletResponse response)
 *     throws IOException, MultiPartRequestContentException {
 *   HttpMultiPartServletRequest rq = new HttpMultiPartServletRequest(request);
 *   rq.parseRequest();
 *   String orderXML = rq.getParameter("orderFile");
 *   byte[] photo = rq.getParameterByteArray("");
 *   ...
 * </pre>
 * <p>
 * In addition to the normal request parameters, information about uploaded
 * files as well as the file data itself is available through special parameters.
 * These special parameters names are made up of the input control's name with
 * a suffix of "_FileName", "_ContentType" and "_ByteArray". For uploaded files
 * with a Content-Type of "text/...", the following parameters are available:
 * <ul>
 * <li>Example where input control's "name" attribute is "myFile" and the
 * user selected "readlist.txt" to upload.
 * </li>
 * <li><code>request.getParameter("myFile")</code> will return the text
 * contents of the uploaded file.
 * </li>
 * <li><code>request.getParameter("myFile_FileName")</code> will return
 * "readlist.txt", the name of the uploaded file. (Note: may contain path
 * information depending on browser).
 * </li>
 * <li><code>request.getParameter("myFile_ContentType")</code> will return
 * "text/plain", the mime content type of the uploaded file.
 * </li>
 * <li><code>request.getParameter("myFile_ByteArray")</code> will return null.
 * It is not used by this type of file.
 * </li>
 * </ul>
 * For uploaded files with a Content-Type of "application/...", "image/...",
 * "video/...", or "audio/..." the following parameters are available:
 * <ul>
 * <li>Example where input control's "name" attribute is "myPicture" and the
 * user selected "portrait.jpg" to upload.
 * </li>
 * <li><code>request.getParameter("myPicture")</code> will return an empty
 * String ("").
 * </li>
 * <li><code>request.getParameter("myPicture_FileName")</code> will return
 * "portrait.jpg", the name of the uploaded file. (Note: may contain path
 * information depending on browser).
 * </li>
 * <li><code>request.getParameter("myPicture_ContentType")</code> will return
 * "image/jpeg" the mime content type of the uploaded file.
 * </li>
 * <li><code>request.getParameter("myPicture_ByteArray")</code> will return a
 * byte array containing the binary data of the picture.
 * </li>
 * </ul>
 * This class implements the HttpServletRequest interface. In addition to the
 * regular methods, there are a few additional methods of interest:
 * <ul>
 * <li><code>parseRequest</code> This will parse the request and make all the
 * parameters and content available. This is automatically invoked by used by a
 * ServletForm.
 * </li>
 * <li><code>get/setmaxFileSize</code>Controls the maximum allowed size for
 * an uploaded file. Files beyond this size will cause a MultiPartRequestContentException.
 * The default size is 1 megabyte.
 * </li>
 * <li><code>getParameter(String name)</code>This method functions as specified
 * in the servlet API. It will return the String value of the parameter.
 * </li>
 * <li><code>getParameterByteArray(String name)</code>Returns the requested
 * parameter as an array of bytes rather than a String. No character conversion
 * occurs. This method is only works for the "_ByteArray" parameter.
 * </li>
 * <li><code>getParameterByteArrays(String name)</code>Returns an array of
 * byte arrays. This is used when multiple files are uploaded with the same
 * parameter name.
 * </li>
 * <li><code>getParameterNames()</code>Returns an enumeration of parameter
 * names (as specified in servlet API).
 * </li>
 * <li><code>getParameterValues(String name)</code> Returns a String array of
 * values for the given parameter. This is used when multiple values are sent
 * under the same parameter name. (as specified in servlet API).
 * </li>
 * <li><code>getReader()</code>You should NOT invoke this method. It will
 * always throw an <code>IllegalStateException</code>.
 * </li>
 * <li><code>getRequest</code> Provides access to the original underlying
 * HttpServletRequest.
 * </li>
 * </ul>
 * Most of the remaining methods of the HttpServletRequest API implemented in
 * this class simply call the cooresponding method in the HttpServletRequest.
 * There are a few additional public methods which are exposed for the sole
 * purpose of unit testing: they are not intended for general use.
 * @author Marty Phelan
 * @version 1.0
 */
public class HttpMultiPartServletRequest implements HttpServletRequest {
  private HashMap map = new HashMap();
  private String boundary;
  private StringTokenizer tokens;
  private boolean endOfData;
  private ServletInputStream servletInputStream;
  private int maxLineLength = 4096;
  private int maxBufferSize = 1024 * 64;
  private int bufferSize = maxBufferSize;
  private int bufferPosition = maxBufferSize;
  private byte[] buffer = new byte[maxBufferSize];
  private int maxFileSize = 1024 * 1024; // default 1M
  private HttpServletRequest request;
  private static final String QUOTE = "\"";
  private static final String TOKEN_DELIM_DEFAULT = " :;=" + QUOTE;
  private String startBoundary;
  private String endBoundary;
  private String currentDelimiters = TOKEN_DELIM_DEFAULT;
  /**
   * String suffix for byte array parameter. Value "_ByteArray".
   */
  public static final String PARM_BYTE_ARRAY_SUFFIX = "_ByteArray";
  /**
   * String suffix for file name parameter. Value "_FileName".
   */
  public static final String PARM_FILE_NAME_SUFFIX = "_FileName";
  /**
   * String suffix for file content type. Value "_ContentType".
   */
  public static final String PARM_CONTENT_TYPE_SUFFIX = "_ContentType";
  /**
   * String identifier for Basic authentication. Value "BASIC"
   */
  public static final String BASIC_AUTH = "BASIC";
  /**
   * String identifier for Basic authentication. Value "FORM"
   */
  public static final String FORM_AUTH = "FORM";
  /**
   * String identifier for Basic authentication. Value "CLIENT_CERT"
   */
  public static final String CLIENT_CERT_AUTH = "CLIENT_CERT";
  /**
   * String identifier for Basic authentication. Value "DIGEST"
   */
  public static final String DIGEST_AUTH = "DIGEST";
  /**
   * String identifier for multipart request type. Value "multipart/form-data"
   */
  public static final String MULTIPART_FORM_DATA = "multipart/form-data";

  /**
   * Constructs a new HttpMultiPartServletRequest for the given HttpServletRequest.
   * You must invoke the <code>parseRequest()</code> method before using this
   * class.
   */
  public HttpMultiPartServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  // ========================================================================
  //                         Public Methods
  // ========================================================================

  /**
   * Parse the servlet request and extract parameter information. This method
   * checks to ensure that this is a multipart/form-data type request. It then
   * uses the ServletInputStream to read the body of the request. It parses
   * the request body and stores parameters and files in the parameter map.
   * @throws MultiPartRequestSizeException if max line length or file size exceeded
   * @throws MultiPartRequestContentException if invalid data received
   * @throws IOException if problem reading data stream
   */
  public void parseRequest() throws MultiPartRequestSizeException,
      MultiPartRequestContentException, IOException {
    try {
      endOfData = false;
      setupBoundary();
      expectLine(startBoundary);
      while(!endOfData)
        processBlock();
    } finally {
      if (servletInputStream != null) {
        servletInputStream.skip(Long.MAX_VALUE);
        servletInputStream.close();
      }
    }
  }

  /**
   * Unit testing method only - do not use.
   * Used by parseRequest to check content type and extract the boundary markers.
   * @throws MultiPartRequestContentException if ContentType is not multipart/form-data
   */
  public void setupBoundary() throws MultiPartRequestContentException {
    if (request.getContentType() == null)
      throw new MultiPartRequestContentException("Unknown Content Type");
    StringTokenizer contentType = new StringTokenizer(
        request.getContentType(), " ;=");
    try {
      if (contentType.nextToken().equals(MULTIPART_FORM_DATA)) {
        if (contentType.nextToken().equals("boundary")) {
          boundary = contentType.nextToken();
          startBoundary = "--" + boundary;
          endBoundary = startBoundary + "--";
        } else {
          throw new MultiPartRequestContentException("Content type is missing boundary attribute");
        }
      } else {
        throw new MultiPartRequestContentException("Content type is not multipart/form-data");
      }
    } catch (NoSuchElementException ex) {
      throw new MultiPartRequestContentException("Content type or boundary attribute is missing");
    }
  }

  /**
   * Unit testing method only - do not use.
   * Used by parseRequest to process each part in a multipart request.
   * throws MultiPartRequestContentException if invalid request format
   * throws IOException if problem reading stream
   */
  public void processBlock()
      throws MultiPartRequestSizeException, MultiPartRequestContentException, IOException {
    String contentType = "text/plain"; // default
    tokenizeLine();
    expectToken("Content-Disposition");
    expectToken("form-data");
    expectToken("name");
    String key = fetchRequiredToken();
    // Check for file information
    if (checkForToken("filename")) {
      storeParameter(key + PARM_FILE_NAME_SUFFIX, fetchRequiredToken());
      // Extract the content type
      tokenizeLine();
      expectToken("Content-Type");
      contentType = fetchRequiredToken();
      storeParameter(key + PARM_CONTENT_TYPE_SUFFIX, contentType);
      // skip remaining mime headers
      String line;
      while ((line = readLine()) != null && line.length() > 0) {}
      if (line == null)
        throw new MultiPartRequestContentException("Unexpected end of input");
    } else {
      expectLine("");
    }
    fetchContent(key, contentType);
  }

  // ========================================================================
  //                  HttpServletRequest Interface Methods
  // ========================================================================

  /**
   * Returns an Enumeration of String  objects containing the names of the
   * parameters contained in this request. If the request has no parameters,
   * the method returns an empty Enumeration.
   * @return an Enumeration of String objects, each String containing the
   * name of a request parameter; or an empty Enumeration if the request
   * has no parameters
   */
  public Enumeration getParameterNames() {
    return Collections.enumeration(map.keySet());
  }

  /**
   * Returns the value of a request parameter as a String, or null if the
   * parameter does not exist. Request parameters are extra information sent
   * with the request. For HTTP servlets, parameters are contained in the
   * query string or posted form data.
   * <p>
   * You should only use this method when you are sure the parameter has only
   * one value. If the parameter might have more than one value, use
   * <code>getParameterValues(java.lang.String).</code>
   * <p>
   * If you use this method with a multivalued parameter, the value returned
   * is equal to the first value in the array returned by
   * <code>getParameterValues</code>.
   * <p>
   * If the parameter data was sent in the request body, such as occurs with
   * an HTTP POST request, then reading the body directly via
   * <code>getInputStream()</code> or <code>getReader()</code> can interfere
   * with the execution of this method.
   * @param name - a String specifying the name of the parameter
   * @return a String representing the single value of the parameter
   */
  public String getParameter(String name) {
    Object[] values = (Object[])map.get(name);
    if (values != null)
      return (String)values[0];
    else
      return null;
  }

  /**
   * Returns an array of <code>String</code> objects containing all of the
   * values the given request parameter has, or <code>null</code> if the
   * parameter does not exist.
   * <p>
   * If the parameter has a single value, the array has a length of 1.
   * @param name - a String containing the name of the parameter whose value
   * is requested
   * @return an array of <code>String</code> objects containing the
   * parameter's values
   */
  public String[] getParameterValues(String name) {
    Object[] values = (Object[])map.get(name);
    if (values != null) {
      String[] results = new String[values.length];
      for (int i = 0; i < results.length; i++) {
        results[i] = (String)values[i];
      }
      return results;
    } else {
      return null;
    }
  }

  /**
   * Returns a <code>java.util.Map</code> of the parameters of this request.
   * Request parameters are extra information sent with the request. For HTTP
   * servlets, parameters are contained in the query string or posted form data.
   * @return an immutable <code>java.util.Map</code> containing parameter
   * names as keys and parameter values as map values. The keys in the
   * parameter map are of type String. The values in the parameter map are
   * of type Object array.
   */
  public Map getParameterMap() {
    return Collections.unmodifiableMap(map);
  }

  /**
   * Returns the binary value of a request parameter as a <code>byte</code>
   * array, or null if the parameter is not a binary file or does not exist.
   * The parameter name must end with "_ByteArray" (eg "myFile_ByteArray").
   * <p>
   * You should only use this method when you are sure the parameter has only
   * one value. If the parameter might have more than one value, use
   * <code>getParameterByteArrays(java.lang.String).</code>
   * <p>
   * If you use this method with a multivalued parameter, the value returned
   * is equal to the first value in the array returned by
   * <code>getParameterByteArrays</code>.
   * <p>
   * @param name - a String specifying the name of the parameter
   * @return a <code>byte</code> array of the binary file data for the parameter.
   */
  public byte[] getParameterByteArray(String name) {
    Object[] values = (Object[])map.get(name);
    if (values != null)
      return (byte[])values[0];
    else
      return null;
  }

  /**
   * Returns an array of <code>byte</code> arrays containing all of the
   * binary file data the given request parameter has, or null if the
   * parameter does not exist.
   * <p>
   * If the parameter has a single value, the array has a length of 1.
   * @param name - a String specifying the name of the parameter
   * @return an array of <code>byte</code> arrays of the binary file data for
   * the parameter.
   */
  public Object[] getParameterByteArrays(String name) {
    return (Object[])map.get(name);
  }

  /**
   * Retrieves the body of the request as binary data using a
   * <code>ServletInputStream</code>. This method caches the request stream
   * after the first call.
   * @return a <code>ServletInputStream</code> object containing the body of
   * the request
   * @throws IllegalStateException if the <code>getReader()</code> method has
   * already been called on the underlying request object.
   * @throws IOException if an input or output exception occurred
   */
  public ServletInputStream getInputStream() throws IOException {
    if (servletInputStream == null)
      servletInputStream = request.getInputStream();
    return servletInputStream;
  }

  /**
   * Do not invoke this method. The parseRequest method will invoke the
   * <code>getInputStream()</code> method, which will invalidate the use of
   * this method.  This method will always throw an
   * <code>IllegalStateException</code>
   * @throws IOException is never thrown in this implementation
   * @throws IllegalStateException whenever this method is invoked.
   */
  public BufferedReader getReader() throws IOException {
    throw new IllegalStateException(
        "Cannot get Reader for multipart/form-data type request");
  }

  // ========================================================================
  //                     Property Accessor Methods
  // ========================================================================

  /**
   * Get the underlying <code>HttpServletRequest</code> for this object.
   * @return the underlying <code>HttpServletRequest</code> for this object.
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * Unit testing method only - do not use.
   * @return the boundary String which separates the parts of this request.
   */
  public String getBoundary() {
    return boundary;
  }

  /**
   * Unit testing method only - do not use.
   * @return true if end of data stream is reached. This is always true after
   * <code>parseRequest()</code> has been invoked.
   */
  public boolean isEndOfData() {
    return endOfData;
  }

  /**
   * Set the maximum file size which can be uploaded. Files which exceed this
   * size will cause an <code>ArraySubscriptException</code>. The default size
   * is 1 megabyte.
   * @param newMaxFileSize the maximum file size which can be uploaded.
   */
  public void setMaxFileSize(int newMaxFileSize) {
    maxFileSize = newMaxFileSize;
  }

  /**
   * Get the maximum file size which can be uploaded. Files which exceed this
   * size will cause an <code>ArraySubscriptException</code>. The default size
   * is 1 megabyte.
   * @return the maximum file size which can be uploaded.
   */
  public int getMaxFileSize() {
    return maxFileSize;
  }

  /**
   * Set the maximum length for a single line. Lines which exceed this size will
   * cause an <code>ArraySubscriptException</code>. The default size is 4,096 bytes.
   * @param newMaxLineLength the maximum length for a single line.
   */
  public void setMaxLineLength(int newMaxLineLength) {
    maxLineLength = newMaxLineLength;
  }

  /**
   * Get the maximum length for a single line. Lines which exceed this size will
   * cause an <code>ArraySubscriptException</code>. The default size is 4,096 bytes.
   * @return the maximum length for a single line.
   */
  public int getMaxLineLength() {
    return maxLineLength;
  }

  // ========================================================================
  //                             Internal Methods
  // ========================================================================

  private void fetchContent(String key, String contentType)
      throws MultiPartRequestSizeException, MultiPartRequestContentException, IOException {
    if (contentType.startsWith("text/"))
      fetchTextPlainContent(key);
    else if (contentType.startsWith("application/"))
      fetchApplicationOctetStreamContent(key);
    else if (contentType.startsWith("image/"))
      fetchApplicationOctetStreamContent(key);
    else if (contentType.startsWith("audio/"))
      fetchApplicationOctetStreamContent(key);
    else if (contentType.startsWith("video/"))
      fetchApplicationOctetStreamContent(key);
    else
      throw new MultiPartRequestContentException("Content type: " + contentType + " not supported");
  }

  private boolean equalsSubArray(char[] find, char[] target, int start, int len) {
    if (len != find.length)
      return false;
    for (int i = 0; i < find.length; i++) {
      if (find[i] != target[i+start])
        return false;
    }
    return true;
  }

  private void fetchTextPlainContent(String key)
      throws MultiPartRequestSizeException, MultiPartRequestContentException, IOException {
    char[] work = new char[maxFileSize];
    int offset = 0;
    int size = 0;
    char[] start = startBoundary.toCharArray();
    char[] end = endBoundary.toCharArray();
    try {
      while (true) {
        size = readChars(work, offset, key);
        // check if this is a boundary line (and maybe last boundary - endOfData)
        if (equalsSubArray(start, work, offset, size)
            || (endOfData = equalsSubArray(end, work, offset, size))) {
            offset -= 2;
            storeParameter(key, new String(work, 0, offset));
            return;
        }
        offset += size;
        work[offset] = '\r';
        offset++;
        work[offset] = '\n';
        offset++;
      }
    } catch (ArrayIndexOutOfBoundsException ex) {
      throw new MultiPartRequestSizeException(
        "Maximum file size exceeded for value named: " + key);
    }
  }

  private void fetchApplicationOctetStreamContent(String key)
      throws MultiPartRequestSizeException, MultiPartRequestContentException, IOException {
    byte[] work = new byte[maxFileSize];
    int offset = 0;
    int size = 0;
    boolean done = false;
    int boundarySize = startBoundary.length() + 1;
    try {
      while (!done) {
        size = readBytes(work, offset);
        if (new String(work, offset, size - 1).startsWith(startBoundary)) {
          // this was a boundary line (and maybe last boundary - endOfData)
          endOfData = new String(work, offset, size - 1).startsWith(endBoundary);
          done = true;
          offset -= 2; // remove last return/newline from results
        } else {
          offset += size;
        }
      }
    } catch (ArrayIndexOutOfBoundsException ex) {
      throw new MultiPartRequestSizeException(
        "Maximum file size exceeded for value named: " + key);
    }
    byte[] results = new byte[offset];
    System.arraycopy(work,0,results,0,offset);
    storeParameter(key, "");
    storeParameter(key + PARM_BYTE_ARRAY_SUFFIX, results);
  }

  private byte readByte() throws MultiPartRequestContentException, IOException {
    // check if buffer empty
    if (bufferPosition >= bufferSize) {
      bufferSize =
          getInputStream().read(buffer, 0, maxBufferSize);
      if (bufferSize == -1)
        throw new MultiPartRequestContentException("Unexpected end of data stream");
      bufferPosition = 0;
    }
    return buffer[bufferPosition++];
  }

  private String readLine() throws MultiPartRequestContentException, IOException {
    byte[] results = new byte[maxLineLength];
    int i = 0;
    byte b;
    while ((b = readByte()) != '\n') {
      if (b != '\r') { // ignore carriage return
        results[i] = b;
        i++;
      }
    }
    return new String(results, 0, i);
  }

  public int readChars(char[] results, int offset, String key)
      throws MultiPartRequestSizeException, MultiPartRequestContentException,
      IOException {
    byte[] bytes = new byte[maxLineLength];
    int i = 0;
    byte b;
    try {
      while ((b = readByte()) != '\n') {
        if (b != '\r') { // ignore carriage return
          bytes[i] = b;
          i++;
        }
      }
    } catch (ArrayIndexOutOfBoundsException ex) {
      throw new MultiPartRequestSizeException(
        "Maximum line length exceeded for value named: " + key);
    }
    // xlate bytes -> chars and store in results
    String s = new String(bytes, 0, i);
    System.arraycopy(s.toCharArray(), 0, results, offset, i);
    return i;
  }

  private int readBytes(byte[] results, int offset)
      throws MultiPartRequestContentException, IOException {
    int originalOffset = offset;
    byte b;
    while ((b = readByte()) != '\n') {
      results[offset] = b;
      offset++;
    }
    if (b == '\n') {
      results[offset] = b;
      offset++;
    }
    return offset - originalOffset;
  }

  private void expectToken(String expected)
      throws MultiPartRequestContentException {
    try {
      String value = fetchRequiredToken();
      if (!value.equals(expected))
        throw new MultiPartRequestContentException("Expected: >>" + expected
            + "<< Received >>" + value + "<<");
    } catch (NoSuchElementException ex) {
      throw new MultiPartRequestContentException("Out of elements. Expected: " + expected);
    }
  }

  /** @todo rewrite not to cause exception */
  private boolean checkForToken(String expected) {
    try {
      expectToken(expected);
      return true;
    } catch (MultiPartRequestContentException ex) {
      return false;
    }
  }

  /** @todo rewrite not to cause exception - return null */
  private String fetchToken()
      throws MultiPartRequestContentException {
    try {
      String value = tokens.nextToken(currentDelimiters);
      // is this value a current delimiter ?
      if (currentDelimiters.indexOf(value) != -1) {
        // Is it a quote ?
        if (value.equals(QUOTE)) {
          // Switch currentDelimiter
          if (currentDelimiters.equals(QUOTE)) {
            currentDelimiters = TOKEN_DELIM_DEFAULT;
          } else {
            currentDelimiters = QUOTE;
            value = tokens.nextToken(QUOTE);
            if (value.equals(QUOTE)) {
              currentDelimiters = TOKEN_DELIM_DEFAULT;
              return "";
            } else {
              return value;
            }
          }
        }
        return fetchToken();
      } else {
        return value;
      }
    } catch (NoSuchElementException ex) {
      throw new MultiPartRequestContentException("Out of elements. Expected a value");
    }
  }

  /** @todo rewrite with fetchToken() */
  private String fetchRequiredToken() throws MultiPartRequestContentException {
    String value = fetchToken();
    if (value == null)
      throw new MultiPartRequestContentException("Out of elements. Expected a value");
    return value;
  }

  private void tokenizeLine()
      throws MultiPartRequestContentException, IOException {
    String line = readLine();
    if (line == null)
      throw new MultiPartRequestContentException("Unexpected end of input");
    tokens = new StringTokenizer(line, TOKEN_DELIM_DEFAULT, true);
    currentDelimiters = TOKEN_DELIM_DEFAULT;
  }

  private void expectLine(String expected)
      throws MultiPartRequestContentException, IOException {
    String line = readLine();
    if (line == null)
      throw new MultiPartRequestContentException("Unexpected end of input");
    if (!line.equals(expected))
      throw new MultiPartRequestContentException("\nExpected: >>" + expected + "<<(length="
          + expected.length() + ")\nReceived: >>" + line + "<<(length="
          + line.length() + ")");
  }

  private void storeParameter(String key, Object value) {
    if (map.containsKey(key)) {
      Object[] values = (Object[])map.get(key);
      Object[] newValues = new Object[values.length + 1];
      System.arraycopy(values, 0, newValues, 0, values.length);
      newValues[values.length] = value;
      map.put(key, newValues);
    } else {
      map.put(key, new Object[] {value});
    }
  }

  // ========================================================================
  //                Direct Proxy of HttpServletRequest Methods
  // ========================================================================

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getAuthType() {
    return request.getAuthType();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Cookie[] getCookies() {
    return request.getCookies();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public long getDateHeader(String name) {
    return request.getDateHeader(name);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getHeader(String name) {
    return request.getHeader(name);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Enumeration getHeaders(String name) {
    return request.getHeaders(name);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Enumeration getHeaderNames() {
    return request.getHeaderNames();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public int getIntHeader(String name) {
    return request.getIntHeader(name);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getMethod() {
    return request.getMethod();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getPathInfo() {
    return request.getPathInfo();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getPathTranslated() {
    return request.getPathTranslated();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getContextPath() {
    return request.getContextPath();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getQueryString() {
    return request.getQueryString();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getRemoteUser() {
    return request.getRemoteUser();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public boolean isUserInRole(String role) {
    return request.isUserInRole(role);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Principal getUserPrincipal() {
    return request.getUserPrincipal();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getRequestedSessionId() {
    return request.getRequestedSessionId();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getRequestURI() {
    return request.getRequestURI();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getServletPath() {
    return request.getServletPath();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public HttpSession getSession(boolean create) {
    return request.getSession(create);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public HttpSession getSession() {
    return request.getSession();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public boolean isRequestedSessionIdValid() {
    return request.isRequestedSessionIdValid();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public boolean isRequestedSessionIdFromCookie() {
    return request.isRequestedSessionIdFromCookie();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public boolean isRequestedSessionIdFromURL() {
    return request.isRequestedSessionIdFromURL();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public boolean isRequestedSessionIdFromUrl() {
    return request.isRequestedSessionIdFromUrl();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Object getAttribute(String name) {
    return request.getAttribute(name);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Enumeration getAttributeNames() {
    return request.getAttributeNames();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getCharacterEncoding() {
    return request.getCharacterEncoding();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public int getContentLength() {
    return request.getContentLength();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getContentType() {
    return request.getContentType();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getProtocol() {
    return request.getProtocol();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getScheme() {
    return request.getScheme();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getServerName() {
    return request.getServerName();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public int getServerPort() {
    return request.getServerPort();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getRemoteAddr() {
    return request.getRemoteAddr();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getRemoteHost() {
    return request.getRemoteHost();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public void setAttribute(String name, Object o) {
    request.setAttribute(name, o);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public void removeAttribute(String name) {
    request.removeAttribute(name);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Locale getLocale() {
    return request.getLocale();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public Enumeration getLocales() {
    return request.getLocales();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public boolean isSecure() {
    return request.isSecure();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public RequestDispatcher getRequestDispatcher(String path) {
    return request.getRequestDispatcher(path);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public String getRealPath(String path) {
    return request.getRealPath(path);
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public StringBuffer getRequestURL() {
    return request.getRequestURL();
  }

  /**
   * Calls same method on the underlying HttpServletRequest.
   * Refer to Servlet API for information about this method.
   */
  public void setCharacterEncoding(String encoding) throws java.io.UnsupportedEncodingException {
    request.setCharacterEncoding(encoding);
  }

  /**
   * For testing purposes only.
   */
  static public void main(String[] args) {
    try {
      HttpMultiPartServletRequest rq = new HttpMultiPartServletRequest(null);
      String testString = "xxxx zzz; yyy-yy=\"aaa/:bbb\"; delim=---YYY  ;;\"\"";
      rq.tokens = new StringTokenizer(testString,rq.TOKEN_DELIM_DEFAULT, true);
      while (rq.tokens.hasMoreTokens())
        System.out.println(">>" + rq.fetchToken() + "<<");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
