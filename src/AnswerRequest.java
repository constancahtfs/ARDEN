
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class AnswerRequest extends Thread{
    private Host host;
    private BlockingQueue requests;
    private CountDownLatch countDownLatch;
    private StorageNode node;
    private int requestCount;
    private ArrayList<CloudByte> comparator;

    public AnswerRequest(Host host_, StorageNode node_, BlockingQueue requests_, CountDownLatch countDownLatch_) throws IOException {
        host = host_;
        node = node_;
        requests = requests_;
        countDownLatch = countDownLatch_;
        requestCount = 0;
        comparator = null;
    }

    public AnswerRequest(Host host_, StorageNode node_, BlockingQueue requests_, CountDownLatch countDownLatch_, ArrayList<CloudByte> comparator_) throws IOException {
        host = host_;
        node = node_;
        requests = requests_;
        countDownLatch = countDownLatch_;
        requestCount = 0;
        comparator = comparator_;
    }



    public ByteBlockRequest DownloadData(ByteBlockRequest request){
        try{

            Socket socket = new Socket(this.host.getAddress(), this.host.getPortInt());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while(true) {

                out.writeObject(MessageType.GETDATA);
                out.writeObject(request.getStartIndex());
                out.writeObject(request.getLength());

                CloudByte[] data = (CloudByte[]) in.readObject();

                if(data == null) continue;

                request.setData(data);

                out.close();
                in.close();
                socket.close();

                return request;

            }
        }
        catch(Exception e){
            System.err.println("[THREAD " + this.host.getPort() + "] Could not download data");
        }
        return null;
    }

    public void FillNodeData(ByteBlockRequest request){

        CloudByte[] data = this.node.getData();
        int startIndex = request.getStartIndex();
        CloudByte[] downloadedData = request.getData();
        for(int i = 0; i < request.getLength(); i++){
            data[i + startIndex] = downloadedData[i];
        }

    }


    public void AnswerUnitRequest(){

        try{

            while(requests.size() != 0){

                ByteBlockRequest request = (ByteBlockRequest) requests.poll();
                ByteBlockRequest requestFilled = DownloadData(request);

                if(requestFilled == null){
                    requests.offer(request);
                    System.err.println("[THREAD " + this.host.getPort() + "] Will terminate");

                    countDownLatch.countDown();
                    return;
                }

                requestCount++;



                comparator.add(requestFilled.getData()[0]);

                countDownLatch.countDown();



            }

            System.out.println("[THREAD " + this.host.getPort() + "] Respondeu a " + requestCount + " pedidos.");
            countDownLatch.await();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {

        System.out.println("[THREAD " + this.host.getPort() + "] Vai responder a pedidos; nº de pedidos: " + requests.size());

        if(comparator != null){
            AnswerUnitRequest();
            return;
        }

        try {

            while(requests.size() != 0){

                ByteBlockRequest request = (ByteBlockRequest) requests.poll();
                ByteBlockRequest requestFilled = DownloadData(request);

                if(requestFilled == null){
                    requests.offer(request);
                    System.err.println("[THREAD " + this.host.getPort() + "] Will terminate");

                    return;
                }

                requestCount++;


                FillNodeData(requestFilled);
                countDownLatch.countDown();

            }

            System.out.println("[THREAD " + this.host.getPort() + "] Respondeu a " + requestCount + " pedidos.");
            countDownLatch.await();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
