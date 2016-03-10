/**
 * MAP-i - Network Simulator
 */
package msm.simulator.network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.Set;

import msm.simulator.Config;
import msm.simulator.exceptions.DataDistributionException;
import msm.simulator.util.NumGenerator;
import msm.simulator.util.SetUtils;

import org.apache.commons.math.MathException;


/**
 * DataDistribution Class
 * 
 * @author pcjesus
 * @version 1.0
 *
 */
public class SpatialDataDistribution {
    
    private static final String FILE_PREFIX = "DD";
    
    public enum DataDistributionType {RANDOM, SLOPE, SPIKE};
    private enum AxisOrientation {X, Y};
    
    private double[][] data;
    
    private boolean loadData;
    private int m;
    private int n;
    private DataDistributionType type;
    private String[] parameters;
    private String params;

    /**
     *  Constructors
     */
    
    public SpatialDataDistribution(boolean loadData, Config config) {
        this.loadData = loadData;
        if(!this.loadData){
            this.type = DataDistributionType.valueOf(config.getValue(Config.PARAM_DATA_DISTRIBUTION_TYPE));
            int size = Integer.valueOf(config.getValue(Config.PARAM_DATA_DISTRIBUTION_SIZE));
            this.n = size;
            this.m = size;
            this.params = config.getValue(Config.PARAM_DATA_DISTRIBUTION_PARAMETERS);
            this.parameters = this.params.split(";");
            this.data = new double[m][n];
        }
    }
    
