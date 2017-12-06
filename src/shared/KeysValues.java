package shared;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/*
 * Contains a list of keys and a list of values, in corresponding order.
 */
public class KeysValues<keyType, valueType> {
	List<keyType> keys;
	List<valueType> values;
	int size;
	Map<keyType, Integer> map;
	valueType defaultValue;
	
	
	public KeysValues(List<keyType> keys, List<valueType> values) {
		this.keys = keys;
		this.values = values;
		this.size = keys.size();
	}
	
	public void loadMap() {
		map = new HashMap<keyType, Integer>();
		for (int i=0; i<size; i++) {
			map.put(keys.get(i), i);
		}
	}
	
	public valueType getByKey(keyType key) {
		if (!containsKey(key)) {
//			System.out.println("Using default value.");
			return defaultValue;
		}
		return values.get(map.get(key));
	}
	
	public int getIndexByKey(keyType key) {
		if (!containsKey(key)) {
			return -1;
		}
		return map.get(key);
	}
	
	public boolean containsKey(keyType key) {
		return map.containsKey(key);
	}
	
	public void setDefaultValue(valueType defaultValue) {
		this.defaultValue = defaultValue;
	}

	public List<keyType> getKeys() {
		return keys;
	}

	public void setKeys(List<keyType> keys) {
		this.keys = keys;
		this.size = keys.size();
	}

	public List<valueType> getValues() {
		return values;
	}

	public void setValues(List<valueType> values) {
		this.values = values;
		if (values.size() < size) {
			size = values.size();
		}
	}

	public int size() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Map<keyType, Integer> getMap() {
		return map;
	}

	public void setMap(Map<keyType, Integer> map) {
		this.map = map;
	}

	
	
	
}
