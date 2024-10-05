import java.util.ArrayList;
import java.util.Iterator;
import com.oocourse.elevator2.TimableOutput;

public class Elevator extends Thread {
    private int id;
    private int currentFloor;
    private ElevatorState state;
    private Strategy strategy;
    private final ArrayList<Person> insideList;
    private final RequestQueue requestList;

    private final RequestQueue initRequest;
    private int capacity;
    private int direction;
    private int fullCapacity;
    private double speed;

    public void addRequest(Person person) {
        requestList.add(person);
    }

    public enum ElevatorState {
        MOVING_UP,
        MOVING_DOWN,
        WAITING,
        OVER,
        RESET
    }

    public Elevator(int id, Strategy.StrategyType strategyType,RequestQueue initRequest,
                    int fullCapacity,double speed) {
        this.id = id;
        this.currentFloor = 1;
        this.state = ElevatorState.MOVING_UP;
        this.requestList = new RequestQueue();
        this.direction = 1;
        this.capacity = 0;
        this.insideList = new ArrayList<>();
        this.strategy = new Strategy(strategyType); // 使用传入的strategyType
        this.initRequest = initRequest;
        this.fullCapacity = fullCapacity;
        this.speed = speed;
    }

    public int getFullCapacity() {
        return fullCapacity;
    }

    public void run() {
        while (true) {
            synchronized (requestList) {
                if (insideList.isEmpty() && requestList.isEmpty() && requestList.isEnd()) {
                    break;
                }
                requestList.notifyAll();
            }
            Strategy.StrategyType advice = strategy.getAdvice(this.state, this.currentFloor,
                    this.capacity, this.direction, this.insideList,
                    this.requestList,this.fullCapacity);
            if (advice != null) {
                executeStrategy(advice);
            }
        }
    }

