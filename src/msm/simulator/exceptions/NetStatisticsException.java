/**
 * MSM - Network Simulator
 */
package msm.simulator.exceptions;


/**
 * @author pjesus
 *
 */
public class NetStatisticsException extends Exception {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -229000632794397898L;
    
    public NetStatisticsException() {
        super();
    }

    public NetStatisticsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetStatisticsException(String message) {
        super(message);
    }

    public NetStatisticsException(Throwable cause) {
        super(cause);
    }

}
