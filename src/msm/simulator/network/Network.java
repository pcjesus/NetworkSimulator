/**
 * MSM - Network Simulator
 */


package msm.simulator.network;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import msm.simulator.ComEngine;
import msm.simulator.Config;
import msm.simulator.Event.EventType;
import msm.simulator.GraphDef.AxisValues;
import msm.simulator.apps.Application;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.NetworkException;
import msm.simulator.util.DataDistribution;
import msm.simulator.util.DataDistribution.DD_TYPE;
import msm.simulator.util.NetStatistics;
import msm.simulator.util.RatingCalc;
import msm.simulator.util.SetUtils;
import msm.simulator.util.NetStatistics.AggFunctions;

/**
 * Network Class
 * 
 * @author pjesus
 * @version 1.0
 */
public abstract class Network implements Serializable, Cloneable {
    
    public enum NetworkOverlay {TREE}

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 1263178990864845332L;

    //Network Type
    private String type;
    
    //Initial number of nodes
    private int initNumNodes;
    
    //Total number of nodes created (from the beginning of times)
    private int totalNumNodes;
    
    //Active nodes
    private int numNodes;
    private Map<Integer, Node> nodes;
    
    //Dead Nodes (Due to crash, churn)
    private int numDeadNodes;
    private Map<Integer, Node> deadNodes;

    //Links list (over all the network)
    private List<Link> links;

    //Application specific for count (should be elsewhere)
    private int numClouds;
    Set<Integer> cloudNodes;
    
    
    //Number of network partitions
    private int maxPartitionSize;
    private Set<Integer> maxPartitionNodes;
    
    //List of partition sizes
    private List<Integer> partitions;
    
    private int diameter;


    /**
     * Constructors
     */
    
    public Network() {
        this.nodes = new HashMap<Integer, Node>();
        this.links = new ArrayList<Link>();
        
        this.deadNodes = new HashMap<Integer, Node>();
    }

    public Network(int num_nodes) {
        this.initNumNodes = num_nodes;
        this.totalNumNodes = num_nodes;
        this.numNodes = num_nodes;
        this.nodes = new HashMap<Integer, Node>(num_nodes);
        this.links = new ArrayList<Link>();
        this.numClouds = 0;
        this.cloudNodes = new HashSet<Integer>();
        
        this.numDeadNodes = 0;
        this.deadNodes = new HashMap<Integer, Node>();
        
        this.partitions = new ArrayList<Integer>();
        this.maxPartitionSize = 0;
    }
    
    public Network(Network net){
        
        this.type = new String(net.getType());
        
        this.initNumNodes = net.getInitNumNodes();
        this.totalNumNodes = net.getTotalNumNodes();
        
        this.numNodes = net.getNumNodes();
        this.nodes = new HashMap<Integer, Node>(net.getNumNodes());
        for(Integer id : net.getNodesKeySet()){
            this.nodes.put(new Integer(id), new Node(net.getNode(id)));
        }
        
        this.numDeadNodes = net.getNumDeadNodes();
        this.deadNodes = new HashMap<Integer, Node>(net.getNumDeadNodes());
        for(Integer id : net.getDeadNodesKeySet()){
            this.deadNodes.put(new Integer(id), new Node(net.getDeadNode(id)));
        }
        
        this.links = new ArrayList<Link>(net.getNumOfLinks());
        for(Link l : net.getLinks()){
            Node n1 = this.nodes.get(l.getNode1().getId());
            Node n2 = this.nodes.get(l.getNode2().getId());
            this.links.add(new Link(n1, n2, l.getStatus(), l.getAppStatus()));
        }
        
        this.numClouds = net.getNumClouds();
        this.cloudNodes = new HashSet<Integer>(net.getNumClouds());
        for(Integer id : net.getCloudNodes()){
            this.cloudNodes.add(new Integer(id));
        }
        
        this.maxPartitionSize = net.getMaxPartitionSize();
        this.maxPartitionNodes = new HashSet<Integer>(net.getMaxPartitionSize());
        for(Integer id : net.getMaxPartitionNodes()){
            this.maxPartitionNodes.add(new Integer(id));
        }
        this.partitions = new ArrayList<Integer>(net.getNumberOfPartitions());
        for(Integer psize : net.getPartitions()){
            this.partitions.add(new Integer(psize));
        }
        
        this.diameter = net.getDiameter();
    }
    
    public abstract Object clone();
    
    
    public void init(String type, Config config){
        this.type = new String(type);
        this.initNumNodes = Integer.valueOf(config.getValue(Config.PARAM_NETWORK_SIZE));
        this.totalNumNodes = this.initNumNodes;
        this.numNodes = this.initNumNodes;
        this.numClouds = 0;
        this.cloudNodes = new HashSet<Integer>();
        
        this.numDeadNodes = 0;
        
        this.maxPartitionSize = 0;
        this.partitions = new ArrayList<Integer>();
        this.maxPartitionNodes = new HashSet<Integer>();
    }
    
    
    public static Network createInstance(Config config)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException {

      //Create Network Instance
      String netType = config.getValue(Config.PARAM_NETWORK_TYPE);
      String className = Network.class.getName() + netType;
      Class<?> networkClass = Class.forName(className);
      Network net = (Network)networkClass.newInstance();
      
      //Initialize Network Instance
      net.init(netType, config);

      return net;
  }

    
    public Set<Integer> getNodesKeySet(){
        return this.nodes.keySet();
    }
    
    public Set<Integer> getDeadNodesKeySet(){
        return this.deadNodes.keySet();
    }
    
    
    public void setType(String type){
        this.type = type;
    }
    
    public String getType(){
        return this.type;
    }

    
    /**
     * @return Returns the numClouds.
     */
    public int getNumClouds() {
        return this.numClouds;
    }

    
    /**
     * @param numClouds The numClouds to set.
     */
    public void setNumClouds(int numClouds) {
        this.numClouds = numClouds;
    }

    
    /**
     * @return the cloudNodes
     */
    public Set<Integer> getCloudNodes() {
        return cloudNodes;
    }
    
    public void addCloudNode(Integer id){
        this.cloudNodes.add(id);
    }
    

    /**
     * @return Returns the initNumNodes.
     */
    public int getInitNumNodes() {
        return this.initNumNodes;
    }


    /**
     * @param numNodes The initNumNodes to set.
     */
    public void setInitNumNodes(int initNumNodes) {
        this.initNumNodes = initNumNodes;
    }
    
    
    /**
     * @return Returns the totalNumNodes.
     */
    public int getTotalNumNodes() {
        return this.totalNumNodes;
    }


    /**
     * @param numNodes The totalNumNodes to set.
     */
    public void setTotalNumNodes(int totalNumNodes) {
        this.totalNumNodes = totalNumNodes;
    }
    
    
    /**
     * @return Returns the numNodes.
     */
    public int getNumNodes() {
        return this.numNodes;
    }


    /**
     * @param numNodes The numNodes to set.
     */
    public void setNumNodes(int numNodes) {
        this.numNodes = numNodes;
    }
    
    
    /**
     * @return Returns the nodes.
     */
    public Map<Integer, Node> getNodesMap() {
        return nodes;
    }

    
    /**
     * @param nodes The nodes to set.
     */
    public void setNodes(Map<Integer, Node> nodes) {
        this.nodes = new HashMap<Integer, Node>(nodes);
    }
    
    
    public Node getNode(int id){
        return this.nodes.get(id);
    }
    
    public void addNode(Integer id, Node node){
        this.nodes.put(id, node);
    }
    
    
    public Collection<Node> getNodes(){
        return this.nodes.values();
    }
    
    
    /**
     * @return Returns the numDeadNodes.
     */
    public int getNumDeadNodes() {
        return this.numDeadNodes;
    }


    /**
     * @param numNodes The numDeadNodes to set.
     */
    public void setNumDeadNodes(int numDeadNodes) {
        this.numDeadNodes = numDeadNodes;
    }
    
    /**
     * @return Returns the dead nodes.
     */
    public Map<Integer, Node> getDeadNodesMap() {
        return deadNodes;
    }

    
    /**
     * @param deadNodes The dead nodes to set.
     */
    public void setDeadNodes(Map<Integer, Node> deadNodes) {
        this.deadNodes = new HashMap<Integer, Node>(deadNodes);
    }
    
    
    public Node getDeadNode(int id){
        return this.deadNodes.get(id);
    }
    
    public void addDeadNode(Integer id, Node node){
        this.deadNodes.put(id, node);
    }
    
    
    public Collection<Node> getDeadNodes(){
        return this.deadNodes.values();
    }  
    
    
    public boolean isDeadNode(int id){
        return this.deadNodes.containsKey(id);
    }
    
    
    /**
     * @return the bigPartitionSize
     */
    public int getMaxPartitionSize() {
        return this.maxPartitionSize;
    }

    
    /**
     * @param bigPartitionSize the bigPartitionSize to set
     */
    public void setMaxPartitionSize(int maxPartitionSize) {
        this.maxPartitionSize = maxPartitionSize;
    }

    
    /**
     * @return the partitions
     */
    public List<Integer> getPartitions() {
        return this.partitions;
    }

    
    /**
     * @param partitions the partitions to set
     */
    public void setPartitions(List<Integer> partitions) {
        this.partitions = partitions;
    }
    
