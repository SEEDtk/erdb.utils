/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import j2html.tags.ContainerTag;

/**
 * This is the most complex type. It describes a structure with named fields. Each field is described by
 * a type declaration and a field name. This means the child nodes are field nodes and not type nodes.
 *
 * @author Bruce Parrello
 *
 */
public class StructureTypeNode extends TypeNode {

	/**
	 * Construct a structure type declaration and consume its tokens.
	 *
	 * @param typeMap		map of previously-defined types
	 * @param specParser	controlling specification parser
	 * @param comments		output list for comments
	 */
	public StructureTypeNode(Map<String, TypeNode> typeMap, SpecParser specParser, List<String> comments) {
		// Get the open-brace delimiter.
		SpecToken token = specParser.nextToken();
		if (! token.isDelim("{"))
			specParser.throwUnexpectedException("\"{\"", token);
		comments.addAll(specParser.pullComments());
		// Now we loop through the field definitions until we find the closing brace. Note that each field
		// definition includes a type declaration, a field name, and a terminating semicolon. We begin with
		// the token for the start of the type declaration. If this is a closing brace, we are done.
		token = specParser.nextToken();
		while (! token.isDelim("}")) {
			// Create a comment list for this field. We initialize it with the preceding comments.
			List<String> fieldComments = new ArrayList<String>(specParser.pullComments());
			TypeNode type = TypeNode.parse(token, typeMap, specParser, fieldComments);
			// Now we need the field name.
			token = specParser.nextToken();
			if (! token.isWord())
				specParser.throwUnexpectedException("a field name", token);
			FieldNode field = new FieldNode(type, token.getText());
			fieldComments.addAll(specParser.pullComments());
			// Finally, get the terminating delimiter.
			token = specParser.nextToken();
			if (! token.isDelim(";"))
				specParser.throwUnexpectedException("\";\"", token);
			fieldComments.addAll(specParser.pullComments());
			// Add the field to the structure definition. Note we can store the comments directly
			// in the field definition, since it's owned by the structure.
			this.addChild(field);
			field.addComments(fieldComments);
			// Get the first token of the next type declaration (which may be a terminating brace).
			token = specParser.nextToken();
		}
		// The comments before the final brace relate to the structure itself.
		comments.addAll(specParser.pullComments());
	}

	@Override
	public ContainerTag toHtml() {
		// TODO code for structure type toHtml
		return null;
	}

	/**
	 * @return an ordered list of the structure fields
	 */
	public List<FieldNode> getFields() {
		List<FieldNode> retVal = this.getChildNodes().stream().map(x -> (FieldNode) x).toList();
		return retVal;
	}
}
