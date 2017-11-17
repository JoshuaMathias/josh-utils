package main;

import lm.HMM;
import functions.FileUtils;

public class CheckHMM {

	/**
	 * Takes an HMM according to a specific format.
	 * @param args
	 * USAGE: input_hmm
	 */
	public static void main(String[] args) {
		String input_hmm_file = "";
		if (args.length > 0) {
			input_hmm_file = args[0];
		} else {
			System.out.println("USAGE: input_hmm");
			System.exit(0);
		}
		

		HMM hmm = new HMM(input_hmm_file);
		
	}

}