    public void addPartition(Integer partitionSize) {
        this.partitions.add(partitionSize);
    }
    
    public int getNumberOfPartitions(){
        return this.partitions.size();
    }
    
    
    public Set<Integer> getMaxPartitionNodes() {
        return this.maxPartitionNodes;
    }
    
    public boolean isPartitioned(){
        return (this.maxPartitionSize > 0);
    }
    
    /**
     * @param maxPartitionNodes the maxPartitionNodes to set
     */
    public void setMaxPartitionNodes(Set<Integer> maxPartitionNodes) {
        this.maxPartitionNodes = maxPartitionNodes;
    }

    public void clearPartitionsData(){
        this.partitions.clear();
        this.maxPartitionNodes.clear();
        this.maxPartitionSize = 0;
    }
    

    public int getNumOfLinks(){
        return this.links.size();
    }
    
    public List<Link> getLinks(){
        return this.links;
    }
    
    public List<Link> getLinks(Node n){
        List<Link> result = new ArrayList<Link>();
        for(Link l : this.links){
            if(l.contains(n)){
                result.add(l);
            }
        }
        return result;
    }
    
    public List<Link> getLinks(int n){
        List<Link> result = new ArrayList<Link>();
        for(Link l : this.links){
            if(l.contains(n)){
                result.add(l);
            }
        }
        return result;
    }
    
    public Link getLink(Node n1, Node n2){
        for(Link l : this.links){
            if(l.equals(n1, n2)){
                return l;
            }
        }
        return null;
    }
    
    
    public Link getLink(int n1, int n2){
        for(Link l : this.links){
            if(l.equals(n1, n2)){
                return l;
            }
        }
        return null;
    }
    
    public void resetLinksAppStatus(boolean useOverlay){
        for(Link l : this.links){
            if(useOverlay){
                l.setAppStatus(l.getStatus());
            } else {
                l.setAppStatus(Link.BIDIRECTIONAL);
            }
        }
    }

    
    protected void addLink(Node n1, Node n2, int status){
        this.links.add(new Link(n1, n2, status));
        n1.addLink(n2.getId());
        n2.addLink(n1.getId());
        n1.addOverlayLink(n2.getId());
        n2.addOverlayLink(n1.getId());
    }
    
    
    protected void addLink(Integer id1, Integer id2, int status){
        Node n1 = this.getNode(id1);
        Node n2 = this.getNode(id2);
        this.links.add(new Link(n1, n2, status));
        n1.addLink(id2);
        n2.addLink(id1);
        n1.addOverlayLink(id2);
        n2.addOverlayLink(id1);
    }
    
    
    protected boolean delLink(Node n1, Node n2){
        Link l = this.getLink(n1, n2);
        
        if(l == null){
            return false;
        } else {
            boolean result = n1.delLink(n2.getId());
            result = result && n2.delLink(n1.getId());
            return result && this.links.remove(l);
        }
    }
    
    
    protected boolean delLink(Integer id1, Integer id2){
        Link l = this.getLink(id1, id2);
        
        if(l == null){
            return false;
        } else {
            boolean result = this.getNode(id1).delLink(id2);
            result = result && this.getNode(id2).delLink(id1);
            return result && this.links.remove(l);
        }
    }
    
    
    
    public abstract void generateNetwork() throws NetworkException;
    
    
    public void reset(){
        this.nodes.clear();
        this.links.clear();
    }
    
    
    
    
    /**
     * Use the Pitagoras Teorm to calculate the distance between two nodes.
     * a^2 + b^2 = c^2, where c is the distance between the two nodes.
     * @param n1 Node one;
     * @param n2 Node two:
     * @return double with the distance between the two argument nodes.
     */
    protected double calcNodesDistance(Node n1, Node n2){
        return Math.hypot(Math.abs(n1.getX()-n2.getX()), Math.abs(n1.getY()-n2.getY()));
    }
    
    
    protected double sqrDistance(Node n1, Node n2){
        return Math.pow(n1.getX()-n2.getX(), 2) + Math.pow(n1.getY()-n2.getY(), 2);
    }
    
    
    
    /**
     * Get the nodes closest to the central position
     * @return
     */
    public Node getCentralNode(){
        
        //Central position coordinates
        double x = 0.5;
        double y = 0.5;
        
        double minPowDistance = 1;
        Node result = null;
        
        for(Node n : this.getNodes()){
            double powDistance = Math.pow(n.getX()-x, 2) + Math.pow(n.getY()-y, 2);
            if(powDistance < minPowDistance){
                minPowDistance = powDistance;
                result = n;
            }
        }
        
        return result;
    }


    

    /**
     * Convert Network data to String (to generate a Graph description file)
     * 
     * @return String representing the information of the network (to use in a graph description file)
     */
    
    public String toStringGraph(boolean showAppStatus) {
        StringBuffer sb = new StringBuffer("Graph G { \n"
                + "node[width=.05,hight=.05,fontsize=7]\n");

        for (Node n : this.nodes.values()) {
            sb.append(n.toStringGraph());
        }

        if(showAppStatus){
            for (Link l : this.links) {
                sb.append(l.toStringAppGraph());
            }
        } else {
            for (Link l : this.links) {
                sb.append(l.toStringGraph());
            }
        }
/*        
        sb.append("node[shape=box,style=filled];\n");
        if(showAppStatus){
            sb.append("\"App Network\"\n");
        } else {
            sb.append("\"Base Network\"\n");
        }
*/ 
        sb.append("}");
       
        return sb.toString();

    }
    
    

/*    public void tick(){
        for(Node n : this.getNodes()){
            n.tick();
        }
    }
    
    public String debugTickStatus(){
        StringBuffer sb = new StringBuffer();
        for(Node n : this.getNodes()){
            sb.append("NODE["+n.getId()+"] -> ");
            sb.append(n.debugTickStatus());
            sb.append("\n");
        }
        
        return sb.toString();
    }
*/    
    
/*
    public Set <Integer> getSendingNodes(){
        Set <Integer>result = new HashSet<Integer>();
        for(Node n: this.getNodes()){
            if(n.isSender()){
                result.add(n.getId());
            }
        }
        
        return result;
    }
    
    
    public Set <Integer> getReceivingNodes(){
        Set <Integer>result = new HashSet<Integer>();
        for(Node n: this.getNodes()){
            if(n.isReceiver()){
                result.add(n.getId());
            }
        }
        
        return result;
    }
*/    
  
    
    /** @deprecated **/
    public BigDecimal calculateDeviation(MathContext mathContext){
        BigDecimal a = RatingCalc.divide(this.numClouds, this.numNodes, mathContext);
        BigDecimal s = BigDecimal.ZERO;
        
        for (Node n : this.nodes.values()) {
            //s = s + (V-a)*(V-a)
            s = s.add(
                    (n.getApplication().getValue().subtract(a, 
                                                            mathContext)).multiply(n.getApplication().getValue().subtract(a, 
                                                                                                                          mathContext), 
                                                                                   mathContext), 
                    mathContext);
        }
        
        // sqrt(s / n) / a        
        return (new BigDecimal(Math.sqrt( s.divide(new BigDecimal(this.numNodes), 
                                                  mathContext).doubleValue()) )).divide(a, 
                                                                                       mathContext);
    }
    
    //RMSE "Normalized?!..."
    /** @deprecated **/
    public BigDecimal calculateRootMeanSquareError(MathContext mathContext){
        BigDecimal nc = new BigDecimal(this.numClouds, mathContext);
        BigDecimal nn = new BigDecimal(this.numNodes, mathContext);
        BigDecimal a = nc.divide(nn, mathContext);
        BigDecimal s = BigDecimal.ZERO;
        
        for (Node n : this.nodes.values()) {
            
            //error = (value - a)
            BigDecimal error = (n.getApplication().getValue()).subtract(a, mathContext);
            
            //s = s + (error)*(error)
            s = s.add((error).multiply(error, mathContext), mathContext);
        }
        
        // sqrt(s / n) / a        
        return (new BigDecimal(Math.sqrt( s.divide(nn, mathContext).doubleValue()) )).divide(a, mathContext);
    }
    
    
    
