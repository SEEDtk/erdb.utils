/**
 *
 */
package org.theseed.spec;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.theseed.erdb.utils.DisplayProcessor;
import org.theseed.io.LineReader;

import j2html.tags.ContainerTag;
import static j2html.TagCreator.*;

/**
 * @author Bruce Parrello
 *
 */
class TestSpecParsers {

	@Test
	void testModuleCompile() throws IOException {
		File inFile = new File("data", "GenomeAnnotation.spec");
		ModuleNode modNode;
		try (LineReader reader = new LineReader(inFile)) {
			SpecParser parser = new SpecParser(reader);
			modNode = new ModuleNode(parser);
			assertThat(modNode.getName(), equalTo("GenomeAnnotation"));
			Map<String, TypeNode> typeMap = modNode.getTypeMap();
			assertThat(typeMap, hasEntry(equalTo("genomeTO"), instanceOf(StructureTypeNode.class)));
			StructureTypeNode gtoType = (StructureTypeNode) typeMap.get("genomeTO");
			List<FieldNode> gtoMembers = gtoType.getFields();
			assertThat(gtoMembers.get(0).getType().getName(), equalTo("genome_id"));
			assertThat(gtoMembers.get(0).getName(), equalTo("id"));
			assertThat(gtoMembers.size(), equalTo(24));
			FieldNode lineage = gtoMembers.get(8);
			assertThat(lineage.getName(), equalTo("ncbi_lineage"));
			assertThat(lineage.getComments().size(), equalTo(0));
			TypeNode lineageType = lineage.getType();
			assertThat(lineageType, instanceOf(ListTypeNode.class));
			assertThat(lineageType.isAnonymous(), equalTo(true));
			TypeNode elementType = (TypeNode) lineageType.getChild(0);
			assertThat(elementType, instanceOf(TupleTypeNode.class));
			assertThat(elementType.isAnonymous(), equalTo(true));
			List<MemberNode> tupleTypes = ((TupleTypeNode) elementType).getMembers();
			MemberNode typeX = tupleTypes.get(0);
			assertThat(typeX.getComments(), contains("taxon_name"));
			assertThat(typeX.getType().getName(), equalTo("string"));
			typeX = tupleTypes.get(1);
			assertThat(typeX.getComments(), contains("taxon_id"));
			assertThat(typeX.getType().getName(), equalTo("int"));
			typeX = tupleTypes.get(2);
			assertThat(typeX.getComments(), contains("taxon_rank"));
			assertThat(typeX.getType().getName(), equalTo("string"));
			assertThat(tupleTypes.size(), equalTo(3));
			FieldNode lastMember = gtoMembers.get(23);
			assertThat(lastMember.getComments().get(0), equalTo("This is used for viral variants, not subsystem variants"));
			assertThat(lastMember.getType(), instanceOf(ListTypeNode.class));
			assertThat(lastMember.getName(), equalTo("computed_variants"));
		}
		ContainerTag modHtml = modNode.toHtml();
        ContainerTag head = head().with(link().withRel("styleSheet").withHref(DisplayProcessor.STYLE_SHEET))
                .with(title(modNode.getName()));
        ContainerTag page = html().with(head, body().with(modHtml));
        try (PrintWriter writer = new PrintWriter(new File("data", "module.html"))) {
            writer.println(page.render());
        }

	}

}
