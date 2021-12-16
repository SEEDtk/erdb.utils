/**
 *
 */
package org.theseed.erdb.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.java.erdb.DbConnection;
import org.theseed.java.erdb.DbTable;
import org.theseed.java.erdb.DbTable.Field;
import org.theseed.utils.ParseFailureException;

import j2html.tags.ContainerTag;
import j2html.tags.DomContent;

import static j2html.TagCreator.*;

/**
 * This command creates an HTML web page containing a diagram of an ERDB database.  This includes
 * an actual picture drawn with SVG and sections about each table.
 *
 * Note that at this time we cannot handle recursive relationships, including ones that are implemented
 * via relationship tables.  This will be fixed when I learn how to control bezier curves.
 *
 * There are no positional parameters.  The HTML is produced on the standard output.
 *
 * The following command-line options are supported.
 *
 * -h	display command-line usage
 * -v	display more frequent log messages
 * -o	html output file (if not STDOUT)
 * -p	font size in points (default 12)
 * -m	margin around the diagram
 *
 * --type		type of database (default SQLITE)
 * --dbfile		database file name (SQLITE only)
 * --url		URL of database (host and name)
 * --parms		database connection parameter string (currently only MySQL)
 * --title		title of this database (default "Database Diagram")
 * --width		width of a rectangle (defeult 120)
 * --height		height of a rectangle (default 80)
 *
 * @author Bruce Parrello
 *
 */
public class DisplayProcessor extends BaseDbProcessor {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(DisplayProcessor.class);
    /** output print writer */
    private PrintWriter writer;
    /** style sheet URL */
    private String STYLE_SHEET = "https://core.theseed.org/SEEDtk/css/erdb.css";
    /** list of lines */
    private List<ContainerTag> lines;
    /** list of rectangle blocks */
    private List<ContainerTag> blocks;
    /** container tag for tables */
    private ContainerTag tableTables;
    /** map of table names to placements; note we must normalize table names to lower-case */
    private Map<String, DbTable.Placement> placeMap;
    /** non-breaking space for empty cells */
    private static final DomContent NBSP = rawHtml("&nbsp;");
    /** constant Y for flag columns */
    private static final DomContent YES_FLAG = text("Y");
    /** rectangle color */
    private static final String RECT_COLOR = "#00CCCC";

    // COMMAND-LINE OPTIONS

    /** output file */
    @Option(name = "--output", aliases = { "-o" }, usage = "output file (if not STDOUT)")
    private File outFile;

    /** height of an output rectangle */
    @Option(name = "--height", metaVar = "100", usage = "height for a table rectangle on the diagram")
    private int height;

    /** width of an output rectangle */
    @Option(name = "--width", metaVar = "150", usage = "width for a table rectangle on the diagram")
    private int width;

    /** padding around the diagram */
    @Option(name = "--margin", aliases = { "-m" }, metaVar = "20", usage = "padding (margin) around the diagram")
    private int margin;

    /** title of the database diagram */
    @Option(name = "--title", usage = "title for the output web page")
    private String title;

    /** font size in points for the diagram */
    @Option(name = "--points", aliases = { "-p" }, metaVar = "12", usage = "font size in points for rectangle titles")
    private int points;

    @Override
    protected void setDbDefaults() {
        this.outFile = null;
        this.height = 80;
        this.width = 120;
        this.points = 12;
        this.margin = 50;
        this.title = "Database Diagram";
    }

    @Override
    protected boolean validateParms() throws IOException, ParseFailureException {
        if (this.height <= 0)
            throw new ParseFailureException("Rectangle height must be positive.");
        if (this.width <= 0)
            throw new ParseFailureException("Rectangle width must be positive.");
        if (this.points <= 0)
            throw new ParseFailureException("Font size must be positive.");
        // Set up the output file.
        if (this.outFile == null) {
            log.info("Output will be to the standard output.");
            this.writer = new PrintWriter(System.out);
        } else {
            log.info("Output will be to {}.", this.outFile);
            this.writer = new PrintWriter(this.outFile);
        }
        return true;
    }

    @Override
    protected void runDbCommand(DbConnection db) throws Exception {
        // Get the list of tables names.
        log.info("Acquiring table list.");
        List<String> tables = db.getTableNames();
        log.info("{} tables in the database.", tables.size());
        // These will track the number of rows and columns we need.  The placement values are 1-based,
        // so the highest value is the one we keep.
        int rows = 0;
        int cols = 0;
        this.placeMap = new HashMap<String, DbTable.Placement>(tables.size() * 4 / 3);
        for (String table : tables) {
            DbTable tableDesc = db.getTable(table);
            DbTable.Placement tablePlace = tableDesc.getPlacement();
            rows = Math.max(tablePlace.getRow(), rows);
            cols = Math.max(tablePlace.getCol(), cols);
            this.placeMap.put(table.toLowerCase(), tablePlace);
        }
        log.info("Diagram has {} rows and {} columns.", rows, cols);
        // Create the table section.
        this.tableTables = div().withId("tables").with(h2("Table Listings"));
        // Create the diagram components.  Note we make do with a good estimate for the number of lines instead
        // of an upper bound.
        this.lines = new ArrayList<ContainerTag>(tables.size() * 2);
        this.blocks = new ArrayList<ContainerTag>(tables.size());
        // Now loop through the tables, creating the HTML.
        for (String table : tables)
            this.processTable(db, table);
        // Create the diagram.  The lines go first so they appear in back.
        ContainerTag diagram = new ContainerTag("svg").attr("width", cols * this.width + this.margin)
                .attr("height", rows * this.height + this.margin).with(this.lines).with(this.blocks);
        // It is time to assemble all of this.
        ContainerTag head = head().with(link().withRel("styleSheet").withHref(STYLE_SHEET))
                .with(title(this.title));
        ContainerTag page = html().with(head, body().with(h1(this.title), diagram, this.tableTables));
        writer.println(page.render());
        writer.flush();
    }

