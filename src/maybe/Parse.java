package maybe;

import migrate.*;

//to parse command lines
public class Parse {
	static void parseCommand(String command){
		String args[] = command.split("\\s+");
		if(args[0].equals("grep")) {
			try {
				if (args.length < 3) {
					System.out.println("Usage: grep <queryString> <inputFile> <outputFile>");
					throw new Exception("Invalid Arguments");
				}
				//GrepProcess p = new GrepProcess(args);
				//new Thread(p, "grep"+p.getPid()).start();
			} catch(Exception e) {
				System.out.println("grep: Error: " + e);
			}
		}
		else {
			System.out.println("Invalid Command: Please try again");
		}
	}
}
