/**
 * MSM - Network Simulator
 */

package msm.simulator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Set Utilities
 * 
 * @author pcjesus
 * @version 1.0
 */

public class SetUtils {

    /**
     * Return a List randomizing a given Set elements.
     * @param <T>
     * 
     * @param objSet Set of object to randomize.
     * 
     * @return List of given Set elements in a random sequence.
     * 
     */
    
    public static <T> List<T> randomizeSet(Set<T> objSet){
        List<T> randomList = new ArrayList<T>(objSet); 
        Collections.shuffle(randomList);
        return randomList;
    }
    
    
    public static <T extends Comparable<? super T>> List<T> sortSet(Set<T> objSet){
        List<T> list = new ArrayList<T>(objSet); 
        Collections.sort(list);
        return list;
    }
    
    public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
        Set<T> result = new HashSet<T>();
        for (T x : setA)
          if (setB.contains(x))
              result.add(x);
        return result;
    }
    
    public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
        Set<T> result = new HashSet<T>();
        result.addAll(setB);
        return result;
    }
    
    public static <T> Set<T> difference(Set<T> setA, Set<T> setB) {
        Set<T> result = new HashSet<T>(setA);
        result.removeAll(setB);
        return result;
    }
    
    
}
