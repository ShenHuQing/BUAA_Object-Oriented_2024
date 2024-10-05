import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.NoticeMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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

    private int money;

    private int socialValue;

    private Person bestAcquaintance;

    private ArrayList<Message> messages;

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
        this.money = 0;
        this.socialValue = 0;
        this.messages = new ArrayList<>();
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

    @Override
    public void addSocialValue(int num) {
        this.socialValue += num;
    }

    @Override
    public int getSocialValue() {
        return this.socialValue;
    }

    @Override
    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public List<Message> getReceivedMessages() {
        ArrayList<Message> receive = new ArrayList<>();
        if (messages.size() < 5) {
            return messages;
        } else {
            for (int i = 0; i < 5; i++) {
                receive.add(messages.get(i));
            }
        }
        return receive;
    }

    @Override
    public void addMoney(int num) {
        this.money += num;
    }

    @Override
    public int getMoney() {
        return this.money;
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

    public void setMessages(ArrayList<Message> newMessages) {
        this.messages = newMessages;
    }

    public void personClearNotive() {
        List<Message> messages = this.getMessages();
        ListIterator<Message> iterator = messages.listIterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message instanceof NoticeMessage) {
                iterator.remove();
            }
        }
        setMessages((ArrayList<Message>)messages);
    }

    public void refreshTag(Person person2) {
        Iterator<Tag> iterator1 = getTags().values().iterator();
        while (iterator1.hasNext()) {
            Tag tag1 = iterator1.next();
            if (tag1.hasPerson(person2)) {
                tag1.delPerson(person2);
            }
        }
    }
}
