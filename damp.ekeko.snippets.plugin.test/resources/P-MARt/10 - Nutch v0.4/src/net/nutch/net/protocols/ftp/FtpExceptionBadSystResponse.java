package net.nutch.net.protocols.ftp;

import net.nutch.net.protocols.ftp.FtpException;

/**
 * Exception indicating bad reply of SYST command.
 *
 * @author John Xing
 */
public class FtpExceptionBadSystResponse extends FtpException {
  FtpExceptionBadSystResponse(String msg) {
    super(msg);
  }
}
