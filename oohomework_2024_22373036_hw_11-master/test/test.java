import com.oocourse.spec3.exceptions.*;
import com.oocourse.spec3.main.*;
import javafx.util.Pair;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;


public class test {
    private MyNetwork network;

    private Set<Pair<Integer, Integer>> addedRelationships;

    @Before
    public void setup() throws RelationNotFoundException, TagIdNotFoundException, EmojiIdNotFoundException, EqualPersonIdException, MessageIdNotFoundException, EqualMessageIdException {
        Random random = new Random();
        network = new MyNetwork();
        HashMap<Integer, Person> persons = new HashMap<>();
        addedRelationships = new HashSet<>();

        // Adding persons to the network
        for (int i = 0; i < 21; i++) {
            MyPerson person = new MyPerson(i, "a", i);
            try {
                network.addPerson(person);
                persons.put(i, person);
            } catch (EqualPersonIdException e) {
                System.out.println("Duplicate person ID: " + person.getId() + ". Ignoring this person.");
            }
        }

        // Adding relationships between persons
        for (int id1 = 0; id1 < 21; id1++) {
            for (int id2 = id1 + 1; id2 < 21; id2++) {
                int value = random.nextInt(25);
                try {
                    if (id1 == id2 || addedRelationships.contains(new Pair<>(id1, id2))) {
                        continue;
                    }
                    addedRelationships.add(new Pair<>(id1, id2));
                    network.addRelation(id1, id2, value);
                } catch (PersonIdNotFoundException | EqualRelationException e) {
                    System.out.println("ERROR");
                }
            }
        }


        for (int i = 0; i < 1001; i++) {
            try {
                network.storeEmojiId(i);
            } catch (Exception e) {
                System.out.println("storeEmojiIError");
            }
        }
        int tagid = 0;
        // Adding emoji messages to the network
        for (int j = 0; j < 96; j = j + 6) {
            Person person1 = persons.get(random.nextInt(21));
            Person person2 = persons.get(random.nextInt(21));
            while (person1.getId() == person2.getId()) {
                person2 = persons.get(random.nextInt(21));
            }
            int emojiId = random.nextInt(1000);
            NoticeMessage message1 = new MyNoticeMessage(j, String.valueOf(emojiId), person1, person2);
            testAddMessage(message1);

            Person person3 = persons.get(random.nextInt(21));
            Person person4 = persons.get(random.nextInt(21));
            while (person3.getId() == person4.getId()) {
                person4 = persons.get(random.nextInt(21));
            }
            int emojiId1 = random.nextInt(1000);
            EmojiMessage message2 = new MyEmojiMessage(j + 1, emojiId1, person3, person4);
            testAddMessage(message2);

            Person person5 = persons.get(random.nextInt(21));
            Person person6 = persons.get(random.nextInt(21));
            while (person5.getId() == person6.getId()) {
                person6 = persons.get(random.nextInt(21));
            }
            int emojiId3 = random.nextInt(1000);
            MyRedEnvelopeMessage message3 = new MyRedEnvelopeMessage(j + 2, emojiId3, person5, person6);
            testAddMessage(message3);

            Person person11 = persons.get(random.nextInt(21));
            int tagId1 = tagid;
            tagid++;
            MyTag tag1;
            try {
                tag1 = new MyTag(tagId1);
                network.addTag(person11.getId(), tag1);
                person11.addTag(tag1);
                for (int i = 0; i < 10; i++) {
                    int id = random.nextInt(20);
                    if (id != person11.getId()) {
                        person11.getTag(tagId1).addPerson(persons.get(id));
                    }
                }
            } catch (PersonIdNotFoundException e) {
                throw new RuntimeException(e);
            } catch (EqualTagIdException e) {
                throw new RuntimeException(e);
            }

            int emojiId4 = random.nextInt(1000);
            MyRedEnvelopeMessage message4 = new MyRedEnvelopeMessage(j + 3, emojiId4, person11, tag1);
            testAddMessage(message4);

            Person person22 = persons.get(random.nextInt(21));
            int tagId2 = tagid;
            tagid++;
            MyTag tag2 = new MyTag(tagId2);
            try {
                network.addTag(person22.getId(), tag2);
                for (int i = 0; i < 10; i++) {
                    int id = random.nextInt(20);
                    if (id != person22.getId()) {
                        person22.getTag(tagId2).addPerson(persons.get(id));
                    }
                }
            } catch (PersonIdNotFoundException | EqualTagIdException e) {
                throw new RuntimeException(e);
            }
            int emojiId5 = random.nextInt(1000);
            MyNoticeMessage message5 = new MyNoticeMessage(j + 4, String.valueOf(emojiId5), person22, tag2);
            testAddMessage(message5);


            Person person33 = persons.get(random.nextInt(21));
            int tagId3 = tagid;
            tagid++;
            MyTag tag3 = new MyTag(tagId3);
            try {
                network.addTag(person33.getId(), tag3);
                for (int i = 0; i < 10; i++) {
                    int id = random.nextInt(20);
                    if (id != person33.getId()) {
                        person33.getTag(tagId3).addPerson(persons.get(id));
                    }
                }
            } catch (PersonIdNotFoundException | EqualTagIdException e) {
                throw new RuntimeException(e);
            }

            int emojiId6 = random.nextInt(1000);
            MyEmojiMessage message6 = new MyEmojiMessage(j + 5, emojiId6, person33, tag3);
            testAddMessage(message6);
        }
    }

