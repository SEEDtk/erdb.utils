/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

/**
 * This node contains a type definition. The type definition can be a primitive type, a tuple, a structure,
 * an alias, or a mapping.
 *
 * @author Bruce Parrello
 *
 */
public abstract class TypeNode extends SpecNode implements Comparable<TypeNode> {

	// FIELDS
	/** type name */
	private String typeName;
	/** unique type identifier */
	private String typeID;
	/** number of times used as a child */
	private int refCount;
	/** number of times used in a function */
	private int useCount;
	/** index number for next type identifier */
	private static int nextNum = 0;

	/**
	 * Construct an anonymous type node.
	 */
	public TypeNode() {
		this.typeName = null;
		this.refCount = 0;
		this.useCount = 0;
		// Compute the unique type ID.
		nextNum++;
		this.typeID = String.format("type%06d", nextNum);
	}

	/**
	 * Store the type name.
	 *
	 * @param name		proposed new type name
	 */
	public void setName(String name) {
		this.typeName = name;
	}

	/**
	 * @return the name of this type
	 */
	public String getName() {
		return this.typeName;
	}

	/**
	 * @return the HTML for the heading line of the function display
	 */
	public ContainerTag getHeader() {
		return h3(a(this.typeName).withName(this.typeID));
	}

	/**
	 * Add a child to this type. We have the additional task of updating the
	 * child's reference count.
	 *
	 * @param child		child node to add
	 */
	@Override
	public void addChild(SpecNode child) {
		TypeNode childType = null;
		if (child instanceof TypeNode)
			childType = (TypeNode) child;
		else if (child instanceof FieldNode)
			childType = (TypeNode) ((FieldNode) child).getType();
		if (childType != null) {
			childType.refCount++;
		}
		super.addChild(child);
	}

	/**
	 * @return TRUE if this is an anonymous type
	 */
	public boolean isAnonymous() {
		return this.typeName == null;
	}

	/**
	 * @return TRUE if this is a complex type
	 */
	public boolean isComplex() {
		return ! (this instanceof PrimitiveTypeNode);
	}

	/**
	 * Parse a type declaration and return the appropriate type node. At the point of
	 * entry, we are positioned before the first token of the declaration. At the
	 * end, we will be in front of whatever token follows (a type name, field name, or
	 * delimiter, depending on context).
	 *
	 * @param typeMap		map of type names to currently-available types
	 * @param specParser	controlling specification parser
	 * @param comments		output list for comments
	 *
	 * @return a definition node for the type found
	 */
	public static TypeNode parse(Map<String, TypeNode> typeMap, SpecParser specParser, List<String> comments) {
		// Get the first token.
		SpecToken token1 = specParser.nextToken();
		// Save the initial comments.
		comments.addAll(specParser.pullComments());
		// The type declared will be put in here.
		TypeNode retVal = parse(token1, typeMap, specParser, comments);
		// Return the type declaration node.
		return retVal;
	}

	/**
	 * This parses a type declaration when the first token has already been consumed.
	 *
	 * @param token1		initial token of the type declaration
	 * @param typeMap		map of type names to currently-available types
	 * @param specParser	controlling specification parser
	 * @param comments		output list for comments
	 *
	 * @return a definition node for the type found
	 */
	public static TypeNode parse(SpecToken token1, Map<String, TypeNode> typeMap, SpecParser specParser,
			List<String> comments) {
		TypeNode retVal;
		// There are four key reserved words for a type declaration. We process those here.
		String tokenText = token1.getText();
		switch (tokenText) {
		case "structure" :
			retVal = new StructureTypeNode(typeMap, specParser, comments);
			break;
		case "tuple" :
			retVal = new TupleTypeNode(typeMap, specParser, comments);
			break;
		case "list" :
			retVal = new ListTypeNode(typeMap, specParser, comments);
			break;
		case "mapping" :
			retVal = new MappingTypeNode(typeMap, specParser, comments);
			break;
		default :
			// Here we have a simple type name. The type must already exist.
			// No more tokens will be absorbed.
			retVal = typeMap.get(tokenText);
			if (retVal == null)
				specParser.throwSyntaxException("Undefined type \"" + tokenText + "\" specified.");
			break;
		}
		return retVal;
	}

