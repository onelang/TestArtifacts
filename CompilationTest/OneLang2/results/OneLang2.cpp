#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>
#include <stdexcept>
#include <string>

enum class TokenKind { Number, Identifier, Operator_, String_ };
const char* TokenKindToStr[] = { "Number", "Identifier", "Operator_", "String_" };

class Token {
  public:
    TokenKind kind;
    std::string value;

    Token(TokenKind kind, std::string value) {
        this->kind = kind;
        this->value = value;
    }

  private:
};

class ExprLangLexer {
  public:
    int offset = 0;
    one::vec<one::sp<Token>> tokens = std::make_shared<std::vector<one::sp<Token>>>(std::initializer_list<one::sp<Token>>{  });
    std::string expression;
    one::vec<std::string> operators;

    ExprLangLexer(std::string expression, one::vec<std::string> operators) {
        this->expression = expression;
        this->operators = operators;
        if (!this->tryToReadNumber()) {
            this->tryToReadOperator();
            this->tryToReadLiteral();
        }
        
        while (this->hasMoreToken()) {
            if (!this->tryToReadOperator()) {
                this->fail(std::string("expected operator here"));
            }
            
            if (!this->tryToReadLiteral()) {
                this->fail(std::string("expected literal here"));
            }
        }
    }

    void fail(std::string message) {
        int end_offset = this->offset + 30;
        if (end_offset > this->expression.size()) {
            end_offset = this->expression.size();
        }
        auto context = this->expression.substr(this->offset, end_offset - this->offset) + std::string("...");
        throw std::runtime_error(std::string("TokenizerException: ") + message + " at '" + context + "' (offset: " + std::to_string(this->offset) + ")");
    }
    
    bool hasMoreToken() {
        this->skipWhitespace();
        return !this->eof();
    }
    
    void add(TokenKind kind, std::string value) {
        this->tokens->push_back(std::make_shared<Token>(kind, value));
        this->offset += value.size();
    }
    
    std::string tryToMatch(std::string pattern) {
        auto matches = OneRegex::matchFromIndex(pattern, this->expression, this->offset);
        return matches == nullptr ? std::string("") : matches->at(0);
    }
    
    bool tryToReadOperator() {
        this->skipWhitespace();
        for (auto it = this->operators->begin(); it != this->operators->end(); ++it) {
            auto op = *it;
            if (this->expression.compare(this->offset, op.size(), op) == 0) {
                this->add(TokenKind::Operator_, op);
                return true;
            }
        }
        return false;
    }
    
    bool tryToReadNumber() {
        this->skipWhitespace();
        
        auto number = this->tryToMatch(std::string("[+-]?(\\d*\\.\\d+|\\d+\\.\\d+|0x[0-9a-fA-F_]+|0b[01_]+|[0-9_]+)"));
        if (number == std::string("")) {
            return false;
        }
        
        this->add(TokenKind::Number, number);
        
        if (this->tryToMatch(std::string("[0-9a-zA-Z]")) != std::string("")) {
            this->fail(std::string("invalid character in number"));
        }
        
        return true;
    }
    
    bool tryToReadIdentifier() {
        this->skipWhitespace();
        auto identifier = this->tryToMatch(std::string("[a-zA-Z_][a-zA-Z0-9_]*"));
        if (identifier == std::string("")) {
            return false;
        }
        
        this->add(TokenKind::Identifier, identifier);
        return true;
    }
    
    bool tryToReadString() {
        this->skipWhitespace();
        
        auto match = this->tryToMatch(std::string("'(\\\\'|[^'])*'"));
        if (match == std::string("")) {
            match = this->tryToMatch(std::string("\"(\\\\\"|[^\"])*\""));
        }
        if (match == std::string("")) {
            return false;
        }
        
        auto str = match.substr(1, 1 + match.size() - 2 - 1);
        str = match[0] == '\'' ? OneStringHelper::replace(str, std::string("\\'"), std::string("'")) : OneStringHelper::replace(str, std::string("\\\""), std::string("\""));
        this->tokens->push_back(std::make_shared<Token>(TokenKind::String_, str));
        this->offset += match.size();
        return true;
    }
    
    bool eof() {
        return this->offset >= this->expression.size();
    }
    
    void skipWhitespace() {
        while (!this->eof()) {
            auto c = this->expression[this->offset];
            if (c == ' ' || c == '\n' || c == '\t' || c == '\r') {
                this->offset++;
            } else {
                break;
            }
        }
    }
    
    bool tryToReadLiteral() {
        auto success = this->tryToReadIdentifier() || this->tryToReadNumber() || this->tryToReadString();
        return success;
    }

  private:
};

class TestClass {
  public:
    void testMethod() {
        auto lexer = std::make_shared<ExprLangLexer>(std::string("1+2"), std::make_shared<std::vector<std::string>>(std::initializer_list<std::string>{ std::string("+") }));
        auto result = std::string("");
        for (auto it = lexer->tokens->begin(); it != lexer->tokens->end(); ++it) {
            auto token = *it;
            if (result != std::string("")) {
                result += std::string(", ");
            }
            result += token->value;
        }
        
        std::cout << std::string("[") + std::to_string(lexer->tokens->size()) + "]: " + result << std::endl;
    }

  private:
};

int main()
{
    try {
        TestClass c;
        c.testMethod();
    } catch(std::exception& err) {
        std::cout << "Exception: " << err.what() << '\n';
    }
    return 0;
}