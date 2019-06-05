package cs455.scaling.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.LinkedList;

import cs455.scaling.hash.Hash;

/*
 * Description
 * 
 */
public class WorkerThread implements Runnable {

	private static final int EIGHT_KB_PAYLOAD = 8129;
	private final ThreadPoolManager threadPoolManager;
	private final LinkedList<String> hashList = new LinkedList<String>();
	private final TaskQueue taskQueue;

	public WorkerThread(ThreadPoolManager threadPoolManager, TaskQueue taskQueue) {
		this.threadPoolManager = threadPoolManager;
		this.taskQueue = taskQueue;
	}

	@Override
	public void run() {
		while(true) {

			//TODO something with batch-size and batch-time here?
			//System.out.println("IS IT HERE? " + threadPoolManager.getWork().get(0).getType());
			Task task = null;
			try {
				//task = threadPoolManager.dequeue();
				task = taskQueue.dequeue();
			} catch (InterruptedException e1) {
				System.out.println("WorkerThread: ThreadPoolManager failed to dequeue task");
				System.out.println(e1.getMessage());
			}

			try {
				//System.out.println("TASK TYPE: " + task.getType());
				// First - Read the data the client sent
//				if(task.getType() == Task.ACCEPT_TASK) {
//					accept(task);
//				}
				if(task.getType() == Task.READ_TASK) {
					read(task);
				}
				// Second - Compute the hash of a byte array of data
				else if(task.getType() == Task.HASH_TASK) {
					computeHash(task);
				}
				// Third - Write the data back to the client
				else if(task.getType() == Task.WRITE_TASK) {
					write(task);
				}

				else {
					System.out.println("Invalid task type");
				}
			}
			catch (Exception e) {
				System.out.println("WorkerThread Error: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

//	private void accept(Task task) throws IOException {
//		System.out.println("HERE");
//		ServerSocketChannel serverChannel = (ServerSocketChannel) task.getSelectionKey().channel();
//
//		SocketChannel clientChannel = serverChannel.accept();
//		clientChannel.configureBlocking(false);
//		SelectionKey clientKey = clientChannel.register(task.getSelector(), SelectionKey.OP_READ);
//
//		// debugging
//		if(clientChannel.isConnected()) {
//			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//			System.out.println("[INFO] - Successfully connected to " + clientChannel.getRemoteAddress() + 
//					" at " + timestamp);
//		}
//		// Enqueues task that will check if client has pending data that needs to be read for the server
//		taskQueue.enqueue(new Task(clientKey, Task.READ_TASK));
//
//		//System.out.println("After enqeueing, workQueue size is " + threadPoolManager.getWork().size());
//		threadPoolManager.incrementActiveClientConnections();
//	}

	/*
	 * Read data from Client 
	 */
	private void read(Task task) throws IOException {
		SelectionKey clientKey = task.getSelectionKey();
		SocketChannel client = (SocketChannel) clientKey.channel();
		ByteBuffer buffer = ByteBuffer.allocate(EIGHT_KB_PAYLOAD);
		int read = 0;

		buffer.clear();
		// TODO Handle read == -1 scenario after rewind() ?
		while(buffer.hasRemaining() && read != -1) {
			read = client.read(buffer);
		}
		//System.out.println("Read is: " + read);

		// make it ready for read() again after write() happens
		buffer.rewind();

		// Places read data into a hash task to be processed using the Hash.java class
		Task hashTask = new Task(clientKey, Task.HASH_TASK, buffer.array());
		//threadPoolManager.enqueue(hashTask);
		taskQueue.enqueue(hashTask);

		// set the key's interest set to writing
		clientKey.interestOps(SelectionKey.OP_WRITE);
	}

	private void computeHash(Task task) {
		String decodedHash = Hash.SHA1FromBytes(task.getHashData());
		Task writeTask = new Task(task.getSelectionKey(), Task.WRITE_TASK, decodedHash);
		//threadPoolManager.enqueue(writeTask);
		taskQueue.enqueue(writeTask);
	}


	private void write(Task task) throws IOException {
		SelectionKey clientKey = task.getSelectionKey();
		SocketChannel client = (SocketChannel) clientKey.channel();
		// Need to pad since SenderThread will stop if it receives anything under 40 characters
		String bufferReadyString = String.format("%40s", task.getComputedHash());
		//ByteBuffer buffer = ByteBuffer.wrap(task.getComputedHash().getBytes());
		ByteBuffer buffer = ByteBuffer.wrap(bufferReadyString.getBytes());
		buffer.rewind();
		//int write = 0;
		while(buffer.hasRemaining()) {
			//write = client.write(buffer);
			client.write(buffer);
		}

		//System.out.println("Wrote bytes :" + write);

		Task readTask = new Task(clientKey, Task.READ_TASK);
		//threadPoolManager.enqueue(readTask);
		taskQueue.enqueue(readTask);

		// set the key's interest set to reading
		clientKey.interestOps(SelectionKey.OP_READ);

		threadPoolManager.incrementTotalMessagesProcessed();
	}
	//	
	//	private synchronized void enqueue(Task task) {
	//		if(!threadPoolManager.getWorkQueue().isEmpty()) {
	//			System.out.println("All worker threads notified of available task");
	//			notifyAll();
	//		}
	//		// TODO Check if you need setter for this
	//		threadPoolManager.addTaskToQueue(task);
	//	}
	//	
	//	private synchronized Task dequeue() throws InterruptedException {
	//		while(threadPoolManager.getWorkQueue().isEmpty()) {
	//			System.out.println("Thread waiting to dequeue");
	//			wait();
	//		}
	//		System.out.println("Thread successfully dequeued!");
	//		return threadPoolManager.removeTaskFromQueue();
	//	}
}