	/**
	 * Parse a list of type declarations. The type declarations are comma-delimited and enclosed in
	 * some sort of bracketing (either parentheses or angle brackets). They will all be stored as
	 * child nodes of the specified parent node. Each type declaration can be followed by a field
	 * name that is stored as a comment.
	 *
	 * @param node			node to contain the type declarations
	 * @param openDelim		expected first delimiter
	 * @param closeDelim	expected termination delimiter
	 * @param typeMap		map of currently-available types
	 * @param specParser	controlling specification parser
	 * @param comments		comment buffer for this type declaration instance
	 */
	public static void parseTypeList(SpecNode node, String openDelim, String closeDelim,
			Map<String, TypeNode> typeMap, SpecParser specParser, List<String> comments) {
		// Get the open bracket. The comments here go to the incoming declaration, not
		// the child.
		SpecToken token = specParser.nextToken();
		comments.addAll(specParser.pullComments());
		if (! token.isDelim(openDelim))
			specParser.throwUnexpectedException("\"" + openDelim + "\"", token);
		// Now we are positioned on the first child. Between now and the next delimiter, all
		// comments go to the current child.
		token = specParser.nextToken();
		boolean endFound = token.isDelim(closeDelim);
		while (! endFound) {
			// Initialize this child's comment list.
			List<String> childComments = new ArrayList<String>(1);
			// Parse the type declaration here.
			TypeNode childType = TypeNode.parse(token, typeMap, specParser, childComments);
			// Check for a delimiter.
			token = specParser.nextToken();
			// Any comments go to the child comments.
			childComments.addAll(specParser.pullComments());
			if (token.isWord()) {
				// Here we have a field name. Add it as the first comment.
				childComments.add(0, token.getText());
				// The delimiter must follow.
				token = specParser.nextToken();
			}
			// Here we are definitely on a delimiter. It must be a comma or the close.
			if (token.isDelim(closeDelim))
				endFound = true;
			else if (! token.isDelim(","))
				specParser.throwUnexpectedException("\",\" or \"" + closeDelim + "\"", token);
			else {
				// Push past the delimiter to get the next type's first token.
				token = specParser.nextToken();
			}
			childComments.addAll(specParser.pullComments());
			// Add the child we've found.
			node.addChild(childType);
			node.storeChildComments(childComments);
		}
	}

	/**
	 * @return the child types of this type
	 */
	public List<TypeNode> getChildTypes() {
		List<TypeNode> retVal = this.getChildNodes().stream().filter(x -> x instanceof TypeNode)
				.map(x -> (TypeNode) x).toList();
		return retVal;
	}

	/**
	 * @return the member types of this type
	 */
	public List<MemberNode> getMembers() {
		List<MemberNode> retVal = IntStream.range(0, this.getChildCount())
				.mapToObj(i -> new MemberNode(this, i)).toList();
		return retVal;
	}

	/**
	 * @return the unique ID label of this type
	 */
	public String getId() {
		return this.typeID;
	}

	/**
	 * The HTML for the type is most commonly just the name with a reference to its anchor tag.
	 * If the type is anonymous, however, we must expand the full thing.
	 *
	 * @return the HTML for this type when used anywhere in the document
	 */
	public ContainerTag toHtml() {
		ContainerTag retVal;
		if (this.isAnonymous())
			retVal = this.toDetailHtml();
		else {
			retVal = a(this.typeName).withHref("#" + this.typeID);
		}
		return retVal;
	}

	/**
	 * @return the detailed HTML expansion of the type definition
	 */
	protected abstract ContainerTag toDetailHtml();

	/**
	 * Compare two types. The ordering is from highest to lowest use count, then lowest to
	 * highest parent count, then alphabetically by name. Anonymous types are last,
	 * sorted by type ID.
	 *
	 * @param other		other type to compare
	 */
	@Override
	public int compareTo(TypeNode other) {
		int retVal = other.useCount - this.useCount;
		if (retVal == 0)
			retVal = this.refCount - other.refCount;
		if (retVal == 0) {
			if (this.typeName == null) {
				if (other.typeName == null)
					retVal = this.typeID.compareTo(other.typeID);
				else
					retVal = -1;
			} else if (other.typeName == null)
				retVal = 1;
			else
				retVal = this.typeName.compareTo(other.typeName);
		}
		return retVal;
	}

	/**
	 * Denote this type has been used in a function.
	 */
	protected void markUsed() {
		this.useCount++;
	}

}
