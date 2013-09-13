package maybe;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// record process info
public class ProcessTable {
	private HashMap<String, String> table;	//Example:<pid:cmd, running@ip>
	ReentrantReadWriteLock rwl;
	
	public ProcessTable(){
		table = new HashMap<String, String>();
		rwl = new ReentrantReadWriteLock();
	}
	
	public boolean addProc(String arg, String ip){
		rwl.writeLock().lock();
		table.put(arg, Message.runningPrefix+ip);
		rwl.writeLock().unlock();
		return true;
	}
	
	public boolean terminateProc(String key){
		rwl.writeLock().lock();
		table.put(key, Message.terminatedStatus);
		rwl.writeLock().unlock();
		return true;
	}
	
	public void printProcList(){
		rwl.readLock().lock();
		for(String key : table.keySet()){
			String value = table.get(key);
			if(value.equals("terminated"))
				System.out.println("Process " + key + " was terminated");
			else {
				System.out.println("Process "+ key + " " + table.get(key));
			}
		}
		rwl.readLock().unlock();
	}
	
	public String getStatus(String str){
		String full =  table.get(str);
		if(full == null)
			return full;
		if(full.equals(Message.terminatedStatus)){
			return full;
		}
		else if(full.equals(Message.suspendStatus)){
			return full;
		}
		else{
			return full.substring(Message.runningPrefix.length());
		}
	}
	
	public boolean setSuspend(String key){
		if(!table.containsKey(key))
			return false;
		table.put(key, Message.suspendStatus);
		return true;
	}
}
