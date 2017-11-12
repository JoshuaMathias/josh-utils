package lm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	Map<String, Double> trigrams;
	List<String> states;
	
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
		List<String> hmmLines = FileUtils.readLines(hmm_file);
		if (hmmLines.size() > 4) {
			state_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(0),'=').get(1));
			sym_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(1),'=').get(1));
			init_line_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(2),'=').get(1));
			trans_line_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(3),'=').get(1));
			emiss_line_num = Integer.parseInt(ParseUtils.splitChar(hmmLines.get(4),'=').get(1));
		}
		for (String line : hmmLines) {
			if (line.contains("\\init")) {
				break;
			}
		}
		
		int initLineCount = 0;
		for (String line : hmmLines) {
			if (line.contains("\\transition")) {
				break;
			}
			initLineCount++;
			String[] splitLine = line.split("\\s");
			if (splitLine.length > 1) {
				initials.put(splitLine[0], Double.parseDouble(splitLine[1]));
			}
		}

		
		int transLineCount = 0;
		for (String line : hmmLines) {
			if (line.contains("\\emission")) {
				break;
			}
			transLineCount++;
			String[] splitLine = line.split("\\s");
			if (splitLine.length > 2) {
				states.add(splitLine[1]);
				String trigram = splitLine[0]+ParseUtils.splitChar(splitLine[1],'_').get(1);
				trigrams.put(trigram, Double.parseDouble(splitLine[2]));
			}
		}
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
		// Calculate bigram probs.
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
		state_num = (int) Math.pow(tags.size(),maxOrder-1);
		sym_num = symbols.size();
		init_line_num = initials.size();
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
		hmmString.append("state_num="+state_num+"\nsyn_num="+sym_num+"\ninit_line_num="+init_line_num+"\ntrans_line_num="+trans_line_num+
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
