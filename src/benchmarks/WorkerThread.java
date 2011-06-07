package benchmarks;

public abstract class WorkerThread implements Runnable{
	public Thread threadInstance;
	public abstract void run();
	public abstract double getThroughput();
	public abstract String getFileListString();
	
	public void setThread(Thread thread){
		threadInstance = thread;
	}
}
