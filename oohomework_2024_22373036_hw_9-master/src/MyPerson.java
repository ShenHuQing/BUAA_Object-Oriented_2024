import com.oocourse.spec1.main.Person;
import java.util.HashMap;

public class MyPerson implements Person {
    private final int id;
    private final String name;
    private final int age;

    private  HashMap<Integer, Person> acquaintance;
    private  HashMap<Integer, Integer> value;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.acquaintance = new HashMap<>();
        this.value = new HashMap<>();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Person) {
            return ((Person) obj).getId() == id;
        }
        return false;
    }

    public boolean isLinked(Person person) {
        return acquaintance.containsKey(person.getId()) || person.getId() == id;
    }

    public int queryValue(Person person) {
        Integer val = value.get(person.getId());
        return val != null ? val : 0;
    }

    public void addAcquaintance(Person person, int otherValue) {
        acquaintance.put(person.getId(), person);
        value.put(person.getId(), otherValue);
    }

    public void removeAcquaintance(Person person) {
        acquaintance.remove(person.getId());
        value.remove(person.getId());
    }

    public  HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public boolean strictEquals(Person person) { return true; }
}
