package TransactionalIO;

import java.io.*;

public class TransactionalFileOutputStream extends OutputStream implements Serializable {
	protected int fileOffset;
	protected String file;
	
	public TransactionalFileOutputStream(String file) throws FileNotFoundException {
		this.file = file;
		this.fileOffset = 0;
	}
	
	public FileOutputStream open() throws IOException {
		FileOutputStream fs = new FileOutputStream(this.file, true);
		return fs;
	}

	public void write(int element) throws IOException {
		FileOutputStream fs = this.open();
		fs.write(element);
		this.fileOffset++;
		fs.close();
    }
	
}
