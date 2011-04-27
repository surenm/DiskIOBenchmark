package benchmarks;

import java.io.InputStream;
import java.io.Reader;
import java.io.File;

public class ReadThread extends Thread{
	private String[] filesList;
	private long blockSize;
	private int chunkSize;
	private double timeElapsed;
	
	ReadThread(String[] _fileList, long _blockSize, int _chunk_size ){
		filesList = _fileList;
		blockSize = _blockSize;
		chunkSize = _chunk_size;
		timeElapsed = 0.0;
	}
	
	public void run(){
		for (String fileName : filesList) {
			long startTime = System.currentTimeMillis();
	
			long endTime = System.currentTimeMillis();
			timeElapsed += ( endTime - startTime )/60;
		}
						
	}

}
