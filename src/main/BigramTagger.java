package main;

import lm.HMM;
import functions.FileUtils;

public class BigramTagger {

	/**
	 * Takes a training file with POS tags and outputs an HMM according to a specific format.
	 * @param args
	 * USAGE: output_hmm
	 * Training data is received from standard in.
	 */
	public static void main(String[] args) {
		String output_hmm_file = "";
		if (args.length > 0) {
			output_hmm_file = args[0];
		} else {
			System.out.println("USAGE: output_hmm");
			System.exit(0);
		}
		
//		String training_str = FileUtils.readIn();
		String training_str = FileUtils.readFile(args[1]);
		HMM hmm = new HMM(training_str, 2);
		FileUtils.writeFile(output_hmm_file, hmm.toString());
		
	}

}
