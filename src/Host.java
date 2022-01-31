import java.io.Serializable;

/*
 * 	Encapsulates port + address
 *
 * */
public class Host implements Serializable{

    private String address;
    private String port;

    public Host(String _address, String _port) {
        address = _address;
        port = _port;
    }

    /*******************************************************
     *
     * 		GETTERS
     *
     *******************************************************/

    public String getAddress() {
        return address;
    }

    public String getPort() {
        return port;
    }

    public int getPortInt() {
        return Integer.parseInt(port);
    }

}
