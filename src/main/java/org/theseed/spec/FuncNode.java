/**
 *
 */
package org.theseed.spec;

import static j2html.TagCreator.*;

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
	/** unique function identifier */
	private String funcID;
	/** index number for next function identifier */
	private static int nextNum = 0;

	/**
	 * Construct a new, blank function node.
	 *
	 * @param name		function name
	 */
	protected FuncNode(String name) {
		this.parmCount = 0;
		this.funcName = name;
		this.authRequired = false;
		// Generate the anchor label.
		nextNum++;
		this.funcID = String.format("func%06d", nextNum);
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
		// The function declaration is fairly complex. It has a table of parameter types and a table of return types.
		// The function name is not included, since that is output by the parent.
		List<ContainerTag> parts = new ArrayList<ContainerTag>(4);
		// Start with the comments (if any).
		List<String> comments = this.getComments();
		if (! comments.isEmpty())
			parts.add(SpecNode.formatComments(comments));
		// Put a note here if authorization is required.
		if (this.authRequired)
			parts.add(p(em("This function requires authorization.")));
		// Now we output the parameters.
		List<MemberNode> parms = this.getParms();
		if (parms.isEmpty())
			parts.add(p("There are no parameters."));
		else {
			List<ContainerTag> parmRows = this.createMemberTable("parm", parms);
			parts.add(table().withClass("parms").with(parmRows));
		}
		// Next we output the return types.
		List<MemberNode> results = this.getResults();
		if (results.isEmpty())
			parts.add(p("The function does not return any values."));
		else {
			List<ContainerTag> resultRows = this.createMemberTable("result", results);
			parts.add(table().withClass("results").with(resultRows));
		}
		// Build the full function declaration.
		ContainerTag retVal = div().with(parts);
		return retVal;
	}

	/**
	 * @param type		label for this table's members
	 * @param members	list of member nodes to output
	 *
	 * @return a table displaying the member types and comments
	 */
	private List<ContainerTag> createMemberTable(String type, List<MemberNode> members) {
		// We build a table with three columns-- index, type, and comments.
		List<ContainerTag> parmRows = new ArrayList<ContainerTag>(members.size() + 1);
		parmRows.add(tr(th("#"), th(type + " type"), th("description")));
		int idx = 0;
		for (MemberNode parm : members) {
			idx++;
			TypeNode parmType = parm.getType();
			ContainerTag commentHtml = SpecNode.formatComments(parm.getComments());
			parmRows.add(tr(th(Integer.toString(idx)), td(parmType.toHtml()), td(commentHtml)));
		}
		return parmRows;
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
	 * @return the HTML for the heading line of the function display
	 */
	public ContainerTag getHeader() {
		return h3(a(this.funcName).withName(this.funcID));
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

	/**
	 * @return the unique ID label of this function
	 */
	public String getId() {
		return this.funcID;
	}


}
