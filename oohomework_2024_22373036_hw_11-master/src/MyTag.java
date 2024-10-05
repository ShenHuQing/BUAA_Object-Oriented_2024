import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.HashMap;

public class MyTag implements Tag {
    private final int id;

    private int valueSum;

    private int valueSumDirty;
    private int ageSum;
    private int ageMean;
    private int ageMeanDirty;
    private int ageVar;

    private int ageVarDirty;
    private HashMap<Integer,Person> persons;

    public MyTag(int id) {
        this.id = id;
        this.persons = new HashMap<>();
        this.ageMean = 0;
        this.ageMeanDirty = 0;
        this.ageVar = 0;
        this.ageVarDirty = 0;
        this.ageSum = 0;
        this.valueSumDirty = 0;
        this.valueSum = 0;
    }

    public int getId() {
        return this.id;
    }

    public void addPerson(Person person) {
        if (!hasPerson(person)) {
            this.persons.put(person.getId(), person);
            ageMeanDirty = 1;
            ageVarDirty = 1;
            valueSumDirty = 1;
            ageSum += person.getAge();
        }
    }

    public boolean hasPerson(Person person) {
        return this.persons.containsKey(person.getId());
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Tag)) {
            return false;
        } else {
            return ((Tag) obj).getId() == id;
        }
    }

    public int getValueSum() {
        if (persons.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Person person1: persons.values()) {
            for (Person person2: persons.values()) {
                if (person1.isLinked(person2) && person2.isLinked(person1)) {
                    sum += person1.queryValue(person2);
                }
            }
        }
        this.valueSum = sum;
        this.valueSumDirty = 0;
        return sum;
    }

    @Override
    public int getAgeMean() {
        if (persons.isEmpty()) {
            return 0;
        }
        if (ageVarDirty == 0 && ageMeanDirty == 0) {
            return this.ageMean;
        }
        this.ageMean = ageSum / persons.size();
        return ageMean;
    }

    public int getAgeVar() {
        if (persons.isEmpty()) {
            return 0;
        }
        if (ageVarDirty == 0 && ageMeanDirty == 0) {
            return this.ageVar;
        } else {
            int mean = getAgeMean();
            int varianceSum = 0;
            for (Person person : persons.values()) {
                int deviation = person.getAge() - mean;
                varianceSum += deviation * deviation;
            }
            ageVarDirty = 0;
            this.ageVar =  varianceSum / persons.size();
            return ageVar;
        }
    }

    public void delPerson(Person person) {
        if (hasPerson(person)) {
            this.persons.remove(person.getId());
            ageMeanDirty = 1;
            ageVarDirty = 1;
            valueSumDirty = 1;
            ageSum -= person.getAge();
        }
    }

    public int getSize() {
        return persons.size();
    }

    public void setValueSum(int valueSum) {
        this.valueSum = valueSum;
    }

    public int getValueSumDirty() {
        return valueSumDirty;
    }

    public int getValueSumEasy() {
        return valueSum;
    }

    public void setValueSumDirty(int valueSumDirty) {
        this.valueSumDirty = valueSumDirty;
    }

    public HashMap<Integer, Person> getPersons() {
        return persons;
    }
}