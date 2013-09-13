package maybe;

// messages sent or received to communicate
public class Message {
	public static final String error_adding_job_table = "error in adding table";
	public static final String request_workload = "send me workload";
	public static final String command_line = "do the command";
	public static final String send_object = "send object";
	
	public static final String send_status = "send";
	public static final String receive_status = "receive";
	
	public static final String terminated = "terminated:";
	public static final String slaveSetup = "slave is set up";
	
	public static final String serializeThread = "serialize";
	public static final String processThread = "process";
	
	public static final String receiveObj = "obj is ready";
	public static final String terminatedStatus = "terminated";
	public static final String runningPrefix = "running@";
	
	public static final String randomProc = "__random__";
	
	public static final String enterSuspend = "into suspend";
	public static final String suspendStatus = "suspending";
	public static final String reRun = "re-start running";
	
	public static final String procPackName = "migrate.";
	public static final String killProc = "kill process";
}
