import java.util.ArrayList;

public class Strategy {
    private final StrategyType strategyType;
    private ArrayList<Person> insideList = new ArrayList<>();
    private RequestQueue requestList = new RequestQueue();

    public Strategy(StrategyType strategyType,RequestQueue requestList) {
        this.strategyType = strategyType;
        this.requestList = requestList;
    }

    public StrategyType getAdviceType() {
        return strategyType;
    }

    public enum StrategyType {
        OPEN,MOVE, WAIT, OVER, REVERSE
    }

    public StrategyType getAdvice(Elevator.ElevatorState state,int curFloor, int curNum,
                                  int direction, ArrayList<Person> insideList,
                                  RequestQueue requestList) {
        if (canOpenForIn(curFloor,requestList,direction,curNum,insideList)
                || canOpenForOut(curFloor, insideList)) {
            return StrategyType. OPEN;
        }

        synchronized (requestList) {
            if (curNum != 0) {  // 如果电梯里有人
                return StrategyType.MOVE;
            } else {
                if (requestList.isEmpty()) {
                    return StrategyType.WAIT;
                } else {
                    if (hasReqInOriginDirection(curFloor, requestList, direction)) {
                        return StrategyType.MOVE;
                    } else {
                        return StrategyType.REVERSE;
                    }
                }
            }
        }
    }

    public boolean canOpenForOut(int curFloor, ArrayList<Person> insideList) {
        synchronized (insideList) {
            for (Person person : insideList) {
                if (person.getToFloor() == curFloor) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean canOpenForIn(int curFloor, RequestQueue requestList,
                                int direction, int curNum,ArrayList<Person> insideList) {
        synchronized (requestList) {
            if (insideList.isEmpty()) {
                for (Person person : requestList.getRequestQueue()) {
                    int toFloor = person.getToFloor();
                    int fromFloor = person.getFromFloor();
                    if (fromFloor == curFloor && curNum < 6) {
                        requestList.notifyAll();
                        return true;
                    }
                }
                requestList.notifyAll();
                return false;
            } else {
                for (Person person : requestList.getRequestQueue()) {
                    int toFloor = person.getToFloor();
                    int fromFloor = person.getFromFloor();
                    if (fromFloor == curFloor && curNum < 6 &&
                            (direction * (person.getToFloor() - person.getFromFloor()) > 0)) {
                        requestList.notifyAll();
                        return true;
                    }
                }
                requestList.notifyAll();
                return false;
            }
        }
    }

    public boolean hasReqInOriginDirection(int curFloor, RequestQueue requestList, int direction) {
        // 检查方向是否为正向
        boolean isPositiveDirection = direction > 0;
        if (!requestList.getRequestQueue().isEmpty()) {
            Person firstPerson = requestList.getRequestQueue().get(0);
            int reqFloor = firstPerson.getFromFloor();
            // 检查请求是否在当前楼层之前，并且方向与当前方向一致
            if ((isPositiveDirection && reqFloor >= curFloor)
                    || (!isPositiveDirection && reqFloor <= curFloor)) {
                return true;
            }
        }
        // 如果没有匹配的请求，则返回false
        return false;
    }

}
