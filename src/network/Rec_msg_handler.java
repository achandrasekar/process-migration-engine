package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

public class Rec_msg_handler {
	public Rec_msg_handler(Socket in_sock) throws ConnectException,IOException{
		sock = in_sock;
		reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	}
	
	public String rec_line(){
		String str = null;
		try {
			str = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return str;
	}
	
	public static String receive(Socket sock){
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		String str = null;
		try {
			str = in.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return str;
	}
	
	Socket sock;
	BufferedReader reader;
}
