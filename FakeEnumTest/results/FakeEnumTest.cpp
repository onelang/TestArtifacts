#include <one.hpp>
class TokenType {
  public:
    static std::string end_token;
    static std::string whitespace;
    static std::string identifier;
    static std::string operator_x;
    static std::string no_initializer;

  private:
};

std::string TokenType::end_token = std::string("EndToken");
std::string TokenType::whitespace = std::string("Whitespace");
std::string TokenType::identifier = std::string("Identifier");
std::string TokenType::operator_x = std::string("Operator");

class TestClass {
  public:
    std::string testMethod() {
        auto casing_test = TokenType::end_token;
        return casing_test;
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