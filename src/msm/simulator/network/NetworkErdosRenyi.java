/**
 * MSM - Network Simulator
 */


package msm.simulator.network;


import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
public class NetworkErdosRenyi extends Network implements Cloneable {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -2859906668955720777L;
    

    private static final String NET_TYPE_ERDOSRENYI = "ErdosRenyi";
    
    private double probability;
    private int edges;

    /**
     * Constructors
     */
    
    public NetworkErdosRenyi(){
        super();
    }
    

    public NetworkErdosRenyi(int num_nodes) {
        super(num_nodes);
    }
    
    public NetworkErdosRenyi(NetworkErdosRenyi net){
        super(net);
        this.probability = net.getProbability();
    }
    
    
    public Object clone(){
        return new NetworkErdosRenyi(this);
    }

    public void init(String type, Config config){
        super.init(type, config);
        this.probability = Double.valueOf(config.getValueWithDefault(Config.PARAM_NETWORK_PROBABILITY, "-1"));
        if(this.probability <= 0){
            this.edges = Integer.valueOf(config.getValue(Config.PARAM_NETWORK_EDGES));
            this.probability = (this.edges * 2.0d) / (super.getInitNumNodes() * (super.getInitNumNodes() - 1));
        }
    }
    

    /**
     * @return Returns the probability.
     */
    public double getProbability() {
        return this.probability;
    }

    
    /**
     * @param probability The probability to set.
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }
    
    
    /**
     * @return Returns the number of edges.
     */
    public int getEdges() {
        return this.edges;
    }

    
    /**
     * @param edges The number of edges to set.
     */
    public void setEdges(int edges) {
        this.edges = edges;
    }

    
    public void generateNetwork() {
        
        super.setType(NET_TYPE_ERDOSRENYI);
        this.setEdges(0);
        
        //Generate Random Nodes
        Random randPos = new Random();
        for (int i = 0; i < super.getNumNodes(); i++) {
            double x = randPos.nextDouble();
            double y = randPos.nextDouble();
            super.addNode(new Integer(i), new Node(i, x, y));
        }
        
        //Generate Random Links with uniform probability p
        Deque<Integer>nodesList = new LinkedList<Integer>(super.getNodesKeySet());
        Random randLinkCreation = new Random();
        while(!nodesList.isEmpty()){
            Integer targetId = nodesList.removeFirst();
            for(Integer otherNodeId : nodesList){
                //Decide link creation according to the given probability
                if(randLinkCreation.nextDouble() <= this.getProbability()){
                    super.addLink(targetId, otherNodeId, Link.BIDIRECTIONAL);
                    this.setEdges(this.getEdges() + 1);
                    super.getNode(targetId).connect();
                    super.getNode(otherNodeId).connect();
                }
            }
        }
        
        //NOTE: Do not force network connection
        //Connect Network Graph
        //this.connectGraph();
    }
    
    
    
    /** @todo Melhorar Algoritmo **/
//    public void connectGraph(){
//        
//        Map<Integer, Node> nn = new HashMap<Integer, Node>();
//        Random randNode = new Random();
//        while(nn.size() < (super.getNumNodes()/2)){
//            Map<Integer, Node> pending = new HashMap<Integer, Node>();
//            int randN = randNode.nextInt(super.getNumNodes());
//            pending.put(new Integer(randN), super.getNode(randN));
//            //nn.clear();
//            nn = new HashMap<Integer, Node>(pending);
//            while(pending.size() > 0){
//                Iterator<Node> it = pending.values().iterator();
//                Node n1 = it.next();
//                pending.remove(new Integer(n1.getId()));
//                for(Integer n2 : n1.getLinks()){
//                    if(!nn.containsKey(n2)){
//                        nn.put(n2, super.getNode(n2));
//                        pending.put(n2, super.getNode(n2));
//                    }
//                }
//            }
//            for(Node n : nn.values()){
//                n.connect();
//            }
//            for(Node n1: super.getNodes()){
//                if(!n1.isConnected()){
//                    for(Integer l : n1.getLinks()){
//                        if(super.getNode(l).isConnected()){
//                            n1.connect();
//                            break;
//                        }
//                    }
//                    if(!n1.isConnected()){
//                       double d = Double.MAX_VALUE;
//                       Node n = null;
//                       for(Node n2: super.getNodes()){
//                           double d2 = this.calcNodesDistance(n1, n2);
//                           if((d2 < d) && n2.isConnected()){
//                               n = n2;
//                               d = d2;
//                               
//                           }
//                           
//                       }
//                       super.addLink(n1, n, Link.BIDIRECTIONAL);
//                       n1.connect();
//                    }
//                }
//            }
//        }
//        
//        /**
//         * Escolher metade dos nodos
//         *    Addicionar nodo a lista dos connectados
//         *    Para cada um pegar nos links
//         *      Addicionar cada nodo (link) a lista dos conectados
//         *          Fazer o procecamento anterior para os links do link
//         * Connectar todos os nodos da lista dos connectados
//         * Para todos os nodos da rede nao connectados
//         *      Distancia = maxima
//         *      ...  
//         *   
//         *      
//         */
//    }
    
    
    /**
     * Connect node to the network (establish links).
     */
    protected void connectNode(Integer id) {
        
        Random randLinkCreation = new Random();
        for(Integer nID : super.getNodesKeySet()){
            
            //Decide link creation according to the given probability
            if(randLinkCreation.nextDouble() <= this.getProbability()){
          
                //Establish link
                super.addLink(id, nID, Link.BIDIRECTIONAL);
                super.getNode(nID).addArrivingNode(id);
            }
        }
        
        //Set node to connected
        super.getNode(id).connect();
    }
    
    
    /**
     * Re-connect node to the network (establish link).
     * NOTE: Use avoid special nodes from been disconnected (e.g. clouds).
     */
    protected void reconnectNode(Integer id) {
        
        //Select Random node
        Set<Integer> nodes = super.getNodesKeySet();
        List<Integer> rNodes = SetUtils.randomizeSet(nodes);
        Integer rndNode = rNodes.get(0);
        
        //Establish link with random node 
        super.addLink(id, rndNode, Link.BIDIRECTIONAL);
        super.getNode(rndNode).addArrivingNode(id);
        super.getNode(id).connect();
    }
    
    
    public String getReportPrefixName(){
        return super.getType() + "-" + super.getInitNumNodes() + "-" + this.getEdges();
    }
    
    
    public String toString(){
        return "Network [type: "+ super.getType() +"; size: "+ super.getNumNodes() +"; probability: "+this.getProbability()+"; edges: "+this.getEdges()+"]";
    }

}
