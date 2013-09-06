package network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Send_msg_handler{

	public Send_msg_handler(String ip_addr, int port_num){
		this.ip = ip_addr;
		this.port = port_num;
	}

	public boolean send_str(String str){
		Socket sock;
		try {
			sock = new Socket(ip, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		PrintWriter out;
		try {
			out = new PrintWriter(sock.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		out.println(str);
		try {
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public static boolean send_line(Socket out_sock, String str){
		PrintWriter out = null;
		try {
			out = new PrintWriter(out_sock.getOutputStream(), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		out.println(str);
		
		return true;
	}
	
	private String ip;
	private int port;
	Socket sock;
}
