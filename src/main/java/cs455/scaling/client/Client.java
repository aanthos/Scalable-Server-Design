package cs455.scaling.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;

/*
 * Client class description
 */

public class Client {
	
	private InetAddress serverHost;
	private int serverPort;
	private int messageRate;
	private Thread senderThread;
	
	private int totalSentMessages = 0;
	private int totalReceivedMessages = 0;
	
	/*
	 * Getter and Setter Methods
	 */
	public synchronized void resetTotalSentMessages() { totalSentMessages = 0 ; }
	public synchronized void resetTotalReceivedMessages() { totalReceivedMessages = 0 ; }
	public synchronized void incrementTotalSentMessages() { totalSentMessages++; }
	public synchronized void incrementTotalReceivedMessages() { totalReceivedMessages++; }
	public synchronized int getTotalSentMessages() { return totalSentMessages; }
	public synchronized int getTotalReceivedMessages() { return totalReceivedMessages; }
	
	
	/*
	 * Client constructor.
	 * 
	 * @param serverHost
	 * @param serverPort
	 * @param messageRate
	 * 
	 */
	public Client(String serverHost, String serverPort, String messageRate) {
		try {
			this.serverHost = InetAddress.getByName(serverHost);
		} catch (UnknownHostException uhe) {
			System.out.println(uhe.getMessage());
			System.out.println("Invalid server-host argument");
		}
		
		this.serverPort = Integer.parseInt(serverPort);
		this.messageRate = Integer.parseInt(messageRate);
		
		startSenderThread();
		startClientStatisticsThread();
	}
	
	/*
	 * Description
	 */
	private void startSenderThread() {
		SenderThread sender = new SenderThread(this.serverHost, this.serverPort, this.messageRate, this);
		senderThread = new Thread(sender);
		senderThread.start();
	}
	
	private void startClientStatisticsThread() {
		ClientStatistics clientStats = new ClientStatistics(this);
		Thread thread = new Thread(clientStats);
		thread.start();
	}
	
	public static void main(String[] args) {
		
		if(args.length == 3) {
			Client client = new Client(args[0], args[1], args[2]);
 		}
		else {
			System.out.println("Invalid number of arguments");
		}
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println("[INFO] - Client starting up at: " + timestamp);
	}
	
}
