
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.RenameClassRefactoring;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Main program for repackaging. This object simply stores the main program
 *  and interprets the command line arguments for repackaging one or more
 *  files.
 *
 *@author    Chris Seguin
 */
public class RenameType {
	//  Instance Variables
	private RenameClassRefactoring renameClass;


	/**
	 *  Actual work of the main program occurs here
	 *
	 *@param  args  the command line arguments
	 */
	public void run(String[] args) {
		renameClass = RefactoringFactory.get().renameClass();
		if (init(args)) {
			try {
				renameClass.run();
			}
			catch (RefactoringException re) {
				System.out.println(re.getMessage());
			}
		}
	}


	/**
	 *  Initialize the variables with command line arguments
	 *
	 *@param  args  the command line arguments
	 *@return       true if we should continue processing
	 */
	public boolean init(String[] args) {
		int nCurrentArg = 0;

		while (nCurrentArg < args.length) {
			if (args[nCurrentArg].equals("-dir")) {
				renameClass.setDirectory(args[nCurrentArg + 1]);
				nCurrentArg += 2;
			}
			else if (args[nCurrentArg].equals("-help")) {
				printHelpMessage();
				nCurrentArg++;
				return false;
			}
			else if (args[nCurrentArg].equals("-from")) {
				renameClass.setOldClassName(args[nCurrentArg + 1]);
				nCurrentArg += 2;
			}
			else if (args[nCurrentArg].equals("-to")) {
				renameClass.setNewClassName(args[nCurrentArg + 1]);
				nCurrentArg += 2;
			}
			else {
				System.out.println("Unknown argument:  " + args[nCurrentArg]);
				nCurrentArg++;
			}
		}

		return true;
	}


	/**
	 *  Print the help message
	 */
	protected void printHelpMessage() {
		System.out.println("Syntax:  java RenameType \\ ");
		System.out.println("        [-dir <dir>] [-help] ");
		System.out.println("        -from <className> -to <className>");
		System.out.println("");
		System.out.println("  where:");
		System.out.println("    <dir>        is the name of the directory containing the files");
		System.out.println("    <className>  is the name of the class");
	}


	/**
	 *  Main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args) {
		try {
			//  Make sure everything is installed properly
			(new RefactoryInstaller(true)).run();

			(new RenameType()).run(args);
		}
		catch (Throwable thrown) {
			thrown.printStackTrace();
		}

		System.exit(0);
	}
}
