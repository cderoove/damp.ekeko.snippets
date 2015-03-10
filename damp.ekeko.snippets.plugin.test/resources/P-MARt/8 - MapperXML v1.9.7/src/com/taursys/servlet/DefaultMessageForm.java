package com.taursys.servlet;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;

/**
 * Title:        Mapper XML
 * Description:  Sub-project for Mapper XML support without using XMLC
 * Copyright:    Copyright (c) 2001
 * Company:      Taurus Systems
 * @author Marty Phelan
 * @version 1.0
 */

public class DefaultMessageForm extends ServletForm {

  public DefaultMessageForm() {
  }
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws java.lang.Exception {
    resp.setContentType("text/html");
    PrintWriter pw = resp.getWriter();
    pw.println("<html><head></head><body><h1>Mapper Message</h1><p>"
        + message + "</p></body></html>");
    pw.flush();
  }
  public void setMessage(String newMessage) {
    message = newMessage;
  }
  public String getMessage() {
    return message;
  }
  private String message = "Your message goes here";
  public boolean recycle() {
    return true;
  }
}
