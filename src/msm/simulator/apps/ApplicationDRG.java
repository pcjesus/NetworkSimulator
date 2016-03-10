/**
 * MSM - Network Simulator
 */
package msm.simulator.apps;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import msm.simulator.ComEngine;
import msm.simulator.Config;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.ConfigException;
import msm.simulator.network.Message;
import msm.simulator.util.NetStatistics;


/**
 * Dynamic Network version
 * @author pjesus
 *
 */


public class ApplicationDRG extends Application {
    
    private static final String PARAM_TICK_TIMEOUT = "TICK_TIMEOUT";
    private static final String PARAM_MSG_TIMEOUT = "MSG_TIMEOUT";
    
//    private static final String PARAM_RND_RANGE = "RND_RANGE";
    private static final String PARAM_CLOUDS_NUM = "CLOUDS_NUM";
    private static final String PARAM_KNOW_NEIGHBORS_INIT_VALUES = "KNOW_NEIGHBORS_INIT_VALUES";
    private static final String PARAM_ASSESS_ALL_NEIGHBORS = "ASSESS_ALL_NEIGHBORS";
    private static final String PARAM_SEND_TO_ALL_NEIGHBORS = "SEND_TO_ALL_NEIGHBORS";
    private static final String PARAM_SEND_BY_BROADCAST = "SEND_BY_BROADCAST";
    private static final String PARAM_LEADER_PROBABILITY = "LEADER_PROBABILITY";
    
    
    // Init Value
//    private BigDecimal initValue;
    
    // Temporary mappings (estimates)
    private Map<Integer, BigDecimal> tmpEstimates;
//    private Map<Integer, BigDecimal> tmpFlows;
    
    //STATE
    private BigDecimal estimate;
    //Leader Probability
    private float pg;
    //Leader
    private Integer leader;
    //mode
    private enum StateModes {IDLE, MEMBER, LEADER};
    private StateModes mode;
    
    private Random rnd; 
    
    private enum MessageTypes {GCM, JACK, GAM};
    //Indicate if waiting for JACK/GAM message
//    private boolean waitResponse;
    
    //Time waiting for JACK/GAM
    private int cntTime;
    
    
    //Tick timeout Config
    private int tickTimeout;
    
    //Message timeout Config (time to wait for the reception of a message)
    private int msgTimeout;
    
    String clockTick;
    String clockGAM;

    //Function to compute
    private NetStatistics.AggFunctions function;
    
    //Random range number
    //private int rndRange;
    
    //Indicate the neighbors values will be initially known
//    private boolean knowNeighborsInitValue;
    
    //Indicate the all the neighbors will be assess in each round or only one (algorithm variant)
//    private boolean assessAllNeighbors;
    
    //Indicate new data will be sent to all neighbors or only one (algorithm variant)
//    private boolean sendToAllNeighbors;
    
    //Indicate the message transmission policy (when TRUE all message are send by broadcast)
    private boolean sendByBroadcast;
    
    //Cloud Number
    private int cloudsNum;
    private BigDecimal bdCloudsNum;
    
    //Start Probability
    private float sp;
    private Random rndStart;
    
    private String debugMsg;
    private BigDecimal previousValue;
    private BigDecimal previousDeviation;
    private BigDecimal previousDeviation2;
//    private BigDecimal previousMaxNetValue = new BigDecimal(-Double.MAX_VALUE);
//    private BigDecimal previousMinNetValue = new BigDecimal(Double.MAX_VALUE);
    private BigDecimal previousMaxNetValue = BigDecimal.valueOf(-Double.MAX_VALUE);
    private BigDecimal previousMinNetValue = BigDecimal.valueOf(Double.MAX_VALUE);
    
    private MathContext truncMC;
    
    //Auxiliary variable used to hold the neighbor to process in each round (when needed)
    private Integer selectedNeighbor = null;
    
