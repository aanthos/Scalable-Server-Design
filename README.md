File Descriptions

cs455.scaling.hash

	Hash.java - Contains the method for computing the SHA-1 hash of a byte array of data

cs455.scaling.server

	Server.java - Creates the server socket channel and selector. Initializes the ThreadPoolManager

	ThreadPoolManager.java - Initializes worker threads, task queue, and server statistics thread. Also listens for new client connections and places the task in the task queue
	
	WorkerThread.java - Manages client connections and processes each task. Holds the read, write, and compute hash methods here.

	TaskQueue.java - Maintains tasks in a FIFO queue implemented using a LinkedList. Holds synchronized enqueue and dequeue methods here

	Task.java - Has different types of tasks as work units for worker threads

	ServerStatistics.java - Runs every 20 seconds and outputs statistical information such as server throughput and active client connections. Resets total messages processed each 20 second interval

cs455.scaling.client

	Client.java - Initializes the sender thread and client statistics thread
	
	SenderThread.java - Registers with the server and then sends a randomly generated data packet to the server at a rate of 1000ms/messageRate. Receives hash codes computed from the server. 

	ClientStatistics.java - Runs every 20 seconds and outputs statistical information such as messages sent and messages received.
