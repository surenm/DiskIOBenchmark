package benchmarks;

import java.io.File;
import java.util.Vector;

import org.apache.commons.cli.*;

public class DiskIOBenchmark {
	public static final boolean DEBUG = false ;

	public static void main(String[] args) {
		// Initiate a command line parser 
		CommandLineParser parser = new PosixParser();
		
		// create an options list 
		Options options = new Options();
		Option read = new Option("read", 
			"if the IO operation to be performed is read");
		Option write = new Option("write", 
			"if the IO operation to be performed is write");
		Option help = new Option("help", 
			"Print this help");
		
		Option threadsOption = OptionBuilder.withArgName("threads")
			.hasArg()
			.withDescription("Number of threads of execution")
			.create("threads");
		Option blocksOption = OptionBuilder.withArgName("blocks")
			.hasArg()
			.withDescription("Number of blocks to be written")
			.create("blocks");
		Option blockSizeOption = OptionBuilder.withArgName("block_size")
			.hasArg()
			.withDescription("Size of each block to be written")
			.create("block_size");
		Option chunkSizeOption = OptionBuilder.withArgName("chunk_size")
			.hasArg()
			.withDescription("Size of each small chunk to be read/written")
			.create("chunk_size");
		
		options.addOption(read);
		options.addOption(write);
		options.addOption(help);
		options.addOption(threadsOption);
		options.addOption(blocksOption);
		options.addOption(blockSizeOption);
		options.addOption(chunkSizeOption);
	
		//Options for calling do IO 
		String ioAction = "read";
		int threads = 1 ;
		int blocks = 1 ;
		long blockSize = 1024 * 1024 * 100 ; 
		int chunkSize = 4096;
		try {
			CommandLine commandLine = parser.parse(options, args);
			if(commandLine.hasOption("write")) 
				ioAction = "write";
			
			if(commandLine.hasOption("threads"))
				threads = Integer.parseInt(commandLine.getOptionValue("threads"));
			
			if(commandLine.hasOption("blocks")) 
				blocks = Integer.parseInt(commandLine.getOptionValue("blocks"));
			
			if(commandLine.hasOption("block_size"))
				blockSize = Long.parseLong(commandLine.getOptionValue("block_size"));
			
			if(commandLine.hasOption("chunk_size"))
				chunkSize = Integer.parseInt(commandLine.getOptionValue("chunk_size"));
			
			if(commandLine.hasOption("blocks"))
				blocks = Integer.parseInt(commandLine.getOptionValue("blocks"));
			
			String paths[] = commandLine.getArgs();
			if(paths.length == 0) {
				String path = System.getProperty("user.dir");
				paths = new String[] { path } ;
			}
			
			doIO(ioAction, paths, threads, chunkSize, blockSize, blocks);
		}
		catch (ParseException err){
			System.err.println("Command line parsing failed. " +
					"Reason:" + err.getMessage());
		}
		
	}

	private static String[] getFileListing(String path){
		File dirEntity = new File(path);
		File[] fileList = dirEntity.listFiles();
		Vector<String> retVector = new Vector<String>();
		for (File file : fileList) {
			if(!file.isDirectory())
				retVector.add(file.getAbsolutePath());
		}
		String[] ret = new String[retVector.size()];
		retVector.toArray(ret);
		return ret;
	}

	private static void doIO(String ioAction, String[] paths, int thread_count, 
				int chunkSize, long blockSize, int blocks){
		if(DEBUG) { 
			System.out.println("IO Type: " + ioAction);
			System.out.println("Paths: \n");
			for (String path : paths) {
				System.out.println(path);
			}
			System.out.println("Number of threads: " + thread_count);
			System.out.println("Chunk Size in Bytes:" + chunkSize);
			System.out.println("Block Size in Bytes: " + blockSize);
			System.out.println("Number of blocks:" + blocks);
		}
		
		Vector<String> fullPaths =new Vector<String>();
		for (String path : paths) {
			String[] fileListing = getFileListing(path);
			for (String fileName : fileListing) {
				fullPaths.add(fileName);
			}
		}
		
		if(thread_count > fullPaths.size()) thread_count = fullPaths.size();
		int files_per_thread = fullPaths.size()/thread_count;
		
		// The array of threads to create
		Thread[] threads = new Thread[thread_count];
				
		// Start the time counter
		long startTime = System.currentTimeMillis();
		
		// Create all IO Threads
		for(int i=0; i < thread_count; i++)
			try {
				
				String[] threadPaths = new String[files_per_thread];
				
				/* Find out which IO action is going to be done and accordingly
				 * create threads
				 */
				if(ioAction == "read"){
					fullPaths.subList(i*files_per_thread,(i+1)*files_per_thread).toArray(threadPaths);
					ReadThread readThread = new ReadThread(threadPaths, chunkSize);
					threads[i] = new Thread(readThread);
				}
					
				else {
					WriteThread writeThread = new WriteThread(paths, blockSize, chunkSize);
					threads[i] = new Thread(writeThread);
				}
				
				// Run the thread
				threads[i].start();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		for(int i=0; i < thread_count; i++)
			try {
				// Join each IO thread
				threads[i].join();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		// Stop the time counter and calculate timeElapsed
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime-startTime;
		System.out.println(timeElapsed);
	}
}