    public ApplicationDRG(){
        super();
        this.debugMsg = new String();
    }
    
    
    public void init(Config config, String... args) throws ApplicationException {
        
        int index = Integer.parseInt(args[0]);
        int repetition = Integer.parseInt(args[1]);
        try {
            this.function = super.getComEngine().getAggFunction();
            switch(this.function) {
                case AVG    : //this.rndRange = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingBcast.PARAM_RND_RANGE)); 
                              //setInitRandomValue();
                              break;
                              
                case COUNT  : this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationDRG.PARAM_CLOUDS_NUM)); 
                              this.bdCloudsNum = new BigDecimal(this.cloudsNum);
                              setInitCloudsValue(repetition);
                              break;
                default : 
                    System.err.println("DEBUG: Aggregation Function ["+this.function+"] NOT Supported.");
                    System.err.println("DEBUG: \t- Using COUNT Aggregation Function instead.");
                    this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationDRG.PARAM_CLOUDS_NUM)); 
                    this.bdCloudsNum = new BigDecimal(this.cloudsNum);
                    setInitCloudsValue(repetition);
            }
            
            
            this.tickTimeout = Integer.parseInt(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationDRG.PARAM_TICK_TIMEOUT));
            this.msgTimeout = Integer.parseInt(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationDRG.PARAM_MSG_TIMEOUT));
            super.getComEngine().getNetwork().setNumClouds(this.cloudsNum);
            
//            this.knowNeighborsInitValue = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_KNOW_NEIGHBORS_INIT_VALUES));
//            this.assessAllNeighbors = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_ASSESS_ALL_NEIGHBORS));
//            this.sendToAllNeighbors = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_SEND_TO_ALL_NEIGHBORS));
            this.sendByBroadcast = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_SEND_BY_BROADCAST));
            this.pg = Float.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationDRG.PARAM_LEADER_PROBABILITY));
            
        }catch (ConfigException ce){
            throw (ApplicationException)new ApplicationException(ce).initCause(ce);
        }
        
        this.rndStart = new Random();
        
        
        this.setValue(super.getInitValue());
        this.setEstimate(super.getInitValue());
        this.initState();
        
        this.previousValue = this.getValue();
