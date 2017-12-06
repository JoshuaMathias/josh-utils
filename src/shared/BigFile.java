package shared;

/*
 * Class for processing very large files.
 * Provides chunking and time reporting capabilities.
 */
public class BigFile {
	long startTime;
	long lastTime;

	public BigFile() {
		
	}
	
	public void startTimer() {
		startTime = System.nanoTime();
		lastTime = System.nanoTime();
	}
	
	public void printTime() {
		long timeDiff = System.nanoTime()-startTime;
		System.err.println(timeDiff/1000000.0+" milliseconds");
		if (timeDiff > 1000000000) {
			timeDiff /= 1000000000.0;
			System.err.print(timeDiff+" seconds");
			if (timeDiff > 60) {
				timeDiff /= 60.0;
				System.err.print(", "+timeDiff+" minutes");
			}
			if (timeDiff > 60) {
				timeDiff /= 60.0;
				System.err.print(", "+timeDiff+" hours");
			}
			if (timeDiff > 24) {
				timeDiff /= 60.0;
				System.err.print(", "+timeDiff+" days");
			}
			System.err.println();
		} else {
			if (timeDiff > 1000000) {
				timeDiff /= 1000000.0;
				System.err.println(timeDiff+" milliseconds");
			} else {
				System.err.println(timeDiff+" nanoseconds");
			}
		}
	}
	
	public void printTime(String message) {
		System.out.println(message);
		printTime();
	}
	
	public void printNextTime(String message) {
		System.out.println(message);
		long timeDiff = System.nanoTime()-lastTime;
		System.err.println("Time since last: "+timeDiff/1000000.0+" milliseconds");
		printTime();
		lastTime = System.nanoTime();
	}
}
