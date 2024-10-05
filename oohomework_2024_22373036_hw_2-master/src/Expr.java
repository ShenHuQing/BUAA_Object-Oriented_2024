import java.math.BigInteger;
import java.util.ArrayList;

public class Expr implements Factor {
    private final ArrayList<Term> terms;

    public Expr() {
        this.terms = new ArrayList<>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public void simplify(Expr expr,StringBuilder finalTerm) {
        if (expr == null) {
            System.out.println("DEBUG");
        } else {
            ArrayList<Mono> monos = new ArrayList<>();
            String exprStr = expr.toString();
            //System.out.println("Expr"+exprStr);
            for (String term : splitTerm(exprStr)) {

                monos.add(Mono.toMono(term));
            }
            ArrayList<Mono> mergeMonos = new ArrayList<>();
            mergeSimilarTerms(monos,mergeMonos);
            Mono.printMonos(mergeMonos,finalTerm);
        }
    }

    public void mergeSimilarTerms(ArrayList<Mono> monos,ArrayList<Mono> mergedMonos) {
        for (int i = 0; i < monos.size(); i++) {
            Mono currentMono = monos.get(i);
            boolean merged = false;
            if (!mergedMonos.isEmpty()) {
                for (Mono mergedMono : mergedMonos) {
                    if (Mono.monoEqual(currentMono, mergedMono)) {
                        mergedMono.setCoefficient(mergedMono.getCoefficient().
                                add(currentMono.getCoefficient()));
                        merged = true;
                        break;
                    }
                }
            }
            if (!merged) {
                mergedMonos.add(currentMono);
            }
        }
    }


    public ArrayList<String> splitTerm(String exprStr) {
        ArrayList<String> terms = new ArrayList<>();
        int last = 0;
        int count = 0;
        for (int i = 0; i < exprStr.length(); i++) {
            if (count == 0 && exprStr.charAt(i) == '+' && i > 0) {
                terms.add(exprStr.substring(last, i));
                last = i + 1;
            } else if (exprStr.charAt(i) == '(') {
                count--;
            } else if (exprStr.charAt(i) == ')') {
                count++;
            }
        }
        terms.add(exprStr.substring(last));
        return terms;
    }

    public static ArrayList<String> splitFactor(String s) {
        ArrayList<String> factors = new ArrayList<>();
        int last = 0; // 上一个分隔符的位置
        int count = 0; // 括号计数器，用于跟踪括号的嵌套级别
        // 遍历表达式
        for (int i = 0; i < s.length(); i++) {
            // 如果当前字符是乘号且不是在括号内部，并且不是在表达式的开头
            if (count == 0 && s.charAt(i) == '*' && i > 0) {
                factors.add(s.substring(last, i));
                last = i + 1;
            } else if (s.charAt(i) == '(') { // 如果当前字符是左括号
                count--; // 增加括号计数器，表示进入了更深一层的括号嵌套
            } else if (s.charAt(i) == ')') { // 如果当前字符是右括号
                count++; // 减少括号计数器，表示退出了一层括号嵌套
            }
        }
        factors.add(s.substring(last));
        return factors;
    }

    // 找到匹配的右括号
    static int findClosingParenthesis(String str, int startIndex) {
        int count = 1;
        for (int i = startIndex; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            if (currentChar == '(') {
                count++;
            } else if (currentChar == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        // 如果未找到匹配的右括号，则返回字符串的长度
        return str.length();
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
