import java.util.ArrayDeque;
import java.util.Queue;

public class BlockingQueue<ByteBlockRequest> {

    private Queue<ByteBlockRequest> q;

    public BlockingQueue(int size){
        if(!(size>0))
            throw new IllegalArgumentException("Fila tem de ser maior que 0");
        /* ArrayDeque allows us to add or remove an element from both sides */
        q = new ArrayDeque<>(size);
    }

    public synchronized void offer(ByteBlockRequest b) {
        q.add(b);
    }

    public synchronized ByteBlockRequest poll() {
        return q.remove();
    }

    public boolean isEmpty() {
        return q.isEmpty();
    }

    public synchronized int size() {
        return q.size();
    }
}