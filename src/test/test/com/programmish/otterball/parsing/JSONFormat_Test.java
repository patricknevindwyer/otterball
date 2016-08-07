package test.com.programmish.otterball.parsing;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.programmish.otterball.parsing.JSONFormat;
import com.programmish.otterball.parsing.JSONParser;
import com.programmish.otterball.parsing.ParsedElement;

public class JSONFormat_Test {

	@Test
	public void testCompact() {
		
		// Compact a verbose object
		JSONParser jp = new JSONParser();
		String raw = "{\n\t\"a\" : [\n\t\t1,\n\t\t2\n\t]\n}";
		List<ParsedElement> elements = jp.parse(raw);
		String compacted = JSONFormat.compact(raw, elements);
		
		assertTrue("Verbose JSON compacts", compacted.length() == 11);
		
		elements = jp.parse(compacted);
		assertTrue("Compacted JSON should be valid", elements.size() > 0);
		
		// already compact JSON should be identical
		raw = "[1,2,3,4]";
		elements = jp.parse(raw);
		compacted = JSONFormat.compact(raw, elements);
		assertTrue("Already compact JSON shouldn't be altered by compacting", raw.equals(compacted));
	}
	
	@Test
	public void testReflow() {
		
		// simple reflow
		JSONParser jp = new JSONParser();
		String raw = "{\"a\":[1,2]}";
		List<ParsedElement> elements = jp.parse(raw);
		
		assertTrue("Compact JSON is parsable", elements.size() > 0);
		
		String reflowed = JSONFormat.reflow(raw, "\t", elements);
		
		assertTrue("Reflowed is longer than compact", raw.length() < reflowed.length());
		
		// more complex example
		raw = "{\"a\": 1, \"b\": {\"c\": [1, 2, 3, {}]}}";
		elements = jp.parse(raw);
		
		assertTrue("JSON is parsable", elements.size() > 0);
		
		reflowed = JSONFormat.reflow(raw, "    ", elements);
		assertTrue("Reflowed is longer than compact", raw.length() < reflowed.length());
		
	}
	
	protected void dump(List<ParsedElement> elements) {
		for (ParsedElement e : elements) {
			System.out.println(e.type + "\t\t(" + e.start + "," + e.end + ")");
		}		
	}

}
