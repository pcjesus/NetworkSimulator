/**
 * MAP-i - Network Simulator
 */
package msm.simulator.network;

import java.math.MathContext;
import java.util.TreeMap;

import msm.simulator.ComEngine;
import msm.simulator.Config;
import msm.simulator.Event;
import msm.simulator.exceptions.ApplicationException;
import msm.simulator.exceptions.DynamicsException;
import msm.simulator.util.ArrayUtils;


/**
 * Dynamics Class
 * 
 * Handle network dynamism
 * 
 * @author pcjesus
 * @version 1.0
 */
public class Dynamics {
    
    
    
    /**
     * Churn configuration parameters
     */
    private double[] churnRates;
    private int[] churnAtTime;
    private int[] churnRepetitions;
    private boolean churnRepeatPattern;
    private boolean churnFromInit;
    private boolean churnCountIsolated;
    
    private boolean dynamismOccured;
    
    private double[] valueChangeRates;
    private double[] valueChangeCoverageRatio;
    private int[] valueChangeAtTime;
    private int[] valueChangeRepetitions;
    private String[] valueChangeOperators;
    private boolean valueChangeRepeatPattern;
//    private boolean valueChangeFromInit;
    
    private static String PARAM_DELIMITER = ";";
    
    public static String VC_OP_MULTI = "*";
    public static String VC_OP_ADD = "+";
    
    //Churn Events
    private TreeMap<Integer, Event<Integer>> dynamicEvents;
    
    
    public Dynamics(Config config) 
        throws DynamicsException {
        
        try {
            String churnRateCfg = config.getValue(Config.PARAM_CHURN_RATE);
            if(!churnRateCfg.isEmpty()){
                this.churnRates = ArrayUtils.splitDouble(churnRateCfg, PARAM_DELIMITER);
            } else {
                this.churnRates = new double[0];
            }
            String churnPeriodLengthsCfg = config.getValue(Config.PARAM_CHURN_PERIOD_LENGTH);
            if(!churnPeriodLengthsCfg.isEmpty()){
                this.churnAtTime = ArrayUtils.splitInt(churnPeriodLengthsCfg, PARAM_DELIMITER);
            } else {
                this.churnAtTime = new int[0];
            }
            String churnPeriodRepetitionsCfg = config.getValue(Config.PARAM_CHURN_PERIOD_REPETITION);
            if(!churnPeriodRepetitionsCfg.isEmpty()){
                this.churnRepetitions = ArrayUtils.splitInt(churnPeriodRepetitionsCfg, PARAM_DELIMITER);
            } else {
                this.churnRepetitions = new int[0];
            }
            this.churnRepeatPattern = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_CHURN_REPETITION, "true"));
            this.churnFromInit = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_CHURN_RATE_FROM_INIT, "true"));
            this.churnCountIsolated = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_CHURN_COUNT_ISOLATED_NODES, "true"));
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new DynamicsException("Error loading configuration parameter: "+ e.getMessage(), e);
        }
        
        //Validate loaded data
        if((this.churnRates.length != this.churnAtTime.length) 
                || (this.churnAtTime.length != this.churnRepetitions.length) ) {
            throw new DynamicsException("Invalid churn configuration: "
                    + Config.PARAM_CHURN_RATE + ", " +Config.PARAM_CHURN_PERIOD_LENGTH + ", and " 
                    + Config.PARAM_CHURN_PERIOD_REPETITION + "lists must be of the same size.");
        }
        
        for(double rate : this.churnRates){
            if(rate == 0.0){
                throw new DynamicsException("Invalid churn configuration "
                        + Config.PARAM_CHURN_RATE + " values cannot be equal to 0.");
            }
        }
        
        
        for(int plenght : this.churnAtTime){
            if(plenght < 1){
                throw new DynamicsException("Invalid churn configuration "
                        + Config.PARAM_CHURN_PERIOD_LENGTH + " values must be >= 1.");
            }
        }
        
        for(int prep : this.churnRepetitions){
            if(prep < 1){
                throw new DynamicsException("Invalid churn configuration "
                        + Config.PARAM_CHURN_PERIOD_REPETITION + " values must be >= 1.");
            }
        }
        
        this.dynamicEvents = new TreeMap<Integer, Event<Integer>>();
        this.dynamismOccured = false;
        
        
        try{
            String valueChangeRateCfg = config.getValue(Config.PARAM_VALUE_CHANGE_RATE);
            if(!valueChangeRateCfg.isEmpty()){
                this.valueChangeRates = ArrayUtils.splitDouble(valueChangeRateCfg, PARAM_DELIMITER);
            } else {
                this.valueChangeRates = new double[0];
            }
            String valueChangeCoverageCfg = config.getValue(Config.PARAM_VALUE_CHANGE_COVERAGE_RATIO);
            if(!valueChangeCoverageCfg.isEmpty()){
                this.valueChangeCoverageRatio = ArrayUtils.splitDouble(valueChangeCoverageCfg, PARAM_DELIMITER);
            } else {
                this.valueChangeCoverageRatio = new double[0];
            }
            String valueChangeAtTimeCfg = config.getValue(Config.PARAM_VALUE_CHANGE_AT_TIME);
            if(!valueChangeAtTimeCfg.isEmpty()){
                this.valueChangeAtTime = ArrayUtils.splitInt(valueChangeAtTimeCfg, PARAM_DELIMITER);
            } else {
                this.valueChangeAtTime = new int[0];
            }
            String valueChangeRepetitionsCfg = config.getValue(Config.PARAM_VALUE_CHANGE_REPETITION);
            if(!valueChangeRepetitionsCfg.isEmpty()){
                this.valueChangeRepetitions = ArrayUtils.splitInt(valueChangeRepetitionsCfg, PARAM_DELIMITER);
            } else {
                this.valueChangeRepetitions = new int[0];
            }
            String valueChangeOperatorsCfg = config.getValue(Config.PARAM_VALUE_CHANGE_OPERATOR);
            if(!valueChangeOperatorsCfg.isEmpty()){
                this.valueChangeOperators = valueChangeOperatorsCfg.split(PARAM_DELIMITER);
            } else {
                this.valueChangeOperators = new String[0];
            }
            this.valueChangeRepeatPattern = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_VALUE_CHANGE_REPEAT_PATTERN, "true"));
