/**
 *
 */
package org.theseed.spec;

import java.util.List;
import java.util.Map;

import j2html.tags.ContainerTag;

/**
 * This type describes a normal hash, with keys and values. The keys and values are strongly typed,
 * so the type declaration declares two child types.
 *
 * @author Bruce Parrello
 *
 */
public class MappingTypeNode extends TypeNode {

	/**
	 * Construct a mapping type and consume its tokens.
	 *
	 * @param typeMap		map of currently-existing types
	 * @param specParser	controlling specification parser
	 * @param comments		comment buffer for comments relating to this type instance
	 */
	public MappingTypeNode(Map<String, TypeNode> typeMap, SpecParser specParser, List<String> comments) {
		TypeNode.parseTypeList(this, "<", ">", typeMap, specParser, comments);
		// A mapping type has exactly two subtypes-- key and value.
		if (this.getChildCount() != 2)
			specParser.throwSyntaxException("A mapping type must have exactly two subtypes.");
	}

	@Override
	public ContainerTag toHtml() {
		// A mapping is displayed as a two-row table. The key and value are stored as child types.


		// TODO code for mapping type toHtml
		return null;
	}

}
