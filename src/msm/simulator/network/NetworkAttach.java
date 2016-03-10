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
public class NetworkAttach extends Network {
    

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 2042530148696503385L;
    
    
    private static final String NET_TYPE_ATTACH = "Attach";
    
    private int degree;

    /**
     * Constructors
     */
    
    public NetworkAttach(){
        super();
    }
    

    public NetworkAttach(int num_nodes) {
        super(num_nodes);
    }
    
    
    public NetworkAttach(NetworkAttach net){
        super(net);
        this.degree = net.getDegree();
    }
    
    
    public Object clone(){
        return new NetworkAttach(this);
    }
    

    public void init(String type, Config config){
        super.init(type, config);
        this.degree = Integer.valueOf(config.getValue(Config.PARAM_NETWORK_DEGREE));
    }

    /**
     * @return Returns the degree.
     */
    public int getDegree() {
        return degree;
    }

    
    /**
     * @param degree The degree to set.
     */
    public void setDegree(int degree) {
        this.degree = degree;
    }
    

    public void generateNetwork() {
        
        int d = this.getDegree();
        
        super.setType(NET_TYPE_ATTACH);
        
        //Generate Random Nodes
        Random randPos = new Random();
        for (int i = 0; i < super.getNumNodes(); i++) {
            double x = randPos.nextDouble();
            double y = randPos.nextDouble();
            super.addNode(new Integer(i), new Node(i, x, y));
        }
        
        //Generate Attach Links
        Random randNode = new Random();
        Random randUni = new Random();
        int n1 = randNode.nextInt(super.getNumNodes());
        int n2 = randNode.nextInt(super.getNumNodes());
        while(n1 == n2){
            n2 = randNode.nextInt(super.getNumNodes());
        }
        addLink(super.getNode(n1), super.getNode(n2), Link.BIDIRECTIONAL);
        int totlinks = 2;
        int links = super.getNumNodes() * d / 2;
        for(int i = 0; i < links; i++){
            n1 = randNode.nextInt(super.getNumNodes());
            n2 = n1;
            while( (n1 == n2)
                    || super.getNode(n1).existLink(n2) ){
                n1 = randNode.nextInt(super.getNumNodes());
                int pos = (int)(totlinks * randUni.nextDouble());
                n2 = node(pos);
            }
            addLink(super.getNode(n1), super.getNode(n2), Link.BIDIRECTIONAL);
            totlinks = totlinks + 2;
        }
        
        //Connect Network Graph
        this.connectGraph();
        
    }
    
    
    private int node(int pos){
        int i = 0;
        int id = 0;
        for(Node n : super.getNodes()){
            int nls = n.numberOfLinks();
            if((i+nls) <= pos) {
                i = i + nls;
            } else {
                Integer[] links = n.getLinks().toArray(new Integer[nls]);
                id = links[pos-i];
                break;
            }
        }
        return id;
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
        
        Random rndUni = new Random();
        
        //Establish links (preferential attachment)
        for(int i=0; i < this.degree; i++){
            
            //Get total number of links
            int totlinks = super.getNumOfLinks();
            
            //Choose a random number from total number of links
            int pos = (int)(totlinks * rndUni.nextDouble());
            
            //Get a node according to the chosen random position,
            // node with greater degree will have a higher probability of been chosen
            int nID = node(pos);
            
            //Add link
            super.addLink(id, nID, Link.BIDIRECTIONAL);
            super.getNode(nID).addArrivingNode(id);
        }
        
        //Set node to connected
        super.getNode(id).connect();
    }
    
    
    /**
     * Re-connect node to the network (establish link).
     * NOTE: Use avoid special nodes from been disconnected (e.g. clouds).
     */
    protected void reconnectNode(Integer id) {
        
        Random rndUni = new Random();
        
        //Get total number of links
        int totlinks = super.getNumOfLinks();
        
        //Choose a random number from total number of links
        int pos = (int)(totlinks * rndUni.nextDouble());
        
        //Get a node according to the chosen random position,
        // node with greater degree will have a higher probability of been chosen
        int nID = node(pos);
        
        //Establish link with random node 
        super.addLink(id, nID, Link.BIDIRECTIONAL);
        super.getNode(nID).addArrivingNode(id);
        super.getNode(id).connect();
    }
    
    
    
    public String getReportPrefixName(){
        return super.getType() + "-" + super.getInitNumNodes() + "-" + this.getDegree();
    }
    
    
    public String toString(){
        return "Network [type: "+ super.getType() +"; size: "+ super.getNumNodes() +"; degree: "+this.getDegree()+"]";
    }

}
