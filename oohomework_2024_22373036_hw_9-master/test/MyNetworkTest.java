import com.oocourse.spec1.exceptions.EqualPersonIdException;
import com.oocourse.spec1.exceptions.EqualRelationException;
import com.oocourse.spec1.exceptions.PersonIdNotFoundException;
import com.oocourse.spec1.main.Person;
import javafx.util.Pair;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import java.util.*;


public class MyNetworkTest {
    private MyNetwork network;
    private MyNetwork replicaNetwork;
    @Before
    public void setup() {
        network = new MyNetwork();
        replicaNetwork = new MyNetwork();
        for (int i = 0; i < 21; i++) {
            MyPerson person = new MyPerson(i, "a", i);
            MyPerson person1 = new MyPerson(i, "a", i);
            try {
                network.addPerson(person);
                replicaNetwork.addPerson(person1);
            } catch (EqualPersonIdException e) {
                System.out.println("Duplicate person ID: " + person.getId() + ". Ignoring this person.");
            }
        }
    }

    @After
    public void clear()
    {
        network = null;
        replicaNetwork = null;
    }

    @Test
    public void testQueryTripleSum() {
        Random random = new Random();
        for (int j = 0; j < 100; j++) {
            setup();
            Set<Pair<Integer, Integer>> addedRelationships = new HashSet<>();
            for (int i = 0; i < 100; i++) {
                int id1 = random.nextInt(20);
                int id2 = random.nextInt(20);
                if (id1 == id2 || addedRelationships.contains(new Pair<>(id1, id2))) {
                    continue;
                }
                addedRelationships.add(new Pair<>(id1, id2));
                int value = random.nextInt(100);
                testAddRelationships(id1, id2, value);
                boolean equalMaps = MapEqual();
                assertTrue(equalMaps);
                int expected = network.queryTripleSum();
                int actual = actualQueryTripleCount();
                assertEquals(expected, actual);
            }
        }
    }

    private boolean MapEqual() {
        Person[] persons1 = network.getPersons();
        Person[] persons2 = replicaNetwork.getPersons();

        // 检查数组长度是否相等
        if (persons1.length != persons2.length) {
            return false;
        }

        // 遍历数组元素，比较对应位置的对象引用是否相等
        for (int i = 0; i < persons1.length; i++) {
            assertTrue(((MyPerson) persons1[i]).strictEquals(persons2[i]));
            if (!((MyPerson) persons1[i]).strictEquals(persons2[i])) {
                return false;
            }
        }
        return true;
    }

    public void testAddRelationships(int id1, int id2, int value) {
        try {
            network.addRelation(id1, id2, value);
            replicaNetwork.addRelation(id1, id2, value);
        } catch (PersonIdNotFoundException e) {
            System.out.println("Person ID not found: " + e.getMessage());
        } catch (EqualRelationException e) {
            System.out.println("Equal relation already exists: " + e.getMessage());
        }
    }

    public int actualQueryTripleCount() {
        // 查询三元组总数
        int count = 0;
        Person[] peopleArray = replicaNetwork.getPersons();

        // 遍历人员数组
        for (int i = 0; i < peopleArray.length; i++) {
            for (int j = i + 1; j < peopleArray.length; j++) {
                for (int k = j + 1; k < peopleArray.length; k++) {
                    // 检查是否存在关系链接这三个人员，如果存在则增加计数器
                    if (peopleArray[i].isLinked(peopleArray[j])
                            && peopleArray[j].isLinked(peopleArray[k])
                            && peopleArray[k].isLinked(peopleArray[i])) {
                        count++;
                    }
                }
            }
        }
        return count;
    }



}
