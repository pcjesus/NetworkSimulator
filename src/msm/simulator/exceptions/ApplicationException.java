/**
 * MSM - Network Simulator
 */
package msm.simulator.exceptions;


/**
 * @author pjesus
 *
 */
public class ApplicationException extends Exception {

    
    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 5664404155100917017L;
    

    public ApplicationException() {
        super();
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(Throwable cause) {
        super(cause);
    }

}
