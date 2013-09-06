package maybe;

//record a slave's ip, port, socket, workload ...
public class Slave_cond {
	public Slave_cond(String addr, int port_num){
		ip = addr;
		port = port_num;
		proc_num = 0;
		isValid = true;
	}
	
	public boolean setProcNum(int num){
		proc_num = num;
		return true;
	}
	
	public boolean setVidality(boolean flag){
		isValid = flag;
		return true;
	}
	
	public String getIp(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public int getLoad(){
		return this.proc_num;
	}
	
	public boolean getValidity(){
		return isValid;
	}
	
	private String ip;
	int port;
	int proc_num;
	boolean isValid;
}
