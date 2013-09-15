package migrate;

import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;

import TransactionalIO.*;

public class Grep implements MigratableProcess
{
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private int pid;

	private transient volatile boolean suspending = false;

	public int getPid() {
		return this.pid;  // returns the pid
	}
	
	public Grep(){
		suspending = false;
	}
	
	public Grep(String args[], Integer pid) throws Exception
	{
		this.query = args[1];
		this.inFile = new TransactionalFileInputStream(args[2]);
		this.outFile = new TransactionalFileOutputStream(args[3]);
		this.pid = pid;
	}
	
	public void run()
	{
		DataInputStream ds = new DataInputStream(this.inFile);
		PrintStream pst = new PrintStream(this.outFile);
		try {
			while (!suspending) {
				
				
				String line = ds.readLine();
				

				if (line == null) break;
				
				
				if (line.contains(query)) {	
					pst.println(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					System.out.println("stopped");
					return;
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("grep: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending) {
			try {
				// create a file with process id, so that it can be easily identified
				String serializedFile = "Grep" + this.pid + ".ser";
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedFile));
				out.writeObject(this);
				out.flush();
				out.close();
			} catch(IOException e) {
				System.out.println("grep: Error: " + e);
			}
		}
		
	}
	
}