    /**
     * Product the diagram data for the specified table.  This includes plotting the table's rectangle,
     * drawing all of the outbound links, and creating the display table for the fields.
     *
     * @param table		name of the table to process
     *
     * @throws SQLException
     */
    private void processTable(DbConnection db, String table) throws SQLException {
        log.info("Processing table {}.", table);
        // Get the table's descriptor and placement.
        DbTable tableDesc = db.getTable(table);
        DbTable.Placement placement = this.placeMap.get(table.toLowerCase());
        // First, we build the table of fields.
        ContainerTag fieldTable = table().with(tr().with(th("Field"), th("Key?"), th("Type"), th("description")));
        // We process the primary key first.
        String primaryKey = tableDesc.getKeyName();
        if (primaryKey != null) {
            DbTable.Field primaryDesc = tableDesc.getField(primaryKey);
            fieldTable.with(this.fieldRow(primaryDesc, true));
        }
        // Now process the other fields.
        for (DbTable.Field field : tableDesc.getFields()) {
            if (! field.getName().equals(primaryKey))
                fieldTable.with(this.fieldRow(field, false));
        }
        ContainerTag section = div().with(a(h3(table)).withName(table), p(placement.getComment()),
                fieldTable);
        this.tableTables.with(section);
        // Produce the rectangle for the table.  The rectangle is managed by an inner SVG group.
        int x1 = this.margin + (placement.getCol() - 1) * this.width;
        int y1 = this.margin + (placement.getRow() - 1) * this.height;
        ContainerTag tableGroup = new ContainerTag("svg").attr("x", x1).attr("y", y1)
                .attr("width", this.width).attr("height", this.height).with(
                new ContainerTag("rect").attr("x", 0).attr("y", 0).attr("width", "100%").attr("height", "100%")
                        .attr("fill", RECT_COLOR).attr("stroke", "black").attr("stroke-width", 2),
                new ContainerTag("text").attr("x", "50%").attr("y", "50%").attr("font-size", this.points)
                        .attr("fill", "black").with(text(table)));
        this.blocks.add(a(tableGroup).withHref("#" + table));
        // Now we draw the lines.  Each line will start at our centerpoint.
        int x0 = x1 + this.width / 2;
        int y0 = y1 + this.height / 2;
        // Loop through the links.
        for (Map.Entry<String, DbTable.Link> link : tableDesc.getLinks()) {
            // Get the other endpoint, midway between us and the other table.
            DbTable.Placement otherPlace = this.placeMap.get(link.getKey());
            int x2 = (x1 + this.margin + otherPlace.getCol() * this.width) / 2;
            int y2 = (y1 + this.margin + otherPlace.getRow() * this.height) / 2;
            // Start the line.
            ContainerTag linkLine = new ContainerTag("line").attr("x1", x0).attr("y1", y0).attr("x2", x2)
                    .attr("y2", y2).attr("stroke", "black").attr("stroke-linecap", "round");
            // Now we need to determine the nature of the link.  If our field is NOT the primary key,
            // we are on the "many" side.  If the other field is nullable, we are conditional (1 or 0).
            // Otherwise, we are on the "one" side.
            DbTable.Link linker = link.getValue();
            if (! linker.getLocalField().equals(primaryKey)) {
                // A "many" half-link is a thick link.
                linkLine.attr("stroke-width", 7);
            } else {
                DbTable.Field otherField = db.getTable(link.getKey()).getField(linker.getOtherField());
                if (otherField.isNullable())
                    linkLine.attr("stroke-dasharray", "10,5");
                linkLine.attr("stroke-width", 1);
            }
            // Add the line to the line list.
            this.lines.add(linkLine);
        }
    }

    /**
     * Create the table row describing the indicated field
     *
     * @param field			field to process
     * @param isPrimary		TRUE if the field is the primary key
     *
     * @return the table row for this field
     */
    private ContainerTag fieldRow(Field field, boolean isPrimary) {
        ContainerTag retVal = tr().with(td(field.getName()),
                td(isPrimary ? YES_FLAG : NBSP),
                td(field.getType().toString()),
                td(field.getComment()));
        return retVal;
    }

}
