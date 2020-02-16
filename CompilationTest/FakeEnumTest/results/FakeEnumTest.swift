class TokenType {
  static var endToken: String = "EndToken"
  static var whitespace: String = "Whitespace"
  static var identifier: String = "Identifier"
  static var operatorX: String = "Operator"
  static var noInitializer: String
}

let casingTest = TokenType.endToken
print(casingTest)