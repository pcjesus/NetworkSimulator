/**
 * 
 */
package msm.simulator.network;

import java.util.ArrayList;
import java.util.List;


/**
 * @author pcjesus
 *
 */
public class MessageBroadcast<DATA> extends Message<DATA> {
    
    public static final String TO_ANY = "ANY";
    
    private List<String> toList;
    
    /**
     * Constructors
     */
    
    public MessageBroadcast(){
        super();
    }
    
    public MessageBroadcast(String msgId, String from, long seqNum, DATA data){
        super(msgId, from, TO_ANY, seqNum, data);
        this.toList = new ArrayList<String>();
    }
    
    
    public MessageBroadcast(String msgId, String from, List<String> to, long seqNum, DATA data){
        super(msgId, from, TO_ANY, seqNum, data);
        this.toList = new ArrayList<String>(to);
    }
    
    
    /**
     * Add brodcast destination
     * @param to
     */
    public void addBrodcastDestination(String to){
        this.toList.add(to);
    }
    
    /**
     * Get Broadcast destination size
     * 
     * @return
     */
    public int numberOfDestinations() {
        return this.toList.size();
    }
    
    
    public String toString(){
        StringBuffer sb = new StringBuffer(this.getClass().getSimpleName()+"[ID=");
        sb.append(this.getMsgId());
        sb.append(";TO=");
        sb.append(this.getTo());
        sb.append("(");
        sb.append(this.toList);
        sb.append(")");
        sb.append(";FROM=");
        sb.append(this.getFrom());
        sb.append(";SEQN=");
        sb.append(this.getSeqNum());
        sb.append(";DATA=");
        sb.append(this.getData());
        sb.append("]");
        return sb.toString(); 
    }
    
    

}
