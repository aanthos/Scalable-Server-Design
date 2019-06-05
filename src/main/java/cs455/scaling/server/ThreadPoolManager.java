/*
 * Thread pool needs methods that allow:
 * 1) Spare WorkerThread to be retrieved
 * 2) WorkerThread returns itself to the pool after finishing the task 
 */


package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

public class ThreadPoolManager {

	private ArrayList<WorkerThread> workerThreads;
	private final TaskQueue taskQueue;
	private int totalMessagesProcessed = 0;
	private int activeClientConnections = 0;
	private Selector selector;

	private int batchSize;
	private int batchTime;

	/*
	 * Getter and Setter methods
	 */
	public synchronized int getTotalMessagesProcessed() { return totalMessagesProcessed; }
	public synchronized void incrementTotalMessagesProcessed() { totalMessagesProcessed++; }
	public synchronized void resetTotalMessagesProcessed() { totalMessagesProcessed = 0; }

	public synchronized int getActiveClientConnections() { return activeClientConnections; }
	public synchronized void incrementActiveClientConnections() { activeClientConnections++; }
	public synchronized void decrementActiveClientConnections() { activeClientConnections--; }

	/*
	 * Constructor that creates the thread pool
	 * 
	 * @param numThreads number of threads to be created once
	 * 
	 */
	public ThreadPoolManager(int numThreads, Selector selector) {
		// Start the shared task queue
		taskQueue = new TaskQueue();
		this.workerThreads = new ArrayList<WorkerThread>(numThreads);
		this.selector = selector;
//		this.batchSize = batchSize;
//		this.batchTime = batchTime;

		for(int i = 0; i < numThreads; i++) {
			WorkerThread workerThread = new WorkerThread(this, this.taskQueue);
			this.workerThreads.add(workerThread);
		}

		startWorkerThreads(this.workerThreads);

		startServerStatistics();

		try {
			listenForNewClientChannels();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private void startServerStatistics() {
		ServerStatistics serverStatistics = new ServerStatistics(this);
		Thread thread = new Thread(serverStatistics);
		thread.start();
	}

	private void startWorkerThreads(ArrayList<WorkerThread> workerThreads) {
		for(int i = 0; i < workerThreads.size(); i++) {
			Thread thread = new Thread(workerThreads.get(i));
			thread.start();
		}
	}

	public void listenForNewClientChannels() throws IOException {
		// constantly listening for new client connections from selector
		while(true) {
			// Blocks until at least one channel is ready for the events that were registered
			this.selector.select();

			// Accesses the ready channels via the selected key set through an iterator
			Iterator keys = selector.selectedKeys().iterator();

			// loops through keys and tests key to determine what referenced channel by key is ready for
			while(keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();

				// remove current key from set of selected keys
				keys.remove();

				// Checks if server socket channel has pending connection requests
				if(key.isAcceptable()) {
					accept(key);
					//taskQueue.enqueue(new Task(key, Task.ACCEPT_TASK, this.selector));

				}
			}
		}
	}

		public void accept(SelectionKey key) throws IOException {
			// Contains channel for which this key was created. Type-casts channel as ServerSocketChannel
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			 
			// registers unblocking client with the server
			// SelectionKey register method registers and returns selectionkey
			SocketChannel client = serverChannel.accept();
			client.configureBlocking(false);
			SelectionKey clientSelectionKey = client.register(selector, SelectionKey.OP_READ);
			
			// debugging
			if(client.isConnected()) {
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				System.out.println("[INFO] - Successfully connected to " + client.getRemoteAddress() + 
						" at " + timestamp);
			}
			
			// Enqueues task that will check if client has pending data that needs to be read for the server
			taskQueue.enqueue(new Task(clientSelectionKey, Task.READ_TASK));
			
			//System.out.println("After enqeueing, workQueue size is " + threadPoolManager.getWork().size());
			incrementActiveClientConnections();
		}

	/*
	 * Description
	 */
	//	public synchronized void enqueue(Task task) {
	//		// Probably do batch-size and back-time stuff
	//		
	//		// notifies threads that are waiting to dequeue (wait in dequeue method) that a task will be added
	//		if(!workQueue.isEmpty()) {
	//			System.out.println("All worker threads notified of available task");
	//			notifyAll();
	//		}
	//		
	//		workQueue.add(task);
	//	}
	//
	//	public synchronized Task dequeue() throws InterruptedException {
	//		// probably do batch-size and batch-time stuff
	//		
	//		// checks to see if queue is empty. If so, blocks the queue until item is enqueued
	//		while(workQueue.isEmpty()) {
	//			System.out.println("Thread waiting to dequeue");
	//			wait();
	//		}
	//		System.out.println("Thread successfully dequeued!");
	//		return workQueue.remove(0);
	//	}
	//	

}
