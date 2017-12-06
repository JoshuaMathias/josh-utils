package functions;
import hep.aida.bin.StaticBin1D;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.doublealgo.Statistic.VectorVectorFunction;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

/*
 * Functions for counting and obtaining statistics or information about text or files.
 */
public class StatUtils {
	static Statistic.VectorVectorFunction vectorFunction;
	
	/*
	 * Settings, parameters.
	 */
	public static void setVectorMeasure(VectorVectorFunction measure) {
		vectorFunction = measure;
	}
	
	public static void setEuclidMeasure() {
		vectorFunction = Statistic.EUCLID;
	}
	
	
	/*
	 * Probabilities
	 */
	
	/*
	 * gramCounts: A List of HashMaps, where each map represents an n-gram.
	 * Returns a list of map of Ngram probabilities, for each n provided in gramCounts.
	 */
	public static List<HashMap<String, Double>> getNGramProbs(List<HashMap<String, Double>> gramCounts) {
		List<HashMap<String, Double>> gramProbs = new ArrayList<HashMap<String, Double>>();
		for (int i=0; i<gramCounts.size(); i++) {
			gramProbs.add(new HashMap<String, Double>());
		}
		HashMap<String, Double> firstGram = gramCounts.get(0);
		double total = getMapTotalDouble(firstGram); // Get total number of tokens.
		HashMap<String, Double> probsMap = gramProbs.get(0);
		for (Entry<String, Double> entry : firstGram.entrySet()) {
			probsMap.put(entry.getKey(), entry.getValue() / total);
		}
		for (int n=1; n<gramCounts.size(); n++) {
			probsMap = gramProbs.get(n);
			HashMap<String, Double> countsMap = gramCounts.get(n);
			HashMap<String, Double> previousMap = gramCounts.get(n-1);
			for (Entry<String, Double> entry : countsMap.entrySet()) {
				String prevGram = ParseUtils.getNWordsStr(entry.getKey(), n); // Get the previous gram.
				try {
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
		return gramProbs;
	}
	
	/*
	 * gramCounts: A List of HashMaps, where each map represents an n-gram.
	 * Returns a list of map of Ngram probabilities, for each n provided in gramCounts.
	 */
	public static List<Map<String, Double>> getNGramTagProbs(List<Map<String, Double>> gramCounts) {
		List<Map<String, Double>> gramProbs = new ArrayList<Map<String, Double>>();
		for (int i=0; i<gramCounts.size(); i++) {
			gramProbs.add(new HashMap<String, Double>());
		}
		Map<String, Double> firstGram = gramCounts.get(0);
		double total = getMapTotalDouble(firstGram); // Get total number of tokens.
		Map<String, Double> probsMap = gramProbs.get(0);
		for (Entry<String, Double> entry : firstGram.entrySet()) {
			probsMap.put(entry.getKey(), entry.getValue() / total);
		}
		for (int n=1; n<gramCounts.size(); n++) {
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
		return gramProbs;
	}
	
	
	/*
	 * Word and gram counting
	 */
	
	
	/*
	 * Writes a vocab file of word\tfrequency on each line, in descending order by frequency.
	 */
	public static Map<String, Integer> writeTokenCounts(String text, String outputFile) {
		Map<String, Integer> tokenCounts = getTokenCounts(text);
		List<Entry<String, Integer>> sortedCounts = sortValues(tokenCounts, true);
		FileUtils.writeFile(outputFile, ParseUtils.listEntriesToString(sortedCounts));
		return tokenCounts;
	}
	
	/*
	 * Returns a map containing each different type of token (key),
	 * with the number of times it appears in the given text (value).
	 */
	public static Map<String, Integer> getTokenCounts(String text) {
		String[] tokens = text.split("\\s+");
		HashMap<String, Integer> tokenCounts = new HashMap<String, Integer>();
		for (int i=0; i<tokens.length; i++) {
			if (tokens[i].length() > 0) {
				incrementOne(tokenCounts, tokens[i]);
			}
		}
		return tokenCounts;
	}
	
	/*
	 * Returns a map containing each different type of token (key),
	 * with the number of times it appears in the given text (value).
	 * Only make counts for the words.
	 * Don't include first and last tokens (<s> and </s>).
	 */
	public static Map<String, Integer> getPOSWordCounts(List<List<String[]>> tokens) {
		HashMap<String, Integer> tokenCounts = new HashMap<String, Integer>();
		for (List<String[]> sentence : tokens) {
			for (int i=1; i<sentence.size()-1; i++) {
				if (sentence.get(i).length > 0) {
					incrementOne(tokenCounts, sentence.get(i)[0]);
				}
			}
		}
		return tokenCounts;
	}
	
	
	/*
	 * Returns a List containing a List for each n-gram order, from n=1 to n=maxOrder,
	 * each map containing each distinct n-gram (key) with its count/frequency as its value.
	 * The entries in the LinkedMap are ordered by count, in ascending order.
	 */
	public static List<List<Entry<String, Integer>>> getNGramCounts(String text, int maxOrder) {
		List<Map<String, Integer>> gramCounts = new ArrayList<Map<String, Integer>>();
		for (int i=0; i<maxOrder; i++) {
			gramCounts.add(new HashMap<String, Integer>());
		}
		if (maxOrder > 0) {
			List<List<String>> lines = ParseUtils.getLinesAsSentences(text);
			int startI=0;
			int currentN=0;
			String gramStr = "";
			for (int lineI=0; lineI<lines.size(); lineI++) {
//				System.out.println("lineI: "+lineI);
				List<String> line = lines.get(lineI);
				
				gramStr = "";
//				System.out.println("gramStr: "+gramStr);
				for (startI=0; startI<line.size(); startI++) {
					for (currentN=0; currentN<maxOrder; currentN++) {
						int endI = startI+currentN;
						if (endI >= line.size()) {
							break;
						}
						if (currentN == 0) {
//							if (endI == 0 || endI == line.size()-1) { // Don't include <s> or </s> as unigrams.
//								continue;
//							}
						} else {
							gramStr += " ";
						}
						gramStr += line.get(endI);
						gramCounts.set(currentN, incrementOne(gramCounts.get(currentN),gramStr));
					}
//					System.out.println("gramStr: "+gramStr+" last count: "+gramCounts.get(currentN-2).get(gramStr));
					gramStr = "";
				}
			}
		}
		List<List<Entry<String, Integer>>> orderedGramCounts = new ArrayList<List<Entry<String, Integer>>>();
		for (Map<String, Integer> gramMap : gramCounts) {
			orderedGramCounts.add(sortValues(gramMap));
		}
		
		return orderedGramCounts;
	}
	
	/*
	 * Character counting
	 */
	
	/*
	 * Return the number of instances of the given char.
	 */
	public static int getNumChar(String text, char ch) {
		int count = 0;
		char[] chars = text.toCharArray();
		for (int i=0; i<chars.length; i++) {
			if (chars[i] == ch) {
				count++;
			}
		}
		return count;
	}
	
	/*
	 * Get number of words.
	 */
	public static int getNumWords(String text) {
		if (text.length() == 0) {
			return 0;
		}
		return getNumChar(text, ' ')+1;
	}
	
	/*
	 * Get number of lines.
	 */
	public static int getNumLines(String text) {
		if (text.length() == 0) {
			return 0;
		}
		return getNumChar(text, '\n')+1;
	}
	
	/*
	 * Regex counting
	 */
	
	// Prints the number of instances found for the given regex of one character.
	// Prints each character with the number of instances for each character, in descending order.
	public static void printNumInstances(List<File> files, String regex) {
//		Pattern p = Pattern.compile("[\\t\\f\\r\\x0B\u0020\u00A0\u1680\u180E\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u200B\u202F\u205F\u3000\uFEFF]");  // insert your pattern here
		System.out.println(regex+"\n");
		Pattern p = Pattern.compile(regex);
		Map<String, Integer> instances = new HashMap<String, Integer>();
		for (File file : files) {
			String fileStr = FileUtils.readFile(file.getAbsolutePath());
			Matcher m = p.matcher(fileStr);
			while (m.find()) {
					instances = incrementOne(instances, ParseUtils.getUnicode(m.group().toCharArray()[0])+" ("+m.group()+")");
			}
		}
		List<Entry<String, Integer>> orderedInstances = sortValues(instances, true);
		for (Entry<String, Integer> entry : orderedInstances) {
			System.out.println(entry.getKey()+": "+entry.getValue());
		}
	}
	
	// Prints the number of instances found for the given regex category (may be multiple characters).
		// Each regex category is separated by ||
		// Prints each category with the number of instances for each category, in descending order.
		// Example regex: "<[^>\n]*>||Off(Off)+||_(_)+"
		public static void printNumCategories(List<File> files, String regex) {
			Pattern regexPattern = Pattern.compile(regex.replace("||", "|"));
			String[] regexes = regex.split("\\|\\|");
			List<Pattern> patterns = new ArrayList<Pattern>();
//			try {
			List<String> foundStrs = new ArrayList<String>();
//			BufferedWriter writer = getLineWriter("output.txt");
			for (String regCategory : regexes) {
				System.out.println("Pattern: "+regCategory);

//				writer.write("Pattern: "+regCategory+"\n");
				patterns.add(Pattern.compile(regCategory));
			}
			Map<String, Integer> instances = new HashMap<String, Integer>();
			for (File file : files) {
				String fileStr = CleanUtils.rmBlankSpace(FileUtils.readFile(file.getAbsolutePath()));
				Matcher m = regexPattern.matcher(fileStr);
				while (m.find()) {
						String foundStr = m.group().replace("\\s", "");
//						System.out.println("foundStr: "+foundStr);
						if (!foundStrs.contains(foundStr)) {
//							System.out.println("foundStr: "+foundStr+"\n");
//							writer.write("foundStr: "+foundStr+"\n\n");
							foundStrs.add(foundStr);
						}
						for (Pattern pattern : patterns) {
							if (pattern.matcher(foundStr).matches()) {
//								System.out.println("Pattern matched: "+pattern.toString());
								instances = incrementOne(instances, pattern.toString());
							}
						}
				}
			}
			List<Entry<String, Integer>> orderedInstances = sortValues(instances, true);
			for (Entry<String, Integer> entry : orderedInstances) {
				System.out.println(entry.getKey()+": "+entry.getValue());
			}
//			writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		
		/*
		 * Sorting
		 */
		
		/*
		 * Sorts a HashMap by keys.
		 */
		
		/*
		 * Default, sort values descending, then keys ascending
		 */
		public static List<Entry<String, Double>> sortValuesThenKeysDouble(Map<String, Double> passedMap) {
			return sortValuesThenKeysDouble(passedMap, true, false);
		}
		
		/*
		 * false: ascending (default sorting). true: descending
		 */
		public static List<Entry<String, Double>> sortValuesThenKeysDouble(Map<String, Double> passedMap, final boolean reverseVal, final boolean reverseKey) {
		    List<Entry<String, Double>> mapList = new LinkedList<Entry<String, Double>>(passedMap.entrySet());
		    Comparator<Entry<String, Double>> entryComparator = new  Comparator<Entry<String, Double>>() {
		    	public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
		    		int valComparison;
		    		if (reverseVal) {
		    			valComparison = e2.getValue().compareTo(e1.getValue());
		    		} else {
		    			valComparison = e1.getValue().compareTo(e2.getValue());
		    		}
		    		if (valComparison == 0) {
		    			if (reverseKey) {
		    				return e2.getKey().compareTo(e1.getKey());
		    			} else {
		    				return e1.getKey().compareTo(e2.getKey());
		    			}
		    		} else {
		    			return valComparison;
		    		}
		    	}
		    };
		    
		    Collections.sort(mapList, entryComparator);
		    return mapList;
		}
		
		/*
		 * Default, sort values descending, then keys ascending
		 */
		public static List<Entry<String, Integer>> sortValuesThenKeys(Map<String, Integer> passedMap) {
			return sortValuesThenKeys(passedMap, true, false);
		}
		
		/*
		 * false: ascending (default sorting). true: descending
		 */
		public static List<Entry<String, Integer>> sortValuesThenKeys(Map<String, Integer> passedMap, final boolean reverseVal, final boolean reverseKey) {
		    List<Entry<String, Integer>> mapList = new LinkedList<Entry<String, Integer>>(passedMap.entrySet());
		    Comparator<Entry<String, Integer>> entryComparator = new  Comparator<Entry<String, Integer>>() {
		    	public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
		    		int valComparison;
		    		if (reverseVal) {
		    			valComparison = e2.getValue().compareTo(e1.getValue());
		    		} else {
		    			valComparison = e1.getValue().compareTo(e2.getValue());
		    		}
		    		if (valComparison == 0) {
		    			if (reverseKey) {
		    				return e2.getKey().compareTo(e1.getKey());
		    			} else {
		    				return e1.getKey().compareTo(e2.getKey());
		    			}
		    		} else {
		    			return valComparison;
		    		}
		    	}
		    };
		    
		    Collections.sort(mapList, entryComparator);
		    return mapList;
		}
		
		/*
		 * Sort by keys and return a LinkedList.
		 * 
		 */
		public static List<Entry<String, Integer>> sortKeys(Map<String, Integer> passedMap, final boolean reverse) {
		    List<Entry<String, Integer>> mapList = new LinkedList<Entry<String, Integer>>(passedMap.entrySet());
		    Comparator<Entry<String, Integer>> entryComparator = new  Comparator<Entry<String, Integer>>() {
		    	public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
		    		if (reverse) {
		    			return e2.getKey().compareTo(e1.getKey());
		    		}
		    		return e1.getKey().compareTo(e2.getKey());
		    	}
		    };
		    
		    Collections.sort(mapList, entryComparator);
		    return mapList;
		}
		
		/*
		 * Sort by keys and return a LinkedList.
		 */
		public static List<Entry<String, Double>> sortKeysDouble(Map<String, Double> passedMap, final boolean reverse) {
		    List<Entry<String, Double>> mapList = new LinkedList<Entry<String, Double>>(passedMap.entrySet());
		    Comparator<Entry<String, Double>> entryComparator = new  Comparator<Entry<String, Double>>() {
		    	public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
		    		if (reverse) {
		    			return e2.getKey().compareTo(e1.getKey());
		    		}
		    		return e1.getKey().compareTo(e2.getKey());
		    	}
		    };
		    
		    Collections.sort(mapList, entryComparator);
		    return mapList;
		}
		
		public static List<Entry<String, Integer>> sortValues(Map<String, Integer> passedMap) {
			return sortValues(passedMap, true);
		}

		// Sorts a HashMap by values and returns a LinkedList.
		// Ascending by default
		// If reverse is true, entries with greater values, or the opposite of the normal ordering, will be first (descending).
		public static List<Entry<String, Integer>> sortValues(
		        Map<String, Integer> passedMap, final boolean reverse) {
		    List<Entry<String, Integer>> mapList = new LinkedList<Entry<String, Integer>>(passedMap.entrySet());
		    Comparator<Entry<String, Integer>> entryComparator = new  Comparator<Entry<String, Integer>>() {
		    	public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
		    		if (reverse) {
		    			return e2.getValue().compareTo(e1.getValue());
		    		}
		    		return e1.getValue().compareTo(e2.getValue());
		    	}
		    };
		    
		    Collections.sort(mapList, entryComparator);
		    return mapList;
		}
		
