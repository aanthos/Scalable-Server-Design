package cs455.scaling.client;

import java.sql.Timestamp;

public class ClientStatistics implements Runnable {

	private final Client client;

	public ClientStatistics(Client client) {
		this.client = client;
	}

	@Override
	public void run() {
		while(true) {
			// wait for 20 seconds each loop
			// TODO Use java.util.Timer in Client class to schedule when ClientStatistics runs
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println("[" + timestamp + "] Total Sent Count: " + client.getTotalSentMessages() + 
					", Total Received Count: " + client.getTotalReceivedMessages());
			
			client.resetTotalSentMessages();
			client.resetTotalReceivedMessages();
		}

	}


}
