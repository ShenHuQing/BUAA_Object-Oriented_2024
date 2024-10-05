import com.oocourse.elevator3.DoubleCarResetRequest;
import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.NormalResetRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import java.io.IOException;
import java.util.ArrayList;

public class InputHandle extends Thread {
    private final RequestQueue personRequests;

    private final ElevatorController controller;

    private final ArrayList<Elevator> elevators;

    private ArrayList<RequestQueue> requests;

    public InputHandle(RequestQueue requests,ArrayList<Elevator> elevators,
                       ElevatorController controller,ArrayList<RequestQueue> allrequests) {
        this.personRequests = requests;
        this.elevators = elevators;
        this.controller = controller;
        this.requests = allrequests;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                try {
                    elevatorInput.close();
                    personRequests.setEnd(true);
                    controller.setEnd(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else {
                if (request instanceof PersonRequest) {
                    PersonRequest personRequest = (PersonRequest) request;
                    int personId = personRequest.getPersonId();
                    int fromFloor = personRequest.getFromFloor();
                    int toFloor = personRequest.getToFloor();
                    Person person = new Person(personId, fromFloor, toFloor);
                    personRequests.add(person);
                    CalculateNum.addNum();
                } else if (request instanceof NormalResetRequest) {
                    NormalResetRequest resetRequest = (NormalResetRequest) request;
                    int elevatorId = resetRequest.getElevatorId();
                    int capacity = resetRequest.getCapacity();
                    double speed = resetRequest.getSpeed();
                    Elevator elevator = elevators.get(elevatorId - 1);
                    synchronized (elevator.getRequestList()) {
                        elevator.setState(Elevator.ElevatorState.NORMALRESET);
                        elevator.setFullCapacity(capacity);
                        elevator.setSpeed(speed);
                        elevator.getRequestList().notifyAll();
                    }
                } else {
                    DoubleCarResetRequest resetRequest = (DoubleCarResetRequest) request;
                    int elevatorId = resetRequest.getElevatorId();
                    int capacity = resetRequest.getCapacity();
                    double speed = resetRequest.getSpeed();
                    int transformFloor = resetRequest.getTransferFloor();
                    Elevator elevator = elevators.get(elevatorId - 1);
                    synchronized (elevator.getRequestList()) {
                        elevator.setState(Elevator.ElevatorState.DCRESET);
                        elevator.setDcReset(resetRequest);
                        elevator.getRequestList().notifyAll();
                    }
                }
            }
        }
    }

}