    public SpatialDataDistribution(Config config) {
        this.loadData = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_LOAD_DATA_DISTRIBUTION, "false"));
        if(!this.loadData){
            this.type = DataDistributionType.valueOf(config.getValue(Config.PARAM_DATA_DISTRIBUTION_TYPE));
            int size = Integer.valueOf(config.getValue(Config.PARAM_DATA_DISTRIBUTION_SIZE));
            this.n = size;
            this.m = size;
            String params = config.getValue(Config.PARAM_DATA_DISTRIBUTION_PARAMETERS);
            this.parameters = params.split(";");
            this.data = new double[m][n];
        }
    }
    
    public SpatialDataDistribution(SpatialDataDistribution dd) {
        this.data = dd.getData();
    }
    
    
    
    
    /**
     * Load data distribution from a text file
     * 
     * @param inputDir Path of the input directory with the file
     * @param fileName Name of the file to load
     * 
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadFromFile(String inputDir, String fileName) 
        throws FileNotFoundException, IOException, ClassNotFoundException {

        FileReader fr = new FileReader(inputDir + fileName);
        BufferedReader br = new BufferedReader(fr);
        
        int m = SpatialDataDistribution.getNumberOfLines(inputDir + fileName);
        
        String line = br.readLine().trim();
        String[] lineData = line.split("\\s+");
        int n = lineData.length;
        this.initDataMatrix(m, n);
        int row = 0;
        this.setRowData(row,lineData);
        
        while((line = br.readLine()) != null){
            line = line.trim();
            row++;
            lineData = line.split("\\s+");
            this.setRowData(row,lineData);
        }
        
        
        br.close();
        fr.close();
    }
    
    private void initDataMatrix(int m, int n){
        this.m = m;
        this.n = n;
        this.data = new double[m][n];
    }
    
    
    private void setRowData(int rowIndex, String[] rowData){
        for(int i = 0; i < rowData.length; i++){
            this.data[rowIndex][i] = Double.parseDouble(rowData[i]);
        }
    }
    
    /**
     * Determine the number of lines of a text file
     * @param path location of the file
     * @return the total number of lines of the file
     * @throws IOException
     */
    private static int getNumberOfLines(String path) throws IOException {
        
        RandomAccessFile rf = new RandomAccessFile(path,"r");
        long lastRec=rf.length();
        rf.close();
              
        FileReader fr = new FileReader(path);
        LineNumberReader lr = new LineNumberReader(fr);
        lr.skip(lastRec);
        int m = lr.getLineNumber();
        lr.close();
        fr.close();
        
        return m;
        
    }
    
    /**
     * Determine the number of lines of a text file
     * @param path location of the file
     * @return the total number of lines of the file
     * @throws IOException
     * 
     * @deprecated slower method?... Use: getNumberOfLines(String path)
     */
    private static int getNumberOfLines2(String path) throws IOException {
        FileReader fr = new FileReader(path);
        
        int numberOfLines = 0;
        LineNumberReader lr = null;
        try {
            lr = new LineNumberReader(fr);
            while ((lr.readLine()) != null) { 
                continue;
            }
            numberOfLines = lr.getLineNumber();
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(lr != null) {
                lr.close();
            }
        }
        
        fr.close();
        
        return numberOfLines;
    }
    
    
    public void generateData() 
        throws DataDistributionException{
        
        switch (this.type) {
            case RANDOM:    
                try {
                    generateRandomData();
                } catch (MathException e) {
                    throw (DataDistributionException)new DataDistributionException("Error generating random data.", e).initCause(e);
                }
                break;
            case SLOPE:
                generateSlope();
                break;
            case SPIKE:
                generateSpike();
                break;
            default:
                throw new DataDistributionException("Unknown Data Distribution: " + this.type);
        }
    }
    
    
    private void generateRandomData() throws MathException{
        
        //Create Random Number Generator, according to the defined distribution parameters 
        NumGenerator ng = new NumGenerator();
        ng.loadConfig(this.params);
        
        //Set random data distribution
        for (int i=0; i < this.data.length; i++) {
            for (int j=0; j < this.data[i].length; j++){
                this.data[i][j] = ng.generateDouble();
            }
        }
    }
    
    
    private void generateSlope() 
        throws DataDistributionException{
        
        //Get Parameters
        double min = Double.valueOf(this.parameters[0]);
        double max = Double.valueOf(this.parameters[1]);
        AxisOrientation orientation = AxisOrientation.valueOf(this.parameters[2]);
        
        double value = min;
        
        //Set slope data distribution
        if(orientation == AxisOrientation.X){
        
        double step = Math.abs(max - min)/this.m;
        
        for (int i=0; i < this.data.length; i++) {
            for (int j=0; j < this.data[i].length; j++){
                this.data[i][j] = value;
            }
            
            if(max > min){
                value = value + step;
            } else {
                value = value - step;
            }
        }
        
        } else if (orientation == AxisOrientation.Y) {
            
            double step = Math.abs(max - min)/this.n;
            
            for (int i=0; i < this.data.length; i++) {
                for (int j=0; j < this.data[i].length; j++){
                    this.data[j][i] = value;
                }
                
                if(max > min){
                    value = value + step;
                } else {
                    value = value - step;
                }
            }
            
        } else {
            throw new DataDistributionException("Invalid Slope Orientation: "+ this.parameters[2]);
        }
        
    }
    
    
    private void generateSpike() 
        throws DataDistributionException {
        
        //Get Parameters
        double min = Double.valueOf(this.parameters[0]);
        double max = Double.valueOf(this.parameters[1]);
        int num = Integer.valueOf(this.parameters[2]);
        
        //Set spike data distribution
        
        //Set all data distribution with the min value
        for (int i=0; i < this.data.length; i++) {
            for (int j=0; j < this.data[i].length; j++){
                this.data[j][i] = min;
            }
        }
        
        //Choose and set spikes with the max value
        if(num == 1){
            
            //Set the only spike at the center
            int i = this.data.length / 2;
            int j = this.data[i].length / 2;
            
            this.data[i][j] = max;
            
        } else if (num > 1) {
            
            //Set spikes with the max value at random positions
            Random rndGen = new Random();
            
            //Set random spikes
            for (int k=0; k < num; k++) {
                int i = rndGen.nextInt(this.m);
                int j = rndGen.nextInt(this.n);
                this.data[i][j] = max;
            }            
            
        } else {
            throw new DataDistributionException("Invalid Spike Number: "+ this.parameters[2]);
        }
        
    }
    
    
    /**
     * Set data value uniformly at random (within defined range) to all network nodes
     * 
     * @param net Network object to set its node values
     */
