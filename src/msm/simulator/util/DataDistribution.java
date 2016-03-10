/**
 * 
 */
package msm.simulator.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * @author pcjesus
 *
 */
public class DataDistribution implements Cloneable {
    
    public enum DD_TYPE {HISTOGRAM, CDF};
    private TreeMap<BigDecimal, BigDecimal> data;
    private DD_TYPE type;
    private BigDecimal min;
    private BigDecimal max;
    
    /*
     * 
     * Constructors
     * 
     */
    
    public DataDistribution(DD_TYPE type){
        this.type = type;
        this.data = new TreeMap<BigDecimal, BigDecimal>();
        this.min = null;
        this.max = null;
    }
    
    public DataDistribution(DataDistribution dd){
    	this.type = dd.getType();
    	this.data = new TreeMap<BigDecimal, BigDecimal> (dd.getData());
    	this.min = (dd.getMin() == null)?null:new BigDecimal(dd.getMin().toString());
    	this.max = (dd.getMax() == null)?null:new BigDecimal(dd.getMax().toString());
    }
    
    public DataDistribution(DD_TYPE type, Collection<BigDecimal> labels){
        this.type = type;
        this.data = new TreeMap<BigDecimal, BigDecimal>();
        for(BigDecimal l : labels){
            this.data.put(l, BigDecimal.ZERO);
        }
        this.min = null;
        this.max = null;
    }
    
    public DataDistribution(DD_TYPE type, Collection<BigDecimal> labels, BigDecimal min, BigDecimal max){
        this.type = type;
        this.data = new TreeMap<BigDecimal, BigDecimal>();
        for(BigDecimal l : labels){
            this.data.put(l, BigDecimal.ZERO);
        }
        this.min = min;
        this.max = max;
    }
    
    
    
    public void addEntry(BigDecimal label, BigDecimal frequency){
        this.data.put(label, frequency);
    }
    
    public void removeEntry(BigDecimal label) {
    	this.data.remove(label);
    }
    
    public BigDecimal getValue(BigDecimal label){
        return this.data.get(label);
    }
    
    
    /**
     * Convert CDF data distribution to Histogram.
     * 
     * Note: Minimum must be defined
     * 
     * @param minFrequency frequency of the minimum
     */
    public void convertCDF2Histogram(BigDecimal minFrequency){
        
        TreeMap<BigDecimal, BigDecimal> cdf = (TreeMap<BigDecimal, BigDecimal>) this.data.clone();
        
        BigDecimal prevLabel = this.getMin();
        BigDecimal prevFreq = minFrequency;
        this.data.put(prevLabel, prevFreq);
        
        
        for(BigDecimal label : cdf.keySet()){
            BigDecimal freq = cdf.get(label);
            this.data.put(label, freq.subtract(prevFreq));
            prevLabel = label;
            prevFreq = freq;
        }
        
        this.setType(DD_TYPE.HISTOGRAM);
        
    }
    
    
    public void convertFrequencies2Fractions(BigDecimal populationSize, MathContext mc){
        
        for(BigDecimal label : this.data.keySet()){
            this.data.put(label, this.data.get(label).divide(populationSize, mc));
        }
    }
    
    
    public BigDecimal getSmallerClosestLabel(BigDecimal label){
        
        BigDecimal result = this.data.firstKey();
        for(BigDecimal l : this.data.keySet()){
            if(l.compareTo(label) <= 0){
                result = l;
            } else {
                break;
            }
        }
        
        return result;
    }
    
    public BigDecimal getBiggerClosestLabel(BigDecimal label){
        
        BigDecimal result = this.data.lastKey();
        for(BigDecimal l : this.data.descendingKeySet()){
            if(l.compareTo(label) >= 0){
                result = l;
            } else {
                break;
            }
        }
        
        return result;
    }

    public BigDecimal getAbsoluteClosestLabel(BigDecimal label){
    
    	return this.getBiggerClosestLabel(label).min(this.getSmallerClosestLabel(label));
    }
    
    
    public void addCDFValue(BigDecimal label, MathContext mc){
        
        //Compute CDF value for label
        BigDecimal f = this.data.get(label);
        if(f == null){
            
            BigDecimal prevLabel = this.data.lowerKey(label);
            if(prevLabel == null){
                f = BigDecimal.ONE;
            } else {
                f = this.data.get(prevLabel).add(BigDecimal.ONE, mc);
            }
            
        } else {
            
            f = f.add(BigDecimal.ONE, mc);
            
        }
        
        //Add label with respective frequency
        this.data.put(label, f);
        
        //Update (increment) frequencies of tail values
        for(BigDecimal l : this.data.tailMap(label, false).keySet()){
            BigDecimal newF = this.data.get(l).add(BigDecimal.ONE, mc);
            this.data.put(l, newF);
        }
        
    }
    
    
    public BigDecimal maxDiffLabel(MathContext mc){
        
        BigDecimal maxDiff = new BigDecimal(-Double.MAX_VALUE);
        BigDecimal maxLabel = firstLabel();
        NavigableMap<BigDecimal, BigDecimal> entries = this.data.tailMap(this.data.lastKey(), false);
        
        for(BigDecimal currentLabel :  entries.keySet()){
            Entry<BigDecimal, BigDecimal> previousEntry = this.data.lowerEntry(currentLabel);
            BigDecimal previousValue = previousEntry.getValue();
            BigDecimal currentValue = this.data.get(currentLabel);
            
            BigDecimal diff = (currentValue.subtract(previousValue, mc)).abs(mc);
            
            if(diff.compareTo(maxDiff) > 0){
                maxDiff = diff;
                maxLabel = currentLabel;
            }
        }
        
        
        return maxLabel;
        
    }
    
    
    public BigDecimal minDiff(MathContext mc){
        
        BigDecimal minDiff = new BigDecimal(Double.MAX_VALUE);
        BigDecimal minLabel = firstLabel();
        NavigableMap<BigDecimal, BigDecimal> entries = this.data.subMap(this.data.firstKey(), false, this.data.lastKey(), false);
        
        for(BigDecimal currentLabel :  entries.keySet()){
            Entry<BigDecimal, BigDecimal> nextEntry = this.data.higherEntry(currentLabel);
            Entry<BigDecimal, BigDecimal> previousEntry = this.data.lowerEntry(currentLabel);
            BigDecimal nextValue = nextEntry.getValue();
            BigDecimal previousValue = previousEntry.getValue();
            
            BigDecimal diff = (nextValue.subtract(previousValue, mc)).abs(mc);
            
            if(diff.compareTo(minDiff) < 0){
                minDiff = diff;
                minLabel = currentLabel;
            }
        }
        
        
        return minLabel;
        
    }
    
   


