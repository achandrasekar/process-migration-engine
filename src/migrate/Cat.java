package migrate;

import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;

import TransactionalIO.*;

public class Cat implements MigratableProcess
{
	private TransactionalFileInputStream  inFile[];
	private int pid;
	private int fileCount;

	private transient volatile boolean suspending;

	public int getPid() {
		return this.pid;
	}
	
	public Cat() {
	}
	
	public Cat(String args[], Integer pid) throws Exception
	{
		// Initialize all the files that needs to be concatenated
		this.inFile = new TransactionalFileInputStream[args.length];
		fileCount = 0;
		for(int i=1; i<args.length; i++) {
			this.inFile[i-1] = new TransactionalFileInputStream(args[i]);
		}
		this.pid = pid;
	}

	public void run()
	{
		
		DataInputStream ds = new DataInputStream(this.inFile[fileCount]);
		
		try {
			while (!suspending) {
				System.out.println("dummy");
				String line = ds.readLine();
				
				// See if its the last file before terminating
				if (line == null) {
					fileCount++;
					if(fileCount >= this.inFile.length) {
						break;
					}
					ds = new DataInputStream(this.inFile[fileCount]);
				}
				
				if(line != null)
					System.out.println(line);  // Outputs the result to console
				
				// Make cat take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("cat: Error: " + e);
		} catch (NullPointerException e) {
			
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