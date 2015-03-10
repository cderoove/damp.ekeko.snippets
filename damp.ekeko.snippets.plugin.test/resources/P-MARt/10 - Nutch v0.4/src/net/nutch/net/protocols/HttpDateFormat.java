package net.nutch.net.protocols;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * class to handle HTTP dates.
 *
 * Modified from FastHttpDateFormat.java in jakarta-tomcat.
 *
 * @author John Xing
 */
public class HttpDateFormat {

  protected static SimpleDateFormat format = 
    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

  /**
   * HTTP date uses TimeZone GMT
   */
  static {
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  //HttpDate (long t) {
  //}

  //HttpDate (String s) {
  //}

//  /**
//   * Get the current date in HTTP format.
//   */
//  public static String getCurrentDate() {
//
//    long now = System.currentTimeMillis();
//    if ((now - currentDateGenerated) > 1000) {
//        synchronized (format) {
//            if ((now - currentDateGenerated) > 1000) {
//                currentDateGenerated = now;
//                currentDate = format.format(new Date(now));
//            }
//        }
//    }
//    return currentDate;
//
//  }

  /**
   * Get the HTTP format of the specified date.
   */
  public static String toString(Date date) {
    String string;
    synchronized (format) {
      string = format.format(date);
    }
    return string;
  }

  public static String toString(Calendar cal) {
    String string;
    synchronized (format) {
      string = format.format(cal.getTime());
    }
    return string;
  }

  public static String toString(long time) {
    String string;
    synchronized (format) {
      string = format.format(new Date(time));
    }
    return string;
  }

  public static Date toDate(String dateString) throws ParseException {
    Date date;
    synchronized (format) {
      date = format.parse(dateString);
    }
    return date;
  }

  public static long toLong(String dateString) throws ParseException {
    long time;
    synchronized (format) {
      time = format.parse(dateString).getTime();
    }
    return time;
  }

  public static void main(String[] args) throws Exception {
    HttpDateFormat format = new HttpDateFormat();

    Date now = new Date(System.currentTimeMillis());

    String string = format.toString(now);

    long time = format.toLong(string);

    System.out.println(string);
    System.out.println(format.toString(time));
  }

}
