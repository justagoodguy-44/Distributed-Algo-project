package cs451.Custom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class CommunicationLogger {
	
	private static final String PATH_TO_LOGS = "./../../../../../logs";
	private static final int PORT_OFFSET = 11000;
	private int processNumber;
	private File logFile;
	private String filePath;
	private FileWriter fileWriter;
	private List<String> toBeWrittenToFile;
			
	public CommunicationLogger() {
		InitLogFile();
		toBeWrittenToFile = new LinkedList<String>();
	}
	
	public void logSend(int seqNr) {
		String log = String.format("b %i \n", seqNr);
		toBeWrittenToFile.add(log);
	}
	
	public void logDeliver(int srcPort, int seqNr) {
		String log = String.format("d %i %i \n", srcPort - PORT_OFFSET, seqNr);
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
		String logFileName = String.format("logs_%i", processNumber);
		filePath = String.format("%s/%s", PATH_TO_LOGS, logFileName);
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
