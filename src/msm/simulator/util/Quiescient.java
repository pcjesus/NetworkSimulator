/**
E * MSM - Network Simulator
 */
package msm.simulator.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import msm.simulator.util.DataDistribution.*;

/**
 * CDF estimate quiescient state detection
 * 
 * @author mborges
 * 
 */

public class Quiescient {
	private Integer maxObservations = 2;
	private Integer observationsCounter = 0;
	private BigDecimal epsilonLow = new BigDecimal(0.10);
	private BigDecimal epsilonHigh = new BigDecimal(0.15);
	private BigDecimal currentDelta = null;
	private BigDecimal previousDelta = null;
	private BigDecimal currentEpsilon = null;
	private boolean quiescient = false;
	private MathContext mc = new MathContext(21, RoundingMode.HALF_DOWN);

	private static enum Q_ASSESS {
		MAX, AVG
	};

	private DataDistribution currentObservation = null;
	private DataDistribution previousObservation = null;
	private DataDistribution currentCDFDifference = null;

	// private DataDistribution differences = new DataDistribution(DD_TYPE.CDF);

	/**
	 * 
	 */
	public Quiescient(Integer maxObservations, BigDecimal epsilonLow,
			BigDecimal epsilonHigh) {
		//assert maxObservations > 1;
		assert epsilonLow.compareTo(BigDecimal.ZERO) > 0;
		assert epsilonHigh.compareTo(BigDecimal.ZERO) > 0;
		this.maxObservations = maxObservations;
		this.epsilonLow = epsilonLow;
		this.epsilonHigh = epsilonHigh;
	}

	// TODO inferred deltas
	public Quiescient(Integer maxObservations) {
		assert maxObservations > 1;
	}

	/**
	 * 
	 * @param dd
	 * @return
	 */
	public boolean setObservation(DataDistribution dd, int samplesSize) {
		this.currentObservation = (DataDistribution) dd.clone();

		if (this.previousObservation != null
				/*&& this.previousObservation.size() == samplesSize
				&& this.currentObservation.size() == samplesSize*/
				&& this.currentObservation.getLabels().equals(
						this.previousObservation.getLabels())) {
			//this.computeQuiesciency(Q_ASSESS.MAX);
			this.computeQuiesciencySimple();
			
		}
		
		
		
		this.previousObservation = (DataDistribution) this.currentObservation
		.clone();
				
		return this.quiescient;
	}

	/**
	 * 
	 * @param assessMaxOnly
	 */
	private void computeQuiesciency(Q_ASSESS assessMaxOnly) {
		this.currentCDFDifference = absoluteSubtractDD(
				this.currentObservation, this.previousObservation);

		this.currentDelta = (assessMaxOnly == Q_ASSESS.MAX) ? this.currentCDFDifference
				.getMaxFreq() : this.currentCDFDifference.averageFreq();

		if (this.previousDelta != null) {
			this.currentEpsilon = this.previousDelta.subtract(this.currentDelta, this.mc)
					.divide(this.currentDelta, this.mc).abs(this.mc);
			if (!this.quiescient) {
				if (this.currentEpsilon.compareTo(this.epsilonLow) <= 0) {
					this.observationsCounter++;
				} else {
					this.observationsCounter = 0;
				}
			} else { // is quiescient
				if (this.currentEpsilon.compareTo(this.epsilonHigh) > 0) {
					this.observationsCounter++;
				} else {
					this.observationsCounter = 0;
				}
			}

			if (this.observationsCounter >= this.maxObservations
					&& this.quiescient == false) {
				this.quiescient = true;
				this.currentEpsilon = null;
				this.observationsCounter = 0;
			} else if (this.observationsCounter >= this.maxObservations
					&& this.quiescient == true) {
				this.quiescient = false; // wakes up
				this.observationsCounter = 0;
				this.currentEpsilon = null;
			}
		} 
		
		this.previousDelta = this.currentDelta;

		// error assessment: e = (|e^t-1 - e^t|/ e^t)

	}
	
	private void computeQuiesciencySimple(){
		this.currentCDFDifference = absoluteSubtractDD(
				this.currentObservation, this.previousObservation);

		this.currentDelta = this.currentCDFDifference.getMaxFreq();

		if (this.previousDelta != null) {
			this.currentEpsilon = this.previousDelta.subtract(this.currentDelta, this.mc)
			.divide(this.currentDelta, this.mc).abs(this.mc);
			
			if (!this.quiescient && this.currentEpsilon.compareTo(this.epsilonLow) <= 0) {
				this.quiescient = true;
				this.currentEpsilon = null;
			} else if (this.quiescient && this.currentEpsilon.compareTo(this.epsilonHigh) > 0) {
				this.quiescient = false;
				this.currentEpsilon = null;
			}
		}
		
		this.previousDelta = this.currentDelta;

		// error assessment: e = (|e^t-1 - e^t|/ e^t)
	}

	/**
	 * 
	 * @return
	 */
	public boolean isQuiescient() {
		return this.quiescient;
	}

	/**
	 * 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("State:" + this.isQuiescient()
				+ "\n\t Consecutive obs within delta:"
				+ this.observationsCounter + "\n\t Current obs:"
				+ this.currentObservation + "\n\t Prev.   obs:"
				+ this.previousObservation + "\n\t Curr.  diff:"
				+ this.currentCDFDifference + "\n\t Max.   diff:"
				+ this.currentDelta + "\n\t Curr. error:" + this.currentEpsilon);

		return sb.toString();
	}

	private DataDistribution absoluteSubtractDD(DataDistribution a,
			DataDistribution b) {
		
		assert a.getLabels().equals(b.getLabels());
		
		DataDistribution c = new DataDistribution(DD_TYPE.CDF);
		for (BigDecimal label : a.getLabels()) {
			c.addEntry(
					label,
					a.getValue(label).subtract(b.getValue(label), this.mc)
							.abs(this.mc));
		}
		return c;
	}

}
