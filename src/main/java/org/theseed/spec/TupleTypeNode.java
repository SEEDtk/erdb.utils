/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

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
	public ContainerTag toDetailHtml() {
		final int n = this.getChildCount();
		List<ContainerTag> rows = new ArrayList<ContainerTag>(n + 1);
		// Start with the header row.
		rows.add(tr(th("type"), th("comment")));
		// Add the item rows.
		for (int i = 0; i < n; i++) {
			ContainerTag typeHtml = ((TypeNode) this.getChild(i)).toHtml();
			ContainerTag commentHtml = SpecNode.formatComments(this.getChildComment(i));
			rows.add(tr(td(typeHtml), td(commentHtml)));
		}
		ContainerTag retVal = div(p("Tuple Type"), table().with(rows));
		return retVal;
	}

}
