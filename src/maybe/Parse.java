package maybe;

import java.io.File;

//to parse command lines
public class Parse {	
	static String[] parseCommand(String command){
		String args[] = command.split("\\s+");
		
		// grep process
		if(args[0].equals("Grep")) {
			if (args.length != 4) {
				System.out.println("Usage: Grep <queryString> <inputFile> <outputFile>");
				return null;
			}

			// Check if the input files given are accessible
			File f = new File(args[2]);
			if(!(f.exists() && f.isFile())) {
				System.out.println(args[2] + " is not a file or it does not exist. Try again.");
				return null;
			}
		}
		
		else if(args[0].equals("test")){
			return args;
		}
		
		else if(args[0].equals("ws") && args.length == 1)
			return args;
		
		else if(args[0].equals("ps")){
			return args;
		}
		
		else if(args[0].equals("transfer") && args.length >= 3){
			return args;
		}
		
		else if(args[0].equals("kill") && args.length >= 2){
			return args;
		}
		
		else if(args[0].equals("quit") && args.length == 1)
			return args;
		
		// cat process
		else if(args[0].equals("Cat")) {
			if(args.length < 2) {
				System.out.println("Usage: Cat <filename1> <filename2> ...");
				return null;
			}
			
			// Check if the input files given are accessible
			for(int i=1; i<args.length; i++) {
				File f = new File(args[i]);
				if(!(f.exists() && f.isFile())) {
					System.out.println(args[i] + " is not a file or it does not exist. Try again.");
					return null;
				}
			}
		}
		
		// cmp process
		else if(args[0].equals("Cmp")) {
			if(args.length != 3) {
				System.out.println("Usage: Cmp <filename1> <filename2>");
				return null;
			}
			
			// Check if the input files given are accessible
			for(int i=1; i<args.length; i++) {
				File f = new File(args[i]);
				if(!(f.exists() && f.isFile())) {
					System.out.println(args[i] + " is not a file or it does not exist. Try again.");
					return null;
				}
			}
		}
		
		// Did not match any of the existing processes
		else {
			System.out.println("Invalid Command: Please try again");
			return null;
		}
		
		// Perfect
		return args;
	}
}
//transfer 1:test 10.0.0.20
