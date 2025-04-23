/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The definition parser is a list parser that processes type and function definitions. A list of type
 * and function definitions makes up a module.
 *
 * @author Bruce Parrello
 *
 */
public class DefinitionParser extends ListParser {

	// FIELDS
	/** map of type names to type definition nodes */
	private Map<String, TypeNode> typeMap;

	/**
	 * Parse the list of definitions in a module node.
	 *
	 * @param parentNode	parent module node
	 * @param specParser	controlling specification parser
	 */
	public DefinitionParser(ModuleNode parentNode, SpecParser specParser) {
		super(parentNode, specParser);
		// Create the primitive types. Note that we never remove from this map, but we want to
		// preserve the order of insertion from the specification file.
		this.typeMap = new HashMap<String, TypeNode>();
		this.typeMap.put("int", new PrimitiveTypeNode("int", "basic integer number"));
		this.typeMap.put("float", new PrimitiveTypeNode("float", "basic floating-point number"));
		this.typeMap.put("string", new PrimitiveTypeNode("string", "character or text string"));
	}

	@Override
	protected SpecNode processDeclaration(SpecToken next) {
		SpecNode retVal;
		// Save the spec parser.
		SpecParser parser = this.getSpecParser();
		// Create an output list for comments.
		List<String> comments = new ArrayList<String>(1);
		// Here we must parse a definition. Check the opening token.
		String tokenText = next.getText();
		switch (tokenText) {
		case "typedef" :
			// Here we have a type definition.
			TypeNode type = TypeNode.parse(this.typeMap, parser, comments);
			// Get the type name and save all the comments into the type node.
			SpecToken nameToken = parser.nextToken();
			comments.addAll(parser.pullComments());
			type.addComments(comments);
			// Insure we have a word token here.
			if (! nameToken.isWord())
				parser.throwUnexpectedException("identifier", nameToken);
			String name = nameToken.getText();
			// Insure the type name is not a duplicate.
			if (this.typeMap.containsKey(name))
				parser.throwSyntaxException("Duplicate type name \"" + name + "\".");
			// Is this type an alias?
			if (! type.isAnonymous()) {
				// Create an alias node.
				type = new AliasTypeNode(name, type);
			} else {
				type.setName(name);
			}
			// Save the type under the specified name.
			this.typeMap.put(name, type);
			retVal = type;
			// Push past the delimiter.
			SpecToken token = parser.nextToken();
			if (! token.isDelim(";"))
				parser.throwUnexpectedException("\";\"", token);
			break;
		case "funcdef" :
			// Here we have a function declaration.
			retVal = FuncNode.parse(this.typeMap, parser);
			break;
		default :
			// Here we have an error.
			retVal = null;
			parser.throwUnexpectedException("\"typedef\" or \"funcdef\"", next);
		}
		return retVal;
	}

}
