package main;

import shared.BigFile;
import functions.FileUtils;
import lm.HMM;

public class HMMTagger {

	/**
	 * Takes an HMM according to a specific format.
	 * @param args
	 * USAGE: input_hmm test_file output_file
	 */
	public static void main(String[] args) {
		String input_hmm_file = "";
		String test_file = "";
		String output_file = "";
		if (args.length > 2) {
			input_hmm_file = args[0];
			test_file = args[1];
			output_file = args[2];
		} else {
			System.out.println("USAGE: input_hmm test_file output_file");
			System.exit(0);
		}
//		BigFile bigFile = new BigFile();
//		bigFile.startTimer();
		HMM hmm = new HMM(input_hmm_file);
//		bigFile.printTime();
		hmm.tagFile(test_file, output_file);
//		bigFile.printTime();
		
	}

}
