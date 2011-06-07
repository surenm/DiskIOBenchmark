package benchmarks;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class DiskIOBenchmark {

	static final boolean DEBUG = false ;
	static Options options = new Options();
	static private CountDownLatch startIO = new CountDownLatch(1);

	public static void main(String[] args) {
		// Initiate a command line parser 
		CommandLineParser parser = new PosixParser();
		
		// create an options list 
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
		String ioAction = "";
		int threads = 1 ;
		int blocks = 1 ;
		long blockSize = 1024 * 1024 * 100 ; 
		int chunkSize = 4096;
		try {
			CommandLine commandLine = parser.parse(options, args);
			if(commandLine.hasOption("help"))
				printHelp();
			
			if(commandLine.hasOption("write")) 
				ioAction = "write";
			
			if(commandLine.hasOption("read"))
				ioAction = "read";
			
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
			if(ioAction == "") {
				System.err.println("Incorrect Usage");
				printHelp(1);
			}
			doIO(ioAction, paths, threads, chunkSize, blockSize, blocks);
		}
		catch (ParseException err){
			System.err.println("Command line parsing failed. " +
					"Reason:" + err.getMessage());
			printHelp(1);
		}
	}

	private static void printHelp(int status) {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("disk_io_benchmark.jar", options);
		System.exit(status);
	}
	
	private static void printHelp(){
		printHelp(0);
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
				int chunkSize, long blockSize, int blocks) {
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
		if(ioAction == "read"){
			for (String path : paths) {
				String[] fileListing = getFileListing(path);
				for (String fileName : fileListing) {
					fullPaths.add(fileName);
				}
			}
		}
		else {
			for(String path : paths) {
				if(path.charAt(path.length()-1) != '/') path = path + '/';
				for(int i=0; i<blocks; i++){
					String fileName = path + Integer.toString(i) + ".txt";
					fullPaths.add(fileName);
				}
			}
		}
		
		if(thread_count > fullPaths.size()) thread_count = fullPaths.size();
		int files_per_thread = fullPaths.size()/thread_count;
		
		// The array of threads to create
		WorkerThread[] threads = new WorkerThread[thread_count];
				
		// Create all IO Threads
		for(int i=0; i < thread_count; i++){
			try {
				// Calculate the file names this particular thread is going to work upon
				String[] threadPaths = new String[files_per_thread];
				fullPaths.subList(i*files_per_thread,(i+1)*files_per_thread).toArray(threadPaths);
				
				/* Find out which IO action is going to be done and accordingly
				 * create threads
				 */
				if(ioAction == "read"){
					ReadThread readThread = new ReadThread(threadPaths, chunkSize, startIO);
					threads[i] = readThread;
					threads[i].setThread(new Thread(readThread));
				}	
				else {
					WriteThread writeThread = new WriteThread(threadPaths, blockSize, chunkSize, startIO);
					threads[i]= writeThread; 
					threads[i].setThread(new Thread(writeThread));
				}	
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		// Start the time counter
		
		
		for(int i=0; i < thread_count; i++){
			// Run the thread
			threads[i].threadInstance.start();
		}
		startIO.countDown();
		try {
			startIO.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		long startTime = System.currentTimeMillis();
		
		for(int i=0; i < thread_count; i++){
			try {
				// Join each IO thread
				threads[i].threadInstance.join();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Stop the time counter and calculate timeElapsed
		long endTime = System.currentTimeMillis();
		double timeElapsed = (endTime-startTime)/1000; //millisec to sec conversion
		
		// Calculate Throughput
		double totalIOSize = 0;
		for (String fileName : fullPaths) {
			totalIOSize += Utils.get_file_size(fileName);
		}
		double overallThroughput = totalIOSize/(timeElapsed);
		
		//format all doubles to 4 decimal places
		totalIOSize = Utils.round_off(totalIOSize);
		overallThroughput = Utils.round_off(overallThroughput);
		
		//Print the comma separated values of results
		System.out.print(ioAction); System.out.print(',');
		System.out.print(chunkSize); System.out.print(',');
		System.out.print(totalIOSize); System.out.print(',');
		System.out.print(timeElapsed); System.out.print(',');
		System.out.print(thread_count); System.out.print(',');
		System.out.print(paths.length); System.out.print(',');
		System.out.print(overallThroughput); System.out.print(',');
		for(int i = 0; i < thread_count; i++){
			System.out.print(threads[i].getThroughput()); 
			if(i < thread_count-1) System.out.print('-');
		}
		System.out.print(",");
		for(int i = 0; i <thread_count; i++){
			System.out.print(threads[i].getFileListString());
			if(i < thread_count-1) System.out.print("-");
		}
		System.out.println();
	}
}
