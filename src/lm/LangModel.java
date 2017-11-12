package lm;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

// Represents a language model, for training and for querying.
public class LangModel {
	String lmFile;
	HashMap<String, Double> ngrams;
	
	/*
	 * Load a given language model file.
	 */
	public LangModel(String lmFile) {
		this.lmFile = lmFile;
		loadLM();
	}
	
	/*
	 * Load n-gram language model file into HashMap.
	 */
	public void loadLM() {
		ngrams = new HashMap<String, Double>();
		List<String> lines = FileUtils.readLines(lmFile);
		int lineI = 0;
		while (!lines.get(lineI).startsWith("\\1-grams:")) {
			lineI++;
		}
		lineI++; // Skip line with '\1-grams:'
		for (; lineI<lines.size(); lineI++) {
			List<String> tokens = ParseUtils.splitTabs(lines.get(lineI));
			if (tokens.size() > 3) {
				Double prob = Double.valueOf(tokens.get(1));
				String gramStr = tokens.get(3);
//				for (int tokenI=4; tokenI<tokens.size(); tokenI++) {
//					gramStr += " "+tokens.get(tokenI);
//				}
//				if (gramStr.equals(", they")) {
//					System.out.println(tokens);
//					System.out.println("gramStr: "+gramStr+" => "+prob);
//				}
				ngrams.put(gramStr, prob);
			}
		}
		
	}
	
	/*
	 * Records the perplexity of the language model.
	 * Use interpolation.
	 */
	public void writePerplexity(double l1, double l2, double l3, String test_data, String output_file) {
		StringBuilder outStr = new StringBuilder();
		outStr.append("\n");
		List<List<String>> sentences = ParseUtils.getLinesAsSentences(FileUtils.readFile(test_data));
		double sum = 0.0;
		double word_num = 0;
		double oov_num = 0;
		double sent_num = sentences.size();
		for (int sentI=0; sentI<sentences.size(); sentI++) {
			List<String> sentence = sentences.get(sentI);
			outStr.append("Sent #"+(sentI+1)+": "+ParseUtils.listToString(sentence)+"\n");
			double sentProb = 0.0;
			int sent_oov = 0;
//			System.out.println("sentence: "+ParseUtils.listToString(sentence));
			int sent_word_num = sentence.size()-2;
			word_num += sent_word_num; // Words in line minus <s> and </s>
			String prevStr = "";
			String wMin1 = "";
			String wMin2 = "";
			for (int i=1; i<sentence.size(); i++) {
				String gramStr = sentence.get(i);
				wMin1 = sentence.get(i-1);
				if (i > 1) {
					wMin2 = sentence.get(i-2);
					prevStr = wMin2+" "+wMin1;
				} else {
					prevStr = wMin1;
				}
				outStr.append(i+": lg P("+gramStr+" | "+prevStr+") = ");
				double wordProb = 0;
				boolean unseenGram = false;
				if (ngrams.containsKey(gramStr)) {
					double P1 = ngrams.get(gramStr);
					wordProb += l1 * P1;
//					if (sentI==24) {
//						outStr.append("\ngramStr: "+gramStr+" prob: "+ngrams.get(gramStr)+" l1: "+l1+" P1: "+P1+"\n");
//					}
					gramStr = wMin1 + " "+gramStr;
					if (ngrams.containsKey(gramStr)) {
						double P2 =  ngrams.get(gramStr);
						wordProb += l2 * P2;
						if (i > 1) {
							gramStr = wMin2 + " "+ gramStr;
							if (ngrams.containsKey(gramStr)) {
								double P3 =  ngrams.get(gramStr);
								wordProb += l3 * P3;
							} else {
								unseenGram = true;
							}
						}
//						if (sentI==24) {
//							outStr.append("\ngramStr: "+gramStr+" prob: "+ngrams.get(gramStr)+" l2: "+l2+" P2: "+P2+"\n");
//						}
					} else {
						unseenGram = true;
					}
					wordProb = Math.log10(wordProb);
					outStr.append(String.format("%.17f",wordProb));
					if (unseenGram) {
						outStr.append(" (unseen ngrams)");
					}
				} else {
					sent_oov++;
					outStr.append("-inf (unknown word)");
				}
				outStr.append("\n");
				sentProb += wordProb;
//				outStr.append("sentprob="+sentProb+"\n");
			}
			int sent_num_temp = 1;
			double sent_cnt = sent_word_num + sent_num_temp - sent_oov;
			double sent_total = -sentProb / sent_cnt;
			double sent_ppl = Math.pow(10, sent_total);
			outStr.append("1 sentence, "+sent_word_num+" words, "+sent_oov+" OOVs\n");
			outStr.append("lgprob="+sentProb+" ppl="+sent_ppl+"\n");
			outStr.append("\n\n\n");
			sum += sentProb;
			oov_num += sent_oov;
		}
		double cnt = word_num + sent_num - oov_num;
		double total = -sum / cnt;
		double ppl = Math.pow(10, total);
		double ave_lgprob = sum / cnt;
		outStr.append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n");
		outStr.append("sent_num="+(int)sent_num+" word_num="+(int)word_num+" oov_num="+(int)oov_num+"\n");
		outStr.append("lgprob="+sum+" ave_lgprob="+ave_lgprob+" ppl="+ppl+"\n");
		FileUtils.writeFile(output_file, outStr.toString());
	}
	
	/*
	 * Write an n-gram language model to a file, up to maxOrder-gram.
	 * Use the modified ARPA format.
	 */
	public static void writeGramLM(List<HashMap<String, Double>> gramCounts, String outFile) {
		List<HashMap<String, Double>> gramProbs = StatUtils.getNGramProbs(gramCounts);
		StringBuilder lmStr = new StringBuilder();
		lmStr.append("\\data\\\n");
		int maxOrder = gramCounts.size();
		// Write count summary at beginning of file. e.g. ngram 1: type=5171 token=25551
		for (int n=0; n<maxOrder; n++) {
			int tokenCount = StatUtils.getMapTotalDouble(gramCounts.get(n)).intValue();
			int typeCount = gramCounts.get(n).size();
			lmStr.append("ngram "+(n+1)+": type="+typeCount+" token="+tokenCount+"\n");
		}
		
		// Write entries for each n-gram.
		// Format: 1160 0.0453993972838636 -1.34294991273826 ,
		for (int n=0; n<maxOrder; n++) {
			lmStr.append("\n\\"+(n+1)+"-grams:\n");
			HashMap<String, Double> probsMap = gramProbs.get(n);
			HashMap<String, Double> countsMap = gramCounts.get(n);
			List<Entry<String, Double>> sortedCounts = StatUtils.sortValuesDouble(countsMap, true);
			for (Entry<String, Double> countEntry : sortedCounts) {
				String gramStr = countEntry.getKey();
				Double prob = probsMap.get(gramStr);
				String formattedProb = "";
				if (prob == 1.0) {
					formattedProb = "1";
				} else if (prob == 0.0) {
					formattedProb = "0";
				} else {
					formattedProb = String.format("%.17f",prob);
				}
				lmStr.append(countsMap.get(gramStr).intValue()+"\t"+formattedProb+"\t"+Math.log10(prob)+"\t"+gramStr+"\n");
			}
		}
		lmStr.append("\n\\end\\\n");
		
		FileUtils.writeFile(outFile, lmStr.toString());
	}
	
	
}
