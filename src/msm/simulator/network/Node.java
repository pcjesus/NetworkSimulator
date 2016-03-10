/**
 * MSM - Network Simulator
 */


package msm.simulator.network;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import msm.simulator.apps.Application;


/**
 * Node Class
 * 
 * @author pjesus
 * @version 1.0
 */

public class Node implements Serializable {

   /**
     * 
     */
    private static final long serialVersionUID = 8512568557761711198L;

     // Node Identifier
    private int id;

    // Node X position
    private double x;

    // Node Y Position
    private double y;
    
    // Node eccentricity;
    private int eccentricity;
    
    // Node Links
    private Set<Integer> physicalLinks;
    
    // Node Overlay Links
    private Set<Integer> overlayLinks;

    private boolean connected;
    
    private BigDecimal dataValue;
    
    // Application
    private Application app;
    
    //Message to Send
    //private LinkedHashMap<String, Message<?>> msgToSend;
    
    //Message Received
    private Map<String, Message<?>> msgReceived;
    
    //private boolean isSender;
    //private boolean isReceiver;
    
    private long msgSendCount; 
    private long msgRcvCount;
    //private long msgDiscardSendCount;
    //private long msgDiscardRcvCount;
    private long msgLossCount;

    
    private Set<Integer> arrivingNodes;
    private Set<Integer> leavingNodes;
    
    
    /**
     * Constructors
     */
    
    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.physicalLinks = new HashSet<Integer>();
        this.overlayLinks = new HashSet<Integer>();
        this.connected = false;
        
        this.arrivingNodes = new HashSet<Integer>();
        this.leavingNodes = new HashSet<Integer>();
        
        this.init(null);
    }
    
    
    public Node(Node node){
        this.id = node.getId();
        this.x = node.getX();
        this.y = node.getY();
        this.eccentricity = node.getEccentricity();
        
        this.physicalLinks = new HashSet<Integer>();
        for(Integer id : node.getLinks()){
            this.physicalLinks.add(new Integer(id));
        }
        
        this.overlayLinks = new HashSet<Integer>();
        for(Integer id : node.getOverlayLinks()){
            this.overlayLinks.add(new Integer(id));
        }
        
        this.connected = node.isConnected();
        
        this.dataValue = new BigDecimal(node.getDataValue().doubleValue());
        
        this.arrivingNodes = new HashSet<Integer>();
        for(Integer id : node.getArrivingNodes()){
            this.arrivingNodes.add(new Integer(id));
        }
        
        this.leavingNodes = new HashSet<Integer>();
        for(Integer id : node.getLeavingNodes()){
            this.leavingNodes.add(new Integer(id));
        }
        
        //NOTE: Remaining data is set by init method (ready to simulate an application)
        this.init(null);
    }
    
    
    public void init(Application app){
        this.setApplication(app);
//        this.msgToSend = new LinkedHashMap<String, Message<?>>();
        this.msgReceived = new LinkedHashMap<String, Message<?>>();
//        this.isSender = false;
//        this.isReceiver = false;
        this.msgSendCount = 0;
        this.msgRcvCount = 0;
//        this.msgDiscardSendCount = 0;
//        this.msgDiscardRcvCount = 0;
        this.msgLossCount = 0;
    }

    

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }


    /**
     * @param id The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }


    /**
     * @return Returns the x.
     */
    public double getX() {
        return x;
    }


    /**
     * @param x The x to set.
     */
    public void setX(double x) {
        this.x = x;
    }


    /**
     * @return Returns the y.
     */
    public double getY() {
        return y;
    }


    /**
     * @param y The y to set.
     */
    public void setY(double y) {
        this.y = y;
    }
    
    
    public boolean existLink(int id){
        return this.physicalLinks.contains(new Integer(id));
    }
    
    
    public void addLink(int id){
        this.physicalLinks.add(new Integer(id));
    }
    
    public boolean delLink(Integer id){
        boolean result = this.physicalLinks.remove(id);
        if(this.physicalLinks.isEmpty()){
            this.disconnect();
        }
        
        return result;
    }

    
    public void clearAllLinks(){
        this.physicalLinks.clear();
        this.overlayLinks.clear();
        this.disconnect();
    }
    
    public Set<Integer> getLinks(){
        return this.physicalLinks;
    }
    
    public void setLinks(Set<Integer> links){
        this.physicalLinks = links;
    }
    
