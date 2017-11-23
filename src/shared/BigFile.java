package shared;

/*
 * Class for processing very large files.
 * Provides chunking and time reporting capabilities.
 */
public class BigFile {
	long startTime;

	public BigFile() {
		
	}
	
	public void startTimer() {
		startTime = System.nanoTime();
	}
	
	public void printTime() {
		long timeDiff = System.nanoTime()-startTime;
		if (timeDiff > 1000000000) {
			timeDiff /= 1000000000;
			System.err.print(timeDiff+" seconds");
			if (timeDiff > 60) {
				timeDiff /= 60;
				System.err.print(", "+timeDiff+" minutes");
			}
			if (timeDiff > 60) {
				timeDiff /= 60;
				System.err.print(", "+timeDiff+" hours");
			}
			if (timeDiff > 24) {
				timeDiff /= 60;
				System.err.print(", "+timeDiff+" days");
			}
			System.err.println();
		} else {
			if (timeDiff > 1000000) {
				timeDiff /= 1000000;
				System.err.println(timeDiff+" milliseconds");
			} else {
				System.err.println(timeDiff+" nanoseconds");
			}
		}
	}
}
