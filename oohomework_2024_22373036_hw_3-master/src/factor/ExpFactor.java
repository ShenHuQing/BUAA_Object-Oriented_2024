package factor;

import java.math.BigInteger;

public class ExpFactor implements Factor {
    private final String base;
    private final BigInteger exponent;

    public ExpFactor(String base, BigInteger exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    public String toString() {
        return "exp(" + base + ")^" + exponent;
    }
}