		public static Map<String, Integer> sortValuesGetMap(
		        Map<String, Integer> passedMap, final boolean reverse) {
		    List<Entry<String, Integer>> mapList = new LinkedList<Entry<String, Integer>>(passedMap.entrySet());
		    Comparator<Entry<String, Integer>> entryComparator = new  Comparator<Entry<String, Integer>>() {
		    	public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
		    		if (reverse) {
		    			return e2.getValue().compareTo(e1.getValue());
		    		}
		    		return e1.getValue().compareTo(e2.getValue());
		    	}
		    };
		    
		    Collections.sort(mapList, entryComparator);
		    LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		    for (Entry<String, Integer> entry : mapList) {
		    	sortedMap.put(entry.getKey(), entry.getValue());
		    }
		    return sortedMap;
		}
		
		public static List<Entry<String, Double>> sortValuesDouble(Map<String, Double> passedMap) {
			return sortValuesDouble(passedMap, true);
		}

		// Sorts a HashMap by values and returns a LinkedList.
		// Ascending by default
		// If reverse is true, entries with greater values, or the opposite of the normal ordering, will be first (descending).
		public static List<Entry<String, Double>> sortValuesDouble(
		        Map<String, Double> passedMap, final boolean reverse) {
		    List<Entry<String, Double>> mapList = new LinkedList<Entry<String, Double>>(passedMap.entrySet());
		    Comparator<Entry<String, Double>> entryComparator = new  Comparator<Entry<String, Double>>() {
		    	public int compare(Entry<String, Double> e1, Entry<String, Double> e2) {
		    		if (reverse) {
		    			return e2.getValue().compareTo(e1.getValue());
		    		}
		    		return e1.getValue().compareTo(e2.getValue());
		    	}
		    };
		    
		    Collections.sort(mapList, entryComparator);
		    return mapList;
		}
		
		/*
		 * Adding counts
		 */
		
		//Increment the count of a specific key of a map within a map, whether it already exists or not.
		public static Map<String, Map<String, Integer>> incrementOneMap(Map<String, Map<String, Integer>> map, String key, String innerKey) {
			Map<String, Integer> innerMap = null;
			if (!map.containsKey(key)) {
				innerMap = new HashMap<String, Integer>();
			} else {
				innerMap = map.get(key);

			}
			map.put(key, incrementOne(innerMap, innerKey));
			return map;
		}
		
		//Increment the count of a specific key of a map, whether it already exists or not.
		public static TreeMap<String, Integer> incrementOne(TreeMap<String, Integer> map,
				String id) {
			Integer totalFreq = 0;
			if (!map.containsKey(id)) {
				totalFreq = 1;
			} else {
				totalFreq = map.get(id) + 1;

			}
			map.put(id, totalFreq);
			return map;
		}
		
		//Increment the count of a specific key of a map, whether it already exists or not.
		public static Map<String, Integer> incrementOne(Map<String, Integer> map,
				String id) {
			Integer totalFreq = 0;
			if (!map.containsKey(id)) {
				totalFreq = 1;
			} else {
				totalFreq = map.get(id) + 1;

			}
			map.put(id, totalFreq);
			return map;
		}
		
		//Increment the count (Double) of a specific key of a map within a map, whether it already exists or not.
		public static Map<String, Map<String, Double>> incrementDoubleMap(Map<String, Map<String, Double>> map, String key, String innerKey) {
			Map<String, Double> innerMap = null;
			if (!map.containsKey(key)) {
				innerMap = new HashMap<String, Double>();
			} else {
				innerMap = map.get(key);

			}
			map.put(key, incrementDouble(innerMap, innerKey));
			return map;
		}
		
		//Increment the count (Double) of a specific key of a map, whether it already exists or not.
		public static Map<String, Double> incrementDouble(Map<String, Double> map,
				String id) {
			Double totalFreq = 0.0;
			if (!map.containsKey(id)) {
				totalFreq = 1.0;
			} else {
				totalFreq = map.get(id) + 1.0;

			}
			map.put(id, totalFreq);
			return map;
		}
		
		/*
		 * Totals
		 */
		public static Double getMapTotalDouble(Map<String, Double> map) {
			double total = 0;
			for (Entry<String, Double> entry : map.entrySet()) {
				total += entry.getValue();
			}
			return total;
		}
		
		public static Integer getMapTotal(Map<String, Integer> map) {
			int total = 0;
			for (Entry<String, Integer> entry : map.entrySet()) {
				total += entry.getValue();
			}
			return total;
		}
		
		public static Double getListTotalDouble(List<Double> list) {
			double total = 0;
			for (Double entry : list) {
				total += entry;
			}
			return total;
		}
		
		public static Double getListTotal(List<Integer> list) {
			double total = 0;
			for (Integer entry : list) {
				total += entry;
			}
			return total;
		}
		
		
		public static Map<String, Double> divideByTotal(Map<String, Integer> map) {
			double total = getMapTotal(map);
			Map<String, Double> normalizedMap = new HashMap<String, Double>();
			for (Entry<String, Integer> entry : map.entrySet()) {
				normalizedMap.put(entry.getKey(), entry.getValue()/total);
			}
			return normalizedMap;
		}
		
		// Returns the input map, with each item normalized by the total.
		public static Map<String, Double> divideByTotalDouble(Map<String, Double> map) {
			double total = getMapTotalDouble(map);
			for (Entry<String, Double> entry : map.entrySet()) {
				map.put(entry.getKey(), entry.getValue()/total);
			}
			return map;
		}
		
		public static Integer getTotalElementsDouble(Map<String, Map<String, Double>> map) {
			int total=0;
			for (Entry<String, Map<String, Double>> entry : map.entrySet()) {
				total+=entry.getValue().size();
			}
			return total;
		}
		
		/*
		 * Return the square root of the sum 
		 */
		public static Double getSumOfSquaresRoot(List<Double> points) {
			double total = 0.0;
			for (int i=0; i<points.size(); i++) {
				double point = points.get(i);
				total += point * point;
			}
			return Math.sqrt(total);
		}
		
		public static Double getSumOfSquaresRoot(double[] points) {
			StaticBin1D pointsBin = new StaticBin1D();
//			double sum = 0.0; // Without using StaticBin
			for (int i=0; i<points.length; i++) {
				pointsBin.add(points[i]);
//				double point = points[i];
//				sum += point * point;
			}
			double sum = pointsBin.sumOfSquares();
			return Math.sqrt(sum);
		}
		
		/*
		 * Comparison
		 */
		
		// Compare two doubles for equality by checking whether the difference between the two doubles is less than a small number.
		public static boolean equalsDouble(double d1, double d2) {
			if (Math.abs(d2-d1) < .000001) {
				return true;
			}
			return false;
		}
		
		/*
		 * Arrays
		 */
		
		public static int[][] fill2DArray(int[][] arr, int val) {
			for (int i=0; i<arr.length; i++) {
				Arrays.fill(arr[i], val);
			}
			return arr;
		}
		
		public static double[][] fill2DArrayDouble(double[][] arr, double val) {
			for (int i=0; i<arr.length; i++) {
				Arrays.fill(arr[i], val);
			}
			return arr;
		}
		
		public static List<Double> fillListDouble(List<Double> arr, int size, double val) {
			for (int i=0; i<size; i++) {
				arr.add(val);
			}
			return arr;
		}
		
		public static double[] fillArrayDouble(double[] arr, double val) {
			for (int i=0; i<arr.length; i++) {
				arr[i] = val;
			}
			return arr;
		}
		
		/*
		 * Distance and Comparison
		 */
		
		public static double getDistance(double[] points1, double[] points2) {
			DoubleMatrix1D matrix1 = new DenseDoubleMatrix1D(points1);
			DoubleMatrix1D matrix2 = new DenseDoubleMatrix1D(points2);
			return vectorFunction.apply(matrix1, matrix2);
		}
		
		/*
		 * Calculate the Euclidean Distance, comparing each corresponding point.
		 * There must be the same number of elements in points1 as in points2.
		 */
		public static double euclideanDistance(List<Double> points1, List<Double> points2) {
			double sum = 0.0;
			for (int i=0; i<points1.size(); i++) {
				double diff = points1.get(i)-points2.get(i);
				sum += diff * diff;
			}
//			return Math.sqrt(sum);
			return sum; // Use where we're only comparing relative distances.
		}
		
		public static double euclideanDistance(double[] points1, double[] points2) {
//			DoubleMatrix1D matrix1 = new DenseDoubleMatrix1D(points1);
//			DoubleMatrix1D matrix2 = new DenseDoubleMatrix1D(points2);
//			return vectorFunction.apply(matrix1, matrix2);
			
			// With for loops
			double sum = 0.0;
			for (int i=0; i<points1.length; i++) {
				double diff = points1[i]-points2[i];
				sum += diff * diff;
			}
//			return Math.sqrt(sum);
			return sum; // Use where we're only comparing relative distances.
		}
		
		public static double euclideanDistance(DoubleMatrix1D matrix1, DoubleMatrix1D matrix2) {
			return vectorFunction.apply(matrix1, matrix2);
		}
		
		/*
		 * Calculate the Cosine Similarity, comparing each corresponding point.
		 * There must be the same number of elements in points1 as in points2.
		 */
		public static double cosineSimilarity(List<Double> points1, List<Double> points2) {
			double dotProduct = dotMultiply(points1, points2);
			double sqSum1 = getSumOfSquaresRoot(points1);
			double sqSum2 = getSumOfSquaresRoot(points2);
			return dotProduct / (sqSum1 * sqSum2);
		}
		
		public static double cosineSimilarity(double[] points1, double[] points2) {
			DoubleMatrix1D matrix1 = new DenseDoubleMatrix1D(points1);
			DoubleMatrix1D matrix2 = new DenseDoubleMatrix1D(points1);
			return matrix1.zDotProduct(matrix2) / (Math.sqrt(matrix1.zDotProduct(matrix1) * matrix2.zDotProduct(matrix2)));
			// Try Algebra.DEFAULT.norm2?
			
			// With for loops
//			double dotProduct = dotMultiply(points1, points2);
//			double sqSum1 = getSumOfSquaresRoot(points1);
//			double sqSum2 = getSumOfSquaresRoot(points2);
//			return dotProduct / (sqSum1 * sqSum2);
		}
		public static double cosineSimilarity(DoubleMatrix1D matrix1, DoubleMatrix1D matrix2) {
			return matrix1.zDotProduct(matrix2) / (Math.sqrt(matrix1.zDotProduct(matrix1) * matrix2.zDotProduct(matrix2)));
		}
		
		public static double cosineSimilarity(DoubleMatrix1D matrix1, DoubleMatrix1D matrix2, double squaresSum2) {
			return matrix1.zDotProduct(matrix2) / (Math.sqrt(matrix1.zDotProduct(matrix1)) * squaresSum2);
		}
		
