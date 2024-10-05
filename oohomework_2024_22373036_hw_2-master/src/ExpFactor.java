import java.math.BigInteger;

public class ExpFactor implements Factor {
    private final Factor base;
    private final BigInteger exponent;

    public ExpFactor(Factor base, BigInteger exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    public String toString() {
        return "exp(" + base + ")^" + exponent;
    }
}
