package functions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import shared.KeysValues;

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
					sb.setLength(0);
				}
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
	
	/*
	 * Return only the part of the String before the nth occurrence of char ch.
	 */
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
	 * Return only the part of the String after the nth occurrence of char ch.
	 */
	public static String getAfterNChar(String text, char ch, int n) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		int chCount = 0;
		int i=0;
		for (; i<chars.length; i++) {
			if (chars[i] == ch) {
				chCount++;
				if (chCount >= n) {
					break;
				}
			}
		}
		for (; i<chars.length; i++) {
			sb.append(chars[i]);
		}
		String tokens = sb.toString();
		sb.setLength(0);
		return tokens;
	}
	
	/*
	 * Return only the part of the String after the first occurrence of the given String.
	 */
	public static String getAfterString(String text, String str) {
		int foundI = text.indexOf(str);
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		for (int i=foundI+1; i<chars.length; i++) {
			sb.append(chars[i]);
		}
		String tokens = sb.toString();
		sb.setLength(0);
		return tokens;
	}
	
	/*
	 * Return only the part of the String after the first occurrence of the given Regular expression.
	 */
	public static String getAfterRegex(String text, String regex) {
		Matcher textMatcher = Pattern.compile(regex).matcher(text);
		textMatcher.find();
		int foundI = textMatcher.end();
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		for (int i=foundI; i<chars.length; i++) {
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
	public static List<String> splitWhitespace(String text) {
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
		if (sb.length() > 0) {
			words.add(sb.toString());
		}
		sb.setLength(0);
		return words;
	}
	
	/*
	 * Get an array with two elements, split by the last instance of the given character.
	 */
	public static String[] splitLastChar(String text, char ch) {
		char[] chars = text.toCharArray();
		String[] splitText = null;
		for (int i=chars.length-1; i>=0; i--) {
			if (chars[i] == ch) {
				splitText = new String[2];
				splitText[0]=text.substring(0,i);
				if (chars.length>i+1) {
					splitText[1]=text.substring(i+1);
				} else {
					splitText[1]="";
				}
				break;
			}
		}
//		System.out.println("splitText: "+splitText[0]+" "+splitText[1]);
		if (splitText == null) {
			return new String[]{text};
		}
		return splitText;
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
	 *  Split by whitespace
	 */
	public static List<List<String>> splitLinesWhitespace(String text) {
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
			} else if (Character.isWhitespace(chars[i]) || chars[i] == ' ') {
				if (sb.length() > 0) {
//					System.out.println("Adding word: "+sb.toString());
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
		if (sb.length() > 0) {
			if (sb.length() > 0) {
				words.add(sb.toString());
			}
			sb.setLength(0);
		}
		if (words.size() > 1) {
//			System.out.println("Adding line: "+words);
			words.add("</s>");
			lines.add(words);
		}
		sb.setLength(0);
		return lines;
	}
	
	/*
	 *  Get a list of lines represented by a list of words in each line.
	 *  Doesn't include empty lines.
	 *  Add <s> at the beginning of each line and </s> at the end of each line.
	 */
	public static List<List<String[]>> getLinesAsPOSSentences(String text) {
		StringBuilder sentenceSB = new StringBuilder();
		char[] chars;
		chars = text.toCharArray();
		List<List<String[]>> lines = new ArrayList<List<String[]>>();
		List<String[]> words = new ArrayList<String[]>();
		words.add(new String[] {"<s>","BOS"});
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == '\n' || chars[i] == '\r') {
				if (sentenceSB.length() > 0) {
//					System.out.println("Adding word: "+sentenceSB.toString());
					String word = sentenceSB.toString();
					String[] splitWord = splitLastChar(word, '/');
					if (splitWord.length > 1) {
//						splitWord[0] = escapeChar(splitWord[0], '/');
						words.add(splitWord);
					} else {
						words.add(new String[] {word, word});
					}
				}
				if (words.size() > 1) {
//					System.out.println("Adding line: "+words);
					words.add(new String[] {"<\\/s>","EOS"});
					lines.add(words);
				}
				words = new ArrayList<String[]>();
				words.add(new String[] {"<s>","BOS"});
				sentenceSB.setLength(0);
			} else if (chars[i] == ' ') {
				if (sentenceSB.length() > 0) {
					String word = sentenceSB.toString();
					String[] splitWord = splitLastChar(word, '/');
					if (splitWord.length > 1) {
//						splitWord[0] = escapeChar(splitWord[0], '/');
						words.add(splitWord);
					} else {
						words.add(new String[] {word, word});
					}
				}
				sentenceSB.setLength(0);
			} else {
				sentenceSB.append(chars[i]);
			}
		}
		if (sentenceSB.length() > 0) {
			String word = sentenceSB.toString();
			String[] splitWord = splitLastChar(word, '/');
			if (splitWord.length > 1) {
				splitWord[0] = escapeChar(splitWord[0], '/');
				words.add(splitWord);
			} else {
				words.add(new String[] {word, word});
			}
		}
		if (words.size() > 1) {
			words.add(new String[] {"<\\/s>","EOS"});
//			System.out.print("Adding line: ");
//			for (String[] token : words) {
//				System.out.print("("+token[0]+","+token[1]+")"+" ");
//			}
//			System.out.println();
			lines.add(words);
		}
		sentenceSB.setLength(0);
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
	 * Character analysis
	 */
	public static boolean containsDigit(String text) {
		char[] chars = text.toCharArray();
		for (int i=0; i<chars.length; i++) {
			if (Character.isDigit(chars[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsUppercase(String text) {
		char[] chars = text.toCharArray();
		for (int i=0; i<chars.length; i++) {
			if (Character.isUpperCase(chars[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsHyphen(String text) {
		return containsChar(text, '-');
	}
	
	public static boolean containsChar(String text, char ch) {
		char[] chars = text.toCharArray();
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == ch) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Escaping
	 */
	// Puts '\' before every instance of a given character in the given text.
	public static String escapeChar(String text, char ch) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		char[] chars = text.toCharArray();
		if (sb == null) {
			sb = new StringBuilder();
		}
		char prevCh = 0;
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == ch) {
				if (prevCh != '\\') { // Only escape if the character isn't already escaped.
					sb.append('\\'); // Escape the character
				}
			}
			sb.append(chars[i]);
			prevCh = chars[i];
		}
		String newText = sb.toString();
		sb.setLength(0);
		return newText;
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
	
	public static String arrayToString(String[] arr) {
		StringBuilder listStr = new StringBuilder();
		for (String token : arr) {
			listStr.append(token+" ");
		}
		listStr.setLength(listStr.length()-1);
		return listStr.toString();
	}
	
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
			mapStr.append(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		return mapStr.toString();
	}
	
	public static String mapToStringDouble(Map<String, Double> map) {
		StringBuilder mapStr = new StringBuilder();
		for (Entry<String, Double> entry : map.entrySet()) {
			mapStr.append(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		return mapStr.toString();
	}
	
	public static String mapToString2D(Map<String, Map<String, Integer>> map) {
		StringBuilder mapStr = new StringBuilder();
		for (Map<String, Integer> entryMap : map.values()) {
			for (Entry<String, Integer> entry : entryMap.entrySet()) {
				mapStr.append(entry.getKey()+"\t"+entry.getValue()+"\n");
			}
		}
		return mapStr.toString();
	}
	
	public static String mapToStringDouble2D(Map<String, Map<String, Double>> map) {
		StringBuilder mapStr = new StringBuilder();
		for (Entry<String, Map<String, Double>> entryMap : map.entrySet()) {
			for (Entry<String, Double> entry : entryMap.getValue().entrySet()) {
				mapStr.append(entryMap.getKey()+"\t"+entry.getKey()+"\t"+entry.getValue()+"\n");
			}
		}
		return mapStr.toString();
	}
	
	public static String listEntriesToString(List<Entry<String, Integer>> listEntries) {
		StringBuilder listStr = new StringBuilder();
		for (Entry<String, Integer> entry : listEntries) {
			listStr.append(entry.getKey()+"\t"+entry.getValue()+"\n");
		}
		return listStr.toString();
	}
	
	/*
	 * valueFirst: If true, list the value and then the key on each line.
	 */
	public static String listEntriesToString(List<Entry<String, Integer>> listEntries, boolean valueFirst) {
		if (valueFirst) {
			return listEntriesToStringValueKey(listEntries);
		} else {
			return listEntriesToString(listEntries);
		}
	}
	
	/*
	 * valueFirst: If true, list the value and then the key on each line.
	 */
	public static String listEntriesToStringValueKey(List<Entry<String, Integer>> listEntries) {
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
				List<String> splitLine = splitTabs(lines.get(i));
				if (splitLine.size() > 1) {
					entries.put(splitLine.get(keyIndex), Double.parseDouble(splitLine.get(valueIndex)));
				}
			} catch (NumberFormatException e) {
				System.out.println("Skipping entry "+i+": "+lines.get(i));
			}
		}
		return entries;
	}
	
	/*
	 * text: lines, where the items in each line are delimited by whitespace.
	 * Returns a LinkedHashMap where the first word in each line is the key, and the value is a List of Doubles
	 * (all the other items on the line).
	 */
	public static Map<String, List<Double>> stringToMapList(String text) {
		List<String> lines = splitLines(text);
		int keyIndex = 0;
		HashMap<String, List<Double>> entries = new HashMap<String, List<Double>>();
		for (int i=0; i<lines.size(); i++) {
			try {
				List<String> splitLine = splitWhitespace(lines.get(i));
				if (splitLine.size() > 0) {
					List<Double> values = new ArrayList<Double>();
					for (int wordI=1; wordI<splitLine.size(); wordI++) {
						values.add(Double.parseDouble(splitLine.get(wordI)));
					}
					entries.put(splitLine.get(keyIndex), values);
				}
			} catch (NumberFormatException e) {
				System.out.println("Skipping entry "+i+": "+lines.get(i));
			}
		}
		return entries;
	}
	
	/*
	 * text: lines, where the items in each line are delimited by whitespace.
	 * Returns a KeysValues object, containing a list of String keys and a 
	 * corresponding list of a list of Doubles.
	 */
	public static KeysValues<String, List<Double>> stringToTwoLists(String text) {
		List<String> lines = splitLines(text);
		int keyIndex = 0;
		List<String> keys = new ArrayList<String>();
		List<List<Double>> values = new ArrayList<List<Double>>();
		for (int i=0; i<lines.size(); i++) {
			try {
				List<String> splitLine = splitWhitespace(lines.get(i));
				if (splitLine.size() > 0) {
					List<Double> lineValues = new ArrayList<Double>();
					for (int wordI=1; wordI<splitLine.size(); wordI++) {
						lineValues.add(Double.parseDouble(splitLine.get(wordI)));
					}
					keys.add(splitLine.get(keyIndex));
					values.add(lineValues);
				}
			} catch (NumberFormatException e) {
				System.out.println("Skipping entry "+i+": "+lines.get(i));
			}
		}
		
		return new KeysValues<String, List<Double>>(keys,values);
	}
	
	public static KeysValues<String, double[]> stringToListArray(String text) {
		List<String> lines = splitLines(text);
		int keyIndex = 0;
		List<String> keys = new ArrayList<String>();
		List<double[]> values = new ArrayList<double[]>();
		for (int i=0; i<lines.size(); i++) {
			try {
				List<String> splitLine = splitWhitespace(lines.get(i));
				if (splitLine.size() > 0) {
					double[] lineValues = new double[splitLine.size()-1];
					for (int wordI=1; wordI<splitLine.size(); wordI++) {
						lineValues[wordI-1] = Double.parseDouble(splitLine.get(wordI));
					}
					keys.add(splitLine.get(keyIndex));
					values.add(lineValues);
				}
			} catch (NumberFormatException e) {
				System.out.println("Skipping entry "+i+": "+lines.get(i));
			}
		}
		return new KeysValues<String, double[]>(keys,values);
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