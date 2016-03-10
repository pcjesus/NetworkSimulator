/**
 * MSM - Network Simulator
 */
package msm.simulator.apps;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import msm.simulator.ComEngine;
import msm.simulator.Config;
import msm.simulator.ComEngine.Model;
import msm.simulator.PlotData;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.ConfigException;
import msm.simulator.network.Message;
import msm.simulator.util.NetStatistics;
import msm.simulator.util.SetUtils;


/**
 * Dynamic Network version
 * @author pjesus
 *
 */


public class ApplicationFlowUpdatingDynamic extends Application {
    
    private static final String PARAM_TICK_TIMEOUT = "TICK_TIMEOUT";
    private static final String PARAM_FD_TIMEOUT = "FD_TIMEOUT";
    
    private enum AsynchronousStrategy {ONLY_TIMEOUT}
    private static final String DEFAULT_ASYNC_STRATEGY = "ONLY_TIMEOUT";
    private static final String PARAM_ASYNC_STRATEGY = "ASYNC_STRATEGY";
    private static final String PARAM_ASYNC_STRATEGY_PARAMS = "ASYNC_STRATEGY_PARAMS";
    
    private static final String PARAM_QUISCENCE_ERROR = "QUISCENCE_ERROR";
    private static final String PARAM_AWAKE_ERROR = "AWAKE_ERROR";
//    private static final String PARAM_STOP_SENDING_MSG_WHEN_QUIESCENT = "STOP_SENDING_MSG_WHEN_QUIESCENT";
    private static final String PARAM_QUIESCENCE_TIMEOUT = "QUIESCENCE_TIMEOUT";
    
//    private static final String PARAM_RND_RANGE = "RND_RANGE";
    private static final String PARAM_CLOUDS_NUM = "CLOUDS_NUM";
    private static final String PARAM_KNOW_NEIGHBORS_INIT_VALUES = "KNOW_NEIGHBORS_INIT_VALUES";
    private static final String PARAM_ASSESS_ALL_NEIGHBORS = "ASSESS_ALL_NEIGHBORS";
    private static final String PARAM_SEND_TO_ALL_NEIGHBORS = "SEND_TO_ALL_NEIGHBORS";
    private static final String PARAM_SEND_BY_BROADCAST = "SEND_BY_BROADCAST";
    
    
    // Init Value
//    private BigDecimal initValue;
    
    // Temporary mappings (estimates and flows)
    private Map<Integer, BigDecimal> tmpEstimates;
    private Map<Integer, BigDecimal> tmpFlows;
    
    //flow values (STATE)
    private Map<Integer, BigDecimal> stateFlows;
    private BigDecimal estimate;
    
    //Used by uncast version (select neighbor heuristic)
    private Map<Integer, BigDecimal> knownEstimates;
    

    //Received Message Buffer
    private Map<String, Message<?>> msgRcvBuffer;
    
    //Tick timeout Config
    private int tickTimeout;
    String clockEvtKey = null;
    
    //Asynchronous strategy used, and parameters
    private AsynchronousStrategy asyncStrategy;
    private String[] asyncParams;

    //Fault Detector data
//    private Map<Integer, Integer> fdLastInterrogationRcv;
    private Map<Integer, Integer> fdLastResponseRcv;
    private Map<Integer, Boolean> fdSuspected;
//    private Map<Integer, Integer> fdTagExpirationTime;
//    private long fdTagCount;
//    private int fdLastInterogationTime;
    private int fdTimeout;
    private boolean useFD;
    
    //Quiescence/Awake error
    private boolean useQuiescence;
    private boolean quiescent;
    private double qError;
    private double aError;
    private int qTimeout;
//    private boolean stopSendingMsg;
    private BigDecimal prevEstimate;
    
    private boolean prevQuiescent;
    private int maxQ;
    private int minQ;
    private double avgQ;
    private int qPeriod;
    private List<Integer> qPeriods;
    
    //Function to compute
    private NetStatistics.AggFunctions function;
    
    //Random range number
    //private int rndRange;
    
    //Indicate the neighbors values will be initially known
    private boolean knowNeighborsInitValue;
    
    //Indicate the all the neighbors will be assess in each round or only one (algorithm variant)
    private boolean assessAllNeighbors;
    
    //Indicate new data will be sent to all neighbors or only one (algorithm variant)
    private boolean sendToAllNeighbors;
    
    //Indicate the message transmission policy (when TRUE all message are send by broadcast)
    private boolean sendByBroadcast;
    
    //Cloud Number
    private int cloudsNum;
    private BigDecimal bdCloudsNum;
    
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
    
    
    private CustomFUReport fuReport;  
    private String outDir;
    private int index;
    private int repetition;
    private int limitTime;
    private int limitRepetition;
    private String netType;
    
    public ApplicationFlowUpdatingDynamic(){
        super();
        this.debugMsg = new String();
    }
    
    
    public void init(Config config, String... args) throws ApplicationException {
        
        this.index = Integer.parseInt(args[0]);
        this.repetition = Integer.parseInt(args[1]);
        this.outDir = config.getValueWithDefault(Config.PARAM_OUTPUT_DIR, Config.DEFAULT_OUTPUT_DIR);
        this.limitTime = Integer.parseInt(config.getValue(Config.PARAM_LIMIT_TIME)) - 1;
        this.limitRepetition = Integer.valueOf(config.getValueWithDefault(Config.PARAM_SIMULATION_REPETITION, "1")) - 1;
        this.netType = super.getComEngine().getNetwork().getType() + "-" + super.getComEngine().getNetwork().getInitNumNodes();
        
        try {
            this.function = super.getComEngine().getAggFunction();
            switch(this.function) {
                case AVG    : //this.rndRange = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingBcast.PARAM_RND_RANGE)); 
                              //setInitRandomValue();
                              break;
                              
                case COUNT  : this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingDynamic.PARAM_CLOUDS_NUM)); 
                              this.bdCloudsNum = new BigDecimal(this.cloudsNum);
                              setInitCloudsValue(repetition);
                              break;
                default : 
                    System.err.println("DEBUG: Aggregation Function ["+this.function+"] NOT Supported.");
                    System.err.println("DEBUG: \t- Using COUNT Aggregation Function instead.");
                    this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingDynamic.PARAM_CLOUDS_NUM)); 
                    this.bdCloudsNum = new BigDecimal(this.cloudsNum);
                    setInitCloudsValue(repetition);
            }
            
            
            this.tickTimeout = Integer.parseInt(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingDynamic.PARAM_TICK_TIMEOUT));
            super.getComEngine().getNetwork().setNumClouds(this.cloudsNum);
            
            this.knowNeighborsInitValue = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_KNOW_NEIGHBORS_INIT_VALUES));
            this.assessAllNeighbors = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_ASSESS_ALL_NEIGHBORS));
            this.sendToAllNeighbors = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_SEND_TO_ALL_NEIGHBORS));
            this.sendByBroadcast = Boolean.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, PARAM_SEND_BY_BROADCAST));
            
            String sAsyncStrategy = config.getValueWithDefault(DEFAULT_ASYNC_STRATEGY, index, Config.PARAM_APPLICATION_PARAM, PARAM_ASYNC_STRATEGY);
            this.asyncStrategy = AsynchronousStrategy.valueOf(sAsyncStrategy);
            String sAsyncParams = config.getValueWithDefault("", index, Config.PARAM_APPLICATION_PARAM, PARAM_ASYNC_STRATEGY_PARAMS);
            this.asyncParams = sAsyncParams.split(";");
            
            String sFDTimeout = config.getValueWithDefault("", index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingDynamic.PARAM_FD_TIMEOUT);
            this.fdTimeout = (sFDTimeout == null || (sFDTimeout.trim().length()) == 0)? -1 : Integer.parseInt(sFDTimeout);
            this.useFD = (this.fdTimeout > 0);
            
            String sQError = config.getValueWithDefault("", index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingDynamic.PARAM_QUISCENCE_ERROR);
            this.qError = (sQError == null || (sQError.trim().length()) == 0)? -1 : Double.parseDouble(sQError);
            this.useQuiescence = (this.qError > 0);
            if(this.useQuiescence){
                this.quiescent = false;
                this.prevQuiescent = false;
                String sAError = config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationFlowUpdatingDynamic.PARAM_AWAKE_ERROR);
                this.aError = (sAError == null || (sAError.trim().length()) == 0)? this.qError : Double.parseDouble(sAError);
