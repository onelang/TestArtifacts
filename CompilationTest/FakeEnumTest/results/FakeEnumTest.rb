class TokenType
  @end_token = "EndToken"
  @whitespace = "Whitespace"
  @identifier = "Identifier"
  @operator_x = "Operator"
  @no_initializer = nil

  class << self
    attr_accessor :end_token, :whitespace, :identifier, :operator_x, :no_initializer
  end
end

casing_test = TokenType.end_token
puts casing_test