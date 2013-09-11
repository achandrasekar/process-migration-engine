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

//Problem to be discussed -- sockets remain open or create a new one when needed?

public class Master_node implements Runnable {
	public Master_node(){
		s_table = new Slave_table();
		procTable = new ProcessTable();
	}
	
	public boolean execute(){			
		//execute master_node. waiting for input and choose a slave to
		//do the command.
		Scanner sca = new Scanner(System.in);
		
		try {
			String local = InetAddress.getLocalHost().getHostAddress();
			System.out.println(local);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true){
			System.out.print("==>");
			String command_line = sca.nextLine();	
			
			if(command_line.equals("ps")){
				procTable.printProcList();
				continue;
			}
			
			String ip_and_port = null;	
			while((ip_and_port = s_table.findMin()) == null){
				System.out.println("no slave machine is available, please wait");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//to do: Do It Yourself
			}
			
			String ip = HelperFuncs.getIpFromMerge(ip_and_port);
			int port = HelperFuncs.getPortFromMerge(ip_and_port);
			System.out.println("ip:"+ip+" port:"+port);
			Send_msg_handler s_handler = new Send_msg_handler(ip, port);
			s_handler.send_str(Message.command_line);	// tell slave node to launch a process
			s_handler.send_str(command_line);		// send user input command
			procTable.addProc(command_line);		//add process.  
		}
	}

	@Override
	public void run() {					//a new thread used to record and rearrange workload of slaves
		// TODO Auto-generated method stub
		String t_name = Thread.currentThread().getName();
		//System.out.println(t_name);
		if(t_name.equals("checking")){
			while(true){
				Slave_cond[] slave_array = s_table.getArray();
				this.updata_table(slave_array);
				if(slave_array != null){
					for(int j = 0; j < slave_array.length; j++)
						System.out.println(String.valueOf(slave_array[j].getLoad()));
				}
				this.load_balance();
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else if(t_name.equals("receiving")){
			ServerSocket server_sock = null;
			try {
				server_sock = new ServerSocket(master_port);
			} catch (IOException e) {
				System.out.println("Cannot establish socket in receiving");
				System.exit(-1);
				e.printStackTrace();
			}
			
			while(true){
				Socket client_sock = null;
				String msg = null;
				try {
					client_sock = server_sock.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String remote_ip = client_sock.getInetAddress().getHostAddress();
				int remote_port = client_sock.getPort();
				msg = Rec_msg_handler.receive(client_sock);
				//String local_ip = client_sock.getInetAddress().getHostAddress();
				//int local_port = client_sock.getLocalPort();
				//System.out.println("local:"+local_ip+local_port);
				//System.out.println(remote_ip+remote_port+":");
				System.out.printf("msg is %s:\n", msg);
				
				if(msg.equals(Message.terminated)){
					procTable.terminateProc(msg.substring(Message.terminated.length()));
				}
				else if(msg.equals(Message.slaveSetup)){
					if(!s_table.addTable(remote_ip, Slave_node.slave_port)){
						System.out.println("error occured in adding job table");
						Send_msg_handler.send_line(client_sock, Message.error_adding_job_table);
					}
					try {
						client_sock.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		else return;
	}
	
	private boolean updata_table(Slave_cond[] slave_array){
		Socket sock = null;
		for(int i = 0; i < slave_array.length; i++){
			try {
				System.out.println(slave_array[i].getIp()+slave_array[i].getPort());
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
				
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			} catch(ConnectException e){			// connect to the node disconnect
				System.out.println("connect error");
				s_table.delete_item(slave_array[i]);		// delete the slave in the table
				continue;
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
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
				Send_msg_handler handler = 
						new Send_msg_handler(slave_array[i].getIp(), slave_array[i].getPort());
				handler.send_str(Message.send_object);
				while(large_array[i] > 0){
					while(lessRecord < less_array.length - 1 && less_array[lessRecord] <= 0)
						lessRecord++;
					less_array[lessRecord]--;
					large_array[i] --;
					handler.send_str(HelperFuncs.mergeIpPortCommand
							(slave_array[lessRecord].getIp(), slave_array[lessRecord].getPort(), "__random__"));
					System.out.println("relieve node:" + slave_array[lessRecord].getIp() + slave_array[lessRecord].getPort());
					//slave_array[i].setProcNum(slave_array[i].getLoad() - 1);
					s_table.loadMinusMinus(slave_array[i].getIp(), slave_array[i].getPort());
				}
			}
		}
	}
	
	private ProcessTable procTable;
	public static final int master_port = 1080;
	private Slave_table s_table;
}
