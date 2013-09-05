package TransactionalIO;

import java.io.*;

public class TransactionalFileOutputStream extends FileOutputStream implements Serializable {
	protected int fileOffset;
	
	public TransactionalFileOutputStream(File file) throws FileNotFoundException {
		super(file);
		this.fileOffset = 0;
	}
}

