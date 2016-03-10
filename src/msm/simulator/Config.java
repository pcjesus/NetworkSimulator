/**
 * MSM - Network Simulator
 */
package msm.simulator;



import java.util.Properties;
import msm.simulator.exceptions.ConfigException;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;


/**
 * @author pjesus
 *
 */
public class Config {

    /*
     * Configuration parameters
     */
    
    //Network
    public static final String PARAM_NETWORK_TYPE = "NETWORK_TYPE";
    public static final String PARAM_NETWORK_SIZE = "NETWORK_SIZE";
    public static final String PARAM_NETWORK_DEGREE = "NETWORK_DEGREE";
    public static final String PARAM_NETWORK_RADIUS = "NETWORK_RADIUS";
    public static final String PARAM_NETWORK_PROBABILITY = "NETWORK_PROBABILITY";
    public static final String PARAM_NETWORK_EDGES = "NETWORK_EDGES";
    public static final String PARAM_NETWORK_OVERLAY = "NETWORK_OVERLAY";
    public static final String PARAM_NETWORK_CONFIG_FILEPATH = "NETWORK_CONFIG_FILEPATH";
    
    
    public static final String PARAM_SAVE_NETWORK = "SAVE_NETWORK";
    public static final String PARAM_LOAD_NETWORK = "LOAD_NETWORK";
    public static final String PARAM_NETWORK_FILE_TO_LOAD = "NETWORK_FILE_TO_LOAD";
    public static final String PARAM_CREATE_OVERLAY = "CREATE_OVERLAY";
    
    //Data Distribution
    public static final String PARAM_LOAD_DATA_DISTRIBUTION = "LOAD_DATA_DISTRIBUTION";
    public static final String PARAM_DATA_DISTRIBUTION_TO_LOAD = "DATA_DISTRIBUTION_TO_LOAD";
    public static final String PARAM_DATA_DISTRIBUTION_TYPE = "DATA_DISTRIBUTION_TYPE";
    public static final String PARAM_DATA_DISTRIBUTION_SIZE = "DATA_DISTRIBUTION_SIZE";
    public static final String PARAM_DATA_DISTRIBUTION_PARAMETERS = "DATA_DISTRIBUTION_PARAMETERS";
    
    public static final String PARAM_DATA_DISTRIBUTION_PROCESSING = "DATA_DISTRIBUTION_PROCESSING";
    public static final String PARAM_DATA_DISTRIBUTION_INTERVALS = "DATA_DISTRIBUTION_INTERVALS";
    public static final String PARAM_DATA_DISTRIBUTION_INTERVALS_STRATEGY = "DATA_DISTRIBUTION_INTERVALS_STRATEGY";
    
    //Dynamics
    public static final String PARAM_CHURN_RATE = "CHURN_RATE";
    public static final String PARAM_CHURN_PERIOD_LENGTH = "CHURN_PERIOD_LENGTH";
    public static final String PARAM_CHURN_PERIOD_REPETITION = "CHURN_PERIOD_REPETITION";
    public static final String PARAM_CHURN_REPETITION = "CHURN_REPETITION";
    public static final String PARAM_CHURN_RATE_FROM_INIT = "CHURN_RATE_FROM_INIT";
    public static final String PARAM_CHURN_COUNT_ISOLATED_NODES = "CHURN_COUNT_ISOLATED_NODES";
    
    public static final String PARAM_VALUE_CHANGE_RATE = "VALUE_CHANGE_RATE";
    public static final String PARAM_VALUE_CHANGE_COVERAGE_RATIO = "VALUE_CHANGE_COVERAGE_RATIO";
    public static final String PARAM_VALUE_CHANGE_AT_TIME = "VALUE_CHANGE_AT_TIME";
    public static final String PARAM_VALUE_CHANGE_REPETITION = "VALUE_CHANGE_REPETITION";
    public static final String PARAM_VALUE_CHANGE_OPERATOR = "VALUE_CHANGE_OPERATOR";
    public static final String PARAM_VALUE_CHANGE_REPEAT_PATTERN = "VALUE_CHANGE_REPEAT_PATTERN";
    public static final String PARAM_VALUE_CHANGE_FROM_INIT = "VALUE_CHANGE_FROM_INIT";
    
    //Simulation
    public static final String PARAM_LOOP_BREAK_LIMIT = "LOOP_BREAK_LIMIT";
    public static final String PARAM_ACCOUNT_INFINIT_LOOPS = "ACCOUNT_INFINIT_LOOPS";
    public static final String PARAM_LIMIT_TIME = "LIMIT_TIME";
    public static final String PARAM_LIMIT_STD_DEVIATION = "LIMIT_STD_DEVIATION";
    