    /*
     * 
     * Getters/Setters
     * 
     */
    
    
    /**
     * @return the frequencies
     */
    public Collection<BigDecimal> getFrequencies() {
        return this.data.values();
    }

    
    /**
     * @return the labels
     */
    public Set<BigDecimal> getLabels() {
        return this.data.keySet();
    }
    
    public NavigableSet<BigDecimal> getDescendingLabels() {
        return this.data.descendingKeySet();
    }
    
    public SortedMap<BigDecimal, BigDecimal> getLabelsSorted(BigDecimal min){
    	return this.data.tailMap(min);
    }


    public int size(){
        return this.data.size();
    }
    
    
    public Set<BigDecimal> headLabels(BigDecimal toLabel){
        return (this.data.headMap(toLabel)).keySet();
    }
    
    public BigDecimal lastLabel(){
        return this.data.lastKey();
    }
    
    public BigDecimal firstLabel(){
        return this.data.firstKey();
    }
    
    public BigDecimal higherLabel(BigDecimal label){
        return this.data.higherKey(label);
    }
    
    public BigDecimal lowerLabel(BigDecimal label){
        return this.data.lowerKey(label);
    }
    
    
    public NavigableMap<BigDecimal, BigDecimal> headDD(BigDecimal toLabel, boolean inclusive){
        return this.data.headMap(toLabel, inclusive);
    }
    
    public NavigableMap<BigDecimal, BigDecimal> tailDD(BigDecimal fromLabel, boolean inclusive){
        return this.data.tailMap(fromLabel, inclusive);
    }
    
    public NavigableMap<BigDecimal, BigDecimal> subDD(BigDecimal fromLabel, boolean fromInclusive, BigDecimal toLabel, boolean toInclusive){
        return this.data.subMap(fromLabel, fromInclusive, toLabel, toInclusive);
    }
    
    public Entry<BigDecimal, BigDecimal> higherEntry(BigDecimal label){
        return this.data.higherEntry(label);
    }
    
    public Entry<BigDecimal, BigDecimal> lowerEntry(BigDecimal label){
        return this.data.lowerEntry(label);
    }
    
    /**
     * @return the type
     */
    public DD_TYPE getType() {
        return type;
    }


    
    /**
     * @param type the type to set
     */
    public void setType(DD_TYPE type) {
        this.type = type;
    }


    
	public void clear() {
		this.data.clear();
		
	}
	
	public TreeMap<BigDecimal, BigDecimal> getData(){
		return this.data;
	}
	
	public void setData(TreeMap<BigDecimal, BigDecimal> data){
		this.data = data;
	}
	
	
    /**
     * @return the min
     */
    public BigDecimal getMin() {
        return min;
    }

    
    /**
     * @param min the min to set
     */
    public void setMin(BigDecimal min) {
        this.min = min;
    }

    
    /**
     * @return the max
     */
    public BigDecimal getMax() {
        return max;
    }

    
    /**
     * @param max the max to set
     */
    public void setMax(BigDecimal max) {
        this.max = max;
    }
    
    

    public String toString(){
		StringBuffer sb = new StringBuffer();
		
		sb.append("DD [");
		
		for (BigDecimal l : this.getLabels()) {
			sb.append(l.toString() + ":" + this.getValue(l).toString() + "; ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	
	public Object clone(){
	    return new DataDistribution(this);
	}
	
	public BigDecimal getMaxFreq(){
		BigDecimal result = new BigDecimal(-Double.MAX_VALUE);
		for (BigDecimal f : this.getFrequencies()){
			result = result.max(f);
		}
		return result;
	}
	
	public BigDecimal sumFreq() {
		BigDecimal result = null;
		for(BigDecimal i : this.getFrequencies()) {
			result = result.add(i);
		}
		return result;
	}
	
	public BigDecimal averageFreq(){
		return this.sumFreq().divide(new BigDecimal(this.data.size()));
	}
	
}