//            this.valueChangeFromInit = Boolean.valueOf(config.getValueWithDefault(Config.PARAM_VALUE_CHANGE_FROM_INIT, "true"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new DynamicsException("Error loading configuration parameter: "+ e.getMessage(), e);
        }
    
        //Validate loaded data
        if((this.valueChangeRates.length != this.valueChangeCoverageRatio.length) 
                || (this.valueChangeCoverageRatio.length != this.valueChangeAtTime.length)
                || (this.valueChangeAtTime.length != this.valueChangeRepetitions.length)
                || (this.valueChangeRepetitions.length != this.valueChangeOperators.length)) {
            throw new DynamicsException("Invalid Value Change configuration: "
                    + Config.PARAM_VALUE_CHANGE_RATE + ", " 
                    + Config.PARAM_VALUE_CHANGE_COVERAGE_RATIO + ", " 
                    + Config.PARAM_VALUE_CHANGE_AT_TIME + ", " 
                    + Config.PARAM_VALUE_CHANGE_REPETITION + ", and " 
                    + Config.PARAM_VALUE_CHANGE_OPERATOR + "lists must be of the same size.");
        }
    
        for(double rate : this.valueChangeRates){
            if(rate == 0.0){
                throw new DynamicsException("Invalid value change configuration "
                        + Config.PARAM_VALUE_CHANGE_RATE + " values cannot be equal to 0.");
            }
        }
        
        for(double coverage : this.valueChangeCoverageRatio){
            if(coverage <= 0.0 || coverage > 1.0){
                throw new DynamicsException("Invalid value change configuration "
                        + Config.PARAM_VALUE_CHANGE_COVERAGE_RATIO + " values must be greater than 0 and less or equal to 1.");
            }
        }
    
    
        for(int attime : this.valueChangeAtTime){
            if(attime < 1){
                throw new DynamicsException("Invalid value change configuration "
                        + Config.PARAM_VALUE_CHANGE_AT_TIME + " values must be >= 1.");
            }
        }
    
        for(int rep : this.valueChangeRepetitions){
            if(rep < 1){
                throw new DynamicsException("Invalid value change configuration "
                        + Config.PARAM_VALUE_CHANGE_REPETITION + " values must be >= 1.");
            }
        }
        
        
        for(int prep : this.valueChangeRepetitions){
            if(prep < 1){
                throw new DynamicsException("Invalid value change configuration "
                        + Config.PARAM_VALUE_CHANGE_REPETITION + " values must be >= 1.");
            }
        }
        
        for(String op : this.valueChangeOperators){
            if(!op.equals(VC_OP_ADD) && !op.equals(VC_OP_MULTI)){
                throw new DynamicsException("Invalid value change configuration "
                        + Config.PARAM_VALUE_CHANGE_OPERATOR + ": "+op+". Operator must be "+VC_OP_ADD+" or "+VC_OP_MULTI+".");
            }
        }
        
        
    }
    
    
    /**
     * Create dynamic events (i.e. churn and value change), according to loaded definitions
     * 
     * @param timeLimit
     */
    public void scheduleEvents(int timeLimit){
        
        //IMPORTANTE: Value Change will replace Churn Events at the same time (losing them).
        //TODO: Use a Set of events at the same time to allow both events to occure at the same time.
        
        //Clear events
        this.dynamicEvents.clear();
        
        //Schedule churn events
        this.scheduleChurnEvents(timeLimit);
        
        //Schedule value change events
        this.scheduleValueChangeEvents(timeLimit);
        
    }
    
    
    /**
     * Create Churn events, according to loaded definitions
     * 
     * @param timeLimit
     */
    private void scheduleChurnEvents(int timeLimit){
        
        int t = 0;
        int i = 0;
        int maxi = this.churnRates.length;
                    
        //At least one churn definitions is set (else no churn rate is found)
        if(maxi > 0) {
        
            //Create churn events according to defined settings
            if (this.churnRepeatPattern) {

                // Create events until the time limit is reached,
                // considering that the all period list is continuously repeated
                // (loop period list)
                while (t <= timeLimit) {

                    // Repeat event creation
                    int j = 1;
                    while ((t <= timeLimit)
                            && (j <= this.churnRepetitions[i])) {

                        // Increment time
                        t = t + this.churnAtTime[i];

                        // Create churn event
                        this.dynamicEvents.put(t, new Event<Integer>(t, null,
                                Event.EventType.CHURN, i));

                        // Increment repetition
                        j++;
                    }

                    // Cycle churn definitions
                    if (i < maxi) {
                        // Increment churn definition index
                        i++;
                    } else {
                        // Set index to the 1st value when it max value is
                        // reached
                        i = 0;
                    }
                }

            } else {

                // Create defined churn events, stopping if the time limit is
                // reached
                while ((t <= timeLimit) && (i < maxi)) {

                    // Repeat event creation
                    int j = 1;
                    while ((t <= timeLimit)
                            && (j <= this.churnRepetitions[i])) {

                        // Increment time
                        t = t + this.churnAtTime[i];

                        // Create churn event
                        this.dynamicEvents.put(t, new Event<Integer>(t, null,
                                Event.EventType.CHURN, i));

                        // Increment repetition
                        j++;
                    }

                    // Increment churn definition index
                    i++;
                }

            }           
        
        } // if exist churn definitions
        
    }
    
    
    /**
     * Create Value Change events, according to loaded definitions
     * 
     * @param timeLimit
     */
    private void scheduleValueChangeEvents(int timeLimit){
        
        int t = 0;
        int i = 0;
        int maxi = this.valueChangeRates.length;
        
        //At least one Value Change definitions is set (else no definition is set)
        if(maxi > 0) {
        
            //Create value change events according to defined settings
            if (this.valueChangeRepeatPattern) {

                // Create events until the time limit is reached,
                // considering that the all period list is continuously repeated
                // (loop period list)
                while (t <= timeLimit) {

                    // Repeat event creation
                    int j = 1;
                    while ((t <= timeLimit)
                            && (j <= this.valueChangeRepetitions[i])) {

                        // Increment time
                        t = t + this.valueChangeAtTime[i];

                        // Create value change event
                        this.dynamicEvents.put(t, new Event<Integer>(t, null, 
                                Event.EventType.VALUE_CHANGE, i));

                        // Increment repetition
                        j++;
                    }

                    // Cycle value change definitions
                    if (i < maxi) {
                        // Increment value change definition index
                        i++;
                    } else {
                        // Set index to the 1st value when it max value is
                        // reached
                        i = 0;
                    }
                }

            } else {

                // Create defined value change events, stopping if the time limit is
                // reached
                while ((t <= timeLimit) && (i < maxi)) {

                    // Repeat event creation
                    int j = 1;
                    while ((t <= timeLimit)
                            && (j <= this.valueChangeRepetitions[i])) {

                        // Increment time
                        t = t + this.valueChangeAtTime[i];

                        // Create churn event
                        this.dynamicEvents.put(t, new Event<Integer>(t, null,
                                Event.EventType.VALUE_CHANGE, i));

                        // Increment repetition
                        j++;
                    }

                    // Increment value change definition index
                    i++;
                }

            }

        
        } // if exist value change definitions
        
    }
    
    
    
    /**
     * Return time next scheduled dynamic event (churn or value change), 
     * or maximum time if none exists
     * 
     * @return time of the next churn event
     */
    public int nextDynamismEventTime(){
        
        if(this.dynamicEvents.size() > 0){
            return this.dynamicEvents.firstKey();
        } else {
            return Integer.MAX_VALUE;
        }
    }
    
    
    public int processEvent(Event<Integer> dynamicEvt, Network net, SpatialDataDistribution dd, 
                            ComEngine ce, Config conf, Integer simulationIndex, 
                            Integer repetitionCount) throws DynamicsException {
        
        switch (dynamicEvt.getType()) {
            case CHURN:
                return churn(dynamicEvt, net, dd, ce, conf, simulationIndex, repetitionCount);
            case VALUE_CHANGE:
                return valueChange(dynamicEvt, net, ce.getMathContext());
            default:
                throw new DynamicsException("Dynamic event type not supported (invalid): "+dynamicEvt.getType()+". ");
        }
    }
    
    /**
     * Pull (get and remove) next dynamic event
     * @return next dynamic event to process
     */
    public Event<Integer> pullNextEvent(){
        return this.dynamicEvents.pollFirstEntry().getValue();
    }
    
    
    /**
     * Apply churn model to the given network (at the beginning of each round)
     * 
     * @param churnEvt Churn Event
     * @param net Target network
     * @param dd Spatial Data Distribution
     * @param ce Communication Engine
     * @param conf Configuration
     * @param simulationIndex Simulation index
     * @param repetitionCount Repetition index
     * 
     * @return number of nodes leaving/arriving
     * 
     * @throws DynamicsException
     */
    private int churn(Event<Integer> churnEvt, Network net, SpatialDataDistribution dd, ComEngine ce, Config conf, 
            Integer simulationIndex, Integer repetitionCount) throws DynamicsException {
        
        //Get churn configuration index
        int index = churnEvt.getData();
        
        //Get churn rate
        double rate = this.churnRates[index];
        int churnNum;
        if(this.churnFromInit){
            churnNum = (int) Math.round((net.getInitNumNodes() * rate) / 100);
        } else {
            churnNum = (int) Math.round((net.getNumNodes() * rate) / 100);
        }
        
        int result = churnNum;
        
        //Apply churn
        if(churnNum < 0){
            
            //Nodes leaving
            churnNum = Math.abs(churnNum);
            if(churnNum < net.getNumNodes()){
                try{
                    result = -net.nodesLeaving(this.churnCountIsolated, churnNum, ce, conf, simulationIndex, repetitionCount);
                } catch (ApplicationException ae) {
                    throw (DynamicsException)new DynamicsException(ae).initCause(ae);
                }
            } else {
                throw new DynamicsException("Number of leaving/crashing nodes ("+churnNum+") must be less than the network size ("+net.getNumNodes()+")!");
            }
            
        } else if (churnNum > 0){
            
            //New nodes arriving
            try{
                result = net.nodesArriving(churnNum, dd, ce, conf, simulationIndex, repetitionCount);
            } catch (ApplicationException ae) {
                throw (DynamicsException)new DynamicsException(ae).initCause(ae);
            }
            
        }
        
        //Return churn value (number of nodes leaving/arriving)
        return result;
        
    }
    
    
    /**
     * Apply value change event to the given network (at the beginning of each round)
     * 
     * @param valueChangeEvt Value Change Event
     * @param net Target network
     * @param dd Spatial Data Distribution
     * @param ce Communication Engine
     * @param conf Configuration
     * @param simulationIndex Simulation index
     * @param repetitionCount Repetition index
     * 
     * @return number of nodes that changed their value
     * 
     * @throws DynamicsException
     */
    private int valueChange(Event<Integer> valueChangeEvt, Network net, MathContext mc) throws DynamicsException {
        
        //Get value change configuration index
        int index = valueChangeEvt.getData();
        
        //Get value change parameters
        double rate = this.valueChangeRates[index];
        double coverage = this.valueChangeCoverageRatio[index];
        String operator = this.valueChangeOperators[index];
        int numNodes = (int) Math.round(net.getNumNodes() * coverage);
        
        int result = numNodes;
        
        //Apply value change
        if (numNodes > 0){
            result = net.changeNodeValues(numNodes, rate, operator, mc);
        }
        
        //Return churn value (number of nodes leaving/arriving)
        return result;
        
    }
    
    /**
     * Apply churn model to the given network (at the beginning of each round)
     * @param net Target network
     * 
     * @return number of nodes leaving/arriving
     * 
     * @throws DynamicsException
     * @deprecated
     */
    private int churn(int iteration, Network net, SpatialDataDistribution dd, ComEngine ce, Config conf, 
            Integer simulationIndex, Integer repetitionCount) throws DynamicsException {
        
        //TODO Change to create a churn event
        
        //Get period index
        int index = 0;
        
        if(churnRepeatPattern){
            
            //Get index, considering that the all period list is continuously repeated, 
            //except the 1rst period (loop period list)
            int maxIndex = this.churnAtTime.length - 1;
            int periodLimit = this.churnAtTime[index];
            
            //Find index
            while(periodLimit <= iteration){
                
                
                if(index < maxIndex) {
                    
                    //Increment index
                    index++;
                } else {
                    
                    //Set index to the 2nd value when it max value is reached
                    index = 1;
                }
                periodLimit = periodLimit + this.churnAtTime[index];
            }
            
        } else {
            
            //Get index, considering that only the last period is indefinitely repeated
            int periodLimit = this.churnAtTime[index];
            while((periodLimit <= iteration) && (index < (this.churnAtTime.length-1))){
                index++;
                periodLimit = periodLimit + this.churnAtTime[index];
            }
        }
        
        
        //Get churn rate
        double rate = this.churnRates[index];
        int churnNum;
        if(this.churnFromInit){
            churnNum = (int) Math.round((net.getInitNumNodes() * rate) / 100);
        } else {
            churnNum = (int) Math.round((net.getNumNodes() * rate) / 100);
        }
        
        int result = churnNum;
        
        //Apply churn
        if(churnNum < 0){
            
            //Nodes leaving
            churnNum = Math.abs(churnNum);
            if(churnNum < net.getNumNodes()){
                try{
                    result = -net.nodesLeaving(this.churnCountIsolated, churnNum, ce, conf, simulationIndex, repetitionCount);
                } catch (ApplicationException ae) {
                    throw (DynamicsException)new DynamicsException(ae).initCause(ae);
                }
            } else {
                throw new DynamicsException("Number of leaving/crashing nodes ("+churnNum+") must be less than the network size ("+net.getNumNodes()+")!");
            }
            
        } else if (churnNum > 0){
            
            //New nodes arriving
            try{
                result = net.nodesArriving(churnNum, dd, ce, conf, simulationIndex, repetitionCount);
            } catch (ApplicationException ae) {
                throw (DynamicsException)new DynamicsException(ae).initCause(ae);
            }
            
        }
        
        //Return churn value (number of nodes leaving/arriving)
        return result;
        
    }
    


    
    /**
     * GETTERS / SETTERS
     */
    
    /**
     * @return the churnRates
     */
    public double[] getChurnRates() {
        return churnRates;
    }


    
    /**
     * @param churnRates the churnRates to set
     */
    public void setChurnRates(double[] churnRates) {
        this.churnRates = churnRates;
    }


    
    /**
     * @return the churnPeriod
     */
    public int[] getChurnPeriod() {
        return churnAtTime;
    }


    
    /**
     * @param churnPeriod the churnPeriod to set
     */
    public void setChurnPeriod(int[] churnPeriod) {
        this.churnAtTime = churnPeriod;
    }


    
    /**
     * @return the repeatPeriod
     */
    public boolean isRepeatPeriod() {
        return churnRepeatPattern;
    }


    
    /**
     * @param repeatPeriod the repeatPeriod to set
     */
    public void setRepeatPeriod(boolean repeatPeriod) {
        this.churnRepeatPattern = repeatPeriod;
    }


    
    /**
     * @return the fromInit
     */
    public boolean isFromInit() {
        return churnFromInit;
    }


    
    /**
     * @param fromInit the fromInit to set
     */
    public void setFromInit(boolean fromInit) {
        this.churnFromInit = fromInit;
    }


    
    /**
     * @return the countIsolated
     */
    public boolean isCountIsolated() {
        return churnCountIsolated;
    }


    
    /**
     * @param countIsolated the countIsolated to set
     */
    public void setCountIsolated(boolean countIsolated) {
        this.churnCountIsolated = countIsolated;
    }


    
    /**
     * @return the dynamismOccured
     */
    public boolean hasDynamismOccured() {
        return this.dynamismOccured;
    }


    
    /**
     * @param dynamismOccured set to boolean value
     */
    public void setDynamismOccured(boolean dynamismOccured) {
        this.dynamismOccured = dynamismOccured;
    }
    
    
    public String getValueChangeOperator(int index){
        return this.valueChangeOperators[index];
    }
    
    public double getValueChangeRate(int index){
        return this.valueChangeRates[index];
    }

}
