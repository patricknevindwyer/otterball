package test.com.programmish.otterball.parsing;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.programmish.otterball.parsing.PythonUnicodeParser;
import com.programmish.otterball.parsing.TextRange;

public class PythonUnicodeParser_Test {


	@Test
	public void testHasFingerprint() {
		
		PythonUnicodeParser jp = new PythonUnicodeParser();
		
		// simple string that SHOULD have fingerprint
		assertTrue(jp.hasFingerprint("{u'foo': 24}"));
		assertTrue(jp.hasFingerprint("{u'bool': true, u'nother': false}"));
		
		assertFalse(jp.hasFingerprint("{'foo': 24}"));
		
		assertFalse(jp.hasFingerprint("{u'foo':]"));
		
		assertFalse(jp.hasFingerprint("foo"));
	}

	@Test
	public void testFindSections() {
		
		PythonUnicodeParser jp = new PythonUnicodeParser();
		
		// a simple extraction
		List<TextRange> ranges = jp.findSections("foo\nbar\n{u'baz': [1, 2, 3]}");
		
		assertTrue(ranges.size() == 1);
		assertTrue(ranges.get(0).start == 8);
		assertTrue(ranges.get(0).end == 27);
		
		// two extractions
		ranges = jp.findSections("{u'baz': \n32}\nfoobalily\n\t{u'bar': [1,3,4]}");
		assertTrue(ranges.size() == 2);
		
	}


}
