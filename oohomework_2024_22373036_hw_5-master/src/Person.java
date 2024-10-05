public class Person {
    private final int id;
    private final char fromBuilding;
    private final char toBuilding;
    private final int fromFloor;
    private final int toFloor;
    private final int elevatorId;

    public Person(int id, char fromBuilding, char toBuilding,
                  int fromFloor, int toFloor, int elevatorId) {
        this.id = id;
        this.fromBuilding = fromBuilding;
        this.toBuilding = toBuilding;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.elevatorId = elevatorId;
    }

    public int getId() {
        return id;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getElevatorId() {
        return elevatorId;
    }

    @Override
    public String toString() {
        return this.id + "+" + this.fromFloor + "+" + this.toFloor + "+" + this.elevatorId;
    }
}