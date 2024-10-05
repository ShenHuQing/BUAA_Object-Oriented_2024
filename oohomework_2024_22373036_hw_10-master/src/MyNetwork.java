import com.oocourse.spec2.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec2.exceptions.EqualPersonIdException;
import com.oocourse.spec2.exceptions.EqualTagIdException;
import com.oocourse.spec2.exceptions.PathNotFoundException;
import com.oocourse.spec2.exceptions.PersonIdNotFoundException;
import com.oocourse.spec2.exceptions.RelationNotFoundException;
import com.oocourse.spec2.exceptions.TagIdNotFoundException;
import com.oocourse.spec2.main.Network;
import com.oocourse.spec2.main.Person;
import com.oocourse.spec2.main.Tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
                person1.removeAcquaintance(person2);
                person2.removeAcquaintance(person1);
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
                //删除tag
                Iterator<Tag> iterator1 = person1.getTags().values().iterator();
                while (iterator1.hasNext()) {
                    Tag tag1 = iterator1.next();
                    if (tag1.hasPerson(person2)) {
                        tag1.delPerson(person2);
                    }
                }

                Iterator<Tag> iterator2 = person2.getTags().values().iterator();
                while (iterator2.hasNext()) {
                    Tag tag2 = iterator2.next();
                    if (tag2.hasPerson(person1)) {
                        tag2.delPerson(person1);
                    }
                }
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

    @Override
    public void addTag(int personId, Tag tag)
            throws PersonIdNotFoundException, EqualTagIdException {
        if (contains(personId)) {
            Person person = getPerson(personId);
            if (!person.containsTag(tag.getId())) {
                person.addTag(tag);
            } else {
                throw new MyEqualTagIdException(tag.getId());
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    @Override
    public void addPersonToTag(int id1, int id2, int tagId)
            throws PersonIdNotFoundException, RelationNotFoundException,
            TagIdNotFoundException, EqualPersonIdException {
        if (containsPerson(id1) && containsPerson(id2) &&
                getPerson(id1).isLinked(getPerson(id2)) && id1 != id2) {
            Person person1 = getPerson(id1);
            Person person2 = getPerson(id2);
            if (person2.containsTag(tagId) &&
                    !person2.getTag(tagId).hasPerson(getPerson(id1))
                    && getPerson(id2).getTag(tagId).getSize() <= 1111) {
                person2.getTag(tagId).addPerson(person1);
                MyTag tag = (MyTag) person2.getTag(tagId);
                tag.setValueSumDirty(1);
            } else if (!person2.containsTag(tagId)) {
                throw new MyTagIdNotFoundException(tagId);
            } else if (person2.getTag(tagId).hasPerson(getPerson(id1))) {
                throw new MyEqualPersonIdException(id1);
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

    @Override
    public int queryTagValueSum(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (contains(personId)) {
            Person person = getPerson(personId);
            if (person.containsTag(tagId)) {
                MyTag tag = (MyTag) person.getTag(tagId);
                if (tag.getValueSumDirty() == 0) {
                    return tag.getValueSumEasy();
                }
                return person.getTag(tagId).getValueSum();
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public int queryTagAgeVar(int personId, int tagId) throws PersonIdNotFoundException,
            TagIdNotFoundException {
        if (contains(personId)) {
            Person person = getPerson(personId);
            if (person.containsTag(tagId)) {
                return person.getTag(tagId).getAgeVar();
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public void delPersonFromTag(int id1, int id2, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (containsPerson(id1) && containsPerson(id2) &&
                getPerson(id2).containsTag(tagId)) {
            Person person1 = getPerson(id1);
            Person person2 = getPerson(id2);
            if (person2.getTag(tagId).hasPerson(person1)) {
                person2.getTag(tagId).delPerson(person1);
                MyTag tag = (MyTag) person2.getTag(tagId);
                tag.setValueSumDirty(1);
            } else if (!person2.getTag(tagId).hasPerson(person1)) {
                throw new MyPersonIdNotFoundException(id1);
            }
        } else {
            if (!contains(id1)) {
                throw new MyPersonIdNotFoundException(id1);
            }
            else if (!contains(id2)) {
                throw new MyPersonIdNotFoundException(id2);
            }
            else if (!getPerson(id2).containsTag(tagId)) {
                throw new MyTagIdNotFoundException(tagId);
            }
        }
    }

    public void delTag(int personId, int tagId)
            throws PersonIdNotFoundException, TagIdNotFoundException {
        if (contains(personId)) {
            Person person = getPerson(personId);
            if (person.containsTag(tagId)) {
                person.delTag(tagId);
            } else {
                throw new MyTagIdNotFoundException(tagId);
            }
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new MyPersonIdNotFoundException(id);
        }
        if (((MyPerson) getPerson(id)).getAcquaintance().isEmpty()) {
            throw new MyAcquaintanceNotFoundException(id);
        }
        return ((MyPerson)(people.get(id))).getBestAcquaintance().getId();
    }

    public int queryCoupleSum() {
        int sum = 0;
        for (Person person1 : people.values()) {
            if (!((MyPerson) person1).getAcquaintance().isEmpty()) {
                Person best = ((MyPerson) person1).getBestAcquaintance();
                if (((MyPerson)best).getBestAcquaintance() == person1) {
                    sum++;
                }
            }
        }
        return sum / 2;
    }

    public HashMap<Integer, Person> getPeople() {
        return people;
    }

    public Person[] getPersons() { return null; }

    public int queryShortestPath(int source, int target)
            throws PersonIdNotFoundException, PathNotFoundException {
        if (!containsPerson(source)) {
            throw new MyPersonIdNotFoundException(source);
        }
        if (!containsPerson(target)) {
            throw new MyPersonIdNotFoundException(target);
        }
        if (!disjointSetUnion.isConnected(source,target)) {
            throw new MyPathNotFoundException(source, target);
        }
        if (source == target) {
            return 0;
        }
        if (getPerson(source).queryValue(getPerson(target)) > 0) {
            return 0;
        }
        Queue<Integer> queueSource = new LinkedList<>();
        Queue<Integer> queueTarget = new LinkedList<>();
        Map<Integer, Integer> visitedSource = new HashMap<>();
        queueSource.offer(source);
        visitedSource.put(source, 0);
        queueTarget.offer(target);
        Map<Integer, Integer> visitedTarget = new HashMap<>();
        visitedTarget.put(target, 0);
        while (!queueSource.isEmpty() && !queueTarget.isEmpty()) {
            int distance;
            if (queueSource.size() <= queueTarget.size()) {
                distance = visitLevel(queueSource, visitedSource, visitedTarget);
            } else {
                distance = visitLevel(queueTarget, visitedTarget, visitedSource);
            }
            if (distance != -1) {
                return distance;
            }
        }
        return -1;
    }

    private int visitLevel(Queue<Integer> queue, Map<Integer, Integer> visited,
                           Map<Integer, Integer> otherVisited) {
        int currentSize = queue.size();
        for (int i = 0; i < currentSize; i++) {
            int currentNode = queue.poll();
            int currentDistance = visited.get(currentNode);
            MyPerson person = (MyPerson) getPeople().get(currentNode);
            for (Person acquaintance : person.getAcquaintance().values()) {
                Integer neighbor = acquaintance.getId();
                if (!visited.containsKey(neighbor)) {
                    visited.put(neighbor, currentDistance + 1);
                    queue.offer(neighbor);
                    if (otherVisited.containsKey(neighbor)) {
                        return currentDistance + otherVisited.get(neighbor);
                    }
                }
            }
        }
        return -1; // This level does not meet
    }

}


