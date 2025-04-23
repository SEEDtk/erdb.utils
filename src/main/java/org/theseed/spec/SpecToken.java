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

	/**
	 * @return TRUE if this is a comment token
	 */
	public boolean isComment() {
		return this.type == SpecToken.Type.COMMENT;
	}

	/**
	 * Determine if this token represents the specified reserved word.
	 *
	 * @param word	expected reserved word
	 *
	 * @return TRUE if this token is the expected reserved word, else FALSE
	 */
	public boolean isWord(String word) {
		return this.type == SpecToken.Type.WORD && this.text.equals(word);
	}

	/**
	 * @return TRUE if this token is a word
	 */
	public boolean isWord() {
		return this.type == SpecToken.Type.WORD;
	}

	/**
	 * Determine if this token represents the specified delimiter.
	 *
	 * @param delim		expected delimiter
	 *
	 * @return TRUE if this token is the expected delimiter, else FALSE
	 */
	public boolean isDelim(String delim) {
		return this.type == SpecToken.Type.DELIM && this.text.equals(delim);
	}

}
