package lm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

/*
 * Represents states and transitions of a POS tagger.
 */
public class HMM {

	int state_num=0; // The number of states
	int sym_num=0; // The size of output symbol alphabet
	int init_line_num=0; // The number of lines for the initial probability
	int trans_line_num=0; // The number of lines for the transition probability
	int emiss_line_num=0; // The number of lines for the emission probability
	int maxOrder = 2;
	double l1 = 0.0;
	double l2 = 0.0;
	double l3 = 0.0;
	String unkFilename = "";
	Map<String, Double> initials; // Key: state_str. Value: prob.
	List<Map<String, Double>> transitions; // Key: From_state. Value: Map: Key: to_state. Value: prob.
	Map<String, Map<String, Double>> symbols; // Key: state_str. Value: Map: Key: symbol. Value: prob.
	List<String> tags; // List of every possible tag.
	Map<String, Double> gramProbs;
	Set<String> states;
	Set<String> tokens;
	HashMap<String, Integer> stateToIndex;
	String[] indexToState;
	HashMap<String, Integer> symbolToIndex;
	String[] indexToSymbol;
	double[][] stateGraph;
	double[][] stateSymbols;
	double[] initialProbs;
	List<Integer> initialIndices;
	
	List<Map<String, Double>> gramCounts; // Counts for each gram of POS tags.
	Map<String, Map<String, Integer>> symbolCounts; // Counts for the symbols of each tag. Key: tag. Value: Map: Key: symbol. Value: prob.
	Map<String, Integer> initialCounts; // Counts for the first symbol of each sentence.
	
	Map<String, Double> unkProbs;
	
	
	public HMM(String trainingStr, int maxOrder) {
		this.maxOrder = maxOrder;
		getNGramTagCounts(trainingStr, maxOrder);
		if (maxOrder == 2) {
			buildBigramHMM();
		} else if (maxOrder == 3) {
			buildTrigramHMM();
		}
		
	}
	
	public HMM(String trainingStr, int maxOrder, double l1, double l2, double l3, String unkFilename) {
		this.maxOrder = maxOrder;
		getNGramTagCounts(trainingStr, maxOrder);
		if (maxOrder == 2) {
			buildBigramHMM();
		} else if (maxOrder == 3) {
			this.l1 = l1;
			this.l2 = l2;
			this.l3 = l3;
			this.unkFilename = unkFilename;
			initSmoothing();
			
			buildTrigramHMM();
		}
		
	}
	
	public HMM(String hmm_file) {
		checkHMM(hmm_file);
	}
	
