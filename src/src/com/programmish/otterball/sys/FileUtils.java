package com.programmish.otterball.sys;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {
	
	/**
	 * Read contents of a file in a fairly memory efficient and Charset aware manner. This
	 * is taken from this answer on StackOverflow http://stackoverflow.com/a/326440
	 * @param filename Filename to read
	 * @param encoding Encoding of the file
	 * @return File contents
	 */
	public static String getFileContents(String filename, Charset encoding) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(filename));
			return encoding.decode(ByteBuffer.wrap(encoded)).toString();
		}
		catch (IOException ioe) {
			return "";
		}
	}

}
