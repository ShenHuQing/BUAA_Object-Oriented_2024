import com.oocourse.spec1.exceptions.PersonIdNotFoundException;

import java.util.HashMap;

public class MyPersonIdNotFoundException extends PersonIdNotFoundException {
    private final int id;
    private static int allCount = 0;
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();

    public MyPersonIdNotFoundException(int id) {
        this.id = id;
        this.allCount++;
        countMap.put(id, countMap.getOrDefault(id, 0) + 1);
    }

    public void print() {
        System.out.printf("pinf-%d, %d-%d%n", allCount, id, countMap.get(id));
    }
}
