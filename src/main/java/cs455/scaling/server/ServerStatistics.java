package cs455.scaling.server;

import java.sql.Timestamp;

public class ServerStatistics implements Runnable {
	
	private final ThreadPoolManager threadPoolManager;
	
	public ServerStatistics(ThreadPoolManager threadPoolManager) {
		this.threadPoolManager = threadPoolManager;
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			double serverThroughput = threadPoolManager.getTotalMessagesProcessed() / 20.0;
			int activeClientConnections = threadPoolManager.getActiveClientConnections();
			double mean = 0;
			if(activeClientConnections != 0 ) {
				mean = (threadPoolManager.getTotalMessagesProcessed() / activeClientConnections) / 20.0;
			}
			double standardDev = ((Math.abs(mean - activeClientConnections)) / 20.0);
			
			System.out.println("[" + timestamp + "] Server Throughput: " + serverThroughput + 
					" messages/s, Active Client Connections: " + activeClientConnections + 
					", Mean Per-client Throughput: " + mean + " messages/s, Std. Dev. Of Per-Client" +
					" Throughput: " + standardDev + " messages/s");
			
			threadPoolManager.resetTotalMessagesProcessed();
		}
	}
}
