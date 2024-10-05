import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MyPerson implements Person {
    private final int id;
    private final String name;
    private final int age;

    private int bestAcquaintanceDirty;

    private  HashMap<Integer, Person> acquaintance;
    private  HashMap<Integer, Integer> value;

    private HashMap<Integer,Tag> tags;

    private final TreeMap<Integer, Set<Person>> acquaintancesByValue;

    private Person bestAcquaintance;

    public MyPerson(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.acquaintance = new HashMap<>();
        this.value = new HashMap<>();
        this.tags = new HashMap<>();
        this.bestAcquaintance = null;
        this.acquaintancesByValue = new TreeMap<>(Collections.reverseOrder());
        this.bestAcquaintanceDirty = 0;
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

    public boolean containsTag(int id) {
        return this.tags.containsKey(id);
    }

    public Tag getTag(int id) {
        return this.tags.get(id);
    }

    public void addTag(Tag tag) {
        if (!this.tags.containsKey(tag.getId())) {
            this.tags.put(tag.getId(),tag);
        }
    }

    public void delTag(int id) {
        if (this.tags.containsKey(id)) {
            this.tags.remove(id);
        }
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
        acquaintancesByValue.putIfAbsent(otherValue,
                new TreeSet<>(Comparator.comparingInt(Person::getId)));
        acquaintancesByValue.get(otherValue).add(person);
        this.bestAcquaintanceDirty = 1;
    }

    public void removeAcquaintance(Person person) {
        int otherValue = queryValue(person);
        acquaintance.remove(person.getId());
        value.remove(person.getId());
        if (acquaintancesByValue.containsKey(otherValue)) {
            acquaintancesByValue.get(otherValue).remove(person);
            // 如果删除后该value对应的set为空，则从acquaintancesByValue中移除该键值对
            if (acquaintancesByValue.get(otherValue).isEmpty()) {
                acquaintancesByValue.remove(otherValue);
            }
        }
        this.bestAcquaintanceDirty = 1;
    }

    public  HashMap<Integer, Person> getAcquaintance() {
        return acquaintance;
    }

    public HashMap<Integer, Integer> getValue() {
        return value;
    }

    public Person getBestAcquaintance() {
        if (acquaintancesByValue.isEmpty()) {
            return null; // 如果没有任何关系，返回 null
        }
        if (this.bestAcquaintanceDirty == 0) {
            return this.bestAcquaintance;
        }
        else {
            this.bestAcquaintance = acquaintancesByValue.firstEntry().getValue().iterator().next();
            // 返回值最大的第一个人
            return bestAcquaintance;
        }
    }

    public void setBestAcquaintance(Person bestAcquaintance) {
        this.bestAcquaintance = bestAcquaintance;
    }

    public boolean strictEquals(Person person) { return true; }

    public HashMap<Integer, Tag> getTags() {
        return tags;
    }

    public void setTags(HashMap<Integer, Tag> tags) {
        this.tags = tags;
    }
}

