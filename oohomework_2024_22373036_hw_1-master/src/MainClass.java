import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String inputRep = input.replaceAll("[ \t]", "");
        String inputRepNext = deleteRedundantSymbols(inputRep);
        StringBuilder inputDel = deletePlusAfterLp(inputRepNext);
        String express = inputDel.toString();
        String expression = deleteRedundantSymbols(express);
        Lexer lexer = new Lexer(expression);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        expr.simplify(expr);
    }

    public static StringBuilder deletePlusAfterLp(String inputRep) {
        StringBuilder inputDel = new StringBuilder();
        for (int i = 0; i < inputRep.length(); i++) {
            if (inputRep.charAt(i) == '(' && inputRep.charAt(i + 1) == '+') {
                inputDel.append(inputRep.charAt(i));
                i = i + 1;
            } else {
                inputDel.append(inputRep.charAt(i));
            }
        }
        return inputDel;
    }

    public static String deleteRedundantSymbols(String expr) {
        String result = expr.replaceAll("(\\+\\+)|(--)", "+");
        result = result.replaceAll("(-\\+)|(\\+-)", "-");
        result = result.replaceAll("\\^\\+", "^");
        result = result.replaceAll("\\*\\+", "*");
        result = result.replaceAll("^\\+", "");
        result = result.replaceAll("(\\+\\+)|(--)", "+");
        result = result.replaceAll("(-\\+)|(\\+-)", "-");
        result = result.replaceAll("^\\+", "");
        return result;
    }
}
