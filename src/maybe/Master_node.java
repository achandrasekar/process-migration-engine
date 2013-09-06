package maybe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
			String ip_and_port = s_table.findMin();	
			if(ip_and_port == null){
				System.out.println("fill in this code in Master_node->execute");
				//to do: Do It Yourself
			}
			else{
				String ip = HelperFuncs.getIpFromMerge(ip_and_port);
				int port = HelperFuncs.getPortFromMerge(ip_and_port);
				System.out.println("ip"+ip+"port:"+port);
				Send_msg_handler s_handler = new Send_msg_handler(ip, port);
				s_handler.send_str(command_line);
			}
			
		}
	}

	@Override
	public void run() {					//a new thread used to record and rearrange workload of slaves
		// TODO Auto-generated method stub
		String t_name = Thread.currentThread().getName();
		//System.out.println(t_name);
		if(t_name.equals("checking")){
			
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
		else return;
	}
	
	public static final int master_port = 1081;
	private Slave_table s_table;
}
