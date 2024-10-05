import com.oocourse.elevator1.ElevatorInput;
import com.oocourse.elevator1.PersonRequest;

import java.io.IOException;

public class InputHandle extends Thread {
    private final RequestQueue requests;

    public InputHandle(RequestQueue requests) {
        this.requests = requests;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        while (true) {
            PersonRequest personRequest = elevatorInput.nextPersonRequest();
            if (personRequest == null) {
                try {
                    elevatorInput.close();
                    requests.setEnd(true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            } else {
                int personId = personRequest.getPersonId();
                int fromFloor = personRequest.getFromFloor();
                int toFloor = personRequest.getToFloor();
                int elevatorId = personRequest.getElevatorId();
                char fromBuilding = 'a';
                char toBuilding = 'a';
                Person person = new Person(personId,fromBuilding,
                        toBuilding, fromFloor, toFloor,elevatorId);
                requests.add(person);
            }
        }
    }

}
