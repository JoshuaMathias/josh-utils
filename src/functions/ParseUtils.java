package functions;
import java.util.ArrayList;
import java.util.List;

/*
 * Functions for processing text.
 */
public class ParseUtils {
	static StringBuilder sb;
	
	/*
	 * Get a list of tokens separated by ' '.
	 * Doesn't include empty tokens.
	 */
	public static List<String> splitSpaces(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<String> words = new ArrayList<String>();
		for (char i=0; i<chars.length; i++) {
			if (chars[i] == ' ') {
				if (sb.length() > 0) {
					words.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(chars[i]);
			}
		}
		sb.setLength(0);
		return words;
	}
	
	public static List<String> splitLines(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<String> words = new ArrayList<String>();
		for (char i=0; i<chars.length; i++) {
			if (chars[i] == '\n') {
				if (sb.length() > 0) {
					words.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(chars[i]);
			}
		}
		sb.setLength(0);
		return words;
	}
	
	public static List<String> splitTabs(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<String> words = new ArrayList<String>();
		for (char i=0; i<chars.length; i++) {
			if (chars[i] == '\t') {
				if (sb.length() > 0) {
					words.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(chars[i]);
			}
		}
		sb.setLength(0);
		return words;
	}
	
	/*
	 * Get a list of all words separated by \n and ' '
	 */
	public static List<String> splitWordsLines(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<String> words = new ArrayList<String>();
		for (char i=0; i<chars.length; i++) {
			if (chars[i] == ' ' || chars[i] == '\n') {
				if (sb.length() > 0) {
					words.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(chars[i]);
			}
		}
		sb.setLength(0);
		return words;
	}
	
	/*
	 *  Get a list of lines represented by a list of words in each line.
	 *  Doesn't include empty lines.
	 */
	public static List<List<String>> splitLinesWords(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<List<String>> lines = new ArrayList<List<String>>();
		List<String> words = new ArrayList<String>();
		for (char i=0; i<chars.length; i++) {
			if (chars[i] == '\n') {
				if (sb.length() > 0) {
					words.add(sb.toString());
				}
				if (words.size() > 0) {
					lines.add(words);
					System.out.println("new line: "+words);
				}
				words = new ArrayList<String>();
				sb.setLength(0);
			} else if (chars[i] == ' ') {
				if (sb.length() > 0) {
					words.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(chars[i]);
			}
		}
		sb.setLength(0);
		return lines;
	}
	
	// Returns the Unicode value of a char.
	// Example: \u002e
	public static String getUnicode(char ch) {
		return "\\u" + Integer.toHexString(ch | 0x10000).substring(1);
	}
	
}