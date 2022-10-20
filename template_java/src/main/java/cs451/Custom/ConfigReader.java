package cs451.Custom;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigReader {
	
	private File configFile;
	private long nbMessages;
	private int dstPid;
	
	public ConfigReader(String path) {
		configFile = new File(path);
		extractValues();
	}
	
	public long getNbMessages() {
		return nbMessages;
	}
	
	public int getDestPid() {
		return dstPid;
	}
	
	private void extractValues()  {
		Scanner scanner;
		try {
			scanner = new Scanner(configFile);
			nbMessages = scanner.nextLong();
			dstPid = scanner.nextInt();
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	 
	
	
	

}
