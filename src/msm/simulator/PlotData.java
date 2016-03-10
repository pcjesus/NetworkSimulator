/**
 * MSM - Network Simulator
 */
package msm.simulator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;


/**
 * @author pjesus
 *
 */
public class PlotData {
    
    public static final String PARAM_PLOT_TYPES = "PLOT_TYPES";
    
    private String filename;
    private String plotType;
    private int simulatonNumber;
    
    
    public PlotData(int simulatonNumber, String plotType){
        this.simulatonNumber = simulatonNumber;
        this.plotType = plotType;
    }


    
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }


    
    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }


    
    /**
     * @return the plotType
     */
    public String getPlotType() {
        return plotType;
    }


    
    /**
     * @param plotType the plotType to set
     */
    public void setPlotType(String plotType) {
        this.plotType = plotType;
    }


    
    /**
     * @return the simulatonNumber
     */
    public int getSimulatonNumber() {
        return simulatonNumber;
    }


    
    /**
     * @param simulatonNumber the simulatonNumber to set
     */
    public void setSimulatonNumber(int simulatonNumber) {
        this.simulatonNumber = simulatonNumber;
    }
    
    
    public void createGenericResult2DPlot(String outputDir, String netPrefix, String appType, Integer simIndex, 
            String resultType, List<BigDecimal> xValues, List<BigDecimal> yValues) 
      throws IOException {
        
        String filename = netPrefix + "_" + appType + "_" + simIndex + "_" + resultType + ".xy";
        
        FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
        
        int size = xValues.size();
        for(int i=0; i < size; i++) {
            p.println (xValues.get(i) + " " + yValues.get(i));
        }
        
        p.close();
        
        out.close();
        
        this.setFilename(filename);
        
    }
    
    
    public void createGenericResult3DPlot(String outputDir, String netPrefix, String appType, Integer simIndex, 
            String resultType, List<BigDecimal> xValues, List<BigDecimal> yValues, List<BigDecimal> zValues) 
      throws IOException {
        
        String filename = netPrefix + "_" + appType + "_" + simIndex + "_" + resultType + ".xyz";
        
        FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
        
        int size = xValues.size();
        for(int i=0; i < size; i++) {
            p.println (xValues.get(i) + " " + yValues.get(i) + " " + zValues.get(i));
        }
        
        p.close();
        
        out.close();
        
        this.setFilename(filename);
        
    }
    
    
    
    public String toString(){
        StringBuffer sb = new StringBuffer("PlotData {simulatonNumber=");
        sb.append(simulatonNumber);
        sb.append("; plotType=");
        sb.append(plotType);
        sb.append("; filename=");
        sb.append(filename);
        sb.append("}");
        return sb.toString();
    }
    

}
