package cs455.scaling.server;

import java.util.LinkedList;

public class TaskQueue {
	private final LinkedList<Task> taskQueue;
//	private int batchSize;
//	// in seconds
//	private int batchTime;
//	private LinkedList<Task> batch;
//	private long timeStart; 
//	private long endTime;
	
	public LinkedList<Task> getQueue() { return taskQueue; }
	
	public TaskQueue () {
		this.taskQueue = new LinkedList<Task>();
//		this.batchSize = batchSize;
//		this.batch = new LinkedList<Task>();
//		
//		this.timeStart = System.currentTimeMillis();
//		this.endTime = System.currentTimeMillis();
	}
	
	public synchronized void enqueue(Task task) {
		if(taskQueue.isEmpty()) {
			notify();
		}
		
		taskQueue.add(task);
//		endTime = System.currentTimeMillis();
//		
//		if(taskQueue.size() == batchSize || batchTime < ((endTime - timeStart) / 1000)) {
//			notify();
//		}
	}
	
	public synchronized Task dequeue() throws InterruptedException {
		while(taskQueue.isEmpty()) {
			wait();
		}
		
//		timeStart = System.currentTimeMillis();
		Task task = taskQueue.remove();
		return task;
	}
}