//                if(this.getAppNode().getId() == 0){
//                    System.out.println("[DEBUG] - this.aError="+this.aError);
//                }
//                this.stopSendingMsg = Boolean.valueOf(config.getValueWithDefault("true", index, Config.PARAM_APPLICATION_PARAM, PARAM_STOP_SENDING_MSG_WHEN_QUIESCENT));
                String sQTimeout = config.getValueWithDefault("", index, Config.PARAM_APPLICATION_PARAM, PARAM_QUIESCENCE_TIMEOUT);
                this.qTimeout = (sQTimeout == null || (sQTimeout.trim().length()) == 0)? Integer.MAX_VALUE : Integer.parseInt(sQTimeout);
                this.maxQ = Integer.MIN_VALUE;
                this.minQ = Integer.MAX_VALUE;
                this.avgQ = -1;
                this.qPeriod = 0;
                this.qPeriods = new ArrayList<Integer>();
            }

            
        }catch (ConfigException ce){
            ce.printStackTrace();
            throw (ApplicationException)new ApplicationException(ce).initCause(ce);
        }        
        
        super.setValue(super.getInitValue());
        this.setEstimate(super.getInitValue());
        super.setBaseValue(this.getEstimate()); //trick for nice result values
        this.initState();
        
        this.previousValue = super.getValue();
//        this.previousDeviation = new BigDecimal(Double.MAX_VALUE, super.getMathContext());
//        this.previousDeviation2 = new BigDecimal(Double.MAX_VALUE, super.getMathContext());
        this.previousDeviation = BigDecimal.valueOf(Double.MAX_VALUE);
        this.previousDeviation2 = BigDecimal.valueOf(Double.MAX_VALUE);
        
        truncMC = new MathContext(super.getMathContext().getPrecision()-3, RoundingMode.DOWN);
        
        //Get Singleton CustomFUReport instance and initialize it for the current repetition
        this.fuReport = CustomFUReport.getInstance();
        this.fuReport.initCustomFUReport(this.useFD, this.useQuiescence, this.repetition);
    }
    
    
    public void init2(){
        if(this.knowNeighborsInitValue){
            
            //Get Estimated Values from all neighbors
            Set <Integer> links;
            if(super.getComEngine().useOverlay()){
                links = super.getAppNode().getOverlayLinks();
            } else {
                links = super.getAppNode().getLinks();
            }
            
            for(Integer n : links){
                ApplicationFlowUpdatingDynamic neighborApp = (ApplicationFlowUpdatingDynamic) super.getComEngine().getNetwork().getNode(n).getApplication();
                this.tmpEstimates.put(n, neighborApp.getBaseValue());
            }
            
            //Calculate new estimate
            BigDecimal e = super.getInitValue();
            BigDecimal a = (e.add(sumMapValues(this.tmpEstimates), super.getMathContext())).divide(new BigDecimal(this.tmpEstimates.size() + 1), super.getMathContext());
            
            //Calculate and set new state
            
            for(Integer neighborId : links){
                
                //Calculate new flow 
                // [= F_j - (e_new - E_j)]
                BigDecimal flow_new = this.stateFlows.get(neighborId).add(a.subtract(this.tmpEstimates.get(neighborId), super.getMathContext()), super.getMathContext());

                //Set new flow
                this.stateFlows.put(neighborId, flow_new);

                //Set new estimated value
                if(!this.sendToAllNeighbors){
                    this.knownEstimates.put(neighborId, a);
                }
                
            }
            
            this.setEstimate(estimate(this.stateFlows));
            super.setValue(aggEstimate(this.getEstimate()));
            super.setBaseValue(this.getEstimate()); //trick for nice result values
            
            this.tmpEstimates.clear();
        }
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
        this.tmpFlows = new HashMap<Integer, BigDecimal>(links.size());
        this.stateFlows = new HashMap<Integer, BigDecimal>(links.size());
        
        if(!this.sendToAllNeighbors){
            this.knownEstimates = new HashMap<Integer, BigDecimal>(links.size());
        }
        
        for(Integer n : links){
            if(!this.sendToAllNeighbors){
                this.knownEstimates.put(n, BigDecimal.ZERO);
            }
            this.stateFlows.put(n, BigDecimal.ZERO);

        }
        
        //Init FD state
        if(this.useFD){
            
            this.fdLastResponseRcv = new HashMap<Integer, Integer>(links.size());
            this.fdSuspected = new HashMap<Integer, Boolean>(links.size());
            
            for(Integer n : links){
                this.fdLastResponseRcv.put(n, 0);
                this.fdSuspected.put(n, false);
            }
        }
        
        //Init Quiescence state
        if(this.useQuiescence){
            this.prevEstimate = super.getValue();
        }
        
        //Clear message received buffer
        this.msgRcvBuffer = new HashMap<String, Message<?>>();
        
        
    }
    
    
    public void onReceive(Message<?> msg){
        
        //Add message to local buffer (overwrite existing message from the same source)
        try {

            Message<?> m = this.msgRcvBuffer.get(msg.getFrom());
            if(m != null && m.getSeqNum() > msg.getSeqNum()){
                //Old message not add to buffer
//                if(super.getAppNode().getId() == 20){
//                    System.out.println("NOT ADDED ["+super.getAppNode().getId()+"] - \tRcv Msg Id: "+msg.getMsgId()+"; \tMsg seq: "+msg.getSeqNum());
//                }
            } else {
                this.msgRcvBuffer.put(msg.getFrom(), msg);
//                if(super.getAppNode().getId() == 20){
//                    System.out.println("ADDED ["+super.getAppNode().getId()+"] - \tRcv Msg Id: "+msg.getMsgId()+"; \tMsg seq: "+msg.getSeqNum());
//                }
            }
            
             
            
            //Update FD data (if used)
            if(this.useFD){
                updateFD(msg);
            }
            
        } catch (Exception e){
            System.out.println("DEBUG - Msg: "+msg+"; Target Node: "+super.getAppNode().getId());
            String debug = "\t DEBUG - InitValue="+super.getInitValue()+"; Value="+this.getValue()+"; Estimate="+this.getEstimate()+";\n"; 
            System.out.println("\t DEBUG - STATE FLOWS: "+this.toStringFlowValues());
            System.out.flush();
            e.printStackTrace();
            System.err.flush();
        }
        
        
        if(super.getComEngine().getModel() == Model.Asynchronous){

            switch (this.asyncStrategy) {
                case ONLY_TIMEOUT:
                    // Check if message from all neighbors have been received
                    if (this.msgRcvBuffer.size() >= this.stateFlows.size()) {
                        // Remove previously scheduled tick event
                        if (this.clockEvtKey != null) {
                            super.resetTimeout(this.clockEvtKey);
                        }
                        // Force execution of algorithm (onTick event)
                        this.onTick();
                    }
                    break;
                default:
                    break;
            } //switch
        } //if
        
    }
    
    
    
    private void updateFD(Message<?> msg){
        
        Integer senderId = Integer.valueOf(msg.getFrom());
        Integer msgTime = msg.getTime();
        
        //Register interrogation tag (time)
        fdRegisterMsgReception(senderId, msgTime);
        
/*        
        //Register interrogation tag (time)
        fdRegisterInterrogation(senderId, msgTime);
        
        
        //Process interrogation responses
        String msgData = (String)msg.getData(); //Message Data (format: ID0|F0|E0|FD0;...;IDn|Fn|En|FDn;)
        String msgs[] = msgData.split(";");
        for(String data : msgs){
            if(data.length() == 0){
                System.err.println("Node "+super.getAppNode().getId()+" received empty data at round " + super.getComEngine().getGlobalTime());
            } else {
                String elements[] = data.split("\\|");
                int Id = 0;
                try{
                    Id = Integer.parseInt(elements[0]);
                } catch (Exception e){
                    System.out.println("MESSSAGE: "+msg);
                    System.out.println("MESSSAGE DATA: "+msgData);
                    System.out.println("Node "+super.getAppNode().getId()+" neighbors: "+super.getAppNode().getLinks());
                    for(Integer id : super.getAppNode().getLinks()){
                        System.out.println("Neighbor "+id+" neighbors: "+super.getComEngine().getNetwork().getNode(id).getLinks());
                    }
                    e.printStackTrace();
                    System.exit(0);
                }
                
                if(super.getAppNode().getId() == Id){
                
                    Integer fdTagRcv = new Integer(elements[3]);
                    
                    //Handle received response (register/update suspicions and interrogation time history)
                    fdRegisterResponse(senderId, fdTagRcv);
                    
                    break;
                }
                
            } //msg data not empty
        } //for(String data : msgs)
*/        
        
        //Verify nodes suspicion
        updateNodesSuspicion();
        
    }
    
