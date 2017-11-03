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
	 * Splitting Methods
	 */
	
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
	 * Split string by a given character, but stop after a certain number of entries are found.
	 */
	public static List<String> splitNChar(String text, char ch, int n) {
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
					if (tokens.size() >= n) {
						return tokens;
					}
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
	 * Get only n first words in the text, split by ' '.
	 */
	public static List<String> getNWords(String text, int n) {
		return splitNChar(text, ' ', n);
	}
	
	public static String getUntilNChar(String text, char ch, int n) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		int chCount = 0;
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == ch) {
				chCount++;
				if (chCount >= n) {
					String tokens = sb.toString();
					sb.setLength(0);
					return tokens;
				}
			}
			sb.append(chars[i]);
		}
		String tokens = sb.toString();
		sb.setLength(0);
		return tokens;
	}
	
	/*
	 * Get only n first words in the text, split by ' '.
	 * Return as a String.
	 */
	public static String getNWordsStr(String text, int n) {
		return getUntilNChar(text, ' ', n);
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
			if (chars[i] == '\n' || chars[i] == '\r') {
				if (sb.length() > 0) {
//					System.out.println("Adding word: "+sb.toString());
					words.add(sb.toString());
				}
				if (words.size() > 0) {
//					System.out.println("Adding line: "+words);
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
	
	/*
	 *  Get a list of lines represented by a list of words in each line.
	 *  Doesn't include empty lines.
	 *  Add <s> at the beginning of each line and </s> at the end of each line.
	 */
	public static List<List<String>> getLinesAsSentences(String text) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars;
		chars = text.toCharArray();
		List<List<String>> lines = new ArrayList<List<String>>();
		List<String> words = new ArrayList<String>();
		words.add("<s>");
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == '\n' || chars[i] == '\r') {
				if (sb.length() > 0) {
//					System.out.println("Adding word: "+sb.toString());
					words.add(sb.toString());
				}
				if (words.size() > 1) {
//					System.out.println("Adding line: "+words);
					words.add("</s>");
					lines.add(words);
				}
				words = new ArrayList<String>();
				words.add("<s>");
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
	
	/*
	 * Encoding
	 */
	
	// Returns the Unicode value of a char.
	// Example: \u002e
	public static String getUnicode(char ch) {
		return "\\u" + Integer.toHexString(ch | 0x10000).substring(1);
	}
	
	
	/*
	 * Trimming
	 */
	
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
	
	/*
	 * ToString methods.
	 */
	
	public static String listToString(List<String> list) {
		StringBuilder listStr = new StringBuilder();
		for (String token : list) {
			listStr.append(token+" ");
		}
		listStr.setLength(listStr.length()-1);
		return listStr.toString();
	}
	
	public static String mapToString(Map<String, Integer> map) {
		StringBuilder mapStr = new StringBuilder();
		for (Entry<String, Integer> entry : map.entrySet()) {
			mapStr.append(entry.getValue()+"\t"+entry.getKey()+"\n");
		}
		return mapStr.toString();
	}
	
	public static String mapToStringDouble(Map<String, Double> map) {
		StringBuilder mapStr = new StringBuilder();
		for (Entry<String, Double> entry : map.entrySet()) {
			mapStr.append(entry.getValue()+"\t"+entry.getKey()+"\n");
		}
		return mapStr.toString();
	}
	
	public static String listEntriesToString(List<Entry<String, Integer>> listEntries) {
		StringBuilder listStr = new StringBuilder();
		for (Entry<String, Integer> entry : listEntries) {
			listStr.append(entry.getValue()+"\t"+entry.getKey()+"\n");
		}
		return listStr.toString();
	}
	
	/*
	 * FromString methods.
	 */
	
	/*
	 * text: lines, where the columns in each line are delimited by tabs.
	 * Returns a HashMap that maps values to keys, using the specified index as the key column.
	 */
	public static HashMap<String, Double> stringToMap(String text, int keyIndex) {
		List<String> lines = splitLines(text);
		int valueIndex = 0;
		if (keyIndex == 0) {
			valueIndex = 1;
		}
		HashMap<String, Double> entries = new HashMap<String, Double>();
		for (int i=0; i<lines.size(); i++) {
			try {
				List<String> splitLines = splitTabs(lines.get(i));
				if (splitLines.size() > 1) {
					entries.put(splitLines.get(keyIndex), Double.parseDouble(splitLines.get(valueIndex)));
				}
			} catch (NumberFormatException e) {
				System.out.println("Skipping entry "+i+": "+lines.get(i));
			}
		}
		return entries;
	}
	
	/*
	 * text: lines, where the columns in each line are delimited by tabs.
	 * Returns a list of HashMaps that maps values to keys, using the specified index as the key column.
	 * Each map represents a gram. We assume the gram based on the number of words in the value column.
	 */
	public static List<HashMap<String, Double>> stringToNGramList(String text, int keyIndex) {
		List<String> lines = splitLines(text);
		int valueIndex = 0;
		if (keyIndex == 0) {
			valueIndex = 1;
		}
		List<HashMap<String, Double>> gramMaps = new ArrayList<HashMap<String, Double>>();
		
		for (int i=0; i<lines.size(); i++) {
			try {
				List<String> splitLine = splitTabs(lines.get(i));
				if (splitLine.size() > 1) {
					int n = StatUtils.getNumWords(splitLine.get(keyIndex));
					while (gramMaps.size() < n) {
						gramMaps.add(new HashMap<String, Double>());
					}
//					System.out.println("Adding entry for gram "+n+": "+splitLine.get(keyIndex));
					 gramMaps.get(n-1).put(splitLine.get(keyIndex), Double.parseDouble(splitLine.get(valueIndex)));
				}
			} catch (NumberFormatException e) {
				System.out.println("Skipping entry "+i+": "+lines.get(i));
			}
		}
		return gramMaps;
	}
	
	
}