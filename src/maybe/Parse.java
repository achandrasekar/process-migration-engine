package maybe;

import migrate.*;

//to parse command lines
public class Parse {
	static void parseCommand(String command){
		String args[] = command.split("\\s+");
		if(args[0].equals("GrepProcess")) {
			try {
				GrepProcess p = new GrepProcess(args);
				new Thread(p, "grep"+p.getPid()).start();
			} catch(Exception e) {
				System.out.println("GrepProcess: Error: " + e);
			}
		}
	}
}
