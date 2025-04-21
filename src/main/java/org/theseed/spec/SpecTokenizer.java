/**
 *
 */
package org.theseed.spec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.theseed.io.LineReader;

/**
 * This object presents an input stream as a sequence of specification tokens. It takes a line
 * input reader as input and produces the token stream as output.
 *
 * We track our current position in the input file. The next available character after a method
 * call is always non-white. We start by skipping whitespace, and each time we consume a token
 * we skip all trailing whitespace.
 *
 * @author Bruce Parrello
 *
 */
public class SpecTokenizer implements Iterator<SpecToken>, Iterable<SpecToken> {

	// FIELDS
	/** current input stream */
	private LineReader inStream;
	/** current line number */
	private int lineNum;
	/** current line buffer */
	private String currentLine;
	/** position of next character in line buffer */
	private int pos;
	/** tab width */
	protected static String TAB_STRING = StringUtils.repeat(' ', 8);

	/**
	 * This object represents a comment line during comment parsing.
	 */
	protected class CommentLine {

		/** starting column */
		private int col;
		/** comment text */
		private String text;

		/**
		 * Construct a comment line from the current text.
		 */
		public CommentLine() {
			// Skip leading whitespace.
			SpecTokenizer.this.skipLeadingWhite();
			// If we are on an asterisk, skip over any following space. If we are
			// on a comment terminator, stay on the asterisk.
			char ch = SpecTokenizer.this.curr();
			if (ch == '*') {
				SpecTokenizer.this.pos++;
				if (! SpecTokenizer.this.eol()) {
					ch = SpecTokenizer.this.curr();
					if (ch == ' ')
						SpecTokenizer.this.pos++;
					else if (ch == '/')
						SpecTokenizer.this.pos--;
				}
			}
			this.col = SpecTokenizer.this.pos;
			while (! SpecTokenizer.this.eol() && ! SpecTokenizer.this.stringFound("*/"))
				SpecTokenizer.this.pos++;
			// Note we strip trailing whitespace.
			this.text = StringUtils.stripEnd(SpecTokenizer.this.currentLine.substring(this.col, SpecTokenizer.this.pos),
					null);
		}

		/**
		 * @return the starting column index
		 */
		public int getCol() {
			return this.col;
		}

		/**
		 * @return the comment line text
		 */
		public String getText() {
			return this.text;
		}

		/**
		 * @return TRUE if this comment line is blank
		 */
		public boolean isEmpty() {
			return this.text.isEmpty();
		}

	}

	/**
	 * Construct a spec tokenizer.
	 *
	 * @param reader	line reader for the input file
	 */
	public SpecTokenizer(LineReader reader) {
		this.inStream = reader;
		// We need to position on the first token.
		if (! inStream.hasNext()) {
			// The input file is empty. Set up a dummy buffer.
			this.currentLine = "";
			this.pos = 0;
			this.lineNum = 1;
		} else {
			// Here we have data in the input file. Prime the buffer with the first line and
			// eat the starting whitespace.
			this.currentLine = reader.next();
			this.pos = 0;
			this.lineNum = 1;
			this.skipWhite();
		}
	}

	/**
	 * Check to see if the specified string exists at the current position in the current line.
	 *
	 * @param string	string we want to find
	 *
	 * @return TRUE if we find the string here, else FALSE
	 */
	public boolean stringFound(String string) {
		final int end = this.pos + string.length();
		return (end <= this.currentLine.length() &&
				string.contentEquals(this.currentLine.subSequence(this.pos, end)));
	}

	/**
	 * Move forward until we run out of white space.
	 */
	private void skipWhite() {
		boolean stillWhite = true;
		while (stillWhite) {
			if (this.eol())
				stillWhite = this.getNextLine();
			else if (Character.isWhitespace(this.curr()))
				this.pos++;
			else
				stillWhite = false;
		}
	}

