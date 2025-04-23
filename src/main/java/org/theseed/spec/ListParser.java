/**
 *
 */
package org.theseed.spec;

import java.util.List;

/**
 * A list parser parses a list of declarations, each of which will become a node. The declarations
 * units are separated by semicolons and enclosed in braces. The elements of the list are
 * children of the parent node passed in. The subclass determines what types of nodes are
 * expected in the list.
 *
 * @author Bruce Parrello
 *
 */
public abstract class ListParser {

	// FIELDS
	/** parent node */
	private SpecNode parent;
	/** controlling specification parser */
	private SpecParser parser;

	/**
	 * Construct a list parser.
	 *
	 * @param parentNode	parent node to contain the list elements
	 * @param specParser	specification parser for navigation the token stream
	 */
	public ListParser(SpecNode parentNode, SpecParser specParser) {
		this.parent = parentNode;
		this.parser = specParser;
	}

	/**
	 * Parse the current list and add its children to the parent node.
	 */
	public void parse() {
		// Get the next non-comment token. It should be an open brace. The comments are
		// attached to the parent node.
		SpecToken opening = parent.nextToken(this.parser);
		if (! opening.isDelim("{"))
			this.parser.throwUnexpectedException("\"{\"", opening);
		// Now we loop through the declarations until we find the closing brace, asking
		// the subclass to parse the nodes.
		SpecToken next = this.parser.nextToken();
		while (! next.isDelim("}")) {
			// Save the initial comments.
			List<String> initialComments = parser.pullComments();
			// Parse the declaration, absorbing the semi-colon.
			SpecNode declaration = this.processDeclaration(next);
			// Store the initial comments.
			declaration.storeInitialComments(initialComments);
			this.parent.addChild(declaration);
			next = this.parser.nextToken();
		}
		// Save any comments that belong to the ending delimiter.
		this.parent.addComments(parser.pullComments());
	}

	/**
	 * Parse the declaration at the current position. The declaration node is returned and
	 * will be added to the parent node by the parser. The trailing delimiter must be
	 * consumed.
	 *
	 * @param next		token that initiates the declaration
	 *
	 * @return the specification node representing the declaration
	 */
	protected abstract SpecNode processDeclaration(SpecToken next);

	/**
	 * @return the controlling spec parser
	 */
	protected SpecParser getSpecParser() {
		return this.parser;
	}

}