    public static final String PARAM_MATH_PRECISION = "MATH_PRECISION";
    public static final String DEFAULT_MATH_PRECISION = "15";
    public static final String PARAM_MATH_ROUNDING_MODE = "MATH_ROUNDING_MODE";
    public static final String DEFAULT_MATH_ROUNDING_MODE = "DOWN";
    
    public static final String PARAM_SIMULATION_REPETITION = "SIMULATION_REPETITION";
    public static final String PARAM_SIMULATION_EXTRA_REPETITION = "SIMULATION_EXTRA_REPETITION";
    public static final String PARAM_SIMULATION_NUMBER = "SIMULATION_NUMBER";
    public static final String PARAM_COMMUNICATION_ENGINE = "_COMMUNICATION_ENGINE";
    public static final String PARAM_COMMUNICATION_ENGINE_PARAM = "_COMMUNICATION_ENGINE_PARAM_";
    public static final String PARAM_APPLICATION_TYPE = "_APPLICATION_TYPE";
    public static final String PARAM_APPLICATION_PARAM = "_APPLICATION_PARAM_";
    public static final String PARAM_REPORT_STATE_TRACE_PARAM = "_REPORT_STATE_TRACE_PARAM_";
    
    public static final String PARAM_RESULTS_SAMPLE_PERIOD = "RESULTS_SAMPLE_PERIOD";
    
    public static final String PARAM_FUNCTION = "FUNCTION";
    public static final String DEFAULT_FUNCTION = "COUNT";
    
    public static final String PARAM_OUTPUT_DIR = "OUTPUT_DIR";
    public static final String DEFAULT_OUTPUT_DIR = "./";
    public static final String PARAM_INPUT_DIR = "INPUT_DIR";
    public static final String DEFAULT_INPUT_DIR = "./";
    
    //Reports
    public static final String PARAM_CREATE_NETWORK_LINKS_HISTOGRAM = "CREATE_NETWORK_LINKS_HISTOGRAM";
    public static final String PARAM_SHOW_NETWORK_LINKS_HISTOGRAM = "SHOW_NETWORK_LINKS_HISTOGRAM";
    public static final String PARAM_CREATE_NETWORK_GRAPH_IMAGE = "CREATE_NETWORK_GRAPH_IMAGE";
    public static final String PARAM_SHOW_NETWORK_GRAPH_IMAGE = "SHOW_NETWORK_GRAPH_IMAGE";
    public static final String PARAM_CREATE_DATA_DISTRIBUTION = "CREATE_DATA_DISTRIBUTION";
    public static final String PARAM_SHOW_DATA_DISTRIBUTION = "SHOW_DATA_DISTRIBUTION";
//    public static final String PARAM_SHOW_RESULT_PLOT = "SHOW_RESULT_PLOT";
/*    public static final String PARAM_SHOW_MSG_SEND_COUNT_PLOT = "SHOW_MSG_SEND_COUNT_PLOT";
    public static final String PARAM_SHOW_MSG_RCV_COUNT_PLOT = "SHOW_MSG_RCV_COUNT_PLOT";
    public static final String PARAM_SHOW_MSG_DISC_SEND_COUNT_PLOT = "SHOW_MSG_DISC_SEND_COUNT_PLOT";
    public static final String PARAM_SHOW_MSG_DISC_RCV_COUNT_PLOT = "SHOW_MSG_DISC_RCV_COUNT_PLOT";
    public static final String PARAM_SHOW_MSG_LOSS_COUNT_PLOT = "SHOW_MSG_LOSS_COUNT_PLOT";
*/    

    public static final String PARAM_CMD_GENERATE_GRAPH_IMAGE = "CMD_GENERATE_GRAPH_IMAGE";
    public static final String PARAM_CMD_SHOW_GRAPH_IMAGE = "CMD_SHOW_GRAPH_IMAGE";
    public static final String PARAM_CMD_SHOW_RESULT_PLOT = "CMD_SHOW_RESULT_PLOT";
    public static final String PARAM_CMD_SHOW_MSG_COUNT_PLOT = "CMD_SHOW_MSG_COUNT_PLOT";
    
    
    public static final String PARAM_GRAPH_NUMBER = "GRAPH_NUMBER";
    public static final String PARAM_GRAPH_PARAM = "_GRAPH_PARAM_";

    //Configuration from file properties
    private Properties properties;

    //Configuration File
    private String configFilePath;

    //Last Modified
    private long lastModified;

    /**
     * Constructors
     */

    public Config() {
        this.properties = new Properties();
    }


