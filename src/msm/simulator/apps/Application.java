/**
 * MSM - Network Simulator
 */

package msm.simulator.apps;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import msm.simulator.ComEngine;
import msm.simulator.Config;
import msm.simulator.Event;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.ComEngineException;
import msm.simulator.network.Message;
import msm.simulator.network.MessageBroadcast;
import msm.simulator.network.Node;
import msm.simulator.util.DataDistribution;



/**
 * @author pjesus
 *
 */
public abstract class Application {
    
    private String appType;
    
    private Node appNode;
    private ComEngine comEngine;
    
    private BigDecimal value;
    
    //Not use (auxiliary value)
    private BigDecimal baseValue;
    
    private BigDecimal initValue;
    
    private DataDistribution ddValue;
    
    
    private MathContext mc;
    
    //Register last EvtClockId
    private Event<?> lastEvt;
    
    public Application(){
    }
    
    public static Application createInstance(String appType, Node appNode, ComEngine comEngine)
      throws ClassNotFoundException, IllegalAccessException, InstantiationException {

        //Create Application Instance
        String className = Application.class.getName() + appType;
        Class<?> applicationClass = Class.forName(className);
        Application app = (Application)applicationClass.newInstance();

        app.setAppType(appType);
        app.setAppNode(appNode);
        app.setComEngine(comEngine);
        app.setMathContext(comEngine.getMathContext());
        
        app.setInitValue(appNode.getDataValue());
        return app;
    }
    
    
    
    
    public abstract void init(Config config, String... args) throws ApplicationException;
    
    public abstract void onReceive(Message<?> msg);

    public abstract void onTick();
    
    public abstract String debugOnReceiveStatus();
    
    public abstract String debugTickStatus();
    
    public abstract String getState();
    
    
    public abstract void messageGeneration();
    
    public abstract void stateTransition(Collection<Message<?>> msgsReceived);
    
    //Redefine this method to set initial state depending from the all network initialization
    public void init2(){};
    
    /**
     * @deprecated
     */
    public int sendMessage(String from, String to, Object msgData){
        try {
            return this.comEngine.sendMessage(new Message<Object>(from, to, msgData));
        } catch (ComEngineException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        
        return -1;
    }
    
    public int sendMessage(Message<?> msg){
        try {
            return this.comEngine.sendMessage(msg);
        } catch (ComEngineException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        
        return -1;
    }

/*    
    public void sendMessage(Message msg){
        this.comEngine.sendMessage(msg);
    }
*/
    /**
     * @deprecated
     */
    public int broadcastMessage(String from, Object msgData){
        try {
            return this.comEngine.broadcastMessage(new Message<Object>(from, MessageBroadcast.TO_ANY, msgData));
        } catch (ComEngineException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        
        return -1;
    }
    
    public int broadcastMessage(Message<?> msg){
        try {
            return this.comEngine.broadcastMessage(msg);
        } catch (ComEngineException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        
        return -1;
    }
    
    /**
     * @deprecated
     */
    public String setTimeout(int timeout) throws ApplicationException {
        try {
            return this.comEngine.setTimeout(timeout, String.valueOf(this.getAppNode().getId()), null);
        } catch (ComEngineException e) {
            throw (ApplicationException) new ApplicationException(e).initCause(e);
        }
    }
    
    public String setTimeout(int timeout, Object data) throws ApplicationException {
        try {
            return this.comEngine.setTimeout(timeout, String.valueOf(this.getAppNode().getId()), data);
        } catch (ComEngineException e) {
            throw (ApplicationException) new ApplicationException(e).initCause(e);
        }
    }
    
    public boolean resetTimeout(String clockEvtKey){
        return this.comEngine.reset(clockEvtKey, String.valueOf(this.getAppNode().getId()));
    }
    
    
    /**
     * @return Returns the appType.
     */
    public String getAppType() {
        return this.appType;
    }




    
    /**
     * @param appType The appType to set.
     */
    public void setAppType(String appType) {
        this.appType = appType;
    }
    
    
    /**
     * @return Returns the value.
     */
    public BigDecimal getValue() {
        return this.value;
    }
    
    public DataDistribution getDDValue() {
        return this.ddValue;
    }

    public BigDecimal getBaseValue() {
        return this.baseValue;
    }

    
    /**
     * @param value The value to set.
     */
    public void setValue(BigDecimal value) {
        this.value = value;
        this.comEngine.setAppStateChanged(true);
    }
    
    public void setDDValue(DataDistribution ddValue) {
        this.ddValue = ddValue;
        this.comEngine.setAppStateChanged(true);
    }
    
    public void setBaseValue(BigDecimal basevalue) {
        this.baseValue = basevalue;
    }

    
    /**
     * @return Returns the appNode.
     */
    public Node getAppNode() {
        return appNode;
    }

    
    /**
     * @param appNode The appNode to set.
     */
    public void setAppNode(Node appNode) {
        this.appNode = appNode;
    }


    
    /**
     * @param comEngine The comEngine to set.
     */
    private void setComEngine(ComEngine comEngine) {
        this.comEngine = comEngine;
    }

    
    /**
     * @return the comEngine
     */
    public ComEngine getComEngine() {
        return comEngine;
    }

    
    /**
     * @return the mc
     */
    public MathContext getMathContext() {
        return mc;
    }

    
    /**
     * @param mc the mc to set
     */
    public void setMathContext(MathContext mc) {
        this.mc = mc;
    }


    /**
     * @return Returns the value.
     */
    public BigDecimal getEstimatedValue() {
        switch(this.getComEngine().getAggFunction()) {
            case AVG    : return this.value;
                          
            case COUNT  : if(this.getValue().equals(BigDecimal.ZERO)) {
                            return this.value;
                          } else {
                              BigDecimal numClouds = new BigDecimal(this.getComEngine().getNetwork().getNumClouds());
                              return numClouds.divide(this.value, this.getMathContext());            
                          }
            default : 
                if(this.getValue().equals(BigDecimal.ZERO)) {
                    return this.value;
                } else {
                    BigDecimal numClouds = new BigDecimal(this.getComEngine().getNetwork().getNumClouds());
                    return numClouds.divide(this.value, this.getMathContext());            
                }
        }
    }

    
    /**
     * @return the initValue
     */
    public BigDecimal getInitValue() {
        return initValue;
    }

    
    /**
     * @param initValue the initValue to set
     */
    public void setInitValue(BigDecimal initValue) {
        this.initValue = initValue;
        this.comEngine.setAppStateChanged(true);
    }

    
    /**
     * @return the lastEvtId
     */
    public Event<?> getLastEvt() {
        return this.lastEvt;
    }

    
    /**
     * @param lastEvt the lastEvt to set
     */
    public void setLastEvt(Event<?> lastEvt) {
        this.lastEvt = lastEvt;
    }
    

}
