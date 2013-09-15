package migrate;

import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;

import TransactionalIO.*;

public class Cmp implements MigratableProcess
{
	private TransactionalFileInputStream  file1, file2;
	private int pid;
	private int lineCount;

	private transient volatile boolean suspending;

	public int getPid() {
		return this.pid;  // returns the pid
	}
	
	public Cmp(){
		
	}
	
	public Cmp(String args[], Integer pid) throws Exception
	{
		this.file1 = new TransactionalFileInputStream(args[1]);
		this.file2 = new TransactionalFileInputStream(args[2]);
		this.pid = pid;
		this.lineCount = 0;
		System.out.println(args[1] + " " + args[2] + " Differ:");
	}

	public void run()
	{
		DataInputStream stream1 = new DataInputStream(this.file1);
		DataInputStream stream2 = new DataInputStream(this.file2);
		
		try {
			while (!suspending) {
				// Deserialize if its an already existing object and resume from there or simply go with the this reference
				String line1 = stream1.readLine();
				String line2 = stream2.readLine();
				
				// Terminate if either of the files end
				if (line1 == null || line2 == null) break;
				//Keep the line number count
				lineCount++;
				
				if (!line1.equals(line2)) {
					System.out.println("Line "+ lineCount);
				}
				
				// Make cmp take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
						System.out.println("stopped");
						return;
				} 
			}
		}  
		catch (EOFException e) {
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

}