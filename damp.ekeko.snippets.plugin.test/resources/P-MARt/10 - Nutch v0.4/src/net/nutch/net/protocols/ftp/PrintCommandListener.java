package net.nutch.net.protocols.ftp;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

import java.util.logging.Logger;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;


/***
 * This is a support class for logging all ftp command/reply traffic.
 *
 * @author John Xing
 ***/

public class PrintCommandListener implements ProtocolCommandListener
{
    private Logger __logger;

    public PrintCommandListener(Logger logger)
    {
        __logger = logger;
    }

    public void protocolCommandSent(ProtocolCommandEvent event) {
      try {
        __logIt(event);
      } catch (IOException e) {
        __logger.info("PrintCommandListener.protocolCommandSent(): "+e);
      }
    }

    public void protocolReplyReceived(ProtocolCommandEvent event) {
      try {
        __logIt(event);
      } catch (IOException e) {
        __logger.info("PrintCommandListener.protocolReplyReceived(): "+e);
      }
    }

    private void __logIt(ProtocolCommandEvent event) throws IOException {
      BufferedReader br =
        new BufferedReader(new StringReader(event.getMessage()));
      String line;
      while ((line = br.readLine()) != null) {
        __logger.info("ftp> "+line);
      }
    }
}