	public void checkHMM(String hmm_file) {
		List<List<String>> hmmLines = ParseUtils.splitLinesWhitespace(FileUtils.readFile(hmm_file));
//		System.out.println("hmm lines: "+hmmLines.size());
//		if (hmmLines.size() > 4) {
//			state_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(0),'=').get(1));
//			sym_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(1),'=').get(1));
//			init_line_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(2),'=').get(1));
//			trans_line_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(3),'=').get(1));
//			emiss_line_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(4),'=').get(1));
//		}
		int lineI = 5;
		for (; lineI<hmmLines.size(); lineI++) {
			if (hmmLines.get(lineI).get(0).contains("\\init")) {
				break;
			}
		}
		lineI++;
		
//		states = new HashSet<String>();
		stateToIndex = new HashMap<String, Integer>();
		int stateIndex=0;
		// Parse initials
		initials = new HashMap<String, Double>();
//		int initLineCount = 0;
		List<String> line;
		for (; lineI<hmmLines.size(); lineI++) {
			line = hmmLines.get(lineI);
			if (line.get(0).contains("\\transition")) {
				break;
			}
//			initLineCount++;
			if (line.size() > 1) {
				double prob = Double.parseDouble(line.get(1));
				if (prob < 0.0 || prob > 1.0) {
					System.err.println("warning: the prob is not in [0,1] range: "+line);
					continue;
				}
				initials.put(line.get(0), Math.log10(prob));
				stateToIndex.put(line.get(0), stateIndex); // Get a set of all states.	
				stateIndex++;
//				states.add(line.get(0));
			}
		}
		lineI++;

		// Parse transitions
//		gramProbs = new HashMap<String, Double>();
//		HashSet<String> tagsSet = new HashSet<String>();
//		int transLineCount = 0;

//		for (; lineI<hmmLines.size(); lineI++) {
//			line = hmmLines.get(lineI);
//			if (line.contains("\\emission")) {
//				break;
//			}
//			transLineCount++;
//			String[] splitLine = line.split("\\s");
//			if (splitLine.length > 2) {
//					states.add(splitLine[1]); // Get a set of all states.	
//				if (maxOrder > 2 && ParseUtils.splitChar(splitLine[1],'_').size() > 1) {
//					// Form a trigram A_B_C out of the to and from states A_B, B_C.
//					tags.add(splitLine[0]);
//					tags.add(splitLine[1]);
//					List<String> splitGram = ParseUtils.splitChar(splitLine[1],'_');
//					String trigram = splitLine[0]+"_"+splitGram.get(1);
//					gramProbs.put(trigram, Double.parseDouble(splitLine[2]));
//					tagsSet.add(splitGram.get(1));
//				} else if (maxOrder == 2) {
//					gramProbs.put(splitLine[0]+"_"+splitLine[1], Double.parseDouble(splitLine[2]));
//				}
//			}
//		}
		// Map state names to indices
		int transitionLineI = lineI;
		for (; lineI<hmmLines.size(); lineI++) {
			line = hmmLines.get(lineI);
			if (line.get(0).contains("\\emission")) {
				break;
			}
			if (line.size() > 2) {
				if (!stateToIndex.containsKey(line.get(1))) {
					stateToIndex.put(line.get(1), stateIndex); // Get a set of all states.	
					stateIndex++;
				}
			}
		}
		// Map indices to state names.
		indexToState = new String[stateToIndex.size()];
		for (Entry<String, Integer> stateEntry : stateToIndex.entrySet()) {
			indexToState[stateEntry.getValue()] = stateEntry.getKey();
		}
		// Form graph using state indices.
		state_num = stateIndex;
		stateGraph = new double[state_num][state_num]; // Each from state has an array of to states.
		StatUtils.fillArrayDouble(stateGraph, Double.NEGATIVE_INFINITY);
		int emissionLineI=lineI;
		lineI = transitionLineI; // Go through transition lines again.
		for (; lineI<emissionLineI; lineI++) {
			line = hmmLines.get(lineI);
			if (line.size() > 2) {
				// Store to_state in array of from_state.
				double prob = Double.parseDouble(line.get(2));
				if (prob < 0.0 || prob > 1.0) {
					System.err.println("warning: the prob is not in [0,1] range: "+line);
					continue;
				}
				stateGraph[stateToIndex.get(line.get(0))][stateToIndex.get(line.get(1))] = Math.log10(prob);
			}
		}
		
		// Make array of initial probabilities
		initialProbs = new double[state_num];
		Arrays.fill(initialProbs, Double.NEGATIVE_INFINITY);
		initialIndices = new ArrayList<Integer>();
		for (Entry<String, Double> entry : initials.entrySet()) {
			int initIndex = stateToIndex.get(entry.getKey());
			initialProbs[initIndex] = entry.getValue();
			initialIndices.add(initIndex);
		}
		
//		tags = new ArrayList<String>(tagsSet);
		lineI++;
		
		// Parse emissions
//		symbols = new HashMap<String, Map<String, Double>>();
//		tokens = new HashSet<String>();
////		int emisLineCount = 0;
//		for (; lineI<hmmLines.size(); lineI++) {
//			line = hmmLines.get(lineI);
////			emisLineCount++;
//			if (line.size() > 2) {
////				String symbols = splitLine[0]+ParseUtils.splitChar(splitLine[1],'_').get(1);
//				Map<String, Double> posMap;
//				if (symbols.containsKey(line.get(0))) {
//					posMap = symbols.get(line.get(0));
//				} else {
//					posMap = new HashMap<String, Double>();
//				}
//				posMap.put(line.get(1), Double.parseDouble(line.get(2)));
//			
//				symbols.put(line.get(0),posMap);
//				tokens.add(line.get(1));
//			}
//		}
		
		symbolToIndex = new HashMap<String, Integer>();
		int symbolIndex = 0;
		symbolToIndex.put("<unk>", symbolIndex);
		symbolIndex++;
		emissionLineI = lineI;
		for (; lineI<hmmLines.size(); lineI++) {
			line = hmmLines.get(lineI);
			if (line.size() > 2) {
				if (!symbolToIndex.containsKey(line.get(1))) {
					symbolToIndex.put(line.get(1), symbolIndex);
					symbolIndex++;
				}
			}
		}
		indexToSymbol = new String[symbolIndex+1];
		sym_num = symbolIndex+1;
		stateSymbols = new double[sym_num][sym_num];
		StatUtils.fillArrayDouble(stateSymbols, Double.NEGATIVE_INFINITY);
		lineI = emissionLineI;
		for (; lineI<hmmLines.size(); lineI++) {
			line = hmmLines.get(lineI);
			if (line.size() > 2) {
				double prob = Double.parseDouble(line.get(2));
				if (prob < 0.0 || prob > 1.0) {
					System.err.println("warning: the prob is not in [0,1] range: "+line);
					continue;
				}
				stateSymbols[stateToIndex.get(line.get(0))][symbolToIndex.get(line.get(1))] = Math.log10(prob);
//				System.out.println("Setting symbol prob at state "+stateToIndex.get(line.get(0))+" symbol "+symbolToIndex.get(line.get(1))+" to "+Math.log10(prob));
			}
		}
		hmmLines = null;
		
//		if (state_num != states.size()) {
//			System.out.println("warning: different numbers of state_num: claimed="+state_num+", real="+states.size());
//		} else {
//			System.out.println("state_num="+state_num);
//		}
//		if (sym_num != tokens.size()) {
//			System.out.println("warning: different numbers of sym_num: claimed="+sym_num+", real="+tokens.size());
//		} else {
//			System.out.println("sym_num="+sym_num);
//		}
//		
//		if (init_line_num != initLineCount) {
//			System.out.println("warning: different numbers of init_line_num: claimed="+init_line_num+", real="+initLineCount);
//		} else {
//			System.out.println("init_line_num="+init_line_num);
//		}
//		
//		if (trans_line_num != transLineCount) {
//			System.out.println("warning: different numbers of trans_line_num: claimed="+trans_line_num+", real="+transLineCount);
//		} else {
//			System.out.println("trans_line_num="+trans_line_num);
//		}
//		
//		if (emiss_line_num != emisLineCount) {
//			System.out.println("warning: different numbers of emiss_line_num: claimed="+emiss_line_num+", real="+emisLineCount);
//		} else {
//			System.out.println("emiss_line_num="+emiss_line_num);
//		}
		// Check that initial state probabilities sum to 1.
//		double init_prob_sum = StatUtils.getMapTotalDouble(initials);
//		if (!StatUtils.equalsDouble(init_prob_sum, 1.0)) {
//			System.out.println("warning: the init_prob_sum is "+init_prob_sum);
//		}
		
		// Check that transition probabilities sum to 1.
		// For each from_state, sum each of its possible to_states.
		
//		for (String fromState : states) {
//			double sum = 0.0;
//			for (String tag : tags) {
//				String complete_gram = fromState+"_"+tag;
//				if (gramProbs.containsKey(complete_gram)) {
//					sum+=gramProbs.get(complete_gram);
//				}
//			}
//			if (!StatUtils.equalsDouble(sum, 1.0)) {
//				System.out.println("warning: the trans_prob_sum for state "+fromState+" is "+sum);
//			}
//		}
		
		// Check that the symbol probs for each tag (in emission) sum to 1.
//		for (String state : states) {
//			if (!state.equals("BOS")) {
//				double sum = 0.0;
//				if (symbols.containsKey(state)) {
//					sum = StatUtils.getMapTotalDouble(symbols.get(state));
//				} 
//				if (!StatUtils.equalsDouble(sum, 1.0)) {
//					System.out.println("warning: the emiss_prob_sum for state "+state+" is "+sum);
//				}
//			}
//		}
	}
	
