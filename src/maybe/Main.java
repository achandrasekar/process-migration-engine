package maybe;

public class Main {
	public static void main(String[] args){
		//based on argument type, initiate master_node or slave_node.
		if(args.length == 0){
			Master_node master = new Master_node();
			new Thread(master, "checking").start();		//check and rearrange workload
			new Thread(master, "receiving").start();	//receive message from slaves
			master.execute();					//waiting for user input and arrange work for slaves
		}
		else if(isSlaveArg(args)){
			Slave_node slave = new Slave_node(args[1]);
			slave.execute();					//receive message from master or other slaves
			new Thread(slave).start();
		}
		else{
			System.out.println("Wrong parameters. Example: java maybe/Main -c 127.0.0.1");
		}
	}

	private static boolean isSlaveArg(String[] args){
		if(args.length == 2 && args[0].equals("-c"))
			return true;
		else return false;
	}
}
