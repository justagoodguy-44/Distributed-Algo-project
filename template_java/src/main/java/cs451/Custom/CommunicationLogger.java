package cs451.Custom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommunicationLogger {
	
	private static String pathToOutput;
	private File logFile;
	private static FileWriter fileWriter;
	private static Queue<String> toBeWrittenToFile;
			
	public CommunicationLogger() {
		toBeWrittenToFile = new ConcurrentLinkedQueue<String>();		
		InitLogFile();
	}
	
	public static void writeLogsToFile() {
		Close();
	}
	
	public void logSend(int seqNr) {
		String log = String.format("b %d \n", seqNr);
		toBeWrittenToFile.add(log);
	}
	
	public void logDeliver(int srcPort, int seqNr) {
		String log = String.format("d %d %d \n", ProcessIDHelpers.getIdFromPort(srcPort) , seqNr);
		toBeWrittenToFile.add(log);
	}
	
	//Writes to file and closes the file writer
	public static void Close() {
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
	
	public static void setPathToOutput(String pathToOutput) {
		CommunicationLogger.pathToOutput = pathToOutput; 
	}
	
	private void InitLogFile() {
		//Create file
		logFile = new File(pathToOutput);
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
			fileWriter = new FileWriter(pathToOutput);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
			
}
