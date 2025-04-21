/**
 *
 */
package org.theseed.spec;

/**
 * This object represents a token in the input for an application spec. The token can either be
 * a word, a delimiter, or a comment.
 *
 * @author Bruce Parrello
 *
 */
public class SpecToken {

	// FIELDS
	/** token type */
	private Type type;
	/** token text */
	private String text;

	/**
	 * This enum describes the types of tokens.
	 */
	public static enum Type {
		WORD, DELIM, COMMENT;
	}

	/**
	 * Construct a token.
	 *
	 * @param tokenType		token type
	 * @param tokenText		token text
	 */
	public SpecToken(Type tokenType, String tokenText) {
		this.type = tokenType;
		this.text = tokenText;
	}

	/**
	 * @return the token type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * @return the token text
	 */
	public String getText() {
		return this.text;
	}

	@Override
	public String toString() {
		return this.type + ": " + this.text;
	}

}
