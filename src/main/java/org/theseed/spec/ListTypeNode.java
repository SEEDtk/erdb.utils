/**
 *
 */
package org.theseed.spec;

import java.util.List;
import java.util.Map;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

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
	public ContainerTag toDetailHtml() {
		// A list type is a list of objects of some other type. We format it as a DIV block.
		ContainerTag retVal = div(text("List of "), this.getChild(0).toHtml(), SpecNode.formatComments(this.getComments()));
		return retVal;
	}

}
