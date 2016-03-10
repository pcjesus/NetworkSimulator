/**
 * DC - Network Simulator
 */
package msm.simulator.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import msm.simulator.exceptions.NetStatisticsException;
import msm.simulator.network.Node;
import msm.simulator.util.DataDistribution.DD_TYPE;


/**
 * @author pcjesus
 *
 */
public class NetStatistics {
    
    public enum AggFunctions {AVG, COUNT, MAX, MIN, SUM}

    
    /**
     * @deprecated
     */
    public static BigDecimal calculateCorrectValue(AggFunctions function, Collection<Node> nodes, MathContext mathContext) {
/*        
        switch(function) {
            case AVG    : return calculateAverage(nodes, mathContext);
                          
            case COUNT  : return calculateCount(nodes, mathContext);
                          
            default : 
                return calculateCount(nodes, mathContext);
        }
*/        
        //return calculateAverageOfEstimatedValues(nodes, mathContext);
        return calculateTrueAverage(nodes, mathContext);
        

    }
    
    
    public static BigDecimal calculateInitTrueValue(AggFunctions aggfunc, Collection<Node> nodes, MathContext mathContext) 
      throws NetStatisticsException {
        
        switch (aggfunc) {
            case AVG:
                return calculateTrueAverage(nodes, mathContext);
            case COUNT:
                return calculateTrueCount(nodes, mathContext);
            case SUM:
                return calculateTrueSum(nodes, mathContext);

            default:
                throw new NetStatisticsException("Invalid Aggregation Function! Supported fuctions:" + AggFunctions.values());
        }
        
        
    }
    
    
    private static BigDecimal calculateAverageOfEstimatedValues(Collection<Node> nodes, MathContext mathContext){
        BigDecimal s = BigDecimal.ZERO;
        for (Node node : nodes) {
            
            //s = s + value
            s = s.add(node.getApplication().getValue(), mathContext);
        }
        BigDecimal n = new BigDecimal(nodes.size(), mathContext);
        
        return s.divide(n, mathContext);
    }
    
    
    private static BigDecimal calculateTrueAverage(Collection<Node> nodes, MathContext mathContext){
        BigDecimal s = BigDecimal.ZERO;
        for (Node node : nodes) {
            
            //s = s + init value
            s = s.add(node.getApplication().getInitValue(), mathContext);
        }
        BigDecimal n = new BigDecimal(nodes.size(), mathContext);
        
        return s.divide(n, mathContext);
    }
    
    
    
    
  
    private static BigDecimal calculateTrueCount(Collection<Node> nodes, MathContext mathContext){
        
        return new BigDecimal(nodes.size(), mathContext);
    }
    
    
    public static BigDecimal calculateTrueSum(Collection<Node> nodes, MathContext mathContext) {
        BigDecimal s = BigDecimal.ZERO;
        for (Node n : nodes) {
            //s = s + value
            s = s.add(n.getApplication().getInitValue(), mathContext);
        }
        return s;
    }

    
    
    public static BigDecimal sumNodeValues(Collection<Node> nodes, MathContext mathContext) {
        BigDecimal s = BigDecimal.ZERO;
        for (Node n : nodes) {
            //s = s + value
            s = s.add(n.getApplication().getValue(), mathContext);
        }
        return s;
    }
    
    
    public static BigDecimal sumNodeSquaredValues(Collection<Node> nodes, MathContext mathContext) {
        BigDecimal ss = BigDecimal.ZERO;
        for (Node n : nodes) {
            //ss = ss + value * Value
            ss = ss.add((n.getApplication().getValue()).multiply(n.getApplication().getValue(), mathContext), mathContext);
        }
        return ss;
    }
    
    
    public static BigDecimal sumNodeSquaredErrors(Collection<Node> nodes, BigDecimal trueAgg, MathContext mathContext) {
        BigDecimal ss = BigDecimal.ZERO;
        for (Node n : nodes) {
            //e = value - trueAgg
            BigDecimal e = (n.getApplication().getValue()).subtract(trueAgg, mathContext);
            //ss = ss + e * e
            ss = ss.add(e.multiply(e, mathContext), mathContext);
        }
        return ss;
    }
    
