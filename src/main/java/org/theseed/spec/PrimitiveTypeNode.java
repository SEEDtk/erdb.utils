/**
 *
 */
package org.theseed.spec;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

/**
 * This object represents a primitive type.
 *
 * @author Bruce Parrello
 *
 */
public class PrimitiveTypeNode extends TypeNode {

	// FIELDS
	/** type description */
	private String desc;

	/**
	 * Construct a primitive type node with the specified description.
	 *
	 * @param name			name of the primitive type
	 * @param description	brief description of the primitive type
	 */
	public PrimitiveTypeNode(String name, String description) {
		this.desc = description;
		this.setName(name);
	}

	@Override
	public ContainerTag toHtml() {
		ContainerTag retVal = null;
		retVal = div(a().withName(this.getId()), h3(this.getName()), p(this.desc));
		return retVal;
	}

}
