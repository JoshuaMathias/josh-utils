package functions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileUtils {
	// If the path is a directory, recursively get a list of all the files.
		public static List<File> listFiles(File path) {
			List<File> files = new ArrayList<File>();
			if (path.isDirectory()) {
				for (File currentFile : path.listFiles()) {
					if (currentFile.isFile()) {
						files.add(currentFile);
					} else if (currentFile.isDirectory()) {
						files.addAll(listFiles(currentFile));
					}
				}
			} else if (files.size() == 0 && path.exists()) {
				files.add(path);
			}
			return files;
		}
		
		// If the path is a directory, recursively get a list of all the files.
		// Return the files as a map of folders. Ignores files not in subfolders.
		public static HashMap<String, List<File>> listFolders(File path) {
			HashMap<String, List<File>> fileMap = new HashMap<String, List<File>>();
			if (path.isDirectory()) {
				for (File currentFile : path.listFiles()) {
					if (currentFile.isDirectory()) {
						List<File> files = listFiles(currentFile);
						fileMap.put(currentFile.getName(), files);
					}
				}
			}
			return fileMap;
		}

		//Get a reader that can be read line by line.
		public static BufferedReader getReader(String filename) {
			BufferedReader reader = null;
			try {
				return new BufferedReader( new InputStreamReader(
	                    new FileInputStream(filename), "UTF-8"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return reader;
		}
		
		//Get a writer to write line by line.
		public static BufferedWriter getWriter(String filename) {
			BufferedWriter writer = null;
			try {
				return new BufferedWriter
					    (new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return writer;
		}
		
		// Get content of file as one string from an InputStream.
		public static String readFile(InputStream is) {
			StringBuilder sb = new StringBuilder(512);
			try {
				Reader r = new InputStreamReader(is, "UTF-8");
				int c = 0;
				while ((c = r.read()) != -1) {
					sb.append((char) c);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return sb.toString();
		}
		
		// Get content of file as one string from filename.
		public static String readFile(String filename) {
			try {
				return readFile(new FileInputStream(filename));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		// Get text from file as one string from File object.
		public static String readFile(File file) {
			try {
				return readFile(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
}
