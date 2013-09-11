package maybe;

import java.net.Socket;

// Used for handling migration. Every time the slave needs to move out/in a process,
// create a new thread handling of Migration_handler to handle this migration.
public class Migration_handler implements Runnable{
	
	public Migration_handler(Socket s, String str){
		sock = s;
		str = obj_name;		
	}
	@Override
	public void run() {
		String status = Thread.currentThread().getName();
		if(status.equals(Message.send_status)){
			this.serialize_send();
		}
		else if(status.equals(Message.receive_status)){
			this.serialize_receive();
		}
		else return;
	}
	
	private boolean serialize_send(){
		
		return true;
	}
	
	private boolean serialize_receive(){
		
		return true;
	}
	
	Socket sock;
	String obj_name;	// process name. if it is "__random__", then choose any one existing process
						// when used to receive, obj_name is null
}
