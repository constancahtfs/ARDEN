import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/*
 * 	Responsible for detecting if node got disconnected
 *  If so, sends disconnect warning to directory in order to get node removed from the registered nodes list
 *
 * */
public class DisconnectController extends Thread {

    private Directory directory;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public DisconnectController(Directory directory_) {
        directory = directory_;
    }

    /*
     * 	Establish a connection with node to check if it got disconnected or not
     * 	If a connection attempt was unsuccessful it will give an error (catch) which means the node got disconnected
     * 	[NOTE] Should specify exception in catch clause
     *
     * */
    private boolean IsConnectionAvaiable(String address, int port) {

        try {
            socket = new Socket(address, port);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(null); // Just testing connection
            socket.close();
            return true;
        }
        catch(Exception ex) {
            return false;
        }
    }

    @Override
    public void run() {

        try {

            sleep(10000); // Waits a bit before starting the first connection

            while(true) {

                ArrayList<Host> nodes = new ArrayList<>(directory.getNodes());

                for(Host host : nodes){

                    boolean connected = IsConnectionAvaiable(host.getAddress(), host.getPortInt());

                    if(!connected) {

                        System.out.println("Node " + host.getAddress() + " " + host.getPort() + " got disconnected");

                        DirectoryManager.NodeGotDisconnected(host.getAddress(), host.getPort()); // Warns directory that node got disconnected

                    }
                }

                sleep(10000); // Waits 10 seconds to not create connections constantly

            }

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

}
