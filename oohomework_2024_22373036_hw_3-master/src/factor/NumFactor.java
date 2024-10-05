package factor;

import java.math.BigInteger;

public class NumFactor implements Factor {
    private final BigInteger num;

    public NumFactor(BigInteger num) {
        this.num = num;
    }

    public String toString() {
        return this.num.toString();
    }
}