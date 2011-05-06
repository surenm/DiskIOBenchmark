package benchmarks;

import java.io.*;
import java.util.Arrays;

public class WriteThread implements	Runnable{
	private String[] filesList;
	private long blockSize;
	private int chunkSize;
	private double timeElapsed;
	private byte[] str ;
	
	WriteThread(String[] _fileList, long _blockSize, int _chunkSize ){
		filesList = _fileList;
		blockSize = _blockSize;
		chunkSize = _chunkSize;
		timeElapsed = 0.0;
		str = new byte[chunkSize];
		Arrays.fill(str, (byte)'0');
	}
	
	public void run(){
		for (String fileName : filesList) {
			long chunks = blockSize/chunkSize;
			
			try {
				// Start timer
				long startTime = System.currentTimeMillis();
				
				// Write @chunks chunks of @chunk_size data to the file
				OutputStream unBufferedStream = new FileOutputStream(fileName);				
				while(chunks-- != 0) unBufferedStream.write(str);
				unBufferedStream.close();
				
				// End timer
				long endTime = System.currentTimeMillis();
				timeElapsed += ( endTime - startTime );
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}				
	}
}
