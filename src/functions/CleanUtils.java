package functions;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Functions for cleaning or preparing text.
 */
public class CleanUtils {
	
	/*
	 * Normalize related characters.
	 */
	public static String normalizeChars(String text) {
		text = text.replaceAll("[“‟”＂❝❞]", "\""); //Use one standard quotation mark.
		text = text.replaceAll("[‘‛’❛❜’]", "'"); //Use one standard apostrophe.
		return text;
	}
	
	/*
	 * Use only one type of line break and space.
	 */
	public static String normalizeSpace(String text) {
		text = text.replaceAll("[\\t\\f\\x0B\u0020\u00A0\u1680\u180E\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u200B\u202F\u205F\u3000\uFEFF]", " ");
		text = text.replaceAll("[\\u000A\\u000D\u0085\u000B\u000C\u2028\u2029]", "\n");
		return text;
	}
	
	
	// Removes blank lines and replaces all whitespace with a single space.
	public static String rmBlankSpace(String text) {
		text = normalizeSpace(text);
		text = text.replaceAll("^\\s+|\\s+$|\\s*(\n)\\s*|(\\s)\\s*", "$1$2"); //Replace double spacing and other non-line spacing with one space.
		
		return text;
	}
	
	// Remove code or formatting syntax.
	public static String rmCode(String text) {
		text = text.replaceAll("<[^>\n]*>", " "); //Remove HTML and XML
		text = text.replaceAll("([^.@\\s]+)(\\.[^.@\\s]+)*@([^.@\\s]+\\.)+([^.@\\s]+)", " _ "); //Remove email addresses
		text = text.replaceAll("((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)", " "); //Remove URLs
		text = text.replaceAll("Off(Off)+", " "); //Text extracted from PDF files often contain the word Off for each checkbox.
		text = text.replaceAll("_(_)+", " _ "); //Remove fill in the blanks: ______
		text = text.replaceAll("(\\s|^)[^\\p{L}]{5,}(\\s|$)", " _ "); //Remove tokens with five or more characters that contain no letters (includes phone numbers).
		return text;
	}
}
