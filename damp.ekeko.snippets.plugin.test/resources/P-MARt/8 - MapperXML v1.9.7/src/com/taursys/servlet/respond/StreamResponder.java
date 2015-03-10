/**
 * StreamResponder - responds by sending its input stream to the response output stream.
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
package com.taursys.servlet.respond;

import com.taursys.servlet.ServletForm;
import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;

/**
 * StreamResponder responds by sending its input stream to the response output stream.
 * The inputStream must be open before the respond method is invoked.  The
 * respond method will close the inputStream upon completion.  You must reset
 * or re-open the inputStream before this response object can respond again.
 * @author Marty Phelan
 * @version 1.0
 */
public class StreamResponder extends ContentResponder {
  private java.io.InputStream inputStream;

  /**
   * Constructs a new StreamResponder
   */
  public StreamResponder() {
  }

  /**
   * Responds by sending given input stream to response output stream.
   * The inputStream must be open and ready.  It will close inputStream
   * upon completion.
   * @throws Exception if problem responding
   */
  public void respond() throws Exception {
    getServletForm().getResponse().setContentType(getContentType());
    OutputStream out = getServletForm().getResponse().getOutputStream();
    byte[] bytes = new byte[1024];
    int count;
    count = inputStream.read(bytes);
    while(count > 0) {
      out.write(bytes, 0, count);
      out.flush();
      count = inputStream.read(bytes);
    }
    inputStream.close();
  }

  /**
   * Set the inputStream which will be used for the response.
   * The inputStream must be open and ready.  It will be closed by the
   * <code>respond</code> method upon completion.
   * @param newInputStream which will be used for the response.
   */
  public void setInputStream(java.io.InputStream newInputStream) {
    inputStream = newInputStream;
  }

  /**
   * Get the inputStream which will be used for the response.
   * The inputStream must be open and ready.  It will be closed by the
   * <code>respond</code> method upon completion.
   * @return inputStream which will be used for the response.
   */
  public java.io.InputStream getInputStream() {
    return inputStream;
  }
}
