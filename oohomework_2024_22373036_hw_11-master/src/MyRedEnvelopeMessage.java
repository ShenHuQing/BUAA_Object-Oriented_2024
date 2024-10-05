import com.oocourse.spec3.main.Person;
import com.oocourse.spec3.main.RedEnvelopeMessage;
import com.oocourse.spec3.main.Tag;

public class MyRedEnvelopeMessage extends MyMessage implements RedEnvelopeMessage {
    private final int money;

    public MyRedEnvelopeMessage(int id, int money, Person person1,
                                Person person2) {
        super(id, money * 5, person1, person2);
        this.money = money;
    }

    public MyRedEnvelopeMessage(int id, int money, Person person1, Tag tag) {
        super(id, money * 5, person1, tag);
        this.money = money;
    }

    public int getMoney() {
        return money;
    }
}
