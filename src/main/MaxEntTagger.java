package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import shared.FeatureToken;

import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

public class MaxEntTagger {
	
	static Map<String, Integer> featureCounts;
	static List<List<FeatureToken>> tokenLines;
	
	public static void extractFeatures(List<List<String[]>> sentences, int rare_thres, Map<String, Integer> voc, boolean train) {
		// Each token has a set of features. If the token contains the feature (1), it's in the set. Otherwise, it's not (0).
		// While going through each word in each line, count the frequencies of each feature in a map.
		int rareCount = 0;
		if (train) {
			featureCounts = new LinkedHashMap<String, Integer>();
		}
		tokenLines = new ArrayList<List<FeatureToken>>();
		String feature;
		for (List<String[]> line : sentences) {
			List<FeatureToken> lineTokens = new ArrayList<FeatureToken>();
			for (int wordI=1; wordI<line.size()-1; wordI++) { // Don't include <s> and </s>
				String[] word = line.get(wordI);
				int numWords = line.size();
				if (word.length > 1) {
					String wordStr = word[0];
					FeatureToken token = new FeatureToken(word[0],word[1]);
					boolean rareWord = false;
					if (!voc.containsKey(wordStr) || voc.get(wordStr) < rare_thres) { // If the word is rare.
						rareWord = true;
						rareCount++;
					} else {
						// curW
						feature = "curW="+wordStr;
						token.addFeature(feature);
					}
					// prevW prev2W nextW next2W prevT prevTwoTags
					if (wordI>1) {
						feature = "prevW="+line.get(wordI-1)[0];
					} else {
						feature = "prevW=BOS";
					}
					token.addFeature(feature);
					if (wordI>2) {
						feature = "prev2W="+line.get(wordI-2)[0];
					} else {
						feature = "prev2W=BOS";
					}
					token.addFeature(feature);
					if (wordI<numWords-2) {
						feature = "nextW="+line.get(wordI+1)[0];
					} else {
						feature = "nextW=EOS";
					}
					token.addFeature(feature);
					if (wordI<numWords-3) {
						feature = "next2W="+line.get(wordI+2)[0];
					} else {
						feature = "next2W=EOS";
					}
					token.addFeature(feature);
					if (wordI>1) {
						feature = "prevT="+line.get(wordI-1)[1];
					} else {
						feature = "prevT=BOS";
					}
					token.addFeature(feature);
					if (wordI>2) {
						feature = "prevTwoTags="+line.get(wordI-2)[1]+"+"+line.get(wordI-1)[1];
					} else if (wordI>1) {
						feature = "prevTwoTags=BOS+"+line.get(wordI-1)[1];
					} else {
						feature = "prevTwoTags=BOS+BOS";
					}
					token.addFeature(feature);
					if (rareWord) {
						//  pref=j pref=jo suf=n suf=in containNum containUC containHyp
						char[] chars = wordStr.toCharArray();
						String currPref = "";
						for (int charI=0; charI<chars.length && charI<4; charI++) {
							currPref+=chars[charI];
							feature = "pref="+currPref;
							token.addFeature(feature);
						}
						String currSuf = "";
						for (int charI=chars.length-1; charI>0 && charI>chars.length-5; charI--) {
							currSuf=chars[charI]+currSuf;
							feature = "suf="+currSuf;
							token.addFeature(feature);
						}

						if (ParseUtils.containsDigit(wordStr)) {
							feature = "containNum";
							token.addFeature(feature);
						}
						if (ParseUtils.containsUppercase(wordStr)) {
							feature = "containUC";
							token.addFeature(feature);
						}
						if (ParseUtils.containsHyphen(wordStr)) {
							feature = "containHyp";
							token.addFeature(feature);
						}
					}
					if (train) {
						// Increment feature counts for each feature of this token/word.
						for (String currFeat : token.getFeatures()) {
							if (!currFeat.contains("contain")) {
								StatUtils.incrementOne(featureCounts, currFeat);
							}
						}
					}
					lineTokens.add(token);
				}
			}
			tokenLines.add(lineTokens);
		}
		if (train && rareCount > 0) {
			featureCounts.put("containNum", rareCount);
			featureCounts.put("containUC", rareCount);
			featureCounts.put("containHyp", rareCount);
		}
	}
	
	

