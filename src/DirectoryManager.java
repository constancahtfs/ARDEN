import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/*
 * 	Directory static operations for nodes and threads to use
 *
 * */
public class DirectoryManager {

    private final static int port_ = 8082;
    private final static String address_ = "127.0.0.1";

    /*
     * 	Close connections
     *
     * */
    private static void CloseConnection(Socket socket, DataOutputStream out, BufferedReader in) {

        try {

            out.close();
            in.close();
            socket.close();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /*
     * 	Send message through the data output stream
     *
     * */
    private static void SendMessage(String message, DataOutputStream out) {
        try {

            out.write((message + "\n").getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*******************************************************
     *
     * 		DIRECTORY OPERATIONS
     *
     *******************************************************/

    /*
     * 	Order to remove node from directory registration list
     * 	(Node got disconnected)
     *
     * */
    public static void NodeGotDisconnected(String address, String port) {

        Socket socket = null;
        DataOutputStream out = null;
        BufferedReader in = null;

        try {

            socket = new Socket(address_, port_); // Connect to directory
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            SendMessage("disconnect " + address + " " + port, out); // Message to disconnect

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            CloseConnection(socket,out,in);
        }

    }

    /*
     * 	Node send message to register in directory and retrieves registered nodes (expect itself)
     *
     * */
    public static ArrayList<Host> RegisterAndGetRegisteredNodes(String address, String port) {

        Socket socket = null;
        DataOutputStream out = null;
        BufferedReader in = null;

        ArrayList<Host> registeredNodes = new ArrayList<Host>();

        try {

            socket = new Socket(address_, port_); // Connect to directory
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            SendMessage("INSC " + address + " " + port, out); // Message to register
            SendMessage("nodes", out); // Message to get all registered nodes

            registeredNodes = GetRegisteredNodesExcept(address, port, in); // Waits for directory answer

            if(registeredNodes == null) { // This should not happen
                System.out.println("Something went wrong while getting the registered nodes");
                registeredNodes = new ArrayList<Host>();
            }

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            CloseConnection(socket,out,in);
        }

        return registeredNodes;
    }

    public static void Register(String address, String port) {

        Socket socket = null;
        DataOutputStream out = null;
        BufferedReader in = null;

        ArrayList<Host> registeredNodes = new ArrayList<Host>();

        try {

            socket = new Socket(address_, port_); // Connect to directory
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            SendMessage("INSC " + address + " " + port, out); // Message to register


        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            CloseConnection(socket,out,in);
        }

    }


    public static ArrayList<Host> GetRegisteredNodes(String address, String port) {

        Socket socket = null;
        DataOutputStream out = null;
        BufferedReader in = null;

        ArrayList<Host> registeredNodes = new ArrayList<Host>();

        try {

            socket = new Socket(address_, port_); // Connect to directory
            out = new DataOutputStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            SendMessage("nodes", out); // Message to get all registered nodes

            registeredNodes = GetRegisteredNodesExcept(address, port, in); // Waits for directory answer

            if(registeredNodes == null) { // This should not happen
                System.out.println("Something went wrong while getting the registered nodes");
                registeredNodes = new ArrayList<Host>();
            }

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            CloseConnection(socket,out,in);
        }

        return registeredNodes;
    }


    /*
     * 	Listens to messages sent by the directory in order to aggregate all registered nodes in one list
     * 	List does not include the node that calls this method
     *
     * */
    private static ArrayList<Host> GetRegisteredNodesExcept(String address, String port, BufferedReader in) {

        ArrayList<Host> registeredNodes = new ArrayList<Host>();

        try {

            while(true) {

                String response = in.readLine();
                //System.out.println(response);

                // End of registered nodes list
                if(response.equals("END"))
                    break;

                // Split response format: node <ADDRESS> <PORT>
                String[] responseSplit = response.split(" ");

                // In case of a registered node response
                if(responseSplit.length == 3 && responseSplit[0].equals("node")) {

                    // Retrieve address and port
                    String responseAddress = responseSplit[1];
                    String responsePort = responseSplit[2];

                    // If it is not the given node then add to list
                    if(!(responseAddress.equals(address) && responsePort.equals(port)))
                        registeredNodes.add(new Host(responseSplit[1], responseSplit[2]));
                }

            }

            return registeredNodes;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }







}
