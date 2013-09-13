package maybe;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ThreadTable {
	private HashMap<String, Thread> table;
	ReentrantReadWriteLock rwl;
	
	public ThreadTable(){
		table = new HashMap<String, Thread>();
		rwl = new ReentrantReadWriteLock();
	}
	
	public void add(String k, Thread p){
		rwl.writeLock().lock();
		table.put(k, p);
		rwl.writeLock().unlock();
	}
	
	public Thread find(String key){
		return table.get(key);
	}
	
	public boolean deleteNode(String s){
		if(table.remove(s) == null){
			System.out.println(s +" is not in the hashmap");
			return false;
		}
		return true;
	}
}
