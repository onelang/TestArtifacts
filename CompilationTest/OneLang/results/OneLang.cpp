#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>

class TokenType {
  public:
    static std::string end_token;
    static std::string whitespace;
    static std::string identifier;
    static std::string operator_x;

  private:
};

std::string TokenType::end_token = std::string("EndToken");
std::string TokenType::whitespace = std::string("Whitespace");
std::string TokenType::identifier = std::string("Identifier");
std::string TokenType::operator_x = std::string("Operator");

class Token {
  public:
    std::string value;
    bool is_operator;

    Token(std::string value, bool is_operator) {
        this->value = value;
        this->is_operator = is_operator;
    }

  private:
};

class StringHelper {
  public:
    static bool startsWithAtIndex(std::string str, std::string substr, int idx) {
        return str.substr(idx, idx + substr.size() - idx) == substr;
    }

  private:
};

class Tokenizer {
  public:
    int offset;
    std::string text;
    one::vec<std::string> operators;

    Tokenizer(std::string text, one::vec<std::string> operators) {
        this->text = text;
        this->operators = operators;
        this->offset = 0;
    }

    std::string getTokenType() {
        if (this->offset >= this->text.size()) {
            return TokenType::end_token;
        }
        
        auto c = this->text[this->offset];
        return c == ' ' || c == '\n' || c == '\t' || c == '\r' ? TokenType::whitespace : ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9') || c == '_' ? TokenType::identifier : TokenType::operator_x;
    }
    
    one::vec<one::sp<Token>> tokenize() {
        auto result = std::make_shared<std::vector<one::sp<Token>>>(std::initializer_list<one::sp<Token>>{  });
        
        while (this->offset < this->text.size()) {
            auto char_type = this->getTokenType();
            
            if (char_type == TokenType::whitespace) {
                while (this->getTokenType() == TokenType::whitespace) {
                    this->offset++;
                }
            } else if (char_type == TokenType::identifier) {
                int start_offset = this->offset;
                while (this->getTokenType() == TokenType::identifier) {
                    this->offset++;
                }
                auto identifier = this->text.substr(start_offset, this->offset - start_offset);
                result->push_back(std::make_shared<Token>(identifier, false));
            } else {
                auto op = std::string("");
                for (auto it = this->operators->begin(); it != this->operators->end(); ++it) {
                    auto curr_op = *it;
                    if (StringHelper::startsWithAtIndex(this->text, curr_op, this->offset)) {
                        op = curr_op;
                        break;
                    }
                }
                
                if (op == std::string("")) {
                    break;
                }
                
                this->offset += op.size();
                result->push_back(std::make_shared<Token>(op, true));
            }
        }
        
        return result;
    }

  private:
};

class TestClass {
  public:
    void testMethod() {
        auto operators = std::make_shared<std::vector<std::string>>(std::initializer_list<std::string>{ std::string("<<"), std::string(">>"), std::string("++"), std::string("--"), std::string("=="), std::string("!="), std::string("!"), std::string("<"), std::string(">"), std::string("="), std::string("("), std::string(")"), std::string("["), std::string("]"), std::string("{"), std::string("}"), std::string(";"), std::string("+"), std::string("-"), std::string("*"), std::string("/"), std::string("&&"), std::string("&"), std::string("%"), std::string("||"), std::string("|"), std::string("^"), std::string(","), std::string(".") });
        
        auto input = std::string("hello * 5");
        auto tokenizer = std::make_shared<Tokenizer>(input, operators);
        auto result = tokenizer->tokenize();
        
        std::cout << std::string("token count:") << std::endl;
        std::cout << result->size() << std::endl;
        for (auto it = result->begin(); it != result->end(); ++it) {
            auto item = *it;
            std::cout << (item->value + std::string("(") + (item->is_operator ? std::string("op") : std::string("id")) + std::string(")")) << std::endl;
        }
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