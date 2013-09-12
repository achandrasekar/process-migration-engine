package TransactionalIO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TransactionalFileInputStream extends FileInputStream implements java.io.Serializable {
	protected int fileOffset;
	protected int fileSize;
	volatile boolean fileOpened;
	
	// Constructor which calls its super class and then initializes offset and flag to denote new or process switched file
	public TransactionalFileInputStream(String file) throws FileNotFoundException {
		super(file);
		this.fileOffset = 0;
		this.fileOpened = true;
		try {
			this.fileSize = this.available();
		}
		catch(IOException e) {
			this.fileSize = -1;
		}
	}
	
	// To read a line from stream
	public String readLine() throws IOException {
		int i, j=0;
		char[] array = new char[2400];
		
		// Checking if file is process switched and moves by the offset from serialized stream object
		if(!this.fileOpened && this.fileOffset > 0) {
			this.fileOpened = true;
			this.skip(this.fileOffset);
		}
		
		// Read byte by byte
		i = this.read();
		this.fileOffset++;
		
		// Exits on \n or eof
		while(i != -1 && i != 10) {
			array[j++] = (char)i;
			i = this.read();
			this.fileOffset++;
		}
		
		// Convert the character array into a string and return
		return new String(array);
	}
}