//        this.previousDeviation = new BigDecimal(Double.MAX_VALUE, super.getMathContext());
//        this.previousDeviation2 = new BigDecimal(Double.MAX_VALUE, super.getMathContext());
        this.previousDeviation = BigDecimal.valueOf(Double.MAX_VALUE);
        this.previousDeviation2 = BigDecimal.valueOf(Double.MAX_VALUE);
        
        truncMC = new MathContext(super.getMathContext().getPrecision()-3, RoundingMode.DOWN);
    }
    
    
    public void init2(){
/*        if(this.knowNeighborsInitValue){
            
            //Get Estimated Values from all neighbors
            Set <Integer> links;
            if(super.getComEngine().useOverlay()){
                links = super.getAppNode().getOverlayLinks();
            } else {
                links = super.getAppNode().getLinks();
            }
            
            Map<Integer, BigDecimal> tmpEstimates = new HashMap<Integer, BigDecimal>(links.size());
            for(Integer n : links){
                ApplicationDRG neighborApp = (ApplicationDRG) super.getComEngine().getNetwork().getNode(n).getApplication();
                tmpEstimates.put(n, neighborApp.getValue());
            }
            
            //Calculate new estimate
            BigDecimal e = super.getInitValue();
            BigDecimal a = (e.add(sumMapValues(tmpEstimates), super.getMathContext())).divide(new BigDecimal(tmpEstimates.size() + 1), super.getMathContext());
            
            //Set new value
            this.setValue(a);
        }
*/
    }
    
    
    
    private void setInitCloudsValue(int repetition){
        
        //Use different clouds in each repetition
        //  - Set ids in sequence (for all executions/repetitions) 
        
        int id = super.getAppNode().getId();
        
        //downLimit = (cloudsNum * repetition) - nSize * TRUNC((cloudsNum * repetition)/nSize)
        int downLimit = this.cloudsNum * repetition;
        int nSize = super.getComEngine().getNetwork().getNumNodes();
        int cof = downLimit / nSize;
        downLimit = downLimit - (nSize * cof);
        
        //upLimit = downLimit + cloudsNum
        int upLimit = downLimit + this.cloudsNum;
        
        //rest = upLimit - nSize (Practically used only when nSize > upLimit)
        int rest = upLimit - nSize;
        
        // isCloud only if: (downLimit <= id < upLimit) or (0 <= id < rest)
        if((((downLimit <= id) && (id < upLimit)) || ((0 <= id) && (id < rest)))){
            super.setInitValue(BigDecimal.ONE);
            super.getComEngine().getNetwork().addCloudNode(id);
        } else {
            super.setInitValue(BigDecimal.ZERO);
        }
        

    }
    
    
    private void initState(){
        Set <Integer> links;
        if(super.getComEngine().useOverlay()){
            links = super.getAppNode().getOverlayLinks();
        } else {
            links = super.getAppNode().getLinks();
        }
        this.tmpEstimates = new HashMap<Integer, BigDecimal>(links.size());
//        this.tmpFlows = new HashMap<Integer, BigDecimal>(links.size());
/*        this.stateFlows = new HashMap<Integer, BigDecimal>(links.size());
        for(Integer n : links){
//            this.estimatedValues.put(n, BigDecimal.ZERO);
            this.stateFlows.put(n, BigDecimal.ZERO);
        }
*/
        this.mode = StateModes.IDLE;
        this.rnd = new Random();
        
//        this.waitResponse = false;
        this.cntTime = 0;
    }
    
    
    public void onReceive(Message<?> msg){
        
        // Get Sender Id
        Integer senderId = Integer.valueOf(msg.getFrom());

        // Get Message Data (format: TYPE|DATA;...;TYPEn|DATAn;)
        String msgData = (String) msg.getData();
        String msgs[] = msgData.split(";");

        for (String data : msgs) {

            if (data.length() == 0) {
                System.err.println("Node " + super.getAppNode().getId()
                        + " received empty data at time "
                        + super.getComEngine().getGlobalTime());
            } else {

                String elements[] = data.split("\\|");

                switch (MessageTypes.valueOf(elements[0])) {
                    case GCM:
                        if (this.getMode() == StateModes.IDLE) {
                            // Become Member
                            this.setMode(StateModes.MEMBER);
                            this.setLeader(senderId);
                            sendJACK();
                            super.resetTimeout(this.clockTick);
                            try {
                                //Waiting for GAM
                                this.clockGAM = super.setTimeout(this.msgTimeout);
                            } catch (ApplicationException e) {
                                System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
                                e.printStackTrace();
                                System.exit(0);
                            }
                        }
                        break;
                    case JACK:
                        if (this.getMode() == StateModes.LEADER) {

                            int leaderID = Integer.parseInt(elements[1]);

                            if (super.getAppNode().getId() == leaderID) {
                                this.tmpEstimates.put(senderId,
                                        new BigDecimal(elements[2], super
                                                .getMathContext()));
                            }

                        }
                        break;
                    case GAM:
                        if (this.getMode() == StateModes.MEMBER) {

                            int leaderID = Integer.parseInt(elements[1]);
                            int receverID = Integer.parseInt(elements[2]);

                            if ((this.getLeader() == leaderID) && (receverID == super.getAppNode().getId())) {
//                                this.stateFlows.put(senderId,
//                                        new BigDecimal(elements[3], super
//                                                .getMathContext()));
                                this.setEstimate(new BigDecimal(elements[3], super.getMathContext()));
                                super.setValue(this.aggEstimate(this.getEstimate()));
                                super.resetTimeout(this.clockGAM);
                                reset();
                                this.setMode(StateModes.IDLE);
                                try {
                                    //Wait before deciding to become leader again
                                    this.clockTick = super.setTimeout(this.tickTimeout);
                                } catch (ApplicationException e) {
                                    System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
                                    e.printStackTrace();
                                    System.exit(0);
                                }
                            }
                        }
                        break;
                    default:
                        System.err.println("\nUnexcpected Message Type: "
                                + elements[1]);
                        break;
                }

            } // Message data not empty

        } // for msgs (from the same sender)

    }

    
    public void onTick() {
        
        switch (this.getMode()) {
            case IDLE:
                    if(decideToBecomeLeader()){
                        //Send GCM
                        sendGCM();
                        try {
                            //Waiting for JACKs
                            super.setTimeout(this.msgTimeout);
                        } catch (ApplicationException e) {
                            System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
                            e.printStackTrace();
                            System.exit(0);
                        }
                    } else {
                        try {
                            //Wait before deciding to become leader again
                            this.clockTick = super.setTimeout(this.tickTimeout);
                        } catch (ApplicationException e) {
                            System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
                            e.printStackTrace();
                            System.exit(0);
                        }
                    }
                break;
            case MEMBER:
                      //Stop waiting for Leader
                    reset();
                    this.setMode(StateModes.IDLE);
                    try {
                        //Wait before deciding to become leader again
                        this.clockTick = super.setTimeout(this.tickTimeout);
                    } catch (ApplicationException e) {
                        System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
                        e.printStackTrace();
                        System.exit(0);
                    }
                break;
            case LEADER:
                    computeAverage();
                    if(this.tmpEstimates.size() > 0){
                        //Send GAM
                        sendGAM();
                    }
                    this.setMode(StateModes.IDLE);
                    try {
                        //Wait before deciding to become leader again
                        this.clockTick = super.setTimeout(this.tickTimeout);
                    } catch (ApplicationException e) {
                        System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
                        e.printStackTrace();
                        System.exit(0);
                    }
                break;
            default:
                System.err.println("\n[stateTransition] Unexcpected State Mode: "+this.getMode());
                break;
        }
        
    }

    
    public String debugOnReceiveStatus(){
        return "Value="+super.getValue()+"; Estimate="+this.getEstimate();
    }
    
    
    
    

    
    /**
     * Message-Generation function
     * 
     * msg_i((F, E, v), j) = (-F, E, i)
     */

    public void messageGeneration(){
        
        switch (this.getMode()) {
            case IDLE:
                if(this.tmpEstimates.size() > 0){
                    sendGAM();
                }
                break;
            case LEADER:
                if(this.cntTime == 1){
                    sendGCM();
                }
                break;
            case MEMBER:
                if(this.cntTime == 1){
                    sendJACK();
                }
                break;
            default:
                //Do nothing
                //System.err.println("\n[messageGeneration] Unexcpected State Mode: "+this.getMode());
                break;
        }
    }
    
    
    // Send GCM
    private void sendGCM(){
        
        //Set Sender (i)
        Integer self = super.getAppNode().getId();
        String from = String.valueOf(self);
        
        if(this.sendByBroadcast){
            
            //Create Message Data (format: TYPE|DATA)
            StringBuffer sb = new StringBuffer(MessageTypes.GCM + "|");
            sb.append(from);
            
            //Broadcast Message
            super.broadcastMessage(from, sb.toString());
            
        } else {
        
            Set <Integer> links;
            if(super.getComEngine().useOverlay()){
                links = super.getAppNode().getOverlayLinks();
            } else {
                links = super.getAppNode().getLinks();
            }
            for(Integer receiverId : links){
                
                //Create Message Data (format: TYPE|DATA)
                StringBuffer sb = new StringBuffer(MessageTypes.GCM + "|");
                sb.append(from);
        
                //Send Message (i -> j) - Put it in the senders channel
                super.sendMessage(from, receiverId.toString(), sb.toString());
                
            }
        
        }
        
    }
    
    
    // Send estimate to leader
    private void sendJACK(){
        
        Integer self = super.getAppNode().getId();

        // Set Sender (i)
        String from = String.valueOf(self);
        String to = this.getLeader().toString();

        //Create Message Data (format: TYPE|DATA)
        StringBuffer sb = new StringBuffer();

        // Neighbor Message Data (format: JACK|leader|<Estimate>)
        sb.append(MessageTypes.JACK);
        sb.append("|");
        sb.append(to);
        sb.append("|");
        sb.append(this.getEstimate());

        // Send JACK Message
        if (this.sendByBroadcast) {

            sb.append(";"); // ??? May be needed...

            // Send Broadcast Message (i -> 0..j) - Put it in the (single)
            // sender channel
            super.broadcastMessage(from, sb.toString());

        } else {

            // Send Message (i -> j) - Put it in the senders channel
            super.sendMessage(from, to, sb.toString());

        }            

        
    }
    
    
    // Send flows to members
    private void sendGAM(){
        
        //Set Sender (i)
        String from = String.valueOf(super.getAppNode().getId());
        
        if(this.sendByBroadcast){
            
            //Init Message Data (format: TYPE|j|DATA;...;TYPE|k|DATA;)
            StringBuffer sb = new StringBuffer();
            
            for(Integer receiverId : this.tmpEstimates.keySet()){
                
                //Create GAM Message Data (format: GAM|ID(j)|<Flow>)
                sb.append(MessageTypes.GAM);
                sb.append("|");
                sb.append(from);
                sb.append("|");
                sb.append(receiverId);
                sb.append("|");
//                sb.append(this.stateFlows.get(receiverId).multiply(new BigDecimal(-1, super.getMathContext())));
                sb.append(this.getEstimate());
                sb.append(";");
            }
            
            if(this.tmpEstimates.size() > 0){
                //Send Broadcast Message (i -> 0..j) - Put it in the (single) sender channel
                super.broadcastMessage(from, sb.toString());
            } else {
                System.err.println("NO MESSAGE TO SEND (Node "+super.getAppNode().getId()+") at time "+super.getComEngine().getGlobalTime());
            }
            
        } else {
        
            for(Integer receiverId : this.tmpEstimates.keySet()){
            
                //Init Message Data (format: TYPE|j|DATA)
                StringBuffer sb = new StringBuffer();
        
                //Neighbor Message Data (format: GAM|ID(j)|<Flow>)
                sb.append(MessageTypes.GAM);
                sb.append("|");
                sb.append(from);
                sb.append("|");
                sb.append(receiverId);
                sb.append("|");
//                sb.append(this.stateFlows.get(receiverId).multiply(new BigDecimal(-1, super.getMathContext())));
                sb.append(this.getEstimate());
                
                //Send Message (i -> j) - Put it in the senders channel
                super.sendMessage(from, receiverId.toString(), sb.toString());
        
            }
        }
        
    }
    
    
    public void stateTransition(Collection<Message<?>> msgsReceived){
        
        //Process all received messages
        this.processReceivedMessages(msgsReceived);
        
        if(ComEngine.useDebug){
            this.debugMsg = "\t (1) - Mode: "+this.getMode()+"; InitValue="+super.getInitValue()+"; Value="+this.getValue()+"; Estimate="+this.getEstimate()+";\n";
        }
        
        //Update State
        if ((this.getMode() == StateModes.MEMBER) && (this.cntTime == 0)){
            this.setMode(StateModes.IDLE);
        }
        
        if((this.getMode() == StateModes.IDLE) && (this.cntTime == 0)){
            this.tmpEstimates.clear();
//            this.setValue(super.getInitValue().subtract(sumMapValues(this.stateFlows), super.getMathContext()));
            decideToBecomeLeader();
        }
        
        if((this.getMode() == StateModes.LEADER) && (this.cntTime == 0)){
            computeAverage();
            this.setMode(StateModes.IDLE);
        } 
        
        
        if(cntTime > 0){
//            reset();
//            this.setMode(StateModes.IDLE);
            this.cntTime--;
        }
        
        if(ComEngine.useDebug){
            this.debugMsg += "\t (2) - Mode: "+this.getMode()+"; InitValue="+super.getInitValue()+"; Value="+this.getValue()+"; Estimate="+this.getEstimate()+";";
        }
        
    }
    
    
    private void processReceivedMessages(Collection<Message<?>> msgsReceived) {

        for (Message<?> msg : msgsReceived) {

            // Get Sender Id
            Integer senderId = Integer.valueOf(msg.getFrom());

            // Get Message Data (format: TYPE|DATA;...;TYPEn|DATAn;)
            String msgData = (String) msg.getData();
            String msgs[] = msgData.split(";");

            for (String data : msgs) {

                if (data.length() == 0) {
                    System.err.println("Node " + super.getAppNode().getId()
                            + " received empty data at time "
                            + super.getComEngine().getGlobalTime());
                } else {

                    String elements[] = data.split("\\|");

                    switch (MessageTypes.valueOf(elements[0])) {
                        case GCM:
                            if (this.getMode() == StateModes.IDLE) {
                                // Become Member
                                this.setMode(StateModes.MEMBER);
                                this.setLeader(senderId);
                                this.cntTime = 2;
                            }
                            break;
                        case JACK:
                            if (this.getMode() == StateModes.LEADER) {

                                int leaderID = Integer.parseInt(elements[1]);

                                if (super.getAppNode().getId() == leaderID) {
                                    this.tmpEstimates.put(senderId,
                                            new BigDecimal(elements[2], super
                                                    .getMathContext()));
                                }

                            }
                            break;
                        case GAM:
                            if (this.getMode() == StateModes.MEMBER) {

                                int leaderID = Integer.parseInt(elements[1]);
                                int receverID = Integer.parseInt(elements[2]);

                                if ((this.getLeader() == leaderID) && (receverID == super.getAppNode().getId())) {
//                                    this.stateFlows.put(senderId,
//                                            new BigDecimal(elements[3], super
//                                                    .getMathContext()));
                                    
                                    this.setEstimate(new BigDecimal(elements[3], super.getMathContext()));
                                    super.setValue(this.aggEstimate(this.getEstimate()));
                                    this.setMode(StateModes.IDLE);
                                    this.setLeader(null);
                                }
                            }
                            break;
                        default:
                            System.err.println("\nUnexcpected Message Type: "
                                    + elements[1]);
                            break;
                    }

                } // Message data not empty

            } // for msgs (from the same sender)
        } // for msgsReceived (from different senders)
    }
    
    
    // Decide link direction (of neighbors with lower ID) and update state
    private boolean decideToBecomeLeader(){

        double randVal = this.rnd.nextDouble();
        if(randVal <= this.pg){
            //Update mode
            this.setMode(StateModes.LEADER);
            this.cntTime = 2;
            return true;
        } else {
            this.cntTime = 3;
            return false;
        }
    }
    
    
    // Compute Average (if leader; with received estimates)
    private void computeAverage() {

        // If received estimates from other nodes (elected)
        if (this.tmpEstimates.size() > 0) {

            BigDecimal sum_e = sumMapValues(this.tmpEstimates);
            BigDecimal a;

            // Compute new average
            BigDecimal e = this.getEstimate();
            a = (e.add(sum_e, super.getMathContext())).divide(new BigDecimal(
                    this.tmpEstimates.size() + 1), super.getMathContext());

            // Compute flows
/*            for (Integer neighborId : this.tmpEstimates.keySet()) {

                // [= -f_ji + (A_m - e_j)]
                BigDecimal flow_new = this.stateFlows.get(neighborId).add(
                        a.subtract(this.tmpEstimates.get(neighborId), super
                                .getMathContext()), super.getMathContext());

                // Set new flow
                this.stateFlows.put(neighborId, flow_new);

            }
*/          
            this.setEstimate(a);
            super.setValue(this.aggEstimate(this.getEstimate()));

        }

    }
    
    
    private void reset(){
        
        this.leader = null;
        
        
        //Update estimate
//        this.setValue(super.getInitValue().subtract(sumMapValues(this.stateFlows), super.getMathContext()));
        
    }
    
    

    private BigDecimal aggEstimate(BigDecimal e){
         
        switch(this.function) {
            case AVG    : 
                return e;
                          
            case COUNT  : 
                if(e.compareTo(BigDecimal.ZERO) > 0){
                    return this.bdCloudsNum.divide(e, super.getMathContext());
                } else {
                    return BigDecimal.ZERO;
                }
                
            default : 
                System.err.println("DEBUG: Aggregation Function ["+this.function+"] NOT Supported.");
                System.err.println("DEBUG: \t- Using COUNT Aggregation Function instead.");
                if(e.compareTo(BigDecimal.ZERO) > 0){
                    return this.bdCloudsNum.divide(e, super.getMathContext());
                } else {
                    return BigDecimal.ZERO;
                }
        }
    }
    
    
    private void detectLinkChanges(){

        //Detect arriving nodes
        Set<Integer> arriving = super.getAppNode().getArrivingNodes();
        
        //Detect leaving nodes
        Set<Integer> leaving = super.getAppNode().getLeavingNodes();
        
        //Handle arriving nodes
        if(arriving.size() > 0){
            processArrivingNeighbor(arriving);
            super.getAppNode().clearArrivingNodes();
        }
        
        //Handle leaving/crash nodes
        if(leaving.size() > 0){
            processLeavingNeighbor(leaving);
            super.getAppNode().clearLeavingNodes();
        }
        
    }
    
    
    private void detectLeavingNodes(){
        
        //Detect leaving nodes
        Set<Integer> leaving = super.getAppNode().getLeavingNodes();
        
        //Handle leaving/crash nodes
        if(leaving.size() > 0){
            processLeavingNeighbor(leaving);
            super.getAppNode().clearLeavingNodes();
        }
        
        //Arriving nodes are implicitly handled when a message from a new node arrives
        super.getAppNode().clearArrivingNodes();
        
    }
    
    
    private void processArrivingNeighbor(Set<Integer> nodes){
/*        
        for(Integer nodeID : nodes){
            if(this.knowNeighborsInitValue){
                ApplicationDRG nodeApp = (ApplicationDRG) super.getComEngine().getNetwork().getNode(nodeID).getApplication();
    //            this.estimatedValues.put(nodeID, nodeApp.getValue());
                this.stateFlows.put(nodeID, BigDecimal.ZERO);
            } else {
    //            this.estimatedValues.put(nodeID, BigDecimal.ZERO);
                this.stateFlows.put(nodeID, BigDecimal.ZERO);
            }            
        }
        
        
        if(this.knowNeighborsInitValue){
            
            //Calculate new estimate
            BigDecimal e = super.getValue();
            BigDecimal a = (e.add(sumMapValues(this.tmpEstimates), super.getMathContext())).divide(new BigDecimal(this.tmpEstimates.size() + 1), super.getMathContext());
            
            //Calculate and set new state
            
            //Get All Neighbors
            Set<Integer> neighborsList = this.stateFlows.keySet();
            
            for(Integer neighborId : neighborsList){
                
                //Calculate new flow 
                // [= F_j - (e_new - E_j)]
                BigDecimal flow_new = this.stateFlows.get(neighborId).subtract(a.subtract(this.tmpEstimates.get(neighborId), super.getMathContext()), super.getMathContext());

                //Set new flow
                this.stateFlows.put(neighborId, flow_new);

                //Set new estimated value
    //            this.estimatedValues.put(neighborId, e_new);
                
            }
            
            this.setValue(super.getInitValue().add(sumMapValues(this.stateFlows), super.getMathContext()));
            this.tmpEstimates.clear();
        }
 */       
        
    }
    
    
    
    private void processLeavingNeighbor(Set<Integer> nodes){
/*        
        for(Integer nodeID : nodes){
    //        this.estimatedValues.remove(nodeID);
            this.stateFlows.remove(nodeID);
            
            //If selected neighbor from previous round leaves, no one will receive the message
            if(this.selectedNeighbor != null && this.selectedNeighbor.equals(nodeID)){
                this.selectedNeighbor = null;
            }
        }
*/        
    }
    
    
    
    
    
    
    public String debugTickStatus(){
        return this.debugMsg 
//            + "\n" + this.toStringEstimatedValues() 
//            + "\n" + this.toStringFlowValues()
            + "\n\tLEADER: " + this.getLeader()
            + "\n\tWaitTime: " + this.cntTime;
    }
    
    
    private boolean start(){
        double randVal = this.rndStart.nextDouble();
        if(randVal <= this.sp){
            return true;
        } else {
            return false;
        }
    }
    

    private BigDecimal sumMapValues(Map<?, BigDecimal> map){
        BigDecimal a = BigDecimal.ZERO;
        for(BigDecimal x : map.values()){
            a = a.add(x);
        }
        return a;
    }
    
   
