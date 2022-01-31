import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/*
 * 	Class responsible for detecting errors in node data
 *
 * */
public class ErrorDetector extends Thread {

    private StorageNode node;


    public ErrorDetector(StorageNode node_) {
        System.out.println("Error detector created for node " + node_.getAddress() + " " + node_.getPort());
        node = node_;
    }




    /*
     * 	Sends unit correction request for corrupted cloud byte
     *
     * */
    private void SendRequest(int errorIndex) {

        try{

            System.out.println("[" + this.node.getPort() + "] Created request for data download");

            ArrayList<Host> nodes = DirectoryManager.GetRegisteredNodes(node.getAddress(), node.getPort());

            ArrayList<CloudByte> cbComparator = new ArrayList<CloudByte>();
            CountDownLatch countDownLatch = new CountDownLatch(nodes.size());

            for(Host nodeHost : nodes){

                BlockingQueue requests = new BlockingQueue(1);
                requests.offer(new ByteBlockRequest(errorIndex, 1, this.node.getNodeHost()));

                new AnswerRequest(nodeHost, node, requests, countDownLatch, cbComparator).start();
            }

            System.out.println("[" + this.node.getPort() + "] Initialized threads and is now waiting");

            countDownLatch.await();

            CloudByte answer = cbComparator.get(0);
            System.out.println("[" + this.node.getPort() + "] Got answer! " + answer);

            node.setDataInIndex(errorIndex , answer);





        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    public void run() {

        try {

            while(true) {

                CloudByte[] nodeData = node.getData();

                for(int i = 0; i < nodeData.length; i++) {

                    if(node.getData()[i] == null) { // If this occurs, an error was made -> data can NOT be null

                        System.err.println("[" + node.getPort() + "] CloudByte is null: " + i);

                    }
                    else {

                        if(!node.getData()[i].isParityOk() && !node.errorsContain(i)) { // Error detected in cloud byte

                            node.errorsAdd(i);
                            System.err.println("[" + node.getPort() + "] ERROR on index " + i);

                            SendRequest(i); // Send table unit request for corrupted cloud byte correction

                            System.out.println("[" + node.getPort() + "] Correção feita - " + node.getData()[i].isParityOk());
                            node.errorsRemove(i);

                        }
                    }
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
