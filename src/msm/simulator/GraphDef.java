/**
 * MSM - Network Simulator
 */
package msm.simulator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;


/**
 * @author pjesus
 *
 */
public class GraphDef {
    
    public static final String PARAM_GRAPH_TYPE = "GRAPH_TYPE";
    public static final String PARAM_RESULT_TYPE = "RESULT_TYPE";
    public static final String PARAM_X_VALUES = "X_VALUES";
    public static final String PARAM_Y_VALUES = "Y_VALUES";
    public static final String PARAM_Z_VALUES = "Z_VALUES";
    public static final String PARAM_DISPLAYED_SIMULATIONS = "DISPLAYED_SIMULATIONS";
    public static final String PARAM_TITLE = "TITLE";
    public static final String PARAM_X_LABEL = "X_LABEL";
    public static final String PARAM_Y_LABEL = "Y_LABEL";
    public static final String PARAM_Z_LABEL = "Z_LABEL";
    public static final String PARAM_PLOT_SETTINGS = "PLOT_SETTINGS";
    public static final String PARAM_SHOW_PLOT = "SHOW_PLOT";
    
    public enum GraphTypes {D2, D3};
    
    public enum AxisValues {RMSE, NormRMSE, Iterations, MsgSend, MsgRcv, MsgLoss, ValidValues, 
        MAX, MIN, RANGE, MEAN, INIT, SUM, XPos, YPos, DataValue, InitDataValue, AllDataValues, Time,
        InitDataFrequency, InitDataFraction, DataIntervals, DataIntervalsCDF, KSMax, KSAvg, DEMax, DEAvg, 
        MsgLatency, MLFrequency, MLFraction, MLCDF, MEAN_A, MAX_ERROR, MIN_ERROR, MEAN_AA};

    private GraphTypes graphType;
    private String resultType;
    private AxisValues xvalues;
    private AxisValues yvalues;
    private AxisValues zvalues;
    private String title;
    private String xlabel;
    private String ylabel;
    private String zlabel;
    private String plotSettings;
    private boolean showPlot;
    private Map<Integer, PlotData> plotsData;
    
    public GraphDef(String graphType, String resultType, String xvalues, String yvalues, String zvalues, String title, 
                    String xlabel, String ylabel, String zlabel, String plotSettings, String showPlot){
        this.graphType = GraphTypes.valueOf(graphType);
        this.resultType = resultType;
        this.xvalues = AxisValues.valueOf(xvalues);
        this.yvalues = AxisValues.valueOf(yvalues);
        this.zvalues = AxisValues.valueOf(zvalues);
        this.title = title;
        this.xlabel = xlabel;
        this.ylabel = ylabel;
        this.zlabel = zlabel;
        this.plotSettings = plotSettings;
        this.showPlot = Boolean.valueOf(showPlot);
        this.plotsData = new HashMap<Integer, PlotData>();
    }
    
    
    public void addPlotData(PlotData plotData){
        
        this.plotsData.put(plotData.getSimulatonNumber(), plotData);
    }
    
