import java.util.ArrayList;
import java.util.List;

public class ElevatorController extends Thread {
    private final ArrayList<Elevator> elevators;
    private ArrayList<RequestQueue> requests;
    private final RequestQueue requestInit;
    private int random = 0;

    public ElevatorController(int numberOfElevators,RequestQueue requestInit
            , ArrayList<RequestQueue> requests) {
        this.elevators = new ArrayList<>();
        for (int i = 0; i < numberOfElevators; i++) {
            elevators.add(new Elevator(i, Strategy.StrategyType.WAIT,requestInit));
        }
        this.requests = requests;
        this.requestInit = requestInit;
    }

    @Override
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
                if (requestInit.isEmpty() && requestInit.isEnd()) {
                    // 检查所有电梯是否完成了它们的请求
                    synchronized (requests) {
                        for (int i = 0; i < requests.size(); i++) {
                            requests.get(i).setEnd(true);
                        }
                        return;
                    }
                }
                requestInit.notifyAll();
            }

            Person person = requestInit.getOneRequestAndRemove();
            if (person == null) {
                continue;
            }
            synchronized (elevators) {
                Elevator selectedElevator = elevators.get(person.getElevatorId() - 1);
                selectedElevator.addRequest(person);
            }
        }
    }

    public List<Elevator> getElevators() {
        return elevators;
    }

}
