public class PreProcessr {
    public static void preProcessor(StringBuilder finalFunc) {
        String finalFuncStr = finalFunc.toString();
        String finalFuncStrRep = finalFuncStr.replaceAll("[ \t]", "");
        String finalFuncStrRepNext = deleteRedundantSymbols(finalFuncStrRep);
        StringBuilder inputDel = deletePlusAfterLp(finalFuncStrRepNext);
        String express = inputDel.toString();
        String expression = deleteRedundantSymbols(express);
        finalFunc.setLength(0);
        finalFunc.append(expression);
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
