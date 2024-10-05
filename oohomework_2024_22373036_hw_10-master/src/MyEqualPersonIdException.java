import com.oocourse.spec2.exceptions.EqualPersonIdException;

import java.util.HashMap;

public class MyEqualPersonIdException extends EqualPersonIdException {
    private final int id;
    private static int allCount = 0;
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();

    public MyEqualPersonIdException(int id) {
        this.id = id;
        allCount++;
        countMap.put(id, countMap.getOrDefault(id, 0) + 1);
    }

    public void print() {
        System.out.printf("epi-%d, %d-%d%n", allCount, id, countMap.get(id));
    }
}
