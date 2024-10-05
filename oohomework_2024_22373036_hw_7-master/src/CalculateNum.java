public class CalculateNum {
    private static int num;

    public CalculateNum() {
        num = 0;
    }

    public static synchronized void addNum() {
        num++;
    }

    public static synchronized void deleteNum() {
        num--;
    }

    public static synchronized boolean isEnd() {
        return num == 0;
    }

    public static int getCount() {
        return num;
    }
}
