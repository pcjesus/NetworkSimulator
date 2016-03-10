/**
 * MSM - Network Simulator
 */
package msm.simulator.apps;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import msm.simulator.Config;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.ConfigException;
import msm.simulator.network.Message;
import msm.simulator.util.NetStatistics;



/**
 * Application Push-Pull Gossiping:
 *  Use a Random Tick foreach cycle.
 *  
 *  (2010/03/09) Add Synchronous Model support;
 *  (2010/03/11) Add restart mechanism to handle churn;
 *  
 * @author pjesus
 */

public class ApplicationPushPullGossiping extends Application {
    
    private static final String PARAM_TICK_TIMEOUT = "TICK_TIMEOUT";
    
    private static final String PARAM_CYCLE = "CYCLE";
    private static final String PARAM_CLOUDS_NUM = "CLOUDS_NUM";
    private static final String PARAM_EPOCH_LENGTH = "EPOCH_LENGTH";
    
    public static final String MSG_TYPE_PUSH = "N";
    public static final String MSG_TYPE_PULL = "S";
    
    private int cycle;
    private int cycleCount;
    private int tickCount;
    
    
    private BigDecimal estimate;
    
    //Cloud Number
    private int cloudsNum;
    private BigDecimal bdCloudsNum;
    
    //Epoch length (restart)
    private int epochLength;
    private int epoch;
    
    //Function to compute
    private NetStatistics.AggFunctions function;

    private boolean active;
    private String waitFor;
    
    private Map<Integer, BigDecimal> pullTargets;
//    private Map<Integer, BigDecimal> massTracker;
    
    //Received Message Buffer
    private Map<String, Message<?>> msgRcvBuffer;
    
    //Tick timeout Config
    private int tickTimeout;

    public ApplicationPushPullGossiping(){
        super();
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
                              
                case COUNT  : this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushPullGossiping.PARAM_CLOUDS_NUM)); 
                              this.bdCloudsNum = new BigDecimal(this.cloudsNum);
                              setInitCloudsValue(repetition);
                              break;
                default : 
                    System.err.println("DEBUG: Aggregation Function ["+this.function+"] NOT Supported.");
                    System.err.println("DEBUG: \t- Using COUNT Aggregation Function instead.");
                    this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushPullGossiping.PARAM_CLOUDS_NUM));
                    setInitCloudsValue(repetition);
            }
            
            this.tickTimeout = Integer.parseInt(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushPullGossiping.PARAM_TICK_TIMEOUT));
            
            this.cycle = Integer.parseInt(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushPullGossiping.PARAM_CYCLE));
            this.epochLength = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushPullGossiping.PARAM_EPOCH_LENGTH));
            super.getComEngine().getNetwork().setNumClouds(this.cloudsNum);
        
        }catch (ConfigException ce){
            throw (ApplicationException)new ApplicationException(ce).initCause(ce);
        }
        
        this.setEstimate(super.getInitValue());
        this.setValue(super.getInitValue());
        this.initState();
        
        this.cycleCount = this.cycle;
        Random randCycle = new Random();
        this.tickCount = randCycle.nextInt(this.cycle);
        
        this.epoch = (super.getComEngine().getGlobalTime() == 0)?0:((super.getComEngine().getGlobalTime() - 1) / this.getEpochLength()) + 1;
        
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
        this.active = false;
        this.waitFor = new String();
        
        this.pullTargets = new HashMap<Integer, BigDecimal>();
