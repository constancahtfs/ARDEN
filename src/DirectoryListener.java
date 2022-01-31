import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/*
 * 	Class responsible for treating directory clients requests
 *
 * */
public class DirectoryListener extends Thread{

    private Directory directory;
    private BufferedReader in;
    private PrintWriter out;
    private static Socket socket;

    public DirectoryListener(Directory directory_, Socket socket_) {
        directory = directory_;
        socket = socket_;
    }

    @Override
    public void run() {
        try {

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            while(true) {

                String message = in.readLine();
                ArrayList<Host> nodes = directory.getNodes();

                if(message == null) continue;

                System.out.println(message);

                String[] messageSplit = message.split(" ");

                if(messageSplit.length != 3 && messageSplit.length != 1) { // Only available options have length = 3 or 1
                    System.out.println("Invalid message");
                    continue;
                }

                System.out.println("Received message: " + messageSplit[0]);

                switch(messageSplit[0]) {


                    // List nodes hosts
                    case "nodes":
                        for(Host n : nodes) {
                            out.println("node " + n.getAddress() + " " + n.getPort());
                        }
                        out.println("END");
                        break;


                    // Register node in list
                    case "INSC":
                        Host nodeHost = new Host(messageSplit[1], messageSplit[2]);
                        // [NOTE] Host validation missing
                        nodes.add(nodeHost); // Register node in directory
                        System.out.println("Node registered");
                        break;


                    // Node got disconnected (Sent by DisconnectController thread)
                    case "disconnect":
                        String address = messageSplit[1];
                        String port = messageSplit[2];
                        directory.removeNode(address, port); // Remove node from directory
                        break;


                    // Something else
                    default:
                        System.out.println("Invalid message");
                        continue;

                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

    }


}
