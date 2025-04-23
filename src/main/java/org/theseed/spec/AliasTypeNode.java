/**
 *
 */
package org.theseed.spec;

import j2html.tags.ContainerTag;

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

	@Override
	public ContainerTag toHtml() {
		// TODO code for toHtml
		return null;
	}

}
