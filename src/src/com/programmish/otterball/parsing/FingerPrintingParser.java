package com.programmish.otterball.parsing;

import java.util.List;

/**
 * The finger printing parsers look at a stream of data and identify the bounds of content
 * that they match. These are meant to be quick and dirty - just shy of brace counting. Different
 * interfaces define syntactic and semantic parsing.
 * 
 * @author patricknevindwyer
 *
 */
public interface FingerPrintingParser {
	
	/**
	 * Given a blob of text, test whether it roughly matches our 
	 * parsing type.
	 * 
	 * @param blob
	 * @return
	 */
	public boolean hasFingerprint(String blob);
	
	/**
	 * Extract ranges of text from a larger string that match our 
	 * parsing type.
	 * 
	 * @param blob
	 * @return
	 */
	public List<TextRange> findSections(String blob);
	
	/**
	 * Basic naming for the parser, so we have something human readable. 
	 * @return
	 */
	public String parserName();
}
