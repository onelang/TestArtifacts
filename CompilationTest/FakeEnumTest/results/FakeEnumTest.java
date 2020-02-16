class TokenType {
    public static String endToken = "EndToken";
    public static String whitespace = "Whitespace";
    public static String identifier = "Identifier";
    public static String operatorX = "Operator";
    public static String noInitializer;
}

class Program {
    public static void main(String[] args) throws Exception {
        String casingTest = TokenType.endToken;
        System.out.println(casingTest);
    }
}