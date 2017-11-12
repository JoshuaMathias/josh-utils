package main;

import lm.HMM;
import functions.FileUtils;

public class TrigramTagger {

	/**
	 * Takes a training file with POS tags and outputs an HMM according to a specific format.
	 * @param args
	 * USAGE: output_hmm l1 l2 l3 unk_prob_file
	 * Training data is received from standard in.
	 */
	public static void main(String[] args) {
		String output_hmm_file = "";
		double l1 = 0.0;
		double l2 = 0.0;
		double l3 = 0.0;
		if (args.length > 4) {
			output_hmm_file = args[0];
			l1 = Double.parseDouble(args[1]);
			l2 = Double.parseDouble(args[2]);
			l3 = Double.parseDouble(args[3]);
		} else {
			System.out.println("USAGE: output_hmm l1 l2 l3 unk_prob_file");
			System.exit(0);
		}
		
//		String training_str = FileUtils.readIn();
		String training_str = FileUtils.readFile(args[4]);
		HMM hmm = new HMM(training_str, 3);
		FileUtils.writeFile(output_hmm_file, hmm.toString());
		
	}

}
