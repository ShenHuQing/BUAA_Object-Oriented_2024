import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.TimableOutput;
import java.util.ArrayList;
import java.util.Iterator;

public class Elevator extends Thread {
    private String id;
    private boolean isDouble;
    private int maxFloor;
    private int minFloor;
    private int transFloor;
    private DoubleCarResetRequest dcReset;
    private Elevator other;
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
    private RequestQueue resettingRecieve;

    public void addRequest(Person person) {
        requestList.add(person);
    }

    public void addresettingRecieve(Person person) {
        resettingRecieve.add(person);
    }

    public enum ElevatorState {
        MOVING_UP,
        MOVING_DOWN,
        WAITING,
        OVER,
        NORMALRESET,
        DCRESET
    }

    public Elevator(String id, Strategy.StrategyType strategyType,RequestQueue initRequest,
                    int fullCapacity,double speed,int minFloor,int maxFloor) {
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
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.isDouble = false;
        this.other = null;
        this.resettingRecieve = new RequestQueue();
    }

    public int getFullCapacity() {
        return fullCapacity;
    }

    public void run() {
        while (true) {
            leaveTransFloor();
            synchronized (requestList) {
                if (insideList.isEmpty() && requestList.isEmpty() && requestList.isEnd()) {
                    break;
                }
                requestList.notifyAll();
            }
            Strategy.StrategyType advice = strategy.getAdvice(this.state, this.currentFloor,
                    this.capacity,  this.insideList,
                    this.requestList, this.fullCapacity, this.transFloor, this);
            if (advice != null) {
                executeStrategy(advice);
            }
        }
    }