	/*
	 * Viterbi Algorithm
	 * Input: 
	 * 	observations of len T: List<String> observations
	 * 	state-graph of len N: double[fromState][toState] stateGraph
	 *  Returns best path, as a String of states, with the last token of the string being the probability of the path.
	 */
	public String viterbi(List<String> observations) {
		observations.add("<s>");
		int num_obs = observations.size();
		double[][] pathProbs = new double[state_num+2][num_obs+1]; // path probability matrix viterbi[N+2, T]
		pathProbs = StatUtils.fillArrayDouble(pathProbs, Double.NEGATIVE_INFINITY);
		int[][] backpointer = new int[state_num+2][num_obs+1]; // Backpointers, for retrieving the best path at the end.
		// Initialization
		// For each initial state, put its initial probability in the first column of pathProbs, for the index of the initial state.
		// Set backpointers for each state to the most likely 
//		for (int initialI=0; initialI<initials.size(); initialI++) {
		for (int initI : initialIndices) {
			pathProbs[initI][0] = initialProbs[initI];
		}
		int stepI;
		for (stepI=1; stepI<=num_obs; stepI++) {
			int symI = 0; // Set symbol to <unk> by default.
			if (symbolToIndex.containsKey(observations.get(stepI-1))) {
				symI = symbolToIndex.get(observations.get(stepI-1));
			}
			for (int stateI=0; stateI<state_num; stateI++) {
				double symProb = stateSymbols[stateI][symI];
				if (symProb == Double.NEGATIVE_INFINITY) {
					continue;
				} 
//				else {
//					System.out.print("state: "+stateI+" "+indexToState[stateI]+" ");
//					System.out.print("sym index: "+symI+" sym prob: "+symProb+"\n");
//				}
				double bestProb = Double.NEGATIVE_INFINITY;
				int bestState = -1;
				for (int prevStateI=0; prevStateI<state_num; prevStateI++) {
					double pathProb = pathProbs[prevStateI][stepI-1];
//					System.out.println("pathProbs: "+pathProbs);
			
					if (pathProb == Double.NEGATIVE_INFINITY) {
						continue;
					} 
//					else {
//						System.out.print("pathProb: "+pathProb+" ");
//					}
					double transProb = stateGraph[prevStateI][stateI];
					if (transProb == Double.NEGATIVE_INFINITY) {
						continue;
					} 
//					else {
//						System.out.print("transProb: "+transProb);
//					}
//					System.out.print("prev state: "+prevStateI+" {"+indexToState[prevStateI]+") current state: "+stateI+" ("+indexToState[stateI]+") pathprob: "+pathProb+" transProb: "+transProb+"\n");
					double prevProb = pathProb + transProb + symProb;
//					System.out.println("prevProb: "+prevProb);
					if (prevProb > bestProb) {
//						System.out.println("Found prob: "+prevProb+" state: "+prevStateI);
						bestProb = prevProb;
						bestState = prevStateI;
					}
//					System.out.println();
				}
//				System.out.println("Best prob: "+bestProb+" best state: "+bestState+" ("+indexToState[bestState]+")");
				pathProbs[stateI][stepI] = bestProb;
				backpointer[stateI][stepI] = bestState;
			}
			
		}
		double bestProb = Double.NEGATIVE_INFINITY;
		int bestState = -1;
		for (int nextStateI=0; nextStateI<state_num; nextStateI++) {
			double nextProb = pathProbs[nextStateI][num_obs-1];
			if (nextProb > bestProb) {
				bestProb = nextProb;
				bestState = nextStateI;
			}
//			System.err.println("best final prob: "+bestProb+" best final state: "+bestState);
		}
		
//		backpointer[bestState][stepI] = bestState;
		StringBuilder tagStr = new StringBuilder();
		if (bestState == -1) {
			tagStr.append("*NONE*");
		} else {
			int currPointer = bestState;
			for (stepI=num_obs-1; stepI>=0; stepI--) {
				tagStr.insert(0, indexToState[currPointer]+" ");
				currPointer = backpointer[currPointer][stepI];
			}
			tagStr.append(bestProb);
		}
		observations.remove(observations.size()-1);
		return tagStr.toString();
	}
	
