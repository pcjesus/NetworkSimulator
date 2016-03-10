/**
 * 
 */
package msm.simulator.network;


/**
 * @author pjesus
 *
 */
public class Message<DATA> {
    
    private String msgId;
    
    private String from;
    private String to;
    private long seqNum;
    private DATA data;
    
    private int time; //send/creation time
    
    
    /**
     * Constructors
     */
    
    public Message(){
    }
    
    public Message(String from, String to, DATA data){
        this.from = from;
        this.to = to;
        this.data = data;
    }
    
    
    public Message(String msgId, String from, String to, long seqNum, DATA data){
        this.msgId = msgId;
        this.from = from;
        this.to = to;
        this.seqNum = seqNum;
        this.data = data;
    }

    
    /**
     * @return Returns the data.
     */
    public Object getData() {
        return data;
    }

    
    /**
     * @param data The data to set.
     */
    public void setData(DATA data) {
        this.data = data;
    }

    
    /**
     * @return Returns the from.
     */
    public String getFrom() {
        return from;
    }

    
    /**
     * @param from The from to set.
     */
    public void setFrom(String from) {
        this.from = from;
    }

    
    /**
     * @return Returns the msgId.
     */
    public String getMsgId() {
        return msgId;
    }

    
    /**
     * @param msgId The msgId to set.
     */
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    
    /**
     * @return Returns the seqNum.
     */
    public long getSeqNum() {
        return seqNum;
    }

    
    /**
     * @param seqNum The seqNum to set.
     */
    public void setSeqNum(long seqNum) {
        this.seqNum = seqNum;
    }

    
    /**
     * @return Returns the to.
     */
    public String getTo() {
        return to;
    }

    
    /**
     * @param to The to to set.
     */
    public void setTo(String to) {
        this.to = to;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer(this.getClass().getSimpleName()+"[ID=");
        sb.append(this.getMsgId());
        sb.append(";TO=");
        sb.append(this.getTo());
        sb.append(";FROM=");
        sb.append(this.getFrom());
        sb.append(";SEQN=");
        sb.append(this.getSeqNum());
        sb.append(";TIME=");
        sb.append(this.getTime());
        sb.append(";DATA=");
        sb.append(this.getData());
        sb.append("]");
        return sb.toString(); 
    }

    
    /**
     * @return the time
     */
    public int getTime() {
        return time;
    }

    
    /**
     * @param time the time to set
     */
    public void setTime(int time) {
        this.time = time;
    }
    
}