	/**
	 * USAGE: maxent_tagger.sh train_file test_file rare_thres feat_thres output_dir
	 * @param train_file test_file rare_thres feat_thres output_dir
	 */
	public static void main(String[] args) {
		String train_file = ""; // Tagged words with format wn/tn.
		String test_file = ""; // Tagged words with format wn/tn.
		int rare_thres = 0; // Any words that appear less times than this are rare.
			// Use pref=xx and suf=xx for rare words.
		int feat_thres = 0; // All w_i (word) features are kept. For all other features, if the feature appears less times than
		// feat_thres in the training data, remove the feature from the feature vectors.
		String output_dir = "";
		if (args.length > 4) {
			train_file = args[0];
			test_file = args[1];
			rare_thres = Integer.parseInt(args[2]);
			feat_thres = Integer.parseInt(args[3]);
			output_dir = args[4];
		} else {
			System.out.println("USAGE: train_file test_file rare_thres feat_thres output_dir");
			System.exit(0);
		}
		File outDir = new File(output_dir);
		if (!outDir.isDirectory()) {
			System.out.println("output_dir should be a directory.");
			System.exit(0);
		}
		// Step 1: Create feature vectors for training and test.
		// Create train_voc
		// Format: word\tfreq
		// Sorted by freq in descending order.
		// 1.1. Create train_voc.
		String trainText = FileUtils.readFile(train_file);
		List<List<String[]>> sentences = ParseUtils.getLinesAsPOSSentences(trainText);
		Map<String, Integer> trainVoc = StatUtils.getPOSWordCounts(sentences);
		List<Entry<String, Integer>> sortedCounts = StatUtils.sortValues(trainVoc, true);
		String train_voc_file = output_dir+File.separator+"train_voc";
		FileUtils.writeFile(train_voc_file, ParseUtils.listEntriesToString(sortedCounts));
		sortedCounts = null; // Save memory.
		// 1.1. Use freq and rare_thres to identify rare words (where freq < rare_thres).
		// 1.2. Form feature vectors for words in train_file. Store features and frequencies in init_feats.
		// Represent features as a List (lines) of a List (line) of Tokens (word).
		extractFeatures(sentences, rare_thres, trainVoc, true);
		
		// Create init_feats
		// Format: featName\tfreq
		// Sorted by freq in descending order.
		String init_feats_file = output_dir+File.separator+"init_feats";
		featureCounts = StatUtils.sortValuesGetMap(featureCounts, true);
		FileUtils.writeFile(init_feats_file, ParseUtils.mapToString(featureCounts));
		
		// Create kept_feats
		// Format: featName\tfreq
		// Subset of init_feats, after removing features with freq < feat_thres
		// 1.3. Use feat_thres to filter out features and create kept_feats. Don't filter out any curW features.
		List<String> filteredFeatures = new ArrayList<String>(); // Features that are to be removed.
		for (Entry<String, Integer> currFeat : featureCounts.entrySet()) {
			if (!currFeat.getKey().startsWith("curW") && currFeat.getValue() < feat_thres) {
				filteredFeatures.add(currFeat.getKey());
			}
		}
		String kept_feats_file = output_dir+File.separator+"kept_feats";
		for (String filtered : filteredFeatures) {
			featureCounts.remove(filtered);
		}
		FileUtils.writeFile(kept_feats_file, ParseUtils.mapToString(featureCounts));
		
		
		// Create final_train.vectors.txt
		// Feature vectors for train_file in Mallet text format.
		// Only keep features in kept_feats
		// 1.4. Remove features not in kept_feats from training vectors.
		// 1.6. Replace all occurrences of "," with "comma".
		// 1.4. Write training vectors.
		String final_train_file = output_dir+File.separator+"final_train.vectors.txt";
		FileUtils.writeFile(final_train_file, FeatureToken.featureTokensToString(tokenLines, featureCounts).replace(",","comma"));
		
//		// Create final_test.vectors.txt
//		// Feature vectors for the test_file.
//		// Same format as final_train.vectors.txt
//		// 1.5. Create and write feature vectors for test_file.
//		// 1.6. Replace all occurrences of "," with "comma" (for Mallet).
//		List<List<String[]>> testSentences = ParseUtils.getLinesAsPOSSentences(FileUtils.readFile(test_file));
//		extractFeatures(testSentences, rare_thres, trainVoc, false);
//		String final_test_file = output_dir+File.separator+"final_test.vectors.txt";
//		FileUtils.writeFile(final_test_file, FeatureToken.featureTokensToString(tokenLines, featureCounts).replace(",","comma"));
//		
//		String malletDir="";
//		String scriptFile = output_dir+File.separator+"script.sh";
////		malletDir = "/home/joshua/workspace/mallet-2.0.8/bin/";
//		// Step 2: Run mallet import-file
//		// mallet import-file --input final_train.vectors.txt --output final_train.vectors
//		// Create final_train.vectors
//		// Binary format of final_train.vectors.txt
//		String final_train_bin_file = output_dir+File.separator+"final_train.vectors";
//		String command = malletDir+"mallet import-file --input "+output_dir+File.separator+"final_train.vectors.txt --output "+final_train_bin_file;
//		FileUtils.writeFile(scriptFile, "#!/bin/sh\n\n"+command+"\n");
//		FileUtils.runCommand("sh "+scriptFile);
//		
//		// Binary format of final_test.vectors.txt
//		String final_test_bin_file = output_dir+File.separator+"final_test.vectors";
//		command = malletDir+"mallet import-file --input "+output_dir+File.separator+"final_test.vectors.txt --output "+final_test_bin_file+" --use-pipe-from "+final_train_bin_file;
//		FileUtils.writeFile(scriptFile, "#!/bin/sh\n\n"+command+"\n");
//		FileUtils.runCommand("sh "+scriptFile);
//		
//		// Step 3: Run mallet train-classifier
//		// mallet train-classifier --trainer MaxEnt --input final_train.vectors --output-classifier me_model --report train:accuracy --report test:accuracy > me_model.stdout 2> me_model.stderr
//		// Create me_model
//		// Binary format of MaxEnt model, produced by the MaxEnt trainer.
//		// Create me_model.stdout
//		// Create me_model.stderr
//		String model_file = output_dir+File.separator+"me_model";
//		String model_out_file = output_dir+File.separator+"me_model.stdout";
//		String model_err_file = output_dir+File.separator+"me_model.stderr";
////		command = malletDir+"mallet train-classifier --trainer MaxEnt --training-file "+final_train_bin_file+" --testing-file "+final_test_bin_file+" --output-classifier "+model_file+" --report train:accuracy --report test:accuracy > "+model_out_file+" 2> "+model_err_file;
//		command = malletDir+"mallet train-classifier --trainer MaxEnt --training-file "+final_train_bin_file+" --testing-file "+final_test_bin_file+" --output-classifier "+model_file+" --report train:accuracy --report test:accuracy";
//		System.out.println("script file: "+"#!/bin/sh\n\n"+command+"\n");
////		FileUtils.writeFile(scriptFile, "#!/bin/sh\n\n"+command+"\n");
////		FileUtils.runCommand("sh "+scriptFile);
//		FileUtils.runCommandOutErr(command, model_out_file, model_err_file);
//		
//		// Step 4: Run mallet classify-file
//		// mallet classify-file --input final_test.vectors.txt --classifier me_model --output sys_out
//		// Create sys_out
//		String sys_out_file = output_dir+File.separator+"sys_out";
//		command = malletDir+"mallet classify-file --input "+final_test_file+" --classifier me_model --output "+sys_out_file;
//		FileUtils.writeFile(scriptFile, "#!/bin/sh\n\n"+command+"\n");
//		FileUtils.runCommand("sh "+scriptFile);
//		// Step 5: Calculate test accuracy
		
		
	}
}
