package TransactionalIO;

import java.io.*;

public class TransactionalFileOutputStream extends FileOutputStream implements Serializable {
	protected int fileOffset;
	
	public TransactionalFileOutputStream(String file) throws FileNotFoundException {
		super(file);
		this.fileOffset = 0;
	}
	
	// Appending a line to the output file and tracking the offset
	public void writeLine(String line) throws IOException {
		char[] array = line.toCharArray();
		for(int i=0; i<array.length; i++, this.fileOffset++) {
			this.write(array[i]);
		}
	}
}