//        this.massTracker = new HashMap<Integer, BigDecimal>();
        
        //Clear message received buffer
        this.msgRcvBuffer = new HashMap<String, Message<?>>();
        
    }
   
    
    public void onReceive(Message<?> msg){
        
        //Add message to local buffer (overwrite existing message from the same source)
        this.msgRcvBuffer.put(msg.getFrom(), msg); 

/*        
        String msgData = (String)msg.getData();
        String elements[] = msgData.split("\\|");
        
        String msgType = elements[0];
        BigDecimal val = new BigDecimal(elements[1]);
        
        //Pull Message
        if(msgType.equals(MSG_TYPE_PULL)){
            
            //UPDATE
            this.update(val);
            
            this.setActive(false);
            this.setWaitFor(null);
            
        } else if(msgType.equals(MSG_TYPE_PUSH)) {
            
            //Create Message Data (format: TYPE|VALUE)
            StringBuffer sb = new StringBuffer(MSG_TYPE_PULL + "|");
            sb.append(this.getEstimate());
            String syncMsgData =  sb.toString();
        
            //Get Sender and Receiver
            String from = msg.getTo();
            String to = msg.getFrom();
        
            //SEND Pull MESSAGE
            super.sendMessage(from, to, syncMsgData);
            
            //UPDATE
            this.update(val);
            
        } else {
            System.err.println("DEBUG: Unknown Message Type");
        }
*/        
    }
    
    private void update(BigDecimal val) {
        this.setEstimate( (this.getEstimate().add(val, super.getMathContext())).divide(new BigDecimal(2), super.getMathContext()) );
        super.setValue(this.aggEstimate(this.getEstimate()));
    }
     
    
    public String debugOnReceiveStatus(){
        if(isActive()){
            StringBuffer sb = new StringBuffer("Value=");
            sb.append(super.getValue());
            sb.append("; Estimate=");
            sb.append(this.getEstimate());
            sb.append("; WaitFor=");
            sb.append(this.getWaitFor());
            sb.append(" - ACTIVE! \n");
            return sb.toString();
        } else {
            return "Value="+super.getValue();
        }
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
            super.setTimeout(this.tickTimeout);
        } catch (ApplicationException e) {
            System.err.println("ERROR SETTING TIMOUT: "+e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        
/*        
        this.cycleCount--;
        
        //Send new messages
        if(cycleCount == tickCount && !this.isActive()){
            
            //Create Message Data (format: TYPE|VALUE)
            StringBuffer sb = new StringBuffer(MSG_TYPE_PUSH + "|");
            sb.append(this.getEstimate());
            String syncMsgData =  sb.toString();
            
            //Get Sender and Receiver
            String from = String.valueOf(super.getAppNode().getId());
            //GETNEIGHBOR
            String to = super.getAppNode().selectRandomNeighboor().toString();
            
            //SEND Push Message
            super.sendMessage(from, to, syncMsgData);
            
            //Set Active Thread
            this.setActive(true);
            this.setWaitFor(to);
        }
        
        //Set New Cycle
        if(cycleCount==0){
            
            //Reset Cycle
            this.cycleCount = cycle;
            
            //Set tick in next cycle
            Random randCycle = new Random();
            this.tickCount = randCycle.nextInt(this.cycle);
        }
*/
    }
    
    
    public String debugTickStatus(){
        return "Value="+super.getValue()+"; Estimate="+this.getEstimate()+"; ACTIVE="+this.isActive()+"; cycleCount="+this.cycleCount+"; tickCount="+this.tickCount;
    }
  

    
    
    
    public void messageGeneration(){
        
        //Allow new nodes only to participate in the next epoch
        if(this.participate()){
        
            /*
             * Restart after at each epoch
             */
        
            if(!this.restart()){
        
                /*
                 * Detect/Process link changes
                 */
        
                this.detectChurn();
        
        
                //Set Sender (i)
                String from = String.valueOf(super.getAppNode().getId());
        
        
                //Generate all PULL Messages
                for(Integer targetID : this.pullTargets.keySet()){
            
                    //Create Message Data (format: TYPE|VALUE)
                    StringBuffer sb = new StringBuffer(MSG_TYPE_PULL + "|");
                    sb.append(this.pullTargets.get(targetID));
                    String syncMsgData =  sb.toString();
        
                    //Set Receiver
                    String to = targetID.toString();
        
                    //SEND Pull MESSAGE
                    super.sendMessage(from, to, syncMsgData);
                }
        
                //clear stored data of pull messages to send
                this.pullTargets.clear();
        
        
                /*
                 * Handle PUSH Message generation
                 */
        
                this.cycleCount--;
        
                //Send new messages
                if(cycleCount == tickCount && !this.isActive()){
            
                    //Create Message Data (format: TYPE|VALUE)
                    StringBuffer sb = new StringBuffer(MSG_TYPE_PUSH + "|");
                    sb.append(this.getEstimate());
                    String syncMsgData =  sb.toString();
            
                    //Get Receiver
                    //GETNEIGHBOR
                    String to = super.getAppNode().selectRandomNeighboor().toString();
            
                    //SEND Push Message
                    super.sendMessage(from, to, syncMsgData);
            
                    //Set Active Thread
                    this.setActive(true);
                    this.setWaitFor(to);
                }
        
                //Set New Cycle
                if(cycleCount==0){
            
                    //Reset Cycle
                    this.cycleCount = this.cycle;
            
                    //Set tick in next cycle
                    Random randCycle = new Random();
                    this.tickCount = randCycle.nextInt(this.cycle);
                }
                
            } //restart
        
        } //participate
        
    }
    
    
    public void stateTransition(Collection<Message<?>> msgsReceived){
        
        //Allow new nodes only to participate in the next epoch
        if(this.participate()){
        
            /*
             * Process all received messages
             */
        
            for (Message<?> msg : msgsReceived){
            
                //Message format: <type(PULL/PUSH)>|<value>
                String msgData = (String)msg.getData();
                String elements[] = msgData.split("\\|");
                            
                String msgType = elements[0];
                BigDecimal val = new BigDecimal(elements[1]);
            
                //Pull Message
                if(msgType.equals(MSG_TYPE_PULL)){
                
                    //UPDATE
                    this.update(val);
                
                    this.setActive(false);
                    this.setWaitFor(null);
                
                } else if(msgType.equals(MSG_TYPE_PUSH)) {
                
                    //Store data to further generate PULL messages 
                    Integer senderId = Integer.valueOf(msg.getFrom());
                    this.pullTargets.put(senderId, this.getEstimate());
//                this.massTracker.put(senderId, this.massDifference(val));
                
                    //UPDATE
                    this.update(val);
                
                } else {
                    System.err.println("DEBUG: Unknown Message Type");
                }
            
            }
        
        }
       
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
    
    
    private void detectChurn(){
        
        //Detect leaving nodes
        Set<Integer> leaving = super.getAppNode().getLeavingNodes();
        
        //Handle leaving/crash nodes
        if(leaving.size() > 0){
            processLeavingNeighbor(leaving);
            super.getAppNode().clearLeavingNodes();
        }
        
        //Arriving nodes are implicitly handled when initialized
        super.getAppNode().clearArrivingNodes();
        
    }
    
    
    private void processLeavingNeighbor(Set<Integer> nodes){
        
        for(Integer nodeID : nodes){          
            
            //If target of a PUSH fails... 
            if(this.isActive() && (Integer.valueOf(this.getWaitFor()).equals(nodeID))){
                
                //just stop waiting for the PULL message (no mass restore required)
                this.setWaitFor(null);
                this.setActive(false);
            }
            
            //If target of a PULL fails...
            if(this.pullTargets.keySet().contains(nodeID)){
                
                //do not send the PULL message (early detection avoid need for mass restore, but is unrealistic)
                this.pullTargets.remove(nodeID);
                
            }

/*            
            //If target of a PULL fails...
            if(this.massTracker.keySet().contains(nodeID)){
                
                //restore mass
                this.restore(this.massTracker.get(nodeID));
                
            }
*/
            
        }
        
//        this.massTracker.clear();
        
    }
    
/*    
    private BigDecimal massDifference(BigDecimal val) {
        return (super.getValue().subtract(val, super.getMathContext())).divide(new BigDecimal(2), super.getMathContext());
    }

/*    
    private void restore(BigDecimal mass) {
        super.setValue(super.getValue().add(mass, super.getMathContext()));
    }
*/
    
    /**
     * Restart algorithm according to the epoch length
     */
    private boolean restart(){
        if( (super.getComEngine().getGlobalTime() > 1) && (super.getComEngine().getGlobalTime() % this.getEpochLength()) == 0){
//            System.out.println("\tRESTART at round :"+super.getComEngine().getRound());
            this.setEstimate(super.getInitValue());
            super.setValue(super.getInitValue());
            this.initState();
            
            return true;
        } else {
            
            return false;
        }
    }
    
    
    private boolean participate(){
        if( (super.getComEngine().getGlobalTime() / this.getEpochLength()) >= this.epoch){
            return true;
        } else {
//            System.out.println("\t"+super.getComEngine().getRound()+" - NOT PARTICIPATE only at epoch:"+this.epoch);
            return false;
        }
    }
    

    
    
    /**
     * @return the epochLength
     */
    public int getEpochLength() {
        return epochLength;
    }





    /**
     * @return Returns the active.
     */
    private boolean isActive() {
        return active;
    }


    
    /**
     * @param active The active to set.
     */
    private void setActive(boolean active) {
        this.active = active;
    }

    
    /**
     * @return Returns the waitFor.
     */
    private String getWaitFor() {
        return waitFor;
    }


    
    /**
     * @param waitFor The waitFor to set.
     */
    private void setWaitFor(String waitFor) {
        this.waitFor = waitFor;
    }


    public String getState(){
        return String.valueOf(super.getValue());
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