/*    
    private void fdRegisterInterrogation(Integer senderId, Integer msgTime){
        
        //Register interrogation tag (time)
        Integer lastInterrogationRcv = this.fdLastInterrogationRcv.get(senderId); 
        if(lastInterrogationRcv == null){
            //New node (first message received from him)
            this.fdLastInterrogationRcv.put(senderId, msgTime);
        } else if(msgTime > lastInterrogationRcv){
            this.fdLastInterrogationRcv.put(senderId, msgTime);
        }
    }
*/    
    
    private void fdRegisterMsgReception(Integer senderId, Integer msgTime){
        
        //Register received response
        Integer lastRegisteredMsg = this.fdLastResponseRcv.get(senderId); 
        
        //NOTE: lastTagRcv cannot be null, should be previously handled by method: fdRegisterInterogation
        if(lastRegisteredMsg == null){
            //New node (first message received from him)
            this.fdLastResponseRcv.put(senderId, msgTime);
        } else if(msgTime > lastRegisteredMsg){
            
            this.fdLastResponseRcv.put(senderId, msgTime);
            
            //Set sender as trusted
            this.fdSuspected.put(senderId, false);
            
            //Check if lastInterrogationRcv exists, and remove it from time expiration history if not
            //TODO Check if min and remove it and all previous
//            if((lastRegisteredMsg != null) && (!existLERcvTag(lastRegisteredMsg))){
//                this.fdTagExpirationTime.remove(lastRegisteredMsg);
//            }
        }
    }
    
