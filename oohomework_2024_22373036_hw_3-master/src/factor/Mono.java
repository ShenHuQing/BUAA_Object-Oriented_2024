package factor;

import java.math.BigInteger;
import java.util.ArrayList;
import main.PreProcessor;
import parser.Lexer;
import parser.Parser;

public class Mono extends Term {
    private BigInteger coefficient;
    private BigInteger powerExponentX;
    private BigInteger powerExponentY;
    private BigInteger powerExponentZ;
    private String exponent;

    public Mono(BigInteger coefficient, BigInteger powerExponentX,
                BigInteger powerExponentY, BigInteger powerExponentZ, String exponent) {
        this.coefficient = coefficient;
        this.powerExponentX = powerExponentX;
        this.powerExponentY = powerExponentY;
        this.powerExponentZ = powerExponentZ;
        this.exponent = exponent;
    }

    public static boolean monoEqual(Mono mono1, Mono mono2) {
        return mono1.powerExponentX.equals(mono2.powerExponentX) &&
                mono1.powerExponentY.equals(mono2.powerExponentY) &&
                mono1.powerExponentZ.equals(mono2.powerExponentZ) &&
                mono1.exponent.equals(mono2.exponent);
    }

    public String toString() {
        StringBuilder termBuilder = new StringBuilder();
        termBuilder.append(coefficient).append("*x^").append(powerExponentX)
                .append("*y^").append(powerExponentY).append("*z^").append(powerExponentZ)
                .append("*exp(").append(exponent).append(")");
        return termBuilder.toString();
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public BigInteger getPowerExponentX() {
        return powerExponentX;
    }

    public BigInteger getPowerExponentY() {
        return powerExponentY;
    }

    public BigInteger getPowerExponentZ() {
        return powerExponentZ;
    }

    public void setCoefficient(BigInteger coefficient) {
        this.coefficient = coefficient;
    }

    public void setPowerExponentX(BigInteger powerExponentX) {
        this.powerExponentX = powerExponentX;
    }

    public void setPowerExponentY(BigInteger powerExponentY) {
        this.powerExponentY = powerExponentY;
    }

    public void setPowerExponentZ(BigInteger powerExponentZ) {
        this.powerExponentZ = powerExponentZ;
    }

    public String getExponent() {
        return exponent;
    }

    public void setExponent(String exponent) {
        this.exponent = exponent;
    }

    public static Mono toMono(String term) {
        BigInteger coefficient = BigInteger.ONE;
        BigInteger powerExponentX = BigInteger.ZERO;
        BigInteger powerExponentY = BigInteger.ZERO;
        BigInteger powerExponentZ = BigInteger.ZERO;
        StringBuilder exponent = new StringBuilder();
        for (String factor : Expr.splitFactor(term)) {
            if (factor.startsWith("exp")) {
                String str = factor.toString();//exp()^5
                int closeIndex = Expr.findClosingParenthesis(str,4);
                String expContent = str.substring(4, closeIndex);

                String expPower;
                int index = closeIndex + 1;
                if (index < str.length() && str.charAt(index) == '^') {
                    expPower = str.substring(index + 1,str.length());
                } else {
                    expPower = "1";
                }
                exponent.append("(").append(expContent).append(")")
                        .append("*").append(expPower).append("+");
            }
            else if (factor.contains("x")) {
                if (factor.equals("x")) {
                    powerExponentX = powerExponentX.add(new BigInteger("1"));
                } else if (factor.matches("x\\^(\\d+)")) {
                    powerExponentX = powerExponentX.add(new BigInteger(factor.split("\\^")[1]));
                }
            }  else if (factor.contains("y")) {
                if (factor.equals("y")) {
                    powerExponentY = powerExponentY.add(new BigInteger("1"));
                } else if (factor.matches("y\\^(\\d+)")) {
                    powerExponentY = powerExponentY.add(new BigInteger(factor.split("\\^")[1]));
                }
            } else if (factor.contains("z")) {
                if (factor.equals("z")) {
                    powerExponentZ = powerExponentZ.add(new BigInteger("1"));
                } else if (factor.matches("z\\^(\\d+)")) {
                    powerExponentZ = powerExponentZ.add(new BigInteger(factor.split("\\^")[1]));
                }
            } else {
                if (factor.startsWith("-")) {
                    coefficient = coefficient.multiply(new BigInteger("-1"));
                    factor = factor.substring(1); // 移除负号

                }
                coefficient = coefficient.multiply(new BigInteger(factor));
            }
        }
        StringBuilder termTerm = new StringBuilder();//termTerm 展开后的exp
        simplyExp(termTerm,coefficient,exponent);
        Mono mono = new Mono(coefficient,powerExponentX,powerExponentY,
                powerExponentZ,termTerm.toString());
        return mono;
    }

    public static void simplyExp(StringBuilder termTerm, BigInteger coefficient,
                                 StringBuilder exponent) {
        if (!coefficient.equals(BigInteger.ZERO)) {
            if (exponent.length() != 0) {
                exponent.deleteCharAt(exponent.length() - 1);
                Lexer lexer = new Lexer(exponent.toString());
                Parser parser = new Parser(lexer);
                Expr expr = parser.parseExpr();
                expr.simplify(expr,termTerm);
                PreProcessor.preProcessor(termTerm);
                if (termTerm.length() == 0) {
                    termTerm.append("0");
                }
            } else {
                termTerm.append("0");
            }
        }
    }

    public static void printMonos(ArrayList<Mono> monos, StringBuilder finalTerm) {
        for (Mono mono : monos) {
            Boolean previousTermExists = false;
            StringBuilder termBuilder = new StringBuilder();
            BigInteger coefficient = mono.getCoefficient();//系数判断
            if (!coefficient.equals(BigInteger.ZERO)) {
                if (coefficient.compareTo(BigInteger.ZERO) > 0) {
                    termBuilder.append("+");
                } else {
                    termBuilder.append("-");
                    coefficient = coefficient.abs();
                } if (!coefficient.equals(BigInteger.ONE)) {
                    termBuilder.append(coefficient);
                    previousTermExists = true;
                } String exponent = mono.getExponent();
                if (!exponent.equals("0")) { //
                    if (previousTermExists) {
                        termBuilder.append("*");
                    } if (exponent.length() == 1) {
                        termBuilder.append("exp(").append(exponent).append(")");
                    } else {
                        termBuilder.append("exp((").append(exponent).append("))");
                    } previousTermExists = true;
                }
                if (!mono.getPowerExponentX().equals(BigInteger.ZERO)) {
                    if (previousTermExists) {
                        termBuilder.append("*");
                    } termBuilder.append("x");
                    if (!mono.getPowerExponentX().equals(BigInteger.ONE)) {
                        termBuilder.append("^").append(mono.getPowerExponentX());
                    } previousTermExists = true;
                }
                if (!mono.getPowerExponentY().equals(BigInteger.ZERO)) {
                    if (previousTermExists) {
                        termBuilder.append("*");
                    } termBuilder.append("y");
                    if (!mono.getPowerExponentY().equals(BigInteger.ONE)) {
                        termBuilder.append("^").append(mono.getPowerExponentY());
                    } previousTermExists = true;
                }
                if (!mono.getPowerExponentZ().equals(BigInteger.ZERO)) {
                    if (previousTermExists) {
                        termBuilder.append("*");
                    } termBuilder.append("z");
                    if (!mono.getPowerExponentZ().equals(BigInteger.ONE)) {
                        termBuilder.append("^").append(mono.getPowerExponentZ());
                    } previousTermExists = true;
                } if (coefficient.equals(BigInteger.ONE) && (!previousTermExists)) {
                    termBuilder.append(coefficient);
                }
            } if (termBuilder.length() > 0) {
                finalTerm.append(termBuilder.toString() + "+");
            }
        }
        if (finalTerm.length() == 0) {
            finalTerm.append("0");
        } else {
            finalTerm.deleteCharAt(finalTerm.length() - 1);
        }

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
}
