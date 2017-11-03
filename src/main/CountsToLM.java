package main;

import java.util.HashMap;
import java.util.List;

import lm.LangModel;

import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

public class CountsToLM {

	/**
	 * Take an input file with ngram counts and output a language model file in the modified ARPA format.
	 * 
	 * @param args
	 * USAGE: ngram_count_file lm_file
	 */
	public static void main(String[] args) {
		String ngram_count_file = "";
		String lm_file = "";
		if (args.length > 1) {
			ngram_count_file = args[0];
			lm_file = args[1];
		} else {
			System.out.println("USAGE: ngram_count_file lm_file");
			System.exit(0);
		}

		LangModel.writeGramLM(ParseUtils.stringToNGramList(FileUtils.readFile(ngram_count_file), 1), lm_file);
		
		
	}

}
