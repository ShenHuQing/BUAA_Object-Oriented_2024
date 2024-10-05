import com.oocourse.elevator1.TimableOutput;
import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private int id;
    private int currentFloor;
    private ElevatorState state;
    private Strategy strategy;
    private ArrayList<Person> insideList;
    private RequestQueue requestList;

    private RequestQueue initRequest;
    private int capacity;
    private int direction;

    public void addRequest(Person person) {
        requestList.add(person);
    }

    public enum ElevatorState {
        MOVING_UP,
        MOVING_DOWN,
        WAITING,
        OVER
    }

    public Elevator(int id, Strategy.StrategyType strategyType,RequestQueue initRequest) {
        this.id = id;
        this.currentFloor = 1;
        this.state = ElevatorState.MOVING_UP;
        this.requestList = new RequestQueue();
        this.direction = 1;
        this.capacity = 0;
        this.insideList = new ArrayList<>();
        this.strategy = new Strategy(strategyType, requestList); // 使用传入的strategyType
        this.initRequest = initRequest;
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
                    this.capacity, this.direction, this.insideList, this.requestList);
            if (advice != null) {
                executeStrategy(advice);
            }
        }
    }

    private void executeStrategy(Strategy.StrategyType advice) {
        try {
            switch (advice) {
                case OVER:
                    // 结束电梯线程
                    return;
                case MOVE:
                    sleep(400);
                    move(); // 电梯沿着原方向移动一层
                    int realId = id + 1;
                    TimableOutput.println("ARRIVE-" + currentFloor + "-" + realId);
                    break;
                case REVERSE:
                    sleep(400);
                    rervese();
                    int realId1 = id + 1;

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
                default:
                    break;
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
        Iterator<Person> iterator = insideList.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            int destination = person.getToFloor();
            if (destination == currentFloor) {
                int realId = id + 1;
                TimableOutput.println("OUT-" + person.getId() + "-" + currentFloor + "-" + realId);
                iterator.remove(); // 从 InsideList 中移除
                capacity--;
            }
        }
    }

    private void handlePassengerIn() {
        Iterator<Person> iterator = requestList.getRequestQueue().iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            int destination = person.getFromFloor();
            if (insideList.isEmpty()) {
                if (destination == currentFloor && capacity < 6) {
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
                if (destination == currentFloor && capacity < 6
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

}