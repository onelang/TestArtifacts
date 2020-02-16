#include <iostream>
#include <string>

class TestClass {
  public:
    void testMethod() {
        auto str_val = std::string("str");
        int num = 1337;
        auto b = true;
        auto result = std::string("before ") + str_val + ", num: " + std::to_string(num) + ", true: " + (b ? "true" : "false") + " after";
        std::cout << result << std::endl;
        std::cout << std::string("before ") + str_val + ", num: " + std::to_string(num) + ", true: " + (b ? "true" : "false") + " after" << std::endl;
        
        auto result2 = std::string("before ") + str_val + std::string(", num: ") + std::to_string(num) + std::string(", true: ") + (b ? "true" : "false") + std::string(" after");
        std::cout << result2 << std::endl;
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