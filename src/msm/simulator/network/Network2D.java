/**
 * MSM - Network Simulator
 */


package msm.simulator.network;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import msm.simulator.Config;

/**
 * Network Class
 * 
 * @author pjesus
 * @version 1.0
 */
public class Network2D extends Network {
    
    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 1680467755074454635L;
    
    
    
    private static final String NET_TYPE_2D = "2D";
    
    
    private float radius;
    

    /**
     * Constructors
     */
    
    public Network2D(){
        super();
    }
    

    public Network2D(int num_nodes) {
        super(num_nodes);
    }
    
    public Network2D(Network2D net){
        super(net);
        this.radius = net.getRadius();
    }
    
    
    public Object clone(){
        return new Network2D(this);
    }

    
    public void init(String type, Config config){
        super.init(type, config);
        this.radius = Float.valueOf(config.getValue(Config.PARAM_NETWORK_RADIUS));
    }
    
    
    /**
     * @return Returns the r.
     */
    public float getRadius() {
        return radius;
    }

    
    /**
     * @param r The r to set.
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void generateNetwork() {
        
        float r = this.getRadius();
        
        super.setType(NET_TYPE_2D);
        
        
        //Generate Random Nodes
        Random randPos = new Random();
        for (int i = 0; i < super.getNumNodes(); i++) {
            double x = randPos.nextDouble();
            double y = randPos.nextDouble();
            super.addNode(new Integer(i), new Node(i, x, y));
        }
        
        //Generate 2D links
        double dist = r / Math.sqrt(super.getNumNodes());
        //double dist = r / super.getNumNodes();
        for (int i = 0; i < super.getNumNodes(); i++) {
            Node n1 = super.getNode(i);
            for(int j = i+1; j < super.getNumNodes(); j++){
                Node n2 = super.getNode(j);
                if (super.calcNodesDistance(n1, n2) < dist){
                //if (super.sqrDistance(n1, n2) < dist){    
                    addLink(n1, n2, Link.BIDIRECTIONAL);
                }
            }
        }
        
        //Connect Network Graph
        this.connectGraph();

    }
    
    
    
    /** @todo Melhorar Algoritmo **/
    public void connectGraph(){
        
        Map<Integer, Node> nn = new HashMap<Integer, Node>();
        Random randNode = new Random();
        while(nn.size() < (super.getNumNodes()/2)){
            Map<Integer, Node> pending = new HashMap<Integer, Node>();
            int randN = randNode.nextInt(super.getNumNodes());
            pending.put(new Integer(randN), super.getNode(randN));
            //nn.clear();
            nn = new HashMap<Integer, Node>(pending);
            while(pending.size() > 0){
                Iterator<Node> it = pending.values().iterator();
                Node n1 = it.next();
                pending.remove(new Integer(n1.getId()));
                for(Integer n2 : n1.getLinks()){
                    if(!nn.containsKey(n2)){
                        nn.put(n2, super.getNode(n2));
                        pending.put(n2, super.getNode(n2));
                    }
                }
            }
            for(Node n : nn.values()){
                n.connect();
            }
            for(Node n1: super.getNodes()){
                if(!n1.isConnected()){
                    for(Integer l : n1.getLinks()){
                        if(super.getNode(l).isConnected()){
                            n1.connect();
                            break;
                        }
                    }
                    if(!n1.isConnected()){
                       double d = Double.MAX_VALUE;
                       Node n = null;
                       for(Node n2: super.getNodes()){
                           double d2 = this.calcNodesDistance(n1, n2);
                           if((d2 < d) && n2.isConnected()){
                               n = n2;
                               d = d2;
                               
                           }
                           
                       }
                       super.addLink(n1, n, Link.BIDIRECTIONAL);
                       n1.connect();
                    }
                }
            }
        }
        
        /**
         * Escolher metade dos nodos
         *    Addicionar nodo a lista dos connectados
         *    Para cada um pegar nos links
         *      Addicionar cada nodo (link) a lista dos conectados
         *          Fazer o procecamento anterior para os links do link
         * Connectar todos os nodos da lista dos connectados
         * Para todos os nodos da rede nao connectados
         *      Distancia = maxima
         *      ...  
         *   
         *      
         */
    }
    
    
    /**
     * Connect node to the network (establish links).
     */
    protected void connectNode(Integer id) {
        
        //Compute range
        double dist = this.getRadius() / Math.sqrt(super.getInitNumNodes());
        
        //Establish 2D links
        Node target = super.getNode(id);
        double minDistance = 1;
        Node closestNode = null;
        for(Node n : super.getNodes()){
            
            //Only establish link with nodes different from himself
            if(!n.equals(target)){
            
                double tmpDist = super.calcNodesDistance(target, n);
                if (tmpDist < dist){
                    super.addLink(target, n, Link.BIDIRECTIONAL);
                    n.addArrivingNode(id);
                    target.connect();
                }
            
                //Keep closest node (may be needed)
                if(tmpDist < minDistance){
                    minDistance = tmpDist;
                    closestNode = n;
                }
            }
        }
        
        //Check node connection
        if(!target.isConnected()){
            
            //Connect to closest node 
            //Note: Node found previously (more efficient than re-computing distances)
            super.addLink(target, closestNode, Link.BIDIRECTIONAL);
            closestNode.addArrivingNode(id);
            target.connect();
        }
    }
    
    
    /**
     * Re-connect node to the network (establish link).
     * NOTE: Use avoid special nodes from been disconnected (e.g. clouds).
     */
    protected void reconnectNode(Integer id) {
        
        
        //Find closest node
        Node target = super.getNode(id);
        double minDistance = 1;
        Node closestNode = null;
        for(Node n : super.getNodes()){
            
            //Only consider nodes different from himself
            if(!n.equals(target)){
            
                double tmpDist = super.calcNodesDistance(target, n);
                if(tmpDist < minDistance){
                    minDistance = tmpDist;
                    closestNode = n;
                }
            }
        }
        
        //Connect to closest node 
        super.addLink(target, closestNode, Link.BIDIRECTIONAL);
        closestNode.addArrivingNode(id);
        target.connect();
    }
    
    
    
    public String getReportPrefixName(){
        return super.getType() + "-" + super.getInitNumNodes() + "-" + this.getRadius();
    }
    
    
    public String toString(){
        return "Network [type: "+ super.getType() +"; size: "+ super.getNumNodes() +"; radius: "+this.getRadius()+"]";
    }
    

}
