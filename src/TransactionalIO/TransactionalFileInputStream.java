package TransactionalIO;

import java.io.*;

public class TransactionalFileInputStream extends InputStream implements java.io.Serializable {
	protected int fileOffset;
	public String file;

	public int getFileOffset() {
		return this.fileOffset;
	}
	
	public TransactionalFileInputStream(String s) throws FileNotFoundException {
		this.file = s;
		this.fileOffset = 0;
	}

	public FileInputStream open() throws IOException {
		FileInputStream fs = new FileInputStream(this.file);
		fs.skip(this.fileOffset);
		return fs;
	}

	public int read() throws IOException {
		FileInputStream fs = this.open();
		int element = fs.read();
		if(element != -1)
			this.fileOffset++;
		fs.close();
		return element;
    }
	
}
