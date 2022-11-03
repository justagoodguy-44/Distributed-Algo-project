package cs451.Custom.Helpers;

public class ProcessIDHelpers {
	
	private static int PROCESS_NB_TO_PORT_OFFSET = 11000;
	
	public static int getIdFromPort(int port) {
		int id = port - PROCESS_NB_TO_PORT_OFFSET;
		if(id < 1) {
			throw new IllegalArgumentException("invalid port number given or the offset is incorrect");
		}
		return id;
	}
	
	public static int getPortFromId(int id) {
		int port = id + PROCESS_NB_TO_PORT_OFFSET;
		if(port < PROCESS_NB_TO_PORT_OFFSET) {
			throw new IllegalArgumentException("invalid id given");
		}
		return port;
	}

}
