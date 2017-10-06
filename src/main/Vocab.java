package main;

import tokenize.Tokenizer;
import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;


// Class for making vocabulary.
public class Vocab {
	// Read from standard in and write the vocab to standard out.
	public static void main(String[] args) {
		String inFile = null;
		if (args.length > 0) {
			inFile = args[0];
		}
		String text = "";
		if (inFile == null) {
			text = FileUtils.readStdIn();
		} else {
			text = FileUtils.readFile(inFile);
		}
		String vocabStr = ParseUtils.mapToString(StatUtils.sortHashMapByValues(StatUtils.getTokenCounts(text), false));
		if (inFile == null) {
			System.out.println(vocabStr);
		} else {
			FileUtils.writeFile(inFile+".voc.test", vocabStr);
		}
	}
}
