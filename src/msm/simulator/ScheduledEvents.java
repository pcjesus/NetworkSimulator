/**
 * 
 */
package msm.simulator;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import msm.simulator.Event.EventType;


/**
 * @author pcjesus
 *
 */
public class ScheduledEvents {
    
    //Scheduled events
    private TreeMap<Integer, Set<Event<?>>> events;
    
    
    /**
     * CONSTRUCTORS
     */
    
    
    public ScheduledEvents(){
        
        //Create an empty event structure
        this.events = new TreeMap<Integer, Set<Event<?>>>();
    }
    
    
    /**
     * Constructor with an initial event
     * @param event
     */
    public ScheduledEvents(Event<?> event){
        
        //Create an event structure, containing the event passed as parameter
        this.events = new TreeMap<Integer, Set<Event<?>>>();
        Set<Event<?>> eventSet = new HashSet<Event<?>>();
        eventSet.add(event);
        this.events.put(new Integer(event.getTime()), eventSet);
    }
    
    
    /**
     * Add (schedule) a new event
     * 
     * @param event to be added
     * @return event key (global time value)
     */
    public Integer add(Event<?> event){
        Integer tKey = event.getTime();
        
        //Get events scheduled for the same time
        Set<Event<?>> evtSet = this.events.get(tKey);
        
        //No event already scheduled for the given time
        if(evtSet == null){
            
            //Create a new event set and add the event
            Set<Event<?>> eventSet = new HashSet<Event<?>>();
            eventSet.add(event);
            this.events.put(new Integer(tKey), eventSet);
        
        //Event set for the same time already exist
        } else {
            
            //Add the new event
            evtSet.add(event);
        }
        
        return tKey; 
        
    }

    
    /**
     * Get the next set of events, scheduled at the (earliest) same time
     * 
     * @return Next scheduled set of events (for the earliest time) 
     */
    public Set<Event<?>> getNext(){
        
        return (this.events.firstEntry()).getValue();
    }
    
    
    /**
     * Remove the next set of events, scheduled at the same (earliest) time
     * @return Next scheduled set of events (for the earliest time)
     */
    public Set<Event<?>> removeNext(){
        
        return this.events.remove(this.events.firstKey());
    }
    
    
    /**
     * Pull (get and remove) the next set of events, scheduled at the same (earliest) time
     * @return Next scheduled set of events (for the earliest time)
     */
    public Set<Event<?>> pullNext(){
        
        return (this.events.pollFirstEntry()).getValue();
    }
    
    
    /**
     * Return the time of the first occurring set of events
     * 
     * @return time of the first set of events
     */
    public int getTimeOfNextEvents(){
        return this.events.firstKey();
    }
    
    
    /**
     * Return the time of the last occurring set of events
     * 
     * @return time of the last set of events
     */
    public int getTimeOfLastEvents(){
        return this.events.lastKey();
    }
    
    
    /**
     * Return the number of similar events (same type and node Id) scheduled for the same time
     * 
     * @param time when the event is scheduled
     * @param evtType type of the event
     * @param nodeId Id of the node associated to the event
     * 
     * @return number of similar events scheduled for the same time
     */
    public int numberOfSimilarEventAtSameTime(int time, EventType evtType, String nodeId) {
        
        //Get events scheduled for the same time
        Set<Event<?>> evtSet = this.events.get(time);
        
        //No event already scheduled for the given time
        if(evtSet == null){
            
            return 0;
        
        //Event set for the same time already exist
        } else {
            
            int count = 0;
            
            for(Event<?> evt : evtSet){
                if((evt.getType() == evtType) && evt.getNodeID().equals(nodeId)){
                    count++;
                }
            }
            
            return count;
        }
        
    }
    
    /**
     * Return the number of events scheduled for the same time
     * 
     * @param time when the event is scheduled
     * 
     * @return number of events scheduled for the same time
     */
    public int numberOfEventAtSameTime(int time) {
        
        //Get events scheduled for the same time
        Set<Event<?>> evtSet = this.events.get(time);
        
        //No event already scheduled for the given time
        if(evtSet == null){
            
            return 0;
        
        //Event set for the same time already exist
        } else {
            
            return evtSet.size();
        }
        
    }
    
    
    /**
     * Remove event matching the one given as parameter.
     * 
     * @param event to remove
     * 
     * @return true if the event exists (successfully removed), false otherwise
     */
    public boolean removeEvent(Event<?> event){
        
        Integer tKey = event.getTime();
        
        //Get events scheduled for the same time
        Set<Event<?>> evtSet = this.events.get(tKey);
        
        //No event scheduled for the given time
        if(evtSet == null){
            
            return false;
        
        //Existing events for the given time
        } else {
            
            //Search for event and remove it
            for(Event<?> evt : evtSet){
            
                if(evt.match(event)){
                    
                    //Event found
                    boolean result = evtSet.remove(evt);
                    if(evtSet.isEmpty()){
                        //Remove events entry if set is empty
                        this.events.remove(tKey);
                    }
                    return result;
                }
            }
            
            //Event not found
            return false;
        }
        
    }
    
    
    public int size(){
        return this.events.size();
    }
    
    
    public void clear(){
        this.events.clear();
    }
    

}
