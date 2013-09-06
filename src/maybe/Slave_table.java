package maybe;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// To manage slave_conds in master_node
public class Slave_table {
	public Slave_table(){
		hash_table = new HashMap<String, Slave_cond>();
		rwl = new ReentrantReadWriteLock();
	}
	
	public boolean addTable(String ip, int port){
		String key = ip + "|||" + port;
		System.out.println("key is: " + key);
		Slave_cond slave = new Slave_cond(ip, port);
		rwl.writeLock().lock();
		hash_table.put(key, slave);
		rwl.writeLock().unlock();
		return true;
	}
	
	// return the node of min workload in form "ip|||port"
	// if no node is in table, return null.
	public String findMin(){
		int min_value = MAX;
		String min_node = null;
		rwl.readLock().lock();
		for(String key : hash_table.keySet()){
			Slave_cond tmp_slave = hash_table.get(key);
			if(tmp_slave.proc_num < min_value){
				min_node = key;
				min_value = tmp_slave.proc_num;
			}	
		}
		rwl.readLock().unlock();
		return min_node;
	}
	
	private HashMap<String, Slave_cond> hash_table;
	private final int MAX = 100000;
	ReentrantReadWriteLock rwl;
}

class Slave_comparator implements Comparator<Slave_cond>{

	@Override
	public int compare(Slave_cond arg0, Slave_cond arg1) {
		if(arg0.proc_num < arg1.proc_num)
			return -1;
		else if(arg0.proc_num > arg1.proc_num)
			return 1;
		else return 0;
	}
	
}
