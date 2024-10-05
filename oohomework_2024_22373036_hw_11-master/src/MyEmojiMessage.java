import com.oocourse.spec3.main.EmojiMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyEmojiMessage extends MyMessage implements EmojiMessage {
    private final int emojiId;

    public MyEmojiMessage(int id, int socialValue, Person person1, Person person2) {
        super(id, socialValue, person1, person2);
        this.emojiId = socialValue;
    }

    public MyEmojiMessage(int id, int socialValue, Person person, Tag tag) {
        super(id, socialValue, person, tag);
        this.emojiId = socialValue;
    }

    public int getEmojiId() {
        return emojiId;
    }

}
