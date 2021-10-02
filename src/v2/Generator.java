package v2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Generator {
	
	public static void main(String[] args) {
		generate();
	}
	
	public static String loadResourceFile(String name) {
		String text = "";
		
		try {
			InputStream inputStream = Generator.class.getClassLoader().getResourceAsStream(name);
			InputStreamReader streamReader;
			streamReader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader reader = new BufferedReader(streamReader);
			for (String line; (line = reader.readLine()) != null;) {
				text += line + "\n";
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public static String loadFile(String path) {
		return loadFile(new File(path));
	}
	
	public static String loadFile(File file) {
		String text = "";
		
		try(Scanner reader = new Scanner(file)) {
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				text += line + "\n";
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return text;
	}
	
	public static void generate() {
		String alpha = "ABCDEFGHI";
		String functionals = "";
		String classes = "";
		String template = loadFile("event.java");
		
		for(int i=0; i<=alpha.length(); i++) {
			String dollars = ",_".repeat(alpha.length() - i);
			String generics = "";
			String params = "";
			String inputs = "";
			for(int j=0; j<i; j++) {
				boolean last = j == i -1;
				generics += alpha.charAt(j);
				params += alpha.charAt(j) + " " + Character.toLowerCase(alpha.charAt(j));
				inputs += Character.toLowerCase(alpha.charAt(j));
				if(!last) {
					generics += ",";
					params += ", ";
					inputs += ", ";
				}
			}
			
			classes += template
				.replaceAll("PI", "P" + i)
				.replaceAll("GENERICDOLLARS", generics + dollars)
				.replaceAll("GENERICS", generics)
				.replaceAll("PARAMS", params)
				.replaceAll("INPUTS", inputs)
				.replaceAll("_", "\\$")
				+ "\n";
		}
		
		System.out.println(functionals + "\n" + classes);
	}
}
