package TransactionalIO;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TransactionalFileInputStream extends FileInputStream implements java.io.Serializable {
	protected int fileOffset;
	protected int fileSize;
	volatile boolean fileOpened;
	
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
	
	public String readLine() throws IOException {
		int i, j=0;
		char[] array = new char[2400];
		if(!this.fileOpened && this.fileOffset > 0) {
			this.fileOpened = true;
			this.skip(this.fileOffset);
		}
		i = this.read();
		this.fileOffset++;
		
		while(i != -1 && i != 46) {
			array[j++] = (char)i;
			i = this.read();
			this.fileOffset++;
		}
		
		return new String(array);
	}
}
