/**
 * MSM - Network Simulator
 */
package msm.simulator.exceptions;


/**
 * @author pjesus
 *
 */
public class ConfigException extends Exception {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -6541774260567175883L;

    
    public ConfigException() {
        super();
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }

}
