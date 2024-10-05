import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RequestQueue {
    private ArrayList<Person> requestQueue; // 将 requestQueue 改为实例变量
    private boolean isOver;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public RequestQueue() {
        this.requestQueue = new ArrayList<>();
        this.isOver = false;
    }

    public synchronized void add(Person person) {
        requestQueue.add(person);
        notifyAll();
    }

    public synchronized Person getOneRequestAndRemove() {
        while (isEmpty() && !isEnd()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (requestQueue.isEmpty()) {
            return null;
        }
        Person firstPerson = requestQueue.get(0);
        requestQueue.remove(0);
        notifyAll(); // 删除元素后通知等待的线程
        return firstPerson;
    }

    public ArrayList<Person> getRequestQueue() {
        rwLock.readLock().lock();
        try {
            return requestQueue;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public synchronized void setEnd(boolean isOver) {
        this.isOver = isOver;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return isOver;
    }

    public synchronized boolean isEmpty() {
        return requestQueue.isEmpty();
    }

}
