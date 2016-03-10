/**
 * MSM - Network Simulator
 */
package msm.simulator.apps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import msm.simulator.Config;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.ConfigException;
import msm.simulator.network.Message;
import msm.simulator.util.NetStatistics;



/**
 * Application Push-Synopse (with share of 0.5 -> Push-Sum Protocol):
 *  Use a Random Tick foreach cycle.
 *  
 * @author pjesus
 */

public class ApplicationPushSynopse extends Application {
    
    private static final String PARAM_TICK_TIMEOUT = "TICK_TIMEOUT";
    
//    private static final String PARAM_CYCLE = "CYCLE";
    private static final String PARAM_CLOUDS_NUM = "CLOUDS_NUM";
    private static final String PARAM_SHARE = "SHARE";
    
    public static final String MSG_TYPE_PUSH = "N";
    public static final String DEFAULT_SHARE = "0.5";
    
//    private int cycle;
//    private int cycleCount;
//    private int tickCount;
    
    //Function to compute
    private NetStatistics.AggFunctions function;
    
    //Cloud Number
    private int cloudsNum;
    private BigDecimal bdCloudsNum;
    
    //Value Sum
    private BigDecimal s;
    
    //Weight
    private BigDecimal w;
    
    private BigDecimal estimate;
    
    //Share
    private float share;
    
    //Sum Buffer
    private ArrayList<BigDecimal> bufS = new ArrayList<BigDecimal>();
    
    //Weight Buffer
    private ArrayList<BigDecimal> bufW = new ArrayList<BigDecimal>();
    

    //Received Message Buffer
    private Map<String, Message<?>> msgRcvBuffer;
    
    //Tick timeout Config
    private int tickTimeout;
    

    public ApplicationPushSynopse(){
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
                              
                case COUNT  : this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushSynopse.PARAM_CLOUDS_NUM)); 
                              this.bdCloudsNum = new BigDecimal(this.cloudsNum);
                              setInitCloudsValue(repetition);
                              break;
                default : 
                    System.err.println("DEBUG: Aggregation Function ["+this.function+"] NOT Supported.");
                    System.err.println("DEBUG: \t- Using COUNT Aggregation Function instead.");
                    this.cloudsNum = Integer.valueOf(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushSynopse.PARAM_CLOUDS_NUM));
                    setInitCloudsValue(repetition);
            }
            
            this.tickTimeout = Integer.parseInt(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushSynopse.PARAM_TICK_TIMEOUT));
            
//            this.cycle = Integer.parseInt(config.getValue(index, Config.PARAM_APPLICATION_PARAM, ApplicationPushSynopse.PARAM_CYCLE));
            this.share = Float.valueOf(config.getValueWithDefault(DEFAULT_SHARE, index, Config.PARAM_APPLICATION_PARAM, ApplicationPushSynopse.PARAM_SHARE));
            
            super.getComEngine().getNetwork().setNumClouds(this.cloudsNum);
        }catch (ConfigException ce){
            throw (ApplicationException)new ApplicationException(ce).initCause(ce);
        }
        
        super.setValue(super.getInitValue());
        this.setEstimate(super.getInitValue());
        this.initState();
                
//        super.setValue(w.divide(s, super.getMathContext()));
        
//        this.cycleCount = this.cycle;
//        Random randCycle = new Random();
//        this.tickCount = randCycle.nextInt(this.cycle);
        
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
        
        //TODO Switch s and w
        this.s = BigDecimal.ONE; //new BigDecimal(this.cloudsNum); // @todo ERRO? (x = BigDecimal.ONE)
        this.w = super.getInitValue();
        
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
        BigDecimal valX = new BigDecimal(elements[1]);
        BigDecimal valW = new BigDecimal(elements[2]);
        
        //Push Message
        if(msgType.equals(MSG_TYPE_PUSH)){
            
            //Add received sum value to buffer
            bufS.add(valX);
            
            //Add received weight value to buffer
            bufW.add(valW);
            
        } else {
            System.err.println("DEBUG: Unknown Message Type");
        }
