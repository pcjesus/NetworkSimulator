/**
 * MSM - Network Simulator
 */


package msm.simulator.network;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import msm.simulator.Config;
import msm.simulator.exceptions.NetworkException;
import msm.simulator.util.SetUtils;

/**
 * Network Class
 * 
 * @author pjesus
 * @version 1.0
 */
public class NetworkFromFile extends Network implements Cloneable {
    
    /**
     * Generated serial version UID
     */
    
    private static final long serialVersionUID = -3944550240876304409L;
    
    private static final String NET_TYPE_FROM_FILE = "FromFile";
    private static final String SPLIT_REGEXP = "[\\W&&[^\\.]]++"; //"[\W&&[^\.]]++";
    
//    private int degree;
    private String filePath;

    /**
     * Constructors
     */
    
    public NetworkFromFile(){
        super();
    }
    

    public NetworkFromFile(int num_nodes) {
        super(num_nodes);
    }
    
    public NetworkFromFile(NetworkFromFile net){
        super(net);
//        this.degree = net.getDegree();
    }
    
    
    public Object clone(){
        return new NetworkFromFile(this);
    }

    public void init(String type, Config config){
        super.init(type, config);
        this.filePath = config.getValue(Config.PARAM_NETWORK_CONFIG_FILEPATH);
//        this.degree = Integer.valueOf(config.getValue(Config.PARAM_NETWORK_DEGREE));
    }
    

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }


    
    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = new String(filePath);
    }
    
    
    /**
     * @return Returns the degree.
     */
//    public int getDegree() {
//        return degree;
//    }

    
    /**
     * @param degree The degree to set.
     */
//    public void setDegree(int degree) {
//        this.degree = degree;
//    }
    
    

    
    public void generateNetwork() throws NetworkException {
        
        super.setType(NET_TYPE_FROM_FILE);
        
        //Read network configuration (i.e. node links)
        //Format (each line): <Node1>=<neighbor1>,<neighbor2>,...,<neighborj>
        Properties nodeLinks = new Properties();
        
        try {
            FileInputStream fisFile = new FileInputStream(this.filePath);
            nodeLinks.load(fisFile);
            fisFile.close();
        } catch (FileNotFoundException fnfe) {
            System.err.println("Wrong File Argument: " + this.filePath +
                               ".\nFile do not exist! ");
            System.err.flush();
            throw new NetworkException(fnfe);
        } catch (IOException ioe) {
            System.err.println("I/O Error!\nCan Not read file: " + this.filePath);
            System.err.flush();
            throw new NetworkException(ioe);
        }
        
        //Put nodes at a random position
        Random randPos = new Random();
        for (int i = 0; i < super.getNumNodes(); i++) {
            double x = randPos.nextDouble();
            double y = randPos.nextDouble();
            super.addNode(new Integer(i), new Node(i, x, y));
        }
        
        //Create node links
        for (int i = 0; i < super.getNumNodes(); i++) {
            String neighborsList = nodeLinks.getProperty(String.valueOf(i));
            String[] neighbors = neighborsList.split(SPLIT_REGEXP);
            
            for(String nbr : neighbors){
                int j = Integer.valueOf(nbr);
                if(!super.getNode(i).existLink(j)){
                    addLink(super.getNode(i), super.getNode(j), Link.BIDIRECTIONAL);
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
        
        //Select Random neighbors
        Set<Integer> nodes = super.getNodesKeySet();
        List<Integer> rNodes = SetUtils.randomizeSet(nodes);
        
        Random randDegree = new Random();
        int degree = randDegree.nextInt(super.getNumNodes()) + 1;
        rNodes = rNodes.subList(0, degree);
        
        //Establish links
        for(Integer nID : rNodes){
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
        return super.getType() + "-" + super.getInitNumNodes();
    }
    
    
    public String toString(){
        return "Network [type: "+ super.getType() +"; size: "+ super.getNumNodes() +"; network config file: "+this.getFilePath()+"]";
    }

}
