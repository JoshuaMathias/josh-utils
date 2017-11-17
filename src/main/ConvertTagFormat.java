package main;

import functions.FileUtils;
import lm.HMM;

public class ConvertTagFormat {

	/**
	 * Takes an HMM according to a specific format.
	 * @param args
	 * USAGE: input_file output_file
	 */
	public static void main(String[] args) {
		String input_file = "";
		String output_file = "";
		if (args.length > 0) {
			input_file = args[0];
			if (args.length > 1) {
				output_file = args[1];
			}
		}
		
		String formattedFile = "";
		if (input_file.length() > 0) {
			formattedFile = HMM.convertFormat(FileUtils.readFile(input_file));
		} else {
			formattedFile = HMM.convertFormat(FileUtils.readIn());
		}
		if (output_file.length() > 0) {
			FileUtils.writeFile(output_file, formattedFile);
		} else {
			System.out.println(formattedFile);
		}
	}

}
