import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


public class RemoveDuplicated {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		stripDuplicatesFromFile("C:/Users/Jalil0s/Desktop/signature.txt");

	}
	
	public static void stripDuplicatesFromFile(String filename) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
	    
	    Set<String> lines = new HashSet<String>(100000); // maybe should be bigger
	    String line;
	    while ((line = reader.readLine()) != null) {
	        lines.add(line);
	    }
	    reader.close();
	    
	    // Haset complet
	    
	    // 
	    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
	    for (String unique : lines) {
	        writer.write(unique);
	        writer.newLine();
	    }
	    writer.close();
	}

}
