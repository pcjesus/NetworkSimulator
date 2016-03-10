/**
 * MSM - Network Simulator
 */
package msm.simulator.exceptions;


/**
 * @author pjesus
 *
 */
public class NetworkException extends Exception {


    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 7548282234806464251L;
    private boolean critical;


    public NetworkException() {
        super();
        this.critical = true;
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
        this.critical = true;
    }

    public NetworkException(String message) {
        super(message);
        this.critical = true;
    }
    
    public NetworkException(String message, boolean critical) {
        super(message);
        this.critical = critical;
    }

    public NetworkException(Throwable cause) {
        super(cause);
        this.critical = true;
    }
    
    /**
     * @return the critical
     */
    public boolean isCritical() {
        return critical;
    }

}