    public static BigDecimal sumNodeSquaredErrors_Count(Collection<Node> nodes, BigDecimal trueCount, MathContext mathContext) {
        BigDecimal ss = BigDecimal.ZERO;
        
        for (Node n : nodes) {
            //e = value - trueAgg
            BigDecimal e = (BigDecimal.ONE).divide(n.getApplication().getValue(), mathContext);
            e = e.subtract(trueCount, mathContext);
            //ss = ss + e * e
            ss = ss.add(e.multiply(e, mathContext), mathContext);
        }
        return ss;
    }
    
    
    public static BigDecimal nodeVariance(Collection<Node> nodes, BigDecimal value, MathContext mathContext) {
        BigDecimal a = calculateAverageOfEstimatedValues(nodes, mathContext);
        //(v - a)^2
        return (value.subtract(a, mathContext)).pow(2, mathContext);
    }
    
    public static BigDecimal nodeDeviation(Collection<Node> nodes, BigDecimal value, MathContext mathContext) {
        BigDecimal a = calculateAverageOfEstimatedValues(nodes, mathContext);
        //sqrt((v - a)^2)
        //return new BigDecimal(Math.sqrt((value.subtract(a, mathContext)).pow(2, mathContext).doubleValue()), mathContext);
        return BigDecimal.valueOf(Math.sqrt((value.subtract(a, mathContext)).pow(2, mathContext).doubleValue()));
    }
    
    
    public static BigDecimal nodeLocalEstimateVariance(Collection<Node> nodes, List<BigDecimal> values, MathContext mathContext) {
        BigDecimal a = calculateTrueAverage(nodes, mathContext);
        
        BigDecimal cubeDiff = BigDecimal.ZERO;
        for(BigDecimal value : values){
            //cubeDiff = cubeDiff + (v - a)^2
            cubeDiff = cubeDiff.add(value.subtract(a, mathContext).pow(2, mathContext), mathContext);
        }
        return cubeDiff;
    }
    
    public static BigDecimal nodeLocalEstimateDeviation(Collection<Node> nodes, Collection<BigDecimal> values, MathContext mathContext) {
        BigDecimal a = calculateTrueAverage(nodes, mathContext);
        
        BigDecimal cubeDiff = BigDecimal.ZERO;
        for(BigDecimal value : values){
            //cubeDiff = cubeDiff + (v - a)^2
            cubeDiff = cubeDiff.add(value.subtract(a, mathContext).pow(2, mathContext), mathContext);
        }
        //sqrt(cubeDiff)
        //return new BigDecimal(Math.sqrt(cubeDiff.doubleValue()), mathContext);
        return BigDecimal.valueOf(Math.sqrt(cubeDiff.doubleValue()));
    }
    
    
    /**
     * 
     */
    
