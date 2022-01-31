import java.io.Serializable;

/*
 * 	Class responsible for encapsulating requests and answers related to data correction
 *
 * */
public class ByteBlockRequest implements Serializable {

    private Host nodeHost; // Node host (address + port)
    private int startIndex;
    private int length;
    private CloudByte[] data; // This field will be populated for the answer
    private String whoAnsweredPort;

    public ByteBlockRequest(int startIndex_, int length_, Host nodeHost_) {
        nodeHost = nodeHost_;
        startIndex = startIndex_;
        length = length_;
        whoAnsweredPort = null;
    }

    /*
     * 	Checks if this request host is equal to the given host
     *
     * */
    public boolean isSameHost(Host nodeHost_) {
        return (nodeHost.getAddress().equals(nodeHost_.getAddress()) &&
                nodeHost.getPort().equals(nodeHost_.getPort()));
    }

    /*******************************************************
     *
     * 		GETTERS AND SETTERS
     *
     *******************************************************/

    public int getStartIndex() {
        return startIndex;
    }

    public int getLength() {
        return length;
    }

    public void setData(CloudByte[] data_) {
        data = data_;
    }

    public int getDataLength() {
        return data.length;
    }

    public CloudByte[] getData() {
        return data;
    }

    public Host getNodeHost() {
        return nodeHost;
    }

    public String getAddress() {
        return nodeHost.getAddress();
    }

    public String getPort() {
        return nodeHost.getPort();
    }

    public int getPortInt() {
        return nodeHost.getPortInt();
    }
}