    /**
     * Return list of valid nodes, according to the computed aggregation function.
     * E.g. Nodes with the value less or equal to zero are not eligible for the COUNT function (yield a negative or infinite estimate).
     * 
     * @param aggfunc Aggregation Function
     * 
     * @return Collection of eligible nodes to compute statistics
     */
    private Collection<Node> getValidNodes(NetStatistics.AggFunctions aggfunc){
        
        if(aggfunc == AggFunctions.COUNT){
            
            //Only consider nodes with valid values (different from zero),
            //belonging to the biggest partition
            Set<Node> validNodes = new HashSet<Node>();
            
            Collection<Node> nodesToCheck;
            
            //Check partition existence
            if(this.isPartitioned()){
                
                //Only nodes from the biggest partition are considered
                nodesToCheck = this.getPartitionNodes();
            } else {
                nodesToCheck = this.getNodes();
            }
                
            for (Node node : nodesToCheck) {
                if(node.getApplication().getValue().compareTo(BigDecimal.ZERO) > 0){
                    validNodes.add(node);
                }
            }
            
            return validNodes;
            
        } else {
            
            //Check partition existence
            if(this.isPartitioned()){
                
                //Only nodes from the biggest partition are considered
                return getPartitionNodes();
                
            } else {
                //All (active) nodes are valid
                return this.nodes.values();
            }
            
        }
        
    }
    
    
    public Collection<Node> getPartitionNodes(){
        
        Set<Node> validNodes = new HashSet<Node>();
        for(Integer nodeId : this.maxPartitionNodes){
            validNodes.add(this.nodes.get(nodeId));
        }
        
        return validNodes;
    }
    
    
    /**
     * Return number of nodes with valid estimations, according to the computed aggregation function.
     * E.g. Nodes with the value less or equal to zero are not eligible for the COUNT function (yield a negative or infinite estimate).
     * 
     * @param aggfunc Aggregation Function
     * 
     * @return Number of eligible nodes to compute statistics
     */
    public int numberOfValidNodesEstimations(NetStatistics.AggFunctions aggfunc){
        
        Collection<Node> nodesToCheck;
        
        //Check partition existence
        if(this.isPartitioned()){
            
            //Only nodes from the biggest partition are considered
            nodesToCheck = this.getPartitionNodes();
        } else {
            
            nodesToCheck = this.getNodes();
        }
        
        if(aggfunc == AggFunctions.COUNT){
            
            //Only consider nodes with valid values (different from zero)
            int num = 0;
            for (Node node : nodesToCheck) {
                if(node.getApplication().getValue().compareTo(BigDecimal.ZERO) > 0){
                    num++;
                }
            }
            
            return num;
            
        } else {
            
            //Check partition existence
            if(this.isPartitioned()){
                
                //Only nodes from the biggest partition are considered
                return this.maxPartitionSize;
                
            } else {
                
                //number of (active) nodes
                return this.numNodes;
            }
            
        }
        
    }
    
    
    /**
     * Return list of valid nodes values, according to the computed aggregation function.
     * E.g. Nodes with the value zero are not eligible for the COUNT function (yield an infinite estimate).
     * 
     * @param aggfunc Aggregation Function
     * @param mathContext Math Context
     * 
     * @return List of data values from all eligible nodes
     */
    public List<BigDecimal> getAllValidNodesValues(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        Collection<Node> validNodes = this.getValidNodes(aggfunc);
        List<BigDecimal> result = new ArrayList<BigDecimal>(validNodes.size());
        
        if(aggfunc == AggFunctions.COUNT) {
            BigDecimal numClouds = new BigDecimal(this.getNumClouds());
            for(Node n : validNodes){
                result.add(numClouds.divide(n.getApplication().getValue(), mathContext));
            }
        } else {
            for(Node n : validNodes){
                result.add(n.getApplication().getValue());
            }
        }
                
        return result;
    }
    
    
    
    //NEW RMSE
    public BigDecimal calculateRootMeanSquareError(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        
        Collection<Node> validNodes = getValidNodes(aggfunc);
        
        BigDecimal n = new BigDecimal(validNodes.size(), mathContext);
        
        //se = SUM_VALID( (v - v_i)^2 )
        BigDecimal se = NetStatistics.sumNodeSquaredErrors(validNodes, trueValue, mathContext);
        
        // sqrt(se/n)
        try{
            //return new BigDecimal(Math.sqrt((se.divide(n, mathContext)).doubleValue()), mathContext); //Sould NOT be used... Gottcha!
            return BigDecimal.valueOf(Math.sqrt((se.divide(n, mathContext)).doubleValue()));
        } catch (Exception ex) 
            {
                
                System.err.println("\nse="+se);
                System.err.println("n="+n);
                
                ex.printStackTrace();
                System.exit(0);
                return null;
            }
        

    }
    
    //NEW RMSE - trick to compare e instead of real count output (i.e. 1/e)
    public BigDecimal calculateRootMeanSquareError_Count(NetStatistics.AggFunctions aggfunc, BigDecimal trueCount, MathContext mathContext){
        
        Collection<Node> validNodes = getValidNodes(aggfunc);
        
        BigDecimal n = new BigDecimal(validNodes.size(), mathContext);
        
        //se = SUM_VALID( (v - v_i)^2 )
        BigDecimal se = NetStatistics.sumNodeSquaredErrors_Count(validNodes, trueCount, mathContext);
        
        // sqrt(se/n)
        try{
            return new BigDecimal(Math.sqrt( (se.divide(n, mathContext)).doubleValue() ), mathContext);
        } catch (Exception ex) 
            {
                
                System.err.println("\nse="+se);
                System.err.println("n="+n);
                
                ex.printStackTrace();
                System.exit(0);
                return null;
            }
        

    }
    
    
    //NEW MSE
    public BigDecimal calculateMeanSquareError(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        
        Collection<Node> validNodes = getValidNodes(aggfunc);
        
        BigDecimal n = new BigDecimal(validNodes.size(), mathContext);
        
        //se = SUM_VALID( (v - v_i)^2 )
        BigDecimal se = NetStatistics.sumNodeSquaredErrors(getValidNodes(aggfunc), trueValue, mathContext);
        
        // se/n
        return se.divide(n, mathContext);

    }
    
    //NEW Normalized RMSE - Fraction relatively to the correct (mean) value: RMSE/trueValue
    public BigDecimal calculateNormalizedRootMeanSquareError(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        BigDecimal rmse = calculateRootMeanSquareError(aggfunc, trueValue, mathContext);
        // RMSE/trueValue
        return rmse.divide(trueValue, mathContext);
    }
    
    //NEW Normalized RMSE - Fraction relatively to the correct (mean) value: RMSE/trueValue
    public BigDecimal calculateNormalizedRootMeanSquareError(BigDecimal rmse, BigDecimal trueValue, MathContext mathContext){
        // RMSE/trueValue
        return rmse.divide(trueValue, mathContext);
    }
    
    //NEW (Normalized for COUNT - Fraction relatively to the correct (mean) value: RMSE/trueValue)
    /** @deprecated **/
    public BigDecimal calculateRootMeanSquareError2(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        BigDecimal n = new BigDecimal(this.numNodes, mathContext);
        BigDecimal ss = NetStatistics.sumNodeSquaredValues(this.nodes.values(), mathContext);
        // sqrt(ss/n - trueValue * trueValue) / trueValue
        return (new BigDecimal(Math.sqrt( Math.abs(((ss.divide(n, mathContext)).subtract(trueValue.multiply(trueValue, mathContext), mathContext)).doubleValue()) ), mathContext)).divide(trueValue, mathContext);
    }
    
    //NEW Normalized MSE - Fraction relatively to the correct squared (mean) value: RMSE/trueValue^2
    public BigDecimal calculateNormalizedMeanSquareError(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        BigDecimal mse = calculateMeanSquareError(aggfunc, trueValue, mathContext);
        // MSE / (trueValue^2)
        BigDecimal squaredTrueValue = trueValue.multiply(trueValue, mathContext);
        return mse.divide(squaredTrueValue, mathContext);
    }
    
    //NEW (Normalized for COUNT - Fraction relatively to the correct squared (mean) value: RMSE/trueValue^2)
    /** @deprecated **/
    public BigDecimal calculateMeanSquareError2(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        BigDecimal n = new BigDecimal(this.numNodes, mathContext);
        BigDecimal ss = NetStatistics.sumNodeSquaredValues(this.nodes.values(), mathContext);
        // (ss/n - trueValue^2) / (trueValue^2)
        BigDecimal squaredTrueValue = trueValue.multiply(trueValue, mathContext);
        return ((ss.divide(n, mathContext)).subtract(squaredTrueValue, mathContext)).divide(squaredTrueValue, mathContext);
    }
    
    
    //NEW
    /**
     * @deprecated
     */
    
    public BigDecimal calculateCorrectValue(NetStatistics.AggFunctions aggfunc, MathContext mathContext){        
        return NetStatistics.calculateCorrectValue(aggfunc, this.nodes.values(), mathContext);
    }
    
    /** @deprecated **/
    public BigDecimal calculateRMSE(MathContext mathContext){
        BigDecimal nc = new BigDecimal(this.numClouds, mathContext);
        BigDecimal nn = new BigDecimal(this.numNodes, mathContext);
        BigDecimal s = BigDecimal.ZERO;
        
        for (Node n : this.nodes.values()) {
            
            //error = (x-nn)
            BigDecimal x = BigDecimal.ZERO;
            try {
                x = nc.divide(n.getApplication().getValue(), mathContext);
            } catch (ArithmeticException ae){
                //Do nothing (use default value: 0)
            }
            BigDecimal error = x.subtract(nn,mathContext);
            
            //s = s + (error)*(error)
            s = s.add(error.multiply(error, mathContext), mathContext);
        }
        
        //sqrt(s / n)        
        return new BigDecimal(Math.sqrt( s.divide(nn, mathContext).doubleValue() ), mathContext);
    }
    
