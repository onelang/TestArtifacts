#include <iostream>

class TestClass {
  public:
    void testMethod() {
        auto str = std::string("a1A");
        for (int i = 0; i < str.size(); i++) {
            auto c = str[i];
            auto is_upper = 'A' <= c && c <= 'Z';
            auto is_lower = 'a' <= c && c <= 'z';
            auto is_number = '0' <= c && c <= '9';
            std::cout << (is_upper ? std::string("upper") : is_lower ? std::string("lower") : is_number ? std::string("number") : std::string("other")) << std::endl;
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