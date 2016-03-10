/**
 * 
 */
package msm.simulator;


/**
 * Event scheduled by an asynchronous simulation
 * 
 * @author pcjesus
 *
 */
public class Event<D> {
    
    public enum EventType {TICK, MSG_RECEIVE, MSG_LOSS, CHURN, VALUE_CHANGE};
    
    //Time the event is scheduled to occur
    private int time;
    //ID of the node associated to the event
    private String nodeID;
    //Type of the event
    private EventType type;
    //Data associated to the event
    private D data;
    //Event ID (may not uniquely identify event...)
    private String id;

    
    /**
     * Constructor 
     * 
     * @param time Time the event will occur;
     * @param nodeID Node associated to the event;
     * @param type Event Type;
     * @param data Data associated to the event;
     */
    public Event(int time, String nodeID, EventType type, D data){
        this.time = time;
        this.nodeID = nodeID!=null?new String(nodeID):null;
        this.type = type;
        this.data = data;
        this.id = null;
    }
    
    /**
     * Constructor 
     * 
     * @param time Time the event will occur;
     * @param nodeID Node associated to the event;
     * @param type Event Type;
     * @param data Data associated to the event;
     * @param id key associated to the event
     */
    public Event(int time, String nodeID, EventType type, D data, String id){
        this.time = time;
        this.nodeID = nodeID!=null?new String(nodeID):null;
        this.type = type;
        this.data = data;
        this.id = id;
    }
    
    
    public boolean match(Event<?> event){
        
        if(event.getId() != null){
            return this.id.equals(event.getId());
        } else if(event.getData() != null){
            return ( (this.time == event.getTime()) 
                    && (this.type == event.getType()) 
                    && (this.nodeID.equals(event.getNodeID())) 
                    && (this.data.equals(event.getData())) );
        } else {
            return ( (this.time == event.getTime()) 
                    && (this.type == event.getType()) 
                    && (this.nodeID.equals(event.getNodeID())) );
        }
    }
    

    /**
     * GETTERS / SETTERS
     */

    
    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }


    
    /**
     * @return the nodeID
     */
    public String getNodeID() {
        return nodeID;
    }


    
    /**
     * @return the type
     */
    public EventType getType() {
        return type;
    }
    
    
    
    /**
     * @return the data
     */
    public D getData() {
        return data;
    }


    
    /**
     * @param data the data to set
     */
    public void setData(D data) {
        this.data = data;
    }
    
    
    /**
     * @return the id
     */
    public String getId() {
        return this.id;
    }


    
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder("Event[type=");
        sb.append(this.getType());
        sb.append("; time=");
        sb.append(this.getTime());
        sb.append("; nodeID=");
        sb.append(this.getNodeID());
        sb.append("; data=");
        sb.append(this.getData());
        sb.append("; Id=");
        sb.append(this.getId());
        return sb.toString();
    }

    
    
}