/*    public void setData(Network net){
        
        Random rndGen = new Random();
        
        for(Node n : net.getNodes()){
            n.setDataValue(new BigDecimal(rndGen.nextInt(rndRange)));
        }
    }
*/    
    
    public void setDataDistribution(Network net){
        
        //If force spike assignment at nodes
        if(this.type == DataDistributionType.SPIKE && Boolean.valueOf(this.parameters[3]) == true){
            
            double min = Double.valueOf(this.parameters[0]);
            double max = Double.valueOf(this.parameters[1]);
            int num = Integer.valueOf(this.parameters[2]);
            
            //Set all nodes with the min value
            for(Node n : net.getNodes()){
                n.setDataValue(new BigDecimal(min));
            }
            
            if(num == 1){
                
                //Set node closest to the network center with max value
                Node n = net.getCentralNode();
                n.setDataValue(new BigDecimal(max));
                
            } else {
                
                //Set random nodes with the max value
                Set<Integer> nodes = net.getNodesKeySet();
                List<Integer> rNodes = SetUtils.randomizeSet(nodes);
                if(num < rNodes.size()){
                    rNodes = rNodes.subList(0, num);
                }
                
                for(Integer nodeId : rNodes){
                    Node n = net.getNode(nodeId);
                    n.setDataValue(new BigDecimal(max));
                }
            }
            
        
        //Default data assignment
        } else {
        
            for(Node n : net.getNodes()){
            
                //Get node position (in the unit square)
                double x = n.getX();
                double y = n.getY();
            
                int i = getProximityCoordinate(x, this.m);
                int j = getProximityCoordinate(y, this.n);
            
                n.setDataValue(new BigDecimal(this.data[i][j]));
            }
        
        }
    }
    
    
    private int getProximityCoordinate(double x, int dimension){
            
/*        double resTemp = x * dimension;
        int res = (int) resTemp;
        if((resTemp - res) >= 0.5){
            res++;
        }
        
        return res;
*/        
        return (int) (x * dimension);
    }
    
    
    public void setDataDistribution(Node node){
        
        //Could be necessary to consider the existence of spikes, and forcing them....
        
        int i = getProximityCoordinate(node.getX(), this.m);
        int j = getProximityCoordinate(node.getY(), this.n);
        
        node.setDataValue(new BigDecimal(this.data[i][j]));
    }

    
    
    /**
     * @return the data
     */
    public double[][] getData() {
        return data;
    }

    
    /**
     * @param data the data to set
     */
    public void setData(double[][] data) {
        this.data = data;
    }
    
    
    /**
     * @return the loadData
     */
    public boolean isLoadData() {
        return loadData;
    }

    
    /**
     * @param loadData the loadData to set
     */
    public void setLoadData(boolean loadData) {
        this.loadData = loadData;
    }
    

    /**
     * @return the m number of rows
     */
    public int getM() {
        return m;
    }

    
    /**
     * @param m the m to set
     */
    public void setM(int m) {
        this.m = m;
    }

    
    /**
     * @return the n number of columns
     */
    public int getN() {
        return n;
    }

    
    /**
     * @param n the n to set
     */
    public void setN(int n) {
        this.n = n;
    }

    public String getReportPrefixName(){
        return FILE_PREFIX;
    }
    

    
    /**
     * @return the type
     */
    public DataDistributionType getType() {
        return type;
    }

    
    /**
     * @param type the type to set
     */
    public void setType(DataDistributionType type) {
        this.type = type;
    }

    
    /**
     * @return the parameters
     */
    public String[] getParameters() {
        return parameters;
    }

    
    /**
     * @param parameters the parameters to set
     */
    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataDistribution [\ndata(");
        builder.append(m);
        builder.append(",");
        builder.append(n);
        builder.append(")=\n");
        for(int i=0; i < data.length; i++){
            for(int j=0; j < data[i].length; j++){
                builder.append(this.data[i][j]);
                builder.append(" ");
            }
            builder.append("\n");
        }
        builder.append("loadData=");
        builder.append(loadData);
        builder.append("\ntype=");
        builder.append(this.type);
        builder.append("\nparameters=");
        builder.append(this.parameters);
        builder.append("\n]");
        return builder.toString();
    }
    
    
    

}
