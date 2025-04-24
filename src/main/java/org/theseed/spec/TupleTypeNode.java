/**
 *
 */
package org.theseed.spec;

import java.util.List;
import java.util.Map;
import j2html.tags.ContainerTag;

/**
 * Here we have a tuple type. A tuple is essentially a structure with anonymous fields. In practice,
 * it appears as a list in the JSON output.
 *
 * @author Bruce Parrello
 *
 */
public class TupleTypeNode extends TypeNode {

	/**
	 * Construct a tuple type and consume its tokens.
	 *
	 * @param typeMap		map of currently-existing types
	 * @param specParser	controlling specification parser
	 * @param comments		comment buffer for comments relating to this type instance
	 */
	public TupleTypeNode(Map<String, TypeNode> typeMap, SpecParser specParser, List<String> comments) {
		TypeNode.parseTypeList(this, "<", ">", typeMap, specParser, comments);
	}

	@Override
	public ContainerTag toHtml() {
		// TODO code for tuple type toHtml
		return null;
	}

}
