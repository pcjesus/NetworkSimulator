/**
 * 
 */
package msm.simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import msm.simulator.exceptions.ConfigException;
import msm.simulator.network.SpatialDataDistribution;


/**
 * @author pcjesus
 *
 */
public class AverageFileResults {

    public static final String PARAM_NUM_INPUT_FILES = "NUM_INPUT_FILES";
    public static final String PARAM_INPUT_FILES = "_INPUT_FILES";
    public static final String PARAM_OUTPUT_FILENAME = "OUTPUT_FILENAME";
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        
        if((args == null) || (args.length == 0)){
            System.err.println("No argument specified!\n" + 
                               "Please specify complete configuration file path as argument.");
            System.exit(0);
        }
        
        Config config = null;
        
        try {
            
            //Load Configuration File
            config = new Config(args[0]);
            
        } catch (ConfigException ce){
            System.err.println("Configuration Error: " + ce.getMessage());
            System.exit(0);
        }   
        
        
        //Files Output Directory
        String outDir = config.getValueWithDefault(Config.PARAM_OUTPUT_DIR, Config.DEFAULT_OUTPUT_DIR);
        
        //Files Input Directory
        String inDir = config.getValueWithDefault(Config.PARAM_INPUT_DIR, Config.DEFAULT_INPUT_DIR);
        
        
        //Get Math Precision
        int mathPrecision = Integer.parseInt(config.getValueWithDefault(Config.PARAM_MATH_PRECISION, Config.DEFAULT_MATH_PRECISION));
        RoundingMode mathRoundingMode = RoundingMode.valueOf(config.getValueWithDefault(Config.PARAM_MATH_ROUNDING_MODE, Config.DEFAULT_MATH_ROUNDING_MODE));
        MathContext mc = new MathContext(mathPrecision, mathRoundingMode);
        
        //Get Number of File
        int numFiles = Integer.valueOf(config.getValueWithDefault(AverageFileResults.PARAM_NUM_INPUT_FILES, "1"));
        
        //Get Output Filename
        String outputFilename = config.getValue(AverageFileResults.PARAM_OUTPUT_FILENAME);
        
        //Get Input Files
        HashMap<String, ArrayList<BigDecimal>> data = new HashMap();
        for(int i=0; i < numFiles; i++){
            try {
                data.put(config.getValue(i+1, AverageFileResults.PARAM_INPUT_FILES), new ArrayList<BigDecimal>());
                
            } catch (ConfigException e) {
                System.err.println("ERROR Loading Input Files Config: "+e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
        }
        
        System.out.println("Loading data files: ");
        //Load input files data
        for(String file : data.keySet()){
            System.out.print("\t"+file+" ... ");
            try {
                
                //Load data to average
                System.out.print("[data - ");
                data.put(file, AverageFileResults.loadFromFile(inDir, file, 1, mc));
                System.out.println("OK] ");
                
            } catch (FileNotFoundException e) {
                System.err.println("ERROR Loading "+file+": "+e.getMessage());
                e.printStackTrace();
                System.exit(0);
            } catch (IOException e) {
                System.err.println("ERROR Loading "+file+": "+e.getMessage());
                e.printStackTrace();
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.err.println("ERROR Loading "+file+": "+e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
        }
        System.out.println("OK!");
        
        
        System.out.println("Searching for file with less values...");
        String indexFile = null;
        int minSize = Integer.MAX_VALUE;
        for(String file : data.keySet()){
            int fileSize = data.get(file).size();
            System.out.println("\tFile:"+file+"; Size: "+fileSize);
            if(fileSize < minSize){
                indexFile = file;
                minSize = fileSize;
            }
        }
        System.out.println("Result -> File:"+indexFile+"; Size: "+minSize);
        
        
        System.out.print("Loading index file ("+indexFile+")... ");
        ArrayList<BigDecimal> index = null;
        //Load index from smallest file
        try {
            
            //Load index
            index = AverageFileResults.loadFromFile(inDir, indexFile, 0, mc);
            
        } catch (FileNotFoundException e) {
            System.err.println("ERROR Loading index from "+indexFile+": "+e.getMessage());
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("ERROR Loading index from "+indexFile+": "+e.getMessage());
            e.printStackTrace();
            System.exit(0);
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR Loading index from "+indexFile+": "+e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("OK!");
        
        
        System.out.print("Computing average...");
        ArrayList<BigDecimal> result = new ArrayList(index.size());
        //Compute average
        for(int i=0; i < index.size(); i++){
            BigDecimal sum = BigDecimal.ZERO;
            int n = 0;
            for(String file : data.keySet()){
                sum = sum.add(data.get(file).get(i), mc);
                n = n + 1;
            }
            result.add(sum.divide(new BigDecimal(n, mc), mc));
        }
        System.out.println("OK!");
        
        
        System.out.print("Storing results...");
        //Store result
        try {
            storeResults(outDir, outputFilename, index, result);
        } catch (IOException e) {
            System.err.println("ERROR Storing results: "+e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("OK!");
        
        

    } //main
    
    
    private static ArrayList<BigDecimal> loadFromFile(String inputDir, String fileName, int index, MathContext mc) 
        throws FileNotFoundException, IOException, ClassNotFoundException {

        FileReader fr = new FileReader(inputDir + fileName);
        BufferedReader br = new BufferedReader(fr);
    
//        int m = getNumberOfLines(inputDir + fileName);
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>();
                
        String line;
        while((line = br.readLine()) != null){
            line = line.trim();
            String[] lineData = line.split("\\s+");
//            System.out.println("LineData:" + lineData[1]);
//            System.out.flush();
            result.add(new BigDecimal(lineData[index], mc));
        }
    
        br.close();
        fr.close();
        
        return result;
    }
    
    
    
    private static void storeResults(String outputDir, String filename, List<BigDecimal> index, List<BigDecimal> results) 
      throws IOException {
                
        FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
        
        int size = index.size();
        for(int i=0; i < size; i++) {
            p.println (index.get(i) + " " + results.get(i));
        }
        
        p.close();
        
        out.close(); 
    }
    
    
//    /**
//     * Determine the number of lines of a text file
//     * @param path location of the file
//     * @return the total number of lines of the file
//     * @throws IOException
//     */
//    private static int getNumberOfLines(String path) throws IOException {
//        
//        RandomAccessFile rf = new RandomAccessFile(path,"r");
//        long lastRec=rf.length();
//        rf.close();
//              
//        FileReader fr = new FileReader(path);
//        LineNumberReader lr = new LineNumberReader(fr);
//        lr.skip(lastRec);
//        int m = lr.getLineNumber();
//        lr.close();
//        fr.close();
//        
//        return m;
//        
//    }
    

}
