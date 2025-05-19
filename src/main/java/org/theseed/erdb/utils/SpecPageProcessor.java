/**
 *
 */
package org.theseed.erdb.utils;

import static j2html.TagCreator.body;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.link;
import static j2html.TagCreator.title;

import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.basic.ParseFailureException;
import org.theseed.io.LineReader;
import org.theseed.spec.ModuleNode;
import org.theseed.spec.SpecParser;
import org.theseed.utils.BaseTextProcessor;

import j2html.tags.ContainerTag;

/**
 * This command produces a web page describing a spec file. The spec file is read from the standard input and
 * the web page is produced on the standard output.
 *
 * The command-line options are as follows:
 *
 * -h	display command-line usage
 * -i	input file containing the genome spec (if not STDIN)
 * -o	output file for the web page (if not STDOUT)
 *
 * @author Bruce Parrello
 *
 */
public class SpecPageProcessor extends BaseTextProcessor {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(SpecPageProcessor.class);

    @Override
    protected void setTextDefaults() {
    }

    @Override
    protected void validateTextParms() throws IOException, ParseFailureException {
    }

    @Override
    protected void runPipeline(LineReader inputStream, PrintWriter writer) throws Exception {
        log.info("Initializing parser.");
        SpecParser parser = new SpecParser(inputStream);
        log.info("Creating module node.");
        ModuleNode modNode = new ModuleNode(parser);
        log.info("Building web page.");
        ContainerTag modHtml = modNode.toHtml();
        ContainerTag head = head().with(link().withRel("styleSheet").withHref(DisplayProcessor.STYLE_SHEET))
                .with(title(modNode.getName()));
        ContainerTag page = html().with(head, body().with(modHtml));
        log.info("Writing output.");
        writer.println(page.render());
    }

}
