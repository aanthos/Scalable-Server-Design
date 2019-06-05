package cs455.scaling.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import cs455.scaling.hash.Hash;

/*
 * SenderThread description
 * 
 * References:
 * 		https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SocketChannel.html
 * 		https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SelectionKey.html
 */
public class SenderThread implements Runnable {

	private Selector selector;
	private SocketChannel socketChannel;
	private int messageRate;
	private final Client client;
	private static final int EIGHT_KB_PAYLOAD = 8129;
	private static final int RECEIVED_BYTE_PAYLOAD = 40;
	private final LinkedList<String> hashList = new LinkedList<String>();
	//volatile boolean stop = false;

	/*
	 * Constructor
	 * 
	 */
	public SenderThread(InetAddress IPAddress, int portNum, int messageRate, Client client) {
		this.messageRate = messageRate;
		this.client = client;

		try {
			// Opens selector, opens unblocking socket channel, and then attempts to connect to Server
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.connect(new InetSocketAddress(IPAddress, portNum));

			// registers the channel with the Selector that the channel is ready for writing
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_WRITE);

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			System.out.println("Client failed to connect to the server");
		}
	}

	@Override
	public void run() {
		while(true) {
			try {
				// Blocks until at least one channel is ready for the events that were registered
				this.selector.select();

				// Accesses the ready channels via the selected key set through an iterator
				Iterator keys = selector.selectedKeys().iterator();

				// constantly listening for new client connections from selector

				// loops through keys and tests key to determine what referenced channel by key is ready for
				while(keys.hasNext()) {
					SelectionKey key = (SelectionKey) keys.next();
					keys.remove();

					// checks if server channel can be written to
					if(key.isWritable()) {
						write(key);
					}

					// Checks if server channel has pending data/response to receive
					if(key.isReadable()) {
						//System.out.println("Reading now");
						read(key);
					}
				}
			} catch(Exception e) {
				System.out.println(e.getMessage());
				System.out.println("Error in SenderThread");
			}
		}
	}

	/*
	 * Generates data packet to send to the server.
	 * Sleeps to achieve targeted production rate of 1000/messageRate
	 * 
	 */
	public void write(SelectionKey key) throws IOException, InterruptedException {
		//System.out.println("Writable");
		SocketChannel server = (SocketChannel) key.channel();

		// Generates 8KB data packet and randomly generates values for these bytes
		Random random = new Random();
		byte[] data = new byte[EIGHT_KB_PAYLOAD];
		random.nextBytes(data);

		// Channels will not wait to write all of the data, they will write what they can and move on
		// Allocate buffer of appropriate length, write from buffer until no data remains
		ByteBuffer buffer = ByteBuffer.wrap(data);

		// Goes back to the beginning of the buffer so that it may be read/written fully again
		// important for SocketChannel write method because it only writes remaining bytes after where it left off
		buffer.rewind();

		while(buffer.hasRemaining()) {
			server.write(buffer);
		}

		// Save the hash value in the list
		String dataHash = Hash.SHA1FromBytes(data);
		addHash(dataHash);

		// Set key's interest to reading
		key.interestOps(SelectionKey.OP_READ);

		client.incrementTotalSentMessages();

		// Rate at which each connection will generate packets is messageRate per-second.
		// Typical value of messageRate will be between 2-4
		Thread.sleep(1000 / messageRate);	
	}

	/*
	 * After server receives client data, it will compute hash code for data packet and send it back to the client.
	 * Client uses this method to read server's response/acknowledgement, which has the hash code.
	 * Client then verifies the hash code and removes from linked list
	 * 
	 */
	public void read(SelectionKey key) throws IOException {
		
		SocketChannel server = (SocketChannel) key.channel();
		// Server WorkerThread sends data in payload sizes of 40 or less
		ByteBuffer buffer = ByteBuffer.allocate(RECEIVED_BYTE_PAYLOAD);
		int read = 0;
		
		//System.out.println("Current buffer: " + new String(buffer.array()));
		buffer.clear();
		//System.out.println("Readable");
		// TODO Handle read == -1 scenario after rewind() ?
		while(buffer.hasRemaining() && read != -1) {
			read = server.read(buffer);
		}
		
		//System.out.println("Read:" + read);
		
		// Goes back to the beginning of the buffer so that it may be read/written fully again
		buffer.rewind();
		
		//System.out.println("Here2");

		// May need to cast as BigInteger like in Hash class
		String receivedDataHash = new String(buffer.array(), "UTF-8");
		String trimmedData = receivedDataHash.trim();

		//System.out.println("Received Data Hash: " + trimmedData);

		if(hashList.contains(trimmedData)) {
			//System.out.println("SenderThread: Removing hashcode from list");
			removeHash(trimmedData);
			client.incrementTotalReceivedMessages();
		}
		else {
			//System.out.println("Read hashcode byte from server does not exist in hash list!");
		}


		// sets the key's interest set to writing
		key.interestOps(SelectionKey.OP_WRITE);
	}

	public synchronized void addHash(String hash) {
		hashList.add(hash);
	}

	public synchronized void removeHash(String hash) {
		hashList.remove(hash);
	}

}
