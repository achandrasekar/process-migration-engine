package maybe;

import migrate.*;

//to parse command lines
public class Parse {
	static String[] parseCommand(String command){
		String args[] = command.split("\\s+");
		if(args[0].equals("GrepProcess")) {
			try {
				//GrepProcess p = new GrepProcess(args);
				//new Thread(p, "grep"+p.getPid()).start();
				return args;
			} catch(Exception e) {
				System.out.println("GrepProcess: Error: " + e);
			}
		}
		
		else if(args[0].equals("test")){
			return args;
		}
		
		else if(args[0].equals("test1")){
			return args;
		}
		
		else if(args[0].equals("ps")){
			return args;
		}
		
		else if(args[0].equals("transfer") && args.length == 3){
			return args;
		}
		
		else if(args[0].equals("kill")){
			return args;
		}
		return null;
	}
}
//transfer 1:test 10.0.0.20