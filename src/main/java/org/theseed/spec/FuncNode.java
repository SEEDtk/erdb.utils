/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import j2html.tags.ContainerTag;

/**
 * This object represents a function declaration. The function declaration has zero or more
 * parameters, zero or more return values, and may or may not require authentication.
 * The parameters and the return types are all child nodes. The number of parameters is
 * also the index of the first return type.
 *
 * @author Bruce Parrello
 *
 */
public class FuncNode extends SpecNode implements Comparable<FuncNode> {

	// FIELDS
	/** number of parameters */
	private int parmCount;
	/** function name */
	private String funcName;
	/** TRUE if authentication is required, else FALSE */
	private boolean authRequired;

	/**
	 * Construct a new, blank function node.
	 *
	 * @param name		function name
	 */
	protected FuncNode(String name) {
		this.parmCount = 0;
		this.funcName = name;
		this.authRequired = false;
	}

	/**
	 * Parse the function definition at the current location. The entire definition will
	 * be consumed, up to and including the terminating delimiter.
	 *
	 * @param typeMap	map of previously-defined types
	 * @param parser	controlling specification parser
	 *
	 * @return the function definition node
	 */
	public static FuncNode parse(Map<String, TypeNode> typeMap, SpecParser parser) {
		// We will accumulate comments in here.
		List<String> comments = new ArrayList<String>(1);
		// Get the function name.
		SpecToken token = parser.nextToken();
		FuncNode retVal = new FuncNode(token.getText());
		// Save the comments.
		comments.addAll(parser.pullComments());
		// Parse the parameter type list.
		TypeNode.parseTypeList(retVal, "(", ")", typeMap, parser, comments);
		// Save the parameter count.
		retVal.parmCount = retVal.getChildCount();
		// Get the next token. It can be "returns", "authentication" or a delimiter.
		token = parser.nextToken();
		comments.addAll(parser.pullComments());
		if (token.isWord("returns")) {
			// Here we have a return list to parse.
			TypeNode.parseTypeList(retVal, "(", ")", typeMap, parser, comments);
			// Get the next token after the return list.
			token = parser.nextToken();
			comments.addAll(parser.pullComments());
		}
		if (token.isWord("authentication")) {
			// Here we need to consume the following "required" token as well.
			token = parser.nextToken();
			comments.addAll(parser.pullComments());
			if (! token.isWord("required"))
				parser.throwUnexpectedException("\"required\"", token);
			// Push forward to the terminating semicolon.
			token = parser.nextToken();
			comments.addAll(parser.pullComments());
			// Save the authentication flag.
			retVal.authRequired = true;
		}
		if (! token.isDelim(";"))
			parser.throwUnexpectedException("\";\"", token);
		// Store the comments and return the function node.
		retVal.addComments(comments);
		return retVal;
	}

	@Override
	public ContainerTag toHtml() {
		// TODO code for function declaration toHtml
		return null;
	}

	@Override
	public int compareTo(FuncNode o) {
		// Function nodes are sorted by function name.
		return this.funcName.compareTo(o.funcName);
	}


	@Override
	public int hashCode() {
		return Objects.hash(funcName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FuncNode other = (FuncNode) obj;
		return Objects.equals(this.funcName, other.funcName);
	}

	/**
	 * @return the function name
	 */
	public String getName() {
		return this.funcName;
	}

	/**
	 * @return the list of function parameters
	 */
	List<MemberNode> getParms() {
		List<MemberNode> retVal = IntStream.range(0, this.parmCount).mapToObj(i -> new MemberNode(this, i))
				.toList();
		return retVal;
	}

	/**
	 * @return the list of function results
	 */
	List<MemberNode> getResults() {
		List<MemberNode> retVal = IntStream.range(this.parmCount, this.getChildCount())
				.mapToObj(i -> new MemberNode(this, i)).toList();
		return retVal;
	}

	/**
	 * @return TRUE if authentication is required for this function
	 */
	public boolean requiresAuthentication() {
		return this.authRequired;
	}

}
