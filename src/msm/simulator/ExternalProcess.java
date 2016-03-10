/**
 * MSM - Network Simulator
 */

package msm.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * @author pjesus
 *
 */
public class ExternalProcess extends Thread {
    
    private Process cmdProcess;
    private String command;
    private String description;
    private File workingDir;
    
    
    /**
     * Constructors
     */
    
    public ExternalProcess(String command, String description, String workingDir){
        this.command = new String(command);
        this.description = new String(description);
        this.workingDir = (workingDir!=null)?new File(workingDir):null;
    }
    
    
    /**
     * Getters/Setters
     */
    
    public Process getProcess(){
        return cmdProcess;
    }
    
    
    /**
     * @return Returns the command.
     */
    public String getCommand() {
        return command;
    }


    
    /**
     * @param command The command to set.
     */
    public void setCommand(String command) {
        this.command = command;
    }
    
    
    public void executeComand(){
    	
    	try {
            
    	    this.cmdProcess = Runtime.getRuntime().exec(this.command, null, this.workingDir);
        
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(cmdProcess.getErrorStream()));

            String s = null;

            // read the output from the command
            String cmdOutput = new String();
            while ((s = stdInput.readLine()) != null) {
                cmdOutput+=s;
            }
           
            // read any errors from the attempted command
            boolean error = false;
            String cmdError = new String();
            while ((s = stdError.readLine()) != null) {
                error = true;
                cmdError+=s;
            }
        
            if(error){
                System.out.println(this.description+"... ERROR! ");
            } else {
                System.out.println(this.description+"... OK! ");
            }

            if(cmdOutput.length() > 0){
                System.out.println("Here is the standard output of the command:\n"
                                   + cmdOutput);
            }
            if(cmdError.length() > 0){
                System.err.println("Here is the standard error of the command:\n"
                               + cmdError);
            }
         
        } catch (IOException ioe){
            System.out.println("ERROR! ");
            System.err.println ("Error executing command: " + ioe.getMessage());
            ioe.printStackTrace();
        }
        
    }
    
    
    
    /**
     * Thread execution
     */
    
    public void run() {
        
    	executeComand();
        
    }
    

}
