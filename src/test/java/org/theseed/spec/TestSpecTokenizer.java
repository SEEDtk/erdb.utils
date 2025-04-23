/**
 *
 */
package org.theseed.spec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.text.TextStringBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.io.LineReader;

/**
 * @author Bruce Parrello
 *
 */
class TestSpecTokenizer {

	/** logging facility */
	protected static Logger log = LoggerFactory.getLogger(TestSpecTokenizer.class);


	@Test
	void testTokenizing() throws IOException {
		File inFile = new File("data", "test.spec");
		File testFile = new File("data", "GenomeAnnotation.txt");
		try (LineReader reader = new LineReader(inFile);
				LineReader verifier = new LineReader(testFile)) {
			SpecTokenizer tokenStream = new SpecTokenizer(reader);
			for (SpecToken token : tokenStream) {
				// Get the next comparison token.
				Iterator<String[]> sectionIter = verifier.new SectionIter("//", "\t");
				String[] parts = sectionIter.next();
				SpecToken.Type type = SpecToken.Type.valueOf(parts[0]);
				String loc = parts[1];
				assertThat(tokenStream.location(), equalTo(loc));
				assertThat(loc, token.getType(), equalTo(type));
				TextStringBuilder textBuffer = new TextStringBuilder(80);
				while (sectionIter.hasNext()) {
					parts = sectionIter.next();
					if (parts.length == 0)
						textBuffer.append('\n');
					else {
						assertThat(loc, parts.length, equalTo(1));
						textBuffer.appendSeparator('\n').append(parts[0]);
					}
				}
				assertThat(loc, token.getText(), equalTo(textBuffer.toString()));
			}
		}
	}

}
