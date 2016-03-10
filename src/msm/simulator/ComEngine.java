/**
 * MSM - Network Simulator
 */


package msm.simulator;


import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msm.simulator.Event.EventType;
import msm.simulator.apps.Application;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.ComEngineException;
import msm.simulator.exceptions.ConfigException;
import msm.simulator.exceptions.NumGenerationException;
import msm.simulator.network.Message;
import msm.simulator.network.Network;
import msm.simulator.network.Node;
import msm.simulator.util.NetStatistics;
import msm.simulator.util.NumGenerator;
import msm.simulator.util.NumGenerator.GenerationFunction;
import msm.simulator.util.SetUtils;

import org.apache.commons.math.MathException;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomGenerator;


/**
 * @author pjesus
 */
public class ComEngine {

    public static enum Model {Synchronous, Asynchronous};
    
    public static final String METHOD_INIT_COMMUNICATION = "InitCommunication";
    public static final String METHOD_COMMUNICATION = "Communication";

    public static final String PARAM_CLOUDS_NUM = "CLOUDS_NUM";
    public static final String PARAM_SEND_MSG_LIMIT = "SEND_MSG_LIMIT";
    public static final String PARAM_RCV_MSG_LIMIT = "RCV_MSG_LIMIT";
    public static final String PARAM_SEND_RCV_DEPENDENCY = "SEND_RCV_DEPENDENCY";
    public static final String PARAM_MSG_DISCARD = "MSG_DISCARD";
    public static final String PARAM_LOSS_PROBABILITY = "LOSS_PROBABILITY";
    public static final String PARAM_LOSS_AT_SENDER = "LOSS_AT_SENDER";
    
    public static final String PARAM_USE_OVERLAY = "USE_OVERLAY";
    public static final String PARAM_CREATE_APP_COMMUNICATION_GRAPH = "SHOW_CREATE_COMMUNICATION_GRAPH";
    public static final String PARAM_SHOW_APP_COMMUNICATION_GRAPH = "SHOW_APP_COMMUNICATION_GRAPH";
    
    public static final String PARAM_MSG_TRANSMISSION_TIME = "MSG_TRANSMISSION_TIME";
    public static final String PARAM_INIT_DELAY = "INIT_DELAY";
    
    public static final String CLOCK_EVENT_KEY_SEPARATOR = "/";

    private NetStatistics.AggFunctions aggFunction;
/*
    private int sendMsgLimit;
    private int rcvMsgLimit;
    private boolean sendRcvDependency;
    private boolean msgDiscard;
*/
    private float lossProbability;
    private boolean lossAtSender;
    private boolean useOverlay;
    private boolean createAppComGraph;
    private boolean showAppComGraph;
    
    //Number generator to compute messages transmission time
    private NumGenerator timeGenerator;
    
    //Number generator to compute the init delay time
    private NumGenerator initDelayGenerator;

//    private Random rndLoss;
    private RandomGenerator rndLoss;
    
    //Global time (round in the case of the synchronous execution)
    private int globalTime;
    
    //Indicate if the state of some application has changed
    private boolean appStateChanged; 

    private String appType;

    private Network net;

    private long msgCount;
    private Map<Integer, Integer> msgLatencies;
    private boolean registerMsgLatencies;
    
    private MathContext mc;
    
    //Scheduled Applications Events
    private ScheduledEvents appEvents;
    
    //Scheduled Internal Events
    //private ScheduledEvents internalEvents;
    
    private Model model;
    
    
    private static final String DEBUG_PROP  = "msm.simulator.ComEngine.DEBUG";
    public static final boolean useDebug;
    static {
        useDebug = Boolean.valueOf(System.getProperty(DEBUG_PROP, "false")).booleanValue();
    }

    long mt_debug = 0;
    
