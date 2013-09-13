package maybe;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import migrate.MigratableProcess;

// record running objects in slave node.
// we can find the object reference by the pid
public class RunningObjTable {
	private HashMap<String, MigratableProcess> table;
	ReentrantReadWriteLock rwl;
	
	public RunningObjTable(){
		table = new HashMap<String, MigratableProcess>();
		rwl = new ReentrantReadWriteLock();
	}
	
	public void add(String k, MigratableProcess p){
		rwl.writeLock().lock();
		table.put(k, p);
		rwl.writeLock().unlock();
	}
	
	public String randomPick(){
		for(String k : table.keySet()){
			return k;
		}
		return null;
	}
	
	public boolean deleteNode(String s){
		if(table.remove(s) == null){
			System.out.println(s +" is not in the hashmap");
			return false;
		}
		return true;
	}
	
	public MigratableProcess find(String key){
		return table.get(key);
	}
	
	public boolean contains(String key){
		return table.containsKey(key);
	}
	
}
