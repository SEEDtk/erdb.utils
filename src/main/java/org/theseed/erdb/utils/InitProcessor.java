/**
 *
 */
package org.theseed.erdb.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.theseed.utils.BaseProcessor;
import org.theseed.utils.ParseFailureException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.theseed.java.erdb.DbConnection;

/**
 * This command connects to a database and initializes its schema from an SQL file.
 *
 * The positional parameter will be the name of the SQL file.  The following command-line options are
 * supported.
 *
 * -h	display command-line usage
 * -v	display more frequent log messages
 *
 * --type		type of database (default SQLITE)
 * --dbfile		database file name (SQLITE only)
 *
 * @author Bruce Parrello
 *
 */
public class InitProcessor extends BaseProcessor implements DbConnection.IParms {

    // FIELDS
    /** logging facility */
    protected static Logger log = LoggerFactory.getLogger(InitProcessor.class);
    /** database connection */
    private DbConnection db;

    // COMMAND-LINE OPTIONS

    /** database engine type */
    @Option(name = "--type", usage = "type of database engine")
    private DbConnection.Type dbEngine;

    /** name of file containing the database */
    @Option(name = "--dbfile", metaVar = "sqlite.db", usage = "name of the database file (for SQLITE)")
    private File dbFile;

    /** name of the SQL input file */
    @Argument(index = 0, metaVar = "initialize.sql", usage = "SQL file to create the tables")
    private File sqlFile;

    @Override
    protected void setDefaults() {
        this.dbEngine = DbConnection.Type.SQLITE;
        this.dbFile = null;
    }

    @Override
    protected boolean validateParms() throws IOException, ParseFailureException {
        // Verify that the SQL file is readable.
        if (! this.sqlFile.canRead())
            throw new FileNotFoundException("SQL file " + this.sqlFile + " is not found or unreadable.");
        // Create the database.
        try {
            this.db = this.dbEngine.create(this);
        } catch (SQLException e) {
            throw new IOException("SQL Error: " + e.toString());
        }
        return true;
    }

    @Override
    protected void runCommand() throws Exception {
        // First, we must drop all the current tables.
        log.info("Removing current tables.");
        this.db.clearTables();
        // Now, run the SQL statements.
        log.info("Executing initialization script from {}.", this.sqlFile);
        this.db.scriptUpdate(this.sqlFile);
        // Report on the number of tables in the database.
        log.info("{} tables in database.", this.db.getTableNames().size());
    }

    @Override
    public File getDbFile() {
        return this.dbFile;
    }

}