    private void executeStrategy(Strategy.StrategyType advice) {
        try {
            switch (advice) {
                case MOVE:
                    long moveTime = (long)(this.speed * 1000);
                    move(); // 电梯沿着原方向移动一层
                    sleep(moveTime);
                    int realId = id + 1;
                    TimableOutput.println("ARRIVE-" + currentFloor + "-" + realId);
                    break;
                case REVERSE:
                    long moveTime1 = (long)(speed * 1000);
                    rervese();
                    int realId1 = id + 1;
                    sleep(moveTime1);
                    TimableOutput.println("ARRIVE-" + currentFloor + "-" + realId1);
                    break;
                case WAIT:
                    synchronized (requestList) {
                        if (!requestList.isEnd()) {
                            requestList.wait();
                        }
                        requestList.notifyAll();
                    }
                    break;
                case OPEN:
                    openDoor();
                    handlePassengerOut();
                    handlePassengerIn();
                    closeDoor();
                    break;
                case RESET:
                    pullPerson();
                    resetElevator();
                    break;
                default:
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pullPerson() {
        synchronized (insideList) {
            if (!insideList.isEmpty()) {
                openDoor();
                synchronized (initRequest) {
                    for (int i = 0; i < insideList.size(); i++) {
                        int act = this.id + 1;
                        TimableOutput.println("OUT-" + insideList.get(i).getId() + "-" +
                                this.currentFloor + "-" + act);
                        if (insideList.get(i).getToFloor() != this.currentFloor) {
                            Person person = new Person(insideList.get(i).getId(),
                                    this.currentFloor, insideList.get(i).getToFloor());
                            initRequest.add(person);
                        }
                    }
                    // 添加元素后唤醒其他等待的线程
                    initRequest.notifyAll();
                }
                insideList.clear();
                this.capacity = 0;
                closeDoor();
            }
        }
    }

    public void resetElevator() {
        try {
            int id1 = this.id + 1;
            TimableOutput.println("RESET_BEGIN-" + id1);
            synchronized (requestList) {
                if (!requestList.isEmpty()) {
                    synchronized (initRequest) {
                        for (int i = 0; i < requestList.getRequestQueue().size(); i++) {
                            initRequest.add(requestList.getRequestQueue().get(i));
                        }
                        // 添加元素后唤醒其他等待的线程
                        initRequest.notifyAll();
                    }
                    requestList.getRequestQueue().clear();
                }
                requestList.notifyAll();
            }
            sleep(1200);
            TimableOutput.println("RESET_END-" + id1);
            if (direction == 1) {
                this.state = ElevatorState.MOVING_UP;
            } else {
                this.state = ElevatorState.MOVING_DOWN;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void changeDirect(int curFloor, ArrayList<Person> insideList, int direction) {
        // 检查方向是否为正向
        if (!insideList.isEmpty()) {
            boolean isPositiveDirection = direction > 0;
            Person firstPerson = insideList.get(0);
            int reqFloor = firstPerson.getToFloor();
            // 检查请求是否在当前楼层之前，并且方向与当前方向一致
            if ((isPositiveDirection && reqFloor >= curFloor)
                    || (!isPositiveDirection && reqFloor <= curFloor)) {
                return;
            } else {
                switch (state) {
                    case MOVING_UP:
                        this.direction = -1;
                        this.state = ElevatorState.MOVING_DOWN;
                        break;
                    case MOVING_DOWN:
                        this.direction = +1;
                        this.state = ElevatorState.MOVING_UP;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void handlePassengerOut() {
        synchronized (insideList) {
            Iterator<Person> iterator = insideList.iterator();
            while (iterator.hasNext()) {
                Person person = iterator.next();
                int destination = person.getToFloor();
                if (destination == currentFloor) {
                    int realId = id + 1;
                    TimableOutput.println("OUT-" + person.getId() +
                            "-" + currentFloor + "-" + realId);
                    iterator.remove(); // 从 InsideList 中移除
                    capacity--;
                }
            }
        }
    }

    private void handlePassengerIn() {
        synchronized (requestList) {
            Iterator<Person> iterator = requestList.getRequestQueue().iterator();
            while (iterator.hasNext()) {
                Person person = iterator.next();
                int destination = person.getFromFloor();
                if (insideList.isEmpty()) {
                    if (destination == currentFloor && capacity < fullCapacity) {
                        int realId = id + 1;
                        TimableOutput.println("IN-" + person.getId()
                            + "-" + currentFloor + "-" + realId);
                        iterator.remove(); // 从 RequestList 中移除
                        insideList.add(person); // 将乘客添加到 InsideList 中
                        capacity++;
                        changeDirect(currentFloor, insideList, direction);
                        continue;
                    }
                }
                if (!insideList.isEmpty()) {
                    if (destination == currentFloor && capacity < fullCapacity
                        && (direction * (person.getToFloor() - person.getFromFloor()) > 0)) {
                        int realId = id + 1;
                        TimableOutput.println("IN-" + person.getId()
                            + "-" + currentFloor + "-" + realId);
                        iterator.remove(); // 从 RequestList 中移除
                        insideList.add(person); // 将乘客添加到 InsideList 中
                        capacity++;
                    }
                }
            }
        }
    }

    public void move() {
        switch (state) {
            case MOVING_UP:
                currentFloor++;
                this.direction = 1;
                break;
            case MOVING_DOWN:
                currentFloor--;
                this.direction = -1;
                break;
            default:
                break;
        }
    }

    public void rervese() {
        switch (state) {
            case MOVING_UP:
                currentFloor--;
                this.direction = -1;
                this.state = ElevatorState.MOVING_DOWN;
                break;
            case MOVING_DOWN:
                currentFloor++;
                this.direction = +1;
                this.state = ElevatorState.MOVING_UP;
                break;
            default:
                break;
        }
    }

    public void openDoor() {
        int realId = id + 1;
        TimableOutput.println("OPEN-" + currentFloor + "-" + realId);
        try {
            sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void closeDoor() {
        try {
            sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int realId = id + 1;
        TimableOutput.println("CLOSE-" + currentFloor + "-" + realId);
    }

    public void setState(ElevatorState state) {
        this.state = state;
    }

    public RequestQueue getRequestList() {
        return requestList;
    }

    public void setFullCapacity(int fullCapacity) {
        this.fullCapacity = fullCapacity;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public ArrayList<Person> getInsideList() {
        return insideList;
    }

    public RequestQueue getInitRequest() {
        return initRequest;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDirection() {
        return direction;
    }

    public int getElevatorId() {
        return id;
    }

    public ElevatorState getElevatorState() {
        return state;
    }
}
