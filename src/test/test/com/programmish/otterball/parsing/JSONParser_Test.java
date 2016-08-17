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
	public void testLookaheadBoundary() {
		// lookaheads for literal values can go past the end of the string
		// boundary, leading to a parse exception
		
		
		String lookaheads[] = {"[true, f", "{\"boo\": nu", "[false, tr", "[false, t", "[false, tru"};
		
		for (String lookahead: lookaheads) {
			JSONParser jp = new JSONParser();
			List<ParsedElement> elements = jp.parse(lookahead);
			assertTrue(String.format("lookahead <%s> doesn't crash the parser", lookahead),elements.size() == 3);
		}
		
		JSONParser jp = new JSONParser();
		List<ParsedElement> elements = jp.parse("{\"foo\": true");
		assertTrue("Barewords are parsed at the right lookahead length", elements.size() == 4);
		
		elements = jp.parse("{\"foo\": false");
		assertTrue("Barewords are parsed at the right lookahead length", elements.size() == 4);
		
		elements = jp.parse("[1, 2, 3, nul");
		assertTrue("Barewords are parsed at the right lookahead length", elements.size() == 7);
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
		assertTrue(elements.size() == 19);
		
		// we need to make sure this is validating...
		elements = jp.parse("{\"a\": [1 : , ,]}");
		assertTrue("Lists validate delimiters - and return partial parses", elements.size() == 5);
		
		// let's try a mixed list
		elements = jp.parse("[1, 2.0, -3.14, \"hello\", \"[not, a, real, list]\", true, false, {}]");
		assertTrue(elements.size() > 0);
		
		// make sure lists have delimiters
		elements = jp.parse("[1 2 3]");
		assertTrue("Lists need delimiters - and return partial parses", elements.size() == 2);
		
		// can't start a list with a delimiter
		elements = jp.parse("[, 2, 3]");
		assertTrue("Lists can't start with delimiters - return partial parse", elements.size() == 1);
		
		// can't end a list with a delimiter
		elements = jp.parse("[1, 2, 3, ]");
		assertTrue("Lists can't end with delimiters - partial parses", elements.size() == 7);
		
		// make sure objects have separators
		elements = jp.parse("{\"a\" 3}");
		assertTrue("Objects need separators - partial parses", elements.size() == 2);
		
		// make sure objects have delimiters - 
		//		numeric should be the last value 
		elements = jp.parse("{\"a\": 1 \"b\": 2}");
		assertTrue("Objects need delimiters - partial parses", elements.size() == 4);
		
		// 		true should be the last value
		elements = jp.parse("{\"a\": true \"b\": 2}");
		assertTrue("Objects need delimiters - partial parses", elements.size() == 4);
		
		// 		string "apple" should be the last value
		elements = jp.parse("{\"a\": \"apple\" \"b\": 2}");
		assertTrue("Objects need delimiters - partial parses", elements.size() == 4);

		
		// objects can't end with delimiters or separators
		elements = jp.parse("{\"a\":}");
		assertTrue("Objects can't end with separators - partial parses", elements.size() == 3);
		
		elements = jp.parse("{\"a\": 1,}");
		assertTrue("Objects can't end with delimiters - partial parsing", elements.size() == 5);
		
		// objects can't start with delimiters or separators
		elements = jp.parse("{: 2}");
		assertTrue("Objects can't start with separators - partial parse", elements.size() == 1);
		
		elements = jp.parse("{, \"a\": 3}");
		assertTrue("Objects can't start with delimiters - partial parse", elements.size() == 1);
				
	}
	
	protected void dump(List<ParsedElement> elements) {
		for (ParsedElement e : elements) {
			System.out.println(e.type + "\t\t(" + e.start + "," + e.end + ")");
		}		
	}
}