//    public Integer getLinkedNode(int index){
//        return this.physicalLinks.get(index);
//    }
    
    public void connect(){
        this.connected = true;
    }
    
    public boolean isConnected(){
        return this.connected;
    }
    
    public void disconnect(){
        this.connected = false;
    }
    
    
    public boolean existOverlayLink(int id){
        return this.overlayLinks.contains(new Integer(id));
    }
    
    
    public void addOverlayLink(int id){
        this.overlayLinks.add(new Integer(id));
    }
    
    public void delOverlayLink(Integer id){
        this.overlayLinks.remove(id);
    }
    
    public Set<Integer> getOverlayLinks(){
        return this.overlayLinks;
    }
    
    public void setOverlayLinks(Set<Integer> links){
        this.overlayLinks = links;
    }
    
//    public Integer getOverlayLinkedNode(int index){
//        return this.overlayLinks.get(index);
//    }
    
    
    public void setApplication(Application app){
        this.app = app;
    }
    
    
    public Application getApplication(){
        return this.app;
    }
    
    
    /**
     * Convert node data to String (to generate a Graph description file)
     * 
     * @return String representing the information of the node (to use in a graph description file)
     */
    public String toStringGraph() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.id);
        sb.append("[ pos = \"");
        
        int normValue = 50;
        //Normalize X value (Multiply by "normValue") to be visible in graph image
        sb.append(this.x*normValue);
        sb.append(",");
        
        //Normalize Y value (Multiply by "normValue") to be visible in graph image
        sb.append(this.y*normValue);
        sb.append("!\"];\n");
        return sb.toString();
    }


    
    
    /**
     * Send To
     * Method to simulate data send over network.
     *
     */
/*    public void sendTo(Node other, Message<?> msg){
        
        other.addReceiveMessage(msg);
        other.setReceiver(true);
        
        this.delMessageToSend(msg.getMsgId());
        if(this.numberOfMessageToSend() == 0){
            this.setSender(false);
        }
        
        this.incMsgSendCount();
    }
*/    
    
    
    /**
     * Send Broadcast
     * Method to simulate data broadcast over network.
     *
     */
/*    public void sendBroadcast(Network net, Message<?> msg, boolean useOverlay){
        
        Set<Integer> links;
        if(useOverlay){
            links = this.getOverlayLinks();
        } else {
            links = this.getLinks();
        }
        
        for(Integer neighbor : links){
            
            //Get neighbor Node
            Node other = net.getNode(neighbor);
            other.addReceiveMessage(msg);
            other.setReceiver(true);
            
        }

        this.delMessageToSend(msg.getMsgId());
        if(this.numberOfMessageToSend() == 0){
            this.setSender(false);
        }
        
        this.incMsgSendCount();
    }
*/    
 
    /**
     * Receive
     * Method to simulate message simulation.
     *
     */
/*    
    public void receive(Message<?> msg){
        
        this.app.onReceive(msg);
        this.delReceivedMessage(msg.getMsgId());
        if(this.numberOfReceivedMessages() == 0){
            this.setReceiver(false);
        }
        
        this.incMsgRcvCount();
        
    }
*/    
    
    /**
     * Lose Message at Sender
     * Method to simulate lost of message over network (at the sender)
     *
     */
