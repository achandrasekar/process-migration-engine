package migrate;

import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;
import TransactionalIO.*;

public class CmpProcess implements MigratableProcess
{
	private TransactionalFileInputStream  file1, file2;
	private int pid;

	private volatile boolean suspending;

	public int getPid() {
		return this.pid;  // returns the pid
	}
	
	public CmpProcess(String args[], int pid) throws Exception
	{
		this.file1 = new TransactionalFileInputStream(args[1]);
		this.file2 = new TransactionalFileInputStream(args[2]);
		this.pid = pid;
		System.out.println(args[1] + " " + args[2] + " Differ:");
	}

	public void run()
	{
		int i = 0;
		try {
			while (!suspending) {
				// Deserialize if its an already existing object and resume from there or simply go with the this reference
				resume();
				
				String line1 = this.file1.readLine();
				String line2 = this.file2.readLine();
				
				// Terminate if either of the files end
				if (line1 == null || line2 == null) break;
				//Keep the line number count
				i++;
				if (!line1.equals(line2)) {
					System.out.println("Line "+ i);
				}
				
				// Make cmp take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("cmp: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending) {
			try {
				// create a file with process id, so that it can be easily identified
				String serializedFile = "Cmp" + this.pid + ".ser";
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedFile));
				out.writeObject(this);
				out.flush();
				out.close();
			} catch(IOException e) {
				System.out.println("cmp: Error: " + e);
			}
		}
		
	}
	
	public void resume() {
		try {
			// Identify the serialized file
			String serializedFile = "Grep" + this.pid + ".ser";
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedFile));
		
			try {	
				// Get the serialized object and set it to the current object
				CmpProcess p = (CmpProcess)in.readObject();
				in.close();
				this.pid = p.pid;
				this.file1 = p.file1;
				this.file2 = p.file2;
			} catch(Exception e) {
				System.out.println("cmp: Error: " + e);
			}
			
		} catch(FileNotFoundException f) {
			// Nothing needed here, this means it hasn't been suspended before
		} catch(IOException e) {
			System.out.println("cmp: Error: " + e);
		}
	}

}