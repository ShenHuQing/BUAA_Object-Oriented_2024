import java.util.HashMap;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = Integer.parseInt(scanner.nextLine());
        HashMap<String, FuncFactor> funcMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String funcDefinition = scanner.nextLine();
            StringBuilder sbNext = new StringBuilder();
            sbNext.append(funcDefinition);
            PreProcessr.preProcessor(sbNext);
            FuncFactor.parseDefinition(sbNext.toString(), funcMap);
        }
        String expression = scanner.nextLine();
        StringBuilder sb = new StringBuilder();
        sb.append(expression);
        PreProcessr.preProcessor(sb);
        StringBuilder finalFunc = new StringBuilder();
        Parser.parseDefFunc(sb.toString(),funcMap,finalFunc);
        PreProcessr.preProcessor(finalFunc);
        Lexer lexer = new Lexer(finalFunc.toString());
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        StringBuilder finalTerm = new StringBuilder();
        expr.simplify(expr,finalTerm);
        StringBuilder sb2 = new StringBuilder();
        sb2.append(finalTerm);
        PreProcessr.preProcessor(sb2);
        System.out.println(sb2);


    }
}
