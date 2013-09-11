package maybe;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import migrate.test;
import network.Rec_msg_handler;
import network.Send_msg_handler;

public class Slave_node implements Runnable{
	
	public Slave_node(String arg){						//arg is the hostname
		master_ip = arg;
		master_port = Master_node.master_port;
		int count = 0;
		while(!notify_master()){
			System.out.println("Cannot connect to server. Retry in 2 seconds.");
			count++;
			if(count >= 100){
				System.out.println("Cannot connect to the server. Terminated.");
				System.exit(-1);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("a new slave node is set up");
	}
	
	private boolean notify_master(){
		Socket sock;
		try {
			sock = new Socket(master_ip, master_port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return false;
		}
		
		//String local_ip = sock.getLocalAddress().getHostAddress();
		//int local_port = sock.getLocalPort();
		if(!Send_msg_handler.send_line(sock, Message.slaveSetup))
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
				procCommand procObj = new procCommand(client_sock);
				new Thread(procObj).start();				
				// parse and do the command.
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Override
	public void run() {									//receiving serialization 
		try {
			ServerSocket server_sock = new ServerSocket(serialization_port);
			while(true)
			{
				Socket client_sock = server_sock.accept();
				Migration_handler handler = new Migration_handler(client_sock, null);
				new Thread(handler, Message.receive_status).start();
			}
		}catch (ConnectException e){
			System.out.println("cannont connect in serialization receive");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private String master_ip;
	private int master_port;
	public static int workload = 1;
	public static final int slave_port = 2080;
	public static final int serialization_port = 3080;

}


class procCommand implements Runnable{
	public procCommand(Socket s){
		sock = s;
	}
	
	
	private Socket sock;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Rec_msg_handler rec_handler;
		System.out.println("outer:"+Slave_node.workload);
		
		try{
			rec_handler = new Rec_msg_handler(sock);
		}catch(ConnectException e){
			System.out.println("Connect Exception in proc");
			return;
		}catch(IOException e){
			System.out.println("IO exception");
			return;
		}
			
		String message = rec_handler.rec_line();
			
		if(message.equals("test")){
			System.out.println("beforwork:"+Slave_node.workload);
			Slave_node.workload++;
			System.out.println("aaawork:"+Slave_node.workload);
			test a = new test();
			Thread tt;
			tt = new Thread(a);
			tt.start();
			try {
				tt.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("after"+Slave_node.workload);
			Slave_node.workload--;
			Send_msg_handler.send_line(sock, Message.terminated+message);
		}
			
		else if(message.equals(Message.request_workload)){
			System.out.println("workload:"+Slave_node.workload);
			Send_msg_handler.send_line(sock, String.valueOf(Slave_node.workload));
			return;
		}
		
		else if(message.equals(Message.send_object)){
			String ip_port_command;
			while((ip_port_command = rec_handler.rec_line()) != null){
				String command = HelperFuncs.getCommandFromMerge(ip_port_command);
				String ip_port = HelperFuncs.getIpPortFromMerge(ip_port_command);
				String ip = HelperFuncs.getIpFromMerge(ip_port);
				int port = HelperFuncs.getPortFromMerge(ip_port);
				try {
					Socket se_sock = new Socket(ip, Slave_node.serialization_port);
					Migration_handler mHandler = new Migration_handler(se_sock, command);
					new Thread(mHandler, "send_status").start();;		//new thread dealing with serialization
				}catch(ConnectException e){
					System.out.println("connect exception in proc");
				}catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					
			}
		}	
	}
}
