package maybe;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import network.Rec_msg_handler;
import network.Send_msg_handler;

public class Slave_node{
	
	public Slave_node(String arg){						//arg is the hostname
		master_ip = arg;
		master_port = Master_node.master_port;
		int count = 0;
		while(!notify_master()){
			count++;
			if(count >= 100){
				System.out.println("Cannot connect to the server");
				System.exit(-1);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("over");
	}
	
	private boolean notify_master(){
		Socket sock;
		try {
			sock = new Socket(master_ip, master_port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		String local_ip = sock.getLocalAddress().getHostAddress();
		int local_port = sock.getLocalPort();
		if(!Send_msg_handler.send_line(sock, local_ip+local_port))
			return false;		
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public void execute(){
		try {
			ServerSocket serv_sock = new ServerSocket(slave_port);
			while(true){
				Socket client_sock = serv_sock.accept();
				String msg = Rec_msg_handler.receive(client_sock);
				this.proc_msg(msg, client_sock);
				System.out.println(msg);
				
				// parse and do the command.
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void proc_msg(String message, Socket sock){
		if(message.equals(Message.request_workload)){
			Send_msg_handler.send_line(sock, String.valueOf(workload));
			return;
		}
	}

	private String master_ip;
	private int master_port;
	private int workload = 1;
	public static final int slave_port = 2080;
}
