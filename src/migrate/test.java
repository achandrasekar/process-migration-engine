package migrate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class test implements MigratableProcess{
	String [] tt;
	int pid;
	
	public test(String[] a, Integer p){
		tt = a;
		pid = p;
	}
	
	public test(){
		
	}
	
	@Override
	
	public void run() {
		// TODO Auto-generated method stub
		try {
			System.out.println("wowowowowow");
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		System.out.println("run suspend");
			try {
				String serializedFile = "test"+this.pid+".ser";
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedFile));
				out.writeObject(this);
				out.flush();
				out.close();
			} catch(IOException e) {
				System.out.println("GrepProcess: Error: " + e);
			}
		
	}

}
