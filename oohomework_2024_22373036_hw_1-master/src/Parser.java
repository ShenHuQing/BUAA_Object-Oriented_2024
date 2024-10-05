import java.math.BigInteger;

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
                lexer.next(); //读到数字末尾 跳出
            } else {
                break;
            }

        }
        return term;
    }  //？空指针

    private Factor parseFactor() {
        String currentChar = lexer.peek();
        if (currentChar.contains("(")) {
            lexer.next();
            Factor expr = parseExpr();
            lexer.next();
            return expr;
        } else if (currentChar.contains("x")) {
            Factor variable = new Variable(currentChar);
            lexer.next();
            return variable;
        } else {
            Number num = new Number(new BigInteger(lexer.peek()));
            lexer.next();
            return num;
        }
    }

}
