package maybe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import network.Rec_msg_handler;
import network.Send_msg_handler;

public class Master_node implements Runnable {
	private ProcessTable procTable;		// record process info
	public static final int master_port = 1083;	//socket port number
	private Slave_table s_table;	// record slave workload
	private int pid;			// universal id to be given to processes
	private final int wait_interval = 5000;	// time interval to re-connect if connection fail occurs
	
	public Master_node(){
		s_table = new Slave_table();
		procTable = new ProcessTable();
		pid = 0;
	}
	
	public boolean execute(){			
		//execute master_node. waiting for input and choose a slave to do the command.
		Scanner sca = new Scanner(System.in);
		
		try {
			String local = InetAddress.getLocalHost().getHostAddress();
			System.out.println(local);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		while(true){
			System.out.print("==>");
			String command_line = sca.nextLine();	
			
			String[] commandAndArg = Parse.parseCommand(command_line);
			if(commandAndArg ==  null){
				System.out.println("Command not supported or in wrong format");
				continue;
			}
			else if(commandAndArg[0].equals("ps"))				// show all the processes
				procTable.printProcList();
			else if(commandAndArg[0].equals("quit"))			// quit system
				System.exit(1);
			else if(commandAndArg[0].equals("kill"))			// kill a process
				procKill(commandAndArg);	
			else if(commandAndArg[0].equals("transfer"))		// transfer a process to another node
				procTransfer(commandAndArg[1], commandAndArg[2]);	
			else
				this.procCommands(commandAndArg);
		}
	}
	
	private boolean procKill(String[] commandAndArg){
		String pidAndName = commandAndArg[1];
		String nodeIp = this.getProcIp(pidAndName);
		if(nodeIp == null)
			return false;
		Send_msg_handler s_handler = new Send_msg_handler(nodeIp, Slave_node.slave_port);
		s_handler.send_str(Message.killProc);
		s_handler.send_str(pidAndName);
		procTable.terminateProc(pidAndName);
		return true;
	}
	
	private boolean procTransfer(String fromPidName, String toIp){		// transfer frompid toip
		//transfer fromip toip
		//String fromPidName = commandAndArg[1];
		String fromStatus = this.getProcIp(fromPidName);
		if(fromStatus == null)					// something is wrong. just go on next loop. 
			return false;							// Error handling is done in getProcIp() 
		//String toIp = commandAndArg[2];
		int port = Slave_node.slave_port;
		Send_msg_handler s_handler = new Send_msg_handler(fromStatus, port);
		s_handler.send_str(Message.send_object);
		s_handler.send_str(HelperFuncs.mergeIpPortCommand(toIp, Slave_node.serialization_port, fromPidName));
		s_handler.close();
		return true;
	}
	
	private boolean transRandomProc(String fromIp, String toIp){
		int port = Slave_node.slave_port;
		Send_msg_handler s_handler = new Send_msg_handler(fromIp, port);
		s_handler.send_str(Message.send_object);
		s_handler.send_str(HelperFuncs.mergeIpPortCommand(toIp, Slave_node.serialization_port, Message.randomProc));
		s_handler.close();
		return true;
	}
	
	private String getProcIp(String pid){
		String fromStatus = procTable.getStatus(pid);
		if(fromStatus == null){
			System.out.println(pid + " not exist");
			return null;
		}
		System.out.println("fromStatus:"+fromStatus);
		if(fromStatus.equals(Message.terminatedStatus)){
			System.out.println("the process is already terminated");
			return null;
		}
		if(fromStatus.equals(Message.suspendStatus)){
			System.out.println("the process is suspended");
			return null;
		}
		return fromStatus;
	}
	
	private boolean procCommands(String[] commandAndArg){
		String ip_and_port = null;	
		while((ip_and_port = s_table.findMin()) == null){
			System.out.println("no slave machine is available, please wait");
			wait(this.wait_interval);
		}
	
		String ip = HelperFuncs.getIpFromMerge(ip_and_port);
		int port = HelperFuncs.getPortFromMerge(ip_and_port);
		Send_msg_handler s_handler = new Send_msg_handler(ip, port);
		String stdCmd = this.getCommand(commandAndArg);
		pid++;
		s_handler.send_str(stdCmd);		// send user input command	
		s_handler.send_str(String.valueOf(pid));
		String newPidName = String.valueOf(pid)+":"+stdCmd;
		procTable.addProc(newPidName.trim(), ip);		//add process.
		return true;
		// pid:cmd
	}
	
	//sleep for milisecs miliseconds
	private void wait(int milisecs){
		try {
			Thread.sleep(milisecs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private String getCommand(String[] commandAndArg){
		String stdCmd = "";
		for(int i = 0; i < commandAndArg.length; i++)
			stdCmd += commandAndArg[i] + " ";
		stdCmd = stdCmd.trim();
		return stdCmd;
	}

	@Override
	public void run() {					//a new thread used to record and rearrange workload of slaves
		String t_name = Thread.currentThread().getName();

		if(t_name.equals("checking"))		// checking workload of every slaves.	
			this.checkProc();
		else if(t_name.equals("receiving")){
			try {
				this.receiveMessage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else return;
	}
	
	private void checkProc(){	// check workload
		while(true){
			Slave_cond[] slave_array = s_table.getArray();
			this.updata_table(slave_array);
			if(slave_array != null){
				for(int j = 0; j < slave_array.length; j++){
					System.out.print("Node: "+ slave_array[j].getIp()+"\t");
					System.out.println("Process Number: "+String.valueOf(slave_array[j].getLoad()));
				}
			}
			
			this.load_balance();
			
			this.wait(this.wait_interval);
		}
	}
	
	// receive message from slaves and do related work
	private void receiveMessage() throws IOException{
		ServerSocket server_sock = HelperFuncs.createServerSocket(master_port);
		
		while(true){
			Socket client_sock = null;
			String msg = null;
			client_sock = server_sock.accept();

			String remote_ip = client_sock.getInetAddress().getHostAddress();
			Rec_msg_handler receiver;
			try {
				receiver = new Rec_msg_handler(client_sock);
			} catch (ConnectException e1) {
				e1.printStackTrace();
				continue;
			} catch (IOException e1) {
				e1.printStackTrace();
				continue;
			}
			
			msg = receiver.rec_line();
			System.out.printf("msg is %s\n", msg);
			
			if(msg.equals(Message.terminated)){
				String terminatedProc = Rec_msg_handler.receive(client_sock);
				System.out.println("terminatedProc:"+terminatedProc);
				procTable.terminateProc(terminatedProc);
			}
			else if(msg.equals(Message.slaveSetup)){
				if(!s_table.addTable(remote_ip, Slave_node.slave_port)){
					System.out.println("error occured in adding job table");
					Send_msg_handler.send_line(client_sock, Message.error_adding_job_table);
				}
				client_sock.close();
			}
			
			else if(msg.equals(Message.enterSuspend)){
				String proc = receiver.rec_line();
				System.out.println("proc is"+proc);
				procTable.setSuspend(proc);
			}
			if(msg.equals(Message.reRun)){
				String recPid = receiver.rec_line();
				String recIp = receiver.getIp();
				System.out.println("received Pid is:" + recPid);
				procTable.addProc(recPid, recIp);
			}
		}
	}
	
	// update s_table based on received messages
	private boolean updata_table(Slave_cond[] slave_array){
		Socket sock = null;
		for(int i = 0; i < slave_array.length; i++){
			try {
				sock = new Socket(slave_array[i].getIp(), slave_array[i].getPort());
				BufferedReader reader = null;
				reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				PrintWriter writer = null;
				writer = new PrintWriter(sock.getOutputStream(), true);
				writer.println(Message.request_workload);
				int workload = 0;
				workload = Integer.parseInt(reader.readLine());
				slave_array[i].setProcNum(workload);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				continue;
			} catch(ConnectException e){			// connect to the node disconnect
				System.out.println("connect error");
				s_table.delete_item(slave_array[i]);		// delete the slave in the table
				continue;
			}
			catch (IOException e) {
				e.printStackTrace();
				System.out.println("IOex");
				continue;
			}
		}
		return true;
	}
	
	private void load_balance(){					// to balance load of slaves
		int mean_num = 0;
		Slave_cond[] slave_array = s_table.getArray();
		int[] less_array = new int[slave_array.length];
		int[] large_array = new int[slave_array.length];
		for(int i = 0; i < slave_array.length; i++)
			mean_num += slave_array[i].proc_num;
		if(slave_array.length != 0)
			mean_num = mean_num/slave_array.length;

		for(int i = 0; i < slave_array.length; i++){
			if(slave_array[i].proc_num < mean_num){
				less_array[i] = mean_num - slave_array[i].proc_num;
				large_array[i] = -1;
			}
			else if(slave_array[i].proc_num > 1.2 * mean_num + 1){
				large_array[i] = slave_array[i].proc_num - (int)(1.2* mean_num);
				less_array[i] = -1;
			}
			else{
				large_array[i] = -1;
				less_array[i] = -1;
			}
		}
		
		int lessRecord = 0;
		for(int i = 0; i < slave_array.length; i++){
			if(large_array[i] > 0){
				while(large_array[i] > 0){
					while(lessRecord < less_array.length - 1 && less_array[lessRecord] <= 0)
						lessRecord++;
					System.out.println("load balancing");
					less_array[lessRecord]--;
					large_array[i] --;
					// transfer a random process from a busy process to an easy one
					this.transRandomProc(slave_array[i].getIp(), slave_array[lessRecord].getIp());
					s_table.loadMinusMinus(slave_array[i].getIp(), slave_array[i].getPort());
				}
			}
		}
	}
}
