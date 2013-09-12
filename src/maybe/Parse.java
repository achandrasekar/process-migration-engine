package maybe;

import migrate.*;
import java.io.File;

//to parse command lines
public class Parse {	
	static boolean parseCommand(String command){
		String args[] = command.split("\\s+");
		
		// grep process
		if(args[0].equals("grep")) {
			if (args.length != 4) {
				System.out.println("Usage: grep <queryString> <inputFile> <outputFile>");
				return false;
			}

			// Check if the input files given are accessible
			File f = new File(args[2]);
			if(!(f.exists() && f.isFile())) {
				System.out.println(args[2] + " is not a file or it does not exist. Try again.");
				return false;
			}
		}
		
		// cat process
		else if(args[0].equals("cat")) {
			if(args.length < 2) {
				System.out.println("Usage: cat <filename1> <filename2> ...");
				return false;
			}
			
			// Check if the input files given are accessible
			for(int i=1; i<args.length; i++) {
				File f = new File(args[i]);
				if(!(f.exists() && f.isFile())) {
					System.out.println(args[i] + " is not a file or it does not exist. Try again.");
					return false;
				}
			}
		}
		
		// Did not match any of the existing processes
		else {
			System.out.println("Invalid Command: Please try again");
			return false;
		}
		
		// Perfect
		return true;
	}
}
