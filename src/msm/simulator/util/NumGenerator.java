/**
 * 
 */
package msm.simulator.util;

import msm.simulator.exceptions.NumGenerationException;

import org.apache.commons.math.MathException;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;



/**
 * @author pcjesus
 * @param <T>
 *
 */
public class NumGenerator {
    
    public enum GenerationFunction {CONSTANT, GAUSSIAN, POISSON, UNIFORM, EXPONENTIAL, WEIBULL}
    

    private GenerationFunction genFunc;
    private String[] params;
    RandomDataImpl randomData;
    
    /**
     * CONSTRUCTORS
     */
    
    public NumGenerator(){
    }
    
    public NumGenerator(GenerationFunction generationFunction, Integer seed, String... parameters) {
//        throws NumGenerationException {

        
        this.genFunc = generationFunction;
        this.params = new String[parameters.length];
        System.arraycopy(parameters, 0, this.params, 0, parameters.length);
        
        if(seed != null){
            this.randomData = new RandomDataImpl(new MersenneTwister(seed));
        } else {
            this.randomData = new RandomDataImpl(new MersenneTwister());
        }
/*        
        switch (this.genFunc) {
            case CONSTANT:
                ConstantGenerator<Integer> constantGenerator = new ConstantGenerator<Integer>(Integer.valueOf(this.params[0]));
                this.numGen = constantGenerator;
                break;
            case GAUSSIAN:
                GaussianGenerator gaussianGenerator = new GaussianGenerator(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]), new MersenneTwisterRNG());
                this.numGen = gaussianGenerator;
                break;
            case POISSON:
                PoissonGenerator poissonGenerator = new PoissonGenerator(Double.valueOf(this.params[0]), new MersenneTwisterRNG());
                this.numGen = poissonGenerator;
                break;
            case UNIFORM:
                DiscreteUniformGenerator uniformGenerator = new DiscreteUniformGenerator(Integer.parseInt(this.params[0]), Integer.parseInt(this.params[1]), new MersenneTwisterRNG());
                this.numGen = uniformGenerator;
                break;
            case EXPONENTIAL:
<<<<<<< TREE
                ExponentialGenerator exponentialGenerator = new ExponentialGenerator(Double.valueOf(this.params[0]), new MersenneTwisterRNG());
                this.numGen = exponentialGenerator;
=======
                if(seed != null){
                    //exponentialGenerator = new ExponentialGenerator(Double.valueOf(this.params[0]), new Random(seed));
                    this.randomData = new RandomDataImpl(new MersenneTwister(seed));
                } else {
                    //exponentialGenerator = new ExponentialGenerator(Double.valueOf(this.params[0]), new Random());
                    //exponentialGenerator = new ExponentialGenerator(Double.valueOf(this.params[0]), new MersenneTwisterRNG());
                    this.randomData = new RandomDataImpl(new MersenneTwister());
                }
                //this.numGen = exponentialGenerator;
>>>>>>> MERGE-SOURCE
                break;
            case WEIBULL:
                this.randomData = new RandomDataImpl(new MersenneTwister());
                break;
            default:
                throw new NumGenerationException("Unknown generation function: " + this.genFunc);
        }
*/        
    }
    
    
    /**
     * Load a configuration string, with elements separated by ";".
     * The first elements corresponds to the GenerationFunction  and the remaining to its parameters.
     * 
     * @param config String with the GenerationFunction and respective parameters (separated by ";").
     * @throws NumGenerationException  
     */
    public void loadConfig(String config) {
//        throws NumGenerationException {
        
        String[] params = config.split(";");
        this.genFunc = GenerationFunction.valueOf(params[0]);
        this.params = new String[params.length - 1];
        System.arraycopy(params, 1, this.params, 0, (params.length - 1));
        
        this.randomData = new RandomDataImpl(new MersenneTwister());
        
/*
        switch (this.genFunc) {
            case CONSTANT:
                ConstantGenerator<Integer> constantGenerator = new ConstantGenerator<Integer>(Integer.valueOf(this.params[0]));
                this.numGen = constantGenerator;
                break;
            case GAUSSIAN:
                GaussianGenerator gaussianGenerator = new GaussianGenerator(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]), new MersenneTwisterRNG());
                this.numGen = gaussianGenerator;
                break;
            case POISSON:
                PoissonGenerator poissonGenerator = new PoissonGenerator(Double.valueOf(this.params[0]), new MersenneTwisterRNG());
                this.numGen = poissonGenerator;
                break;
            case UNIFORM:
                DiscreteUniformGenerator uniformGenerator = new DiscreteUniformGenerator(Integer.parseInt(this.params[0]), Integer.parseInt(this.params[1]), new MersenneTwisterRNG());
                this.numGen = uniformGenerator;
                break;
            case EXPONENTIAL:
                ExponentialGenerator exponentialGenerator = new ExponentialGenerator(Double.valueOf(this.params[0]), new MersenneTwisterRNG());
                this.numGen = exponentialGenerator;
                break;
            case WEIBULL:
                this.randomData = new RandomDataImpl(new MersenneTwister());
                break;
            default:
                throw new NumGenerationException("Unknown generation function: " + this.genFunc);
        }
*/        
    }
    
    
    public Integer generateInteger() throws MathException {
        
        switch (this.genFunc) {
            case CONSTANT:
                return Integer.valueOf(this.params[0]);
            case GAUSSIAN:
                Double resultG = this.randomData.nextGaussian(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
                if(this.params.length > 3){
                    //Get adding factor
                    resultG = resultG + Double.valueOf(this.params[3]);
                }
                if(this.params.length > 2){
                    //Get Multiplication factor
                    resultG = resultG * Double.valueOf(this.params[2]);
                }
                return Integer.valueOf((int) Math.round(resultG));
            case POISSON:
                Long resultP = this.randomData.nextPoisson(Double.valueOf(this.params[0]));
                if(this.params.length > 2){
                    //Get adding factor
                    resultP = resultP + Long.valueOf(this.params[2]);
                }
                if(this.params.length > 1){
                    //Get Multiplication factor
                    resultP = resultP * Long.valueOf(this.params[1]);
                }
                return resultP.intValue();
            case UNIFORM:
                Double resultU = this.randomData.nextUniform(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
                if(this.params.length > 3){
                    //Get adding factor
                    resultU = resultU + Double.valueOf(this.params[3]);
                }
                if(this.params.length > 2){
                    //Get Multiplication factor
                    resultU = resultU * Double.valueOf(this.params[2]);
                }
                return Integer.valueOf((int) Math.round(resultU));
            case EXPONENTIAL:
                Double resultE = this.randomData.nextExponential(Double.valueOf(this.params[0]));
                if(this.params.length > 2){
                    //Get adding factor
                    resultE = resultE + Double.valueOf(this.params[2]);
                }
                if(this.params.length > 1){
                    //Get Multiplication factor
                    resultE = resultE * Double.valueOf(this.params[1]);
                }
                return Integer.valueOf((int) Math.round(resultE));
            case WEIBULL:
                Double resultW = this.randomData.nextWeibull(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
                if(this.params.length > 2){
                    //Get Multiplication factor
                    resultW = resultW * Double.valueOf(this.params[2]);
                }
                if(this.params.length > 3){
                    //Get adding factor
                    resultW = resultW + Double.valueOf(this.params[3]);
                }
                return Integer.valueOf((int) Math.round(resultW));
            default:
                return null;
        }
        
    }
    
/*    
    public static Integer generateInteger(GenerationFunction generationFunction, String... parameters) throws NumGenerationException, MathException {
        
        switch (generationFunction) {
            case CONSTANT:
                return Integer.valueOf(parameters[0]);
            case GAUSSIAN:
                randomData = new RandomDataImpl(new MersenneTwister());
                Double resultG = this.randomData.nextGaussian(Double.valueOf(parameters[0]), Double.valueOf(parameters[1]));
                if(parameters.length > 3){
                    //Get adding factor
                    resultG = resultG + Double.valueOf(parameters[3]);
                }
                if(parameters.length > 2){
                    //Get Multiplication factor
                    resultG = resultG * Double.valueOf(this.params[2]);
                }
                return Integer.valueOf((int) Math.round(resultG));
            case POISSON:
                PoissonGenerator poissonGenerator = new PoissonGenerator(Double.valueOf(parameters[0]), new MersenneTwisterRNG());
                return (Integer) poissonGenerator.nextValue();
            case UNIFORM:
                DiscreteUniformGenerator uniformGenerator = new DiscreteUniformGenerator(Integer.parseInt(parameters[0]), Integer.parseInt(parameters[1]), new MersenneTwisterRNG());
                return (Integer) uniformGenerator.nextValue();
            case EXPONENTIAL:
                ExponentialGenerator exponentialGenerator = new ExponentialGenerator(Double.valueOf(parameters[0]), new MersenneTwisterRNG());
                Double resultE = (Double) exponentialGenerator.nextValue();
                return Integer.valueOf((int) Math.round(resultE));
            case WEIBULL:    
                RandomDataImpl rndData = new RandomDataImpl(new MersenneTwister());
                Double resultW = rndData.nextWeibull(Double.valueOf(parameters[0]), Double.valueOf(parameters[1]));
                if(parameters.length > 2){
                    //Get Multiplication factor
                    resultW = resultW * Double.valueOf(parameters[2]);
                }
                if(parameters.length > 3){
                    //Get adding factor
                    resultW = resultW + Double.valueOf(parameters[3]);
                }
                return Integer.valueOf((int) Math.round(resultW));
            default:
                throw new NumGenerationException("Unknown generation function: " + generationFunction);
        }
    }
*/    

/*    
    public static Integer generateInteger(String config) throws NumGenerationException, MathException{
        String[] sparams = config.split(";");
        GenerationFunction gf = GenerationFunction.valueOf(sparams[0]);
        
        switch (gf) {
            case CONSTANT:
                ConstantGenerator<Integer> constantGenerator = new ConstantGenerator<Integer>(Integer.valueOf(sparams[1]));
                return (Integer) constantGenerator.nextValue();
            case GAUSSIAN:
                GaussianGenerator gaussianGenerator = new GaussianGenerator(Double.valueOf(sparams[1]), Double.valueOf(sparams[2]), new MersenneTwisterRNG());
                Double resultG = (Double) gaussianGenerator.nextValue();
                return Integer.valueOf((int) Math.round(resultG));
            case POISSON:
                PoissonGenerator poissonGenerator = new PoissonGenerator(Double.valueOf(sparams[1]), new MersenneTwisterRNG());
                return (Integer) poissonGenerator.nextValue();
            case UNIFORM:
                DiscreteUniformGenerator uniformGenerator = new DiscreteUniformGenerator(Integer.parseInt(sparams[1]), Integer.parseInt(sparams[2]), new MersenneTwisterRNG());
                return (Integer) uniformGenerator.nextValue();
            case EXPONENTIAL:
                ExponentialGenerator exponentialGenerator = new ExponentialGenerator(Double.valueOf(sparams[1]), new MersenneTwisterRNG());
                Double resultE = (Double) exponentialGenerator.nextValue();
                return Integer.valueOf((int) Math.round(resultE));
            case WEIBULL:    
                RandomDataImpl rndData = new RandomDataImpl(new MersenneTwister());
                Double resultW = rndData.nextWeibull(Double.valueOf(sparams[0]), Double.valueOf(sparams[1]));
                if(sparams.length > 2){
                    //Get Multiplication factor
                    resultW = resultW * Double.valueOf(sparams[2]);
                }
                if(sparams.length > 3){
                    //Get adding factor
                    resultW = resultW + Double.valueOf(sparams[3]);
                }
                return Integer.valueOf((int) Math.round(resultW));
            default:
                throw new NumGenerationException("Unknown generation function: " + gf);
        }
    }
*/
    

    public Double generateDouble() throws MathException {
        
        switch (this.genFunc) {
            case CONSTANT:
                return Double.valueOf(this.params[0]);
            case GAUSSIAN:
                return this.randomData.nextGaussian(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
            case POISSON:
                return (double) this.randomData.nextPoisson(Double.valueOf(this.params[0]));
            case UNIFORM:
                return this.randomData.nextUniform(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
            case EXPONENTIAL:
                return this.randomData.nextExponential(Double.valueOf(this.params[0]));
            case WEIBULL: 
                return this.randomData.nextWeibull(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
            default:
                return null;
        }
        
    }
    
    public Float generateFloat() throws MathException {
        
        switch (this.genFunc) {
            case CONSTANT:
                return Float.valueOf(this.params[0]);
            case GAUSSIAN:
                return (float) this.randomData.nextGaussian(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
            case POISSON:
                return (float) this.randomData.nextPoisson(Double.valueOf(this.params[0]));
            case UNIFORM:
                return (float) this.randomData.nextUniform(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
            case EXPONENTIAL:
                return (float) this.randomData.nextExponential(Double.valueOf(this.params[0]));
            case WEIBULL: 
                return (float) this.randomData.nextWeibull(Double.valueOf(this.params[0]), Double.valueOf(this.params[1]));
            default:
                return null;
        }
        
    }
    
    
}
