/**
 *
 */
package org.theseed.spec;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import j2html.tags.ContainerTag;

/**
 * This is the initial node for the module definition. It is expected it will absorb the whole
 * input file.
 *
 * @author Bruce Parrello
 *
 */
public class ModuleNode extends SpecNode {

	// FIELDS
	/** module name */
	private String modName;

	/**
	 * Construct the master module node for a specification parser.
	 *
	 * @param specParser	specification parser for the module input file
	 */
	public ModuleNode(SpecParser specParser) {
		// Get the initiating reserved word and save the associated comments.
		SpecToken token = this.nextToken(specParser);
		// Insure it's a module start, as we expect.
		if (! token.isWord("module"))
			specParser.throwUnexpectedException("\"module\"", token);
		// Get the module name.
		token = this.nextToken(specParser);
		if (! token.isWord())
			specParser.throwUnexpectedException("module name", token);
		this.modName = token.getText();
		// Save the comments.
		this.addComments(specParser.pullComments());
		// Create the definition parser.
		ListParser parser = new DefinitionParser(this, specParser);
		// Parse the type and function definitions.
		parser.parse();
	}

	/**
	 * @return the module name
	 */
	public String getName() {
		return this.modName;
	}

	/**
	 * @return the list of types
	 */
	public List<TypeNode> getTypes() {
		List<TypeNode> retVal = this.getChildNodes().stream().filter(x -> x instanceof TypeNode).map(x -> (TypeNode) x)
				.toList();
		return retVal;
	}

	/**
	 * This returns the function definitions, sorted by name.
	 *
	 * @return a map of function names to function definitions
	 */
	public Map<String, FuncNode> getFunctions() {
		Map<String, FuncNode> retVal = new TreeMap<String, FuncNode>();
		for (SpecNode node : this.getChildNodes()) {
			if (node instanceof FuncNode) {
				FuncNode funcNode = (FuncNode) node;
				retVal.put(funcNode.getName(), funcNode);
			}
		}
		return retVal;
	}

	@Override
	public ContainerTag toHtml() {
		// TODO code for Module toHtml
		return null;
	}

	/**
	 * @return the type map for this module
	 */
	public Map<String, TypeNode> getTypeMap() {
		List<TypeNode> list = this.getTypes();
		Map<String, TypeNode> retVal = new TreeMap<String, TypeNode>();
		for (TypeNode type : list)
			retVal.put(type.getName(), type);
		return retVal;
	}

}