    public ComEngine() {

        this.msgCount = 0;
//        this.rndLoss = new Random();
        this.rndLoss = new MersenneTwister();
        this.appEvents = new ScheduledEvents();
        
    }    
    
    
    
    
    /**
     * Modeling the execution of a synchronous network model.
     * 
     * Execution begins with all processes in arbitrary state, and all channels empty.
     * The processes repeat the following steps, in lock-step:
     * - Apply the message-generation function to the current state
     *   (Generate message to be sent to all neighbors and put them in the appropriated channels) 
     * - Apply the state-transition function to the current state and incoming messages
     *   (obtain a new state and remove all messages from channels)
     * 
     * @see N. Lynch, Distributed Algorithms, Morgan Kaufmann
     */
    
    public void SynchronousInitCommunication(Network net, Config conf, Integer simulationIndex, Integer repetitionCount) 
    throws ClassNotFoundException, IllegalAccessException, InstantiationException, ComEngineException {

       this.globalTime = 0;
       this.appEvents.clear();
        
       // Load Specific parameters
       this.net = net;
       try {
           this.appType = conf.getValue(simulationIndex, Config.PARAM_APPLICATION_TYPE);
           //this.cloudsNum = Integer.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_CLOUDS_NUM));
           String func = conf.getValueWithDefault(Config.DEFAULT_FUNCTION, simulationIndex, Config.PARAM_APPLICATION_PARAM, Config.PARAM_FUNCTION);
           this.aggFunction = NetStatistics.AggFunctions.valueOf(func);
//           this.sendMsgLimit = Integer.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_SEND_MSG_LIMIT));
//           this.sendMsgLimit = 0;
//           this.rcvMsgLimit = Integer.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_RCV_MSG_LIMIT));
//           this.rcvMsgLimit = 0;
//           this.sendRcvDependency = Boolean.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_SEND_RCV_DEPENDENCY));
//           this.sendRcvDependency = false;
//           this.msgDiscard = Boolean.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_MSG_DISCARD));
//           this.msgDiscard = false;
           this.useOverlay = Boolean.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_USE_OVERLAY));
           this.createAppComGraph = Boolean.valueOf(conf.getValueWithDefault("true", simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_CREATE_APP_COMMUNICATION_GRAPH));
           this.showAppComGraph = Boolean.valueOf(conf.getValueWithDefault("false", simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_SHOW_APP_COMMUNICATION_GRAPH));
           this.lossProbability = Float.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_LOSS_PROBABILITY));
           this.lossAtSender = Boolean.valueOf(conf.getValueWithDefault("true", simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_LOSS_AT_SENDER));
           
           int mathPrecision = Integer.parseInt(conf.getValueWithDefault(Config.PARAM_MATH_PRECISION, Config.DEFAULT_MATH_PRECISION));
           RoundingMode mathRoundingMode = RoundingMode.valueOf(conf.getValueWithDefault(Config.PARAM_MATH_ROUNDING_MODE, Config.DEFAULT_MATH_ROUNDING_MODE));
           this.mc = new MathContext(mathPrecision, mathRoundingMode);
           
           //Create a constant time generator (for rounds to proceed one by one, in lock-step)
           this.timeGenerator = new NumGenerator(GenerationFunction.CONSTANT, null, "1");
           //Set initial delay configuration (for rounds to proceed one by one, in lock-step)
           this.initDelayGenerator = new NumGenerator(GenerationFunction.CONSTANT, null, "1");
           
           
       }catch (ConfigException ce){
           throw (ComEngineException)new ComEngineException(ce).initCause(ce);
       }

       // Init Network nodes n value
       for (Node n : net.getNodes()) {
           Application app = Application.createInstance(this.appType, n, this);
           try {
               app.init(conf, simulationIndex.toString(), repetitionCount.toString());
           }catch (ApplicationException ae){
               throw (ComEngineException)new ComEngineException(ae).initCause(ae);
           }
           n.init(app);
           
           //Schedule initial message generation event for the each node
//           try {
               //Note: always 1
               this.addApplicationEvent(1, String.valueOf(n.getId()), EventType.TICK, null);
               //this.addApplicationEvent(NumGenerator.generateInteger(this.initDelayConfig), String.valueOf(n.getId()), EventType.TICK, null);
//           }catch (NumGenerationException ne){
//               throw (ComEngineException)new ComEngineException(ne).initCause(ne);
//           } catch (MathException me) {
//               throw (ComEngineException)new ComEngineException(me).initCause(me);
//           }
       }
       
       //Reset application communication link status
       if(this.createAppComGraph){
           this.getNetwork().resetLinksAppStatus(this.useOverlay());
       }
       
       
       this.msgLatencies = new HashMap<Integer, Integer>();
       this.registerMsgLatencies = false;
       
       this.appStateChanged = false;
       
       //Init events for all nodes
       
       
   }
    
    public void SynchronousCommunication() 
        throws ComEngineException {
        
        
        if(this.globalTime == 0){
            // Init Network nodes state (depending from the all network first initialization)
            for (Node n : net.getNodes()) {
                n.getApplication().init2();
            }  
        }
        
        if(useDebug){
            System.out.println("\n\nCURRENT GLOBAL TIME: "+this.globalTime);
        }
        
        //Process next events
        Set<Event<?>> evtSet = this.appEvents.pullNext();
        
        if(useDebug){
            System.out.println("NEXT EVENTS TO PROCESS: ");
        }
        
        for(Event<?> e : evtSet){
            
            if(useDebug){
                System.out.println("\t\t -> "+e);
            }
            
            Integer nodeId = Integer.valueOf(e.getNodeID());
            
            //Only process event if node is not dead (due to churn)
            if(!this.net.isDeadNode(nodeId)){
                
                //Get event target node
                Node node = this.net.getNode(nodeId);
            
                //Handler events
                switch (e.getType()) {
                    case TICK:
                        //Invoke application message generation handler
                        node.getApplication().messageGeneration();
                        //Create TICK event
                        //this.addApplicationEvent(this.computeMsgTransmissionTime(), e.getNodeID(), EventType.TICK, null);
                        this.addApplicationEvent(1, e.getNodeID(), EventType.TICK, null);
                        break;
                    case MSG_LOSS:
                        //Increment message loss count
                        node.incMsgLossCount();
                        break;    
                    default:
                        throw new ComEngineException("Unknown event type: " + e.getType() +" -> " + e);
                }
            
            } //if not dead node
        }
        
        //State-transition of all nodes in the same round
        
        // Get all nodes IDs
        Set<Integer> nodesSet = this.net.getNodesKeySet();
        //Set<Integer> nodes = this.net.getNodesKeySet();

        // Randomize nodes Ids list
        List<Integer> nodes = SetUtils.sortSet(nodesSet);
        
        for(Integer nodeId : nodes){
            
            Node node = this.net.getNode(nodeId);
            
            //Get all received messages 
            List<Message<?>> msgsReceived = new ArrayList<Message<?>>(node.getReceivedMessages());
            node.incMsgRcvCount(msgsReceived.size());
            
            if(useDebug){
                System.out.println("\tNODE["+nodeId+"]: ");
                for(Message<?> msg : msgsReceived){
                    System.out.println("\t\tMSG -> "+msg);
                }
            }
            
            //Invoke application state transition handler
            node.getApplication().stateTransition(msgsReceived);
            
            if(useDebug){
                System.out.println(node.getApplication().debugTickStatus());
            }
        
            //Clear message channels (message receive buffer)
            node.clearMessageChannels();
        }
        
        // Update Global Time
        this.globalTime = this.appEvents.getTimeOfNextEvents();

    }
    
    
    /**
     * Modeling the execution of an asynchronous network model.
     * 
     * Event-driven simulation model, in which nodes take steps at arbitrary speeds.
     * Nodes computation time is not taken into consideration, but message can take an 
     * arbitrary time to be transmitted (variable delay according to the network characteristics).
     * Each node can possess a local clock, local clocks are not synchronized (different clock drifts).
     * 
     * Execution begins with all processes in arbitrary state, and all channels empty.
     * Each processes respond to the occurrence of events, updating it's state 
     * and/or triggering new events.
     * 
     * Main Events:
     * - MSG_RECEIVE : Event triggered by the reception of a message. 
     *      This event is scheduled by the node that sends the message.
     * - TICK : Event triggered by a timer (according to the node local clock), from a timeout, 
     *      to take a periodic action, or stop waiting for something. 
     * 
     */
    
    public void AsynchronousInitCommunication(Network net, Config conf, Integer simulationIndex, Integer repetitionCount) 
        throws ClassNotFoundException, IllegalAccessException, InstantiationException, ComEngineException {

       this.globalTime = 0;
       this.appEvents.clear();
        
       // Load Specific parameters
       this.net = net;
       try {
           //Application Instance Used
           this.appType = conf.getValue(simulationIndex, Config.PARAM_APPLICATION_TYPE);
           
           //Should be an application parameter (or report configuration)
           String func = conf.getValueWithDefault(Config.DEFAULT_FUNCTION, simulationIndex, Config.PARAM_APPLICATION_PARAM, Config.PARAM_FUNCTION);
           this.aggFunction = NetStatistics.AggFunctions.valueOf(func);
           
           //Not Used
/*           
           this.sendMsgLimit = 0;
           this.rcvMsgLimit = 0;
           this.sendRcvDependency = false;
           this.msgDiscard = false;
*/
           
           //Related with overlay (should be set elsewhere)
           this.useOverlay = Boolean.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_USE_OVERLAY));
           this.createAppComGraph = Boolean.valueOf(conf.getValueWithDefault("true", simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_CREATE_APP_COMMUNICATION_GRAPH));
           this.showAppComGraph = Boolean.valueOf(conf.getValueWithDefault("false", simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_SHOW_APP_COMMUNICATION_GRAPH));
           
           //Message Loss Settings
           this.lossProbability = Float.valueOf(conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_LOSS_PROBABILITY));
           this.lossAtSender = Boolean.valueOf(conf.getValueWithDefault("true", simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_LOSS_AT_SENDER));
           
           //Message Transmission time configuration
           String msgTransmissionTimeConfig = conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_MSG_TRANSMISSION_TIME);
           
           //Create the number generator used to compute message transmission time
           this.timeGenerator = new NumGenerator();
           this.timeGenerator.loadConfig(msgTransmissionTimeConfig);
           
           //Load initial delay configuration
           String initDelayConfig = conf.getValue(simulationIndex, Config.PARAM_COMMUNICATION_ENGINE_PARAM, PARAM_INIT_DELAY);
           
           //Create the number generator used to compute message transmission time
           this.initDelayGenerator = new NumGenerator();
           this.initDelayGenerator.loadConfig(initDelayConfig);
           
           //Precision settings (should be set before not specific of each simulation)
           int mathPrecision = Integer.parseInt(conf.getValueWithDefault(Config.PARAM_MATH_PRECISION, Config.DEFAULT_MATH_PRECISION));
           RoundingMode mathRoundingMode = RoundingMode.valueOf(conf.getValueWithDefault(Config.PARAM_MATH_ROUNDING_MODE, Config.DEFAULT_MATH_ROUNDING_MODE));
           this.mc = new MathContext(mathPrecision, mathRoundingMode);
           
       }catch (ConfigException ce){
           throw (ComEngineException)new ComEngineException(ce).initCause(ce);
       }

       // Init Network nodes n value
       for (Node n : net.getNodes()) {
           Application app = Application.createInstance(this.appType, n, this);
           try {
               app.init(conf, simulationIndex.toString(), repetitionCount.toString());
           }catch (ApplicationException ae){
               throw (ComEngineException)new ComEngineException(ae).initCause(ae);
           }
           n.init(app);
           
           //Schedule initial clock tick event for the each node, according to the defined initial delay
           try {
               int initDelay = this.initDelayGenerator.generateInteger();
               if(initDelay >= 1){
                   this.addApplicationEvent(initDelay, String.valueOf(n.getId()), EventType.TICK, null);
               } else {
                   throw new ComEngineException("Invalid Initial Delay! Computed value: "+initDelay);
               }
           } catch (MathException me) {
               throw (ComEngineException)new ComEngineException(me).initCause(me);
           }
       }
       
       //Init set of receiving nodes to control send and receive dependency
       //this.rcvNodes = new HashSet<Integer>();
       
       //Reset application communication link status
       if(this.createAppComGraph){
           this.getNetwork().resetLinksAppStatus(this.useOverlay());
       }
       
       this.msgLatencies = new HashMap<Integer, Integer>();
       this.registerMsgLatencies = false;
       
       this.appStateChanged = false;
       
   }
    
    public void AsynchronousCommunication() 
        throws ComEngineException {
        
        if(this.globalTime == 0){
            // Init Network nodes state (depending from the all network first initialization)
            for (Node n : net.getNodes()) {
                n.getApplication().init2();
            }
        }
        
        if(useDebug){
            System.out.println("\n\nCURRENT GLOBAL TIME: "+this.globalTime);
        }
        
        //Process next events
        Set<Event<?>> evtSet = this.appEvents.pullNext();
        
        if(useDebug){
            System.out.println("NEXT EVENTS TO PROCESS: ");
        }
        
        for(Event<?> e : evtSet){
            
            if(useDebug){
                System.out.println("\t\t -> "+e);
            }
            
            Integer nodeId = Integer.valueOf(e.getNodeID());
            
            //Only process event if node is not dead (due to churn)
            if(!this.net.isDeadNode(nodeId)){
            
                //Get event target node
                Node node = net.getNode(nodeId);
            
//            System.out.println("[DEBUG] - Process Event: "+e.getType()+"\tNode: "+e.getNodeID()+"\tGlobalTime: "+this.globalTime);
            
                //Handler events
                switch (e.getType()) {
                    case MSG_RECEIVE:
                        //Increment message receive count
                        node.incMsgRcvCount();
                        //Get received message from node buffer
                        String msgId = (String) e.getData();
                        Message<?> receivedMsg = node.getReceivedMessage(msgId);
                        if(useDebug){
                            System.out.println("\tNODE["+e.getNodeID()+"]: ");
                            System.out.println("\t\tMSG -> "+receivedMsg);
                        }
                        //Invoke message application handler
                        node.getApplication().onReceive(receivedMsg);
                        if(useDebug){
                            System.out.println(node.getApplication().debugOnReceiveStatus());
                        }
                        //Remove message from node buffer
                        node.delReceivedMessage(msgId);
                        break;
                    case TICK:
                        //Invoke clock tick application handler
                        node.getApplication().setLastEvt(e);
                        node.getApplication().onTick();
                        if(useDebug){
                            System.out.println(node.getApplication().debugTickStatus());
                        }
                        break;
                    case MSG_LOSS:
                        //Increment message loss count
                        node.incMsgLossCount();
                        break;
                    default:
                        throw new ComEngineException("Unknown event type: " + e.getType() +" -> " + e);
                }
                
            } // if not dead node
            
        }
        
        // Update Global Time
        this.globalTime = this.appEvents.getTimeOfNextEvents();
    }
    
    
    
    private int computeMsgTransmissionTime() throws ComEngineException{
        
        this.mt_debug++;
        
        int result;
        try {
            result = this.timeGenerator.generateInteger();
        } catch (MathException me) {
            throw (ComEngineException) new ComEngineException("Error generting transmission time!").initCause(me);
        }
        
        int retry = 0;
        while(result < 1 && retry < 1){
            try {
                result = this.timeGenerator.generateInteger();
            } catch (MathException me) {
                throw (ComEngineException) new ComEngineException("Error generting transmission time!").initCause(me);
            }
            retry++;
        }
        
        if(result >= 1){
            
            //Register message latencies statistics.
            if(this.registerMsgLatencies()){
                if(this.msgLatencies.containsKey(result)){
                    this.msgLatencies.put(result, this.msgLatencies.get(result) + 1);
                } else {
                    this.msgLatencies.put(result, 1);
                }
            }
            
            return result;
        } else {
            throw new ComEngineException("Invalid Transmission Time! Computed value (retries 1x): "+result+" - MT_DEBUG (1 retries): "+this.mt_debug);
        }
    }
    
    
    public void addApplicationEvent(int t, String nodeID, EventType evtType, Object data){
        
        int time = this.globalTime + t;
        int numEvts = this.appEvents.numberOfEventAtSameTime(time);
        
        //Create key
        StringBuilder sb = new StringBuilder();
        sb.append(time);
        sb.append(CLOCK_EVENT_KEY_SEPARATOR);
        sb.append(numEvts);
        
        
        //Generate Event (internal event)
        Event<Object> appEvt = new Event<Object>(time, nodeID, evtType, data, sb.toString()); 

        //Schedule event
        this.appEvents.add(appEvt);
        
    }


    public int sendMessage(Message<?> msg) throws ComEngineException {
        
        if ((this.lossProbability > 0.0) && this.toLose()){
            
            if(this.lossAtSender){

                //Get Sender
                int senderId = Integer.valueOf(msg.getFrom());
                Node sender = this.net.getNode(senderId);
                
                //Increment sender message lost count
                sender.incMsgLossCount();
                
            } else {
                
//TODO OPTIMIZATION: if in synchronous model, directly add 1 and avoid computing message transmission time (with constant value 1)
                
                //Add Message Loss Event at receiver
                this.addApplicationEvent(this.computeMsgTransmissionTime(), msg.getTo(), EventType.MSG_LOSS, msg);
                
            }
            
        } else {
            
            //Get Sender
            int senderId = Integer.valueOf(msg.getFrom());
            Node sender = this.net.getNode(senderId);
            //Increment message sent count
            sender.incMsgSendCount();
            
            //Set message sequence number e generate ID
            msg.setSeqNum(sender.getMsgSendCount());
            String msgId = this.generateMsgId(msg);
            
            //Do not create the message receive event 
            if(this.getModel() != Model.Synchronous){

                //Add Message Receive Event at receiver
                this.addApplicationEvent(this.computeMsgTransmissionTime(), msg.getTo(), EventType.MSG_RECEIVE, msgId);
            
            }
            
            //Increment Message Count
            this.incMsgCount();
            
            //Add message to target message receive list
            int receiverId = Integer.valueOf(msg.getTo());
            Node receiver = this.net.getNode(receiverId);
            msg.setMsgId(msgId);
            receiver.addReceiveMessage(msg);
            
            //Note: Message Rcv Count incremented elsewhere (before processing)
        }
        
        msg.setTime(this.getGlobalTime());
        return msg.getTime();
        
    }
    
    
