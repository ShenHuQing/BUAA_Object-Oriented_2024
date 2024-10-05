import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Expr implements Factor {
    private final ArrayList<Term> terms;

    public Expr() {
        this.terms = new ArrayList<>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public void simplify(Expr expr) {
        HashMap<BigInteger, BigInteger> map = new HashMap<>();
        if (expr == null) {
            System.out.println("DEBUG");
        }
        else {
            String exprStr = expr.toString();
            String[] terms = exprStr.split("\\+");
            for (String term : terms) {
                sum_Term(term,map);
            }
        }

        printPolynomial(map);
    }

    private void sum_Term(String term, HashMap<BigInteger, BigInteger> map) {
        ArrayList<String> factors = new ArrayList<>(Arrays.asList(term.split("\\*")));
        BigInteger coefficient = new BigInteger("1");
        BigInteger exp = new BigInteger("0");
        for (String factor : factors) {
            if (factor.contains("x")) {
                if (factor.equals("x")) {
                    exp = exp.add(new BigInteger("1"));
                } else if (factor.matches("x\\^(\\d+)")) {
                    exp = exp.add(new BigInteger(factor.split("\\^")[1]));
                }
            } else {
                if (factor.startsWith("-")) {
                    coefficient = coefficient.multiply(new BigInteger("-1"));
                    factor = factor.substring(1); // 移除负号
                }
                coefficient = coefficient.multiply(new BigInteger(factor));
            }
        }

        // 使用计算得到的项更新 map
        if (map.containsKey(exp)) {
            BigInteger value = map.get(exp);
            map.put(exp, value.add(coefficient));
        } else {
            map.put(exp, coefficient);
        }
    }

    public void printPolynomial(HashMap<BigInteger, BigInteger> map) {
        StringBuilder polynomial = new StringBuilder();
        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            BigInteger exp = entry.getKey();
            BigInteger coefficient = entry.getValue();
            if (coefficient.compareTo(BigInteger.ZERO) != 0) {
                if (polynomial.length() > 0) {
                    if (coefficient.compareTo(BigInteger.ZERO) > 0) {
                        polynomial.append("+");
                    } else {
                        polynomial.append("-");
                        coefficient = coefficient.abs();
                    }
                }
                if (!exp.equals(BigInteger.ZERO)) {
                    if (!coefficient.equals(BigInteger.ONE)) {
                        polynomial.append(coefficient);
                        polynomial.append("*");
                    }
                    if (!exp.equals(BigInteger.ONE)) {
                        polynomial.append("x^").append(exp);
                    } else {
                        polynomial.append("x");
                    }
                } else {
                    polynomial.append(coefficient); // 添加常数项
                }
            }
        }
        if (polynomial.length() ==  0) {
            polynomial.append("0");
        }
        System.out.println(polynomial.toString());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!terms.isEmpty()) {
            for (int i = 0; i < terms.size(); i++) {
                if (i > 0) {
                    sb.append("+");
                }
                sb.append(terms.get(i));
            }
        }
        return sb.toString();
    }
}
