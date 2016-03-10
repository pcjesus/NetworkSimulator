/**
 * MSM - Network Simulator
 */


package msm.simulator;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import msm.simulator.GraphDef.AxisValues;
import msm.simulator.exceptions.ConfigException;
import msm.simulator.exceptions.DynamicsException;
import msm.simulator.exceptions.NetStatisticsException;
import msm.simulator.exceptions.NetworkException;
import msm.simulator.network.Dynamics;
import msm.simulator.network.Network;
import msm.simulator.network.Node;
import msm.simulator.network.SpatialDataDistribution;
import msm.simulator.util.DataDistribution;
import msm.simulator.util.NetStatistics;





/**
 * Network Simulator Class
 * 
 * @author pjesus
 * @version 1.0
 */


public class LightNetworkSimulator {
    
    public static enum DataIntervalsStartegy {EQUIWIDTH, EQUIDEPTH};
    
    

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
        
        //Files Output Directory
        String inDir = config.getValueWithDefault(Config.PARAM_INPUT_DIR, Config.DEFAULT_INPUT_DIR);
        
        //Get Math Precision
        int mathPrecision = Integer.parseInt(config.getValueWithDefault(Config.PARAM_MATH_PRECISION, Config.DEFAULT_MATH_PRECISION));
        RoundingMode mathRoundingMode = RoundingMode.valueOf(config.getValueWithDefault(Config.PARAM_MATH_ROUNDING_MODE, Config.DEFAULT_MATH_ROUNDING_MODE));
        MathContext mc = new MathContext(mathPrecision, mathRoundingMode);
        
