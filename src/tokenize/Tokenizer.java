package tokenize;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import functions.FileUtils;
import functions.ParseUtils;

public class Tokenizer {
	
	static String protectedFile = "resources/protected.en";
	Pattern protectedPattern;
	String text;
	char[] textChars;
	int numChars;
	StringBuilder newText;
	boolean wasSpace;
	char prevCh = 0;
	char currCh = 0;
	char nextCh = 0;
	String defaultAbbrevFile = "resources/abbrev-list";
	
	public static String getProtectedRegex(String abbrevStr) {
		String regex = "";
		regex = "(?:[^\\p{N}\\p{L}]|\\b)";
		regex += "(";
		// Get lines of protected regular expressions.
		List<String> regexLines = FileUtils.getResourceLines(protectedFile);
		for (int i=0; i<regexLines.size(); i++) {
			String justRegex = ParseUtils.trimComments(regexLines.get(i),'#').trim();
			if (justRegex.length() > 0) {
				regex += justRegex + '|';
			}
		}
		List<String> abbLines = ParseUtils.splitLines(abbrevStr);
		for (int i=0; i<abbLines.size(); i++) {
			String justRegex = ParseUtils.trimComments(abbLines.get(i),'#').trim();
			if (justRegex.length() > 0) {
				regex += justRegex + '|';
			}
		}
		regex = ParseUtils.trimEnd(regex, '|').trim();
		regex += ")";
//		regex += "(?:$|\\s)";
//		System.out.println("protected pattern: "+regex);
		return regex;
	}
	
	public Tokenizer(String abbrevFile) {
		protectedPattern = Pattern.compile(getProtectedRegex(FileUtils.readFile(abbrevFile)));
	}
	
	public Tokenizer() {
		protectedPattern = Pattern.compile(getProtectedRegex(FileUtils.readResource(defaultAbbrevFile)));
	}
	
	/*
	 * Tokenize unprotected non-alphanumeric characters with spaces.
	 */
	public String tokenize(String text) {
		this.text = text;
		newText = new StringBuilder();
		newText.ensureCapacity(text.length()*3);
		textChars = text.toCharArray();
		numChars = textChars.length;
		wasSpace = false;
		int currI = 0;
		text = tokenizeApostrophes(text);
		Matcher protectedM = protectedPattern.matcher(text);
		int groupNum = 1; // According to the pattern formed at getProtectedRegex()
		while (protectedM.find()) {
			
			String protectedStr = protectedM.group(groupNum);
			// Skip empty strings.
			if (protectedStr.length() < 1 || (protectedStr.length() == 1 && Character.isWhitespace(protectedStr.charAt(0)))) {
				continue;
			}
			appendInBetween(currI,protectedM.start(groupNum));
//			System.out.println("protected: "+protectedStr);
			char[] protectedChars = protectedStr.toCharArray();
			for (int i=0; i<protectedStr.length(); i++) {
				char ch = protectedChars[i];
				if (Character.isWhitespace(ch)) {
					if (ch == '\n' || ch == '\r') {
						newText.append(ch);
					} else {
						if (!wasSpace) {
							newText.append(ch);
						}
					} 
					wasSpace = true;
				} else {
					newText.append(ch);
					if (wasSpace) {
						wasSpace = false;
					}
				}
			}
			currI = protectedM.end(groupNum);
		}
		appendInBetween(currI,numChars);
		String tokText = ParseUtils.trimEnd(newText.toString(),' ');
		return tokText;
	}
	
	/*
	 * Go through the text unprotected by regex 
	 */
	void appendInBetween(int start, int end) {
//		System.out.println("AppendInBetween: "+start+" to "+end+": "+text.substring(start, end));
		if (start > 0) {
			currCh = textChars[start-1];
		}
		if (start < end) {
			nextCh = textChars[start];
		} else {
			return;
		}
		for (int i=start+1; i<=end; i++) {
			prevCh = currCh;
			currCh = nextCh;
			if (i==numChars) {
				nextCh = ' ';
			} else {
				nextCh = textChars[i];
			}
//			System.out.println("prevCh: "+prevCh+" currCh: "+currCh+" nextCh: "+nextCh);
			int currType = Character.getType(currCh);
			if (Character.isWhitespace(currCh)) {
				if (currCh == '\n' || currCh == '\r') {
					newText.append(currCh);
				} else {
					if (!wasSpace) {
						newText.append(' ');
					}
				}
				wasSpace = true;
			} else if (!Character.isLetter(currCh) && !Character.isDigit(currCh) && currType != Character.NON_SPACING_MARK) {
				if (currCh == '\'' || currCh == '’') { // Handle apostrophes
					if (prevCh == 's') {
						if (i > 2 && Character.isLetter(textChars[i-3])) {
							newText.append(currCh);
							if (wasSpace) {
								wasSpace = false;
							}
						} else {
							if (!wasSpace) {
								newText.append(' ');
							}
							newText.append(currCh);
							if (!Character.isWhitespace(nextCh)) {
								newText.append(' ');
							}
							wasSpace = true;
						}
					} else if (prevCh == 'n' && nextCh != 's') {
						if (i > 2 && Character.isLetter(textChars[i-3])) {
							newText.setLength(newText.length()-1); // Remove the 'n' that was already added.
							newText.append(' ');
							newText.append(prevCh);
							newText.append(currCh);
							if (wasSpace) {
								wasSpace = false;
							}
						} else {
							if (!wasSpace) {
								newText.append(' ');
							}
							newText.append(currCh);
							if (nextCh != '\n') {
								newText.append(' ');
							}
							wasSpace = true;
						}
					} else if (Character.isLetter(prevCh)) {
						newText.append(' ');
						newText.append(currCh);
						if (wasSpace) {
							wasSpace = false;
						}
					} else {
						newText.append(currCh);
						if (wasSpace) {
							wasSpace = false;
						}
					}
				} else {
					if (!wasSpace) {
						newText.append(' ');
					}
					newText.append(currCh);
					if (nextCh != '\n' && nextCh != '\r') {
						newText.append(' ');
					}
					wasSpace = true;
				}
			} else {
				newText.append(currCh);
				if (wasSpace) {
					wasSpace = false;
				}
			}
		}
	}
	
	// Handle apostrophes.
	// Separate conjunctions such as he's -> he 's and isn't -> is n't. Use only English characters for this.
	// This function is for show. It describes in regex what appendInBetween() is doing more efficiently.
	public static String tokenizeApostrophes(String text) {
		text.replaceAll("(\\w+)n('|’)(([\\w]&&[^s])\\w+)+", "($1 n$2$3");
		text.replaceAll("(\\w)+('|’)(\\w)+", "$1 $2$3");
		text.replaceAll("([^\\w\\s^])('|’)", "$1 $2");
		text.replaceAll("('|’)([^\\w\\s$])", "$1 $2");
		return text;
	}
	
	// Read from standard in and write the tokenized text to standard out.
	public static void main(String[] args) {
		String abbrevFile = "";
		if (args.length > 0) {
			abbrevFile = args[0];
		} else {
			System.err.println("Enter abbreviation file.");
			return;
		}
		Tokenizer tokenizer = new Tokenizer(abbrevFile);

		String origText = FileUtils.readIn();
		
		String tokText = tokenizer.tokenize(origText);
		System.out.println(tokText);
	}
}
