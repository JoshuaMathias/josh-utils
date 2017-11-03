package main;

import lm.LangModel;

public class Perplexity {

	/**
	 * Calculate and print the perplexity of a language model using test data.
	 * @param args
	 * USAGE: lm_file l1 l2 l3 test_data output_file
	 */
	public static void main(String[] args) {
		String lm_file = "";
		double l1 = 0.0;
		double l2 = 0.0;
		double l3 = 0.0;
		String test_data = "";
		String output_file = "";
		if (args.length > 5) {
			lm_file = args[0];
			l1 = Double.valueOf(args[1]);
			l2 = Double.valueOf(args[2]);
			l3 = Double.valueOf(args[3]);
			test_data = args[4];
			output_file = args[5];
		} else {
			System.out.println("USAGE: lm_file l1 l2 l3 test_data output_file");
			System.exit(0);
		}
		
		LangModel lm = new LangModel(lm_file);
		lm.writePerplexity(l1, l2, l3, test_data, output_file);

	}

}
