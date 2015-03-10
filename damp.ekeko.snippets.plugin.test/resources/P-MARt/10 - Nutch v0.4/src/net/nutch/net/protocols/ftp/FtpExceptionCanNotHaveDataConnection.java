package net.nutch.net.protocols.ftp;

import net.nutch.net.protocols.ftp.FtpException;

/**
 * Exception indicating failure of opening data connection.
 *
 * @author John Xing
 */
public class FtpExceptionCanNotHaveDataConnection extends FtpException {
  FtpExceptionCanNotHaveDataConnection(String msg) {
    super(msg);
  }
}