/*    
    private boolean existLERcvTag(Integer rcvTag){
        for(Integer tag : this.fdLastResponseRcv.values()){
            if(tag.compareTo(rcvTag) <= 0){
                return true;
            }
        }
        return false;
    }
*/    
    
    private void updateNodesSuspicion(){
        Integer currentTime = super.getComEngine().getGlobalTime();
        for(Map.Entry<Integer, Integer> e : this.fdLastResponseRcv.entrySet()){
//            Integer expireTime = this.fdTagExpirationTime.get(e.getValue());
            Integer expireTime = e.getValue() + this.fdTimeout;
            if(currentTime > expireTime){
                this.fdSuspected.put(e.getKey(), true);
            }
        }
    }
    
    
    public String debugOnReceiveStatus(){
        return "Value="+super.getValue()+"; Estimate="+this.getEstimate();
    }
    
    
    public void onTick() {
        
        //Invoke State Transition (algorithm)
        this.stateTransition(this.msgRcvBuffer.values());
        
        //Clear message buffer
        this.msgRcvBuffer.clear();
        
        //Invoke message generation (send message to neighbors)
        this.messageGeneration();
        
        //Schedule next tick
        try {
            this.clockEvtKey = super.setTimeout(this.tickTimeout);
        } catch (ApplicationException e) {
            System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        
    }
    

    
    /**
     * Message-Generation function
     * 
     * msg_i((F, E, v), j) = (-F, E, i)
     */

    public void messageGeneration(){
        
        //Detect and handle link changes
//        detectLinkChanges();
        
//        if(this.quiescent && this.stopSendingMsg){
        if(this.quiescent && (this.qTimeout < this.qPeriod)){
            
            //STOP SENDING MESSAGES
            
        } else {
        
            Set<Integer> neighboorsToSend = new HashSet<Integer>();
        
            if(this.sendToAllNeighbors){
            
                //Get All Neighbors
                neighboorsToSend = this.stateFlows.keySet();
            
            } else {
            
                if (this.selectedNeighbor!=null){
                
                    //Add selected neighbor to receivers list
                    neighboorsToSend.add(this.selectedNeighbor);
                }
            
            }
        
        
            //Set Sender (i)
            String from = String.valueOf(super.getAppNode().getId());
        
            if(this.sendByBroadcast){
            
                //Init Message Data (format: 0|F0|E0|FD0...j|Fj|Ej|FDj)
                StringBuffer sb = new StringBuffer();
            
                for(Integer receiverId : neighboorsToSend){
        
                    //Neighbor Message Data (format: ID(j)|F|E)
                    sb.append(receiverId);
                    sb.append("|");
                    sb.append(this.stateFlows.get(receiverId));
                    sb.append("|");
                    sb.append(this.getEstimate());
                    sb.append(";");
                }
            
                if(neighboorsToSend.size() > 0){
                    //Send Broadcast Message (i -> 0..j) - Put it in the (single) sender channel
                    super.broadcastMessage(from, sb.toString());
                                
                } else {
                    int id = super.getAppNode().getId();
                    Set<Integer> neighbors = super.getAppNode().getLinks();
                    boolean isdead = super.getComEngine().getNetwork().isDeadNode(id);
                    System.err.println("["+super.getComEngine().getGlobalTime()+"] - NO MESSAGE TO SEND (Node "+from+"); isDead? "+isdead+"; neighbors: "+neighbors);
                }
            
            
            } else {
        
                for(Integer receiverId : neighboorsToSend){
            
                    //Init Message Data (format: j|F|E)
                    StringBuffer sb = new StringBuffer();
        
                    //Neighbor Message Data (format: ID(j)|F|E)
                    sb.append(receiverId);
                    sb.append("|");
                    sb.append(this.stateFlows.get(receiverId));
                    sb.append("|");
                    sb.append(this.getEstimate());
        
                    //Send Message (i -> j) - Put it in the senders channel
                    super.sendMessage(from, receiverId.toString(), sb.toString());   
        
                }
            }
            
        } //if NOT quiescent OR NOT stop sending messages
        
    }
    
    
    public void stateTransition(Collection<Message<?>> msgsReceived){
        
        
        if(this.useFD){
            
            if(super.getComEngine().getModel() == Model.Asynchronous){ 
                
                //Verify nodes suspicion
                updateNodesSuspicion();
                
                //Handle suspicious nodes (leaving/crash) from FD
                handleSuspiciousNodes(false);
            }
            
        } else {
            
            //Detect and handle leaving/crash nodes
            detectLeavingNodes();
            
        }
        
        
        //Create temporary flow map with previous flow values
        //(used if no message is received, received flow update this value)
        this.tmpFlows.putAll(this.stateFlows);
        
        //Create temporary estimate map with current estimate, computed at the end of previous round
        //(used if no message is received, received estimates update this value)
        
        if(this.sendToAllNeighbors){
            for(Integer id : this.stateFlows.keySet()){
                this.tmpEstimates.put(id, this.getEstimate());
            }
        } else {
            for(Integer id : this.stateFlows.keySet()){
                this.tmpEstimates.put(id, this.knownEstimates.get(id));
            }
        }
        
        //Process all received messages
        //NOTE: Data from new nodes is added
        for (Message<?> msg : msgsReceived){
            
            //Get Sender Id
            Integer senderId = Integer.valueOf(msg.getFrom());
            
            
            //Update FD data (if used)
            if(this.useFD){
            
                if(super.getComEngine().getModel() != Model.Asynchronous){ 
                    //Handled elsewhere in case of asynchronous, i.e. at onReceive method
                    Integer msgTime = msg.getTime();
                
                    //Register message reception (time)
                    fdRegisterMsgReception(senderId, msgTime);
                }
            
            }
            
            //Get Message Data (format: ID1|DELTA1|VALUE1;...;IDn|DELTAn|VALUEn;)
            String msgData = (String)msg.getData();
            String msgs[] = msgData.split(";");
            
            for(String data : msgs){
                
                if(data.length() == 0){
                    System.err.println("Node "+super.getAppNode().getId()+" received empty data at round " + super.getComEngine().getGlobalTime());
                } else {
            
                    String elements[] = data.split("\\|");
                
                    int Id = 0;
                    try{
                        Id = Integer.parseInt(elements[0]);
                    } catch (Exception e){
                        System.out.println("MESSSAGES: "+msgsReceived);
                        System.out.println("MESSSAGE DATA: "+msgData);
                        System.out.println("Node "+super.getAppNode().getId()+" neighbors: "+super.getAppNode().getLinks());
                        for(Integer id : super.getAppNode().getLinks()){
                            System.out.println("Neighbor "+id+" neighbors: "+super.getComEngine().getNetwork().getNode(id).getLinks());
                        }
                        e.printStackTrace();
                        System.exit(0);
                    }
                
                    if(super.getAppNode().getId() == Id){
                
                        BigDecimal newFlow = new BigDecimal(elements[1]);
                        BigDecimal newEstimate = new BigDecimal(elements[2]);
                
                        //Set flow with symmetric of the value received (Add/Update entry)
                        this.tmpFlows.put(senderId, newFlow.multiply(new BigDecimal(-1, super.getMathContext())));
                        
                        this.stateFlows.put(senderId, newFlow.multiply(new BigDecimal(-1, super.getMathContext())));
                
                        //Set received estimate (Add/Update entry)
                        this.tmpEstimates.put(senderId, newEstimate);
                        
                        if(!this.sendToAllNeighbors){
                            this.knownEstimates.put(senderId, newEstimate);
                        }
                    
                        break;
                    }
                
                }
            }
        } //for (Message<?> msg : msgsReceived)
        
        
        //Update FD suspicions (if used)
        if(this.useFD){
            
            //If not Asynchronous (i.e. synchronous) update and handle suspicion after processing messages
            if(super.getComEngine().getModel() != Model.Asynchronous){ 
            
                //Verify nodes suspicion
                updateNodesSuspicion();
            
                //Handle suspicious nodes (leaving/crash) from FD
                handleSuspiciousNodes(true);
            }
        }
        
        
//        this.setValue(estimate(this.flowValues));
        if(ComEngine.useDebug){
            this.debugMsg = "\t (1) - InitValue="+super.getInitValue()+"; Value="+super.getValue()+"; Estimate="+this.getEstimate()+";\n";
//            this.debugMsg += "\t    FD -> Suspected: "+this.fdSuspected+"; Last Response Rcv: "+this.fdLastResponseRcv+"; Tag Expiration Time: "+this.fdTagExpirationTime+";\n";
            this.debugMsg += "\t    FD -> Suspected: "+this.fdSuspected+"; Last Response Rcv: "+this.fdLastResponseRcv+";\n";
        }
        
        //Calculate actual value [= <Initial Value> + <Sum of deltas received>]
        //double a_now = this.initValue + sumMapValues(this.deltaValuesRcv);
        BigDecimal e = estimate(this.tmpFlows);
        
        Set<Integer> neighborsList = new HashSet<Integer>();
        BigDecimal a;
        
        if(this.assessAllNeighbors){
        
            //Calculate new value considering neighbors estimated values
            //[= (<Value> + <Sum of neighbors estimated values>) / (<Number of Neighbors> + 1)]
            a = (e.add(sumMapValues(this.tmpEstimates), super.getMathContext())).divide(new BigDecimal(this.tmpEstimates.size() + 1), super.getMathContext());

        
            if (this.sendToAllNeighbors){
                
                //Get All Neighbors (use tmpFlows instead of statFlows, because tmpFlows consider flows from new neighbors)
                neighborsList = this.tmpFlows.keySet();
                
            } else {
                
                this.selectedNeighbor = selectNeighbor(false, e);
                neighborsList.add(this.selectedNeighbor);
            }
            
        
        } else {
            
            this.selectedNeighbor = selectNeighbor(false, e);
            neighborsList.add(this.selectedNeighbor);
            
            //Calculate new value considering neighbors estimated values
            //[= (<Value> + <neighbors estimated values>) / (2)]
            a = (e.add(this.tmpEstimates.get(this.selectedNeighbor), super.getMathContext())).divide(new BigDecimal(2), super.getMathContext());
          
//            System.out.println(super.getAppNode().getId()+" - a="+a);
        }
        
        
        for(Integer neighborId : neighborsList){
            
            //Calculate new flow 
            // [= F_j - (e_new - E_j)]
            BigDecimal flow_new = this.tmpFlows.get(neighborId).add(a.subtract(this.tmpEstimates.get(neighborId), super.getMathContext()), super.getMathContext());

            //Set new flow
            this.stateFlows.put(neighborId, flow_new);

            if(!this.sendToAllNeighbors){
                //Set new estimated value
                this.knownEstimates.put(neighborId, a);
            }
        
        }
        
        this.setEstimate(this.estimate(this.stateFlows));
        super.setValue(this.aggEstimate(this.getEstimate()));
        super.setBaseValue(this.getEstimate()); //trick for nice result values
        this.tmpEstimates.clear();
        this.tmpFlows.clear();
        
        if(ComEngine.useDebug){
            this.debugMsg += "\t (2) - InitValue="+super.getInitValue()+"; Value="+this.getValue()+"; Estimate="+this.getEstimate()+";"; 
        }
        
        Integer time = super.getComEngine().getGlobalTime();
        if(this.useFD || this.useQuiescence){
            //Init report data for current time
            this.fuReport.initTimeReportData(this.useFD, this.useQuiescence, this.repetition, time);
        }
        
        if(this.useFD){
            this.registerFDMistakes(time);
        }
        
        if(this.useQuiescence){
            this.prevQuiescent = this.quiescent;
            this.quiescent = this.determineQuiscence(super.getMathContext());
            this.prevEstimate = super.getValue();
            this.registerQuiescenceData(time);
        }
        
        
        if(this.useFD || this.useQuiescence){
            try {
                this.fuReport.storeReport(this.useFD, this.useQuiescence, this.limitRepetition, this.limitTime, this.index, this.repetition, this.netType, this.outDir, super.getMathContext());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }
    
    
    private Integer selectNeighbor(boolean random, BigDecimal ei){
        if(random){
            //Get Random Neighbor (use tmpFlows instead of statFlows, because tmpFlows consider flows from new neighbors)
            List<Integer> randomNeighborsList = SetUtils.randomizeSet(this.tmpFlows.keySet());
            return randomNeighborsList.get(0);
        } else {
            HashSet<Integer> candidates = new HashSet<Integer>();
            BigDecimal maxdif = BigDecimal.ZERO;
            for(Entry<Integer, BigDecimal> e : this.knownEstimates.entrySet()){
                BigDecimal dif = ei.subtract(e.getValue(), super.getMathContext()).abs(super.getMathContext());
                if(dif.compareTo(maxdif) > 0){
                    candidates.clear();
                    candidates.add(e.getKey());
                    maxdif = dif;
                } else if (dif.compareTo(maxdif) == 0){
                    candidates.add(e.getKey());
                }
            }
            
//            System.out.println(super.getAppNode().getId()+" - ei="+ei+"; "+toStringMap("knownEstimates", this.knownEstimates));
            
            
            List<Integer> randomNeighborsList = SetUtils.randomizeSet(candidates);
            
//            System.out.println(super.getAppNode().getId()+" - "+"Selected Neighbor: "+randomNeighborsList.get(0));
            return randomNeighborsList.get(0);
            
        }
    }
    
    
    private void registerFDMistakes(Integer time){
        
        int incorrectlySuspected = 0;
        int notSuspected = 0;
        
        for(Map.Entry<Integer, Boolean> e : this.fdSuspected.entrySet()){
            boolean isDead = super.getComEngine().getNetwork().isDeadNode(e.getKey());
            if((e.getValue() == true) && !isDead){
                incorrectlySuspected++;
            } else if ((e.getValue() == false) && isDead) {
                notSuspected++;
            }
        }
        
        boolean mistakeFound = false;
        if(incorrectlySuspected > 0){
            this.fuReport.addIncorreclySuspected(this.repetition, time, incorrectlySuspected);
            this.fuReport.addWithIncorreclySuspected(this.repetition, time);
            this.fuReport.addMistake(this.repetition, time, incorrectlySuspected);
            mistakeFound = true;
        }
        
        if(notSuspected > 0){
            this.fuReport.addNotSuspected(this.repetition, time, notSuspected);
            this.fuReport.addWithNotSuspected(this.repetition, time);
            this.fuReport.addMistake(this.repetition, time, notSuspected);
            mistakeFound = true;
        }
        
        if(mistakeFound){
            this.fuReport.addWithMistake(this.repetition, time);
        }
        
    }
    
    
    private boolean determineQuiscence(MathContext mc){
        
        //Compute current error
        double error;
        if(super.getValue().compareTo(BigDecimal.ZERO) > 0){
            BigDecimal bdError = ((super.getValue().subtract(this.prevEstimate)).abs()).divide(super.getValue(), mc);
            error = bdError.doubleValue();
        } else {
            error = Double.MAX_VALUE;
        }
        
        if(error <= this.qError){
            return true;
        } else if (error >= this.aError){
            return false;
        } else {
            return this.quiescent;
        }
    }
    
    
    private void registerQuiescenceData(Integer time){
        
        //Node is quiescent
        if(this.quiescent){
            this.fuReport.incQuiescentNodes(this.repetition, time);
            
            if(!prevQuiescent){
                //Start quiescence state
                this.qPeriod = 1;
            } else {
                //still quiescent
                this.qPeriod++;
            }
            
        }
        
        //Leave quiescence
        if(!this.quiescent && this.prevQuiescent){
            this.fuReport.incNodesLeavingQuiescence(this.repetition, time);
            
            this.qPeriods.add(this.qPeriod);
            this.fuReport.addQuiescencePeriod(this.repetition, this.qPeriod);
            
            if(this.qPeriod > this.maxQ){
                this.maxQ = this.qPeriod;
                this.fuReport.setMaxPeriodByRep(this.repetition, this.maxQ);
            }
            
            if(this.qPeriod < this.minQ){
                this.minQ = this.qPeriod;
                this.fuReport.setMinPeriodByRep(this.repetition, this.minQ);
            }
        }
    }
    

    private BigDecimal estimate(Map<?,BigDecimal> allFlows){
        
        return super.getInitValue().subtract(sumMapValues(allFlows), super.getMathContext());
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
            processLeavingNeighbor(leaving, false);
            super.getAppNode().clearLeavingNodes();
        }
        
    }
    
    
    private void detectLeavingNodes(){
        
        //Detect leaving nodes
        Set<Integer> leaving = super.getAppNode().getLeavingNodes();
        
        //Handle leaving/crash nodes
        if(leaving.size() > 0){
            processLeavingNeighbor(leaving, false);
            super.getAppNode().clearLeavingNodes();
        }
        
        //Arriving nodes are implicitly handled when a message from a new node arrives
        super.getAppNode().clearArrivingNodes();
        
    }
    
    private Set<Integer> getSuspiciousNodes(){
        Set<Integer> result = new HashSet<Integer>();
        for(Map.Entry<Integer, Boolean> e : this.fdSuspected.entrySet()){
            if(e.getValue() == true){
                result.add(e.getKey());
            }
        }
        
        return result;
    }
    
    private void handleSuspiciousNodes(boolean handleTmpData){
        
        //Detect suspicious nodes from FD
        Set<Integer> suspicious = this.getSuspiciousNodes();
        
        //Handle suspicious (leaving/crash) nodes
        if(suspicious.size() > 0){
            processLeavingNeighbor(suspicious, handleTmpData);
        }
        
//        if(super.getAppNode().getLeavingNodes().size() > 0){
//            System.out.println("[DEBUG] (handleSuspiciousNodes) - Node ("+super.getAppNode().getId()+") getLeavingNodes:"+super.getAppNode().getLeavingNodes());
//        }
        
        //Leaving nodes are detected by FD
        super.getAppNode().clearLeavingNodes();
        
        //Arriving nodes are implicitly handled when a message from a new node arrives
        super.getAppNode().clearArrivingNodes();
        
    }
    
    
    private void processArrivingNeighbor(Set<Integer> nodes){
        
        for(Integer nodeID : nodes){
            if(this.knowNeighborsInitValue){
                ApplicationFlowUpdatingDynamic nodeApp = (ApplicationFlowUpdatingDynamic) super.getComEngine().getNetwork().getNode(nodeID).getApplication();
    //            this.estimatedValues.put(nodeID, nodeApp.getValue());
                this.tmpEstimates.put(nodeID, nodeApp.getValue());
                this.stateFlows.put(nodeID, BigDecimal.ZERO);
            } else {
    //            this.estimatedValues.put(nodeID, BigDecimal.ZERO);
                this.stateFlows.put(nodeID, BigDecimal.ZERO);
            }
        }
        
        
        if(this.knowNeighborsInitValue){
            
            //Calculate new estimate
            BigDecimal e = this.getEstimate();
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
            
            this.setEstimate(this.estimate(this.stateFlows));
            super.setValue(this.aggEstimate(this.getEstimate()));
            super.setBaseValue(this.getEstimate()); //trick for nice result values
            this.tmpEstimates.clear();
        }
        
        
    }
    
    
    
    private void processLeavingNeighbor(Set<Integer> nodes, boolean handleTmpData){
        
        for(Integer nodeID : nodes){
    //        this.estimatedValues.remove(nodeID);
            this.stateFlows.remove(nodeID);
            
            if(handleTmpData){
                this.tmpEstimates.remove(nodeID);
                this.tmpFlows.remove(nodeID);
            }
                        
            //If selected neighbor from previous round leaves, no one will receive the message
            if(this.selectedNeighbor != null && this.selectedNeighbor.equals(nodeID)){
                this.selectedNeighbor = null;
            }
        }
        
    }
    
    
    
    
    
    
    public String debugTickStatus(){
        return this.debugMsg 
//            + "\n" + this.toStringEstimatedValues() 
            + "\n" + this.toStringFlowValues();
    }
    
    

    private BigDecimal sumMapValues(Map<?, BigDecimal> map){
        BigDecimal a = BigDecimal.ZERO;
        for(BigDecimal x : map.values()){
            a = a.add(x);
        }
        return a;
    }
    
     
    private String toStringMap(String label, Map<Integer, BigDecimal> m){
        StringBuffer sb = new StringBuffer("\t"+label+": ");
        for(Integer neighboorId : m.keySet()){
            sb.append(neighboorId);
            sb.append(" -> ");
            sb.append(m.get(neighboorId));
            sb.append("; ");
            
        }
        
        return sb.toString();
    }
    
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
    
    


    

    public String getState(){
/*        
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
            ApplicationFlowUpdatingDynamic neighborApp = (ApplicationFlowUpdatingDynamic) super.getComEngine().getNetwork().getNode(n).getApplication();
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
*/
        return "";
    }
    
    
    public BigDecimal getNeighborFlowValue(Integer neighborId){
        return this.stateFlows.get(neighborId);
    }
    
    public void setNeighborFlowValue(Integer neighborId, BigDecimal flowValue){
        this.stateFlows.put(neighborId, flowValue);
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
    
    
}

/**
 * Singleton class to store costume report data.
 * Implementation according to the classical singleton design pattern
 * Issues: not thread safe, sensible to multiple class loaders, not support serialize/deserialize sequences)
 * 
 * @author pcjesus
 *
 */
class CustomFUReport {
    private static CustomFUReport instance = null;
    
    private Map<Integer, Map<Integer, Integer>> fdMistakes;
    private Map<Integer, Map<Integer, Integer>> fdNotSuspected;
    private Map<Integer, Map<Integer, Integer>> fdIncorreclySuspected;
    private Map<Integer, Map<Integer, Integer>> fdWithMistakes;
    private Map<Integer, Map<Integer, Integer>> fdWithNotSuspected;
    private Map<Integer, Map<Integer, Integer>> fdWithIncorreclySuspected;
    
    private Map<Integer, Map<Integer, Integer>> qNumNodes;
    private Map<Integer, Map<Integer, Integer>> qNumNodesLeaveQuiescence;
    private Map<Integer, Map<Integer, Integer>> qPeriodsDistribution;
    private Map<Integer, Integer> qMaxPeriodByRep;
    private Map<Integer, Integer> qMinPeriodByRep;
    
    //Control variables
    private int currentTime;
    private int currentRepetition;
    
    private CustomFUReport() {
        // Exists only to defeat instantiation.
        
        //FD Data
        this.fdMistakes = new HashMap<Integer, Map<Integer, Integer>>();
        this.fdNotSuspected = new HashMap<Integer, Map<Integer, Integer>>();
        this.fdIncorreclySuspected = new HashMap<Integer, Map<Integer, Integer>>();
        this.fdWithMistakes = new HashMap<Integer, Map<Integer, Integer>>();
        this.fdWithNotSuspected = new HashMap<Integer, Map<Integer, Integer>>();
        this.fdWithIncorreclySuspected = new HashMap<Integer, Map<Integer, Integer>>();
        
        //Quiescence Data
        this.qNumNodes = new HashMap<Integer, Map<Integer, Integer>>();
        this.qNumNodesLeaveQuiescence = new HashMap<Integer, Map<Integer, Integer>>();
        this.qPeriodsDistribution = new HashMap<Integer, Map<Integer, Integer>>();
        this.qMaxPeriodByRep = new HashMap<Integer, Integer>();
        this.qMinPeriodByRep = new HashMap<Integer, Integer>();
        
        //Generic data
        this.currentTime = 0;
        this.currentRepetition = 0;
    }
    
    public static CustomFUReport getInstance() {
        if(instance == null) {
            instance = new CustomFUReport();
        }
        return instance;
    }
    
    
    public void initCustomFUReport(boolean useFD, boolean useQuiescence, int rep){
        
        if(useFD){
            this.initFDFUReport(rep);
        }
        
        if(useQuiescence){
            this.initQuiescenceFUReport(rep);
        }
        this.currentTime = -1;
    }
    
    private void initFDFUReport(int rep){
        if(this.fdMistakes.get(rep) == null){
            //Init data for the given repetition
            this.fdMistakes.put(rep, new HashMap<Integer, Integer>());
            this.fdNotSuspected.put(rep, new HashMap<Integer, Integer>());
            this.fdIncorreclySuspected.put(rep, new HashMap<Integer, Integer>());
            this.fdWithMistakes.put(rep, new HashMap<Integer, Integer>());
            this.fdWithNotSuspected.put(rep, new HashMap<Integer, Integer>());
            this.fdWithIncorreclySuspected.put(rep, new HashMap<Integer, Integer>());
            this.currentRepetition = rep;
        }
    }
    
    private void initQuiescenceFUReport(int rep){
        if(this.qNumNodes.get(rep) == null){
            //Init data for the given repetition
            this.qNumNodes.put(rep, new HashMap<Integer, Integer>());
            this.qNumNodesLeaveQuiescence.put(rep, new HashMap<Integer, Integer>());
            this.qPeriodsDistribution.put(rep, new HashMap<Integer, Integer>());
            this.qMaxPeriodByRep.put(rep, Integer.MIN_VALUE);
            this.qMinPeriodByRep.put(rep, Integer.MAX_VALUE);
            this.currentRepetition = rep;
        }
    }
    
    
    public void initTimeReportData(boolean useFD, boolean useQuiescence, int rep, int time){
        if(this.currentTime != time){
            this.currentTime = time;
            
            if(useFD){
                this.initFDTimeData(rep,time);
            }
            
            if(useQuiescence){
                this.initQuiescenceTimeData(rep,time);
            }
        }
    }
    
    
    private void initFDTimeData(int rep, int time){
            this.fdMistakes.get(rep).put(this.currentTime, 0);
            this.fdNotSuspected.get(rep).put(this.currentTime, 0);
            this.fdIncorreclySuspected.get(rep).put(this.currentTime, 0);
            this.fdWithMistakes.get(rep).put(this.currentTime, 0);
            this.fdWithNotSuspected.get(rep).put(this.currentTime, 0);
            this.fdWithIncorreclySuspected.get(rep).put(this.currentTime, 0);
    }
    
    
    private void initQuiescenceTimeData(int rep, int time){
        this.qNumNodes.get(rep).put(this.currentTime, 0);
        this.qNumNodesLeaveQuiescence.get(rep).put(this.currentTime, 0);
    }
    
    
    public void storeReport(boolean useFD, boolean useQuiescence, int repetitionToStore, int timeToStore, 
            int simulatonNumber, int repNum, String netType, 
            String outputDir, MathContext mc) throws IOException {
        
        //TODO Improve efficiency!!! Always saving to the file for all nodes (only the last matters)...
        
//        System.out.println("[DEBUG] (storeReport) - repetitionToStore: "+repetitionToStore+"; this.currentRepetition: "+this.currentRepetition
//                + "; timeToStore: "+timeToStore+ "; this.currentTime: "+this.currentTime);
        
        if(this.currentTime >= timeToStore){
            
//            System.out.println("[DEBUG] (storeReport) - Write reptition file!");
            
            if(useFD){
                this.storeFDRepetitionReport(repetitionToStore, timeToStore, simulatonNumber, repNum, netType, outputDir, mc);
            }
            
        }
        
        
        if(this.currentTime >= timeToStore && this.currentRepetition >= repetitionToStore){
            
//            System.out.println("[DEBUG] (storeReport) - Write AVERAGE file!");
            
            if(useFD){
                this.storeFDReport(repetitionToStore, timeToStore, simulatonNumber, repNum, netType, outputDir, mc);
            }
            
            if(useQuiescence){
                this.storeQuiescenceReport(repetitionToStore, timeToStore, simulatonNumber, repNum, netType, outputDir, mc);
            }
        }     
            
    }
    
    
    private void storeFDRepetitionReport(int repetitionToStore, int timeToStore, 
            int simulatonNumber, int repNum, String netType, 
            String outputDir, MathContext mc) throws IOException {
        
            //Create Mistakes plot file
            PlotData pdMistakes = new PlotData(simulatonNumber, "FDMistakes"+repNum);
            List<BigDecimal> xValues = new ArrayList<BigDecimal>(this.fdMistakes.size());
            List<BigDecimal> yValues = new ArrayList<BigDecimal>(this.fdMistakes.size());
            for(Map.Entry<Integer, Integer> e : this.fdMistakes.get(repNum).entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(new BigDecimal(e.getValue()));
            }
            pdMistakes.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDMistakes"+repNum, xValues, yValues);
        
            //Create NotSuspected plot file
            PlotData pdNotSuspected = new PlotData(simulatonNumber, "FDNotSuspected"+repNum);
            xValues = new ArrayList<BigDecimal>(this.fdNotSuspected.size());
            yValues = new ArrayList<BigDecimal>(this.fdNotSuspected.size());
            for(Map.Entry<Integer, Integer> e : this.fdNotSuspected.get(repNum).entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(new BigDecimal(e.getValue()));
            }
            pdNotSuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNotSuspected"+repNum, xValues, yValues);

            //Create IncorreclySuspected plot file
            PlotData pdIncorreclySuspected = new PlotData(simulatonNumber, "FDIncorreclySuspected"+repNum);
            xValues = new ArrayList<BigDecimal>(this.fdIncorreclySuspected.size());
            yValues = new ArrayList<BigDecimal>(this.fdIncorreclySuspected.size());
            for(Map.Entry<Integer, Integer> e : this.fdIncorreclySuspected.get(repNum).entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(new BigDecimal(e.getValue()));
            }
            pdIncorreclySuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDIncorreclySuspected"+repNum, xValues, yValues);

          //Create Mistakes plot file
            PlotData pdWithMistakes = new PlotData(simulatonNumber, "FDNodeMistakes"+repNum);
            xValues = new ArrayList<BigDecimal>(this.fdWithMistakes.size());
            yValues = new ArrayList<BigDecimal>(this.fdWithMistakes.size());
            for(Map.Entry<Integer, Integer> e : this.fdWithMistakes.get(repNum).entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(new BigDecimal(e.getValue()));
            }
            pdWithMistakes.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNodeMistakes"+repNum, xValues, yValues);
        
            //Create NotSuspected plot file
            PlotData pdWithNotSuspected = new PlotData(simulatonNumber, "FDNodeNotSuspected"+repNum);
            xValues = new ArrayList<BigDecimal>(this.fdWithNotSuspected.size());
            yValues = new ArrayList<BigDecimal>(this.fdWithNotSuspected.size());
            for(Map.Entry<Integer, Integer> e : this.fdWithNotSuspected.get(repNum).entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(new BigDecimal(e.getValue()));
            }
            pdWithNotSuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNodeNotSuspected"+repNum, xValues, yValues);

            //Create IncorreclySuspected plot file
            PlotData pdWithIncorreclySuspected = new PlotData(simulatonNumber, "FDNodeIncorreclySuspected"+repNum);
            xValues = new ArrayList<BigDecimal>(this.fdWithIncorreclySuspected.size());
            yValues = new ArrayList<BigDecimal>(this.fdWithIncorreclySuspected.size());
            for(Map.Entry<Integer, Integer> e : this.fdWithIncorreclySuspected.get(repNum).entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(new BigDecimal(e.getValue()));
            }
            pdWithIncorreclySuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNodeIncorreclySuspected"+repNum, xValues, yValues);

    }
    
    
    private void storeFDReport(int repetitionToStore, int timeToStore, 
                    int simulatonNumber, int repNum, String netType, 
                    String outputDir, MathContext mc) throws IOException {
            
            //Create Mistakes plot file
            PlotData pdMistakes = new PlotData(simulatonNumber, "FDMistakes");
            Map<Integer, BigDecimal> repAvg = calculateMeanValueMap(this.fdMistakes, this.currentTime, mc);
            List<BigDecimal> xValues = new ArrayList<BigDecimal>(repAvg.size());
            List<BigDecimal> yValues = new ArrayList<BigDecimal>(repAvg.size());
            for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(e.getValue());
            }
            pdMistakes.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDMistakes", xValues, yValues);
        
            //Create NotSuspected plot file
            PlotData pdNotSuspected = new PlotData(simulatonNumber, "FDNotSuspected");
            repAvg = calculateMeanValueMap(this.fdNotSuspected, this.currentTime, mc);
            xValues = new ArrayList<BigDecimal>(repAvg.size());
            yValues = new ArrayList<BigDecimal>(repAvg.size());
            for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(e.getValue());
            }
            pdNotSuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNotSuspected", xValues, yValues);

            //Create IncorreclySuspected plot file
            PlotData pdIncorreclySuspected = new PlotData(simulatonNumber, "FDIncorreclySuspected");
            repAvg = calculateMeanValueMap(this.fdIncorreclySuspected, this.currentTime, mc);
            xValues = new ArrayList<BigDecimal>(repAvg.size());
            yValues = new ArrayList<BigDecimal>(repAvg.size());
            for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(e.getValue());
            }
            pdIncorreclySuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDIncorreclySuspected", xValues, yValues);

            //Create Mistakes plot file
            PlotData pdWithMistakes = new PlotData(simulatonNumber, "FDNodeMistakes");
            repAvg = calculateMeanValueMap(this.fdWithMistakes, this.currentTime, mc);
            xValues = new ArrayList<BigDecimal>(repAvg.size());
            yValues = new ArrayList<BigDecimal>(repAvg.size());
            for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(e.getValue());
            }
            pdWithMistakes.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNodeMistakes", xValues, yValues);
        
            //Create NotSuspected plot file
            PlotData pdWithNotSuspected = new PlotData(simulatonNumber, "FDNodeNotSuspected");
            repAvg = calculateMeanValueMap(this.fdWithNotSuspected, this.currentTime, mc);
            xValues = new ArrayList<BigDecimal>(repAvg.size());
            yValues = new ArrayList<BigDecimal>(repAvg.size());
            for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(e.getValue());
            }
            pdWithNotSuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNodeNotSuspected", xValues, yValues);

            //Create IncorreclySuspected plot file
            PlotData pdWithIncorreclySuspected = new PlotData(simulatonNumber, "FDNodeIncorreclySuspected");
            repAvg = calculateMeanValueMap(this.fdWithIncorreclySuspected, this.currentTime, mc);
            xValues = new ArrayList<BigDecimal>(repAvg.size());
            yValues = new ArrayList<BigDecimal>(repAvg.size());
            for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
                xValues.add(new BigDecimal(e.getKey()));
                yValues.add(e.getValue());
            }
            pdWithIncorreclySuspected.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "FDNodeIncorreclySuspected", xValues, yValues);
    }
    
    
    private void storeQuiescenceReport(int repetitionToStore, int timeToStore, 
            int simulatonNumber, int repNum, String netType, 
            String outputDir, MathContext mc) throws IOException {
    
        //Create Num. of Quiescent nodes plot file
        PlotData pdQuiescent = new PlotData(simulatonNumber, "QNumNodes");
        Map<Integer, BigDecimal> repAvg = calculateMeanValueMap(this.qNumNodes, this.currentTime, mc);
        List<BigDecimal> xValues = new ArrayList<BigDecimal>(repAvg.size());
        List<BigDecimal> yValues = new ArrayList<BigDecimal>(repAvg.size());
        for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
            xValues.add(new BigDecimal(e.getKey()));
            yValues.add(e.getValue());
        }
        pdQuiescent.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "QNumNodes", xValues, yValues);

        //Create Num. of nodes leaving quiescence plot file
        PlotData pdNodesLeaveQuiescence = new PlotData(simulatonNumber, "QNumNodesLeaveQuiescence");
        repAvg = calculateMeanValueMap(this.qNumNodesLeaveQuiescence, this.currentTime, mc);
        xValues = new ArrayList<BigDecimal>(repAvg.size());
        yValues = new ArrayList<BigDecimal>(repAvg.size());
        for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
            xValues.add(new BigDecimal(e.getKey()));
            yValues.add(e.getValue());
        }
        pdNodesLeaveQuiescence.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "QNumNodesLeaveQuiescence", xValues, yValues);
        
        
        //Create Num. of nodes leaving quiescence plot file
        PlotData pdPeriodDistribution = new PlotData(simulatonNumber, "QPeriodDistribution");
        repAvg = calculateMeanDistribution(this.qPeriodsDistribution, mc);
        xValues = new ArrayList<BigDecimal>(repAvg.size());
        yValues = new ArrayList<BigDecimal>(repAvg.size());
        for(Map.Entry<Integer, BigDecimal> e : repAvg.entrySet()){
            xValues.add(new BigDecimal(e.getKey()));
            yValues.add(e.getValue());
        }
        pdPeriodDistribution.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "QPeriodDistribution", xValues, yValues);

        //Max period by repetition
        PlotData pdMaxPeriodByRep = new PlotData(simulatonNumber, "QMaxPeriodByRep");
        xValues = new ArrayList<BigDecimal>(this.qMaxPeriodByRep.size());
        yValues = new ArrayList<BigDecimal>(this.qMaxPeriodByRep.size());
        for(Map.Entry<Integer, Integer> e : this.qMaxPeriodByRep.entrySet()){
            xValues.add(new BigDecimal(e.getKey()));
            yValues.add(new BigDecimal(e.getValue()));
        }
        pdMaxPeriodByRep.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "QMaxPeriodByRep", xValues, yValues);

        //Min period by repetition
        PlotData pdMinPeriodByRep = new PlotData(simulatonNumber, "QMinPeriodByRep");
        xValues = new ArrayList<BigDecimal>(this.qMinPeriodByRep.size());
        yValues = new ArrayList<BigDecimal>(this.qMinPeriodByRep.size());
        for(Map.Entry<Integer, Integer> e : this.qMinPeriodByRep.entrySet()){
            xValues.add(new BigDecimal(e.getKey()));
            yValues.add(new BigDecimal(e.getValue()));
        }
        pdMinPeriodByRep.createGenericResult2DPlot(outputDir, netType, "FUReport", simulatonNumber, "QMinPeriodByRep", xValues, yValues);

 }
    
    
    
    private static Map<Integer, BigDecimal> calculateMeanValueMap(Map<Integer, Map<Integer, Integer>> allValuesByRepetition, int time, MathContext mc) {
        
        Map<Integer,BigDecimal> result = new HashMap<Integer, BigDecimal>();
        
        BigDecimal total[] = new BigDecimal[time+1];
        int count[] = new int[time+1];
        
        for(Integer validRep : allValuesByRepetition.keySet()){
            
            //Get values from all simulation repetitions
            Map<Integer, Integer> repValues = allValuesByRepetition.get(validRep);
            
            for(int i=0; i < repValues.size(); i++){
                if(total[i] != null){
                    total[i] = total[i].add(new BigDecimal(repValues.get(i)), mc);
                    count[i] = count[i] + 1;
                } else {
                    total[i] = new BigDecimal(repValues.get(i));
                    count[i] = 1;
                }
            }
            
        }
        
        for(int i=0; (i < total.length) && (total[i] != null); i++){
            result.put(i, total[i].divide(new BigDecimal(count[i]), mc));
        }
        
        return result;
    }
    
    
    private static Map<Integer, BigDecimal> calculateMeanDistribution(Map<Integer, Map<Integer, Integer>> allValuesByRepetition, MathContext mc) {
        
        Map<Integer,BigDecimal> result = new HashMap<Integer, BigDecimal>();
        
        BigDecimal total = BigDecimal.ZERO;
        
        //Sum count of all periods (of all repetitions)
        for(Integer validRep : allValuesByRepetition.keySet()){
            
            //Get values from all simulation repetitions
            Map<Integer, Integer> repValues = allValuesByRepetition.get(validRep);
            
            for(Map.Entry<Integer, Integer> periodData : repValues.entrySet()){
                BigDecimal prevCount = result.get(periodData.getKey());
                if(prevCount == null){
                    result.put(periodData.getKey(), new BigDecimal(periodData.getValue()));
                } else {
                    result.put(periodData.getKey(), prevCount.add(new BigDecimal(periodData.getValue())));
                }
                total = total.add(new BigDecimal(periodData.getValue()));
            }
            
        }
        
        
        
        //Average distribution values
        BigDecimal numRepetitions = new BigDecimal(allValuesByRepetition.size());
        BigDecimal avgTotal = total.divide(numRepetitions, mc);
        for(Map.Entry<Integer, BigDecimal> resultData : result.entrySet()){
            result.put(resultData.getKey(), (resultData.getValue().divide(numRepetitions, mc)).divide(avgTotal, mc));
        }
        
        return result;
    }
    
    
    public void addNotSuspected(Integer rep, Integer time, Integer value){
        Integer prevValue = this.fdNotSuspected.get(rep).get(time);
        this.fdNotSuspected.get(rep).put(time, prevValue + value);
    }
    
    public void addWithNotSuspected(Integer rep, Integer time){
        Integer prevValue = this.fdWithNotSuspected.get(rep).get(time);
        this.fdWithNotSuspected.get(rep).put(time, prevValue + 1);
    }
    
    public void addIncorreclySuspected(Integer rep, Integer time, Integer value){
        Integer prevValue = this.fdIncorreclySuspected.get(rep).get(time);
        this.fdIncorreclySuspected.get(rep).put(time, prevValue + value);
    }
    
    public void addWithIncorreclySuspected(Integer rep, Integer time){
        Integer prevValue = this.fdWithIncorreclySuspected.get(rep).get(time);
        this.fdWithIncorreclySuspected.get(rep).put(time, prevValue + 1);
    }
    
    public void addMistake(Integer rep, Integer time, Integer value){
        Integer prevValue = this.fdMistakes.get(rep).get(time);
        this.fdMistakes.get(rep).put(time, prevValue + value);
    }
    
    public void addWithMistake(Integer rep, Integer time){
        Integer prevValue = this.fdWithMistakes.get(rep).get(time);
        this.fdWithMistakes.get(rep).put(time, prevValue + 1);
    }
    
    
    public void incQuiescentNodes(Integer rep, Integer time) {
        Integer prevValue = this.qNumNodes.get(rep).get(time);
        this.qNumNodes.get(rep).put(time, prevValue + 1);
    }
    
    public void incNodesLeavingQuiescence(Integer rep, Integer time) {
        Integer prevValue = this.qNumNodesLeaveQuiescence.get(rep).get(time);
        this.qNumNodesLeaveQuiescence.get(rep).put(time, prevValue + 1);
    }
    
    public void setMaxPeriodByRep(Integer rep, Integer max) {
        Integer currentMax = this.qMaxPeriodByRep.get(rep);
        if(max > currentMax){
            this.qMaxPeriodByRep.put(rep, max);
        }
    }
    
    public void setMinPeriodByRep(Integer rep, Integer min) {
        Integer currentMin = this.qMinPeriodByRep.get(rep);
        if(min < currentMin){
            this.qMinPeriodByRep.put(rep, min);
        }
    }
    
    
    public void addQuiescencePeriod(Integer rep, Integer period) {
        Integer prevValue = this.qPeriodsDistribution.get(rep).get(period);
        if(prevValue == null){
            this.qPeriodsDistribution.get(rep).put(period, 1);
        } else {
            this.qPeriodsDistribution.get(rep).put(period, prevValue + 1);
        }
    }
    
 }
