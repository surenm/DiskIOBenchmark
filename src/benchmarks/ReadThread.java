package benchmarks;

import java.io.InputStream;
import java.io.Reader;
import java.io.File;

public class ReadThread extends Thread{
	private String[] filesList;
	private int chunkSize;
	private double timeElapsed;
	
	ReadThread(String[] _fileList, int _chunkSize ){
		filesList = _fileList;
		chunkSize = _chunkSize;
		timeElapsed = 0.0;
	}
	
	public void run(){
		for (String fileName : filesList) {
			long startTime = System.currentTimeMillis();
			
			System.out.println(fileName);
			
			long endTime = System.currentTimeMillis();
			timeElapsed += ( endTime - startTime );
		}
		System.out.println(timeElapsed);

	}
}