    public Config(String configFilePath) throws ConfigException {
        this.properties = new Properties();
        this.configFilePath = configFilePath;
        readConfigFile();
    }




    /**
     * Read configuration file data
     */

    public void readConfigFile() throws ConfigException {
        try {

            FileInputStream fisFile = new FileInputStream(this.configFilePath);
            this.properties.load(fisFile);
            fisFile.close();

            File file = new File(this.configFilePath);
            this.lastModified = file.lastModified();

        } catch (FileNotFoundException fnfe) {
            System.err.println("Wrong File Argument: " + this.configFilePath +
                               ".\nFile do not exist! ");
            System.err.flush();
            throw new ConfigException(fnfe);
        } catch (IOException ioe) {
            System.err.println("I/O Error!\nCan Not read file: " + this.configFilePath);
            System.err.flush();
            throw new ConfigException(ioe);
        }
    }



    public void reloadConfigFile() throws ConfigException {
        this.properties.clear();
        try {

            FileInputStream fisFile = new FileInputStream(this.configFilePath);
            this.properties.load(fisFile);
            fisFile.close();

            File file = new File(this.configFilePath);
            this.lastModified = file.lastModified();

        } catch (FileNotFoundException fnfe) {
            System.err.println("Wrong File Argument: " + this.configFilePath +
                               ".\nFile do not exist! ");
            System.err.flush();
            throw new ConfigException(fnfe);
        } catch (IOException ioe) {
            System.err.println("I/O Error!\nCan Not read file: " + this.configFilePath);
            System.err.flush();
            throw new ConfigException(ioe);
        }
    }
    
    
    /**
     * Write configuration data to file
     */

    public void writeConfigFile() throws ConfigException {

        try {

            FileOutputStream fosFile = new FileOutputStream(this.configFilePath);
            this.properties.store(fosFile, "Network Simulator Config File");
            fosFile.flush();
            fosFile.close();

        } catch (FileNotFoundException fnfe) {
            System.err.println("Wrong File Argument: " + this.configFilePath +
                               ".\nFile do not exist! ");
            System.err.flush();
            throw new ConfigException(fnfe);
        } catch (IOException ioe) {
            System.err.println("I/O Error!\nCan Not write file: " + this.configFilePath);
            System.err.flush();
            throw new ConfigException(ioe);
        }

    }



    public boolean isModified(){
        File file = new File(this.configFilePath);
        return (this.lastModified < file.lastModified());
    }

    public void setModified(){
        File file = new File(this.configFilePath);
        this.lastModified = file.lastModified();
    }
    

    /**
     * Handle Configuration Parameters
     */


    public String getValue(String param){
        return this.properties.getProperty(param);
    }
    
    public String getValueWithDefault(String param, String defaultValue){
        return this.properties.getProperty(param, defaultValue);
    }

    public void setValue(String param, String value){
        this.properties.setProperty(param, value);
    }
    
    public String getValue(String param, int index) throws ConfigException {
        String result = null;
        
        for(int i=index; ((i > 0) && (result == null)); i--){
            result = this.properties.getProperty(i+param);
        }
        
        if(result == null){
            throw new ConfigException("Unknown Config Parameter \"<index>"+param+"\" (recursive from index "+index+")");
        }
        
        return result;
    }

    public void setValue(String param, int index, String value){
        this.properties.setProperty(index+param, value);
    }
    
    public String getValue(int index, String... params) throws ConfigException {
        String result = null;
        String sParams = "";
        for(int i=0; i < params.length; i++){
            sParams += params[i];
        }
        for(int i=index; ((i > 0) && (result == null)); i--){
            result = this.properties.getProperty(i+sParams);
        }
        if(result == null){
            throw new ConfigException("Unknown Config Parameter \"<index>"+sParams+"\" (recursive from index "+index+")");
        }
        return result;
    }

    public void setValue(String value, int index, String... params){
        String sParams = "";
        for(int i=0; i < params.length; i++){
            sParams += params[i];
        }
        this.properties.setProperty(index+sParams, value);
    }
    
    public String getValueWithDefault(String defaultValue, int index, String... params) throws ConfigException {
        String result = null;
        String sParams = "";
        for(int i=0; i < params.length; i++){
            sParams += params[i];
        }
        for(int i=index; ((i > 0) && (result == null)); i--){
            result = this.properties.getProperty(i+sParams);
        }
        if((result == null) || (result.length() == 0)){
            result = defaultValue;
            //throw new ConfigException("Unknown Config Parameter \"<index>"+sParams+"\" (recursive from index "+index+")");
        }
        return result;
    }
    
}
