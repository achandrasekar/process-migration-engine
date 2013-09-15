package maybe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import migrate.MigratableProcess;
import network.Send_msg_handler;

// Used for handling migration. Every time the slave needs to move out/in a process,
// create a new thread handling of Migration_handler to handle this migration.
public class Migration_handler implements Runnable{
	
	public Migration_handler(Socket s, String str){
		sock = s;
		pidAndName = str;		
	}
	@Override
	public void run() {
		String status = Thread.currentThread().getName();
		if(status.equals(Message.send_status)){
			this.serialize_send();
		}
		else if(status.equals(Message.receive_status)){
			try {
				this.serialize_receive();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else return;
	}
	
	private boolean serialize_send(){
		MigratableProcess procInface = Slave_node.runProc.find(pidAndName);
		System.out.println("pidAndName:"+pidAndName);
		if(procInface == null){
			System.out.println("object is not found");
			return false;
		}
		procInface.suspend();
		//System.out.println("class name is:"+className);
		Send_msg_handler.send_line(sock, Message.receiveObj);//after joining, tell slave to receive
		Send_msg_handler.send_line(sock, pidAndName);
		System.out.println("yah");
		return true;
	}
	
	private boolean serialize_receive() throws ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException, IOException{
		String className = getClassName(pidAndName);
		String pidStr = pidAndName.substring(0, pidAndName.indexOf(':'));
		System.out.println("class name is:"+className);
		Class<?> newClass = Class.forName(Message.procPackName + className);
		MigratableProcess mProc;
		System.out.println("frist");
		mProc = (MigratableProcess) newClass.newInstance();
		System.out.println("seconde");
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(className+pidStr+".ser"));
		mProc = (MigratableProcess)in.readObject();
		System.out.println("sdsd");
		Thread newProc = new Thread(mProc);
		newProc.start();
		in.close();
		Slave_node.tTable.add(pidAndName, newProc);
		Slave_node.runProc.add(pidAndName, mProc);
		return true;
	}
	
	private String getClassName(String pidAndName){
		String tmp = pidAndName.substring(pidAndName.indexOf(':') + 1);
		if(tmp.indexOf(' ') != -1)
			return tmp.substring(0, tmp.indexOf(' '));
		else
			return tmp.substring(0);
	}
	
	Socket sock;
	String pidAndName;	// process name. if it is "__random__", then choose any one existing process
						// when used to receive, obj_name is null
}
