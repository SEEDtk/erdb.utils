/**
 *
 */
package org.theseed.spec;

/**
 * This is a special exception that indicates a syntax error in an application spec.
 *
 * @author Bruce Parrello
 *
 */
public class SpecParsingException extends RuntimeException {

	/** serialization identifier */
	private static final long serialVersionUID = 2324971034514103231L;

	/**
	 * @param line		line number containing the error
	 * @param pos		position at which the error was detected
	 * @param message	basic error message
	 */
	public SpecParsingException(int line, int pos, String message) {
		super("Spec syntax error at line " + line + " col " + Integer.toString(pos + 1) + ": " + message);
	}

}
