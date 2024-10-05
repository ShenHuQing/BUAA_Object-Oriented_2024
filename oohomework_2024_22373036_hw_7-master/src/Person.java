import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Person {
    private final int id;
    private int fromFloor;
    private final int toFloor;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public Person(int id, int fromFloor, int toFloor) {
        this.id = id;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
    }

    public int getId() {
        rwLock.readLock().lock();
        try {
            return id;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public int getFromFloor() {
        rwLock.readLock().lock();
        try {
            return fromFloor;
        } finally {
            rwLock.readLock().unlock();
        }

    }

    public int getToFloor() {

        rwLock.readLock().lock();
        try {
            return toFloor;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void setFromFloor(int changeStart) {
        rwLock.writeLock().lock();
        try {
            this.fromFloor = changeStart;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        return this.id + "+" + this.fromFloor + "+" + this.toFloor;
    }
}