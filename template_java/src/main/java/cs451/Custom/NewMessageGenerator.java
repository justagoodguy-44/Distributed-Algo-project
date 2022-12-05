package cs451.Custom;

public class NewMessageGenerator {
	
	private static int nextMsg = 1;
	
	public static int getNewMessage() {
		return nextMsg++;
	}
}
