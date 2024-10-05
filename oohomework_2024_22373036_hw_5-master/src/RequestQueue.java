import java.util.ArrayList;

public class RequestQueue {
    private ArrayList<Person> requestQueue; // 将 requestQueue 改为实例变量
    private boolean isOver;

    public RequestQueue() {
        this.requestQueue = new ArrayList<>();
        this.isOver = false;
    }

    public synchronized void add(Person person) {
        requestQueue.add(person);
        notifyAll(); // 添加元素后通知等待的线程
    }

    public synchronized Person getFirst() {
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
        notifyAll();
        return firstPerson;
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
        return requestQueue;
    }

    //    private Person assignPerson(Person person) {
    //        int personFloor = person.getFromFloor();
    //        int personDestFloor = person.getToFloor();
    //        int personDirection = personDestFloor > personFloor ? 1 : -1; // 1表示向上，-1表示向下
    //        Elevator nearestElevator = null;
    //        int minDistance = Integer.MAX_VALUE;
    //        for (Elevator elevator : elevators) {
    //            int elevatorDirection = elevator.getDirection();
    //            if (elevator.getInsideList().size() < elevator.getCapacity()) {
    //                int elevatorFloor = elevator.getCurrentFloor();
    //                if (elevator.getStatement() == Elevator.ElevatorState.WAITING ||
    //                        (elevatorDirection == personDirection
    //                        || elevatorDirection == 0)) { // 0表示电梯当前无运行方向
    //
    //                    int distance = Math.abs(elevatorFloor - personFloor);
    //                    if (distance < minDistance) {
    //                        minDistance = distance;
    //                        nearestElevator = elevator;
    //                    }
    //                }
    //            }
    //        }
    //
    //        return nearestElevator;
    //    }

    public synchronized void setEnd(boolean isOver) {
        this.isOver = isOver;
        notifyAll(); // 设置结束状态后通知等待的线程
    }

    public synchronized boolean isEnd() {
        return isOver;
    }

    public synchronized boolean isEmpty() {
        return requestQueue.isEmpty();
    }

    public synchronized void deletePerson(Person person) {
        requestQueue.remove(person);
    }
}
