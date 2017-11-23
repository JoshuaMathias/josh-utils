package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import shared.ManyFiles;
import tokenize.TokenUtils;
import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

public class CreateVectors {

	public static String getVectorStr(File file) {
		String text = FileUtils.readFile(file);
		text = TokenUtils.tokenizeFeatures(ParseUtils.getAfterRegex(text, "\n\\s*\n"));
		HashMap<String, Integer> tokenCounts = StatUtils.getTokenCounts(text);
		List<Entry<String, Integer>> sortedTokens = StatUtils.sortKeys(tokenCounts, false);
		StringBuilder outputStr = new StringBuilder();
		String targetLabel = file.getParentFile().getName();
		outputStr.append(file.getPath()+" "+targetLabel);
		for (Entry<String, Integer> token : sortedTokens) {
			outputStr.append(" "+token.getKey()+" "+token.getValue());
		}
		return outputStr.toString();
	}
	
	/**
	 * Create test and train vectors from a set of directories, where the given ratio represents
	 *  the portion to be used as training (e.g. .9).
	 * USAGE: train_vector_file test_vector_file ratio dir1 dir2 ...
	 * @param train_vector_file test_vector_file ratio dir1 dir2 ...
	 * 
	 */
	public static void main(String[] args) {
		String train_vector_file = "";
		String test_vector_file = "";
		double ratio = 0.0;
		List<String> dirs = new ArrayList<String>();
		if (args.length > 3) {
			train_vector_file = args[0];
			test_vector_file = args[1];
			ratio = Double.parseDouble(args[2]);
			for (int argI=3; argI<args.length; argI++) {
				dirs.add(args[argI]);
			}
		} else {
			System.out.println("USAGE: train_vector_file test_vector_file ratio dir1 dir2 ...");
			System.exit(0);
		}
		StringBuilder trainingOutput = new StringBuilder();
		StringBuilder testOutput = new StringBuilder();
		// Write training and test vectors.
		for (String dir : dirs) {
			ManyFiles currFiles = new ManyFiles(dir);
			currFiles.sortFiles(false);
			currFiles.splitTestSet(ratio);
			List<File> trainingFiles = currFiles.getTrainingFiles();
//			System.out.println("Num training files: "+trainingFiles.size());
			for (File file : trainingFiles) {
//				System.out.println("training file: "+file.getName());
				trainingOutput.append(getVectorStr(file)+"\n");
			}
			List<File> testFiles = currFiles.getTestFiles();
//			System.out.println("Num test files: "+testFiles.size());
			for (File file : testFiles) {
//				System.out.println("test file: "+file.getName());
				testOutput.append(getVectorStr(file)+"\n");
			}
			
		}
		FileUtils.writeFile(train_vector_file, trainingOutput.toString());
		FileUtils.writeFile(test_vector_file, testOutput.toString());
	}

}
