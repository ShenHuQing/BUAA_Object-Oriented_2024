import com.oocourse.spec3.exceptions.AcquaintanceNotFoundException;
import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;
import com.oocourse.spec3.exceptions.EqualEmojiIdException;
import com.oocourse.spec3.exceptions.EqualMessageIdException;
import com.oocourse.spec3.exceptions.EqualPersonIdException;
import com.oocourse.spec3.exceptions.EqualTagIdException;
import com.oocourse.spec3.exceptions.MessageIdNotFoundException;
import com.oocourse.spec3.exceptions.PathNotFoundException;
import com.oocourse.spec3.exceptions.PersonIdNotFoundException;
import com.oocourse.spec3.exceptions.RelationNotFoundException;
import com.oocourse.spec3.exceptions.TagIdNotFoundException;
import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Network;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyNetwork implements Network {
    private  HashMap<Integer, Person> people = new HashMap<>();
    private int tripleSum;
    private int blockSum;
    private DisjointSetUnion disjointSetUnion;
    private HashMap<Integer,Message> messages;
    private static HashMap<Integer,Integer> emojiHeatList;

    public MyNetwork() {
        this.tripleSum = 0;
        this.blockSum = 0;
        this.disjointSetUnion = new DisjointSetUnion(this);
        this.messages = new HashMap<>();
        this.emojiHeatList = new HashMap<>();
    }

    public boolean contains(int id) {
        return people.containsKey(id);
    }

    public boolean containsPerson(int id) {
        return people.containsKey(id);
    }

    public Person getPerson(int id) {
        return people.getOrDefault(id, null);
    }

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
        if (!containsPerson(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } if (!containsPerson(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } if (getPerson(id1).isLinked(getPerson(id2)) || id1 == id2) {
            throw new MyEqualRelationException(id1, id2);
        }
        blockSum = disjointSetUnion.merge(id1, id2,blockSum);
        MyPerson person1 = (MyPerson) getPerson(id1);
        MyPerson person2 = (MyPerson) getPerson(id2);
        Set<Integer> countedTriplets = new HashSet<>();
        if (person2.getAcquaintance().size() > person1.getAcquaintance().size()) {
            updateTripleSum(person2, person1, countedTriplets);
        } else {
            updateTripleSum(person1, person2, countedTriplets);
        } person1.addAcquaintance(person2, value);
        person2.addAcquaintance(person1, value);
    }

    private void updateTripleSum(MyPerson primary,
                                 MyPerson secondary, Set<Integer> countedTriplets) {
        for (Person acquaintance : primary.getAcquaintance().values()) {
            if (acquaintance.equals(primary) || acquaintance.equals(secondary)) {
                continue;
            } if (secondary.isLinked(acquaintance)) {
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
                person1.removeAcquaintance(person2);
                person2.removeAcquaintance(person1);
                person1.addAcquaintance(person2, nextValue);
                person2.addAcquaintance(person1, nextValue);
            } else {
                Set<Integer> countedTriplets = new HashSet<>();
                if (person2.getAcquaintance().size() > person1.getAcquaintance().size()) {
                    reduceTripleSum(person2, person1, countedTriplets);
                } else {
                    reduceTripleSum(person1, person2, countedTriplets);
                } person1.removeAcquaintance(person2);
                person2.removeAcquaintance(person1);
                blockSum = disjointSetUnion.deleteRelation(id1, id2,blockSum);
                person1.refreshTag(person2);
                person2.refreshTag(person1);
            }
        } else {
            dealError(id1, id2);
        }
    }

    private void dealError(int id1, int id2) throws MyPersonIdNotFoundException,
            MyEqualPersonIdException, MyRelationNotFoundException {
        if (!contains(id1)) {
            throw new MyPersonIdNotFoundException(id1);
        } else if (!contains(id2)) {
            throw new MyPersonIdNotFoundException(id2);
        } else if (id1 == id2) {
            throw new MyEqualPersonIdException(id1);
        } else if (!getPerson(id1).isLinked(getPerson(id2))) {
            throw new MyRelationNotFoundException(id1, id2);
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

    public boolean isCircle(int id1, int id2) throws PersonIdNotFoundException {
        if (contains(id1) && contains(id2)) {
            return disjointSetUnion.isConnected(id1, id2);
        } else {
            if (!contains(id1)) {
                throw new MyPersonIdNotFoundException(id1);
            } else {
                throw new MyPersonIdNotFoundException(id2);
            }
        }
    }

    public int queryBlockSum() {
        return blockSum;
    }

    public int queryTripleSum() {
        return tripleSum;
    }

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
            dealError(id1, id2);
        }
    }

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
            } else if (!contains(id2)) {
                throw new MyPersonIdNotFoundException(id2);
            } else if (!getPerson(id2).containsTag(tagId)) {
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

    public boolean containsMessage(int id) {
        return messages.containsKey(id);
    }

    public void addMessage(Message message) throws EqualMessageIdException,
            EmojiIdNotFoundException, EqualPersonIdException {
        if (containsMessage(message.getId())) {
            throw new MyEqualMessageIdException(message.getId());
        } if ((message instanceof EmojiMessage) &&
                !containsEmojiId(((EmojiMessage) message).getEmojiId())) {
            throw new MyEmojiIdNotFoundException(((EmojiMessage) message).getEmojiId());
        } if (message.getType() == 0 && message.getPerson1().equals(message.getPerson2())) {
            throw new MyEqualPersonIdException(message.getPerson1().getId());
        }
        messages.put(message.getId(), message);
    }

    public Message getMessage(int id) {
        return messages.getOrDefault(id, null);
    }

    public void sendMessage(int id) throws RelationNotFoundException, MessageIdNotFoundException,
            TagIdNotFoundException {
        if (containsMessage(id)) {
            Message message = getMessage(id);
            if (message.getType() == 0 && message.getPerson1().isLinked(message.getPerson2()) &&
                    !message.getPerson1().equals(message.getPerson2())) {
                ((MyMessage)message).sendMessageForOne();
            } else if (message.getType() == 1 && message.getPerson1().
                    containsTag(message.getTag().getId())) {
                ((MyMessage)message).sendMessageForTag();
            } else {
                if (containsMessage(id) && message.getType() == 0 &&
                        !message.getPerson1().isLinked(message.getPerson2())) {
                    throw new MyRelationNotFoundException(message.getPerson1().getId(),
                            message.getPerson2().getId());
                } if (containsMessage(id) && message.getType() == 1 &&
                        !getMessage(id).getPerson1().containsTag(getMessage(id).getTag().getId())) {
                    throw new MyTagIdNotFoundException(getMessage(id).getTag().getId());
                }
            }
            messages.remove(id);
        } else if (!containsMessage(id)) {
            throw new MyMessageIdNotFoundException(id);
        }
    }

    public int querySocialValue(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getSocialValue();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    public List<Message> queryReceivedMessages(int id) throws PersonIdNotFoundException {
        if (containsPerson(id)) {
            return getPerson(id).getReceivedMessages();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    public boolean containsEmojiId(int id) {
        return emojiHeatList.containsKey(id);
    }

    public void storeEmojiId(int id) throws EqualEmojiIdException {
        if (!containsEmojiId(id)) {
            emojiHeatList.put(id,0);
        } else {
            throw new MyEqualEmojiIdException(id);
        }
    }

    public int queryMoney(int id) throws PersonIdNotFoundException {
        if (contains(id)) {
            return getPerson(id).getMoney();
        } else {
            throw new MyPersonIdNotFoundException(id);
        }
    }

    public int queryPopularity(int id) throws EmojiIdNotFoundException {
        if (containsEmojiId(id)) {
            return emojiHeatList.get(id);
        } else {
            throw new MyEmojiIdNotFoundException(id);
        }
    }

    public int deleteColdEmoji(int limit) {
        emojiHeatList.entrySet().removeIf(entry -> entry.getValue() < limit);
        Iterator<Map.Entry<Integer, Message>> messageIterator = messages.entrySet().iterator();
        while (messageIterator.hasNext()) {
            Map.Entry<Integer, Message> entry = messageIterator.next();
            Message message = entry.getValue();
            if (message instanceof EmojiMessage) {
                EmojiMessage emojiMessage = (EmojiMessage) message;
                if (!containsEmojiId(emojiMessage.getEmojiId())) {
                    messageIterator.remove();
                }
            }
        }
        return emojiHeatList.size();
    }

    public void clearNotices(int personId) throws PersonIdNotFoundException {
        if (contains(personId)) {
            ((MyPerson)getPerson(personId)).personClearNotive();
        } else {
            throw new MyPersonIdNotFoundException(personId);
        }
    }

    public int queryBestAcquaintance(int id)
            throws PersonIdNotFoundException, AcquaintanceNotFoundException {
        if (!containsPerson(id)) {
            throw new MyPersonIdNotFoundException(id);
        } else if (((MyPerson) getPerson(id)).getAcquaintance().isEmpty()) {
            throw new MyAcquaintanceNotFoundException(id);
        } return ((MyPerson)(people.get(id))).getBestAcquaintance().getId();
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
        } else if (!containsPerson(target)) {
            throw new MyPersonIdNotFoundException(target);
        } else if (!disjointSetUnion.isConnected(source,target)) {
            throw new MyPathNotFoundException(source, target);
        }
        if (source == target) {
            return 0;
        } else if (getPerson(source).queryValue(getPerson(target)) > 0) {
            return 0;
        } return disjointSetUnion.shortPath(source,target,people);
    }

    public static HashMap<Integer, Integer> MygetEmojiHeatList() {
        return emojiHeatList;
    }

    public Message[] getMessages() { return null; }

    public int[] getEmojiIdList() { return null; }

    public int[] getEmojiHeatList() { return null; }
}