    public int numberOfPlots(){
        return this.plotsData.size();
    }

    
    /**
     * @return the plotData
     */
    public Map<Integer, PlotData> getPlotsData() {
        return plotsData;
    }

    
    /**
     * @param plotData the plotData to set
     */
    public void setPlotsData(Map<Integer, PlotData> plotsData) {
        this.plotsData = plotsData;
    }

    
    /**
     * @return the graphType
     */
    public GraphTypes getGraphType() {
        return graphType;
    }

    
    /**
     * @param graphType the graphType to set
     */
    public void setGraphType(String graphType) {
        this.graphType = GraphTypes.valueOf(graphType);
    }
    
    
    /**
     * @return the resultType
     */
    public String getResultType() {
        return resultType;
    }

    
    /**
     * @param resultType the resultType to set
     */
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    
    /**
     * @return the xlabel
     */
    public String getXlabel() {
        return xlabel;
    }

    
    /**
     * @param xlabel the xlabel to set
     */
    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    
    /**
     * @return the xvalues
     */
    public AxisValues getXvalues() {
        return xvalues;
    }

    
    /**
     * @param xvalues the xvalues to set
     */
    public void setXvalues(String xvalues) {
        this.xvalues = AxisValues.valueOf(xvalues);
    }

    
    /**
     * @return the ylabel
     */
    public String getYlabel() {
        return ylabel;
    }

    
    /**
     * @param ylabel the ylabel to set
     */
    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }

    
    /**
     * @return the yvalues
     */
    public AxisValues getYvalues() {
        return yvalues;
    }

    
    /**
     * @param yvalues the yvalues to set
     */
    public void setYvalues(String yvalues) {
        this.yvalues = AxisValues.valueOf(yvalues);
    }
    
    
    /**
     * @return the zlabel
     */
    public String getZlabel() {
        return zlabel;
    }

    
    /**
     * @param ylabel the ylabel to set
     */
    public void setZlabel(String zlabel) {
        this.zlabel = zlabel;
    }

    
    /**
     * @return the zvalues
     */
    public AxisValues getZvalues() {
        return zvalues;
    }

    
    /**
     * @param zvalues the zvalues to set
     */
    public void setZvalues(String zvalues) {
        this.zvalues = AxisValues.valueOf(zvalues);
    }
    
    
    public String createGenericResultPlotGroup(String outputDir, String netPrefix, int numSim) 
      throws IOException { 

        String filename = netPrefix + "_" + this.getResultType() + "_" +numSim + ".gp";

        FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object

        p.println (this.getPlotSettings());
        p.println ("set title '"+this.getTitle()+"'");
        p.println ("set ylabel '"+this.getYlabel()+"'");
        p.println ("set xlabel '"+this.getXlabel()+"'");
        switch (this.getGraphType()) {
            case D2:
                p.print("plot ");
                break;
            case D3:
                p.println ("set zlabel '"+this.getZlabel()+"'");
                p.print("splot ");
                break;
            default:
                System.err.println ("Error unexpected Graph Type: "+ this.getGraphType());
                break;
        }
        
        int size = this.numberOfPlots();
        for(int i=1; i <= size; i++){
            PlotData pd = this.plotsData.get(i);
            if(i == size) {
                if(pd.getPlotType().length() > 0){
                    p.println("'"+ pd.getFilename() + "' with "+ pd.getPlotType());
                } else {
                    p.println("'"+ pd.getFilename() + "' ");
                }
            } else {
                if(pd.getPlotType().length() > 0){
                    p.print("'"+ pd.getFilename() + "' with "+pd.getPlotType()+", ");
                } else {
                    p.print("'"+ pd.getFilename() + "', ");
                }
            }
        }

        p.close();

        out.close();

        return filename;

    }
    
    
    public String toString(){
        StringBuffer sb = new StringBuffer("GraphDef {graphType=");
        sb.append(this.graphType);
        sb.append("; resultType=");
        sb.append(this.resultType);
        sb.append("; xvalues=");
        sb.append(this.xvalues);
        sb.append("; yvalues=");
        sb.append(this.yvalues);
        sb.append("; zvalues=");
        sb.append(this.zvalues);
        sb.append("; title=");
        sb.append(this.title);
        sb.append("; xlabel=");
        sb.append(this.xlabel);
        sb.append("; ylabel=");
        sb.append(this.ylabel);
        sb.append("; zlabel=");
        sb.append(this.zlabel);
        sb.append("; showPlot=");
        sb.append(this.showPlot);
        sb.append("; plotsData=");
        sb.append(this.plotsData);
        sb.append("}");
        return sb.toString();
    }


    
    /**
     * @return the showPlot
     */
    public boolean isShowPlot() {
        return showPlot;
    }


    
    /**
     * @param showPlot the showPlot to set
     */
    public void setShowPlot(boolean showPlot) {
        this.showPlot = showPlot;
    }


    
    /**
     * @param xvalues the xvalues to set
     */
    public void setXvalues(AxisValues xvalues) {
        this.xvalues = xvalues;
    }


    
    /**
     * @param yvalues the yvalues to set
     */
    public void setYvalues(AxisValues yvalues) {
        this.yvalues = yvalues;
    }


    
    /**
     * @return the plotSettings
     */
    public String getPlotSettings() {
        return plotSettings;
    }


    
    /**
     * @param plotSettings the plotSettings to set
     */
    public void setPlotSettings(String plotSettings) {
        this.plotSettings = plotSettings;
    }
    
}