    /** @deprecated **/
    public BigDecimal calculateMSE(MathContext mathContext){
        BigDecimal nc = new BigDecimal(this.numClouds, mathContext);
        BigDecimal nn = new BigDecimal(this.numNodes, mathContext);
        BigDecimal s = BigDecimal.ZERO;
        
        for (Node n : this.nodes.values()) {
            
            //error = (x-nn)
            BigDecimal x = BigDecimal.ZERO;
            try {
                x = nc.divide(n.getApplication().getValue(), mathContext);
            } catch (ArithmeticException ae){
                //Do nothing (use default value: 0)
            }
            BigDecimal error = x.subtract(nn,mathContext);
            
            //s = s + (error)*(error)
            s = s.add(error.multiply(error, mathContext), mathContext);        }
        
        //(s / n)        
        return s.divide(nn, mathContext);
    }
    
    
    public BigDecimal calculateMeanDegree(MathContext mathContext){

        double sum = 0;
        
        //Sum total of nodes links
        for (Node n : this.nodes.values()) {
            sum = sum + n.numberOfLinks();      
        }
        
        //Return mean node links      
        return new BigDecimal(sum, mathContext).divide(new BigDecimal(this.getNumNodes(), mathContext), mathContext);
    }
    
    public int calculateMinDegree(){

        int min = this.numNodes;
        
        //Sum total of nodes links
        for (Node n : this.nodes.values()) {
            if(min > n.numberOfLinks()){
                min = n.numberOfLinks();
            }
        }
        
        //Return Min node links      
        return min;
    }
    
    
    public int calculateMaxDegree(){

        int max = 0;
        
        //Sum total of nodes links
        for (Node n : this.nodes.values()) {
            if(max < n.numberOfLinks()){
                max = n.numberOfLinks();
            }
        }
        
        //Return Max node links      
        return max;
    }
    
    
    /**
     * Calculate the network diameter
     * @return diameter of the network
     */
    public int calculateDiameter(){
        
        int result = 0;
        
        //Compute eccentricity of all nodes
        Map<Integer, Integer> excMap = new HashMap<Integer, Integer>(this.numNodes);
        for(Node n : this.getNodes()){
            int exc = computeNodeEccentricity(n);
            excMap.put(n.getId(), exc);
        }
        
        //Calculate the diameter (max eccentricity)
        for(Integer e : excMap.values()){
            if(e > result){
                result = e;
            }
        }
        return result;
    }
    
    
    /**
     * Calculate the ratio of the number of nodes with an eccentricity equal to the network diameter.
     * @return ratio of peripheral nodes 
     */
    public double calculatePeripheralRatio(){
        
        int numNodesAtD = 0;
        for(Node n : this.getNodes()){
            if(n.getEccentricity() == this.getDiameter()){
                numNodesAtD++;
            }
        }
        
        return (double) numNodesAtD / this.getNumNodes();
    }
    
    
    /**
     * Compute node eccentricity
     * @param n input node
     * @return eccentricity of the node n
     */
    public int computeNodeEccentricity(Node n){
       
        //Compute the shortest path distance to all nodes
        Map<Integer, Double> shortPathDist = Dijkstra(n);
        
        //Compute max, i.e. eccentricity
        double max = 0;
        for(Double e : shortPathDist.values()){
            if((e != Double.MAX_VALUE) && (e > max)){
                max = e;
            }
        }
        
        return (int)max;
    }
    
    
    /**
     * Calculate and set the network diameter and the nodes eccentricity.
     */
    public void computeDiameterAndEccentricities(){
        
        int d = 0;
        
        //Compute eccentricity of all nodes
        Map<Integer, Integer> excMap = new HashMap<Integer, Integer>(this.numNodes);
        for(Node n : this.getNodes()){
            int ecc = computeNodeEccentricity(n);
            excMap.put(n.getId(), ecc);
            
            //Set node eccentricity
            n.setEccentricity(ecc);
        }
        
        //Calculate the diameter (max eccentricity)
        for(Integer e : excMap.values()){
            if(e > d){
                d = e;
            }
        }
        this.setDiameter(d);
        
    }
    
    
    private Map<Integer, Double> Dijkstra(Node start){
        
        //Initializations
        HashMap<Integer, Double> dist = new HashMap<Integer, Double>(this.numNodes);
        //previous -> Only needed to keep track of the shortest paths
//        HashMap<Integer, Integer> previous = new HashMap<Integer, Integer>(this.numNodes); 
        for(Integer nodeID  : this.getNodesKeySet()){
            dist.put(nodeID, Double.MAX_VALUE); // Unknown distance function from source to v
//            previous.put(nodeID, null); // Previous node in optimal path from source
        }
        
        dist.put(start.getId(), 0.0); // Distance from source to source
        Set<Integer> q = new HashSet<Integer>(this.getNodesKeySet()); // All nodes in the graph are unoptimized - thus are in Q
        
        // The main loop
        while(!q.isEmpty()){
            
            Double min = Double.MAX_VALUE;
            Integer u = null; //vertex in q with smallest distance in dist[]
            for(Integer id : q){
                if(dist.get(id) < min){
                    min = dist.get(id);
                    u = id;
                }
            }
            
            if(min == Double.MAX_VALUE){
                break; // all remaining vertices are inaccessible from source
            }
            
            q.remove(u);
            Set<Integer> unvisitedNeighbors = SetUtils.intersection(this.nodes.get(u).getLinks(), q);
            for(Integer v : unvisitedNeighbors){
                double alt = dist.get(u) + 1; // dist_between(u, v) always equal to one in this case
                if(alt < dist.get(v)){
                    dist.put(v, alt);
//                    previous.put(v, u);
                    //possible optimization here: decrease-key v in q (Reorder v in the Queue: see wikipedia)
                }
            }
        }
        
        return dist;
        
    }
    
    
    public int calculateNumberOfLinks(){
        
        return this.links.size();
    }
    
    
    public int calculateNumberOfActiveLinks(){
        
        int result = 0;
        
        for(Link l : this.links){
            if(l.getAppStatus() > 0){
                result++;
            }
        }
        
        return result;
    }
    

    
/*    
    public String createResultPlot(String appType, Integer simIndex, List<BigDecimal> devs) throws IOException {
        String filename = getReportPrefixName() + "_" + appType + "_" + simIndex + ".xy";
        
        int deltax = 1;
        
        FileOutputStream out = new FileOutputStream(filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
        
        int x = 0;
        for (BigDecimal d : devs) {

            p.println (x + " " + d);
            
            x = x + deltax;
        }
        
        p.close();
        
        out.close();
        
        return filename;
        
    }
 */    
    /**
     * @deprecated
     */
    public String createResultPlotGroup(String outputDir, List<String> files, int numSim) throws IOException {
        String filename = getReportPrefixName() + "_" + numSim + ".gp";

        FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
 
        p.println ("set logscale y");
        p.println ("set title 'SIMULATION RESULTS'");
        p.println ("set ylabel 'Root Mean Square Error'");
        p.println ("set xlabel 'Iterations'");
        p.print("plot ");
        int size = files.size();
        for(int i=0; i < size; i++){
            String f = files.get(i);
            if(i == (size-1)) {
                p.println("'"+ f + "' with lines");
            } else {
                p.print("'"+ f + "' with lines, ");
            }
        }
        
        p.close();
        
        out.close();
        
        return filename;
        
    }
    
    
    /** @deprecated **/
    public String createGenericResultPlot(String outputDir, String appType, Integer simIndex, String resultType, 
            List<BigDecimal> xValues, List<BigDecimal> yValues) throws IOException {
        
        String filename = getReportPrefixName() + "_" + appType + "_" + simIndex + "_" + resultType + ".xy";
        
        FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
        
        int size = xValues.size();
        for(int i=0; i < size; i++) {
            p.println (xValues.get(i) + " " + yValues.get(i));
        }
        
        p.close();
        
        out.close();
        
        return filename;
        
    }
    
