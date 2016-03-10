/**
 * MSM - Network Simulator
 */

package msm.simulator.util;



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;


/**
 * Rating Calculation
 * 
 * @author pcjesus
 * @version 1.0
 */


public class RatingCalc {


    public static double multiply(double n1, double n2, int precision) {
        MathContext mc = new MathContext(precision);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.multiply(b, mc);

        return c.doubleValue();
    }
    
    public static double multiply(double n1, double n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.multiply(b, mc);

        return c.doubleValue();
    }
    
    public static double multiply(double n1, double n2, MathContext mc) {
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.multiply(b, mc);

        return c.doubleValue();
    }
    
    
    public static BigDecimal multiply(BigDecimal n1, BigDecimal n2, int precision) {
        MathContext mc = new MathContext(precision);
        return n1.multiply(n2, mc);
    }
    
    public static BigDecimal multiply(BigDecimal n1, BigDecimal n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        return n1.multiply(n2, mc);
    }
    
    public static BigDecimal multiply(BigDecimal n1, BigDecimal n2, MathContext mc) {
        return n1.multiply(n2, mc);
    }
    

    public static double divide(double n1, double n2, int precision) {
        MathContext mc = new MathContext(precision);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.divide(b, mc);

        return c.doubleValue();
    }
    
    public static double divide(double n1, double n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.divide(b, mc);

        return c.doubleValue();
    }
    
    public static double divide(double n1, double n2, MathContext mc) {
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.divide(b, mc);

        return c.doubleValue();
    }
    
    
    public static BigDecimal divide(int n1, int n2, MathContext mc){
        BigDecimal a = new BigDecimal(n1, mc);
        BigDecimal b = new BigDecimal(n2, mc);
        return a.divide(b, mc);
    }
    
    
    public static BigDecimal divide(BigDecimal n1, BigDecimal n2, int precision) {
        MathContext mc = new MathContext(precision);
        return n1.divide(n2, mc);
    }
    
    public static BigDecimal divide(BigDecimal n1, BigDecimal n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        return n1.divide(n2, mc);
    }
    
    public static BigDecimal divide(BigDecimal n1, BigDecimal n2, MathContext mc) {
        return n1.divide(n2, mc);
    }

    
    public static double add(double n1, double n2, int precision) {
        MathContext mc = new MathContext(precision);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.add(b, mc);

        return c.doubleValue();
    }
    
    public static double add(double n1, double n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.add(b, mc);

        return c.doubleValue();
    }
    
    public static double add(double n1, double n2, MathContext mc) {
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.add(b, mc);

        return c.doubleValue();
    }

    
    public static BigDecimal add(BigDecimal n1, BigDecimal n2, int precision) {
        MathContext mc = new MathContext(precision);
        return n1.add(n2, mc);
    }
    
    public static BigDecimal add(BigDecimal n1, BigDecimal n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        return n1.add(n2, mc);
    }
    
    public static BigDecimal add(BigDecimal n1, BigDecimal n2, MathContext mc) {
        return n1.add(n2, mc);
    }
    

    public static double subtract(double n1, double n2, int precision) {
        MathContext mc = new MathContext(precision);
        BigDecimal a = new BigDecimal(new Double(n1).toString());
        BigDecimal b = new BigDecimal(new Double(n2).toString());
        BigDecimal c = null;
        c = a.subtract(b, mc);

        return c.doubleValue();
    }
    
    public static double subtract(double n1, double n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.subtract(b, mc);

        return c.doubleValue();
    }
    
    public static double subtract(double n1, double n2, MathContext mc) {
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal b = new BigDecimal(new Double(n2).toString(), mc);
        BigDecimal c = null;
        c = a.subtract(b, mc);

        return c.doubleValue();
    }
    

    public static BigDecimal subtract(BigDecimal n1, BigDecimal n2, int precision) {
        MathContext mc = new MathContext(precision);
        return n1.subtract(n2, mc);
    }
    
    public static BigDecimal subtract(BigDecimal n1, BigDecimal n2, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        return n1.subtract(n2, mc);
    }
    
    public static BigDecimal subtract(BigDecimal n1, BigDecimal n2, MathContext mc) {
        return n1.subtract(n2, mc);
    }
    
    public static double abs(double n1, int precision) {
        MathContext mc = new MathContext(precision);
        BigDecimal a = new BigDecimal(new Double(n1).toString());
        BigDecimal c = null;
        c = a.abs(mc);

        return c.doubleValue();
    }
    
    public static double abs(double n1, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal c = null;
        c = a.abs(mc);

        return c.doubleValue();
    }
    
    public static double abs(double n1, MathContext mc) {
        BigDecimal a = new BigDecimal(new Double(n1).toString(), mc);
        BigDecimal c = null;
        c = a.abs(mc);

        return c.doubleValue();
    }

    
    public static BigDecimal abs(BigDecimal n1, int precision) {
        MathContext mc = new MathContext(precision);
        return n1.abs(mc);
    }
    
    public static BigDecimal abs(BigDecimal n1, int precision, RoundingMode roundingMode) {
        MathContext mc = new MathContext(precision, roundingMode);
        return n1.abs(mc);
    }
    
    public static BigDecimal abs(BigDecimal n1, MathContext mc) {
        return n1.abs(mc);
    }

}
