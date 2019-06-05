package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Iterator;

/*
 * Class description
 * 
 * References: 
 * 		https://www.baeldung.com/java-nio-selector
 * 		http://cs.ecs.baylor.edu/~donahoo/practical/JavaSockets2/code/TCPServerSelector.java
 * 		http://tutorials.jenkov.com/java-nio/selectors.html#selecting-channels-via-a-selector
 */

public class Server {
	
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;
	private ThreadPoolManager threadPoolManager;
	
	/*
	 * Constructor
	 * 
	 */
	public Server(int portNum, int threadPoolSize, int batchSize, int batchTime) {
		try {
			// With selector, only one thread is used instead of several to manage multiple channels
			// allows channels that are ready to be read/written to be selected
			selector = Selector.open();
			
			// Creates new ServerSocketChannel that does not block
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			
			// Binds channel's socket to local address with specified port number and
			// Registers the channel with Selector to accept new connections.
			serverSocketChannel.socket().bind(new InetSocketAddress(portNum));
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		
		// Start the thread pool manager
		threadPoolManager = new ThreadPoolManager(threadPoolSize, this.selector);

//		startServerStatistics();
	}
	
//	private void startServerStatistics() {
//		ServerStatistics serverStatistics = new ServerStatistics(threadPoolManager);
//		Thread thread = new Thread(serverStatistics);
//		thread.start();
//	}
	
//	/*
//	 * Description
//	 */
//	public void listenForNewClientChannels() throws IOException {
//		// constantly listening for new client connections from selector
//		while(true) {
//			// Blocks until at least one channel is ready for the events that were registered
//			this.selector.select();
//			
//			// Accesses the ready channels via the selected key set through an iterator
//			Iterator keys = selector.selectedKeys().iterator();
//			
//			// loops through keys and tests key to determine what referenced channel by key is ready for
//			while(keys.hasNext()) {
//				SelectionKey key = (SelectionKey) keys.next();
//				
//				// remove current key from set of selected keys
//				keys.remove();
//				
//				// Checks if server socket channel has pending connection requests
//				if(key.isAcceptable()) {
//					accept(key);
//				}
//				
//				// NOTE THESE TWO ARE DONE IN THE WORKER THREAD
////				// Checks if client socket channel has pending data
////				if(key.isReadable()) {
////					//this.read(key);
////				}
////				
////				// checks if client socket channel can be written to
////				// and if the key is valid/channel is not closed
////				if(key.isWritable() && key.isValid()) {
////					//  this.write(key);
////				}
//			}
//		}
//	}
//	
//	public void accept(SelectionKey key) throws IOException {
//		// Contains channel for which this key was created. Type-casts channel as ServerSocketChannel
//		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
//		 
//		// registers unblocking client with the server
//		// SelectionKey register method registers and returns selectionkey
//		SocketChannel client = serverChannel.accept();
//		client.configureBlocking(false);
//		SelectionKey clientSelectionKey = client.register(selector, SelectionKey.OP_READ);
//		
//		// debugging
//		if(client.isConnected()) {
//			System.out.println("Connected to " + client.getRemoteAddress() + "!");
//		}
//		
//		// Enqueues task that will check if client has pending data that needs to be read for the server
//		taskQueue.enqueue(new Task(clientSelectionKey, Task.READ_TASK));
//		
//		//System.out.println("After enqeueing, workQueue size is " + threadPoolManager.getWork().size());
//		threadPoolManager.incrementActiveClientConnections();
//	}
	
	
	public static void main(String args[]) {
		if(args.length == 4) {
			int portNum = Integer.parseInt(args[0]);
			int threadPoolSize = Integer.parseInt(args[1]);
			int batchSize = Integer.parseInt(args[2]);
			int batchTime = Integer.parseInt(args[3]);
			
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println("[INFO] - Server starting up at: " + timestamp);
			Server server = new Server(portNum, threadPoolSize, batchSize, batchTime);
		}
		
		else {
			System.out.println("Invalid number of arguments");
		}
	}
}
