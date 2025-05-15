/**
 *
 */
package org.theseed.spec;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

/**
 * This type node indicates an alias name for an existing type.
 *
 * @author Bruce Parrello
 *
 */
public class AliasTypeNode extends TypeNode {

	/**
	 * Construct an alias type node.
	 *
	 * @param name	alias name for the type
	 * @param type	original type definition
	 */
	public AliasTypeNode(String name, TypeNode type) {
		this.setName(name);
		this.addChild(type);
	}

	/**
	 * @return the type being aliased
	 */
	public TypeNode getActualType() {
		TypeNode retVal = (TypeNode) this.getChild(0);
		return retVal;
	}

	@Override
	public ContainerTag toDetailHtml() {
		// Get the target type.
		TypeNode target = this.getActualType();
		// Extract its anchor label.
		String label = target.getId();
		// Build an anchor tag to link to it.
		ContainerTag retVal = a(target.getName()).withHref("#" + label);
		return retVal;
	}

}
