/**
 *
 */
package org.theseed.spec;

import java.util.List;
import java.util.Map;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

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
	public ContainerTag toDetailHtml() {
		// A mapping is displayed as a two-row table. The key and value are stored as child types.
		TypeNode keyType = (TypeNode) this.getChild(0);
		TypeNode valueType = (TypeNode) this.getChild(1);
		ContainerTag mapTable = table().with(tr(th("Key"), td(keyType.toHtml()), td(SpecNode.formatComments(this.getChildComment(0)))),
				tr(th("Value"), td(valueType.toHtml()), td(SpecNode.formatComments(this.getChildComment(0)))));
		return div(p("Mapping Type"), mapTable);
	}

}
