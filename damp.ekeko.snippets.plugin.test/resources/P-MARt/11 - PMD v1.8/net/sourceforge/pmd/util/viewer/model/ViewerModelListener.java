package net.sourceforge.pmd.util.viewer.model;

/**
 * identiefie a listener of the ViewerModel
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: ViewerModelListener.java,v 1.1 2004/07/14 16:37:13 ngjanice Exp $
 */
public interface ViewerModelListener
{
  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void viewerModelChanged( ViewerModelEvent e );
}


/*
 * $Log: ViewerModelListener.java,v $
 * Revision 1.1  2004/07/14 16:37:13  ngjanice
 * 14 juillet 2004 - 12h32
 *
 * Revision 1.1  2003/09/23 20:32:42  tomcopeland
 * Added Boris Gruschko's new AST/XPath viewer
 *
 * Revision 1.1  2003/09/24 01:33:03  bgr
 * moved to a new package
 *
 * Revision 1.1  2003/09/22 05:21:54  bgr
 * initial commit
 *
 */