	/*
	 * Use the Viterbi algorithm on a give line of text to apply POS tags.
	 * Return format: text => tags lgprob
	 */
	public String tagLine(String text) {
		List<String> words = ParseUtils.splitSpaces(text);
		String bestTags = viterbi(words);
		String taggedStr = ParseUtils.listToString(words)+" => "+bestTags;
		return taggedStr;
	}
	
	/*
	 * Read in file input_file and tag each line.
	 * Write to file output_file.
	 * Use the Viterbi algorithm to perform the tagging.
	 */
	public void tagFile(String input_file, String output_file) {
		List<String> lines = FileUtils.readLines(input_file);
		StringBuilder outputStr = new StringBuilder();
		for (String line : lines) {
			outputStr.append(tagLine(line)+"\n");
		}
		
		FileUtils.writeFile(output_file, outputStr.toString());
	}
	
	/*
	 * Convert tagging format to w1/t1 wn/tn.
	 */
	public static String convertFormat(String inputStr) {
		List<String> lines = ParseUtils.splitLines(inputStr);
		StringBuilder outputStr = new StringBuilder();
		for (String line : lines) {
			String[] splitLine = line.split("\\s=>\\s");
			if (splitLine.length > 1) {
				String[] words = splitLine[0].split("\\s+");
				String[] tags = splitLine[1].split("\\s+");
				if (words.length != tags.length-2) { // Skip start state (BOS_BOS) and don't include probability at end.
					System.err.println("Warning: There are "+words.length+" words and "+(tags.length-2)+" corresponding tags in the following line:\n"+line);
				}
				for (int wordI=0; wordI<words.length; wordI++) {
					outputStr.append(words[wordI]+"/");
					if (tags.length-2 > wordI) {
						List<String> splitState = ParseUtils.splitChar(tags[wordI+1], '_');
						if (splitState.size() > 1) {
							outputStr.append(splitState.get(1));
						} else {
							outputStr.append(tags[wordI+1]);
						}
					} else {
						outputStr.append(words[wordI]);
					}
					if (wordI<words.length-1) {
						outputStr.append(" ");
					}
				}
				outputStr.append("\n");
			}
		}
		return outputStr.toString();
	}
	
