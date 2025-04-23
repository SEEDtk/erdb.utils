/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.theseed.io.LineReader;

/**
 * This object parses a specification. It takes a stream of tokens and converts it into
 * a module definition node.
 *
 * @author Bruce Parrello
 *
 */
public class SpecParser {

	// FIELDS
	/** current token stream */
	private SpecTokenizer tokenStream;
	/** current token iterator */
	private Iterator<SpecToken> tokens;
	/** current saved comment list */
	private List<String> comments;

	/**
	 * Construct a parser for an input stream.
	 *
	 * @param reader	line reader containing the input
	 */
	public SpecParser(LineReader reader) {
		// Attach ourselves to the line reader to get tokens.
		this.tokenStream = new SpecTokenizer(reader);
		this.tokens = this.tokenStream.iterator();
		this.comments = new ArrayList<String>(1);
	}

	/**
	 * Get the next non-comment token.
	 *
	 * @return a non-comment token that is not null
	 */
	public SpecToken nextToken() {
		SpecToken retVal = null;
		while (this.tokens.hasNext() && retVal == null) {
			SpecToken token = this.tokens.next();
			if (token.isComment())
				this.comments.add(token.getText());
			else
				retVal = token;
		}
		if (retVal == null)
			this.tokenStream.throwParseError("Unexpected end of file.");
		return retVal;
	}

	/**
	 * This throws an exception that indicates the wrong token was found at the current position.
	 *
	 * @param expected	expected token description
	 * @param found		token actually found
	 */
	public void throwUnexpectedException(String expected, SpecToken found) {
		String errorText = "Expected " + expected + " found \"" + found + "\".";
		this.tokenStream.throwParseError(errorText);
	}

	/**
	 * Get the comments for the current token and remove them from the comment list.
	 *
	 * @return the list of comments for the current token
	 */
	public List<String> pullComments() {
		List<String> retVal = this.comments;
		// Note we create a new list because the old list is now owned by the client.
		this.comments = new ArrayList<String>(1);
		return retVal;
	}

	/**
	 * This throws an exception that indicates a general error at the current position.
	 *
	 * @param message	description of the error.
	 */
	public void throwSyntaxException(String message) {
		this.tokenStream.throwParseError(message);
	}

}