/*    
    public void sendMessage(String from, String to, Object msgData) {
        
        //Create Message
        String msgId = generateMsgId(from, to);
        Message<Object> msg = new Message<Object>(msgId, from, to, this.getMsgCount(), msgData);
        
        //Get Sender
        int senderId = Integer.valueOf(from);
        Node sender = this.net.getNode(senderId);
        
        //Add Message to Send
        sender.addMessageToSend(msg);
        sender.setSender(true);
        
        //Increment Message Count
        this.incMsgCount();
    }
*/
    
/*    
    public void broadcastMessage(String from, Object msgData) {
        
        //Get Sender
        int senderId = Integer.valueOf(from);
        Node sender = this.net.getNode(senderId);
        
        //Get All Neighbors
        List<Integer> neighbors = sender.getLinks();
        
        //Send message to all Neighbors
        for(Integer to : neighbors){
            
            //Create Message
            String msgId = generateMsgId(from, to.toString());
            Message msg = new Message(msgId, from, to.toString(), this.getMsgCount(), msgData);
            
            //Add Message to Send
            sender.addMessageToSend(msg);
            sender.setSender(true);
        }
        
        //Increment Message Count
        this.incMsgCount();
        
    }
 */    
    
    public int broadcastMessage(Message<?> msg) throws ComEngineException {
        
        //Get Sender
        int senderId = Integer.valueOf(msg.getFrom());
        Node sender = this.net.getNode(senderId);
        
        if ((this.lossProbability > 0.0) && this.toLose()){

            if(this.lossAtSender){
                
                //Increment sender message lost count
                sender.incMsgLossCount();
                
            } else {
                
                //Get All Neighbors
                Set<Integer> neighbors;
                if(this.useOverlay){
                    neighbors = sender.getOverlayLinks();
                } else {
                    neighbors = sender.getLinks();
                }
                
                for(Integer to : neighbors){
                    
//TODO OPTIMIZATION: if in synchronous model, directly add 1 and avoid computing message transmission time (with constant value 1)
                    
                    //Add Message Loss Event at receiver
                    this.addApplicationEvent(this.computeMsgTransmissionTime(), to.toString(), EventType.MSG_LOSS, msg);
                }
                
                
            }
            
        } else {
            
            //Get All Neighbors
            Set<Integer> neighbors;
            if(this.useOverlay){
                neighbors = sender.getOverlayLinks();
            } else {
                neighbors = sender.getLinks();
            }
            
            //Increment message sent count
            sender.incMsgSendCount();
            
            //Set message sequence number e generate ID
            msg.setSeqNum(sender.getMsgSendCount());
            String msgId = this.generateMsgId(msg);
            
            //Generate Message Receive Event for all neighbors
            for(Integer to : neighbors){
                
                
                //Do not create the message receive event 
                if(this.getModel() != Model.Synchronous){
                
                    //Add Message Receive Event at receiver
                    this.addApplicationEvent(this.computeMsgTransmissionTime(), to.toString(), EventType.MSG_RECEIVE, msgId);
                
                }
                
                
                int receiverId = Integer.valueOf(to);
                Node receiver = this.net.getNode(receiverId);
                msg.setMsgId(msgId);
                receiver.addReceiveMessage(msg);
                
                //Note: Message Rcv Count incremented elsewhere (before processing)
                
            }
            
            //Increment Message Count
            this.incMsgCount();
        }
        
        msg.setTime(this.getGlobalTime());
        return msg.getTime();
        
    }
    
    
    /**
     * Create a clock event for the specified node, scheduled according to the defined timeout
     * 
     * @param timeout time to wait before triggering the event
     * @param nodeId Id of the node associated to the event
     * @param data internal data associated to the event (Useful when a node generate multiple events) 
     * 
     * @return key to identify of the node clock event
     * 
     * @throws ComEngineException
     */
    public String setTimeout(int timeout, String nodeId, Object data) throws ComEngineException {
        
        if(timeout > 0){
            
            int time = this.globalTime + timeout;
//            int numEvts = this.appEvents.numberOfSimilarEventAtSameTime(time, EventType.TICK, nodeId);
            int numEvts = this.appEvents.numberOfEventAtSameTime(time);
            
            //Create key
            StringBuilder sb = new StringBuilder();
            sb.append(time);
            sb.append(CLOCK_EVENT_KEY_SEPARATOR);
            sb.append(numEvts);
            
//            Event<String> clockEvt = new Event<String>(time, nodeId, EventType.TICK, String.valueOf(numEvts)); 
            Event<Object> clockEvt = new Event<Object>(time, nodeId, EventType.TICK, data, sb.toString()); 
            this.appEvents.add(clockEvt);
            
            //Return clock event key
            return sb.toString();
            
        } else {
            throw new ComEngineException("Invalid timeout value, must be greater than 0: "+timeout);
        }
        
    }
    
    
    /**
     * Remove the matching clock event (clock reset)
     * 
     * @param clockEvtKey Clock event key (specific to the node)
     * @param nodeId ID of the concerned node
     * 
     * @return true if the clock event exists (successfully removed), false otherwise
     */
    public boolean reset(String clockEvtKey, String nodeId){
        String[] keyValue = clockEvtKey.split(CLOCK_EVENT_KEY_SEPARATOR);
        int time = Integer.parseInt(keyValue[0]);
//        
//        Event<String> clockEvt = new Event<String>(time, nodeId, EventType.TICK, keyValue[1]); 
        Event<Object> clockEvt = new Event<Object>(time, nodeId, EventType.TICK, null, clockEvtKey); 
        return this.appEvents.removeEvent(clockEvt);
    }


