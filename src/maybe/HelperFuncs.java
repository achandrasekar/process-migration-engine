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
	
	private static final String demiliter = "|||";
}
