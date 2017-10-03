package main;

import java.util.List;

import functions.ParseUtils;

public class test {
	
	public static void main(String[] args) {
		List<List<String>> words = ParseUtils.splitLinesWords("\n\n test \ntest2  test3  test4 \n");
		for (int i=0; i<words.size(); i++) {
			System.out.println("word "+i+": "+words.get(i));
		}
		
		
	}
}
