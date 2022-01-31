import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/*
 * 	Node class -> Most important class of this system
 *
 * */
public class StorageNode implements Serializable{

    private Host host;
    private CloudByte[] data;
    private ServerSocket serverSocket;
    private ArrayList<Host> nodes;
    private ArrayList<Integer> errors;


    /*
     * 	Constructor for node that has local data
     *
     * */
    public StorageNode(String address, String port, String filePath) {

        this.host = new Host(address, port);
        this.data = new CloudByte[1000000];
        this.errors = new ArrayList<Integer>();


        ReadLocalData(filePath); // Read local data (in Executables folder)

        System.out.println("Node " + address + " " + port + " created and registered. This node has local data");
    }

    /*
     * 	Constructor for node that does NOT have local data -> must download
     *
     * */
    public StorageNode(String address, String port) {
        this.host = new Host(address, port);
        this.data = new CloudByte[1000000];
        this.errors = new ArrayList<Integer>();


        nodes = DirectoryManager.GetRegisteredNodes(address, port); // Get registered nodes

        System.out.println("Node " + address + " " + port + " created and registered. This node does not have local data");

        DownloadData(nodes);
    }


    /*
     * 	Read local file and populate data attribute
     *
     * */
    private void ReadLocalData(String filePath) {
        try {

            File file = new File(filePath);
            byte[] localData = Files.readAllBytes(file.toPath());

            for(int i = 0; i < data.length; i++) {
                data[i] = new CloudByte(localData[i]);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * 	Download data from other nodes
     *
     * */
    private void DownloadData(ArrayList<Host> nodes) {
        try {
            System.out.println("Creating requests for node " + this.host.getAddress() + " " + this.host.getPort());

            int numberOfRequests = 100;
            int length = 10000; // 10 000 bytes
            int startIndex = 0;

            CountDownLatch countDownLatch = new CountDownLatch(numberOfRequests);
            BlockingQueue requests = new BlockingQueue(numberOfRequests);


            for(int i = 0; i < numberOfRequests; i++) {

                requests.offer(new ByteBlockRequest(startIndex, length, this.host));
                startIndex = startIndex + length;

            }
            System.out.println("[" + this.host.getPort() + "] Created requests for data download");

            if(nodes.size() == 0){
                data = null;
                System.err.println("[" + this.host.getPort() + "] There are no nodes available for data download");
                return;
            }

            for(Host nodeHost : nodes){
                new AnswerRequest(nodeHost, this, requests, countDownLatch).start();
            }

            System.out.println("[" + this.host.getPort() + "] Initialized threads and is now waiting");

            countDownLatch.await();

            int i = 0;
            for(CloudByte c : data){
                if(c == null)
                    System.err.println("NULL EM " + i);
                i++;
            }



        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     *  Called on node creation
     *  Will listen to data correction, data population and data retrieval messages
     *  Will also create two types of threads:
     *  	- Error injector
     *  	- Error detector
     *  This threads will be running constantly
     *
     * */
    public void Init() {

        if(data == null){
            System.err.println("[" + this.host.getPort() + "] Will terminate");
            return;
        }

        try {

            DirectoryManager.Register(getAddress(), getPort());

            new ErrorInjector(this).start();
            new ErrorDetector(this).start();
            new ErrorDetector(this).start();

            Socket socket;
            serverSocket = new ServerSocket(this.host.getPortInt());
            System.out.println("[" + this.host.getPort() + "] Pronto para receber pedidos");

            while(true) {

                socket = serverSocket.accept(); // Accepts connections


                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                MessageType messageType = (MessageType) objectInputStream.readObject(); // Type of message

                if(messageType == null) continue;

                switch(messageType) {

                    // Data retrieval message
                    case GETDATA:

                        int startIndex = (int) objectInputStream.readObject();
                        int length = (int) objectInputStream.readObject();

                        CloudByte[] pieceOfData = new CloudByte[length];


                        for(int i = 0; i < pieceOfData.length; i++) {

                            if(!data[startIndex + i].isParityOk()){
                                System.err.println("[" + this.host.getPort() + "] Is parity ok? " + (startIndex + i) + " " + data[startIndex + i].isParityOk());
                                pieceOfData = null;
                                objectOutputStream.writeObject(pieceOfData);
                                break;
                            }

                            pieceOfData[i] = data[startIndex + i];
                        }

                        objectOutputStream.writeObject(pieceOfData);

                        break;

                    default:
                        continue;

                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    public synchronized boolean errorsContain(int index) {
        return errors.contains(index);
    }

    public synchronized void errorsRemove(int index) {

        for(int i = 0; i < errors.size(); i++){
            if(errors.get(i) == index)
                errors.remove(i);
        }

    }

    public synchronized void errorsAdd(int index) {
        errors.add(index);
    }

    public ArrayList<Host> getRegisteredNodes() {
        return nodes;
    }

    public String getAddress() {
        return host.getAddress();
    }

    public Host getNodeHost() {
        return host;
    }

    public String getPort() {
        return host.getPort();
    }

    public int getPortInt() {
        return host.getPortInt();
    }

    public synchronized CloudByte[] getData() {
        return data;
    }

    public void setData(CloudByte[] data_) {
        this.data = data_;
    }

    public void setDataInIndex(int index, CloudByte data_) {
        this.data[index] = data_;
    }

    public static void main(String[] args) {


        if (args.length == 4){
            String filePath = new File("Executables/" + args[3]).getAbsolutePath();
            new StorageNode(args[1], args[2], filePath).Init();
        }
        else{
            new StorageNode(args[1], args[2]).Init();
        }
    }


}