/*    public void loseMsgSent(Message<?> msg){
        
        this.delMessageToSend(msg.getMsgId());
        if(this.numberOfMessageToSend() == 0){
            this.setSender(false);
        }
        
        this.incMsgLossCount();
    }
*/    
    
    /**
     * Lose Message at Receiver
     * Method to simulate lost of message over network (at the receiver)
     *
     */
 /*   public void loseMsgReceived(Message<?> msg){
        
        this.delReceivedMessage(msg.getMsgId());
        if(this.numberOfReceivedMessages() == 0){
            this.setReceiver(false);
        }
        
        this.incMsgLossCount();
    }
*/    
    
    public String debugOnReceiveStatus(){
        return this.app.debugOnReceiveStatus();
    }
    
    

    
    public int numberOfLinks(){
        return this.physicalLinks.size();
    }
    
    public int numberOfOverlayLinks(){
        return this.overlayLinks.size();
    }

    
    public List<Integer> selectRandomNeighboors(int numberOfLinks) {
        List <Integer> randNodes = new ArrayList<Integer>(this.physicalLinks);
        Collections.shuffle(randNodes);
        if(numberOfLinks > this.numberOfLinks()){
            numberOfLinks = this.numberOfLinks();
        }
        List <Integer> result = randNodes.subList(0, numberOfLinks);
        return result;
    }
    
    
    public Integer selectRandomNeighboor() {
        List <Integer> randNodes = new ArrayList<Integer>(this.physicalLinks);
        Collections.shuffle(randNodes);
        Integer result = randNodes.get(0);
        return result;
    }
    
    
    public List<Integer> selectRandomOverlayNeighboors(int numberOfLinks) {
        List <Integer> randNodes = new ArrayList<Integer>(this.overlayLinks);
        Collections.shuffle(randNodes);
        if(numberOfLinks > this.numberOfOverlayLinks()){
            numberOfLinks = this.numberOfOverlayLinks();
        }
        List <Integer> result = randNodes.subList(0, numberOfLinks);
        return result;
    }
    
    
    public Integer selectRandomOverlayNeighboor() {
        List <Integer> randNodes = new ArrayList<Integer>(this.overlayLinks);
        Collections.shuffle(randNodes);
        Integer result = randNodes.get(0);
        return result;
    }
    
    

/*    
    public void tick(){
        this.app.onTick();
    }
*/    
/*    
    public String debugTickStatus(){
        return this.app.debugTickStatus();
    }
*/    

 
/*    
    public void addMessageToSend(Message<?> msg){
        this.msgToSend.put(msg.getMsgId(), msg);
    }
    
    public void delMessageToSend(String msgId){
        this.msgToSend.remove(msgId);
    }
    
    public int numberOfMessageToSend(){
        return this.msgToSend.size();
    }
    
    public Collection<Message<?>> getMessagesToSend(){
        return this.msgToSend.values();
    }    
    
    public void clearMessagesToSend(){
        this.incMsgDiscardSendCountBy(this.numberOfMessageToSend());
        this.msgToSend.clear();
        this.setSender(false);
    }
*/
    
    public void addReceiveMessage(Message<?> msg){
        this.msgReceived.put(msg.getMsgId(), msg);
    }
    
    public void delReceivedMessage(String msgId){
        this.msgReceived.remove(msgId);
    }
    
    public int numberOfReceivedMessages(){
        return this.msgReceived.size();
    }
    
    public Collection<Message<?>> getReceivedMessages(){
        return this.msgReceived.values();
    }
    
    
    public Message<?> getReceivedMessage(String msgId){
        return this.msgReceived.get(msgId);
    }
    
/*
    public void clearReceiveMessages(){
        this.incMsgDiscardRcvCountBy(this.numberOfReceivedMessages());
        this.msgReceived.clear();
        this.setReceiver(false);
    }
*/    
    
    public void clearMessageChannels(){
        this.msgReceived.clear();
//        this.setReceiver(false);
    }

    
    /**
     * @return Returns the isReceiver.
     */
/*    public boolean isReceiver() {
        return isReceiver;
    }

    
    /**
     * @param isReceiver The isReceiver to set.
     */
/*    public void setReceiver(boolean isReceiver) {
        this.isReceiver = isReceiver;
    }

    
    /**
     * @return Returns the isSender.
     */
/*    public boolean isSender() {
        return isSender;
    }

    
    /**
     * @param isSender The isSender to set.
     */
