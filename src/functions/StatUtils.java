package functions;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Functions for counting and obtaining statistics or information about text or files.
 */
public class StatUtils {
	
	// Prints the number of instances found for the given regex of one character.
	// Prints each character with the number of instances for each character, in descending order.
	public static void printNumInstances(List<File> files, String regex) {
//		Pattern p = Pattern.compile("[\\t\\f\\r\\x0B\u0020\u00A0\u1680\u180E\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u200B\u202F\u205F\u3000\uFEFF]");  // insert your pattern here
		System.out.println(regex+"\n");
		Pattern p = Pattern.compile(regex);
		HashMap<String, Integer> instances = new HashMap<String, Integer>();
		for (File file : files) {
			String fileStr = FileUtils.readFile(file.getAbsolutePath());
			Matcher m = p.matcher(fileStr);
			while (m.find()) {
					instances = incrementOne(instances, ParseUtils.getUnicode(m.group().toCharArray()[0])+" ("+m.group()+")");
			}
		}
		LinkedHashMap<String, Integer> orderedInstances = sortHashMapByValues(instances, false);
		for (String key : orderedInstances.keySet()) {
			System.out.println(key+": "+orderedInstances.get(key));
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
			HashMap<String, Integer> instances = new HashMap<String, Integer>();
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
			LinkedHashMap<String, Integer> orderedInstances = sortHashMapByValues(instances, false);
			for (String key : orderedInstances.keySet()) {
				System.out.println(key+": "+orderedInstances.get(key));
			}
//			writer.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		

		//Sorts a HashMap by values and returns a LinkedHashMap.
		public static LinkedHashMap<String, Integer> sortHashMapByValues(
		        HashMap<String, Integer> passedMap, boolean ascending) {
		    List<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		    List<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
		    if (ascending) {
		    	 Collections.sort(mapValues);
				 Collections.sort(mapKeys);
		    } else {
		    	Comparator<String> stringOrder = Collections.reverseOrder();
		    	Comparator<Integer> intOrder = Collections.reverseOrder();
		    	Collections.sort(mapValues, intOrder);
		    	Collections.sort(mapKeys, stringOrder);
		    }

		    LinkedHashMap<String, Integer> sortedMap =
		        new LinkedHashMap<String, Integer>();

		    Iterator<Integer> valueIt = mapValues.iterator();
		    while (valueIt.hasNext()) {
		        Integer val = valueIt.next();
		        Iterator<String> keyIt = mapKeys.iterator();

		        while (keyIt.hasNext()) {
		            String key = keyIt.next();
		            Integer comp1 = passedMap.get(key);
		            Integer comp2 = val;

		            if (comp1.equals(comp2)) {
		                keyIt.remove();
		                sortedMap.put(key, val);
		                break;
		            }
		        }
		    }
		    return sortedMap;
		}
		
		//Increment the count of a specific key of a map within a map, whether it already exists or not.
		public static HashMap<String, HashMap<String, Integer>> incrementOneMap(HashMap<String, HashMap<String, Integer>> map, String key, String innerKey) {
			HashMap<String, Integer> innerMap = null;
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
		public static HashMap<String, Integer> incrementOne(HashMap<String, Integer> map,
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
		public static HashMap<String, HashMap<String, Double>> incrementDoubleMap(HashMap<String, HashMap<String, Double>> map, String key, String innerKey) {
			HashMap<String, Double> innerMap = null;
			if (!map.containsKey(key)) {
				innerMap = new HashMap<String, Double>();
			} else {
				innerMap = map.get(key);

			}
			map.put(key, incrementDouble(innerMap, innerKey));
			return map;
		}
		
		//Increment the count (Double) of a specific key of a map, whether it already exists or not.
		public static HashMap<String, Double> incrementDouble(HashMap<String, Double> map,
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
}