	/*
	 * Initialize interpolation weights and the <unk> probabilities.
	 */
	public void initSmoothing() {
		unkProbs = new HashMap<String, Double>();
		List<String> unkLines = FileUtils.readLines(unkFilename);
		for (String line : unkLines) {
			String[] splitLine = line.split("\\s");
			if (splitLine.length > 1) {
				unkProbs.put(splitLine[0], Double.parseDouble(splitLine[1]));
			}
		}
		tokens.add("<unk>");
	}
	
	/*
	 * Separate symbols and POS tags. Start with BOS_symbol (for bigram).
	 * Add 
	 */
	public void buildBigramHMM() {
		initials = new LinkedHashMap<String, Double>();
		symbols = new LinkedHashMap<String, Map<String, Double>>();
		// Calculate unigram symbol to tag probabilities, as count(tag for symbol) / total(symbol).
		for (Entry<String, Map<String, Integer>> entry : symbolCounts.entrySet()) {
			symbols.put(entry.getKey(), StatUtils.divideByTotal(entry.getValue()));
		}
		transitions = StatUtils.getNGramTagProbs(gramCounts);
		initials = StatUtils.divideByTotal(initialCounts);
	}
	
	public void buildTrigramHMM() {
		initials = new LinkedHashMap<String, Double>();
		symbols = new LinkedHashMap<String, Map<String, Double>>();
		// Calculate unigram symbol to tag probabilities, as count(tag for symbol) / total(symbol) * (1-P(<unk>|tag)).
		if (unkProbs == null) {
			System.out.println("Unk probs is null");
			initSmoothing();
		}
		for (Entry<String, Map<String, Integer>> entry : symbolCounts.entrySet()) {
			double total = StatUtils.getMapTotal(entry.getValue());
			double unkProb;
			if (unkProbs.containsKey(entry.getKey())) {
				unkProb = unkProbs.get(entry.getKey());
			} else {
				unkProb = 0.0;
//				System.out.println("Warning: No <unk> prob for tag "+entry.getKey());
			}
			Map<String, Double> normalizedMap = new HashMap<String, Double>();
			for (Entry<String, Integer> symbolEntry : entry.getValue().entrySet()) {
				normalizedMap.put(entry.getKey(), symbolEntry.getValue()/total * (1-unkProb));
			}
			symbols.put(entry.getKey(), normalizedMap);
		}
		transitions = calcTriProbs();
		initials = StatUtils.divideByTotal(initialCounts);
	}
	
