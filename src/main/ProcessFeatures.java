package main;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import tokenize.TokenUtils;
import tokenize.Tokenizer;
import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

public class ProcessFeatures {

	/**
	 * USAGE: proc_file.sh input_file targetLabel output_file
	 * @param input_file targetLabel output_file
	 * 
	 */
	public static void main(String[] args) {
		String input_file = "";
		String targetLabel = "";
		String output_file = "";
		if (args.length > 2) {
			input_file = args[0];
			targetLabel = args[1];
			output_file = args[2];
		} else {
			System.out.println("USAGE: input_file targetLabel output_file");
			System.exit(0);
		}
		String text = FileUtils.readFile(input_file);
		text = TokenUtils.tokenizeFeatures(ParseUtils.getAfterRegex(text, "\n\\s*\n"));

		HashMap<String, Integer> tokenCounts = StatUtils.getTokenCounts(text);
		List<Entry<String, Integer>> sortedTokens = StatUtils.sortKeys(tokenCounts, false);
		StringBuilder outputStr = new StringBuilder();
		File inputFile = new File(input_file);

		outputStr.append(inputFile.getName()+" "+targetLabel);
		for (Entry<String, Integer> token : sortedTokens) {
			outputStr.append(" "+token.getKey()+" "+token.getValue());
		}
		FileUtils.writeFile(output_file, outputStr.toString()+"\n");
		
	}

}
