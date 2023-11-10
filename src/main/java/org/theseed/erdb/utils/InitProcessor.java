/**
 *
 */
package org.theseed.erdb.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.kohsuke.args4j.Argument;
import org.theseed.basic.ParseFailureException;
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
public class InitProcessor extends BaseDbProcessor  {

    // COMMAND-LINE OPTIONS

    /** name of the SQL input file */
    @Argument(index = 0, metaVar = "initialize.sql", usage = "SQL file to create the tables")
    private File sqlFile;

    @Override
    protected void setDbDefaults() {
    }

    @Override
    protected boolean validateParms() throws IOException, ParseFailureException {
        // Verify that the SQL file is readable.
        if (! this.sqlFile.canRead())
            throw new FileNotFoundException("SQL file " + this.sqlFile + " is not found or unreadable.");
        return true;
    }

    @Override
    protected void runDbCommand(DbConnection db) throws Exception {
        // First, we must drop all the current tables.
        log.info("Removing current tables.");
        db.clearTables();
        // Now, run the SQL statements.
        log.info("Executing initialization script from {}.", this.sqlFile);
        db.scriptUpdate(this.sqlFile);
        // Report on the number of tables in the database.
        log.info("{} tables in database.", db.getTableNames().size());
    }

 }