	/*
	 * Returns a List containing a Map for each n-gram order, from n=1 to n=maxOrder,
	 * each map containing each distinct n-gram (key) with its count/frequency as its value.
	 * Break each token by the last slash / into a symbol and a tag.
	 * if currentN==0:
		 * Replace all / in the symbol with \/.
		 * Increment count for the symbol's count for this tag.
	 * Increment count for gram count for this tag.
	 */
	public void getNGramTagCounts(String text, int maxOrder) {
		tokens = new HashSet<String>();
		gramCounts = new ArrayList<Map<String, Double>>();
		for (int i=0; i<maxOrder; i++) {
			gramCounts.add(new LinkedHashMap<String, Double>());
		}
		symbolCounts = new LinkedHashMap<String, Map<String, Integer>>();
		initialCounts = new HashMap<String, Integer>();
		if (maxOrder > 0) {
			List<List<String[]>> lines = ParseUtils.getLinesAsPOSSentences(text);
			int startI=0;
			int currentN=0;
			String gramStr = "";
			for (int lineI=0; lineI<lines.size(); lineI++) {
				List<String[]> line = lines.get(lineI);
				String tempBOS = "BOS_BOS";
				for (int i=2; i<maxOrder; i++) {
					StatUtils.incrementDouble(gramCounts.get(i-1), tempBOS);
					tempBOS += "_BOS";
				}
				gramStr = "";
				
				for (startI=0; startI<line.size(); startI++) {
					for (currentN=0; currentN<maxOrder; currentN++) { // All n-grams for the current last word.
						int endI = startI+currentN;
						if (endI >= line.size()) {
							break;
						}
						String[] token = line.get(endI);
						if (currentN == 0) {
//							if (endI == 0 || endI == line.size()-1) { // Don't include <s> or </s> as unigrams.
//								continue;
//							}
							// POS to unigram symbol counts.
							if (startI>0) {
								StatUtils.incrementOneMap(symbolCounts, token[1], token[0]);
							} else {
								StatUtils.incrementOne(initialCounts, token[1]);
							}
							tokens.add(token[0]);
						} else {
							gramStr += "_";
						}
						gramStr += token[1];
						StatUtils.incrementDouble(gramCounts.get(currentN),gramStr);
//						System.out.println("incrementing gramStr: "+gramStr);
					}
//					System.out.println("gramStr: "+gramStr+" last count: "+gramCounts.get(currentN-2).get(gramStr));
					gramStr = "";
				}
				String tempEOS = "EOS_EOS";
				for (int i=2; i<maxOrder; i++) {
//					System.out.println("Incrementing "+tempEOS+" for gram "+i);
					gramCounts.set(i,StatUtils.incrementDouble(gramCounts.get(i-1), tempEOS));
					tempEOS += "_EOS";
				}
			}
		}
		tags = new ArrayList<String>(gramCounts.get(0).keySet());
	}
	
