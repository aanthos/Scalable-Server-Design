package cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/*
 * Description
 */
public class Task {
	public static final int READ_TASK = 0;
	public static final int HASH_TASK = 1;
	public static final int WRITE_TASK = 2;
	public static final int ACCEPT_TASK = 3;
	
	private SelectionKey key;
	private int type;
	private byte[] hashData;
	private String computedHash;
	private Selector selector;

	// Used for accept tasks
	public Task(SelectionKey key, int type, Selector selector) {
		this.key = key;
		this.type = type;
		this.selector = selector;
	}
	
	// Used for read tasks
	public Task(SelectionKey key, int type) {
		this.key = key;
		this.type = type;
	}
	
	// Used for hash tasks
	public Task(SelectionKey key, int type, byte[] hashData) {
		this.key = key;
		this.type = type;
		this.hashData = hashData;
	}
	
	// Used for write tasks
	public Task(SelectionKey key, int type, String computedHash) {
		this.key = key;
		this.type = type;
		this.computedHash = computedHash;
	}
	
	public int getType() {
		return this.type;
	}
	
	public SelectionKey getSelectionKey() {
		return this.key;
	}
	
	public byte[] getHashData() {
		return this.hashData;
	}
	
	public String getComputedHash() {
		return this.computedHash;
	}
	
	public Selector getSelector() {
		return this.selector;
	}
}
