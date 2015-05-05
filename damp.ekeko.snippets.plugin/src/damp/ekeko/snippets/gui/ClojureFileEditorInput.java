package damp.ekeko.snippets.gui;

import java.io.File;

public class ClojureFileEditorInput {

	protected String pathToFile = null;

	public ClojureFileEditorInput() {
		super();
	}

	public String getPathToPersistentFile() {
		return pathToFile;
	}

	public void setPathToPersistentFile(String path) {
		pathToFile = path;
	}

	public boolean isAssociatedWithPersistentFile() {
		return pathToFile != null;
	}

	public boolean associatedPersistentFileExists() {
		if(!isAssociatedWithPersistentFile())
			return false;
		File file = new File(getPathToPersistentFile());
		return file.exists();
	}

}