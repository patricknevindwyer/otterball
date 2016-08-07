package test.com.programmish.otterball.parsing;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.programmish.otterball.parsing.JSONParser;
import com.programmish.otterball.parsing.ParsedElement;
import com.programmish.otterball.parsing.TextRange;

public class JSONParser_Test {

	@Test
	public void testHasFingerprint() {
		
		JSONParser jp = new JSONParser();
		
		// simple string that SHOULD have fingerprint
		assertTrue(jp.hasFingerprint("{\"foo\": 24}"));
		assertTrue(jp.hasFingerprint("{\"bool\": true, \"nother\": false}"));
		
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
	
	@Test
	public void testParse() {
		
		JSONParser jp = new JSONParser();
		
		// start simple
		List<ParsedElement> elements = jp.parse("{\"foo\": 3}");
		assertTrue(elements.size() == 5);
		
		// harder number
		elements = jp.parse("{\"a\": -3.14}");
		assertTrue(elements.size() == 5);
		
		// step it up a bit
		elements = jp.parse("{\"floob\": { \"a\": 1, \"b\": [1, 2.0, -3.4 ]}}");
		this.dump(elements);
		assertTrue(elements.size() > 0);
		
		// we need to make sure this is validating...
		elements = jp.parse("{\"a\": [1 : , ,]}");
		assertTrue(elements.size() == 0);
		
		
	}
	
	protected void dump(List<ParsedElement> elements) {
		for (ParsedElement e : elements) {
			System.out.println(e.type + "\t\t(" + e.start + "," + e.end + ")");
		}		
	}
}
