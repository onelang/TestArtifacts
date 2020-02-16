#include <iostream>
#include <string>

class TestClass {
  public:
    std::string reverseString(std::string str) {
        auto result = std::string("");
        for (int i = str.size() - 1; i >= 0; i--) {
            result += str[i];
        }
        return result;
    }
    
    std::string testMethod() {
        std::cout << this->reverseString(std::string("print value")) << std::endl;
        return std::string("return value");
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