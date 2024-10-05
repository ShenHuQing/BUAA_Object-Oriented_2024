import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.Message;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.RedEnvelopeMessage;
import com.oocourse.spec3.main.Tag;

public class MyMessage implements Message {
    private int id;
    private int socialValue;
    private int type;
    private Person person1;
    private Person person2;
    private Tag tag;

    // 构造函数
    public MyMessage(int id, int socialValue, Person person1, Person person2) {
        this.id = id;
        this.socialValue = socialValue;
        this.type = 0;
        this.person1 = person1;
        this.person2 = person2;
        this.tag = null;
    }

    public MyMessage(int id, int socialValue, Person person, Tag tag) {
        this.id = id;
        this.socialValue = socialValue;
        this.type = 1;
        this.person1 = person;
        this.person2 = null;
        this.tag = tag;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getSocialValue() {
        return socialValue;
    }

    @Override
    public Person getPerson1() {
        return person1;
    }

    @Override
    public Person getPerson2() {
        return person2;
    }

    @Override
    public Tag getTag() {
        return tag;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Message)) {
            return false;
        } else {
            return ((Tag) obj).getId() == id;
        }
    }

    public void sendMessageForOne() {
        Person person1 = getPerson1();
        Person person2 = getPerson2();
        person1.addSocialValue(getSocialValue());
        person2.addSocialValue(getSocialValue());
        if (this instanceof RedEnvelopeMessage) {
            int money = ((RedEnvelopeMessage) this).getMoney();
            person1.addMoney(-money);
            person2.addMoney(money);
        } else if (this instanceof EmojiMessage) {
            int emojiId = (((EmojiMessage) this).getEmojiId());
            if (MyNetwork.MygetEmojiHeatList().containsKey(emojiId)) {
                MyNetwork.MygetEmojiHeatList().put(emojiId,
                        MyNetwork.MygetEmojiHeatList().get(emojiId) + 1);
            } else {
                MyNetwork.MygetEmojiHeatList().put(emojiId,1);
            }
        } person2.getMessages().add(0, this);
    }

    public void sendMessageForTag() {
        Person person1 = this.getPerson1();
        person1.addSocialValue(this.getSocialValue());
        MyTag tag = ((MyTag)this.getTag());
        for (Person person: tag.getPersons().values()) {
            person.addSocialValue(this.getSocialValue());
        }
        if (this instanceof RedEnvelopeMessage && this.getTag().getSize() > 0) {
            int money = ((RedEnvelopeMessage) this).getMoney() / this.getTag().getSize();
            for (Person person2 : tag.getPersons().values()) {
                person1.addMoney(-money);
                person2.addMoney(money);
            }
        } else if (this instanceof EmojiMessage) {
            int emojiId = (((EmojiMessage) this).getEmojiId());
            if (MyNetwork.MygetEmojiHeatList().containsKey(emojiId)) {
                MyNetwork.MygetEmojiHeatList().put(emojiId,
                        MyNetwork.MygetEmojiHeatList().get(emojiId) + 1);
            } else {
                MyNetwork.MygetEmojiHeatList().put(emojiId,1);
            }
        }
    }

}
