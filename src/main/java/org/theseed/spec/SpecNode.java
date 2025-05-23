/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

/**
 * This object represents the tree that describes a specification structure or substructure. The basic
 * components of a specification are module definitions, type definitions, type specifications,
 * function definitions, and field declarations.
 *
 * A module definition is a sequence of type and function definitions. It is preceded by the reserved
 * word "module" and the components are enclosed in braces and terminated by semicolons.
 *
 * A type definition consists of a type specification and a type name. It is preceded by the reserved
 * word "typedef".
 *
 * A type specification is either a type keyword ("string", "int", "float", or a
 * previously-defined type name), or a complex type specification. The complex type
 * specifications are
 *
 * 	-	"structure" followed by a list of field declarations; the field declarations are terminated by
 * 		semi-colons and enclosed in braces
 *  -	"tuple" followed by a list of field declarations; the field declarations are separated by
 *  	commas and enclosed in angle brackets
 *  -	"list" followed by a single field declaration enclosed in angle brackets
 *  -	"mapping" followed by two field declarations, separated by commas and enclosed in angle brackets
 *
 * A field declaration consists of a type specification followed by an optional field name
 *
 * A function definition consists of a function name, a list of parameters, a return type, and an
 * authentication indicator. The parameters are a field declarations enclosed in parentheses and
 * separated by commas. The return type is a single type specification enclosed in parentheses. The
 * authentication indicator is the reserved phrase "authentication required" and is optional.
 *
 * @author Bruce Parrello
 *
 */
public abstract class SpecNode {

	// FIELDS
	/** list of children */
	private List<SpecNode> childNodes;
	/** list of child comments */
	private List<List<String>> childComments;
	/** associated parent comments */
	private List<String> comments;

	/**
	 * Create a blank specification node.
	 */
	public SpecNode() {
		this.childNodes = new ArrayList<SpecNode>(5);
		this.comments = new ArrayList<String>(1);
		this.childComments = new ArrayList<List<String>>(5);
	}

	/**
	 * Store the specified comments in this node. These go at the end of the list.
	 *
	 * @param pullComments
	 */
	protected void addComments(List<String> pullComments) {
		this.comments.addAll(pullComments);
	}

	/**
	 * Find the next token for this node and store the comments.
	 *
	 * @return the next non-comment token
	 */
	protected SpecToken nextToken(SpecParser parser) {
		SpecToken retVal = parser.nextToken();
		this.addComments(parser.pullComments());
		return retVal;
	}

	/**
	 * Add a child node.
	 *
	 * @param child		new child node to add
	 */
	protected void addChild(SpecNode child) {
		this.childNodes.add(child);
		// Add the associated comment list.
		this.childComments.add(new ArrayList<String>(1));
	}

	/**
	 * Store comments for the current child.
	 *
	 * @param comments	comments to add
	 */
	public void storeChildComments(List<String> comments) {
		this.childComments.getLast().addAll(comments);
	}

	/**
	 * Store the initial comments for this node. These go at the beginning of the list.
	 *
	 * @param comments0		initial comments
	 */
	public void storeInitialComments(List<String> comments0) {
		this.comments.addAll(0, comments0);
	}

	/**
	 * @return the number of child nodes
	 */
	public int getChildCount() {
		return this.childNodes.size();
	}

	/**
	 * @return the list of children
	 */
	public List<SpecNode> getChildNodes() {
		return this.childNodes;
	}


	/**
	 * @return basic HTML for this node
	 */
	public abstract ContainerTag toHtml();

	/**
	 * Get the child at the specified position.
	 *
	 * @param idx	child position
	 *
	 * @return the specified child node
	 */
	public SpecNode getChild(int idx) {
		return this.childNodes.get(idx);
	}

	/**
	 * Get the membership comments for the child at the specified position.
	 *
	 * @param idx	child position
	 *
	 * @return the specified child's membership comments
	 */
	public List<String> getChildComment(int idx) {
		return this.childComments.get(idx);
	}

	/**
	 * @return the list of comments for this node
	 */
	public List<String> getComments() {
		return this.comments;
	}

	/**
	 * Format comments as HTML paragraphs.
	 *
	 * @param commentList	list of comment strings
	 *
	 * @return the comments in HTML form
	 */
	public static ContainerTag formatComments(List<String> commentList) {
		ContainerTag retVal;
		// Check for the easy case of no comments.
		if (commentList.isEmpty())
			retVal = div(rawHtml("&nbsp;")).withClass("comment");
		else {
			// Paragraphs will be put in here.
			List<ContainerTag> parts = new ArrayList<ContainerTag>(commentList.size());
			// Preformat blocks will be built here.
			List<String> preBlock = new ArrayList<String>(5);
			// Loop through the comments.
			for (String comment : commentList) {
				if (! comment.isEmpty() && Character.isWhitespace(comment.charAt(0))) {
					// Here we have part of a preformat block.
					preBlock.add(comment);
				} else {
					// Insure we've cleared out the preformat block.
					if (! preBlock.isEmpty()) {
						parts.add(formatPreBlock(preBlock));
						preBlock.clear();
					}
					if (comment.isBlank()) {
						// Here we have a blank line.
						parts.add(p(rawHtml("&nbsp;")));
					} else {
						// here we have a normal line.
						parts.add(p(comment));
					}
				}
			}
			// Insure we've processed any residual indented block.
			if (! preBlock.isEmpty())
				parts.add(formatPreBlock(preBlock));
			// Assemble the comments.
			retVal = div().with(parts).withClass("comment");
		}
		return retVal;
	}

	/**
	 * @return the specified lines as a pre-formatted block
	 *
	 * @param preBlock	lines to pre-format
	 */
	private static ContainerTag formatPreBlock(List<String> preBlock) {
		return pre(StringUtils.join(preBlock, '\n'));
	}

}
