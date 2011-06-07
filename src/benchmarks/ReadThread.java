package benchmarks;

import java.io.*;
import java.util.concurrent.CountDownLatch;

public class ReadThread extends WorkerThread{
	private String[] filesList;
	private int chunkSize;
	private double timeElapsed;
	private byte[] str;
	private long totalSize;
	private double throughput ;
	private CountDownLatch startLatch;
	
	ReadThread(String[] _fileList, int _chunkSize, CountDownLatch latch ){
		filesList = _fileList;
		chunkSize = _chunkSize;
		timeElapsed = 0.0;
		totalSize = 0;
		for (String fileName : filesList) {
			totalSize += Utils.get_file_size(fileName);
		}
		throughput = 0.0;
		str = new byte[chunkSize];
		startLatch = latch;
	}
	
	public void run(){
		try {
			startLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (String fileName : filesList) {
			try {
				// Start timer
				long startTime = System.currentTimeMillis();
				
				InputStream unBufferedStream = new FileInputStream(fileName);
				while(unBufferedStream.read(str, 0, chunkSize) != -1);
				unBufferedStream.close();
				
				// End timer
				long endTime = System.currentTimeMillis();
				timeElapsed += (endTime - startTime)/1000;
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		
		throughput = totalSize/timeElapsed;
	}
	
	public double getThroughput(){
		return Utils.round_off(throughput); 
	}
	
	public String getFileListString(){
		String ret = new String();
		for (String fileName : filesList) {
			ret = fileName + ":";
		}
		return ret ;
	}
}
