import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        expr.addTerm(parseTerm());
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            expr.addTerm(parseTerm());
        }
        return expr;
    }

    public Term parseTerm() {
        Term term = new Term();
        BigInteger num = new BigInteger("-1");
        Factor negativeOne = new Number(num);
        Factor positiveOne = new Number(BigInteger.ONE);
        while (true) {
            if (lexer.peek().equals("-")) {
                term.addFactor(negativeOne);
                lexer.next();
            } else if (lexer.peek().equals("+")) {
                lexer.next();
            } //先处理正负号，符号处理为-1*
            Factor factor = parseFactor();
            if (lexer.hasNext() && lexer.peek().equals("^")) {  //连乘
                lexer.next();
                int exponent = Integer.parseInt(lexer.peek());
                if (exponent != 0) {
                    for (int i = 0; i < exponent; i++) {
                        term.addFactor(factor);
                    }
                } else {
                    term.addFactor(positiveOne);
                }
                lexer.next();
            } else {
                term.addFactor(factor);
            }
            if (lexer.hasNext() && lexer.peek().equals("*")) {
                lexer.next();
            } else {
                break;
            }

        }
        return term;
    }

    private Factor parseFactor() {
        String currentChar = lexer.peek();
        if (currentChar.equals("(")) { // Check for parentheses
            lexer.next();
            Factor expr = parseExpr();
            lexer.next(); // Consume ")"
            return expr;
        } else if (lexer.peek().equals("-")) {
            lexer.next();
            lexer.next();
            return new Number(new BigInteger("-1"));
        } else if (Character.isDigit(currentChar.charAt(0))) {
            Factor number = new Number(new BigInteger(lexer.peek()));
            lexer.next();
            return number;
        } else if (currentChar.equals("exp")) {
            lexer.next();
            lexer.next();
            Factor base = parseExpr();
            lexer.next();
            if (lexer.hasNext() && Objects.equals(lexer.peek(), "^")) {
                lexer.next();//num
                BigInteger exp = BigInteger.valueOf(Integer.parseInt(lexer.peek()));
                lexer.next();
                ExpFactor expFactor = new ExpFactor(base, exp);
                return expFactor;
            }
            else {
                ExpFactor expFactor = new ExpFactor(base, BigInteger.ONE);
                return expFactor;

            }
        } else {
            Factor variable = new Variable(currentChar);
            lexer.next();
            return variable;
        }
    }

    static int findClosingParenthesis(String str, int start) {
        int depth = 1;
        for (int i = start; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                depth++;
            }
            if (str.charAt(i) == ')') {
                depth--;
            }
            if (depth == 0) {
                return i;
            }

        }
        return -1;
    }

    public static void parseDefFunc(String str, HashMap<String, FuncFactor> funcMap,
                                    StringBuilder finalFunc) {
        int index = 0;
        while (index < str.length()) {
            char currentChar = str.charAt(index);
            if ((currentChar == 'f' || currentChar == 'g' || currentChar == 'h')
                    && index + 1 < str.length() && str.charAt(index + 1) == '(') {
                String funcName = String.valueOf(currentChar);
                int closeIndex = findClosingParenthesis(str, index + 2);
                String innerExpression = str.substring(index + 2, closeIndex);
                ArrayList<String> argsList = splitArguments(innerExpression);
                FuncFactor funcFactor = funcMap.get(funcName);
                String defFuncExpression = funcFactor.getExpression();
                ArrayList<String> parameters = funcFactor.getParameters();
                for (int i = 0; i < parameters.size(); i++) {
                    while (argsList.get(i).contains("f") || argsList.get(i).contains("g")
                            || argsList.get(i).contains("h")) {
                        StringBuilder tempFunc = new StringBuilder();
                        parseDefFunc(argsList.get(i), funcMap, tempFunc);
                        argsList.set(i, tempFunc.toString());
                    }
                    defFuncExpression = defFuncExpression.
                            replace(parameters.get(i), "(" + argsList.get(i) + ")");
                }
                finalFunc.append("(");
                finalFunc.append(defFuncExpression);
                finalFunc.append(")");
                index = closeIndex + 1;
            } else {
                finalFunc.append(currentChar);
                index++;
            }
        }
    }

    // 分割参数列表
    private static ArrayList<String> splitArguments(String arguments) {
        ArrayList<String> result = new ArrayList<>();
        int start = 0;
        int depth = 0;
        for (int i = 0; i < arguments.length(); i++) {
            char c = arguments.charAt(i);
            if (c == '(') {
                depth++;
            }
            else if (c == ')') {
                depth--;
            }
            else if (c == ',' && depth == 0) {
                result.add(arguments.substring(start, i));
                start = i + 1;
            }
        }
        result.add(arguments.substring(start));
        return result;
    }

}