	/**
	 * Move forward on the current line until we run out of white space.
	 */
	private void skipLeadingWhite() {
		while (! this.eol() && Character.isWhitespace(this.curr()))
			this.pos++;
	}

	/**
	 * @return the current character, or a space if we are at end-of-line
	 */
	private char curr() {
		char retVal;
		if (this.eol())
			retVal = ' ';
		else
			retVal = this.currentLine.charAt(this.pos);
		return retVal;
	}

	/**
	 * @return TRUE if we are at the end of the current line
	 */
	private boolean eol() {
		return this.pos >= this.currentLine.length();
	}

	/**
	 * Attempt to read a new data line.
	 *
	 * @return TRUE if successful, FALSE if we are at end-of-file
	 */
	private boolean getNextLine() {
		boolean retVal;
		if (! this.inStream.hasNext())
			retVal = false;
		else {
			this.readNextLine();
			retVal = true;
		}
		return retVal;
	}

	/**
	 * Read a new data line when we know one is available.
	 */
	private void readNextLine() {
		String buffer = inStream.next();
		this.lineNum++;
		this.pos = 0;
		// Fix the tabs.
		this.currentLine = StringUtils.replace(buffer, "\t", TAB_STRING);
	}

	@Override
	public Iterator<SpecToken> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		// Because of the whitespace policy, if we are at the end of a line,
		// we are also at the end of the file.
		return ! this.eol();
	}

	@Override
	public SpecToken next() {
		SpecToken retVal;
		// If we are at the end of a line, it's an error.
		if (this.eol())
			throw new NoSuchElementException();
		// Examine the current position to determine the type of token.
		char ch = this.curr();
		if (ch == '_' || Character.isLetter(ch))
			retVal = this.findWord(ch);
		else if (ch == '/')
			retVal = this.findComment();
		else
			retVal = this.checkDelim(ch);
		// Push past any whitespace at the current position.
		this.skipWhite();
		// Return the token itself.
		return retVal;
	}

	/**
	 * Create a token for the word at the current position. We consume everything up to the
	 * first delimiter. The delimiter can be whitespace or anything other than a letter,
	 * digit, or underscore.
	 *
	 * @param ch	character at the current position
	 *
	 * @return a spec token for the word
	 */
	private SpecToken findWord(char ch) {
		// Save the starting location.
		int pos0 = this.pos;
		// Push through all the identifier characters.
		while (ch == '_' || Character.isLetterOrDigit(ch)) {
			this.pos++;
			ch = this.curr();
		}
		// Insure we have a valid delimiter here.
		if (! this.isDelim(ch))
			this.throwParseError("Invalid character found.");
		// Extract the token text and create the token.
		String tokenText = this.currentLine.substring(pos0, this.pos);
		SpecToken retVal = new SpecToken(SpecToken.Type.WORD, tokenText);
		// Push past any whitespace at the current position.
		this.skipWhite();
		// Return the token itself.
		return retVal;
	}

	/**
	 * @return TRUE if the character is a valid word delimiter, else FALSE
	 *
	 * @param ch	character to check
	 */
	private boolean isDelim(char ch) {
		boolean retVal;
		switch (ch) {
		case '/' :
		case '<' :
		case '>' :
		case '(' :
		case ')' :
		case '{' :
		case '}' :
		case ',' :
		case ';' :
		case ' ' :
			retVal = true;
			break;
		default :
			retVal = false;
		}
		return retVal;
	}

	/**
	 * Here we are positioned on a slash, which means the start of a comment. We eat
	 * everything until we reach the comment terminator and collect the text with
	 * appropriate line breaks. The text formatting is based on the concept of a starting
	 * column. The starting column is the column position for the first non-white
	 * character in the first non-blank line. Note that if the first non-white character
	 * is an asterisk it does not go into the output, and the starting column follows
	 * the space right after the asterisk. Lines indented from the starting column get
	 * indented in the output. This is immensely crude.
	 *
	 * @return a spec token for the comment at the current position
	 */
	private SpecToken findComment() {
		// Verify that the slash is followed by an asterisk.
		if (! this.stringFound("/*"))
			this.throwParseError("Invalid use of slash.");
		// Push past any white space that follows the comment-open indicator.
		this.pos += 2;
		this.skipWhite();
		// We'll collect our comment text in here.
		List<CommentLine> lines = new ArrayList<CommentLine>();
		// This flag will be set when we find the comment terminator.
		boolean endFound = false;
		// This loop processes a line at a time. On the last line, it will stop after
		// the terminating slash.
		while (! endFound) {
			// Parse in the comment text from the current line.
			CommentLine newLine = this.new CommentLine();
			// Have we found the comment terminator?
			if (this.eol()) {
				// No, we must keep going.
				if (! this.inStream.hasNext())
					this.throwParseError("Unterminated comment.");
				this.readNextLine();
			} else {
				// Yes, we found the comment terminator.
				endFound = true;
				// Push past it.
				this.pos += 2;
			}
			// Add the comment line to the output.
			lines.add(newLine);
		}
		// Skip past leading white space.
		this.skipLeadingWhite();
		// Now we must assemble the comment. We first remove leading and trailing blank lines.
		while (! lines.isEmpty() && lines.getLast().isEmpty())
			lines.removeLast();
		while (! lines.isEmpty() && lines.getFirst().isEmpty())
			lines.removeFirst();
		// If no lines are left, then we return an empty comment.
		SpecToken retVal;
		if (lines.isEmpty())
			retVal = new SpecToken(SpecToken.Type.COMMENT, "");
		else {
			// Get the starting column from the first line.
			int startCol = lines.getFirst().getCol();
			// Now we build the comment. Adjacent lines that begin at the starting column are
			// joined. Blank lines start a new output group. Lines indented from the starting
			// column keep their leading spaces.
			List<String> commentParas = new ArrayList<String>(lines.size());
			StringBuilder current = new StringBuilder(80);
			for (CommentLine line : lines) {
				String text = line.getText();
				if (text.isEmpty()) {
					// A blank line terminates the previous comment group.
					commentParas.add(current.toString());
					current.setLength(0);
				} else {
					int col = line.getCol();
					if (col > startCol) {
						// An indented line terminates the previous comment group and puts in
						// the indent.
						commentParas.add(current.toString());
						current.setLength(0);
						current.append(StringUtils.repeat(' ', col - startCol));
					} else if (! current.isEmpty()) {
						// If we are joining lines, insert a space.
						current.append(' ');
					}
					// Store the comment text.
					current.append(text);
				}
			}
			// Insure we capture the residual.
			if (! current.isEmpty())
				commentParas.add(current.toString());
			// Join the comment pieces together with new-lines.
			retVal = new SpecToken(SpecToken.Type.COMMENT, StringUtils.join(commentParas, '\n'));
		}
		return retVal;
	}

	/**
	 * Create a token for a delimiter. The number of delimiters is small, and anything else
	 * will cause an exception.
	 *
	 * @param ch	character at the current position
	 *
	 * @return a token for the delimiter.
	 */
	private SpecToken checkDelim(char ch) {
		// Verify the delimiter is valid.
		if (! this.isDelim(ch))
			this.throwParseError("Invalid delimiter.");
		this.pos++;
		this.skipWhite();
		return new SpecToken(SpecToken.Type.DELIM, Character.toString(ch));
	}

	/**
	 * Throw a parsing error.
	 *
	 * @param msg	message to include about the error
	 */
	public void throwParseError(String msg) {
		throw new SpecParsingException(this.lineNum, this.pos, msg);
	}

	/**
	 * @return the current location in the input file
	 */
	public String location() {
		return Integer.toString(this.lineNum) + " col " + Integer.toString(pos + 1);
	}


}
