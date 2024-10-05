package factor;

public class VarFactor implements Factor {
    private final String var;

    public VarFactor(String var) {
        this.var = var;
    }

    public String toString() {
        return var;
    }
}