import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * 	Class responsible for injecting errors in node data
 * 	It listens to node console
 *
 * */
public class ErrorInjector extends Thread {

    private static StorageNode node;
    private static BufferedReader in;


    public ErrorInjector(StorageNode node_) {
        System.out.println("ErrorInjector created for node " + node_.getAddress() + " " + node_.getPort());
        node = node_;
        in = new BufferedReader(new InputStreamReader(System.in)); // Listens to node console
    }

    /*
     * 	Closes buffered reader connection
     *
     * */
    private static void CloseConnection() {

        try {

            in.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        try {

            while(true) {

                String userInput = (String)in.readLine(); // Reads console

                if(userInput == null) continue;

                String[] userInputSplit = userInput.split(" ");

                if(userInputSplit[0].equals("ERROR")) {

                    if(!Utils.ErrorIndexIsValid(userInputSplit[1])) { // Input validation
                        System.out.println("Invalid number, please try again");
                        continue;
                    }

                    System.out.println("Injection message received");

                    int errorIndex = Integer.parseInt(userInputSplit[1]); // Error index for the corruption

                    System.out.println("[" + node.getPort() + "] Before: Node data is corrupted? " + !node.getData()[errorIndex].isParityOk());

                    node.getData()[errorIndex].makeByteCorrupt(); // Corrupt cloud byte

                    System.out.println("[" + node.getPort() + "] After: Node data is corrupted? " + !node.getData()[errorIndex].isParityOk());

                }
                else
                    System.out.println("Invalid format. Must be in format ERROR <number>");

            }

        } catch (IOException | NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            CloseConnection();
        }

    }


}
