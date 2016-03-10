/**
 * MSM - Network Simulator
 */
package msm.simulator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import msm.simulator.exceptions.ConfigException;
import msm.simulator.exceptions.ReportException;
import msm.simulator.network.SpatialDataDistribution;
import msm.simulator.network.Network;
import msm.simulator.network.Node;


/**
 * @author pjesus
 *
 */
public class Reports {
    
    
    public static final String PARAM_HEADER = "HEADER";
    public static final String DEFAULT_HEADER = "Iterations;";
    public static final String PARAM_NODE_ID = "NODE_ID";
    public static final String RANDOMD_NODE_ID = "RANDOM";
    
    
    public static void CreateAndShowNetworkLinksHistogram(Network net, String outputDir, Config config, boolean show){
        
        String app = config.getValue(Config.PARAM_CMD_SHOW_RESULT_PLOT);
        
        String pgFile = "";
        try {
            System.out.print("Create Network Links Histogram... ");
            pgFile = createLinksHistogram(net, outputDir);
            System.out.println(" ["+ pgFile +"] OK! ");
        }catch (ReportException re) {
            System.out.println("ERROR! ");
            System.err.println (re.getMessage());
        }
        
        if(show){
            String desc = "Show Network Links Histogram [filename: "+pgFile+"]";
            ExternalProcess ep = new ExternalProcess(MessageFormat.format(app, new Object[]{pgFile}), desc, outputDir);
            ep.start();
        }
        
    }
    
    
    public static String createLinksHistogram(Network net, String outputDir) throws ReportException {
        
        //Set filename
        String filename = net.getReportPrefixName() + "-links";
        
        //Get Number of Links per Node and Max Node Links
        int maxLinks = Integer.MIN_VALUE;
        List<Integer> nls = new ArrayList<Integer>(net.getNumNodes());
        for(Node n : net.getNodes()){
            int numLinks = n.numberOfLinks();
            nls.add(numLinks);
            if(numLinks > maxLinks){
                maxLinks = numLinks;
            }
        }
        
        // Create Histogram Values (Number of Nodes per Link Number)
        int[] nn = new int[(maxLinks + 1)]; //default values: 0
        for(Integer nl : nls){
            nn[nl] = nn[nl] + 1;
        }
        
        FileOutputStream out;
        PrintStream p;
        
        try {
            
            out = new FileOutputStream(outputDir + filename + ".xy"); // declare a file output object
            p = new PrintStream(out); // declare a print stream object

            for (int i=0; i < (maxLinks + 1); i++) {
                p.println ((i+1) + " " + nn[i]);
            }
            p.close();
            out.close();
            
        } catch (IOException ioe) {
            throw new ReportException("Error writing to Histogram XY file: "+ioe.getMessage(), ioe);
        }
        
        try {
            
            out = new FileOutputStream(outputDir + filename + ".gp"); // declare a file output object
            p = new PrintStream(out); // declare a print stream object
            p.println ("set logscale y");
            p.println ("set logscale x");
            p.println ("set title 'LINKS DISTRIBUTION'");
            p.println ("set ylabel 'Number of nodes'");
            p.println ("set xlabel 'Number of links'");
            p.println ("plot '"+filename+".xy' with boxes, '"+filename+".xy' notitle");
            p.close();
            out.close();
            
        } catch (IOException ioe) {
            throw new ReportException("Error writing to Histogram GP file: "+ioe.getMessage(), ioe);
        }

        return filename + ".gp";
        
    }
    
    
    
    
    
    public static void CreateAndShowNetworkGraphImage(Network net, String outputDir, Config config, String suffix, boolean showAppStatus, boolean show){
        
        String app = config.getValue(Config.PARAM_CMD_SHOW_GRAPH_IMAGE);
        
        String graphImageFile = "";
        try {
            graphImageFile = createNetworkGraphImage(net, outputDir, config, suffix, showAppStatus, show);
        } catch (ReportException re) {
            System.err.println (re.getMessage());
        }
        
        if(show){
            String desc = "Show Network Graph image [filename: "+graphImageFile+"]";
            ExternalProcess ep = new ExternalProcess(MessageFormat.format(app, new Object[]{graphImageFile}), desc, outputDir);
            ep.start();
        }
        
    }
    
        
    public static String createNetworkGraphImage(Network net, String outputDir, Config config, String suffix, boolean showAppStatus, boolean show) 
        throws ReportException {
        
        String app = config.getValue(Config.PARAM_CMD_GENERATE_GRAPH_IMAGE);
        String netName = net.getReportPrefixName();
        String netGraphFile = netName + suffix +".dot";
        String netGraphImage = netName + suffix +".png";
        
        
        FileOutputStream out; // declare a file output object
        PrintStream p; // declare a print stream object

        try
        {
                // Create a new file output stream
                // connected to .dot filename
                out = new FileOutputStream(outputDir + netGraphFile);

                // Connect print stream to the output stream
                p = new PrintStream( out );

                p.println (net.toStringGraph(showAppStatus));

                p.close();
                
                out.close();
                
        } catch (IOException e) {
            throw new ReportException("Error writing to Network Graph file: "+e.getMessage(), e);
        }
        
        if(show){
            String desc = "Create Network Graph Image File";
            ExternalProcess ep = new ExternalProcess(MessageFormat.format(app, new Object[]{netGraphFile, netGraphImage}), desc, outputDir);
            ep.executeComand();
        }
        
        return netGraphImage;
    }
    
    
    public static void ShowResultPlot(String plotGroupFile, String outputDir, Config config){
        
        String app = config.getValue(Config.PARAM_CMD_SHOW_RESULT_PLOT);
        
        String desc = "Show Network Result Group Plot [filename: "+plotGroupFile+"]";
        ExternalProcess ep = new ExternalProcess(MessageFormat.format(app, new Object[]{plotGroupFile}), desc, outputDir);
        ep.start();
        
    }
    
/*    
    public static void ShowMsgCountPlot(String plotGroupFile, Config config){
        
        String app = config.getValue(Config.PARAM_CMD_SHOW_MSG_COUNT_PLOT);
        
        String desc = "Show Simulation Message Count Group Plot [filename: "+plotGroupFile+"]";
        ExternalProcess ep = new ExternalProcess(MessageFormat.format(app, new Object[]{plotGroupFile}), desc);
        ep.start();
        
    }
*/    
    
