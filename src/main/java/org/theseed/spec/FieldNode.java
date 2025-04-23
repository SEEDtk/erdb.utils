/**
 *
 */
package org.theseed.spec;

import j2html.tags.ContainerTag;

/**
 * This node represents a field declaration for a structure type. It has one child-- the type declaration--
 * and contains the field name.
 *
 * @author Bruce Parrello
 *
 */
public class FieldNode extends SpecNode {

	// FIELDS
	/** field name */
	private String fieldName;

	/**
	 * Create a new field node.
	 *
	 * @param type	type of the field
	 * @param text	name of the field
	 */
	public FieldNode(TypeNode type, String text) {
		this.addChild(type);
		this.fieldName = text;
	}

	/**
	 * @return the name of this field
	 */
	public String getName() {
		return this.fieldName;
	}

	/**
	 * @return the type of this field
	 */
	public TypeNode getType() {
		return (TypeNode) this.getChild(0);
	}

	@Override
	public ContainerTag toHtml() {
		// TODO code for field declaration toHtml
		return null;
	}

}
