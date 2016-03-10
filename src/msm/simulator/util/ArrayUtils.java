/**
 * MSM - Network Simulator
 */

package msm.simulator.util;


/**
 * Array Utilities
 * 
 * @author pcjesus
 * @version 1.0
 */

public class ArrayUtils {

    
    /**
     * Splits the source string around matches of the given regular expression, and convert elements to doubles.
     * 
     * @param source the source string
     * @param regex the delimiting regular expression
     * @return the array of doubles computed by splitting the source string around matches of the given regular expression
     */
    public static double[] splitDouble(String source, String regex){
        String[] sArray = source.split(regex);
        double[] result = new double[sArray.length];
        
        for(int i=0; i < sArray.length; i++){
            result[i] = Double.valueOf(sArray[i]);
        }
        
        return result;
    }
    
    
    /**
     * Splits the source string around matches of the given regular expression, and convert elements to integers.
     * 
     * @param source the source string
     * @param regex the delimiting regular expression
     * @return the array of integers computed by splitting the source string around matches of the given regular expression
     */
    public static int[] splitInt(String source, String regex){
        String[] sArray = source.split(regex);
        int[] result = new int[sArray.length];
        
        for(int i=0; i < sArray.length; i++){
            result[i] = Integer.valueOf(sArray[i]);
        }
        
        return result;
    }
    
    
    
    
}
