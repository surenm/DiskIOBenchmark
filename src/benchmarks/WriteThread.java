package benchmarks;

public class WriteThread extends Thread{
	private String[] filesList;
	private long blockSize;
	private int chunkSize;
	private double timeElapsed;
	
	WriteThread(String[] _fileList, long _blockSize, int _chunkSize ){
		filesList = _fileList;
		blockSize = _blockSize;
		chunkSize = _chunkSize;
		timeElapsed = 0.0;
	}
	
	public void run(){
		for (String fileName : filesList) {
			long startTime = System.currentTimeMillis();
			try {
				Thread.sleep(4000, 0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			long endTime = System.currentTimeMillis();
			timeElapsed += ( endTime - startTime );
			System.out.println(timeElapsed);
		}				
	}
}
