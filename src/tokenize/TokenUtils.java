package tokenize;

/*
 * Functions for tokenizing.
 */
public class TokenUtils {
	
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
	
	// Return a String containing only letters. Replace space and other characters with a single space.
	// Lowercase
	public static String tokenizeFeatures(String text) {
		text = text.replaceAll("[^a-zA-Z]", " ");
		text = text.toLowerCase();
		return text;
	}

}
