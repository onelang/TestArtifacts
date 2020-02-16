class TokenType {
    static endToken: string = "EndToken";
    static whitespace: string = "Whitespace";
    static identifier: string = "Identifier";
    static operatorX: string = "Operator";
    static noInitializer: string;
}

const casingTest = TokenType.endToken;
console.log(casingTest);