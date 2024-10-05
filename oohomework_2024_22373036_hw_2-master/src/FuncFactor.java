import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuncFactor implements Factor {
    private String name;
    private ArrayList<String> parameters;
    private int count = 0;
    private String expression;

    public FuncFactor(String name, ArrayList<String> parameters, int count, String expression) {
        this.name = name;
        this.parameters = parameters;
        this.count = count;
        this.expression = expression;
    }

    protected static FuncFactor parseDefinition(String definition,
                                                HashMap<String, FuncFactor> funcMap) {
        // 正则表达式匹配函数定义的模式
        String defRegex = "([fgh])\\(((x|y|z)((?:,(?:x|y|z))*))\\)=(.+)";
        Pattern defPattern = Pattern.compile(defRegex);
        Matcher defMatcher = defPattern.matcher(definition);
        if (defMatcher.find()) {
            ArrayList<String> parameters = new ArrayList<>();
            int count = 0;
            String params = defMatcher.group(2);
            for (String param : params.split(",")) {
                String replacedParam = param.trim();
                if (replacedParam.equals("x")) {
                    replacedParam = "a";
                } else if (replacedParam.equals("y")) {
                    replacedParam = "b";
                } else if (replacedParam.equals("z")) {
                    replacedParam = "c";
                }
                parameters.add(replacedParam);
                count++;
            }
            String name = defMatcher.group(1);
            String expression = defMatcher.group(5); // 函数表达式
            expression = expression.replace("x", "a").replace("y", "b").replace("z", "c");
            expression = expression.replace("eap","exp");
            FuncFactor func = new FuncFactor(name, parameters, count, expression);
            funcMap.put(name, func);
            return func;
        } else {
            throw new IllegalArgumentException("Invalid function definition: " + definition);
        }
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public ArrayList<String> getParameters() {
        return parameters;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return String.format("%s(%s) = %s", name, String.join(", ", parameters), expression);
    }

}
