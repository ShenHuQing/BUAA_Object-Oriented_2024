import com.oocourse.spec3.main.NoticeMessage;
import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.Tag;

public class MyNoticeMessage extends MyMessage implements NoticeMessage {
    private final String string;

    public MyNoticeMessage(int id, String string, Person person1, Person person2) {
        super(id, string.length(), person1, person2);
        this.string = string;
    }

    public MyNoticeMessage(int id, String string, Person person1,Tag tag) {
        super(id, string.length(), person1, tag);
        this.string = string;
    }

    public String getString() {
        return string;
    }
}
