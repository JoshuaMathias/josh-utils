package functions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.ProcessBuilder;
import java.lang.ProcessBuilder.Redirect;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
		
		public static List<File> listFiles(String path) {
			return listFiles(new File(path));
		}
		
		// For each directory in the list, recursively get a list of all the files.
		// Skip list entries that are not directories.
		// Return the files as a map of folders (key) to a list of files (value).
		// Ignores files not in subfolders (files in the root folder).
		public static Map<String, List<File>> mapDirFiles(List<String> dirs) {
			Map<String, List<File>> fileMap = new LinkedHashMap<String, List<File>>();
			for (String dir : dirs) {
				File currentDir = new File(dir);
				if (currentDir.isDirectory()) {
					List<File> files = listFiles(currentDir);
					fileMap.put(currentDir.getName(), files);
				}
			}
			return fileMap;
		}
		
		// If the path is a directory, recursively get a list of all the files.
		// Return the files as a map of folders (key) to a list of files (value).
		// Ignores files not in subfolders (files in the root folder).
		public static Map<String, List<File>> mapSubDirFiles(File path) {
			Map<String, List<File>> fileMap = new LinkedHashMap<String, List<File>>();
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
		
		//Get a writer to write line by line.
		public static BufferedWriter getWriter(OutputStream os) {
			BufferedWriter writer = null;
			try {
				return new BufferedWriter
					    (new OutputStreamWriter(os,"UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return writer;
		}
		
		/*
		 * @args filename text
		 */
		public static void writeFile(String filename, String text) {
			try {
				BufferedWriter writer = getWriter(filename);
//				System.out.println("Writing text: "+text+" to "+filename);
				writer.write(text);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public static void writeFile(OutputStream os, String text) {
			try {
				BufferedWriter writer = getWriter(os);
				writer.write(text);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		
		public static List<String> readLines(String filename) {
			return ParseUtils.splitLines(readFile(filename));
		}
		
		// Get text from a resource file
		public static String readResource(String filename) {
			return readFile(getResource(filename));
		}
		
		public static InputStream getResource(String resource){
		    InputStream is ;
		    //Try with the Thread Context Loader. 
		    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		    if(classLoader != null){
		        is = classLoader.getResourceAsStream(resource);
		        if(is != null){
		            return is;
		        }
		    }
		    //Let's now try with the classloader that loaded this class.
		    classLoader = FileUtils.class.getClassLoader();
		    if(classLoader != null){
		        is = classLoader.getResourceAsStream(resource);
		        if(is != null){
		            return is;
		        }
		    }
		    //Last ditch attempt. Get the resource from the classpath.
		    return ClassLoader.getSystemResourceAsStream(resource);
		}
		
		public static List<String> getFileLines(String filename) {
			return ParseUtils.splitLines(readFile(filename));
		}
		
		public static List<String> getResourceLines(String filename) {
			return ParseUtils.splitLines(readResource(filename));
		}
		
		/*
		 * Get all input as one String, with whitespace.
		 */
		public static String readIn() {
			return readFile(System.in);
		}
		
		/*
		 * Write to standard out. Ensure UTF8
		 */
		public static void writeStdOut(String text) {
			try {
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new InternalError("VM does not support mandatory encoding UTF-8");
			}
			System.out.println(text);
		}
		
		/*
		 * Commands
		 */
		/*
		 * Run a command, without handling output or input.
		 */
		public static void runCommand(String command) {
			System.out.println("Command: "+command);
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * Run a command, without handling output or input.
		 */
		public static void runCommand(String[] command) {
//			System.out.println("Command: "+command);
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * Run a command and redirect standard output and standard input to the given file locations.
		 */
		public static void runCommandOutErr(String command, String outFile, String errFile) {
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectOutput(Redirect.to(new File(outFile)));
			builder.redirectError(Redirect.to(new File(errFile)));
			try {
				builder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * Run a command, and retrieve the output.
		 */
		public static String getCommandOutput(String command) {
			try {
				Process p = Runtime.getRuntime().exec(command);
				return readFile(p.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}
		
		/*
		 * Run a command, and output to stdout.
		 */
		public static void printCommandOutput(String command) {
			System.out.println("Command: "+command);
			try {
				Process p = Runtime.getRuntime().exec(command);
				writeStdOut(readFile(p.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
}