    void executeStrategy(Strategy.StrategyType advice) {
        try {
            switch (advice) {
                case MOVE:
                    long moveTime = (long)(this.speed * 1000);
                    move(); // 电梯沿着原方向移动一层
                    sleep(moveTime);
                    TimableOutput.println("ARRIVE-" + currentFloor + "-" + this.getPrintId());
                    break;
                case REVERSE:
                    long moveTime1 = (long)(speed * 1000);
                    rervese();
                    sleep(moveTime1);
                    TimableOutput.println("ARRIVE-" + currentFloor + "-" + this.getPrintId());
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
                        TimableOutput.println("OUT-" + insideList.get(i).getId() + "-" +
                                this.currentFloor + "-" + this.getPrintId());
                        if (insideList.get(i).getToFloor() != this.currentFloor) {
                            Person person = new Person(insideList.get(i).getId(),
                                    this.currentFloor, insideList.get(i).getToFloor());
                            initRequest.add(person);
                        } else {
                            CalculateNum.deleteNum();
                        }
                    }
                    initRequest.notifyAll();
                }
                insideList.clear();
                this.capacity = 0;
                closeDoor();
            }
        }
    }

    public void leaveTransFloor() {
        if (this.isDouble && this.currentFloor == this.transFloor
                && this.getInsideList().isEmpty() && this.getRequestList().isEmpty()) {
            long moveTime = (long)(this.speed * 1000);
            try {
                sleep(moveTime);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (this.id.contains("A")) {
                currentFloor--;
                this.direction = -1;
                this.state = ElevatorState.MOVING_DOWN;
                TimableOutput.println("ARRIVE-" + currentFloor + "-" + this.getPrintId());
            } else {
                currentFloor++;
                this.direction = 1;
                this.state = ElevatorState.MOVING_UP;
                TimableOutput.println("ARRIVE-" + currentFloor + "-" + this.getPrintId());
            }
        }
    }

    public void resetElevator() {
        try {
            TimableOutput.println("RESET_BEGIN-" + this.getPrintId());
            synchronized (requestList) {
                if (!requestList.isEmpty()) {
                    synchronized (initRequest) {
                        for (int i = 0; i < requestList.getRequestQueue().size(); i++) {
                            initRequest.add(requestList.getRequestQueue().get(i));
                        }
                        initRequest.notifyAll();
                    }
                    requestList.getRequestQueue().clear();
                }
                requestList.notifyAll();
            }
            sleep(1200);
            TimableOutput.println("RESET_END-" + this.getPrintId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (this.state == ElevatorState.DCRESET) {
            int capacity = dcReset.getCapacity();
            double speed = dcReset.getSpeed();
            int transformFloor = dcReset.getTransferFloor();
            this.setFullCapacity(capacity);
            this.setSpeed(speed);
            this.setMinFloor(1);
            this.setMaxFloor(transformFloor);
            this.setDouble(true);
            this.setDirection(-1);
            this.state = ElevatorState.MOVING_DOWN;
            this.setCurrentFloor(transformFloor - 1);
            this.setTransFloor(transformFloor);
            this.setOther(ElevatorController.setElevater(Integer.parseInt(id),
                    initRequest,capacity,speed,transformFloor,this));
            this.setId(this.id + "-A");
        } else {
            if (direction == 1) {
                this.state = ElevatorState.MOVING_UP;
            } else {
                this.state = ElevatorState.MOVING_DOWN;
            }
        }
        if (!resettingRecieve.isEmpty()) {
            for (int i = 0;i < resettingRecieve.getRequestQueue().size(); i++) {
                TimableOutput.println("RECEIVE-" +
                        resettingRecieve.getRequestQueue().get(i).getId()
                        + "-" + this.getPrintId());
            }
        }
    }

    public void changeDirect(int curFloor, ArrayList<Person> insideList, int direction) {
        // 检查方向是否为正向
        if (!insideList.isEmpty()) {
            boolean isPositiveDirection = direction > 0;
            Person firstPerson = insideList.get(0);
            int reqFloor = firstPerson.getToFloor();
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
                    TimableOutput.println("OUT-" + person.getId() +
                            "-" + currentFloor + "-" + this.getPrintId());
                    iterator.remove(); // 从 InsideList 中移除
                    capacity--;
                    CalculateNum.deleteNum();
                }
                if (currentFloor == transFloor && !canArrive(person.getToFloor())) {
                    TimableOutput.println("OUT-" + person.getId() +
                            "-" + currentFloor + "-" + this.getPrintId());
                    iterator.remove(); // 从 InsideList 中移除
                    capacity--;
                    initRequest.add(person);
                    person.setFromFloor(currentFloor);
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
                        TimableOutput.println("IN-" + person.getId()
                                + "-" + currentFloor + "-" + this.getPrintId());
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
                        TimableOutput.println("IN-" + person.getId()
                                + "-" + currentFloor + "-" + this.getPrintId());
                        iterator.remove(); // 从 RequestList 中移除
                        insideList.add(person); // 将乘客添加到 InsideList 中
                        capacity++;
                    }
                }
            }
        }
    }

    public int calculateMoveFloor() {
        int moveFloor = 0;
        switch (state) {
            case MOVING_UP:
                moveFloor = currentFloor + 1;
                break;
            case MOVING_DOWN:
                moveFloor = currentFloor - 1;
                break;
            default:
                break;
        }
        return moveFloor;
    }

    public void move() {
        if (this.getOther() != null) {
            synchronized (this) {
                while (calculateMoveFloor() == transFloor
                        || this.getOther().currentFloor == transFloor) {
                    if (this.getOther().currentFloor != transFloor) {
                        break;
                    }
                    try {
                        wait(100); // 确保在同步块中调用wait
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // 处理中断异常
                        return; // 适当地处理中断
                    }
                }
            }
        }
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
        if (this.getOther() != null) {
            synchronized (this) {
                while (calculateMoveFloor() == transFloor
                        || this.getOther().currentFloor == transFloor) {
                    if (this.getOther().currentFloor != transFloor) {
                        break;
                    }
                    try {
                        wait(100); // 确保在同步块中调用wait
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // 处理中断异常
                        return; // 适当地处理中断
                    }
                }
            }
        }
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
        TimableOutput.println("OPEN-" + currentFloor + "-" + this.getPrintId());
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
        TimableOutput.println("CLOSE-" + currentFloor + "-" + this.getPrintId());
    }

    public void setDcReset(DoubleCarResetRequest dcReset) {
        this.dcReset = dcReset;
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

    public void setSpeed(double speed) { this.speed = speed; }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public ArrayList<Person> getInsideList() {
        return insideList;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getDirection() {
        return direction;
    }

    public Elevator getOther() {
        return other;
    }

    public void setOther(Elevator other) {
        this.other = other;
    }

    public void setTransFloor(int transFloor) {
        this.transFloor = transFloor;
    }

    public ElevatorState getElevatorState() {
        return state;
    }

    public String getPrintId() {
        int realId = Character.getNumericValue(this.id.charAt(0)) + 1;
        String result;
        if (this.id.length() == 1) {
            result = String.valueOf(realId);
        } else {
            String name = this.id.substring(1); // 获取第一个字符之后的子串
            result = realId + name;
        }
        return result;
    }

    public boolean canArrive(int floor) {
        if (floor >= minFloor && floor <= maxFloor) {
            return true;
        }
        return false;
    }

    public void setDouble(boolean isDouble) {
        this.isDouble = isDouble;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setMaxFloor(int maxFloor) {
        this.maxFloor = maxFloor;
    }

    public void setMinFloor(int minFloor) {
        this.minFloor = minFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }
}
