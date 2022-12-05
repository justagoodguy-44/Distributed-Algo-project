package cs451.Custom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import cs451.Custom.Helpers.ProcessIDHelpers;
import cs451.Custom.Network.NetworkParams;

public class CommunicationLogger {
	
	private static CommunicationLogger logger = null;
	
	private static String pathToOutput;
	private File logFile;
	private static FileWriter fileWriter;
	private static Queue<String> toBeWrittenToFile;
	
	
	public static void setInstance(String pathToOutput) {
		logger = new CommunicationLogger(pathToOutput);
	}
	
	public static CommunicationLogger getInstance() {
		return logger;
	}
			
	private CommunicationLogger(String pathToOutput) {
		this.pathToOutput = pathToOutput;
		this.toBeWrittenToFile = new ConcurrentLinkedQueue<>();
		InitLogFile();
	}
	
	public static void writeLogsToFile() {
		Close();
	}
	
	public void logSend(int seqNr) {
		String log = String.format("b %d \n", seqNr);
		toBeWrittenToFile.add(log);
	}
	
	public void logDeliver(int srcPid, int seqNr) {
		String log = String.format("d %d %d \n", srcPid , seqNr);
		toBeWrittenToFile.add(log);
	}
	
	public void logAgree(Set<Integer> vals) {
		String valsString = "";
		for(Integer val : vals) {
			valsString = valsString + val + " ";
		}
		toBeWrittenToFile.add(valsString);
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