    /** @deprecated **/
    public String createGenericResultPlotGroup(String outputDir, List<String> files, String resultType, int numSim,
                                               String title, String xlabel, String ylabel, 
                                               String graphType) 
      throws IOException {
        
        String filename = getReportPrefixName() + "_" + resultType + "_" +numSim + ".gp";

        FileOutputStream out = new FileOutputStream(outputDir + filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
 
        p.println ("set logscale y");
        p.println ("set title '"+title+"'");
        p.println ("set ylabel '"+ylabel+"'");
        p.println ("set xlabel '"+xlabel+"'");
        p.print("plot ");
        int size = files.size();
        for(int i=0; i < size; i++){
            String f = files.get(i);
            if(i == (size-1)) {
                p.println("'"+ f + "' with "+ graphType);
            } else {
                p.print("'"+ f + "' with "+graphType+", ");
            }
        }
        
        p.close();
        
        out.close();
        
        return filename;
        
    }
    
    
    
    public BigDecimal getMaxError(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        
        BigDecimal max = new BigDecimal(-Double.MAX_VALUE, mathContext);
        
        Collection<Node> nodesToCheck;
        if(this.isPartitioned()){
            nodesToCheck = this.getPartitionNodes();
        } else {
            nodesToCheck = this.getNodes();
        }
        
        for (Node n : nodesToCheck) {
            BigDecimal dif = (n.getApplication().getValue()).subtract(trueValue, mathContext).abs(mathContext);
            if(max.compareTo(dif) < 0){
                max = dif;
            }
        }
        return max.divide(trueValue, mathContext);
    }
    
    
    public BigDecimal getMinError(NetStatistics.AggFunctions aggfunc, BigDecimal trueValue, MathContext mathContext){
        
        BigDecimal min = new BigDecimal(Double.MAX_VALUE, mathContext);
        
        Collection<Node> nodesToCheck;
        if(this.isPartitioned()){
            nodesToCheck = this.getPartitionNodes();
        } else {
            nodesToCheck = this.getNodes();
        }
        
        for (Node n : nodesToCheck) {
            BigDecimal dif = (n.getApplication().getValue()).subtract(trueValue, mathContext).abs(mathContext);
            if(min.compareTo(dif) > 0){
                min = dif;
            }
        }
        return min.divide(trueValue, mathContext);
    }

    
    /**
     * Get Network MAX aggregated value
     * @return
     */
    private BigDecimal getMaxValue(MathContext mathContext){
        BigDecimal max = new BigDecimal(-Double.MAX_VALUE, mathContext);
        
        Collection<Node> nodesToCheck;
        if(this.isPartitioned()){
            nodesToCheck = this.getPartitionNodes();
        } else {
            nodesToCheck = this.getNodes();
        }
        
        for (Node n : nodesToCheck) {
            if(max.compareTo(n.getApplication().getValue()) < 0){
                max = n.getApplication().getValue();
            }
        }
        return max;
    }
    
    
    /**
     * Get Network MAX aggregated value
     * @return
     */
    private BigDecimal getMaxValue(Collection<Node> nodes, MathContext mathContext){
        BigDecimal max = new BigDecimal(-Double.MAX_VALUE, mathContext);
        for (Node n : nodes) {
            if(max.compareTo(n.getApplication().getValue()) < 0){
                max = n.getApplication().getValue();
            }
        }
        return max;
    }
    
    
    /**
     * Get Network MIN aggregated value
     * @return
     */
    private BigDecimal getMinValue(MathContext mathContext){
        BigDecimal min = new BigDecimal(Double.MAX_VALUE, mathContext);
        
        Collection<Node> nodesToCheck;
        if(this.isPartitioned()){
            nodesToCheck = this.getPartitionNodes();
        } else {
            nodesToCheck = this.getNodes();
        }
        for (Node n : nodesToCheck) {
            if(min.compareTo(n.getApplication().getValue()) > 0){
                min = n.getApplication().getValue();
            }
        }
        return min;
    }
    
    /**
     * Get Network MIN aggregated value
     * @return
     */
    private BigDecimal getMinValue(Collection<Node> nodes, MathContext mathContext){
        BigDecimal min = new BigDecimal(Double.MAX_VALUE, mathContext);
        for (Node n : nodes) {
            if(min.compareTo(n.getApplication().getValue()) > 0){
                min = n.getApplication().getValue();
            }
        }
        return min;
    }
    
    
    //NEW
    /**
     * Get Network MAX aggregated value
     * @return
     */
    public BigDecimal getMaxValue(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        return getMaxValue(mathContext);
/*        
        switch(aggfunc) {
            case AVG    : return getMaxValue(mathContext);
                          
            case COUNT  : Collection<Node> validNodes = getValidNodes(aggfunc);
                          BigDecimal min = getMinValue(validNodes, mathContext);
                          BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                          return numClouds.divide(min, mathContext);
                          
            default : 
                return getMaxValue(mathContext);
        }
*/
    }
    
    //NEW
    /**
     * Get Network MIN aggregated value
     * @return
     */
    public BigDecimal getMinValue(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        return getMinValue(mathContext);
/*        
        switch(aggfunc) {
            case AVG    : return getMinValue(mathContext);
                          
            case COUNT  : Collection<Node> validNodes = getValidNodes(aggfunc);
                          BigDecimal max = getMaxValue(validNodes, mathContext);
                          BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                          return numClouds.divide(max, mathContext);            

            default : 
                return getMinValue(mathContext);
        }
*/
    }
    
    
    /**
     * Get Network Mean aggregated value
     * @deprecated
     * @return
     */
    public BigDecimal getMeanValue(MathContext mathContext){
        
        BigDecimal sum = BigDecimal.ZERO;
        
        // Sum total of nodes values
        for (Node n : this.nodes.values()) {
            sum = sum.add(n.getApplication().getValue(), mathContext);
        }
       
        //Return mean aggregated value      
        return sum.divide(new BigDecimal(this.getNumNodes(), mathContext), mathContext);

    }
    
    
    /**
     * Get Network Mean aggregated value
     * @return
     */
    public BigDecimal getMeanValue(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        BigDecimal sum = BigDecimal.ZERO;
        Collection<Node> validNodes = getValidNodes(aggfunc);
        
        // Sum total of nodes values
        for (Node vn : validNodes) {
            sum = sum.add(vn.getApplication().getValue(), mathContext);
        }
        
        return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);

        /*
        //Return mean aggregated value
        switch(aggfunc) {
            case AVG    : return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
                          
            case COUNT  : BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                          return numClouds.divide(sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext), mathContext);             
            default : 
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
        }
        */
        
    }
    
    public BigDecimal getMeanValue_A(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        BigDecimal sum = BigDecimal.ZERO;
        Collection<Node> validNodes = getValidNodes(aggfunc);
        
        switch(aggfunc) {
            case AVG    : 
                // Sum total of nodes values
                for (Node vn : validNodes) {
                    sum = sum.add(vn.getApplication().getValue(), mathContext);
                }
                
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
                                          
            case COUNT  : 
                BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                // Sum total of nodes values
                for (Node vn : validNodes) {
                    BigDecimal e = numClouds.divide(vn.getApplication().getValue(), mathContext);
                    sum = sum.add(e, mathContext);
                }
                
                return numClouds.divide(sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext), mathContext);
                
            default : 
                // Sum total of nodes values
                for (Node vn : validNodes) {
                    sum = sum.add(vn.getApplication().getValue(), mathContext);
                }
                
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
        }
        
        

        /*
        //Return mean aggregated value
        switch(aggfunc) {
            case AVG    : return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
                          
            case COUNT  : BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                          return numClouds.divide(sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext), mathContext);             
            default : 
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
        }
        */
        
    }
    
    
    public BigDecimal getMeanValue_AA(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        BigDecimal sum = BigDecimal.ZERO;
        Collection<Node> validNodes = getValidNodes(aggfunc);
//        Collection<Node> validNodes = this.nodes.values();
        
        switch(aggfunc) {
            case AVG    : 
                // Sum total of nodes values
                for (Node vn : validNodes) {
//                    sum = sum.add(vn.getApplication().getValue(), mathContext);
                    sum = sum.add(vn.getApplication().getBaseValue(), mathContext);
                }
                
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
                                          
            case COUNT  : 
                BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                // Sum total of nodes values
                for (Node vn : validNodes) {
//                    BigDecimal e = numClouds.divide(vn.getApplication().getValue(), mathContext);
//                    sum = sum.add(e, mathContext);
                    sum = sum.add(vn.getApplication().getBaseValue(), mathContext);
                }
                
//                return numClouds.divide(sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext), mathContext);
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
                
            default : 
                // Sum total of nodes values
                for (Node vn : validNodes) {
                    sum = sum.add(vn.getApplication().getValue(), mathContext);
                }
                
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
        }
        
    }
    
    
    /**
     * Get Network Mean initial value (true value), always consider all nodes
     * @return
     */
    public BigDecimal getMeanInitValue(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }  
        
        if(aggfunc == AggFunctions.COUNT) {
            return new BigDecimal(validNodes.size());
        } else {
            
            BigDecimal sum = BigDecimal.ZERO;
            
            // Sum total of nodes values
            for (Node n : validNodes) {
                sum = sum.add(n.getApplication().getInitValue(), mathContext);
            }
            
            return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
            
        }
        
        
        
/*        
        //Return mean aggregated value
        switch(aggfunc) {
            case AVG    : return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);
                          
            case COUNT  : 
                          BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                          return numClouds.divide(sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext), mathContext);         
            default : 
                return sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext);

        }
*/

    }
    
    
    /**
     * Get the sum of all aggregated values
     * @return
     */
    public BigDecimal getValuesSum(NetStatistics.AggFunctions aggfunc, MathContext mathContext){
        
        BigDecimal sum = BigDecimal.ZERO;
        Collection<Node> validNodes = getValidNodes(aggfunc);
        
        // Sum total of nodes values
        for (Node vn : validNodes) {
            sum = sum.add(vn.getApplication().getValue(), mathContext);
        }
        
        return sum;
/*        
        //Return mean aggregated value
        switch(aggfunc) {
            case AVG    : return sum;
                          
            case COUNT  : BigDecimal numClouds = new BigDecimal(this.getNumClouds());
                          return numClouds.divide(sum.divide(new BigDecimal(validNodes.size(), mathContext), mathContext), mathContext);             
            default : 
                return sum;
        }
*/
    }
    
    
    public long getTotalMessageSend(){
    	long result = 0;
        for (Node n : this.nodes.values()) {
                result += n.getMsgSendCount();
        }
        for (Node n : this.deadNodes.values()) {    
            result += n.getMsgSendCount();
        }
        return result;
    }
    
    
    public long getTotalMessageRcv(){
    	long result = 0;
        for (Node n : this.nodes.values()) {
                result += n.getMsgRcvCount();
        }
        for (Node n : this.deadNodes.values()) {
            result += n.getMsgRcvCount();
    }
        return result;
    }
    
