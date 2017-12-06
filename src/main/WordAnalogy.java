package main;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

import shared.BigFile;
import shared.KeysValues;

import functions.FileUtils;
import functions.ParseUtils;
import functions.StatUtils;

/*
 * Calculates D given A, B, C, where C is to D as A is to B.
 * Trains on trained embedded word vectors, and evaluates results.
 */
public class WordAnalogy {


	/**
	 * USAGE: word_analogy.sh vector_file input_dir output_dir flag1 flag2
	 * @param train_file test_file rare_thres feat_thres output_dir
	 */
	public static void main(String[] args) {
		String vector_file = ""; // vectors.txt. Format: w v1 ... vn. Separate line for each word.
		String input_dir = ""; // Test files. Format: A B C D
		String output_dir = ""; // Contains a file for every file in input_dir, with the same name and word lists, except that D
			// is the word selected by the algorithm.
		int flag1 = 0; //  If not 0, normalize vectors. Divide each vn by Z, where Z = sqrt(v1**2 + ... vn**2)
		int flag2 = 0; // If 0, use Euclidean distance. Else, use cosine similarity.
		if (args.length > 4) {
			vector_file = args[0];
			input_dir = args[1];
			output_dir = args[2];
			flag1 = Integer.parseInt(args[3]);
			flag2 = Integer.parseInt(args[4]);
		} else {
			System.out.println("USAGE: vector_file input_dir output_dir flag1 flag2");
			System.exit(0);
		}
		if (flag2 == 0) {
			StatUtils.setEuclidMeasure();
		}
//		Double[] testDoubles = new ArrayList<Double>();
//		Double[] testDoubles2 = new ArrayList<Double>();
//		double test1 = .5;
//		double test2 = .4;
//		double test3 = -.3;
//		double test4 = .8;
//		testDoubles.add(test1);
//		testDoubles.add(test2);
//		testDoubles2.add(test3);
//		testDoubles2.add(test4);
//		System.out.println(StatUtils.euclideanDistance(testDoubles, testDoubles2));
		BigFile timer = new BigFile();
		timer.startTimer();
		// Store vectors
		String vectorsStr = FileUtils.readFile(vector_file);
		// KeysValues lists
		KeysValues<String, double[]> vectors = ParseUtils.stringToListArray(vectorsStr);
		
		vectors.loadMap();
		
		int numVectors = vectors.size();
//		timer.printNextTime("Parsed vectors:");
		List<String> keys = vectors.getKeys();
		List<double[]> values = vectors.getValues();
		List<DoubleMatrix1D> vectorMatrices = new ArrayList<DoubleMatrix1D>(numVectors);
		double[] sumOfSquares = new double[numVectors];
		for (int i=0; i<numVectors; i++) {
			DenseDoubleMatrix1D nextMatrix = new DenseDoubleMatrix1D(values.get(i));
			vectorMatrices.add(nextMatrix);
			sumOfSquares[i] = Math.sqrt(nextMatrix.zDotProduct(nextMatrix));
		}
		if (flag1 != 0) {
			// Normalize vectors

			for (int lineI=0; lineI<numVectors; lineI++) {
				double[] points = values.get(lineI);
				double numPoints = points.length;
				double Z = StatUtils.getSumOfSquaresRoot(points);
				for (int vi=0; vi<numPoints; vi++) {
					points[vi] = points[vi]/Z;
				}
			}
		}
		// Verify that each vector has the same number of points.
		int numPoints = values.get(0).length;
		for (int i=0; i<vectors.size(); i++) {
			double[] points = values.get(i);
			if (points.length != numPoints) {
				System.err.println("Vector "+i+" has "+points.length+" points instead of "+numPoints+": "+keys.get(i)+" "+values);
				System.exit(1);
			}
		}
//		timer.printNextTime("Prepared vectors:");
		// Go through each question/example, keeping track of the accuracy.
		// Euclidean distance: sqrt/((q1-p1)**2+...+(qn-pn)**2)
		// Cosine similarity: Multiply every corresponding value in the two lists of points (dot product).
		// 	Then, divide by (len(A)*len(B)).
		
		List<File> inputFiles = FileUtils.listFiles(input_dir);
		// Calculate accuracy for every file.
		double[] defaultPoints = new double[numPoints];
		defaultPoints = StatUtils.fillArrayDouble(defaultPoints, 0.0);
		vectors.setDefaultValue(defaultPoints); // For OOV words.
		int sumCorrect = 0;
		double sumExamples = 0;
		for (int fileI=0; fileI<inputFiles.size(); fileI++) {
			int numCorrect = 0;
			File file = inputFiles.get(fileI);
			List<List<String>> wordExamples = ParseUtils.splitLinesWhitespace(FileUtils.readFile(file));
			Writer outWriter = FileUtils.getWriter(output_dir+File.separator+file.getName());
			double[] AMinusB = null;
			String lastA = null;
			String lastB = null;
			String AWord = null;
			String BWord = null;
			String CWord = null;
			String DWord = null;
			double[] AVector = null;
			double[] BVector = null;
			for (int exI=0; exI<wordExamples.size(); exI++) {
				// A: 0, B: 1, C: 2, D: 3
				List<String> example = wordExamples.get(exI);
				if (example.size() <= 3) {
					System.err.println("Line has less than four words: "+ParseUtils.listToString(example));
				}
				AWord = example.get(0);
				BWord = example.get(1);
				CWord = example.get(2);
				DWord = example.get(3);
				// D = B - A + C
				if (lastA == null || !AWord.equals(lastA)) { // If A is different from last time.
					AVector = vectors.getByKey(AWord);
					if (lastB == null || !BWord.equals(lastB)) {
						BVector = vectors.getByKey(BWord);
					}
					AMinusB = StatUtils.subtract(BVector,AVector);
				} else if (lastB == null || !BWord.equals(lastB)) {
					BVector = vectors.getByKey(BWord);
					AMinusB = StatUtils.subtract(BVector,AVector);
				}
				
				double[] ABPlusC = StatUtils.add(AMinusB,vectors.getByKey(CWord));
//				DoubleMatrix1D ABPlusCVector = new DenseDoubleMatrix1D(ABPlusC);
//				double[] ABPlusC = AMinusB;
				// Find the word vector with the least distance from vector ABPlusC
				double bestDiff;
				String bestWord = "";
//				System.out.println("example: "+example);
				if (flag2 == 0) {
					bestDiff = Double.POSITIVE_INFINITY;
					for (int vectorI=0; vectorI<vectors.size(); vectorI++) {
						double[] DVector = values.get(vectorI);
//						DoubleMatrix1D DVector = vectorMatrices.get(vectorI);
						double diff = StatUtils.euclideanDistance(ABPlusC, DVector);
//						System.out.println("diff: "+diff);
						if (diff < bestDiff) {
							bestDiff = diff;
							bestWord = keys.get(vectorI);
//							System.out.println("new best word: "+bestWord+" diff: "+bestDiff);
							
						}
					}
				} else {
					bestDiff = 0.0;
					DoubleMatrix1D ABPlusCVector = new DenseDoubleMatrix1D(ABPlusC);
					for (int vectorI=0; vectorI<vectors.size(); vectorI++) {
//						double[] DVector = values.get(vectorI);
//						double diff = StatUtils.cosineSimilarity(ABPlusC, DVector);
						DoubleMatrix1D DVector = vectorMatrices.get(vectorI);
//						double diff = StatUtils.cosineSimilarity(ABPlusCVector, DVector);
						double diff = StatUtils.cosineSimilarity(ABPlusCVector, DVector, sumOfSquares[vectorI]);
//						System.out.println("diff: "+diff);
						if (diff > bestDiff) {
							bestDiff = diff;
							bestWord = keys.get(vectorI);
//							System.out.println("new best word: "+bestWord+" diff: "+bestDiff);
						}
					}
				}
				try {
					outWriter.write(AWord+" "+BWord+" "+CWord+" "+bestWord+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (DWord.equals(bestWord)) {
					numCorrect++;
				}
			}
			double fileAcc = numCorrect / (double)wordExamples.size() * 100;
			String accStr = String.format("%.2f", fileAcc);
					
			System.out.println(file.getName()+":\nACCURACY TOP1: "+accStr+"% ("+numCorrect+"/"+wordExamples.size()+")");
			sumCorrect += numCorrect;
			sumExamples += wordExamples.size();
//			timer.printNextTime("Finished file "+file.getName());
			try {
				outWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		double totalAcc = sumCorrect / sumExamples * 100;
		String accStr = String.format("%.2f", totalAcc);
		System.out.println("\nTotal accuracy: "+accStr+"% ("+sumCorrect+"/"+(int)sumExamples+")");
//		timer.printNextTime("End:");
		// Output to stdout:
		/*
		 * filename:
		 * ACCURACY TOP1: <file accuracy>% (correct/numExamples)
		 * ...
		 * 
		 * Total accuracy: <total accuracy>% (corSum/numSum)
		 */
		// correct: Examples where the calculated word D is the same as D in the provided example.
		// accuracy: correct/numExamples (e.g. 25.20%)
	}

}
