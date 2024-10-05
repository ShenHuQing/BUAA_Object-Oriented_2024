public class Lexer {
    private final String input;
    private int pos = 0;

    public String getInput() {
        return input;
    }

    private String curToken;

    public Lexer(String input) {
        this.input = input;
        this.next();
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = getNumber();
        } else if (c == '+' || c == '-' || c == '*' || c == '(' || c == ')') {
            pos += 1;
            curToken = String.valueOf(c);
        } else if (c == '^' || c == 'x' || c == 'y' || c == 'z') {
            pos += 1;
            curToken = String.valueOf(c);
        } else if (pos + 3 < input.length() && input.substring(pos, pos + 4).equals("exp(")) {
            pos += 3;
            curToken = "exp";
            //pos到了第一个左括号
        }
    }

    public String peek() {
        return this.curToken;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getAfter() {
        return input.substring(pos + 1, input.length());
    }

    public boolean hasNext() {
        return pos < input.length();
    }

    public String getBase(int a,int b) {
        return input.substring(a,b);
    }

}
