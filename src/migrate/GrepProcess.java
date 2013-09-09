package migrate;

import java.io.*;
import java.lang.Thread;
import java.lang.InterruptedException;
import TransactionalIO.*;

public class GrepProcess implements MigratableProcess
{
	public static int counter;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private int pid;

	private volatile boolean suspending;

	public int getPid() {
		return this.pid;
	}
	
	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		this.query = args[0];
		this.inFile = new TransactionalFileInputStream(args[1]);
		this.outFile = new TransactionalFileOutputStream(args[2]);
		this.pid = GrepProcess.counter++;
	}

	public void run()
	{

		try {
			while (!suspending) {
				// Deserialize if its an already existing object and resume from there or simply go with the this reference
				resume();
				
				String line = this.inFile.readLine();

				if (line == null) break;
				
				if (line.contains(query)) {
					this.outFile.writeLine(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}


		suspending = false;
	}

	public void suspend()
	{
		suspending = true;
		while (suspending) {
			try {
				String serializedFile = "Grep" + this.pid + ".ser";
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedFile));
				out.writeObject(this);
				out.flush();
				out.close();
			} catch(IOException e) {
				System.out.println("GrepProcess: Error: " + e);
			}
		}
		
	}
	
	public void resume() {
		try {
			String serializedFile = "Grep" + this.pid + ".ser";
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedFile));
		
			try {	
				GrepProcess p = (GrepProcess)in.readObject();
				in.close();
				this.pid = p.pid;
				this.inFile = p.inFile;
				this.outFile = p.outFile;
				this.query = p.query;
			} catch(Exception e) {
				System.out.println("GrepProcess: Error: " + e);
			}
			
		} catch(FileNotFoundException f) {
			// Nothing needed here
		} catch(IOException e) {
			System.out.println("GrepProcess: Error: " + e);
		}
	}

}