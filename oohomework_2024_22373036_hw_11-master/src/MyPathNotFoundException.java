import com.oocourse.spec3.exceptions.PathNotFoundException;

import java.util.HashMap;

public class MyPathNotFoundException extends PathNotFoundException {
    private final int id1;
    private final int id2;
    private static int allCount = 0;
    private static final HashMap<Integer, Integer> countMap = new HashMap<>();

    public MyPathNotFoundException(int id1, int id2) {
        this.id1 = id1;
        this.id2 = id2;
        allCount++; // 总次数加1
        countMap.put(id1, countMap.getOrDefault(id1, 0) + 1);
        if (id1 != id2) {
            countMap.put(id2, countMap.getOrDefault(id2, 0) + 1);
        }
    }

    public void print() {
        if (id1 <= id2) {
            System.out.printf("pnf-%d, %d-%d, %d-%d%n", allCount, id1,
                    countMap.get(id1), id2, countMap.get(id2));
        } else {
            System.out.printf("pnf-%d, %d-%d, %d-%d%n", allCount, id2,
                    countMap.get(id2), id1, countMap.get(id1));
        }

    }
}
