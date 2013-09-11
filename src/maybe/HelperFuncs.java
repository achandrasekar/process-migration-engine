package maybe;

public class HelperFuncs {
	//return "ip|||port"
	public static String mergeIpPort(String ip, int port){
		return ip + demiliter + port;
	}
	
	public static String getIpFromMerge(String ip_and_port){
		int index = ip_and_port.indexOf(demiliter);
		if(index != -1)
			return ip_and_port.substring(0, index);
		else
			return null;
	}
	
	public static int getPortFromMerge(String ip_and_port){
		int index = ip_and_port.indexOf(demiliter);
		if(index != -1)
			return Integer.parseInt(ip_and_port.substring(index + demiliter.length()));
		else 
			return -1;
	}
	
	public static String getIpPortFromMerge(String ip_port_command){		//"ip|||port$$$commnad"
		int index = ip_port_command.indexOf(c_demiliter);
		if(index != -1)
			return ip_port_command.substring(0, index);
		else
			return null;
	}
	
	public static String getCommandFromMerge(String ip_port_command){
		int index = ip_port_command.indexOf(c_demiliter);
		if(index != -1)
			return ip_port_command.substring(index + c_demiliter.length());
		else 
			return null;
	}
	
	public static String mergeIpPortCommand(String ip, int port, String command){
		return ip+demiliter+port+c_demiliter+command;
	}
	
	private static final String demiliter = "|||";
	private static final String c_demiliter = "$$$";
}
