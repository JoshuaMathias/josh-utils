package shared;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import functions.FileUtils;

/*
 * Class for processing a large number of files.
 * Provides chunking and time reporting capabilities.
 */
public class ManyFiles {
	List<File> files;
	List<File> trainingFiles = new ArrayList<File>();
	List<File> testFiles = new ArrayList<File>();
	String pathname;
	
	public ManyFiles(List<String> filenames) {
		
	}
	
	public ManyFiles(String pathname) {
		this.pathname = pathname;
		files = FileUtils.listFiles(pathname);
	}

	/*
	 * Split files into training and test sets, where
	 * trainingRatio is the portion to be used for training.
	 * Round down the training ratio to an integer number of training files.
	 * The first files in the list are used as training files.
	 */
	public void splitTestSet(double trainingRatio) {
		int numTrain = (int)((double)files.size()*trainingRatio);
		trainingFiles = files.subList(0, numTrain);
		testFiles = files.subList(numTrain, files.size());
	}
	
	/*
	 * Sort files by file name.
	 * If reverse, use opposite (by convention descending) order.
	 */
	public void sortFiles(final boolean reverse) {

		Comparator<File> fileNameComparator = new  Comparator<File>() {
			public int compare(File e1, File e2) {
				int comparison;
				if (reverse) {
					comparison = e2.getName().compareTo(e1.getName());
				} else {
					comparison = e1.getName().compareTo(e2.getName());
				}
				return comparison;
			}
		};
		Collections.sort(files, fileNameComparator);

	}
	
	public void sortFiles() {
		sortFiles(false);
	}
	
	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}
	
	public void addFiles(List<File> files) {
		this.files.addAll(files);
	}
	
	public void addFile(File file) {
		this.files.add(file);
	}

	public List<File> getTrainingFiles() {
		return trainingFiles;
	}

	public void setTrainingFiles(List<File> trainingFiles) {
		this.trainingFiles = trainingFiles;
	}
	
	public void addTraining(List<File> moreTraining) {
		this.trainingFiles.addAll(moreTraining);
	}
	
	public void addTraining(File moreTraining) {
		this.trainingFiles.add(moreTraining);
	}


	public List<File> getTestFiles() {
		return testFiles;
	}

	public void setTestFiles(List<File> testFiles) {
		this.testFiles = testFiles;
	}
	
	public void addTest(List<File> moreTest) {
		this.trainingFiles.addAll(moreTest);
	}
	
	public void addTest(File moreTest) {
		this.trainingFiles.add(moreTest);
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}
	
	

}
