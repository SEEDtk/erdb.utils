/**
 *
 */
package org.theseed.spec;

import java.util.List;
import java.util.Map;

import j2html.tags.ContainerTag;

/**
 * This type defines a list of objects of identical type. We expect to find a single type declaration
 * enclosed in angle brackets. This type is stored as our only child. This is a general sub-case
 * of a type list, which consists of type declarations in a comma-delimited list between two
 * brackets of some sort (angle or paren). Each type declaration can be followed by an optional
 * name that is treated as an initial comment.
 *
 * @author Bruce Parrello
 *
 */
public class ListTypeNode extends TypeNode {

	/**
	 * Construct a list type and consume its tokens.
	 *
	 * @param typeMap		map of currently-existing types
	 * @param specParser	controlling specification parser
	 * @param comments		comment buffer for comments relating to this type instance
	 */
	public ListTypeNode(Map<String, TypeNode> typeMap, SpecParser specParser, List<String> comments) {
		TypeNode.parseTypeList(this, "<", ">", typeMap, specParser, comments);
		if (this.getChildCount() != 1)
			specParser.throwSyntaxException("Lists must have exactly one member type.");
	}

	@Override
	public ContainerTag toHtml() {
		// TODO code for list type toHtml
		return null;
	}

}