/*    
    public long getTotalMessageDiscardSend(){
        long result = 0;
        for (Node n : this.nodes.values()) {
                result += n.getMsgDiscardSendCount();
        }
        for (Node n : this.deadNodes.values()) {
            result += n.getMsgDiscardSendCount();
        }
        return result;
    }
    
/*    
    public long getTotalMessageDiscardRcv(){
        long result = 0;
        for (Node n : this.nodes.values()) {
                result += n.getMsgDiscardRcvCount();
        }
        for (Node n : this.deadNodes.values()) {
            result += n.getMsgDiscardRcvCount();
    }
        return result;
    }
*/    
    
    public long getTotalMessageLoss(){
        long result = 0;
        for (Node n : this.nodes.values()) {
                result += n.getMsgLossCount();
        }
        for (Node n : this.deadNodes.values()) {
            result += n.getMsgLossCount();
        }
        return result;
    }

    
    
    public static String saveToFile(Network net, String outputDir, String sufix) throws FileNotFoundException, IOException {
        
        String fileName = net.getReportPrefixName() + sufix + ".net";
        
        //Serialize the original class object
        FileOutputStream fo = new FileOutputStream(outputDir + fileName);
        ObjectOutputStream so = new ObjectOutputStream(fo);
        so.writeObject(net);
        so.flush();
        so.close();
        
        return fileName;
    }


    public static Network loadFromFile(String outputDir, String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {
        
        //Deserialize in to new class object
        FileInputStream fi = new FileInputStream(outputDir + fileName);
        ObjectInputStream si = new ObjectInputStream(fi);  
        Network net = (Network) si.readObject();
        si.close();
        
        return net;
    }
    
    
/*    
    public String createMsgCountPlot(String appType, Integer simIndex, List<BigDecimal> numMsgs, String type) throws IOException {
        String filename = getReportPrefixName() + "_" + appType + "_" + type + simIndex + ".xy";
        
        
        FileOutputStream out = new FileOutputStream(filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
        
        int i = 0;
        for (BigDecimal l : numMsgs) {

            p.println (i + " " + l);
            i++;
        }
        
        p.close();
        
        out.close();
        
        return filename;
        
    }
    
    
    public String createMsgCountPlotGroup(List<String> files, int numSim, String type) throws IOException {
        String filename = getReportPrefixName() + "_" + type + numSim + ".gp";

        FileOutputStream out = new FileOutputStream(filename); // declare a file output object
        PrintStream p = new PrintStream(out); // declare a print stream object
 
        p.println ("set logscale y");
        p.println ("set title 'TOTAL MESSAGES COUNT - "+ type +"'");
        p.println ("set ylabel 'Total number of messages'");
        p.println ("set xlabel 'Iterations'");
        p.print("plot ");
        int size = files.size();
        for(int i=0; i < size; i++){
            String f = files.get(i);
            if(i == (size-1)) {
                p.println("'"+ f + "' with lines");
            } else {
                p.print("'"+ f + "' with lines, ");
            }
        }
        
        p.close();
        
        out.close();
        
        return filename;
        
    }
*/    

    public String getReportPrefixName(){
        return this.type + "-" + this.initNumNodes;
    }
    
    
    public Node selectRandomNode() {
        List <Node> randNodes = new ArrayList<Node>(this.nodes.values());
        Collections.shuffle(randNodes);
        Node result = randNodes.get(0);
        return result;
    }
    
    public List<Node> selectRandomNodes(int num) {
        List <Node> randNodes = new ArrayList<Node>(this.nodes.values());
        Collections.shuffle(randNodes);
        List <Node> result = randNodes.subList(0, num);
        return result;
    }
    
    
    public void createOverlay(Config config) 
      throws NetworkException {
        
        String overlay = config.getValue(Config.PARAM_NETWORK_OVERLAY);
        NetworkOverlay netOverlay = NetworkOverlay.valueOf(overlay);
        switch (netOverlay) {
            case TREE : 
                createTreeOverlay();
                break;

            default:
                throw new NetworkException("Unknown Network Overlay");
        }
    }
    
    
    public void createTreeOverlay(){
        List<Integer> rndNodesList = SetUtils.randomizeSet(this.nodes.keySet());
        
        //Pick a random node as a root
        Integer rootId = rndNodesList.get(0);
        
        //Init visited set, add root
        Set<Integer> visited = new HashSet<Integer>();
        visited.add(rootId);
        
        //Init visiting set, add all root neighbors
        Set<Integer> visiting = new HashSet<Integer>(this.getNodeNeighborIds(rootId));
        
        //Init toVisit set
        Set<Integer> toVisit = new HashSet<Integer>();
        
        //Visit all nodes to create the tree overlay (remove loops)
        do {
            
            //Check loops between visiting nodes
            for(Integer parentId : visiting){
                
                Set<Integer> neighboors = this.getNodeNeighborIds(parentId);
                neighboors.remove(parentId);
                for(Integer childId : neighboors){
                    if(visiting.contains(childId)){
                        //Loop found, deactivate link
                        this.getLink(parentId, childId).decStatus();
                        this.getNode(parentId).delOverlayLink(childId);
                    
                    } else if(!visited.contains(childId)) {
                        //Set child to be visited (if not already)
                        if(!toVisit.add(childId)){
                            //Loop found (child already set by another visiting node), deactivate link
                            Link l = this.getLink(parentId, childId);
                            l.decStatus();
                            this.getNode(parentId).delOverlayLink(childId);
                            l.decStatus();
                            this.getNode(childId).delOverlayLink(parentId);
                        }
                    }
                }
            }
            
            
            //Visit next nodes
            visited.clear();
            visited.addAll(visiting);
            visiting.clear();
            visiting.addAll(toVisit);
            toVisit.clear();
            
        } while (!visiting.isEmpty());
    }
    
    
    public HashSet<Node> getNodeNeighbors(int id){
        
        Node target = this.getNode(id);
        Set<Integer> listOfNeighbors = target.getLinks();
        
        HashSet<Node> result = new HashSet<Node>(listOfNeighbors.size());
        for(Integer nei : listOfNeighbors){
            result.add(this.getNode(nei));
        }
        
        return result;
        
    }
    
    
    public Set<Integer> getNodeNeighborIds(int id){
        
        Node target = this.getNode(id);
        return target.getLinks();
        
    }
    
    
    public List<BigDecimal> getAllNodesValues(AxisValues values, MathContext mc){
        
        List<BigDecimal> result = new ArrayList<BigDecimal>(this.numNodes);
        
        switch (values) {
            case XPos:
                for(Node n : this.getNodes()){
                    result.add(new BigDecimal(n.getX(), mc));
                }
                break;
            case YPos:
                for(Node n : this.getNodes()){
                    result.add(new BigDecimal(n.getY(), mc));
                }
                break;
            case DataValue:
                for(Node n : this.getNodes()){
                    result.add(n.getApplication().getValue());
                }
                break;   
            case InitDataValue:
                for(Node n : this.getNodes()){
                    result.add(n.getDataValue());
                }
                break;
            default:
                System.err.println("DEBUG: Unknown Node Value Type ["+values+"]");
                break;
        }        
        
        return result;
    }
    
    
    private DataDistribution getRealDataDistribution(Collection<Node> nodes, Collection<BigDecimal> labels, MathContext mathContext){
        
        //initialize temporary data: array of labels and array with corresponding count
        BigDecimal[] l = labels.toArray(new BigDecimal[0]);
        long[] c = new long[l.length];
        for(int i = 0; i < c.length; i++) {
            c[i] = 0; //init counts to zero
        }
        
        // Counts of each label
        for (Node n : nodes) {
            //For all node initial data value
            BigDecimal v = n.getDataValue();
            for(int i = 0; i < l.length; i++){
                //If data value <= than label then increase corresponding count
                if(v.compareTo(l[i]) <= 0){
                    c[i] = c[i] + 1;
                }
            }
        }
        
        //Create resulting DataDistribution representations
        DataDistribution result = new DataDistribution(DD_TYPE.CDF, labels);
        for(int i = 0; i < c.length; i++) {
            //Add fraction (count/total number) for each label
            result.addEntry(l[i], new BigDecimal(((double)c[i]/nodes.size()), mathContext));
        }
        
        return result; 
        
    }
    
    
    public DataDistribution getRealDataDistribution(MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        //Create DataDistribution representations
        DataDistribution result = new DataDistribution(DD_TYPE.CDF);
        
        // Add value to DD and update it accordingly
        for (Node n : validNodes) {
            //For all node initial data value
            BigDecimal v = n.getDataValue();
            result.addCDFValue(v, mathContext);
        }
        
        //Convert frequencies to fractions
        result.convertFrequencies2Fractions(new BigDecimal(validNodes.size()), mathContext);
        
        return result; 
        
    }
    
    public BigDecimal calculateKS_Max(DataDistribution realDD, MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal max_ks = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            
            BigDecimal ks = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal realLabel = realDD.getSmallerClosestLabel(l);
                BigDecimal realValue = realDD.getValue(realLabel);
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realValue, mathContext)).abs();
                if(dif.compareTo(ks) > 0){
                    ks = dif;
                }
            }
            
            if(ks.compareTo(max_ks) > 0){
                max_ks = ks;
            }
        }
        
        return max_ks;
        
    }
    
    
    public BigDecimal calculateKS_Avg(DataDistribution realDD, MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal sum_ks = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            
            BigDecimal ks = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal realLabel = realDD.getSmallerClosestLabel(l);
                BigDecimal realValue = realDD.getValue(realLabel);
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realValue, mathContext)).abs();
                if(dif.compareTo(ks) > 0){
                    ks = dif;
                }
            }
            
            sum_ks = sum_ks.add(ks, mathContext);
        }
        
        return sum_ks.divide(new BigDecimal(validNodes.size()), mathContext);
        
    }
    
    
    public BigDecimal calculateKS_AtNode(DataDistribution realDD, DataDistribution nodeDD, MathContext mathContext){
                    
        BigDecimal ks = BigDecimal.ZERO;
        for(BigDecimal l : nodeDD.getLabels()){
            BigDecimal realLabel = realDD.getSmallerClosestLabel(l);
            BigDecimal realValue = realDD.getValue(realLabel);
            BigDecimal dif = ((nodeDD.getValue(l)).subtract(realValue, mathContext)).abs();
            if(dif.compareTo(ks) > 0){
                ks = dif;
            }
        }
                
        return ks;
    }
    
    
    public BigDecimal calculateDE_Max(DataDistribution realDD, MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal max_de = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            
            BigDecimal de = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal realLabel = realDD.getSmallerClosestLabel(l);
                BigDecimal realValue = realDD.getValue(realLabel);
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realValue, mathContext)).abs();
                de = de.add(dif, mathContext);
            }
            
            if(de.compareTo(max_de) > 0){
                max_de = de;
            }
        }
        
        return max_de;
        
    }
    
    
    public BigDecimal calculateDE_Avg(DataDistribution realDD, MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal sum_de = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            
            BigDecimal de = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal realLabel = realDD.getSmallerClosestLabel(l);
                BigDecimal realValue = realDD.getValue(realLabel);
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realValue, mathContext)).abs();
                de = de.add(dif, mathContext);
            }
            
            sum_de = sum_de.add(de, mathContext);
        }
        
        return sum_de.divide(new BigDecimal(validNodes.size()), mathContext);
        
    }
    
    
    public BigDecimal calculateKS_Max(MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal max_ks = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            DataDistribution realDD = this.getRealDataDistribution(validNodes, nodeDD.getLabels(), mathContext);
            
            BigDecimal ks = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realDD.getValue(l), mathContext)).abs();
                if(dif.compareTo(ks) > 0){
                    ks = dif;
                }
            }
            
            if(ks.compareTo(max_ks) > 0){
                max_ks = ks;
            }
        }
        
        return max_ks;
        
    }
    
    
    public BigDecimal calculateKS_Avg(MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal sum_ks = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            DataDistribution realDD = this.getRealDataDistribution(validNodes, nodeDD.getLabels(), mathContext);
            
            BigDecimal ks = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realDD.getValue(l), mathContext)).abs();
                if(dif.compareTo(ks) > 0){
                    ks = dif;
                }
            }
            
            sum_ks = sum_ks.add(ks, mathContext);
        }
        
        return sum_ks.divide(new BigDecimal(validNodes.size()), mathContext);
        
    }
    
    
    
    public BigDecimal calculateDE_Max(MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal max_de = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            DataDistribution realDD = this.getRealDataDistribution(validNodes, nodeDD.getLabels(), mathContext);
            
            BigDecimal de = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realDD.getValue(l), mathContext)).abs();
                de = de.add(dif, mathContext);
            }
            
            if(de.compareTo(max_de) > 0){
                max_de = de;
            }
        }
        
        return max_de;
        
    }
    
    
    public BigDecimal calculateDE_Avg(MathContext mathContext){
        
        //Get list of all valid nodes (considering partitioning)
        Collection<Node> validNodes;
        if(this.isPartitioned()){
            validNodes = this.getPartitionNodes();
        } else {
            validNodes = this.getNodes();
        }
        
        BigDecimal sum_de = BigDecimal.ZERO;
        for(Node n : validNodes){
            DataDistribution nodeDD = n.getApplication().getDDValue();
            DataDistribution realDD = this.getRealDataDistribution(validNodes, nodeDD.getLabels(), mathContext);
            
            BigDecimal de = BigDecimal.ZERO;
            for(BigDecimal l : nodeDD.getLabels()){
                BigDecimal dif = ((nodeDD.getValue(l)).subtract(realDD.getValue(l), mathContext)).abs();
                de = de.add(dif, mathContext);
            }
            
            sum_de = sum_de.add(de, mathContext);
        }
        
        return sum_de.divide(new BigDecimal(validNodes.size()), mathContext);
        
    }
    
    
    
    /**
     * Dynamics handling...
     */
    
    
    /**
     * Simulate nodes leaving the network.
     * 
     * @param num - number of nodes leaving
     * @param ce - communication engine
     * @throws ApplicationException 
     */
    public int nodesLeaving(boolean countIsolated, int num, ComEngine ce, Config conf, Integer simulationIndex, Integer repetitionCount) throws ApplicationException{
        
        //Randomize nodes list
        Set<Integer> nodes = this.getNodesKeySet();
        List<Integer> rAllNodes = SetUtils.randomizeSet(nodes);
        
        //Avoid cloud nodes from been removed
        if(ce.getAggFunction() == AggFunctions.COUNT){

            //remove clouds from randomized list
            for(Integer cloudId : this.getCloudNodes()){
                rAllNodes.remove(cloudId);
            }
            
        }
        
        //Select leaving nodes
        List<Integer> rNodes = new ArrayList<Integer>(rAllNodes);
        
        //Remove nodes links
        Set<Integer> additionalNodes = new HashSet<Integer>();
        
        //Skipped nodes
        Set<Integer> skippedNodes = new HashSet<Integer>();
        
        int count = 0;
        int total = num;
        for(int i=0;  i < total; i++){
            
            Integer nodeId = rNodes.get(i);
            Set<Integer> neighboors  = new HashSet<Integer>(this.getNode(nodeId).getLinks());
            
            //Check if the node disconnection will also disconnect a cloud node from the network 
            if((ce.getAggFunction() == AggFunctions.COUNT) && isolateCloud(neighboors)){
                
                //Skip node to avoid cloud disconnection
                skippedNodes.add(nodeId);
                total++;
                
            } else if(additionalNodes.contains(nodeId)) {
                
                //Skip nodes, already in additional leaving nodes
                total++;                
                
            } else {
                
                //Otherwise proceed
                for(Integer nID : neighboors){
                    Node neighbor = this.getNode(nID);
                    neighbor.delLink(nodeId);
                    neighbor.delOverlayLink(nodeId);
                    neighbor.addLeavingNode(nodeId);
                    
                    if(!neighbor.isConnected()){
                        
                        //Neighboor left disconnected, will also be removed
                        additionalNodes.add(nID);
                        
                        //TODO Sometimes additional nodes may exceed churn number, skip in this case
                        if(countIsolated && total > (i+1)){
                            total--;
                        }
                    }
                }
                
                this.getNode(nodeId).clearAllLinks();
                count++;
                
            }
            
        }
        
        
        //Detect disconnected cloud nodes
/*        Set<Integer> disconnectedClouds = new HashSet<Integer>();
        if(ce.getAggFunction() == AggFunctions.COUNT){

            for(Integer cloudId : this.getCloudNodes()){
                
                if(additionalNodes.remove(cloudId)){
                    disconnectedClouds.add(cloudId);
                }
            }
        }
*/
        
        //Update list of leaving nodes
        Set<Integer> leavingNodes = new HashSet<Integer>(rNodes.subList(0, total));
        
        //Remove skipped nodes
        for(Integer skippedNode : skippedNodes){
            leavingNodes.remove(skippedNode);
        }
        
        //Add additional disconnected nodes to leaving nodes
        leavingNodes.addAll(additionalNodes);
        
        //Remove Nodes from active map and add to dead map
        for(Integer nodeId : leavingNodes){
            this.deadNodes.put(nodeId, this.getNode(nodeId));
            this.numDeadNodes++;
            this.nodes.remove(nodeId);
            this.numNodes--;
        }
        
        //Remove all nodes links
        this.removeNodeLinks(rNodes);
        
/*        
        //Re-connect and Initialize disconnected cloud nodes
        for(Integer cloudId : disconnectedClouds){
            System.err.println("RECONNECT CLOUD!!!");
            this.reconnectNode(cloudId);
            this.getNode(cloudId).getApplication().init(conf, simulationIndex.toString(), repetitionCount.toString());
        }
        
        //Invoke 2nd application initialization (can only be done after all have executed the 1st initialization) 
        for(Integer cloudId : disconnectedClouds){
            
            this.getNode(cloudId).getApplication().init2();
        }
*/
        
        return leavingNodes.size();
        
        //NOTE: Nothing done to buffered messages (considered lost, but not counted)
        
    }
    
    
    
    /**
     * Simulate nodes arriving to the network
     * @param num
     * @throws InstantiationException 
     * @throws IllegalAccessException 
     * @throws ClassNotFoundException 
     * @throws ApplicationException 
     */
    public int nodesArriving(int num, SpatialDataDistribution dd, ComEngine ce, Config conf, Integer simulationIndex, Integer repetitionCount) 
        throws ApplicationException{
        
        Set<Integer> arrivingNodes = new HashSet<Integer>(num);
        
        Random randPos = new Random();
        for(int i=0, nextID = this.totalNumNodes; i < num; i++, nextID++){
            
            //Create nodes at a random position
            double x = randPos.nextDouble();
            double y = randPos.nextDouble();
            this.addNode(new Integer(nextID), new Node(nextID, x, y));
            this.numNodes++;
            
            //Connect new node to the network (establish links)
            this.connectNode(nextID);
            
            //Set node data value according to data distribution
            dd.setDataDistribution(this.getNode(nextID));
            
            //Add new node to arriving list (to be initialized later)
            arrivingNodes.add(nextID);
            
        }
        
        
        //Initialize application (should only be done after all links are created)
        for(Integer newNode : arrivingNodes){
            
            //Init node application
            Application app;
            try{
                app = Application.createInstance(ce.getAppType(), this.getNode(newNode), ce);
            } catch (Exception e) {
                throw (ApplicationException) new ApplicationException(e).initCause(e);
            }
            app.init(conf, simulationIndex.toString(), repetitionCount.toString());
            //app.init2();
            this.getNode(newNode).init(app);
            
        }
        
        
        //Invoke 2nd application initialization (can only be done after all have executed the 1st initialization) 
        for(Integer newNode : arrivingNodes){
            
            this.getNode(newNode).getApplication().init2();
            
            //Schedule initial clock tick event for node to start processing
            ce.addApplicationEvent(0, String.valueOf(newNode), EventType.TICK, null);
        }
        
        
        //Set new total number of nodes created
        this.totalNumNodes = this.totalNumNodes + num;
        
        return num;
        
    }
    
    
    /**
     * Change (init) values of nodes, simulate node values dinamism
     * 
     * @param numNodes Number of nodes that change value
     * @param rate value change to apply
     * @param operator value change operation to apply
     * @param mc MathContext definition
     * 
     * @return number of nodes that had their value changed
     */
    public int changeNodeValues(int numNodes, double rate, String operator, MathContext mc){
        
        //Get list of nodes to change value
        List<Integer> rAllNodes;
        if(numNodes == this.getNumNodes()){
            rAllNodes = new ArrayList<Integer>(this.getNodesKeySet());
        } else {
            //TODO Randomize nodes list?
//            Set<Integer> nodes = this.getNodesKeySet();
//            rAllNodes = SetUtils.randomizeSet(nodes);
            rAllNodes = new ArrayList<Integer>(this.getNodesKeySet());
            rAllNodes = rAllNodes.subList(0, numNodes);
        }
        
        int result = 0;
        
        //Apply value change
        for(Integer nodeId : rAllNodes){
            Node node = this.getNode(nodeId);
            
            BigDecimal dataValue = node.getDataValue();
            
            if(operator.equals(Dynamics.VC_OP_MULTI)) {
                //Multiply current value by rate (i.e. augment/decrease by a specified ratio)
                dataValue = dataValue.multiply(new BigDecimal(rate), mc);
                node.setDataValue(dataValue);
                node.getApplication().setInitValue(dataValue);
                result++;
            } else if (operator.equals(Dynamics.VC_OP_ADD)){
                //Add to current value by rate (i.e. augment/decrease by an absolute value)
                dataValue = dataValue.add(new BigDecimal(rate), mc);
                node.setDataValue(dataValue);
                node.getApplication().setInitValue(dataValue);
                result++;
            }
        }
        
        return result;
        
    }
    
    
    
    /**
     * Connect node to the network (establish links).
     * Specific for each network type.
     */
    protected abstract void connectNode(Integer id);
    
    /**
     * Re-connect node to the network (establish link).
     * Specific for each network type.
     * NOTE: Use avoid special nodes from been disconnected (e.g. clouds)
     */
    protected abstract void reconnectNode(Integer id);
    
    
    /**
     * Remove links containing a node from the list given.
     * 
     * @param lNodes - node's links to be removed
     */
    private void removeNodeLinks(Collection<Integer> lNodes){
        ListIterator<Link> lit = this.links.listIterator();
        while(lit.hasNext()){
            Link l = lit.next();
            if(l.contains(lNodes)){
                lit.remove();
            }
        }
    }
    
    
    /**
     * Detect if the remotion of the provided list of neighbors will isolate (disconnect) a cloud node.
     * 
     * @param neighbors set of neighbors to check;
     * 
     * @return true if a cloud node is part of the neighbors list and it only has one link (keeping the node connected), false otherwise.
     */
    private boolean isolateCloud(Set<Integer> neighbors){
        for(Integer cloudId : this.getCloudNodes()){
            if(neighbors.contains(cloudId) && this.getNode(cloudId).numberOfLinks() < 2) {
                return true;   
            }
        }
        
        return false;
    }
    
    
    
    public void checkPartitioning(NetStatistics.AggFunctions aggfunc) throws NetworkException{
        
        List<Integer> netNodes = new ArrayList<Integer>(this.getNodesKeySet());
        int netSize = netNodes.size();
        
        int cloudPartitionIndex = -1;
        int maxIndex = -1;
        int maxSize = 0;
        Set<Integer> maxNodes = new HashSet<Integer>();
        this.clearPartitionsData();
        
        while(!netNodes.isEmpty()){
            Integer currentNode = netNodes.get(0);
            Set<Integer> partitionNodes = new HashSet<Integer>();
            getPartition(currentNode, netNodes, partitionNodes);
            int partitionSize = partitionNodes.size();
            
            if(partitionSize == netSize){
                
                //Network NOT partitioned
                
            } else if(partitionSize < netSize){
                
                this.addPartition(partitionSize);
                if(partitionSize > maxSize){
                    maxSize = partitionSize;
                    maxIndex = this.getNumberOfPartitions() - 1;
                    maxNodes = new HashSet<Integer>(partitionNodes);
                }
                
                //Network partitioned
                System.out.println("\t\tPARTITION DETECTED - Size: "+partitionSize);
                
                if(aggfunc == AggFunctions.COUNT){
                    for(Integer cloudId : this.getCloudNodes()){
                        if(partitionNodes.contains(cloudId)){
                            cloudPartitionIndex = this.getNumberOfPartitions() - 1;
                            System.out.println("\t\t\tContains Cloud: "+cloudId);
                        }
                    }
                }
            
            } else {
                
                //ERROR
                throw new NetworkException("Invalid partion size (cannot be greater than network)!");
            }
        }
        
//        System.out.println("\t\t"+partitions.size()+" PARTITION DETECTED!");
//        for(int i=0; i < partitions.size(); i++){
//            if(i==cloudPartitionIndex){
//                System.out.print("\t\t\tsize: "+partitions.get(i)+"(contains cloud)");
//            } else {
//                System.out.print("\t\t\tsize: "+partitions.get(i));
//            }
//        }
        
        
        if((aggfunc == AggFunctions.COUNT) && (cloudPartitionIndex != maxIndex)){
            throw new NetworkException("INVALID STATE: CLOUD NOT IN BIGGER PARTITION!", false);
        }
        
        this.setMaxPartitionSize(maxSize);
        this.setMaxPartitionNodes(maxNodes);
        
    }
    
    
    
    /**
     * Check if the network is connected (assumed undirected graph), i.e. not partitioned.
     * 
     * @return Return true if the network is connected and false if it is partitioned;
     */
    public boolean isConnected() {
        
        List<Integer> netNodes = new ArrayList<Integer>(this.getNodesKeySet());
        int netSize = netNodes.size();
        
        Integer currentNode = netNodes.get(0);
        Set<Integer> partitionNodes = new HashSet<Integer>();
        
        getPartition(currentNode, netNodes, partitionNodes);
        int partitionSize = partitionNodes.size();
        
        if(partitionSize == netSize){
            
            return true;
            
        } else {
            
            return false;
        }
    }
    
    
    private Set<Integer> getPartition(Integer currentNode, List<Integer> nodesToCheck, Set<Integer> partition){
        nodesToCheck.remove(currentNode);
        for(Integer neighbor : this.getNode(currentNode).getLinks()){
            if(nodesToCheck.contains(neighbor)){
                getPartition(neighbor, nodesToCheck, partition);
            }
        }
        
        partition.add(currentNode);
        
        return partition;
    }

    
    /**
     * @return the diameter
     */
    public int getDiameter() {
        return diameter;
    }

    
    /**
     * @param diameter the diameter to set
     */
    public void setDiameter(int diameter) {
        this.diameter = diameter;
    }
    
    
    
    
    
}
