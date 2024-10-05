import com.oocourse.elevator1.TimableOutput;

import java.util.ArrayList;

public class MainClass {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();
        ArrayList<RequestQueue> allrequest = new ArrayList<>(); //由每个电梯的任务构成的总任务
        RequestQueue requestsInit = new RequestQueue();//用于读任务
        InputHandle inputThread = new InputHandle(requestsInit);
        inputThread.start();
        ElevatorController controller = new ElevatorController(6,requestsInit,allrequest);
        controller.start();
        for (Elevator elevator : controller.getElevators()) {
            allrequest.add(elevator.getRequestList());
            elevator.start();
            // 将当前电梯的请求队列添加到总任务列表中
        }

    }

}
