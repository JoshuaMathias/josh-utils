package shared;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/*
 * Represents a token or word that has a Set of features.
 */
public class FeatureToken {
	Set<String> features;
	String word;
	String tag;

	public static String featureTokensToString(List<List<FeatureToken>> linesTokens, Map<String, Integer> keptFeatures) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<linesTokens.size(); i++) {
			List<FeatureToken> lineTokens = linesTokens.get(i);
			for (int j=0; j<lineTokens.size(); j++) {
				FeatureToken token = lineTokens.get(j);
				sb.append((i+1)+"-"+j+"-"+token.getWord()+" "+token.getTag());
				for (String feature : token.getFeatures()) {
					if (keptFeatures.containsKey(feature)) {
						sb.append(" "+feature+" 1");
					}
				}
				sb.append("\n");
				
			}
		}
		return sb.toString();
	}
	
	public FeatureToken(String word, String tag) {
		this.word = word;
		this.tag = tag;
		features = new LinkedHashSet<String>();
	}
	
	public FeatureToken(Set<String> features) {
		this.features = features;
	}
	
	public void addFeature(String feature) {
		features.add(feature);
	}

	public void removeFeature(String feature) {
		if (features.contains(feature)) {
			features.remove(feature);
		}
	}
	
	public void removeFeatures(List<String> filteredFeatures) {
		for (String feature : filteredFeatures) {
			if (features.contains(feature)) {
				features.remove(feature);
			}
		}
	}
	
	public Set<String> getFeatures() {
		return features;
	}

	public void setFeatures(Set<String> features) {
		this.features = features;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}
	
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(word+" "+tag);
		for (String feature : features) {
			sb.append(" "+feature+" 1");
		}
		return sb.toString();
	}
	
}
