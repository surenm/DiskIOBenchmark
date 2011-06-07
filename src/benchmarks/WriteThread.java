package benchmarks;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class WriteThread extends WorkerThread{
	private String[] filesList;
	private long blockSize;
	private int chunkSize;
	private double timeElapsed;
	private byte[] str ;
	private long totalSize;
	private double throughput ;
	private CountDownLatch startLatch;
	
	WriteThread(String[] _fileList, long _blockSize, int _chunkSize, CountDownLatch latch){
		filesList = _fileList;
		blockSize = _blockSize;
		chunkSize = _chunkSize;
		timeElapsed = 0.0;
		totalSize = blockSize * filesList.length / (1024*1024);
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
			long chunks = blockSize/chunkSize;
			
			try {
				// Start timer
				long startTime = System.currentTimeMillis();
				
				// Write @chunks chunks of @chunk_size data to the file
				OutputStream unBufferedStream = new FileOutputStream(fileName);				
				while(chunks-- != 0) {
					unBufferedStream.write(str);
				}
				unBufferedStream.close();
				
				// End timer
				long endTime = System.currentTimeMillis();
				timeElapsed += (endTime - startTime)/1000;
					
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		throughput = totalSize/(timeElapsed);
	}
	
	public double getThroughput(){
		return Utils.round_off(throughput); 
	}
	
	public String getFileListString(){
		String ret = new String();
		for (String fileName : filesList) {
			ret += fileName + ":";
		}
		return ret ;
	}
}
