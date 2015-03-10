package net.nutch.net.protocols.ftp;

import net.nutch.net.protocols.ProtocolException;


/***
 * Superclass for important exceptions thrown during FTP talk,
 * that must be handled with care.
 *
 * @author John Xing
 */
public class FtpException extends ProtocolException {

  public FtpException() {
    super();
  }

  public FtpException(String message) {
    super(message);
  }

  public FtpException(String message, Throwable cause) {
    super(message, cause);
  }

  public FtpException(Throwable cause) {
    super(cause);
  }

}
