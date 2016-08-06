package test.com.programmish.otterball.parsing;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.programmish.otterball.parsing.JSONParser;
import com.programmish.otterball.parsing.TextRange;

public class JSONParser_Test {

	@Test
	public void testHasFingerprint() {
		
		JSONParser jp = new JSONParser();
		
		// simple string that SHOULD have fingerprint
		assertTrue(jp.hasFingerprint("{\"foo\": 24}"));
		
		assertFalse(jp.hasFingerprint("{u'foo': 24}"));
		
		assertFalse(jp.hasFingerprint("{\"foo\":]"));
		
		assertFalse(jp.hasFingerprint("foo"));
	}

	@Test
	public void testFindSections() {
		
		JSONParser jp = new JSONParser();
		
		// a simple extraction
		List<TextRange> ranges = jp.findSections("foo\nbar\n{\"baz\": [1, 2, 3]}");
		
		assertTrue(ranges.size() == 1);
		assertTrue(ranges.get(0).start == 8);
		assertTrue(ranges.get(0).end == 26);
		
		// two extractions
		ranges = jp.findSections("{\"baz\": \n32}\nfoobalily\n\t{\"bar\": [1,3,4]}");
		assertTrue(ranges.size() == 2);
		
	}

}
