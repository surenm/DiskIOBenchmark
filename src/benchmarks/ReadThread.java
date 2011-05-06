package benchmarks;

import java.io.*;

public class ReadThread implements Runnable{
	private String[] filesList;
	private int chunkSize;
	private double timeElapsed;
	private byte[] str;
	
	ReadThread(String[] _fileList, int _chunkSize ){
		filesList = _fileList;
		chunkSize = _chunkSize;
		timeElapsed = 0.0;
		str = new byte[chunkSize];
	}
	
	public void run() {
		for (String fileName : filesList) {
			
			try {
				// Start timer
				long startTime = System.currentTimeMillis();
				
				InputStream unBufferedStream = new FileInputStream(fileName);
				while(unBufferedStream.read(str, 0, chunkSize) != -1);
				unBufferedStream.close();
				
				// End timer
				long endTime = System.currentTimeMillis();
				timeElapsed += ( endTime - startTime );
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		}
	}
}
