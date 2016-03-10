/**
 * MSM - Network Simulator
 */


package msm.simulator.network;


import java.util.List;
import java.util.Random;
import java.util.Set;

import msm.simulator.Config;
import msm.simulator.util.SetUtils;

/**
 * Network Class
 * 
 * @author pjesus
 * @version 1.0
 */
public class NetworkRing extends Network {
    
    
    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 5314160023575258217L;
    
    
    private static final String NET_TYPE_RING = "Ring";

    /**
     * Constructors
     */
    
    public NetworkRing(){
        super();
    }
    

    public NetworkRing(int num_nodes) {
        super(num_nodes);
    }
    
    public NetworkRing(NetworkRing net){
        super(net);
    }
    
    
    public Object clone(){
        return new NetworkRing(this);
    }

    public void init(String type, Config config){
        super.init(type, config);
    }

    
    public void generateNetwork() {
        
        super.setType(NET_TYPE_RING);
        
        //Generate Random Nodes
        Random randPos = new Random();
        for (int i = 0; i < super.getNumNodes(); i++) {
            double x = randPos.nextDouble();
            double y = randPos.nextDouble();
            super.addNode(new Integer(i), new Node(i, x, y));
        }
        
        //Get all nodes IDs
        Set<Integer> nodesSet = super.getNodesKeySet();
        
        //Randomize nodes list
        List<Integer> rNodes = SetUtils.randomizeSet(nodesSet);
        
        //Link nodes as a chain
        for(int i=1; i < rNodes.size(); i++){
            Node n1 = super.getNode(rNodes.get(i-1));
            Node n2 = super.getNode(rNodes.get(i));
            addLink(n1, n2, Link.BIDIRECTIONAL);
            
            n1.connect();
            n2.connect();
        }
        
        //Link chain extremities
        Node n1 = super.getNode(rNodes.get(0));
        Node n2 = super.getNode(rNodes.get(rNodes.size()-1));
        addLink(n1, n2, Link.BIDIRECTIONAL);

    }
    
    
    
    /**
     * Connect node to the network (establish links).
     */
    protected void connectNode(Integer id) {
        
        //Select Random contact point
        Node contactNode = super.selectRandomNode();
        
        //Establish links between contact node and one of it's neighbors
        Integer[] links = contactNode.getLinks().toArray(new Integer[contactNode.numberOfLinks()]);
        Integer neighborId = links[0];
        super.delLink(contactNode.getId(), neighborId);
        contactNode.addLeavingNode(neighborId);
        super.getNode(neighborId).addLeavingNode(contactNode.getId());
        super.addLink(id, contactNode.getId(), Link.BIDIRECTIONAL);
        contactNode.addArrivingNode(id);
        super.addLink(id, neighborId, Link.BIDIRECTIONAL);
        super.getNode(neighborId).addArrivingNode(id);
            
        //Set node to connected
        super.getNode(id).connect();
    }
    
    
    /**
     * Re-connect node to the network (establish link).
     * NOTE: Use avoid special nodes from been disconnected (e.g. clouds).
     */
    protected void reconnectNode(Integer id) {
        
        //Select Random contact point
        Node contactNode = super.selectRandomNode();
        
        //Establish links
        if(contactNode.numberOfLinks() == 0){
            
            super.addLink(id, contactNode.getId(), Link.BIDIRECTIONAL);
            contactNode.addArrivingNode(id);
            
        } else {
            
            //Connect between contact node and one of it's neighbors
            Integer[] links = contactNode.getLinks().toArray(new Integer[contactNode.numberOfLinks()]);
            Integer neighborId = links[0];
            super.delLink(contactNode.getId(), neighborId);
            contactNode.addLeavingNode(neighborId);
            super.getNode(neighborId).addLeavingNode(contactNode.getId());
            super.addLink(id, contactNode.getId(), Link.BIDIRECTIONAL);
            contactNode.addArrivingNode(id);
            super.addLink(id, neighborId, Link.BIDIRECTIONAL);
            super.getNode(neighborId).addArrivingNode(id);
        }
        
        //Set node to connected
        super.getNode(id).connect();
    }

    
    public String getReportPrefixName(){
        return super.getType() + "-" + super.getInitNumNodes();
    }
    
    
    public String toString(){
        return "Network [type: "+ super.getType() +"; size: "+ super.getNumNodes() +"]";
    }

}
