package maybe;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import migrate.MigratableProcess;
import migrate.test;
import network.Rec_msg_handler;
import network.Send_msg_handler;

public class Slave_node implements Runnable{
	
	public static String master_ip;
	public static int master_port;
	public static int workload = 0;
	public static final int slave_port = 2080;
	public static final int serialization_port = 3080;
	public static RunningObjTable runProc;
	public static ThreadTable tTable;
	
	public Slave_node(String arg){						//arg is the hostname
		master_ip = arg;
		master_port = Master_node.master_port;
		runProc = new RunningObjTable();
		tTable = new ThreadTable();
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
				Rec_msg_handler receiver = new Rec_msg_handler(client_sock);
				if(!receiver.rec_line().equals(Message.receiveObj)){			// if  it is a noise
					receiver.close();
					continue;
				}
				String pidAndName = receiver.rec_line();
				System.out.println("received line is "+pidAndName);
				Migration_handler handler = new Migration_handler(client_sock, pidAndName);
				Thread recThread = new Thread(handler, Message.receive_status);
				recThread.start();
				recThread.join();
				workload++;
				Send_msg_handler sender = new Send_msg_handler(master_ip, master_port);
				sender.send_str(Message.reRun);
				sender.send_str(pidAndName);
				//change workload and tell master to change status
			}
		}catch (ConnectException e){
			System.out.println("cannont connect in serialization receive");
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}





class procCommand implements Runnable{

	private Socket sock;
	private static ReentrantReadWriteLock rwl;
	
	public procCommand(Socket s){
		sock = s;
		rwl = new ReentrantReadWriteLock();
	}
		
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Rec_msg_handler rec_handler;
		
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
		//System.out.println("try:"+message);
		String[] args = message.split("\\s+");
			
			
		if(message.equals(Message.request_workload)){
			this.procReqWorkload();
			return;
		}
		
		else if(message.equals(Message.send_object)){
			this.procSendObj(rec_handler);
			return;
		}
		
		else if(message.equals(Message.killProc)){
			procKill(rec_handler);
			return;
		}
		
		else if(args!= null){				// non-built in commands
			this.procNonBuiltin(message, args, rec_handler);
			return;
		}
	}
	
	private void procReqWorkload(){
		Send_msg_handler.send_line(sock, String.valueOf(Slave_node.workload));
		return;
	}
	
	private void procSendObj(Rec_msg_handler rec_handler){
		String ip_port_command;
		ip_port_command = rec_handler.rec_line();
		System.out.println("command is:"+ip_port_command);
		String pidName = HelperFuncs.getCommandFromMerge(ip_port_command);
		System.out.println("String pidName is:"+pidName);
		rwl.writeLock().lock();
		if(pidName.equals(Message.randomProc))
			pidName = Slave_node.runProc.randomPick();
		if(!Slave_node.runProc.contains(pidName)){
			System.out.println(pidName+" not exist in this machine");
			rwl.writeLock().unlock();
		return;
		}
		this.doSer(ip_port_command, pidName);
		if(Slave_node.runProc.contains(pidName)){
			Slave_node.runProc.deleteNode(pidName);
			Slave_node.workload--;
		}
		rwl.writeLock().unlock();
		Send_msg_handler feedback = new Send_msg_handler(Slave_node.master_ip, Slave_node.master_port);
		feedback.send_str(Message.enterSuspend);
		feedback.send_str(pidName);
		feedback.close();
	}
	
	private void procKill(Rec_msg_handler rec_handler){
		String pidAndName = rec_handler.rec_line();
		System.out.println("in:"+pidAndName);
		MigratableProcess obj = Slave_node.runProc.find(pidAndName);
		if(obj == null){
			System.out.println(pidAndName + "not exists");
			return;
		}
		Thread theThread = Slave_node.tTable.find(pidAndName);
		theThread.interrupt();
		Slave_node.tTable.deleteNode(pidAndName);
		Slave_node.runProc.deleteNode(pidAndName);
		Slave_node.workload--;
		return;
	}
	
	private void procNonBuiltin(String message, String[] args, Rec_msg_handler rec_handler){
		MigratableProcess mProc = null;
		String procName = "";
		try {			
			String pid = rec_handler.rec_line();
			System.out.println("pid is:"+pid);
			procName = pid+":"+message;
			Class<?> newProc = Class.forName("migrate."+args[0]);
			Class[] paramArray = new Class[2];
			paramArray[0] = String[].class;
			paramArray[1] = Integer.class;
			mProc = (MigratableProcess) newProc.getDeclaredConstructor(paramArray)
					.newInstance(new Object[]{args, new Integer(Integer.parseInt(pid))});
			//System.out.println("beforwork:"+Slave_node.workload);
			Slave_node.workload++;
			//System.out.println("aaawork:"+Slave_node.workload);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Thread tt = new Thread(mProc);
		Slave_node.runProc.add(procName.trim(), mProc);
		Slave_node.tTable.add(procName.trim(), tt);
		System.out.println("added procName is :"+ procName.trim());
		tt.start();
		try {
			tt.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Slave_node.runProc.contains(procName)){
			System.out.println("ssssss");
			Slave_node.runProc.deleteNode(procName);
			Slave_node.workload--;
		}
		Send_msg_handler backHandler = new Send_msg_handler(Slave_node.master_ip, Slave_node.master_port);
		backHandler.send_str(Message.terminated);
		backHandler.send_str(procName);
		System.out.println("11procName is "+procName);
	}
	
	private void doSer(String ip_port_command, String pidName){
		String ip_port = HelperFuncs.getIpPortFromMerge(ip_port_command);
		String ip = HelperFuncs.getIpFromMerge(ip_port);
		int port = HelperFuncs.getPortFromMerge(ip_port);
		try {
			System.out.println("ip:"+ip+" port:"+Slave_node.serialization_port);
			Socket se_sock = new Socket(ip, Slave_node.serialization_port);
			Migration_handler mHandler = new Migration_handler(se_sock, pidName);
			Thread sendThread = new Thread(mHandler, Message.send_status);
			sendThread.start();		//new thread dealing with serialization
			sendThread.join();
		}catch(ConnectException e){
			e.printStackTrace();
			System.out.println("connect exception in proc");
		}catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