/*    
    private String toStringEstimatedValues(){
        StringBuffer sb = new StringBuffer("\tESTIMATED VALUES: ");
        for(Integer neighboorId : this.estimates.keySet()){
            sb.append(neighboorId);
            sb.append(" -> ");
            sb.append(this.estimates.get(neighboorId));
            sb.append("; ");
        }
        
        return sb.toString();
    }
*/    

/*
    private String toStringFlowValues(){
        StringBuffer sb = new StringBuffer("\tFLOW VALUES: ");
        for(Integer neighboorId : this.stateFlows.keySet()){
            sb.append(neighboorId);
            sb.append(" -> ");
            sb.append(this.stateFlows.get(neighboorId));
            sb.append("; ");
            
        }
        
        return sb.toString();
    }
*/    
    
    


    

    public String getState(){
        
        BigDecimal currentDeviation = NetStatistics.nodeDeviation(super.getComEngine().getNetwork().getNodes(), super.getValue(), super.getMathContext());
        
        //Get Estimated Values from all neighbors
        Set <Integer> links;
        if(super.getComEngine().useOverlay()){
            links = super.getAppNode().getOverlayLinks();
        } else {
            links = super.getAppNode().getLinks();
        }
        Set<BigDecimal> e = new HashSet<BigDecimal>(links.size());
        for(Integer n : links){
            ApplicationDRG neighborApp = (ApplicationDRG) super.getComEngine().getNetwork().getNode(n).getApplication();
            e.add(neighborApp.getValue());
        }
        BigDecimal currentDeviation2 = NetStatistics.nodeLocalEstimateDeviation(super.getComEngine().getNetwork().getNodes(), e, super.getMathContext());
        
        String state = this.getValue() + ";" + (this.previousValue.subtract(this.getValue(), super.getMathContext())) 
            + ";" + this.getAppNode().getMsgSendCount()
            + ";" + this.getAppNode().getMsgRcvCount()
//            + ";" + this.getAppNode().getMsgDiscardSendCount()
//            + ";" + this.getAppNode().getMsgDiscardRcvCount()
            + ";" + this.getAppNode().getMsgLossCount()
            + ";" + NetStatistics.sumNodeValues(super.getComEngine().getNetwork().getNodes(), super.getMathContext())
            + ";" + NetStatistics.nodeVariance(super.getComEngine().getNetwork().getNodes(), super.getValue(), super.getMathContext())
            + ";" + currentDeviation
            + ";" + currentDeviation2;
        
            String converge = (currentDeviation.compareTo(this.previousDeviation) <= 0)?"1":"-1";
            String converge2 = (currentDeviation2.compareTo(this.previousDeviation2) <= 0)?"1":"-1";
            state = state + ";" + converge+ ";" + converge2;
            
            //Get Network Min/Max info
            BigDecimal maxNetValue = this.getComEngine().getNetwork().getMaxValue(this.getComEngine().getAggFunction(), super.getMathContext());
            BigDecimal minNetValue = this.getComEngine().getNetwork().getMinValue(this.getComEngine().getAggFunction(), super.getMathContext());
            BigDecimal truncMaxNetValue = maxNetValue.round(truncMC);
            BigDecimal truncMinNetValue = minNetValue.round(truncMC);
            int maxComp = truncMaxNetValue.compareTo(this.previousMaxNetValue);
            int minComp = truncMinNetValue.compareTo(this.previousMinNetValue);
            String maxConverge = String.valueOf(maxComp);
            String minConverge = String.valueOf(minComp);
            String testMax = (maxComp <= 0)?"0":"1";
            String testMin = (minComp >= 0)?"0":"1";
            
            this.previousMaxNetValue = truncMaxNetValue;
            this.previousMinNetValue = truncMinNetValue;
            state = state + ";" + maxNetValue+ ";" + maxConverge + ";" + minNetValue+ ";" + minConverge + ";" + testMax + ";" + testMin;
            
            
        this.previousValue = this.getValue();
        this.previousDeviation = currentDeviation;
        this.previousDeviation2 = currentDeviation2;
        return state;
    }
    
