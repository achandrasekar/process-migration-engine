package migrate;

import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;

import TransactionalIO.*;

public class Cat implements MigratableProcess
{
	private TransactionalFileInputStream  inFile[];
	private int pid;

	private volatile boolean suspending;

	public int getPid() {
		return this.pid;
	}
	
	public Cat(String args[], int pid) throws Exception
	{
		// Initialize all the files that needs to be concatenated
		this.inFile = new TransactionalFileInputStream[args.length];
		for(int i=0; i<args.length; i++) {
			this.inFile[i] = new TransactionalFileInputStream(args[i]);
		}
		this.pid = pid;
	}

	public void run()
	{
		int i = 0;
		try {
			while (!suspending) {

				BufferedReader br = new BufferedReader(new InputStreamReader(this.inFile[i]));
				String line = br.readLine();
				
				// See if its the last file before terminating
				if (line == null) {
					i++;
					if(i >= this.inFile.length) {
						break;
					}
				}
				
				System.out.println(line);  // Outputs the result to console
				
				// Make cat take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("cat: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending) {
			try {
				String serializedFile = "Cat" + this.pid + ".ser";
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedFile));
				out.writeObject(this);
				out.flush();
				out.close();
			} catch(IOException e) {
				System.out.println("cat: Error: " + e);
			}
		}
		
	}


}