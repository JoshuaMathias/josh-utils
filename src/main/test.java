package main;

import java.util.List;

import tokenize.Tokenizer;

import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

public class test {
	
	public static void main(String[] args) {
		Tokenizer tokenizer = new Tokenizer();
		String origText = "-12.123,232,";
		String filename = "";
		if (args.length > 0) {
			filename = args[0];
			origText = FileUtils.readFile(filename);
		}
		System.out.println("Getting num lines");
//		System.out.println("orig text lines: "+StatUtils.getNumLines(origText));
		
		String tokText = tokenizer.tokenize(origText);
//		System.out.println("tok text lines: "+StatUtils.getNumLines(tokText));
		FileUtils.writeFile(filename+".tok.test", tokText);
	}
}
