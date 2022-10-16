package cs451.Custom;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigReader {
	
	private File configFile;
	private int nbMessages;
	private int dstPid;
	
	public ConfigReader(String path) {
		configFile = new File(path);
		extractValues();
	}
	
	public int getNbMessages() {
		return nbMessages;
	}
	
	public int getDestPid() {
		return dstPid;
	}
	
	private void extractValues()  {
		Scanner scanner;
		try {
			scanner = new Scanner(configFile);
			nbMessages = scanner.nextInt();
			dstPid = scanner.nextInt();
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	 
	
	
	

}