/*    public void setSender(boolean isSender) {
        this.isSender = isSender;
    }
*/

	public long getMsgSendCount() {
		return msgSendCount;
	}


	public void setMsgSendCount(long msgSendCount) {
		this.msgSendCount = msgSendCount;
	}
	
	public void incMsgSendCount() {
		this.msgSendCount++;
	}

/*
	public LinkedHashMap<String, Message<?>> getMsgToSend() {
		return msgToSend;
	}


	public void setMsgToSend(LinkedHashMap<String, Message<?>> msgToSend) {
		this.msgToSend = msgToSend;
	}
*/

	public long getMsgRcvCount() {
		return msgRcvCount;
	}


	public void setMsgRcvCount(long msgRcvCount) {
		this.msgRcvCount = msgRcvCount;
	}
	
	
	public void incMsgRcvCount() {
		this.msgRcvCount++;
	}
	
	
	public void incMsgRcvCount(long msgRcvCount) {
        this.msgRcvCount+=msgRcvCount;
    }


	public Map<String, Message<?>> getMsgReceivedMap() {
		return msgReceived;
	}


	public void setMsgReceivedMap(Map<String, Message<?>> msgReceived) {
		this.msgReceived = msgReceived;
	}

/*    
    public long getMsgDiscardSendCount() {
        return msgDiscardSendCount;
    }


    public void setMsgDiscardSendCount(long msgDiscardSendCount) {
        this.msgDiscardSendCount = msgDiscardSendCount;
    }
    
    
    public void incMsgDiscardSendCount() {
        this.msgDiscardSendCount++;
    }
    
    
    public void incMsgDiscardSendCountBy(long value) {
        this.msgDiscardSendCount+=value;
    }
    
    public long getMsgDiscardRcvCount() {
        return msgDiscardRcvCount;
    }


    public void setMsgDiscardRcvCount(long msgDiscardRcvCount) {
        this.msgDiscardRcvCount = msgDiscardRcvCount;
    }
    
    
    public void incMsgDiscardRcvCount() {
        this.msgDiscardRcvCount++;
    }
    
    
    public void incMsgDiscardRcvCountBy(long value) {
        this.msgDiscardRcvCount+=value;
    }
*/    
    
    public long getMsgLossCount() {
        return msgLossCount;
    }


    public void setMsgLossCount(long msgLossCount) {
        this.msgLossCount = msgLossCount;
    }
    
    
    public void incMsgLossCount() {
        this.msgLossCount++;
    }
    
    
    public void incMsgLossCountBy(long value) {
        this.msgLossCount+=value;
    }


    
    /**
     * @return the dataValue
     */
    public BigDecimal getDataValue() {
        return dataValue;
    }


    
    /**
     * @param dataValue the dataValue to set
     */
    public void setDataValue(BigDecimal dataValue) {
        this.dataValue = dataValue;
    }

    
    
    /**
     * @return the arrivingNodes
     */
    public Set<Integer> getArrivingNodes() {
        return arrivingNodes;
    }
    
    public void addArrivingNode(int id){
        this.arrivingNodes.add(new Integer(id));
    }
    
    public void clearArrivingNodes(){
        this.arrivingNodes.clear();
    }


    
    /**
     * @return the leavingNodes
     */
    public Set<Integer> getLeavingNodes() {
        return leavingNodes;
    }
    
    public void addLeavingNode(int id){
        this.leavingNodes.add(new Integer(id));
    }
    
    public void clearLeavingNodes(){
        this.leavingNodes.clear();
    }
    
    
    


    public boolean equals(Node n){
        if(n.getId() == this.id){
            return true;
        } else {
            return false;
        }
    }
    
    public boolean equals(int id){
        if(id == this.id){
            return true;
        } else {
            return false;
        }
    }
    
    public int compareTo(Node n){
        return this.id - n.getId();
    }


    
    /**
     * @return the eccentricity
     */
    public int getEccentricity() {
        return eccentricity;
    }


    
    /**
     * @param eccentricity the eccentricity to set
     */
    public void setEccentricity(int eccentricity) {
        this.eccentricity = eccentricity;
    }
    
    
    
    
}
