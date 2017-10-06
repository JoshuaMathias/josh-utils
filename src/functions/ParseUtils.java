package functions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		return splitChar(text, ' ');
	}
	
	public static List<String> splitLines(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<String> tokens = new ArrayList<String>();
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == '\n' || chars[i] == '\r') {
				if (sb.length() > 0) {
					tokens.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(chars[i]);
			}
		}
		if (sb.length() > 0) {
			tokens.add(sb.toString());
		}
		sb.setLength(0);
		return tokens;
	}
	
	public static List<String> splitTabs(String text) {
		return splitChar(text, '\t');
	}
	
	public static List<String> splitChar(String text, char ch) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<String> tokens = new ArrayList<String>();
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == ch) {
				if (sb.length() > 0) {
//					System.out.println("Adding token "+sb.toString());
					tokens.add(sb.toString());
				}
				sb.setLength(0);
			} else {
				sb.append(chars[i]);
			}
		}
		if (sb.length() > 0) {
			tokens.add(sb.toString());
		}
		sb.setLength(0);
		return tokens;
	}
	
	/*
	 * Get a list of all words separated by whitespace
	 */
	public static List<String> splitSpace(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		List<String> words = new ArrayList<String>();
		for (int i=0; i<chars.length; i++) {
			if (Character.isWhitespace(chars[i])) {
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
		for (int i=0; i<chars.length; i++) {
			if (Character.getType(chars[i]) == Character.LINE_SEPARATOR) {
				if (sb.length() > 0) {
					words.add(sb.toString());
				}
				if (words.size() > 0) {
					lines.add(words);
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
	
	/*
	 * Return only the part of the String before the given character is found, where there's a space before the character.
	 */
	public static String trimComments(String text, char ch) {
		char[] chars = text.toCharArray();
		boolean foundTab = true; // Comment could start at beginning of line.
		if (sb == null) {
			sb = new StringBuilder();
		}
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == '\t') {
				foundTab = true;
			} else if (foundTab) {
				if (chars[i] == ch) {
					break;
				}
				foundTab = false;
			}
			sb.append(chars[i]);
		}
		String trimmed = sb.toString();
		sb.setLength(0);
		return trimmed;
	}
	
	/*
	 * Remove the last character if it is the given char.
	 */
	public static String trimEnd(String text, char ch) {
		int len = text.length();
		if (text.length() > 0 && text.charAt(len-1) == ch) {
			if (len > 1) {
				return text.substring(0,text.length()-2);
			} else {
				return "";
			}
		}
		return text;
	}
	
	public static String mapToString(Map<String, Integer> map) {
		String mapStr = "";
		for (Entry<String, Integer> entry : map.entrySet()) {
			mapStr += entry.getKey()+"\t"+entry.getValue()+"\n";
		}
		return mapStr;
	}
	
	public static String mapToStringDouble(Map<String, Double> map) {
		String mapStr = "";
		for (Entry<String, Double> entry : map.entrySet()) {
			mapStr += entry.getKey()+"\t"+entry.getValue()+"\n";
		}
		return mapStr;
	}
}