	/*
	 * Returns a list of map of Ngram probabilities, for each n provided in gramCounts.
	 */
	public List<Map<String, Double>> calcTriProbs() {
		List<Map<String, Double>> gramProbs = new ArrayList<Map<String, Double>>();
		for (int i=0; i<gramCounts.size(); i++) {
			gramProbs.add(new HashMap<String, Double>());
		}
		// Calculate unigram probs.
		Map<String, Double> firstGram = gramCounts.get(0);
		double total = StatUtils.getMapTotalDouble(firstGram); // Get total number of tokens.
		Map<String, Double> probsMap = gramProbs.get(0);
		for (Entry<String, Double> entry : firstGram.entrySet()) {
			probsMap.put(entry.getKey(), entry.getValue() / total);
		}
		// Calculate bigram and trigram probs, without smoothing.
		for (int n=1; n<3; n++) {
			probsMap = gramProbs.get(n);
			Map<String, Double> countsMap = gramCounts.get(n);
			Map<String, Double> previousMap = gramCounts.get(n-1);
			for (Entry<String, Double> entry : countsMap.entrySet()) {
				String prevGram = ParseUtils.getUntilNChar(entry.getKey(), '_', n); // Get the previous gram.
				try {
//					if (prevGram.contains("EOS")) {
//						System.out.println("entry key: "+entry.getKey()+" entry value: "+entry.getValue()+" prevGram: "+prevGram+" prevValue: "+previousMap.get(prevGram));
//					}
					probsMap.put(entry.getKey(), entry.getValue() / previousMap.get(prevGram));
				} catch (NullPointerException e) {
					if (!previousMap.containsKey(prevGram)) {
						System.out.println("Couldn't find previous gram for "+entry.getKey()+": "+prevGram);
					}
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		// Calculate trigram probs.
		// Include all possible trigrams, using interpolation.
		int n=2;
		Map<String, Double> countsMap = gramCounts.get(n);
		Map<String, Double> previousMap = gramCounts.get(n-1);
		probsMap = gramProbs.get(n);
		int p3UnkProb = 1/((tags.size()-2)+1); // Don't include BOS and EOS for tag size.
		for (int i=0; i<tags.size(); i++) {
			String iStr = tags.get(i);
			for (int j=0; j<tags.size(); j++) {
				String jStr = tags.get(j);
				for (int k=0; k<tags.size(); k++) {;
					String kStr = tags.get(k);
					String fromBigram = iStr+"_"+jStr;
					String toBigram = jStr+"_"+kStr;
					String trigram = fromBigram+"_"+kStr;
//					System.out.println("trigram: "+trigram);
					double p1 = 0.0;
					double p2 = 0.0;
					double p3 = 0.0;
					if (firstGram.containsKey(kStr)) {
						p1 = firstGram.get(kStr);
					}
					if (previousMap.containsKey(jStr)) {
						p2 = previousMap.get(toBigram);
					}
					if (countsMap.containsKey(trigram)) {
						p3 = countsMap.get(trigram);
					} else if (tags.get(k).equals("BOS")){
						p3 = 0;
					} else {
						p3 = p3UnkProb;
					}
					double p3_smoothed = l3*p3 + l2*p2 + l1*p1;
					probsMap.put(trigram, p3_smoothed);
				}
			}
		}
		
		return gramProbs;
	}
	
	
	@Override
	public String toString() {
		state_num = (int) Math.pow(tags.size(),maxOrder-1); // 
		init_line_num = initials.size();
		sym_num = tokens.size()-1; // Don't count <s>
		if (maxOrder == 2) {
			emiss_line_num = StatUtils.getTotalElementsDouble(symbols);
		}
		StringBuilder emitStr = new StringBuilder();
		if (maxOrder == 3) {
			ArrayList<String> emitLines = new ArrayList<String>();
			for (int j=0; j<tags.size(); j++) {
				String jStr = tags.get(j);
				if (symbols.containsKey(jStr)) {
					for (Entry<String, Double> symbolEntry : symbols.get(jStr).entrySet()) {
						emitLines.add("_"+jStr +"\t"+symbolEntry.getKey()+"\t"+symbolEntry.getValue());
					}
					if (unkProbs.containsKey(jStr)) {
						emitLines.add("_"+jStr +"\t<unk>\t"+unkProbs.get(jStr));
					}
					for (int i=0; i<tags.size(); i++) {
						String iStr = tags.get(i);
						for (String emitLine : emitLines) {
							emiss_line_num++;
							emitStr.append(iStr+emitLine+"\n");
						}
					}
				}
				
			}
			emitLines.clear();
		}
		else {
			emitStr.append(ParseUtils.mapToStringDouble2D(symbols));
		}
				
		StringBuilder transitionStr = new StringBuilder();
		Map<String, Double> gramProbs = transitions.get(maxOrder-1);
		if (maxOrder == 2) {
			for (String fromTag : tags) {
				for (String toTag : tags) {
					String gramStr = fromTag+"_"+toTag;
					//				System.out.println("gramStr: "+gramStr);
					if (gramProbs.containsKey(gramStr)) {
						trans_line_num++;
						transitionStr.append(fromTag+"\t"+toTag+"\t"+gramProbs.get(gramStr)+"\n");
					}
				}
			}
		} else if (maxOrder == 3) {
			for (int i=0; i<tags.size(); i++) {
				String iStr = tags.get(i);
				for (int j=0; j<tags.size(); j++) {
					String jStr = tags.get(j);
					for (int k=0; k<tags.size(); k++) {;
						String kStr = tags.get(k);
						String fromBigram = iStr+"_"+jStr;
						String toBigram = jStr+"_"+kStr;
						String trigram = fromBigram+"_"+kStr;
						//				System.out.println("gramStr: "+gramStr);
						if (gramProbs.containsKey(trigram)) {
							trans_line_num++;
							transitionStr.append(fromBigram+"\t"+toBigram+"\t"+gramProbs.get(trigram)+"\n");
						}
					}
				}
			}
		}
//		System.out.println("trans_line_num: "+trans_line_num);
		
		StringBuilder hmmString = new StringBuilder();
		hmmString.append("state_num="+state_num+"\nsym_num="+sym_num+"\ninit_line_num="+init_line_num+"\ntrans_line_num="+trans_line_num+
				"\nemiss_line_num="+emiss_line_num+"\n\n");
		hmmString.append("\\init\n");
		// Add initial symbol probabilities
		hmmString.append(ParseUtils.mapToStringDouble(initials)+"\n");
		
		hmmString.append("\n\\transition\n");
		// Add gram probabilities
		hmmString.append(transitionStr);
		
		
		hmmString.append("\n\\emission\n");
		// Add symbol probabilities
		hmmString.append(emitStr);


		return hmmString.toString();
	}

}