/*
    public void broadcastMessage(String from, Object msgData) {
        
        //Get Sender
        int senderId = Integer.valueOf(from);
        Node sender = this.net.getNode(senderId);
        
        //Get All Neighbors
        Set<Integer> neighbors;
        if(this.useOverlay){
            neighbors = sender.getOverlayLinks();
        } else {
            neighbors = sender.getLinks();
        }
        
        //Create Message
        String msgId = generateMsgId(from, MessageBroadcast.TO_ANY);
        MessageBroadcast<Object> msg = new MessageBroadcast<Object>(msgId, from, this.getMsgCount(), msgData);
        
        //Add all Neighbors to Broadcast destination list
        for(Integer to : neighbors){
            msg.addBrodcastDestination(String.valueOf(to));
        }
        
        //Add Message to Send
        sender.addMessageToSend(msg);
        sender.setSender(true);
        
        //Increment Message Count
        this.incMsgCount();
        
    }
*/    
    
    private String generateMsgId(Message<?> msg){
        StringBuffer sb = new StringBuffer();
        //sb.append(this.getMsgCount());
        sb.append(msg.getSeqNum());
        sb.append("-");
        sb.append(msg.getFrom());
        sb.append("-");
        sb.append(msg.getTo());
        return sb.toString();
    }
    

    
    private boolean toLose(){
        float randVal = this.rndLoss.nextFloat();
        if(randVal <= this.lossProbability){
            return true;
        } else {
            return false;
        }
    }


    
    /**
     * @return Returns the msgCount.
     */
    public long getMsgCount() {
        return msgCount;
    }


    
    /**
     * @param msgCount The msgCount to set.
     */
    public void setMsgCount(long msgCount) {
        this.msgCount = msgCount;
    }
    
    
    public void incMsgCount() {
        this.msgCount++;
    }

    
    public Network getNetwork(){
        return this.net;
    }
    
    public MathContext getMathContext(){
        return this.mc;
    }
    
    public NetStatistics.AggFunctions getAggFunction(){
        return this.aggFunction;
    }
    
    /**
     * @return the useOverlay
     */
    public boolean useOverlay() {
        return this.useOverlay;
    }
    
    
    /**
     * @return the createAppComGraph
     */
    public boolean createAppComGraph() {
        return this.createAppComGraph;
    }
    
    /**
     * @return the showAppComGraph
     */
    public boolean showAppComGraph() {
        return this.showAppComGraph;
    }

    
    /**
     * @return the appType
     */
    public String getAppType() {
        return appType;
    }

    
    /**
     * @return the round
     */
    public int getGlobalTime() {
        return globalTime;
    }
    
    
    public void setModel(String model){
        this.model = Model.valueOf(model);
    }
    
    public Model getModel(){
        return this.model;
    }
    
    
    public boolean registerMsgLatencies(){
        return this.registerMsgLatencies;
    }
    
    public boolean setRegisterMsgLatencies(boolean registerMsgLatencies){
        return this.registerMsgLatencies = registerMsgLatencies;
    }




    
    /**
     * @return the msgLatencies
     */
    public Map<Integer, Integer> getMsgLatencies() {
        return this.msgLatencies;
    }
    
    public void resetMsgLatencies() {
        this.msgLatencies.clear();
    }
    
    
    /**
     * @return the appStateChanged
     */
    public boolean hasAppStateChanged() {
        return this.appStateChanged;
    }


    
    /**
     * @param appStateChanged the appStateChanged to set
     */
    public void setAppStateChanged(boolean appStateChanged) {
        this.appStateChanged = appStateChanged;
    }
    
    
}
