package it.unimib.disco.essere.main.graphmanager;

import java.io.File;

public class IsNotGitFolderException extends RuntimeException {

	private static final long serialVersionUID = -8907130387331308721L;

	public IsNotGitFolderException(String message){
		super(message);
	}
	public IsNotGitFolderException(File folder){
		super("Is not a git folder: "+folder);
	}
	public IsNotGitFolderException(String string, Throwable t) {
		super(string, t);
	}
}