*/        
        
    }
    
    

    
    public String debugOnReceiveStatus(){
        StringBuffer sb = new StringBuffer("Value=");
        sb.append(super.getValue());
        sb.append("; bufS=");
        sb.append(this.bufS);
        sb.append("; bufW=");
        sb.append(this.bufW);
        
        return sb.toString();
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
        if(cycleCount == tickCount){
            
            //Process all received data
            this.s = this.sumBufferValues(bufS);
            this.w = this.sumBufferValues(bufW);
            bufS.clear();
            bufW.clear();
            
            //set new estimate
            super.setValue(s.divide(w, super.getMathContext()));
            
            
            //Determine number of targets (use equal shares)
            int nTargets;
            if(super.getComEngine().useOverlay()){
                nTargets = super.getAppNode().numberOfOverlayLinks() + 1;
            } else {
                nTargets = super.getAppNode().numberOfLinks() + 1;
            }
            if(this.share != 0.0){
                nTargets = (int) (1/this.share);
            }
            
            //Calculate values to send
            BigDecimal tempX = this.s.divide(new BigDecimal(nTargets), super.getMathContext());
            BigDecimal tempW = this.w.divide(new BigDecimal(nTargets), super.getMathContext());
            
            //Create Message Data (format: TYPE|VALUE|WEIGHT)
            StringBuffer sb = new StringBuffer(MSG_TYPE_PUSH + "|");
            sb.append(tempX);
            sb.append("|");
            sb.append(tempW);
            String msgData =  sb.toString();
            
            //Get Sender and Receivers
            String from = String.valueOf(super.getAppNode().getId());
            List<Integer> targets;
            if(super.getComEngine().useOverlay()){
                targets = super.getAppNode().selectRandomOverlayNeighboors(nTargets-1);
            } else {
                targets = super.getAppNode().selectRandomNeighboors(nTargets-1);
            }
            
            //SEND Push Messages (to itself and selected neighbors)
            super.sendMessage(from, from, msgData);
            for(Integer to : targets){
                super.sendMessage(from, to.toString(), msgData);
            }
            
            
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
    
    
    public void messageGeneration(){
        
        //Determine number of targets (use equal shares)
        int nTargets;
        if(super.getComEngine().useOverlay()){
            nTargets = super.getAppNode().numberOfOverlayLinks() + 1;
        } else {
            nTargets = super.getAppNode().numberOfLinks() + 1;
        }
        if(this.share != 0.0){
            nTargets = (int) (1/this.share);
        }
        
        //Calculate values to send
        BigDecimal tempX = this.s.divide(new BigDecimal(nTargets), super.getMathContext());
        BigDecimal tempW = this.w.divide(new BigDecimal(nTargets), super.getMathContext());
        
        //Create Message Data (format: TYPE|VALUE|WEIGHT)
        StringBuffer sb = new StringBuffer(MSG_TYPE_PUSH + "|");
        sb.append(tempX);
        sb.append("|");
        sb.append(tempW);
        String msgData =  sb.toString();
        
        //Get Sender and Receivers
        String from = String.valueOf(super.getAppNode().getId());
        List<Integer> targets;
        if(super.getComEngine().useOverlay()){
            targets = super.getAppNode().selectRandomOverlayNeighboors(nTargets-1);
        } else {
            targets = super.getAppNode().selectRandomNeighboors(nTargets-1);
        }
        
        //SEND Push Messages (to itself and selected neighbors)
        super.sendMessage(from, from, msgData);
        for(Integer to : targets){
            super.sendMessage(from, to.toString(), msgData);
        }
        
    }
    
    
    public void stateTransition(Collection<Message<?>> msgsReceived){
        
        //Process all received messages
        for (Message<?> msg : msgsReceived){
            
            String msgData = (String)msg.getData();
            String elements[] = msgData.split("\\|");
            
            String msgType = elements[0];
            BigDecimal valX = new BigDecimal(elements[1]);
            BigDecimal valW = new BigDecimal(elements[2]);
            
            //Push Message
            if(msgType.equals(MSG_TYPE_PUSH)){
                
                //Add received sum value to buffer
                bufS.add(valX);
                
                //Add received weight value to buffer
                bufW.add(valW);
                
            } else {
                System.err.println("DEBUG: Unknown Message Type");
            }    
        }
        
        
        //Process buffered data
        this.s = this.sumBufferValues(bufS);
        this.w = this.sumBufferValues(bufW);
        bufS.clear();
        bufW.clear();
        
        //set new estimate
        this.setEstimate(this.estimate());
        super.setValue(this.aggEstimate(this.getEstimate()));
        
    }
    
    private BigDecimal estimate(){
        
        return this.w.divide(this.s, super.getMathContext());
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
    
    
    public String debugTickStatus(){
        return "Value="+super.getValue()+"; S="+this.s+"; W="+this.w; //+"; cycleCount="+this.cycleCount+"; tickCount="+this.tickCount;
    }



    public String getState(){
        return String.valueOf(super.getValue());
    }
    
    
    private BigDecimal sumBufferValues(ArrayList<BigDecimal> buf){
        BigDecimal a = BigDecimal.ZERO;
        for(BigDecimal x : buf){
            a = a.add(x);
        }
        return a;
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
