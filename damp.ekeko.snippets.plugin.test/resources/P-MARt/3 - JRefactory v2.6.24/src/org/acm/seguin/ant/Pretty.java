package org.acm.seguin.ant;

import java.io.File;
import java.util.Vector;

import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.pretty.PrettyPrintFile;
import org.acm.seguin.refactor.undo.FileSet;
import org.acm.seguin.util.FileSettings;

/**
 * @author     Ara Abrahamian (ara_e@email.com)
 * @created    May 18, 2001
 * @version    $Revision: 1.1 $
 */
public final class Pretty extends Task
{
   private Vector    filesets = new Vector();
   private boolean   cvs      = false;
   private CVSUtil   cvsUtil  = new CVSUtil();

    /**
     * Adds a set of files (nested fileset attribute).
     */
    public void addFileset(FileSet set)
    {
        filesets.addElement(set);
    }

   public void setSettingsdir(File new_settings_dir)
   {
      FileSettings.setSettingsRoot(new_settings_dir);
   }

   public void setCvs( boolean cvs )
   {
      this.cvs = cvs;
   }

    public void execute() throws BuildException
    {
        // make sure we don't have an illegal set of options
        validateAttributes();

      try
      {
      PrettyPrintFile ppf  = new PrettyPrintFile();

         ppf.setAsk(false);

         for( int i=0; i<filesets.size(); i++ )
         {
         FileSet fs        = (FileSet) filesets.elementAt(i);
         DirectoryScanner ds  = fs.getDirectoryScanner(project);
         File from_dir     = fs.getDir(project);
         String[] src_files   = ds.getIncludedFiles();

            for( int j=0; j<src_files.length; j++ )
            {
            File source_file  = new File( from_dir + File.separator + src_files[j]);

               if( cvs==false || cvs==true && (cvsUtil.isFileModified(source_file)) )
               {
                  System.out.println("formatting:" + source_file );
                  ppf.setParserFactory(new FileParserFactory(source_file));

                  // reformat
                  ppf.apply(source_file);
               }
            }
         }
      }
      catch (Exception ex)
      {
         ex.printStackTrace();

         throw new BuildException("Cannot javastyle files", location);
      }
   }

    /**
     * Ensure we have a consistent and legal set of attributes, and set
     * any internal flags necessary based on different combinations
     * of attributes.
     */
    protected void validateAttributes() throws BuildException
    {
        if( filesets.size() == 0 )
        {
            throw new BuildException("Specify at least one fileset.");
        }

        //possibly some other attributes: overwrite/destDir/etc
    }
}
