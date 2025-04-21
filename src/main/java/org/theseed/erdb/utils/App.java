package org.theseed.erdb.utils;
import java.util.Arrays;

import org.theseed.basic.BaseProcessor;

/**
 * Commands for ERDB utilities
 *
 * init		initialize a database from an SQL script file
 * display	display a web page describing a database
 * spec		display a web page describing a specification
 */
public class App
{
    public static void main( String[] args )
    {
        // Get the control parameter.
        String command = args[0];
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        BaseProcessor processor;
        // Determine the command to process.
        switch (command) {
        case "init" :
            processor = new InitProcessor();
            break;
        case "display" :
            processor = new DisplayProcessor();
            break;
        case "spec" :
        	processor = new SpecPageProcessor();
        	break;
        default:
            throw new RuntimeException("Invalid command " + command);
        }
        // Process it.
        boolean ok = processor.parseCommand(newArgs);
        if (ok) {
            processor.run();
        }
    }
}
