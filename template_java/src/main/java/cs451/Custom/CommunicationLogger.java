package cs451.Custom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CommunicationLogger {
	
	public static final String PATH_TO_OUTPUTS = "./outputs";
	
	private int processId;
	private File logFile;
	private String filePath;
	private FileWriter fileWriter;
	private List<String> toBeWrittenToFile;
			
	public CommunicationLogger(int processId) {
		toBeWrittenToFile = new LinkedList<String>();
		this.processId = processId;
		
		InitLogFile();

	}
	
	public void logSend(int seqNr) {
		String log = String.format("b %d \n", seqNr);
		toBeWrittenToFile.add(log);
	}
	
	public void logDeliver(int srcPort, int seqNr) {
		String log = String.format("d %d %d \n", ProcessIDHelpers.getIdFromPort(srcPort) , seqNr);
	}
	
	//Writes to file and closes the file writer
	public void Close() {
		for(String text : toBeWrittenToFile) {
			try {
				fileWriter.write(text);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void InitLogFile() {
		//Create file
		String outputFileName = String.format("logs_%d", processId);
		filePath = String.format("%s/%s", PATH_TO_OUTPUTS, outputFileName);
		logFile = new File(filePath);
		if(logFile.exists()) {
			logFile.delete();
		}
		try {
			logFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Initialize the FileWriter
		try {
			fileWriter = new FileWriter(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
			
}
