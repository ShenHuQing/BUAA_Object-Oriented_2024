import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.exceptions.RelationNotFoundException;
import com.oocourse.spec1.main.Network;
import com.oocourse.spec1.main.Person;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MyNetwork implements Network {
    private  HashMap<Integer, Person> people = new HashMap<>();
    private int tripleSum;
    private int blockSum;
    private DisjointSetUnion disjointSetUnion;

    public MyNetwork() {
        this.tripleSum = 0;
        this.blockSum = 0;
        this.disjointSetUnion = new DisjointSetUnion(this);
    }

    public boolean contains(int id) {
        return people.containsKey(id);
    }

    @Override
    public boolean containsPerson(int id) {
        return people.containsKey(id);
    }

    @Override
    public Person getPerson(int id) {
        return people.getOrDefault(id, null);
    }

    @Override
    public void addPerson(Person person) throws EqualPersonIdException {
        if (!this.contains(person.getId())) {
            people.put(person.getId(), person);
            disjointSetUnion.add(person.getId());
            blockSum++;
        } else {
            throw new MyEqualPersonIdException(person.getId());
        }
    }

    public void addRelation(int id1, int id2, int value)
            throws PersonIdNotFoundException, MyEqualRelationException {
        if (containsPerson(id1) && containsPerson(id2)) {
            if (!getPerson(id1).isLinked(getPerson(id2))) {
                int nextBlockSum = disjointSetUnion.merge(id1, id2,blockSum);
                blockSum = nextBlockSum;
                MyPerson person1 = (MyPerson) getPerson(id1);
                MyPerson person2 = (MyPerson) getPerson(id2);
                Set<Integer> countedTriplets = new HashSet<>();
                if (person2.getAcquaintance().size() > person1.getAcquaintance().size()) {
                    updateTripleSum(person2, person1, countedTriplets);
                } else {
                    updateTripleSum(person1, person2, countedTriplets);
                }
                person1.addAcquaintance(person2, value);
                person2.addAcquaintance(person1, value);
            } else {
                if (id1 == id2) {
                    throw new MyEqualRelationException(id1,id2);
                }
                else if (getPerson(id1).isLinked(getPerson(id2))) {
                    throw new MyEqualRelationException(id1, id2);
                }
            }
        } else {
            if (!contains(id1)) {
                throw new MyPersonIdNotFoundException(id1);
            }
            else if (!contains(id2)) {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    private void updateTripleSum(MyPerson primary,
                                 MyPerson secondary, Set<Integer> countedTriplets) {
        for (Person acquaintance : primary.getAcquaintance().values()) {
            if (acquaintance.equals(primary) || acquaintance.equals(secondary)) {
                continue;
            }
            if (secondary.isLinked(acquaintance)) {
                int tripletId = getTripletId(primary.getId()
                        , secondary.getId(), acquaintance.getId());
                if (countedTriplets.add(tripletId)) {
                    tripleSum++;
                }
            }
        }
    }

    private int getTripletId(int id1, int id2, int id3) {
        int[] ids = {id1, id2, id3};
        Arrays.sort(ids);
        return Arrays.hashCode(ids);
    }

    public void modifyRelation(int id1, int id2, int value) throws PersonIdNotFoundException,
            EqualPersonIdException, RelationNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) &&
                getPerson(id1).isLinked(getPerson(id2)) && id1 != id2) {
            MyPerson person1 = (MyPerson) getPerson(id1);
            MyPerson person2 = (MyPerson) getPerson(id2);
            int currentValue = queryValue(id1, id2);
            int nextValue = currentValue + value;
            if (nextValue > 0) {
                // 更新关系值
                person1.addAcquaintance(person2, nextValue);
                person2.addAcquaintance(person1, nextValue);
            } else {
                // 删除关系前，减少相关三角关系计数
                Set<Integer> countedTriplets = new HashSet<>();
                if (person2.getAcquaintance().size() > person1.getAcquaintance().size()) {
                    reduceTripleSum(person2, person1, countedTriplets);
                } else {
                    reduceTripleSum(person1, person2, countedTriplets);
                }
                // 删除关系
                person1.removeAcquaintance(person2);
                person2.removeAcquaintance(person1);
                int nextBlockSum = disjointSetUnion.deleteRelation(id1, id2,blockSum);
                blockSum = nextBlockSum;
            }
        } else {
            if (!contains(id1)) {
                throw new MyPersonIdNotFoundException(id1);
            }
            else if (!contains(id2)) {
                throw new MyPersonIdNotFoundException(id2);
            }
            else if (id1 == id2) {
                throw new MyEqualPersonIdException(id1);
            }
            else if (!getPerson(id1).isLinked(getPerson(id2))) {
                throw new MyRelationNotFoundException(id1, id2);
            }
        }
    }

    private void reduceTripleSum(MyPerson primary,
                                 MyPerson secondary, Set<Integer> countedTriplets) {
        for (Person acquaintance : primary.getAcquaintance().values()) {
            if (acquaintance.equals(primary) || acquaintance.equals(secondary)) {
                continue;
            }
            if (secondary.isLinked(acquaintance)) {
                int tripletId = getTripletId(primary.getId(),
                        secondary.getId(), acquaintance.getId());
                if (countedTriplets.add(tripletId)) {
                    tripleSum--;
                }
            }
        }
    }

    @Override
    public int queryValue(int id1, int id2) throws
            PersonIdNotFoundException, MyRelationNotFoundException {
        if (contains(id1) && contains(id2)) {
            if (getPerson(id1).isLinked(getPerson(id2))) {
                return getPerson(id1).queryValue(getPerson(id2));
            } else {
                throw new MyRelationNotFoundException(id1, id2);
            }
        } else {
            if (!contains(id1)) {
                throw new MyPersonIdNotFoundException(id1);
            } else {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    @Override
    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (contains(id1) && contains(id2)) {
            if (disjointSetUnion.isConnected(id1,id2)) {
                return true;
            } return false;
        } else {
            if (!contains(id1)) {
                throw new MyPersonIdNotFoundException(id1);
            } else {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    @Override
    public int queryBlockSum() {
        return blockSum;
    }

    @Override
    public int queryTripleSum() {
        return tripleSum;
    }

    public HashMap<Integer, Person> getPeople() {
        return people;
    }

    public Person[] getPersons() { return null; }
}

