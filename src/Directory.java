import sun.plugin2.main.client.DisconnectedExecutionContext;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/*
 * 	[PHASE 7]
 *
 * 	Class responsible for
 * 		- Registering nodes
 *		- Giving information on which nodes are registered
 *
 * */
public class Directory {

    private final String address = "127.0.0.1";
    private final int port = 8082;

    private ArrayList<Host> nodes; // Registered nodes


    public Directory() {
        nodes = new ArrayList<Host>();
    }


    /*
     * 	Accepts new connection attempts and treats each client
     *
     * */
    public void Init() {

        System.out.println("Directory is now listening");

        ServerSocket serverSocket;
        Socket socket;

        try {

            serverSocket = new ServerSocket(port);
            new DisconnectController(this).start();

            while(true) {

                socket = serverSocket.accept(); // New connection from client
                new DirectoryListener(this, socket).start(); // Treat client

            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*
     * 	Removes node from list of registered nodes
     *  This is used every time a nodes disconnects (see DisconnectController thread)
     *
     * */
    public synchronized void removeNode(String address, String port) {

        for(Host n : nodes) {

            if(n.getAddress().equals(address) && n.getPort().equals(port)) { // [NOTE] Should create an "equals" method inside Host class
                nodes.remove(n);
                System.out.println("Node " + address + " " + port + " was removed from directory");
                return;
            }
        }

    }

    /*
     * 	Retrieves list of registered nodes
     *
     * */
    public synchronized ArrayList<Host> getNodes() {
        return nodes;
    }



    public static void main(String[] args) {

        new Directory().Init();

    }

}
