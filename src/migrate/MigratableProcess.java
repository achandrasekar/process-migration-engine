package migrate;

import java.io.Serializable;

//all processes should implement this interface
public interface MigratableProcess extends Runnable, Serializable{
	public void suspend();
}
