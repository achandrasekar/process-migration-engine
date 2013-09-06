package maybe;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// To manage slave_conds in master_node
public class Slave_table {
	public Slave_table(){
		p_queue = new PriorityQueue<Slave_cond>(10, new Slave_comparator());
		rwl = new ReentrantReadWriteLock();
	}
	
	public boolean addTable(String ip, int port){
		String key = ip + "|||" + port;
		System.out.println("key is: " + key);
		Slave_cond slave = new Slave_cond(ip, port);
		rwl.writeLock().lock();
		p_queue.add(slave);
		rwl.writeLock().unlock();
		return true;
	}
	
	// return the node of min workload in form "ip|||port"
	// if no node is in table, return null.
	public String findMin(){
		rwl.readLock().lock();
		Slave_cond slave = p_queue.peek();
		rwl.readLock().unlock();
		String ip_and_port = slave.getIp() + "|||" + slave.getPort();
		return ip_and_port;
	}
	
	public Slave_cond[] getArray(){
		return (Slave_cond[])p_queue.toArray(new Slave_cond[0]);
	}
	
	public boolean delete_item(Slave_cond slave){
		p_queue.remove(slave);
		return true;
	}
	
	private PriorityQueue<Slave_cond> p_queue;
	ReentrantReadWriteLock rwl;
}


//Comparator for slave_cond, comparing based on slave_cond.proc_num
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
