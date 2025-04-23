/**
 *
 */
package org.theseed.spec;

import java.util.List;

/**
 * A member node contains a type specification node and the comments associated with its membership. Note
 * that this is not a type of specification node, but rather a vehicle used to return comments and
 * types together.
 *
 * @author Bruce Parrello
 *
 */
public class MemberNode {

	// FIELDS
	/** type node */
	private TypeNode node;
	/** comments relating to its membership */
	private List<String> comments;

	/**
	 * Construct a member node for the specified child of the specified specification node.
	 *
	 * @param parent	parent node
	 * @param idx		child index
	 */
	public MemberNode(SpecNode parent, int idx) {
		this.node = (TypeNode) parent.getChild(idx);
		this.comments = parent.getChildComment(idx);
	}

	/**
	 * @return the member node itself
	 */
	public TypeNode getType() {
		return this.node;
	}

	/**
	 * @return the membership comments
	 */
	public List<String> getComments() {
		return this.comments;
	}

}