    public void testAddMessage(Message message) throws EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException {
        network.addMessage(message);
    }

    public void testSendMessage(int id) throws RelationNotFoundException, TagIdNotFoundException, MessageIdNotFoundException, EmojiIdNotFoundException, EqualPersonIdException, EqualMessageIdException {
        network.sendMessage(id);
    }

    public int actualSum(int limit) throws AcquaintanceNotFoundException, PersonIdNotFoundException {
        int oldEmojiIdLength = network.getEmojiIdList().length;
        int[] oldEmojiIdList = network.getEmojiIdList();
        int oldEmojiHeatLength = network.getEmojiHeatList().length;
        int[] oldEmojiHeatList = network.getEmojiHeatList();
        int oldLength = network.getMessages().length;
        Message[] oldMessages = network.getMessages();

        assertEquals(oldEmojiIdLength, oldEmojiHeatLength);
        ArrayList<Integer> realEmojiIdList = new ArrayList<>();
        ArrayList<Integer> realEmojiHeatList = new ArrayList<>();
        int realLength = 0;

        assertEquals(oldEmojiIdLength, network.getEmojiIdList().length);
        assertEquals(oldEmojiHeatLength, network.getEmojiHeatList().length);

        for (int i = 0; i < oldEmojiHeatList.length; i++) {
            if (oldEmojiHeatList[i] >= limit) {
                realEmojiIdList.add(oldEmojiIdList[i]);
                realEmojiHeatList.add(oldEmojiHeatList[i]);
                realLength++;
            }
        }
        assertEquals(realEmojiIdList.size(), realEmojiHeatList.size());

        int my = network.deleteColdEmoji(limit);
        Message[] newMessages = network.getMessages();
        int newEmojiIdLength = network.getEmojiIdList().length;
        int newEmojiHeatLength = network.getEmojiHeatList().length;

        assertEquals(oldLength - my, newMessages.length);

        assertEquals(realLength, newEmojiIdLength);
        assertEquals(realLength, newEmojiHeatLength);

        for (int i = 0; i < newMessages.length; i++) {
            if (newMessages[i] instanceof EmojiMessage) {
                int emojiId = ((EmojiMessage) newMessages[i]).getEmojiId();
                for (int j = 0; j < oldEmojiIdLength; j++) {
                    if (oldEmojiIdList[j] == emojiId) {
                        assertEquals(oldEmojiHeatList[j] >= limit, true);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < oldLength; i++) {
            Message message = oldMessages[i];
            if (!(message instanceof EmojiMessage)) {
                assertEquals(network.containsMessage(message.getId()), true);
            }  else if((message instanceof EmojiMessage) && ((EmojiMessage) message).getEmojiId()>=limit){
                    assertEquals(network.containsMessage(message.getId()), true);
                }
            }

        return realLength;
    }


    // 清除网络对象
    @After
    public void clear() {
        network = null;
    }

    @Test
    public void test() throws AcquaintanceNotFoundException, PersonIdNotFoundException, RelationNotFoundException, TagIdNotFoundException, EmojiIdNotFoundException, EqualPersonIdException, MessageIdNotFoundException, EqualMessageIdException {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            setup();
            for(int j = 0;j < 20 ;j ++) {
                int group = random.nextInt(95);
                if(network.containsMessage(group)) {
                    testSendMessage(group);
                }
            }
            int limit = random.nextInt(10);
            actualSum(limit);
            clear();
        }
    }
}