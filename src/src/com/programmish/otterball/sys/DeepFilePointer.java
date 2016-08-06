package com.programmish.otterball.sys;


import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Point to specific points or offsets in a file, or just the file itself.
 * 
 * @author patricknevindwyer
 *
 */
public class DeepFilePointer {

	private String filename;
	private int offset;
	private String filebase;
	
	public String getFilebase() {
		return filebase;
	}

	public void setFilebase(String filebase) {
		this.filebase = filebase;
	}

	public DeepFilePointer(String filename) {
		this(filename, 0);
	}
	
	public DeepFilePointer(String filename, int offset) {
		this.filename = filename;
		this.offset = offset;
	}
	
	public String getReadableString() {
		
		// If there is no filebase, find the bare file name of this file,
		// otherwise use the filebase to create a relative path name
		if (this.filebase == null) {
			Path p = Paths.get(this.filename);
			return p.getFileName().toString();
		}
		else {
			return this.filename.replace(this.filebase, "");
		}
	}
	
	public boolean matches(String search) {
		return getReadableString().toLowerCase().contains(search);
	}
	
	public String getAbsoluteFilename() {
		return this.filename;
	}
}