    public static String[] createStateTraceFile(String outputDir, String prefixeName, String appType, Integer simIndex, Map<Integer, List<String>> stateTrace, String type, Config config) 
      throws IOException, ConfigException {
        
        String[] filenames = new String[stateTrace.size()];
        String header = config.getValue(simIndex, Config.PARAM_REPORT_STATE_TRACE_PARAM, Reports.PARAM_HEADER);
        
        int idx = 0;
        for(Integer nodeId : stateTrace.keySet()){
            
            String filename = prefixeName + "_" + appType + "_" + type + "[" + nodeId + "]" + simIndex + ".csv";
        
            FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
            PrintStream p = new PrintStream(out); // declare a print stream object
            
            p.println (Reports.DEFAULT_HEADER + header);
        
            int i=0;
            for (String s : stateTrace.get(nodeId)) {

                p.println (i + ";" + s);
                i++;
            }
        
            p.close();
        
            out.close();
            
            filenames[idx] = filename;
            idx++;
        }
        
        return filenames;
        
    }
    
    
    
    public static void CreateAndShowDataDistributionGraph(SpatialDataDistribution dd, String outputDir, Config config, boolean show){
        
        String app = config.getValue(Config.PARAM_CMD_SHOW_RESULT_PLOT);
        
        String pgFile = "";
        try {
            System.out.print("Create Data Distribution Graph... ");
            pgFile = createDataDistributionGraph(dd, outputDir);
            System.out.println(" ["+ pgFile +"] OK! ");
        }catch (ReportException re) {
            System.out.println("ERROR! ");
            System.err.println (re.getMessage());
        }
        
        if(show){
            String desc = "Show Data Distribution Graph [filename: "+pgFile+"]";
            ExternalProcess ep = new ExternalProcess(MessageFormat.format(app, new Object[]{pgFile}), desc, outputDir);
            ep.start();
        }
        
    }
    
    
    public static String createDataDistributionGraph(SpatialDataDistribution dd, String outputDir) throws ReportException {
        
        //Set filename
        String filename = dd.getReportPrefixName() + "-graph";
        
        
        FileOutputStream out;
        PrintStream p;
        
        double matrixdata[][] = dd.getData();
        
        try {
            
            out = new FileOutputStream(outputDir + filename + ".xyz"); // declare a file output object
            p = new PrintStream(out); // declare a print stream object

            //Print data distribution (x:matrix line; y:matrix row; z:matrix data)
            for (int i=0; i < matrixdata.length; i++) {
                for (int j=0; j < matrixdata[i].length; j++){
                    p.println (i + " " + j + " " + matrixdata[i][j]);
                }
            }
            p.close();
            out.close();
            
        } catch (IOException ioe) {
            throw new ReportException("Error writing to Graph XYZ file: "+ioe.getMessage(), ioe);
        }
        
        try {
            
            out = new FileOutputStream(outputDir + filename + ".gp"); // declare a file output object
            p = new PrintStream(out); // declare a print stream object
            p.println("set multiplot layout 1,2 title 'DATA DISTRIBUTION'");
            p.println();
            p.print("set dgrid3d ");
            p.print(matrixdata.length);
            p.print(",");
            p.println(matrixdata[0].length);
            p.println("set pm3d");
            p.println ("splot '"+filename+".xyz' notitle with pm3d");
            p.println();
            p.print("set dgrid3d ");
            p.print(matrixdata.length);
            p.print(",");
            p.println(matrixdata[0].length);
            p.println("set pm3d map");
            p.println ("splot '"+filename+".xyz' notitle");
            p.close();
            out.close();
            
        } catch (IOException ioe) {
            throw new ReportException("Error writing to Graph GP file: "+ioe.getMessage(), ioe);
        }

        return filename + ".gp";
        
    }
    

}