/*    
    public BigDecimal getNeighborEstimatedValue(Integer neighborId){
        return this.estimates.get(neighborId);
    }
    
    public void setNeighborEstimatedValue(Integer neighborId, BigDecimal estimatedValue){
        this.estimates.put(neighborId, estimatedValue);
    }
*/    
/*    
    public BigDecimal getNeighborFlowValue(Integer neighborId){
        return this.stateFlows.get(neighborId);
    }
    
    public void setNeighborFlowValue(Integer neighborId, BigDecimal flowValue){
        this.stateFlows.put(neighborId, flowValue);
    }
*/    
    /**
     * @return the mode
     */
    public StateModes getMode() {
        return mode;
    }


    
    /**
     * @param mode the mode to set
     */
    public void setMode(StateModes mode) {
        this.mode = mode;
    }


    
    /**
     * @return the leader
     */
    public Integer getLeader() {
        return leader;
    }


    
    /**
     * @param leader the leader to set
     */
    public void setLeader(Integer leader) {
        this.leader = leader;
    }
    
    
    /**
     * @return the estimate
     */
    public BigDecimal getEstimate() {
        return estimate;
    }


    
    /**
     * @param estimate the estimate to set
     */
    public void setEstimate(BigDecimal estimate) {
        this.estimate = estimate;
    }
    
    
/*    
    public BigDecimal getValue(){
        if(super.getValue().equals(BigDecimal.ZERO)) {
            return super.getValue();
        } else {
            return BigDecimal.ONE.divide(super.getValue(), super.getMathContext());            
        }
    }
    
    public void setValue(BigDecimal value){
        if(value.equals(BigDecimal.ZERO)){
            super.setValue(value);
        } else {
            super.setValue(BigDecimal.ONE.divide(value, super.getMathContext()));
        }
    }
*/    
    
}
