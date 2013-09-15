package maybe;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;


// To manage slave_conds in master_node
public class Slave_table {
	
	private PriorityQueue<Slave_cond> p_queue;
	private ReentrantReadWriteLock rwl;
	
	public Slave_table(){	
		p_queue = new PriorityQueue<Slave_cond>(10, workComparator);
		rwl = new ReentrantReadWriteLock();
	}
	
	public boolean addTable(String ip, int port){
		String key = ip + "|||" + port;
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
		Slave_cond slave = p_queue.poll();
		if(slave == null){
			//System.out.println("slave is null");
			rwl.readLock().unlock();
			return null;
		}
		rwl.readLock().unlock();
		String ip_and_port = slave.getIp() + "|||" + slave.getPort();
		slave.setProcNum(slave.getPort()+1);
		rwl.writeLock().lock();
		p_queue.add(slave);
		rwl.writeLock().unlock();
		return ip_and_port;
	}
	
	public Slave_cond[] getArray(){
		rwl.readLock().lock();
		Slave_cond[] slave_array = (Slave_cond[])p_queue.toArray(new Slave_cond[0]);
		rwl.readLock().unlock();
		return slave_array;
	}
	
	public boolean delete_item(Slave_cond slave){
		rwl.writeLock().lock();
		p_queue.remove(slave);
		rwl.writeLock().unlock();
		return true;
	}
	
	public void loadMinusMinus(String ip, int port){
		Iterator<Slave_cond> it = p_queue.iterator();
		while(it.hasNext()){
			Slave_cond ss = it.next();
			if(ss.getIp().equals(ip) && ss.getPort() == port)
				ss.setProcNum(ss.getLoad() - 1);
		}
	}
	

	
	public static Comparator<Slave_cond> workComparator = new Comparator<Slave_cond>(){
		@Override
		public int compare(Slave_cond arg0, Slave_cond arg1) {
			if(arg0.proc_num < arg1.proc_num)
			{
				return -1;}
			else if(arg0.proc_num > arg1.proc_num){
				return 1;
			}
			return 0;
		}
	};
}


//Comparator for slave_cond, comparing based on slave_cond.proc_num
class Slave_comparator implements Comparator<Slave_cond>{

	@Override
	public int compare(Slave_cond arg0, Slave_cond arg1) {
		if(arg0.proc_num < arg1.proc_num){
			return -1;
		}
		else if(arg0.proc_num > arg1.proc_num){
			return 1;
		}
		else return 0;
	}
	
}

