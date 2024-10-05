import java.util.ArrayList;

public class Term implements Factor {
    private final ArrayList<Factor> factors;

    public Term() {
        this.factors = new ArrayList<>();
    }

    public static boolean isExpression(String str) {
        boolean flag = false;
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (count == 0 && str.charAt(i) == '+' && i > 0) {
                flag = true;
                return flag;
            } else if (str.charAt(i) == '(') {
                count--;
            } else if (str.charAt(i) == ')') {
                count++;
            }
        }
        return flag;
    }

    public void addExprFactor(Factor factor,ArrayList<Factor> tempFactors,
                              ArrayList<Term> tempTerms,ArrayList<Factor> factors) {
        if (factors.isEmpty()) {
            for (int i = 0; i < tempTerms.size(); i++) {
                tempFactors.add(tempTerms.get(i));
                if (i != tempTerms.size() - 1) {
                    tempFactors.add(new Variable("+"));
                }
            }
        } else {
            for (int i = 0; i < tempTerms.size(); i++) {
                int flag = 0;
                for (Factor symbol : factors) {
                    boolean isplus = symbol.toString().equals("+");
                    if (!isplus) {
                        if (flag == 0) {
                            tempFactors.add(tempTerms.get(i));
                            flag = 1;
                        }
                        tempFactors.add(symbol);
                    } else {
                        flag = 0;
                        tempFactors.add(new Variable("+"));
                    }
                }
                if (i < tempTerms.size() - 1) {
                    tempFactors.add(new Variable("+"));
                }
            }
        }
    }

    public void addNormalFactor(Factor factor,
                                ArrayList<Factor> tempFactors, ArrayList<Term> tempTerms,
                                ArrayList<Factor> factors,Factor add)  {
        if (!factors.isEmpty()) {
            int flag = 0;
            for (Factor value : factors) {
                if (!value.toString().equals("+")) {
                    if (flag == 0) {
                        tempFactors.add(factor);
                        flag = 1;
                    }
                    tempFactors.add(value);
                } else {
                    flag = 0;
                    tempFactors.add(add);
                }
            }
        } else {
            tempFactors.add(factor);
        }
    }

    public void addFactor(Factor factor) {
        Factor add = new Variable("+");
        ArrayList<Factor> tempFactors = new ArrayList<>();
        ArrayList<Term> tempTerms = new ArrayList<>();
        if (isExpression(factor.toString())) { //为表达式
            tempTerms = getNewTerms(factor);
            addExprFactor(factor, tempFactors,tempTerms,factors);
        } else {
            addNormalFactor(factor,tempFactors,
                    tempTerms,factors, add);
        }
        factors.clear();
        factors.addAll(tempFactors);
    }

    public ArrayList<Term> getNewTerms(Factor factor) {
        ArrayList<Term> term = new ArrayList<>();
        Lexer lexer = new Lexer(factor.toString());
        Parser parser = new Parser(lexer);
        term.add(parser.parseTerm());
        while (lexer.peek().equals("+")) {
            lexer.next();
            term.add(parser.parseTerm());
        }
        return term;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int flag1 = 0;
        if (!factors.isEmpty()) {
            for (int i = 0; i < factors.size(); i++) {
                if (factors.get(i).toString().equals("+")) {
                    sb.append("+");
                    flag1 = 1;
                } else {
                    if (i > 0 && flag1 == 0) {
                        sb.append("*");
                    }
                    sb.append(factors.get(i).toString());
                    flag1 = 0;
                }
            }
        }
        return sb.toString();
    }
}
