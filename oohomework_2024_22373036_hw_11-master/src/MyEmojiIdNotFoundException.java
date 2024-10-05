import com.oocourse.spec3.exceptions.EmojiIdNotFoundException;

import java.util.HashMap;

public class MyEmojiIdNotFoundException extends EmojiIdNotFoundException {
    private final int id;
    private static int allCount = 0;
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();

    public MyEmojiIdNotFoundException(int id) {
        this.id = id;
        allCount++;
        countMap.put(id, countMap.getOrDefault(id, 0) + 1);
    }

    public void print() {
        System.out.printf("einf-%d, %d-%d%n", allCount, id, countMap.get(id));
    }
}