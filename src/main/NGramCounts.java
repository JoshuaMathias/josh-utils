package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

public class NGramCounts {

	/**
	 * Write the unigrams, bigrams, and trigrams of a given file to a given output file.
	 * @param args
	 * USAGE: training_data ngram_count_file [max_order]
	 */
	public static void main(String[] args) {
		String training_data = "";
		String ngram_count_file = "";
		int max_order = 3;
		if (args.length > 1) {
			training_data = args[0];
			ngram_count_file = args[1];
		} else {
			System.out.println("USAGE: training_data ngram_count_file [max_order]");
			System.exit(0);
		}
		if (args.length > 2) {
			try {
				max_order = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				System.out.println("Error using max_order: "+max_order);
				e.printStackTrace();
			}
		}
		
		List<List<Entry<String, Integer>>> gramCounts = StatUtils.getNGramCounts(FileUtils.readFile(training_data), max_order);
		BufferedWriter countWriter = FileUtils.getWriter(ngram_count_file);
		try {
			for (List<Entry<String, Integer>> gramMap : gramCounts) {
				countWriter.write(ParseUtils.listEntriesToString(gramMap));
			}
			countWriter.close();
		} catch (IOException e) {
			System.out.println("Error writing counts");
			e.printStackTrace();
		}
	}
}