        //Get Number of Simulations Repetitions
        int repeatSim = Integer.valueOf(config.getValueWithDefault(Config.PARAM_SIMULATION_REPETITION, "1"));
        int extraRepeatSim = Integer.valueOf(config.getValueWithDefault(Config.PARAM_SIMULATION_EXTRA_REPETITION, "0"));
        boolean accountInfinitLoops = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_ACCOUNT_INFINIT_LOOPS, "true"));
        
        // Get Loop Break Limit
        int loopBreak = Integer.parseInt(config.getValueWithDefault(Config.PARAM_LOOP_BREAK_LIMIT, "50"));
        
        // Get Limit Simulation Time
        int limitTime = Integer.parseInt(config.getValue(Config.PARAM_LIMIT_TIME));
        
        // Get Limit Standard Deviation 
        BigDecimal limitDev = new BigDecimal(config.getValue(Config.PARAM_LIMIT_STD_DEVIATION), mc);
        
        //Get Number of Simulations
        int numSim = Integer.valueOf(config.getValue(Config.PARAM_SIMULATION_NUMBER));
        
        
        // Get Results sampling period
        int resultPeriod = Integer.valueOf(config.getValue(Config.PARAM_RESULTS_SAMPLE_PERIOD));
        
        
        boolean saveNetwork = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_SAVE_NETWORK, "true"));
        boolean loadNetwork = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_LOAD_NETWORK, "false"));
        boolean createOverlay = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_CREATE_OVERLAY, "false"));
        boolean loadDataDitribution = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_LOAD_DATA_DISTRIBUTION, "false"));
        
        boolean processDataDistribution = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_DATA_DISTRIBUTION_PROCESSING, "false"));
        int ddIntervals = Integer.valueOf(config.getValueWithDefault(Config.PARAM_DATA_DISTRIBUTION_INTERVALS, "0"));
        DataIntervalsStartegy ddIntervalStrategy = DataIntervalsStartegy.valueOf(config.getValueWithDefault(Config.PARAM_DATA_DISTRIBUTION_INTERVALS_STRATEGY, "EQUIWIDTH"));
        
        SpatialDataDistribution netData = new SpatialDataDistribution(loadDataDitribution, config);
        if(loadDataDitribution){
            
            //Loading Data Distribution
            String dataFile = config.getValue(Config.PARAM_DATA_DISTRIBUTION_TO_LOAD);
            
            System.out.print("Load Data Distribution from file ["+dataFile+"]... ");
            try {
                
                //Load Data Distribution from File
                netData.loadFromFile(inDir, dataFile);
                System.out.println("OK! ");
                
            } catch (Exception e){
                System.out.println("ERROR! ");
                System.err.println("Error loading Data Distribution from file: "+ e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
            
        } else {
            
            //Creating data distribution
            System.out.print("Creating Data Distribution ... ");
            
            try {
                
                netData.generateData();
                System.out.println("OK! ");
                
            } catch (Exception e){
                System.out.println("ERROR! ");
                System.err.println("Error generating Data Distribution: "+ e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
            
        }
        
        if(Boolean.valueOf(config.getValueWithDefault(Config.PARAM_CREATE_DATA_DISTRIBUTION, "true"))){
            
            //Show Data Distribution Graph
            Reports.CreateAndShowDataDistributionGraph(netData, outDir, config, Boolean.valueOf(config.getValue(Config.PARAM_SHOW_DATA_DISTRIBUTION)));
        }
        
        
        //Dynamics Model
        Dynamics dynamism = null;
        try {
            //Setting Dynamics Model
            System.out.print("Setting Network Dynamics Model... ");
            dynamism = new Dynamics(config);
            System.out.println("OK! ");
        } catch (DynamicsException de) {
            System.out.println("ERROR! ");
            System.err.println(de.getMessage());
            System.exit(0);
        }

        
        //Network net = null;
        Network nets[] = new Network[repeatSim+extraRepeatSim];
        String netFiles[] = new String[repeatSim+extraRepeatSim];
        
        if(loadNetwork){
            
            String netFile = config.getValue(Config.PARAM_NETWORK_FILE_TO_LOAD);
            
            System.out.print("Load Network from file ["+netFile+"]... ");
            try {
                
                //Load Network from File
                nets[0] = Network.loadFromFile(inDir, netFile);
                System.out.println("OK! ");
                
            } catch (Exception e){
                System.out.println("ERROR! ");
                System.err.println("Error loading Network from file: "+ e.getMessage());
                System.exit(0);
            }
            
        } else {
        
            System.out.print("Creating Network... ");
            try {
            
                //Create Network Instance
                nets[0] = Network.createInstance(config);
                System.out.println("OK! ");
            
            } catch (Exception e){
                System.out.println("ERROR! ");
                System.err.println("Error creating Network: "+ e.getMessage());
                System.exit(0);
            }
        
            System.out.print("Generating " + nets[0] + "... ");
            try {
                nets[0].generateNetwork();
            } catch (Exception e){
                System.out.println("ERROR! ");
                System.err.println("Error generating Network: "+ e.getMessage());
                System.exit(0);
            }
            System.out.println("(Mean Degree: "+nets[0].calculateMeanDegree(mc)+") OK! ");
            System.out.println("\t\t\t(Min Degree: "+nets[0].calculateMinDegree()+") OK! ");
            System.out.println("\t\t\t(Max Degree: "+nets[0].calculateMaxDegree()+") OK! ");
            //TODO Calculate Network Diameter
            
            //Set network data values
            System.out.print("Setting data distribution... ");
            netData.setDataDistribution(nets[0]);
            System.out.println("OK! ");
            
            
            if(createOverlay){
                
                System.out.print("Creating Network Overlay... ");
                try {
                    nets[0].createOverlay(config);
                    
                } catch (Exception e){
                    System.out.println("ERROR! ");
                    System.err.println("Error creating Network Overlay: "+ e.getMessage());
                    System.exit(0);
                }
            }
            
            
            if(saveNetwork){
                
                System.out.print("Saving Network to file... ");
                try {
                    
                    //Save Network to File
                    String netFile = Network.saveToFile(nets[0], outDir, "_1");
                    netFiles[0] = netFile;
                    System.out.println(" ["+netFile+"] OK! ");
                    
                } catch (Exception e){
                    System.out.println("ERROR! ");
                    System.err.println("Error saving Network to file: "+ e.getMessage());
                    e.printStackTrace();
                }
                
            } 
        
        }

        
        if(Boolean.valueOf(config.getValueWithDefault(Config.PARAM_CREATE_NETWORK_LINKS_HISTOGRAM, "true"))){
            
            //Show Network Links Histogram
            Reports.CreateAndShowNetworkLinksHistogram(nets[0], outDir, config, Boolean.valueOf(config.getValue(Config.PARAM_SHOW_NETWORK_LINKS_HISTOGRAM)));
        }

        
        if(Boolean.valueOf(config.getValueWithDefault(Config.PARAM_CREATE_NETWORK_GRAPH_IMAGE, "true"))){
            
            //Show Network Graph Image
            Reports.CreateAndShowNetworkGraphImage(nets[0], outDir, config, "_1", false, Boolean.valueOf(config.getValue(Config.PARAM_SHOW_NETWORK_GRAPH_IMAGE)));
        }
        
        
        //Creating networks topologies for all repetitions (remaining)
        System.out.println("Creating networks topologies for all repetitions ... ");
        for(int i=1; i < repeatSim+extraRepeatSim; i++){
            
            Network n = null;
            if(i<repeatSim){
                System.out.println("("+i+")... ");
            } else {
                System.out.println("extra ("+i+")... ");
            }
            
            if(loadNetwork){
                try {
                    System.out.print("\tLoad Network from file... ");
                    n = Network.loadFromFile(outDir, netFiles[0]);
                    System.out.println("OK! ");
                } catch (Exception e){
                    System.err.println("Error loading Network from file: "+ e.getMessage());
                    System.exit(0);
                }
            } else {
                try{
                    System.out.print("\tCreating Network... ");
                    n = Network.createInstance(config);
                    System.out.println("OK! ");
                } catch (Exception e){
                    System.err.println("Error creating Network instance: "+ e.getMessage());
                    System.exit(0);
                }
            }
            
            //Generating Network Topology
            try{
                System.out.print("\tGenerating network...");
                n.reset();
                n.generateNetwork();
                System.out.println("OK");
            } catch (Exception e){
                System.err.println("Error generating Network instance: "+ e.getMessage());
                System.exit(0);
            }
            System.out.println("\tNetwork mean degree: "+n.calculateMeanDegree(mc));
            //TODO Calculate network Diameter
            
            //Set network data values
            System.out.print("Setting data distribution... ");
            netData.setDataDistribution(n);
            System.out.println("OK");
            
            if(saveNetwork){
                try {
                    System.out.print("\tSaving Network to file... ");
                    netFiles[i] = Network.saveToFile(n, outDir, "_"+(i+1));
                    System.out.println(" ["+netFiles[i]+"] OK! ");
                } catch (Exception e){
                    System.err.println("Error saving Network to file: "+ e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            } else {
                nets[i] = n; 
            }
            
        }
        System.out.println("OK!\n");
        
        
        
        
        //Load Generic Graphs configuration and Init needed structures
        System.out.print("Load Generic Graph configuration ... ");

        //Get Number of Generic Graph
        int numGraph = Integer.valueOf(config.getValue(Config.PARAM_GRAPH_NUMBER));
        
        Map<Integer, GraphDef> genericGraphs = new HashMap<Integer, GraphDef>(numGraph);
        Map<Integer, Set<Integer>> simulationGraphs = new HashMap<Integer, Set<Integer>>(numSim);
        
        boolean existAllDataValuesGraph = false;
        
        for(int g=1; g <= numGraph; g++){
            
            try {
                String cfgGraphType = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_GRAPH_TYPE);
                String cfgResType = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_RESULT_TYPE);
                String cfgXValue = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_X_VALUES);
                String cfgYValue = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_Y_VALUES);
                String cfgZValue = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_Z_VALUES);
                String cfgTitle = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_TITLE);
                String cfgXLabel = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_X_LABEL);
                String cfgYLabel = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_Y_LABEL);
                String cfgZLabel = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_Z_LABEL);
                String cfgPlotSettings = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_PLOT_SETTINGS);
                String  cfgShowPLot = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_SHOW_PLOT);
                
                GraphDef gd = new GraphDef(cfgGraphType, cfgResType, cfgXValue, cfgYValue, cfgZValue, cfgTitle, cfgXLabel, cfgYLabel, cfgZLabel, cfgPlotSettings, cfgShowPLot);
                genericGraphs.put(g, gd);
                
                if(gd.getXvalues() == AxisValues.AllDataValues || gd.getYvalues() == AxisValues.AllDataValues || gd.getZvalues() == AxisValues.AllDataValues) {
                    existAllDataValuesGraph = true;
                }

                String cfgPlotTypes = config.getValue(g, Config.PARAM_GRAPH_PARAM, PlotData.PARAM_PLOT_TYPES);
                String[] cfgPlotTypesList = cfgPlotTypes.split(";");
                String cfgSim = config.getValue(g, Config.PARAM_GRAPH_PARAM, GraphDef.PARAM_DISPLAYED_SIMULATIONS);
                String[] cfgSimList = cfgSim.split(";");
                if (cfgSimList.length > 0 && cfgPlotTypesList.length ==  cfgSimList.length){
                    for(int i=0; i < cfgSimList.length; i++){
                    
                        Integer simID = Integer.valueOf(cfgSimList[i]);
                        
                        PlotData pd = new PlotData(simID, cfgPlotTypesList[i]);
                        genericGraphs.get(g).addPlotData(pd);
                        
                        if(simulationGraphs.containsKey(simID)){
                            simulationGraphs.get(simID).add(g);
                        } else {
                            Set<Integer> graphs = new HashSet<Integer>();
                            graphs.add(g);
                            simulationGraphs.put(simID, graphs);
                        }
                    }
                } else {
                    System.err.println("Cannot initialize generic graph  "+g);
                    System.err.println("Invalid Parameter "+GraphDef.PARAM_DISPLAYED_SIMULATIONS+": "+cfgSim);
                    break;
                }
                
            }catch (ConfigException ce){
                System.err.println("Cannot init generic graph  "+g);
                System.err.println("Parameter not defined: "+ce.getMessage());
                break;
            }
            
            
            
        }
        
        System.out.println("OK!\n");
        
        
        //Perform each simulation
        for(int i=1; i <= numSim; i++){
            
            // Get algorithms to use
            String sApp = null;
            String sComEng = null;
            try {
                sApp = config.getValue(Config.PARAM_APPLICATION_TYPE, i);
                sComEng = config.getValue(Config.PARAM_COMMUNICATION_ENGINE, i);
            }catch (ConfigException ce){
                System.err.println("Cannot execute simulation  "+i);
                System.err.println("Parameter not defined: "+ce.getMessage());
                break;
            }
            
            System.out.println("Execute Algorithm: "+sApp+" - "+sComEng+" ["+i+"] (repeat: "+repeatSim+"x + "+extraRepeatSim+" extra):");
                
            ComEngine comEng = new ComEngine();
            Class<?>[] argClasses = {Network.class, Config.class, Integer.class, Integer.class};
            comEng.setModel(sComEng);
            
           
            //Init variables to store results for all repetitions
            Map<Integer, List<BigDecimal>> allDevs = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allNormDevs = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allNumMsgSend = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allNumMsgRcv = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
//            Map<Integer, List<BigDecimal>> allNumMsgDiscSend = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
//            Map<Integer, List<BigDecimal>> allNumMsgDiscRcv = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allNumMsgLoss = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allMaxErrors = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allMinErrors = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allMaxs = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allMins = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allRanges = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allMeans = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allMeans_A = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allInitMeans = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allNumValidValues = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allValuesSum = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allTimes = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalTimes = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalDevs = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalNormDevs = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allMaxValues = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allMinValues = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allMeanValues = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allMeanInitValues = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalMessageSend = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalMessageRcv = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
//            Map<Integer, BigDecimal> allTotalMessageDiscardSend = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
//            Map<Integer, BigDecimal> allTotalMessageDiscardRcv = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalMessageLoss = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalLinks = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            Map<Integer, BigDecimal> allTotalActiveLinks = new HashMap<Integer, BigDecimal>(repeatSim+extraRepeatSim);
            
            
            Map<Integer, List<BigDecimal>> allKSMax = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allKSAvg = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allDEMax = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            Map<Integer, List<BigDecimal>> allDEAvg = new HashMap<Integer, List<BigDecimal>>(repeatSim+extraRepeatSim);
            
            Map<Integer, Integer> msgLatencies = null;
            
            //Init data values (List) by iteration (only for the first repetition)
//            Map<Integer, List<BigDecimal>> allDataValues = new HashMap<Integer, List<BigDecimal>>();
            
            //Init node trace map (only trace first repetition)
//            Map<Integer, List<String>> tempStateTrace = new HashMap<Integer, List<String>>();
//            int inodeToTrace = 0;
//            Node nodeToTrace = nets[0].getNode(inodeToTrace);
//            tempStateTrace.put(nodeToTrace.getId(), new ArrayList<String>());
//            try {
//                String cfgTrace = config.getValue(i, Config.PARAM_REPORT_STATE_TRACE_PARAM, Reports.PARAM_NODE_ID);
//                String[] nodeIdsToTrace = cfgTrace.split(";");
//                if(nodeIdsToTrace.length > 0){
//                    tempStateTrace.clear();
//                    for(int j=0; j < nodeIdsToTrace.length; j++){
//                        String nodeId = nodeIdsToTrace[j];
//                        if(nodeId.equalsIgnoreCase(Reports.RANDOMD_NODE_ID)){
//                            nodeToTrace = nets[0].selectRandomNode();
//                        } else {
//                            inodeToTrace = Integer.parseInt(nodeId);
//                            nodeToTrace = nets[0].getNode(inodeToTrace);
//                        }
//                        tempStateTrace.put(nodeToTrace.getId(), new ArrayList<String>());
//                    }
//                }
//            }catch (ConfigException ce){
//                System.err.println("Warning! Node Id to trace not defined. Using default node: "+inodeToTrace);
//                System.err.println("Parameter not defined: "+ce.getMessage());
//            }
            
            // Init infinit loops counter
            int infinitLoops = 0;
            
            Network net0 = null;
            List<Integer> validRepetitions = new ArrayList<Integer>();
            
            //Repeat Simulation
            for(int rep=0; (rep < repeatSim+extraRepeatSim) && (validRepetitions.size() < repeatSim); rep++){
                
                System.out.println("("+rep+")... ");
                
                //Init temporary variables to store results
                List<BigDecimal> tempDevs = new ArrayList<BigDecimal>();
                List<BigDecimal> tempNormDevs = new ArrayList<BigDecimal>();
                List<BigDecimal> tempNumMsgSend = new ArrayList<BigDecimal>();
                List<BigDecimal> tempNumMsgRcv = new ArrayList<BigDecimal>();
//                List<BigDecimal> tempNumMsgDiscSend = new ArrayList<BigDecimal>();
//                List<BigDecimal> tempNumMsgDiscRcv = new ArrayList<BigDecimal>();
                List<BigDecimal> tempNumMsgLoss = new ArrayList<BigDecimal>();
                List<BigDecimal> tempMaxErrors = new ArrayList<BigDecimal>();
                List<BigDecimal> tempMinErrors = new ArrayList<BigDecimal>();
                List<BigDecimal> tempMaxs = new ArrayList<BigDecimal>();
                List<BigDecimal> tempMins = new ArrayList<BigDecimal>();
                List<BigDecimal> tempRanges = new ArrayList<BigDecimal>();
                List<BigDecimal> tempMeans = new ArrayList<BigDecimal>();
                List<BigDecimal> tempMeans_A = new ArrayList<BigDecimal>();
                List<BigDecimal> tempInitMeans = new ArrayList<BigDecimal>();
                List<BigDecimal> tempNumValidValues = new ArrayList<BigDecimal>();
                List<BigDecimal> tempValuesSum = new ArrayList<BigDecimal>();
                List<BigDecimal> tempTimes = new ArrayList<BigDecimal>();
                
                List<BigDecimal> tempKSMax = new ArrayList<BigDecimal>();
                List<BigDecimal> tempKSAvg = new ArrayList<BigDecimal>();
                List<BigDecimal> tempDEMax = new ArrayList<BigDecimal>();
                List<BigDecimal> tempDEAvg = new ArrayList<BigDecimal>();
                                
                int totalTime=0; //total simulation time
                int cntResultSamples=0; //number of results samples
                int nextSampleTime=resultPeriod; //Results sample time
                BigDecimal dev = BigDecimal.ZERO;
                BigDecimal normdev = BigDecimal.ZERO;
                BigDecimal trueValue = BigDecimal.ZERO;
                Network net = null;
                
                boolean loopStop = false;
                boolean validSimulation = true;
                validRepetitions.add(rep);
            
                try {
               
                    //Set Network to use
                    System.out.print("\tSet network topology..."); 
                    if(saveNetwork && (rep != 0)){
                        try {
                            System.out.print("("+netFiles[rep]+")...");
                            net = Network.loadFromFile(outDir, netFiles[rep]);
                        } catch (Exception e){
                            System.err.println("Error loading Network from file: "+ e.getMessage());
                            System.exit(0);
                        }
                    } else {
                        net = (Network) nets[rep].clone();
                    }
                    System.out.println("OK!");
                    System.out.println("\tNetwork mean degree: "+net.calculateMeanDegree(mc));
                    System.out.println("\tNetwork min degree: "+net.calculateMinDegree());
                    System.out.println("\tNetwork max degree: "+net.calculateMaxDegree());
                    

                    
                    //Invoke Init Comunication Method
                    Method im = comEng.getClass().getDeclaredMethod(sComEng+ComEngine.METHOD_INIT_COMMUNICATION, argClasses);
                    Object[] argObjects = {net, config, i, rep};
                    im.invoke(comEng, argObjects);
                    if(validRepetitions.get(0) == rep){
                        comEng.setRegisterMsgLatencies(true);
                    }
                    
                    //Schedule Churn events
                    dynamism.scheduleEvents(limitTime);
                    
                    //Collect initial statistics (time = 0)
                    if(processDataDistribution){
                        
                        DataDistribution realDD = net.getRealDataDistribution(mc);
                        
                        //Initial Max Kolmogorov-Smirnov distance found at a node
                        tempKSMax.add(net.calculateKS_Max(realDD, mc));
                        
                        //Initial Average Kolmogorov-Smirnov distance of all nodes
                        tempKSAvg.add(net.calculateKS_Avg(realDD, mc));
                        
                        //Initial Max Distribution Error (sum of distances) found at a node
                        tempDEMax.add(net.calculateDE_Max(realDD, mc));
                        
                        //Initial Average Distribution Error (sum of distances) of all nodes
                        tempDEAvg.add(net.calculateDE_Avg(realDD, mc));
                        
                    } else {
                        
                        try {
                            trueValue = NetStatistics.calculateInitTrueValue(comEng.getAggFunction(), net.getNodes(), mc);
                        } catch (NetStatisticsException ne) {
                            System.err.println("Unexcpected Error computing statistics: "+ ne.getMessage());
                        }
                        dev = net.calculateRootMeanSquareError(comEng.getAggFunction(), trueValue, mc);
//                        normdev = net.calculateNormalizedRootMeanSquareError(comEng.getAggFunction(), trueValue, mc);
                        normdev = net.calculateNormalizedRootMeanSquareError(dev, trueValue, mc);
                        tempDevs.add(new BigDecimal(dev.toString(), mc));
                        tempNormDevs.add(new BigDecimal(normdev.toString(), mc));
                        BigDecimal maxError = net.getMaxError(comEng.getAggFunction(), trueValue, mc);
                        tempMaxErrors.add(maxError);
                        BigDecimal minError = net.getMinError(comEng.getAggFunction(), trueValue, mc);
                        tempMinErrors.add(minError);
                        BigDecimal max = net.getMaxValue(comEng.getAggFunction(), mc);
                        tempMaxs.add(max);
                        BigDecimal min = net.getMinValue(comEng.getAggFunction(), mc);
                        tempMins.add(min);
                        tempRanges.add(max.subtract(min, mc));
                        tempMeans.add(net.getMeanValue(comEng.getAggFunction(), mc));
                        tempMeans_A.add(net.getMeanValue_A(comEng.getAggFunction(), mc));
                        tempInitMeans.add(net.getMeanInitValue(comEng.getAggFunction(), mc));
                        tempNumValidValues.add(new BigDecimal(net.numberOfValidNodesEstimations(comEng.getAggFunction()), mc));
                        tempValuesSum.add(net.getValuesSum(comEng.getAggFunction(), mc));
                        
                    }
                    
                    //Calculate and store initial global results (time = 0)
                    tempNumMsgSend.add(new BigDecimal(net.getTotalMessageSend(), mc));
                    tempNumMsgRcv.add(new BigDecimal(net.getTotalMessageRcv(), mc));
//                    tempNumMsgDiscSend.add(new BigDecimal(net.getTotalMessageDiscardSend(), mc));
//                    tempNumMsgDiscRcv.add(new BigDecimal(net.getTotalMessageDiscardRcv(), mc));
                    tempNumMsgLoss.add(new BigDecimal(net.getTotalMessageLoss(), mc));
                    tempTimes.add(BigDecimal.ZERO);

                    
                    //Only store initial State Trace results of first valid repetition
//                    if(validRepetitions.get(0) == rep){
//                        for(Integer nId : tempStateTrace.keySet()){
//                            nodeToTrace = net.getNode(nId);
//                            tempStateTrace.get(nId).add(nodeToTrace.getApplication().getState());
//                        }  
//                    }
                    
               
                    //Init Loop Break Variables
                    BigDecimal previous_dev = BigDecimal.ZERO;
                    int loopCnt=0;
                    
                    int sampleCnt=0;
               
                    //Start Simulation Iteration
                    //for(int iter=0; iter < limitIters; iter++, iterCnt=iter){
                    for(int time=0; time < limitTime; time=comEng.getGlobalTime()){
                        
                        //Update Total Time (only for executed iterations)
                        totalTime=time; 
                        
                        //Check if next event in time correspond to churn
                        while((dynamism.nextDynamismEventTime() <= time) && (dynamism.nextDynamismEventTime() <= nextSampleTime)){
                        
                            //Process next dynamic event
                            Event<Integer> dynamicEvt = dynamism.pullNextEvent();
                            int churnNum = dynamism.processEvent(dynamicEvt, net, netData, comEng, config, i, rep);
                            if(churnNum != 0){
                                switch (dynamicEvt.getType()) {
                                    case CHURN:
                                        System.out.println("\t["+time+"] CHURN: "+churnNum+"; Network size: "+net.getNumNodes()+", Mean Degree: "+net.calculateMeanDegree(mc));
                                        break;
                                    case VALUE_CHANGE:
                                        System.out.println("\t["+time+"] VALUE CHANGE: "+churnNum+"; Operator: "+dynamism.getValueChangeOperator(dynamicEvt.getData())+", Rate/Value: "+dynamism.getValueChangeRate(dynamicEvt.getData()));
                                        break;
                                    default:
                                        System.out.println("\t["+time+"] UNKNOWN DYNAMIC EVENT!");
                                        break;
                                }
                            
                                //Set flag to signal churn
                                dynamism.setDynamismOccured(true);
                                
                                //Check if network is partitioned
                                net.checkPartitioning(comEng.getAggFunction());  
                            }
                        }
                   
                        //Only invoke engine event no more result sampling and churn are excepted
                        if((nextSampleTime > time) && (dynamism.nextDynamismEventTime() > time)) {
                        
                            //Invoke Communication Method
                            Method cm = comEng.getClass().getDeclaredMethod(sComEng+ComEngine.METHOD_COMMUNICATION, new Class<?>[]{});
                            cm.invoke(comEng, new Object[]{});
                        }
                   
                        //Only compute and sample result at specific sampling time intervals
                        
                        while ((nextSampleTime <= time) && (nextSampleTime < dynamism.nextDynamismEventTime())){
                            
                            sampleCnt++;
                            
                            
                            //Collect statistics
                            if(processDataDistribution){
                                
                                //Only compute new state statistics if state has changed, otherwise duplicate last computed value
                                if(comEng.hasAppStateChanged() || dynamism.hasDynamismOccured()){
                                
                                    DataDistribution realDD = net.getRealDataDistribution(mc);
                                
                                    //Max Kolmogorov-Smirnov distance found at a node
                                    tempKSMax.add(net.calculateKS_Max(realDD, mc));
                                
                                    //Average Kolmogorov-Smirnov distance of all nodes
                                    tempKSAvg.add(net.calculateKS_Avg(realDD, mc));
                                
                                    //Max Distribution Error (sum of distances) found at a node
                                    tempDEMax.add(net.calculateDE_Max(realDD, mc));
                                
                                    //Average Distribution Error (sum of distances) of all nodes
                                    tempDEAvg.add(net.calculateDE_Avg(realDD, mc));
                                    
                                    comEng.setAppStateChanged(false);
                                    dynamism.setDynamismOccured(false);
                                
                                } else {
                                    
                                    tempKSMax.add(tempKSMax.get(tempKSMax.size()-1));
                                    tempKSAvg.add(tempKSAvg.get(tempKSAvg.size()-1));
                                    tempDEMax.add(tempDEMax.get(tempDEMax.size()-1));
                                    tempDEAvg.add(tempDEAvg.get(tempDEAvg.size()-1));
                                    
                                }
                                
                            } else {
                                
                                //Re-compute true value, may change with due to churn
                                
                                if(dynamism.hasDynamismOccured()){
                                    
                                    if(net.isPartitioned()){
                                        try {
                                            trueValue = NetStatistics.calculateInitTrueValue(comEng.getAggFunction(),net.getPartitionNodes(), mc);
                                        } catch (NetStatisticsException ne) {
                                            System.err.println("Unexcpected Error computing statistics: "+ ne.getMessage());
                                        }
                                    } else {
                                        try {
                                            trueValue = NetStatistics.calculateInitTrueValue(comEng.getAggFunction(),net.getNodes(), mc);
                                        } catch (NetStatisticsException ne) {
                                            System.err.println("Unexcpected Error computing statistics: "+ ne.getMessage());
                                        }
                                    }
                                    
                                }
                                
//                                System.out.println("[time="+time+", sampleCnt: "+sampleCnt+"] TRUE Value: "+trueValue);
                                
                                                                
                                //Only compute new state statistics if state has changed, otherwise duplicate last computed value
                                if(comEng.hasAppStateChanged() || dynamism.hasDynamismOccured()){
                                                                        
                                    dev = net.calculateRootMeanSquareError(comEng.getAggFunction(), trueValue, mc);
                                    normdev = net.calculateNormalizedRootMeanSquareError(dev, trueValue, mc);
                                    
//                                    BigDecimal trueCount = (BigDecimal.ONE).divide(trueValue, mc);
//                                    BigDecimal devCount = net.calculateRootMeanSquareError_Count(comEng.getAggFunction(), trueCount, mc);
//                                    BigDecimal normdev_count = net.calculateNormalizedRootMeanSquareError(devCount, trueCount, mc);
//                                    
//                                    System.out.println("CV(RMSE): "+normdev+"; CV(RMSE) 2: "+normdev_count);
                                    
//                                    normdev = net.calculateNormalizedRootMeanSquareError(comEng.getAggFunction(), trueValue, mc);
                                    tempDevs.add(new BigDecimal(dev.toString(), mc));
                                    tempNormDevs.add(new BigDecimal(normdev.toString(), mc));
                                    BigDecimal maxError = net.getMaxError(comEng.getAggFunction(), trueValue, mc);
                                    tempMaxErrors.add(maxError);
                                    BigDecimal minError = net.getMinError(comEng.getAggFunction(), trueValue, mc);
                                    tempMinErrors.add(minError);
                                    BigDecimal max = net.getMaxValue(comEng.getAggFunction(), mc);
                                    tempMaxs.add(max);
                                    BigDecimal min = net.getMinValue(comEng.getAggFunction(), mc);
                                    tempMins.add(min);
                                    tempRanges.add(max.subtract(min, mc));
                                    tempMeans.add(net.getMeanValue(comEng.getAggFunction(), mc));
                                    tempMeans_A.add(net.getMeanValue_A(comEng.getAggFunction(), mc));                                    
                                    tempInitMeans.add(net.getMeanInitValue(comEng.getAggFunction(), mc));
                                    tempNumValidValues.add(new BigDecimal(net.numberOfValidNodesEstimations(comEng.getAggFunction()), mc));
                                    tempValuesSum.add(net.getValuesSum(comEng.getAggFunction(), mc));
                                    
                                    comEng.setAppStateChanged(false);
                                    dynamism.setDynamismOccured(false);
                                    
                                } else {
                                    
                                    tempDevs.add(tempDevs.get(tempDevs.size()-1));
                                    tempNormDevs.add(tempNormDevs.get(tempNormDevs.size()-1));
                                    tempMaxErrors.add(tempMaxErrors.get(tempMaxErrors.size()-1));
                                    tempMinErrors.add(tempMinErrors.get(tempMinErrors.size()-1));
                                    tempMaxs.add(tempMaxs.get(tempMaxs.size()-1));
                                    tempMins.add(tempMins.get(tempMins.size()-1));
                                    tempRanges.add(tempRanges.get(tempRanges.size()-1));
                                    tempMeans.add(tempMeans.get(tempMeans.size()-1));
                                    tempMeans_A.add(tempMeans_A.get(tempMeans_A.size()-1));
                                    tempInitMeans.add(tempInitMeans.get(tempInitMeans.size()-1));
                                    tempNumValidValues.add(tempNumValidValues.get(tempNumValidValues.size()-1));
                                    tempValuesSum.add(tempValuesSum.get(tempValuesSum.size()-1));
                                    
                                }
                                
                            }
                            
//                            BigDecimal initValue = tempInitMeans.get(tempInitMeans.size()-1);
//                            if(initValue.compareTo(trueValue) != 0){
//                                System.out.println("[time="+time+", sampleCnt: "+sampleCnt+"] TRUE Value: "+trueValue+"; INIT Value: "+initValue);
//                            }
                            
                            //Store global simulation results
                            tempNumMsgSend.add(new BigDecimal(net.getTotalMessageSend(), mc));
                            tempNumMsgRcv.add(new BigDecimal(net.getTotalMessageRcv(), mc));
//                            tempNumMsgDiscSend.add(new BigDecimal(net.getTotalMessageDiscardSend(), mc));
//                            tempNumMsgDiscRcv.add(new BigDecimal(net.getTotalMessageDiscardRcv(), mc));
                            tempNumMsgLoss.add(new BigDecimal(net.getTotalMessageLoss(), mc));
                            tempTimes.add(new BigDecimal(time, mc));

                            
                            //Only on the first valid repetition
//                            if(validRepetitions.get(0) == rep){
//                            
//                                //Store State Trace
//                                for(Integer nId : tempStateTrace.keySet()){
//                                    nodeToTrace = net.getNode(nId);
//                                    if(nodeToTrace == null){
//                                        nodeToTrace = net.getDeadNode(nId);
//                                    }
//                                    tempStateTrace.get(nId).add(nodeToTrace.getApplication().getState());
//                                }
//                            
//                                if(!processDataDistribution && existAllDataValuesGraph){
//                                    //Store data values of all nodes by time
//                                    allDataValues.put(sampleCnt, net.getAllValidNodesValues(comEng.getAggFunction(), mc));
//                                }
//                                
//                            }
                            
                            
                            if (!processDataDistribution) {
                                
                                // Check Loop - While true (count repetitions)
                                if (normdev.compareTo(previous_dev) == 0) {
                                    loopCnt++;
                                } else {
                                    previous_dev = normdev;
                                    loopCnt = 0;
                                }
                            }
                            
                            //Update next sample time
                            nextSampleTime = nextSampleTime + resultPeriod;
                            
                            //Increment results sample count
                            cntResultSamples++;
                            
                        } //while sample results
                        
                   
                        if (!processDataDistribution) {
                            
                            // Stop condition reached
                            if (normdev.compareTo(limitDev) < 0) {
                                break;
                            }

                            // Stop if Loop id detected
                            if (loopCnt >= loopBreak) {
                                loopStop = true;
                                System.out.print("<Infinte Loop Detected>...");
                                if (!accountInfinitLoops) {
                                    infinitLoops++;
                                }
                                break;
                            }
                        }
                        
                        
                    } //for simulation iterations
                                        
                    
                } catch (SecurityException e) {
                    System.out.println("ERROR! ");
                    e.printStackTrace();
                    System.exit(0);
                } catch (NoSuchMethodException e) {
                    System.out.println("ERROR! ");
                    e.printStackTrace();
                    System.exit(0);
                } catch (IllegalArgumentException e) {
                    System.out.println("ERROR! ");
                    e.printStackTrace();
                    System.exit(0);
                } catch (IllegalAccessException e) {
                    System.out.println("ERROR! ");
                    e.printStackTrace();
                    System.exit(0);
                } catch (InvocationTargetException e) {
                    System.out.println("ERROR! ");
                    e.printStackTrace();
                    System.exit(0);
                } catch (DynamicsException e) {
                    System.out.println("ERROR! ");
                    e.printStackTrace();
                    System.exit(0);
                } catch (NetworkException ne) {
                    if(ne.isCritical()){
                        System.out.println("ERROR! ");
                        ne.printStackTrace();
                        System.exit(0);
                    } else {
                        
                        //Reset data stored at the first valid simulation
//                        if(validRepetitions.size() == 1){
//                            for(Integer nId : tempStateTrace.keySet()){
//                                tempStateTrace.get(nId).clear();
//                            }
//                            allDataValues.clear();
//                        }
                        
                        //Invalidate repetition simulation
                        validSimulation = false;
                        validRepetitions.remove(new Integer(rep));
                    }
                } catch (ArithmeticException ne) {
                    System.out.println("ERROR! TRUE Value: "+trueValue);
                    ne.printStackTrace();
                    System.exit(0);
                }
                
                
                if((!accountInfinitLoops && loopStop) || !validSimulation){
                    System.out.println("Results not considered!");
                } else {
                    
                    
                    //Get Stored message latencies (note: no data may be available (e.g. synchronous model))
                    if(validRepetitions.get(0) == rep){
                        msgLatencies = comEng.getMsgLatencies();
                    }
                    
                    //Store simulation iterations results
                    if(processDataDistribution){
                        allKSMax.put(rep, tempKSMax);
                        allKSAvg.put(rep, tempKSAvg);
                        allDEMax.put(rep, tempDEMax);
                        allDEAvg.put(rep, tempDEAvg);
                    } else {
                        allDevs.put(rep, tempDevs);
                        allNormDevs.put(rep, tempNormDevs);
                        allMaxErrors.put(rep, tempMaxErrors);
                        allMinErrors.put(rep, tempMinErrors);
                        allMaxs.put(rep, tempMaxs);
                        allMins.put(rep, tempMins);
                        allRanges.put(rep, tempRanges);
                        allMeans.put(rep, tempMeans);
                        allMeans_A.put(rep, tempMeans_A);
                        allInitMeans.put(rep, tempInitMeans);
                        allNumValidValues.put(rep, tempNumValidValues);
                        allValuesSum.put(rep, tempValuesSum);
                        allTotalDevs.put(rep, dev);
                        allTotalNormDevs.put(rep, normdev);
                        allMaxValues.put(rep, net.getMaxValue(comEng.getAggFunction(), mc));
                        allMinValues.put(rep, net.getMinValue(comEng.getAggFunction(), mc));
                        allMeanValues.put(rep, net.getMeanValue(comEng.getAggFunction(), mc));
                        allMeanInitValues.put(rep, net.getMeanInitValue(comEng.getAggFunction(), mc));
                    }
                                        
                    allNumMsgSend.put(rep, tempNumMsgSend);
                    allNumMsgRcv.put(rep, tempNumMsgRcv);
//                    allNumMsgDiscSend.put(rep, tempNumMsgDiscSend);
//                    allNumMsgDiscRcv.put(rep, tempNumMsgDiscRcv);
                    allNumMsgLoss.put(rep, tempNumMsgLoss);
                    allTimes.put(rep, tempTimes);
                    allTotalTimes.put(rep, new BigDecimal(totalTime, mc));     
                    allTotalMessageSend.put(rep, new BigDecimal(net.getTotalMessageSend(), mc));
                    allTotalMessageRcv.put(rep, new BigDecimal(net.getTotalMessageRcv(), mc));
//                    allTotalMessageDiscardSend.put(rep, new BigDecimal(net.getTotalMessageDiscardSend(), mc));
//                    allTotalMessageDiscardRcv.put(rep, new BigDecimal(net.getTotalMessageDiscardRcv(), mc));
                    allTotalMessageLoss.put(rep, new BigDecimal(net.getTotalMessageLoss(), mc));
                    allTotalLinks.put(rep, new BigDecimal(net.calculateNumberOfLinks(), mc));
                    allTotalActiveLinks.put(rep, new BigDecimal(net.calculateNumberOfActiveLinks(), mc));                   
                    
                }
                
                System.out.println("\tTotal Time: "+totalTime);
                if(processDataDistribution){
                    
                    //TODO Print any desired data distribution statistic 
                    
                } else {
                    
                    System.out.println("\tRMSE: "+dev);
                    System.out.println("\tNorm RMSE: "+normdev);
                    BigDecimal netMin = net.getMinValue(comEng.getAggFunction(), mc);
                    if(netMin.compareTo(BigDecimal.ZERO) == 0){
                        System.out.println("\tNet MIN: Infinite (Value="+netMin+")");
                    } else {
                        System.out.println("\tNet MIN: "+netMin);
                    }
                    BigDecimal netMax = net.getMaxValue(comEng.getAggFunction(), mc);
                    if(netMax.compareTo(BigDecimal.ZERO) == 0){
                        System.out.println("\tNet MAX: Infinite (Value="+netMax+")");
                    } else {
                        //System.out.println("Debug: "+numClouds+", "+netMax);
                        System.out.println("\tNet MAX: "+netMax);
                    }
                    BigDecimal netMean = net.getMeanValue(comEng.getAggFunction(), mc);
                    System.out.println("\tNet MEAN: "+netMean);
                    
                    BigDecimal netMeanInit = net.getMeanInitValue(comEng.getAggFunction(), mc);
                    System.out.println("\tNet MEAN INIT (true): "+netMeanInit);
                    
                }
                
                System.out.println("\tTotal Message Send: "+net.getTotalMessageSend());
                System.out.println("\tTotal Message Receive: "+net.getTotalMessageRcv());
//                System.out.println("\tTotal Message Send Discard: "+net.getTotalMessageDiscardSend());
//                System.out.println("\tTotal Message Receive Discard: "+net.getTotalMessageDiscardRcv());
                System.out.println("\tTotal Message Loss: "+net.getTotalMessageLoss());
                System.out.println("\tNumber of Links: "+net.calculateNumberOfLinks());
                System.out.println("\tNumber of Active Links: "+net.calculateNumberOfActiveLinks());
                System.out.println("\tValid Simultion: "+validSimulation);
                
                //Store resulting network from first valid repetition
                if(validRepetitions.size() == 1 && validRepetitions.get(0) == rep){
                    net0 = net;
                }
                
            } //end repetition for
            
            System.out.println("STOP!!!");
            
            System.out.println("Valid Simulation repetitions: "+validRepetitions);
            System.out.println("Infinit Loops: "+infinitLoops);
            int validTime = totalValidTime(allTotalTimes);
            int validSamplesCnt = totalValidResultSamples(allTotalTimes, resultPeriod);
            System.out.println("Valid Result Samples Count: "+validSamplesCnt);
            System.out.println("Total Time (Mean value): "+calculateMeanValue(allTotalTimes, mc));
            
            if(processDataDistribution){
                
                //TODO Print final desired data distribution statistic 
                
            } else {
                
                System.out.println("Valid Normalized RMSE (Mean value at iteration "+validTime+"): "+meanValueAtIteration(allNormDevs, validSamplesCnt, mc));
                System.out.println("Normalized RMSE (Mean value): "+calculateMeanValue(allTotalNormDevs, mc));
                System.out.println("RMSE (Mean value): "+calculateMeanValue(allTotalDevs, mc));
            }
            
           
           //Compute maximum number of collected result samples 
           int maxResultSamples = (limitTime / resultPeriod) + 1;
            
           if(simulationGraphs.size() > 0) {
           
               //Generate plot data and register file in Generic Graph Map
               Set<Integer> graphs = simulationGraphs.get(i);
               System.out.println("Creating Result Plot Data (Graphs: "+graphs+"): ");
               for(Integer graphNum : graphs){
                   GraphDef gd = genericGraphs.get(graphNum);
//                   boolean allNodesValues = false;
                   boolean dataIntevals = false;
                   boolean useMsgLatencies = false;
                   boolean useMLPercentage = false;
                   boolean useMLCDF = false;
                   List<Integer> latencies = null;
                   
                   List<BigDecimal> xValues;
                   List<BigDecimal> yValues;
                   List<BigDecimal> zValues;
                   
                   if(processDataDistribution){
                       
                       switch(gd.getXvalues()) {
                           case Iterations : xValues = calculateIterationsList(allKSMax,validRepetitions, mc); 
                                             break;
                           case MsgSend : xValues = calculateMeanValueList(allNumMsgSend, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MsgRcv : xValues = calculateMeanValueList(allNumMsgRcv, validRepetitions, maxResultSamples, mc); 
                                         break;  
                           case MsgLoss : xValues = calculateMeanValueList(allNumMsgLoss, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case Time : xValues = calculateMeanValueList(allTimes, validRepetitions, maxResultSamples, mc);
                                       break;
                           case KSMax : xValues = calculateMeanValueList(allKSMax, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case KSAvg : xValues = calculateMeanValueList(allKSAvg, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case DEMax : xValues = calculateMeanValueList(allDEMax, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case DEAvg : xValues = calculateMeanValueList(allDEAvg, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case XPos : 
                           case YPos : 
                           case InitDataValue : xValues = net0.getAllNodesValues(gd.getXvalues(), mc);
                                                break;    
                           case InitDataFrequency : 
                           case InitDataFraction : xValues = net0.getAllNodesValues(AxisValues.InitDataValue, mc);
                                                break;
                           case DataIntervalsCDF :
                           case DataIntervals : dataIntevals = true;
                                                xValues = null;
                                                break;
                           default : 
                               System.err.println("DEBUG: Invalid/Unknown X-Value ["+gd.getXvalues()+"]");
                               xValues = new ArrayList<BigDecimal>();
                               break;
                       }
                       
                       switch(gd.getYvalues()) {
                           case Iterations : yValues = calculateIterationsList(allKSMax, validRepetitions, mc); 
                                             break;
                           case MsgSend : yValues = calculateMeanValueList(allNumMsgSend, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MsgRcv : yValues = calculateMeanValueList(allNumMsgRcv, validRepetitions, maxResultSamples, mc); 
                                         break;
                           case MsgLoss : yValues = calculateMeanValueList(allNumMsgLoss, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case Time : yValues = calculateMeanValueList(allTimes, validRepetitions, maxResultSamples, mc);
                                       break;
                           case KSMax : yValues = calculateMeanValueList(allKSMax, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case KSAvg : yValues = calculateMeanValueList(allKSAvg, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case DEMax : yValues = calculateMeanValueList(allDEMax, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case DEAvg : yValues = calculateMeanValueList(allDEAvg, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case XPos : 
                           case YPos : 
                           case InitDataValue : yValues = net0.getAllNodesValues(gd.getYvalues(), mc);
                                                break;
                           case InitDataFrequency :
                           case InitDataFraction : yValues = net0.getAllNodesValues(AxisValues.InitDataValue, mc);
                                                   break;    
                           case DataIntervalsCDF :                     
                           case DataIntervals : dataIntevals = true;
                                                yValues = null;
                                                break;
                           default : 
                               System.err.println("DEBUG: Invalid/Unknown Y-Value ["+gd.getYvalues()+"]");
                               yValues = new ArrayList<BigDecimal>();
                               break;
                       }
                       
                       switch(gd.getZvalues()) {
                           case Iterations : zValues = calculateIterationsList(allKSMax, validRepetitions, mc); 
                                             break;
                           case MsgSend : zValues = calculateMeanValueList(allNumMsgSend, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MsgRcv : zValues = calculateMeanValueList(allNumMsgRcv, validRepetitions, maxResultSamples, mc); 
                                         break;
                           case MsgLoss : zValues = calculateMeanValueList(allNumMsgLoss, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case Time : zValues = calculateMeanValueList(allTimes, validRepetitions, maxResultSamples, mc);
                                       break;
                           case KSMax : zValues = calculateMeanValueList(allKSMax, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case KSAvg : zValues = calculateMeanValueList(allKSAvg, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case DEMax : zValues = calculateMeanValueList(allDEMax, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case DEAvg : zValues = calculateMeanValueList(allDEAvg, validRepetitions, maxResultSamples, mc); 
                                        break;
                           case XPos : 
                           case YPos : 
                           case InitDataValue : zValues = net0.getAllNodesValues(gd.getZvalues(), mc);
                                                break;
                           default : 
                               System.err.println("DEBUG: Invalid/Unknown Z-Value ["+gd.getZvalues()+"]");
                               zValues = new ArrayList<BigDecimal>();
                               break;
                       }
                       
                       
                   } else {
                       
                       switch(gd.getXvalues()) {
                           case RMSE : xValues = calculateMeanValueList(allDevs, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case NormRMSE : xValues = calculateMeanValueList(allNormDevs, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case Iterations : xValues = calculateIterationsList(allDevs,validRepetitions, mc); 
                                             break;
                           case MsgSend : xValues = calculateMeanValueList(allNumMsgSend, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MsgRcv : xValues = calculateMeanValueList(allNumMsgRcv, validRepetitions, maxResultSamples, mc); 
                                         break;  
    /*                       
                           case MsgDiscSend : xValues = calculateMeanValueList(allNumMsgDiscSend, validRepetitions, maxResultSamples, mc); 
                                              break;
                           case MsgDiscRvc : xValues = calculateMeanValueList(allNumMsgDiscRcv, validRepetitions, maxResultSamples, mc); 
                                             break;
    */
                           case MsgLoss : xValues = calculateMeanValueList(allNumMsgLoss, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MAX_ERROR : xValues = calculateMeanValueList(allMaxErrors, validRepetitions, maxResultSamples, mc);
                                            break;
                           case MIN_ERROR : xValues = calculateMeanValueList(allMinErrors, validRepetitions, maxResultSamples, mc);
                                           break;
                           case MAX : xValues = calculateMeanValueList(allMaxs, validRepetitions, maxResultSamples, mc);
                                      break;
                           case MIN : xValues = calculateMeanValueList(allMins, validRepetitions, maxResultSamples, mc);
                                      break;
                           case RANGE : xValues = calculateMeanValueList(allRanges, validRepetitions, maxResultSamples, mc);
                                        break;
                           case MEAN : xValues = calculateMeanValueList(allMeans, validRepetitions, maxResultSamples, mc);
                                       break;
                           case MEAN_A : xValues = calculateMeanValueList(allMeans_A, validRepetitions, maxResultSamples, mc);
                                       break;            
                           case INIT : xValues = calculateMeanValueList(allInitMeans, validRepetitions, maxResultSamples, mc);
                                       break;
                           case ValidValues : xValues = calculateMeanValueList(allNumValidValues, validRepetitions, maxResultSamples, mc);
                                        break;
                           case SUM : xValues = calculateMeanValueList(allValuesSum, validRepetitions, maxResultSamples, mc);
                                      break;
                           case Time : xValues = calculateMeanValueList(allTimes, validRepetitions, maxResultSamples, mc);
                                       break;
                           case XPos : 
                           case YPos : 
                           case DataValue : 
                           case InitDataValue : xValues = net0.getAllNodesValues(gd.getXvalues(), mc);
                                                break;
//                           case AllDataValues : allNodesValues = true;
//                                                xValues = null;
//                                                break;
                           case DataIntervals : dataIntevals = true;
                                                xValues = null;
                                                break;
                           case MsgLatency : useMsgLatencies = true;
                                             latencies = getLatencyList(msgLatencies);
                                             xValues = new ArrayList<BigDecimal>();
                                             break;
                           case MLFrequency : useMLPercentage = false;
                                              xValues = null;
                                              break;
                           case MLFraction : useMLPercentage = true;
                                               xValues = null;
                                               break;
                           case MLCDF : useMLCDF = true;
                                        xValues = null;
                                        break;
                           default : 
                               System.err.println("DEBUG: Invalid/Unknown X-Value ["+gd.getXvalues()+"]");
                               xValues = new ArrayList<BigDecimal>();
                               break;
                       }
                   
                       switch(gd.getYvalues()) {
                           case RMSE : yValues = calculateMeanValueList(allDevs, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case NormRMSE : yValues = calculateMeanValueList(allNormDevs, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case Iterations : yValues = calculateIterationsList(allDevs, validRepetitions, mc); 
                                             break;
                           case MsgSend : yValues = calculateMeanValueList(allNumMsgSend, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MsgRcv : yValues = calculateMeanValueList(allNumMsgRcv, validRepetitions, maxResultSamples, mc); 
                                         break;  
    /*
                           case MsgDiscSend : yValues = calculateMeanValueList(allNumMsgDiscSend, validRepetitions, maxResultSamples, mc); 
                                              break;
                           case MsgDiscRvc : yValues = calculateMeanValueList(allNumMsgDiscRcv, validRepetitions, maxResultSamples, mc); 
                                             break;
    */
                           case MsgLoss : yValues = calculateMeanValueList(allNumMsgLoss, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MAX_ERROR : yValues = calculateMeanValueList(allMaxErrors, validRepetitions, maxResultSamples, mc); 
                                           break;
                           case MIN_ERROR : yValues = calculateMeanValueList(allMinErrors, validRepetitions, maxResultSamples, mc); 
                                           break;
                           case MAX : yValues = calculateMeanValueList(allMaxs, validRepetitions, maxResultSamples, mc); 
                                      break;
                           case MIN : yValues = calculateMeanValueList(allMins, validRepetitions, maxResultSamples, mc); 
                                      break;
                           case RANGE : yValues = calculateMeanValueList(allRanges, validRepetitions, maxResultSamples, mc);
                                        break;
                           case MEAN : yValues = calculateMeanValueList(allMeans, validRepetitions, maxResultSamples, mc);
                                       break;
                           case MEAN_A : yValues = calculateMeanValueList(allMeans_A, validRepetitions, maxResultSamples, mc);
                                       break;            
                           case INIT : yValues = calculateMeanValueList(allInitMeans, validRepetitions, maxResultSamples, mc);
                                       break;
                           case ValidValues : yValues = calculateMeanValueList(allNumValidValues, validRepetitions, maxResultSamples, mc);
                                              break;
                           case SUM : yValues = calculateMeanValueList(allValuesSum, validRepetitions, maxResultSamples, mc);
                                      break;
                           case Time : yValues = calculateMeanValueList(allTimes, validRepetitions, maxResultSamples, mc);
                                       break;           
                           case XPos : 
                           case YPos : 
                           case DataValue : 
                           case InitDataValue : yValues = net0.getAllNodesValues(gd.getYvalues(), mc);
                                                break;
//                           case AllDataValues : allNodesValues = true;
//                                                yValues = null;
//                                                break;
                           case DataIntervals : dataIntevals = true;
                                                yValues = null;
                                                break;
                           case MsgLatency : useMsgLatencies = true;
                                             latencies = getLatencyList(msgLatencies);
                                             yValues = new ArrayList<BigDecimal>();
                                             break;
                           case MLFrequency : useMLPercentage = false;
                                              yValues = null;
                                              break;
                           case MLFraction : useMLPercentage = true;
                                             yValues = null;
                                             break;
                           case MLCDF : useMLCDF = true;
                                        yValues = null;
                                        break;
                           default : 
                               System.err.println("DEBUG: Invalid/Unknown Y-Value ["+gd.getYvalues()+"]");
                               yValues = new ArrayList<BigDecimal>();
                               break;
                       }
                       
                       switch(gd.getZvalues()) {
                           case RMSE : zValues = calculateMeanValueList(allDevs, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case NormRMSE : zValues = calculateMeanValueList(allNormDevs, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case Iterations : zValues = calculateIterationsList(allDevs, validRepetitions, mc); 
                                             break;
                           case MsgSend : zValues = calculateMeanValueList(allNumMsgSend, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MsgRcv : zValues = calculateMeanValueList(allNumMsgRcv, validRepetitions, maxResultSamples, mc); 
                                         break;  
    /*
                           case MsgDiscSend : zValues = calculateMeanValueList(allNumMsgDiscSend, validRepetitions, maxResultSamples, mc); 
                                              break;
                           case MsgDiscRvc : zValues = calculateMeanValueList(allNumMsgDiscRcv, validRepetitions, maxResultSamples, mc); 
                                             break;
    */
                           case MsgLoss : zValues = calculateMeanValueList(allNumMsgLoss, validRepetitions, maxResultSamples, mc); 
                                          break;
                           case MAX_ERROR : zValues = calculateMeanValueList(allMaxErrors, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case MIN_ERROR : zValues = calculateMeanValueList(allMinErrors, validRepetitions, maxResultSamples, mc); 
                                       break;
                           case MAX : zValues = calculateMeanValueList(allMaxs, validRepetitions, maxResultSamples, mc); 
                                      break;
                           case MIN : zValues = calculateMeanValueList(allMins, validRepetitions, maxResultSamples, mc); 
                                      break;
                           case RANGE : zValues = calculateMeanValueList(allRanges, validRepetitions, maxResultSamples, mc);
                                        break;
                           case MEAN : zValues = calculateMeanValueList(allMeans, validRepetitions, maxResultSamples, mc);
                                       break;
                           case MEAN_A : zValues = calculateMeanValueList(allMeans_A, validRepetitions, maxResultSamples, mc);
                                       break;            
                           case INIT : zValues = calculateMeanValueList(allInitMeans, validRepetitions, maxResultSamples, mc);
                                       break;
                           case ValidValues : zValues = calculateMeanValueList(allNumValidValues, validRepetitions, maxResultSamples, mc);
                                              break;
                           case SUM : zValues = calculateMeanValueList(allValuesSum, validRepetitions, maxResultSamples, mc);
                                      break;
                           case Time : zValues = calculateMeanValueList(allTimes, validRepetitions, maxResultSamples, mc);
                                       break;
                           case XPos : 
                           case YPos : 
                           case DataValue : 
                           case InitDataValue : zValues = net0.getAllNodesValues(gd.getZvalues(), mc);
                                                break;
                           default : 
                               System.err.println("DEBUG: Invalid/Unknown Z-Value ["+gd.getZvalues()+"]");
                               zValues = new ArrayList<BigDecimal>();
                               break;
                       }
                       
                   }
                   

                   
                   //Re-assign X and Y values
//                   if(allNodesValues){
//                       
//                       List<BigDecimal> newValues;
//                       
//                       //Assign all data values to X axis, and re-assign Y values (repeating them according to values by round)
//                       if(xValues == null){
//                           xValues = new ArrayList<BigDecimal>(allDataValues.size());
//                           newValues = new ArrayList<BigDecimal>(yValues.size());
//                           for(Integer rnd : allDataValues.keySet()){
//                               xValues.addAll(allDataValues.get(rnd));
//                               for(int j=0; j < allDataValues.get(rnd).size(); j++){
//                                   newValues.add(yValues.get(rnd));
//                               }
//                           }
//                           yValues = newValues;
//                       
//                       //Assign all data values to Y axis, and re-assign X values (repeating them according to values by round)
//                       } else if(yValues == null) {
//                           yValues = new ArrayList<BigDecimal>(allDataValues.size());
//                           newValues = new ArrayList<BigDecimal>(xValues.size());
//                           for(Integer rnd : allDataValues.keySet()){
//                               yValues.addAll(allDataValues.get(rnd));
//                               for(int j=0; j < allDataValues.get(rnd).size(); j++){
//                                   newValues.add(xValues.get(rnd));
//                               }
//                           }
//                           xValues = newValues;
//                           
//                       //Unexpected graph definition
//                       } else {
//                           System.out.println("ERROR! ");
//                           System.err.println ("Invalid graph definition using "+AxisValues.AllDataValues);
//                       }
//                   }
                   
                   
                   if(dataIntevals){
                       
                       //Reassign values to separate data into intervals
                       if(xValues == null){
                                                                                 
                           if(ddIntervalStrategy == DataIntervalsStartegy.EQUIWIDTH){
                               
                               DataDistribution dd = null;
                               if(gd.getXvalues() == AxisValues.DataIntervals){
                                   
                                   dd = NetStatistics.setEquiWidthHistogram(yValues, ddIntervals, mc);
                                   
                               } else if (gd.getXvalues() == AxisValues.DataIntervalsCDF) {
                                   
                                   dd = NetStatistics.setEquiWidthCDF(yValues, ddIntervals, mc);
                               }
                               
                               if (gd.getYvalues() == AxisValues.InitDataFraction) {
                                   dd.convertFrequencies2Fractions(new BigDecimal(yValues.size()), mc);
                               }
                                 
                               yValues = new ArrayList<BigDecimal>(dd.getFrequencies());
                               xValues = new ArrayList<BigDecimal>(dd.getLabels());                               
                               
                           } else if (ddIntervalStrategy == DataIntervalsStartegy.EQUIDEPTH){
                               
                               DataDistribution dd = null;
                               if(gd.getXvalues() == AxisValues.DataIntervals){
                                   
                                   dd = NetStatistics.setEquiDepthHistogram(yValues, ddIntervals, mc);
                                   
                               } else if (gd.getXvalues() == AxisValues.DataIntervalsCDF) {
                                   
                                   dd = NetStatistics.setEquiDepthCDF(yValues, ddIntervals, mc);
                               }
                               
                               if (gd.getYvalues() == AxisValues.InitDataFraction) {
                                   dd.convertFrequencies2Fractions(new BigDecimal(yValues.size()), mc);
                               }
                                 
                               yValues = new ArrayList<BigDecimal>(dd.getFrequencies());
                               xValues = new ArrayList<BigDecimal>(dd.getLabels());
                               
                           }                       
                           
                       } else if(yValues == null) {
                                                     
                           if(ddIntervalStrategy == DataIntervalsStartegy.EQUIWIDTH){
                               
                               DataDistribution dd = null;
                               if(gd.getYvalues() == AxisValues.DataIntervals){
                                   
                                   dd = NetStatistics.setEquiWidthHistogram(xValues, ddIntervals, mc);
                                   
                               } else if (gd.getYvalues() == AxisValues.DataIntervalsCDF) {
                                   
                                   dd = NetStatistics.setEquiWidthCDF(xValues, ddIntervals, mc);
                               }
                               
                               if (gd.getXvalues() == AxisValues.InitDataFraction) {
                                   dd.convertFrequencies2Fractions(new BigDecimal(xValues.size()), mc);
                               }
                                 
                               xValues = new ArrayList<BigDecimal>(dd.getFrequencies());
                               yValues = new ArrayList<BigDecimal>(dd.getLabels());                            
                               
                           } else if (ddIntervalStrategy == DataIntervalsStartegy.EQUIDEPTH){
                               
                               DataDistribution dd = null;
                               if(gd.getYvalues() == AxisValues.DataIntervals){
                                   
                                   dd = NetStatistics.setEquiDepthHistogram(xValues, ddIntervals, mc);
                                   
                               } else if (gd.getYvalues() == AxisValues.DataIntervalsCDF) {
                                   
                                   dd = NetStatistics.setEquiDepthCDF(xValues, ddIntervals, mc);
                               }
                               
                               if (gd.getXvalues() == AxisValues.InitDataFraction) {
                                   dd.convertFrequencies2Fractions(new BigDecimal(xValues.size()), mc);
                               }
                                 
                               xValues = new ArrayList<BigDecimal>(dd.getFrequencies());
                               yValues = new ArrayList<BigDecimal>(dd.getLabels());                               
                               
                           } 
                           
                       }
                       
                   } //dataIntevals
                   
                   
                   if(useMsgLatencies){
                       
                       if(latencies.size() == 0){
                           
                           //No latencies registered (i.e. synchronous model), use empty x y value lists
                           
                           xValues = new ArrayList<BigDecimal>();
                           yValues = new ArrayList<BigDecimal>();
                           
                       } else {
                       
                           if (xValues == null) {
                           
                               if(useMLPercentage){
                                   xValues = getLatencyPercentages(latencies, msgLatencies, mc);
                               } else if (useMLCDF) {
                                   xValues = getLatencyCDF(latencies, msgLatencies, mc);
                               } else {
                                   xValues = getLatencyFrequencies(latencies, msgLatencies);
                               }
                               yValues = convertIntegerList2BigDecimalList(latencies);
                           
                           } else if (yValues == null) {
                           
                               if(useMLPercentage){
                                   yValues = getLatencyPercentages(latencies, msgLatencies, mc);
                               } else if (useMLCDF) {
                                   yValues = getLatencyCDF(latencies, msgLatencies, mc);
                               } else {
                                   yValues = getLatencyFrequencies(latencies, msgLatencies);
                               }
                               xValues = convertIntegerList2BigDecimalList(latencies);
                           
                           }
                       
                       } // latencies.size() == 0 ?
                       
                   }
                   
               
                   try {
                   
                       //Generate and Register plot data
                       PlotData pd = gd.getPlotsData().get(i);
                       switch (gd.getGraphType()) {
                        case D2 :
                            pd.createGenericResult2DPlot(outDir, nets[0].getReportPrefixName(), sApp, i, gd.getResultType(), xValues, yValues);
                            break;
                        case D3 :
                            pd.createGenericResult3DPlot(outDir, nets[0].getReportPrefixName(), sApp, i, gd.getResultType(), xValues, yValues, zValues);
                            break;
                        default:
                            System.out.println("ERROR! ");
                            System.err.println ("Error unexpected Graph Type: "+gd.getGraphType());
                            break;
                       }
                       
                       System.out.print("\t[filename: "+pd.getFilename()+"]... ");
                       System.out.println("OK! ");
                   
                   }catch (IOException ioe) {
                       System.out.println("ERROR! ");
                       System.err.println ("Error writing to file Plot File");
                   } catch (Exception e){
//                       System.out.println("ERROR to Catch AllDataValues configuration (not processed by LightNetworkSimulator)! ");
                       System.err.println ("DEBUG: Error writing to file Plot File (LightSimulator not process AllDataValues)");
                   }
               }
           } else {
               
               //No plot data generated
               System.out.println("NO Result Plot Data created.");
           }
           
/*           
           System.out.print("Creating Message Send Count plot... ");
           try {
               String plotName = net.createMsgCountPlot(sApp, i, calculateMeanValueList(allNumMsgSend, limitIters, mc), "MsgSend");
               System.out.print("[filename: "+plotName+"]... ");
               msgSendFiles.add(plotName);
                    
               System.out.println("OK! ");
           }catch (IOException ioe) {
               System.out.println("ERROR! ");
               System.err.println ("Error writing to file Plot File");
           }
           
           
           System.out.print("Creating Message Receive Count plot... ");
           try {
               String plotName = net.createMsgCountPlot(sApp, i, calculateMeanValueList(allNumMsgRcv, limitIters, mc), "MsgRcv");
               System.out.print("[filename: "+plotName+"]... ");
               msgRcvFiles.add(plotName);
                    
               System.out.println("OK! ");
           }catch (IOException ioe) {
               System.out.println("ERROR! ");
               System.err.println ("Error writing to file Plot File");
           }
           
           
           System.out.print("Creating Message Discard Send Count plot... ");
           try {
               String plotName = net.createMsgCountPlot(sApp, i, calculateMeanValueList(allNumMsgDiscSend, limitIters, mc), "MsgDiscSend");
               System.out.print("[filename: "+plotName+"]... ");
               msgDiscSendFiles.add(plotName);
                    
               System.out.println("OK! ");
           }catch (IOException ioe) {
               System.out.println("ERROR! ");
               System.err.println ("Error writing to file Plot File");
           }
           
           
           System.out.print("Creating Message Discard Receive Count plot... ");
           try {
               String plotName = net.createMsgCountPlot(sApp, i, calculateMeanValueList(allNumMsgDiscRcv, limitIters, mc), "MsgDiscRcv");
               System.out.print("[filename: "+plotName+"]... ");
               msgDiscRcvFiles.add(plotName);
                    
               System.out.println("OK! ");
           }catch (IOException ioe) {
               System.out.println("ERROR! ");
               System.err.println ("Error writing to file Plot File");
           }
           
           
           System.out.print("Creating Message Loss Count plot... ");
           try {
               String plotName = net.createMsgCountPlot(sApp, i, calculateMeanValueList(allNumMsgLoss, limitIters, mc), "MsgLoss");
               System.out.print("[filename: "+plotName+"]... ");
               msgLossFiles.add(plotName);
                    
               System.out.println("OK! ");
           }catch (IOException ioe) {
               System.out.println("ERROR! ");
               System.err.println ("Error writing to file Plot File");
           }
*/           
           
//           System.out.print("Creating State Trace Files for Node "+tempStateTrace.keySet()+"... ");
//           try {
//               String[] plotNames = Reports.createStateTraceFile(outDir, nets[0].getReportPrefixName(), sApp, i, tempStateTrace, "StatusTrace", config);
//               String sPlotNames = "";
//               for(String plotName : plotNames){
//                   sPlotNames = sPlotNames + ", " + plotName;
//               }
//               sPlotNames = sPlotNames.substring(2, sPlotNames.length());
//               System.out.print("[filenames: "+sPlotNames+"]... ");
//               System.out.println("OK! ");
//           }catch (IOException ioe) {
//               System.out.println("ERROR! ");
//               System.err.println ("Error writing to Status Trace File");
//           } catch (ConfigException ec){
//               System.out.println("ERROR! ");
//               System.err.println ("Error writing to Status Trace File");
//               System.err.println("Configuration Error: " + ec.getMessage());
//           }
           
           if(processDataDistribution){
               
               //TODO Print final desired data distribution statistic 
               
           } else {
               
               BigDecimal netMin = calculateMeanValue(allMinValues, mc);
               if(netMin.compareTo(BigDecimal.ZERO) == 0){
                   System.out.println("Net MIN (Mean value): Infinite (Value="+netMin+")");
               } else {
                   System.out.println("Net MIN (Mean value): "+netMin);
               }
               BigDecimal netMax = calculateMeanValue(allMaxValues, mc);
               if(netMax.compareTo(BigDecimal.ZERO) == 0){
                   System.out.println("Net MAX (Mean value): Infinite (Value="+netMax+")");
               } else {
                   //System.out.println("Debug: "+numClouds+", "+netMax);
                   System.out.println("Net MAX (Mean value): "+netMax);
               }
               BigDecimal netMean = calculateMeanValue(allMeanValues, mc);
               System.out.println("Net MEAN (Mean value): "+netMean);
               
               BigDecimal netInitMean = calculateMeanValue(allMeanInitValues, mc);
               System.out.println("Net INIT (Mean init value): "+netInitMean);
               
           }
           
           System.out.println("Total Message Send (Mean value): "+calculateMeanValue(allTotalMessageSend, mc));
           System.out.println("Total Message Receive (Mean value): "+calculateMeanValue(allTotalMessageRcv, mc));
//           System.out.println("Total Message Send Discard (Mean value): "+calculateMeanValue(allTotalMessageDiscardSend, mc));
//           System.out.println("Total Message Receive Discard (Mean value): "+calculateMeanValue(allTotalMessageDiscardRcv, mc));
           System.out.println("Total Message Loss (Mean value): "+calculateMeanValue(allTotalMessageLoss, mc));
           System.out.println("Total Number of Links (Mean value): "+calculateMeanValue(allTotalLinks, mc));
           System.out.println("Total Number of Active Links (Mean value): "+calculateMeanValue(allTotalActiveLinks, mc));
           System.out.println("\n\n");
           
           if(comEng.createAppComGraph()){
               
               //Show Network Graph Image
               Reports.CreateAndShowNetworkGraphImage(nets[0], outDir, config, "_App_"+i, true, comEng.showAppComGraph());
           }
           
        } //end simulation for
        
        
   
        String pgFile = "";
/*        try {
            //Create Plot group file
            pgFile = nets[0].createResultPlotGroup(outDir, files, numSim);
        }catch (IOException ioe) {
            System.err.println ("Error writing to Result Plot Group File");
        }
        
/*        
        if(Boolean.valueOf(config.getValue(Config.PARAM_SHOW_RESULT_PLOT))){
            
            //Show Network Result Group Plot
            Reports.ShowResultPlot(pgFile, config);
        }
*/        
        
        //Show Generic Graphs
        for(GraphDef gd : genericGraphs.values()){
            
            try {
            
                //Create Plot group file
                pgFile = gd.createGenericResultPlotGroup(outDir, nets[0].getReportPrefixName(), numSim);
                
            }catch (IOException ioe) {
                System.err.println ("Error writing to " + gd.getResultType() + " Plot Group File");
            }
            
            
            if(gd.isShowPlot()){
                
                //Show Graph Result Group Plot
                Reports.ShowResultPlot(pgFile, outDir, config);
            }
            
        }
        
/*        
        if(Boolean.valueOf(config.getValue(Config.PARAM_SHOW_MSG_SEND_COUNT_PLOT))){
        	
        	try {
        		
        		//Create Message Send Count Plot group file
        		pgFile = net.createMsgCountPlotGroup(msgSendFiles, numSim, "MsgSend");
        	}catch (IOException ioe) {
                System.err.println ("Error writing to Message Count Plot Group File");
            }
        	
            //Show Message Count Group Plot
            Reports.ShowMsgCountPlot(pgFile, config);
        	
        }
        
        
        if(Boolean.valueOf(config.getValue(Config.PARAM_SHOW_MSG_RCV_COUNT_PLOT))){
            
            try {
                
                //Create Message Receive Count Plot group file
                pgFile = net.createMsgCountPlotGroup(msgRcvFiles, numSim, "MsgRcv");
            }catch (IOException ioe) {
                System.err.println ("Error writing to Message Count Plot Group File");
            }
            
            //Show Message Count Group Plot
            Reports.ShowMsgCountPlot(pgFile, config);
            
        }
        
        
        if(Boolean.valueOf(config.getValue(Config.PARAM_SHOW_MSG_DISC_SEND_COUNT_PLOT))){
            
            try {
                
                //Create Message Discard Send Count Plot group file
                pgFile = net.createMsgCountPlotGroup(msgDiscSendFiles, numSim, "MsgDiscSend");
            }catch (IOException ioe) {
                System.err.println ("Error writing to Message Count Plot Group File");
            }
            
            //Show Message Count Group Plot
            Reports.ShowMsgCountPlot(pgFile, config);
            
        }
        
        
        if(Boolean.valueOf(config.getValue(Config.PARAM_SHOW_MSG_DISC_RCV_COUNT_PLOT))){
            
            try {
                
                //Create Message Discard Receive Count Plot group file
                pgFile = net.createMsgCountPlotGroup(msgDiscRcvFiles, numSim, "MsgDiscRvc");
            }catch (IOException ioe) {
                System.err.println ("Error writing to Message Count Plot Group File");
            }
            
            //Show Message Count Group Plot
            Reports.ShowMsgCountPlot(pgFile, config);
            
        }
        
        
        if(Boolean.valueOf(config.getValue(Config.PARAM_SHOW_MSG_LOSS_COUNT_PLOT))){
            
            try {
                
                //Create Message Loss Count Plot group file
                pgFile = net.createMsgCountPlotGroup(msgLossFiles, numSim, "MsgLoss");
            }catch (IOException ioe) {
                System.err.println ("Error writing to Message Count Plot Group File");
            }
            
            //Show Message Count Group Plot
            Reports.ShowMsgCountPlot(pgFile, config);
            
        }
*/        
        
        System.out.println("\n");
        System.out.println("--- Simulation End ---");
        System.out.println("\n");
        System.exit(0);
        
    }
    
    
    private static BigDecimal calculateMeanValue(Map<Integer, BigDecimal> allTotals, MathContext mc) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal size = new BigDecimal(allTotals.size(), mc);
        for(BigDecimal value : allTotals.values()){
            total = total.add(value, mc);
        }
        
        return total.divide(size, mc);
    }
    
    
    private static int totalValidTime(Map<Integer, BigDecimal> totalTimesByRepetition){
        int result = Integer.MAX_VALUE;
        for(BigDecimal timeByRep : totalTimesByRepetition.values()){
            int time = timeByRep.intValue();
            if(time < result){
                result = time;
            }
        }
        
        return result;
    }
    
    
    private static int totalValidResultSamples(Map<Integer, BigDecimal> totalTimesByRepetition, int samplePeriod){
        int result = Integer.MAX_VALUE;
        for(BigDecimal timeByRep : totalTimesByRepetition.values()){
            int time = timeByRep.intValue();
            int samplesCnt = time / samplePeriod;
            if(samplesCnt < result){
                result = samplesCnt;
            }
        }
        
        return result;
    }
    
    
    private static BigDecimal meanValueAtIteration(Map<Integer, List<BigDecimal>> allValuesByRepetition, int iterNum, MathContext mc) {
        
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal count = BigDecimal.ZERO;
        
        for(List<BigDecimal> repList : allValuesByRepetition.values()){
            total = total.add(repList.get(iterNum-1), mc);
            count = count.add(BigDecimal.ONE, mc);
        }
        
        return total.divide(count, mc);
    }
    
    
    private static List<BigDecimal> calculateMeanValueList(Map<Integer, List<BigDecimal>> allValuesByRepetition, List<Integer> validRepetitions, int numIter, MathContext mc) {
        
        List<BigDecimal> result = new ArrayList<BigDecimal>(numIter+1);
        
        BigDecimal total[] = new BigDecimal[numIter+1];
        BigDecimal count[] = new BigDecimal[numIter+1];
        
        for(Integer validRep : validRepetitions){
            
            //Only consider values from valid simulation repetitions
            List<BigDecimal> repList = allValuesByRepetition.get(validRep);
            
            for(int i=0; i < repList.size(); i++){
                if(total[i] != null){
                    total[i] = total[i].add(repList.get(i), mc);
                    count[i] = count[i].add(BigDecimal.ONE, mc);
                } else {
                    total[i] = repList.get(i);
                    count[i] = BigDecimal.ONE;
                }
            }
        }
        
        for(int i=0; (i < total.length) && (total[i] != null); i++){
            result.add(total[i].divide(count[i], mc));
        }
        
        return result;
    }
    
    
/*    
    private static List<BigDecimal> calculateIterationsList(int numIter, MathContext mc) {
        
        int size = numIter+1;
        List<BigDecimal> result = new ArrayList<BigDecimal>(numIter+1);
        
        for(int i=0; i < size; i++){
            result.add(new BigDecimal(i, mc));
        }
        
        return result;
    }
*/    
    
    private static List<BigDecimal> calculateIterationsList(Map<Integer, List<BigDecimal>> allValuesByRepetition, List<Integer> validRepetitions, MathContext mc) {
        
        //Determine maximum iteration value
        int max = 0;
        for(Integer validRep : validRepetitions){
            
            List<BigDecimal> repList = allValuesByRepetition.get(validRep);
            
            if (repList.size() > max ) {
                max = repList.size();
            }
        }
        
        //Create Iteration List
        List<BigDecimal> result = new ArrayList<BigDecimal>(max);
        
        for(int i=0; i < max; i++){
            result.add(new BigDecimal(i, mc));
        }
        
        return result;
        
    }
    
    
    private static List<Integer> getLatencyList(Map<Integer, Integer> msgLatencies){
        return new ArrayList<Integer>(msgLatencies.keySet());
    }
    
    private static List<BigDecimal> convertIntegerList2BigDecimalList(List<Integer> integerList){
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(integerList.size());
        for(Integer i : integerList){
            result.add(new BigDecimal(i));
        }
        return result;
    }
    
    private static List<BigDecimal> getLatencyFrequencies(List<Integer> latencies, Map<Integer, Integer> msgLatencies){
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(latencies.size());
        
        for(Integer latency : latencies){
            result.add(new BigDecimal(msgLatencies.get(latency)));
        }
        return result;
    }
    
    private static List<BigDecimal> getLatencyPercentages(List<Integer> latencies, Map<Integer, Integer> msgLatencies, MathContext mc){
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(latencies.size());
        
        BigDecimal totalFrequencies = BigDecimal.ZERO;
        for(Integer frequency : msgLatencies.values()){
            totalFrequencies = totalFrequencies.add(new BigDecimal(frequency));
        }
        
        for(Integer latency : latencies){
            result.add((new BigDecimal(msgLatencies.get(latency))).divide(totalFrequencies, mc));
        }
        return result;
    }
    
    
    private static List<BigDecimal> getLatencyCDF(List<Integer> latencies, Map<Integer, Integer> msgLatencies, MathContext mc){
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(latencies.size());
        
        BigDecimal totalFrequencies = BigDecimal.ZERO;
        for(Integer frequency : msgLatencies.values()){
            totalFrequencies = totalFrequencies.add(new BigDecimal(frequency));
        }
        
        
        //Create a set of sorted latencies
        TreeSet<Integer> sLatencies = new TreeSet<Integer>(latencies);
        
        //Set CDF values
        Map<Integer, BigDecimal> latenciesCDF = new HashMap<Integer, BigDecimal>();
        Integer prevKey = sLatencies.pollFirst();
        BigDecimal prevValue = (new BigDecimal(msgLatencies.get(prevKey))).divide(totalFrequencies, mc);
        latenciesCDF.put(prevKey, prevValue);
        for(Integer curKey : sLatencies){
            
            BigDecimal curF = (new BigDecimal(msgLatencies.get(curKey))).divide(totalFrequencies, mc);
            BigDecimal curValue = prevValue.add(curF, mc);
            latenciesCDF.put(curKey, curValue);
            prevValue = curValue;
        }
        
        //Return results, according to the order of the input latencies
        for(Integer latency : latencies){
            result.add(latenciesCDF.get(latency));
        }
        return result;
    }
    

}
