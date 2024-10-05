import com.oocourse.elevator2.TimableOutput;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ElevatorController extends Thread {
    private  ArrayList<Elevator> elevators;
    private ArrayList<RequestQueue> requests;
    private final RequestQueue requestInit;
    private int random = 0;

    private static boolean isEnd;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ElevatorController(int numberOfElevators,RequestQueue requestInit
            , ArrayList<RequestQueue> requests) {
        this.elevators = new ArrayList<>();
        for (int i = 0; i < numberOfElevators; i++) {
            elevators.add(new Elevator(i, Strategy.StrategyType.WAIT,requestInit,6,0.4));
        }
        this.requests = requests;
        this.requestInit = requestInit;
    }

    public void setEnd(boolean flag) {
        lock.writeLock().lock();
        try {
            isEnd = flag;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean setFinished() {
        boolean y = isFinished();
        requestInit.notifyAll();
        return y;
    }

    public boolean isFinished() {
        if (!isEnd) {
            return false;
        }
        if (!requestInit.isEmpty()) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            boolean isEmpty;
            int capacity;
            synchronized (elevators.get(i)) {
                isEmpty = elevators.get(i).getRequestList().isEmpty();
                capacity = elevators.get(i).getCapacity();
            }
            if (!isEmpty || capacity != 0) {
                return false;
            }
        }

        return true;
    }

    public boolean isReset() {
        for (int i = 0; i < 6; i++) {
            synchronized (elevators.get(i)) {
                Boolean isReset = elevators.get(i).getElevatorState()
                        == Elevator.ElevatorState.RESET;
                if (isReset) {
                    return true;
                }
            }
        }
        return false;
    }

    public void run() {
        while (true) {
            synchronized (requestInit) {
                while (requestInit.isEmpty() && !requestInit.isEnd()) {
                    try {
                        requestInit.wait(); // 没有请求时，等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (isReset()) {
                    try {
                        requestInit.wait(1200); // 没有请求时，等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (requestInit.isEmpty() && requestInit.isEnd() && !isReset()) {
                    requestInit.setEnd(true);
                    synchronized (requests) {
                        for (int i = 0; i < requests.size(); i++) {
                            requests.get(i).setEnd(true);
                        }
                        requestInit.notifyAll();
                        return;
                    }
                }

            }

            Person person = requestInit.getOneRequestAndRemove();
            if (person == null) {
                continue;
            }
            int selectedElevator = assignPerson(person);
            elevators.get(selectedElevator).addRequest(person);
            int actuall = selectedElevator + 1;
            TimableOutput.println("RECEIVE-" + person.getId() + "-" + actuall);

        }
    }

    private int assignPerson(Person person) {
        lock.readLock().lock();
        int personFloor = person.getFromFloor();
        int personDestFloor = person.getToFloor();
        int personDirection = personDestFloor > personFloor ? 1 : -1; // 1表示向上，-1表示向下
        int nearestElevatorId = 10;
        int minDistance = Integer.MAX_VALUE;
        try {
            for (Elevator elevator : elevators) {
                int num = elevator.getInsideList().size() +
                        elevator.getRequestList().getRequestQueue().size();
                if (elevator.getElevatorState() != Elevator.ElevatorState.RESET) {
                    int elevatorDirection = elevator.getDirection();
                    if (num < elevator.getFullCapacity()) {
                        int elevatorFloor = elevator.getCurrentFloor();
                        if ((elevatorDirection == personDirection
                                || elevator.getRequestList().isEmpty() &&
                                elevator.getInsideList().isEmpty())) { // 0表示电梯当前无运行方向
                            int distance = Math.abs(elevatorFloor - personFloor);
                            if (distance < minDistance) {
                                minDistance = distance;
                                nearestElevatorId = elevator.getElevatorId();
                            }
                        }
                    }
                }
            }
            if (nearestElevatorId == 10) {
                int minNum = 100000;
                for (Elevator elevator : elevators) {
                    int num = elevator.getInsideList().size() +
                            elevator.getRequestList().getRequestQueue().size();
                    if (elevator.getElevatorState() != Elevator.ElevatorState.RESET) {
                        if (num < minNum) {
                            minNum = num;
                            nearestElevatorId = elevator.getElevatorId();
                        }
                    }
                }
                return nearestElevatorId;
            } else {
                return nearestElevatorId;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public ArrayList<Elevator> getElevators() {
        return elevators;
    }

}