//		DoubleMatrix1D matrix1 = DoubleFactory1D.dense.make(points1);
		
		
		/*
		 * Subtract each corresponding element in the lists.
		 * Subtract point2 from point1.
		 */
		public static List<Double> subtract(List<Double> points1, List<Double> points2) {
			List<Double> resultList = new ArrayList<Double>();
			for (int i=0; i<points1.size(); i++) {
				resultList.add(points1.get(i) - points2.get(i));
			}
			return resultList;
		}
		
		public static double[] subtract(double[] points1, double[] points2) {
			double[] resultList = new double[points1.length];
			for (int i=0; i<points1.length; i++) {
				resultList[i] = points1[i] - points2[i];
			}
			return resultList;
		}
		
		/*
		 * Add each corresponding element in the lists.
		 * Add point2 to point1.
		 */
		public static List<Double> add(List<Double> points1, List<Double> points2) {
			List<Double> resultList = new ArrayList<Double>();
			for (int i=0; i<points1.size(); i++) {
				resultList.add(points1.get(i) + points2.get(i));
			}
			return resultList;
		}
		
		public static double[] add(double[] points1, double[] points2) {
			double[] resultList = new double[points1.length];
			for (int i=0; i<points1.length; i++) {
				resultList[i] = points1[i] + points2[i];
			}
			return resultList;
		}
		
		/*
		 * Multiply each corresponding element in the lists.
		 */
		public static double dotMultiply(List<Double> points1, List<Double> points2) {
			double sum = 0.0;
			for (int i=0; i<points1.size(); i++) {
				sum += points1.get(i) * points2.get(i);
			}
			return sum;
		}
		
		public static double dotMultiply(double[] points1, double[] points2) {
			DoubleMatrix1D matrix1 = new DenseDoubleMatrix1D(points1);
			DoubleMatrix1D matrix2 = new DenseDoubleMatrix1D(points1);
			return matrix1.zDotProduct(matrix2);
			// With for loops
//			double sum = 0.0;
//			for (int i=0; i<points1.length; i++) {
//				sum += points1[i] * points2[i];
//			}
//			return sum;
		}
 }
