package benchmarks;

import java.io.File;
import java.text.DecimalFormat;

public class Utils {
	/*
	 *  Return the size of the file at fileName in MB
	 */
	public static double get_file_size(String fileName){
		File f = new File(fileName);
		return f.length()/(1024*1024);
	}
	
	/*
	 * Format doubles to 4 decimal places
	 */
	public static double round_off(double d){
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		try{
			return Double.valueOf(decimalFormat.format(d));
		}
		catch(NumberFormatException err){
			return -1.0;
		}
		
	}
}
