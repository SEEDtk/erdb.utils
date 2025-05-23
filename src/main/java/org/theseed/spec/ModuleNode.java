/**
 *
 */
package org.theseed.spec;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

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
		// Now we need to update the type node reference counts. This requires a recursive
		// traversal of the named types. (Only the named types matter for this.) We create
		// a "visited" set to prevent traversing the same node twice. Circular references
		// basically don't count.
		Set<String> visited = new HashSet<String>(this.getChildCount() * 4 / 3 + 1);
		// This is a stack of the type nodes not yet processed.
		Deque<TypeNode> types = new ArrayDeque<TypeNode>();
		// Fill it with the high-level types from this module and mark every one as visited.
		for (SpecNode child : this.getChildNodes()) {
			if (child instanceof TypeNode) {
				TypeNode type = (TypeNode) child;
				types.add(type);
				visited.add(type.getName());
			}
		}
		// Process the type stack until it is empty.
		while (! types.isEmpty()) {
			TypeNode type = types.pop();
			for (SpecNode child : type.getChildNodes()) {
				TypeNode childType = null;
				if (child instanceof TypeNode)
					childType = (TypeNode) child;
				else if (child instanceof FieldNode)
					childType = ((FieldNode) child).getType();
				if (childType != null) {
					childType.markReferenced();
					String childName = childType.getName();
					if (childName == null)
						types.push(childType);
					else if (! visited.contains(childName)) {
						visited.add(childName);
						types.push(childType);
					}
				}
			}
		}
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
				.sorted().toList();
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
		// The module is the highest-level entry in the specification, so it defines the body of the
		// web page. We have three sections-- the table of contents, the type definitions, and
		// the function definitions.
		// First we build the type definition segment. In the process, we'll accumulate a type table
		// of contents. This is a map of type names to identifiers.
		Map<String, String> typeTocMap = new LinkedHashMap<String, String>();
		// Get the list of types.
		List<TypeNode> typeList = this.getTypes();
		// This will be a list of type definitions.
		List<ContainerTag> typeDivs = new ArrayList<ContainerTag>(typeList.size());
		// Loop through the types.
		for (TypeNode type : typeList) {
			String typeName = type.getName();
			typeTocMap.put(typeName, type.getId());
			// The type definition consists of a header and a definition.
			ContainerTag typeDiv = div(type.getHeader(), type.toDetailHtml()).withClass("type");
			typeDivs.add(typeDiv);
		}
		// Now we wrap the type definitions in a section.
		ContainerTag typeListHtml = div(a(h2("Type Definitions")).withName("Types")).with(typeDivs);
		// Next we build the function definition segment. We need another TOC map and a list
		// of definitions.
		Map<String, String> funcTocMap = new TreeMap<String, String>();
		Collection<FuncNode> funcList = this.getFunctions().values();
		List<ContainerTag> funcDivs = new ArrayList<ContainerTag>(funcList.size());
		// Loop through the functions.
		for (FuncNode func : funcList) {
			String funcName = func.getName();
			funcTocMap.put(funcName, func.getId());
			// The function also consists of a header and a definition.
			ContainerTag funcDiv = div(func.getHeader(), func.toHtml()).withClass("function");
			funcDivs.add(funcDiv);
		}
		// Wrap the function definitions in a section as well.
		ContainerTag funcListHtml = div(a(h2("Function Definitions")).withName("Functions")).with(funcDivs);
		// We'll assemble the sections here.
		List<ContainerTag> sections = new ArrayList<ContainerTag>(4);
		// Set up for the table of contents.
		List<ContainerTag> tocItems = new ArrayList<ContainerTag>(4);
		// Do we have module-level comments?
		List<String> modComments = this.getComments();
		if (! modComments.isEmpty()) {
			// Yes. Create a section for it.
			sections.add(div(a(h2("Notes")).withName("Notes"), SpecNode.formatComments(modComments)));
			tocItems.add(li(a("Notes").withHref("#Notes")));
		}
		// Add the other sections.
		sections.add(typeListHtml);
		sections.add(funcListHtml);
		// Add the type definitions.
		ContainerTag tocItem = this.buildToc("Types", typeTocMap);
		tocItems.add(tocItem);
		// Add the function definitions.
		tocItem = this.buildToc("Functions", funcTocMap);
		tocItems.add(tocItem);
		// Assemble the full TOC section.
		ContainerTag tocList = ul().with(tocItems);
		// Put it at the front of the sections.
		sections.add(0, tocList);
		// Assemble the full module definition.
		ContainerTag retVal = div(h1(this.modName)).with(sections);
		return retVal;
	}

	/**
	 * Build a table of contents section.
	 *
	 * @param section		section name
	 * @param tocMap		map of elements to IDs
	 *
	 * @return	a list item for this TOC section
	 */
	private ContainerTag buildToc(String section, Map<String, String> tocMap) {
		ContainerTag retVal = li(a(section).withHref("#" + section),
				ol().with(tocMap.entrySet().stream().map(x -> li(a(x.getKey()).withHref("#" + x.getValue())))));
		return retVal;
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
