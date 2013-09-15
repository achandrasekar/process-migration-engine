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

	private volatile boolean suspending;

	public int getPid() {
		return this.pid;  // returns the pid
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

		try {
			while (!suspending) {

				BufferedReader br = new BufferedReader(new InputStreamReader(this.inFile));
				String line = br.readLine();

				if (line == null) break;
				
				if (line.contains(query)) {
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.outFile));
					bw.write(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore it
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