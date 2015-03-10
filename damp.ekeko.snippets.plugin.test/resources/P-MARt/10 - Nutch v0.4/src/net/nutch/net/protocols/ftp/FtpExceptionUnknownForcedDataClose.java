package net.nutch.net.protocols.ftp;

import net.nutch.net.protocols.ftp.FtpException;

/**
 * Exception indicating unrecognizable reply from server after
 * forced closure of data channel by client (our) side.
 *
 * @author John Xing
 */
public class FtpExceptionUnknownForcedDataClose extends FtpException {
  FtpExceptionUnknownForcedDataClose(String msg) {
    super(msg);
  }
}