    public static BigDecimal sumValues(Collection<BigDecimal> values, MathContext mathContext) {
        BigDecimal s = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            //s = s + value
            s = s.add(v, mathContext);
        }
        return s;
    }


    public static BigDecimal sumSquaredValues(Collection<BigDecimal> values, MathContext mathContext) {
        BigDecimal s = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            //s = s + value^2
            s = s.add(v.pow(2, mathContext), mathContext);
        }
        return s;
    }
    
    public static BigDecimal squaredSumOfValues(Collection<BigDecimal> values, MathContext mathContext) {
        BigDecimal s = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            //s = s + value
            s = s.add(v, mathContext);
        }
        //s^2
        return s.pow(2, mathContext);
    }
    
    
    public static BigDecimal sumSquaredError(Collection<BigDecimal> values, BigDecimal trueAgg, MathContext mathContext) {
        BigDecimal ss = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            //e = value - trueAgg
            BigDecimal e = v.subtract(trueAgg, mathContext);
            //ss = ss + e * e
            ss = ss.add(e.multiply(e, mathContext), mathContext);
        }
        return ss;
    }
    
    public static BigDecimal calculateAverage(Collection<BigDecimal> values, MathContext mathContext) {
        BigDecimal s = BigDecimal.ZERO;
        for (BigDecimal v : values) {

            // s = s + value
            s = s.add(v, mathContext);
        }
        BigDecimal n = new BigDecimal(values.size(), mathContext);

        return s.divide(n, mathContext);
    }
    
    
    public static BigDecimal calculatePotential(Collection<BigDecimal> values, MathContext mathContext) {

        BigDecimal a = calculateAverage(values, mathContext);
        return sumSquaredError(values, a, mathContext);
    }
    
    
    public static BigDecimal maximum(BigDecimal[] values){
        
        BigDecimal max = values[0];
        
        for(BigDecimal v : values){
            if(v.compareTo(max) > 0){
                max = v;
            }
        }
        
        return max;
    }
    
    public static BigDecimal minimum(BigDecimal[] values){
        
        BigDecimal min = values[0];
        
        for(BigDecimal v : values){
            if(v.compareTo(min) < 0){
                min = v;
            }
        }
        
        return min;
    }
    
    
    public static ArrayList<BigDecimal> setEquiWidthIntervals(Collection<BigDecimal> values, int numIntervals, MathContext mathContext){
        
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(numIntervals+1);
        
        BigDecimal min = NetStatistics.minimum(values.toArray(new BigDecimal[0]));
        BigDecimal max = NetStatistics.maximum(values.toArray(new BigDecimal[0]));
        BigDecimal dist = max.subtract(min, mathContext);
        BigDecimal intervalSize = dist.divide(new BigDecimal(numIntervals), mathContext);
        
        result.add(min);
        for(int i=0; i < (numIntervals-1); i++){
            result.add(result.get(i).add(intervalSize, mathContext));
        }
        result.add(max);
        
        return result;
        
    }
    
    
    public static DataDistribution setEquiWidthCDF(Collection<BigDecimal> values, int numIntervals, MathContext mathContext){
        
        DataDistribution result = new DataDistribution(DD_TYPE.CDF);        
        BigDecimal min = NetStatistics.minimum(values.toArray(new BigDecimal[0]));
        BigDecimal max = NetStatistics.maximum(values.toArray(new BigDecimal[0]));
        
        BigDecimal dist = max.subtract(min, mathContext);
        BigDecimal intervalSize = dist.divide(new BigDecimal(numIntervals), mathContext);
        
        result.setMin(min);
        result.setMax(max);
        
        //Create an ordered map with input values
        SortedMap<BigDecimal, Integer> orderedValues = new TreeMap<BigDecimal, Integer>();
        for(BigDecimal value : values){
            Integer count = orderedValues.get(value);
            if(count == null){
                orderedValues.put(value, 1);
            } else {
                orderedValues.put(value, count + 1);
            }
        }
        
        //Create EquiWidth data distribution
        int tempF = 0;
        boolean isLast = false;
        BigDecimal label = min.add(intervalSize, mathContext);
        for(BigDecimal v : orderedValues.keySet()){
            
            if(!isLast && v.compareTo(label) > 0){
                result.addEntry(label, new BigDecimal(tempF));
                label = label.add(intervalSize, mathContext);
                if((max.subtract(label)).compareTo(intervalSize) < 0){
                    isLast = true;
                }
            }
            
            tempF = tempF + orderedValues.get(v);
        }
        
        result.addEntry(max, new BigDecimal(tempF));
        
        return result;
        
    }
    
    
    
    
    public static DataDistribution setEquiWidthHistogram(Collection<BigDecimal> values, int numIntervals, MathContext mathContext){
        
        DataDistribution result = setEquiWidthCDF(values, numIntervals, mathContext);
        BigDecimal min = result.getMin();
        int minFrequency = 0;
        for(BigDecimal v : values){
            if(v.compareTo(min) == 0){
                minFrequency = minFrequency + 1;
            }
        }
        result.convertCDF2Histogram(new BigDecimal(minFrequency));
        
        
        return result;
    }
    
    
    public static DataDistribution setEquiDepthCDF(Collection<BigDecimal> values, int numIntervals, MathContext mathContext){
                
        DataDistribution result = new DataDistribution(DD_TYPE.CDF);
        
        //Get absolute frequency for each interval
        double freq = (double)values.size() / (double)numIntervals;
        
        //Create an ordered map with input values
        SortedMap<BigDecimal, Integer> orderedValues = new TreeMap<BigDecimal, Integer>();
        for(BigDecimal value : values){
            Integer count = orderedValues.get(value);
            if(count == null){
                orderedValues.put(value, 1);
            } else {
                orderedValues.put(value, count + 1);
            }
        }
        
        //Create EquiDepth data distribution
        double cumulativeF = freq;
        int tempF = 0;
        int prevF = 0;
        BigDecimal label = orderedValues.firstKey();
        for(BigDecimal v : orderedValues.keySet()){
            
            tempF = tempF + orderedValues.get(v);
            
            if(tempF > cumulativeF){
                result.addEntry(label, new BigDecimal(prevF));
                cumulativeF = cumulativeF + freq;
            }
            
            label = v;
            prevF = tempF;
        }
        result.addEntry(label, new BigDecimal(tempF));
        
        return result;
    }
    
    
    public static DataDistribution setEquiDepthHistogram(Collection<BigDecimal> values, int numIntervals, MathContext mathContext){
        
        DataDistribution result = setEquiDepthCDF(values, numIntervals, mathContext);
        BigDecimal min = NetStatistics.minimum(values.toArray(new BigDecimal[0]));
        result.setMin(min);
        int minFrequency = 0;
        for(BigDecimal v : values){
            if(v.compareTo(min) == 0){
                minFrequency = minFrequency + 1;
            }
        }
        result.convertCDF2Histogram(new BigDecimal(minFrequency));
        
        
        return result;
    }
    

    public static long[] setCDF(Collection<BigDecimal> values, List<BigDecimal> intervals, MathContext mathContext){
        
        long[] result = new long[intervals.size()];
        for(int i=0; i < result.length; i++){
            result[i] = 0;
        }
        
        for(BigDecimal v : values){
            int i = intervals.size() - 1;
            while((i >= 0) && (v.compareTo(intervals.get(i)) <= 0)){
                result[i] = result[i] + 1;
                i--;
            }
            
        }
        
        return result;
    }

    public static ArrayList<BigDecimal> setHistogramFrequencies(Collection<BigDecimal> values, List<BigDecimal> intervals, MathContext mathContext){
        
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(intervals.size());
        
        long[] cdf = NetStatistics.setCDF(values, intervals, mathContext);
        
        result.add(new BigDecimal(cdf[0]));
        for(int i=1; i < intervals.size(); i++){
            result.add(new BigDecimal(cdf[i]-cdf[i-1]));
        }
        
        return result;
        
    }
    
    public static ArrayList<BigDecimal> setHistogramFractions(Collection<BigDecimal> values, List<BigDecimal> intervals, MathContext mathContext){
        
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(intervals.size());
        
        long[] cdf = NetStatistics.setCDF(values, intervals, mathContext);
        
        double size = values.size();
        //result.add(new BigDecimal((double)cdf[0]/size));
        result.add(BigDecimal.valueOf((double)cdf[0]/size));
        for(int i=1; i < intervals.size(); i++){
            //result.add(new BigDecimal((double)(cdf[i]-cdf[i-1])/size, mathContext));
            result.add(BigDecimal.valueOf((double)(cdf[i]-cdf[i-1])/size));
        }
        
        return result;
        
    }
    
    
    public static ArrayList<BigDecimal> setCDFFrequencies(Collection<BigDecimal> values, List<BigDecimal> intervals, MathContext mathContext){
        
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(intervals.size());
        
        long[] cdf = NetStatistics.setCDF(values, intervals, mathContext);
        
        for(int i=0; i < intervals.size(); i++){
            result.add(new BigDecimal(cdf[i]));
        }
        
        return result;
    }
    
    
    public static ArrayList<BigDecimal> setCDFFractions(Collection<BigDecimal> values, List<BigDecimal> intervals, MathContext mathContext){
        
        ArrayList<BigDecimal> result = new ArrayList<BigDecimal>(intervals.size());
        
        long[] cdf = NetStatistics.setCDF(values, intervals, mathContext);
        
        double size = values.size();
        for(int i=0; i < intervals.size(); i++){
            //result.add(new BigDecimal((double)cdf[i]/size, mathContext));
            result.add(BigDecimal.valueOf((double)cdf[i]/size));
        }
        
        return result;
    }
    
 
}
