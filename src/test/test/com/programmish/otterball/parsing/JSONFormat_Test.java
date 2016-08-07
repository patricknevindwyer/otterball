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
		
		JSONParser jp = new JSONParser();
		String raw = "{\n\t\"a\" : [\n\t\t1,\n\t\t2\n\t]\n}";
		List<ParsedElement> elements = jp.parse(raw);
		dump(elements);
		String compacted = JSONFormat.compact(raw, elements);
		System.out.println("<" + compacted + ">");
		assertTrue(compacted.length() == 11);
	}

	protected void dump(List<ParsedElement> elements) {
		for (ParsedElement e : elements) {
			System.out.println(e.type + "\t\t(" + e.start + "," + e.end + ")");
		}		
	}

}
