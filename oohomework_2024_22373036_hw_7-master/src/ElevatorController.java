import com.oocourse.elevator3.TimableOutput;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ElevatorController extends Thread {
    private static ArrayList<Elevator> elevators;
    private static ArrayList<RequestQueue> requests;
    private final RequestQueue requestInit;
    private int random = 0;

    private int transNum;

    private static boolean isEnd;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ElevatorController(int numberOfElevators,RequestQueue requestInit
            , ArrayList<RequestQueue> requests) {
        this.elevators = new ArrayList<>();
        for (int i = 0; i < numberOfElevators; i++) {
            elevators.add(new Elevator(String.valueOf(i), Strategy.StrategyType.WAIT,
                    requestInit,6,0.4,1,11));
        }
        this.requests = requests;
        this.requestInit = requestInit;
    }

    public int getTransNum() {
        lock.readLock().lock();
        try {
            return transNum;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setTransNum(int transNum) {
        lock.writeLock().lock();
        try {
            this.transNum = transNum;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void setEnd(boolean flag) {
        lock.writeLock().lock();
        try {
            isEnd = flag;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void addElevator(Elevator elevator) {
        lock.writeLock().lock();
        try {
            elevators.add(elevator);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isFinished() {
        if (!isEnd) {
            return false;
        }
        if (!requestInit.isEmpty()) {
            return false;
        }
        for (Elevator elevator : elevators) {
            boolean isEmpty;
            int capacity;
            synchronized (elevator) {
                isEmpty = elevator.getRequestList().isEmpty();
                capacity = elevator.getCapacity();
            }
            if (!isEmpty || capacity != 0) {
                System.out.println(elevator.getRequestList().getRequestQueue().get(0));
                return false;
            }
        }

        return true;
    }

    public boolean isReset() {
        for (Elevator elevator : elevators) {
            synchronized (elevator) {
                Boolean isReset = elevator.getElevatorState() == Elevator.ElevatorState.DCRESET
                        || elevator.getElevatorState()
                        == Elevator.ElevatorState.NORMALRESET;
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
                if (requestInit.isEnd() && requestInit.isEmpty() && !CalculateNum.isEnd()) {
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (requestInit.isEmpty() && requestInit.isEnd() && CalculateNum.isEnd()) {
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
            Elevator selectedElevator = assignPerson(person);
            if (selectedElevator.getElevatorState() == Elevator.ElevatorState.NORMALRESET
                   || selectedElevator.getElevatorState() == Elevator.ElevatorState.DCRESET) {
                selectedElevator.addRequest(person);
                selectedElevator.addresettingRecieve(person);
            }
            else {
                TimableOutput.println("RECEIVE-" + person.getId() +
                        "-" + selectedElevator.getPrintId());
                selectedElevator.addRequest(person);
            }
        }
    }

    private Elevator assignPerson(Person person) {
        lock.readLock().lock();
        int personFloor = person.getFromFloor();
        int personDestFloor = person.getToFloor();
        int personDirection = personDestFloor > personFloor ? 1 : -1; // 1表示向上，-1表示向下
        Elevator nearestElevator = null;
        int minDistance = Integer.MAX_VALUE;
        try {
            for (Elevator elevator : elevators) {
                if (elevator.canArrive(person.getFromFloor()) &&
                        elevator.canArrive((person.getToFloor()))) {
                    int num = elevator.getInsideList().size() +
                            elevator.getRequestList().getRequestQueue().size();
                    int elevatorDirection = elevator.getDirection();
                    if (num < elevator.getFullCapacity()) {
                        if ((elevatorDirection == personDirection
                                || elevator.getRequestList().isEmpty() &&
                                elevator.getInsideList().isEmpty())) { // 0表示电梯当前无运行方向
                            if (Math.abs(elevator.getCurrentFloor() - personFloor) < minDistance) {
                                minDistance = Math.abs(elevator.getCurrentFloor() - personFloor);
                                nearestElevator = elevator;
                            }
                        }
                    }
                }
            } //人少不用换乘距离近
            if (nearestElevator == null) {
                int minNum = 100000;
                for (Elevator elevator : elevators) {
                    if (elevator.canArrive(person.getFromFloor())
                            && elevator.canArrive(person.getToFloor())) {
                        if (elevator.getRequestList().getRequestQueue().size()
                                + elevator.getInsideList().size() < 18) {
                            int num = elevator.getInsideList().size() +
                                    elevator.getRequestList().getRequestQueue().size();
                            if (num < minNum) {
                                minNum = num;
                                nearestElevator = elevator;
                            }
                        }
                    }
                }
            } //不用换乘的人最少的
            if (nearestElevator == null) {
                int minNum = 100000;
                for (Elevator elevator : elevators) {
                    if (elevator.canArrive(person.getFromFloor())) {
                        int num = elevator.getInsideList().size() +
                                elevator.getRequestList().getRequestQueue().size();
                        if (num < minNum) {
                            minNum = num;
                            nearestElevator = elevator;
                        }
                    }
                }
            } return nearestElevator;
        } finally {
            lock.readLock().unlock();
        }
    }

    public ArrayList<Elevator> getElevators() {
        return elevators;
    }

    public static Elevator setElevater(int id, RequestQueue personRequests, int capacity,
                                       double speed, int transformFloor, Elevator elevator) {
        Elevator elevator1 = new Elevator(id + "-B", Strategy.StrategyType.MOVE,
                personRequests, capacity,speed,transformFloor,11);
        elevator1.setCurrentFloor(transformFloor + 1);
        elevator1.setDouble(true);
        elevator1.setOther(elevator);
        elevator1.setTransFloor(transformFloor);
        ElevatorController.addElevator(elevator1);
        requests.add(elevator1.getRequestList());
        elevator1.start();
        return elevator1;
    }
}

