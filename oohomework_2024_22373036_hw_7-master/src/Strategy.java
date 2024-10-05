import java.util.ArrayList;

public class Strategy {
    private final StrategyType strategyType;

    public Strategy(StrategyType strategyType) {
        this.strategyType = strategyType;
    }

    public enum StrategyType {
        OPEN,MOVE, WAIT, OVER, REVERSE, RESET
    }

    public StrategyType getAdvice(Elevator.ElevatorState state,int curFloor, int curNum,
                                  ArrayList<Person> insideList,
                                  RequestQueue requestList,int fullCap,
                                  int transFloor,Elevator elevator) {
        if (state == Elevator.ElevatorState.NORMALRESET
                || state == Elevator.ElevatorState.DCRESET) {
            return StrategyType.RESET;
        }
        if (canOpenForIn(curFloor,requestList,elevator.getDirection(),curNum,insideList,fullCap)
                || canOpenForOut(curFloor, insideList,transFloor,elevator)) {
            return StrategyType. OPEN;
        }
        synchronized (requestList) {
            if (curNum != 0) {  // 如果电梯里有人
                return StrategyType.MOVE;
            } else {
                if (requestList.isEmpty()) {
                    return StrategyType.WAIT;
                } else {
                    if (hasReqInOriginDirection(curFloor, requestList,elevator.getDirection())) {
                        return StrategyType.MOVE;
                    } else {
                        return StrategyType.REVERSE;
                    }
                }
            }
        }
    }

    public boolean canOpenForOut(int curFloor, ArrayList<Person> insideList,
                                 int transFloor,Elevator elevator) {
        synchronized (insideList) {
            for (Person person : insideList) {
                if (person.getToFloor() == curFloor) {
                    return true;
                }
                if (curFloor == transFloor && !elevator.canArrive(person.getToFloor()))  {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean canOpenForIn(int curFloor, RequestQueue requestList,
                                int direction, int curNum,ArrayList<Person>
                                        insideList,int fullCap) {
        synchronized (requestList) {
            if (insideList.isEmpty()) {
                for (Person person : requestList.getRequestQueue()) {
                    int toFloor = person.getToFloor();
                    int fromFloor = person.getFromFloor();
                    if (fromFloor == curFloor && curNum < fullCap) {
                        requestList.notifyAll();
                        return true;
                    }
                }
                requestList.notifyAll();
                return false;
            } else {
                for (Person person : requestList.getRequestQueue()) {
                    int fromFloor = person.getFromFloor();
                    if (fromFloor == curFloor && curNum < fullCap &&
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
        synchronized (requestList) {
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

}
