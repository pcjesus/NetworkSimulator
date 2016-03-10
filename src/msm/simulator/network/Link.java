/**
 * MSM - Network Simulator
 */
package msm.simulator.network;

import java.io.Serializable;
import java.util.Collection;


/**
 * @author pjesus
 * @version 1.0
 *
 */
public class Link implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8723938041238366706L;
    
    public static final int NOT_CONNECTED = 0;
    public static final int UNIDIRECTIONAL = 1;
    public static final int BIDIRECTIONAL = 2;
    
    private Node n1;
    private Node n2;
    private int status;
    private int appStatus;
    
    
    
    /**
     * @param n1
     * @param n2
     * @param status
     */
    public Link(Node n1, Node n2, int status) {
        this.n1 = n1;
        this.n2 = n2;
        this.status = status;
        this.appStatus = this.status;
    }
    
    
    public Link(Node n1, Node n2, int status, int appStatus) {
        this.n1 = n1;
        this.n2 = n2;
        this.status = status;
        this.appStatus = appStatus;
    }
    
    
    public Link(Link l) {
        this.n1 = new Node(l.getNode1());
        this.n2 = new Node(l.getNode2());
        this.status = l.getStatus();
        this.appStatus = l.getAppStatus();
    }
    
    
    /**
     * Convert link data to String (to generate a Graph description file)
     * 
     * @return String representing the information of the link (to use in a graph description file)
     */
    public String toStringGraph() {
        StringBuffer sb = new StringBuffer("\n  ");
        sb.append(this.n1.getId());
        sb.append(" -- ");
        sb.append(this.n2.getId());
        switch (status) {
            case Link.NOT_CONNECTED:
                sb.append(" [color=red4, style=dotted];");
                break;
            case Link.UNIDIRECTIONAL:
                sb.append(" [color=blue4, style=bold];");
                break;
            case Link.BIDIRECTIONAL:
                sb.append(" [color=black, style=bold];");
                break;
            default:
                sb.append(";");
                break;
        }
        return sb.toString();
    }
    
    
    public String toStringAppGraph() {
        StringBuffer sb = new StringBuffer("\n  ");
        sb.append(this.n1.getId());
        sb.append(" -- ");
        sb.append(this.n2.getId());
        switch (appStatus) {
            case Link.NOT_CONNECTED:
                sb.append(" [color=red, style=dotted];");
                break;
            case Link.UNIDIRECTIONAL:
                sb.append(" [color=blue, style=bold];");
                break;
            case Link.BIDIRECTIONAL:
                sb.append(" [color=black, style=bold];");
                break;
            default:
                sb.append(";");
                break;
        }
        return sb.toString();
    }


    
    /**
     * @return Returns the n1.
     */
    public Node getNode1() {
        return n1;
    }


    
    /**
     * @param n1 The n1 to set.
     */
    public void setNode1(Node n1) {
        this.n1 = n1;
    }


    
    /**
     * @return Returns the n2.
     */
    public Node getNode2() {
        return n2;
    }


    
    /**
     * @param n2 The n2 to set.
     */
    public void setNode2(Node n2) {
        this.n2 = n2;
    }


    
    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    
    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    
    public void incStatus() {
        if(this.status < 2){
            this.status++;
        }
    }
    
    public void decStatus() {
        if(this.status > 0){
            this.status--;
        }
    }
    
    /**
     * @return the status
     */
    public int getAppStatus() {
        return appStatus;
    }

    
    /**
     * @param status the status to set
     */
    public void setAppStatus(int appStatus) {
        this.appStatus = appStatus;
    }
    
    
    public void incAppStatus() {
        if(this.appStatus < 2){
            this.appStatus++;
        }
    }
    
    public void decAppStatus() {
        if(this.appStatus > 0){
            this.appStatus--;
        }
    }
    
    public boolean equals(Node n1, Node n2){
        if( (n1.equals(this.n1) && n2.equals(this.n2)) || (n2.equals(this.n1) && n1.equals(this.n2))){
            return true;
        } else {
            return false;
        }
    }
    
    public boolean equals(int n1, int n2){
        if( (this.n1.equals(n1) && this.n2.equals(n2)) || (this.n2.equals(n1) && this.n1.equals(n2))){
            return true;
        } else {
            return false;
        }
    }
    
    
    public boolean contains(Node n){
        if( n.equals(this.n1) || n.equals(this.n2) ){
            return true;
        } else {
            return false;
        }
    }
    
    public boolean contains(int n){
        if( this.n1.equals(n) || this.n2.equals(n) ){
            return true;
        } else {
            return false;
        }
    }
    
    
    public boolean contains(Collection<Integer> lNodes){
        
        for(Integer nodeId : lNodes){
            if( this.n1.equals(nodeId) || this.n2.equals(nodeId) ){
                return true;
            }
        }
        
        return false;
    }
    
    
}
